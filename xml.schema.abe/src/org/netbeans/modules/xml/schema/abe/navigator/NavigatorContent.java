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

package org.netbeans.modules.xml.schema.abe.navigator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.axi.AXIModel;
import org.netbeans.modules.xml.axi.AXIModelFactory;
import org.netbeans.modules.xml.schema.abe.nodes.AXINodeVisitor;
import org.netbeans.modules.xml.schema.ui.basic.SchemaModelCookie;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.Model.State;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 * Navigator component containing a tree of abe components along with
 * element and type filters
 *
 * @author  Chris Webster
 */
public class NavigatorContent extends JPanel
    implements ExplorerManager.Provider, PropertyChangeListener {
    /** silence compiler warnings */
    private static final long serialVersionUID = 1L;
    /** Explorer manager for the tree view. */
    private final ExplorerManager explorerManager;
    /** Our schema component node tree view. */
    private final BeanTreeView treeView;
    private final javax.swing.JLabel notAvailableLabel = new javax.swing.JLabel(
            NbBundle.getMessage(NavigatorContent.class, "MSG_NotAvailable")); //NOI18N
    
    /**
     * Creates a new instance of SchemaNavigatorContent.
     */
    public NavigatorContent() {
	setLayout(new BorderLayout());
	explorerManager = new ExplorerManager();
	treeView = new BeanTreeView();
	treeView.setRootVisible(false);
	explorerManager.addPropertyChangeListener(this);
        
        //initialize the notAvailableLabel
        notAvailableLabel.setHorizontalAlignment(SwingConstants.CENTER);
        notAvailableLabel.setEnabled(false);
        Color usualWindowBkg = UIManager.getColor("window"); //NOI18N
        notAvailableLabel.setBackground(usualWindowBkg != null ? usualWindowBkg : Color.white);
        // to ensure our background color will have effect
        notAvailableLabel.setOpaque(true);        
    }
    
    public ExplorerManager getExplorerManager() {
	return explorerManager;
    }
    
    public void show(DataObject dobj) {
        AXIModel model = getAXIModel(dobj);
        if (model == null || model.getState() != Model.State.VALID) {
            showError();
        } else {
            show(model.getRoot());
        }
    }
    
    private void showError() {
        if (notAvailableLabel.isShowing()) {
            return;
        }
        remove(treeView);
        add(notAvailableLabel, BorderLayout.CENTER);
        redraw();
    }

    private void redraw() {
	TopComponent tc = (TopComponent) SwingUtilities.
                getAncestorOfClass(TopComponent.class,this);
	if (tc != null) {
	    tc.revalidate();
	    tc.repaint();
	}
    }
    
    private void show(AXIComponent component) {
        remove(notAvailableLabel);
        add(treeView, BorderLayout.CENTER);
	AXINodeVisitor nv = new AXINodeVisitor();
	Node n = nv.getNode(component);
	getExplorerManager().setRootContext(n);
        redraw();
    }
    
    // TODO add explorer manager listener to trigger navigation in
    // main view
    
    public boolean requestFocusInWindow() {
	return treeView.requestFocusInWindow();
    }
            
    public void propertyChange(PropertyChangeEvent evt) {
        String property = evt.getPropertyName();
        if(AXIModel.STATE_PROPERTY.equals(property)) {
            onModelStateChanged(evt);
            return;
        }
        
        TopComponent tc = (TopComponent) SwingUtilities.
                getAncestorOfClass(TopComponent.class,this);
        if (ExplorerManager.PROP_SELECTED_NODES.equals(property)) {
            Node[] filteredNodes = (Node[])evt.getNewValue();
            if (filteredNodes != null && filteredNodes.length >= 1) {
                // Set the active nodes for the parent TopComponent.
                if (tc != null) {
                    tc.setActivatedNodes(filteredNodes);
                }
            }
        } else if(TopComponent.getRegistry().PROP_ACTIVATED.equals(property) &&
                tc == TopComponent.getRegistry().getActivated()) {
            tc.setActivatedNodes(getExplorerManager().getSelectedNodes());
        }
    }
    
    public void onModelStateChanged(PropertyChangeEvent evt) {
        State newState = (State)evt.getNewValue();
        if(newState != AXIModel.State.VALID) {
            showError();
            return;
        }
        AXIModel model = (AXIModel)evt.getSource();
        show(model.getRoot());
    }
    
    //Always listens to the active model. Remove earlier listeners.
    private AXIModel getAXIModel(DataObject dobj) {
        try {
            SchemaModelCookie modelCookie = (SchemaModelCookie)dobj.
                    getCookie(SchemaModelCookie.class);
            assert modelCookie != null;            
            AXIModel model = AXIModelFactory.getDefault().getModel(modelCookie.getModel());
            if(model != null) {
                model.removePropertyChangeListener(this);
                model.addPropertyChangeListener(this);
            }
            
            return model;
	} catch (IOException ioe) {
            //will show blank page if there is an error.
	}
        
        return null;
    }

    @Override
    public void addNotify() {
	super.addNotify();
	redraw();
    }
    
}
