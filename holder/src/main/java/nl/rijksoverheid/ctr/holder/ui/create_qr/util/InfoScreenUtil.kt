package nl.rijksoverheid.ctr.holder.ui.create_qr.util

import android.app.Application
import nl.rijksoverheid.ctr.appconfig.CachedAppConfigUseCase
import nl.rijksoverheid.ctr.design.ext.formatDateTime
import nl.rijksoverheid.ctr.design.ext.formatDayMonthYear
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteTestResult3
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteEventsVaccinations
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteTestResult2
import nl.rijksoverheid.ctr.shared.ext.getStringOrNull
import nl.rijksoverheid.ctr.shared.models.PersonalDetails
import org.json.JSONObject
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

interface InfoScreenUtil {
    fun getForRemoteTestResult2(result: RemoteTestResult2.Result,
                                personalDetails: PersonalDetails,
                                testDate: String): InfoScreen
    fun getForRemoteTestResult3(event: RemoteTestResult3.Event,
                                fullName: String,
                                testDate: String,
                                validUntil: String,
                                birthDate: String): InfoScreen
    fun getForRemoteVaccination(event: RemoteEventsVaccinations.Event,
                                fullName: String,
                                birthDate: String): InfoScreen
    fun getForDomesticQr(personalDetails: PersonalDetails): InfoScreen
    fun getForEuropeanTestQr(readEuropeanCredential: JSONObject): InfoScreen
    fun getForEuropeanVaccinationQr(readEuropeanCredential: JSONObject): InfoScreen
}

