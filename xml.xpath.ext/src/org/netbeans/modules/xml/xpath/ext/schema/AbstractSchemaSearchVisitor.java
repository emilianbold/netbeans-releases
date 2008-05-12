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

    private boolean skipAnyAttribute = false;

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
        checkComponent(la);
    }

    @Override
    public void visit(GlobalAttribute ga) {
        checkComponent(ga);
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
        if (!skipAnyAttribute) {
            checkComponent(aa);
        }
    }

    // --------------- References ------------------
    @Override
    public void visit(ElementReference er) {
        // # 105159, #130053
        if (!isXdmDomUsed(er)) {
            checkComponent(er);
        }
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
        // # 105159, #130053
        if (!isXdmDomUsed(ar)) {
            checkComponent(ar);
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
        visitChildren(gag);
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
        boolean prevValue = skipAnyAttribute;
        try {
            if (!myAnyAttrList.isEmpty()) {
                skipAnyAttribute = true;
            }
            NamedComponentReference<GlobalType> gtRef = ce.getBase();
            if (gtRef != null) {
                GlobalType gt = gtRef.get();
                if (gt != null) {
                    visitChildren(gt);
                }
            }
        } finally {
            skipAnyAttribute = prevValue;
        }
        //
        visitChildren(ce);
    }

    @Override
    public void visit(GlobalComplexType gct) {
        visitChildren(gct);
    }

    @Override
    public void visit(LocalComplexType lct) {
        visitChildren(lct);
    }

    @Override
    public void visit(GlobalGroup gg) {
        visitChildren(gg);
    }

    @Override
    public void visit(Redefine r) {
        visitChildren(r);
    }

    @Override
    public void visit(ComplexContentRestriction ccr) {
        visitChildren(ccr);
        //
        List<AnyAttribute> myAnyAttrList = ccr.getChildren(AnyAttribute.class);
        //
        boolean prevValue = skipAnyAttribute;
        try {
            if (!myAnyAttrList.isEmpty()) {
                skipAnyAttribute = true;
            }
            //
            // According to the restriction rules, only the attributes are derived 
            // automatically. Other entities has to be copied from base type. 
            NamedComponentReference<GlobalComplexType> baseTypeRef = ccr.getBase();
            if (baseTypeRef != null) {
                GlobalComplexType baseType = baseTypeRef.get();
                if (baseType != null) {
                    CollectAttributesVisitor attrSearch =
                            new CollectAttributesVisitor(myAnyAttrList.isEmpty());
                    attrSearch.collectFrom(baseType);
                    //
                    // Process normal attributes.
                    List<Attribute> myAttrList = ccr.getChildren(Attribute.class);
                    //
                    List<Attribute> attrList = attrSearch.getAttributes();
                    if (attrList != null) {
                        for (Attribute attr : attrList) {
                            if (!contains(attrList, attr)) {
                                checkComponent(attr);
                            }
                        }
                    }
                    //
                    // Process any attribute if specified. 
                    if (!skipAnyAttribute) {
                        AnyAttribute anyAttr = attrSearch.getAnyAttribute();
                        if (anyAttr != null) {
                            checkComponent(anyAttr);
                        }
                    }
                }
            }
        } finally {
            skipAnyAttribute = prevValue;
        }
    }

    @Override
    public void visit(SimpleContentRestriction scr) {
        visitChildren(scr);
        //
        List<AnyAttribute> myAnyAttrList = scr.getChildren(AnyAttribute.class);
        //
        boolean prevValue = skipAnyAttribute;
        try {
            if (!myAnyAttrList.isEmpty()) {
                skipAnyAttribute = true;
            }
            //
            // According to the restriction rules, only the attributes are derived 
            // automatically. Other entities has to be copied from base type. 
            NamedComponentReference<GlobalType> baseTypeRef = scr.getBase();
            if (baseTypeRef != null) {
                GlobalType baseType = baseTypeRef.get();
                if (baseType != null) {
                    CollectAttributesVisitor attrSearch =
                            new CollectAttributesVisitor(myAnyAttrList.isEmpty());
                    attrSearch.collectFrom(baseType);
                    //
                    // Process normal attributes.
                    List<Attribute> attrList = attrSearch.getAttributes();
                    if (attrList != null) {
                        for (Attribute attr : attrList) {
                            checkComponent(attr);
                        }
                    }
                    //
                    // Process any attribute if specified. 
                    if (!skipAnyAttribute) {
                        AnyAttribute anyAttr = attrSearch.getAnyAttribute();
                        if (anyAttr != null) {
                            checkComponent(anyAttr);
                        }
                    }
                }
            }
        } finally {
            skipAnyAttribute = prevValue;
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
    /**
     * This auxiliary method is a workaround fro the issuer #130053
     * @param sc
     * @return
     */
    protected boolean isXdmDomUsed(SchemaComponent sc) {
        org.w3c.dom.Element domElement = sc.getPeer();
        String packageName = domElement.getClass().getPackage().getName();
        return "org.netbeans.modules.xml.xdm.nodes".equals(packageName); // NOI18N

    }

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
     * Checks if the attributes' list contains the attribute. 
     * Only name and namespace of the attribute is taken into consideration.
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
    protected boolean contains(List<Attribute> attrList, Attribute attribute) {
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
                        return true;
                    } else {
                        String ns = attr.getModel().getEffectiveNamespace(attr);
                        if (ns != null && namespace.equals(ns)) {
                            return true;
                        }
                    }
                }
            }
        }
        //
        return false;
    }
}
