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

var projectConf = require('${projectConfig}');

module.exports = function(config) {
    projectConf(config);

    var fileSeparator = '${fileSeparator}';

    // base path
    if (config.basePath) {
        if (config.basePath.substr(0, 1) === '/' // unix
                || config.basePath.substr(1, 1) === ':') { // windows
            // absolute path, do nothing
        } else {
            config.basePath = '${projectWebRoot}' + fileSeparator + config.basePath;
        }
    } else {
        config.basePath = '${projectWebRoot}' + fileSeparator;
    }

    config.reporters = config.reporters || [];
    config.reporters = config.reporters.concat([
        //'progress',
        'netbeans'
    ]);
    <#if coverage>
    config.reporters = config.reporters.concat([
        'coverage'
    ]);
    </#if>
    config.reporters = config.reporters.filter(function (e, i, arr) {
        return arr.lastIndexOf(e) === i;
    });
    config.plugins = config.plugins || [];
    config.plugins = config.plugins.concat([
        'karma-chrome-launcher',
        '${karmaNetbeansReporter}'
    ]);
    <#if coverage>
    config.plugins = config.plugins.concat([
        'karma-coverage'
    ]);
    </#if>
    config.plugins = config.plugins.filter(function (e, i, arr) {
        return arr.lastIndexOf(e) === i;
    });
    config.browsers = ['Chrome'];
    config.colors = true;
    config.autoWatch = false;
    config.singleRun = false;

    <#if coverage>
    config.preprocessors = config.preprocessors || {};
    var projectWebRootLength = '${projectWebRoot}'.length() + fileSeparator.length();
    for (var i = 0; i < config.files.length; ++i) {
        var file = config.files[i];
        if (file.substr(0, projectWebRootLength) === '${projectWebRoot}' + fileSeparator) {
            config.preprocessors[file] = config.preprocessors[file] || [];
            config.preprocessors[file].push(['coverage']);
        }
    }
    // XXX
    config.coverageReporter = {
        type: 'cobertura',
        dir: 'coverage' + fileSeparator,
        file: 'cobertura.xml'
    }
    </#if>

};
