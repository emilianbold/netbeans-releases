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
package org.netbeans.modules.dlight.sync;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import org.netbeans.modules.dlight.api.storage.DataRow;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata.Column;
import org.netbeans.modules.dlight.spi.indicator.Indicator;
import org.netbeans.modules.dlight.util.DLightExecutorService;
import org.netbeans.modules.dlight.util.UIThread;

/**
 * Thread usage indicator
 * @author Vladimir Kvashin
 */
public class SyncIndicator extends Indicator<SyncIndicatorConfiguration> {

    private final JButton b = new JButton("Repair...");//NOI18N
    private JLabel label;
    private SyncIndicatorPanel panel;
    private final Set<String> acceptedColumnNames;
    private int lastLocks;
    private int lastThreads;

    public SyncIndicator(SyncIndicatorConfiguration configuration) {
        super(configuration);
        this.acceptedColumnNames = new HashSet<String>();
        for (Column column : getMetadataColumns()) {
            acceptedColumnNames.add(column.getColumnName());
        }
    }

    @Override
    public synchronized JComponent getComponent() {
        if (panel == null) {
            panel = new SyncIndicatorPanel();
        }
        return panel.getPanel();
    }

    public void reset() {
    }

    public void updated(List<DataRow> rows) {
        for (DataRow row : rows) {
            String locks = row.getStringValue("locks"); // NOI18N
            String threads = row.getStringValue("threads"); // NOI18N
            if (locks != null && threads != null) {
                lastLocks = (int) Float.parseFloat(locks);
                lastThreads = Integer.parseInt(threads);
            }
        }
    }

    @Override
    protected void tick() {
        panel.addData(lastLocks, lastThreads);
    }

    @Override
    protected void repairNeeded(boolean needed) {
        if (needed) {
            b.setAlignmentX(JComponent.CENTER_ALIGNMENT);
            b.setAlignmentY(JComponent.CENTER_ALIGNMENT);
            label =  new JLabel(getRepairActionProvider().getReason());
            label.setAlignmentX(JComponent.CENTER_ALIGNMENT);
            label.setAlignmentY(JComponent.CENTER_ALIGNMENT);
            b.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    final Future<Boolean> result = getRepairActionProvider().asyncRepair();
                    DLightExecutorService.submit(new Callable<Boolean>() {

                        public Boolean call() throws Exception {
                            Boolean status = result.get();
                            UIThread.invoke(new Runnable() {

                                public void run() {
                                    panel.getPanel().remove(b);
                                    panel.getPanel().remove(label);
                                }
                            });
                            return status.booleanValue();
                        }
                    }, "Click On Repair in Sync Indicator task");//NOI18N
                }
            });
            panel.getPanel().add(b);
            panel.getPanel().add(label);
        } else {
            //Remove here Button REpair
            panel.getPanel().remove(b);
            panel.getPanel().remove(label);
            panel.getPanel().add(new JLabel(getRepairActionProvider().isValid() ? "Will show data on the next run" : "Invalid"));//NOI18N
        }
    }
}
