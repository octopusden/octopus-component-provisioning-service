package org.octopusden.octopus.provisioning.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.octopusden.octopus.provisioning.common.dto.provisioning.ProvisioningState

class ProvisioningStateJsonTest {
    @Test
    fun stateSerialisesAsItsName() {
        assertEquals("\"NOT_STARTED\"", ObjectMapper().writeValueAsString(ProvisioningState.NOT_STARTED))
    }
}
