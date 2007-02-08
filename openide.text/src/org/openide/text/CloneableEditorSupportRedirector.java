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
 * The Original Software is NetBeans. 
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.openide.text;

import org.openide.util.Lookup;

/**
 * Allows to find another {@link CloneableEditorSupport} that all the 
 * requests passed to given one should be redirected to. This is useful
 * for redirecting operation on <a href="@org-openide-filesystems@/org/openide/filesystems/FileObject.html">
 * FileObject</a> to another one in cases when two <code>FileObject</code>s
 * represent the same physical file.
 * 
 * @author Jaroslav Tulach
 * @since 6.13
 */
public abstract class CloneableEditorSupportRedirector {
    /** Find a delegate for given {@link CloneableEditorSupport}'s {@link Lookup}.
     * The common code can be to extract for example a 
     * <a href="@org-openide-filesystems@/org/openide/filesystems/FileObject.html">
     * FileObject</a> from the lookup and use its location to find another
     * <code>CloneableEditorSupport</code> to delegate to.
     * 
     * @param env the environment associated with current CloneableEditorSupport
     * @return null or another CloneableEditorSupport to use as a replacement
     */
    protected abstract CloneableEditorSupport redirect(Lookup env);
    
    static CloneableEditorSupport findRedirect(CloneableEditorSupport one) {
        for (CloneableEditorSupportRedirector r : Lookup.getDefault().lookupAll(CloneableEditorSupportRedirector.class)) {
            CloneableEditorSupport ces = r.redirect(one.getLookup());
            if (ces != null && ces != one) {
                return ces;
            }
        }
        return null;
    }
}


