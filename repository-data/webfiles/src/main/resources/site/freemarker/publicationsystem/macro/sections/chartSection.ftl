<#ftl output_format="HTML">

<#macro chartSection section>
    <div id="${section.uniqueId}" style="width:100%; height:400px;"></div>

    <script>
        $(function () {
            var myChart = Highcharts.chart('${section.uniqueId}', <#outputformat "plainText">${section.chartConfig}</#outputformat>);
        });
    </script>
</#macro>
