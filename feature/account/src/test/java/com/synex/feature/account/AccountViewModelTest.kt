package com.synex.feature.account

import com.synex.core.data.SynexRepository
import com.synex.core.model.MarketQuote
import com.synex.core.model.OnboardingStatus
import com.synex.core.model.OverviewSnapshot
import com.synex.core.model.PortfolioSummary
import com.synex.core.model.TradingAccount
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AccountViewModelTest {
    private val dispatcher = UnconfinedTestDispatcher()

    @Before fun setUp() = Dispatchers.setMain(dispatcher)

    @After fun tearDown() = Dispatchers.resetMain()

    @Test
    fun emptyAccountListIsAConnectStateNotAnError() {
        val viewModel = AccountViewModel(FakeRepository())

        assertFalse(viewModel.state.value.isLoading)
        assertTrue(viewModel.state.value.accounts.isEmpty())
        assertNull(viewModel.state.value.errorMessage)
    }

    @Test
    fun connectRequestsTheBackendAuthorizationUrl() {
        val viewModel = AccountViewModel(FakeRepository())

        viewModel.connectDeriv()

        assertEquals("https://oauth.deriv.test/authorize", viewModel.state.value.connectionUrl)
        assertFalse(viewModel.state.value.isConnecting)
    }

    @Test
    fun returningFromBrowserRefreshesTheLinkedAccount() {
        val repository = FakeRepository()
        val viewModel = AccountViewModel(repository)
        val account = TradingAccount("VRTC123", "USD", 10_000.0, isVirtual = true)

        viewModel.connectDeriv()
        viewModel.onBrowserOpened()
        repository.currentAccounts = listOf(account)
        viewModel.onAppResumed()

        assertEquals(listOf(account), viewModel.state.value.accounts)
        assertEquals(account.loginId, viewModel.state.value.selectedLoginId)
        assertFalse(viewModel.state.value.waitingForConnection)
        assertEquals("Deriv account connected successfully.", viewModel.state.value.connectionMessage)
    }
}

private class FakeRepository : SynexRepository {
    override val activeLoginId = MutableStateFlow<String?>(null)
    var currentAccounts: List<TradingAccount> = emptyList()

    override suspend fun accounts(): List<TradingAccount> {
        if (activeLoginId.value !in currentAccounts.map(TradingAccount::loginId)) {
            activeLoginId.value = currentAccounts.firstOrNull()?.loginId
        }
        return currentAccounts
    }

    override suspend fun derivConnectUrl() = "https://oauth.deriv.test/authorize"
    override fun selectAccount(loginId: String) { activeLoginId.value = loginId }
    override suspend fun overview(): OverviewSnapshot = error("Not used")
    override suspend fun markets(): List<MarketQuote> = error("Not used")
    override suspend fun portfolio(): PortfolioSummary = error("Not used")
    override suspend fun onboardingStatus(): OnboardingStatus = error("Not used")
    override suspend fun acknowledgeRisk(disclosureVersion: String) = error("Not used")
}
