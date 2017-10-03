package `in`.sdqali.teamcity

import jetbrains.buildServer.serverSide.BuildRevision
import jetbrains.buildServer.serverSide.impl.BaseBuild
import jetbrains.buildServer.vcs.VcsRootInstance
import jetbrains.buildServer.vcs.impl.VcsRootInstanceImpl
import jetbrains.buildServer.web.openapi.PagePlace
import jetbrains.buildServer.web.openapi.PagePlaces
import jetbrains.buildServer.web.openapi.PlaceId
import jetbrains.buildServer.web.openapi.PluginDescriptor
import org.junit.Before
import org.junit.Test
import org.mockito.BDDMockito.*
import org.mockito.Mockito.mock
import javax.servlet.http.HttpServletRequest
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ExternalChangesLinkExtensionTest {
    private lateinit var extension: ExternalChangesLinkExtension
    private lateinit var pagePlaces: PagePlaces
    private lateinit var descriptor: PluginDescriptor
    private lateinit var pagePlace: PagePlace
    private lateinit var placeId: PlaceId

    @Before
    fun setup() {
        pagePlace = mock(PagePlace::class.java)
        pagePlaces = mock(PagePlaces::class.java)
        descriptor = mock(PluginDescriptor::class.java)
        `when`(pagePlaces.getPlaceById(PlaceId.VCS_ROOT_IN_BUILD_NOTE)).thenReturn(pagePlace)
        extension = ExternalChangesLinkExtension(pagePlaces, descriptor)
    }


    @Test
    fun affectsBuildChangesTab() {
        val request = createRequest("/viewLog.html", "/", "buildChangesDiv")
        assertTrue(extension.isAvailable(request), "Extension should be available, but is not.")
    }

    @Test
    fun doesNotAffectOtherTabs() {
        val request = createRequest("/viewLog.html", "/", "buildLog")
        assertFalse(extension.isAvailable(request), "Extension should not be available, but is.")
    }

    @Test
    fun computesUrlBasedOnTemplateFromBuildParams() {
        val request = createRequest("/viewLog.html", "/", "buildChangesDiv")
        val buildParams = mapOf("external.changes.viewer.template" to "http://example.com/{changeSetDisplayRevision}")
        val buildData = buildDataFor("test-revision", 12345678, buildParams)
        val vcsRoot = vcsRootInstance(12345678, "https://github.com/sdqali/todo.kotlin")

        given(request.getAttribute("buildData")).willReturn(buildData)
        given(request.getAttribute("vcsRoot")).willReturn(vcsRoot)

        val input = mutableMapOf<String, Any>()
        extension.fillModel(input, request)
        assertEquals("http://example.com/test-revision", input["url"])
    }

    @Test
    fun deducesUrlBasedOnServiceIfTemplateParamIsMissing() {
        val request = createRequest("/viewLog.html", "/", "buildChangesDiv")
        val buildParams = mapOf("external.changes.viewer.template" to "")
        val buildData = buildDataFor("test-revision", 12345678, buildParams)
        val vcsRoot = vcsRootInstance(12345678, "https://github.com/sdqali/todo.kotlin")

        given(request.getAttribute("buildData")).willReturn(buildData)
        given(request.getAttribute("vcsRoot")).willReturn(vcsRoot)

        val input = mutableMapOf<String, Any>()
        extension.fillModel(input, request)
        assertEquals("https://github.com/sdqali/todo.kotlin/commit/test-revision", input["url"])
    }

    private fun buildDataFor(revision: String, vcsRootId: Long, buildParams: Map<String, String>): BaseBuild {
        val buildData = mock(BaseBuild::class.java)
        val vcsRoot = vcsRootInstance(vcsRootId, "https://github.com/sdqali/todo.kotlin")
        val revision = BuildRevision(vcsRoot, revision, "", revision)
        val revisions = arrayListOf(revision)
        given(buildData.revisions).willReturn(revisions)
        given(buildData.buildFinishParameters).willReturn(buildParams)
        return buildData
    }

    private fun vcsRootInstance(vcsRootId: Long, fetchUrl: String): VcsRootInstance {
        val vcsRoot = mock(VcsRootInstanceImpl::class.java)
        given(vcsRoot.id).willReturn(vcsRootId)
        given(vcsRoot.getProperty("url")).willReturn(fetchUrl)
        return vcsRoot
    }

    private fun createRequest(uri: String, contextPath: String, tabName: String): HttpServletRequest {
        val request = mock(HttpServletRequest::class.java)
        given(request.contextPath).willReturn(contextPath)
        given(request.requestURI).willReturn(uri)
        given(request.getAttribute("tab")).willReturn(tabName)
        return request
    }
}