/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.annotationsupport;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata.Column;
import org.netbeans.modules.dlight.core.stack.api.FunctionCallWithMetric;
import org.netbeans.modules.dlight.core.stack.dataprovider.SourceFileInfoDataProvider;
import org.netbeans.modules.dlight.core.stack.spi.AnnotatedSourceSupport;
import org.netbeans.modules.dlight.spi.SourceFileInfoProvider.SourceFileInfo;

/**
 *
 * @author thp
 */
@org.openide.util.lookup.ServiceProvider(service = org.netbeans.modules.dlight.core.stack.spi.AnnotatedSourceSupport.class)
public class AnnotatedSourceSupportImpl implements AnnotatedSourceSupport {

    private final static Logger log = Logger.getLogger("dlight.annotationsupport"); // NOI18N
    private static boolean checkedLogging = checkLogging();
    private static boolean logginIsOn;

    public void updateSource(SourceFileInfoDataProvider sourceFileInfoProvider, List<Column> metrics, List<FunctionCallWithMetric> functionCalls) {
        log(sourceFileInfoProvider, metrics, functionCalls);
    }

    private void log(SourceFileInfoDataProvider sourceFileInfoProvider, List<Column> metrics, List<FunctionCallWithMetric> functionCalls) {
        if (!logginIsOn) {
            return;
        }
        log.fine("AnnotatedSourceSupportImpl.updateSource");
        log.finest("metrics:");
        for (Column column : metrics) {
            log.finest("  getColumnLongUName " + column.getColumnLongUName());
            log.finest("  getColumnName " + column.getColumnName());
            log.finest("  getColumnUName " + column.getColumnUName());
            log.finest("  getExpression = " + column.getExpression());
            log.finest("");
        }
        log.finest("functionCalls:");
        for (FunctionCallWithMetric functionCall : functionCalls) {
            log.finest("  getDisplayedName " + functionCall.getDisplayedName());
            log.finest("  getDisplayedName " + functionCall.getFunction());
            log.finest("  getDisplayedName " + functionCall.getFunction().getName());
            log.finest("  getDisplayedName " + functionCall.getFunction().getQuilifiedName());
            StringBuilder sb = new StringBuilder(functionCall.getFunction().toString());
            sb.append(" ["); // NOI18N
            for (Column column : metrics) {
                String metricId = column.getColumnName();
                Object metricVal = functionCall.getMetricValue(metricId);
                String metricUName = column.getColumnUName();
                sb.append(metricUName + "=" + metricVal + " "); // NOI18N
            }

            SourceFileInfo sourceFileInfo = sourceFileInfoProvider.getSourceFileInfo(functionCall);
            if (sourceFileInfo != null) {
                if (sourceFileInfo.isSourceKnown()) {
                    sb.append(sourceFileInfo.getFileName() + ":"); // NOI18N
                }
                sb.append(sourceFileInfo.getLine());
            }
            sb.append("]"); // NOI18N
            log.finest("  " + sb.toString());
            log.finest("  " + functionCall);
        }
    }

    private static boolean checkLogging() {
        if (checkedLogging) {
            return true;
        }
        logginIsOn = false;
        String logProp = System.getProperty("dlight.annotationsupport"); // NOI18N
        if (logProp != null) {
            logginIsOn = true;
            if (logProp.equals("FINE")) { // NOI18N
                log.setLevel(Level.FINE);
            } else if (logProp.equals("FINER")) { // NOI18N
                log.setLevel(Level.FINER);
            } else if (logProp.equals("FINEST")) { // NOI18N
                log.setLevel(Level.FINEST);
            }
        }
        return true;
    }
}
