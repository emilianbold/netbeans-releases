/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.api.webmodule;

/** Specific source group types for web project;
 *
 * @author  mk115033
 */
public interface WebProjectConstants {

    /** source folders for JSPs, HTML ...
    */    
    public static final String TYPE_DOC_ROOT="doc_root"; //NOI18N
    
    /** source folders type for TLD files ...
    */
    public static final String TYPE_WEB_INF="web_inf"; //NOI18N

    /** source folders for Tag Files
    */
    public static final String TYPE_TAGS="tags"; //NOI18N
    
    /**
     * Standard command for redeploying a web project.
     * @see ActionProvider
     */
    public static final String COMMAND_REDEPLOY = "redeploy" ; //NOI18N
    
}
