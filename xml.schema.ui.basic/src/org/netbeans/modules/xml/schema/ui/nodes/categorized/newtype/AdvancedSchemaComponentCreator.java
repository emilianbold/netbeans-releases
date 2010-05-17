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

/*
 * AdvancedSchemaComponentCreator.java
 *
 * Created on April 20, 2006, 5:05 PM
 *
 */

package org.netbeans.modules.xml.schema.ui.nodes.categorized.newtype;

import java.util.Collection;
import java.util.HashSet;
import org.netbeans.modules.xml.schema.model.AnyAttribute;
import org.netbeans.modules.xml.schema.model.Attribute;
import org.netbeans.modules.xml.schema.model.AttributeGroupReference;
import org.netbeans.modules.xml.schema.model.AttributeReference;
import org.netbeans.modules.xml.schema.model.ComplexContent;
import org.netbeans.modules.xml.schema.model.ComplexContentRestriction;
import org.netbeans.modules.xml.schema.model.ComplexExtension;
import org.netbeans.modules.xml.schema.model.ComplexExtensionDefinition;
import org.netbeans.modules.xml.schema.model.ComplexTypeDefinition;
import org.netbeans.modules.xml.schema.model.ElementReference;
import org.netbeans.modules.xml.schema.model.Enumeration;
import org.netbeans.modules.xml.schema.model.Field;
import org.netbeans.modules.xml.schema.model.GlobalAttribute;
import org.netbeans.modules.xml.schema.model.GlobalAttributeGroup;
import org.netbeans.modules.xml.schema.model.GlobalComplexType;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalGroup;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.GroupReference;
import org.netbeans.modules.xml.schema.model.Import;
import org.netbeans.modules.xml.schema.model.Include;
import org.netbeans.modules.xml.schema.model.Redefine;
import org.netbeans.modules.xml.schema.model.Key;
import org.netbeans.modules.xml.schema.model.KeyRef;
import org.netbeans.modules.xml.schema.model.LocalAttribute;
import org.netbeans.modules.xml.schema.model.LocalComplexType;
import org.netbeans.modules.xml.schema.model.LocalElement;
import org.netbeans.modules.xml.schema.model.LocalGroupDefinition;
import org.netbeans.modules.xml.schema.model.LocalSimpleType;
import org.netbeans.modules.xml.schema.model.LocalType;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaComponentReference;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.netbeans.modules.xml.schema.model.Selector;
import org.netbeans.modules.xml.schema.model.Sequence;
import org.netbeans.modules.xml.schema.model.SimpleContent;
import org.netbeans.modules.xml.schema.model.SimpleContentRestriction;
import org.netbeans.modules.xml.schema.model.SimpleExtension;
import org.netbeans.modules.xml.schema.model.SimpleTypeRestriction;
import org.netbeans.modules.xml.schema.model.Unique;
import org.netbeans.modules.xml.schema.ui.nodes.categorized.customizer.AdvancedGlobalAttributeCustomizer;
import org.netbeans.modules.xml.schema.ui.nodes.categorized.customizer.AdvancedGlobalElementCustomizer;
import org.netbeans.modules.xml.schema.ui.nodes.categorized.customizer.AdvancedLocalAttributeCustomizer;
import org.netbeans.modules.xml.schema.ui.nodes.categorized.customizer.AdvancedLocalElementCustomizer;
import org.netbeans.modules.xml.schema.ui.nodes.categorized.customizer.ComplexTypeCustomizer;
import org.netbeans.modules.xml.schema.ui.nodes.categorized.customizer.ImportCustomizer;
import org.netbeans.modules.xml.schema.ui.nodes.categorized.customizer.IncludeCustomizer;
import org.netbeans.modules.xml.schema.ui.nodes.categorized.customizer.RedefineCustomizer;
import org.netbeans.modules.xml.schema.ui.nodes.categorized.customizer.SimpleTypeCustomizer;
import org.netbeans.modules.xml.schema.ui.nodes.categorized.customizer.ReferenceCustomizer;
import org.netbeans.modules.xml.xam.Nameable;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;

