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
    }
}

rootProject.name = "Synex"
include(":app")
include(":core:model")
include(":core:network")
include(":core:data")
include(":core:ui")
include(":feature:overview")
include(":feature:markets")
include(":feature:portfolio")
include(":feature:account")
include(":feature:legal")
include(":feature:auth")
include(":feature:onboarding")
include(":feature:trade")
include(":feature:activity")
include(":feature:funding")
 
