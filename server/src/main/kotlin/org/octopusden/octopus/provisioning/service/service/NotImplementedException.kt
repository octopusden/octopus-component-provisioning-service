package org.octopusden.octopus.provisioning.service.service

/** Thrown by an operation whose contract is declared but not built yet; the API answers 501. */
class NotImplementedException(
    message: String = "Not implemented yet",
) : RuntimeException(message)
