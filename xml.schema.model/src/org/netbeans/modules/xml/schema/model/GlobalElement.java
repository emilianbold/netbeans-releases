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
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.netbeans.modules.xml.xam.Nameable;

/**
 *
 * @author Chris Webster
 */
public interface GlobalElement extends Element, ReferenceableSchemaComponent,
	NameableSchemaComponent, TypeContainer  {
    public static final String FINAL_PROPERTY = "final";
    public static final String ABSTRACT_PROPERTY = "abstract";
    public static final String SUBSTITUTION_GROUP_PROPERTY = "substitutionGroup";
    
    Set<Block> getBlock();
    void setBlock(Set<Block> block);
    Set<Block> getBlockDefault();
    Set<Block> getBlockEffective();
    
   
    
    Boolean isAbstract();
    void setAbstract(Boolean abstr);
    boolean getAbstractDefault();
    boolean getAbstractEffective();
    
    public enum Final implements Derivation {
        ALL(Type.ALL), RESTRICTION(Type.RESTRICTION), EXTENSION(Type.EXTENSION),
        EMPTY(Type.EMPTY);
        private Derivation.Type value;
        Final(Derivation.Type v) { value = v; }
        public String toString() { return value.toString(); }
    }
    
    Set<Final> getFinal();
    void setFinal(Set<Final> finalValue);
    /**
     * @return either the value of #Schema.getFinalDefaultEffective or empty set.
     */
    Set<Final> getFinalDefault();
    Set<Final> getFinalEffective();
    
    NamedComponentReference<GlobalElement> getSubstitutionGroup();
    void setSubstitutionGroup(NamedComponentReference<GlobalElement> element);
    
}
