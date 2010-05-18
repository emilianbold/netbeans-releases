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

package org.netbeans.modules.xml.schema.refactoring;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;

import org.netbeans.modules.xml.schema.model.*;
import org.netbeans.modules.xml.schema.model.visitor.*;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.Referenceable;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;

/**
 *
 * @author Nam Nguyen
 * @author Samaresh
 */
class RenameReferenceVisitor extends DefaultSchemaVisitor {
    
    /**
     * The global component being modified.
     */
    private ReferenceableSchemaComponent global_component = null;
    private String oldName = null;
    
    /**
     * Creates a new instance of RefactorVisitor
     */
    public RenameReferenceVisitor() {
    }
    
    public void rename(Model mod, Set<RefactoringElementImplementation> elements, RenameRefactoring request) {
        if ( elements == null || mod == null) return;
        if (! (mod instanceof SchemaModel)) return;

        Referenceable ref = request.getRefactoringSource().lookup(Referenceable.class);
        this.oldName = request.getContext().lookup(String.class);
        if (! (ref instanceof ReferenceableSchemaComponent)  || oldName == null) {
            return;
        }
        SchemaModel model = (SchemaModel) mod;
        boolean startTransaction = ! model.isIntransaction();
        
         global_component = (ReferenceableSchemaComponent) ref;
         SchemaComponent currentComponent = null;
        try {
            if (startTransaction) {
                model.startTransaction();
            }
          //  Collection<Usage> items = usage.getItems();
            for (RefactoringElementImplementation item : elements) {
                if (item.isEnabled() && item.getLookup().lookup(SchemaComponent.class)!=null) {
                    currentComponent = item.getLookup().lookup(SchemaComponent.class);
                    currentComponent.accept(this);
                }
            }
        } finally {
            if (startTransaction && model.isIntransaction()) {
                model.endTransaction();
            }
        }
    }
    
    private <T extends ReferenceableSchemaComponent> NamedComponentReference<T>
            createReference(Class<T> type, SchemaComponent referencing) {
        if (type.isAssignableFrom(global_component.getClass())) {
            return referencing.createReferenceTo(type.cast(global_component), type);
        } else {
            assert false : type.getName()+" is not assignable from "+global_component.getClass().getName();
            return null;
        }
    }
    
    /**
     * For CommonSimpleRestriction, GlobalReference will be:
     * getBase(), when a GlobalSimpleType is modified.
     */
    public void visit(SimpleTypeRestriction str) {
        NamedComponentReference<GlobalSimpleType> ref = createReference(GlobalSimpleType.class, str);
        if (ref != null) {
            str.setBase(ref);
        }
    }
    
    /**
     * For LocalElement, GlobalReference will be:
     * getType(), when a GlobalType is modified,
     * getRef(), when a GlobalElement is modified.
     */
    public void visit(LocalElement element) {
        NamedComponentReference<GlobalType> ref = createReference(GlobalType.class, element);
        if (ref != null) {
            element.setType(ref);
        }
    }
    
    public void visit(ElementReference element) {
        NamedComponentReference<GlobalElement> ref = createReference(GlobalElement.class, element);
        if (ref != null) {
            element.setRef(ref);
        }
    }
    
    /**
     * For GlobalElement, GlobalReference will be:
     * getType(), when a GlobalType is modified,
     * getSubstitutionGroup(), when a GlobalElement is modified.
     */
    public void visit(GlobalElement element) {
        NamedComponentReference<GlobalType> ref = createReference(GlobalType.class, element);
        if (ref != null) {
            element.setType(ref);
        } else {
            NamedComponentReference<GlobalElement> ref2 = createReference(GlobalElement.class, element);
            if (ref2 != null) {
                element.setSubstitutionGroup(ref2);
            }
        }
    }
    
    /**
     * For LocalAttribute, GlobalReference will be:
     * getType(), when a GlobalSimpleType is modified,
     * getRef(), when a GlobalAttribute is modified.
     */
    public void visit(LocalAttribute attribute) {
        NamedComponentReference<GlobalSimpleType> ref = createReference(GlobalSimpleType.class, attribute);
        if (ref != null) {
            attribute.setType(ref);
        }
    }
    
    /**
     * For LocalAttribute, GlobalReference will be:
     * getType(), when a GlobalSimpleType is modified,
     * getRef(), when a GlobalAttribute is modified.
     */
    public void visit(AttributeReference attribute) {
        NamedComponentReference<GlobalAttribute> ref = createReference(GlobalAttribute.class, attribute);
        if (ref != null) {
            attribute.setRef(ref);
        }
    }
    
    /**
     * For AttributeGroupReference, GlobalReference will be:
     * getGroup(), when a GlobalAttributeGroup is modified.
     */
    public void visit(AttributeGroupReference agr) {
        NamedComponentReference<GlobalAttributeGroup> ref = createReference(GlobalAttributeGroup.class, agr);
        if (ref != null) {
            agr.setGroup(ref);
        }
    }
    
    /**
     * For ComplexContentRestriction, GlobalReference will be:
     * getBase(), when a GlobalComplexType is modified.
     */
    public void visit(ComplexContentRestriction ccr) {
        NamedComponentReference<GlobalComplexType> ref = createReference(GlobalComplexType.class, ccr);
        if (ref != null) {
            ccr.setBase(ref);
        }
    }
    
    /**
     * For SimpleExtension, GlobalReference will be:
     * getBase(), when a GlobalType is modified.
     */
    public void visit(SimpleExtension extension) {
        NamedComponentReference<GlobalType> ref = createReference(GlobalType.class, extension);
        if (ref != null) {
            extension.setBase(ref);
        }
    }
    
    /**
     * For ComplexExtension, GlobalReference will be:
     * getBase(), when a GlobalType is modified.
     */
    public void visit(ComplexExtension extension) {
        NamedComponentReference<GlobalType> ref = createReference(GlobalType.class, extension);
        if (ref != null) {
            extension.setBase(ref);
        }
    }
    
    /**
     * For GroupReference, GlobalReference will be:
     * getRef(), when a GlobalGroup is modified.
     */
    public void visit(GroupReference gr) {
        NamedComponentReference<GlobalGroup> ref = createReference(GlobalGroup.class, gr);
        if (ref != null) {
            gr.setRef(ref);
        }
    }
    
    /**
     * For List, GlobalReference will be:
     * getType(), when a GlobalSimpleType is modified.
     */
    public void visit(List list) {
        NamedComponentReference<GlobalSimpleType> ref = createReference(GlobalSimpleType.class, list);
        if (ref != null) {
            list.setType(ref);
        }
    }
    
    public void visit(Union u) {
        NamedComponentReference<GlobalSimpleType> ref = createReference(GlobalSimpleType.class, u);
        if (ref != null) {
            ArrayList<NamedComponentReference<GlobalSimpleType>> members =
                    new ArrayList(u.getMemberTypes());
            for (int i=0; i<members.size(); i++) {
                if (members.get(i).getRefString().indexOf(oldName) > -1) {
                    members.remove(i);
                    members.add(i, ref);
                    break;
                }
            }
            u.setMemberTypes(members);
        }
    }
}
