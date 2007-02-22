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

/*
 * ReadOnlySchemaComponentNode.java
 *
 * Created on April 12, 2006, 2:41 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.ui.nodes.categorized;

import java.awt.datatransfer.Transferable;
import java.beans.PropertyEditor;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import javax.swing.Action;
import org.netbeans.modules.xml.refactoring.actions.RefactorAction;
import org.netbeans.modules.xml.schema.ui.nodes.SchemaComponentNode;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Property;
import org.openide.nodes.Node.PropertySet;
import org.openide.nodes.PropertySupport;
import org.openide.util.Lookup;
import org.openide.util.datatransfer.NewType;
import org.openide.util.datatransfer.PasteType;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author Ajit Bhate
 */
public class ReadOnlySchemaComponentNode extends FilterNode {
    private transient String displayTemplate;
    
    public ReadOnlySchemaComponentNode(Node original ,String displayTemplate) {
        this(original);
        this.displayTemplate = displayTemplate;
    }
    
    public ReadOnlySchemaComponentNode(Node original) {
        this(original,new Children(original), new InstanceContent());
    }
    
    protected ReadOnlySchemaComponentNode(Node original,
            org.openide.nodes.Children children, InstanceContent ic) {
        super(original, children, new ProxyLookup(new Lookup[] {
            new AbstractLookup(ic), original.getLookup()}));
        ic.add(this);
        SchemaComponentNode scn = (SchemaComponentNode) original.getLookup().
                lookup(SchemaComponentNode.class);
        if(scn!=null) scn.setReferencingNode(this);
    }
    
    public void setDisplayTemplate(String displayTemplate) {
        this.displayTemplate = displayTemplate;
    }
    
    public String getDisplayName() {
        String retValue = super.getDisplayName();
        return retValue;
    }
    
    public String getDefaultDisplayName() {
	String displayName = null;
	SchemaComponentNode scn = (SchemaComponentNode)
	    getLookup().lookup(SchemaComponentNode.class);
	if (scn != null) {
	    displayName = scn.getDefaultDisplayName();
	} else {
	    displayName = super.getDisplayName();
	}
	if(displayTemplate!=null)
            displayName = MessageFormat.format(displayTemplate,displayName);
	return displayName;
    }
    
    
    public String getHtmlDisplayName() {
        String retValue = super.getHtmlDisplayName();
        if(retValue == null) retValue = getDefaultDisplayName();
        if(retValue != null)
            retValue = "<font color='#999999'>"+retValue+"</font>";
        return retValue;
    }
    
    public boolean canRename() {
        return false;
    }
    
    public boolean canDestroy() {
        return false;
    }
    
    public boolean canCut() {
        return false;
    }
    
    public boolean canCopy() {
        // Allow read-only components to be copied (issue 95341).
        return true;
    }
    
    public boolean hasCustomizer() {
        return false;
    }
    
    public Node.PropertySet[] getPropertySets() {
        PropertySet[] retValue;
        
        retValue = super.getPropertySets();
        for(int i=0;i<retValue.length;i++) {
            Property[] props = retValue[i].getProperties();
            for(int j=0;j<props.length;j++) {
                final Property prop = props[j];
                props[j] = new PropertySupport.ReadOnly(
                        prop.getName(),	prop.getValueType(),
                        prop.getDisplayName(),prop.getShortDescription()) {
                    public Object getValue() throws IllegalAccessException,InvocationTargetException {
                        return prop.getValue();
                    }
                    public PropertyEditor getPropertyEditor() {
                        //TODO editor should be r/o
                        return prop.getPropertyEditor();
                    }
                    
                };
            }
        }
        return retValue;
    }
    
    public NewType[] getNewTypes() {
        return new NewType[]{};
    }
    
    public PasteType[] getPasteTypes(Transferable transferable) {
        // Disallow pasting anything to read-only nodes.
        return new PasteType[0];
    }
    
    public PasteType getDropType(Transferable transferable, int action, int index) {
        // Disallow dropping anything to read-only nodes.
        return null;
    }
    
    public Action[] getActions(boolean context) {
        ArrayList<Action> actionList = new ArrayList<Action>();
        Collections.addAll(actionList,super.getActions(context));
        for(int i=0;i<actionList.size();) {
            Action a=actionList.get(i++);
            if(a instanceof RefactorAction) {
                actionList.remove(a);
                break;
            }
        }
        return actionList.toArray(new Action[0]);
    }
    
    private static class Children extends FilterNode.Children {
        public Children(Node original) {
            super(original);
        }
        
        protected Node copyNode(Node node) {
            // set details node r/o
            DetailsNode detailsNode =
                    (DetailsNode) node.getLookup().lookup(DetailsNode.class);
            if(detailsNode!=null) detailsNode.setReadOnly(true);
            // if already wrapped in RO node return super.copyNode
            ReadOnlySchemaComponentNode roNode = (ReadOnlySchemaComponentNode)node.
                    getLookup().lookup(ReadOnlySchemaComponentNode.class);
            if(roNode!=null) return super.copyNode(roNode);
            // wrap into readonlyscnode
            return new ReadOnlySchemaComponentNode(node);
        }
    }
}
