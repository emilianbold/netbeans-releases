/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.tax.grammar;

import org.netbeans.tax.TreeNode;
import org.netbeans.tax.TreeElement;
import org.netbeans.tax.TreeAttribute;

/**
 *
 * @author Libor Kramolis
 * @version 0.1
 */
public interface Grammar {
    
    /** */
    //      public AbstractElemnentDecl[] definedElements ();
    
    /** */
    //      public AbstractAttributeDecl[] definedAttributes ();
    
    /** */
    //      public AbstractEntityDecl[] definedEntities ();
    
    /** */
    //      public AbstractNotationDecl[] definedNotations ();
    
    
    /** Create validator for grammar. */
    public Validator getValidator ();
    
    
    /** */
    public boolean isNamespaceAware ();
    
    
    /** Listen to grammar changes. */
    //    public void addPropertyChangeListener (PropertyChangeListener listener);
    
    /** */
    //    public void removePropertyChangeListener (PropertyChangeListener listener);
    
}
