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
package org.netbeans.modules.dlight.indicators.impl;

import java.awt.BorderLayout;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.netbeans.modules.dlight.api.storage.DataRow;
import org.netbeans.modules.dlight.indicators.ClockIndicatorConfiguration;
import org.netbeans.modules.dlight.spi.indicator.Indicator;

public class ClockIndicator extends Indicator<ClockIndicatorConfiguration> {

    private static final int SECOND_IN_MILLISECONDS = 1000;
    private ClockPanel panel;
    private long currentTime;

    public ClockIndicator(ClockIndicatorConfiguration configuration) {
        super(configuration);
        panel = new ClockPanel();
    }

    @Override
    public JComponent getComponent() {
        return panel;
    }

    public void updated(List<DataRow> data) {
        if (data.isEmpty()) {
            return;
        }

        DataRow lastRow = data.get(data.size() - 1);
        currentTime = lastRow.getLongValue(getMetadataColumnName(0));
        panel.update();
    }

    protected void tick() {
    }

    @Override
    protected void repairNeeded(boolean needed) {
    }

    public void reset() {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    private class ClockPanel extends JPanel {

        private JLabel timeLabel = new JLabel("00:00:00"); //NOI18N

        public ClockPanel() {
            setLayout(new BorderLayout(10, 10));
            add(timeLabel, BorderLayout.CENTER);
        }

        private void update() {
            int seconds = (int) currentTime / SECOND_IN_MILLISECONDS;
            int hours = seconds / (60 * 60);
            int minutes = (seconds - hours * 60 * 60) / 60;
            int real_seconds = (seconds - hours * 60 * 60 - minutes * 60);
            String timerStr = (hours < 10 ? "0" : "") + hours + //NOI18N
                    ":" + (minutes < 10 ? "0" : "") + minutes + //NOI18N
                    ":" + (real_seconds < 10 ? "0" : "") + real_seconds; //NOI18N
            timeLabel.setText(timerStr);
        }
    }
}
