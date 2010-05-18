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
package com.sun.rave.web.ui.component;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;
import javax.faces.component.EditableValueHolder;
import javax.faces.component.NamingContainer;
import javax.faces.component.UIComponent;
import javax.faces.component.UIData;
import javax.faces.context.FacesContext;
import javax.faces.event.FacesEvent;
import javax.faces.event.ActionEvent;
import com.sun.data.provider.RowKey;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.Set;
import java.util.HashSet;
import java.util.Collection;
import java.util.Arrays;
import com.sun.rave.web.ui.util.MessageUtil;

/**
 * <p>Component that represents an input form.</p>
 */

public class Form extends FormBase {
    private VirtualFormDescriptor submittedVirtualForm;                //the virtual form that was submitted
    private static final String VF_DELIM_1 = ",";   //NOI18N
    private static final String VF_DELIM_2 = "|";   //NOI18N
    private static final String ID_SEP = String.valueOf(NamingContainer.SEPARATOR_CHAR);
    public static final char ID_WILD_CHAR = '*';
    private static final String ID_WILD = String.valueOf(ID_WILD_CHAR);
    private transient Map erasedMap = new HashMap();  //has an EditableValueHolder as the key, and an Object[] value pair or a TableValues as the value
    private transient Set nonDefaultRetainStatusEvhs = new HashSet(); //contains EditableValueHolders with a retain status different from the default
    private static final boolean DEFAULT_RETAIN_STATUS = true; //default for whether non-participating submitted values are retained
    
    /**
     * <p>Override <code>UIForm.processDecodes(FacesContext)</code> to ensure
     *	    correct virtual form processing.</p>
     *
     * @param context <code>FacesContext</code> for the current request
     *
     * @exception NullPointerException Thrown when <code>context</code> is null
     */
    public void processDecodes(FacesContext context) {
        if (context == null) {
            throw new NullPointerException();
        }
        
        submittedVirtualForm = null;
        erasedMap.clear();
        //clearing out nonDefaultRetainStatusEvhs occurs in restoreNonParticipatingSubmittedValues
        //(which is called during renderering)
        //so that application code can muck with the retain statuses in preprocess
        
        // Process this component itself
        decode(context);    //may set submittedVirtualForm
        
        // if we're not the submitted form, don't process children.
        if (!isSubmitted()) {
            return;
        }
        
        // Process all facets and children of this component
        Iterator kids = getFacetsAndChildren();
        while (kids.hasNext()) {
            UIComponent kid = (UIComponent)kids.next();
            kid.processDecodes(context);
        }
        
        if (submittedVirtualForm != null) {
            //if the children of the Form are known to participate in submittedVirtualForm,
            //then don't bother erasing
            if (!childrenAreKnownToParticipate(this, submittedVirtualForm)) {
                eraseVirtualFormNonParticipants(this, null, null);
            }
        }
    }
    
    public void queueEvent(FacesEvent event) {
        FacesEvent relevantEvent = event;
        if (event instanceof WrapperEvent) {
            WrapperEvent wrapperEvent = (WrapperEvent)event;
            relevantEvent = wrapperEvent.getFacesEvent();
        }
        if (relevantEvent instanceof ActionEvent && submittedVirtualForm == null) {
            UIComponent sourceComp = relevantEvent.getComponent();
            VirtualFormDescriptor virtualFormComponentSubmits = getVirtualFormComponentSubmits(sourceComp);
            if (virtualFormComponentSubmits != null) {  //if the source component does in fact submit a virtual form
                submittedVirtualForm = virtualFormComponentSubmits;   //then set that virtual form as having been submitted
            }
        }
        super.queueEvent(event);
    }
    
    public void setSubmittedVirtualForm(VirtualFormDescriptor vfd) {
        if (submittedVirtualForm == null && vfd != null) {
            submittedVirtualForm = vfd;
        }
    }
    
    public void setVirtualForms(VirtualFormDescriptor[] vfds) {
        setVirtualForms(vfds, true);
    }
    
    private void setVirtualForms(VirtualFormDescriptor[] vfds, boolean sync) {
        super.setVirtualForms(vfds);
        if (sync) {
            String configStr = generateVirtualFormsConfig(vfds);
            setVirtualFormsConfig(configStr, false);
        }
    }
    
