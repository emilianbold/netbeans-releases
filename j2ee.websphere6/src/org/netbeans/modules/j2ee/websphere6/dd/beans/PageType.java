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
public class PageType extends org.netbeans.modules.schema2beans.BaseBean implements DDXmiConstants {

    static Vector comparators = new Vector();
    private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(4, 2, 0);



    public PageType() {
        this(Common.USE_DEFAULT_VALUES);
    }
    public PageType(int options) {
        super(comparators, runtimeVersion);
        // Properties (see root bean comments for the bean graph)
        initPropertyTables(1);
        this.initialize(options);
    }
    public void initialize(int options) {
       
    }
    public void setDefaults() {
         setXmiId("Page_");
         setName("");
         setUri("/");
    }
    
   
    public static void addComparator(org.netbeans.modules.schema2beans.BeanComparator c) {
        comparators.add(c);
    }
         
    public static void removeComparator(org.netbeans.modules.schema2beans.BeanComparator c) {
        comparators.remove(c);
    }
    
    public void setXmiId(String value) {
        this.setAttributeValue(PAGES_XMI_ID,value);
    }
    
    public String getXmiId() {
        return (String)this.getAttributeValue(PAGES_XMI_ID);
    }
    public void setName(String value) {
        this.setAttributeValue(PAGES_NAME,value);
    }
    public String getName() {
        return (String)this.getAttributeValue(PAGES_NAME);
    }
    public void setUri(String value) {
        this.setAttributeValue(PAGES_URI,value);
    }
    public String getUri() {
        return (String)this.getAttributeValue(PAGES_URI);
    } 
    
    
    public void validate() throws org.netbeans.modules.schema2beans.ValidateException {
        
        if(getXmiId()==null) {
            throw new org.netbeans.modules.schema2beans.ValidateException("getXmiId() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, "PageType", this);	// NOI18N
        }
        if(getName()==null) {
            throw new org.netbeans.modules.schema2beans.ValidateException("getName() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, "PageType", this);	// NOI18N
        }
        if(getUri()==null) {
            throw new org.netbeans.modules.schema2beans.ValidateException("getUri() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, "PageType", this);	// NOI18N
        }
        
                
        
    }
    
    
    
    // Dump the content of this bean returning it as a String
    public void dump(StringBuffer str, String indent){
        String s;
        Object o;
        org.netbeans.modules.schema2beans.BaseBean n;
        str.append(indent);
        str.append("PageType");	// NOI18N
        //this.dumpAttributes(PAGE, 0, str, indent);
        
        
    }
    public String dumpBeanNode(){
        StringBuffer str = new StringBuffer();
        str.append(getClass().getName());	// NOI18N
        this.dump(str, "\n  ");	// NOI18N
        return str.toString();
    }
}
