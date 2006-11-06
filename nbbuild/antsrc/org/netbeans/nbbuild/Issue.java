/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.nbbuild;

import java.io.*;
import java.util.*;
import java.util.Date;

/** Represents on issue in issuezilla.
 * Created by {@link Issuezilla#getBug}
 *
 * @author Ivan Bradac, refactored by Jaroslav Tulach
 */
public final class Issue extends Object implements Comparable {
    //static final String ENHANCEMENT = "ENHANCEMENT";
    static final String ISSUE_TYPE = "issue_type"; 
    static final String SHORT_DESC = "short_desc";
    static final String LONG_DESC = "long_desc";
    static final String COMMENT = "comment";
    static final String ISSUE_ID = "issue_id";
    static final String ISSUE_STATUS = "issue_status";
    static final String RESOLUTION = "resolution";
    static final String COMPONENT = "component";
    static final String REPORTER = "reporter";
    static final String VERSION = "version";
    static final String SUBCOMPONENT = "subcomponent";
    static final String REP_PLATFORM = "rep_platform";
    static final String OP_SYS = "op_sys"; 
    static final String PRIORITY = "priority";
    static final String ASSIGNED_TO = "assigned_to";
    static final String CC = "cc";
    static final String DEPENDS_ON = "dependson";
    static final String BLOCKS = "blocks";
    static final String CREATED = "creation_ts";
    static final String VOTES = "votes";
    static final String KEYWORDS = "keywords";
    static final String STATUS_WHITEBOARD = "status_whiteboard";
    
    /** The target milestone attribute name. */
    static final String TARGET_MILESTONE = "target_milestone";

    /** Name of the attribute containing the long_desc as a list */
    static final String LONG_DESC_LIST = "long_desc_list";

    private Map<String,Object> attributes = new HashMap<String,Object>(49);


    /**
     * Gets the id as an Integer.
     *
     * @return the issue_id as 
     */
    public int getId() {
        Object id = getAttribute(ISSUE_ID);
        try {
            return Integer.parseInt ((String) id);
        } catch (Exception ex) {
            return -1;
        }
    }

    /** Who is assigned to this bug.
     * @return name of person assigned to this bug
     */
    public String getAssignedTo () {
        return string (ASSIGNED_TO);
    }

    /** Who reported the bug.
     * @return name of the reporter
     */
    public String getReportedBy () {
        return string (REPORTER);
    }
    
    /** Everyone who is interested in the issue.
     * @return array of names or empty array if nobody is
     */
    public String[] getObservedBy () {
        List<?> l = (List<?>) getAttribute (CC);
        if (l != null) {
            return l.toArray(new String[0]);
        } else {
            return new String[0];
        }
    }

    /** Status of the bug, verified, etc.
     * @return textual name of the status.
     */
    public String getStatus () {
        return string (ISSUE_STATUS);
    }

    /** Resolution: Fixed, etc...
     * @return textual name of resolution.
     */
    public String getResolution () {
        return string (RESOLUTION);
    }

    /** Type of the issue: Bug, Enhancement, Task, etc...
     * @return textual name of issue type
     */
    public String getType () {
        return string (ISSUE_TYPE);
    }

    /** Priority of the issue.
     * @return integer describing priority, -1 if unknown
     */
    public int getPriority () {
        String s = string (PRIORITY);
        if (s.length () == 2 && s.charAt (0) == 'P') {
            return s.charAt (1) - '0';
        } else {
            return -1;
        }
    }
    
    /** A time when this issue has been created.
     * @return the date or begining of epoch if wrongly defined
     */
    public Date getCreated () {
        Date d = (Date)getAttribute (CREATED);
        return d == null ? new Date (0) : d;
    }
    
    /** The summary or short description of the bug.
     * @return string
     */
    public String getSummary () {
        return string (SHORT_DESC);
    }

