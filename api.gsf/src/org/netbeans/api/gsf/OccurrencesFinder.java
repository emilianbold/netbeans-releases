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

import java.util.Map;
import org.netbeans.api.gsf.OffsetRange;

/**
 * A CancellableTask responsible for computing the set of highlights matching
 * occurrences of the same program element as the one under the caret.
 * 
 * @author Tor Norbye
 */
public interface OccurrencesFinder extends CancellableTask<CompilationInfo> {
    /**
     * Set the caret position for which the current program element should
     * be computed. This method will be called before {@link #run} is called
     * to produce the set of highlights.
     */
    public abstract void setCaretPosition(int position);
    
    /**
     * Return a set of occurrence highlights computed by the last call to
     * {@link #run}.
     */
    public abstract Map<OffsetRange, ColoringAttributes> getOccurrences();
}
