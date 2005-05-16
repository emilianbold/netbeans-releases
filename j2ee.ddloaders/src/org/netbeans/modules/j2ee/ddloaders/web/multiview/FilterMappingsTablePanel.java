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

import org.netbeans.modules.j2ee.dd.api.web.FilterMapping;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.dd.api.web.Filter;
import org.netbeans.modules.j2ee.ddloaders.web.DDDataObject;
import org.netbeans.modules.xml.multiview.ui.DefaultTablePanel;
import org.netbeans.modules.xml.multiview.ui.EditDialog;
import org.netbeans.modules.xml.multiview.ui.SimpleDialogPanel;
import org.netbeans.modules.xml.multiview.ui.SectionView;
import org.netbeans.modules.xml.multiview.ui.SectionPanel;
import org.openide.util.NbBundle;
import org.openide.DialogDescriptor;

/**
 *
 * @author  mk115033
 * Created on October 1, 2002, 3:52 PM
 */
public class FilterMappingsTablePanel extends DefaultTablePanel {
    private FilterMappingsTableModel model;
    private WebApp webApp;
    private DDDataObject dObj;
    private SectionView view;
    
    /** Creates new form FilterMappingsTablePanel */
    public FilterMappingsTablePanel(final SectionView view, final DDDataObject dObj, final FilterMappingsTableModel model) {
    	super(model);
    	this.model=model;
        this.dObj=dObj;
        this.view=view;
        webApp = dObj.getWebApp();
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                
                int row = getTable().getSelectedRow();
                String filterName = (String)model.getValueAt(row,0);
                dObj.modelUpdatedFromUI();
                model.removeRow(row);
                // updating filter's panel title
                Filter filter = (Filter)webApp.findBeanByName("Filter","FilterName",filterName); //NOI18N
                if (filter!=null) {
                    SectionPanel panel = view.findSectionPanel(filter);
                    panel.setTitle(((FiltersMultiViewElement.FiltersView)view).getFilterTitle(filter));
                }
                
            }
        });
        addButton.addActionListener(new TableActionListener(true));
        editButton.addActionListener(new TableActionListener(false));
    }

    void setModel(WebApp webApp, FilterMapping[] mappings) {
        model.setData(webApp,mappings);
        this.webApp=webApp;
    }
    
    private class TableActionListener implements java.awt.event.ActionListener {
        private boolean add;
        TableActionListener(boolean add) {
            this.add=add;
        }
        
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            String[] allFilters = DDUtils.getFilterNames(webApp);
            String[] allServlets = DDUtils.getServletNames(webApp);
            int row = (add?-1:getTable().getSelectedRow());
            FilterMapping mapping = null;
            if (add) {
                try {
                   mapping = (FilterMapping)webApp.createBean("FilterMapping"); //NOI18N
                } catch (ClassNotFoundException ex) {}
            } else {
                mapping = webApp.getFilterMapping(row);
            }
            final FilterMappingPanel dialogPanel = new FilterMappingPanel(mapping,allFilters,allServlets);
            final EditDialog dialog = new EditDialog(dialogPanel,
                NbBundle.getMessage(FilterMappingsTablePanel.class,"TTL_filterMapping"),
                add) {
                protected String validate() {
                    if (!dialogPanel.hasFilterNames())
                         return  NbBundle.getMessage(FilterMappingsTablePanel.class,"LBL_no_filters");
                    String urlPattern = dialogPanel.getUrlPattern();
                    if (dialogPanel.getUrlRB().isSelected() && urlPattern.length()==0)
                        return  NbBundle.getMessage(FilterMappingsTablePanel.class,"TXT_missingURL");
                    return null;
                }
            };
            if (allFilters==null || allFilters.length==0 ) // Disable OK with error message
                dialog.checkValues();
            else if (add) // 
                dialog.setValid(false); // Disable OK
            javax.swing.event.DocumentListener docListener = new EditDialog.DocListener(dialog);
            dialogPanel.getUrlTF().getDocument().addDocumentListener(docListener);
            dialogPanel.getUrlRB().addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    dialog.checkValues();
                }
            });
            dialogPanel.getServletNameRB().addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    dialog.checkValues();
                }
            });
            java.awt.Dialog d = org.openide.DialogDisplayer.getDefault().createDialog(dialog);
            d.show();
            dialogPanel.getUrlTF().getDocument().removeDocumentListener(docListener);
            if (dialog.getValue().equals(EditDialog.OK_OPTION)) {
                dObj.modelUpdatedFromUI();
                String filterName = dialogPanel.getFilterName();
                String urlPattern = dialogPanel.getUrlPattern();
                String servletName = dialogPanel.getServletName();
                String[] dispatcher = dialogPanel.getDispatcherTypes();
                if (add) {
                    model.addRow(new Object[]{filterName,urlPattern,servletName,dispatcher});
                } else {
                    String oldName = (String)model.getValueAt(row,0);
                    model.editRow(row, new Object[]{filterName,urlPattern,servletName,dispatcher});
                    // udating title for filter panel with old name
                    if (!filterName.equals(oldName)) {
                        Filter filter = (Filter)webApp.findBeanByName("Filter","FilterName",oldName); //NOI18N
                        if (filter!=null) {
                            SectionPanel panel = view.findSectionPanel(filter);
                            panel.setTitle(((FiltersMultiViewElement.FiltersView)view).getFilterTitle(filter));
                        }
                    }
                }
                // updating filter's panel title
                Filter filter = (Filter)webApp.findBeanByName("Filter","FilterName",filterName); //NOI18N
                if (filter!=null) {
                    SectionPanel panel = view.findSectionPanel(filter);
                    panel.setTitle(((FiltersMultiViewElement.FiltersView)view).getFilterTitle(filter));
                }
            }
        }
    }
}
