/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.localhistory.ui.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.nio.charset.Charset;
import javax.swing.*;

import org.netbeans.api.diff.*;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.localhistory.store.StoreEntry;
import org.netbeans.modules.versioning.util.NoContentPanel;
import org.openide.ErrorManager;
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
public class LocalHistoryDiffView implements PropertyChangeListener, ActionListener {
           
    private final LocalHistoryTopComponent master;
    private DiffPanel panel;
    private Component diffComponent;
    private DiffController diffView;            
    
    /** Creates a new instance of LocalHistoryView */
    public LocalHistoryDiffView(LocalHistoryTopComponent master) {
        this.master = master;
        panel = new DiffPanel();                                                              
        panel.nextButton.addActionListener(this);
        panel.prevButton.addActionListener(this);
        showNoContent(NbBundle.getMessage(LocalHistoryDiffView.class, "MSG_DiffPanel_NoVersion"));                
    }    
        
    public void propertyChange(PropertyChangeEvent evt) {
        if(ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {
            selectionChanged(evt);
        } else if (DiffController.PROP_DIFFERENCES.equals(evt.getPropertyName())) {
            refreshNavigationButtons();
        }
    }
      
    JPanel getPanel() {
        return panel;
    }    
    
    private void selectionChanged(PropertyChangeEvent evt) {
        Node[] newSelection = ((Node[]) evt.getNewValue());
        if(newSelection == null || newSelection.length == 0) {                
            showNoContent(NbBundle.getMessage(LocalHistoryDiffView.class, "MSG_DiffPanel_NoVersion"));
            return;
        }

        StoreEntry se = newSelection[0].getLookup().lookup(StoreEntry.class);
        
        if( se == null ) {
            showNoContent(NbBundle.getMessage(LocalHistoryDiffView.class, "MSG_DiffPanel_IllegalSelection"));
            return;
        }
        refreshDiffPanel(se);                
    }           
    
    private void refreshDiffPanel(StoreEntry se) {       
        DiffPrepareTask prepareTask = new DiffPrepareTask(se);
        RequestProcessor.Task task = RequestProcessor.getDefault().create(prepareTask);        
        task.schedule(0);        
    }        

    private class DiffPrepareTask implements Runnable {
        
        private final StoreEntry entry;

        public DiffPrepareTask(final StoreEntry se) {
            entry = se;
        }

        public void run() {
            // XXX how to get the mimetype
            
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {            
                    try {   
                        
                        FileObject fo = FileUtil.toFileObject(entry.getFile());
                        Charset cs = fo != null || !fo.isFolder() ? cs = FileEncodingQuery.getEncoding(fo) : null;
                        InputStreamReader storeReader = cs != null ? 
                                    new InputStreamReader(entry.getStoreFileInputStream(), cs) :
                                    new InputStreamReader(entry.getStoreFileInputStream());
                                                                        
                        StreamSource ss1 = StreamSource.createSource("historyfile", entry.getFile().getName() + " " + StoreEntryNode.getFormatedDate(entry), entry.getMIMEType(), storeReader);
                        
                        String title;
                        StreamSource ss2;
                        File file = entry.getFile();
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
                        ErrorManager.getDefault().notify(ioe);
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
