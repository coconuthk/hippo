package uk.nhs.digital.common.components;

import static org.hippoecm.hst.content.beans.query.builder.ConstraintBuilder.and;
import static org.hippoecm.hst.content.beans.query.builder.ConstraintBuilder.constraint;
import static org.hippoecm.hst.content.beans.query.builder.ConstraintBuilder.or;
import static java.util.stream.Collectors.toList;

import org.hippoecm.hst.content.beans.query.HstQuery;
import org.hippoecm.hst.content.beans.query.builder.Constraint;
import org.hippoecm.hst.content.beans.query.builder.HstQueryBuilder;
import org.hippoecm.hst.content.beans.standard.*;
import org.hippoecm.hst.core.component.HstRequest;
import org.hippoecm.hst.core.component.HstResponse;
import org.hippoecm.hst.core.parameters.ParametersInfo;
import org.hippoecm.hst.util.ContentBeanUtils;
import org.hippoecm.hst.util.SearchInputParsingUtils;
import org.onehippo.cms7.essentials.components.CommonComponent;
import org.onehippo.cms7.essentials.components.info.EssentialsListComponentInfo;
import org.onehippo.cms7.essentials.components.paging.Pageable;
import uk.nhs.digital.nil.beans.Indicator;
import uk.nhs.digital.ps.beans.Archive;
import uk.nhs.digital.ps.beans.Dataset;
import uk.nhs.digital.ps.beans.LegacyPublication;
import uk.nhs.digital.ps.beans.Publication;
import uk.nhs.digital.ps.beans.Series;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * We are not extending "EssentialsSearchComponent" because we could not find a elegant way of using our own search
 * HstObject in the faceted search.
 */
@ParametersInfo(type = EssentialsListComponentInfo.class)
public class SearchComponent extends CommonComponent {

    private static final String WILDCARD_IN_USE_CHAR = "*";
    private static final int WILDCARD_POSTFIX_MIN_LENGTH = 3;

    private static final String REQUEST_PARAM_SORT = "sort";
    private static final String SORT_RELEVANCE = "relevance";
    private static final String SORT_DATE = "date";
    private static final String SORT_DEFAULT = SORT_RELEVANCE;

    @Override
    public void doBeforeRender(HstRequest request, HstResponse response) {
        EssentialsListComponentInfo paramInfo = getComponentInfo(request);
        HippoFacetNavigationBean facetNavigationBean = getFacetNavigationBean(request);

        if (facetNavigationBean != null) {
            HippoResultSetBean resultSet = facetNavigationBean.getResultSet();
            Pageable<HippoBean> pageable = getPageableFactory()
                .createPageable(
                    resultSet.getDocumentIterator(HippoBean.class),
                    facetNavigationBean.getCount().intValue(),
                    paramInfo.getPageSize(),
                    getCurrentPage(request)
                );

            request.setAttribute("pageable", pageable);
        } else {

            // No results, so parse URL to get selected facets

            // sample url is: /site/search/information-type/Official+statistics/year/2017
            String url = request.getRequestURI();

            // sample extractedFacets: information-type, Official+statistics, year, 2017
            Object[] extractedFacets  = Arrays.stream(url.split("/"))
                .filter(o -> !o.equals("") && !o.equals("site") && !o.equals("search"))
                .toArray();

            // (ONLY if facets exist)
            if (extractedFacets.length > 0) {

                Map<String, String> facetsAndValues = new HashMap<>();

                for (int i = 0; i < extractedFacets.length; i += 2) {
                    facetsAndValues.put(extractedFacets[i].toString(), extractedFacets[i + 1].toString());
                }


                // WIP.......



            }
        }

        request.setAttribute("query", getQueryParameter(request));
        request.setAttribute("facets", facetNavigationBean);
        request.setAttribute("sort", getSortOption(request));
        request.setAttribute("cparam", paramInfo);
    }

    protected HippoFacetNavigationBean getFacetNavigationBean(HstRequest request) {
        return ContentBeanUtils.getFacetNavigationBean(buildQuery(request));
    }

    protected String getQueryParameter(HstRequest request) {
        return getAnyParameter(request, REQUEST_PARAM_QUERY);
    }