/**
 * This is advanced creator which creates templates for some components.
 *
 * @author Ajit Bhate
 */
public class AdvancedSchemaComponentCreator extends SchemaComponentCreator {
    /** Creates a new instance of AdvancedSchemaComponentCreator */
    public AdvancedSchemaComponentCreator() {
    }
    
    public void visit(ElementReference le) {
        super.visit(le);
        if(getOperation() == Operation.SHOW_CUSTOMIZER) {
            setCustomizer(new ReferenceCustomizer<ElementReference>(
                    SchemaComponentReference.create(le),getParent()));
        }
    }
    
    public void visit(AttributeReference la) {
        super.visit(la);
        if(getOperation() == Operation.SHOW_CUSTOMIZER) {
            setCustomizer(new ReferenceCustomizer<AttributeReference>(
                    SchemaComponentReference.create(la),getParent()));
        }
    }
    
    public void visit(GroupReference gr) {
        super.visit(gr);
        if(getOperation() == Operation.SHOW_CUSTOMIZER) {
            setCustomizer(new ReferenceCustomizer<GroupReference>(
                    SchemaComponentReference.create(gr),getParent()));
        }
    }
    
    public void visit(AttributeGroupReference agr) {
        super.visit(agr);
        if(getOperation() == Operation.SHOW_CUSTOMIZER) {
            setCustomizer(new ReferenceCustomizer<AttributeGroupReference>(
                    SchemaComponentReference.create(agr),getParent()));
        }
    }
    
    public void visit(Import imp) {
        super.visit(imp);
        if(getOperation() == Operation.SHOW_CUSTOMIZER) {
            setCustomizer(new ImportCustomizer(imp));
        }
    }
    
    public void visit(Include include) {
        super.visit(include);
        if(getOperation() == Operation.SHOW_CUSTOMIZER) {
            setCustomizer(new IncludeCustomizer(include));
        }
    }
    
    public void visit(Redefine redefine) {
        super.visit(redefine);
        if(getOperation() == Operation.SHOW_CUSTOMIZER) {
            setCustomizer(new RedefineCustomizer(redefine));
        }
    }
    
    public void visit(LocalSimpleType lst) {
        super.visit(lst);
        if(getOperation() == Operation.SHOW_CUSTOMIZER) {
            SchemaComponent parent = getParent();
            SimpleTypeRestriction str = getParent().getModel().getFactory().
                    createSimpleTypeRestriction();
            lst.setDefinition(str);
            setParent(lst);
            visit(str);
            setCustomizer(new SimpleTypeCustomizer<LocalSimpleType>(
                    SchemaComponentReference.create(lst)
                    ,parent, getStringType()));
        }
    }
    
    public void visit(GlobalSimpleType gst) {
        super.visit(gst);
        if(getOperation() == Operation.SHOW_CUSTOMIZER) {
            updateName(gst,"newSimpleType"); //TODO FIX hardcoding
            SchemaComponent parent = getParent();
            SimpleTypeRestriction str = getParent().getModel().getFactory().
                    createSimpleTypeRestriction();
            gst.setDefinition(str);
            setParent(gst);
            visit(str);
            setCustomizer(new SimpleTypeCustomizer<GlobalSimpleType>(
                    SchemaComponentReference.create(gst),
                    parent, getStringType()));
        }
    }
    
    public void visit(SimpleTypeRestriction str) {
        super.visit(str);
        if(getOperation() == Operation.ADD || getOperation() == Operation.SHOW_CUSTOMIZER) {
            // set type to primitive string
            NamedComponentReference<GlobalSimpleType> stringType =
                    createStringTypeReference(str);
            if (stringType != null) {
                str.setBase(stringType);
            }
        }
    }
    
