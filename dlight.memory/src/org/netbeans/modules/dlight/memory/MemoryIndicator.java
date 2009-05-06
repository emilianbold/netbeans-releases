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
package org.netbeans.modules.dlight.memory;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import org.netbeans.modules.dlight.api.storage.DataRow;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata.Column;
import org.netbeans.modules.dlight.indicators.graph.GraphConfig;
import org.netbeans.modules.dlight.indicators.graph.RepairPanel;
import org.netbeans.modules.dlight.spi.indicator.Indicator;
import org.netbeans.modules.dlight.util.DLightExecutorService;
import org.netbeans.modules.dlight.util.UIThread;
import org.netbeans.modules.dlight.util.UIUtilities;
import org.openide.util.NbBundle;

/**
 * Memory usage indicator
 * @author Vladimir Kvashin
 */
public class MemoryIndicator extends Indicator<MemoryIndicatorConfiguration> {

    private  MemoryIndicatorPanel panel;
    private final Set<String> acceptedColumnNames;
    private long lastValue;

    public MemoryIndicator(MemoryIndicatorConfiguration configuration) {
        super(configuration);
        this.panel = new MemoryIndicatorPanel();
        this.acceptedColumnNames = new HashSet<String>();
        for (Column column : getMetadataColumns()) {
            acceptedColumnNames.add(column.getColumnName());
        }
    }

    @Override
    public JComponent getComponent() {
        return panel.getPanel();
    }

    public void reset() {
    }

    public void updated(List<DataRow> rows) {
        for (DataRow row : rows) {
            for (String column : row.getColumnNames()) {
                if (acceptedColumnNames.contains(column)) {
                    String value = row.getStringValue(column); //TODO: change to Long
                    lastValue = Long.parseLong(value);
                }
            }
        }
    }

    @Override
    protected void tick() {
        panel.addData(lastValue);
    }

    @Override
    protected void repairNeeded(boolean needed) {
        if (needed) {
            final RepairPanel repairPanel = new RepairPanel(getRepairActionProvider().getValidationStatus(), new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    final Future<Boolean> result = getRepairActionProvider().asyncRepair();
                    DLightExecutorService.submit(new Callable<Boolean>() {
                        public Boolean call() throws Exception {
                            UIThread.invoke(new Runnable() {
                                public void run() {
                                    panel.getPanel().setOverlay(null);
                                }
                            });
                            return result.get();
                        }
                    }, "Click On Repair in Memory Indicator task");//NOI18N
                }
            });
            UIThread.invoke(new Runnable() {
                public void run() {
                    panel.getPanel().setEnabled(false);
                    panel.getPanel().setOverlay(repairPanel);
                }
            });
        } else {
            final JEditorPane label = UIUtilities.createJEditorPane(getRepairActionProvider().getMessage(getRepairActionProvider().getValidationStatus()), true, GraphConfig.TEXT_COLOR);
            UIThread.invoke(new Runnable() {
                public void run() {
                    panel.getPanel().setOverlay(label);
                }
            });
        }
    }

    private static String getMessage(String name) {
        return NbBundle.getMessage(MemoryIndicator.class, name);
    }
}
