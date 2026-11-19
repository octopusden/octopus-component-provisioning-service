package org.octopusden.octopus.provisioning.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.octopusden.octopus.provisioning.common.dto.provisioning.ProvisioningState
import tools.jackson.databind.json.JsonMapper

class ProvisioningStateJsonTest {
    @Test
    fun stateSerialisesAsItsName() {
        assertEquals("\"NOT_STARTED\"", JsonMapper().writeValueAsString(ProvisioningState.NOT_STARTED))
    }
}
