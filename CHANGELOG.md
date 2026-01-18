# Changelog

All notable changes to Stillness will be documented in this file.

## [1.0] - 2026-01-19

First stable release after years of production use.

### Features
- Reverse templating using Apache Velocity syntax
- Variable extraction: `prefix $var suffix`
- Property extraction: `$obj.property`
- Iteration with `#foreach`
- Conditionals with `#if`/`#elseif`/`#else`
- Macros with `#macro`
- Custom directives:
  - `#match(expr)` - require pattern presence
  - `#regex(pattern)` - regex matching with capture groups
  - `#optional(expr)` - optional pattern matching
  - `#follow(url)` - fetch and parse linked resources
- Alternate values with Velocity syntax `${left|right}`
- Debug HTML output showing exact match failure location
- Whitespace normalization option
- StillnessTool for servlet integration

### Changed
- Updated dependencies (junit 4.13.1, slf4j 1.7.32)
- Fixed URLs in pom.xml (http → https)

## [0.6] - 2021

- Allow foreach to create list directly in context
- #define directive improvements
- Debug output enhancements
- Macro execution fixes

## [0.5] - 2020

- Initial public version
- Core extraction engine
- Basic directive support
