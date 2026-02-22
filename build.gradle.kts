plugins {
    id("java")
    `maven-publish`
}

group = "mod.deplayer.coffeechat.coffeeirc"
version = "26.d1"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "mod.deplayer.coffeechat.coffeeirc.Main"
    }
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.register<Jar>("sourcesJar") {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

tasks.withType<AbstractPublishToMaven>().configureEach {
    dependsOn(tasks.named<Jar>("sourcesJar"))
}

tasks.build {
    dependsOn(tasks.named<Jar>("sourcesJar"))
}