    public void setVirtualFormsConfig(String configStr) {
        setVirtualFormsConfig(configStr, true);
    }
    
    private void setVirtualFormsConfig(String configStr, boolean sync) {
        super.setVirtualFormsConfig(configStr);
        if (sync) {
            VirtualFormDescriptor[] vfds = generateVirtualForms(configStr);
            setVirtualForms(vfds, false);
        }
    }
    
    private VirtualFormDescriptor getVirtualFormComponentSubmitsByFullyQualifiedId(String fqId) {
        if (!isValidFullyQualifiedId(fqId)) {
            return null;
        }
        
        //first try regular configuration
        VirtualFormDescriptor vfd = getVirtualFormComponentSubmitsByFullyQualifiedId(fqId, getVirtualForms());
        if (vfd != null) {
            return vfd;
        }
        
        //try internal configuration
        vfd = getVirtualFormComponentSubmitsByFullyQualifiedId(fqId, getInternalVirtualForms());
        return vfd;
    }
    
    private VirtualFormDescriptor getVirtualFormComponentSubmitsByFullyQualifiedId(String fqId, VirtualFormDescriptor[] vfds) {
        if (vfds == null || vfds.length < 1) {
            return null;
        }
        
        //look for matches of fqId against all of vfds's submitters--without any trailing wilds.
        //if no match found, try the parent fqId.
        //this technique ensures that the most appropriate submitter is found first and its vf is returned
        String currentFqId = fqId;
        while (currentFqId.length() > 0) {
            for (int v = 0; v < vfds.length; v++) {
                VirtualFormDescriptor vfd = vfds[v];
                String[] submitters = vfd.getSubmittingIds();
                for (int s = 0; submitters != null && s < submitters.length; s++) {
                    String submitter = submitters[s];
                    if (submitter == null) continue;
                    String wildSuffix = ID_SEP + ID_WILD;
                    if (submitter.endsWith(wildSuffix)) {
                        submitter = submitter.substring(0, submitter.length() - wildSuffix.length());
                    }
                    if (submitter.length() < 1) continue;
                    boolean fqIdMatches = fullyQualifiedIdMatchesPattern(currentFqId, submitter);
                    if (fqIdMatches) {
                        return vfd;
                    }
                }
            }
            int lastIndexOfSep = currentFqId.lastIndexOf(ID_SEP);
            currentFqId = currentFqId.substring(0, lastIndexOfSep);
        }
        
        return null;
    }
    
    private int getVirtualFormCount() {
        VirtualFormDescriptor[] vfds = getVirtualForms();
        VirtualFormDescriptor[] ivfds = getInternalVirtualForms();
        return (vfds == null ? 0 : vfds.length) + (ivfds == null ? 0 : ivfds.length);
    }
    
    private VirtualFormDescriptor getVirtualFormComponentSubmits(UIComponent component) {
        if (getVirtualFormCount() < 1) {
            return null;
        }
        String fqId = getFullyQualifiedId(component);
        VirtualFormDescriptor vfd = getVirtualFormComponentSubmitsByFullyQualifiedId(fqId);
        return vfd;
    }
    
    /** Get the virtual form submitted by the component whose id is provided or null if the component does not submit a virtual form. */
    public VirtualFormDescriptor getVirtualFormComponentSubmits(String id) {
        if (getVirtualFormCount() < 1) {
            return null;
        }
        if (isValidFullyQualifiedId(id)) {
            return getVirtualFormComponentSubmitsByFullyQualifiedId(id);
        }
        UIComponent component = findComponentById(id);
        return getVirtualFormComponentSubmits(component);
    }
    
    //this method is public for designer's use
    /**
     * Given a bare, partially qualified, or fully qualified id, find the component.
     * Unlike the inherited <code>findComponent</code> method, this method does recursively 
     * search NamingContainers.
     */
    public UIComponent findComponentById(String id) {
        if (id == null) {
            return null;
        }
        if (id.length() == 0 || id.endsWith(ID_WILD) || (!id.equals(ID_SEP) && id.endsWith(ID_SEP)) ) {
            return null;
        }
        //see if id indicates the Form itself
        String fqId = getFullyQualifiedId(this);
        if (fullyQualifiedIdMatchesPattern(fqId, id)) {
            return this;
        }
        return searchKidsRecursivelyForId(this, id);
    }
    
