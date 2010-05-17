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
package org.netbeans.modules.visualweb.propertyeditors;

import com.sun.rave.propertyeditors.util.JavaInitializer;
import java.text.MessageFormat;

/**
 * A property editor for <code>long</code> or <code>java.lang.Long</code>. By default,
 * values that fall outside of the range <code>Integer.MIN_VALUE</code> ...
 * <code>Integer.MAX_VALUE</code> will be rejected.
 *
 * @author gjmurphy
 */
public class LongPropertyEditor extends NumberPropertyEditor implements
        com.sun.rave.propertyeditors.LongPropertyEditor {

    public static final Long DEFAULT_MIN_VALUE = new Long(Long.MIN_VALUE);

    public static final Long DEFAULT_MAX_VALUE = new Long(Long.MAX_VALUE);

    public LongPropertyEditor() {
        super(DEFAULT_MIN_VALUE, DEFAULT_MAX_VALUE);
    }

    protected Number parseString(String str) throws IllegalTextArgumentException {
        try {
            return Long.valueOf(str);
        } catch (NumberFormatException e) {
            throw new IllegalTextArgumentException(
                    MessageFormat.format(bundle.getString("LongPropertyEditor.formatErrorMessage"),
                    new String[]{str}), e);
        }
    }

    public String getJavaInitializationString() {
        Class c = this.getDesignProperty().getPropertyDescriptor().getPropertyType();
        if (c.equals(Long.class))
            return JavaInitializer.toJavaInitializationString((Long) this.getValue());
        return this.getValue().toString();
    }
}
