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
package org.netbeans.modules.xml.xpath.ext.schema;

import java.util.List;
import org.netbeans.modules.xml.schema.model.All;
import org.netbeans.modules.xml.schema.model.AnyAttribute;
import org.netbeans.modules.xml.schema.model.AnyElement;
import org.netbeans.modules.xml.schema.model.Attribute;
import org.netbeans.modules.xml.schema.model.AttributeGroupReference;
import org.netbeans.modules.xml.schema.model.AttributeReference;
import org.netbeans.modules.xml.schema.model.Choice;
import org.netbeans.modules.xml.schema.model.ComplexContent;
import org.netbeans.modules.xml.schema.model.ComplexContentRestriction;
import org.netbeans.modules.xml.schema.model.ComplexExtension;
import org.netbeans.modules.xml.schema.model.ElementReference;
import org.netbeans.modules.xml.schema.model.GlobalAttribute;
import org.netbeans.modules.xml.schema.model.GlobalAttributeGroup;
import org.netbeans.modules.xml.schema.model.GlobalComplexType;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalGroup;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.GroupReference;
import org.netbeans.modules.xml.schema.model.LocalAttribute;
import org.netbeans.modules.xml.schema.model.LocalComplexType;
import org.netbeans.modules.xml.schema.model.LocalElement;
import org.netbeans.modules.xml.schema.model.Redefine;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.Sequence;
import org.netbeans.modules.xml.schema.model.SimpleContent;
import org.netbeans.modules.xml.schema.model.SimpleContentRestriction;
import org.netbeans.modules.xml.schema.model.SimpleExtension;
import org.netbeans.modules.xml.schema.model.Union;
import org.netbeans.modules.xml.schema.model.visitor.DefaultSchemaVisitor;
import org.netbeans.modules.xml.xam.Named;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;

/**
 * This schema visitor is inteneded to look for a children elements or attributes 
 * It looks only children at the next lavel. 
 * 
 * @author nk160297
 */
public abstract class AbstractSchemaSearchVisitor extends DefaultSchemaVisitor {

    // prevent processing for xsd:any attributes.
    private boolean skipXsdAnyAttribute = false;

    // prevent processing for all attributes.
    private boolean skipAttributes = false;
    
    
    // Indicates if it necessary to look the global objects only.
    // It is used when the schema is the parent of searching.
    protected boolean lookGlobalOnly = false; 
   
    public AbstractSchemaSearchVisitor() {
    }

    /**
     * This method checks if the current schema component satisfies 
     * the search conditions.
     */
    protected abstract void checkComponent(SchemaComponent sc);

    // ----------------------------------------------
    @Override
    public void visit(LocalAttribute la) {
        if (!skipAttributes) {
            checkComponent(la);
        }
    }

    @Override
    public void visit(GlobalAttribute ga) {
        if (!skipAttributes) {
            checkComponent(ga);
        }
    }

    @Override
    public void visit(LocalElement le) {
        checkComponent(le);
    }

    @Override
    public void visit(GlobalElement ge) {
        checkComponent(ge);
    }

    // ----------------------------------------------
    @Override
    public void visit(AnyElement ae) {
        checkComponent(ae);
    }

    @Override
    public void visit(AnyAttribute aa) {
        if (!skipAttributes && !skipXsdAnyAttribute) {
            checkComponent(aa);
        }
    }

    // --------------- References ------------------
    @Override
    public void visit(ElementReference er) {
        //
        NamedComponentReference<GlobalElement> geRef = er.getRef();
        if (geRef != null) {
            GlobalElement ge = geRef.get();

            if (ge != null) {
                visit(ge);
            }
        }
    }

    @Override
    public void visit(AttributeReference ar) {
        if (skipAttributes) {
            return;
        }
        //
        NamedComponentReference<GlobalAttribute> gaRef = ar.getRef();

        if (gaRef != null) {
            GlobalAttribute ga = gaRef.get();
            if (ga != null) {
                visit(ga);
            }
        }
    }

    @Override
    public void visit(AttributeGroupReference agr) {
        if (skipAttributes) {
            return;
        }
        //
        NamedComponentReference<GlobalAttributeGroup> gagRef = agr.getGroup();
        if (gagRef != null) {
            GlobalAttributeGroup gag = gagRef.get();
            if (gag != null) {
                visit(gag);
            }
        }
    }

    @Override
    public void visit(GroupReference gr) {
        NamedComponentReference<GlobalGroup> ggRef = gr.getRef();

        if (ggRef != null) {
            GlobalGroup gg = ggRef.get();
            if (gg != null) {
                visit(gg);
            }
        }
    }

