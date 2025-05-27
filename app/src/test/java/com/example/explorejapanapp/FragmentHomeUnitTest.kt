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
class FragmentHomeUnitTest {

    private lateinit var fragment: Fragment_Home
    private lateinit var fragmentManager: FragmentManager

    @Before
    fun setUp() {
        val context = RuntimeEnvironment.getApplication().applicationContext
        FirebaseApp.initializeApp(context)

        // Створюємо активність на основі AppCompatActivity
        val activity = Robolectric.buildActivity(AppCompatActivity::class.java).create().get()
        fragmentManager = activity.supportFragmentManager

        // Ініціалізуємо фрагмент і додаємо його до активності
        fragment = Fragment_Home()
        val transaction: FragmentTransaction = fragmentManager.beginTransaction()
        transaction.add(fragment, "Fragment_Home")
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
    fun replaceFragment_replacesFragmentInManager() {
        // Створюємо фрагмент для заміни
        val newFragment = City_Tokyo()

        // Викликаємо метод replaceFragment через рефлексію
        val replaceFragmentMethod = Fragment_Home::class.java.getDeclaredMethod("replaceFragment", Fragment::class.java)
        replaceFragmentMethod.isAccessible = true
        replaceFragmentMethod.invoke(fragment, newFragment)

        // Виконуємо всі транзакції FragmentManager
        fragmentManager.executePendingTransactions()
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        // Перевіряємо, чи фрагмент замінений
        val currentFragment = fragmentManager.findFragmentById(R.id.frame_layout)
        assertNotNull("Fragment should not be null", currentFragment)
        assertTrue("Fragment should be City_Tokyo", currentFragment is City_Tokyo)
    }

    @Test
    fun replaceFragment_addsToBackStack() {
        // Очищаємо back stack перед тестом
        while (fragmentManager.backStackEntryCount > 0) {
            fragmentManager.popBackStackImmediate()
        }

        // Створюємо фрагмент для заміни
        val newFragment = City_Tokyo()

        // Викликаємо метод replaceFragment через рефлексію
        val replaceFragmentMethod = Fragment_Home::class.java.getDeclaredMethod("replaceFragment", Fragment::class.java)
        replaceFragmentMethod.isAccessible = true
        replaceFragmentMethod.invoke(fragment, newFragment)

        // Виконуємо всі транзакції FragmentManager
        fragmentManager.executePendingTransactions()
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        // Перевіряємо, чи додано фрагмент у back stack
        assertEquals("Back stack should have one entry", 1, fragmentManager.backStackEntryCount)
    }
}