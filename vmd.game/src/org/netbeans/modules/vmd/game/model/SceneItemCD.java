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
                    //assert getComponent().readProperty(PROPERTY_LAYER).getKind() != PropertyValue.Kind.NULL;
                }
            }
        );
	}
	
}
