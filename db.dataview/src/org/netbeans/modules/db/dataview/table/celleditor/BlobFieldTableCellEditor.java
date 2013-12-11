/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011-2012 Oracle and/or its affiliates. All rights reserved.
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
import java.io.*;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.EventObject;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.*;
import javax.swing.table.TableCellEditor;
import org.netbeans.api.progress.ProgressUtils;
import org.netbeans.modules.db.dataview.util.FileBackedBlob;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;

public class BlobFieldTableCellEditor extends AbstractCellEditor
        implements TableCellEditor,
        ActionListener, AlwaysEnable {
    private static final Logger LOG = Logger.getLogger(
            BlobFieldTableCellEditor.class.getName());
    private static final String EDIT = "edit";

    private static File lastFile;

    private Blob currentValue;
    private JButton button;
    private JPopupMenu popup;
    private JTable table;
    private JMenuItem saveContentMenuItem;
    private JMenuItem miOpenImageMenuItem;
    private JMenuItem miLobLoadAction;
    private JMenuItem miLobNullAction;

    @SuppressWarnings("LeakingThisInConstructor")
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
        saveContentMenuItem = new JMenuItem(NbBundle.getMessage(BlobFieldTableCellEditor.class, "saveLob.title"));
        saveContentMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                saveLobToFile(currentValue);
                fireEditingCanceled();
            }
        });
        popup.add(saveContentMenuItem);

        miOpenImageMenuItem = new JMenuItem("Open as Image");
        miOpenImageMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                openAsImage(currentValue);
                fireEditingCanceled();
            }
        });
        popup.add(miOpenImageMenuItem);

        miLobLoadAction = new JMenuItem(NbBundle.getMessage(BlobFieldTableCellEditor.class, "loadLob.title"));
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
        miLobNullAction = new JMenuItem(NbBundle.getMessage(BlobFieldTableCellEditor.class, "nullLob.title"));
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
        currentValue = (java.sql.Blob) value;
        int modelRow = table.convertRowIndexToModel(row);
        int modelColumn = table.convertColumnIndexToModel(column);
        boolean editable = table.getModel().isCellEditable(modelRow, modelColumn);
        if (currentValue != null) {
            saveContentMenuItem.setEnabled(true);
            miOpenImageMenuItem.setEnabled(true);
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
            saveContentMenuItem.setEnabled(false);
            miOpenImageMenuItem.setEnabled(false);
            button.setText("<NULL>");
        }
        miLobLoadAction.setEnabled(editable);
        miLobNullAction.setEnabled(editable);
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
        if(b == null) {
            return;
        }
        JFileChooser c = new JFileChooser();
        c.setCurrentDirectory(lastFile);
        int fileDialogState = c.showSaveDialog(table);
        if (fileDialogState == JFileChooser.APPROVE_OPTION) {
            File f = c.getSelectedFile();
            lastFile = f;
            InputStream is;
            FileOutputStream fos;
            try {
                is = b.getBinaryStream();
                fos = new FileOutputStream(f);
                if(! doTransfer(is, fos, (int) b.length(), "Saving to file: " + f.toString())) {
                    f.delete();
                }
            } catch (IOException ex) {
                LOG.log(Level.INFO, "IOError while saving BLOB to file", ex);
                displayError(f, ex, false);
            } catch (SQLException ex) {
                LOG.log(Level.INFO, "SQLException while saving BLOB to file", ex);
                displayError(f, ex, false);
            }
        }
    }

    private Blob loadLobFromFile() {
        JFileChooser c = new JFileChooser();
        c.setCurrentDirectory(lastFile);
        Blob result = null;
        int fileDialogState = c.showOpenDialog(table);
        if (fileDialogState == JFileChooser.APPROVE_OPTION) {
            File f = c.getSelectedFile();
            lastFile = f;
            FileInputStream fis;
            try {
                fis = new FileInputStream(f);
                result = new FileBackedBlob();
                if(! doTransfer(fis, result.setBinaryStream(1), (int) f.length(), "Loading file: " + f.toString())) {
                    result = null;
                }
            } catch (IOException ex) {
                LOG.log(Level.INFO, "IOError while loading BLOB from file", ex);
                displayError(f, ex, true);
                result = null;
            } catch (SQLException ex) {
                LOG.log(Level.INFO, "SQLException while loading BLOB from file", ex);
                displayError(f, ex, true);
                result = null;
            }
        }
        return result;
    }

    /**
     * Note: The streams will be closed after this method was invoked
     * 
     * @return true if transfer is complete and not interrupted 
     */
    private boolean doTransfer(InputStream is, OutputStream os, Integer size, String title) throws IOException {
        MonitorableStreamTransfer ft = new MonitorableStreamTransfer(is, os, size);
        Throwable t;
        // Only show dialog, if the filesize is large enougth and has a use for the user
        if (size == null || size > (1024 * 1024)) {
            t = ProgressUtils.showProgressDialogAndRun(ft, title, false);
        } else {
            t = ft.run(null);
        }
        is.close();
        os.close();
        if (t != null && t instanceof RuntimeException) {
            throw (RuntimeException) t;
        } else if (t != null && t instanceof IOException) {
            throw (IOException) t;
        } else if (t != null) {
            throw new RuntimeException(t);
        }
        return !ft.isCancel();
    }

    private void displayError(File f, Exception ex, boolean read) {
        DialogDisplayer dd = DialogDisplayer.getDefault();

        String errorObjectMsg;
        String messageMsg;
        String titleMsg;

        if (ex instanceof SQLException) {
            errorObjectMsg = NbBundle.getMessage(BlobFieldTableCellEditor.class,
                    "lobErrorObject.database");
        } else {
            errorObjectMsg = NbBundle.getMessage(BlobFieldTableCellEditor.class,
                    "lobErrorObject.file");
        }

        if (!read) {
            titleMsg = NbBundle.getMessage(BlobFieldTableCellEditor.class,
                    "blobSaveToFileError.title");
            messageMsg = NbBundle.getMessage(BlobFieldTableCellEditor.class,
                    "blobSaveToFileError.message",
                    errorObjectMsg,
                    f.getAbsolutePath(),
                    ex.getLocalizedMessage());
        } else {
            titleMsg = NbBundle.getMessage(BlobFieldTableCellEditor.class,
                    "blobReadFromFileError.title");
            messageMsg = NbBundle.getMessage(BlobFieldTableCellEditor.class,
                    "blobReadFromFileError.message",
                    errorObjectMsg,
                    f.getAbsolutePath(),
                    ex.getLocalizedMessage());
        }

        NotifyDescriptor nd = new NotifyDescriptor(
                messageMsg,
                titleMsg,
                NotifyDescriptor.OK_CANCEL_OPTION,
                NotifyDescriptor.WARNING_MESSAGE,
                new Object[]{NotifyDescriptor.CANCEL_OPTION},
                NotifyDescriptor.CANCEL_OPTION);

        dd.notifyLater(nd);
    }

    private void openAsImage(Blob b) {
        if (b == null) {
            return;
        }
        try {
            ImageInputStream iis = ImageIO.createImageInputStream(
                    b.getBinaryStream());
            Iterator<ImageReader> irs = ImageIO.getImageReaders(iis);
            if (irs.hasNext()) {
                FileSystem fs = FileUtil.createMemoryFileSystem();
                FileObject fob = fs.getRoot().createData(
                        Long.toString(System.currentTimeMillis()),
                        irs.next().getFormatName());
                OutputStream os = fob.getOutputStream();
                os.write(b.getBytes(1, (int) b.length()));
                os.close();
                DataObject data = DataObject.find(fob);
                OpenCookie cookie = data.getLookup().lookup(OpenCookie.class);
                if (cookie != null) {
                    cookie.open();
                    return;
                }
            }
            displayErrorOpenImage("openImageErrorNotImage.message");    //NOI18N
        } catch (SQLException ex) {
            LOG.log(Level.INFO,
                    "SQLException while opening BLOB as file", ex);     //NOI18N
            displayErrorOpenImage("openImageErrorDB.message");          //NOI18N
        } catch (IOException ex) {
            LOG.log(Level.INFO, "IOError while opening BLOB as file", //NOI18N
                    ex);
        }

    }

    private void displayErrorOpenImage(String messageProperty) {
        DialogDisplayer dd = DialogDisplayer.getDefault();

        String messageMsg = NbBundle.getMessage(BlobFieldTableCellEditor.class,
                messageProperty);
        String titleMsg = NbBundle.getMessage(BlobFieldTableCellEditor.class,
                "openImageError.title");                                //NOI18N

        NotifyDescriptor nd = new NotifyDescriptor(
                messageMsg,
                titleMsg,
                NotifyDescriptor.OK_CANCEL_OPTION,
                NotifyDescriptor.WARNING_MESSAGE,
                new Object[]{NotifyDescriptor.CANCEL_OPTION},
                NotifyDescriptor.CANCEL_OPTION);

        dd.notifyLater(nd);
    }
}
