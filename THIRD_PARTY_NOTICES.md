# Third-Party Notices

ProxyStar may use third-party libraries, build tools, assets, or other
components that are licensed separately from ProxyStar's proprietary code.

## License scope

The proprietary `LICENSE` in this repository applies only to material owned
by Dev Qissy. It does not replace, limit, or revoke permissions granted by
the licenses of third-party components.

## Dependency inventory

This file is a notice placeholder, not a complete inventory of every
dependency shipped in a particular APK. The exact components and versions
can vary by build configuration and dependency resolution.

Before publishing a release, review the project's Gradle files and the
resolved runtime dependency graph. For each third-party component included
in the distributed APK, record at least:

| Component | Version | License | Source / license text |
|---|---|---|---|
| Add each shipped runtime dependency | Exact resolved version | Verify from upstream | Link to the official project and license |

Include transitive runtime dependencies where their licenses or notices
require attribution. Do not assume that a dependency's license can be
determined from its name alone.

## Android and build tooling

Android SDK components, Gradle plugins, Kotlin tooling, and other build-time
dependencies may have their own license terms. Distinguish build-time tools
from libraries actually bundled into the application, while retaining any
notices required by their respective licenses.

## Assets and data sources

List any third-party icons, fonts, images, datasets, or other assets
distributed with ProxyStar, along with their authors, source links, and
required attribution. If proxy entries are collected from public sources,
document the sources where appropriate and respect their terms of use.

## Before release

- [ ] Inspect the resolved runtime dependencies for the release variant.
- [ ] Verify each applicable license from its official source.
- [ ] Add required copyright notices and license texts.
- [ ] Include attribution for bundled assets and data sources.
- [ ] Recheck this file whenever dependencies or bundled assets change.
