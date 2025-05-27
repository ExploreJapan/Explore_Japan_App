package com.example.explorejapanapp

import android.content.Intent
import android.net.Uri
import androidx.fragment.app.Fragment
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
class ExampleUnitTest {

    private lateinit var mainActivity: MainActivity

    @Before
    fun setUp() {
        val context = RuntimeEnvironment.getApplication().applicationContext
        FirebaseApp.initializeApp(context)

        mainActivity = Robolectric.buildActivity(MainActivity::class.java).create().get()
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
    }

    @Test
    fun replaceFragment_updatesCurrentFragmentIndex() {
        val initialIndex = 2
        val newIndex = 3
        val fragment = Fragment()
        mainActivity.setCurrentFragmentIndexForTesting(initialIndex)

        mainActivity.replaceFragment(fragment, newIndex)

        assertEquals(newIndex, mainActivity.getCurrentFragmentIndexForTesting())
    }

    @Test
    fun handleDeepLink_withValidUri_opensTokyoFragment() {
        val uri = Uri.parse("explorejapanapp://tokyo?article=Проживання")
        val intent = Intent().apply { data = uri }

        mainActivity.handleDeepLink(intent)

        // Виконуємо всі завдання в основному лупері
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        // Перевіряємо, чи є фрагмент City_Tokyo
        val fragment = mainActivity.supportFragmentManager.fragments.firstOrNull()
        assertNotNull("Fragment should not be null", fragment)
        assertTrue("Fragment should be City_Tokyo", fragment is City_Tokyo)

        // Додатково перевіряємо аргументи
        val expectedArticle = "Проживання"
        assertEquals(expectedArticle, fragment?.arguments?.getString("articleToOpen"))
    }

    @Test
    fun handleDeepLink_withInvalidUri_doesNothing() {
        val uri = Uri.parse("invalid://scheme")
        val intent = Intent().apply { data = uri }

        // Очищаємо supportFragmentManager перед тестом
        mainActivity.supportFragmentManager.fragments.forEach {
            mainActivity.supportFragmentManager.beginTransaction().remove(it).commit()
        }
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        mainActivity.handleDeepLink(intent)

        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        assertTrue("Fragment list should be empty", mainActivity.supportFragmentManager.fragments.isEmpty())
    }
}

fun MainActivity.setCurrentFragmentIndexForTesting(index: Int) {
    val field = MainActivity::class.java.getDeclaredField("currentFragmentIndex")
    field.isAccessible = true
    field.set(this, index)
}

fun MainActivity.getCurrentFragmentIndexForTesting(): Int {
    val field = MainActivity::class.java.getDeclaredField("currentFragmentIndex")
    field.isAccessible = true
    return field.get(this) as Int
}