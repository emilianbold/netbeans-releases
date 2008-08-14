/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.vmd.game.model;

import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.game.integration.GameCodeSupport;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * @author David Kaspar, Karel Herink
 */
public class SpriteCD extends ComponentDescriptor {
	
	public static final TypeID TYPEID = new TypeID(TypeID.Kind.COMPONENT, "org.netbeans.modules.vmd.game.model.Sprite"); // NOI18N
	
	public TypeDescriptor getTypeDescriptor() {
		return new TypeDescriptor(LayerCD.TYPEID, TYPEID, true, false);
	}
	
	public VersionDescriptor getVersionDescriptor() {
		return null;
	}
	
	public List<PropertyDescriptor> getDeclaredPropertyDescriptors() {
		return SequenceContainerCDProperties.getDeclaredPropertyDescriptors();
	}
	
	protected List<? extends Presenter> createPresenters() {
            List<String> fqnForImport = new LinkedList<String>();
		return Arrays.asList (
            // code
            GameCodeSupport.createSpriteCodePresenter ( fqnForImport ),
            GameCodeSupport.createAddImportPresenter(fqnForImport)
        );
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
