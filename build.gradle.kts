plugins {
    id("java")
    id("jacoco")
}

java{
    toolchain{
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

jacoco{
    toolVersion = "0.8.13"
}

group = "com.mardon"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val lombokVersion = "1.18.46"
val assertjVersion = "3.27.7"
val mockitoVersion = "5.+"
dependencies {
    compileOnly("org.projectlombok:lombok:$lombokVersion")
    annotationProcessor("org.projectlombok:lombok:$lombokVersion")

    testCompileOnly("org.projectlombok:lombok:$lombokVersion")
    testAnnotationProcessor("org.projectlombok:lombok:$lombokVersion")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.junit.platform:junit-platform-launcher")
    testImplementation("org.junit.jupiter:junit-jupiter-params")

    testImplementation("org.assertj:assertj-core:$assertjVersion")

    testImplementation("org.mockito:mockito-core:$mockitoVersion")
    testImplementation("org.mockito:mockito-junit-jupiter:$mockitoVersion")

}

tasks.test {
    useJUnitPlatform{
        val includeTagsProperty = providers.systemProperty("includeTags").orNull
        if (!includeTagsProperty.isNullOrBlank()){
            includeTags(*includeTagsProperty.split(",").map{it.trim()}.toTypedArray())
        }

        val excludeTagsProperty = providers.systemProperty("excludeTags").orNull
        if (!excludeTagsProperty.isNullOrBlank()) {
            excludeTags(*excludeTagsProperty.split(",").map{it.trim()}.toTypedArray())
        }

    }
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport{
    dependsOn(tasks.test)

    reports{
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }
}