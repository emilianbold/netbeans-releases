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

package org.netbeans.api.gsf;

import java.util.List;
import java.util.Set;

import org.netbeans.api.gsf.OffsetRange;
import org.netbeans.api.gsf.CompilationInfo;

import org.netbeans.api.gsf.annotations.CheckForNull;
import org.netbeans.api.gsf.annotations.NonNull;
import org.netbeans.api.gsf.annotations.Nullable;


/**
 * Provide an implementation that will help with instant renaming (in place refactoring)
 *
 * @author <a href="mailto:tor.norbye@sun.com">Tor Norbye</a>
 */
public interface InstantRenamer {
    /**
     * Check whether instant-renaming is allowed for the symbol under the given caret offset,
     * and return true iff it is.
     * @param info The compilation context to be used for parse info
     * @param caretOffset The specific caret location we want to check
     * @param explanationRetValue An array of length 1 whose first element can be set
     *   to a short description string (explaining why renaming is not allowed) which
     *   may be displayed to the user.
     */
    boolean isRenameAllowed(@NonNull CompilationInfo info, int caretOffset, String[] explanationRetValue);

    /**
     * Return a Set of regions that should be renamed if the element under the caret offset is
     * renamed.
     */
    @CheckForNull
    Set<OffsetRange> getRenameRegions(@NonNull CompilationInfo info, int caretOffset);
}
