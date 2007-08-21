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
import java.util.Map;
import org.netbeans.api.gsf.annotations.NonNull;

/**
 * Given a parse tree, scan its structure and produce a flat list of
 * structure items suitable for display in a navigator / outline / structure
 * view
 *
 * @todo Make this a CancellableTask
 * 
 * @author Tor Norbye
 */
public interface StructureScanner {
    /**
     * Compute a list of structure items from the parse tree. The provided
     * {@link HtmlFormatter} can be used to format the HTML strings required for
     * StructureItems.
     */
    List<? extends StructureItem> scan(@NonNull CompilationInfo info, HtmlFormatter formatter);
    
    /**
     * @todo Do this in the same pass as the structure scan?
     * Compute a list of foldable regions, named "codeblocks", "comments", "imports", "initial-comment", ...
     */
    Map<String,List<OffsetRange>> folds(@NonNull CompilationInfo info);
}
