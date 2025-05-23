plugins {
    application
}

repositories { mavenCentral() }

dependencies {
    implementation(libs.gson)
    testImplementation(libs.junit)
}

application {
    mainClass.set("com.tasktracker.Main")
}

tasks.test { useJUnitPlatform() }