    // --------------- Visit containers -------------
    @Override
    public void visit(Schema s) {
        visitChildren(s);
    }

    @Override
    public void visit(All a) {
        visitChildren(a);
    }

    @Override
    public void visit(GlobalAttributeGroup gag) {
        if (!skipAttributes) {
            visitChildren(gag);
        }
    }

    @Override
    public void visit(Choice c) {
        visitChildren(c);
    }

    @Override
    public void visit(SimpleContent sc) {
        visitChildren(sc);
    }

    @Override
    public void visit(ComplexContent cc) {
        visitChildren(cc);
    }

    @Override
    public void visit(SimpleExtension se) {
        NamedComponentReference<GlobalType> gtRef = se.getBase();
        if (gtRef != null) {
            GlobalType gt = gtRef.get();
            if (gt != null) {
                visitChildren(gt);
            }
        }
        //
        visitChildren(se);
    }

    @Override
    public void visit(ComplexExtension ce) {
        List<AnyAttribute> myAnyAttrList = ce.getChildren(AnyAttribute.class);
        //
        boolean prevValue = skipXsdAnyAttribute;
        try {
            if (!myAnyAttrList.isEmpty()) {
                skipXsdAnyAttribute = true;
            }
            NamedComponentReference<GlobalType> gtRef = ce.getBase();
            if (gtRef != null) {
                GlobalType gt = gtRef.get();
                if (gt != null) {
                    visitChildren(gt);
                }
            }
        } finally {
            skipXsdAnyAttribute = prevValue;
        }
        //
        visitChildren(ce);
    }

   @Override
   public void visit(GlobalComplexType gct) {
       if (!lookGlobalOnly) {
           visitChildren(gct);
       }
   }

   @Override
   public void visit(LocalComplexType lct) {
       if (!lookGlobalOnly) {
           visitChildren(lct);
       }
   }

   @Override
   public void visit(GlobalGroup gg) {
       if (!lookGlobalOnly) {
           visitChildren(gg);
       }
   } 

    @Override
    public void visit(Redefine r) {
        visitChildren(r);
    }

    @Override
    public void visit(ComplexContentRestriction ccr) {
        //
        // Nested attributes are collected separately.
        boolean prevSkipAttrValue = skipAttributes;
        try {
            skipAttributes = true;
            visitChildren(ccr);
        } finally {
            skipAttributes = prevSkipAttrValue;
        }
        //
        // Collects local attributes only!
        CollectLocalAttributesVisitor localAttrSearch = 
                new CollectLocalAttributesVisitor(skipXsdAnyAttribute);
        localAttrSearch.collectFrom(ccr);
        List<Attribute> localAttrList = localAttrSearch.getAttributes();
        AnyAttribute localAnyAttr = localAttrSearch.getAnyAttribute();
        //
        for (Attribute localAttr : localAttrList) {
            localAttr.accept(this);
            // checkComponent(localAttr);
        }
        //
        boolean prevSkipAnyValue = skipXsdAnyAttribute;
        try {
            if (localAnyAttr != null) {
                skipXsdAnyAttribute = true;
            }
            //
            // According to the restriction rules, only the attributes are derived 
            // automatically. Other entities has to be copied from base type. 
            NamedComponentReference<GlobalComplexType> baseTypeRef = ccr.getBase();
            if (baseTypeRef != null) {
                GlobalComplexType baseType = baseTypeRef.get();
                if (baseType != null) {
                    CollectInheritedAttributesVisitor attrSearch =
                            new CollectInheritedAttributesVisitor(skipXsdAnyAttribute);
                    attrSearch.collectFrom(baseType);
                    //
                    // The folloing list contains all attributes derived from 
                    // the restriction's base type
                    List<Attribute> inheritedAttrList = attrSearch.getAttributes();
                    //
                    // Now it's necessary to merge derived attributes with local ones.
                    if (inheritedAttrList != null && localAttrList != null) {
                        for (Attribute attr : inheritedAttrList) {
                            //
                            // Add a derived attribute if it isn't overloaded
                            Attribute foundSame = getSameNameAttribute(
                                    localAttrList, attr);
                            if (foundSame == null) {
                                attr.accept(this);
                            }
                        }
                    }
                    //
                    // Process xsd:any attribute if specified. 
                    if (!skipXsdAnyAttribute) {
                        AnyAttribute anyAttr = attrSearch.getAnyAttribute();
                        if (anyAttr != null) {
                            anyAttr.accept(this);
                        }
                    }
                }
            }
        } finally {
            skipXsdAnyAttribute = prevSkipAnyValue;
        }
    }

