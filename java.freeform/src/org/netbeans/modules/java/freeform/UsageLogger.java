/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.freeform;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.apache.tools.ant.module.api.AntProjectCookie;
import org.apache.tools.ant.module.api.support.TargetLister;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.ant.freeform.spi.ProjectAccessor;
import org.netbeans.modules.ant.freeform.spi.ProjectConstants;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Element;

/**
 * #147458: logs information about a freeform project being opened.
 */
class UsageLogger {

    private static final Logger LOG = Logger.getLogger("org.netbeans.ui.metrics.freeform"); // NOI18N
    private static final RequestProcessor RP = new RequestProcessor(UsageLogger.class.getName(), 1, false, false);

    private UsageLogger() {}

    public static void log(final Project p) {
        if (LOG.isLoggable(Level.INFO)) {
            RP.post(new Runnable() {
                public void run() {
                    Object[] data;
                    try {
                        data = data(p);
                    } catch (Exception x) {
                        Exceptions.printStackTrace(x);
                        return;
                    }
                    LogRecord rec = new LogRecord(Level.INFO, "USG_FREEFORM_PROJECT"); // NOI18N
                    rec.setParameters(data);
                    rec.setLoggerName(LOG.getName());
                    rec.setResourceBundle(NbBundle.getBundle(UsageLogger.class));
                    rec.setResourceBundleName(UsageLogger.class.getPackage().getName() + ".Bundle"); // NOI18N
                    LOG.log(rec);
                }
            });
        }
    }

    private static Object[] data(Project p) throws Exception {
        ProjectAccessor accessor = p.getLookup().lookup(ProjectAccessor.class);
        if (accessor == null) {
            throw new IllegalArgumentException("no ProjectAccessor");
        }
        AntProjectHelper helper = accessor.getHelper();
        PropertyEvaluator eval = accessor.getEvaluator();
        AuxiliaryConfiguration aux = ProjectUtils.getAuxiliaryConfiguration(p);
        int compilationUnits = 0;
        int compilationUnitsMissingBuiltTo = 0;
        int compilationUnitsMultipleRoots = 0;
        Set<String> classpathEntries = new HashSet<String>();
        Element java = JavaProjectGenerator.getJavaCompilationUnits(aux);
        if (java != null) {
            for (Element compilationUnitEl : XMLUtil.findSubElements(java)) {
                compilationUnits++;
                int builtTos = 0;
                int roots = 0;
                for (Element other : XMLUtil.findSubElements(compilationUnitEl)) {
                    String name = other.getLocalName();
                    if (name.equals("package-root")) { // NOI18N
                        roots++;
                    } else if (name.equals("built-to")) { // NOI18N
                        builtTos++;
                    } else if (name.equals("classpath")) { // NOI18N
                        String text = XMLUtil.findText(other);
                        if (text != null) {
                            String textEval = eval.evaluate(text);
                            if (textEval != null) {
                                for (String entry : textEval.split("[:;]")) {
                                    if (entry.length() > 0) {
                                        classpathEntries.add(entry);
                                    }
                                }
                            }
                        }
                    }
                }
                if (builtTos == 0) {
                    compilationUnitsMissingBuiltTo++;
                }
                if (roots > 1) {
                    compilationUnitsMultipleRoots++;
                }
            }
        }
        int targets = 0;
        {
            String antScriptS = eval.getProperty(ProjectConstants.PROP_ANT_SCRIPT);
            if (antScriptS == null) {
                antScriptS = "build.xml"; // NOI18N
            }
            FileObject antScript = FileUtil.toFileObject(helper.resolveFile(antScriptS));
            if (antScript != null) {
                AntProjectCookie apc = DataObject.find(antScript).getLookup().lookup(AntProjectCookie.class);
                if (apc != null) {
                    try {
                        targets = TargetLister.getTargets(apc).size();
                    } catch (IOException ioe) {
                        //pass - Broken build.xml which may happen for freeform, targets = 0 and log usage
                    }
                }
            }
        }
        boolean webData = aux.getConfigurationFragment("web-data", "http://www.netbeans.org/ns/freeform-project-web/2", true) != null || // NOI18N
                aux.getConfigurationFragment("web-data", "http://www.netbeans.org/ns/freeform-project-web/1", true) != null; // NOI18N
        /* XXX takes about 1msec per source file to count them, even with a warm disk cache:
        int sourceFiles = 0;
        for (SourceGroup g : ProjectUtils.getSources(p).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA)) {
            for (FileObject kid : NbCollections.iterable(g.getRootFolder().getChildren(true))) {
                if (kid.hasExt("java")) { // NOI18N
                    sourceFiles++;
                }
            }
        }
         */
        // XXX other things which could be reported:
        // number of <properties>s (other than the original project location sometimes inserted by the New Project wizard) or <property-file>s defined
        // number of <view-item>s (other than those inserted by the GUI) defined
        // whether a custom Java platform is configured for the project
        // number of subprojects (i.e. classpath entries corresponding to project-owned sources)
        // number of context-sensitive actions defined
        // number of targets bound to non-context-sensitive actions
        return new Object[] { // Bundle.properties#USG_FREEFORM_PROJECT must match these fields
            someOrMany(compilationUnits),
            someOrMany(compilationUnitsMissingBuiltTo),
            someOrMany(compilationUnitsMultipleRoots),
            someOrMany(classpathEntries.size()),
            someOrMany(targets),
            webData,
        };
    }

    private static String someOrMany(int count) {
        if (count < 10) {
            return Integer.toString(count);
        } else {
            return "~e^" + Math.round(Math.log(count));
        }
    }

}
