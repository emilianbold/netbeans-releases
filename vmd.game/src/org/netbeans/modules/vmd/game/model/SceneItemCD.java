/*
 * SceneItemCD.java
 *
 * Created on January 10, 2007, 6:22 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.vmd.game.model;

import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.api.model.common.ValidatorPresenter;
import org.netbeans.modules.vmd.game.integration.components.GameTypes;
import org.netbeans.modules.vmd.midp.components.MidpTypes;

import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Karel Herink
 */
public class SceneItemCD extends ComponentDescriptor {
	
	/** Creates a new instance of SceneItemCD */
	public SceneItemCD() {
	}

	public static final TypeID TYPEID = new TypeID(TypeID.Kind.COMPONENT, "SceneItem"); // NOI18N

	public static final String PROPERTY_LAYER = "sceneitemcd.prop.layer"; // NOI18N
	public static final String PROPERTY_LOCK = "sceneitemcd.prop.lock"; // NOI18N
	public static final String PROPERTY_VISIBLE = "sceneitemcd.prop.visible"; // NOI18N
	public static final String PROPERTY_POSITION = "sceneitemcd.prop.position"; // NOI18N
	public static final String PROPERTY_Z_ORDER = "sceneitemcd.prop.z.order"; // NOI18N
	
	public TypeDescriptor getTypeDescriptor() {
		return new TypeDescriptor(null, TYPEID, true, false);
	}

	public VersionDescriptor getVersionDescriptor() {
		return null;
	}

	public List<PropertyDescriptor> getDeclaredPropertyDescriptors() {
		return Arrays.asList(
				new PropertyDescriptor(PROPERTY_LAYER, LayerCD.TYPEID, 
					PropertyValue.createNull(), false, false, Versionable.FOREVER),
				new PropertyDescriptor(PROPERTY_LOCK, MidpTypes.TYPEID_BOOLEAN, 
					PropertyValue.createNull(), false, false, Versionable.FOREVER),
				new PropertyDescriptor(PROPERTY_VISIBLE, MidpTypes.TYPEID_BOOLEAN,
					PropertyValue.createNull(), false, false, Versionable.FOREVER),
				new PropertyDescriptor(PROPERTY_POSITION, GameTypes.TYPEID_POINT,
					PropertyValue.createNull(), false, false, Versionable.FOREVER),
				new PropertyDescriptor(PROPERTY_Z_ORDER, MidpTypes.TYPEID_INT,
					PropertyValue.createNull(), false, false, Versionable.FOREVER)
				);
	}

	protected List<? extends Presenter> createPresenters() {
		return Arrays.asList (
            new ValidatorPresenter() {
                protected void checkCustomValidity() {
                    assert getComponent().readProperty(PROPERTY_LAYER).getKind() != PropertyValue.Kind.NULL;
                }
            }
        );
	}
	
}
