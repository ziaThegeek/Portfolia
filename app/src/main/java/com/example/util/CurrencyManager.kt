package com.example.util

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

data class AppCurrency(
    val code: String,
    val symbol: String,
    val name: String,
    val isPrefix: Boolean = true
)

object CurrencyManager {
    private const val PREFS_NAME = "portfolia_currency_prefs"
    private const val KEY_CURRENCY_CODE = "selected_currency_code"

    val AVAILABLE_CURRENCIES = listOf(
        AppCurrency("PKR", "Rs. ", "Pakistani Rupee (PKR)", isPrefix = true),
        AppCurrency("USD", "$", "US Dollar (USD)", isPrefix = true),
        AppCurrency("EUR", "€", "Euro (EUR)", isPrefix = true),
        AppCurrency("GBP", "£", "British Pound (GBP)", isPrefix = true),
        AppCurrency("INR", "₹", "Indian Rupee (INR)", isPrefix = true),
        AppCurrency("AED", "AED ", "UAE Dirham (AED)", isPrefix = true),
        AppCurrency("SAR", "SAR ", "Saudi Riyal (SAR)", isPrefix = true),
        AppCurrency("CAD", "CA$ ", "Canadian Dollar (CAD)", isPrefix = true),
        AppCurrency("AUD", "AU$ ", "Australian Dollar (AUD)", isPrefix = true),
        AppCurrency("JPY", "¥", "Japanese Yen (JPY)", isPrefix = true)
    )

    // Default to PKR as requested by the user
    private val DEFAULT_CURRENCY = AVAILABLE_CURRENCIES[0] // PKR

    private val _currentCurrency = MutableStateFlow(DEFAULT_CURRENCY)
    val currentCurrency: StateFlow<AppCurrency> = _currentCurrency.asStateFlow()

    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        val p = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs = p
        val savedCode = p.getString(KEY_CURRENCY_CODE, "PKR") ?: "PKR"
        val found = AVAILABLE_CURRENCIES.find { it.code.equals(savedCode, ignoreCase = true) }
            ?: DEFAULT_CURRENCY
        _currentCurrency.value = found
    }

    fun setCurrency(currency: AppCurrency) {
        _currentCurrency.value = currency
        prefs?.edit()?.putString(KEY_CURRENCY_CODE, currency.code)?.apply()
    }

    fun format(
        amount: Double,
        currency: AppCurrency = _currentCurrency.value,
        decimals: Int = 0,
        includeSignForPositive: Boolean = false
    ): String {
        val isNegative = amount < 0
        val absAmount = kotlin.math.abs(amount)

        val symbols = DecimalFormatSymbols(Locale.US)
        val pattern = if (decimals > 0) {
            "#,##0." + "0".repeat(decimals)
        } else {
            "#,##0"
        }
        val df = DecimalFormat(pattern, symbols)
        val formattedNumber = df.format(absAmount)

        val sign = when {
            isNegative -> "-"
            includeSignForPositive && amount > 0 -> "+"
            else -> ""
        }

        return if (currency.isPrefix) {
            "$sign${currency.symbol}$formattedNumber"
        } else {
            "$sign$formattedNumber ${currency.symbol}".trim()
        }
    }

    fun formatPrecise(
        amount: Double,
        currency: AppCurrency = _currentCurrency.value,
        includeSignForPositive: Boolean = false
    ): String {
        return format(amount, currency, decimals = 2, includeSignForPositive = includeSignForPositive)
    }

    fun formatCompact(
        amount: Double,
        currency: AppCurrency = _currentCurrency.value
    ): String {
        val abs = kotlin.math.abs(amount)
        val sign = if (amount < 0) "-" else ""
        val (numStr, unit) = when {
            abs >= 1_000_000_000 -> String.format(Locale.US, "%.1fB", abs / 1_000_000_000) to ""
            abs >= 1_000_000 -> String.format(Locale.US, "%.1fM", abs / 1_000_000) to ""
            abs >= 1_000 -> String.format(Locale.US, "%.1fK", abs / 1_000) to ""
            else -> String.format(Locale.US, "%.0f", abs) to ""
        }

        return if (currency.isPrefix) {
            "$sign${currency.symbol}$numStr$unit"
        } else {
            "$sign$numStr$unit ${currency.symbol}".trim()
        }
    }
}
