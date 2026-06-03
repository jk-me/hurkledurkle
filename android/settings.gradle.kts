pluginManagement {
    repositories {
        google()         // <-- CRITICAL: This is where the Android plugin lives
        mavenCentral()   // <-- CRITICAL: This holds secondary Kotlin tools
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()         // <-- Required for app dependencies
        mavenCentral()   // <-- Required for app dependencies
    }
}

rootProject.name = "YourAppName" // Keep your actual app name here
include(":app")