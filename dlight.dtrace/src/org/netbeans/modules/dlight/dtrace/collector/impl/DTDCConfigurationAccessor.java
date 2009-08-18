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
package org.netbeans.modules.dlight.dtrace.collector.impl;

import java.util.List;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata;
import org.netbeans.modules.dlight.dtrace.collector.DTDCConfiguration;
import org.netbeans.modules.dlight.dtrace.collector.support.DtraceParser;

/**
 *
 * @author masha
 */
public abstract class DTDCConfigurationAccessor {

    private static volatile DTDCConfigurationAccessor DEFAULT;

    public static DTDCConfigurationAccessor getDefault() {
        DTDCConfigurationAccessor a = DEFAULT;
        if (a != null) {
            return a;
        }

        try {
            Class.forName(DTDCConfiguration.class.getName(), true,
                    DTDCConfiguration.class.getClassLoader());
        } catch (Exception e) {
        }
        return DEFAULT;
    }

    public static void setDefault(DTDCConfigurationAccessor accessor) {
        if (DEFAULT != null) {
            throw new IllegalStateException();
        }
        DEFAULT = accessor;
    }

    public DTDCConfigurationAccessor() {
    }

    public abstract String getArgs(DTDCConfiguration conf);

    public abstract List<DataTableMetadata> getDatatableMetadata(
            DTDCConfiguration conf);

    public abstract DtraceParser getParser(DTDCConfiguration conf);

    public abstract List<String> getRequiredPrivileges(DTDCConfiguration conf);

    public abstract String getScriptPath(DTDCConfiguration conf);

    public abstract String getID();

    public abstract boolean isStackSupportEnabled(DTDCConfiguration conf);

    public abstract int getIndicatorFiringFactor(DTDCConfiguration conf);

    public abstract boolean isStandalone(DTDCConfiguration conf);

    public abstract String getOutputPrefix(DTDCConfiguration conf);
}