    private UIComponent searchKidsRecursivelyForId(UIComponent parent, String id) {
        Iterator kids = parent.getFacetsAndChildren();
        while (kids.hasNext()) {
            UIComponent kid = (UIComponent)kids.next();
            String fqId = getFullyQualifiedId(kid);
            //see if id indicates kid
            boolean fqIdMatches = fullyQualifiedIdMatchesPattern(fqId, id);
            if (fqIdMatches) {
                return kid;
            }
            UIComponent match = searchKidsRecursivelyForId(kid, id);
            if (match != null) {
                return match;
            }
        }
        return null;
    }
    
    //return true if a virtual form has been submitted and this component participates in that virtual form
    private boolean participatesInSubmittedVirtualForm(UIComponent component) {
        if (submittedVirtualForm == null) {
            return false;
        }
        String fqId = getFullyQualifiedId(component);
        return submittedVirtualForm.hasParticipant(fqId);
    }
    
    /**
     * <p>Generate an 
     * array of <code>VirtualFormDescriptor</code>s based on a virtual form 
     * configuration <code>String</code>.</p>
     */
    /*
     * Be sure to keep this method in sync with the version in 
     * <code>javax.faces.component.html.HtmlFormDesignInfo</code> 
     * (in jsfcl).</p>
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
     * <p>Generate a virtual form 
     * configuration <code>String</code> based on an 
     * array of <code>VirtualFormDescriptor</code>s.</p>
     */
    /*
     * Be sure to keep this method in sync with the version in 
     * <code>javax.faces.component.html.HtmlFormDesignInfo</code> 
     * (in jsfcl).</p>
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
     * Obtain the virtual form compatible fully-qualified id for the supplied component.
     * A fully-qualified id begins with the <code>NamingContainer.SEPARATOR_CHAR</code>
     * (representing the <code>Form</code> itself), contains component ids of the
     * component's ancestors separated by <code>NamingContainer.SEPARATOR_CHAR</code>,
     * and ends with the component's id.
     */
    /*
     * Be sure to keep this method in sync with the versions in 
     * <code>javax.faces.component.html.HtmlFormDesignInfo</code> 
     * (in jsfcl) and 
     * <code>com.sun.rave.web.ui.component.FormDesignInfo</code> 
     * (in webui).</p>
     */
    public static String getFullyQualifiedId(UIComponent component) {
        if (component == null) {
            return null;
        }
        if (component instanceof Form) {
            return ID_SEP;
        }
        String compId = component.getId();
        if (compId == null) {
            return null;
        }
        StringBuffer sb = new StringBuffer(compId);
        UIComponent currentComp = component.getParent();
        boolean formEncountered = false;
        while (currentComp != null) {
            sb.insert(0, ID_SEP);
            if (currentComp instanceof Form) {
                formEncountered = true;
                break;
            }
            else {
                String currentCompId = currentComp.getId();
                if (currentCompId == null) {
                    return null;
                }
                sb.insert(0, currentCompId);
            }
            currentComp = currentComp.getParent();
        }
        if (formEncountered) {
            return sb.toString();
        }
        else {
            return null;
        }
    }
    
    /** 
     * Determine if the id provided is non-null and exhibits the traits of a 
     * fully qualified id. This includes beginning with 
     * <code>NamingContainer.SEPARATOR_CHAR</code>, not ending with that
     * character unless it is the only character, not ending in 
     * <code>Form.ID_WILD_CHAR</code>, and not containing spaces.
     */
    /*
     * Be sure to keep this method in sync with the version in 
     * <code>javax.faces.component.html.HtmlFormDesignInfo</code> 
     * (in jsfcl).</p>
     */
    public static boolean isValidFullyQualifiedId(String id) {
        return id != null && 
                id.startsWith(ID_SEP) && 
                (id.length() == 1 || !id.endsWith(ID_SEP)) && 
                !id.endsWith(ID_WILD) && 
                id.indexOf(' ') == -1;
    }
    
