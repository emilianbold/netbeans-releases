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

package org.netbeans.lib.collab;


/**
 * an access control item is one element in a list of items
 * defining the access rule to a particular resource.
 * Each item contains a list of subjects, or users, and the
 * access level assigned to these subjects.
 *
 * @since version 0.1
 *
 */
public interface PrivacyItem
{

    /**
     * Action for the subjects
     */
    public final static int ALLOW = 0x1;
    public final static int DENY = 0x2;

    /*
     * Type of subjects
     */
    public static final String TYPE_SUBSCRIPTION = "subscription";
    public static final String TYPE_IDENTITIES = "jid";
    public static final String TYPE_GROUP = "group";

    /*
     * Resources describing the access for this PrivacyItem
     */
    public static final int PRESENCE_IN  = 0x1;
    public static final int PRESENCE_OUT = 0x2;
    public static final int MESSAGE      = 0x4;
    public static final int IQ           = 0x8;
    
    public static final String TO   = "to";
    public static final String FROM = "from";
    public static final String BOTH = "both";
    public static final String NONE = "none";
    
    /**
     * retrieve the subject represented by this privacy item
     */
    public String getSubject();
    
    /**
     * set the subject for this privacy item
     * @param subject The subject based on the type of the privacy item.
     */
    public void setSubject(String subject);
    
    /**
     * Set the access level for this PrivacyItem
     * @param access Access level as defined in PrivacyItem
     */    
    public void setAccess(int access);
        
    /**
     * Get the access level for this PrivacyItem
     * @return Access level as defined in PrivacyItem
     */   
    public int getAccess();
    
    /**
     * Set the type of this PrivacyItem
     * @param type as defined in PrivacyItem
     */    
    public void setType(String type);

    /**
     * Get the type of this PrivacyItem
     * @return type of this PrivacyItem
     */  
    public String getType();
    
    /**
     * Set the resource for this PrivacyItem.
     * @param resource The Resource as defined in PrivacyItem
     */
    public void setResource(int resource);
    
    /**
     * Get the resource of this PrivacyItem.
     */
    public int getResource();
    
}
