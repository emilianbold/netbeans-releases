/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.xml.schema.model.impl;

import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.xml.schema.model.Derivation;

/**
 *
 * @author nn136682
 */
public class DerivationsImpl implements Derivation {

    public static class DerivationSet<E> extends HashSet<E> {
        public static final long serialVersionUID = 1L;
        public String toString() {
            StringBuffer sb = new StringBuffer();
            boolean first = true;
            for (E e : this) {
                if (! first) {
                    sb.append(Util.SEP);
                } else {
                    first = false;
                }
                sb.append(e.toString());
            }
            return sb.toString();
        }
    }
}