    /**
     * Determine if the fully qualified id provided matches the supplied pattern.
     * The pattern may be a bare, partially qualified, or fully qualified id.
     * The pattern may also end with the 
     * <code>NamingContainer.SEPARATOR_CHAR</code> followed by the 
     * <code>Form.ID_WILD_CHAR</code>, in which case the children of the component
     * indicated by the pattern will be considered a match.
     */
    /*
     * Be sure to keep this method in sync with the version in 
     * <code>javax.faces.component.html.HtmlFormDesignInfo</code> 
     * (in jsfcl).</p>
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
     * Add a <code>VirtualFormDescriptor</code> to the internal virtual forms.
     * If an existing VirtualFormDescriptor object is found with the same name,
     * the object is replaced.
     *
     * @param descriptor The <code>VirtualFormDescriptor</code> to add.
     */
    public void addInternalVirtualForm(VirtualFormDescriptor descriptor) {
        if (descriptor == null) {
            return;
        }

        // Get current descriptors.
        VirtualFormDescriptor[] oldDescriptors = getInternalVirtualForms();

        // Iterate over each VirtualFormDescriptor object and check for a match.
        if (oldDescriptors != null) {
            for (int i = 0; i < oldDescriptors.length; i++) {
                if (oldDescriptors[i] == null) {
                    continue;
                }
                String name = oldDescriptors[i].getName();
                if (name != null && name.equals(descriptor.getName())) {
                    oldDescriptors[i] = descriptor;
                    return; // No further processing is required.
                }
            }
        }

        // Create array to hold new descriptors.
        int oldLength = (oldDescriptors != null) ? oldDescriptors.length : 0;
        VirtualFormDescriptor[] newDescriptors = new VirtualFormDescriptor[oldLength + 1];
        for (int i = 0; i < oldLength; i++) {
            newDescriptors[i] = oldDescriptors[i];
        }

        // Add new VirtualFormDescriptor object.
        newDescriptors[oldLength] = descriptor;
        setInternalVirtualForms(newDescriptors);
    }

