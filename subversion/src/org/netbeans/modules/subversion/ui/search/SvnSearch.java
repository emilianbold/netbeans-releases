/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.subversion.ui.search;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.ScrollPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionListener;
import org.netbeans.modules.subversion.RepositoryFile;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.client.SvnClient;
import org.netbeans.modules.subversion.client.SvnProgressSupport;
import org.netbeans.modules.subversion.settings.HistorySettings;
import org.netbeans.modules.subversion.util.NoContentPanel;
import org.openide.ErrorManager;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.RequestProcessor;
import org.tigris.subversion.svnclientadapter.ISVNLogMessage;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNRevision;

/**
 * Handles the UI for revision search.
 *
 * @author Tomas Stupka
 */
public class SvnSearch implements ActionListener, DocumentListener {
    
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private final SvnSearchPanel panel;    
    
    private RepositoryFile repositoryRoot;
    private SvnSearchView searchView ;
    private SvnProgressSupport support;
    private NoContentPanel noContentPanel;
            
    public SvnSearch(RepositoryFile repositoryRoot) {
        this.repositoryRoot = repositoryRoot;
        panel = new SvnSearchPanel();
        panel.listButton.addActionListener(this);
        panel.dateFromTextField.getDocument().addDocumentListener(this); 
        
        String date = DATE_FORMAT.format(new Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24 * 7));
        panel.dateFromTextField.setText(HistorySettings.getDefault().getSearchDateFrom(date));
        
        searchView = new SvnSearchView();
        
        panel.listPanel.setLayout(new BorderLayout());  
        panel.listPanel.add(searchView.getComponent());

        noContentPanel = new NoContentPanel();
        panel.noContentPanel.setLayout(new BorderLayout());  
        panel.noContentPanel.add(noContentPanel);
        noContentPanel.setLabel("<No Results - Search Not Performed>");        

        panel.listPanel.setVisible(false);
        panel.noContentPanel.setVisible(true);
    }       

    /**
     * Cancels all running tasks
     */
    public void cancel() {
        if(support != null) {
            support.cancel();
        }
    }
    
    private void listLogEntries() {
        final Date dateFrom = getDateFrom();
        HistorySettings.getDefault().setSearchDateFrom(DATE_FORMAT.format(dateFrom));
                
        noContentPanel.setLabel("<No Results Yet - Search in Progress...>");        
        panel.listPanel.setVisible(false);
        panel.noContentPanel.setVisible(true);       
        
        RequestProcessor rp = Subversion.getInstance().getRequestProcessor(this.repositoryRoot.getRepositoryUrl());
        try { 
            support = new SvnProgressSupport() {
                protected void perform() {
                    SvnClient client;
                    ISVNLogMessage[] lm;
                    try {
                        client = Subversion.getInstance().getClient(SvnSearch.this.repositoryRoot.getRepositoryUrl(), this);
                        lm = client.getLogMessages(repositoryRoot.getRepositoryUrl(), 
                                                   SVNRevision.HEAD, 
                                                   new SVNRevision.DateSpec(dateFrom));
                    } catch (SVNClientException ex) {
                        AbstractNode errorNode = new AbstractNode(Children.LEAF);
                        errorNode.setDisplayName("Error"); 
                        errorNode.setShortDescription(ex.getLocalizedMessage());
                        return;
                    }

                    if(isCanceled()) {
                        return;
                    }    

                    final ISVNLogMessage[] results = lm;
                    EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            panel.listPanel.setVisible(true);
                            panel.noContentPanel.setVisible(false);                     
                            searchView.setResults(results);      
                        }
                    });
                }                        
            };
            support.start(rp, "Searching revisions");
        } finally {
            // XXX and how is this supposed to work?
            support = null;
        }
    }
    
    public JPanel getSearchPanel() {
        return panel;
    }
    
    public SVNRevision getSelectedRevision() {
        return searchView.getSelectedValue();
    }

    public void addListSelectionListener(ListSelectionListener listener) {
        searchView.addListSelectionListener(listener);
    }
    
    public void removeListSelectionListener(ListSelectionListener listener) {
        searchView.removeListSelectionListener(listener);
    }

    public void actionPerformed(ActionEvent e) {
        if(e.getSource()==panel.listButton) {
            listLogEntries();            
        }        
    }
    
    private Date getDateFrom() {
        try {
            return DATE_FORMAT.parse(panel.dateFromTextField.getText());
        } catch (ParseException ex) {
            return null; // should not happen
        }
    }

    public void insertUpdate(DocumentEvent e) {
         validateUserInput();
    }

    public void removeUpdate(DocumentEvent e) {
         validateUserInput();
    }

    public void changedUpdate(DocumentEvent e) {
         validateUserInput();
    }

    private void validateUserInput() {
        String dateString = panel.dateFromTextField.getText();
        if(dateString.equals("")) {
            return;
        }
        boolean isValid = false;
        try {
            DATE_FORMAT.parse(panel.dateFromTextField.getText());
            isValid = true;
        } catch (ParseException ex) {
            ex.printStackTrace();
        }
        panel.listButton.setEnabled(isValid);
    }
    
}
