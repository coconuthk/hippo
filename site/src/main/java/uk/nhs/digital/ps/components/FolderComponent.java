package uk.nhs.digital.ps.components;

import java.util.List;
import org.hippoecm.hst.component.support.bean.BaseHstComponent;
import org.hippoecm.hst.content.beans.query.builder.HstQueryBuilder;
import org.hippoecm.hst.content.beans.query.exceptions.QueryException;
import org.hippoecm.hst.content.beans.query.HstQuery;
import org.hippoecm.hst.content.beans.standard.HippoBean;
import org.hippoecm.hst.content.beans.standard.HippoBeanIterator;
import org.hippoecm.hst.content.beans.standard.HippoFolder;
import org.hippoecm.hst.core.component.HstComponentException;
import org.hippoecm.hst.core.component.HstRequest;
import org.hippoecm.hst.core.component.HstResponse;
import uk.nhs.digital.ps.beans.Dataset;
import uk.nhs.digital.ps.beans.Publication;
import uk.nhs.digital.ps.beans.Series;

public class FolderComponent extends BaseHstComponent {

    @Override
    public void doBeforeRender(final HstRequest request, final HstResponse response) throws HstComponentException {
        super.doBeforeRender(request, response);
        HippoFolder folder = (HippoFolder) request.getRequestContext().getContentBean();
        List<Series> seriesInFolder = folder.getChildBeans(Series.class);
        List<Publication> publicationContentInFolder = folder.getChildBeansByName("content", Publication.class);

        if (seriesInFolder.size() > 0) {

            SeriesComponent seriesComponent = new SeriesComponent();
            seriesComponent.doBeforeRender(request, response);
            return;
        }
        else if (publicationContentInFolder.size() > 0) {
            PublicationComponent publicationComponent = new PublicationComponent();
            publicationComponent.doBeforeRender(request, response);
            return;
        }
        else {
            try {
                request.setAttribute("publications", findAllDocuments(folder));
            } catch (QueryException e) {
                throw new HstComponentException(
                    "Exception occurred during folder search.", e);
            }
        }
    }

    private HippoBeanIterator findAllDocuments(HippoBean scope) throws QueryException {
        HstQueryBuilder queryBuilder = HstQueryBuilder.create(scope);
        HstQuery hstQuery = queryBuilder
            .ofTypes(Publication.class, Series.class, Dataset.class)
            .limit(20)
            .offset(0)
            .build();

        return hstQuery.execute().getHippoBeans();
    }
}