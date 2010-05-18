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

/**
 * This interface is the intersection of all generated methods.
 *
 * @Generated
 */

package org.netbeans.modules.uml.codegen.dataaccess.xmlbeans;

public interface CommonBean
{
    public org.w3c.dom.Comment addComment(String comment);
    
    public void addPropertyChangeListener(String n, java.beans.PropertyChangeListener l);
    
    public void addPropertyChangeListener(java.beans.PropertyChangeListener l);
    
    public int addValue(String name, Object value);
    
    public org.netbeans.modules.schema2beans.BaseBean[] childBeans(boolean recursive);
    
    public void childBeans(boolean recursive, java.util.List beans);
    
    public Object clone();
    
    public org.w3c.dom.Comment[] comments();
    
    public String dtdName();
    
    public void dump(StringBuffer str, String indent);
    
    public boolean equals(Object obj);
    
    public String fullName();
    
    public String[] getAttributeNames();
    
    public String[] getAttributeNames(String propName);
    
    public String getAttributeValue(String name);
    
    public String getAttributeValue(String propName, String name);
    
    public String getAttributeValue(String propName, int index, String name);
    
    public String getDefaultNamespace();
    
    public Object getValue(String name);
    
    public Object getValue(String name, int index);
    
    public Object[] getValues(String name);
    
    public int indexOf(String name, Object value);
    
    public boolean isChoiceProperty();
    
    public boolean isChoiceProperty(String name);
    
    public boolean isNull(String name);
    
    public boolean isNull(String name, int index);
    
    public boolean isRoot();
    
    public void merge(org.netbeans.modules.schema2beans.BaseBean bean);
    
    public void merge(org.netbeans.modules.schema2beans.BaseBean bean, int mode);
    
    public String name();
    
    public org.netbeans.modules.schema2beans.BaseBean parent();
    
    public void reindent();
    
    public void removeComment(org.w3c.dom.Comment comment);
    
    public void removePropertyChangeListener(String n, java.beans.PropertyChangeListener l);
    
    public void removePropertyChangeListener(java.beans.PropertyChangeListener l);
    
    public int removeValue(String name, Object value);
    
    public void removeValue(String name, int index);
    
    public void setAttributeValue(String propName, int index, String name, String value);
    
    public void setDefaultNamespace(String namespace);
    
    public void setValue(String name, Object value);
    
    public void setValue(String name, Object[] value);
    
    public void setValue(String name, int index, Object value);
    
    public int size(String name);
    
    public String toString();
    
    public void validate() throws org.netbeans.modules.schema2beans.ValidateException;
    
    public void write(java.io.OutputStream out) throws java.io.IOException, org.netbeans.modules.schema2beans.Schema2BeansRuntimeException;
    
    public void write(java.io.OutputStream out, String encoding) throws java.io.IOException, org.netbeans.modules.schema2beans.Schema2BeansException;
    
    public void write(java.io.Writer w) throws java.io.IOException, org.netbeans.modules.schema2beans.Schema2BeansException;
    
    public void write(java.io.Writer w, String encoding) throws java.io.IOException, org.netbeans.modules.schema2beans.Schema2BeansException;
    
    public void writeNoReindent(java.io.OutputStream out) throws java.io.IOException, org.netbeans.modules.schema2beans.Schema2BeansException;
    
}
