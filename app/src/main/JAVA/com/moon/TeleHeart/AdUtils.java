package com.moon.TeleHeart;

import android.content.Context;
import android.telephony.TelephonyManager;

import java.util.Locale;
import java.util.TimeZone;

public class AdUtils {
    // List of CIS countries and Russia
    private static final String[] CIS_COUNTRIES = {
            "RU", // Russia
            "AM", // Armenia
            "AZ", // Azerbaijan
            "BY", // Belarus
            "KZ", // Kazakhstan
            "KG", // Kyrgyzstan
            "MD", // Moldova
            "TJ", // Tajikistan
            "TM", // Turkmenistan
            "UZ", // Uzbekistan
            "UA"  // Ukraine
    };

    // List of time zones for Russia and CIS countries
    private static final String[] CIS_TIME_ZONES = {
            "Europe/Moscow", // Russia
            "Asia/Yerevan",  // Armenia
            "Asia/Baku",     // Azerbaijan
            "Europe/Minsk",  // Belarus
            "Asia/Almaty",   // Kazakhstan
            "Asia/Bishkek",  // Kyrgyzstan
            "Europe/Chisinau", // Moldova
            "Asia/Dushanbe", // Tajikistan
            "Asia/Ashgabat", // Turkmenistan
            "Asia/Tashkent", // Uzbekistan
            "Europe/Kyiv"    // Ukraine
    };

    // Static method to check if the user is in a CIS country or Russia
    public static boolean isUserInCISOrRussia(Context context) {
        System.out.println("Starting check for CIS or Russia...");

        // Step 1: Check SIM card country code (highest priority)
        System.out.println("Checking SIM card...");
        String simCountryCode = getSimCountryCode(context);
        if (simCountryCode != null) {
            System.out.println("Country code from SIM card: " + simCountryCode);
            if (isCountryInCIS(simCountryCode)) {
                System.out.println("User is in Russia or a CIS country based on SIM card.");
                return true; // User is in Russia or a CIS country based on SIM card
            } else {
                System.out.println("User is NOT in a CIS country based on SIM card.");
                // If SIM card country code is available but not CIS, return false immediately
                return false;
            }
        } else {
            System.out.println("Failed to fetch country code from SIM card.");
        }

        // Step 2: Fall back to time zone (medium priority) only if SIM card check is inconclusive
        System.out.println("Falling back to time zone...");
        String timeZoneID = TimeZone.getDefault().getID();
        System.out.println("User's time zone: " + timeZoneID);
        if (isTimeZoneInCIS(timeZoneID)) {
            System.out.println("User is in Russia or a CIS country based on time zone.");
            return true; // User is in Russia or a CIS country based on time zone
        } else {
            System.out.println("User is NOT in a CIS country based on time zone.");
        }

        // Step 3: Final fallback to device locale (lowest priority)
        System.out.println("Falling back to device locale...");
        String countryCodeFromLocale = Locale.getDefault().getCountry();
        System.out.println("Country code from locale: " + countryCodeFromLocale);
        if (isCountryInCIS(countryCodeFromLocale)) {
            System.out.println("User is in Russia or a CIS country based on phone language.");
            return true; // User is in Russia or a CIS country based on phone language
        } else {
            System.out.println("User is NOT in Russia or a CIS country based on phone language.");
        }

        // Final result
        System.out.println("User is NOT in Russia or a CIS country.");
        return false; // User is not in Russia or a CIS country
    }

    // Helper method to check if a country code is in the CIS list
    private static boolean isCountryInCIS(String countryCode) {
        for (String cisCountry : CIS_COUNTRIES) {
            if (cisCountry.equalsIgnoreCase(countryCode)) {
                return true;
            }
        }
        return false;
    }

    // Helper method to check if a time zone is in the CIS list
    private static boolean isTimeZoneInCIS(String timeZoneID) {
        for (String cisTimeZone : CIS_TIME_ZONES) {
            if (cisTimeZone.equalsIgnoreCase(timeZoneID)) {
                return true;
            }
        }
        return false;
    }

    // Method to get the SIM card's country code (Android-specific)
    private static String getSimCountryCode(Context context) {
        if (context == null) {
            System.out.println("Context is not available.");
            return null;
        }

        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager == null) {
            System.out.println("TelephonyManager is not available.");
            return null;
        }

        String simCountryCode = telephonyManager.getSimCountryIso();
        if (simCountryCode == null || simCountryCode.isEmpty()) {
            System.out.println("SIM card country code is not available.");
            return null;
        }

        return simCountryCode.toUpperCase();
    }
}