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
package org.netbeans.modules.java.hints.declarative.debugging;

import java.awt.Component;
import java.awt.Rectangle;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JEditorPane;
import javax.swing.JList;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
//import org.openide.util.ImageUtilities;
import org.netbeans.api.settings.ConvertAsProperties;
import org.netbeans.modules.java.hints.declarative.DeclarativeHintRegistry;
import org.netbeans.modules.java.hints.declarative.Utilities;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;

@ConvertAsProperties(dtd = "-//org.netbeans.modules.jackpot30.file.debugging//Debug//EN",
autostore = false)
@Messages("DN_Loading=Loading...")
public final class DebugTopComponent extends TopComponent {

    private static final RequestProcessor WORKER = new RequestProcessor(DebugTopComponent.class.getName(), 1, false, false);
    
    private static DebugTopComponent instance;
    /** path to the icon used by the component and its open action */
//    static final String ICON_PATH = "SET/PATH/TO/ICON/HERE";
    private static final String PREFERRED_ID = "DebugTopComponent";

    //@GuardedBy(AWT)
    private JEditorPane code;
    private boolean initialized;
    
    public DebugTopComponent() {
        initComponents();
        setName(NbBundle.getMessage(DebugTopComponent.class, "CTL_DebugTopComponent"));
        setToolTipText(NbBundle.getMessage(DebugTopComponent.class, "HINT_DebugTopComponent"));
//        setIcon(ImageUtilities.loadImage(ICON_PATH, true));
        
        WORKER.post(new Runnable() {
            @Override public void run() {
                final EditorKit kit = MimeLookup.getLookup("text/x-javahints").lookup(EditorKit.class);
                
                if (kit instanceof Callable) {
                    try {
                        ((Callable) kit).call();
                    } catch (Exception ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
                
                SwingUtilities.invokeLater(new Runnable() {
                    @Override public void run() {
                        code = new javax.swing.JEditorPane();
                        code.setContentType("text/x-javahints");
                        code.setEditorKit(kit);
                        Document doc = code.getEditorKit().createDefaultDocument();
                        doc.putProperty("mimeType", "text/x-javahints");
                        code.setDocument(doc);
                        editorHolder.setViewportView(code);
                        
                        initialized = true;
                        
                        synchronized (DebugTopComponent.this) {
                            DebugTopComponent.this.notifyAll();
                        }
                    }
                });
            }
        });
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        files = new javax.swing.JComboBox();
        editorHolder = new javax.swing.JScrollPane();

        files.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        files.setRenderer(new FileRenderer());
        files.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                filesActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(editorHolder)
                    .addComponent(files, 0, 376, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(files, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(editorHolder, javax.swing.GroupLayout.DEFAULT_SIZE, 246, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void filesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_filesActionPerformed
        synchronized (this) {
            Object selectedObj = files.getSelectedItem();
            
            if (!(selectedObj instanceof FileObject)) return ;
            
            FileObject selected = (FileObject) selectedObj;
            String spec = Utilities.readFile(selected);

            code.setText(spec);

            try {
                Rectangle r = code.modelToView(0);

                if (r != null) {
                    code.scrollRectToVisible(r);
                }
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
            
            hints = HintWrapper.parse(selected, spec);
        }
    }//GEN-LAST:event_filesActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane editorHolder;
    private javax.swing.JComboBox files;
    // End of variables declaration//GEN-END:variables

    private Collection<? extends HintWrapper> hints = Collections.emptyList();

    public synchronized @NonNull Collection<? extends HintWrapper> getHints() {
        return hints;
    }

    public synchronized Document getDocument() {
        //XXX: threading - called off AWT
        while (!initialized) {
            try {
                wait();
            } catch (InterruptedException ex) {
                Logger.getLogger(DebugTopComponent.class.getName()).log(Level.FINE, null, ex);
            }
        }
        return code.getDocument();
    }

    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link #findInstance}.
     */
    public static synchronized DebugTopComponent getDefault() {
        if (instance == null) {
            instance = new DebugTopComponent();
        }
        return instance;
    }

    public static synchronized DebugTopComponent getExistingInstance() {
        return instance;
    }
    
    /**
     * Obtain the DebugTopComponent instance. Never call {@link #getDefault} directly!
     */
    public static synchronized DebugTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            Logger.getLogger(DebugTopComponent.class.getName()).warning(
                    "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system.");
            return getDefault();
        }
        if (win instanceof DebugTopComponent) {
            return (DebugTopComponent) win;
        }
        Logger.getLogger(DebugTopComponent.class.getName()).warning(
                "There seem to be multiple components with the '" + PREFERRED_ID
                + "' ID. That is a potential source of errors and unexpected behavior.");
        return getDefault();
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }

    private long openCount = 0;
    
    @Override
    public void componentOpened() {
        DefaultComboBoxModel loadingModel = new DefaultComboBoxModel();
        
        loadingModel.addElement(Bundle.DN_Loading());
        
        files.setModel(loadingModel);
        files.setSelectedIndex(0);
        
        final long startingCount = ++openCount;
        
        WORKER.post(new Runnable() {
            @Override public void run() {
                final DefaultComboBoxModel dlm = new DefaultComboBoxModel();

                for (FileObject file : DeclarativeHintRegistry.findAllFiles()) {
                    dlm.addElement(file);
                }

                SwingUtilities.invokeLater(new Runnable() {
                    @Override public void run() {
                        if (startingCount != openCount) return ;
                        
                        files.setModel(dlm);
                        if (dlm.getSize()>0)
                            files.setSelectedIndex(0);
                    }
                });
            }
        });
    }

    @Override
    public synchronized void componentClosed() {
        files.setModel(new DefaultComboBoxModel());
        if (code != null) {
            code.setText("");
        }
        hints = Collections.emptyList();
        initialized = false;
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    Object readProperties(java.util.Properties p) {
        if (instance == null) {
            instance = this;
        }
        instance.readPropertiesImpl(p);
        return instance;
    }

    private void readPropertiesImpl(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }

    private static final class FileRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if (value instanceof FileObject) {
                value = FileUtil.getFileDisplayName((FileObject) value);
            }
            return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }
    }
}
