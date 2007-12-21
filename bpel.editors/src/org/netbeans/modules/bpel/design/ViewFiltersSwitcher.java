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

package org.netbeans.modules.bpel.design;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPopupMenu;
import org.netbeans.modules.bpel.design.model.ViewFilters;
import org.openide.util.NbBundle;

/**
 *
 * @author aa160298
 */
public class ViewFiltersSwitcher extends JButton implements ActionListener {
    
    private DesignView designView;
    private JPopupMenu popupMenu;
    private JCheckBoxMenuItem sequenceFilter;
    private JCheckBoxMenuItem partnerLinkFilter;
    
    
    public ViewFiltersSwitcher(DesignView designView) {
        this.designView = designView;
        setText(NbBundle.getMessage(getClass(), "LBL_ViewFiltersSwitcher")); // NOI18N
        addActionListener(this);
        
        sequenceFilter = new JCheckBoxMenuItem(NbBundle.getMessage(getClass(),
                "LBL_ViewFiltersSwitcher_ShowSequences")); // NOI18N
        sequenceFilter.addActionListener(this);
        sequenceFilter.addActionListener(this);
        
        partnerLinkFilter = new JCheckBoxMenuItem(NbBundle.getMessage(getClass(),
                "LBL_ViewFiltersSwitcher_ShowPartnerLinks")); // NOI18N
        partnerLinkFilter.addActionListener(this);
        partnerLinkFilter.addActionListener(this);
        
        popupMenu = new JPopupMenu(NbBundle.getMessage(getClass(),
                "LBL_ViewFiltersSwitcher_PopupMenu")); // NOI18N
        popupMenu.add(sequenceFilter);
        popupMenu.add(partnerLinkFilter);
    }
    
    
    public void actionPerformed(ActionEvent event) {
        if (event.getSource() == this) {
            sequenceFilter.removeActionListener(this);
            partnerLinkFilter.removeActionListener(this);
            
            sequenceFilter.setState(getViewFilters().showImplicitSequences());
            partnerLinkFilter.setState(getViewFilters().showPartnerlinks());
            
            sequenceFilter.addActionListener(this);
            partnerLinkFilter.addActionListener(this);
            
            popupMenu.show(this, 0, getHeight());
        } else if (event.getSource() == sequenceFilter &&
                sequenceFilter.getState() != getViewFilters().showImplicitSequences()){
            getViewFilters().setShowImplicitSequences(sequenceFilter.getState());
            updateView();
            
            
        } else if (event.getSource() == partnerLinkFilter &&
                partnerLinkFilter.getState() != getViewFilters().showPartnerlinks()) {
            getViewFilters().setShowPartnerlinks(partnerLinkFilter.getState());
            updateView();
        }
    }
    
    private void updateView(){
        designView.reloadModel();
        designView.diagramChanged();
        designView.getValidationDecorationProvider().updateDecorations();
    }
    
    private ViewFilters getViewFilters() {
        return designView.getModel().getFilters();
    }
}
