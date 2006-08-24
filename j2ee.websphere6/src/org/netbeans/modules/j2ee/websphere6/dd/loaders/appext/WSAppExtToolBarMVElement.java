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
package org.netbeans.modules.j2ee.websphere6.dd.loaders.appext;

import java.io.IOException;
import org.netbeans.modules.j2ee.websphere6.dd.beans.WSAppExt;
import org.netbeans.modules.j2ee.websphere6.dd.loaders.WSMultiViewDataObject;
import org.netbeans.modules.xml.multiview.*;
import org.netbeans.modules.xml.multiview.ui.*;
import org.openide.nodes.*;
import org.openide.util.RequestProcessor;
import org.netbeans.modules.xml.multiview.Error;
/**
 *
 * @author dlipin
 */
public class WSAppExtToolBarMVElement extends ToolBarMultiViewElement implements java.beans.PropertyChangeListener{
    private ToolBarDesignEditor comp;
    private SectionView view;
    private WSAppExtDataObject dObj;
    private PanelFactory factory;
    private RequestProcessor.Task repaintingTask;
    private boolean needInit=true;
    private static final long serialVersionUID = 76737428339792L;
    
     private static final String APPEXT_MV_ID = WSMultiViewDataObject.MULTIVIEW_APPEXT + 
            WSMultiViewDataObject.DD_MULTIVIEW_POSTFIX;
    
    public WSAppExtToolBarMVElement(WSAppExtDataObject dObj) {
        super(dObj);
        this.dObj=dObj;
        comp = new ToolBarDesignEditor();
        factory=new PanelFactory(comp,dObj);
        setVisualEditor(comp);
        repaintingTask = RequestProcessor.getDefault().create(new Runnable() {
            public void run() {
                javax.swing.SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        repaintView();
                    }
                });
            }
        });
    }
    
    private void repaintView() {
        view =new WSAppExtView(dObj);
        comp.setContentView(view);
        Object lastActive = comp.getLastActive();
        if (lastActive!=null) {
            ((SectionView)view).openPanel(lastActive);
        } else {
           
        }
        view.checkValidity();
        
    }
    
    
    
    public SectionView getSectionView() {
        return view;
    }
    public WSAppExtView getAppExtView() {
        return (WSAppExtView)view;
    }
    
    public void componentShowing() {
        super.componentShowing();
        if (needInit) {
            repaintView();
            needInit=false;
        }
        //view=new WSAppExtView(dObj);
        comp.setContentView(view);
        try {
            ((SectionView)view).openPanel(dObj.getAppExt());
        } catch(java.io.IOException e) {
        }
        view.checkValidity();
    }
    
    public void componentOpened() {
        super.componentOpened();
        try {
            dObj.getAppExt().addPropertyChangeListener(this);
        } catch(IOException ex) {
            ex=null;
        }
    }
    
    public void componentClosed() {
        super.componentClosed();
        try {
            dObj.getAppExt().removePropertyChangeListener(this);
        } catch(IOException ex) {
            ex=null;
        }
    }
    public void propertyChange(java.beans.PropertyChangeEvent evt) {
        if (!dObj.isChangedFromUI()) {
            String name = evt.getPropertyName();
            if ( name.indexOf("ApplicationExt")>0 ) { //NOI18
                // repaint view if the wiew is active and something is changed with filters
                if (APPEXT_MV_ID.equals(dObj.getSelectedPerspective().preferredID())) {
                    repaintingTask.schedule(100);
                } else {
                    needInit=true;
                }
            }
        }
    }
    
    private class WSAppExtView extends SectionView {
        
        private WSAppExt appext;        
        
        WSAppExtView(WSAppExtDataObject dObj) {
            super(factory);
            
            Children rootChildren = new Children.Array();
            Node root = new AbstractNode(rootChildren);
            try {
                this.appext=dObj.getAppExt();
                rootChildren.add(new Node[]{
                    createAppExtAttrNode()                    
                });
            } catch (java.io.IOException ex) {
                System.out.println("ex="+ex);
                root.setDisplayName("Invalid AppExt");
            } finally {
                setRoot(root);
            }
            
        }
        
        
        
        private Node createAppExtAttrNode() {
            Node appextNode = new WSAppExtNode(dObj);
            // add panels
            addSection(new SectionPanel(this,appextNode,appext));
            return appextNode;
        }
    }
   
    
    public Error validateView() {        
        return null;
    }
    
    
    public static class WSAppExtNode extends org.openide.nodes.AbstractNode {
        WSAppExtNode(WSAppExtDataObject appext) {
            super(org.openide.nodes.Children.LEAF);            
            setDisplayName("Application Extension Deployment Information");
            
        }
    }    
    
}
