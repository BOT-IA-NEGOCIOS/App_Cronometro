package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.Routine
import org.json.JSONArray
import org.json.JSONObject

class RoutineRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("tabata_timer_pro_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_ROUTINES_JSON = "key_routines_json"
    }

    fun getRoutines(): List<Routine> {
        val jsonString = prefs.getString(KEY_ROUTINES_JSON, null)
        if (jsonString.isNullOrBlank()) {
            val defaults = Routine.getDefaultRoutines()
            saveRoutines(defaults)
            return defaults
        }

        return try {
            val jsonArray = JSONArray(jsonString)
            val list = mutableListOf<Routine>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    Routine(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        prepareSeconds = obj.optInt("prepareSeconds", 10),
                        workSeconds = obj.optInt("workSeconds", 20),
                        restSeconds = obj.optInt("restSeconds", 10),
                        rounds = obj.optInt("rounds", 8),
                        cycles = obj.optInt("cycles", 1),
                        cycleRestSeconds = obj.optInt("cycleRestSeconds", 60),
                        cooldownSeconds = obj.optInt("cooldownSeconds", 0)
                    )
                )
            }
            if (list.isEmpty()) {
                val defaults = Routine.getDefaultRoutines()
                saveRoutines(defaults)
                defaults
            } else {
                list
            }
        } catch (e: Exception) {
            val defaults = Routine.getDefaultRoutines()
            saveRoutines(defaults)
            defaults
        }
    }

    fun saveRoutine(routine: Routine): List<Routine> {
        val currentList = getRoutines().toMutableList()
        val existingIndex = currentList.indexOfFirst { it.id == routine.id }
        if (existingIndex >= 0) {
            currentList[existingIndex] = routine
        } else {
            currentList.add(0, routine)
        }
        saveRoutines(currentList)
        return currentList
    }

    fun deleteRoutine(routineId: String): List<Routine> {
        val currentList = getRoutines().toMutableList()
        currentList.removeAll { it.id == routineId }
        saveRoutines(currentList)
        return currentList
    }

    fun resetToDefaults(): List<Routine> {
        val defaults = Routine.getDefaultRoutines()
        saveRoutines(defaults)
        return defaults
    }

    private fun saveRoutines(routines: List<Routine>) {
        val jsonArray = JSONArray()
        for (r in routines) {
            val obj = JSONObject().apply {
                put("id", r.id)
                put("name", r.name)
                put("prepareSeconds", r.prepareSeconds)
                put("workSeconds", r.workSeconds)
                put("restSeconds", r.restSeconds)
                put("rounds", r.rounds)
                put("cycles", r.cycles)
                put("cycleRestSeconds", r.cycleRestSeconds)
                put("cooldownSeconds", r.cooldownSeconds)
            }
            jsonArray.put(obj)
        }
        prefs.edit().putString(KEY_ROUTINES_JSON, jsonArray.toString()).apply()
    }
}
