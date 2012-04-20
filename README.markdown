## Synopsis
If you've made it here, chances are you are not quite as satisfied with the Android application framework as you could be. Same for us, that's why we created ignition.

Ignition helps you getting your Android applications off the ground quickly, by offering ready-to-use components and utility classes that wrap a lot of the boilerplate that's involved when writing Android apps. Areas covered by ignition encompass:

 * UI components such as widgets, adapters, dialogs, and more
 * An HTTP wrapper library that allows you to write simple yet robust networking code
 * A class to load remote images off the web and cache them
 * A simple yet effective caching framework (caches to memory and disk, anything from HTTP responses to entire object trees)
 * Several helper classes for easier API level backwards compatibility, Intents, diagnostics, and more
 * A friendlier and more robust implementation of AsyncTask

Ignition is split up into three sub-projects.

 * **ignition-core.** This is an Android library project that is compiled straight into your apps. This allows you to re-use views and shared resources right in your own code.
 * **ignition-support.** This is a standard Java library project, deployed as an ordinary JAR. It contains most of the utility classes. You can use this independently of the core module.
 * **ignition-location.** This is an Android AspectJ library project that is compiled straight into your apps. It allows your location-aware applications to always have the most recent location without the need to handle location updates within your Activity.

## Changelog
The latest version of ignition is 0.2 and was released on April 20th 2012.

Go here for a [list of changes](https://github.com/kaeppler/ignition/wiki/Changelog).

## Documentation
We decided to not write exhaustive documentation, since it would get outdated frequently. Instead, the project is documented both via [sample applications](https://github.com/kaeppler/ignition/wiki/Sample-applications) that are part of the build, as well as online JavaDoc:

 * [ignition-core](http://kaeppler.github.com/ignition-docs/ignition-core/apidocs/)
 * [ignition-support](http://kaeppler.github.com/ignition-docs/ignition-support/apidocs/)
 * [ignition-location](http://kaeppler.github.com/ignition-docs/ignition-location/apidocs/)

## Installation
Please refer to [Installation and setup](https://github.com/kaeppler/ignition/wiki/Installation-and-setup). We are also collecting answers to [frequently asked questions](https://github.com/kaeppler/ignition/wiki/FAQ).

## Getting help
First, please check if your question is answered in the [FAQ](https://github.com/kaeppler/ignition/wiki/FAQ). If not, you can ask questions and get help by joining [ignition-users](https://groups.google.com/group/ignition-users) on Google Groups.

## Contributing
Feel like giving back? We'll happily take contributions via GitHub. For questions, please turn to [ignition-developers](https://groups.google.com/group/ignition-developers) on Google Groups.

## License
Apache License, Version 2.0 (http://www.apache.org/licenses/LICENSE-2.0.html)

## Authors
 * Matthias Käppler (m.kaeppler@gmail.com)
 * Stefano Dacchille (stefano.dacchille@gmail.com)
 * Michael England (mg.england@gmail.com)
 * the Android open-source community (http://stackoverflow.com/questions/tagged/android)
