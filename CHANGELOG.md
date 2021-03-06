Change Log
==========

All notable changes to this project will be documented in this file, which
follows the conventions of [keepachangelog.com](http://keepachangelog.com/).
This project adheres to [Semantic Versioning](http://semver.org/).

## [Unreleased]

...

## [0.7.1] - 2018-01-17

This version is an extremely breaking change, essentially rewriting the library
as `clj-multistream` to reflect the updated name in the multiformats standard.
Many things have changed in the process; the biggest change is to the way that
codecs are composed and that encoding and decoding streams are first-class
concepts now.

See the updated README file for more information.

## [0.5.1] - 2016-02-04

### Added
- Add `encode-with-header!` and `decode-with-header!` functions to core
  namespace, which write and read headers before encoded data.
- Add `header/*headers*` dynamic var which will capture the actual header paths
  which were read or written during an operation.

### Changed
- Mux codecs no longer have a default header of `"/multicodec"`.

## [0.5.0] - 2016-01-22

This version includes a breaking change which splits `multicodec.codecs` into
multiple smaller namespaces. This also adds the _codec predicates_ `encodable?`
and `decodable?`, to determine whether a given codec supports encoding a value
or decoding a header, respectively.

### Added
- Add `bin/BinaryData` protocol to make bin-codec more extensible.
- Add `mux/select` convenience function.
- Add `mux/*dispatched-codec*`, which can be bound to discover the key for
  the codec that `mux-codec` actually used on the input.
- Add `multicodec.codecs.filter` codec to apply arbitrary transformation
  functions on values as they are processed.

### Changed
- Moved codecs into separate namespaces under `multicodec.codecs`.

## [0.4.0] - 2015-11-24

### Changed
- Rename key for binary codec from `:binary` to `:bin`.
- MuxCodec now selects codecs by user-provided keys instead of directly. This
  simplifies the implementation of new selection functions.

## [0.3.0] - 2015-11-21

### Added
- Add `Encoder` and `Decoder` protocols to core with stream-based methods
  `encode!` and `decode!`, respectively.
- Add `encode` and `decode` to core which operate on byte arrays.
- Add `multicodec.codecs` namespace with a few useful codec implementations.

### Changed
- Rename `core/paths` to `core/headers`.
- Move header functions to new `multicodec.header` namespace.
- On validation errors, header functions now throw exceptions with type
  information via `ex-data`.

## [0.2.0] - 2015-11-19

### Added
- `multicodec.core/paths` provides a collection of standard codec paths to use
  for various encodings.

## 0.1.0 - 2015-11-19

Initial project release

[Unreleased]: https://github.com/greglook/clj-multicodec/compare/0.7.1...HEAD
[0.7.1]: https://github.com/greglook/clj-multicodec/compare/0.5.1...0.7.1
[0.5.1]: https://github.com/greglook/clj-multicodec/compare/0.5.0...0.5.1
[0.5.0]: https://github.com/greglook/clj-multicodec/compare/0.4.0...0.5.0
[0.4.0]: https://github.com/greglook/clj-multicodec/compare/0.3.0...0.4.0
[0.3.0]: https://github.com/greglook/clj-multicodec/compare/0.2.0...0.3.0
[0.2.0]: https://github.com/greglook/clj-multicodec/compare/0.1.0...0.2.0
