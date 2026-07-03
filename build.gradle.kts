plugins {
    application
    id("com.gradleup.shadow") version "8.3.1"
}

application.mainClass = "dev.javadrinker.vcpm.Main" //
group = "dev.javadrinker.vcpm"
version = "2.2"

val jdaVersion = "6.3.0" //

repositories {
    mavenCentral()
}

dependencies {
    implementation("net.dv8tion:JDA:$jdaVersion")
    implementation("io.github.cdimascio:java-dotenv:5.2.2")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")
    implementation ("org.jsoup:jsoup:1.22.2")
}


tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.isIncremental = true
}