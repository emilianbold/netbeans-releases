/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.j2ee.clientproject;

import java.io.File;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.JComponent;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;

/**
 * Utility methods for the module.
 */
public class Utils {

    private static final Logger UI_LOGGER = Logger.getLogger("org.netbeans.ui.j2ee.clientproject"); // NOI18N
    private static final Logger USG_LOGGER = Logger.getLogger("org.netbeans.ui.metrics.j2ee.clientproject"); // NOI18N
    
    private static final String WIZARD_PANEL_CONTENT_DATA = WizardDescriptor.PROP_CONTENT_DATA; // NOI18N
    private static final String WIZARD_PANEL_CONTENT_SELECTED_INDEX = WizardDescriptor.PROP_CONTENT_SELECTED_INDEX; //NOI18N;

    private Utils() {
    }
    
    public static String toClasspathString(File[] classpathEntries) {
        if (classpathEntries == null) {
            return "";
        }
        StringBuffer classpath = new StringBuffer();
        for (int i = 0; i < classpathEntries.length; i++) {
            classpath.append(classpathEntries[i].getAbsolutePath());
            if (i + 1 < classpathEntries.length) {
                classpath.append(':');
            }
        }
        return classpath.toString();
    }

    private static void setSteps(WizardDescriptor.Panel[] panels, String[] steps, String[] resultSteps, int offset) {
        int n = steps == null ? 0 : steps.length;
        for (int i = 0; i < panels.length; i++) {
            final JComponent component = (JComponent) panels[i].getComponent();
            String step = i < n ? steps[i] : null;
            if (step == null) {
                step = component.getName();
            }
            component.putClientProperty(WIZARD_PANEL_CONTENT_DATA, resultSteps);
            component.putClientProperty(WIZARD_PANEL_CONTENT_SELECTED_INDEX, i);
            component.getAccessibleContext().setAccessibleDescription(step);
            resultSteps[i + offset] = step;
        }
    }
    
    public static void setSteps(WizardDescriptor.Panel[] panels, String[] steps) {
        setSteps(panels, steps, steps, 0);
    }

    /**
     * Logs the UI gesture.
     *
     * @param bundle resource bundle to use for message
     * @param message message key
     * @param params message parameters, may be <code>null</code>
     */
    public static void logUI(ResourceBundle bundle,String message, Object[] params) {
        Parameters.notNull("message", message);
        Parameters.notNull("bundle", bundle);

        LogRecord logRecord = new LogRecord(Level.INFO, message);
        logRecord.setLoggerName(UI_LOGGER.getName());
        logRecord.setResourceBundle(bundle);
        if (params != null) {
            logRecord.setParameters(params);
        }
        UI_LOGGER.log(logRecord);
    }

    /**
     * Logs feature usage.
     *
     * @param srcClass source class
     * @param message message key
     * @param params message parameters, may be <code>null</code>
     */
    public static void logUsage(Class srcClass, String message, Object[] params) {
        Parameters.notNull("message", message);

        LogRecord logRecord = new LogRecord(Level.INFO, message);
        logRecord.setLoggerName(USG_LOGGER.getName());
        logRecord.setResourceBundle(NbBundle.getBundle(srcClass));
        logRecord.setResourceBundleName(srcClass.getPackage().getName() + ".Bundle"); // NOI18N
        if (params != null) {
            logRecord.setParameters(params);
        }
        USG_LOGGER.log(logRecord);
    }
}
