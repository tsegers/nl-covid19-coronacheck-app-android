package nl.rijksoverheid.ctr.verifier

import mobilecore.Result
import nl.rijksoverheid.ctr.appconfig.AppConfigViewModel
import nl.rijksoverheid.ctr.appconfig.CachedAppConfigUseCase
import nl.rijksoverheid.ctr.appconfig.api.model.AppConfig
import nl.rijksoverheid.ctr.appconfig.models.AppStatus
import nl.rijksoverheid.ctr.introduction.IntroductionViewModel
import nl.rijksoverheid.ctr.introduction.ui.new_terms.models.NewTerms
import nl.rijksoverheid.ctr.introduction.ui.status.models.IntroductionStatus
import nl.rijksoverheid.ctr.shared.MobileCoreWrapper
import nl.rijksoverheid.ctr.shared.livedata.Event
import nl.rijksoverheid.ctr.shared.models.DomesticCredential
import nl.rijksoverheid.ctr.shared.models.ReadDomesticCredential
import nl.rijksoverheid.ctr.shared.models.TestResultAttributes
import nl.rijksoverheid.ctr.shared.utils.TestResultUtil
import nl.rijksoverheid.ctr.verifier.ui.scanner.ScannerViewModel
import nl.rijksoverheid.ctr.verifier.ui.scanner.models.VerifiedQr
import nl.rijksoverheid.ctr.verifier.ui.scanner.models.VerifiedQrResultState
import nl.rijksoverheid.ctr.verifier.ui.scanner.usecases.TestResultValidUseCase
import nl.rijksoverheid.ctr.verifier.ui.scanner.usecases.VerifyQrUseCase
import nl.rijksoverheid.ctr.verifier.ui.scanner.utils.QrCodeUtil
import nl.rijksoverheid.ctr.verifier.ui.scanqr.ScanQrViewModel
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.BufferedSource
import org.json.JSONObject
import java.time.OffsetDateTime

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

fun fakeAppConfigViewModel(appStatus: AppStatus = AppStatus.NoActionRequired) =
    object : AppConfigViewModel() {
        override fun refresh(mobileCoreWrapper: MobileCoreWrapper) {
            appStatusLiveData.value = appStatus
        }
    }

fun fakeIntroductionViewModel(
    introductionStatus: IntroductionStatus = IntroductionStatus.IntroductionFinished.NoActionRequired,
): IntroductionViewModel {
    return object : IntroductionViewModel() {
        override fun getIntroductionStatus(): IntroductionStatus {
            return introductionStatus
        }

        override fun saveIntroductionFinished(newTerms: NewTerms?) {

        }
    }
}

fun fakeScanQrViewModel(
    scanInstructionsSeen: Boolean
) = object : ScanQrViewModel() {
    override fun scanInstructionsSeen(): Boolean {
        return scanInstructionsSeen
    }
}

fun fakeScannerViewModel(
    verifiedQrResultState: VerifiedQrResultState
) = object : ScannerViewModel() {
    override fun validate(qrContent: String) {
        verifiedQrResultStateLiveData.value = Event(verifiedQrResultState)
    }
}

fun fakeTestResultValidUseCase(
    result: VerifiedQrResultState = VerifiedQrResultState.Valid(
        verifiedQr = fakeVerifiedQr()
    )
) = object : TestResultValidUseCase {
    override suspend fun validate(qrContent: String): VerifiedQrResultState {
        return result
    }
}

fun fakeQrCodeUtil(
    isValid: Boolean = true
) = object : QrCodeUtil {
    override fun isValid(creationDate: OffsetDateTime, isPaperProof: String): Boolean {
        return isValid
    }
}

