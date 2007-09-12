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
