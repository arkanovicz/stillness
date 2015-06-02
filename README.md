# Stillness
Extraction template engine with the same syntax as Apache Velocity.

## Basic Idea

Fill an Apache Velocity Context (aka a string -> object map) by trying to match a template against a formatted text.

## Invocation

Example:

~~~~
import stillness.Stillness;
import stillness.ScrapeException;
import org.apache.velocity.context.Context;

...

{
  Stillness scraper = new Stillness();
  scraper.initialize(".../stillness.properties"); // see configuration properties below
  String template = ".../template.rtl";
  String target = "http://...target page url...";
  Context ctx = stillness.scrape(target, template);
  // ... then do something with ctx, why not use Apache Velocity to merge a template with it.
}
~~~~

Refer to the stillness.Stillness class source to see the full API.

## Configuration

Configuration file example:

~~~~
###
# stillness properties 

# concatenate all sequence of whitespace or line return as a single space
stillness.normalize = true1

# new directive
userdirective=stillness.directive.MatchDirective
userdirective=stillness.directive.RegexDirective
userdirective=stillness.directive.FollowDirective
userdirective=stillness.directive.OptionalDirective

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
