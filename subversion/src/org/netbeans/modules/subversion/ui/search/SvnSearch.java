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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionListener;
import org.netbeans.modules.subversion.RepositoryFile;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.client.SvnClient;
import org.netbeans.modules.subversion.client.SvnProgressSupport;
import org.netbeans.modules.subversion.settings.HistorySettings;
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

    private SvnProgressSupport support;
    private SvnSearchView searchView;
    
    public SvnSearch(RepositoryFile repositoryRoot) {
        this.repositoryRoot = repositoryRoot;
        panel = new SvnSearchPanel();
        panel.listButton.addActionListener(this);
        panel.dateFromTextField.getDocument().addDocumentListener(this); 
        
        String date = DATE_FORMAT.format(new Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24 * 7));
        panel.dateFromTextField.setText(HistorySettings.getDefault().getSearchDateFrom(date));        
        searchView = new SvnSearchView(panel.list);
    }       

    /**
     * Cancels all running tasks
     */
    public void cancel() {
        Node rootNode = getExplorerManager().getRootContext();
        if(rootNode != null) {
            getExplorerManager().setRootContext(Node.EMPTY);
            try {                                
                rootNode.destroy();
                if(support != null) {
                    support.cancel();
                }
            } catch (IOException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex); // should not happen
            }            
        }
    }
    
    private void listLogEntries() {
        final Node root = new AbstractNode(new Children.Array());
        getExplorerManager().setRootContext(root);
        final Node[] waitNodes = new Node[] { new WaitNode("Loading..." )};
        root.getChildren().add(waitNodes);
        
        final Date dateFrom = getDateFrom();
        HistorySettings.getDefault().setSearchDateFrom(DATE_FORMAT.format(dateFrom));
                
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
                        root.getChildren().remove(waitNodes);
                        root.getChildren().add(new Node[] {errorNode});
                        return;
                    }

                    if(isCanceled()) {
                        return;
                    }    

                    searchView.setResults(lm);
                }                        
            };
            support.start(rp, "Searching revisions");
        } finally {
            support = null;
        }
    }
   
    public JPanel getSearchPanel() {
        return panel;
    }
    
    public SVNRevision getSelectedRevision() {
        ISVNLogMessage message = (ISVNLogMessage) panel.list.getSelectedValue();
        if(message == null) {
            return null;
        }
        return message.getRevision();
    }

    public void addListSelectionListener(ListSelectionListener listener) {
        panel.list.addListSelectionListener(listener);
    }
    
    public void removeListSelectionListener(ListSelectionListener listener) {
        panel.list.removeListSelectionListener(listener);
    }

    public void actionPerformed(ActionEvent e) {
        if(e.getSource()==panel.listButton) {
            listLogEntries();            
        }        
    }
    
    private ExplorerManager getExplorerManager() {
        return panel.getExplorerManager();
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
