/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.makeproject.configurations;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.cnd.api.compilers.CompilerSet;
import org.netbeans.modules.cnd.api.compilers.Tool;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.makeproject.api.configurations.ItemConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.utils.MIMENames;

/**
 * Writes qmake project (*.pro file) for given configuration.
 *
 * @author Alexey Vladykin
 */
public class QmakeProjectWriter {

    /*
     * Project file name is constructed as prefix + confName + suffix.
     */
    private static final String PROJECT_PREFIX = "Qt-"; // NOI18N
    private static final String PROJECT_SUFFIX = ".pro"; // NOI18N

    /**
     * Qmake variables.
     */
    private static enum Variable {
        TEMPLATE,
        TARGET,
        CONFIG,
        QT,
        SOURCES,
        HEADERS,
        FORMS,
        RESOURCES,
        TRANSLATIONS,
        DEFINES,
        INCLUDEPATH,
        LIBS,
        QMAKE_CC,
        QMAKE_CXX,
        OBJECTS_DIR
    }

    /**
     * Qmake variable operations.
     */
    private static enum Operation {
        ASSIGN("="), // NOI18N
        APPEND("+="); // NOI18N

        private final String op;

        private Operation(String op) {
            this.op = op;
        }

        public String getOp() {
            return op;
        }
    }

    /**
     * 
     */
    private static final String PREPEND = ""; // NOI18N

    /**
     * Project descriptor.
     */
    private final MakeConfigurationDescriptor projectDescriptor;

    /**
     * Configuration that needs a qmake project.
     */
    private final MakeConfiguration configuration;

    /**
     * Constructs new instance.
     *
     * @param projectDescriptor  project descriptor
     * @param configuration  configuration that needs a qmake project
     */
    public QmakeProjectWriter(MakeConfigurationDescriptor projectDescriptor, MakeConfiguration configuration) {
        this.projectDescriptor = projectDescriptor;
        this.configuration = configuration;
    }

    /**
     * Writes qmake project for configuration.
     *
     * @throws java.io.IOException  if an error occurs when writing the file
     */
    public void write() throws IOException {
        if (configuration.isQmakeConfiguration()) {
            File qmakeProject = new File(configuration.getBaseDir(),
                    PROJECT_PREFIX + configuration.getName() + PROJECT_SUFFIX);
            BufferedWriter bw = null;
            try {
                bw = new BufferedWriter(new FileWriter(qmakeProject));
                write(bw);
            } finally {
                if (bw != null) {
                    bw.close();
                }
            }
        }
    }

    private void write(BufferedWriter bw) throws IOException {
        write(bw, Variable.TEMPLATE, Operation.ASSIGN, getTemplate());
        write(bw, Variable.TARGET, Operation.ASSIGN,
                configuration.expandMacros(configuration.getLinkerConfiguration().getOutputValue()));
        write(bw, Variable.CONFIG, Operation.APPEND,
                configuration.getQmakeConfiguration().getConfig().getValue());
        write(bw, Variable.QT, Operation.APPEND,
                configuration.getQmakeConfiguration().getModules().getValue());

        Item[] items = projectDescriptor.getProjectItems();
        write(bw, Variable.SOURCES, Operation.APPEND, getItems(items, MIMENames.C_MIME_TYPE, MIMENames.CPLUSPLUS_MIME_TYPE));
        write(bw, Variable.HEADERS, Operation.APPEND, getItems(items, MIMENames.HEADER_MIME_TYPE));
        write(bw, Variable.FORMS, Operation.APPEND, getItems(items, MIMENames.QT_UI_MIME_TYPE));
        write(bw, Variable.RESOURCES, Operation.APPEND, getItems(items, MIMENames.QT_RESOURCE_MIME_TYPE));
        write(bw, Variable.TRANSLATIONS, Operation.APPEND, getItems(items, MIMENames.QT_TRANSLATION_MIME_TYPE));

        write(bw, Variable.OBJECTS_DIR, Operation.ASSIGN,
                configuration.expandMacros(ConfigurationMakefileWriter.getObjectDir(configuration)));

        write(bw, Variable.QMAKE_CC, Operation.ASSIGN,
                ConfigurationMakefileWriter.getCompilerName(configuration, Tool.CCompiler));
        write(bw, Variable.QMAKE_CXX, Operation.ASSIGN,
                ConfigurationMakefileWriter.getCompilerName(configuration, Tool.CCCompiler));

        CompilerSet cs = configuration.getCompilerSet().getCompilerSet();
        write(bw, Variable.DEFINES, Operation.APPEND,
                configuration.getCCompilerConfiguration().getPreprocessorConfiguration().getOption(cs, PREPEND) +
                configuration.getCCCompilerConfiguration().getPreprocessorConfiguration().getOption(cs, PREPEND));
        write(bw, Variable.INCLUDEPATH, Operation.APPEND,
                configuration.getCCompilerConfiguration().getIncludeDirectories().getOption(cs, PREPEND) +
                configuration.getCCCompilerConfiguration().getIncludeDirectories().getOption(cs, PREPEND));
        write(bw, Variable.LIBS, Operation.APPEND,
                configuration.getLinkerConfiguration().getLibrariesConfiguration().getOption(cs, PREPEND));
    }

    private void write(BufferedWriter bw, Variable var, Operation op, String value) throws IOException {
        bw.write(var.toString());
        bw.write(' '); // NOI18N
        bw.write(op.getOp());
        bw.write(' '); // NOI18N
        bw.write(value);
        bw.write('\n'); // NOI18N
    }

    private void write(BufferedWriter bw, Variable var, Operation op, List<String> values) throws IOException {
        bw.write(var.toString());
        bw.write(' '); // NOI18N
        bw.write(op.getOp());
        for (String value : values) {
            bw.write(' '); // NOI18N
            bw.write(value);
        }
        bw.write('\n'); // NOI18N
    }

    private List<String> getItems(Item[] items, String... mimeTypes) {
        List<String> list = new ArrayList<String>();
        for (Item item : items) {
            ItemConfiguration itemConf = item.getItemConfiguration(configuration);
            if (itemConf.getExcluded().getValue()) {
                continue;
            }
            String actualMimeType = item.getFileObject().getMIMEType();
            for (String mimeType : mimeTypes) {
                if (mimeType.equals(actualMimeType)) {
                    list.add(item.getPath());
                    break;
                }
            }
        }
        return list;
    }

    private String getTemplate() {
        switch (configuration.getConfigurationType().getValue()) {
            case MakeConfiguration.TYPE_QT_APPLICATION:
                return "app"; // NOI18N
            case MakeConfiguration.TYPE_QT_LIBRARY:
                return "lib"; // NOI18N
            default:
                return ""; // NOI18N
        }
    }

}
