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
package org.openide.util.enum;

import java.util.Enumeration;
import java.util.HashSet;


/**
 * Enumeration that scans through another one and removes duplicates.
 * Two objects are duplicate if <CODE>one.equals (another)</CODE>.
 * @deprecated JDK 1.5 treats enum as a keyword so this class was
 *             replaced by {@link org.openide.util.Enumerations#removeDuplicates}.
 * @author Jaroslav Tulach
 */
public class RemoveDuplicatesEnumeration extends FilterEnumeration {
    /** hashtable with all returned objects */
    private HashSet all = new HashSet(37);

    /**
    * @param en enumeration to filter
    */
    public RemoveDuplicatesEnumeration(Enumeration en) {
        super(en);
    }

    /** Filters objects. Overwrite this to decide which objects should be
    * included in enumeration and which not.
    * @param o the object to decide on
    * @return true if it should be in enumeration and false if it should not
    */
    protected boolean accept(Object o) {
        return all.add(o);
    }
}
