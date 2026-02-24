plugins {
    id("java")
    `maven-publish`
}

group = "mod.deplayer.coffeechat.coffeeirc"
version = "26.dr1.1"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation("org.apache.logging.log4j:log4j-api:2.23.1")
    implementation("org.apache.logging.log4j:log4j-core:2.23.1")
    // implementation("org.apache.logging.log4j:log4j-slf4j2-impl:2.23.1")
}

tasks.test {
    useJUnitPlatform()
    jvmArgs = listOf(
        "-Dfile.encoding=UTF-8",
        "-Dsun.stdout.encoding=UTF-8",
        "-Dsun.stderr.encoding=UTF-8"
    )
}

tasks.jar {
    manifest {
        //attributes["Main-Class"] = "mod.deplayer.coffeechat.coffeeirc.CDTE"
        //当你想要开发和测试时，请将上一行的注释符号去掉
        attributes["Add-Opens"] = "java.base/java.lang java.base/java.util"
    }
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.withType<JavaExec> {
    jvmArgs = listOf(
        "-Dfile.encoding=UTF-8",
        "-Dsun.stdout.encoding=UTF-8", 
        "-Dsun.stderr.encoding=UTF-8"
    )
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

tasks.register<JavaExec>("run") {
    dependsOn(tasks.classes)
    //mainClass.set("mod.deplayer.coffeechat.coffeeirc.CDTE")
    //当你想要开发和测试时，请将上一行的注释符号去掉
    classpath = sourceSets.main.get().runtimeClasspath
    jvmArgs = listOf(
        "-Dfile.encoding=UTF-8"
    )
}

tasks.withType<Javadoc> {
    options.memberLevel = JavadocMemberLevel.PRIVATE
}