    /** Getter of descriptions.
     * @return array of descriptions
     */
    public Description[] getDescriptions () {
        Object obj = getAttribute(LONG_DESC_LIST);
        if (obj == null) {
            return new Description[0];
        }

        return ((List<?>) obj).toArray(new Description[0]);
    }
    
    /** A list of bugs that depends on this one.
     * @return array of integer numbers of those bugs or empty array
     */
    public int[] getDependsOn () {
        return ints (DEPENDS_ON);
    }
    
    /** A list of bugs that this issue blocks.
     * @return array of integer numbers of those bugs or empty array
     */
    public int[] getBlocks () {
        return ints (BLOCKS);
    }
    
    /** Name of the milestone this issue should be resolved in.
     * @return string name
     */
    public String getTargetMilestone () {
        return string (TARGET_MILESTONE);
    }
    
    /** Name of the component this issue belongs to.
     * @return string name
     */
    public String getComponent () {
        return string (COMPONENT);
    }
    
    /** Name of subcomponent this issue belongs to.
     * @return string name
     */
    public String getSubcomponent () {
        return string (SUBCOMPONENT);
    }

    /**
     * @deprecated Use <code>getWhiteboardAttribute("duration")</code> instead.
     */
    @Deprecated
    public String getDuration () {
        String val = getWhiteboardAttribute("duration");
        return val == null ? "" :  val;
    }

