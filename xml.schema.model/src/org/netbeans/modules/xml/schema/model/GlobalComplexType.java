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