    public void visit(LocalComplexType lct) {
        if(getOperation() == Operation.VERIFY_ADD) {
            // exclusivity of ComplexTypeDefinition
            if(getChild() instanceof ComplexTypeDefinition) {
                if(lct.getDefinition()!=null)
                    setAddAllowed(false);
            }
            // mutual exclusive attributes and Complex/SimpleContents
            if(getChild() instanceof AnyAttribute ||
                    getChild() instanceof Attribute ||
                    getChild() instanceof AttributeGroupReference) {
                if(lct.getDefinition() instanceof ComplexContent ||
                        lct.getDefinition() instanceof SimpleContent) {
                    setAddAllowed(false);
                }
            }
            // exclusivity of AnyAttribute
            if(getChild() instanceof AnyAttribute) {
                if(lct.getAnyAttribute()!=null)
                    setAddAllowed(false);
            }
            setVisitLevels(4);
        }
        super.visit(lct);
        if(getOperation() == Operation.SHOW_CUSTOMIZER) {
            Sequence sequence = getParent().getModel().getFactory().createSequence();
            lct.setDefinition(sequence);
            setCustomizer(new ComplexTypeCustomizer<LocalComplexType>(
                    SchemaComponentReference.create(lct),getParent()));
        }
    }
    
    public void visit(GlobalComplexType gct) {
        if(getOperation() == Operation.VERIFY_ADD) {
            // exclusivity of ComplexTypeDefinition
            if(getChild() instanceof ComplexTypeDefinition) {
                if(gct.getDefinition()!=null)
                    setAddAllowed(false);
            }
            // mutual exclusive attributes and Complex/SimpleContents
            if(getChild() instanceof AnyAttribute ||
                    getChild() instanceof Attribute ||
                    getChild() instanceof AttributeGroupReference) {
                if(gct.getDefinition() instanceof ComplexContent ||
                        gct.getDefinition() instanceof SimpleContent) {
                    setAddAllowed(false);
                }
            }
            // anyAttribute can be 0 or 1
            if(getChild() instanceof AnyAttribute) {
                if(gct.getAnyAttribute()!=null)
                    setAddAllowed(false);
            }
            // we will look for
            //1	complextype
            //2		complexcontent
            //3			extension
            //4				sequence
            setVisitLevels(4);
        }
        super.visit(gct);
        if(getOperation() == Operation.SHOW_CUSTOMIZER) {
            updateName(gct,"newComplexType"); //TODO FIX hardcoding
            Sequence sequence = getParent().getModel().getFactory().createSequence();
            gct.setDefinition(sequence);
            setCustomizer(new ComplexTypeCustomizer<GlobalComplexType>(
                    SchemaComponentReference.create(gct),getParent()));
        }
    }
    
    public void visit(ComplexContentRestriction ccr) {
        if(getOperation() == Operation.VERIFY_ADD) {
            // exclusivity of ComplexTypeDefinition
            if(getChild() instanceof ComplexTypeDefinition) {
                if(ccr.getDefinition()!=null)
                    setAddAllowed(false);
            }
            // anyAttribute can be 0 or 1
            if(getChild() instanceof AnyAttribute) {
                if(ccr.getAnyAttribute()!=null)
                    setAddAllowed(false);
            }
        }
        super.visit(ccr);
    }
    
    public void visit(ComplexExtension ce) {
        if(getOperation() == Operation.VERIFY_ADD) {
            // exclusivity of ComplexExtensionDefinition
            if(getChild() instanceof ComplexExtensionDefinition) {
                if(ce.getLocalDefinition()!=null)
                    setAddAllowed(false);
            }
            // anyAttribute can be 0 or 1
            if(getChild() instanceof AnyAttribute) {
                if(ce.getAnyAttribute()!=null)
                    setAddAllowed(false);
            }
        }
        super.visit(ce);
    }
    
    public void visit(SimpleContentRestriction scr) {
        if(getOperation() == Operation.VERIFY_ADD) {
            // anyAttribute can be 0 or 1
            if(getChild() instanceof AnyAttribute) {
                if(scr.getAnyAttribute()!=null)
                    setAddAllowed(false);
            }
        }
        super.visit(scr);
    }
    
    public void visit(SimpleExtension se) {
        if(getOperation() == Operation.VERIFY_ADD) {
            // anyAttribute can be 0 or 1
            if(getChild() instanceof AnyAttribute) {
                if(se.getAnyAttribute()!=null)
                    setAddAllowed(false);
            }
        }
        super.visit(se);
    }
    
