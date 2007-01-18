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

import java.util.Arrays;
import java.util.List;
import org.netbeans.modules.vmd.api.model.PropertyDescriptor;
import org.netbeans.modules.vmd.api.model.PropertyValue;

/**
 *
 * @author kaja
 */
public class SequenceContainerCDProperties {
	
	public static final String PROP_DEFAULT_SEQUENCE = "sequencecontainer.prop.defaultsequence"; // NOI18N
	public static final String PROP_SEQUENCES = "sequencecontainer.prop.sequences"; // NOI18N

	public static List<PropertyDescriptor> getDeclaredPropertyDescriptors() {
		return Arrays.asList(
			new PropertyDescriptor(PROP_DEFAULT_SEQUENCE, SequenceCD.TYPEID, 
				PropertyValue.createEmptyArray(SequenceCD.TYPEID), false, false, null),
			new PropertyDescriptor(PROP_SEQUENCES, SequenceCD.TYPEID.getArrayType(), 
				PropertyValue.createEmptyArray(SequenceCD.TYPEID), false, false, null)
		);
	}

	
}