    /*
     * Be sure to keep this class in sync with the version in 
     * <code>javax.faces.component.html.HtmlFormDesignInfo</code> 
     * (in jsfcl).</p>
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
                throw new IllegalArgumentException(
                        getMessage("nullVfName", null)  //NOI18N
                        );
            }
            name = name.trim();
            if (name.length() < 1) {
                throw new IllegalArgumentException(
                        getMessage("vfNameWhitespaceOnly", null)  //NOI18N
                        );
            }
            this.name = name;
        }
        
        public String[] getParticipatingIds()  {
            return participatingIds;
        }
        
        public void setParticipatingIds(java.lang.String[] participatingIds)  {
            for (int i = 0; participatingIds != null && i < participatingIds.length; i++) {
                if (participatingIds[i] == null) {
                throw new IllegalArgumentException(
                        getMessage("nullParticipatingIdAtIndex", new Object[]{new Integer(i)})  //NOI18N
                        );
                }
                participatingIds[i] = participatingIds[i].trim();
                if (participatingIds[i].length() < 1) {
                throw new IllegalArgumentException(
                        getMessage("whitespaceOnlyParticipatingIdAtIndex", new Object[]{new Integer(i)})  //NOI18N
                        );
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
                throw new IllegalArgumentException(
                        getMessage("nullSubmittingIdAtIndex", new Object[]{new Integer(i)})  //NOI18N
                        );
                }
                submittingIds[i] = submittingIds[i].trim();
                if (submittingIds[i].length() < 1) {
                throw new IllegalArgumentException(
                        getMessage("whitespaceOnlySubmittingIdAtIndex", new Object[]{new Integer(i)})  //NOI18N
                        );
                }
            }
            this.submittingIds = submittingIds;
        }
        
        //return true if the component id provided submits this virtual form
        public boolean isSubmittedBy(String fqId) {
            if (!isValidFullyQualifiedId(fqId)) return false;
            for (int i = 0; submittingIds != null && i < submittingIds.length; i++) {
                if (Form.fullyQualifiedIdMatchesPattern(fqId, submittingIds[i])) {
                    return true;
                }
            }
            return false;
        }
        
        //return true if the component id provided participates in this virtual form
        public boolean hasParticipant(String fqId) {
            if (!isValidFullyQualifiedId(fqId)) return false;
            for (int i = 0; participatingIds != null && i < participatingIds.length; i++) {
                if (Form.fullyQualifiedIdMatchesPattern(fqId, participatingIds[i])) {
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
    
    //Examine the participating ids that end in ID_WILD. If any of them match
    //the component's fully qualified id, then the component's children are known
    //to participate in vfd.
    private static boolean childrenAreKnownToParticipate(UIComponent component, VirtualFormDescriptor vfd) {
        if (vfd == null) {
            return false;
        }
        
        String fqId = getFullyQualifiedId(component);
        if (fqId == null) {
            return false;
        }
        
        String[] participants = vfd.getParticipatingIds();
        for (int i = 0; participants != null && i < participants.length; i++) {
            String participant = participants[i];
            String wildSuffix = ID_SEP + ID_WILD;
            if (participant == null || !participant.endsWith(wildSuffix)) {
                continue;
            }
            if (fullyQualifiedIdMatchesPattern(fqId, participant)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * <p>Recursively erase virtual form non-participants by setting their 
     * submitted values to <code>null</code>. This method caches the submitted 
     * values in the <code>erasedMap</code> before actually erasing.
     * If the <code>parent</code> is embedded in one or more tables, 
     * the <code>contextualTables</code> and <code>contextualRows</code>
     * are used to record the table- and row-based context of the 
     * submitted value, so that such context can be stored in the 
     * <code>erasedMap</code>.</p>
     * <p><b>Note:</b> Restoring of submitted values works for 
     * the braveheart table but not the standard jsf table. However, submitted 
     * values inside a standard jsf table are still cached and an attempt is 
     * made to restore them, in case a third-party component extends 
     * <code>UIData</code> and, unlike <code>UIData</code>, does not discard 
     * its saved state during rendering.</p>
     * @param parent A parent component whose children will be examined and 
     * possibly erased
     * @param contextualTables an array of UIData or TableRowGroup components 
     * in the parent's ancestry (with the most distant ancestor as the first 
     * member of the array), or <code>null</code> if the parent is not embedded
     * within any tables
     * @param contextualRows a parallel array of <code>Integer</code> or 
     * <code>RowKey</code> objects representing a row in the corresponding 
     * contextual table, or <code>null</code> if the parent is not embedded
     * within any tables
     */ 
    private void eraseVirtualFormNonParticipants(UIComponent parent, Object[] contextualTables, Object[] contextualRows) {
        // Process all facets and children of this component
        synchronized (erasedMap) {  //prevent multiple threads from the same session simultaneously accessing erasedMap, nonDefaultRetainStatusEvhs
            Iterator kids = parent.getFacetsAndChildren();
            while (kids.hasNext()) {
                UIComponent kid = (UIComponent)kids.next();

                //if this kid is an EditableValueHolder, and it does not participate, set submitted value to null
                if (kid instanceof EditableValueHolder && !participatesInSubmittedVirtualForm(kid)) {
                    EditableValueHolder kidEvh = (EditableValueHolder)kid;
                    //cache the submitted value to be erased in eraseMap
                    Object submittedValueToErase = kidEvh.getSubmittedValue();
                    if (contextualTables == null) {
                        erasedMap.put(kidEvh, submittedValueToErase);
                    }
                    else {
                        addTableValuesEntry(erasedMap, kidEvh, 0, contextualTables, contextualRows, submittedValueToErase);
                    }
                    kidEvh.setSubmittedValue(null);
                }

                //if children of kid are known to participate in submittedVirtualForm,
                //then no need to recurse on kid
                if (childrenAreKnownToParticipate(kid, submittedVirtualForm)) {
                    continue;   //continue to next kid
                }

                //recurse. if kid is a UIData or TableRowGroup, perform a recursive call once per row.
                //if kid is not a UIData or TableRowGroup, simply perform a recursive call once.
                if (kid instanceof UIData) {
                    UIData kidTable = (UIData)kid;
                    int originalRowIndex = kidTable.getRowIndex();
                    int rowIndex = 0;
                    kidTable.setRowIndex(rowIndex);
                    while(kidTable.isRowAvailable()){
                        Object[] localContextualTables = appendToArray(contextualTables, kidTable);
                        Object[] localContextualRows = appendToArray(contextualRows, new Integer(rowIndex));
                        eraseVirtualFormNonParticipants(kidTable, localContextualTables, localContextualRows);
                        kidTable.setRowIndex(++rowIndex);
                    }
                    kidTable.setRowIndex(originalRowIndex);
                } 
                else if (kid instanceof TableRowGroup) {
                    TableRowGroup group = (TableRowGroup) kid;
                    RowKey[] rowKeys = group.getRowKeys();
                    RowKey oldRowKey = group.getRowKey(); // Save RowKey.

                    // Check for null TableDataProvider.
                    if (rowKeys != null) {
                        for (int i = 0; i < rowKeys.length; i++) {
                            group.setRowKey(rowKeys[i]);
                            if (!group.isRowAvailable()) {
                                continue;
                            }
                            Object[] localContextualTables = appendToArray(contextualTables, group);
                            Object[] localContextualRows = appendToArray(contextualRows, rowKeys[i]);
                            eraseVirtualFormNonParticipants(group, localContextualTables, localContextualRows);
                        }
                    }
                    group.setRowKey(oldRowKey); // Restore RowKey.
                } 
                else {
                    eraseVirtualFormNonParticipants(kid, contextualTables, contextualRows);
                }
            }
        }
    }
    
