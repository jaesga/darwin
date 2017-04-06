# Darwin

Darwin is a [Play Framework 1.x](https://www.playframework.com/documentation/1.4.x/home) module. This module brings the
most popular features what *all web/api* project needs.

#### Table of contents

* [Getting started](docs/getting-started.md)
* [First app controller](docs/first-app-controller.md)
* [Securing your controllers](docs/securing-controllers.md)
* [Permissions and how its works](docs/permissions.md)
* [Authenticated API](#TODO)
* [Hooks... ](#TODO)
* [Extending the User model](#TODO)
* [11P products integrations (Latch, MobileConnect...)](#TODO)
* [Deploy Play! App](#TODO)

# Requirements

* Java 1.8
* [Play 1.4.4](https://www.playframework.com/download#alternatives)
* Mongo 2.6 (Only if you would use the default factory)

# Versioning

This project is versioned with snapshots and release versions (like a maven app for example). If add changes to the darwin
core you must change code version at conf/dependencies.yml:
```
../conf/dependencies.yml

self: play -> darwin {{ version }}
```

# Interesting links

* [Play 1.4.x cheatsheet](https://www.playframework.com/documentation/1.4.x/cheatsheet/templates)
* [Play 1.4.x GitHub](https://github.com/playframework/play1)