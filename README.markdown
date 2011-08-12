## Synopsis
If you've made it here, chances are you are not quite as satisfied with the Android application framework as you could be. Same for us, that's why we created ignition.

Ignition helps you getting your Android applications off the ground quickly, by offering ready-to-use components and utility classes that wrap a lot of the boilerplate that's involved when writing Android apps. Areas covered by ignition encompass:

 * UI components such as widgets, adapters, dialogs, and more
 * An HTTP wrapper library that allows you to write simple yet robust networking code
 * A class to load remote images off the web and cache them
 * A simple yet effective caching framework (caches to memory and disk, anything from HTTP responses to entire object trees)
 * Diagnostic helpers to gather device information and help users sending error reports to you via email
 * API level backwards compatibility helpers
 * A friendlier and more robust implementation of AsyncTask

Ignition is split up into two projects:

 * [ignition-core](https://github.com/kaeppler/ignition-core). This is an Android library project that is compiled straight into your apps. This allows you to re-use views and shared resources right in your own code.
 * [ignition-support](https://github.com/kaeppler/ignition-support). This is a standard Java library project, deployed as an ordinary JAR. It contains most of the utility classes. You can use this independently of the core module.

 ## Installation
 For installation instructions, refer to the individual documentation of the sub-projects.

 If you want to checkout the entire project, do this:

```
$git clone git://github.com/kaeppler/ignition.git
$cd ignition
$git submodule init
$git submodule update
```

If on top of that you're working with Eclipse, you may want to generate Eclipse project files. Ignition is built using the wonderful [Gradle build system](http://www.gradle.org), so install Gradle and run:
```
$gradle eclipse
```
This will take care of generating the necessary project files.

## License
Apache License, Version 2.0 (http://www.apache.org/licenses/LICENSE-2.0.html)

## Authors
 * Matthias Käppler (m.kaeppler@gmail.com)
 * Stefano Dacchille (stefano.dacchille@gmail.com)
 * Michael England (mg.england@gmail.com)
 * the Android open-source community (http://stackoverflow.com/questions/tagged/android)
 