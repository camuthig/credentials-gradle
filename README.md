# Credentials Gradle Plugin

[![Build][ico-travisci]][link-travisci]

This plugin provides the ability to create and alter encrypted credential files based on the [Kotlin Credentials](https://github.com/camuthig/kotlin-credentials)
package.

## Install

Add the bintray repository to your plugin management.

```kotlin
// settings.gradle
pluginManagement {
    repositories {
        gradlePluginPortal()
        maven {
            url = uri("https://dl.bintray.com/camuthig/maven")
        }
    }
}

// The remainder of your settings file. Plugin management must be first.
```

Add the plugin to our build file

```kotlin
// gradle.build.kts

plugins {
    id("org.camuthig.credentials") version "0.1.0"
}
```

## Configuration

The plugin adds a new extension, `credentials`

```kotlin
// gradle.build.kts
credentials {
    masterKeyFile = 'path/to/your/key/file.key'
    credentialsFile = 'path/to/your/credentials/file.conf.enc'
}
```

## Usage

### Within the Build

The plugin extension exposes two functions allow you to access your credentials from within your build process. This
can be used, for example, if you need to include database credentials for running migrations in your build system.

```kotlin
flyway {
    url = 'jdbc:h2:file:./target/foobar'
    // This will throw an error, stopping your build, if it is unable to find the credentials files or the key
    user = credentials.getString('sa')
}
```

Another possible usage would be to help you deploy a package to a code repository, like Bintray, as this package does itself.
In this case, it is always possible that you don't have your key accessible, maybe while you are running tests on a CI
server for example.

```kotlin
bintray {
    // This will print a warning and use "defaultValue" if the files are missing or the key is not defined.
    user = credentials.getStringOrElse("bintray.user", "defaultValue")
    // THis will default to an empty string if the key cannot be found
    key = credentials.getStringOrElse("bintray.key")
}
```

### CLI

There are five commands made available through this plugin.

1. `credentialsGenerate`: Create a new credentials/master key pair in the project
1. `credentialsRekey`: Reencrypt the credentials file using a newly generated key
1. `credentialsUpsert --key=myKey --value=secret`: Add a new key to the credentials file with the given value
1. `credentialsDelete --key=myKey` Remove the given key from te credentials file
1. `credentialsShow`: Print out the decrypted credentials file

[ico-travisci]: https://img.shields.io/travis/camuthig/credentials-gradle.svg?style=flat-square
[link-travisci]: https://travis-ci.org/camuthig/credentials-gradle
