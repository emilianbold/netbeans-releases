/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.editor.ext.html.dtd;

import java.util.Set;
import java.util.Iterator;

/** The event fired to all registered interfaces when some DTD is invalidated.
 *
 * @author  Petr Nejedly
 * @version 1.0
 */
public class InvalidateEvent {

    private Set identifiers;

    /** Create new InvalidateEvent for given Set of instances of String
     * representing public identifiers of DTDs to invalidate */
    public InvalidateEvent( Set identifiers ) {
        this.identifiers = identifiers;
    }

    /** Get the iterator of instances of String representing
     * public identifiers of the invalidated DTDs.
     * Usable for classes holding more DTDs. */
    public Iterator getIdentifierIterator() {
        return identifiers.iterator();
    }

    /** Test if given public identifier is invalidated by this event.
     * Usable for classes holding only one DTD. */
    public boolean isInvalidatedIdentifier( String identifier ) {
        return identifiers.contains( identifier );
    }
}
