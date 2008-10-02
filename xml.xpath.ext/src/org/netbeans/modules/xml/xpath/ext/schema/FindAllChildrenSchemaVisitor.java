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
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;

/**
 * This schema visitor is inteneded to look for a children elements or attributes 
 * It looks only children at the next lavel. 
 * 
 * @author nk160297
 */
public class FindAllChildrenSchemaVisitor extends AbstractSchemaSearchVisitor {
    
    private boolean lookForElements;
    private boolean lookForAttributes; 
    
    private List<SchemaComponent> myFound = new ArrayList<SchemaComponent>();
    
    public FindAllChildrenSchemaVisitor(boolean lookForElements, 
            boolean lookForAttributes) {
        super();
        assert lookForElements || lookForAttributes : "one of the flags has to be true"; // NOI18N
        //
        this.lookForElements = lookForElements;
        this.lookForAttributes = lookForAttributes;
    }
    
    public List<SchemaComponent> getFound() {
        return myFound;
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
    
    protected void checkComponent(SchemaComponent sc) {
        if (lookForElements && sc instanceof Element) {
            if (sc instanceof ElementReference) {
                // Need to see deeper to the referenced element
                return;
            }
            myFound.add(sc);
            return;
        }
        if (lookForAttributes && sc instanceof Attribute) {
            // Element required here!
            myFound.add(sc);
            return;
        }
    }
}
