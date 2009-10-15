/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.compapp.casaeditor.nodes;

import java.beans.PropertyEditor;
import java.lang.reflect.InvocationTargetException;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;

/**
 *
 * @author rdara
 */
public class ReadOnlyFilterNode extends FilterNode {

    public ReadOnlyFilterNode(Node original) {
        super(original, new ReadOnlyChildren(original));
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
            return new Node[] {new ReadOnlyFilterNode(n)};
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
