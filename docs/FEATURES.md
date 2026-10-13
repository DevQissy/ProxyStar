# ProxyStar Features

ProxyStar is an Android application focused on proxy discovery, connectivity testing, and server selection.

## Proxy Discovery

ProxyStar collects proxy entries from configured public sources to help users discover available servers.

The underlying source list is not published in this repository.

## Connectivity Testing

The application checks server availability to help distinguish reachable servers from servers that do not respond to its checks.

Test results reflect the conditions at the time of testing and may change.

## Latency Measurement

ProxyStar measures connection response times to help users compare server performance.

Lower latency can be useful for responsiveness, but it does not necessarily mean a server has higher throughput or better reliability.

## Quality-Based Sorting

Servers can be ordered using connection-quality information so users can identify promising options more easily.

## Network-Aware Recommendations

ProxyStar aims to help users find suitable servers as network conditions change. Actual results depend on the available measurements and the current network environment.

## Curated Server Collection

The application provides access to a curated selection of more than 1,000 servers.

The number of available and working servers may vary over time.

## Technology

- **Platform:** Android
- **Primary language:** Kotlin
- **Development environment:** Android Studio
- **Supporting tools:** Python

## Availability

ProxyStar is distributed as a compiled Android APK through its release channels. This public repository does not include the application's source code.

## Limitations

- Public proxy endpoints may become unavailable without notice.
- Latency measurements can fluctuate.
- Test results cannot guarantee future performance.
- Server availability depends on the user's location and network.
- Proxy use does not automatically provide encryption, anonymity, or protection from malicious endpoints.

Features and descriptions should be updated when application behavior changes.
