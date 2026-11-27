package tachiyomi.domain.vault

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode

@Execution(ExecutionMode.SAME_THREAD)
class VaultSessionTest {

    @AfterEach
    fun reset() {
        VaultSession.init(active = false)
    }

    @Test
    fun `id based preference keys are isolated per profile`() {
        VaultSession.init(active = false)
        VaultSession.profileKey("default_category") shouldBe "default_category"
        VaultSession.isVaultKey("default_category") shouldBe false

        VaultSession.init(active = true)
        VaultSession.profileKey("default_category") shouldBe "default_category__vault"
        VaultSession.isVaultKey("default_category__vault") shouldBe true
    }

    @Test
    fun `authentication is required again after backgrounding`() {
        VaultSession.init(active = true)
        VaultSession.requiresAuth.value shouldBe true

        VaultSession.onAuthenticated()
        VaultSession.requiresAuth.value shouldBe false

        VaultSession.onAuthenticating()
        VaultSession.onAppBackgrounded()
        VaultSession.requiresAuth.value shouldBe false

        VaultSession.onAuthenticationDismissed()
        VaultSession.requiresAuth.value shouldBe true
        VaultSession.isAuthenticating shouldBe false

        VaultSession.onAppBackgrounded()
        VaultSession.requiresAuth.value shouldBe true
    }
}
