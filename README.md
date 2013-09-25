# Stefon

Stefon is an asset pipeline for Clojure, closely modelled after Ruby's [Sprockets](https://github.com/sstephenson/sprockets).
It is a rewrite of [dieter](https://github.com/edgecase/dieter).

Stefon is fast, easy to use, and easy to extend.
It uses idiomatic clojure, and is written to support both development use (ie it is very fast), and production use (it precompiles for deployment to a CDN).
Stefon's major selling points are speed, its ease of use in production and development, and its ability to support multiple languages and compressors.


## Usage

Stefon is an asset pipeline.
In production, it is used to precompile assets to be served by a CDN or via Ring's file-wrap middleware.
In develepment mode, it serves files directly, recompiling them on changes.


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
- Minifying JS using the Google Closure compiler (*.closure.js - coming soon)
- Minifying CSS using by removing whitespace (coming soon)
- Replacing asset references (calls to data-uri, etc) with the relevant information about the asset (*.ref)

#### .stefon files

Concatenation of assets is handled by a Stefon manifest file.
A manifest is a file whose name ends in .stefon and whose contents are
a clojure vector of file names / directories to concatenate.

For example, a file named `assets/javascripts/app.js.stefon` with the following contents:

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

#### .less files

Less files have one caveat at present: they read their imports directly from file system, without going through stefon.
That means that you can't use .less.ref files.
As a workaround, make your root file a .ref.less file, and do the assetifying after less compilation has finished.

#### .ref files

To refer to other assets from your asset, use a .ref file.
This allows you to write functions that reference other assets, such as data-uri and asset-path.
If you're familiar with sprockets, this is similar to how you might use a .erb file.

There are some sharp edges here, at the moment.
In particular, there is no dependency tracking, and caching compiled assets is very naive.
This means if you refer to asset A from asset B, and then change A, B won't actually change unless you restart your program.




### Linkage

In order to include links to your assets you may use the link-to-asset function.

```clojure
(link-to-asset "stylesheets/reset.css" config-options)
(link-to-asset "javascripts/app.js.stefon" config-options)
```

### Precompilation

To use precompilation, we need to actually precompile files:

```
lein stefon-precompile
```

and then load the precompiled files

```clojure
(defn init []
  (stefon/init stefon-options))
```


### Configuration Options

The following configuration options are available.

```clojure
;; Searched for assets in the order listed. Must have a directory called 'assets'.
:asset-roots ["resources"]

;; The root for compiled assets, which are written to (serving-root)/assets. In dev mode defaults to "/tmp/stefon")
:serving-root "public"

;; Set to :production to serve precompiled files, or when running `lein stefon-precompile`
:mode :development

;; Where the result of the precompile should be stored. Might be good to keep it out of the web root.
:manifest-file "manifest.json"

;; When precompiling, the list of files to precompile. Can take regexes (coming soon), which will attempt to match all files in the asset roots.
:precompiles ["./assets/myfile.js.stefon"]
```

Note you need to pass your config options to `asset-pipeline` as well as `link-to-asset`.

## Contributing

It is easy to add new preprocessors to stefon.
Most asset types uses the default library for that language, hooked up to stefon using V8.
See [the source](https://github.com/circleci/stefon/blob/master/stefon-core/src/stefon/asset) for easy-to-follow examples.

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
* No longer supports Rhino
* production mode always loads from disk - there is no option to compile lazily
* dev mode mirrors production mode exactly
* the timestamp thing is gone
* Many settings removed, we're down to :asset-roots, :serving-root, :mode, manifest-file and :precompiles
* The pipeline is now truly a pipeline, supporting more than one transformation per file
* Add data-uri support
* drop support for lein1, lein2 only
* use proper tmp dirs

## planned before release
- allow options to be passed to each compiler
- support different versions of each language
- add image compression
- add more compressors
- add more languages, esp markdown
- cdn support (port from circleci)

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
