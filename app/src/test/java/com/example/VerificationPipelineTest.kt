package com.example

import android.graphics.Bitmap
import com.example.util.LocationValidationResult
import com.example.util.VerificationPipeline
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], manifest = Config.NONE)
class VerificationPipelineTest {

    @Test
    fun testHaversineDistanceCalculation() {
        // Test distance between Nairobi CBD (-1.2921, 36.8219) and Westlands (-1.2618, 36.8041)
        // This is approximately 3.9 km (3900 meters)
        val distance = VerificationPipeline.calculateHaversineDistance(
            -1.2921, 36.8219,
            -1.2618, 36.8041
        )
        assertTrue("Distance should be around 3900 meters but was $distance", distance in 3000.0..4500.0)
    }

    @Test
    fun testLocationGeofenceInsideKenya() {
        // Nairobi CBD -> Should pass
        val nairobiResult = VerificationPipeline.validateLocationInsideKenya(-1.2921, 36.8219)
        assertEquals(LocationValidationResult.Passed, nairobiResult)

        // Nyali Beach Mombasa -> Should pass
        val nyaliResult = VerificationPipeline.validateLocationInsideKenya(-4.0255, 39.7314)
        assertEquals(LocationValidationResult.Passed, nyaliResult)

        // Lake Victoria Rectangle -> Should fail
        val waterResult = VerificationPipeline.validateLocationInsideKenya(-0.5000, 34.2000)
        assertTrue(waterResult is LocationValidationResult.Rejected)
        assertEquals(
            "This location is inside a body of water (Lake Victoria).",
            (waterResult as LocationValidationResult.Rejected).reason
        )

        // Nairobi National Park Area -> Should fail
        val parkResult = VerificationPipeline.validateLocationInsideKenya(-1.3700, 36.8500)
        assertTrue(parkResult is LocationValidationResult.Rejected)
        assertEquals(
            "Invalid: Pinned inside a National Park / Forest Reserve.",
            (parkResult as LocationValidationResult.Rejected).reason
        )

        // London coordinates (completely outside Kenya) -> Should fail
        val outsideResult = VerificationPipeline.validateLocationInsideKenya(51.5074, -0.1278)
        assertTrue(outsideResult is LocationValidationResult.Rejected)
        assertEquals(
            "Coordinates are outside the Kenyan border bounding box.",
            (outsideResult as LocationValidationResult.Rejected).reason
        )
    }

    @Test
    fun testAverageHashingBitstring() {
        // Create a basic synthetic bitmap
        val bitmap = Bitmap.createBitmap(8, 8, Bitmap.Config.ARGB_8888)
        
        // Populate pixels (half white, half black to test thresholding)
        for (y in 0 until 8) {
            for (x in 0 until 8) {
                val color = if (y < 4) android.graphics.Color.WHITE else android.graphics.Color.BLACK
                bitmap.setPixel(x, y, color)
            }
        }

        val hash = VerificationPipeline.generateAverageHash(bitmap)
        
        // Assertions
        assertEquals("Hash bitstring must contain exactly 64 bits", 64, hash.length)
        assertTrue("Hash bitstring should only contain 1 and 0", hash.all { it == '1' || it == '0' })
        
        // First 32 bytes should be '1' and remaining '0' based on average threshold
        val firstHalf = hash.substring(0, 32)
        val secondHalf = hash.substring(32)
        assertEquals(32, firstHalf.count { it == '1' })
        assertEquals(32, secondHalf.count { it == '0' })
    }

    @Test
    fun testCalculateHammingDistance() {
        val hash1 = "1111000011110000111100001111000011110000111100001111000011110000"
        val hash2 = "1111000011110000111100001111000011110000111100001111000011111111" // last 4 bits differ
        
        val distance = VerificationPipeline.calculateHammingDistance(hash1, hash2)
        assertEquals(4, distance)
    }
}
