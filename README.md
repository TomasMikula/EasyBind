EasyBind
========

EasyBind leverages lambdas to reduce boilerplate when creating custom bindings.

It also provides a type-safe alternative to `Bindings.select*` methods.

This work is inspired by Anton Nashatyrev's [feature request](https://javafx-jira.kenai.com/browse/RT-35923), which is planned for JavaFX 9. Until then, you have EasyBind.


map
---

Creates a binding whose value is a mapping of some observable value.

```java
ObservableStringValue str = ...;
Binding<Integer> len = EasyBind.map(str, String::length);
```

Compare to plain JavaFX:

```java
ObservableStringValue str = ...;
IntegerBinding len = Bindings.createIntegerBinding(() -> str.get().length(), str);
```

The difference is subtle, but important: In the latter version, `str` is repeated twice &mdash; once in the function to compute binding's value and once as binding's dependency. This opens the possibility that a wrong dependency is specified by mistake.


combine
-------

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


select
------

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
