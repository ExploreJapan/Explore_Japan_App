package com.example.explorejapanapp

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
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
class CityTokyoUnitTest {

    private lateinit var fragment: City_Tokyo
    private lateinit var fragmentManager: FragmentManager

    @Before
    fun setUp() {
        val context = RuntimeEnvironment.getApplication().applicationContext
        FirebaseApp.initializeApp(context)

        // Створюємо активність на основі AppCompatActivity
        val activity = Robolectric.buildActivity(AppCompatActivity::class.java).create().get()
        fragmentManager = activity.supportFragmentManager

        // Ініціалізуємо фрагмент і додаємо його до активності
        fragment = City_Tokyo()
        val transaction: FragmentTransaction = fragmentManager.beginTransaction()
        transaction.add(fragment, "City_Tokyo")
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
    fun onStop_doesNotCrash() {
        try {
            fragment.onStop()
            assertTrue("onStop should execute without crashing", true)
        } catch (e: Exception) {
            fail("onStop should not throw an exception: ${e.message}")
        }
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
    }

    @Test
    fun onDestroyView_doesNotCrash() {
        try {
            fragment.onDestroyView()
            assertTrue("onDestroyView should execute without crashing", true)
        } catch (e: Exception) {
            fail("onDestroyView should not throw an exception: ${e.message}")
        }
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
    }

    @Test
    fun isArticleOpen_initialStateIsFalse() {
        // Перевіряємо початковий стан isArticleOpen
        val isArticleOpenField = City_Tokyo::class.java.getDeclaredField("isArticleOpen")
        isArticleOpenField.isAccessible = true
        val initialState = isArticleOpenField.getBoolean(fragment)

        assertFalse("isArticleOpen should be false by default", initialState)
    }
}