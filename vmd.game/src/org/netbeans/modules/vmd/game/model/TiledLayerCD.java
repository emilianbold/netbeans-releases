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
import org.netbeans.modules.vmd.game.integration.components.GameTypes;
import org.netbeans.modules.vmd.game.integration.GameCodeSupport;

import java.util.Arrays;
import java.util.List;

/**
 * @author David Kaspar, Karel Herink
 */
public class TiledLayerCD extends ComponentDescriptor {
	
	public static final TypeID TYPEID = new TypeID(TypeID.Kind.COMPONENT, "org.netbeans.modules.vmd.game.model.TiledLayer"); // NOI18N
	
	public static final String PROPERTY_TILES = "tiledlayercd.prop.tiles"; // NOI18N
	
	public TypeDescriptor getTypeDescriptor() {
		return new TypeDescriptor(LayerCD.TYPEID, TYPEID, true, false);
	}
	
	public VersionDescriptor getVersionDescriptor() {
		return null;
	}
	
	public void postInitialize(DesignComponent component) {
	}
	
	public List<PropertyDescriptor> getDeclaredPropertyDescriptors() {
		return Arrays.asList(
			new PropertyDescriptor(PROPERTY_TILES, GameTypes.TYPEID_TILED_LAYER_TILES, 
				PropertyValue.createNull(), false, false, null)
		);
	}
	
	protected List<? extends Presenter> createPresenters() {
		return Arrays.asList (
            // code
            GameCodeSupport.createTiledLayerCodePresenter ()
        );
	}
	
}
