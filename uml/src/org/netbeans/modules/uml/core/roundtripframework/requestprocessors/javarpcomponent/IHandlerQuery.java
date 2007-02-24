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

/*
 * File       : IHandlerQuery.java
 * Created on : Oct 29, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpcomponent;

/**
 * @author Aztec
 */
public interface IHandlerQuery
{
    public String getKey();

    public void setSilent(boolean silent);
    public boolean getSilent();

    // Reset allows us to reset an existing query and "reuse" it
    // without having to reconstruct it.

    public void reset();
    public boolean getPersist();

    // A query can be displayed with up to 4 arguments that are substituted for
    // the substrings "%1", "%2", "%3", "%4" in the text

    public boolean doQuery();
    public boolean doQuery( String arg1);
    public boolean doQuery( String arg1, String arg2);
    public boolean doQuery( String arg1, String arg2, String arg3);
    public boolean doQuery( String arg1, String arg2, String arg3, String arg4);    
}
