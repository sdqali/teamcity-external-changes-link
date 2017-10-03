package `in`.sdqali.teamcity

import jetbrains.buildServer.serverSide.impl.BaseBuild
import jetbrains.buildServer.vcs.impl.VcsRootInstanceImpl
import jetbrains.buildServer.web.openapi.PagePlaces
import jetbrains.buildServer.web.openapi.PlaceId
import jetbrains.buildServer.web.openapi.PluginDescriptor
import jetbrains.buildServer.web.openapi.SimplePageExtension
import org.eclipse.jgit.transport.URIish
import org.jetbrains.annotations.NotNull
import javax.servlet.http.HttpServletRequest

class ExternalChangesLinkExtension : SimplePageExtension {
    private val allowedTabs = arrayListOf("buildChangesDiv", "pendingChangesDiv")

    constructor(pagePlaces : PagePlaces, descriptor : PluginDescriptor) :
        super(pagePlaces, PlaceId.VCS_ROOT_IN_BUILD_NOTE, "external-changes-link", descriptor.getPluginResourcesPath("link.jsp")) {
        register()
    }

    override fun isAvailable(request: HttpServletRequest): Boolean {
        return ("buildChangesDiv" == request.getAttribute("tab"))
            .and(PlaceId.VCS_ROOT_IN_BUILD_NOTE.matches(request))
    }

    override fun fillModel(@NotNull model: MutableMap<String, Any>, @NotNull request: HttpServletRequest) {
        super.fillModel(model, request)
        val vcsRoot : VcsRootInstanceImpl = request.getAttribute("vcsRoot") as VcsRootInstanceImpl

        val buildData = request.getAttribute("buildData") as BaseBuild

        buildData.revisions.find {
            it.entry.vcsRoot.id == vcsRoot.id
        }?.let {
            val revision = it.revisionDisplayName
            buildData.buildFinishParameters?.get("external.changes.viewer.template")?.let {
                if(it.isNotBlank()) {
                    model["url"] = commitUrlFromTemplate(it, revision)
                } else {
                    model["url"] = guessUrlFrom(revision, vcsRoot.getProperty("url"))
                }

            }

        }
    }

    private fun guessUrlFrom(revision: String, urlProperty: String?): String {
        urlProperty?.let {
            val urIish = URIish(urlProperty)
            val repoPath = urIish.path
                .replace(Regex("\\.git$"), "")
                .replace(Regex("^/"), "")
            val scheme = if (urIish.scheme == "https")  "https" else "http"
            return when {
                urIish.host.matches(Regex(".*github.*")) -> "$scheme://${urIish.host}/$repoPath/commit/$revision"
                urIish.host.matches(Regex(".*gitlab.*")) -> "$scheme://${urIish.host}/$repoPath/commit/$revision"
                urIish.host.matches(Regex(".*bitbucket.*")) -> "$scheme://${urIish.host}/$repoPath/commits/$revision"
                else -> ""
            }
        } ?: return ""
    }

    private fun commitUrlFromTemplate(viewerTemplate: String, revision: String): String {
        return viewerTemplate.replace("{changeSetDisplayRevision}", revision)
    }
}