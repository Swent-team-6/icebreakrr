pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // Add JitPack repository
        maven("https://jitpack.io") {
            content {
                includeGroupByRegex("com\\.github.*") // Scope to GitHub repositories
            }
        }
    }
}

rootProject.name = "IceBreakrr"
include(":app")
 