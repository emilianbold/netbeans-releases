/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
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
