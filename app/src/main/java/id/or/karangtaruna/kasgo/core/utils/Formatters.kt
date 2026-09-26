package id.or.karangtaruna.kasgo.core.utils

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Formatters {
    private val localeId = Locale("id", "ID")

    fun formatRupiah(amount: Long): String {
        return try {
            val format = NumberFormat.getCurrencyInstance(localeId)
            format.maximumFractionDigits = 0
            val formatted = format.format(amount)
            formatted.replace("Rp", "Rp ")
        } catch (_: Exception) {
            "Rp $amount"
        }
    }

    fun formatTanggal(millis: Long): String {
        return try {
            val sdf = SimpleDateFormat("d MMM yyyy", localeId)
            sdf.format(Date(millis))
        } catch (_: Exception) {
            SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date(millis))
        }
    }

    fun formatPeriode(millis: Long): String {
        return try {
            val sdf = SimpleDateFormat("MMMM yyyy", localeId)
            sdf.format(Date(millis))
        } catch (_: Exception) {
            SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date(millis))
        }
    }

    fun formatTanggalDanJam(millis: Long): String {
        return try {
            val sdf = SimpleDateFormat("d MMM yyyy, HH:mm", localeId)
            "${sdf.format(Date(millis))} WIB"
        } catch (_: Exception) {
            val sdf = SimpleDateFormat("d MMM yyyy, HH:mm", Locale.getDefault())
            "${sdf.format(Date(millis))} WIB"
        }
    }
}