    public void visit(GlobalAttributeGroup gag) {
        if(getOperation() == Operation.VERIFY_ADD) {
            // anyAttribute can be 0 or 1
            if(getChild() instanceof AnyAttribute) {
                if(gag.getAnyAttribute()!=null)
                    setAddAllowed(false);
            }
        }
        super.visit(gag);
        if(getOperation() == Operation.ADD) {
            updateName(gag,"newAttributeGroup"); //TODO FIX hardcoding
        }
    }
    
    public void visit(LocalElement le) {
        if(getOperation() == Operation.VERIFY_ADD) {
            // exclusivity of type definition
            if(getChild() instanceof LocalType) {
                if(le.getType()!=null||le.getInlineType()!=null)
                    setAddAllowed(false);
            }
            // we will look for
            //1	element
            //2		complextype
            //3			complexcontent
            //4				extension
            //5					sequence
            setVisitLevels(5);
        }
        super.visit(le);
        if(getOperation() == Operation.SHOW_CUSTOMIZER) {
            updateName(le,"newElement"); //TODO FIX hardcoding
            LocalComplexType lct = getParent().getModel().getFactory().createLocalComplexType();
            le.setInlineType(lct);
            Sequence sequence = getParent().getModel().getFactory().createSequence();
            lct.setDefinition(sequence);
            setCustomizer(new AdvancedLocalElementCustomizer(
                    SchemaComponentReference.create(le), getParent()));
        }
    }
    
    public void visit(GlobalElement ge) {
        if(getOperation() == Operation.VERIFY_ADD) {
            // exclusivity of type definition
            if(getChild() instanceof LocalType) {
                if(ge.getType()!=null||ge.getInlineType()!=null)
                    setAddAllowed(false);
            }
            setVisitLevels(5);
        }
        super.visit(ge);
        if(getOperation() == Operation.SHOW_CUSTOMIZER) {
            updateName(ge,"newElement"); //TODO FIX hardcoding
            LocalComplexType lct = getParent().getModel().getFactory().createLocalComplexType();
            ge.setInlineType(lct);
            Sequence sequence = getParent().getModel().getFactory().createSequence();
            lct.setDefinition(sequence);
            setCustomizer(new AdvancedGlobalElementCustomizer(
                    SchemaComponentReference.create(ge), getParent()));
        }
    }
    
    public void visit(GlobalAttribute ga) {
        if(getOperation() == Operation.VERIFY_ADD) {
            // exclusivity of type definition
            if(getChild() instanceof LocalType) {
                if(ga.getType()!=null||ga.getInlineType()!=null)
                    setAddAllowed(false);
            }
        }
        super.visit(ga);
        if(getOperation() == Operation.SHOW_CUSTOMIZER) {
            updateName(ga,"newAttribute"); //TODO FIX hardcoding
            // set type to primitive string
            NamedComponentReference<GlobalSimpleType> stringType =
                    createStringTypeReference(ga);
            if (stringType != null) {
                ga.setType(stringType);
            }
            setCustomizer(new AdvancedGlobalAttributeCustomizer(
                    SchemaComponentReference.create(ga), getParent(), getStringType()));
        }
    }
    
    public void visit(LocalAttribute la) {
        if(getOperation() == Operation.VERIFY_ADD) {
            // exclusivity of type definition
            if(getChild() instanceof LocalType) {
                if(la.getType()!=null||la.getInlineType()!=null)
                    setAddAllowed(false);
            }
        }
        super.visit(la);
        if(getOperation() == Operation.SHOW_CUSTOMIZER) {
            updateName(la,"newAttribute"); //TODO FIX hardcoding
            // set type to primitive string
            NamedComponentReference<GlobalSimpleType> stringType =
                    createStringTypeReference(la);
            if (stringType != null) {
                la.setType(stringType);
            }
            setCustomizer(new AdvancedLocalAttributeCustomizer(
                    SchemaComponentReference.create(la), getParent(), getStringType()));
        }
    }
    
