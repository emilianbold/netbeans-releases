/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */


package org.netbeans.modules.xml.schema.model;

import org.netbeans.modules.xml.xam.GlobalReference;

/**
 * This interface represents any non top level occurrence of attributeGroup.
 * @author Chris Webster
 */
public interface AttributeGroupReference extends SchemaComponent {
	public static final String GROUP_PROPERTY = "group";
        
        GlobalReference<GlobalAttributeGroup> getGroup();
	void setGroup(GlobalReference<GlobalAttributeGroup> group);
}
