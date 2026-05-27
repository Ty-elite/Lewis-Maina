package com.example.util

import android.graphics.Bitmap
import kotlin.math.*

object VerificationPipeline {

    /**
     * Calculates the real geodetic distance between two coordinates in meters
     * using the Haversine formula.
     */
    fun calculateHaversineDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371000.0 // Earth's radius in meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    /**
     * Resizes an image to 8x8 pixels, grayscales it, and creates an average value hash (aHash).
     * This perceptual hash function helps match visually close/copied images.
     */
    fun generateAverageHash(bitmap: Bitmap): String {
        val resized = Bitmap.createScaledBitmap(bitmap, 8, 8, true)
        val grayScale = IntArray(64)
        var sum = 0
        for (y in 0 until 8) {
            for (x in 0 until 8) {
                val pixel = resized.getPixel(x, y)
                val r = (pixel shr 16) and 0xff
                val g = (pixel shr 8) and 0xff
                val b = pixel and 0xff
                val gray = (r + g + b) / 3
                grayScale[y * 8 + x] = gray
                sum += gray
            }
        }
        val average = sum / 64
        val hash = StringBuilder()
        for (i in 0 until 64) {
            if (grayScale[i] >= average) {
                hash.append("1")
            } else {
                hash.append("0")
            }
        }
        return hash.toString()
    }

    /**
     * Calculates the Hamming distance (number of mismatching bits) between two pHash values.
     * Distance <= 10 indicates duplicate or heavily edited images.
     */
    fun calculateHammingDistance(h1: String, h2: String): Int {
        var distance = 0
        for (i in 0 until min(h1.length, h2.length)) {
            if (h1[i] != h2[i]) {
                distance++
            }
        }
        return distance
    }

    /**
     * Confirms if the pin location is physically inside the bounding box of Kenya,
     * and does not fall in protected or unpopulated zones (such as lake water, national reserves, etc.)
     */
    fun validateLocationInsideKenya(lat: Double, lng: Double): LocationValidationResult {
        // Kenya's Bounding Box roughly: lat: -4.7 to 4.6, lng: 33.9 to 41.9
        if (lat < -4.7 || lat > 4.6 || lng < 33.9 || lng > 41.9) {
            return LocationValidationResult.Rejected("Coordinates are outside the Kenyan border bounding box.")
        }

        // Check if coordinates land in Lake Victoria (-1.5 to -0.0 lat, 33.9 to 34.8 lng)
        if (lat in -1.5..-0.0 && lng in 33.9..34.8) {
            return LocationValidationResult.Rejected("This location is inside a body of water (Lake Victoria).")
        }

        // Check if coordinates land in Nairobi National Park (-1.42 to -1.35 lat, 36.80 to 36.90 lng)
        if (lat in -1.42..-1.35 && lng in 36.80..36.90) {
            return LocationValidationResult.Rejected("Invalid: Pinned inside a National Park / Forest Reserve.")
        }

        // Check if coordinates land in Tsavo West/East (-3.5 to -2.5 lat, 37.5 to 39.0 lng)
        if (lat in -3.5..-2.5 && lng in 37.5..39.0) {
            return LocationValidationResult.Rejected("Invalid: Pinned inside Tsavo National Park area.")
        }

        return LocationValidationResult.Passed
    }
}

sealed class LocationValidationResult {
    object Passed : LocationValidationResult()
    data class Rejected(val reason: String) : LocationValidationResult()
}
