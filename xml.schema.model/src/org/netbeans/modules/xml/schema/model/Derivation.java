/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */


package org.netbeans.modules.xml.schema.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.xml.schema.model.impl.DerivationsImpl;

/**
 *
 * @author nn136682
 */
public interface Derivation {
    public enum Type {
        EMPTY(""), 
        ALL("#all"), 
        EXTENSION("extension"), 
        RESTRICTION("restriction"), 
        SUBSTITUTION("substitution"), 
        LIST("list"), 
        UNION("union");
        Type(String s) {
            value = s;
        }
        public String toString() {
            return value;
        }
        private String value;
    }
}

