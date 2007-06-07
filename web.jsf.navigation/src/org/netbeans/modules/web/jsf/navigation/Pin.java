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
 *
 * PinNode.java
 *
 * Created on March 22, 2007, 3:57 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.web.jsf.navigation;

import java.awt.Image;
import java.io.IOException;
import javax.swing.Action;
import org.netbeans.modules.web.jsf.navigation.graph.PageFlowSceneElement;
import org.netbeans.modules.web.jsf.navigation.pagecontentmodel.PageContentItem;
import org.openide.ErrorManager;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.PropertySupport.Reflection;
import org.openide.nodes.Sheet;
import org.openide.nodes.Sheet.Set;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author joelle
 */
public class Pin extends PageFlowSceneElement{
    
    private Page pageNode;
    private boolean isDefault = true;
    private PageContentItem pageContentItem;
    
    /** Creates a default PinNode
     * @param pageNode
     */
    public Pin(Page pageNode) {
        this.pageNode = pageNode;
    }
    
    /**
     * Create a nondefault pin in a page which represents a page content item.
     * @param pageNode for which the pin belongs.
     * @param pageContentItem
     */
    public Pin( Page pageNode, PageContentItem pageContentItem) {
        assert pageContentItem != null;
        
        this.pageNode = pageNode;
        this.pageContentItem = pageContentItem;
        isDefault = false;
    }
    
    /**
     * Is this a default pin?
     * @return boolean is Default?
     */
    public boolean isDefault() {
        return isDefault;
    }
    
    @Override
    public String toString() {
        return new String("Pin[pagename=" + pageNode.getDisplayName() + " isDefault=" + isDefault() +"] ");
    }
    
    
    
    /**
     * Is this a default pin?
     * @return Image pageContentItem Image
     */
    public Image getIcon(int type) {
        if( pageContentItem != null ) {
            return pageContentItem.getBufferedIcon();
        }
        return null;
    }
    
    
    /**
     * Get the name of this pin.  Will return content item name.
     * @return String
     */
    public String getName() {
        if( pageContentItem != null ) {
            return pageContentItem.getName();
        }
        return null;
    }
    
    /**
     *
     * @return fromAction String
     */
    public String getFromAction() {
        if( pageContentItem != null ) {
            return pageContentItem.getFromAction();
        }
        return null;
    }
    
    /**
     *
     * @return fromOutcome String
     */
    public String getFromOutcome() {
        if( !isDefault ){
            return pageContentItem.getFromOutcome();
        }
        return null;
    }
    
    public void setFromOutcome(String fromOutcome) {
        if( pageContentItem != null )
            pageContentItem.setFromOutcome(fromOutcome);
    }
    
    public void setFromAction(String fromAction ){
        if( pageContentItem != null )
            pageContentItem.setFromAction(fromAction);
    }
    
    /**
     *
     * @return
     */
    public Page getPageFlowNode() {
        return pageNode;
    }
    
    public Action[] getActions(){
        if( pageContentItem != null ){
            return pageContentItem.getActions();
        }
        return new Action[]{};
    }
    
    
    public boolean equals(Object obj) {
        return (this == obj);
    }
    
    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }
    
    public HelpCtx getHelpCtx() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void destroy() throws IOException {
        if( pinNode != null ){
            pinNode.destroy();
        }
    }
    
    public boolean canDestroy() {
        return false;
    }
    
    public boolean canRename() {
        return false;
    }
    
    
    //    @Override
    //    public boolean equals(Object obj) {
    //        if( pageContentItem != null )
    //            return pageContentItem.equals(obj);
    //        if( !(obj instanceof PinNode)){
    //            return false;
    //        }
    //        PinNode tmpPinNode = (PinNode)obj;
    //        return getPageFlowNode().equals(tmpPinNode.getPageFlowNode()) && getName().equals(tmpPinNode.getName());
    //
    //    }
    //
    //    @Override
    //    public int hashCode() {
    //        if( pageContentItem != null )
    //           return pageContentItem.hashCode();
    //        if( ! isDefault() )
    //            return getPageFlowNode().hashCode() * getName().hashCode();
    //        return getPageFlowNode().hashCode();
    //    }
    
    
    
    public Node getNode() {
        if( pinNode == null )
            pinNode = new MyNode();
        return pinNode;
    }
    
    Node pinNode;
    private class MyNode extends AbstractNode {
        
        public MyNode() {
            super(Children.LEAF);
        }
        
        @Override
        protected Sheet createSheet() {
            Sheet s = Sheet.createDefault();
            Set ss = s.get("general"); // NOI18N
            if (ss == null) {
                ss = new Sheet.Set();
                ss.setName("general"); // NOI18N
                ss.setDisplayName(NbBundle.getMessage(Pin.class, "General")); // NOI18N
                ss.setShortDescription(NbBundle.getMessage(Pin.class, "GeneralHint")); // NOI18N
                s.put(ss);
            }
            Set gs = ss;
            
            try {
                PropertySupport.Reflection p;
                p = new Reflection<String>(pageContentItem, String.class, "getName", "setName"); // NOI18N
                p.setName("fromView"); // NOI18N
                p.setDisplayName(NbBundle.getMessage(Pin.class, "FromView")); // NOI18N
                p.setShortDescription(NbBundle.getMessage(Pin.class, "FromViewHint")); // NOI18N
                ss.put(p);
                
                p = new Reflection<String>(pageContentItem, String.class, "getFromOutcome", "setFromOutcome"); // NOI18N
                p.setName("fromOutcome"); // NOI18N
                p.setDisplayName(NbBundle.getMessage(Pin.class, "Outcome")); // NOI18N
                p.setShortDescription(NbBundle.getMessage(Pin.class, "OutcomeHint")); // NOI18N
                ss.put(p);
                
            } catch (NoSuchMethodException nsme) {
                ErrorManager.getDefault().notify(nsme);
            }
            
            return s;
        }
    }
    
    
}