    public void visit(GlobalGroup gd) {
        if(getOperation() == Operation.VERIFY_ADD) {
            // exclusivity of group definition
            if(getChild() instanceof LocalGroupDefinition) {
                if(gd.getDefinition()!=null)
                    setAddAllowed(false);
            }
        }
        super.visit(gd);
        if(getOperation() == Operation.ADD) {
            updateName(gd,"newGroup"); //TODO FIX hardcoding
            Sequence s = getParent().getModel().getFactory().createSequence();
            setParent(gd);
            visit(s);
        }
    }
    
    public void visit(Unique u) {
        if(getOperation() == Operation.VERIFY_ADD) {
            // Selector can be 1
            if(getChild() instanceof Selector) {
                if(u.getSelector()!=null)
                    setAddAllowed(false);
            }
        }
        super.visit(u);
        if(getOperation() == Operation.ADD) {
            updateName(u,"newUnique");//TODO FIX hardcoding
            setParent(u);
            Selector s = getParent().getModel().getFactory().createSelector();
            visit(s);
            Field f = getParent().getModel().getFactory().createField();
            visit(f);
        }
    }
    
    public void visit(Key key) {
        if(getOperation() == Operation.VERIFY_ADD) {
            // Selector can be 1
            if(getChild() instanceof Selector) {
                if(key.getSelector()!=null)
                    setAddAllowed(false);
            }
        }
        super.visit(key);
        if(getOperation() == Operation.ADD) {
            updateName(key,"newKey");//TODO FIX hardcoding
            setParent(key);
            Selector s = getParent().getModel().getFactory().createSelector();
            visit(s);
            Field f = getParent().getModel().getFactory().createField();
            visit(f);
        }
    }
    
    public void visit(KeyRef kr) {
        if(getOperation() == Operation.VERIFY_ADD) {
            // Selector can be 1
            if(getChild() instanceof Selector) {
                if(kr.getSelector()!=null)
                    setAddAllowed(false);
            }
        }
        super.visit(kr);
        if(getOperation() == Operation.ADD) {
            updateName(kr,"newKeyRef");//TODO FIX hardcoding
            setParent(kr);
            Selector s = getParent().getModel().getFactory().createSelector();
            visit(s);
            Field f = getParent().getModel().getFactory().createField();
            visit(f);
        }
    }
    
    public void visit(Enumeration e) {
        super.visit(e);
        if(getOperation() == Operation.ADD) {
            e.setValue("");
        }
    }
    
    public void visit(Selector s) {
        super.visit(s);
        if(getOperation() == Operation.ADD) {
            s.setXPath("");
        }
    }
    
    public void visit(Field f) {
        super.visit(f);
        if(getOperation() == Operation.ADD) {
            f.setXPath("");
        }
    }
    
    private void updateName(final Nameable<SchemaComponent> component,
            final String preferedName) {
        String name = preferedName;
        HashSet<String> nameSet = new HashSet<String>();
        String takenName;
        for(SchemaComponent child :getParent().getChildren(
                ((SchemaComponent)component).getComponentType())) {
            takenName = ((Nameable)child).getName();
            if(takenName!=null) nameSet.add(takenName);
        }
        int cnt = 1;
        if(nameSet.size() <Integer.MAX_VALUE) {
            while(nameSet.contains(name)) {
                name = preferedName.concat(""+cnt++);
            }
        }
        component.setName(name);
    }
    
    // creates primitive string global reference
    private NamedComponentReference<GlobalSimpleType>
            createStringTypeReference(SchemaComponent component) {
        GlobalSimpleType stringType = getStringType();
        if(stringType!=null)
            return getParent().getModel().getFactory().createGlobalReference
                    (stringType, GlobalSimpleType.class, component);
        return null;
    }
    
    private GlobalSimpleType getStringType() {
        Collection<GlobalSimpleType> types = SchemaModelFactory.getDefault().
                getPrimitiveTypesModel().getSchema().getSimpleTypes();
        for (GlobalSimpleType type : types) {
            if (type.getName().equals("string")) {
                return type;
            }
        }
        return null;
    }
}
