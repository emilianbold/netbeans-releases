/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */


package org.netbeans.modules.xml.schema.model;

import java.util.Set;
import org.netbeans.modules.xml.schema.model.Derivation.Type;

/**
 * This interface represents a simple type.
 * @author Chris Webster
 */
public interface GlobalSimpleType extends SimpleType, GlobalType {
    public static final String FINAL_PROPERTY = "final";
    public enum Final implements Derivation {
        ALL(Type.ALL), LIST(Type.LIST), UNION(Type.UNION), RESTRICTION(Type.RESTRICTION), EMPTY(Type.EMPTY);
        private Derivation.Type value;
        Final(Derivation.Type v) { value = v; }
        public String toString() { return value.toString(); }
    }
    
    Set<Final> getFinal();
    void setFinal(Set<Final> fin);
    /**
     * @return either the value of #Schema.getFinalDefaultEffective or empty set.
     */
    Set<Final> getFinalDefault();
    Set<Final> getFinalEffective();
    
}
