dependencies {
    api(project(":jacodb-core"))
    api(project(":jacodb-analysis"))
    api(project(":jacodb-api"))

    implementation(Libs.kotlin_logging)
    implementation(Libs.kotlinx_cli)
    implementation(Libs.kotlinx_serialization_json)

    testImplementation(testFixtures(project(":jacodb-core")))
}

task("run", JavaExec::class) {
    mainClass.set("org.jacodb.cli.MainKt")
    doFirst {
        classpath = sourceSets["main"].runtimeClasspath
        val analysisConfigPath = project.property("analysisConfigPath")
        val startClasses = project.property("startClasses")
        val classpath = project.property("classpath")
        val reportPath = project.findProperty("reportPath")
        val reportPathArgument = if (reportPath == null) "" else "-o \"$reportPath\""
        args = listOf("-a \"$analysisConfigPath\" -s \"$startClasses\" -cp \"$classpath\" $reportPathArgument")
    }
}