    @Override
    public void visit(SimpleContentRestriction scr) {
        //
        // Nested attributes are collected separately.
        boolean prevSkipAttrValue = skipAttributes;
        try {
            skipAttributes = true;
            visitChildren(scr);
        } finally {
            skipAttributes = prevSkipAttrValue;
        }
        //
        // Collects local attributes only!
        CollectLocalAttributesVisitor localAttrSearch = 
                new CollectLocalAttributesVisitor(skipXsdAnyAttribute);
        localAttrSearch.collectFrom(scr);
        List<Attribute> localAttrList = localAttrSearch.getAttributes();
        AnyAttribute localAnyAttr = localAttrSearch.getAnyAttribute();
        //
        for (Attribute localAttr : localAttrList) {
            localAttr.accept(this);
            // checkComponent(localAttr);
        }
        //
        boolean prevSkipAnyValue = skipXsdAnyAttribute;
        try {
            if (localAnyAttr != null) {
                skipXsdAnyAttribute = true;
            }
            //
            // According to the restriction rules, only the attributes are derived 
            // automatically. Other entities has to be copied from base type. 
            NamedComponentReference<GlobalType> baseTypeRef = scr.getBase();
            if (baseTypeRef != null) {
                GlobalType baseType = baseTypeRef.get();
                if (baseType != null) {
                    CollectInheritedAttributesVisitor attrSearch =
                            new CollectInheritedAttributesVisitor(skipXsdAnyAttribute);
                    attrSearch.collectFrom(baseType);
                    //
                    // The folloing list contains all attributes derived from 
                    // the restriction's base type
                    List<Attribute> inheritedAttrList = attrSearch.getAttributes();
                    //
                    // Now it's necessary to merge derived attributes with local ones.
                    if (inheritedAttrList != null && localAttrList != null) {
                        for (Attribute attr : inheritedAttrList) {
                            //
                            // Add a derived attribute if it isn't overloaded
                            Attribute foundSame = getSameNameAttribute(
                                    localAttrList, attr);
                            if (foundSame == null) {
                                attr.accept(this);
                            }
                        }
                    }
                    //
                    // Process xsd:any attribute if specified. 
                    if (!skipXsdAnyAttribute) {
                        AnyAttribute anyAttr = attrSearch.getAnyAttribute();
                        if (anyAttr != null) {
                            anyAttr.accept(this);
                        }
                    }
                }
            }
        } finally {
            skipXsdAnyAttribute = prevSkipAnyValue;
        }
    }

    @Override
    public void visit(Sequence s) {
        visitChildren(s);
    }

    @Override
    public void visit(Union u) {
        visitChildren(u);
    }

    // ----------------------------------------------
    protected void visitChildren(SchemaComponent sc) {
        for (SchemaComponent child : sc.getChildren()) {
            child.accept(this);
        }
    }

    //-----------------------------------------------

    protected String fastGetRefName(NamedComponentReference ref) {
        if (ref == null) {
            return null;
        }
        String refString = ref.getRefString();
        String[] splitRefString = refString.split(":", 2);
        String result = null;
        //
        if (splitRefString.length == 1) {
            result = splitRefString[0];
        } else if (splitRefString.length == 2) {
            result = splitRefString[1];
        }
        return result;
    }
    
    /**
     * Looks for an attribute in the attrList, which name and namespace are 
     * the same as for specified attribute.
     * 
     * TODO: This method can work incorrectly if the attribute is located 
     * in a schema, which doesn't have targetNamespace specified. 
     * The targetNamespace can be absent in case of inclusion a schema to another 
     * schema. The method getEffectiveNamespace() returns null in such case. 
     * If the case only the name of attribute is compared and it can come 
     * to incorrect results! 
     * 
     * @param attrList
     * @param attribute
     * @return 
     */
    protected Attribute getSameNameAttribute(List<Attribute> attrList, 
            Attribute attribute) {
        //
        String attrName = ((Named)attribute).getName();
        String namespace = attribute.getModel().getEffectiveNamespace(attribute);
        //
        for (Attribute attr : attrList) {
            if (attr instanceof Named) {
                String name = ((Named)attr).getName();
                if (attrName.equals(name)) {
                    // Names are the same
                    // Try compare namespaces
                    if (namespace == null) {
                        return attr;
                    } else {
                        String ns = attr.getModel().getEffectiveNamespace(attr);
                        if (ns != null && namespace.equals(ns)) {
                            return attr;
                        }
                    }
                }
            }
        }
        //
        return null;
    }
}
