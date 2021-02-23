
plugins {
    java
    `maven-publish`
}

repositories {
    maven("https://maven.scijava.org/content/groups/public")
    mavenCentral()
}

dependencies {
    implementation("commons-lang:commons-lang:2.6")
    implementation("net.imglib2:imglib2:5.6.3")
    implementation("net.imglib2:imglib2-algorithm:0.11.1")
    implementation("net.imglib2:imglib2-cache:1.0.0-beta-11")
    implementation("net.imglib2:imglib2-realtransform:2.2.1")
    implementation("net.imglib2:imglib2-ui:2.0.0")
    implementation("org.antlr:ST4:4.0.8")
    implementation("org.apache.commons:commons-math3:3.6.1")
    implementation("org.jdom:jdom2:2.0.6")
    implementation("org.jogamp.gluegen:gluegen-rt-main:2.3.2")
    implementation("org.jogamp.jogl:jogl-all-main:2.3.2")
    implementation("org.joml:joml:1.9.12")
    implementation("org.scijava:ui-behaviour:1.7.4")
    implementation("org.scijava:scijava-common:2.77.0")
    implementation("sc.fiji:bigdataviewer-core:8.0.0")
    implementation("sc.fiji:bigdataviewer-vistools:1.0.0-beta-18")
    implementation("sc.fiji:spim_data:2.2.2")
    testImplementation("junit:junit:4.12")
    testImplementation("net.imagej:imagej:2.0.0-rc-71")
    testImplementation("net.imagej:ij:1.52o")
    testImplementation("net.imglib2:imglib2-ij:2.0.0-beta-44")
}

group = "sc.fiji"
version = "0.1.9-SNAPSHOT"
description = "BigVolumeViewer"

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}
