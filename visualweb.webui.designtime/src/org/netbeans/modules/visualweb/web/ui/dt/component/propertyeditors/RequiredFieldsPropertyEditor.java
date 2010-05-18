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
package org.netbeans.modules.visualweb.web.ui.dt.component.propertyeditors;


import java.beans.PropertyEditorSupport;
import com.sun.rave.designtime.DesignProperty;
import com.sun.rave.designtime.PropertyEditor2;


/**
 *A property editor for the <code>requiredFields</code> property of Property Sheet component
 *
 * @author Gowri
 */
public class RequiredFieldsPropertyEditor extends PropertyEditorSupport implements PropertyEditor2 {

    static final int UNSET = 0;
    static final int TRUE = 1;
    static final int FALSE = 2;

    DesignProperty designProperty;
    String tags[] = new String[3];

    /**
     * Creates a new instance of RequiredFieldsPropertyEditor
     */
    public RequiredFieldsPropertyEditor() {
        tags[UNSET] = "";
        tags[TRUE] = Boolean.TRUE.toString();
        tags[FALSE] = Boolean.FALSE.toString();
    }

    /**
     * Set the design property for this editor.
     */
    public void setDesignProperty(DesignProperty designProperty) {
        this.designProperty = designProperty;
    }

    public void setAsText(String text) throws IllegalArgumentException {
        if (text.equals(tags[UNSET])) {
            this.setValue(null);
        } else if (text.equals(tags[FALSE])) {
            this.setValue(tags[FALSE]);
        } else {
            this.setValue(tags[TRUE]);
        }
    }

    public String getAsText() {
        Object value = this.getValue();
        if (value == null)
            return tags[UNSET];
        if (value.equals(tags[TRUE])) {
            return tags[TRUE];
        } else {
            return tags[FALSE];
        }
    }

    public String[] getTags() {
        return tags;
    }
}
