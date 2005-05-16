/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ddloaders.web.multiview;

import org.netbeans.modules.j2ee.dd.api.web.ErrorPage;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.ddloaders.web.DDDataObject;
import org.netbeans.modules.xml.multiview.ui.DefaultTablePanel;
import org.netbeans.modules.xml.multiview.ui.EditDialog;
import org.netbeans.modules.xml.multiview.ui.SimpleDialogPanel;
import org.openide.util.NbBundle;

/**
 *
 * @author  mk115033
 * Created on October 1, 2002, 3:52 PM
 */
public class ErrorPagesTablePanel extends DefaultTablePanel {
    private ErrorPagesTableModel model;
    private WebApp webApp;
    private DDDataObject dObj;
    
    /** Creates new form ErrorPagesTablePanel */
    public ErrorPagesTablePanel(final DDDataObject dObj, final ErrorPagesTableModel model) {
    	super(model);
    	this.model=model;
        this.dObj=dObj;
        webApp = dObj.getWebApp();
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dObj.modelUpdatedFromUI();
                int row = getTable().getSelectedRow();
                model.removeRow(row);
            }
        });
        addButton.addActionListener(new TableActionListener(true));
        editButton.addActionListener(new TableActionListener(false));
    }

    void setModel(WebApp webApp, ErrorPage[] pages) {
        model.setData(webApp,pages);
        this.webApp=webApp;
    }
    
    private class TableActionListener implements java.awt.event.ActionListener {
        private boolean add;
        TableActionListener(boolean add) {
            this.add=add;
        }
        
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            final int row = (add?-1:getTable().getSelectedRow());
            String[] labels = new String[]{
                NbBundle.getMessage(ErrorPagesTablePanel.class,"LBL_errorPage"),
                NbBundle.getMessage(ErrorPagesTablePanel.class,"LBL_errorCode"),
                NbBundle.getMessage(ErrorPagesTablePanel.class,"LBL_exceptionType")
            };

            char[] mnem = new char[] {
                NbBundle.getMessage(ErrorPagesTablePanel.class,"LBL_errorPage_mnem").charAt(0),
                NbBundle.getMessage(ErrorPagesTablePanel.class,"LBL_errorCode_mnem").charAt(0),
                NbBundle.getMessage(ErrorPagesTablePanel.class,"LBL_exceptionType_mnem").charAt(0)
            };
            SimpleDialogPanel.DialogDescriptor descriptor = new SimpleDialogPanel.DialogDescriptor(labels);
            if (!add) {
                Integer val = (Integer)model.getValueAt(row,1);
                String[] initValues = new String[] {
                    (String)model.getValueAt(row,0),
                    val==null?"":((Integer)val).toString(),
                    (String)model.getValueAt(row,2)
                };
                descriptor.setInitValues(initValues);
            }
            descriptor.setButtons(new boolean[]{true,false,false});
            descriptor.setMnemonics(mnem);
            final SimpleDialogPanel dialogPanel = new SimpleDialogPanel(descriptor);
            dialogPanel.getTextComponents()[0].setEditable(false);
            dialogPanel.getCustomizerButtons()[0].addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    try {
                        org.netbeans.api.project.SourceGroup[] groups = DDUtils.getDocBaseGroups(dObj);
                        org.openide.filesystems.FileObject fo = BrowseFolders.showDialog(groups);
                        if (fo!=null) {
                            String res = DDUtils.getResourcePath(groups,fo,'/',true);
                            dialogPanel.getTextComponents()[0].setText("/"+res);
                        }
                    } catch (java.io.IOException ex) {}
                }
            });
            EditDialog dialog = new EditDialog(dialogPanel,NbBundle.getMessage(ErrorPagesTablePanel.class,"TTL_ErrorPage"),add) {
                protected String validate() {
                    String[] values = dialogPanel.getValues();
                    String page = values[0].trim();
                    String code = values[1].trim();
                    String exc = values[2].trim();
                    if (page.length()==0) {
                        return NbBundle.getMessage(ErrorPagesTablePanel.class,"TXT_EmptyErrorPageLocation");
                    }
                    if (code.length()==0 && exc.length()==0) {
                        return NbBundle.getMessage(ErrorPagesTablePanel.class,"TXT_EP_BothMissing");
                    } else if (code.length()>0 && exc.length()>0) {
                        return NbBundle.getMessage(ErrorPagesTablePanel.class,"TXT_EP_BothSpecified");
                    } else if (code.length()>0)  {
                        Integer c = null;
                        try {
                            c = new Integer(code); 
                        } catch (NumberFormatException ex) {}
                        if (c==null) {
                            return NbBundle.getMessage(ErrorPagesTablePanel.class,"TXT_EP_wrongNumber",code);
                        } else {
                            ErrorPage[] pages = webApp.getErrorPage();
                            boolean exists=false;
                            for (int i=0;i<pages.length;i++) {
                                if (row!=i && c.equals(pages[i].getErrorCode())) {
                                    exists=true;
                                    break;
                                }
                            }
                            if (exists) {
                                return NbBundle.getMessage(ErrorPagesTablePanel.class,"TXT_ErrorCodeExists",c);
                            }
                        }
                    } else {
                        ErrorPage[] pages = webApp.getErrorPage();
                        boolean exists=false;
                        for (int i=0;i<pages.length;i++) {
                            if (row!=i && exc.equals(pages[i].getExceptionType())) {
                                exists=true;
                                break;
                            }
                        }
                        if (exists) {
                            return NbBundle.getMessage(ErrorPagesTablePanel.class,"TXT_ExcTypeExists",exc);
                        }
                    }
                    return null;
                }
            };
            if (add) dialog.setValid(false); // disable OK button
            
            javax.swing.event.DocumentListener docListener = new EditDialog.DocListener(dialog);
            dialogPanel.getTextComponents()[0].getDocument().addDocumentListener(docListener);
            dialogPanel.getTextComponents()[1].getDocument().addDocumentListener(docListener);
            dialogPanel.getTextComponents()[2].getDocument().addDocumentListener(docListener);
            
            java.awt.Dialog d = org.openide.DialogDisplayer.getDefault().createDialog(dialog);
            d.show();
            dialogPanel.getTextComponents()[0].getDocument().removeDocumentListener(docListener);
            dialogPanel.getTextComponents()[1].getDocument().removeDocumentListener(docListener);
            dialogPanel.getTextComponents()[2].getDocument().removeDocumentListener(docListener);
            
            if (dialog.getValue().equals(EditDialog.OK_OPTION)) {
                dObj.modelUpdatedFromUI();
                String[] values = dialogPanel.getValues();
                String page = values[0].trim();
                String code = values[1].trim();
                String exc = values[2].trim();
                if (add)
                    model.addRow(new Object[]{page,(code.length()==0?null:new Integer(code)),(exc.length()==0?null:exc)});
                else 
                    model.editRow(row,new Object[]{page,(code.length()==0?null:new Integer(code)),(exc.length()==0?null:exc)});
            }
        }
    }
}
