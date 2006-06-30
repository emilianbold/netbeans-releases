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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * FindGlobalReferenceVisitor.java
 *
 * Created on October 17, 2005, 6:28 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.model.visitor;

import java.util.Collection;
import java.util.List;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.GlobalAttribute;
import org.netbeans.modules.xml.schema.model.GlobalAttributeGroup;
import org.netbeans.modules.xml.schema.model.GlobalComplexType;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.GlobalGroup;
import org.netbeans.modules.xml.schema.model.Notation;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.xam.Referenceable;

/**
 *
 * @author rico
 */
public class  FindGlobalReferenceVisitor <T extends Referenceable> extends DefaultSchemaVisitor{
    private Class<T> elementType;
    private String localName;
    private Schema schema;
    private T refType;
    private boolean found;
    private GetRefsCommand getRefsCommand;
    
    public T find(Class<T> elementType, String localName, Schema schema){
        if (elementType == null) {
            throw new IllegalArgumentException("elementType == null");
        }
        this.elementType = elementType;
        this.localName = localName;
        this.schema = schema;
        found = false;
        schema.accept(this);
        return refType;
    }
    
    public void visit(Schema schema){
        List<SchemaComponent> ch = schema.getChildren();
        for (SchemaComponent c : ch) {
            c.accept(this);
            if(found) return;
        }
    }
    
    public void  visit(GlobalAttributeGroup e){
        getRefsCommand = new GetRefsCommand(){
            public Collection<GlobalAttributeGroup> performGetRefs(){
                return schema.getAttributeGroups();
            }
        };
        findReference(getRefsCommand, GlobalAttributeGroup.class);
    }
    
    public void visit(GlobalGroup e){
        getRefsCommand = new GetRefsCommand(){
            public Collection<GlobalGroup> performGetRefs(){
                return schema.getGroups();
            }
        };
        findReference(getRefsCommand, GlobalGroup.class);
    }
    
    public void visit(GlobalAttribute e){
        getRefsCommand = new GetRefsCommand(){
            public Collection<GlobalAttribute> performGetRefs(){
                return schema.getAttributes();
            }
        };
        findReference(getRefsCommand, GlobalAttribute.class);
    }
    
    public void visit(GlobalElement e){
        getRefsCommand= new GetRefsCommand(){
            public Collection<GlobalElement> performGetRefs(){
                return schema.getElements();
            }
        };
        findReference(getRefsCommand, GlobalElement.class);
    }
    
    public void visit(GlobalSimpleType e){
        getRefsCommand = new GetRefsCommand(){
            public Collection<GlobalSimpleType> performGetRefs(){
                return schema.getSimpleTypes();
            }
        };
        findReference(getRefsCommand, GlobalSimpleType.class);
    }
    
    public void visit(GlobalComplexType e){
        getRefsCommand = new GetRefsCommand(){
            public Collection<GlobalComplexType> performGetRefs(){
                return schema.getComplexTypes();
            }
        };
        findReference(getRefsCommand, GlobalComplexType.class);
    }
    
    public void visit(Notation e){
        getRefsCommand = new GetRefsCommand(){
            public Collection<Notation> performGetRefs(){
                return schema.getNotations();
            }
        };
        findReference(getRefsCommand, GlobalComplexType.class);
    }
    
    private void findReference(GetRefsCommand refCommand, Class<? extends Referenceable> refClass){
        if(elementType.isAssignableFrom(refClass)){
            for(Referenceable n : refCommand.performGetRefs()){
                if(n.getName().equals(localName)){
                    refType = elementType.cast(n);
                    found =  true;
                    break;
                }
            }
        }
    }
    
    /**
     * Command interface that encapsulates the various
     * Schema methods for obtaining Referenceables
     */
    interface GetRefsCommand{
        Collection<? extends Referenceable> performGetRefs();
    }
}
