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

import java.util.Collection;
import org.netbeans.modules.xml.schema.model.Derivation.Type;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;

/**
 *
 * @author Chris Webster
 */
public interface Element extends SchemaComponent{

    public static final String BLOCK_PROPERTY = "block";
    public static final String DEFAULT_PROPERTY = "default";
    public static final String FIXED_PROPERTY = "fixed";
    public static final String NILLABLE_PROPERTY = "nillable";
    public static final String CONSTRAINT_PROPERTY = "constraint";
    public static final String REF_PROPERTY = "ref";
    
    public enum Block implements Derivation {
        ALL(Type.ALL), RESTRICTION(Type.RESTRICTION), EXTENSION(Type.EXTENSION), SUBSTITUTION(Type.SUBSTITUTION), EMPTY(Type.EMPTY);
        private final Type value;
        Block(Type value) { this.value = value; }
        public String toString() { return value.toString(); }
    }
    
    
    String getDefault();
    void setDefault(String defaultValue);
    
    String getFixed();
    void setFixed(String fixed);
    
    Boolean isNillable();
    void setNillable(Boolean nillable);
    boolean getNillableDefault();
    boolean getNillableEffective();
    
    Collection<Constraint> getConstraints();
    void addConstraint(Constraint c);
    void removeConstraint(Constraint c);
}
