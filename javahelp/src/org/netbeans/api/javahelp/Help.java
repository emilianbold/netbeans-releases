/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.api.javahelp;

import javax.swing.event.ChangeListener;

import javax.help.HelpSet;

import org.openide.util.HelpCtx;

/** An implementation of the JavaHelp system.
* Get the concrete instance using lookup.
* @author Jaroslav Tulach, Jesse Glick
*/
public abstract class Help {
    
    /** public ID for standard helpset DTD
     */    
    public static final String PUBLIC_ID_HELPSET = "-//Sun Microsystems Inc.//DTD JavaHelp HelpSet Version 1.0//EN"; // NOI18N
    /** public ID for NetBeans reference to a helpset
     */    
    public static final String PUBLIC_ID_HELPSETREF = "-//NetBeans//DTD JavaHelp Help Set Reference 1.0//EN"; // NOI18N
    /** public URL for NetBeans reference to a helpset
     */    
    public static final String PUBLIC_URL_HELPSETREF = "http://www.netbeans.org/dtds/helpsetref-1_0.dtd"; // NOI18N
    /** public ID for a help context link
     */    
    public static final String PUBLIC_ID_HELPCTX = "-//NetBeans//DTD Help Context 1.0//EN"; // NOI18N
    /** public URL for a help context link DTD
     */    
    public static final String PUBLIC_URL_HELPCTX = "http://www.netbeans.org/dtds/helpcontext-1_0.dtd"; // NOI18N
    /** "context" for merge attribute on helpsets
     */    
    public static final String HELPSET_MERGE_CONTEXT = "OpenIDE"; // NOI18N
    /** attribute (type Boolean) on helpsets indicating
     * whether they should be merged into the master or
     * not; by default, true
     */    
    public static final String HELPSET_MERGE_ATTR = "mergeIntoMaster"; // NOI18N
    /** A helpID present only in the master help set;
     *however, when displayed by {@link #showHelp} as the helpID in a context,
     *the master help set (with merged-in children) will be shown instead,
     *with no change made to the content pane.
     *Also, this is the help ID mapped to the "default" page in the master help viewer.
     */
    public static final String MASTER_ID = "org.netbeans.api.javahelp.MASTER_ID"; // NOI18N
    
    /** constructor for subclasses
     */    
    protected Help() {}
    
    /** Test whether a given ID is valid in some known helpset.
     * Subclasses may override.
     * Should be a fast operation; if in doubt say you do not know.
     * @return whether it is valid, if this is known; else null
     */
    public abstract Boolean isValidID(String id);
    
    /** Shows help.
     * @param ctx help context
     */
    public void showHelp(HelpCtx ctx) {
        showHelp(ctx, /* #15711 */true);
    }

    /** Shows help.
     * @param ctx help context
     * @param showmaster whether to force the master helpset
     * to be shown (full navigators) even
     * though the supplied ID only applies
     * to one subhelpset
     */
    public abstract void showHelp(HelpCtx ctx, boolean showmaster);

    /** Add a change listener for when help sets change.
     * @param l the listener to add
     */
    public abstract void addChangeListener(ChangeListener l);
    
    /** Remove a change listener.
     * @param l the listener to remove
     */
    public abstract void removeChangeListener(ChangeListener l);

}
