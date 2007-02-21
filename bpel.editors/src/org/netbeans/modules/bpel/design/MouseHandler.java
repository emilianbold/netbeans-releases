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
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.swing.JPopupMenu;
import org.netbeans.modules.bpel.design.model.patterns.Pattern;

import org.netbeans.modules.bpel.design.selection.EntitySelectionModel;
import org.netbeans.modules.bpel.design.selection.PlaceHolderManager;
import org.netbeans.modules.bpel.editors.api.ExternalBpelEditorDiagramClickListener;
import org.openide.util.Lookup;


public class MouseHandler extends MouseAdapter {
    
    private DesignView designView;
    private List mExternalClickListeners = new ArrayList(4);
 

    public MouseHandler(DesignView designView) {
        this.designView = designView;
        designView.addMouseListener(this);
        initializeExternalClickListeners();
    }
    
    public DesignView getDesignView() {
        return designView;
    }
    
    
    public void cancel() {
        getNameEditor().cancelEdit();
    }
    
    
    public void mousePressed(MouseEvent e) {
        getDesignView().requestFocus();
        Pattern p = getDesignView().findPattern(e.getPoint());

        getSelectionModel().setSelectedPattern(p);

        maybeShowPopup(e);
    }

    
    public void mouseReleased(MouseEvent e) {
        maybeShowPopup(e);
    }
    
    
    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
            getDesignView().getNameEditor().startEdit(e.getPoint());
            if (!getDesignView().getNameEditor().isActive()) {
                Pattern selected = getSelectionModel().getSelectedPattern();
                if (selected != null) {
                    getDesignView().performDefaultAction(selected);
                }
            }
        }
        
        // Notify external modules of the click.
        if (!getDesignView().getNameEditor().isActive()) {
            notifyExternalClickListeners(e);
        }
    }
    
    public NameEditor getNameEditor() {
        return getDesignView().getNameEditor();
    }
    
    public EntitySelectionModel getSelectionModel() {
        return getDesignView().getSelectionModel();
    }
    
    public PlaceHolderManager getPlaceHolderManager() {
        return getDesignView().getPlaceHolderManager();
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

    private void initializeExternalClickListeners() {
        Lookup.Result lookupResult = 
                Lookup.getDefault().lookup(new Lookup.Template(ExternalBpelEditorDiagramClickListener.class));
        if (lookupResult != null) {
            Collection instances = lookupResult.allInstances();
            for (Iterator iter=instances.iterator(); iter.hasNext();) {
                ExternalBpelEditorDiagramClickListener listener = 
                        (ExternalBpelEditorDiagramClickListener) iter.next();
                mExternalClickListeners.add(new WeakReference(listener));
            }
        }
    }
    
    private void notifyExternalClickListeners(MouseEvent e) {
        for (Iterator iter=mExternalClickListeners.iterator(); iter.hasNext();) {
            WeakReference ref = (WeakReference) iter.next();
            ExternalBpelEditorDiagramClickListener listener = (ExternalBpelEditorDiagramClickListener) ref.get();
            if (listener != null) {
                listener.diagramClicked(e);
            }
        }
    }
}
