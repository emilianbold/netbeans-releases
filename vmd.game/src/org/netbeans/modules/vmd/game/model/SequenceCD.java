/*
 * SequenceCD.java
 *
 * Created on January 2, 2007, 9:32 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.vmd.game.model;

import org.netbeans.modules.vmd.api.model.ComponentDescriptor;
import org.netbeans.modules.vmd.api.model.Presenter;
import org.netbeans.modules.vmd.api.model.PropertyDescriptor;
import org.netbeans.modules.vmd.api.model.TypeDescriptor;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.api.model.VersionDescriptor;
import java.util.Arrays;
import java.util.List;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.model.Versionable;
import org.netbeans.modules.vmd.game.integration.components.GameTypes;
import org.netbeans.modules.vmd.midp.components.MidpTypes;

/**
 *
 * @author kherink
 */
public class SequenceCD extends ComponentDescriptor {
	
	public static final TypeID TYPEID = new TypeID(TypeID.Kind.COMPONENT, "org.netbeans.modules.vmd.game.model.Squence"); // NOI18N
	
	public static final String PROPERTY_IMAGE_RESOURCE = "sequncecd.prop.imageresource";
	public static final String PROPERTY_NAME = "sequncecd.prop.name";
	public static final String PROPERTY_FRAME_MS = "sequncecd.prop.framems";
	public static final String PROPERTY_FRAMES = "sequncecd.prop.frames";
	
	
	public TypeDescriptor getTypeDescriptor() {
		return new TypeDescriptor(null, TYPEID, true, true);
	}
	
	public List<PropertyDescriptor> getDeclaredPropertyDescriptors() {
		return Arrays.asList(
			new PropertyDescriptor(PROPERTY_IMAGE_RESOURCE, ImageResourceCD.TYPEID,
				PropertyValue.createNull(), false, false, Versionable.FOREVER),
			new PropertyDescriptor(PROPERTY_NAME, MidpTypes.TYPEID_JAVA_LANG_STRING,
				PropertyValue.createNull(), false, false, Versionable.FOREVER),
			new PropertyDescriptor(PROPERTY_FRAME_MS, MidpTypes.TYPEID_INT,
				PropertyValue.createNull(), false, false, Versionable.FOREVER),
			new PropertyDescriptor(PROPERTY_FRAMES, GameTypes.TYPEID_SEQUENCE_FRAMES,
				PropertyValue.createNull(), false, false, Versionable.FOREVER)
		);
	}
	
	public VersionDescriptor getVersionDescriptor() {
		return null;
	}
	
	protected List<? extends Presenter> createPresenters() {
		return null;
	}
	
}
