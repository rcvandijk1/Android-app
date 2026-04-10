package com.homehub.app.icons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AcUnit
import androidx.compose.material.icons.outlined.Blinds
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material.icons.outlined.CoffeeMaker
import androidx.compose.material.icons.outlined.Doorbell
import androidx.compose.material.icons.outlined.Garage
import androidx.compose.material.icons.outlined.HeatPump
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Outlet
import androidx.compose.material.icons.outlined.Router
import androidx.compose.material.icons.outlined.Sensors
import androidx.compose.material.icons.outlined.Speaker
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material.icons.outlined.Tv
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material.icons.outlined.Yard
import androidx.compose.ui.graphics.vector.ImageVector

data class DeviceIconDef(
    val key: String,
    val label: String,
    val image: ImageVector,
)

/**
 * Curated catalog of device icons. Keys are stable strings so the set can
 * evolve without a DB migration: unknown keys fall back to "home".
 */
object DeviceIcons {

    val ALL: List<DeviceIconDef> = listOf(
        DeviceIconDef("home",       "Generic",    Icons.Outlined.Home),
        DeviceIconDef("lightbulb",  "Light",      Icons.Outlined.Lightbulb),
        DeviceIconDef("thermostat", "Thermostat", Icons.Outlined.Thermostat),
        DeviceIconDef("videocam",   "Camera",     Icons.Outlined.Videocam),
        DeviceIconDef("speaker",    "Speaker",    Icons.Outlined.Speaker),
        DeviceIconDef("outlet",     "Plug",       Icons.Outlined.Outlet),
        DeviceIconDef("lock",       "Lock",       Icons.Outlined.Lock),
        DeviceIconDef("tv",         "TV",         Icons.Outlined.Tv),
        DeviceIconDef("ac_unit",    "AC",         Icons.Outlined.AcUnit),
        DeviceIconDef("blinds",     "Blinds",     Icons.Outlined.Blinds),
        DeviceIconDef("sensors",    "Sensor",     Icons.Outlined.Sensors),
        DeviceIconDef("coffee",     "Coffee",     Icons.Outlined.CoffeeMaker),
        DeviceIconDef("router",     "Router",     Icons.Outlined.Router),
        DeviceIconDef("doorbell",   "Doorbell",   Icons.Outlined.Doorbell),
        DeviceIconDef("garage",     "Garage",     Icons.Outlined.Garage),
        DeviceIconDef("heater",     "Heater",     Icons.Outlined.HeatPump),
        DeviceIconDef("vacuum",     "Vacuum",     Icons.Outlined.CleaningServices),
        DeviceIconDef("sprinkler",  "Sprinkler",  Icons.Outlined.Yard),
    )

    private val byKey: Map<String, DeviceIconDef> = ALL.associateBy { it.key }

    const val DEFAULT_KEY: String = "home"

    fun get(key: String): DeviceIconDef =
        byKey[key] ?: byKey.getValue(DEFAULT_KEY)
}
