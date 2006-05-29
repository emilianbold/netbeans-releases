/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.j2ee.websphere6.dd.loaders.appext;

import org.netbeans.modules.j2ee.websphere6.dd.beans.WSAppExt;
import org.netbeans.modules.j2ee.websphere6.dd.loaders.SectionNodes.*;
import org.netbeans.modules.j2ee.websphere6.dd.loaders.WSMultiViewDataObject;
import org.netbeans.modules.j2ee.websphere6.dd.loaders.ui.WSAppExtAttributesPanel;
import org.netbeans.modules.xml.multiview.*;
import org.netbeans.modules.xml.multiview.ui.*;
import org.openide.nodes.*;
import org.openide.util.RequestProcessor;
import org.openide.util.NbBundle;
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
        view=new WSAppExtView(dObj);
        comp.setContentView(view);
        try {
            ((SectionView)view).openPanel(dObj.getAppExt());
        } catch(java.io.IOException e) {
        }
        view.checkValidity();
    }
    
    public void propertyChange(java.beans.PropertyChangeEvent evt) {
        
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
