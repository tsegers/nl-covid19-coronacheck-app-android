/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.holder.ui.myoverview.items

import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.xwray.groupie.viewbinding.BindableItem
import nl.rijksoverheid.ctr.design.ext.*
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.ItemMyOverviewGreenCardBinding
import nl.rijksoverheid.ctr.holder.persistence.database.entities.CredentialEntity
import nl.rijksoverheid.ctr.holder.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.holder.persistence.database.models.GreenCard
import nl.rijksoverheid.ctr.holder.ui.create_qr.usecases.MyOverviewItem
import nl.rijksoverheid.ctr.holder.ui.create_qr.util.GreenCardUtil
import nl.rijksoverheid.ctr.holder.ui.create_qr.util.OriginState
import nl.rijksoverheid.ctr.holder.ui.create_qr.util.OriginUtil
import nl.rijksoverheid.ctr.holder.ui.myoverview.utils.TestResultAdapterItemUtil
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import kotlin.math.ceil

class MyOverviewGreenCardAdapterItem(
    private val greenCard: GreenCard,
    private val originStates: List<OriginState>,
    private val credentialState: MyOverviewItem.GreenCardItem.CredentialState,
    private val launchDate: OffsetDateTime,
    private val onButtonClick: (greenCard: GreenCard, credential: CredentialEntity) -> Unit,
) :
    BindableItem<ItemMyOverviewGreenCardBinding>(R.layout.item_my_overview_green_card.toLong()),
    KoinComponent {

    private val testResultAdapterItemUtil: TestResultAdapterItemUtil by inject()
    private val greenCardUtil: GreenCardUtil by inject()
    private val originUtil: OriginUtil by inject()

    override fun bind(viewBinding: ItemMyOverviewGreenCardBinding, position: Int) {
        applyStyling(
            viewBinding = viewBinding
        )

        setContent(
            viewBinding = viewBinding
        )

        viewBinding.button.setOnClickListener {
            if (credentialState is MyOverviewItem.GreenCardItem.CredentialState.HasCredential) {
                onButtonClick.invoke(greenCard, credentialState.credential)
            }
        }
    }

    private fun applyStyling(viewBinding: ItemMyOverviewGreenCardBinding) {
        val context = viewBinding.root.context
        when (greenCard.greenCardEntity.type) {
            is GreenCardType.Eu -> {
                viewBinding.typeTitle.apply {
                    text = context.getString(R.string.validity_type_european_title)
                    setTextColor(ContextCompat.getColor(context, R.color.darkened_blue))
                }
                viewBinding.button.setEnabledButtonColor(R.color.darkened_blue)
                viewBinding.imageView.setImageResource(R.drawable.illustration_hand_qr_eu)
            }
            is GreenCardType.Domestic -> {
                viewBinding.typeTitle.apply {
                    text = context.getString(R.string.validity_type_dutch_title)
                    setTextColor(ContextCompat.getColor(context, R.color.primary_blue))
                }
                viewBinding.button.setEnabledButtonColor(R.color.primary_blue)
                viewBinding.imageView.setImageResource(R.drawable.illustration_hand_qr_nl)
            }
        }

        // Check enabling button
        viewBinding.button.isEnabled = credentialState is MyOverviewItem.GreenCardItem.CredentialState.HasCredential
    }

    private fun setContent(viewBinding: ItemMyOverviewGreenCardBinding) {
        val context = viewBinding.root.context

        viewBinding.proof1Title.visibility = View.GONE
        viewBinding.proof1Subtitle.visibility = View.GONE
        viewBinding.proof2Title.visibility = View.GONE
        viewBinding.proof2Subtitle.visibility = View.GONE
        viewBinding.proof3Title.visibility = View.GONE
        viewBinding.proof3Subtitle.visibility = View.GONE
        viewBinding.proof1Subtitle.setTextColor(context.getThemeColor(android.R.attr.textColorPrimary))
        viewBinding.proof2Subtitle.setTextColor(context.getThemeColor(android.R.attr.textColorPrimary))
        viewBinding.proof3Subtitle.setTextColor(context.getThemeColor(android.R.attr.textColorPrimary))
        viewBinding.launchText.text = ""
        viewBinding.launchText.visibility = View.GONE

        when (greenCard.greenCardEntity.type) {
            is GreenCardType.Eu -> {
                // European card only has one origin
                val originState = originStates.first()
                val origin = originState.origin
                when (origin.type) {
                    is OriginType.Test -> {
                        setOriginTitle(
                            textView = viewBinding.proof1Title,
                            originState = originState,
                            title = context.getString(R.string.qr_card_test_title_eu)
                        )

                        setOriginSubtitle(
                            textView = viewBinding.proof1Subtitle,
                            originState = originState,
                            subtitle = origin.eventTime.formatDateTime(context),
                        )
                    }
                    is OriginType.Vaccination -> {
                        setOriginTitle(
                            textView = viewBinding.proof1Title,
                            originState = originState,
                            title = context.getString(R.string.qr_card_vaccination_title_eu)
                        )

                        setOriginSubtitle(
                            textView = viewBinding.proof1Subtitle,
                            originState = originState,
                            subtitle = origin.eventTime.toLocalDate().formatDayMonthYear(),
                        )
                    }

                    is OriginType.Recovery -> {
                        setOriginTitle(
                            textView = viewBinding.proof1Title,
                            originState = originState,
                            title = context.getString(R.string.qr_card_recovery_title_eu)
                        )

                        setOriginSubtitle(
                            textView = viewBinding.proof1Subtitle,
                            originState = originState,
                            subtitle = origin.eventTime.toLocalDate().formatDayMonthYear(),
                        )
                    }

                }

                if (launchDate.isAfter(OffsetDateTime.now(ZoneOffset.UTC))) {
                    viewBinding.launchText.text = context.getString(R.string.qr_card_validity_eu, launchDate.formatDayMonth())
                    viewBinding.launchText.visibility = View.VISIBLE
                }
            }
            is GreenCardType.Domestic -> {
                originStates.forEach { originState ->
                    val origin = originState.origin
                    when (origin.type) {
                        is OriginType.Test -> {
                            setOriginTitle(
                                textView = viewBinding.proof3Title,
                                originState = originState,
                                title = context.getString(R.string.qr_card_test_domestic)
                            )

                            setOriginSubtitle(
                                textView = viewBinding.proof3Subtitle,
                                originState = originState,
                                subtitle = context.getString(
                                    R.string.qr_card_validity_valid,
                                    origin.expirationTime.formatDateTime(context)
                                )
                            )
                        }
                        is OriginType.Vaccination -> {
                            setOriginTitle(
                                textView = viewBinding.proof1Title,
                                originState = originState,
                                title = context.getString(R.string.qr_card_vaccination_title_domestic)
                            )

                            setOriginSubtitle(
                                textView = viewBinding.proof1Subtitle,
                                originState = originState,
                                subtitle = context.getString(
                                    R.string.qr_card_validity_valid,
                                    origin.expirationTime.toLocalDate().formatDayMonthYear()
                                )
                            )
                        }
                        is OriginType.Recovery -> {
                            setOriginTitle(
                                textView = viewBinding.proof2Title,
                                originState = originState,
                                title = context.getString(R.string.qr_card_recovery_title_domestic)
                            )

                            setOriginSubtitle(
                                textView = viewBinding.proof2Subtitle,
                                originState = originState,
                                subtitle = context.getString(
                                    R.string.qr_card_validity_valid,
                                    origin.expirationTime.formatDayMonth()
                                )
                            )
                        }
                    }
                }

                // If there is only one origin we can show a countdown if the green card almost expires
                when (val expireCountDownResult =
                    testResultAdapterItemUtil.getExpireCountdownText(expireDate = greenCardUtil.getExpireDate(greenCard))) {
                    is TestResultAdapterItemUtil.ExpireCountDown.Hide -> {
                        viewBinding.expiresIn.visibility = View.GONE
                    }
                    is TestResultAdapterItemUtil.ExpireCountDown.Show -> {
                        viewBinding.expiresIn.visibility = View.VISIBLE
                        if (expireCountDownResult.hoursLeft == 0L) {
                            viewBinding.expiresIn.text = context.getString(
                                R.string.my_overview_test_result_expires_in_minutes,
                                expireCountDownResult.minutesLeft.toString()
                            )
                        } else {
                            viewBinding.expiresIn.text = context.getString(
                                R.string.my_overview_test_result_expires_in_hours_minutes,
                                expireCountDownResult.hoursLeft.toString(),
                                expireCountDownResult.minutesLeft.toString()
                            )
                        }
                    }
                }
            }
        }
    }

    private fun setOriginTitle(
        textView: TextView,
        originState: OriginState,
        title: String
    ) {
        // Small hack, but if the subtitle is not present we remove the ":" from the title copy since that doesn't make sense
        textView.text = if (!originUtil.presentSubtitle(greenCard.greenCardEntity.type, originState)) title.replace(":", "") else title
        textView.visibility = View.VISIBLE
    }

    private fun setOriginSubtitle(
        textView: TextView,
        originState: OriginState,
        subtitle: String,
    ) {
        val context = textView.context

        when {
            !originUtil.presentSubtitle(
                greenCardType = greenCard.greenCardEntity.type,
                originState = originState) -> {
                    textView.text = ""
            }
            originState is OriginState.Future || (greenCard.greenCardEntity.type == GreenCardType.Eu&& this.launchDate.isAfter(OffsetDateTime.now(ZoneOffset.UTC))) -> {
                val realValidFrom = if (this.launchDate.isAfter(OffsetDateTime.now(ZoneOffset.UTC))) this.launchDate else originState.origin.validFrom
                textView.setTextColor(ContextCompat.getColor(context, R.color.link))

                val daysBetween = ceil(ChronoUnit.HOURS.between(OffsetDateTime.now(ZoneOffset.UTC), realValidFrom) / 24.0).toInt()
                if (daysBetween == 1) {
                    textView.text = context.getString(R.string.qr_card_validity_future_day, daysBetween.toString())
                } else {
                    textView.text = context.getString(R.string.qr_card_validity_future_days, daysBetween.toString())
                }

                textView.visibility = View.VISIBLE
            }
            originState is OriginState.Valid -> {
                textView.text = subtitle
                textView.visibility = View.VISIBLE
            }
            originState is OriginState.Expired -> {
                // Should be filtered out and never reach here
            }
        }
    }

    override fun getLayout(): Int {
        return R.layout.item_my_overview_green_card
    }

    override fun initializeViewBinding(view: View): ItemMyOverviewGreenCardBinding {
        return ItemMyOverviewGreenCardBinding.bind(view)
    }
}
