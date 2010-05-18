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

import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * A property editor for strings that represent length measurements, as
 * defined by HTML 4.01. The value may be either a non-negative integer,
 * which represents an absolute length measured in pixels; or a percentage,
 * which represents a percentage of the available screen dimension. The
 * editor will also normalize the submitted string before it is set, e.g.
 * "<code>000123</code>" will be submitted as "<code>123</code>", and
 * "<code> 50 % </code>" will be submitted as "<code>50%</code>".
 *
 * @author gjmurphy
 */
public class LengthPropertyEditor extends PropertyEditorBase implements
        com.sun.rave.propertyeditors.LengthPropertyEditor {

    static ResourceBundle bundle =
            ResourceBundle.getBundle(LengthPropertyEditor.class.getPackage().getName() + ".Bundle"); //NOI18N

    public String getAsText() {
        if (this.getValue() == null && super.unsetValue == null)
            return "";
        return super.getAsText();
    }

    public void setAsText(String text) throws IllegalArgumentException {
        text = text == null ? "" : text.trim();
        if (text.length() > 0) {
            try {
                int value;
                if (text.endsWith("%")) {
                    value = Integer.parseInt(text.substring(0, text.length()-1).trim());
                    if (value < 0 || value > 100)
                        throw new IllegalArgumentException();
                    text = Integer.toString(value) + "%";
                } else {
                    value = Integer.parseInt(text);
                    text = Integer.toString(value);
                }
            } catch( NumberFormatException e ) {
                throw new IllegalTextArgumentException(
                        MessageFormat.format(bundle.getString("LengthPropertyEditor.formatErrorMessage"),
                        new String[]{text}), e);
            }
            super.setValue(text);
        } else {
            super.setValue(null);
        }
    }

}
