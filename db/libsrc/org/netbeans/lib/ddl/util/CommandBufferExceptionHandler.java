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

package org.netbeans.lib.ddl.util;

import java.sql.*;
import org.netbeans.lib.ddl.*;

/**
* Interface of command buffer handler. When you use CommandBuffer to execute
* a bunch of command and any error occures during the execution, this handler
* catches it and lets user to decide if continue or not (when you're dropping
* nonexisting table, you probably would like to continue).
*
* @author Slavek Psenicka
*/
public interface CommandBufferExceptionHandler {

    /** Solves exception situation
    * Returns true if catched exception does not affect data consistency.
    * @param ex Thrown exception
    */
    public boolean shouldContinueAfterException(Exception ex);
}

/*
* <<Log>>
*  3    Gandalf   1.2         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  2    Gandalf   1.1         4/23/99  Slavek Psenicka new version
*  1    Gandalf   1.0         4/6/99   Slavek Psenicka 
* $
*/
