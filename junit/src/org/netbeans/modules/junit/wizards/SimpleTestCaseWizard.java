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
 * Software is Sun Microsystems, Inc. Portions Copyright 2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.junit.wizards;

import org.openide.loaders.TemplateWizard;

/**
 *
 * @author  Marian Petras
 */
public class SimpleTestCaseWizard extends TemplateWizard {

    static final String PROP_CLASS_TO_TEST = "classToTest";             //NOI18N
    static final String PROP_TEST_ROOT_FOLDER = "testRootFolder";       //NOI18N

    /** Creates a new instance of SimpleTestCaseWizard */
    public SimpleTestCaseWizard() {
    }

    /**
     * initializes the settings for the settings panel
     */
    protected void initialize() {
    }
    
}
