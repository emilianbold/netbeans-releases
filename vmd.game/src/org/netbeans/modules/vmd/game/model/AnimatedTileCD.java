/*
 * AnimatedTileCD.java
 *
 * Created on January 10, 2007, 9:38 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.vmd.game.model;

import java.util.Arrays;
import java.util.List;
import org.netbeans.modules.vmd.api.model.ComponentDescriptor;
import org.netbeans.modules.vmd.api.model.Presenter;
import org.netbeans.modules.vmd.api.model.PropertyDescriptor;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.model.TypeDescriptor;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.api.model.VersionDescriptor;

/**
 *
 * @author kaja
 */
public class AnimatedTileCD extends ComponentDescriptor {
	
	public static final TypeID TYPEID = new TypeID(TypeID.Kind.COMPONENT, "org.netbeans.modules.vmd.game.model.AnimatedTile"); // NOI18N
	
			
	private static final String PROP_IMAGE_RESOURCE = "animatedtilecd.prop.imageresource"; // NOI18N
	private static final String PROP_DEFAULT_SEQUENCE = "animatedtilecd.prop.defaultsequence"; // NOI18N
	private static final String PROP_SEQUENCES = "animatedtilecd.prop.sequences"; // NOI18N
	
	public TypeDescriptor getTypeDescriptor() {
		return new TypeDescriptor(null, TYPEID, true, false);
	}

	public VersionDescriptor getVersionDescriptor() {
		return null;
	}

	public List<PropertyDescriptor> getDeclaredPropertyDescriptors() {
		return Arrays.asList(
			new PropertyDescriptor(PROP_IMAGE_RESOURCE, ImageResourceCD.TYPEID, 
				PropertyValue.createNull(), false, false, null),
			new PropertyDescriptor(PROP_DEFAULT_SEQUENCE, SequenceCD.TYPEID, 
				PropertyValue.createEmptyArray(SequenceCD.TYPEID), false, false, null),
			new PropertyDescriptor(PROP_SEQUENCES, SequenceCD.TYPEID.getArrayType(), 
				PropertyValue.createEmptyArray(SequenceCD.TYPEID), false, false, null)
		);
	}

	protected List<? extends Presenter> createPresenters() {
		return null;
	}
	
}
