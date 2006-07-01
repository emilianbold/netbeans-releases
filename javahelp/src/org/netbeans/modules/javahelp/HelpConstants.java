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

package org.netbeans.modules.javahelp;




/** Useful constants pertaining to the help system.
* @author Jesse Glick
*/
public interface HelpConstants {

    /** public ID for standard helpset DTD
     */
    String PUBLIC_ID_HELPSET = "-//Sun Microsystems Inc.//DTD JavaHelp HelpSet Version 1.0//EN"; // NOI18N
    /** public ID for NetBeans reference to a helpset
     */
    String PUBLIC_ID_HELPSETREF = "-//NetBeans//DTD JavaHelp Help Set Reference 1.0//EN"; // NOI18N
    /** public URL for NetBeans reference to a helpset
     */
    String PUBLIC_URL_HELPSETREF = "http://www.netbeans.org/dtds/helpsetref-1_0.dtd"; // NOI18N
    /** public ID for a help context link
     */    
    String PUBLIC_ID_HELPCTX = "-//NetBeans//DTD Help Context 1.0//EN"; // NOI18N
    /** public URL for a help context link DTD
     */    
    String PUBLIC_URL_HELPCTX = "http://www.netbeans.org/dtds/helpcontext-1_0.dtd"; // NOI18N
    /** "context" for merge attribute on helpsets
     */    
    String HELPSET_MERGE_CONTEXT = "OpenIDE"; // NOI18N
    /** attribute (type Boolean) on helpsets indicating
     * whether they should be merged into the master or
     * not; by default, true
     */    
    String HELPSET_MERGE_ATTR = "mergeIntoMaster"; // NOI18N
    /** A helpID present only in the master help set;
     *however, when displayed by {@link #showHelp} as the helpID in a context,
     *the master help set (with merged-in children) will be shown instead,
     *with no change made to the content pane.
     *Also, this is the help ID mapped to the "default" page in the master help viewer.
     */
    String MASTER_ID = "org.netbeans.api.javahelp.MASTER_ID"; // NOI18N

}
