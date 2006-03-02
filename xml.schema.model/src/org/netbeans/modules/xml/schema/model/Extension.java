/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */


package org.netbeans.modules.xml.schema.model;

import org.netbeans.modules.xml.xam.GlobalReference;

/**
 *
 * @author Chris Webster
 */
public interface Extension extends LocalAttributeContainer {
        public static final String BASE_PROPERTY                = "base";
        
	GlobalReference<GlobalType> getBase();
	void setBase(GlobalReference<GlobalType> type);
}
