package com.example.explorejapanapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.google.firebase.FirebaseApp
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.junit.Assert.*
import org.robolectric.shadows.ShadowLooper

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class FragmentProfileUnitTest {

    private lateinit var fragment: Fragment_Profile
    private lateinit var fragmentManager: FragmentManager

    @Before
    fun setUp() {
        val context = RuntimeEnvironment.getApplication().applicationContext
        FirebaseApp.initializeApp(context)

        // Створюємо активність на основі AppCompatActivity
        val activity = Robolectric.buildActivity(AppCompatActivity::class.java).create().get()
        fragmentManager = activity.supportFragmentManager

        // Ініціалізуємо фрагмент і додаємо його до активності
        fragment = Fragment_Profile()
        val transaction: FragmentTransaction = fragmentManager.beginTransaction()
        transaction.add(fragment, "Fragment_Profile")
        transaction.commit()
        fragmentManager.executePendingTransactions()

        // Запускаємо життєвий цикл фрагмента
        fragment.onCreate(Bundle())
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
    }

    @Test
    fun onCreate_initializesFragment() {
        assertNotNull("Fragment should be initialized", fragment)
        assertTrue("Fragment should be attached to context", fragment.context != null)
    }

    @Test
    fun convertCityToUkrainian_withValidCity_translatesToUkrainian() {
        val city = "Tokyo"
        val result = fragment.javaClass.getDeclaredMethod("convertCityToUkrainian", String::class.java).apply {
            isAccessible = true
        }.invoke(fragment, city) as String

        assertEquals("Токіо", result)
    }

    @Test
    fun convertCityToUkrainian_withInvalidCity_returnsOriginalCity() {
        val city = "UnknownCity"
        val result = fragment.javaClass.getDeclaredMethod("convertCityToUkrainian", String::class.java).apply {
            isAccessible = true
        }.invoke(fragment, city) as String

        assertEquals("UnknownCity", result)
    }

    @Test
    fun onStop_doesNotCrash() {
        try {
            fragment.onStop()
            assertTrue("onStop should execute without crashing", true)
        } catch (e: Exception) {
            fail("onStop should not throw an exception: ${e.message}")
        }
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
    }
}