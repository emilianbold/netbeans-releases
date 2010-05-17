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


package org.netbeans.modules.xml.schema.ui.nodes;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Enumeration;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.openide.ErrorManager;
import org.openide.nodes.Node;


/**
 * This class provides a wrapper which invokes flush on the schema model
 * when setValue has been invoked.
 * @author Chris Webster
 */
public class SchemaModelFlushWrapper extends Node.Property {
    private Node.Property delegate;
    private SchemaModel model;
    
    public SchemaModelFlushWrapper(SchemaComponent sc, Node.Property delegate) {
        super(delegate.getValueType());
        model = sc.getModel();
        this.delegate = delegate;
    } 
    
    @Override
    public void setValue(Object object) throws IllegalAccessException, 
    IllegalArgumentException, InvocationTargetException {
		try {
		model.startTransaction();
        delegate.setValue(object);
		}
		finally {
			model.endTransaction();
		}
    }

    @Override
    public void restoreDefaultValue() throws IllegalAccessException, 
    InvocationTargetException {
		try {
		model.startTransaction();
        delegate.restoreDefaultValue();
		}
		finally {
			model.endTransaction();
		}
    }
    
    @Override
    public boolean equals(Object object) {
        return delegate.equals(object);
    }

    @Override
    public void setExpert(boolean expert) {
        delegate.setExpert(expert);
    }

    @Override
    public void setHidden(boolean hidden) {
        delegate.setHidden(hidden);
    }

    @Override
    public void setPreferred(boolean preferred) {
        delegate.setPreferred(preferred);
    }

    @Override
    public void setShortDescription(String text) {
        delegate.setShortDescription(text);
    }

    @Override
    public Object getValue(String attributeName) {
        return delegate.getValue(attributeName);
    }

    @Override
    public void setDisplayName(String displayName) {
        delegate.setDisplayName(displayName);
    }

    @Override
    public void setName(String name) {
        delegate.setName(name);
    }

    @Override
    public void setValue(String attributeName, Object value) {
        delegate.setValue(attributeName, value);
    }

    @Override
    public String toString() {
        return delegate.toString();
    }

    @Override
    public boolean supportsDefaultValue() {
        return delegate.supportsDefaultValue();
    }

    @Override
    public Object getValue() throws IllegalAccessException, 
    InvocationTargetException {
        return delegate.getValue();
    }

    @Override
    public String getShortDescription() {
        return delegate.getShortDescription();
    }

    @Override
    public java.beans.PropertyEditor getPropertyEditor() {
        return delegate.getPropertyEditor();
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public String getHtmlDisplayName() {
        return delegate.getHtmlDisplayName();
    }

    @Override
    public String getDisplayName() {
        return delegate.getDisplayName();
    }
    
    @Override
    public boolean canWrite() {
        return delegate.canWrite();
    }

    @Override
    public boolean canRead() {
        return delegate.canRead();
    }

    @Override
    public Enumeration<String> attributeNames() {
        return delegate.attributeNames();
    }
   
    @Override
    public Class getValueType() {
        return delegate.getValueType();
    }
   
    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public boolean isDefaultValue() {
        return delegate.isDefaultValue();
    }
    
    @Override
    public boolean isExpert() {
        return delegate.isExpert();
    }

    @Override
    public boolean isHidden() {
        return delegate.isHidden();
    }

    @Override
    public boolean isPreferred() {
        return delegate.isPreferred();
    }

    
}
