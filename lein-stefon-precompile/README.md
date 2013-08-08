# Stefon precompile - a leiningen plugin

A leiningen plugin for precompiling assets using Stefon, similar to Rails' `rake asset-precompile`.

## Installation

In `:dev-dependencies`, add:

```clojure
[lein-stefon-precompile "0.1"]
```

## Usage

Run

```bash
lein stefon-precompile
```

and that's all.
Unless you changed stefon's default settings, in which case you must tell lein-stefon-precompile where to find the settings for your project.
In which case, add :stefon-options to your `project.clj` file.
There are a few options for expression :stefon-options:

1.
  A string containing the name of a var in your project, where the var is expected to hold the stefon-options map:

  ```clojure
  :stefon-options "circle.http.assets/stefon-options"
  ```

2.
  A string containing the name of a function in your project, where the function returns a stefon-optinos map:

  ```clojure
  :stefon-options "circle.http.assets/stefon-options-fn"
  ```

3.
  A map containing the stefon options

  ```clojure
  :stefon-options {:production true}
  ```