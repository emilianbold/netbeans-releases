/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.xml.schema.abe.navigator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.io.IOException;
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
import org.netbeans.modules.xml.text.navigator.base.AbstractXMLNavigatorContent;

/**
 * Navigator component containing a tree of abe components along with
 * element and type filters
 *
 * @author  Chris Webster
 */
public class NavigatorContent extends AbstractXMLNavigatorContent {
    /** silence compiler warnings */
    private static final long serialVersionUID = 1L;
    
    /**
     * Creates a new instance of SchemaNavigatorContent.
     */
    public NavigatorContent() {
	setLayout(new BorderLayout());
	treeView = new BeanTreeView();
	treeView.setRootVisible(false);
	explorerManager.addPropertyChangeListener(this);
    }
    
    public void navigate(final DataObject dobj) {
        showWaitPanel();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                AXIModel model = getAXIModel(dobj);
                if (model == null || model.getState() != Model.State.VALID) {
                    showError(AbstractXMLNavigatorContent.ERROR_NO_DATA_AVAILABLE);
                } else {
                    show(model.getRoot());
                }
            }
        });
    }
    
    private void redraw() {
	TopComponent tc = (TopComponent) SwingUtilities.
                getAncestorOfClass(TopComponent.class,this);
	if (tc != null) {
	    tc.revalidate();
	    tc.repaint();
	}
    }
    
    private void show(final AXIComponent component) {
        removeAll();
        if (!treeView.isShowing())
        add(treeView, BorderLayout.CENTER);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                treeView.setRootVisible(false);
                AXINodeVisitor nv = new AXINodeVisitor();
                Node n = nv.getNode(component);
                getExplorerManager().setRootContext(n);
                redraw();
            }
        });
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
        } else if( tc != null &&
                   getExplorerManager() != null &&
                   TopComponent.getRegistry().PROP_ACTIVATED.equals(property) &&
                   tc == TopComponent.getRegistry().getActivated()) {
            tc.setActivatedNodes(getExplorerManager().getSelectedNodes());
        }
    }
    
    public void onModelStateChanged(PropertyChangeEvent evt) {
        State newState = (State)evt.getNewValue();
        if(newState != AXIModel.State.VALID) {
            showError(AbstractXMLNavigatorContent.ERROR_NO_DATA_AVAILABLE);
            return;
        }
        AXIModel model = (AXIModel)evt.getSource();
        show(model.getRoot());
    }
    
    //Always listens to the active model. Remove earlier listeners.
    private AXIModel getAXIModel(DataObject dobj) {
        try {
            SchemaModelCookie modelCookie = dobj.getCookie(SchemaModelCookie.class);
            //it is possible that the dobj is no longer for a schema.
            if(modelCookie == null)
                return null;
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
