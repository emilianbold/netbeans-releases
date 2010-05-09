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
package org.netbeans.modules.dlight.collector.stdout.impl;

import java.util.List;
import java.util.Map;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata;
import org.netbeans.modules.dlight.collector.stdout.CLIODCConfiguration;
import org.netbeans.modules.dlight.collector.stdout.CLIOParser;
import org.netbeans.modules.dlight.spi.storage.DataStorageType;

/**
 *
 * @author masha
 */
public abstract class CLIODCConfigurationAccessor {

    private static volatile CLIODCConfigurationAccessor DEFAULT;

    public static CLIODCConfigurationAccessor getDefault() {
        CLIODCConfigurationAccessor a = DEFAULT;

        if (a != null) {
            return a;
        }

        try {
            Class.forName(CLIODCConfiguration.class.getName(), true,
                    CLIODCConfiguration.class.getClassLoader());
        } catch (Exception e) {
        }

        return DEFAULT;
    }

    public static void setDefault(CLIODCConfigurationAccessor accessor) {
        if (DEFAULT != null) {
            throw new IllegalStateException();
        }

        DEFAULT = accessor;
    }

    public CLIODCConfigurationAccessor() {
    }

    public abstract String getName(CLIODCConfiguration configuration);
    
    public abstract String getCommand(CLIODCConfiguration configuration);

    public abstract String getArguments(CLIODCConfiguration configuration);

    public abstract List<DataTableMetadata> getDataTablesMetadata(CLIODCConfiguration configuration);

    public abstract CLIOParser getParser(CLIODCConfiguration configuration);

    public abstract String getCLIODCConfigurationID();

    public abstract Map<String, String> getDLightTargetExecutionEnv(CLIODCConfiguration configuration);

    public abstract boolean registerAsIndicatorDataProvider(CLIODCConfiguration configuration);

    public abstract DataStorageType getDataStorageType(CLIODCConfiguration configuration);
}
