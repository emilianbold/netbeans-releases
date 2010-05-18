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

package org.netbeans.modules.xml.wsdl.model.extensions.bpel.impl;

import org.netbeans.modules.xml.wsdl.model.extensions.bpel.BPELExtensibilityComponent;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.CorrelationProperty;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.Documentation;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.PartnerLinkType;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.PropertyAlias;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.Role;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.ComponentUpdater;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;

/**
 * @author Nam Nguyen
 * 
 * changed by
 * @author ads
 */
public class BPELComponentUpdater implements
        BPELExtensibilityComponent.Visitor,
        ComponentUpdater<BPELExtensibilityComponent>, 
        ComponentUpdater.Query<BPELExtensibilityComponent>
{

    private BPELExtensibilityComponent parent;

    private ComponentUpdater.Operation operation;
    
    private int index;
    
    private boolean canAdd;

    /** Creates a new instance of BPELComponentUpdater */
    public BPELComponentUpdater() {
    }

    public boolean canAdd(BPELExtensibilityComponent target, Component child) {
        if (!(child instanceof BPELExtensibilityComponent)) {
            return false;
        }
        update(target, (BPELExtensibilityComponent) child, null);
        return canAdd;
    }

    public void update( BPELExtensibilityComponent target,
            BPELExtensibilityComponent child,
            ComponentUpdater.Operation operation )
    {
        update(target, child, -1, operation);
    }

    public void update( BPELExtensibilityComponent target,
            BPELExtensibilityComponent child, int index,
            ComponentUpdater.Operation operation )
    {
        parent = target;
        this.operation = operation;
        this.index = index;
        child.accept(this);
    }

    public void visit( PropertyAlias c ) {
        // never
    }

    public void visit( CorrelationProperty c ) {
        // never
    }

    public void visit( Role child ) {
        if (parent instanceof PartnerLinkTypeImpl) {

            // Have to use sub-api level calls, not role1/role2 calls.
            // Note: this might cause role2 become role1 after sync if source
            // view
            // has lines of role2 revoved. There supposed to be no role2 if
            // there is
            // no role1 and source editing is not main supported usage, so this
            // is fine.

            PartnerLinkTypeImpl target = (PartnerLinkTypeImpl) parent;
            if (operation == ComponentUpdater.Operation.ADD) {
                target.addRole(child);
            }
            else if (operation == ComponentUpdater.Operation.REMOVE) {
                target.removeRole(child);
            } else {
                canAdd = true;
            }
        }
    }

    public void visit( PartnerLinkType c ) {
        // never
    }

    public void visit( org.netbeans.modules.xml.wsdl.model.extensions.bpel.Query c ) {
        if ( parent instanceof PropertyAliasImpl ){
            PropertyAliasImpl propertyAlias = ( PropertyAliasImpl )parent;
            if (operation == ComponentUpdater.Operation.ADD) {
                /* TODO : this is actually wrong. There could be incorrectly added
                 * second query element via editor. In this case we need
                 * to distinguish position that was used for addition
                 * and either insert element or add to the end......
                 */  
                propertyAlias.setQuery( c );
            } else if (operation == ComponentUpdater.Operation.REMOVE) {
                propertyAlias.removeQuery( c );
            } else {
                canAdd = true;
            }
        }
        
    }

    public void visit(Documentation c) {
        if ( parent instanceof PartnerLinkTypeImpl ){
            PartnerLinkTypeImpl partnerLinkType = ( PartnerLinkTypeImpl )parent;
            if (operation == ComponentUpdater.Operation.ADD) {
                //index is greater than -1, then insert with that index
                if (index > -1)
                    partnerLinkType.insertPartnerLinkTypeDocumentationAt( c, index);
                else 
                    partnerLinkType.addPartnerLinkTypeDocumentation(c);
            }
            else if (operation == ComponentUpdater.Operation.REMOVE) {
                partnerLinkType.removePartnerLinkTypeDocumentation( c );
            } else {
                canAdd = true;
            }
        } else if ( parent instanceof RoleImpl ){
            RoleImpl role = ( RoleImpl )parent;
            if (operation == ComponentUpdater.Operation.ADD) {
                //index is greater than -1, then insert with that index
                if (index > -1) {
                    ((AbstractDocumentComponent)role).insertAtIndex(Role.ROLE_DOCUMENTATION_PROPERTY,
                            c, index);
                } else { 
                    role.addRoleDocumentation( c );
                }
            }
            else if (operation == ComponentUpdater.Operation.REMOVE) {
                role.removeRoleDocumentation( c );
            } else {
                canAdd = true;
            }
        }
        
    }

}
