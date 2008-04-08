/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.visualweb.faces.dt.component.html;

import com.sun.rave.designtime.*;
import org.netbeans.modules.visualweb.faces.dt.HtmlDesignInfoBase;
import javax.faces.component.NamingContainer;
import javax.faces.component.UIComponent;
import java.io.Serializable;
import java.util.StringTokenizer;
import java.util.List;
import java.util.ArrayList;
import javax.faces.component.UIForm;
import javax.faces.component.html.HtmlForm;

public class HtmlFormDesignInfo extends HtmlDesignInfoBase {

    private static final String ID_SEP = String.valueOf(NamingContainer.SEPARATOR_CHAR);
    private static final String ID_WILD = String.valueOf("*");  //NOI18N
    private static final String VF_DELIM_1 = ",";   //NOI18N
    private static final String VF_DELIM_2 = "|";   //NOI18N

    public Class getBeanClass() {
        return HtmlForm.class;
    }

    public DisplayAction[] getContextItems(DesignBean lbean) {
        return super.getContextItems(lbean);
    }

    public boolean acceptLink(DesignBean targetBean, DesignBean sourceBean, Class sourceClass) {
        return false;
    }

    public Result linkBeans(DesignBean targetBean, DesignBean sourceBean) {
        return null;
    }
    
    /**
     * Allow form anywhere, so long as parent is not a form and the parent has
     * no form ancestor.
     */
    public boolean acceptParent(DesignBean parentBean, DesignBean childBean, Class childClass) {        
        DesignBean thisBean = parentBean;
        while (thisBean.getBeanParent() != null) {
            if (thisBean.getInstance() instanceof UIForm)
                return false;
            thisBean = thisBean.getBeanParent();
        }       
        return true;
    }

    /**
     * <p>Designtime version of
     * <code>Form.getFullyQualifiedId(UIComponent)</code> for jsfcl.
     */
    /*
     * Be sure to keep this method in sync with the versions in
     * <code>com.sun.rave.web.ui.component.Form</code> (in webui) and
     * <code>com.sun.rave.web.ui.component.FormDesignInfo</code>
     * (in webui).</p>
     */
    public static String getFullyQualifiedId(DesignBean bean) {
        if (bean == null) {
            return null;
        }
        Object beanInstance = bean.getInstance();
        if (! (beanInstance instanceof UIComponent)) {
            return null;
        }
        if (beanInstance instanceof UIForm) {
            return ID_SEP;
        }
        String compId = bean.getInstanceName();
        if (compId == null) {
            return null;
        }
        StringBuffer sb = new StringBuffer(compId);
        DesignBean currentBean = bean.getBeanParent();
        boolean formEncountered = false;
        while (currentBean != null) {
            Object currentBeanInstance = currentBean.getInstance();
            if (currentBeanInstance instanceof UIComponent) {
                sb.insert(0, ID_SEP);
                if (currentBeanInstance instanceof UIForm) {
                    formEncountered = true;
                    break;
                }
                else {
                    String currentCompId = currentBean.getInstanceName();
                    if (currentCompId == null) {
                        return null;
                    }
                    sb.insert(0, currentCompId);
                }
            }
            currentBean = currentBean.getBeanParent();
        }
        if (formEncountered) {
            return sb.toString();
        }
        else {
            return null;
        }
    }

    /**
     * <p>Designtime version of
     * <code>Form.isValidFullyQualifiedId(String)</code> for jsfcl.
     */
    /*
     * Be sure to keep this method in sync with the version in
     * <code>com.sun.rave.web.ui.component.Form</code> (in webui).</p>
     */
    public static boolean isValidFullyQualifiedId(String id) {
        return id != null &&
                id.startsWith(ID_SEP) &&
                (id.length() == 1 || !id.endsWith(ID_SEP)) &&
                !id.endsWith(ID_WILD) &&
                id.indexOf(' ') == -1;
    }

