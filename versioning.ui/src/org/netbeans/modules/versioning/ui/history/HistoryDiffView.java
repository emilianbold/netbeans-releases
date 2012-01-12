/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.versioning.ui.history;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.logging.Level;
import javax.swing.*;
import javax.swing.text.BadLocationException;

import org.netbeans.api.diff.*;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.versioning.util.NoContentPanel;
import org.netbeans.modules.versioning.util.Utils;
import org.netbeans.modules.versioning.util.VersioningEvent;
import org.netbeans.modules.versioning.util.VersioningListener;
import org.openide.cookies.EditorCookie;
import org.openide.explorer.ExplorerManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor.Task;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Tomas Stupka
 */
public class HistoryDiffView implements PropertyChangeListener, VersioningListener {
           
    private final HistoryTopComponent tc;
    private DiffPanel panel;
    private Component diffComponent;
    private DiffController diffView;                
    private DiffPrepareTask prepareDiff = null;
    private Task prepareDiffTask = null;
    private boolean selected;
    private PreparingDiffHandler preparingDiffPanel;
        
    /** Creates a new instance of LocalHistoryView */
    public HistoryDiffView(HistoryTopComponent tc) {
        this.tc = tc;
        panel = new DiffPanel();                                                              
//        History.getInstance().addVersioningListener(this); XXX
        showNoContent(NbBundle.getMessage(HistoryDiffView.class, "MSG_DiffPanel_NoVersion"));                
    }    
        
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if(ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {
            tc.disableNavigationButtons();
            selectionChanged(evt);            
        } else if (DiffController.PROP_DIFFERENCES.equals(evt.getPropertyName())) {
            tc.refreshNavigationButtons(diffView.getDifferenceIndex(), diffView.getDifferenceCount());
        }
    }
      
    @Override
    public void versioningEvent(VersioningEvent event) {
        // XXX
//        if(event.getId() != LocalHistory.EVENT_FILE_CREATED) {
//            return;
//        }
//        File file = (File) event.getParams()[0];
//        if( file == null || prepareDiff == null || !selected ||
//            !file.equals(prepareDiff.entry.getFile()))
//        {
//            return;
//        }        
//        scheduleTask(prepareDiff);
    }
    
    JPanel getPanel() {
        return panel;
    }    
    
    private void selectionChanged(PropertyChangeEvent evt) {
        Node[] newSelection = ((Node[]) evt.getNewValue());
        if ((newSelection != null) && (newSelection.length == 1)) {
            HistoryEntry se = newSelection[0].getLookup().lookup(HistoryEntry.class);
            if (se != null) {
                selected = true;
                refreshDiffPanel(se);
                return;
            }
        }

        selected = false;
        String msgKey = (newSelection == null) || (newSelection.length == 0)
                        ? "MSG_DiffPanel_NoVersion"                     //NOI18N
                        : "MSG_DiffPanel_IllegalSelection";             //NOI18N
        showNoContent(NbBundle.getMessage(HistoryDiffView.class, msgKey));
    }           
    
    private void refreshDiffPanel(HistoryEntry se) {  
        prepareDiff = new DiffPrepareTask(se);
        scheduleTask(prepareDiff);
    }        

    private void scheduleTask(Runnable runnable) {          
        if(prepareDiffTask != null) {
            prepareDiffTask.cancel();
            getPreparingDiffHandler().finish();
    }
        prepareDiffTask = History.getInstance().getRequestProcessor().create(runnable);
        prepareDiffTask.schedule(0);        
    }

    private PreparingDiffHandler getPreparingDiffHandler() {
        if(preparingDiffPanel == null) {
            preparingDiffPanel = new PreparingDiffHandler();
    }
        return preparingDiffPanel;
    }
    
    private class DiffPrepareTask implements Runnable {
        
        private final HistoryEntry entry;

        public DiffPrepareTask(final HistoryEntry se) {
            entry = se;
        }

