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

package com.sun.rave.designtime.markup;

import java.beans.PropertyEditor;

/**
 * <p>The AttributePropertyEditor interface extends the PropertyEditor interface (a special type of
 * property editor for markup attributes) to add the ability to persist and resolve complex types
 * in markup.  Normally, JavaBeans properties are persisted using the PropertyEditor method:
 * "getJavaInitializationString()", which must return a valid Java expression as a String.  When
 * an IDE rebuilds the state of a saved file, a Java compiler is used to interpret the property
 * settings.</p>
 *
 * <p>In markup attributes, a Java expression is not typically what is used to store a setting -
 * and thus a Java compiler cannot be used to interpret the expressions stored in markup.  This
 * interface is designed to replace the "getJavaInitializationString()" and compiler interpretation
 * steps in property persistence.</p>
 *
 * <P><B>IMPLEMENTED BY THE COMPONENT AUTHOR</B> - This interface is designed to be implemented by
 * the component (bean) author.</P>
 *
 * @author Joe Nuxoll
 * @version 1.0
 */
public interface AttributePropertyEditor extends PropertyEditor {

    /**
     * <p>This method is intended for use when generating markup to set the value of a property.  It
     * should return a fragment of markup that can be used to initialize a variable with the current
     * property value.  It is not necessary to include the quote marks, as any attribute string will
     * be automatically enclosed in quotes.  If quotes are included, they will not double-up.</p>
     *
     * <p>Example results are "2", "#FF00FF", "#{WebPage1.button1}", etc.</b>
     *
     * @return A fragment of markup representing an initializer for the current value of this
     *         PropertyEditor.  This will be enclosed in quote marks in the markup.
     */
    public String getMarkupInitializationString();

    /**
     * This method is called while "resurrecting" the state of a DesignBean from markup.  The string
     * stored as the attribute value will be passed in (initString), and an instance of the
     * appropriate type for the property setting is expected to be returned.
     *
     * @param initString The String stored as the attribute value
     * @return The appropriately typed instance representing the property setting.
     */
    public Object resolveMarkupInitializationString(String initString);
}
