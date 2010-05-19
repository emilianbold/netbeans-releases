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

package org.netbeans.modules.dlight.core.stack.api.support;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.text.NumberFormat;
import org.netbeans.modules.dlight.api.storage.types.Time;
import org.netbeans.modules.dlight.core.stack.api.FunctionCallWithMetric;

/**
 *
 * @author mt154047
 */
public final class FunctionMetricFormatter {
    private static NumberFormat format = null;

    private static String formatValue(Object value) {
        // format with three decimals (including 0s)
        if (format == null) {
            format = NumberFormat.getNumberInstance();
            format.setGroupingUsed(false);
            format.setMinimumIntegerDigits(1);
            format.setMinimumFractionDigits(1);
            format.setMaximumFractionDigits(1);
        }
        return format.format(value);
    }

    public static final String getFormattedValue(FunctionCallWithMetric functionCall, String metricID){
        Object value = functionCall.getMetricValue(metricID);
        if (value instanceof Double || value instanceof Float) {
            return formatValue(value);
        }
        if (value instanceof Time) {
            return formatValue(((Time) value).getNanos() / 1e9);
        }

        PropertyEditor editor = value == null ? null : PropertyEditorManager.findEditor(value.getClass());
        if (editor != null){
            editor.setValue(value);
            return editor.getAsText();
        }      
        return value + "";
    }

}