    /**
     * Convert query to include wildcard (*) character after each search term.
     * If the term is a phrase, the search is for an exact match, so no wildcard is required,
     * but the escaped backslash must be removed for the results to be accurate.
     * If the term ends with fullstop or comma, do not apply wildcard
     * <p>
     * e.g. lorem ipsum                 =>      lorem* ipsum*
     * "dolor sit" lorem ipsum     =>      "dolor sit" lorem* ipsum*
     * lor ipsum                   =>      lor* ipsum*
     */
    protected String parseAndApplyWildcards(String query) {

        ArrayList<String> terms = new ArrayList<>();

        // Split terms by space, treat phrases (wrapped in double quotes) as a single term
        Matcher matcher = Pattern.compile("(\"[^\"]*\")|([^\\s\"]+)").matcher(query);
        while (matcher.find()) {
            String term = null;
            if (matcher.group(1) != null) {
                // phrase that was quoted so add without wild card
                term = matcher.group(1);
            } else if (matcher.group(2) != null) {
                term = matcher.group(2);

                if (term.length() >= WILDCARD_POSTFIX_MIN_LENGTH
                    && !term.endsWith(".")
                    && !term.endsWith(",")) {
                    // single unquoted word so add wildcard
                    term = matcher.group(2) + WILDCARD_IN_USE_CHAR;
                }
            }

            terms.add(term);
        }

        query = String.join(" ", terms);

        // Escape any special chars but not the quotes, we have already dealt with those
        // Do this last to properly sanitize any input
        return SearchInputParsingUtils.parse(query, true, new char[]{'"'});
    }


    protected int getCurrentPage(HstRequest request) {
        return getAnyIntParameter(request, REQUEST_PARAM_PAGE, 1);
    }

    protected EssentialsListComponentInfo getComponentInfo(HstRequest request) {
        return getComponentParametersInfo(request);
    }

    private HstQuery buildQuery(HstRequest request) {
        String queryParameter = getQueryParameter(request);
        Constraint searchStringConstraint = null;

        HstQueryBuilder queryBuilder = HstQueryBuilder.create(request.getRequestContext().getSiteContentBaseBean());

        if (queryParameter != null) {
            String query = SearchInputParsingUtils.parse(queryParameter, true);
            String queryIncWildcards = parseAndApplyWildcards(queryParameter);

            searchStringConstraint = or(
                publicationSystemConstraint(query),
                publicationSystemConstraint(queryIncWildcards),
                indicatorLibraryConstraint(query),
                indicatorLibraryConstraint(queryIncWildcards),
                constraint(".").contains(query),
                constraint(".").contains(queryIncWildcards)
            );
        }

        // register content classes
        addPublicationSystemTypes(queryBuilder);
        addIndicatorLibraryTypes(queryBuilder);

        String sortParam = getSortOption(request);
        if (sortParam.equals(SORT_DATE)) {
            queryBuilder.orderByDescending("publicationsystem:NominalDate", "publicationsystem:Title");
        }

        return constructQuery(queryBuilder, searchStringConstraint);
    }

    private String getSortOption(HstRequest request) {
        return Optional.ofNullable(getAnyParameter(request, REQUEST_PARAM_SORT)).orElse(SORT_DEFAULT);
    }

    private HstQuery constructQuery(HstQueryBuilder queryBuilder, Constraint searchStringConstraint) {
        ArrayList<Constraint> constraints = new ArrayList<>(2);
        constraints.add(constraint("common:searchable").equalTo(true));

        if (searchStringConstraint != null) {
            constraints.add(searchStringConstraint);
        }

        return queryBuilder.where(and(constraints.toArray(new Constraint[0]))).build();
    }

    /**
     * Publication System content types that should be included in search
     */
    private void addPublicationSystemTypes(HstQueryBuilder query) {
        query.ofTypes(
            Archive.class,
            Dataset.class,
            LegacyPublication.class,
            Publication.class,
            Series.class
        );
    }

    /**
     * National Indicator Library content type(s) that should be included in the search results
     */
    private void addIndicatorLibraryTypes(HstQueryBuilder query) {
        query.ofTypes(Indicator.class);
    }

    /**
     * Publication System search constraint
     */
    private Constraint publicationSystemConstraint(String query) {
        return or(
            constraint("publicationsystem:Title").contains(query),
            constraint("publicationsystem:Summary").contains(query),
            constraint("publicationsystem:KeyFacts").contains(query),
            constraint("common:SearchableTags").contains(query)
        );
    }

    /**
     * National Indicator Library search constraint
     */
    private Constraint indicatorLibraryConstraint(String query) {
        return or(
            constraint("nationalindicatorlibrary:title").contains(query),
            constraint("nationalindicatorlibrary:methodology").contains(query)
        );
    }
}