    /**
     * <p>Designtime version of
     * <code>Form.fullyQualifiedIdMatchesPattern(String, String)</code>
     * for jsfcl.
     */
    /*
     * Be sure to keep this method in sync with the version in
     * <code>com.sun.rave.web.ui.component.Form</code> (in webui).</p>
     */
    public static boolean fullyQualifiedIdMatchesPattern(String fqId, String pattern) {
        if (!isValidFullyQualifiedId(fqId)) {
            return false;
        }
        if (pattern == null || pattern.length() < 1 || pattern.indexOf(' ') != -1) {
            return false;
        }
        //unless pattern is ":", it should not end with ":"
        if (pattern.endsWith(ID_SEP) && !pattern.equals(ID_SEP)) {
            return false;
        }
        
        String wildSuffix = ID_SEP + ID_WILD;
        
        //if ID_WILD appears in pattern, it must be the last character, and preceded by ID_SEP
        int indexOfWildInPattern = pattern.indexOf(ID_WILD);
        if (indexOfWildInPattern != -1) {
            if (indexOfWildInPattern != pattern.length() - 1) {
                return false;
            }
            if (!pattern.endsWith(wildSuffix)) {
                return false;
            }
        }
        
        if (pattern.equals(wildSuffix)) {
            //if pattern was ":*", then any valid fqId is a match
            return true;
        }
        else if (pattern.endsWith(wildSuffix)) {
            String patternPrefix = pattern.substring(0, pattern.length() - wildSuffix.length());
            if (patternPrefix.startsWith(ID_SEP)) {
                return fqId.equals(patternPrefix) || fqId.startsWith(patternPrefix + ID_SEP);
            }
            else {
                return fqId.endsWith(ID_SEP + patternPrefix) || fqId.indexOf(ID_SEP + patternPrefix + ID_SEP) > -1;
            }
        }
        else {
            if (pattern.startsWith(ID_SEP)) {
                return fqId.equals(pattern);
            }
            else {
                return fqId.endsWith(ID_SEP + pattern);
            }
        }
    }
    
    /** 
     * <p>Designtime version of 
     * <code>Form.generateVirtualForms(String)</code> 
     * for jsfcl.
     */
    /*
     * Be sure to keep this method in sync with the version in 
     * <code>com.sun.rave.web.ui.component.Form</code> (in webui).</p>
     */
    public static VirtualFormDescriptor[] generateVirtualForms(String configStr) {
        //formname1|pid1 pid2 pid3|sid1 sid2 sid3, formname2|pid4 pid5 pid6|sid4 sid5 sid6
        if (configStr == null) {
            return null;
        }
        configStr = configStr.trim();
        if (configStr.length() < 1) {
            return new VirtualFormDescriptor[0];
        }
        //configStr now can't be null, blank, or just ws
        
        StringTokenizer st = new StringTokenizer(configStr, VF_DELIM_1);
        List vfs = new ArrayList(); //list of marshalled vfs
        while (st.hasMoreTokens()) {
            String vf = st.nextToken(); //not null, but could be just whitespace or blank
            vf = vf.trim();
            //vf could be a blank string
            if (vf.length() > 0) {
                vfs.add(vf);
            }
        }
        
        List descriptors = new ArrayList();       //a list of VirtualFormDescriptors
        for (int i = 0; i < vfs.size(); i++) {    //go through each marshalled vf
            String vf = (String)vfs.get(i);                //get the marshalled vf. not mere ws, blank, or null.
            st = new StringTokenizer(vf, VF_DELIM_2);
            String[] parts = new String[3];   //part1 is vf name, part2 is participating ids, part3 is submitting ids
            int partIndex = 0;
            while (partIndex < parts.length && st.hasMoreTokens()) {
                String part = st.nextToken();   //not null, but could be whitespace or blank
                part = part.trim(); //now can't be whitespace, but could be blank
                if (part.length() > 0)  {
                    //part is not null, whitespace, or blank
                    parts[partIndex] = part;
                }
                partIndex++;
            }
            
            VirtualFormDescriptor vfd;
            if (parts[0] != null) {
                vfd = new VirtualFormDescriptor();
                vfd.setName(parts[0]);  //won't be null, blank, or just ws
                descriptors.add(vfd);
            } 
            else {
                continue;   //this marshalled vf has no name. can't create a descriptor for it. go to next marshalled vf
            }
            
            if (parts[1] != null) {
                String pidString = parts[1];    //not null, blank, or just ws
                st = new StringTokenizer(pidString);
                List pidList = new ArrayList();
                while (st.hasMoreTokens()) {
                    String pid = st.nextToken();
                    pidList.add(pid.trim());
                }
                String[] pids = (String[])pidList.toArray(new String[pidList.size()]);  //size guaranteed to be at least 1
                vfd.setParticipatingIds(pids);
            }
            
            if (parts[2] != null) {
                String sidString = parts[2];    //not null, blank, or just ws
                st = new StringTokenizer(sidString);
                List sidList = new ArrayList();
                while (st.hasMoreTokens()) {
                    String sid = st.nextToken();
                    sidList.add(sid.trim());
                }
                String[] sids = (String[])sidList.toArray(new String[sidList.size()]);  //size guaranteed to be at least 1
                vfd.setSubmittingIds(sids);
            }
        }
        return (VirtualFormDescriptor[])descriptors.toArray(new VirtualFormDescriptor[descriptors.size()]); //might be of size 0, but won't be null
    }
    
