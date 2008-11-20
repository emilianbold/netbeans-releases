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
package org.netbeans.modules.ruby.railsprojects.server;

import java.awt.Component;
import java.util.List;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import org.netbeans.api.ruby.platform.RubyPlatform;
import org.netbeans.modules.ruby.railsprojects.server.spi.RubyInstance;

/**
 * A collection of UI related methods / classes
 * refactored out from {@link RailsServerManager}).
 *
 * @author Erno Mononen
 */
public final class RailsServerUiUtils {

    public static JComboBox getServerComboBox(RubyPlatform platform) {
        JComboBox result = new JComboBox();
        if (platform != null) {
            result.setModel(new ServerListModel(platform));
        }
        result.setRenderer(new ServerListCellRendered());
        return result;
    }

    public static class ServerListModel extends AbstractListModel implements ComboBoxModel {

        private final List<? extends RubyInstance> servers;
        private Object selected;

        public ServerListModel(RubyPlatform platform) {
            this.servers = ServerRegistry.getDefault().getServers(platform);
            if (!servers.isEmpty()) {
                this.selected = servers.get(0);
            }
        }

        public int getSize() {
            return servers.size();
        }

        public Object getElementAt(int index) {
            return servers.get(index);
        }

        public void setSelectedItem(Object server) {
            if (selected != server) {
                this.selected = server;
                fireContentsChanged(this, -1, -1);
            }
        }

        public Object getSelectedItem() {
            return selected;
        }
    }

    private static class ServerListCellRendered extends JLabel implements ListCellRenderer {

        public ServerListCellRendered() {
            setOpaque(true);
        }

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            RubyInstance server = (RubyInstance) value;
            if (server != null) {
                setText(server.getDisplayName());
                setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
                setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
            }
            return this;
        }
    }
}
