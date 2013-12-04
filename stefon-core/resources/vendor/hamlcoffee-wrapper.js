var coffeeError;

function noExt(filename) {
  var no_ext = filename.substr(0, filename.lastIndexOf('.')) || filename;
  //no_ext = no_ext.replace(new RegExp("/","gm"),"_"); // to use the fill name, add underscores
  return no_ext;
}

// expects option_overrides to be a json object or null/undefined
function compileHamlCoffee(input, absolute, filename, option_overrides) {
  var k, v, _ref, __hasProp = {}.hasOwnProperty;
  var options = {
    name_fn: noExt,
    source: input,
    jst: true,
    namespace: null,
    format: "html5",
    uglify: false,
    basename: true,
    escapeHtml: true,
    escapeAttribues: true,
    cleanValue: false,
    placement: "global",
    dependencies: {},
    customeHtmlEscape: '',
    customCleanValue: '',
    customPreserve: '',
    customFindAndPreserve: '',
    customSurround: '',
    customSucceed: '',
    customPrecede: '',
    preserveTags: '',
    selfCloseTags: '',
    context: '',
    extendScope: false
  };

  if (typeof option_overrides !== "undefined" && option_overrides !== null) {
    _ref = JSON.parse(option_overrides);
    for (k in _ref) {
      if (!__hasProp.call(_ref, k)) continue;
      options[k] = _ref[k];
    }
  }

  return HamlCoffeeAssets.compile(options.name_fn.call(null, filename),
                                  options.source,
                                  options.jst,
                                  options.namespace,
                                  options.format,
                                  options.uglify,
                                  options.basename,
                                  options.escapeHtml,
                                  options.escapeAttributes,
                                  options.cleanValue,
                                  options.placement,
                                  options.dependencies,
                                  options.customeHtmlEscape,
                                  options.customCleanValue,
                                  options.customPreserve,
                                  options.customFindAndPreserve,
                                  options.customSurround,
                                  options.customSucceed,
                                  options.customPrecede,
                                  options.preserveTags,
                                  options.selfCloseTags,
                                  options.context,
                                  options.extendScope
                                 );
}
