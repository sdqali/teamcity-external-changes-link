package `in`.sdqali.teamcity

import jetbrains.buildServer.vcs.impl.VcsRootInstanceImpl
import jetbrains.buildServer.web.openapi.PagePlace
import jetbrains.buildServer.web.openapi.PagePlaces
import jetbrains.buildServer.web.openapi.PlaceId
import jetbrains.buildServer.web.openapi.PluginDescriptor
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import javax.servlet.http.HttpServletRequest
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ExternalChangesLinkExtensionTest {
    private lateinit var extension: ExternalChangesLinkExtension
    private lateinit var pagePlaces: PagePlaces
    private lateinit var descriptor: PluginDescriptor
    private lateinit var pagePlace: PagePlace

    @Before
    fun setup() {
        pagePlace = mock(PagePlace::class.java)
        pagePlaces = mock(PagePlaces::class.java)
        descriptor = mock(PluginDescriptor::class.java)
        `when`(pagePlaces.getPlaceById(PlaceId.VCS_ROOT_IN_BUILD_NOTE)).thenReturn(pagePlace)
        extension = ExternalChangesLinkExtension(pagePlaces, descriptor)
    }


    @Test
    fun affectsBuildChanges() {
        val request = mock(HttpServletRequest::class.java)
        `when`(request.getAttribute("tab")).thenReturn("buildChangesDiv")
        assertTrue(extension.isAvailable(request), "Extension should be available, but is not.")
    }

    @Test
    fun affectsPendingChanges() {
        val request = mock(HttpServletRequest::class.java)
        `when`(request.getAttribute("tab")).thenReturn("pendingChangesDiv")
        assertTrue(extension.isAvailable(request), "Extension should be available, but is not.")
    }

    @Test
    fun doesNotAffectsPendingChanges() {
        val request = mock(HttpServletRequest::class.java)
        `when`(request.getAttribute("tab")).thenReturn("artifacts")
        assertFalse("Extension should not be available, but is.", extension.isAvailable(request))
    }

    @Test
    fun testExtractsUrlForVcsRootAvailable() {
        val request = mock(HttpServletRequest::class.java)
        val vcsRoot = mock(VcsRootInstanceImpl::class.java)
        val model = mutableMapOf<String, Any>()

        `when`(request.getAttribute("tab")).thenReturn("buildChangesDiv")
        `when`(vcsRoot.getProperty("url")).thenReturn("https://github.com/JetBrains/kotlin.git")
        `when`(request.getAttribute("vcsRoot")).thenReturn(vcsRoot)

        extension.fillModel(model, request)
        assertEquals("https://github.com/JetBrains/kotlin.git", model["fetchUrl"])
    }
}