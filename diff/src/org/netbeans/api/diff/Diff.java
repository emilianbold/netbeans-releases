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

package org.netbeans.api.diff;

import java.awt.Component;
import java.io.IOException;
import java.io.Reader;
import java.util.Collection;

import org.openide.util.Lookup;

/**
 * This class represents a visual diff presenter, that knows how to compute the
 * differences between files and show them to the user.
 *
 * @author  Martin Entlicher
 */
public abstract class Diff extends Object {

    /**
     * Get the default visual diff presenter.
     */
    public static Diff getDefault() {
        return (Diff) Lookup.getDefault().lookup(Diff.class);
    }
    
    /**
     * Get all visual diff presenters registered in the system.
     */
    public static Collection getAll() {
        return Lookup.getDefault().lookup(new Lookup.Template(Diff.class)).allInstances();
    }
    
    /**
     * Show the visual representation of the diff between two sources.
     * @param name1 the name of the first source
     * @param title1 the title of the first source
     * @param r1 the first source
     * @param name2 the name of the second source
     * @param title2 the title of the second source
     * @param r2 the second resource compared with the first one.
     * @param MIMEType the mime type of these sources
     * @return The Component representing the diff visual representation
     *         or null, when the representation is outside the IDE.
     * @throws IOException when the reading from input streams fails.
     */
    public abstract Component createDiff(String name1, String title1,
                                         Reader r1, String name2, String title2,
                                         Reader r2, String MIMEType) throws IOException ;
}