class InfoScreenUtilImpl(private val application: Application,
                         private val cachedAppConfigUseCase: CachedAppConfigUseCase): InfoScreenUtil {
    override fun getForRemoteTestResult2(
        result: RemoteTestResult2.Result,
        personalDetails: PersonalDetails,
        testDate: String): InfoScreen {
        val testType = cachedAppConfigUseCase.getCachedAppConfig()?.nlTestTypes?.firstOrNull {
            it.code == result.testType
        }?.name ?: result.testType

        val title = application.getString(R.string.your_test_result_explanation_toolbar_title)
        val description = application.getString(R.string.your_test_result_explanation_description,
            "${personalDetails.firstNameInitial} ${personalDetails.lastNameInitial} ${personalDetails.birthDay} ${personalDetails.birthMonth}",
            result.testType,
            testDate,
            application.getString(R.string.your_test_result_explanation_negative_test_result),
            result.unique
        )

        return InfoScreen(
            title = title,
            description = description
        )
    }

    override fun getForRemoteTestResult3(
        event: RemoteTestResult3.Event,
        fullName: String,
        testDate: String,
        validUntil: String,
        birthDate: String
    ): InfoScreen {

        val testType = cachedAppConfigUseCase.getCachedAppConfig()?.euTestTypes?.firstOrNull {
            it.code == event.negativeTest?.type
        }?.name ?: event.negativeTest?.type ?: ""

        val testName = event.negativeTest?.name ?: ""

        val testLocation = event.negativeTest?.facility ?: ""

        val testManifacturer = cachedAppConfigUseCase.getCachedAppConfig()?.euTestManufacturers?.firstOrNull {
            it.code == event.negativeTest?.manufacturer
        }?.name ?: event.negativeTest?.manufacturer ?: ""

        val unique = event.unique ?: ""

        val title = application.getString(R.string.your_test_result_explanation_toolbar_title)
        val description = application.getString(
            R.string.your_test_result_3_0_explanation_description,
            fullName,
            birthDate,
            testType,
            testName,
            testDate,
            application.getString(R.string.your_test_result_explanation_negative_test_result),
            testLocation,
            testManifacturer,
            unique
        )

        return InfoScreen(
            title = title,
            description = description
        )
    }

    override fun getForRemoteVaccination(
        event: RemoteEventsVaccinations.Event,
        fullName: String,
        birthDate: String
    ): InfoScreen {
        val title = application.getString(R.string.your_vaccination_explanation_toolbar_title)

        val desease = application.getString(R.string.your_vaccination_explanation_covid_19)

        val hpkCode = cachedAppConfigUseCase.getCachedAppConfig()?.hpkCodes?.firstOrNull {
            it.code == event.vaccination?.hpkCode }?.name ?: event.vaccination?.hpkCode ?: ""

        val brand = cachedAppConfigUseCase.getCachedAppConfig()?.euBrands?.firstOrNull {
            it.code == event.vaccination?.brand }?.name ?: event.vaccination?.brand ?: ""

        val vaccin = when {
            hpkCode.isNotEmpty() -> hpkCode
            brand.isNotEmpty() -> brand
            else -> ""
        }

        val vaccinType = cachedAppConfigUseCase.getCachedAppConfig()?.euVaccinations?.firstOrNull {
            it.code == event.vaccination?.type }?.name ?: event.vaccination?.type ?: ""

        val producer = cachedAppConfigUseCase.getCachedAppConfig()?.euManufacturers?.firstOrNull {
            it.code == event.vaccination?.manufacturer }?.name ?: event.vaccination?.manufacturer ?: ""

        val doses = if (event.vaccination?.doseNumber != null && event.vaccination?.totalDoses != null) {
            application.getString(R.string.your_vaccination_explanation_doses, event.vaccination?.doseNumber, event.vaccination?.totalDoses)
        } else ""

        val vaccinationDate = event.vaccination?.date?.let { it.formatDayMonthYear() } ?: ""
        val vaccinationCountry = event.vaccination?.country ?: ""
        val uniqueCode = event.unique ?: ""

        return InfoScreen(
            title = title,
            description = application.getString(
                R.string.your_vaccination_explanation_description,
                fullName,
                birthDate,
                desease,
                vaccin,
                vaccinType,
                producer,
                doses,
                vaccinationDate,
                vaccinationCountry,
                uniqueCode
            )
        )

    }

    override fun getForDomesticQr(personalDetails: PersonalDetails): InfoScreen {
        val title = application.getString(R.string.qr_explanation_title_domestic)
        val description = application.getString(R.string.qr_explanation_description_domestic,
            "${personalDetails.firstNameInitial} ${personalDetails.lastNameInitial} ${personalDetails.birthDay} ${personalDetails.birthMonth}")

        return InfoScreen(
            title = title,
            description = description
        )
    }

    override fun getForEuropeanTestQr(readEuropeanCredential: JSONObject): InfoScreen {
        val dcc = readEuropeanCredential.optJSONObject("dcc")
        val test = dcc.getJSONArray("t").optJSONObject(0)

        val title = application.getString(R.string.qr_explanation_title_eu)

        val fullName = "${dcc.optJSONObject("nam").getStringOrNull("fn")}, ${dcc.optJSONObject("nam").getStringOrNull("gn")}"

        val birthDate = dcc.getStringOrNull("dob")?.let { birthDate ->
            try {
                LocalDate.parse(birthDate, DateTimeFormatter.ISO_DATE).formatDayMonthYear()
            } catch (e: Exception) {
                ""
            }
        } ?: ""

        val desease = application.getString(R.string.your_vaccination_explanation_covid_19)

        val testType = cachedAppConfigUseCase.getCachedAppConfig()?.euTestTypes?.firstOrNull {
            it.code == test.getStringOrNull("tt") }?.name ?: test.getStringOrNull("tt") ?: ""

        val testName = test.getStringOrNull("nm") ?: ""

        val testDate = test.getStringOrNull("sc")?.let {
            try {
                OffsetDateTime.parse(it, DateTimeFormatter.ISO_OFFSET_DATE_TIME).formatDateTime(application)
            } catch (e: Exception) {
                ""
            }
        } ?: ""

        val testResult = application.getString(R.string.your_test_result_explanation_negative_test_result)

        val testLocation = test.getStringOrNull("tc") ?: ""

        val manufacturer = cachedAppConfigUseCase.getCachedAppConfig()?.euManufacturers?.firstOrNull {
            it.code == test.getStringOrNull("ma") }?.name ?: test.getStringOrNull("ma") ?: ""

        val vaccinationCountry = test.getStringOrNull("co")
        val uniqueCode = test.getStringOrNull("ci")

        val description = application.getString(
            R.string.qr_explanation_description_eu_test,
            fullName,
            birthDate,
            desease,
            testType,
            testName,
            testDate,
            testResult,
            testLocation,
            manufacturer,
            vaccinationCountry,
            uniqueCode)

        return InfoScreen(
            title = title,
            description = description
        )
    }

    override fun getForEuropeanVaccinationQr(readEuropeanCredential: JSONObject): InfoScreen {
        val dcc = readEuropeanCredential.optJSONObject("dcc")
        val vaccination = dcc.getJSONArray("v").optJSONObject(0)

        val title = application.getString(R.string.qr_explanation_title_eu)

        val fullName = "${dcc.optJSONObject("nam").getStringOrNull("fn")}, ${dcc.optJSONObject("nam").getStringOrNull("gn")}"

        val birthDate = dcc.getStringOrNull("dob")?.let { birthDate ->
            try {
                LocalDate.parse(birthDate, DateTimeFormatter.ISO_DATE).formatDayMonthYear()
            } catch (e: Exception) {
                ""
            }
        } ?: ""

        val desease = application.getString(R.string.your_vaccination_explanation_covid_19)

        val vaccin = cachedAppConfigUseCase.getCachedAppConfig()?.euBrands?.firstOrNull {
            it.code == vaccination.getStringOrNull("mp") }?.name ?: vaccination.getStringOrNull("mp") ?: ""

        val vaccinType = cachedAppConfigUseCase.getCachedAppConfig()?.euVaccinations?.firstOrNull {
            it.code == vaccination.getStringOrNull("vp") }?.name ?: vaccination.getStringOrNull("vp") ?: ""

        val manufacturer = cachedAppConfigUseCase.getCachedAppConfig()?.euManufacturers?.firstOrNull {
            it.code == vaccination.getStringOrNull("ma") }?.name ?: vaccination.getStringOrNull("ma") ?: ""

        val doses = if (vaccination.getStringOrNull("dn") != null && vaccination.getStringOrNull("sd") != null) {
            application.getString(R.string.your_vaccination_explanation_doses, vaccination.getStringOrNull("dn"), vaccination.getStringOrNull("sd"))
        } else ""

        val vaccinationDate = vaccination.getStringOrNull("dt")?.let { vaccinationDate ->
            try {
                LocalDate.parse(vaccinationDate, DateTimeFormatter.ISO_DATE).formatDayMonthYear()
            } catch (e: Exception) {
                ""
            }
        } ?: ""

        val vaccinationCountry = vaccination.getStringOrNull("co")
        val uniqueCode = vaccination.getStringOrNull("ci")

        return InfoScreen(
            title = title,
            description = application.getString(
                R.string.qr_explanation_description_eu_vaccination,
                fullName,
                birthDate,
                desease,
                vaccin,
                vaccinType,
                manufacturer,
                doses,
                vaccinationDate,
                vaccinationCountry,
                uniqueCode
            )
        )
    }

}

data class InfoScreen(
    val title: String,
    val description: String
)