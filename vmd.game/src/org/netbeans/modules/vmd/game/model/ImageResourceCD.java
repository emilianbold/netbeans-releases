/*
 * ImageResourceCD.java
 *
 * Created on January 1, 2007, 6:33 PM
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
import org.netbeans.modules.vmd.api.model.Versionable;
import org.netbeans.modules.vmd.midp.components.MidpTypes;

/**
 * 
 * @author Karel Herink
 */
public class ImageResourceCD extends ComponentDescriptor {
	
	public static final TypeID TYPEID = new TypeID(TypeID.Kind.COMPONENT, "org.netbeans.modules.vmd.game.model.ImageResource"); // NOI18N
	
	public static final String PROPERTY_IMAGE_URL = "imageresourcecd.prop.imageurl";
	public static final String PROPERTY_TILE_WIDTH = "imageresourcecd.prop.tilewidth";
	public static final String PROPERTY_TILE_HEIGHT = "imageresourcecd.prop.tileheight";
	

	public TypeDescriptor getTypeDescriptor() {
		return new TypeDescriptor(null, TYPEID, true, false);
	}

	public VersionDescriptor getVersionDescriptor() {
		return null;
	}

	public List<PropertyDescriptor> getDeclaredPropertyDescriptors() {
		return Arrays.asList(
				new PropertyDescriptor(PROPERTY_IMAGE_URL, MidpTypes.TYPEID_JAVA_LANG_STRING, 
					PropertyValue.createNull(), false, false, Versionable.FOREVER),
				new PropertyDescriptor(PROPERTY_TILE_WIDTH, MidpTypes.TYPEID_INT, 
					PropertyValue.createNull(), false, false, Versionable.FOREVER),
				new PropertyDescriptor(PROPERTY_TILE_HEIGHT, MidpTypes.TYPEID_INT, 
					PropertyValue.createNull(), false, false, Versionable.FOREVER)
				);
	}

	protected List<? extends Presenter> createPresenters() {
		return null;
	}
	
}
