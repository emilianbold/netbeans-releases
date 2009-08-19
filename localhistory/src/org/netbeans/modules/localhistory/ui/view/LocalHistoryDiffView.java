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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.localhistory.ui.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import javax.swing.*;

import org.netbeans.api.diff.*;
import org.netbeans.modules.localhistory.LocalHistory;
import org.netbeans.modules.localhistory.store.StoreEntry;
import org.netbeans.modules.localhistory.utils.FileUtils;
import org.netbeans.modules.versioning.util.NoContentPanel;
import org.netbeans.modules.versioning.util.Utils;
import org.netbeans.modules.versioning.util.VersioningEvent;
import org.netbeans.modules.versioning.util.VersioningListener;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.explorer.ExplorerManager;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Tomas Stupka
 */
public class LocalHistoryDiffView implements PropertyChangeListener, ActionListener, VersioningListener {
           
    private final LocalHistoryTopComponent master;
    private DiffPanel panel;
    private Component diffComponent;
    private DiffController diffView;                
    private DiffPrepareTask prepareTask = null;
    private boolean selected;
        
    /** Creates a new instance of LocalHistoryView */
    public LocalHistoryDiffView(LocalHistoryTopComponent master) {
        this.master = master;
        panel = new DiffPanel();                                                              
        panel.nextButton.addActionListener(this);
        panel.prevButton.addActionListener(this);
        LocalHistory.getInstance().addVersioningListener(this);
        showNoContent(NbBundle.getMessage(LocalHistoryDiffView.class, "MSG_DiffPanel_NoVersion"));                
    }    
        