fun fakeVerifiedQr(
    isSpecimen: String = "0",
    birthDay: String = "dummy",
    birthMonth: String = "dummy",
    firstNameInitial: String = "dummy",
    lastNameInitial: String = "dummy",
    isPaperProof: String = "0"
) = VerifiedQr(
    creationDateSeconds = 0,
    testResultAttributes = TestResultAttributes(
        birthDay = birthDay,
        birthMonth = birthMonth,
        firstNameInitial = firstNameInitial,
        lastNameInitial = lastNameInitial,
        isSpecimen = isSpecimen,
        isNLDCC = "1",
        credentialVersion = "1",
        isPaperProof = "0",
        validForHours = "24",
        validFrom = "1622633766",
    )
)

fun fakeVerifyQrUseCase(
    isNLDCC: String = "0",
    isSpecimen: String = "0",
    result: VerifyQrUseCase.VerifyQrResult = VerifyQrUseCase.VerifyQrResult.Success(
        verifiedQr = VerifiedQr(
            creationDateSeconds = 0,
            testResultAttributes = TestResultAttributes(
                birthDay = "dummy",
                birthMonth = "dummy",
                firstNameInitial = "dummy",
                lastNameInitial = "dummy",
                isSpecimen = isSpecimen,
                isNLDCC = isNLDCC,
                credentialVersion = "1",
                isPaperProof = "0",
                validForHours = "24",
                validFrom = "1622633766",
            )
        )
    )
) = object : VerifyQrUseCase {
    override suspend fun get(content: String): VerifyQrUseCase.VerifyQrResult {
        return result
    }
}

fun fakeTestResultUtil(
    isValid: Boolean = true
) = object : TestResultUtil {
    override fun isValid(sampleDate: OffsetDateTime, validitySeconds: Long): Boolean {
        return isValid
    }
}

fun fakeCachedAppConfigUseCase(
    appConfig: AppConfig = AppConfig(
        minimumVersion = 0,
        appDeactivated = false,
        informationURL = "dummy",
        configTtlSeconds = 0,
        maxValidityHours = 0,
        euLaunchDate = "",
        credentialRenewalDays = 0,
        domesticCredentialValidity = 0,
        testEventValidity = 0,
        recoveryEventValidity = 0,
        temporarilyDisabled = false,
        requireUpdateBefore = 0
    ),
    publicKeys: BufferedSource = "{\"cl_keys\":[]}".toResponseBody("application/json".toMediaType()).source()
): CachedAppConfigUseCase = object : CachedAppConfigUseCase {

    override fun getCachedAppConfig(): AppConfig {
        return appConfig
    }

    override fun getCachedAppConfigMaxValidityHours(): Int {
        return appConfig.maxValidityHours
    }

    override fun getCachedAppConfigVaccinationEventValidity(): Int {
        return appConfig.vaccinationEventValidity
    }

    override fun getCachedPublicKeys(): BufferedSource {
        return publicKeys
    }

    override fun getProviderName(providerIdentifier: String?): String {
        return ""
    }
}

fun fakeMobileCoreWrapper(): MobileCoreWrapper {
    return object : MobileCoreWrapper {
        override fun loadDomesticIssuerPks(bytes: ByteArray) {
        }

        override fun createCredentials(body: ByteArray): String {
            return ""
        }

        override fun readCredential(credentials: ByteArray): ByteArray {
            return ByteArray(0)
        }

        override fun createCommitmentMessage(secretKey: ByteArray, nonce: ByteArray): String {
            return ""
        }

        override fun disclose(secretKey: ByteArray, credential: ByteArray): String {
            return ""
        }

        override fun generateHolderSk(): String {
            return ""
        }

        override fun createDomesticCredentials(createCredentials: ByteArray): List<DomesticCredential> {
            return listOf()
        }

        override fun readEuropeanCredential(credential: ByteArray): JSONObject {
            return JSONObject()
        }

        override fun initializeVerifier(configFilesPath: String) = ""

        override fun verify(credential: ByteArray): Result {
            TODO("Not yet implemented")
        }

        override fun readDomesticCredential(credential: ByteArray): ReadDomesticCredential {
            return ReadDomesticCredential(
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                ""
            )
        }
    }
}

