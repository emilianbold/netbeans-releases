/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
