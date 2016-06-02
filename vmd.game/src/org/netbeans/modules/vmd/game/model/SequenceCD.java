/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2007, 2016 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 *
 * Contributor(s):
 */

package org.netbeans.modules.vmd.game.model;

import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.game.integration.components.GameTypes;
import org.netbeans.modules.vmd.game.integration.GameCodeSupport;
import org.netbeans.modules.vmd.midp.components.MidpTypes;

import java.util.Arrays;
import java.util.List;

/**
 *
 * @author kherink
 */
public class SequenceCD extends ComponentDescriptor {
	
	public static final TypeID TYPEID = new TypeID(TypeID.Kind.COMPONENT, "org.netbeans.modules.vmd.game.model.Sequence"); // NOI18N
	
	public static final String PROPERTY_IMAGE_RESOURCE = "sequncecd.prop.imageresource"; // NOI18N
	public static final String PROPERTY_NAME = "sequncecd.prop.name"; // NOI18N
	public static final String PROPERTY_FRAME_MS = "sequncecd.prop.framems"; // NOI18N
	public static final String PROPERTY_FRAMES = "sequncecd.prop.frames"; // NOI18N
	public static final String PROPERTY_FRAME_WIDTH = "sequncecd.prop.frame.width"; // NOI18N
	public static final String PROPERTY_FRAME_HEIGHT = "sequncecd.prop.frame.height"; // NOI18N
	public static final String PROPERTY_ZERO_BASED_INDEX = "sequncecd.prop.zero.based.index"; // NOI18N
	
	
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
				PropertyValue.createNull(), false, false, Versionable.FOREVER),
			new PropertyDescriptor(PROPERTY_FRAME_WIDTH, MidpTypes.TYPEID_INT,
				PropertyValue.createNull(), false, false, Versionable.FOREVER),
			new PropertyDescriptor(PROPERTY_FRAME_HEIGHT, MidpTypes.TYPEID_INT,
				PropertyValue.createNull(), false, false, Versionable.FOREVER),
			new PropertyDescriptor(PROPERTY_ZERO_BASED_INDEX, MidpTypes.TYPEID_BOOLEAN,
				PropertyValue.createNull(), false, false, Versionable.FOREVER)
		);
	}
	
	public VersionDescriptor getVersionDescriptor() {
		return null;
	}
	
	protected List<? extends Presenter> createPresenters() {
		return Arrays.asList (
            // code
            GameCodeSupport.createSequenceCodePresenter ()
        );
	}
	
}
