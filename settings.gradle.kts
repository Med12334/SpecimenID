pluginManagement {
    repositories {
        google()
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

rootProject.name = "SpecimenID"

// Include Application module
include(":app")

// Include Core modules
include(":core:domain")
include(":core:database")
include(":core:vision")

// Include Feature modules
include(":feature:scanner")
include(":feature:settings")
