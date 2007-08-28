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
package org.netbeans.modules.xml.wsdl.model.extensions.bpel.validation.xpath;

import org.netbeans.modules.xml.schema.model.All;
import org.netbeans.modules.xml.schema.model.AttributeGroupReference;
import org.netbeans.modules.xml.schema.model.AttributeReference;
import org.netbeans.modules.xml.schema.model.Choice;
import org.netbeans.modules.xml.schema.model.ComplexContent;
import org.netbeans.modules.xml.schema.model.ComplexContentRestriction;
import org.netbeans.modules.xml.schema.model.ComplexExtension;
import org.netbeans.modules.xml.schema.model.ComplexType;
import org.netbeans.modules.xml.schema.model.Element;
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
import org.netbeans.modules.xml.schema.model.LocalType;
import org.netbeans.modules.xml.schema.model.Redefine;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.Sequence;
import org.netbeans.modules.xml.schema.model.SimpleContent;
import org.netbeans.modules.xml.schema.model.SimpleExtension;
import org.netbeans.modules.xml.schema.model.TypeContainer;
import org.netbeans.modules.xml.schema.model.Union;
import org.netbeans.modules.xml.schema.model.visitor.DefaultSchemaVisitor;
import org.netbeans.modules.xml.xam.Named;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;

/**
 * This schema visitor is inteneded to look for a child element or attribute 
 * It looks only children at the next lavel. 
 * The method visitChildren() has to be used. 
 * The name and namespace of the sought children is specified in the constructor.
 * 
 * The special case is looking for a global element or attribute. 
 * In this case the parent schema component is the Schema object. 
 * 
 * @author nk160297
 */
public class FindChildSchemaVisitor extends DefaultSchemaVisitor {
    
    private String mySoughtName;
    private String mySoughtNamespace;
    private boolean isAttribute; // hints that the sought object is an attribute
    
    private SchemaComponent myFound = null;
    
    public FindChildSchemaVisitor(
            String soughtName, String soughtNamespace, boolean isAttribute) {
        mySoughtName = soughtName;
        mySoughtNamespace = soughtNamespace;
        this.isAttribute = isAttribute;
    }
    
    public boolean isChildFound() {
        return myFound != null;
    }
    
    public SchemaComponent getFound() {
        return myFound;
    }
    
    /**
     * Looks for nested element or attribute. 
     * The name and namespace of the required subcomponent is specified in the constructor.
     * The result can be found with the help of the getFound() method.
     */ 
    public void lookForSubcomponent(SchemaComponent sc) {
        if (sc instanceof Element) {
            assert sc instanceof TypeContainer;
            //
            NamedComponentReference<? extends GlobalType> typeRef = 
                    ((TypeContainer)sc).getType();
            if (typeRef != null) {
                GlobalType globalType = typeRef.get();
                if (globalType != null) {
                    globalType.accept(this);
                }
            }
            //
            if (!isChildFound()) {
                LocalType localType = ((TypeContainer)sc).getInlineType();
                if (localType != null) {
                    localType.accept(this);
                }
            }
        } else if (sc instanceof ComplexType) {
            visitChildren(sc);
        } else if (sc instanceof Schema) {
            // Look for a global schema object
            visitChildren(sc);
        } else {
            // Other elements can't containg nested elements or attributes
        }
    }
    
    // ----------------------------------------------
    
    public void visit(LocalAttribute la) {
        if (isAttribute) {
            checkComponent(la);
        }
    }
    
    public void visit(GlobalAttribute ga) {
        if (isAttribute) {
            checkComponent(ga);
        }
    }
    
    public void visit(LocalElement le) {
        if (!isAttribute) {
            checkComponent(le);
        }
    }
    
    public void visit(GlobalElement ge) {
        if (!isAttribute) {
            checkComponent(ge);
        }
    }
    
    // --------------- References ------------------
    
    public void visit(ElementReference er) {
        // vlv # 105159
        if ( !isAttribute) {
            checkComponent(er);
        }
        if (isChildFound()) {
            return;
        }
        NamedComponentReference<GlobalElement> geRef = er.getRef();

        if (geRef != null) {
            GlobalElement ge = geRef.get();
            if (ge != null) {
                visit(ge);
            }
        }
    }
    
    public void visit(AttributeReference ar) {
        NamedComponentReference<GlobalAttribute> gaRef = ar.getRef();
        if (gaRef != null) {
            GlobalAttribute ga = gaRef.get();
            if (ga != null) {
                visit(ga);
            }
        }
    }
    
    public void visit(AttributeGroupReference agr) {
        NamedComponentReference<GlobalAttributeGroup> gagRef = agr.getGroup();
        if (gagRef != null) {
            GlobalAttributeGroup gag = gagRef.get();
            if (gag != null) {
                visit(gag);
            }
        }
    }
    
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
    
    
    public void visit(Schema s) {
        visitChildren(s);
    }
    
    public void visit(All a) {
        visitChildren(a);
    }
    
    public void visit(GlobalAttributeGroup gag) {
        visitChildren(gag);
    }
    
    public void visit(Choice c) {
        visitChildren(c);
    }
    
    public void visit(SimpleContent sc) {
        visitChildren(sc);
    }
    
    public void visit(ComplexContent cc) {
        visitChildren(cc);
    }
    
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
    
    public void visit(ComplexExtension ce) {
        NamedComponentReference<GlobalType> gtRef = ce.getBase();
        if (gtRef != null) {
            GlobalType gt = gtRef.get();
            if (gt != null) {
                visitChildren(gt);
            }
        }
        //
        visitChildren(ce);
    }
    
    public void visit(GlobalComplexType gct) {
        visitChildren(gct);
    }
    
    public void visit(LocalComplexType lct) {
        visitChildren(lct);
    }
    
    public void visit(GlobalGroup gg) {
        visitChildren(gg);
    }
    
    public void visit(Redefine r) {
        // NOT ABSOLUTELY CLEAR YET
        visitChildren(r);
    }
    
    public void visit(ComplexContentRestriction ccr) {
        visitChildren(ccr);
    }
    
    public void visit(Sequence s) {
        visitChildren(s);
    }
    
    public void visit(Union u) {
        visitChildren(u);
    }
    
    // ----------------------------------------------
    
    private void visitChildren(SchemaComponent sc) {
        if (isChildFound()) {
            return;
        }
        //
        for (SchemaComponent child: sc.getChildren()) {
            child.accept(this);
            //
            if (isChildFound()) {
                return;
            }
        }
    }
    
    private void checkComponent(SchemaComponent sc) {
        if (sc instanceof Named) {
            String namespace = sc.getModel().getEffectiveNamespace(sc);
            String name = ((Named)sc).getName();
            if (mySoughtName.equals(name)) {
                //
                // Compare namespace as well if it is specified
                if (mySoughtNamespace != null) {
                    if (mySoughtNamespace.equals(namespace)) {
                        myFound = sc;
                    }
                } else {
                    myFound = sc;
                }
            } 
        }
    }
}
