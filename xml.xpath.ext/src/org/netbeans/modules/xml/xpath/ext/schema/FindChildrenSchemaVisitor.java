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

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.xml.schema.model.ComplexType;
import org.netbeans.modules.xml.schema.model.Element;
import org.netbeans.modules.xml.schema.model.ElementReference;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.LocalType;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.TypeContainer;
import org.netbeans.modules.xml.xam.Named;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;

import java.util.Set;
import org.netbeans.modules.xml.schema.model.AnyAttribute;
import org.netbeans.modules.xml.schema.model.AnyElement;
import org.netbeans.modules.xml.schema.model.Attribute;
import org.netbeans.modules.xml.schema.model.AttributeReference;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.XPathSchemaContext;

/**
 * This schema visitor is inteneded to look for a children elements or attributes 
 * It looks only children at the next lavel. 
 * The name and namespace of the sought children is specified in the constructor.
 * If the namespace isn't specified, then it looks for a children only by name. 
 * 
 * @author nk160297
 */
public class FindChildrenSchemaVisitor extends AbstractSchemaSearchVisitor {
    
    private XPathSchemaContext mParentContext;
    private String mySoughtName;
    private String mySoughtNamespace;
    private boolean isAttribute; // hints that the sought object is an attribute
    
    private List<SchemaComponent> myFound = new ArrayList<SchemaComponent>();
    
    private boolean mHasAny = false;
    private boolean mHasAnyAttribute = false;
    
    public FindChildrenSchemaVisitor(XPathSchemaContext parentContext, 
            String soughtName, String soughtNamespace, boolean isAttribute) {
        //
        super();
        assert soughtName != null : "At least sought name has to be specified!"; // NOI18N
        //
        mParentContext = parentContext;
        mySoughtName = soughtName;
        mySoughtNamespace = soughtNamespace;
        this.isAttribute = isAttribute;
    }
    
    @Override
    public void visit(ElementReference er) {
        if (!isAttribute) {
            String name = fastGetRefName(er.getRef());
            if (!mySoughtName.equals(name)) {
                return;
            }
            super.visit(er);
        }
    }
    
    @Override
    public void visit(AttributeReference ar) {
        if (isAttribute) {
            String name = fastGetRefName(ar.getRef());
            if (!mySoughtName.equals(name)) {
                return;
            }
            super.visit(ar);
        }
    }
    
    //-----------------------------------------------------------------
    
    public List<SchemaComponent> getFound() {
        return myFound;
    }
    
    public boolean hasAny() {
        return mHasAny;
    }
    
    public boolean hasAnyAttribute() {
        return mHasAnyAttribute;
    }
    
    public void lookForSubcomponent(SchemaComponent sc) {
        if (sc instanceof Element) {
            if (sc instanceof TypeContainer) {
                NamedComponentReference<? extends GlobalType> typeRef = ((TypeContainer)sc).getType();
                if (typeRef != null) {
                    GlobalType globalType = typeRef.get();
                    if (globalType != null) {
                        globalType.accept(this);
                    }
                }
                LocalType localType = ((TypeContainer)sc).getInlineType();
                if (localType != null) {
                    localType.accept(this);
                }
            } else if (sc instanceof ElementReference) {
                NamedComponentReference<GlobalElement> gElemRef = ((ElementReference)sc).getRef();
                if (gElemRef != null) {
                    GlobalElement gElement = gElemRef.get();
                    lookForSubcomponent(gElement);
                }
            }
        } else if (sc instanceof ComplexType) {
            visitChildren(sc);
        } else if (sc instanceof Schema) {
           // Look for a global schema object
           lookGlobalOnly = true;
           try {
               visitChildren(sc);
           } finally {
               lookGlobalOnly = false;
           }

        } else {
            // Other elements can't containg nested elements or attributes
        }
    }

    protected void checkComponent(SchemaComponent sc) {
        if (sc instanceof Named) {
            //
            // It is implied that schema component (sc) is either element or attribute here.
            // Check it corresponds to isAttribute flag.
            if (sc instanceof Element && isAttribute) {
                return;
            } else if (sc instanceof Attribute && !isAttribute) {
                return;
            }
            //
            String name = ((Named)sc).getName();
            if (mySoughtName.equals(name)) {
                //
                // Compare namespace as well if it is specified
                if (mySoughtNamespace == null || mySoughtNamespace.length() == 0) {
                    addElement(sc);
                } else {
                    Set<String> namespacesSet = XPathSchemaContext.Utilities.
                            getEffectiveNamespaces(sc, mParentContext);
                    //
                    if (namespacesSet.contains(mySoughtNamespace)) {
                        addElement(sc);
                    }
                }
            } 
        }
        if (sc instanceof AnyElement) {
            mHasAny = true;
        }
        if (sc instanceof AnyAttribute) {
            mHasAnyAttribute = true;
        }
    }
    
    private void addElement(SchemaComponent element) {
        if (!(element instanceof Named)) {
            myFound.add(element);
            return;
        }

        boolean flag = true;

        for (SchemaComponent el : myFound) {
            if (el instanceof Named) {
                String name1 = ((Named) el).getName();
                String name2 = ((Named) element).getName();
//                String nameSp1 = SchemaModelsStack.getEffectiveNamespace(el, 
//                        new SchemaModelsStack());
//                String nameSp2 = SchemaModelsStack.getEffectiveNamespace(element, 
//                        new SchemaModelsStack());
//                Set<String> set = XPathSchemaContext.Utilities.getEffectiveNamespaces(el, mParentContext);
//                nameSp1 = set.iterator().next();
//                set = XPathSchemaContext.Utilities.getEffectiveNamespaces(element, mParentContext);
//                nameSp2 = set.iterator().next();
                if (name1 != null && name1.equals(name2)) {
      //              myFound.remove(el);
                    flag = false;
                    break;
                }
            }
        }

        if (flag) {
            myFound.add(element);
        }
    }
    
}
