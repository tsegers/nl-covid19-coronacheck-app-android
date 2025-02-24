package nl.rijksoverheid.ctr.holder.ui.create_qr.repositories

import nl.rijksoverheid.ctr.api.interceptors.SigningCertificate
import nl.rijksoverheid.ctr.holder.ui.create_qr.api.TestProviderApiClient
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteEventsVaccinations
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteTestResult3
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteUnomi
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.SignedResponseWithModel

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface EventProviderRepository {
    suspend fun unomiVaccinationEvents(
        url: String,
        token: String,
        signingCertificateBytes: ByteArray
    ): RemoteUnomi

    suspend fun unomiTestEvents(
        url: String,
        token: String,
        signingCertificateBytes: ByteArray
    ): RemoteUnomi

    suspend fun vaccinationEvents(
        url: String,
        token: String,
        signingCertificateBytes: ByteArray
    ): SignedResponseWithModel<RemoteEventsVaccinations>

    suspend fun negativeTestEvent(
        url: String,
        token: String,
        signingCertificateBytes: ByteArray
    ): SignedResponseWithModel<RemoteTestResult3>
}

class EventProviderRepositoryImpl(
    private val testProviderApiClient: TestProviderApiClient
) : EventProviderRepository {

    override suspend fun unomiTestEvents(url: String, token: String, signingCertificateBytes: ByteArray): RemoteUnomi {
        return testProviderApiClient
            .unomiTestEvents(
                url = url,
                authorization = "Bearer $token",
                certificate = SigningCertificate(signingCertificateBytes)
            ).model
    }

    override suspend fun unomiVaccinationEvents(url: String, token: String, signingCertificateBytes: ByteArray): RemoteUnomi {
        return testProviderApiClient
            .unomiVaccinationEvents(
                url = url,
                authorization = "Bearer $token",
                certificate = SigningCertificate(signingCertificateBytes)
            ).model
    }

    override suspend fun vaccinationEvents(
        url: String,
        token: String,
        signingCertificateBytes: ByteArray
    ): SignedResponseWithModel<RemoteEventsVaccinations> {
        return testProviderApiClient.vaccinationEvents(
            url = url,
            authorization = "Bearer $token",
            certificate = SigningCertificate(signingCertificateBytes)
        )
    }

    override suspend fun negativeTestEvent(
        url: String,
        token: String,
        signingCertificateBytes: ByteArray
    ): SignedResponseWithModel<RemoteTestResult3> {
        return testProviderApiClient.negativeTestEvents(
            url = url,
            authorization = "Bearer $token",
            certificate = SigningCertificate(signingCertificateBytes)
        )
    }
}
