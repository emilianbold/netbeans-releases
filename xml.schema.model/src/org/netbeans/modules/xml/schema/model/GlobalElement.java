/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */


package org.netbeans.modules.xml.schema.model;

import java.util.Set;
import org.netbeans.modules.xml.schema.model.Derivation.Type;
import org.netbeans.modules.xml.xam.GlobalReference;
import org.netbeans.modules.xml.xam.Named;

/**
 *
 * @author Chris Webster
 */
public interface GlobalElement extends CommonElement, ReferenceableSchemaComponent,
	Named<SchemaComponent>, TypeContainer  {
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
    
    GlobalReference<GlobalElement> getSubstitutionGroup();
    void setSubstitutionGroup(GlobalReference<GlobalElement> element);
    
}
