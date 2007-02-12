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

package org.netbeans.modules.j2ee.common.method.impl;

import java.util.Collections;
import javax.lang.model.element.Modifier;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.j2ee.common.method.MethodModel;
import org.openide.DialogDescriptor;

/**
 *
 * @author Martin Adamek
 */
public class ValidatingPropertyChangeListenerTest extends NbTestCase {
    
    public ValidatingPropertyChangeListenerTest(String testName) {
        super(testName);
    }
    
    public void testValidate() {
        MethodModel methodModel = MethodModel.create(
                "m1",
                "void",
                null,
                Collections.<MethodModel.Variable>emptyList(),
                Collections.<String>emptyList(),
                Collections.<Modifier>emptySet()
                );
        MethodCustomizerPanel mcPanel = MethodCustomizerPanel.create(
                methodModel,
                false,
                false,
                false,
                false,
                true,
                null,
                false,
                true,
                false
                );
        DialogDescriptor dialogDescriptor = new DialogDescriptor("Test", "Test");
        ValidatingPropertyChangeListener validator = new ValidatingPropertyChangeListener(mcPanel, dialogDescriptor);
        assertTrue(validator.validate());
        mcPanel = MethodCustomizerPanel.create(
                methodModel,
                false,
                false,
                false,
                false,
                true,
                null,
                false,
                true,
                true
                );
        validator = new ValidatingPropertyChangeListener(mcPanel, dialogDescriptor);
        assertFalse(validator.validate());
    }
    
}
