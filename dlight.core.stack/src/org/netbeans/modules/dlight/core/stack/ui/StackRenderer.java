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
package org.netbeans.modules.dlight.core.stack.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.JComponent;
import org.netbeans.modules.dlight.api.storage.DataRow;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata.Column;
import org.netbeans.modules.dlight.api.storage.DataUtil;
import org.netbeans.modules.dlight.api.support.DataModelSchemeProvider;
import org.netbeans.modules.dlight.core.stack.api.FunctionCall;
import org.netbeans.modules.dlight.core.stack.dataprovider.StackDataProvider;
import org.netbeans.modules.dlight.management.api.DLightManager;
import org.netbeans.modules.dlight.management.api.DLightSession;
import org.netbeans.modules.dlight.util.ui.Renderer;

/**
 *
 * @author Alexey Vladykin
 */
public final class StackRenderer implements Renderer<DataRow> {

    private final List<Column> stackColumns;

    public StackRenderer(List<Column> stackColumns) {
        this.stackColumns = Collections.unmodifiableList(
                new ArrayList<Column>(stackColumns)); // yes, it's paranoia :)
    }

    public JComponent render(DataRow data) {
        MultipleCallStackPanel resultPanel = null;
        StackDataProvider stackProvider = findStackDataProvider();
        if (stackProvider != null) {
            for (Column column : stackColumns) {
                int stackId = DataUtil.toInt(data.getData(column.getColumnName()));
                if (0 < stackId) {
                    List<FunctionCall> stack = stackProvider.getCallStack(stackId);
                    if (stack != null) {
                        if (resultPanel == null) {
                            resultPanel = MultipleCallStackPanel.createInstance(stackProvider);
                        }
                        resultPanel.add(column.getColumnUName(), true, stack);
                    }
                }
            }
        }
        return resultPanel;
    }

    private boolean stackDataProviderSearched;
    private StackDataProvider stackDataProvider;

    private synchronized StackDataProvider findStackDataProvider() {
        if (stackDataProvider == null && !stackDataProviderSearched) {
            DLightSession session = DLightManager.getDefault().getActiveSession();
            stackDataProvider = (StackDataProvider) session.createDataProvider(
                    DataModelSchemeProvider.getInstance().getScheme("model:stack"), null); // NOI18N
            stackDataProviderSearched = true;
        }
        return stackDataProvider;
    }
}
