
plugins {
    java
//    `maven-publish`
    application
    id("org.openjfx.javafxplugin") version "0.0.13"
    id("org.springframework.boot") version "2.7.10"
    id("io.spring.dependency-management") version "1.0.15.RELEASE"
    //对于Windows，需要安装 https://wixtoolset.org
    id("org.panteleyev.jpackageplugin") version "1.5.1"
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenLocal()
    maven { url = uri("https://maven.aliyun.com/repository/public/") }
    maven { url = uri("https://maven.aliyun.com/repository/google/") }
    maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin/") }
    maven {
        url = uri("https://repo.maven.apache.org/maven2/")
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    compileOnly("org.projectlombok:lombok")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    annotationProcessor("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-test")

    implementation("org.springframework.cloud:spring-cloud-starter-openfeign:3.1.3")
    implementation("io.github.openfeign:feign-okhttp:11.8")
    implementation("org.apache.httpcomponents:httpclient:4.5.14")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.14.0")
    implementation("com.baomidou:mybatis-plus-boot-starter:3.5.3.1")
    implementation("com.baomidou:mybatis-plus-generator:3.5.3.1")
    implementation("org.openjfx:javafx-fxml:17.0.1")
    implementation("net.rgielen:javafx-weaver-spring-boot-starter:1.3.0")
    implementation("com.dlsc.workbenchfx:workbenchfx-core:11.3.1")
    implementation("org.fxmisc.richtext:richtextfx:0.11.0")
    implementation("org.apache.commons:commons-csv:1.9.0")
    runtimeOnly("com.h2database:h2:2.1.214")
}

group = "org.lifxue"
version = "1.0"
description = "WuZhu"
java.sourceCompatibility = JavaVersion.VERSION_11

//publishing {
//    publications.create<MavenPublication>("maven") {
//        from(components["java"])
//    }
//}

tasks.withType<JavaCompile>() {
    options.encoding = "UTF-8"
}


application {
    //mainModule = "WuZhu"
    mainClass.set("org.lifxue.wuzhu.WuZhuApplication")
}

javafx {
    version = "17.0.1"
    modules("javafx.controls", "javafx.fxml")
}

task("copyDependencies", Copy::class) {
    from(configurations.runtimeClasspath).into("$buildDir/jars")
}

task("copyJar", Copy::class) {
    from(tasks.jar).into("$buildDir/jars")
}

tasks.jpackage {
    dependsOn("build", "copyDependencies", "copyJar")

    input  = "$buildDir/jars"
    destination = "$buildDir/dist"

    appName = "WuZhu"
    vendor = "wuzhu.org"

    mainJar = tasks.jar.get().archiveFileName.get()
    mainClass = "org.lifxue.wuzhu.WuZhuApplication"

    javaOptions = listOf("-Dfile.encoding=UTF-8")

    windows {
        winConsole = true
    }
}