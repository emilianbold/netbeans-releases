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

import com.sun.rave.propertyeditors.ConverterPropertyEditor;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.faces.convert.*;

/**
 * An extension of the base converter property editor that filters out converters
 * added by JSF 1.2. This editor is a hack, necessitated by the fact that at present
 * both releases of JSF are represented at design-time by just the JSF 1.2 library.
 *
 * @author gjmurphy
 */
public class JSF1_1ConverterPropertyEditor extends ConverterPropertyEditor {

    private static Set<Class> facesConverterClassSet = new HashSet<Class>();

    static {
        facesConverterClassSet.add(BigDecimalConverter.class);
        facesConverterClassSet.add(BigIntegerConverter.class);
        facesConverterClassSet.add(BooleanConverter.class);
        facesConverterClassSet.add(ByteConverter.class);
        facesConverterClassSet.add(CharacterConverter.class);
        facesConverterClassSet.add(DateTimeConverter.class);
        facesConverterClassSet.add(DoubleConverter.class);
        facesConverterClassSet.add(FloatConverter.class);
        facesConverterClassSet.add(IntegerConverter.class);
        facesConverterClassSet.add(LongConverter.class);
        facesConverterClassSet.add(NumberConverter.class);
        facesConverterClassSet.add(ShortConverter.class);
    }

    private Class[] converterClasses;

    protected Class[] getConverterClasses() {
        if (converterClasses == null) {
            Class[] inheritedConverterClasses = super.getConverterClasses();
            List<Class> converterClassList = new ArrayList<Class>();
            for (Class converterClass : inheritedConverterClasses) {
                if (!converterClass.getCanonicalName().startsWith("javax.faces.convert.") ||
                        facesConverterClassSet.contains(converterClass))
                    converterClassList.add(converterClass);
            }
            converterClasses = converterClassList.toArray(new Class[converterClassList.size()]);
        }
        return converterClasses;
    }

}
