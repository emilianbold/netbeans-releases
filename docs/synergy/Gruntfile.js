var execSync = require("child_process").execSync;
var svnRevision = execSync("svn info -r 'HEAD' | grep Revision: | awk -F' ' '{print $2}'").toString();
svnRevision = svnRevision.replace(/\s/g, "");
module.exports = function (grunt) {
    var timestamp = Date.now();
    grunt.initConfig({
        pkg: grunt.file.readJSON('package.json'),
        replace: {
            index: {
                src: ['client/app/index.html'],
                dest: ['client/app/index.html'],
                replacements: [{
                        from: /synergy\.js\?v=[0-9]+"/,
                        to: 'synergy.js?v=' + timestamp + "\""
                    }, {
                        from: /polyfills\.js\?v=[0-9]+"/,
                        to: 'polyfills.js?v=' + timestamp + "\""
                    }]
            },
            version: {
                src: ['client/app/js/configuration.js'],
                dest: ['client/app/js/configuration.js'],
                replacements: [{
                        from: /this\.version = "1\.0\.[0-9]+";/,
                        to: 'this.version = "1.0.' + svnRevision + '";'
                    }]
            },
            index2: {
                src: ['client/app/index2.html'],
                dest: ['client/app/index2.html'],
                replacements: [{
                        from: /synergy\.js\?v=[0-9]+"/,
                        to: 'synergy.js?v=' + timestamp + "\""
                    }, {
                        from: /polyfills\.js\?v=[0-9]+"/,
                        to: 'polyfills.js?v=' + timestamp + "\""
                    }]
            },
            appjs: {
                src: ['client/app/js/app.js'],
                dest: ['client/app/js/app.js'],
                replacements: [{
                        from: /\.html\?v=[0-9]+/g,
                        to: '.html?v=' + timestamp
                    }]
            },
            css: {
                src: ['client/app/index.html'],
                dest: ['client/app/index.html'],
                replacements: [{//custom.css?v=7
                        from: /custom\.css\?v=[0-9]+"/,
                        to: 'custom.css?v=' + timestamp + "\""
                    }]
            },
            css2: {
                src: ['client/app/index2.html'],
                dest: ['client/app/index2.html'],
                replacements: [{//custom.css?v=7
                        from: /custom\.css\?v=[0-9]+"/,
                        to: 'custom.css?v=' + timestamp + "\""
                    }]
            },
            testSynergyPartials: {
                src: ['client/test/app/synergy.js'],
                dest: ['client/test/app/synergy.js'],
                replacements: [{//custom.css?v=7
                        from: /partials\//g,
                        to: '../../app/partials/'
                    }]
            },
            testSynergyResources: {
                src: ['client/test/app/synergy.js'],
                dest: ['client/test/app/synergy.js'],
                replacements: [{//custom.css?v=7
                        from: /\.\.\/\.\.\/server\/api/g,
                        to: '../../../server/api'
                    }]
            },
            testReplaceDatabaseName: {
                src: ['server/setup/conf.php'],
                dest: ['server/setup/conf.php'],
                replacements: [{//custom.css?v=7
                        from: /define\('DHOST', 'mysql:host=localhost;dbname=synergy;charset=UTF8'\);/g,
                        to: 'define(\'DHOST\', \'mysql:host=localhost;dbname=synergy_test;charset=UTF8\');'
                    }]
            },
            replaceDatabaseName: {
                src: ['server/setup/conf.php'],
                dest: ['server/setup/conf.php'],
                replacements: [{//custom.css?v=7
                        from: /define\('DHOST', 'mysql:host=localhost;dbname=synergy_test;charset=UTF8'\);/g,
                        to: 'define(\'DHOST\', \'mysql:host=localhost;dbname=synergy;charset=UTF8\');'
                    }]
            }
        },
        cssmin: {
            combine: {
                files: {
                    'client/app/css/min/custom.css': ['client/app/css/custom.css'],
                    'client/app/css/min/docs.css': ['client/app/css/docs.css'],
                    'client/app/css/min/bootstrap.css': ['client/app/css/bootstrap.css'],
                    'client/app/css/min/bootstrap-responsive.css': ['client/app/css/bootstrap-responsive.css']
                }
            }
        },
        uglify: {
            options: {
                mangle: false,
                banner: '/*! <%= pkg.name %> <%= grunt.template.today("yyyy-mm-dd") %> */\n'
            },
            buildSynergy: {
                files: {
                    'client/app/js/min/synergy.js': ['client/app/js/*.js']
                }
            },
            buildTestSynergy: {
                files: {
                    'client/test/app/synergy.js': ['client/app/js/min/synergy.js']
                }
            }
        },
        jshint: {
            "client": {
                "src": ["client/app/js/*.js"],
                options: {
                    "reporterOutput": "",
                    "force": true,
                    "strict": true,
                    "curly": true,
                    "eqnull": true,
//                    "unused": true,
                    "eqeqeq": true,
                    "undef": true,
//                    "camelcase": true,
                    "forin": true,
                    "immed": true,
                    "latedef": true,
                    "newcap": true,
                    "expr": true,
                    "quotmark": "double",
                    "trailing": true,
//                 "globalstrict": true,//
                    globals: {difflib: true, diffview: true, "$": true, angular: true, window: true, google: true},
                    reporter: require('jshint-stylish'),
                    '-W097': true // use strict in function form warning
                }
            }
        }
    });
    grunt.loadNpmTasks('grunt-contrib-uglify');
    grunt.loadNpmTasks('grunt-contrib-cssmin');
    grunt.loadNpmTasks('grunt-contrib-jshint');
    grunt.loadNpmTasks('grunt-text-replace');
    grunt.registerTask('default', ['jshint', 'replace:index', 'replace:version', 'replace:index2', 'replace:appjs', 'replace:css', 'replace:css2', 'uglify:buildSynergy', 'cssmin', 'replace:replaceDatabaseName']);
    grunt.registerTask('testBuild', ['default', 'uglify:buildTestSynergy', 'replace:testSynergyPartials', 'replace:testReplaceDatabaseName', 'replace:testSynergyResources']);
};
