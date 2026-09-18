package org.ukrida.labvora.util

import android.content.Context
import org.ukrida.labvora.data.model.TestHistoryItem

// Menyimpan ID + status pesanan yang sudah pernah dilihat user (per akun).
// Stripe/pill "baru" hanya tampil sekali: saat Status Pesanan dibuka pertama
// kali setelah memesan / setelah status berubah, kunjungan berikutnya hilang.
object SeenOrderStore {
    private const val PREFS = "labvora_seen_orders"

    private fun key(userId: Int) = "seen_ids_$userId"
    private fun statusKey(userId: Int) = "seen_status_$userId"

    private fun readStatusMap(context: Context, userId: Int): Map<Int, String> {
        val raw = context
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getStringSet(statusKey(userId), emptySet())
            .orEmpty()
        val map = mutableMapOf<Int, String>()
        raw.forEach { entry ->
            val sep = entry.indexOf('|')
            if (sep > 0) {
                entry.substring(0, sep).toIntOrNull()?.let { id ->
                    map[id] = entry.substring(sep + 1)
                }
            }
        }
        return map
    }

    // ID yang belum pernah dilihat sama sekali -> pill "Terbaru dipesan".
    fun getNewIds(
        context: Context,
        userId: Int,
        items: List<TestHistoryItem>
    ): List<Int> {
        if (userId <= 0 || items.isEmpty()) return emptyList()
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val seen = prefs.getStringSet(key(userId), emptySet()).orEmpty()
        val statusMap = readStatusMap(context, userId)
        return items.map { it.id }.distinct()
            .filter { id -> !seen.contains(id.toString()) && !statusMap.containsKey(id) }
    }

    // ID baru + ID yang statusnya berubah sejak terakhir dilihat -> stripe + badge.
    fun getFreshIds(
        context: Context,
        userId: Int,
        items: List<TestHistoryItem>
    ): List<Int> {
        if (userId <= 0 || items.isEmpty()) return emptyList()
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val seen = prefs.getStringSet(key(userId), emptySet()).orEmpty()
        val statusMap = readStatusMap(context, userId)
        return items.distinctBy { it.id }
            .filter { item ->
                val idStr = item.id.toString()
                when {
                    // Belum pernah dilihat -> baru
                    !seen.contains(idStr) && !statusMap.containsKey(item.id) -> true
                    // Pernah dilihat, tapi status pindah (misal Menunggu -> Dikonfirmasi) -> baru lagi
                    statusMap[item.id] != null && statusMap[item.id] != item.status -> true
                    else -> false
                }
            }
            .map { it.id }
    }

    fun getUnseenIds(context: Context, userId: Int, allIds: List<Int>): List<Int> {
        if (userId <= 0 || allIds.isEmpty()) return emptyList()
        val seen = context
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getStringSet(key(userId), emptySet())
            .orEmpty()
        return allIds.filter { !seen.contains(it.toString()) }
    }

    // Tandai semua item saat ini sebagai sudah dilihat (beserta statusnya).
    fun markSeenItems(context: Context, userId: Int, items: List<TestHistoryItem>) {
        if (userId <= 0 || items.isEmpty()) return
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val updatedIds = prefs.getStringSet(key(userId), emptySet()).orEmpty().toMutableSet()
        items.forEach { updatedIds.add(it.id.toString()) }
        val updatedStatus = items.distinctBy { it.id }
            .map { "${it.id}|${it.status}" }.toSet()
        prefs.edit()
            .putStringSet(key(userId), updatedIds)
            .putStringSet(statusKey(userId), updatedStatus)
            .apply()
    }

    fun markSeen(context: Context, userId: Int, ids: List<Int>) {
        if (userId <= 0 || ids.isEmpty()) return
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val updated = prefs.getStringSet(key(userId), emptySet()).orEmpty().toMutableSet()
        ids.forEach { updated.add(it.toString()) }
        prefs.edit().putStringSet(key(userId), updated).apply()
    }
}
