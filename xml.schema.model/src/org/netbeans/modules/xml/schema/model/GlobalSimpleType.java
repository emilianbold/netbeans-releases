/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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
