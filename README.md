EasyBind
========

EasyBind leverages lambdas to reduce boilerplate when creating custom bindings, provides a type-safe alternative to `Bindings.select*` methods (inspired by Anton Nashatyrev's [feature request](https://javafx-jira.kenai.com/browse/RT-35923), planned for JavaFX 9) and adds _monadic_ operations to `ObservableValue`.


Binding factory methods
-----------------------

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


### map list

Returns a mapped view of an ObservableList.

```java
ObservableList<String> tabIds = EasyBind.map(tabPane.getTabs(), Tab::getId);
```

In the above example, `tabIds` is updated as tabs are added and removed from `tabPane`.

An equivalent feature has been requested in [RT-35741](https://javafx-jira.kenai.com/browse/RT-35741) and is scheduled for JavaFX 9.


### combine list

Turns an _observable list_ of _observable values_ into a single observable value. The resulting observable value is updated when elements are added or removed to or from the list, as well as when element values change.

```java
Property<Integer> a = new SimpleObjectProperty<>(5);
Property<Integer> b = new SimpleObjectProperty<>(10);
ObservableList<Property<Integer>> list = FXCollections.observableArrayList();

Binding<Integer> sum = EasyBind.combine(
        list,
        stream -> stream.reduce((a, b) -> a + b).orElse(0));

assert sum.getValue() == 0;

// sum responds to element additions
list.add(a);
list.add(b);
assert sum.getValue() == 15;

// sum responds to element value changes
a.setValue(20);
assert sum.getValue() == 30;

// sum responds to element removals
list.remove(a);
assert sum.getValue() == 10;
```

You don't usually have an observable list of _observable_ values, but you often have an observable list of something that _contains_ an observable value. In that case, use the above `map` methods to get an observable list of observable values, as in the example below.

#### Example: Disable "Save All" button on no unsaved changes

Assume a tab pane that contains a text editor in every tab. The set of open tabs (i.e. open files) is changing. Let's further assume we use a custom Tab subclass `EditorTab` that has a boolean `savedProperty()` that indicates whether changes in its editor have been saved.

**Task:** Keep the _"Save All"_ button disabled when there are no unsaved changes in any of the editors.

```java
ObservableList<ObservableValue<Boolean>> individualTabsSaved =
        EasyBind.map(tabPane.getTabs(), t -> ((EditorTab) t).savedProperty());

ObservableValue<Boolean> allTabsSaved = EasyBind.combine(
        individualTabsSaved,
        stream -> stream.allMatch(saved -> saved));

Button saveAllButton = new Button(...);
saveAllButton.disableProperty().bind(allTabsSaved);
```


Monadic observable values
-------------------------

[MonadicObservableValue](http://www.fxmisc.org/easybind/javadoc/org/fxmisc/easybind/monadic/MonadicObservableValue.html) interface adds monadic operations to `ObservableValue`.

```java
interface MonadicObservableValue<T> extends ObservableValue<T> {
    boolean isPresent();
    boolean isEmpty();
    void ifPresent(Consumer<? super T> f);
    T getOrThrow();
    T getOrElse(T other);
    MonadicBinding<T> orElse(T other);
    MonadicBinding<T> orElse(ObservableValue<T> other);
    MonadicBinding<T> filter(Predicate<? super T> p);
    <U> MonadicBinding<U> map(Function<? super T, ? extends U> f);
    <U> MonadicBinding<U> flatMap(Function<? super T, ObservableValue<U>> f);
    <U> SelectBuilder<U> select(Function<? super T, ObservableValue<U>> f);
}
```

Read more about monadic operations in [this blog post](http://tomasmikula.github.io/blog/2014/03/26/monadic-operations-on-observablevalue.html).


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


Links
-----

[Javadoc](http://www.fxmisc.org/easybind/javadoc/overview-summary.html)
