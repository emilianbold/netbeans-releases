requirejs.config({
//    baseUrl: 'js/app',
    paths: {
        config: "js/fileCC/main3",
        lib: "folder1"
    },
    shim: {
        'jquery': {
            exports: ['jQuery', '$']
        }
        ,
        'piwik': {
            exports: 'piwik'
        }
    }
});

define(
        [
            'folder1/module1',
            './folder1/module1',
            'utils',
            'api/v0.1/Options',
            'lib/api/v0.1/OMessages'
        ], 
        function (module1, module11, utils) {
        utils.utilMethod1();
});