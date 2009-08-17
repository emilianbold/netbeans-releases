/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.compapp.javaee.util;

import java.util.HashMap;
import java.util.Map;
import org.openide.util.NbBundle;

/**
 *
 * @author gpatil
 */
public class JavaEEVerifierReportItem {
    private static String OK;
    private static String WARNING;
    private static String ERROR;
    private static Map<String, String> DISPLAY_NAMES;
    
    public static String KEY_FL_NAME = "_filename" ; //NOI18N
    public static String KEY_JNDI_NAME = "_jndiname" ; //NOI18N
    public static String KEY_EXPECTED_CLASS = "_jndinameClass" ; //NOI18N
    public static String KEY_MSG = "_message" ; //NOI18N
    public static String KEY_STATUS = "_status" ; //NOI18N
    public static String KEY_REFERENCING_CLASS = "_referencerclass" ; //NOI18N
    public static String KEY_REFERENCING_EJB = "_referencer" ; //NOI18N
    
    private String fileName; //_filename
    private String jndiName; //_jndiname
    private String expectedClass; //_jndinameClass
    private String message; //_message
    private String status; //_status
    private String referencingClass; //_referencerclass
    private String referencingEjb; //_referencer

    static {
        OK = NbBundle.getMessage(JavaEEVerifierReportItem.class, "msg_ok"); //NOI18N
        WARNING = NbBundle.getMessage(JavaEEVerifierReportItem.class, "msg_warning"); //NOI18N
        ERROR = NbBundle.getMessage(JavaEEVerifierReportItem.class, "msg_error"); //NOI18N
    }
    
    public JavaEEVerifierReportItem(){    
    }
    
    public String getExpectedClass() {
        return expectedClass;
    }

    public void setExpectedClass(String expectedClass) {
        this.expectedClass = expectedClass;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getJndiName() {
        return jndiName;
    }

    public void setJndiName(String jndiName) {
        this.jndiName = jndiName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getReferencingClass() {
        return referencingClass;
    }

    public void setReferencingClass(String referencingClass) {
        this.referencingClass = referencingClass;
    }

    public String getReferencingEjb() {
        return referencingEjb;
    }

    public void setReferencingEjb(String referencingEjb) {
        this.referencingEjb = referencingEjb;
    }

    public String getStatus() {
        return status;
    }

    public String getStatusStr() {
        int s = Integer.parseInt(status);
        String ret = OK;
        if (s == 1){
            ret = WARNING;
        }else if (s == 2){
            ret = ERROR;
        }
        return ret;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    private static String getMsg(String key){
        return NbBundle.getMessage(JavaEEVerifierReportItem.class, key);
    }
    
    public synchronized static Map<String, String> getDisplayNames(){
        if (DISPLAY_NAMES == null){
            DISPLAY_NAMES = new HashMap<String, String>();
            DISPLAY_NAMES.put(KEY_FL_NAME, getMsg("lbl_fileName")); //NOI18N
            DISPLAY_NAMES.put(KEY_STATUS, getMsg("lbl_status")); //NOI18N
            DISPLAY_NAMES.put(KEY_JNDI_NAME, getMsg("lbl_jndiName")); //NOI18N
            DISPLAY_NAMES.put(KEY_EXPECTED_CLASS, getMsg("lbl_exp_cls")); //NOI18N;
            DISPLAY_NAMES.put(KEY_MSG, getMsg("lbl_msg")); //NOI18N
            DISPLAY_NAMES.put(KEY_REFERENCING_CLASS, getMsg("lbl_refng_cls")); //NOI18N;
            DISPLAY_NAMES.put(KEY_REFERENCING_EJB, getMsg("lbl_refng_ejb")); //NOI18N;
        }
        
        return DISPLAY_NAMES;
    }
    
    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append(this.fileName);
        sb.append(":"); //NOI18N
        sb.append(this.status);
        sb.append(":"); //NOI18N
        sb.append(this.jndiName);
        sb.append(":"); //NOI18N
        sb.append(this.message);
        sb.append(":"); //NOI18N
        sb.append(this.expectedClass);
        sb.append(":"); //NOI18N
        sb.append(this.referencingClass);
        sb.append(":"); //NOI18N
        sb.append(this.referencingEjb);
        return sb.toString();
    }
}
