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

package org.netbeans.modules.xml.wsdl.ui.view.grapheditor.widget;

import java.beans.PropertyEditor;
import java.lang.reflect.InvocationTargetException;

import org.openide.actions.PropertiesAction;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.actions.SystemAction;

/**
 * Default filter node implementation for all widgets.
 *
 * @author  Nathan Fiedler
 */
public class ReadOnlyWidgetFilterNode extends FilterNode {

    /**
     * Creates a new instance of WidgetFilterNode.
     *
     * @param original  the original Node.
     */
    public ReadOnlyWidgetFilterNode(Node original) {
        //super(original, new ReadOnlyChildren(original));
        super(original);
    }
        
    @Override
    public javax.swing.Action[] getActions(boolean context) {
        return new javax.swing.Action[] {
                SystemAction.get(PropertiesAction.class)};
    }


    @Override
    public PropertySet[] getPropertySets () {
        PropertySet[] propertySet = super.getPropertySets();
        for(int i = 0; i < propertySet.length; i++) {
            PropertySet pSet = propertySet[i];
            ReadOnlyPropertySet rpSet = new ReadOnlyPropertySet(pSet);
            propertySet[i] = rpSet;
        }
        return propertySet;
    }

    @Override
    public boolean canRename()
    {
        return false;
    }

    @Override
    public boolean canDestroy()
    {
        return false;
    }

    @Override
    public boolean canCut()
    {
        return false;
    }

    @Override
    public boolean canCopy()
    {
        return false;
    }

    @Override
    public boolean hasCustomizer()
    {
        return false;
    }
    
    
    public static class ReadOnlyChildren extends FilterNode.Children {
       
       public ReadOnlyChildren(Node node) {
           super(node);
       }
       
       @Override
       protected Node[] createNodes(Node n) {
            return new Node[] {new ReadOnlyWidgetFilterNode(n)};
       }
   } 
   
   public static class ReadOnlyProperty extends Node.Property {
           
       private Node.Property mDelegate;
           
       public ReadOnlyProperty(Node.Property delegate) {
           super(delegate.getClass());
           this.mDelegate = delegate;
           this.setDisplayName(this.mDelegate.getDisplayName());
           this.setName(this.mDelegate.getName());
           this.setShortDescription(this.mDelegate.getShortDescription());
           this.setExpert(this.mDelegate.isExpert());
           this.setHidden(this.mDelegate.isHidden());
           this.setPreferred(this.mDelegate.isPreferred());
           
       }
       
       @Override
       public boolean equals(Object property) {
           return this.mDelegate.equals(property);
       }
       
       @Override
       public String getHtmlDisplayName() {
           return this.mDelegate.getHtmlDisplayName();
       }
       
       @Override
       public PropertyEditor getPropertyEditor() {
           return this.mDelegate.getPropertyEditor();
       }
       
       @Override
       public Class getValueType() {
           return this.mDelegate.getValueType();
       }
       
       @Override
       public int hashCode() {
           return this.mDelegate.hashCode();
       }
       
       @Override
       public boolean isDefaultValue() {
           return this.mDelegate.isDefaultValue();
       }
       
       @Override
       public void restoreDefaultValue() throws IllegalAccessException,
               InvocationTargetException {
           this.mDelegate.restoreDefaultValue();
       }
       
       @Override
       public boolean supportsDefaultValue() {
           return this.mDelegate.supportsDefaultValue();
       }
       
       @Override
       public boolean canRead() {
           return true;
       }
       
       @Override
       public boolean canWrite() {
           return false;
       }
       
       @Override
       public Object getValue() throws IllegalAccessException,
               InvocationTargetException {
           return mDelegate.getValue();
       }
       
       @Override
       public void setValue(Object val) throws IllegalAccessException,
               IllegalArgumentException, InvocationTargetException {
           //do nothing
       }
   }
   
   public static class ReadOnlyPropertySet extends Node.PropertySet {
           
       private Node.PropertySet mDelegate;
       
       public ReadOnlyPropertySet(Node.PropertySet delegate) {
           super(delegate.getName(), delegate.getDisplayName(), delegate.getShortDescription());
           this.mDelegate = delegate;
       }
       
       @Override
       public Property[] getProperties() {
           Property[] properties = this.mDelegate.getProperties();
           for(int i = 0; i < properties.length; i++) {
               Property p = properties[i];
               ReadOnlyProperty rp = new ReadOnlyProperty(p);
               properties[i] = rp;
           }
           
           return properties;
       }    
   }
}
