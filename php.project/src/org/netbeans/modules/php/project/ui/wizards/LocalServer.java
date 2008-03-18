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

package org.netbeans.modules.php.project.ui.wizards;

import java.awt.Component;
import java.awt.event.ActionListener;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.UIResource;

/**
 * @author Tomas Mysik
 */
public class LocalServer implements Comparable<LocalServer> {

    private final String virtualHost;
    private final String documentRoot;
    private final boolean editable;
    private String srcRoot;

    public LocalServer(final LocalServer localServer) {
        this(localServer.virtualHost, localServer.documentRoot, localServer.srcRoot, localServer.editable);
    }

    public LocalServer(String srcRoot) {
        this(null, null, srcRoot);
    }

    public LocalServer(String documentRoot, String srcRoot) {
        this(null, documentRoot, srcRoot);
    }

    public LocalServer(String virtualHost, String documentRoot, String srcRoot) {
        this(virtualHost, documentRoot, srcRoot, true);
    }

    public LocalServer(String virtualHost, String documentRoot, String srcRoot, boolean editable) {
        this.virtualHost = virtualHost;
        this.documentRoot = documentRoot;
        this.srcRoot = srcRoot;
        this.editable = editable;
    }

    public String getVirtualHost() {
        return virtualHost;
    }

    public String getDocumentRoot() {
        return documentRoot;
    }

    public String getSrcRoot() {
        return srcRoot;
    }

    public void setSrcRoot(String srcRoot) {
        if (!editable) {
            throw new IllegalStateException("srcRoot cannot be changed because instance is not editable");
        }
        this.srcRoot = srcRoot;
    }

    public boolean isEditable() {
        return editable;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getName());
        sb.append("[virtualHost: "); // NOI18N
        sb.append(virtualHost);
        sb.append(", documentRoot: "); // NOI18N
        sb.append(documentRoot);
        sb.append(", srcRoot: "); // NOI18N
        sb.append(srcRoot);
        sb.append(", editable: "); // NOI18N
        sb.append(editable);
        sb.append("]"); // NOI18N
        return sb.toString();
    }

    public int compareTo(LocalServer ls) {
        if (!editable) {
            return -1;
        }
        return srcRoot.compareTo(ls.getSrcRoot());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final LocalServer other = (LocalServer) obj;
        if (virtualHost != other.virtualHost && (virtualHost == null || !virtualHost.equals(other.virtualHost))) {
            return false;
        }
        if (documentRoot != other.documentRoot && (documentRoot == null || !documentRoot.equals(other.documentRoot))) {
            return false;
        }
        if (editable != other.editable) {
            return false;
        }
        if (srcRoot != other.srcRoot && (srcRoot == null || !srcRoot.equals(other.srcRoot))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (virtualHost != null ? virtualHost.hashCode() : 0);
        hash = 97 * hash + (documentRoot != null ? documentRoot.hashCode() : 0);
        hash = 97 * hash + (editable ? 1 : 0);
        hash = 97 * hash + (srcRoot != null ? srcRoot.hashCode() : 0);
        return hash;
    }

    public static class ComboBoxEditor implements javax.swing.ComboBoxEditor, UIResource, DocumentListener {

        private static final long serialVersionUID = -4527321803090719483L;
        private final JTextField component = new JTextField();
        private final ChangeListener changeListener;
        private final ChangeEvent changeEvent = new ChangeEvent(this);
        private LocalServer activeItem;

        public ComboBoxEditor(ChangeListener changeListener) {
            super();
            this.changeListener = changeListener;
            component.setOpaque(true);
            component.getDocument().addDocumentListener(this);
        }

        public Component getEditorComponent() {
            return component;
        }

        public void setItem(Object anObject) {
            if (anObject == null) {
                return;
            }
            assert anObject instanceof LocalServer;
            activeItem = (LocalServer) anObject;
            component.setText(activeItem.getSrcRoot());
        }

        public Object getItem() {
            return new LocalServer(activeItem);
        }

        public void selectAll() {
            component.selectAll();
            component.requestFocus();
        }

        public void addActionListener(ActionListener l) {
            component.addActionListener(l);
        }

        public void removeActionListener(ActionListener l) {
            component.removeActionListener(l);
        }

        public void insertUpdate(DocumentEvent e) {
            processUpdate();
        }

        public void removeUpdate(DocumentEvent e) {
            processUpdate();
        }

        public void changedUpdate(DocumentEvent e) {
            processUpdate();
        }

        private void processUpdate() {
            boolean enabled = false;
            if (activeItem.isEditable()) {
                enabled = true;
                activeItem.setSrcRoot(component.getText().trim());
            }
            component.setEnabled(enabled);
            changeListener.stateChanged(changeEvent);
        }
    }

    public static class ComboBoxRenderer extends JLabel implements ListCellRenderer, UIResource {

        private static final long serialVersionUID = 31965318763243602L;

        public ComboBoxRenderer() {
            super();
            setOpaque(true);
        }

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                boolean cellHasFocus) {
            assert value instanceof LocalServer;
            setName("ComboBox.listRenderer"); // NOI18N
            setText(((LocalServer) value).getSrcRoot());

            // never selected
            setBackground(list.getBackground());
            setForeground(list.getForeground());
            return this;
        }
    }

    public static class ComboBoxModel extends DefaultComboBoxModel {
        private static final long serialVersionUID = 193082264935872743L;

        public ComboBoxModel(LocalServer... defaultLocalServers) {
            for (LocalServer localServer : defaultLocalServers) {
                addElement(localServer);
            }
        }
    }
}
