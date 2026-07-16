package com.synex.feature.onboarding

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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {
    private val dispatcher = UnconfinedTestDispatcher()

    @Before fun setUp() = Dispatchers.setMain(dispatcher)

    @After fun tearDown() = Dispatchers.resetMain()

    @Test
    fun returningCustomerSkipsCompletedDisclosure() {
        val viewModel = OnboardingViewModel(FakeRepository(riskAcknowledged = true))

        assertTrue(viewModel.state.value.completed)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun acceptanceUsesCurrentDisclosureVersion() {
        val repository = FakeRepository(riskAcknowledged = false)
        val viewModel = OnboardingViewModel(repository)

        viewModel.setRiskChecked(true)
        viewModel.acceptRisk()

        assertEquals("synex-risk-disclosure-v1", repository.acceptedVersion)
        assertTrue(viewModel.state.value.completed)
    }
}

private class FakeRepository(riskAcknowledged: Boolean) : SynexRepository {
    override val activeLoginId = MutableStateFlow<String?>(null)
    private val status = OnboardingStatus(riskAcknowledged, "synex-risk-disclosure-v1")
    var acceptedVersion: String? = null

    override suspend fun onboardingStatus() = status
    override suspend fun acknowledgeRisk(disclosureVersion: String) { acceptedVersion = disclosureVersion }
    override fun selectAccount(loginId: String) = Unit
    override suspend fun overview(): OverviewSnapshot = error("Not used")
    override suspend fun markets(): List<MarketQuote> = error("Not used")
    override suspend fun portfolio(): PortfolioSummary = error("Not used")
    override suspend fun accounts(): List<TradingAccount> = error("Not used")
}