    /** <p>Recursively add an entry to <code>erasedMap</code>, whose value is a 
     * <code>TableValues</code> (and whose key is an 
     * <code>EditableValueHolder</code>).</p>
     */
    private void addTableValuesEntry(Map map, Object mapKey, int c, Object[] contextualTables, Object[] contextualRows, Object submittedValueToErase) {
        //use the map and mapKey to get a TableValues, using contextualTables[c] to create tsv if necessary
        TableValues tv = (TableValues)map.get(mapKey);
        if (tv == null) {
            tv = new TableValues(contextualTables[c]);
            map.put(mapKey, tv);
        }

        //ensure an entry is populated in tv.values with contextualRows[c] as the key
        Map values = tv.getValues();
        if (c == contextualTables.length - 1) {        //if last index in contextualTables
            values.put(contextualRows[c], submittedValueToErase);
        }
        else {
            addTableValuesEntry(values, contextualRows[c], c + 1, contextualTables, contextualRows, submittedValueToErase);
        }
    }
    
    private static Object[] appendToArray(Object[] array, Object item) {
        Object[] result;
        if (array == null) {
            result = new Object[]{item};
        }
        else {
            result = new Object[array.length + 1];
            System.arraycopy(array, 0, result, 0, array.length);
            result[array.length] = item;
        }
        return result;
    }
    
    /** 
     * <p>Restore the submitted values erased by the virtual form mechanism 
     * where appropriate.
     * This method is called in <code>FormRenderer.renderStart</code>. It should
     * not be called by developer code.</p>
     * <p><b>Note:</b> Restoring of submitted values works on 
     * <code>TableRowGroup</code> components, but does not work on the 
     * standard faces data table component. This is because in 
     * <code>UIData.encodeBegin</code>, the table's per-row saved state is 
     * typically discarded. The result is that upon
     * exiting <code>FormRenderer.renderStart</code>, the submitted values will 
     * be restored; however, they will subsequently be discarded.
     * Nonetheless, we still cache and restore those submitted values, in case
     * a third-party component extends 
     * <code>UIData</code> and, unlike <code>UIData</code>, does not discard 
     * its saved state during rendering.</p>
     */ 
    public void restoreNonParticipatingSubmittedValues() {
        synchronized (erasedMap) {    //prevent multiple threads from the same session simultaneously accessing erasedMap, nonDefaultRetainStatusEvhs
            for (Iterator iter = erasedMap.entrySet().iterator(); iter.hasNext(); ) {
                Map.Entry entry = (Map.Entry)iter.next();
                EditableValueHolder evh = (EditableValueHolder)entry.getKey();
                //if evh is designated as discarding submitted values, do not restore
                //if DEFAULT_RETAIN_STATUS==true (retain by default), then nonDefaultRetainStatusEvhs contains evhs that discard
                //if DEFAULT_RETAIN_STATUS==false (discard by default), then nonDefaultRetainStatusEvhs contains evhs that retain
                boolean evhAppearsInSet = nonDefaultRetainStatusEvhs.contains(evh);
                boolean discards = DEFAULT_RETAIN_STATUS ? evhAppearsInSet : !evhAppearsInSet;
                if (discards) {
                    continue;
                }
                //restore
                Object erasedMapValue = entry.getValue();
                if (erasedMapValue instanceof TableValues) {
                    TableValues tv = (TableValues)erasedMapValue;
                    restoreTableValues(tv, evh);
                }
                else {
                    evh.setSubmittedValue(erasedMapValue);
                }
            }
            nonDefaultRetainStatusEvhs.clear();   //after restoring, clear out the retain status data
        }
    }
    
