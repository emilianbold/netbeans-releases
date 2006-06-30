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
