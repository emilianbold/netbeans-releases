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
package org.netbeans.modules.dlight.tha;

import java.awt.Component;
import java.util.Collections;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.Renderer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * @author Alexey Vladykin
 */
public final class MasterSlaveView<T> extends JSplitPane implements ListSelectionListener {

    private final JList master;
    private Component slave;
    private Renderer slaveRenderer;

    public MasterSlaveView() {
        this(Collections.<T>emptyList(), null);
    }

    public MasterSlaveView(List<? extends T> data, Renderer slaveRenderer) {
        super(HORIZONTAL_SPLIT);
        this.master = new JList(data.toArray());
        this.master.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.master.addListSelectionListener(this);
        this.slaveRenderer = slaveRenderer;
        setResizeWeight(0.5);
        setLeftComponent(new JScrollPane(master));
        showDetails(master.getSelectedValue(), false);
    }

    public void setMasterData(List<? extends T> data) {
        master.setListData(data.toArray());
    }

    public void setMasterRenderer(ListCellRenderer renderer) {
        master.setCellRenderer(renderer);
    }

    public void setSlaveRenderer(Renderer renderer) {
        slaveRenderer = renderer;
        showDetails(master.getSelectedValue(), true);
    }

    public void valueChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            showDetails(master.getSelectedValue(), true);
        }
    }

    private void showDetails(Object masterItem, boolean keepDividerPos) {
        slave = null;
        if (slaveRenderer != null) {
            slaveRenderer.setValue(masterItem, true);
            slave = slaveRenderer.getComponent();
        }
        if (slave == null) {
            slave = new JLabel("<No details>"); // NOI18N
        }
        int oldDividerPos = keepDividerPos? getDividerLocation() : 0;
        setRightComponent(new JScrollPane(slave));
        if (keepDividerPos) {
            setDividerLocation(oldDividerPos);
        }
    }
}
