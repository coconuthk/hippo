<#ftl output_format="HTML">
<#include "textSection.ftl">
<#include "imageSection.ftl">
<#include "chartSection.ftl">
<#include "related-linkSection.ftl">

<!-- This is a load of global setup for the highcharts config -->
<script src="https://code.highcharts.com/highcharts.js"></script>
<script src="https://code.highcharts.com/modules/exporting.js"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/jquery/2.1.3/jquery.min.js"></script>
<script>
    Highcharts.setOptions({
        colors: ['#005EB8', '#84919C', '#003087', '#71CCEF', '#D0D5D6']
        chart: {
            backgroundColor: '#F4F5F6'
        }
    });
</script>

<#macro sections sections>
    <div data-uipath="ps.publication.body">
        <#list sections as section>
            <#if section.sectionType == 'text'>
                <@textSection section=section />
            <#elseif section.sectionType == 'image'>
                <@imageSection section=section />
            <#elseif section.sectionType == 'relatedLink'>
                <@relatedLinkSection section=section />
            <#elseif section.sectionType == 'chart'>
                <@chartSection section=section />
            </#if>
        </#list>
    </div>
</#macro>
