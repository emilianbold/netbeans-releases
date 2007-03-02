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
package org.netbeans.modules.visualweb.web.ui.dt.component.propertyeditors;

import org.netbeans.modules.visualweb.propertyeditors.ValidatorPropertyEditor;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.faces.validator.*;

/**
 * An extension of the base validator property editor that filters out validators
 * added by JSF 1.2. This editor is a hack, necessitated by the fact that at present
 * both releases of JSF are represented at design-time by just the JSF 1.2 library.
 *
 * @author gjmurphy
 */
public class JSF1_1ValidatorPropertyEditor extends ValidatorPropertyEditor {

    private static Set<Class> facesValidatorClassSet = new HashSet<Class>();

    static {
        facesValidatorClassSet.add(DoubleRangeValidator.class);
        facesValidatorClassSet.add(LengthValidator.class);
        facesValidatorClassSet.add(LongRangeValidator.class);
    }

    private Class[] ValidatorClasses;

    protected Class[] getValidatorClasses() {
        if (ValidatorClasses == null) {
            Class[] inheritedValidatorClasses = super.getValidatorClasses();
            List<Class> ValidatorClassList = new ArrayList<Class>();
            for (Class ValidatorClass : inheritedValidatorClasses) {
                if (!ValidatorClass.getCanonicalName().startsWith("javax.faces.validator.") ||
                        facesValidatorClassSet.contains(ValidatorClass))
                    ValidatorClassList.add(ValidatorClass);
            }
            ValidatorClasses = ValidatorClassList.toArray(new Class[ValidatorClassList.size()]);
        }
        return ValidatorClasses;
    }

}