    /**
     * Get status white board attribute. Status whiteboard attributes
     * are convetionally defined in form <tt>name:WORD=value:WORD</tt> e.g.
     * <tt>duration=50</tt> or <tt>lines=1000</tt>.
     * @param attribute name
     * @return token representing attribute value or <code>null</code>
     */
    public final String getWhiteboardAttribute(String attribute) {
        String ret = null;
        String swb = string (STATUS_WHITEBOARD);
        StringTokenizer st = new StringTokenizer (swb);
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if ( token.startsWith (attribute + '=') ) {
                ret = token.substring (token.indexOf ('=') + 1);
                break;
            }
        }
        return ret;
    }

    /** Number of votes for given component.
     * @return integer representing number of votes or 0 is no votes present
     */
    public int getVotes () {
        try {
            String s = string (VOTES);
            return Integer.parseInt (s);
        } catch (Exception ex) {
            return 0;
        }
    }

    /** All keywords of the issue.
     * @return Keywords deliminated by comma or empty string
     */
    public String getKeywords () {
        try {
            return string (KEYWORDS);
        } catch (Exception ex) {
            return "";
        }
    }
    
    /** Check if the this issue has the specified keyword
     * @return <code>true</code> if specified keyword is set in this issue,
     *  otherwise <code>false</code>.
     */
    public boolean containsKeyword (String keyword) {
        StringTokenizer tokenizer = new StringTokenizer(getKeywords());
        while (tokenizer.hasMoreTokens()) {
            String current = tokenizer.nextToken();
            if (current.equals(keyword))
                return true;
        }
        return false;
    }
    
    /** Is this bug actually an enhancement?
     * @return <CODE>true</CODE> if this is enhancement, <CODE>false</CODE> otherwise
     *
    public boolean isEnhancement() {
        if (attributes == null) {
            return false;
        }
        String s = (String) getAttribute(ISSUE_TYPE); 
        return (s == null) ? false : s.equals(ENHANCEMENT);
    }
     */

    /** Getter to return string for given attribute.
     */
    private String string (String name) {
        Object o = getAttribute (name);
        return o instanceof String ? (String)o : "";
    }
    
    /** Getter for array of integers.
     */
    private int[] ints (String name) {
        List l = (List)getAttribute (name);
        if (l == null) {
            return new int[0];
        }
        
        int[] arr = new int[l.size ()];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = Integer.parseInt ((String)l.get (i));
        }
        return arr;
    }

    /** Package private getter, it is expected to add getter for useful
     * issues.
     */
    Object getAttribute(String name) {
        if (name.equals(LONG_DESC)) {
            return formatLongDescriptions();
        } else {
            return attributes.get(name);
        }
    }


    /** Setter of values, package private. */
    void setAttribute(String name, Object value) {
        attributes.put(name, value);
    }

    /**
     * Gets the name/value pairs of the bug attributes as a Map.
     *
     * @return the name/value pairs of the attributes
     */
    private Map attributes() {
        return attributes;
    }

    /** Converts the object to textual representation.
     * @return a text description of the issue
     */
    public String toString() {   
        StringBuffer buffer;
        if (attributes == null) {
            return "Empty BugBase";
        }
        Iterator it = attributes.entrySet().iterator();
        buffer = new StringBuffer();
        buffer.append(this.getClass().getName() 
                      + " containing these name/value attribute pairs:\n");
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            buffer.append("NAME  : " + entry.getKey() + "\n");
            buffer.append("VALUE : " + entry.getValue() + "\n");      
        }
        return buffer.toString();
    }

    /** Compares issues by their ID
     */
    public int compareTo (Object o) {
        Issue i = (Issue)o;
        return getId () - i.getId ();
    }

    /** 
     * Formats the list of long_desc's into one String 
     *
     * @return the long descriptions in one String
     */
    private String formatLongDescriptions() {
        if (attributes.get (Issue.LONG_DESC) == null) {
            StringBuffer buffer = new StringBuffer("");
            Object obj = getAttribute(LONG_DESC_LIST);
            List descriptions;
            if (obj == null) {
                return null;
            }
            descriptions = (List) obj;
            Iterator it = descriptions.iterator();
            while (it.hasNext()) {
                Description ld = (Description) it.next();
                buffer.append(ld.toString());
            }
            attributes.put (LONG_DESC, buffer.toString());
        }
        return attributes.get (LONG_DESC).toString();
    }
    


    /** 
     * Long description of Issues.
     */
    public final static class Description {
        static final String WHO = "who";
        static final String ISSUE_WHEN = "issue_when";
        static final String BODY = "body";
        static final String THETEXT = "thetext";

        /** Holds value of property who. */
        private String who;

        /** Holds value of property issue_when. */
        private Date when;

        /** Holds value of property thetext. */
        private String body;

        /** Name of the author of the issue.
         * @return Value of property who.
         */
        public String getWho() {
            return who;
        }

        /** Setter for property who.
         * @param who New value of property who.
         */
        void setWho(String who) {
            this.who = who;
        }

        /** When this comment has been added.
         * @return Value of property issue_when.
         */
        public java.util.Date getWhen() {
            return when;
        }

        /** Setter for property issue_when.
         * @param issue_when New value of property issue_when.
         */
        void setIssueWhen(Date when) {
            this.when = when;
        }

        /** The actual text of the issue.
         * @return Value of property thetext.
         */
        public String getBody() {
            return body;
        }

        /** Textual description.
         * @return string representation of the description.
         */
        public String toString() {
            StringBuffer buffer = new StringBuffer();
            buffer.append(getWho());
            buffer.append(", ");
            buffer.append(getWhen());
            buffer.append(" : \n");
            buffer.append(getBody());
            buffer.append("\n\n");
            return buffer.toString();
        }











        /** Setter for property thetext.
         * @param thetext New value of property thetext.
         */
        void setBody(String body) {
            this.body = body;
        }

        void setAtribute(String name, String value) {
            if (name.equalsIgnoreCase(WHO)) {
                setWho(value);
            } else if (name.equalsIgnoreCase(BODY) 
                    || name.equalsIgnoreCase(THETEXT)) {
                setBody(value);
            }
        }

        private String getAttribute(String name) {
            if (name.equalsIgnoreCase(WHO)) {
                return who;
            } else if (name.equalsIgnoreCase(BODY) 
                    || name.equalsIgnoreCase(THETEXT)) {
                return body;
            } else {
                return null;
            }
        }

    }
    
}
