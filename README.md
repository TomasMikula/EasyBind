EasyBind
========

EasyBind leverages lambdas to reduce boilerplate when creating custom bindings.

It also provides a type-safe alternative to `Bindings.select*` methods.

This work is inspired by Anton Nashatyrev's [feature request](https://javafx-jira.kenai.com/browse/RT-35923), which is planned for JavaFX 9. Until then, you can use EasyBind.


API
---

### map

Creates a binding whose value is a mapping of some observable value.

```java
ObservableStringValue str = ...;
Binding<Integer> strLen = EasyBind.map(str, String::length);
```

Compare to plain JavaFX:

```java
ObservableStringValue str = ...;
IntegerBinding strLen = Bindings.createIntegerBinding(() -> str.get().length(), str);
```

The difference is subtle, but important: In the latter version, `str` is repeated twice &mdash; once in the function to compute binding's value and once as binding's dependency. This opens the possibility that a wrong dependency is specified by mistake.


### combine

Creates a binding whose value is a combination of two or more (currently up to six) observable values.

```java
ObservableStringValue str = ...;
ObservableValue<Integer> start = ...;
ObservableValue<Integer> end = ...;
Binding<String> subStr = EasyBind.combine(str, start, end, String::substring);
```

Compare to plain JavaFX:

```java
ObservableStringValue str = ...;
ObservableIntegerValue start = ...;
ObservableIntegerValue end = ...;
StringBinding subStr = Bindings.createStringBinding(() -> str.get().substring(start.get(), end.get()), str, start, end);
```

Same difference as before &mdash; in the latter version, `str`, `start` and `end` are repeated twice, once in the function to compute binding's value and once as binding's dependencies, which opens the possibility of specifying wrong set of dependencies. Plus, the latter is getting less readable.


### select

Type-safe alternative to `Bindings.select*` methods. The following example is borrowed from [RT-35923](https://javafx-jira.kenai.com/browse/RT-35923).

```java
Binding<Boolean> bb = EasyBind.select(control.sceneProperty()) 
        .select(s -> s.windowProperty()) 
        .selectObject(w -> w.showingProperty());
```

Compare to plain JavaFX:

```java
BooleanBinding bb = Bindings.selectBoolean(control.sceneProperty(), "window", "isShowing");
```

The latter version is not type-safe, which means it may cause runtime errors.


Use EasyBind in your project
----------------------------

### Method 1: as a managed dependency (recommended)

Snapshot releases are deployed to Sonatype snapshot repository with these Maven coordinates

| Group ID            | Artifact ID | Version        |
| :-----------------: | :---------: | :------------: |
| org.fxmisc.easybind | easybind    | 1.0.0-SNAPSHOT |

#### Gradle example

```groovy
repositories {
    maven {
        url 'https://oss.sonatype.org/content/repositories/snapshots/' 
    }
}

dependencies {
    compile group: 'org.fxmisc.easybind', name: 'easybind', version: '1.0.0-SNAPSHOT'
}
```

#### Sbt example

```scala
resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

libraryDependencies += "org.fxmisc.easybind" % "easybind" % "1.0.0-SNAPSHOT"
```


### Method 2: as an unmanaged dependency

[Download](https://oss.sonatype.org/content/repositories/snapshots/org/fxmisc/easybind/easybind/1.0.0-SNAPSHOT/) the latest JAR file and place it on your classpath.

