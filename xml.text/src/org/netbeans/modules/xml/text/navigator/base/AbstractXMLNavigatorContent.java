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

package org.netbeans.modules.xml.text.navigator.base;

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.SwingUtilities;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.explorer.view.TreeView;
import org.openide.loaders.DataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.Utilities;

/**
 * A base implementation of XML Navigator UI.
 *
 * @author Samaresh
 * @version 1.0
 */
public abstract class AbstractXMLNavigatorContent extends javax.swing.JPanel
    implements ExplorerManager.Provider, PropertyChangeListener {
    
    /**
     * 
     */
    protected ExplorerManager explorerManager;
    /**
     * 
     */
    protected TreeView treeView;
    
    /**
     * 
     */
    public AbstractXMLNavigatorContent() {
        explorerManager = new ExplorerManager();
        explorerManager.addPropertyChangeListener(this);
        treeView = new BeanTreeView();
    }
    
    /**
     * 
     */
    public void showWaitNode() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
               treeView.setRootVisible(true);
               explorerManager.setRootContext(new WaitNode());
            } 
        });
    }
    
    /**
     * 
     * @param dataObject 
     */
    public abstract void navigate(DataObject dataObject);
        
    /**
     * 
     */
    public void release() {
        removeAll();
        repaint();        
    }
    
    public void propertyChange(PropertyChangeEvent event) {        
    }
    
    public ExplorerManager getExplorerManager() {
	return explorerManager;
    }
    
    private static class WaitNode extends AbstractNode {
        
        private Image waitIcon = Utilities.loadImage("org/netbeans/modules/xml/text/navigator/resources/wait.gif"); // NOI18N
        
        WaitNode( ) {
            super( Children.LEAF );
        }
        
        @Override
        public Image getIcon(int type) {
             return waitIcon;
        }

        @Override
        public Image getOpenedIcon(int type) {
            return getIcon(type);
        }

        @java.lang.Override
        public java.lang.String getDisplayName() {
            return "Please Wait...";
        }
        
    }
    
  }


