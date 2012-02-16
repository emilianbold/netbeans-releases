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
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EventObject;
import java.util.List;
import javax.swing.AbstractCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellEditor;
import org.netbeans.api.progress.ProgressUtils;
import org.netbeans.modules.db.dataview.util.FileBackedClob;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

public class ClobFieldTableCellEditor extends AbstractCellEditor
        implements TableCellEditor,
        ActionListener {
    
    private class CharsetSelector extends JPanel {
        private JComboBox charsetSelect;
        
        CharsetSelector() {
            List<Charset> charset = new ArrayList<Charset>(Charset.availableCharsets().values());
            Collections.sort(charset, new Comparator<Charset>() {
                @Override
                public int compare(Charset o1, Charset o2) {
                    return o1.displayName().compareTo(o2.displayName());
                }
            });
            charsetSelect = new JComboBox();
            charsetSelect.setModel(new DefaultComboBoxModel(charset.toArray()));
            charsetSelect.setSelectedItem(Charset.defaultCharset());
            this.add(charsetSelect);
        }
        
        public Charset getSelectedCharset() {
            return (Charset) charsetSelect.getSelectedItem();
        }
        
        public void setSelectedCharset(Charset selectedCharset) {
            charsetSelect.setSelectedItem(selectedCharset);
        }
    }
    protected static final String EDIT = "edit";
    protected Clob currentValue;
    protected JButton button;
    protected JPopupMenu popup;
    protected JTable table;
    protected int currentRow;
    protected int currentColumn;
    
    public ClobFieldTableCellEditor() {
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
        final JMenuItem miLobSaveAction = new JMenuItem(NbBundle.getMessage(ClobFieldTableCellEditor.class, "saveLob.title"));
        miLobSaveAction.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                saveLobToFile(currentValue);
                fireEditingCanceled();
            }
        });
        popup.add(miLobSaveAction);
        final JMenuItem miLobEditAction = new JMenuItem(NbBundle.getMessage(ClobFieldTableCellEditor.class, "editClob.title"));
        miLobEditAction.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                fireEditingStopped();
                editCell();
            }
        });
        popup.add(miLobEditAction);                
        final JMenuItem miLobLoadAction = new JMenuItem(NbBundle.getMessage(ClobFieldTableCellEditor.class, "loadLob.title"));
        miLobLoadAction.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                Object newValue = loadLobFromFile();
                if (newValue != null) {
                    currentValue = (Clob) newValue;
                }
                fireEditingStopped();
            }
        });
        popup.add(miLobLoadAction);
        final JMenuItem miLobNullAction = new JMenuItem(NbBundle.getMessage(ClobFieldTableCellEditor.class, "nullLob.title"));
        miLobNullAction.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                currentValue = null;
                fireEditingStopped();
            }
        });
        popup.add(miLobNullAction);
        
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (EDIT.equals(e.getActionCommand())) {
            popup.show(button, 0, button.getHeight());
        }
    }
    
    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        currentValue = (java.sql.Clob) value;
        if (currentValue != null) {
            try {
                long size = currentValue.length();
                StringBuilder stringValue = new StringBuilder();
                stringValue.append("<CLOB ");
                if (size < 1000) {
                    stringValue.append(String.format("%1$d Chars", size));
                } else if (size < 1000000) {
                    stringValue.append(String.format("%1$d kChars", size / 1000));
                } else {
                    stringValue.append(String.format("%1$d MChars", size / 1000000));
                }
                stringValue.append(">");
                button.setText(stringValue.toString());
            } catch (SQLException ex) {
                button.setText("<CLOB of unknown size>");
            }
        } else {
            button.setText("<NULL>");
        }
        this.currentColumn = column;
        this.currentRow = row;
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
    
    private void saveLobToFile(Clob b) {
        CharsetSelector charset = new CharsetSelector();
        JFileChooser c = new JFileChooser();
        c.setAccessory(charset);
        int fileDialogState = c.showSaveDialog(table);
        if (fileDialogState == JFileChooser.APPROVE_OPTION) {
            File f = c.getSelectedFile();
            Reader r = null;
            Writer w = null;
            try {
                r = b.getCharacterStream();
                w = new OutputStreamWriter(new FileOutputStream(f), charset.getSelectedCharset());
                if(! doTransfer(r, w, (int) b.length(), "Save to file: " + f.toString(), false)) {
                    f.delete();
                }
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
    
    private Clob loadLobFromFile() {
        CharsetSelector charset = new CharsetSelector();
        JFileChooser c = new JFileChooser();
        c.setAccessory(charset);
        Clob result = null;
        int fileDialogState = c.showOpenDialog(table);
        if (fileDialogState == JFileChooser.APPROVE_OPTION) {
            File f = c.getSelectedFile();
            Reader r = null;
            try {
                result = new FileBackedClob();
                r = new InputStreamReader(new FileInputStream(f), charset.getSelectedCharset());
                if(! doTransfer(r, result.setCharacterStream(1), (int) f.length() / 2, "Load from file: " + f.toString(), true)) {
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
    private boolean doTransfer(Reader in, Writer out, Integer size, String title, boolean sizeEstimated) throws IOException {
        // Only pass size if it is _not_ estimated
        MonitorableCharacterStreamTransfer ft = new MonitorableCharacterStreamTransfer(in, out, sizeEstimated ? null : size);
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
    
    protected void editCell() {
        String stringVal = "";
        if (currentValue != null) {
            try {
                stringVal = currentValue.getSubString(1, (int) currentValue.length());
            } catch (SQLException ex) {
            }
            
        }
        
        JTextArea textArea = new JTextArea(10, 50);
        textArea.setText(stringVal);
        textArea.setCaretPosition(0);
        textArea.setEditable(table.isCellEditable(currentRow, currentColumn));
        
        JScrollPane pane = new JScrollPane(textArea);
        Component parent = WindowManager.getDefault().getMainWindow();
        
        if (table.isCellEditable(currentRow, currentColumn)) {
            int result = JOptionPane.showOptionDialog(parent, pane, table.getColumnName(currentColumn), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null);
            if (result == JOptionPane.OK_OPTION) {
                try {
                    table.setValueAt(new FileBackedClob(textArea.getText()), currentRow, currentColumn);
                } catch (SQLException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        } else {
            JOptionPane.showMessageDialog(parent, pane, table.getColumnName(currentColumn), JOptionPane.PLAIN_MESSAGE, null);
        }
    }
}
