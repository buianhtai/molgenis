<#macro footer version=1>
            </div><#-- close plugin-container -->
        </div><#-- close col-md-12 -->
    </div><#-- close row -->
</div><#-- close container-fluid -->

<#if version == 1>
    <div id="footer-container">
        <div class="container">
            <p class="text-muted text-center small footer">
                <#if app_settings.footer??>
                <span>
                    ${app_settings.footer}
                </span>
                <br>
                </#if>
                <em>
                    This database was created using the open source <a href="http://www.molgenis.org">MOLGENIS
                   software</a><#if molgenis_version?has_content>
                    <span>version ${molgenis_version!?html}</span></#if><#if molgenis_build_date?has_content><span> built
                    on ${molgenis_build_date!?html}</span></#if>.<br>Please cite <a
                        href="https://www.ncbi.nlm.nih.gov/pubmed/30165396">Van der Velde et al (2018)</a>, <a
                        href="http://www.ncbi.nlm.nih.gov/pubmed/21210979">Swertz et al (2010)</a> or <a
                        href="http://www.ncbi.nlm.nih.gov/pubmed/17297480">Swertz &amp; Jansen (2007)</a> on use.
                </em>
            </p>
        </div>
    </div>
  <#if !(authenticated!false)>
    <#include "/login-modal.ftl">
  </#if>
<#else>
    <#-- VUE -->
    <div id="molgenis-footer"></div>

    <script type="text/javascript" src="/@molgenis-ui/legacy-lib/dist/require.js"></script>

    <script>
      requirejs.config({
        baseUrl: '/@molgenis-ui/legacy-lib/dist'
      });

      requirejs(["context.umd.min", "vue.min"], function(context, Vue) {
        new Vue({
          render: function(createElement) {
            const propsData = {
              props: {
                molgenisFooter: {
                  <#if app_settings.footer??>additionalMessage: '${app_settings.footer}', </#if>
                  <#if molgenis_version??>version: '${molgenis_version}', </#if>
                  <#if molgenis_build_date??>buildDate: '${molgenis_build_date}', </#if>
                  <#if molgenis_app_version??>appVersion: '${molgenis_app_version}', </#if>
                }
              }
            };
            return createElement(context.default.FooterComponent, propsData);
          }
        }).$mount('#molgenis-footer');
      });
  </script>

</#if>
</body>
    <#if app_settings.trackingCodeFooter?has_content>
    <script id="app-tracking-code-footer" type="text/javascript">
        ${app_settings.trackingCodeFooter?string}
		</script>
    </#if>
</html>
</#macro>