    /** 
     *  <p>Helper method to restore submitted values for an
     *  <code>EditableValueHolder</code> component from a 
     *  <code>TableValues</code> object, which contains a value for 
     *  each row of each table in the <code>EditableValueHolder</code>'s
     *  ancestry.</p>
     */
    private void restoreTableValues(TableValues tv, EditableValueHolder evh) {
        //capture the old row of the tv and
        //iterate through the tv rows
        Object oldRow;  //an Integer or RowKey
        Iterator rowIterator;
        Object table = tv.getTable();
        Map values = tv.getValues();
        if (table instanceof UIData) {
            //capture the old row
            UIData uidata = (UIData)table;
            int iOldRow = uidata.getRowIndex();
            oldRow = new Integer(iOldRow);
            
            //get rowIterator
            List rowList = new ArrayList();
            rowList.addAll(values.keySet()); //add Set of Integers to List
            Collections.sort(rowList);
            rowIterator = rowList.iterator();
        }
        else {
            //capture the old row
            TableRowGroup rowGroup = (TableRowGroup)table;
            oldRow = rowGroup.getRowKey();
            
            //get rowIterator
            rowIterator = values.keySet().iterator();
        }
        
        while(rowIterator.hasNext()) {
            Object row = rowIterator.next();    //row is an Integer or RowKey
            
            //set the table to that row
            if (table instanceof UIData) {
                UIData uidata = (UIData)table;
                Integer rowInt = (Integer)row;
                int iRow = rowInt.intValue();
                uidata.setRowIndex(iRow);
            }
            else {
                TableRowGroup rowGroup = (TableRowGroup)table;
                RowKey rowKey = (RowKey)row;
                rowGroup.setRowKey(rowKey);
            }
            
            //get the rowValue for that row, which can be an Object or a TableValues
            //and restore that rowValue
            Object rowValue = values.get(row);
            if (rowValue instanceof TableValues) {
                TableValues rowValueTv = (TableValues)rowValue;
                restoreTableValues(rowValueTv, evh);
            }
            else {
                evh.setSubmittedValue(rowValue);
            }
        }
        
        //set table back to old row
        if (table instanceof UIData) {
            UIData uidata = (UIData)table;
            Integer oldRowInt = (Integer)oldRow;
            int iOldRow = oldRowInt.intValue();
            uidata.setRowIndex(iOldRow);
        }
        else {
            TableRowGroup rowGroup = (TableRowGroup)table;
            RowKey oldRowKey = (RowKey)oldRow;
            rowGroup.setRowKey(oldRowKey);
        }
    }
    
    /** <p>Structure that stores a component's submitted value
     * for each row of a table (<code>UIData</code> or 
     * <code>TableRowGroup</code>), either as an 
     * <code>Object</code> or as a 
     * nested <code>TableValues</code>. This permits storing of all the 
     * submitted values for a component in the case where that component has 
     * more than one table in its ancestry.</p>
     */
    private class TableValues {
        private Object table;   //store a UIData or TableRowGroup
        private Map values;     //has a RowKey or Integer as the key, and a submitted value Object or a TableValues as the value
        public TableValues(Object table) {
            this.table = table;
            values = new HashMap();
        }
        
        /** <p>Get the <code>UIData</code> or <code>TableRowGroup</code> 
         *  for this instance.</p>
         */
        public Object getTable() {return this.table;}
        
        /** <p>Get a <code>Map</code> of the values for each row 
         * of the table in question, where the key is an <code>Integer</code> or 
         * <code>RowKey</code>, and the value is a submitted value 
         * <code>Object</code> or a 
         * <code>TableValues</code> instance.</p>
         */
        public Map getValues() {return this.values;}
    }
    
