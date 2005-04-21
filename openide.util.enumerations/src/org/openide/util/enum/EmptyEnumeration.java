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
package org.openide.util.enum;

import java.util.Enumeration;
import java.util.NoSuchElementException;


/**
 * The class that represents empty enumeration.
 * @deprecated JDK 1.5 treats enum as a keyword so this class was
 *             replaced by {@link org.openide.util.Enumerations#empty()}.
 * @author Petr Hamernik
 */
public final class EmptyEnumeration implements Enumeration {
    /** instance of empty enumeration */
    public static final EmptyEnumeration EMPTY = new EmptyEnumeration();

    public boolean hasMoreElements() {
        return false;
    }

    public Object nextElement() throws NoSuchElementException {
        throw new NoSuchElementException();
    }
}
