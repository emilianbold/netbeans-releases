/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xml.text.syntax.javacc.lib;

/**
 * A token Id that is used by JavaCC syntax colourings.
 * It holds additional information if the token is an error
 * one. In such case Syntax could set supposed token.
 * 
 * @author  Petr Kuzel
 * @version 1.0
 */
public class JJTokenID extends org.netbeans.editor.BaseTokenID {

    private boolean error = false;
    
    public JJTokenID(String name, int id) {
        super(name, id);
    }

    public JJTokenID(String name, int id, boolean error) {
        this(name, id);
        this.error = error;
    }
    
    public final int getID() {
        return getNumericID();
    }

    public final boolean isError() {
        return error;
    }
}
