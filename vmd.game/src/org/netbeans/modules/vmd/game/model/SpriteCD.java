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

import org.netbeans.modules.vmd.api.model.*;
import java.util.Arrays;
import java.util.List;

/**
 * @author David Kaspar, Karel Herink
 */
public class SpriteCD extends ComponentDescriptor {
	
	public static final TypeID TYPEID = new TypeID(TypeID.Kind.COMPONENT, "org.netbeans.modules.vmd.game.model.Sprite"); // NOI18N
	
			
	public static final String PROP_DEFAULT_SEQUENCE = "spritecd.prop.defaultsequence"; // NOI18N
	public static final String PROP_SEQUENCES = "spritecd.prop.sequences"; // NOI18N
	
	
	public TypeDescriptor getTypeDescriptor() {
		return new TypeDescriptor(LayerCD.TYPEID, TYPEID, true, false);
	}
	
	public VersionDescriptor getVersionDescriptor() {
		return null;
	}
	
	public List<PropertyDescriptor> getDeclaredPropertyDescriptors() {
		return Arrays.asList(
			new PropertyDescriptor(PROP_DEFAULT_SEQUENCE, SequenceCD.TYPEID, 
				PropertyValue.createEmptyArray(SequenceCD.TYPEID), false, false, null),
			new PropertyDescriptor(PROP_SEQUENCES, SequenceCD.TYPEID.getArrayType(), 
				PropertyValue.createEmptyArray(SequenceCD.TYPEID), false, false, null)
		);
	}
	
	protected List<? extends Presenter> createPresenters() {
		return null;
	}
	
	//this would be the ideal set of properties for a sprite - however the current sprite editor
	//doesn't actually modify most of these properties :(
	 
//	public static final TypeID TYPEID = new TypeID (TypeID.Kind.COMPONENT, "java.microedition.lcdui.game.Sprite"); // NOI18N
//	 
//	private static final String PROP_IMAGE = "image"; // NOI18N
//	private static final String PROP_FRAME_DIMENSION = "frameDimension"; // NOI18N
//	 
//	private static final String PROP_COLLISION_RECTANGLE = "collisionRectangle"; // NOI18N
//	 
//	private static final String PROP_DEFINE_REFERENCE_PIXEL = "defineReferencePixel"; // NOI18N
//	private static final String PROP_REFERENCE_PIXEL = "referencePixel"; // NOI18N
//	 
//	private static final String PROP_TRANSFORMATION = "transformation"; // NOI18N
//	 
//	private static final String PROP_FRAME = "currentFrame"; // NOI18N
//	private static final String PROP_FRAME_SEQUENCE = "sequence"; // NOI18N
//	 
//	private static final int VALUE_TRANS_NONE = 0;
//	private static final int VALUE_TRANS_MIRROR_ROT180 = 1;
//	private static final int VALUE_TRANS_MIRROR = 2;
//	private static final int VALUE_TRANS_ROT180 = 3;
//	private static final int VALUE_TRANS_MIRROR_ROT270 = 4;
//	private static final int VALUE_TRANS_ROT90 = 5;
//	private static final int VALUE_TRANS_ROT270 = 6;
//	private static final int VALUE_TRANS_MIRROR_ROT90 = 7;
//	 
//	public TypeDescriptor getTypeDescriptor() {
//		return new TypeDescriptor(null, TYPEID, true, false);
//	}
//	 
//	public VersionDescriptor getVersionDescriptor() {
//		return null;
//	}
//	 
//	public List<PropertyDescriptor> getDeclaredPropertyDescriptors() {
//		return Arrays.asList (
//			new PropertyDescriptor (PROP_IMAGE, ImageResourceCD.TYPEID, PropertyValue.createNull (), true, false, null),
//			new PropertyDescriptor (PROP_FRAME_DIMENSION, GameTypes.TYPEID_DIMENSION, PropertyValue.createNull (), true, false, null),
//			new PropertyDescriptor (PROP_COLLISION_RECTANGLE, GameTypes.TYPEID_RECTANGLE, PropertyValue.createNull (), true, false, null),
//			new PropertyDescriptor (PROP_DEFINE_REFERENCE_PIXEL, GameTypes.TYPEID_POINT, PropertyValue.createNull (), true, false, null),
//			new PropertyDescriptor (PROP_REFERENCE_PIXEL, GameTypes.TYPEID_POINT, PropertyValue.createNull (), true, false, null),
//			new PropertyDescriptor (PROP_TRANSFORMATION, MidpTypes.TYPEID_INT, MidpTypes.createIntegerValue (VALUE_TRANS_NONE), false, false, null),
//			new PropertyDescriptor (PROP_FRAME, MidpTypes.TYPEID_INT, PropertyValue.createNull (), true, false, null),
//			new PropertyDescriptor (PROP_FRAME_SEQUENCE, SpriteSequenceCD.TYPEID, PropertyValue.createNull (), true, false, null)
//		);
//	}
//	 
//	protected List<? extends Presenter> createPresenters() {
//		return null;
//	}
	 
}
