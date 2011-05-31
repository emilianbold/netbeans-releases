/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.core.stack.storage.impl;

import java.util.Map;
import org.netbeans.modules.dlight.core.stack.api.Function;
import org.netbeans.modules.dlight.core.stack.api.FunctionCallWithMetric;
import org.netbeans.modules.dlight.core.stack.api.FunctionMetric;
import org.netbeans.modules.dlight.core.stack.utils.FunctionNameUtils;
import org.netbeans.modules.dlight.spi.SourceFileInfoProvider.SourceFileInfo;

/* package */ class FunctionCallImpl extends FunctionCallWithMetric {

    private final Map<FunctionMetric, Object> metrics;
    private final int lineNumber;

    FunctionCallImpl(Function function, long offset, Map<FunctionMetric, Object> metrics) {
        super(function, offset);
        this.metrics = metrics;
        SourceFileInfo sourceFileInfo = null;
        try {
            sourceFileInfo = FunctionNameUtils.getSourceFileInfo(function.getSignature());
        } catch (Throwable e) {
            System.err.println(e);
        }
        lineNumber = sourceFileInfo == null ? -1 : sourceFileInfo.getLine();
        setLineNumber(lineNumber);

    }

    FunctionCallImpl(Function function, Map<FunctionMetric, Object> metrics) {
        this(function, 0, metrics);
    }

    @Override
    public String getDisplayedName() {
        if (hasLineNumber()) {
            return FunctionNameUtils.getFunctionName(getFunction().getSignature());
            //+ "  " + getFunction().getSourceFile() + ":" + getLineNumber();//NOI18N
        }
        return getFunction().getName() + (hasOffset() ? ("+0x" + Long.toHexString(getOffset())) : ""); //NOI18N
    }

    @Override
    public Object getMetricValue(FunctionMetric metric) {
        return metrics.get(metric);
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
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("FunctionCall{ function=").append(getFunction()); //NOI18N
        buf.append(", metrics=").append(metrics).append(" }"); //NOI18N
        return buf.toString();
    }

    @Override
    public int getLineNumber() {

        return lineNumber;
    }
}
