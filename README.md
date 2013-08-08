# Stefon

Stefon is an asset pipeline for Clojure, closely modelled after Ruby's [Sprockets](https://github.com/sstephenson/sprockets).
It is a rewrite of [dieter](https://github.com/edgecase/dieter).

Stefon is fast, easy to use, and easy to extend.
It uses idiomatic clojure, and is written to support both development use (ie it is very fast), and production (it precompiles
Stefon's major selling points are speed, its ease of use in production and development, and its ability to support multiple languages and compressors.
It is easy to

## Usage

Stefon is an asset pipeline.
In production, it is used to compile assets to be served by a CDN or via Ring's file-wrap middleware.
In develepment mode, it serves files directly, recompiling them on changes.

It's primary usage is to compile assets directly, though in development mode it also serves files directly, and reduces compilation time.


### Installation

Add stefon as a dependency in leiningen

  :dependencies [[stefon "0.5.0"]]
  :plugins [[lein-stefon-precompile "0.5.0"]]

Insert it into your ring middleware stack

```clojure
(-> app
    (stefon/asset-pipeline config-options))
```

Or if you use noir

```clojure
(server/add-middleware stefon/asset-pipeline config-options)
```

### Supported passes in the pipeline

+ Concatenating JS (*.js.stefon)
+ Concatenating CSS (*.css.stefon)
+ [LESS CSS](http://lesscss.org/) (*.css.less)
+ [CoffeeScript](http://jashkenas.github.com/coffee-script/) (*.js.coffee)
+ [HamlCoffee](https://github.com/9elements/haml-coffee) (*.js.hamlc)
+ Minifying JS using the Google Closure compiler (*.closure.js)

Concatenation of assets is handled by a Stefon manifest file.
A manifest is a file whose name ends in .stefon and whose contents are
a clojure vector of file names / directories to concatenate.

For example, a file named assets/javascripts/app.js.stefon with the following contents:

```clojure
[
  "./base.js"
  "framework.js"
  "./lib/"
  "./models/"
]
```

Stefon would look for base.js in the same directory, and then concatenate each file from the lib and models directories.
It concatenated all files in order.
Directory contents are concatenated in alphabetical order.


### Linkage

In order to include links to your assets you may use the link-to-asset function.

```clojure
(link-to-asset "stylesheets/reset.css" config-options)
(link-to-asset "javascripts/app.js.stefon" config-options)
```

### Precompilation

To precompile files, we need to actually precompile them:

```
lein stefon-precompile
```

and then load the precompiled files

```clojure
(defn init []
      (dieter/init dieter-options))
```


### Configuration Options

    :asset-roots ["resources"]          ; must have a folder called 'assets'. Searched for assets in the order listed.
    :cache-mode :development            ; or :production. :development disables cacheing
    :precompiles ["./assets/myfile.js.stefon"] ; list of files for `lein stefon-precompile` to precompile. If left blank (the default), all files will be precompiled, and errors will be ignored.

Stefon checks for your assets in [asset-root]/assets.

Note you need to pass your config options to asset-pipeline as well as link-to-asset.

## Contributing

It is easy to add new preprocessors to stefon.
Most asset types uses the default library for that language, hooked up to stefon using V8.
See stefon-core/src/stefon/assets/ for easy-to-follow examples.

## License

Distributed under the Eclipse Public License, the same as Clojure.

## Authors

Mostly written by Paul Biggar from [CircleCI](https://circleci.com).
Based on a fork of [dieter](https://github.com/edgecase/dieter) by John Andrews from EdgeCase.
With contributions [by many other](https://github.com/circleci/stefon/graphs/contributors)


## Changelog

### Version 0.5.0 (first release of stefon)
* Almost complete rewrite, with many backward incompatible changes
* forked to circleci/stefon
* No longer support Rhino
* production mode always loads from disk - there is no option to compile lazily
* dev mode is completely in memory
* the timestamp thing is gone
* Many settings removed, we're down to :asset-roots, :precompile-root, :cache-mode and :precompiles
- The pipeline is now truly a pipeline, supporting more than one transformation per file
- Add data-uri support
- allow options to be passed to each compiler
- add image compression
- add more compressors
- add more languages, esp markdown
- cdn support
- drop support for lein1, lein2 only

### Version 0.4.0 (released as dieter)
* Remove support for searching for filenames, because it has very sharp edges
* Throw a FileNotFoundException instead of failing silently when files in a manifest aren't found
* Directory contents are listed in alphabetical order (avoids intermittent failures due to file directory order on Linux)
* Rewritten internals, with more reliable and consistent string and filename handling
* Referring to assets using different extensions is no longer supported

### Version 0.3.0 (released as dieter)
* Use v8 for Less, Hamlcoffee and CoffeeScript
* Cache and avoid recompiling CoffeeScript and HamlCoffee files which haven't changed
* Update to lein2
* Improve stack traces upon failure in Rhino
* Update Coffeescript (1.3.3), Less (1.3.0) and Hamlcoffee (1.2.0) versions
* Ignore transient files from vim and emacs
* Better error reporting of HamlCoffee
* Support multiple asset directories
* Add expire-never headers
* Improve Rhino speed by using one engine per thread
* Update to latest Rhino for better performance
* Support for `lein stefon-precompile`
* Add mime type headers for stefon files

### Version 0.2.0 (released as dieter)
* Handlebars templates are now a separate library. [dieter-ember](https://github.com/edgecase/dieter-ember)
