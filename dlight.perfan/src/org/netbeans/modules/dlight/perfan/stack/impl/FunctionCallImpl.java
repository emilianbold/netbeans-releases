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
package org.netbeans.modules.dlight.perfan.stack.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.modules.dlight.core.stack.api.Function;
import org.netbeans.modules.dlight.core.stack.api.FunctionCall;
import org.netbeans.modules.dlight.core.stack.api.FunctionCallWithMetric;
import org.netbeans.modules.dlight.core.stack.api.FunctionMetric;

/**
 * This class holds metric values for Function calls.
 */
public class FunctionCallImpl extends FunctionCallWithMetric {

    private String fileName;
    private String sourceFile;
    private final Map<FunctionMetric, Object> metrics;
    private final StringBuilder displayedName = new StringBuilder();

    public FunctionCallImpl(
            final Function function,
            final Map<FunctionMetric, Object> metrics) {
        super(function);
        this.metrics = metrics;
    }

    public FunctionCallImpl(
            final Function function, long offset,
            final Map<FunctionMetric, Object> metrics) {
        super(function, offset);
        this.metrics = metrics;
        updateDisplayedName();
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
        updateDisplayedName();
    }

    public String getFileName() {
        return fileName;
    }

    public void setSourceFile(String sourceFile) {
        this.sourceFile = sourceFile;
        updateDisplayedName();
    }

    public boolean hasSourceFileDefined() {
        return sourceFile != null;
    }

    public String getSourceFile() {
        return sourceFile;
    }

    @Override
    public String getDisplayedName() {
        synchronized (displayedName) {
            return displayedName.toString();
        }
    }

    public Object getMetricValue(FunctionMetric metric) {
        return metrics.get(metric);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getFunction().toString());
        sb.append(" [ "); // NOI18N

        for (FunctionMetric m : metrics.keySet()) {
            sb.append(m.getMetricDisplayedName()).append(" == "); // NOI18N
            sb.append(metrics.get(m)).append("; "); // NOI18N
        }

        sb.append("]"); // NOI18N

        return sb.toString();
    }

    @Override
    public boolean hasMetric(String metric_id) {
        for (FunctionMetric metric : metrics.keySet()) {
            if (metric.getMetricID().equals(metric_id)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Object getMetricValue(String metric_id) {
        for (FunctionMetric metric : metrics.keySet()) {
            if (metric.getMetricID().equals(metric_id)) {
                return metrics.get(metric);
            }
        }
        return null;
    }

    private void updateDisplayedName() {
        synchronized (displayedName) {
            displayedName.setLength(0);

            Function f = getFunction();

            if (f != null) {
                displayedName.append(f.getName());
            } else {
                displayedName.append("<unknown>"); // NOI18N
            }

            if (fileName != null) {
                displayedName.append(", " + fileName); // NOI18N
                if (hasOffset()) {
                    displayedName.append(":").append(getOffset()); // NOI18N
                }
            }

            if (displayedName.length() == 0) {
                displayedName.append(super.getDisplayedName());
            }
        }
    }

    private static final Pattern FUNCTION_PATTERN = Pattern.compile("\\s+(.+?)(?:\\s+\\+\\s+0x([0-9a-fA-F]+))?(?:,\\s+line\\s+(\\d+)\\s+in\\s+\"(.+)\")?"); // NOI18N

    public static List<FunctionCall> parseStack(ListIterator<String> it) {
        List<FunctionCall> result = new ArrayList<FunctionCall>();
        while (it.hasNext()) {
            String line = it.next().replace("Stack:", "      "); // NOI18N
            if (8 <= line.length() - line.trim().length()) {
                // stack lines start with whitespace
                result.add(parseFunctionCall(line));
            } else {
                it.previous();
                break;
            }
        }
        Collections.reverse(result);
        return result;
    }

    private static FunctionCall parseFunctionCall(String line) {
        Matcher m = FUNCTION_PATTERN.matcher(line);
        if (m.matches()) {
            FunctionImpl func = new FunctionImpl(m.group(1), m.group(1).hashCode());

            long lineNumber = -1;
            if (m.group(3) != null) {
                try {
                    lineNumber = Long.parseLong(m.group(3));
                } catch (NumberFormatException ex) {
                }
            }
            FunctionCallImpl call = new FunctionCallImpl(func, lineNumber, new HashMap<FunctionMetric, Object>());
            if (m.group(4) != null) {
                call.setFileName(m.group(4));
            }

            return call;
        } else {
            return null;
        }
    }
}
