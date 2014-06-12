/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

var FILE_SEPARATOR = process.env.FILE_SEPARATOR;
var PROJECT_CONFIG = process.env.PROJECT_CONFIG;
var BASE_DIR = process.env.BASE_DIR;
var PROJECT_WEB_ROOT = process.env.PROJECT_WEB_ROOT;
var PROJECT_DIR = process.env.PROJECT_DIR;
var WEB_ROOT_PREFIX = process.env.WEB_ROOT_PREFIX;
var TEST_ROOT_PREFIX = process.env.TEST_ROOT_PREFIX;
var AUTOWATCH = Boolean(process.env.AUTOWATCH);
var KARMA_NETBEANS_REPORTER = process.env.KARMA_NETBEANS_REPORTER;
var COVERAGE = Boolean(process.env.COVERAGE);
var COVERAGE_DIR = process.env.COVERAGE_DIR;

var BROWSERS_MESSAGE = '$NB$netbeans browsers %s';

var util = require('util');
var projectConf = require(PROJECT_CONFIG);

var printMessage = function() {
    var args = Array.prototype.slice.call(arguments);
    process.stdout.write(util.format.apply(null, args) + '\n');
};
var arrayUnique = function(input) {
    return input.filter(function (e, i, arr) {
        return arr.lastIndexOf(e) === i;
    });
};

module.exports = function(config) {
    projectConf(config);

    // base path
    if (config.basePath) {
        if (config.basePath.substr(0, 1) === '/' // unix
                || config.basePath.substr(1, 2) === ':\\') { // windows
            // absolute path, do nothing
        } else {
            config.basePath = BASE_DIR + FILE_SEPARATOR + config.basePath;
        }
    } else {
        config.basePath = BASE_DIR + FILE_SEPARATOR;
    }

    config.reporters = config.reporters || [];
    config.reporters = config.reporters.concat([
        'netbeans'
    ]);
    if (COVERAGE) {
        config.reporters = config.reporters.concat([
            'coverage'
        ]);
    }
    config.reporters = arrayUnique(config.reporters);

    config.plugins = config.plugins || [];
    config.plugins = config.plugins.concat([
        KARMA_NETBEANS_REPORTER
    ]);
    if (COVERAGE) {
        config.plugins = config.plugins.concat([
            'karma-coverage'
        ]);
    }
    config.plugins = arrayUnique(config.plugins);

    printMessage(BROWSERS_MESSAGE, config.browsers.join(','));

    config.colors = true;

    config.autoWatch = AUTOWATCH;

    config.singleRun = false;

    if (COVERAGE) {
        config.preprocessors = config.preprocessors || {};
        var webRootPrefix = WEB_ROOT_PREFIX + FILE_SEPARATOR;
        var testRootPrefix = TEST_ROOT_PREFIX + FILE_SEPARATOR;
        var webRootPrefixLength = WEB_ROOT_PREFIX.length + FILE_SEPARATOR.length;
        var testRootPrefixLength = TEST_ROOT_PREFIX.length + FILE_SEPARATOR.length;
        if (config.coverage) {
            for (var i = 0; i < config.coverage.length; ++i) {
                var file = config.coverage[i];
                config.preprocessors[file] = config.preprocessors[file] || [];
                config.preprocessors[file].push('coverage');
            }
        } else {
            for (var i = 0; i < config.files.length; ++i) {
                var file = config.files[i];
                var inWebRoot = WEB_ROOT_PREFIX && file.substr(0, webRootPrefixLength) === webRootPrefix;
                var inTests = TEST_ROOT_PREFIX && file.substr(0, testRootPrefixLength) === testRootPrefix;
                if (inWebRoot
                        && !inTests) {
                    config.preprocessors[file] = config.preprocessors[file] || [];
                    config.preprocessors[file].push('coverage');
                }
            }
        }
        config.coverageReporter = {
            type: 'clover',
            dir: COVERAGE_DIR + FILE_SEPARATOR,
            file: 'clover.xml'
        };
    }

};
