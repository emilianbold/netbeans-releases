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
 * Intergace provided by JavaCC grammars bridges.
 *
 * @author  Petr Kuzel
 * @version 1.0
 */
public interface JJSyntaxInterface extends JJConstants {

   
    /** Initialize parser to initial state. */
    public void init(CharStream in);
    
    /** Initialize parser to particular state. */
    public void init(CharStream in, int state);
    
    /** Move parser to next state. */
    public void next();
    
    //query methods
    
    /** @return ID of the last recognized token. */
    public int getID();
    
    /** @return length of the last token. */
    public int getLength();
    
    /** @return token string representation. */
    public String getImage();
    
    //state persistence methods 
    
    /** @return last state */
    public int getState();
    
    /** Set last state. */
    public void setState(int state);
    
    /** @return last substates*/
    public int[] getStateInfo();
    
    /** Set last substates. */
    public void setStateInfo(int[] states);

}