    /**
     * <p>Ensure that the supplied <code>EditableValueHolder</code> component 
     * will discard (rather than retain) its submitted value when a virtual 
     * form is submitted in which the component does not participate.</p>
     * @param inputField An <code>EditableValueHolder</code> component that is 
     * <strong>not</strong> a participant in the virtual form that was submitted
     * on this request.
     * @throws IllegalArgumentException if inputField is null.
     * @throws IllegalArgumentException if a virtual form has been
     * submitted and the supplied inputField participates in it.
     */
    public void discardSubmittedValue(EditableValueHolder inputField) {
        if (inputField == null) {
                throw new IllegalArgumentException(
                        getMessage("nullInputField", null)  //NOI18N
                        );
        }
        if (inputField instanceof UIComponent) { //just defensive
            UIComponent uicInputField = (UIComponent)inputField;
            if (participatesInSubmittedVirtualForm(uicInputField)) {
                throw new IllegalArgumentException(
                        getMessage("supplyNonParticipatingInputField", new Object[]{uicInputField.getId(), "discardSubmittedValue"})  //NOI18N
                        );
                
            }
        }
        List evhCollection = new ArrayList();
        evhCollection.add(inputField);
        setRetainWhenNonParticipating(evhCollection, false);
    }
    
    /**
     * <p>Ensure that the participants in the supplied virtual form 
     * will discard (rather than retain) their submitted values when a 
     * different virtual form is submitted.</p>
     * @param virtualFormName The name of a virtual form on this page which 
     * has not been submitted.
     * @throws IllegalArgumentException if no virtual form exists with the 
     * supplied name.
     * @throws IllegalArgumentException if the supplied virtual form has been 
     * submitted on this request.
     */
    public void discardSubmittedValues(String virtualFormName) {
        VirtualFormDescriptor vfd = getVirtualFormByName(virtualFormName);
        if (vfd == null) {
                throw new IllegalArgumentException(
                        getMessage("unrecognizedVfName", new Object[]{virtualFormName})  //NOI18N
                        );
        }
        if (vfd == submittedVirtualForm) {
                throw new IllegalArgumentException(
                        getMessage("supplyUnsubmittedVirtualForm", new Object[]{virtualFormName, "discardSubmittedValues"})  //NOI18N
                        );
        }
        setRetainWhenNonParticipating(vfd, false);
    }
    
    private void setRetainWhenNonParticipating(VirtualFormDescriptor vfd, boolean retain) {
        String[] pids = vfd.getParticipatingIds();
        if (pids == null || pids.length < 1) {
            return;
        }
        List evhList = new ArrayList();
        for (int i = 0; i < pids.length; i++) {
            UIComponent uic = findComponentById(pids[i]);
            if (uic instanceof EditableValueHolder) {
                evhList.add(uic);
            }
        }
        setRetainWhenNonParticipating(evhList, retain);
    }
    
    private void setRetainWhenNonParticipating(Collection evhs, boolean retain) {
        if (evhs == null || evhs.size() < 1) {
            return;
        }
        synchronized(erasedMap) {     //prevent multiple threads from the same session simultaneously accessing erasedMap, nonDefaultRetainStatusEvhs
            //if we retain by default, then nonDefaultRetainStatusEvhs should contain evhs only if we want evhs to discard
            //if we discard by default, then nonDefaultRetainStatusEvhs should contain evhs only if we want evhs to retain
            boolean shouldContain = DEFAULT_RETAIN_STATUS ? !retain : retain;
            if (shouldContain) {
                nonDefaultRetainStatusEvhs.addAll(evhs);
            }
            else {
                nonDefaultRetainStatusEvhs.removeAll(evhs);
            }
        }
    }
    
    private VirtualFormDescriptor getVirtualFormByName(String virtualFormName) {
        if (virtualFormName == null) {
            return null;
        }
        VirtualFormDescriptor[] vfds = getVirtualForms();
        for (int i = 0; vfds != null && i < vfds.length; i++) {
            if (virtualFormName.equals(vfds[i].getName())) {
                return vfds[i];
            }
        }
        return null;
    }
    
    private static String getMessage(String key, Object[] args) {
        String baseName = Form.class.getPackage().getName() + ".Bundle";
        return MessageUtil.getMessage(FacesContext.getCurrentInstance(), baseName, key, args);
    }
    
    public String getEnctype() {
        String encType = super.getEnctype();
        if(encType == null || encType.length() == 0) {
            encType = "application/x-www-form-urlencoded";  //NOI18N
            super.setEnctype(encType);
        }
        return encType;
    }
}
