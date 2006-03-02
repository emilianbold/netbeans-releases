/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */

package org.netbeans.modules.xml.schema.model.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.LocalSimpleType;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.Union;
import org.netbeans.modules.xml.schema.model.visitor.SchemaVisitor;
import org.netbeans.modules.xml.xam.GlobalReference;
import org.w3c.dom.Element;

 
/**
 * DOM based implementation
 * @author Chris Webster
 * @author Vidhya Narayanan
 */
public class UnionImpl extends SchemaComponentImpl implements Union {
    protected UnionImpl(SchemaModelImpl model){
        this(model, createNewComponent(SchemaElements.UNION, model));
    }
    
    public UnionImpl(SchemaModelImpl model,Element el){
        super(model, el);
    }

	/**
	 *
	 *
	 */
	public Class<? extends SchemaComponent> getComponentType() {
		return Union.class;
	}
    
    public Collection<GlobalReference<GlobalSimpleType>> getMemberTypes() {
        
        String val = getAttribute(SchemaAttributes.MEMBER_TYPES);
	if (val == null) {
	    return null;
	}
        List<GlobalReference<GlobalSimpleType>> gsts = new ArrayList<GlobalReference<GlobalSimpleType>>();
        if (val.trim().length()==0) return gsts;
        String[] ss = val.split("( |\t|\n|\r|\f)+");
        for(int i = 0; i < ss.length; i++){
            GlobalReference<GlobalSimpleType> ref =
                    new GlobalReferenceImpl(GlobalSimpleType.class, this, ss[i]);
            gsts.add(ref);
        }
        return gsts;
        
    }
    
    public void removeMemberType(GlobalReference<GlobalSimpleType> gst) {
        String refVal = getPrefixedName(gst.getEffectiveNamespace(),
                gst.get().getName());
        String val = getAttribute(SchemaAttributes.MEMBER_TYPES);
        StringBuffer sb = new StringBuffer();
        if (val != null) {
            String[] ss = val.split("( |\t|\n|\r|\f)+");
            boolean first = true;
            for (String s : ss) {
                if (!s.equals(refVal)) {
                    if (!first)
                        sb.append(" ");
                    else
                        first = false;
                    sb.append(s);
                }
            }
        }
        setAttribute(MEMBER_TYPES_PROPERTY, SchemaAttributes.MEMBER_TYPES, sb.length()==0?null:sb.toString());
    }
    
    public void addMemberType(GlobalReference<GlobalSimpleType> gst) {
        String val = getAttribute(SchemaAttributes.MEMBER_TYPES);
        String refVal = getPrefixedName(gst.getEffectiveNamespace(),
                gst.get().getName());
        if (val == null)
            val = refVal;
        else
            val = val.concat(" ").concat(refVal);
        setAttribute(MEMBER_TYPES_PROPERTY, SchemaAttributes.MEMBER_TYPES, val);
    }
    
    public void accept(SchemaVisitor visitor) {
        visitor.visit(this);
    }
    
    public void removeInlineType(LocalSimpleType type) {
        removeChild(INLINE_TYPE_PROPERTY, type);
    }
    
    public void addInlineType(LocalSimpleType type) {
        appendChild(INLINE_TYPE_PROPERTY, type);
    }
      
    public java.util.Collection<LocalSimpleType> getInlineTypes() {
        return getChildren(LocalSimpleType.class);
    }
}
