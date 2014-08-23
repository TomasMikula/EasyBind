EasyBind
========

EasyBind leverages lambdas to reduce boilerplate when creating custom bindings, provides a type-safe alternative to `Bindings.select*` methods (inspired by Anton Nashatyrev's [feature request](https://javafx-jira.kenai.com/browse/RT-35923), planned for JavaFX 9) and adds _monadic_ operations to `ObservableValue`.


Static methods
--------------

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

### bind list

Occasionally one needs to synchronize the contents of an (observable) list with another observable list. If that is your case, [`listBind`](http://www.fxmisc.org/easybind/javadoc/org/fxmisc/easybind/EasyBind.html#listBind-java.util.List-javafx.collections.ObservableList-) is your friend:

```java
ObservableList<T> sourceList = ...;
List<T> targetList = ...;
EasyBind.listBind(targetList, sourceList);
```

### subscribe to values

Often one wants to execute some code for _each_ value of an `ObservableValue`, that is for the _current_ value and _each new_ value. This typically results in code like this:

```java
this.doSomething(observable.getValue());
observable.addListener((obs, oldValue, newValue) -> this.doSomething(newValue));
```

This can be expressed more concisely using the [`subscribe`](http://www.fxmisc.org/easybind/javadoc/org/fxmisc/easybind/EasyBind.html#subscribe-javafx.beans.value.ObservableValue-java.util.function.Consumer-) helper method:

```java
EasyBind.subscribe(observable, this::doSomething);
```

### conditional collection membership

[`EasyBind.includeWhen`](http://www.fxmisc.org/easybind/javadoc/org/fxmisc/easybind/EasyBind.html#includeWhen-java.util.Collection-T-javafx.beans.value.ObservableValue-) includes or excludes an element in/from a collection based on a boolean condition.

Say that you want to draw a graph and highlight an edge when the edge itself or either of its end vertices is hovered over. To achieve this, let's add `.highlight` CSS class to the edge node when either of the three is hovered over and remove it when none of them is hovered over:

```java
BooleanBinding highlight = edge.hoverProperty()
        .or(v1.hoverProperty())
        .or(v2.hoverProperty());
EasyBind.includeWhen(edge.getStyleClass(), "highlight", highlight);
```

```css
.highlight { -fx-stroke: green; }
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
    Optional<T> getOpt();
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

### Stable release

Current stable release is 1.0.3.

#### Maven coordinates

| Group ID            | Artifact ID | Version |
| :-----------------: | :---------: | :-----: |
| org.fxmisc.easybind | easybind    | 1.0.3   |

#### Gradle example

```groovy
dependencies {
    compile group: 'org.fxmisc.easybind', name: 'easybind', version: '1.0.3'
}
```

#### Sbt example

```scala
libraryDependencies += "org.fxmisc.easybind" % "easybind" % "1.0.3"
```

#### Manual download

[Download](https://github.com/TomasMikula/EasyBind/releases/download/v1.0.3/easybind-1.0.3.jar) the JAR file and place it on your classpath.


### Snapshot releases

Snapshot releases are deployed to Sonatype snapshot repository.

#### Maven coordinates

| Group ID            | Artifact ID | Version        |
| :-----------------: | :---------: | :------------: |
| org.fxmisc.easybind | easybind    | 1.0.4-SNAPSHOT |

#### Gradle example

```groovy
repositories {
    maven {
        url 'https://oss.sonatype.org/content/repositories/snapshots/' 
    }
}

dependencies {
    compile group: 'org.fxmisc.easybind', name: 'easybind', version: '1.0.4-SNAPSHOT'
}
```

#### Sbt example

```scala
resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

libraryDependencies += "org.fxmisc.easybind" % "easybind" % "1.0.4-SNAPSHOT"
```

#### Manual download

[Download](https://oss.sonatype.org/content/repositories/snapshots/org/fxmisc/easybind/easybind/1.0.4-SNAPSHOT/) the latest JAR file and place it on your classpath.


Links
-----

[Javadoc](http://www.fxmisc.org/easybind/javadoc/overview-summary.html)
