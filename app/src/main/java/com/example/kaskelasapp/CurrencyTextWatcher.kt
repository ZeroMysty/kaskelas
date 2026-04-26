package com.example.kaskelasapp

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import java.text.DecimalFormat

class CurrencyTextWatcher(private val editText: EditText) : TextWatcher {
    private val formatter = DecimalFormat("#,###")
    private var isUpdating = false

    init {
        val symbols = formatter.decimalFormatSymbols
        symbols.groupingSeparator = '.'
        symbols.decimalSeparator = ','
        formatter.decimalFormatSymbols = symbols
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    override fun afterTextChanged(s: Editable?) {
        if (isUpdating) return
        isUpdating = true

        try {
            val originalString = s.toString()
            
            // Allow empty string
            if (originalString.isEmpty()) {
                isUpdating = false
                return
            }

            // Remove formatting to get raw number
            val cleanString = originalString.replace(".", "")
            val parsed = cleanString.toLongOrNull()

            if (parsed != null) {
                val formatted = formatter.format(parsed)
                editText.setText(formatted)
                editText.setSelection(formatted.length)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        isUpdating = false
    }
}
