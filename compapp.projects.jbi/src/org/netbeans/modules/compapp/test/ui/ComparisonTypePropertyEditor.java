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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.compapp.test.ui;

import java.util.logging.Logger;
import org.openide.util.NbBundle;

/**
 * FeaturesStatusPropertyEditor.java
 *
 * Created on February 14, 2006, 6:13 PM
 *
 * @author Bing Lu
 */
public class ComparisonTypePropertyEditor extends EnumPropertyEditor {
    private static final Logger mLogger = Logger.getLogger("org.netbeans.modules.compapp.test.ui.FeaturesStatusPropertyEditor"); // NOI18N

    /** Creates a new instance of FeaturesStatusPropertyEditor */
    public ComparisonTypePropertyEditor() {
        super(new String[]{
                    NbBundle.getMessage(ComparisonTypePropertyEditor.class, "comparisionTypePropertyEditor.identical.displayName"), // NOI18N
                    NbBundle.getMessage(ComparisonTypePropertyEditor.class, "comparisionTypePropertyEditor.binary.displayName"), // NOI18N
                    NbBundle.getMessage(ComparisonTypePropertyEditor.class, "comparisionTypePropertyEditor.equals.displayName"), // NOI18N
              },
              new Object[]{
                    "identical", // NOI18N
                    "binary", // NOI18N
                    "equals"} // NOI18N
        );
    }
    
}
