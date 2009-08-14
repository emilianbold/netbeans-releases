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
package org.netbeans.modules.dlight.visualizers.api.impl;

import java.util.List;
import org.netbeans.modules.dlight.api.storage.DataRow;
import org.netbeans.modules.dlight.visualizers.api.AdvancedTableViewVisualizerConfiguration;
import org.netbeans.modules.dlight.visualizers.api.DetailsRenderer;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.TableModel;

/**
 *
 * @author mt154047
 */
public abstract class AdvancedTableViewVisualizerConfigurationAccessor {

    private static volatile AdvancedTableViewVisualizerConfigurationAccessor DEFAULT;

    public static AdvancedTableViewVisualizerConfigurationAccessor getDefault() {
        AdvancedTableViewVisualizerConfigurationAccessor a = DEFAULT;
        if (a != null) {
            return a;
        }

        try {
            Class.forName(AdvancedTableViewVisualizerConfiguration.class.getName(), true,
                AdvancedTableViewVisualizerConfiguration.class.getClassLoader());//
        } catch (Exception e) {
        }
        return DEFAULT;
    }

    public static void setDefault(AdvancedTableViewVisualizerConfigurationAccessor accessor) {
        if (DEFAULT != null) {
            throw new IllegalStateException();
        }
        DEFAULT = accessor;
    }

    public AdvancedTableViewVisualizerConfigurationAccessor() {
    }

    public abstract NodeActionsProvider getNodeActionProvider(AdvancedTableViewVisualizerConfiguration configuration);

    public abstract String getNodeColumnName(AdvancedTableViewVisualizerConfiguration configuration);

    public abstract TableModel getTableModel(AdvancedTableViewVisualizerConfiguration configuration);

    public abstract String getEmptyRunningMessage(AdvancedTableViewVisualizerConfiguration configuration);

    public abstract String getEmptyAnalyzeMessage(AdvancedTableViewVisualizerConfiguration configuration);

    public abstract String getRowNodeColumnName(AdvancedTableViewVisualizerConfiguration configuration);

    public abstract List<String> getHiddenColumnNames(AdvancedTableViewVisualizerConfiguration configuration);

    public abstract boolean isDualPaneMode(AdvancedTableViewVisualizerConfiguration configuration);

    public abstract DetailsRenderer<DataRow> getDetailsRenderer(AdvancedTableViewVisualizerConfiguration configuration);
}