        @Override
        public void run() {
            // XXX how to get the mimetype

            File tmpFile;
            final File file = entry.getFiles()[0]; // XXX
            getPreparingDiffHandler().start();
            try {
                final List<File> siblingTmpFiles = new LinkedList<File>();
                File tempFolder = Utils.getTempFolder();
                tmpFile = new File(tempFolder, file.getName()); // XXX
                entry.getRevisionFile(file, tmpFile);
//                List<HistoryEntry> siblings = entry.getSiblingEntries(); // XXX
//                for (HistoryEntry siblingEntry : siblings) {
//                    File tmpHistorySiblingFile = siblingEntry.getHistoryFile();
//                    siblingTmpFiles.add(tmpHistorySiblingFile);
//                }
            } finally {
                getPreparingDiffHandler().finish();
            }
            final File tmpHistoryFile = tmpFile;
            
            
            
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {            
                    try {   
                        
                        StreamSource ss1 = new LHStreamSource(tmpHistoryFile, file.getName() + " " + RevisionNode.getFormatedDate(entry), getMimeType(file));
                        
                        String title;
                        StreamSource ss2;                        
                        if(file.exists()) {
                            title = NbBundle.getMessage(HistoryDiffView.class, "LBL_Diff_CurrentFile");
                            ss2 = new LHStreamSource(file, title, getMimeType(file));
                        } else {
                            title = NbBundle.getMessage(HistoryDiffView.class, "LBL_Diff_FileDeleted");
                            ss2 = StreamSource.createSource("currentfile", title, getMimeType(file), new StringReader(""));
                        }
                        
                        diffView = DiffController.create(ss1, ss2);
                        diffView.addPropertyChangeListener(HistoryDiffView.this);
                        
                        JComponent c = diffView.getJComponent();
                        setDiffComponent(c);
                        tc.setDiffView(c);
                        if(diffView.getDifferenceCount() > 0) {
                            setCurrentDifference(0);
                        } else {
                            tc.refreshNavigationButtons(diffView.getDifferenceIndex(), diffView.getDifferenceCount());
                        }
                        panel.revalidate();
                        panel.repaint();
                        if("true".equals(System.getProperty("vcshistory.bindDiffRowToEditor", "false"))) {
                            setBaseLocation(title);
                        }

                    } catch (IOException ioe)  {
                        History.LOG.log(Level.SEVERE, null, ioe);
                    }                            
                }

                private void setBaseLocation(String title) throws DataObjectNotFoundException {
                    FileObject fo = FileUtil.toFileObject(file);
                    DataObject dao = fo != null ? DataObject.find(fo) : null;
                    EditorCookie cookie = dao != null ? dao.getLookup().lookup(EditorCookie.class) : null;
                    if(cookie != null) {
                        // hack - care only about dataObjects with opened editors.
                        // otherwise we won't assume it's file were opened to be edited
                        JEditorPane[] panes = cookie.getOpenedPanes();
                        if(panes != null && panes.length > 0) {
                            int p = panes[0].getCaretPosition();
                            if(p > 0) {
                                try {
                                    int row = Utilities.getLineOffset((BaseDocument)panes[0].getDocument(), p);
                                    if(row > 0) {
                                        diffView.setLocation(DiffController.DiffPane.Base, DiffController.LocationType.LineNumber, row);
                                    } 
                                } catch (BadLocationException ex) {
                                    History.getInstance().LOG.log(Level.WARNING, title, ex);
                                }
                            }
                        }
                    }
                }
            });
            
        }

        private String getMimeType(File file) {
            FileObject fo = FileUtils.toFileObject(file);
            if(fo != null) {
                return fo.getMIMEType();   
            } else {
                return "content/unknown"; // NOI18N
            }                
        }
        
    }        
    
    private void showNoContent(String s) {
        setDiffComponent(new NoContentPanel(s));
    }

    private void setDiffComponent(Component component) {
        //int dl = panel.splitPane.getDividerLocation();
        if(diffComponent != null) {
            panel.diffPanel.remove(diffComponent);     
        }       
        panel.diffPanel.add(component, BorderLayout.CENTER);
        diffComponent = component;   
        panel.diffPanel.revalidate();
        panel.diffPanel.repaint();
        //panel.splitPane.setDividerLocation(dl);                
    }       
    
    void onNextButton() {
        if(diffView == null) {
            return;
        }          
        int nextDiffernce = diffView.getDifferenceIndex() + 1;        
        if(nextDiffernce < diffView.getDifferenceCount()) {
            setCurrentDifference(nextDiffernce);    
        }                        
    }

    void onPrevButton() {
        if(diffView == null) {
            return;
        }
        int prevDiffernce = diffView.getDifferenceIndex() - 1;
        if(prevDiffernce > -1) {
            setCurrentDifference(prevDiffernce);                
        }            
    }    
    
    private void setCurrentDifference(int idx) {
        diffView.setLocation(DiffController.DiffPane.Modified, DiffController.LocationType.DifferenceIndex, idx);    
        tc.refreshNavigationButtons(diffView.getDifferenceIndex(), diffView.getDifferenceCount());
    }
    
    private class LHStreamSource extends StreamSource {
        
        private final File file;
        private final String title;
        private final String mimeType;

        public LHStreamSource(File file, String title, String mimeType) {
            this.file = file;
            this.title = title;
            this.mimeType = mimeType;
        }

        @Override
        public boolean isEditable() {
            FileObject fo = FileUtils.toFileObject(file);
            return isPrimary(fo);
        }
        
        private boolean isPrimary(FileObject fo) {            
            if (fo != null) {
                try {
                    DataObject dao = DataObject.find(fo);
                    return fo.equals(dao.getPrimaryFile());
                } catch (DataObjectNotFoundException e) {
                    // no dataobject, never mind
                }
            }
            return true;
        }
    
        @Override
        public Lookup getLookup() {
            FileObject fo = FileUtils.toFileObject(file);
            if (fo != null && isPrimary(fo)) {
                return Lookups.fixed(fo);                 
            } else {
                return Lookups.fixed(); 
            }
        }

        @Override
        public String getName() {
            return title;
        }

        @Override
        public String getTitle() {
            return title;
        }

        @Override
        public String getMIMEType() {
            return mimeType;
        }

        @Override
        public Reader createReader() throws IOException {
            return new FileReader(file);
        }

        @Override
        public Writer createWriter(Difference[] conflicts) throws IOException {
            return null;
        }
    }

    private class PreparingDiffHandler extends JPanel implements ActionListener {

        private JLabel label = new JLabel();
        private Component progressComponent;
        private ProgressHandle handle;
        private final Timer timer;
        public PreparingDiffHandler() {
            label.setText(NbBundle.getMessage(HistoryDiffView.class, "LBL_PreparingDiff"));
            this.setBackground(UIManager.getColor("TextArea.background")); // NOI18N

            setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            add(label, c);
            label.setEnabled(false);
            timer = new Timer(800, this);
}

        
        void start() {
            timer.start();
        }
        
        private synchronized void startProgress() throws MissingResourceException {
            handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(HistoryDiffView.class, "LBL_PreparingDiff"));
            setProgressComponent(ProgressHandleFactory.createProgressComponent(handle));
            handle.start();
            handle.switchToIndeterminate();                    
            setDiffComponent(PreparingDiffHandler.this);
        }
        
        synchronized void finish() {
            timer.stop();
            if(handle != null) {
                handle.finish();
                handle = null;
            }
        }
        
        void setProgressComponent(Component component) {
            if(progressComponent != null) remove(progressComponent);
            if(component != null) {
                this.progressComponent = component;
                GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
                add(component, gridBagConstraints);
            } 
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    startProgress();
                }
            });
        }        
    }
}
