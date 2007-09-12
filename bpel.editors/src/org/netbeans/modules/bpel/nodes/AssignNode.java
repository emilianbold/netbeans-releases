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
package org.netbeans.modules.bpel.nodes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.bpel.model.api.Assign;
import org.netbeans.modules.bpel.model.api.AssignChild;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.Copy;
import org.netbeans.modules.bpel.nodes.dnd.BpelEntityPasteType;
import org.netbeans.modules.bpel.nodes.dnd.CopyEntityPasteType;
import org.netbeans.modules.bpel.properties.Constants;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.model.api.NamedElement;
import org.netbeans.modules.bpel.model.api.events.ChangeEvent;
import org.netbeans.modules.bpel.model.api.events.EntityInsertEvent;
import org.netbeans.modules.bpel.model.api.events.EntityRemoveEvent;
import org.netbeans.modules.bpel.properties.props.PropertyUtils;
import org.openide.nodes.Sheet;
import static org.netbeans.modules.bpel.properties.PropertyType.*;
import org.netbeans.modules.bpel.nodes.actions.ActionType;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Property;
import org.openide.util.Lookup;

/**
 *
 * @author nk160297
 */
public class AssignNode extends BpelNode<Assign> {
    
    public AssignNode(Assign reference, Children children, Lookup lookup) {
        super(reference, children, lookup);
    }
    
    public AssignNode(Assign reference, Lookup lookup) {
        super(reference, new MyChildren(reference, lookup), lookup);
    }
    
    public NodeType getNodeType() {
        return NodeType.ASSIGN;
    }
    
    public String getHelpId() {
        return getNodeType().getHelpId();
    }
    
    protected boolean isDropNodeInstanceSupported(BpelNode childNode) {
        return childNode.getNodeType().equals(NodeType.COPY);
    }
    
    public List<BpelEntityPasteType> createSupportedPasteTypes(BpelNode childNode) {
        if (childNode==null) {
            return Collections.EMPTY_LIST;
        }
        List<BpelEntityPasteType> supportedPTs = new ArrayList<BpelEntityPasteType>();
        if (!(childNode.getNodeType().equals(NodeType.COPY))) {
            return supportedPTs;
        }
        
        Assign assign = getReference();
        BpelEntity childRefObj = (BpelEntity)childNode.getReference();
        supportedPTs.add(new CopyEntityPasteType(assign, (Copy)childRefObj));
        
        return supportedPTs;
    }

    protected Sheet createSheet() {
        Sheet sheet = super.createSheet();
        if (getReference() == null) {
            // The related object has been removed!
            return sheet;
        }
        //
        Sheet.Set mainPropertySet =
                getPropertySet(sheet, Constants.PropertiesGroups.MAIN_SET);
        //
        PropertyUtils.registerAttributeProperty(this, mainPropertySet,
                NamedElement.NAME, NAME, "getName", "setName", null); // NOI18N
        //
        Property prop = PropertyUtils.registerProperty(this, mainPropertySet,
                ASSIGNMENT_COUNT, "sizeOfAssignChildren", null);
        prop.setValue("suppressCustomEditor", Boolean.TRUE); // NOI18N
        //
        return sheet;
    }
    
    public void reload() {
        MyChildren children = (MyChildren)getChildren();
        children.reloadFrom(getReference());
    }
    
    private static class MyChildren extends Children.Keys {
        
        private Lookup lookup;
        
        public MyChildren(Assign assign, Lookup lookup) {
            super();
            this.lookup = lookup;
            reloadFrom(assign);
        }
        
        protected Node[] createNodes(Object key) {
            assert key instanceof AssignChild;
            //
            if (!(key instanceof Copy)) {
                return null;
            }
            //
            return new Node[] {new CopyNode((Copy)key, lookup)};
        }
        
        public void reloadFrom(Assign assign)  {
            if (assign == null) {
                return;
            }
            
            AssignChild[] assignChildArr = assign.getAssignChildren();
            if (assignChildArr != null) {
                setKeys(assignChildArr);
            } else {
                setKeys(new Copy[0]);
            }
        }
    }

    protected ActionType[] getActionsArray() {
        return new ActionType[] {
            ActionType.GO_TO_SOURCE,
            ActionType.GO_TO_DIAGRAMM,
            ActionType.SEPARATOR,
            ActionType.WRAP,
            ActionType.SEPARATOR,
            ActionType.MOVE_UP,
            ActionType.MOVE_DOWN,
            ActionType.SEPARATOR,
            ActionType.TOGGLE_BREAKPOINT,
            ActionType.SEPARATOR,
            ActionType.REMOVE,
            ActionType.SEPARATOR,
            ActionType.SHOW_BPEL_MAPPER,
            ActionType.SEPARATOR,
            ActionType.PROPERTIES
        };
    }
    
    protected void updateComplexProperties(ChangeEvent event) {
        if (event instanceof EntityInsertEvent) {
            BpelEntity parentEvent = event.getParent();
            if (parentEvent != null && parentEvent.equals(this.getReference())) {
                BpelEntity newEntity = ((EntityInsertEvent)event).getValue();
                if (newEntity instanceof Copy) {
                    updateProperty(ASSIGNMENT_COUNT);
                }
            }
        } else if (event instanceof EntityRemoveEvent) {
            BpelEntity parentEvent = event.getParent();
            if (parentEvent != null && parentEvent.equals(this.getReference())) {
                BpelEntity oldEntity = ((EntityRemoveEvent)event).getOldValue();
                if (oldEntity instanceof Copy) {
                    updateProperty(ASSIGNMENT_COUNT);
                }
            }
        }
    }
}
