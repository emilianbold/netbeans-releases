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
import org.netbeans.modules.xml.schema.model.AnyAttribute;
import org.netbeans.modules.xml.schema.model.AnyElement;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.Attribute;
import org.netbeans.modules.xml.schema.model.ComplexType;
import org.netbeans.modules.xml.schema.model.Element;
import org.netbeans.modules.xml.schema.model.ElementReference;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.LocalType;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.TypeContainer;
import org.netbeans.modules.xml.xam.Named;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;

/**
 * This schema visitor is inteneded to look for a children elements or attributes 
 * It looks only children at the next lavel. 
 * 
 * @author nk160297
 */
public class FindAllChildrenSchemaVisitor extends AbstractSchemaSearchVisitor {
    
    private boolean mLookForElements;
    private boolean mLookForAttributes; 
    private boolean mSupportAny;
    
    private List<SchemaComponent> myFound = new ArrayList<SchemaComponent>();
    private boolean mHasAny = false;
    private boolean mHasAnyAttribute = false;
    
    /**
     * 
     * @param lookForElements
     * @param lookForAttributes
     * @param supportAny indicates if it is necessary to add 
     * AnyElement and AnyAttribute to result list. 
     */
    public FindAllChildrenSchemaVisitor(boolean lookForElements, 
            boolean lookForAttributes, boolean supportAny) {
        super();
        assert lookForElements || lookForAttributes : "one of the flags has to be true"; // NOI18N
        //
        mLookForElements = lookForElements;
        mLookForAttributes = lookForAttributes;
        mSupportAny = supportAny;
    }
    
    public List<SchemaComponent> getFound() {
        return myFound;
    }
    
    public boolean hasAny() {
        return mHasAny;
    }
    
    public boolean hasAnyAttribute() {
        return mHasAnyAttribute;
    }
    
    /**
     * Start searching from the specified schema component. 
     * Any kind of component can be used. 
     */ 
    public void lookForSubcomponents(SchemaComponent sc) {
        myFound.clear();
        //
        if (sc instanceof Element) {
            if (sc instanceof TypeContainer) {
                NamedComponentReference<? extends GlobalType> typeRef = 
                        ((TypeContainer)sc).getType();
                if (typeRef != null) {
                    GlobalType globalType = typeRef.get();
                    if (globalType != null) {
                        globalType.accept(this);
                    }
                }
                //
                LocalType localType = ((TypeContainer)sc).getInlineType();
                if (localType != null) {
                    localType.accept(this);
                }
            } else if (sc instanceof ElementReference) {
                NamedComponentReference<GlobalElement> gElemRef = 
                        ((ElementReference)sc).getRef();
                if (gElemRef != null) {
                    GlobalElement gElement = gElemRef.get();
                    // Do recursive call here
                    lookForSubcomponents(gElement);
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
    
    // ----------------------------------------------
    
    protected void checkComponent(SchemaComponent sc) {
        if (mLookForElements && sc instanceof Element) {
            if (sc instanceof ElementReference) {
                // Need to see deeper to the referenced element
                return;
            }
            addSchemaComponent(sc);
            return;
        }
        if (mLookForAttributes && sc instanceof Attribute) {
            // Element required here!
            myFound.add(sc);
            return;
        }
        if (sc instanceof AnyElement) {
            mHasAny = true;
            if (mSupportAny) {
                myFound.add(sc);
            }
        }
        if (sc instanceof AnyAttribute) {
            mHasAnyAttribute = true;
            if (mSupportAny) {
                myFound.add(sc);
            }
        }
    }

    private void addSchemaComponent(SchemaComponent element) {
        if (!(element instanceof Named)) {
            myFound.add(element);
            return;
        }

        boolean flag = true;

        for (SchemaComponent el : myFound) {
            if (el instanceof Named) {
                String name1 = ((Named) el).getName();
                String name2 = ((Named) element).getName();
                String nameSp1 = element.getModel().getEffectiveNamespace(element);
                String nameSp2 = el.getModel().getEffectiveNamespace(el);

                if (name1 != null && name1.equals(name2) &&
                        equalsNemeSpase(nameSp1, nameSp2))
                {
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
    
    private boolean equalsNemeSpase(Object o1, Object o2) {
        if (o1 == null || o2 == null) { return true; }
        return o1.equals(o2);
    }
}
