# Stillness

Reverse templating engine for data extraction, using Apache Velocity syntax.

[![Maven Central](https://img.shields.io/maven-central/v/com.republicate/stillness.svg)](https://search.maven.org/artifact/com.republicate/stillness)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

## Basic Idea

Fill an Apache Velocity Context (a string → object map) by matching a template against formatted text. Instead of *generating* text from a template, Stillness *extracts* data using a template as a pattern.

## Maven Dependency

```xml
<dependency>
    <groupId>com.republicate</groupId>
    <artifactId>stillness</artifactId>
    <version>1.0</version>
</dependency>
```

## Quick Example

Given this HTML snippet:
```html
<div class="product">
    <span class="name">Widget Pro</span>
    <span class="price">$29.99</span>
</div>
```

And this template (`product.rtl`):
```velocity
<div class="product">
    <span class="name">$product.name</span>
    <span class="price">$product.price</span>
</div>
```

Stillness extracts: `{product: {name: "Widget Pro", price: "$29.99"}}`

## Invocation

```java
import com.republicate.stillness.Stillness;
import org.apache.velocity.context.Context;

Stillness scraper = new Stillness();
scraper.initialize("stillness.properties");
Context ctx = scraper.scrape("http://example.com/page", "template.rtl");
// ctx now contains extracted values
```

Refer to the `Stillness` class source for the full API, and `StillnessTool` for servlet integration.

## Configuration

Configuration file example:

~~~~
###
# stillness properties 

# concatenate all sequence of whitespace or line return as a single space
stillness.normalize = true

# new directive
userdirective=com.republicate.stillness.directive.MatchDirective
userdirective=com.republicate.stillness.directive.RegexDirective
userdirective=com.republicate.stillness.directive.FollowDirective
userdirective=com.republicate.stillness.directive.OptionalDirective

###
# velocity properties
runtime.log=/...path.../stillness.log
runtime.log.error.stacktrace = true
runtime.log.warn.stacktrace = true
runtime.log.info.stacktrace = true
runtime.log.invalid.reference = true
runtime.interpolate.string.literals = true
~~~~

## Template Syntax

### Variable

`prefix $foo suffix` sets $foo with the text found between "prefix" and "suffix".

### Property

`prefix $foo.bar suffix` calls $foo.bar setter (typically `$foo.setBar()`) with the text found between `prefix` and `suffix`.

When there are nested qualifiers, as in `$foo.bar.foobar`, the corresponding call is `$foo.getBar().setFoobar(text...)`.

### Method call

`$foo.doSomething()`

### #set directive

`#set($foo = 'bar')` assigns 'bar' to $foo

`#set($foo = 'prefix $bar suffix')` assigns found text to $bar, and full matching string to $foo.

### #if, #else, #elseif directives

<pre>
#if (<i>condition1</i>) <i>block1</i>
#elseif (<i>condition2</i>) <i>block2</i>
#else <i>block3</i>
#end
</pre>

Tests can be character strings, in which case they will evaluate to true only if matched.

### #foreach directive

<pre>
#foreach ($ref in $arg)
  <i>body</i>
#end
</pre>

$arg must be an ArrayList object, in which will be stored found $ref objects at each iteration.
If the body directly uses $ref, then $ref objects will be strings. If, on the contrary, the body uses expressions like $ref.foo, then $ref will be a Map of matched values.

### #include and #parse directives

`#include('file')` will include target file as raw text to be matched.

`#parse('file')` will include target file as template file to be matched.

### Macros

As in Apache Velocity, it's possible to define macros:

<pre>
`#macro(<i>name</i> <i>arg1</i> ... <i>argn</i>)`
  `<i>body</i>`
`#end`
</pre>

### #match directive

`#match(expression)` checks if the expression is present in the source, and stop execution if it isn't.

### #regex directive

Same behaviour than #match, but with a regular expression.
Moreover, it's possible to capture groups.

### #optional directive

`#optional(expression)`
Same as match, but continue anyway.

### #follow directive

<pre>
#follow(<i>url</i>)
  <i>body</i>
#end
</pre>

Fetches the resource located at **url** and uses as a source on which the **body** is matched.

### Error output

In case one the patterns of the template is not matched in the source, the execution of the parser stops and an error output is displayed. This output will take the form of a colorized HTML file, to clearly show where the error does happen.