    /** 
     * <p>Designtime version of 
     * <code>Form.generateVirtualFormsConfig(VirtualFormDescriptor[])</code> 
     * for jsfcl.
     */
    /*
     * Be sure to keep this method in sync with the version in 
     * <code>com.sun.rave.web.ui.component.Form</code> (in webui).</p>
     */
    public static String generateVirtualFormsConfig(VirtualFormDescriptor[] descriptors) {
        if (descriptors == null) {
            return null;
        }
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < descriptors.length; i++) {
            if (descriptors[i] != null) {
                String vf = descriptors[i].toString();
                if (vf.length() > 0) {
                    if (sb.length() > 0) {
                        sb.append(" , ");
                    }
                    sb.append(vf);
                }
            }
        }
        return sb.toString();
    }
    
    /** 
     * <p>Designtime version of 
     * <code>Form.VirtualFormDescriptor</code> 
     * for jsfcl.
     */
    /*
     * Be sure to keep this class in sync with the version in 
     * <code>com.sun.rave.web.ui.component.Form</code> (in webui).</p>
     */
    public static class VirtualFormDescriptor implements Serializable {
        private String name;    //name of the virtual form
        private String[] participatingIds;      //ids of components that participate
        private String[] submittingIds;      //ids of components that submit
        
        public VirtualFormDescriptor() {}
        
        public VirtualFormDescriptor(String name) {
            setName(name);
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            if (name == null) {
                throw new IllegalArgumentException("virtual form name was null");   //NOI18N
            }
            name = name.trim();
            if (name.length() < 1) {
                throw new IllegalArgumentException("virtual form name contained whitespace only");    //NOI18N
            }
            this.name = name;
        }
        
        public String[] getParticipatingIds()  {
            return participatingIds;
        }
        
        public void setParticipatingIds(java.lang.String[] participatingIds)  {
            for (int i = 0; participatingIds != null && i < participatingIds.length; i++) {
                if (participatingIds[i] == null) {
                    throw new IllegalArgumentException("participating id at index " + i + " was null"); //NOI18N
                }
                participatingIds[i] = participatingIds[i].trim();
                if (participatingIds[i].length() < 1) {
                    throw new IllegalArgumentException("participating id at index " + i + " contained whitespace only"); //NOI18N
                }
            }
            this.participatingIds = participatingIds;
        }
        
        public String[] getSubmittingIds()  {
            return submittingIds;
        }
        
        public void setSubmittingIds(java.lang.String[] submittingIds)  {
            for (int i = 0; submittingIds != null && i < submittingIds.length; i++) {
                if (submittingIds[i] == null) {
                    throw new IllegalArgumentException("submitting id at index " + i + " was null"); //NOI18N
                }
                submittingIds[i] = submittingIds[i].trim();
                if (submittingIds[i].length() < 1) {
                    throw new IllegalArgumentException("submitting id at index " + i + " contained whitespace only"); //NOI18N
                }
            }
            this.submittingIds = submittingIds;
        }
        
        //return true if the component id provided submits this virtual form
        public boolean isSubmittedBy(String fqId) {
            if (!isValidFullyQualifiedId(fqId)) return false;
            for (int i = 0; submittingIds != null && i < submittingIds.length; i++) {
                if (fullyQualifiedIdMatchesPattern(fqId, submittingIds[i])) {
                    return true;
                }
            }
            return false;
        }
        
        //return true if the component id provided participates in this virtual form
        public boolean hasParticipant(String fqId) {
            if (!isValidFullyQualifiedId(fqId)) return false;
            for (int i = 0; participatingIds != null && i < participatingIds.length; i++) {
                if (fullyQualifiedIdMatchesPattern(fqId, participatingIds[i])) {
                    return true;
                }
            }
            return false;
        }
        
        public String toString() {
            if (name == null) {return "";}  //NOI18N
            StringBuffer sb = new StringBuffer();
            sb.append(name);
            sb.append(" | ");  //NOI18N
            for (int i = 0; participatingIds != null && i < participatingIds.length; i++) {
                sb.append(participatingIds[i]);
                sb.append(' ');
            }
            sb.append("| ");    //NOI18N
            for (int i = 0; submittingIds != null && i < submittingIds.length; i++) {
                sb.append(submittingIds[i]);
                sb.append(' ');
            }
            return sb.toString().trim();
        }
    }
}
