/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.visualweb.faces.dt_1_1.propertyeditors;

import org.netbeans.modules.visualweb.propertyeditors.ConverterPropertyEditor;
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
