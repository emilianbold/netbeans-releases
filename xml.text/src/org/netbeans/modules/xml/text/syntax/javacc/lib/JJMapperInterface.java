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

import org.netbeans.editor.*;

/**
 * Maps token ID returned by jjgrammer bridge to JJTokenID.
 *
 * @author  Petr Kuzel
 * @version 
 */
public interface JJMapperInterface extends JJConstants {

    
    /** @return token for particular ID. */
    public JJTokenID createToken(int id);
    
    /** @return token guessed for particular state. */
    public JJTokenID guessToken(String token, int state, boolean lastBuffer);
    
    /**
     * Called if  createToken(int id) return isError() token.
     * @return supposed token for particular id and state. 
     */
    public JJTokenID supposedToken(String token, int state, int id);
    
}
