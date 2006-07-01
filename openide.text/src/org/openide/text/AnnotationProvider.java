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
package org.openide.text;

import org.openide.util.Lookup;


/**
 * A provider of annotations for given context.
 *
 * Implementations of this interface are looked up in the global lookup
 * and called to let them attach annotations to the lines in the set.
 * The call is performed during opening of given context.
 *
 * @author Petr Nejedly
 * @since 4.30
 */
public interface AnnotationProvider {
    /**
     * Attach annotations to the Line.Set for given context.
     *
     * @param set the Line.Set to attach annotations to.
     * @param context a Lookup describing the context for the Line.Set.
     *        If the Line.Set is associated with a document originating from
     *        a file, the context shall contain the respective FileObject.
     */
    public void annotate(Line.Set set, Lookup context);
}
