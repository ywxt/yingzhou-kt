plugins {
    id("kotlin-conventions")
    id("testing-conventions")
    id("dokka-conventions")
    id("version-conventions")
    id("license-conventions")
}


dependencies {
    implementation(libs.bundles.arrow)
    implementation(libs.bundles.kotlinLogging)

    implementation(libs.bundles.coroutines)
}
