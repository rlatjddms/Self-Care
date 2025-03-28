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
        maven("https://repository.map.naver.com/archive/maven")
        maven ("https://naver.jfrog.io/artifactory/maven/")
        maven("https://jitpack.io")
    }
}

rootProject.name = "Capstone_Hospital"
include(":app")
