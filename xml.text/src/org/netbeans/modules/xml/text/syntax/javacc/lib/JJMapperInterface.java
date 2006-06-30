/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
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
