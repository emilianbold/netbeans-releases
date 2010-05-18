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
import org.netbeans.modules.refactoring.api.ui.RefactoringActionsFactory;
//import org.netbeans.modules.xml.refactoring.actions.RefactorAction;
import org.netbeans.modules.xml.schema.ui.nodes.SchemaComponentNode;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Property;
import org.openide.nodes.Node.PropertySet;
import org.openide.nodes.PropertySupport;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.actions.NodeAction;
import org.openide.util.actions.SystemAction;
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
        return false;
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
            String actionName =null;
            if(a != null) {
               actionName = (String)a.getValue(Action.NAME);
            }
            if(actionName != null && actionName.equals("Refactor") ) {
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
