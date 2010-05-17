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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.j2ee.websphere6.dd.beans;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;
/**
 *
 * @author dlm198383
 */
public class SpecialSubjectType extends org.netbeans.modules.schema2beans.BaseBean implements DDXmiConstants {

    static Vector comparators = new Vector();
    private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(4, 2, 0);



    public SpecialSubjectType() {
        this(Common.USE_DEFAULT_VALUES);
    }
    public SpecialSubjectType(int options) {
        super(comparators, runtimeVersion);
        // Properties (see root bean comments for the bean graph)
        initPropertyTables(1);
        this.initialize(options);
    }
    public void initialize(int options) {
        
    }
    public void setDefaults() {
        setXmiId("Special_");
        setName(SPECIAL_SUBJECTS_TYPE_EVERYONE_STRING+"_");
        setType(SPECIAL_SUBJECTS_TYPE_EVERYONE);
    }
    
    
    public static void addComparator(org.netbeans.modules.schema2beans.BeanComparator c) {
        comparators.add(c);
    }
    
    public static void removeComparator(org.netbeans.modules.schema2beans.BeanComparator c) {
        comparators.remove(c);
    }
    
    public void setXmiId(String value) {
        this.setAttributeValue(SPECIAL_SUBJECTS_XMI_ID,value);
    }
    
    public String getXmiId() {
        return (String)this.getAttributeValue(SPECIAL_SUBJECTS_XMI_ID);
    }
    public void setName(String value) {
        this.setAttributeValue(SPECIAL_SUBJECTS_NAME,value);
    }
    public String getName() {
        return (String)this.getAttributeValue(SPECIAL_SUBJECTS_NAME);
    }
    public void setType(String value) {
        if(value.equals(SPECIAL_SUBJECTS_TYPE_ALL_AUTHENTICATED_USERS)) {
            this.setAttributeValue(SPECIAL_SUBJECTS_TYPE,
                    SPECIAL_SUBJECTS_TYPE_ALL_AUTHENTICATED_USERS_STRING);
        } else if(value.equals(SPECIAL_SUBJECTS_TYPE_EVERYONE)) {
            this.setAttributeValue(SPECIAL_SUBJECTS_TYPE,
                    SPECIAL_SUBJECTS_TYPE_EVERYONE_STRING);
        } else {
            this.setAttributeValue(SPECIAL_SUBJECTS_TYPE,value);
        }
    }
    public String getType() {
        String str=(String)this.getAttributeValue(SPECIAL_SUBJECTS_TYPE);
        if(str==null) {
            return null;
        } else if(str.equals(SPECIAL_SUBJECTS_TYPE_EVERYONE_STRING)) {
            return SPECIAL_SUBJECTS_TYPE_EVERYONE;
        } else if(str.equals(SPECIAL_SUBJECTS_TYPE_ALL_AUTHENTICATED_USERS_STRING)) {
            return SPECIAL_SUBJECTS_TYPE_ALL_AUTHENTICATED_USERS;
        } else {
            return str;
        }
    }
    
    
    public void validate() throws org.netbeans.modules.schema2beans.ValidateException {
        if(getXmiId()==null) {
            throw new org.netbeans.modules.schema2beans.ValidateException("getXmiId() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, "SpecialSubjectType", this);	// NOI18N
        }
        if(getName()==null) {
            throw new org.netbeans.modules.schema2beans.ValidateException("getName() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, "SpecialSubjectType", this);	// NOI18N
        }
        if(getType()==null) {
            throw new org.netbeans.modules.schema2beans.ValidateException("getType() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, "SpecialSubjectType", this);	// NOI18N
        }
    }
    
    
    
    // Dump the content of this bean returning it as a String
    public void dump(StringBuffer str, String indent){
        String s;
        Object o;
        org.netbeans.modules.schema2beans.BaseBean n;
        str.append(indent);
        str.append(SPECIAL_SUBJECTS);
    }
    public String dumpBeanNode(){
        StringBuffer str = new StringBuffer();
        str.append(getClass().getName());	// NOI18N
        this.dump(str, "\n  ");	// NOI18N
        return str.toString();
    }
}
