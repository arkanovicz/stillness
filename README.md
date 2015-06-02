# Stillness
Extraction template engine with the same syntax as Apache Velocity.

## Basic Idea

Fill an Apache Velocity Context (aka a string -> object map) by trying to match a template against a formatted text.

## Invocation

## Template Syntax

### Variable

 ̀prefix $foo suffix` sets $foo with the text found between "prefix" and "suffix".

### Property

`prefix $foo.bar suffix` calls $foo.bar setter (typically `$foo.setBar()`) with the text found between `prefix` and `suffix`.

When there are nested qualifiers, as in `$foo.bar.foobar`, the corresponding call is `$foo.getBar().setFoobar(text...)`.

### Method call

`$foo.doSomething()` ???

### #set directive

#set($foo = 'bar') assigns 'bar' to $foo
#set($foo = 'prefix $bar suffix') assigns found text to $bar, and full matching string to $foo.

### #if, #else, #elseif directives

#if (**condition1**) **block1**
#elseif (**condition2**) **block2**
#else **block3**
#end

Tests can be character strings, in which case they will evaluate to true only if matched.

### #foreach directive

#foreach ($ref in $arg)
  **body**
#end

$arg must be an ArrayList object, in which will be stored found $ref objects at each iteration.
If the body directly uses $ref, then $ref objects will be strings. If, on the contrary, the body uses expressions like $ref.foo, then $ref will be a Map of matched values.

### #include and #parse directives

#include('file') will include target file as raw text to be matched.

#parse('file') will include target file as template file to be matched.

### Macros

As in Apache Velocity, it's possible to define macros:

`#macro(**name** **arg1** ... **argn**)`
  `**body**`
`#end`

### #match directive

`#match(**expression**)` checks if the expression is present in the source, and stop execution if it isn't.

### #regex directive

Same behaviour than #match, but with a regular expression.
Moreover, it's possible to capture groups.

### #follow directive

#follow(**url**)
  **body**
#end

Fetches the resource located at **url** and uses as a source on which the **body** is matched.

### Error output

In case one the patterns of the template is not matched in the source, the execution of the parser stops and an error output is displayed. This output will take the form of a colorized HTML file, to clearly show where the error does happen.
