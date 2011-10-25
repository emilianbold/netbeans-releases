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
import org.netbeans.modules.dlight.core.stack.api.FunctionCall;
import org.netbeans.modules.dlight.core.stack.api.FunctionCallWithMetric;
import org.netbeans.modules.dlight.core.stack.api.FunctionMetric;
import org.netbeans.modules.dlight.libs.common.PathUtilities;
import org.netbeans.modules.dlight.spi.SourceFileInfoProvider.SourceFileInfo;

/**
 * This class holds metric values for Function calls.
 */
public class FunctionCallImpl extends FunctionCallWithMetric {

    /**
     * TODO: review & reimplement
     * 
     * Current implementation doesn't follow the original idea that
     * Function is a refference to a function (definition) (with it's source
     * file, line number in the file, function name) and FunctionCall is an
     * annotated code within the Function - it has metrics and offset within
     * the Function.
     *
     * Currently Function [FunctionImpl] is used as a FunctionCall and, actually,
     * every annotated line has it's OWN Function object (with OWN RefID).
     *
     * So in current implementation RefID of Function identifies FunctionCall
     * in an unique way [equals relies on this now!]
     *
     */
    private final Map<FunctionMetric, Object> metrics;
    private final long ref;
    private String displayedName = null;
    private SourceFileInfo sourceInfo = null;

    public FunctionCallImpl(
            final FunctionImpl function, long offset,
            final Map<FunctionMetric, Object> metrics) {
        super(function, offset);
        this.ref = function.getRef();
        this.metrics = Collections.unmodifiableMap(new HashMap<FunctionMetric, Object>(metrics));
        updateDisplayedName();
    }

    public synchronized void setSourceFileInfo(SourceFileInfo sourceInfo) {
        this.sourceInfo = sourceInfo;
        updateDisplayedName();
        this.setLineNumber(sourceInfo == null ? -1 : sourceInfo.getLine());
        ((FunctionImpl) this.getFunction()).setSourcefileName(sourceInfo == null ? "<unknown>" : sourceInfo.getFileName());//NOI18N
    }

    public long getFunctionRefID() {
        return ref;
    }

    public synchronized String getSourceFile() {
        return sourceInfo == null ? null : sourceInfo.getFileName();
    }

    @Override
    public synchronized String getDisplayedName() {
        return displayedName;
    }

    @Override
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
    public boolean equals(Object obj) {
        if (!(obj instanceof FunctionCallImpl)) {
            return false;
        }

        FunctionCallImpl that = (FunctionCallImpl) obj;

        if (this.ref != that.ref) {
            return false;
        }

        return this.metrics.equals(that.metrics);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + (this.metrics != null ? this.metrics.hashCode() : 0);
        hash = 29 * hash + (int) (this.ref ^ (this.ref >>> 32));
        return hash;
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

    private synchronized void updateDisplayedName() {
        StringBuilder sb = new StringBuilder(getFunction().getSignature());

        if (sourceInfo != null) {
            String file = sourceInfo.getFileName();
            int line = sourceInfo.getLine();

            sb.append("; "); // NOI18N
            sb.append(PathUtilities.getBaseName(file));

            if (line > 0) {
                sb.append(':').append(line);
            }
        }

        if (sb.length() == 0) {
            sb.append(super.getDisplayedName());
        }

        displayedName = sb.toString();
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
        return result;
    }

    private static FunctionCall parseFunctionCall(String line) {
        Matcher m = FUNCTION_PATTERN.matcher(line);
        if (!m.matches()) {
            return null;
        }

        int lineNumber = -1;

        if (m.group(3) != null) {
            try {
                lineNumber = Integer.parseInt(m.group(3));
            } catch (NumberFormatException ex) {
            }
        }

        final long funcRef = m.group(2) != null 
                ? (m.group(1) + m.group(2)).hashCode() 
                : (m.group(1) + lineNumber).hashCode();
        
        FunctionImpl func = new FunctionImpl(m.group(1), funcRef);

        FunctionCallImpl call = new FunctionCallImpl(func, lineNumber, new HashMap<FunctionMetric, Object>());

        if (m.group(4) != null) {
            call.setSourceFileInfo(new SourceFileInfo(m.group(4), lineNumber, 0));
            func.setSourcefileName(m.group(4));
        }

        return call;
    }
}
