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
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
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

package org.netbeans.modules.jmx;

import java.util.List;
import java.util.ArrayList;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.jmx.common.WizardConstants;

/**
 * class representing a MBean Attribute.
 * @author tl156378
 */
public class MBeanAttribute implements Comparable {
    
    private String name;
    private String typeName;
    private String description;
    private String access;
    private ExecutableElement getter;
    private List<String> getterExceptions = new ArrayList();
    private ExecutableElement setter;
    private List<String> setterExceptions = new ArrayList();
    private boolean isReadable;
    private boolean isWritable;
    private boolean isMethodExits = false;
    private boolean getMethodExits = false;
    private boolean setMethodExits = false;
    private boolean wrapped = false;
    private List classParameterTypes;
    private boolean isArray;
    private TypeMirror mirror;
    /** Creates a new instance of MBeanAttribute */
    public MBeanAttribute(String name, String description,
            ExecutableElement getter, ExecutableElement setter,
            List<? extends TypeParameterElement> classParameterTypes, CompilationInfo info) {
        this.name = name;
        this.description = description;
        this.getter = getter;
        this.setter = setter;
        this.classParameterTypes = classParameterTypes;
        if (getter != null) {
            List<? extends TypeParameterElement> methodParameterTypes = getter.getTypeParameters();
            TypeMirror type = getter.getReturnType();
            mirror = type;
            typeName = JavaModelHelper.getTypeName(type, methodParameterTypes, classParameterTypes, info);
            this.isReadable = true;
            boolean hasIsMethod = (getter.getSimpleName().toString().startsWith("is")); // NOI18N
            this.getMethodExits = !hasIsMethod;
            this.isMethodExits = hasIsMethod;
            
            if (setter != null)
                this.access = WizardConstants.ATTR_ACCESS_READ_WRITE;
            else
                this.access = WizardConstants.ATTR_ACCESS_READ_ONLY;
        } else {
            List<? extends TypeParameterElement> methodParameterTypes = setter.getTypeParameters();
            TypeMirror type = setter.getParameters().get(0).asType();
            typeName = JavaModelHelper.getTypeName(type, methodParameterTypes, classParameterTypes, info);
            mirror = type;
            this.access = WizardConstants.ATTR_ACCESS_WRITE_ONLY;
            this.setMethodExits = true;
        }
    }
    public TypeMirror getTypeMirror() {
        return mirror;
    }
    /**
     * Constructor
     * @param attrName the attribute name
     * @param attrType the attribute type
     * @param attrAccess the attribute access mode
     * @param attrDescription the attribute description
     */
    public MBeanAttribute(String attrName, String attrType, String attrAccess,
            String attrDescription, TypeMirror mirror) {
        this.name = attrName;
        this.typeName = attrType;
        this.access = attrAccess;
        this.description = attrDescription;
        this.getter = null;
        this.setter = null;
        this.mirror = mirror;
    }
    
    /**
     * Constructor
     * @param attrName the attribute name
     * @param attrType the attribute type
     * @param attrAccess the attribute access mode
     * @param attrDescription the attribute description
     * @param isIntrospected true only if attribute have been discovered
     */
    public MBeanAttribute(String attrName, String attrType, String attrAccess,
            String attrDescription, boolean isIntrospected, TypeMirror mirror) {
        this.name = attrName;
        this.typeName = attrType;
        this.access = attrAccess;
        this.description = attrDescription;
        this.wrapped = isIntrospected;
        this.getter = null;
        this.setter = null;
        this.mirror = mirror;
    }
    
    /**
     * Constructor
     * @param attrName the attribute name
     * @param attrType the attribute type
     * @param attrAccess the attribute access mode
     */
    public MBeanAttribute(String attrName, String attrType, String attrAccess) {
        this.name = attrName;
        this.typeName = attrType;
        this.access = attrAccess;
        this.description = ""; // NOI18N
        this.getter = null;
        this.setter = null;
    }
    
    public List getClassParameterTypes() {
        return classParameterTypes;
    }
    
    /**
     * Returns the name of the MBean attribute.
     * @return String the name of the MBean
     */
    public String getName() {
        return name;
    }
    
    /**
     * Sets the attribute name
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }
    
    public boolean isReadable() {
        return (WizardConstants.ATTR_ACCESS_READ_ONLY.equals(access) ||
                WizardConstants.ATTR_ACCESS_READ_WRITE.equals(access));
    }
    
    public boolean isWritable() {
        return (WizardConstants.ATTR_ACCESS_WRITE_ONLY.equals(access) ||
                WizardConstants.ATTR_ACCESS_READ_WRITE.equals(access));
    }
    
    /**
     * Sets the attribute description.
     * @param descr the attribute description to set
     */
    public void setDescription(String descr) {
        this.description = descr;
    }
    
    /**
     * Returns the description for the MBean.
     * @return String the description for the MBean
     */
    public String getDescription() {
        return description;
    }
    
    public ExecutableElement getGetter() {
        return getter;
    }
    
    public ExecutableElement getSetter() {
        return setter;
    }
    
    public List<String> getGetterExceptions() {
        return getterExceptions;
    }
    
    public List<String> getSetterExceptions() {
        return setterExceptions;
    }
    
    public void setGetterExceptions(List<String> exceptions) {
        this.getterExceptions = exceptions;
    }
    
    public void setSetterExceptions(List<String> exceptions) {
        this.setterExceptions = exceptions;
    }
    
    public String getTypeName() {
        return typeName;
    }
    
    /**
     * Sets the attribute access mode.
     * @param access the access mode to set (RO or R/W)
     */
    public void setAccess(String access) {
        this.access = access;
    }
    
    /**
     * Returns the access permission for the MBean.
     * @return String the access permission for the MBean
     */
    public String getAccess() {
        return access;
    }
    
    /**
     * Sets the attribute type name.
     * @param type the type to set
     */
    public void setTypeName(String type) {
        this.typeName = type;
    }
    
    public boolean getIsMethodExits() {
        return isMethodExits;
    }
    
    public void setIsMethodExits(boolean isMethodExits) {
        this.isMethodExits = isMethodExits;
    }
    
    public boolean getGetMethodExits() {
        return getMethodExits;
    }
    
    public void setGetMethodExits(boolean getMethodExits) {
        this.getMethodExits = getMethodExits;
    }
    
    public boolean getSetMethodExits() {
        return setMethodExits;
    }
    
    public void setSetMethodExits(boolean setMethodExits) {
        this.setMethodExits = setMethodExits;
    }
    
    public boolean isWrapped() {
        return wrapped;
    }
    
    public void setWrapped(boolean wrapped) {
        this.wrapped = wrapped;
    }
    
    public int compareTo(Object o) {
        String concat = name + typeName;
        MBeanAttribute obj = (MBeanAttribute) o;
        //System.out.println("COMPARTE TO " + obj);
        //System.out.println("COMPARTE TO " + obj.getName() + " " + obj.getTypeName());
        return concat.compareTo(obj.getName() + obj.getTypeName());
    }
}
