package `in`.sdqali.teamcity

import jetbrains.buildServer.web.openapi.PagePlaces
import jetbrains.buildServer.web.openapi.PlaceId
import jetbrains.buildServer.web.openapi.PluginDescriptor
import jetbrains.buildServer.web.openapi.SimplePageExtension
import javax.servlet.http.HttpServletRequest

class ExternalChangesLinkExtension : SimplePageExtension {
    private val allowedTabs = arrayListOf("buildChangesDiv", "pendingChangesDiv")

    constructor(pagePlaces : PagePlaces, descriptor : PluginDescriptor) :
        super(pagePlaces, PlaceId.VCS_ROOT_IN_BUILD_NOTE, "external-changes-link", descriptor.getPluginResourcesPath("link.jsp")) {
        register()
    }

    override fun isAvailable(request: HttpServletRequest): Boolean {
        val tabName = request.getAttribute("tab")
        return tabName != null && allowedTabs.contains(tabName)
    }
}