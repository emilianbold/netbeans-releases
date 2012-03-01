/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.db.dataview.table.celleditor;

import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.EventObject;
import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellEditor;
import org.netbeans.api.progress.ProgressUtils;
import org.netbeans.modules.db.dataview.util.FileBackedBlob;
import org.openide.util.NbBundle;

public class BlobFieldTableCellEditor extends AbstractCellEditor
        implements TableCellEditor,
        ActionListener {

    protected static final String EDIT = "edit";
    protected Blob currentValue;
    protected JButton button;
    protected JPopupMenu popup;
    protected JTable table;

    public BlobFieldTableCellEditor() {
        button = new JButton();
        button.setActionCommand(EDIT);
        button.addActionListener(this);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setBorderPainted(false);
        button.setRolloverEnabled(false);
        button.setAlignmentX(0);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setFont(new Font(button.getFont().getFamily(), Font.ITALIC, 9));

        popup = new JPopupMenu();
        final JMenuItem miLobSaveAction = new JMenuItem(NbBundle.getMessage(BlobFieldTableCellEditor.class, "saveLob.title"));
        miLobSaveAction.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                saveLobToFile(currentValue);
                fireEditingCanceled();
            }
        });
        popup.add(miLobSaveAction);
        final JMenuItem miLobLoadAction = new JMenuItem(NbBundle.getMessage(BlobFieldTableCellEditor.class, "loadLob.title"));
        miLobLoadAction.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Object newValue = loadLobFromFile();
                if (newValue != null) {
                    currentValue = (Blob) newValue;
                }
                fireEditingStopped();
            }
        });
        popup.add(miLobLoadAction);
        final JMenuItem miLobNullAction = new JMenuItem(NbBundle.getMessage(BlobFieldTableCellEditor.class, "nullLob.title"));
        miLobNullAction.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                currentValue = null;
                fireEditingStopped();
            }
        });
        popup.add(miLobNullAction);

    }

    public void actionPerformed(ActionEvent e) {
        if (EDIT.equals(e.getActionCommand())) {
            popup.show(button, 0, button.getHeight());
        }
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        currentValue = (java.sql.Blob) value;
        if (currentValue != null) {
            try {
                long size = currentValue.length();
                StringBuilder stringValue = new StringBuilder();
                stringValue.append("<BLOB ");
                if (size < 1000) {
                    stringValue.append(String.format("%1$d bytes", size));
                } else if (size < 1000000) {
                    stringValue.append(String.format("%1$d kB", size / 1000));
                } else {
                    stringValue.append(String.format("%1$d MB", size / 1000000));
                }
                stringValue.append(">");
                button.setText(stringValue.toString());
            } catch (SQLException ex) {
                button.setText("<BLOB of unknown size>");
            }
        } else {
            button.setText("<NULL>");
        }
        this.table = table;
        return button;
    }

    @Override
    public Object getCellEditorValue() {
        return currentValue;
    }

    @Override
    public boolean isCellEditable(EventObject anEvent) {
        if (anEvent instanceof MouseEvent) {
            return ((MouseEvent) anEvent).getClickCount() >= 2;
        }
        return super.isCellEditable(anEvent);
    }

    private void saveLobToFile(Blob b) {
        JFileChooser c = new JFileChooser();
        int fileDialogState = c.showSaveDialog(table);
        if (fileDialogState == JFileChooser.APPROVE_OPTION) {
            File f = c.getSelectedFile();
            InputStream is = null;
            FileOutputStream fos = null;
            try {
                is = b.getBinaryStream();
                fos = new FileOutputStream(f);
                if(! doTransfer(is, fos, (int) b.length(), "Saving to file: " + f.toString())) {
                    f.delete();
                }
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private Blob loadLobFromFile() {
        JFileChooser c = new JFileChooser();
        Blob result = null;
        int fileDialogState = c.showOpenDialog(table);
        if (fileDialogState == JFileChooser.APPROVE_OPTION) {
            File f = c.getSelectedFile();
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(f);
                result = new FileBackedBlob();
                if(! doTransfer(fis, result.setBinaryStream(1), (int) f.length(), "Loading file: " + f.toString())) {
                    result = null;
                }
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }
        return result;
    }

    /**
     * @return true if transfer is complete and not iterrupted 
     */
    private boolean doTransfer(InputStream is, OutputStream os, Integer size, String title) throws IOException {
        MonitorableStreamTransfer ft = new MonitorableStreamTransfer(is, os, size);
        Throwable t = null;
        // Only show dialog, if the filesize is large enougth and has a use for the user
        if (size == null || size > (1024 * 1024)) {
            t = ProgressUtils.showProgressDialogAndRun(ft, title, false);
        } else {
            t = ft.run(null);
        }
        if (t != null && t instanceof RuntimeException) {
            throw (RuntimeException) t;
        } else if (t != null && t instanceof IOException) {
            throw (IOException) t;
        } else if (t != null) {
            throw new RuntimeException(t);
        }
        return !ft.isCancel();
    }
}