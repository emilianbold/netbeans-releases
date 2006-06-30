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
 * GlobalComplexTypeImpl.java
 *
 * Created on October 5, 2005, 6:55 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.model.impl;

import java.util.Set;
import org.netbeans.modules.xml.schema.model.GlobalComplexType;
import org.netbeans.modules.xml.schema.model.GlobalComplexType.Block;
import org.netbeans.modules.xml.schema.model.GlobalComplexType.Final;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.visitor.SchemaVisitor;
import org.w3c.dom.Element;


/**
 *
 * @author rico
 */
public class GlobalComplexTypeImpl extends CommonComplexTypeImpl implements GlobalComplexType{
    
    /** Creates a new instance of GlobalComplexTypeImpl */
    public GlobalComplexTypeImpl(SchemaModelImpl model) {
        super(model,createNewComponent(SchemaElements.COMPLEX_TYPE, model));
    }
    
    public GlobalComplexTypeImpl(SchemaModelImpl model, Element el){
        super(model,el);
    }

	/**
	 *
	 *
	 */
	public Class<? extends SchemaComponent> getComponentType()
	{
		return GlobalComplexType.class;
	}
    
    public void setName(String name) {
        RenamingVisitor renameVisitor = new RenamingVisitor();
	renameVisitor.rename(this, name);
    }
    
    public String getName() {
        return getAttribute(SchemaAttributes.NAME);
    }
    
    public void setFinal(Set<Final> finalValue) {
        setAttribute(FINAL_PROPERTY, SchemaAttributes.FINAL, 
                finalValue == null ? null : 
                    Util.convertEnumSet(Final.class, finalValue));
    }
    
    public Set<Final> getFinal() {
        String s = getAttribute(SchemaAttributes.FINAL);
        return s == null ? null : Util.valuesOf(Final.class, s);
    }
    
    public void setBlock(Set<Block> block) {
        setAttribute(BLOCK_PROPERTY, SchemaAttributes.BLOCK,
                block == null ? null : 
                    Util.convertEnumSet(Block.class, block));
    }
    
    public Set<Block> getBlock() {
        String s = getAttribute(SchemaAttributes.BLOCK);
        return s == null ? null : Util.valuesOf(Block.class, s);
    }
    
    public Set<Final> getFinalEffective() {
        Set<Final> v = getFinal();
        return v == null ? getFinalDefault() : v;
    }

    public Set<Final> getFinalDefault() {
        return Util.convertEnumSet(Final.class, getSchemaModel().getSchema().getFinalDefaultEffective());
    }

    public Set<Block> getBlockEffective() {
        Set<Block> v = getBlock();
        return v == null ? getBlockDefault() : v;
    }

    public Set<Block> getBlockDefault() {
        return Util.convertEnumSet(Block.class, getSchemaModel().getSchema().getBlockDefaultEffective());
    }

    public void setAbstract(Boolean isAbstract) {
        setAttribute(ABSTRACT_PROPERTY, SchemaAttributes.ABSTRACT, isAbstract);
    }
    
    public Boolean isAbstract() {
        String s = getAttribute(SchemaAttributes.ABSTRACT);
        return s == null ? null : Boolean.valueOf(s);
    }

    public void accept(SchemaVisitor visitor) {
        visitor.visit(this);
    }
    
    protected Class getAttributeMemberType(SchemaAttributes attr) {
        switch(attr) {
            case FINAL:
                return Final.class;
            case BLOCK:
                return Block.class;
            default:
                return super.getAttributeMemberType(attr);
        }
    }

    public boolean getEffectiveAbstract() {
        Boolean v = isAbstract();
        return v == null ? getAbstractDefault() : v;
    }

    public boolean getAbstractDefault() {
        return false;
    }

    public boolean getAbstractEffective() {
        Boolean v = isAbstract();
        return v == null ? getAbstractDefault() : v;
    }
}
