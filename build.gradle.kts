// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.jetbrainsKotlinAndroid) apply false
    id("com.google.gms.google-services") version "4.4.0" apply false
}

buildscript {
    repositories {
        google() // Ensure this is present
        mavenCentral()
    }
    dependencies {
        classpath(libs.google.services) // Ensure this points to the correct Google services dependency
    }
}