<#ftl output_format="HTML">
<#include "../include/imports.ftl">
<#include "macro/siteHeader.ftl">
<!DOCTYPE html>
<html lang="en" class="no-js">

<#include "app-layout-head.ftl">

<body class="debugs">
    <#-- Add site header without the search bar -->
    <@siteHeader false></@siteHeader>

    <#-- No breadcrumbs here -->

    <main role="main">
        <@hst.include ref="main"/>
    </main>

    <#include "site-footer.ftl"/>

    <#include "scripts/live-engage-chat.ftl"/>

    <@hst.headContributions categoryIncludes="htmlBodyEnd, scripts" xhtml=true/>
</body>

</html>
