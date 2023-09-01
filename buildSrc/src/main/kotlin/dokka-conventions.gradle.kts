plugins {
    id("org.jetbrains.dokka")
}

tasks.withType<Javadoc>().all {
    enabled = false
}

tasks.dokkaJavadoc {
    outputDirectory.set(layout.buildDirectory.dir("javadoc"))
}

tasks.named("build") {
    finalizedBy(tasks.dokkaHtml)
}
