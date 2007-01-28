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

package org.netbeans.modules.vmd.game.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.vmd.api.model.ComponentDescriptor;
import org.netbeans.modules.vmd.api.model.Presenter;
import org.netbeans.modules.vmd.api.model.PropertyDescriptor;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.model.TypeDescriptor;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.api.model.VersionDescriptor;
import org.netbeans.modules.vmd.midp.components.MidpTypes;

/**
 *
 * @author kherink
 */
public class AnimatedTileCD extends ComponentDescriptor {
	
	public static final TypeID TYPEID = new TypeID(TypeID.Kind.COMPONENT, "org.netbeans.modules.vmd.game.model.AnimatedTile"); // NOI18N
	
	
	public static final String PROPERTY_NAME = "animatedtilecd.prop.name";
	public static final String PROP_IMAGE_RESOURCE = "animatedtilecd.prop.imageresource"; // NOI18N
	
	public TypeDescriptor getTypeDescriptor() {
		return new TypeDescriptor(null, TYPEID, true, false);
	}

	public VersionDescriptor getVersionDescriptor() {
		return null;
	}

	public List<PropertyDescriptor> getDeclaredPropertyDescriptors() {
		
		List<PropertyDescriptor> propertyDescriptors = new ArrayList<PropertyDescriptor>();
		propertyDescriptors.addAll(SequenceContainerCDProperties.getDeclaredPropertyDescriptors());
		
		propertyDescriptors.add(new PropertyDescriptor(PROPERTY_NAME, MidpTypes.TYPEID_JAVA_LANG_STRING, 
				PropertyValue.createNull(), false, false, null));
		propertyDescriptors.add(new PropertyDescriptor(PROP_IMAGE_RESOURCE, ImageResourceCD.TYPEID, 
				PropertyValue.createNull(), false, false, null));
		return Collections.unmodifiableList(propertyDescriptors);
	}

	protected List<? extends Presenter> createPresenters() {
		return null;
	}
	
}
