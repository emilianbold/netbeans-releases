/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.spring.beans.ui.customizer;

import java.awt.Component;
import java.util.List;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListDataListener;
import org.netbeans.modules.spring.api.beans.ConfigFileGroup;
import org.openide.util.NbBundle;

/**
 *
 * @author Andrei Badea
 */
public class ConfigFileGroupUIs {

    private ConfigFileGroupUIs() {}

    public static void connect(List<ConfigFileGroup> groups, JList list) {
        list.setModel(new ConfigFileGroupListModel(groups));
        list.setCellRenderer(new ConfigFileGroupRenderer());
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    public static void connect(ConfigFileGroup group, JList list) {
        list.setModel(new ConfigFileListModel(group));
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    public static String getGroupName(ConfigFileGroup group) {
        String name = group.getName();
        if (name == null || name.length() == 0) {
            name = NbBundle.getMessage(ConfigFileGroupUIs.class, "LBL_Unnamed");
        }
        return name;
    }

    public static void disconnect(JList list) {
        list.setModel(new DefaultListModel());
    }

    private static final class ConfigFileGroupListModel implements ListModel {

        private final List<ConfigFileGroup> groups;

        public ConfigFileGroupListModel(List<ConfigFileGroup> groups) {
            this.groups = groups;
        }

        public void addListDataListener(ListDataListener l) {
        }

        public ConfigFileGroup getElementAt(int index) {
            return groups.get(index);
        }

        public int getSize() {
            return groups.size();
        }

        public void removeListDataListener(ListDataListener l) {
        }
    }

    private static final class ConfigFileGroupRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel component = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            ConfigFileGroup group = (ConfigFileGroup)value;
            component.setText(getGroupName(group));
            return component;
        }
    }

    private static final class ConfigFileListModel implements ListModel {

        private ConfigFileGroup group;

        public ConfigFileListModel(ConfigFileGroup group) {
            this.group = group;
        }

        public void addListDataListener(ListDataListener l) {
        }

        public Object getElementAt(int index) {
            return group.getFiles().get(index);
        }

        public int getSize() {
            return group.getFiles().size();
        }

        public void removeListDataListener(ListDataListener l) {
        }
    }
}
