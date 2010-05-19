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

package org.netbeans.modules.dm.virtual.db.ui.property;

import java.beans.PropertyEditor;

/**
 * @author Ritesh Adval
 */
public interface IProperty extends INode {


    public String getGroup();

    public int getPosition();

    public IPropertyCustomizer getPropertyCustomizer();

    public PropertyEditor getPropertyEditor();

    public String getType();

    public Object getValue() throws java.lang.IllegalAccessException, java.lang.reflect.InvocationTargetException;

    public Class getValueType();

    public boolean isCustomEditor();

    public boolean isDefault();

    public boolean isReadOnly();

    public boolean isRequired();

    public boolean isValid();

    public void setCustomEditor(String editor);

    public void setDefault(boolean def);

    public void setDefault(String def);

    public void setEditorClass(String editorClass);

    public void setGroup(String group);

    public void setOwnerObject(Object obj);

    public void setPosition(String position);

    public void setPropertyCustomizer(IPropertyCustomizer customizer);

    public void setPropertyEditor(PropertyEditor editor);

    public void setReadOnly(boolean readOnly);

    public void setReadOnly(String readOnly);

    public void setRequired(boolean required);

    public void setRequired(String required);

    public void setType(String type);

    public void setValue(Object obj) throws java.lang.IllegalAccessException, java.lang.IllegalArgumentException,
            java.lang.reflect.InvocationTargetException;

    public void setValueType(String valueType);

}

