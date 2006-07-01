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

package org.netbeans.api.javahelp;

import javax.swing.event.ChangeListener;


import org.openide.util.HelpCtx;

/** An implementation of the JavaHelp system.
* Get the concrete instance using lookup.
* @author Jaroslav Tulach, Jesse Glick
*/
public abstract class Help {

    /** constructor for subclasses
     */
    protected Help() {}

    /** Test whether a given ID is valid in some known helpset.
     * In lazy mode, should be a fast operation; if in doubt say you do not know.
     * @param id the ID to check for validity
     * @param force if false, do not do too much work (be lazy) and if necessary return null;
     *              if true, must return non-null (meaning the call may block loading helpsets)
     * @return whether it is valid, if this is known; else may be null (only permitted when force is false)
     */
    public abstract Boolean isValidID(String id, boolean force);
    
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
