<#include "resource-macros.ftl">
<#-- modal for single entity data -->
<div class="modal large" id="entityReportModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-xxl">
        <div class="modal-content">
        <#if showStandaloneReportUrl>
            <form class="form-inline" style="text-align: right;margin-right: 1em;">
                <div class="form-group">
                    <small><label for="standalone-report-url">Share this entity report</label></small>
                    <div class="row">
                        <div class="col-md-12">
                            <input type="text" class="form-control input-sm" id="standalone-report-url"
                                   readonly="readonly">
                            <button title="Copy to clipboard" class="btn btn-default btn-sm"
                                    id="copy-standalone-report-url-btn"><span
                                    class="glyphicon glyphicon-duplicate" aria-hidden="true"></span></button>
                        </div>
                    </div>
                </div>
            </form>
            <hr>
        </#if>
        <#include viewName+".ftl">
        </div>
    </div>
</div>

<#if showStandaloneReportUrl>
<script>
    var entityIdForURL = '${entityId}'
    var entityNameForURL = '${entityName}'
    var standaloneReportURL = window.location.origin + molgenis.getContextUrl() + '/details/' + entityNameForURL + '/' + entityIdForURL

    $('#standalone-report-url').val(standaloneReportURL)
    //Add copy functionality
    document.getElementById('copy-standalone-report-url-btn').addEventListener('click', function (e) {
        e.preventDefault()
        var standaloneURL = document.createElement('input')
        standaloneURL.setAttribute('value', $('#standalone-report-url').val())
        document.body.appendChild(standaloneURL)
        standaloneURL.select()
        document.execCommand('copy')
        document.body.removeChild(standaloneURL)
    })
</script>
</#if>