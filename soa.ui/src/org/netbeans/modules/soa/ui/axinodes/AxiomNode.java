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
package org.netbeans.modules.soa.ui.axinodes;

import java.awt.Image;
import org.netbeans.modules.soa.ui.axinodes.NodeType.BadgeModificator;
import org.netbeans.modules.soa.ui.nodes.InstanceRef;
import org.netbeans.modules.soa.ui.nodes.NodeTypeHolder;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

/**
 * This class represents the base class for BPEL related nodes.
 * <p>
 * The PropertyNodeFactory class is implied that the derived nodes has
 * at least one constructor with the specific parameters.
 *
 * @author nk160297
 */
public abstract class AxiomNode<T extends AXIComponent>
        extends AbstractNode 
        implements InstanceRef<T>, NodeTypeHolder<NodeType>
{
    //
    private Object reference;
    
    public AxiomNode(T ref, Lookup lookup) {
        this(ref, Children.LEAF, lookup);
    }
    
    public AxiomNode(T ref, Children children, Lookup lookup) {
        super(children, lookup);
        setReference(ref);
    }
    
    /**
     * The reference to an object which the node represents.
     */
    public T getReference() {
        T ref = (T)reference;
        return ref;
    }
    
    public Object getAlternativeReference() {
        return null;
    }
    
    public Image getIcon(int type) {
        return getNodeType().getImage(BadgeModificator.SINGLE);
    }
    
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }
    
    protected void setReference(T newRef){
        this.reference = newRef;
    }
    
    public abstract NodeType getNodeType();
    
    public String getDisplayName() {
        String instanceName = getName();
        return ((instanceName == null || instanceName.length() == 0)
        ? "" : instanceName + " ") +
                "[" + getNodeType().getDisplayName() + "]"; // NOI18N
    }
    
    public synchronized String getShortDescription() {
        return getDisplayName();
    }
    
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            return true;
        }
        
        //
        if (obj instanceof AxiomNode) {
            return this.getNodeType().equals(((AxiomNode) obj).getNodeType()) &&
                    this.reference.equals(((AxiomNode) obj).reference);
        }
        //
        return false;
    }
    
//    /**
//     * Looks for the Properties Set by the Group enum.
//     * If the group isn't
//     */
//    protected Sheet.Set getPropertySet(
//            Sheet sheet, Constants.PropertiesGroups group) {
//        Sheet.Set propSet = sheet.get(group.getDisplayName());
//        if (propSet == null) {
//            propSet = new Sheet.Set();
//            propSet.setName(group.getDisplayName());
//            sheet.put(propSet);
//        }
//        //
//        return propSet;
//    }
    
    public String getHelpId() {
        return null;
    }
    
    public HelpCtx getHelpCtx() {
        return getHelpId() == null ? null : new HelpCtx(getHelpId());
    }
    
}
