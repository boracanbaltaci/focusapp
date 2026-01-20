package com.focusapp.ui.screens

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class SessionViewModelTest {
    
    private lateinit var viewModel: SessionViewModel
    
    @Before
    fun setup() {
        viewModel = SessionViewModel()
    }
    
    @Test
    fun `test session state is idle initially`() = runTest {
        val state = viewModel.sessionState.value
        assert(state is SessionState.Idle)
    }
    
    @Test
    fun `test current session is null initially`() = runTest {
        val session = viewModel.currentSession.value
        assert(session == null)
    }
}
