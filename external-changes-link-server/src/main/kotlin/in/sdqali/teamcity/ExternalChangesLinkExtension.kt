package `in`.sdqali.teamcity

import jetbrains.buildServer.serverSide.impl.BaseBuild
import jetbrains.buildServer.vcs.impl.VcsRootInstanceImpl
import jetbrains.buildServer.web.openapi.PagePlaces
import jetbrains.buildServer.web.openapi.PlaceId
import jetbrains.buildServer.web.openapi.PluginDescriptor
import jetbrains.buildServer.web.openapi.SimplePageExtension
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

    private fun guessUrlFrom(revision: String, url: String?): String {
        url?.let {
            return when {
                it.matches(Regex(".*github.*")) -> "$url/commit/$revision"
                else -> ""
            }
        } ?: return ""
    }

    private fun commitUrlFromTemplate(viewerTemplate: String, revision: String): String {
        return viewerTemplate.replace("{changeSetDisplayRevision}", revision)
    }
}