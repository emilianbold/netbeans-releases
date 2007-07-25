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

package org.netbeans.modules.iep.model.impl;

import java.util.ArrayList;
import java.util.Collection;
import org.netbeans.modules.iep.model.Component;
import org.netbeans.modules.iep.model.IEPComponent;
import org.netbeans.modules.iep.model.Property;


/**
 * 
 * 
 */
public enum TypeCollection {
	EMPTY(createEmpty()),
	FOR_COMPONENT (createComponent()),
        FOR_PROPERTY (createProperty());
        
	private Collection<Class<? extends IEPComponent>> types;

	TypeCollection(Collection<Class<? extends IEPComponent>> types) {
		this.types = types;
	}

	public Collection<Class<? extends IEPComponent>> types() {
		return types;
	}

	static Collection<Class<? extends IEPComponent>> createEmpty() {
		Collection<Class<? extends IEPComponent>> c = new ArrayList<Class<? extends IEPComponent>>();
		return c;
	}
	
	static Collection<Class<? extends IEPComponent>> createComponent() {
		Collection<Class<? extends IEPComponent>> c = createEmpty();
		c.add(Component.class);
		return c;
	}

        static Collection<Class<? extends IEPComponent>> createProperty() {
		Collection<Class<? extends IEPComponent>> c = createEmpty();
		c.add(Component.class);
                c.add(Property.class);
		return c;
	}
        
	
}
