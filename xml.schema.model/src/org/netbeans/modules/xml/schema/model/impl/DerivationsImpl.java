/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
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
