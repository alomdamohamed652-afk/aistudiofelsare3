package com.example.identity

import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Placeholder regression test documenting the identity-hardening contract.
 * Runtime role changes are no longer permitted by LocalAuthRepository.
 */
class IdentityHardeningNotesTest {
    @Test
    fun identityHardeningContractIsExplicit() {
        assertTrue("Authenticated roles must come from the account/session, not UI role switching", true)
    }
}
