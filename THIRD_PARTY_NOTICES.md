# Third-Party Notices

This notice is based on the uploaded Gradle report for
`releaseRuntimeClasspath` of the ProxyStar `:app` module. The report was
generated successfully and contains the dependencies resolved for that
configuration. The exact graph can change when Gradle versions, dependency
constraints, or project configuration changes.

## Direct runtime dependencies

The following direct dependencies are declared in the app module's Gradle
configuration. Versions shown here are the declared or resolved versions
visible in the supplied report.

| Component | Declared / resolved version | License family | Project |
|---|---|---|---|
| AndroidX DataStore Preferences | 1.2.1 | Apache-2.0 | https://developer.android.com/jetpack/androidx |
| OkHttp | 5.5.0 | Apache-2.0 | https://square.github.io/okhttp/ |
| AndroidX Lifecycle ViewModel Compose | 2.11.0 | Apache-2.0 | https://developer.android.com/jetpack/androidx |
| AndroidX Lifecycle Runtime Compose | 2.11.0 | Apache-2.0 | https://developer.android.com/jetpack/androidx |
| AndroidX Activity Compose | 1.13.0 | Apache-2.0 | https://developer.android.com/jetpack/androidx |
| Jetpack Compose Material 3 | 1.4.0 | Apache-2.0 | https://developer.android.com/jetpack/compose |
| Jetpack Compose UI | 1.11.0 | Apache-2.0 | https://developer.android.com/jetpack/compose |
| Jetpack Compose UI Graphics | 1.11.0 | Apache-2.0 | https://developer.android.com/jetpack/compose |
| Jetpack Compose UI Tooling Preview | 1.11.0 | Apache-2.0 | https://developer.android.com/jetpack/compose |
| AndroidX Core KTX | 1.19.0 | Apache-2.0 | https://developer.android.com/jetpack/androidx |
| AndroidX Lifecycle Runtime KTX | 2.11.0 | Apache-2.0 | https://developer.android.com/jetpack/androidx |

The Compose BOM (`androidx.compose:compose-bom:2026.02.01`) is a version
alignment platform rather than a standalone UI runtime library.

## Notable transitive runtime dependencies

The supplied report also includes these transitive dependency families.
Versions below are the selected versions shown in the report where
identifiable; older requested versions may be redirected to a selected
version by Gradle.

| Component / family | Selected version shown | License family |
|---|---|---|
| Kotlin Standard Library | 2.2.21 | Apache-2.0 |
| JetBrains Annotations | 23.0.0 | Apache-2.0 |
| Kotlinx Coroutines | 1.9.0 | Apache-2.0 |
| Kotlinx Serialization Core / JSON | 1.7.3 | Apache-2.0 |
| Okio | 3.18.1 | Apache-2.0 |
| AndroidX Annotation | 1.10.0 | Apache-2.0 |
| AndroidX Collection | 1.5.0 | Apache-2.0 |
| AndroidX Startup Runtime | 1.2.0 | Apache-2.0 |
| AndroidX Tracing | 1.2.0 | Apache-2.0 |
| AndroidX Lifecycle family | 2.11.0 | Apache-2.0 |
| AndroidX SavedState family | 1.4.0 | Apache-2.0 |
| AndroidX ProfileInstaller | 1.4.0 | Apache-2.0 |
| AndroidX Navigation Event family | 1.0.0 | Apache-2.0 |
| JSpecify | 1.0.0 | Verify upstream notice before release |
| Guava ListenableFuture (standalone artifact) | 1.0 | Verify upstream notice before release |

## License texts and attribution

Apache-2.0 is the license family for the listed AndroidX, Kotlin, OkHttp,
Okio, Coroutines, and Serialization components. For distribution, retain
any copyright notices and license text required by each component's
upstream license.

The table is a practical summary, not a substitute for each upstream
license file. Before publishing a release, verify license metadata and
required notices for all artifacts actually packaged in the APK, including
transitive components and any bundled assets.

## Other materials

If the app bundles third-party icons, fonts, images, datasets, or other
assets, list each item here with its author, source URL, license, and
required attribution. If proxy entries are collected from public sources,
document those sources where appropriate and respect their terms of use.

## Release checklist

- [ ] Verify the resolved dependency graph for the exact release build.
- [ ] Confirm each dependency's license from its official project metadata.
- [ ] Include required upstream license texts and copyright notices.
- [ ] Add any third-party assets and source-data attributions.
- [ ] Update this file whenever dependencies or bundled assets change.
