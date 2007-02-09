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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.localhistory.ui.view;

import org.netbeans.modules.localhistory.ui.view.DiffPanel;
import org.netbeans.modules.localhistory.ui.view.*;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.netbeans.api.diff.Diff;
import org.netbeans.api.diff.DiffView;
import org.netbeans.api.diff.StreamSource;
import org.netbeans.modules.localhistory.store.StoreEntry;
import org.netbeans.modules.versioning.util.NoContentPanel;
import org.openide.ErrorManager;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Tomas Stupka
 */
public class LocalHistoryDiffView implements PropertyChangeListener, ActionListener {
           
    private DiffPanel panel;
    private Component diffComponent;
    private DiffView diffView;            
    
    /** Creates a new instance of LocalHistoryView */
    public LocalHistoryDiffView() {                
        panel = new DiffPanel();                                                              
        panel.nextButton.addActionListener(this);
        panel.prevButton.addActionListener(this);
        showNoContent(NbBundle.getMessage(LocalHistoryDiffView.class, "MSG_DiffPanel_NoVersion"));                
    }    
        
    public void propertyChange(PropertyChangeEvent evt) {
        if(ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {
            selectionChanged(evt);
        } 
    }
      
    JPanel getPanel() {
        return panel;
    }    
    
    private void selectionChanged(PropertyChangeEvent evt) {
        Node[] newSelection = ((Node[]) evt.getNewValue());
        if(newSelection == null || newSelection.length == 0) {                
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
            final Diff diff = Diff.getDefault();
            
            // XXX how to get the mimetype
            
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {            
                    try {   
                        
                        StreamSource ss1 = StreamSource.createSource("historyfile", entry.getFile().getName() + " " + StoreEntryNode.getFormatedDate(entry), entry.getMIMEType(), new InputStreamReader(entry.getStoreFileInputStream()));
                        StreamSource ss2 = StreamSource.createSource("currentfile", NbBundle.getMessage(LocalHistoryDiffView.class, "LBL_Diff_CurrentFile"), entry.getMIMEType(), new FileReader(entry.getFile()));
                        
                        diffView = diff.createDiff(ss1, ss2);  
                        
                        setDiffComponent(diffView.getComponent());                        
                        if(diffView.getDifferenceCount() > 0) {
                            setCurrentDifference(0);
                        } else {
                            refreshNavigationButtons();
                        }
//                            panel.revalidate();
//                            panel.repaint();

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
//        panel.diffPanel.revalidate();
//        panel.diffPanel.repaint();
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
        int nextDiffernce = diffView.getCurrentDifference() + 1;        
        if(nextDiffernce < diffView.getDifferenceCount()) {
            setCurrentDifference(nextDiffernce);    
        }                        
    }

    private void onPrevButton() {
        if(diffView == null) {
            return;
        }
        int prevDiffernce = diffView.getCurrentDifference() - 1;
        if(prevDiffernce > -1) {
            setCurrentDifference(prevDiffernce);                
        }            
    }    
    
    private void setCurrentDifference(int idx) {
        diffView.setCurrentDifference(idx);    
        refreshNavigationButtons();
    }
    
    private void refreshNavigationButtons() {
        int currentDifference = diffView.getCurrentDifference();
        panel.prevButton.setEnabled(currentDifference > 0);
        panel.nextButton.setEnabled(currentDifference < diffView.getDifferenceCount() - 1);
    }    
}
