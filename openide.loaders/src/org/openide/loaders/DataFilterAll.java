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

package org.openide.loaders;

import java.io.Serializable;

/** Filter that accepts everything.
* @author Jaroslav Tulach
*/
class DataFilterAll extends Object implements DataFilter, Serializable {
    static final long serialVersionUID =-760448687111430451L;
    public boolean acceptDataObject (DataObject obj) {
        return true;
    }

    /** Gets a resolvable. */
    public Object writeReplace() {
        return new Replace();
    }

    static class Replace implements Serializable {
        static final long serialVersionUID =3204495526835476127L;
        public Object readResolve() {
            return DataFilter.ALL;
        }
    }
}
