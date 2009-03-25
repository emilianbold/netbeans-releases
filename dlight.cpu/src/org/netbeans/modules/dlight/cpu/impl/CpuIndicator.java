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

package org.netbeans.modules.dlight.cpu.impl;

import org.netbeans.modules.dlight.util.DLightLogger;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import javax.swing.*;
import org.netbeans.modules.dlight.api.storage.DataRow;
import org.netbeans.modules.dlight.spi.indicator.Indicator;

/**
 *
 * @author Vladimir Kvashin
 */
class CpuIndicator extends Indicator<CpuIndicatorConfiguration> {

    private CpuIndicatorPanel panel;
    private Collection<ActionListener> listeners;

    CpuIndicator(CpuIndicatorConfiguration configuration) {
        super(configuration);
        panel = new CpuIndicatorPanel();
    }

    @Override
    public JComponent getComponent() {
        return panel.getPanel();
    }

    public void reset() {
        //graph.reset();
    }

    @Override
    public void updated(List<DataRow> data) {
        DataRow lastRow = null;
        for (DataRow row : data) {
            if (DLightLogger.instance.isLoggable(Level.FINE)) { DLightLogger.instance.fine("UPDATE: " + row.getData().get(0) + " " + row.getData().get(1)); }
            Float usr = (Float) row.getData("utime"); // NOI18N
            Float sys = (Float) row.getData("stime"); // NOI18N
            if (usr != null && sys != null) {
                panel.addData(sys.intValue(), usr.intValue());
                lastRow = row;
            }
        }
        if (lastRow != null) {
            Float usr = (Float) lastRow.getData("utime"); // NOI18N
            Float sys = (Float) lastRow.getData("stime"); // NOI18N
            panel.setSysValue(sys.intValue());
            panel.setUsrValue(usr.intValue());
        }
    }

    /*package*/ void fireActionPerformed() {
        ActionEvent ae = new ActionEvent(this, 0, null);
        for (ActionListener al : getActionListeners()) {
            al.actionPerformed(ae);
        }
    }

    Collection<ActionListener> getActionListeners() {
        synchronized (this) {
            return (listeners == null) ? Collections.<ActionListener>emptyList() : new ArrayList<ActionListener>(listeners);
        }
    }

    void addActionListener(ActionListener listener) {
        synchronized (this) {
            if (listeners == null) {
                listeners = new ArrayList<ActionListener>();
            }
            listeners.add(listener);
        }
    }

    void removeActionListener(ActionListener listener) {
        synchronized (this) {
            if (listeners != null) {
                listeners.remove(listener);
            }
        }
    }
}
