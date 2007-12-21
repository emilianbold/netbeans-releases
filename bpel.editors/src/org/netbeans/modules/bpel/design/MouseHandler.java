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

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JPopupMenu;
import org.netbeans.modules.bpel.design.model.patterns.Pattern;

import org.netbeans.modules.bpel.design.selection.EntitySelectionModel;
import org.netbeans.modules.bpel.design.selection.PlaceHolderManager;
import org.netbeans.modules.bpel.design.DiagramView;


public class MouseHandler extends MouseAdapter {
    
    private DiagramView diagramView;
 

    public MouseHandler(DiagramView diagramView) {
        this.diagramView = diagramView;
        diagramView.addMouseListener(this);
    }
    
    public DiagramView getDiagramView() {
        return diagramView;
    }
    
    
    public void cancel() {
        getNameEditor().cancelEdit();
    }
    
    
    public void mousePressed(MouseEvent e) {
        getDiagramView().requestFocus();
        Pattern p = getDiagramView().findPattern(e.getPoint());

        getSelectionModel().setSelectedPattern(p);

        maybeShowPopup(e);
    }

    
    public void mouseReleased(MouseEvent e) {
        maybeShowPopup(e);
    }
    
    
    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
            getNameEditor().startEdit(e.getPoint());
            if (!getNameEditor().isActive()) {
                Pattern selected = getSelectionModel().getSelectedPattern();
                if (selected != null) {
                    getDiagramView().getDesignView().performDefaultAction(selected);
                }
            }
        }
    }
    
    public NameEditor getNameEditor() {
        return getDiagramView().getNameEditor();
    }
    
    public EntitySelectionModel getSelectionModel() {
        return getDiagramView().getDesignView().getSelectionModel();
    }
    

    
    public boolean maybeShowPopup(MouseEvent e) {
        if (!e.isPopupTrigger()) return false;
        
        Pattern pattern = getSelectionModel().getSelectedPattern();
        
        if (pattern == null) return false;
        
        JPopupMenu popup = pattern.createPopupMenu();
        
        if (popup == null) return false;
        
        popup.show(e.getComponent(), e.getX(), e.getY());
        
        return true;
    }

}