    public void propertyChange(PropertyChangeEvent evt) {
        if(ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {
            disableNavigationButtons();
            selectionChanged(evt);            
        } else if (DiffController.PROP_DIFFERENCES.equals(evt.getPropertyName())) {
            refreshNavigationButtons();
        }
    }
      
    public void versioningEvent(VersioningEvent event) {
        if(event.getId() != LocalHistory.EVENT_FILE_CREATED) {
            return;
        }
        File file = (File) event.getParams()[0];
        if( file == null || prepareTask == null || !selected ||
            !file.equals(prepareTask.entry.getFile()))
        {
            return;
        }        
        scheduleTask(prepareTask);
    }
    
    JPanel getPanel() {
        return panel;
    }    
    
    private void selectionChanged(PropertyChangeEvent evt) {
        Node[] newSelection = ((Node[]) evt.getNewValue());
        selected = true;
        if(newSelection == null || newSelection.length == 0) {
            selected = false;
            showNoContent(NbBundle.getMessage(LocalHistoryDiffView.class, "MSG_DiffPanel_NoVersion"));
            return;
        }

        StoreEntry se = newSelection[0].getLookup().lookup(StoreEntry.class);
        if( se == null ) {
            selected = false;
            showNoContent(NbBundle.getMessage(LocalHistoryDiffView.class, "MSG_DiffPanel_IllegalSelection"));
            return;
        }
        refreshDiffPanel(se);                
    }           
    
    private void refreshDiffPanel(StoreEntry se) {  
        prepareTask = new DiffPrepareTask(se);
        scheduleTask(prepareTask);
    }        

    private static void scheduleTask(Runnable runnable) {          
        RequestProcessor.Task task = RequestProcessor.getDefault().create(runnable);        
        task.schedule(0);        
    }

    /**
     * Copies entry file's content to the given temporary file
     * @param entry contains the file's content
     * @param tmpHistoryFile target temporary file
     * @throws java.io.IOException
     */
    private static void extractHistoryFile (StoreEntry entry, File tmpHistoryFile) throws IOException {
        File file = entry.getFile();
        tmpHistoryFile.deleteOnExit();
        FileUtils.copy(entry.getStoreFileInputStream(), tmpHistoryFile);
        Utils.associateEncoding(file, tmpHistoryFile);
    }
    
    private class DiffPrepareTask implements Runnable {
        
        private final StoreEntry entry;

        public DiffPrepareTask(final StoreEntry se) {
            entry = se;
        }

        public void run() {
            // XXX how to get the mimetype

            final File file = entry.getFile();
            File tempFolder = Utils.getTempFolder();
            // we have to hold references to these files, otherwise associated encoding for them will be lost
            // Utils.associateEncoding holds only a weak reference
            final File tmpHistoryFile = new File(tempFolder, file.getName());
            final List<File> siblingTmpFiles = new LinkedList<File>();
            try {
                extractHistoryFile(entry, tmpHistoryFile);
                List<StoreEntry> siblings = entry.getSiblingEntries();
                for (StoreEntry siblingEntry : siblings) {
                    File tmpHistorySiblingFile = new File(tempFolder, siblingEntry.getFile().getName());
                    siblingTmpFiles.add(tmpHistorySiblingFile);
                    extractHistoryFile(siblingEntry, tmpHistorySiblingFile);
                }
            } catch (IOException ioe) {
                LocalHistory.LOG.log(Level.SEVERE, null, ioe);
                return;
            }

            SwingUtilities.invokeLater(new Runnable() {
                public void run() {            
                    try {   
                        
                        StreamSource ss1 = new LHStreamSource(tmpHistoryFile, entry.getFile().getName() + " " + StoreEntryNode.getFormatedDate(entry), entry.getMIMEType());
                        
                        String title;
                        StreamSource ss2;                        
                        if(file.exists()) {
                            title = NbBundle.getMessage(LocalHistoryDiffView.class, "LBL_Diff_CurrentFile");
                            ss2 = new LHStreamSource(file, title, entry.getMIMEType());
                        } else {
                            title = NbBundle.getMessage(LocalHistoryDiffView.class, "LBL_Diff_FileDeleted");
                            ss2 = StreamSource.createSource("currentfile", title, entry.getMIMEType(), new StringReader(""));
                        }
                        
                        diffView = DiffController.create(ss1, ss2);
                        diffView.addPropertyChangeListener(LocalHistoryDiffView.this);
                        
                        JComponent c = diffView.getJComponent();
                        setDiffComponent(c);
                        master.setDiffView(c);
                        if(diffView.getDifferenceCount() > 0) {
                            setCurrentDifference(0);
                        } else {
                            refreshNavigationButtons();
                        }
                        panel.revalidate();
                        panel.repaint();

                    } catch (IOException ioe)  {
                        LocalHistory.LOG.log(Level.SEVERE, null, ioe);
                    }                            
                }
            });
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
    
    public void actionPerformed(ActionEvent evt) {
        if(evt.getSource() == panel.nextButton) {
            onNextButton();
        } else if(evt.getSource() == panel.prevButton) {
            onPrevButton();
        }
    }

    private void onNextButton() {
        if(diffView == null) {
            return;
        }          
        int nextDiffernce = diffView.getDifferenceIndex() + 1;        
        if(nextDiffernce < diffView.getDifferenceCount()) {
            setCurrentDifference(nextDiffernce);    
        }                        
    }

    private void onPrevButton() {
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
        refreshNavigationButtons();
    }
    
    private void disableNavigationButtons() {
        panel.prevButton.setEnabled(false);
        panel.nextButton.setEnabled(false);
    }    

    private void refreshNavigationButtons() {
        int currentDifference = diffView.getDifferenceIndex();
        panel.prevButton.setEnabled(currentDifference > 0);
        panel.nextButton.setEnabled(currentDifference < diffView.getDifferenceCount() - 1);
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

        public boolean isEditable() {
            FileObject fo = FileUtil.toFileObject(file);
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
    
        public Lookup getLookup() {
            FileObject fo = FileUtil.toFileObject(file);
            if (fo != null && isPrimary(fo)) {
                return Lookups.fixed(fo);                 
            } else {
                return Lookups.fixed(); 
            }
        }

        public String getName() {
            return title;
        }

        public String getTitle() {
            return title;
        }

        public String getMIMEType() {
            return mimeType;
        }

        public Reader createReader() throws IOException {
            return new FileReader(file);
        }

        public Writer createWriter(Difference[] conflicts) throws IOException {
            return null;
        }
    }

}
