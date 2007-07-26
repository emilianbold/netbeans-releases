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
package org.netbeans.modules.subversion.ui.browser;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.sql.Date;
import javax.swing.Action;
import javax.swing.JButton;
import org.netbeans.modules.subversion.RepositoryFile;
import org.netbeans.modules.subversion.ui.browser.RepositoryPathNode.RepositoryPathEntry;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.RequestProcessor;
import org.tigris.subversion.svnclientadapter.SVNNodeKind;
import org.tigris.subversion.svnclientadapter.SVNRevision;

/**
 * Creates a new folder in the browser
 *
 * @author Tomas Stupka
 */
public class CreateFolderAction extends BrowserAction implements PropertyChangeListener {
    private final String defaultFolderName;    
    
    public CreateFolderAction(String defaultFolderName) {        
        this.defaultFolderName = defaultFolderName;
        putValue(Action.NAME, org.openide.util.NbBundle.getMessage(RepositoryPathNode.class, "CTL_Action_MakeDir")); // NOI18N
        setEnabled(false);
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        if (ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {            
            setEnabled(isEnabled());   
        }
    }
    
    public boolean isEnabled() {
        Browser browser = getBrowser();
        if(browser == null) {
            return false;
        }        
        if(browser.getExplorerManager().getRootContext() == Node.EMPTY) {
            return false;
        }
        Node[] nodes = getBrowser().getSelectedNodes();
        if(nodes.length != 1) {
            return false;
        }
        return nodes[0] instanceof RepositoryPathNode && 
               ((RepositoryPathNode) nodes[0]).getEntry().getSvnNodeKind() == SVNNodeKind.DIR;
    }

    /**
     * Configures this action with the actuall browser instance
     */
    public void setBrowser(Browser browser) {        
        Browser oldBrowser = getBrowser();
        if(oldBrowser!=null) {
            oldBrowser.removePropertyChangeListener(this);
        }
        browser.addPropertyChangeListener(this);
        super.setBrowser(browser);                
    }    
    
    public void actionPerformed(ActionEvent e) {
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {                           
                Node[] nodes = getSelectedNodes();
                if(nodes.length > 1) {                        
                    return; 
                }      

                RepositoryPathNode repositoryPathNode = (RepositoryPathNode) nodes[0];                                     
                Children children = repositoryPathNode.getChildren();
                Node[] childNodes = children.getNodes();
                if(childNodes.length > 0) {
                  try {
                        // force listing of all child nodes ...
                        getExplorerManager().setSelectedNodes(new Node[] {childNodes[0]}); 
                    } catch (PropertyVetoException ex) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex); // should not happen
                    }                         
                }

                DialogDescriptor.InputLine input = 
                    new DialogDescriptor.InputLine(java.util.ResourceBundle.getBundle("org/netbeans/modules/subversion/ui/browser/Bundle").getString("CTL_Browser_NewFolder_Prompt"), java.util.ResourceBundle.getBundle("org/netbeans/modules/subversion/ui/browser/Bundle").getString("CTL_Browser_NewFolder_Title"));
                input.setInputText(defaultFolderName);
                DialogDisplayer.getDefault().notify(input);                    
                String newDir = input.getInputText().trim();                    
                if(input.getValue() == DialogDescriptor.CANCEL_OPTION || 
                   input.getValue() == DialogDescriptor.CLOSED_OPTION || 
                   newDir.equals(""))  // NOI18N
                {
                    return;
                }                    

                children = repositoryPathNode.getChildren();
                if(children != null && children.getNodesCount() > 0) {
                    childNodes = children.getNodes();
                    for (int i = 0; i < childNodes.length; i++) {
                        if(childNodes[i].getDisplayName().equals(newDir)) {
                            JButton ok = new JButton(java.util.ResourceBundle.getBundle("org/netbeans/modules/subversion/ui/browser/Bundle").getString("CTL_Browser_OK"));
                            NotifyDescriptor descriptor = new NotifyDescriptor(
                                    org.openide.util.NbBundle.getMessage(CreateFolderAction.class, "MSG_Browser_FolderExists", newDir), // NOI18N
                                    java.util.ResourceBundle.getBundle("org/netbeans/modules/subversion/ui/browser/Bundle").getString("MSG_Browser_WrongFolerName"), // NOI18N
                                    NotifyDescriptor.DEFAULT_OPTION,
                                    NotifyDescriptor.ERROR_MESSAGE,
                                    new Object [] { ok },
                                    ok);
                            DialogDisplayer.getDefault().notify(descriptor);        
                            return;
                        }
                    }
                }

                RepositoryFile parentFile = repositoryPathNode.getEntry().getRepositoryFile();                    
                Node segmentNode = repositoryPathNode;
                String[] segments = newDir.split("/"); // NOI18N
                for (int i = 0; i < segments.length; i++) {                                                
                    
                    RepositoryFile newFile = parentFile.appendPath(segments[i]);
                    RepositoryPathEntry entry = new RepositoryPathEntry(newFile, SVNNodeKind.DIR, new SVNRevision(0), new Date(System.currentTimeMillis()), ""); // XXX gget author
                    Node node = RepositoryPathNode.createNewBrowserNode(getBrowser(), entry);    
                    Node[] newChild = new Node[] {node};
                    segmentNode.getChildren().add(newChild);    
                    segmentNode = node;
                    parentFile = newFile;

                    if( i == segments.length - 1 ) {
                        // we are done, select the node ...
                        try {
                            setSelectedNodes(newChild);
                        } catch (PropertyVetoException ex) {
                            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex); // should not happen
                        }                                          
                    }                            
                }                                                            
            }
        });
    }
}    