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


package org.netbeans.modules.j2ee.ddloaders.web.multiview;

import org.netbeans.modules.j2ee.dd.api.web.SecurityConstraint;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.dd.api.web.WebResourceCollection;
import org.netbeans.modules.j2ee.ddloaders.web.DDDataObject;
import org.netbeans.modules.xml.multiview.ui.DefaultTablePanel;
import org.netbeans.modules.xml.multiview.ui.EditDialog;
import org.openide.util.NbBundle;

/**
 * WebResourceCollectionTablePanel.java
 *
 * Panel for displaying the web resource collection table.
 *
 * @author  ptliu
 */
public class WebResourceCollectionTablePanel extends DefaultTablePanel {
    private DDDataObject dObj;
    private WebResourceCollectionTableModel model;
    private SecurityConstraint constraint;
    
    /**
     * Creates new form WebResourceCollectionTablePanel
     */
    public WebResourceCollectionTablePanel(final DDDataObject dObj,
            final WebResourceCollectionTableModel model) {
        super(model);
        this.dObj = dObj;
        this.model = model;
        
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dObj.modelUpdatedFromUI();
                dObj.setChangedFromUI(true);
                int row = getTable().getSelectedRow();
                model.removeRow(row);
                dObj.setChangedFromUI(false);
            }
        });
        
        editButton.addActionListener(new TableActionListener(false));
        addButton.addActionListener(new TableActionListener(true));
    }
    
    void setModel(WebApp webApp, SecurityConstraint constraint,
            WebResourceCollection[] collections) {
        model.setData(constraint, collections);
        model.setWebApp(webApp);
        this.constraint = constraint;
    }
    
    private class TableActionListener implements java.awt.event.ActionListener {
        private boolean add;
        
        TableActionListener(boolean add) {
            this.add=add;
        }
        
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            final int row = (add?-1:getTable().getSelectedRow());
            final WebResourceCollectionPanel dialogPanel = new WebResourceCollectionPanel();
            
            if (!add) {
                WebResourceCollection col = model.getWebResourceCollection(row);
                dialogPanel.setResourceName(col.getWebResourceName());
                dialogPanel.setDescription(col.getDefaultDescription());
                dialogPanel.setUrlPatterns(col.getUrlPattern());
                dialogPanel.setHttpMethods(col.getHttpMethod());
            }
            
            EditDialog dialog = new EditDialog(dialogPanel,NbBundle.getMessage(EjbRefsTablePanel.class,"TTL_WebResource"),add) {
                protected String validate() {
                    String name = dialogPanel.getResourceName().trim();
                    WebResourceCollection webResource = null;
                    
                    if (row != -1) 
                        webResource = model.getWebResourceCollection(row);
                    
                    if (name.length()==0) {
                        return NbBundle.getMessage(WebResourceCollectionTablePanel.class,"TXT_EmptyWebResourceName");
                    } else {
                        WebResourceCollection[] col = constraint.getWebResourceCollection();
                        
                        for (int i = 0; i < col.length; i++) {
                            if (col[i] != webResource && name.equals(col[i].getWebResourceName())) {
                                return NbBundle.getMessage(WebResourceCollectionTablePanel.class,"TXT_WebResourceNameExists",name);
                            }
                        }
                    }
                    
                    String[] urlPatterns = dialogPanel.getUrlPatterns();
                    if (urlPatterns.length == 0) {
                        return NbBundle.getMessage(WebResourceCollectionTablePanel.class,"TXT_EmptyUrlPatterns",name);
                    }
                    return null;
                }
            };
            
            if (add)
                dialog.setValid(false); // disable OK button
            
            javax.swing.event.DocumentListener docListener = new EditDialog.DocListener(dialog);
            dialogPanel.getResourceNameTF().getDocument().addDocumentListener(docListener);
            dialogPanel.getDescriptionTF().getDocument().addDocumentListener(docListener);
            dialogPanel.getUrlPatternsTF().getDocument().addDocumentListener(docListener);
        
            java.awt.Dialog d = org.openide.DialogDisplayer.getDefault().createDialog(dialog);
            d.setVisible(true);
            
            dialogPanel.getResourceNameTF().getDocument().removeDocumentListener(docListener);
            dialogPanel.getDescriptionTF().getDocument().removeDocumentListener(docListener);
            dialogPanel.getUrlPatternsTF().getDocument().removeDocumentListener(docListener);
            
            if (dialog.getValue().equals(EditDialog.OK_OPTION)) {
                dObj.modelUpdatedFromUI();
                dObj.setChangedFromUI(true);
                
                String resourceName = dialogPanel.getResourceName();
                String[] urlPatterns = dialogPanel.getUrlPatterns();
                String[] httpMethods = dialogPanel.getSelectedHttpMethods();
                String description = dialogPanel.getDescription();
                
                
                if (add)
                    model.addRow(new Object[] {resourceName, urlPatterns,
                    httpMethods, description});
                else
                    model.editRow(row, new Object[]{resourceName, 
                    urlPatterns, httpMethods, description});
              
                dObj.setChangedFromUI(false);
            }
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(new java.awt.GridBagLayout());

    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    
}
