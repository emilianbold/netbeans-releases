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
     *  it shall contain the FileObject the LineSet is associated with.
     */
    public void annotate(Line.Set set, Lookup context);
}
