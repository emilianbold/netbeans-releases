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
 * This interface represents a global complex type.
 * @author Chris Webster
 */
public interface GlobalComplexType extends ComplexType, GlobalType,
        SchemaComponent  {
    
    public static final String ABSTRACT_PROPERTY = "abstract";
    public static final String FINAL_PROPERTY = "final";
    public static final String BLOCK_PROPERTY = "block";
    
    Boolean isAbstract();
    void setAbstract(Boolean isAbstract);
    boolean getAbstractDefault();
    boolean getAbstractEffective();
    
    public enum Block implements Derivation {
        ALL(Type.ALL), EXTENSION(Type.EXTENSION), RESTRICTION(Type.RESTRICTION), EMPTY(Type.EMPTY);
        private Type value;
        Block(Type v) { value = v; }
        public String toString() { return value.toString(); }
    }
    Set<Block> getBlock();
    void setBlock(Set<Block> block);
    Set<Block> getBlockDefault();
    Set<Block> getBlockEffective();
    
    public enum Final implements Derivation {
        ALL(Type.ALL), EXTENSION(Type.EXTENSION), RESTRICTION(Type.RESTRICTION), EMPTY(Type.EMPTY);
        private Type value;
        Final(Type v) { value = v; }
        public String toString() { return value.toString(); }
    }
    
    Set<Final> getFinal();
    void setFinal(Set<Final> finalValue);
    Set<Final> getFinalDefault();
    Set<Final> getFinalEffective();
}
