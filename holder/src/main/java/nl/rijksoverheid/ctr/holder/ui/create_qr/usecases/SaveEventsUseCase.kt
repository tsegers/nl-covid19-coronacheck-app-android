package nl.rijksoverheid.ctr.holder.ui.create_qr.usecases

import nl.rijksoverheid.ctr.holder.persistence.database.HolderDatabase
import nl.rijksoverheid.ctr.holder.persistence.database.entities.EventGroupEntity
import nl.rijksoverheid.ctr.holder.persistence.database.entities.EventType
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteEventsVaccinations
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteTestResult3
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteTestResult2
import java.time.ZoneOffset

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface SaveEventsUseCase {
    suspend fun saveNegativeTest2(negativeTest2: RemoteTestResult2, rawResponse: ByteArray)
    suspend fun saveNegativeTests3(negativeTests3: Map<RemoteTestResult3, ByteArray>)
    suspend fun saveVaccinations(vaccinations: Map<RemoteEventsVaccinations, ByteArray>)
}

class SaveEventsUseCaseImpl(private val holderDatabase: HolderDatabase) : SaveEventsUseCase {

    override suspend fun saveVaccinations(vaccinations: Map<RemoteEventsVaccinations, ByteArray>) {
        // Map remote events to EventGroupEntity to save in the database
        val entities = vaccinations.map {
            EventGroupEntity(
                walletId = 1,
                providerIdentifier = it.key.providerIdentifier ?: error("providerIdentifier is required"),
                type = EventType.Vaccination,
                maxIssuedAt = it.key.events?.map { event -> event.vaccination?.date }
                    ?.maxByOrNull { date -> date?.toEpochDay() ?: error("Date should not be null") }
                    ?.atStartOfDay()?.atOffset(
                        ZoneOffset.UTC
                    ) ?: error("At least one event must be present with a date"),
                jsonData = it.value
            )
        }

        // Save entities in database
        holderDatabase.eventGroupDao().insertAll(entities)
    }

    override suspend fun saveNegativeTest2(negativeTest2: RemoteTestResult2, rawResponse: ByteArray) {
        // Make remote test results to event group entities to save in the database
        val entity = EventGroupEntity(
            walletId = 1,
            providerIdentifier = negativeTest2.providerIdentifier,
            type = EventType.Test,
            maxIssuedAt = negativeTest2.result?.sampleDate!!,
            jsonData = rawResponse
        )

        // Save entity in database
        holderDatabase.eventGroupDao().insertAll(listOf(entity))
    }

    override suspend fun saveNegativeTests3(negativeTests3: Map<RemoteTestResult3, ByteArray>) {
        // Map remote events to EventGroupEntity to save in the database
        val entities = negativeTests3.map {
            EventGroupEntity(
                walletId = 1,
                providerIdentifier = it.key.providerIdentifier ?: error("providerIdentifier is required"),
                type = EventType.Test,
                maxIssuedAt = it.key.events?.map { event -> event.negativeTest?.sampleDate }
                    ?.maxByOrNull { date -> date?.toEpochSecond() ?: error("Date should not be null") }
                    ?: error("At least one event must be present with a date"),
                jsonData = it.value
            )
        }

        // Save entities in database
        holderDatabase.eventGroupDao().insertAll(entities)
    }
}
