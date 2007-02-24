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
 * File       : IParseEventController.java
 * Created on : Oct 27, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework;

import antlr.collections.AST;

/**
 * @author Aztec
 */
public interface IParserEventController
{
    //  Methods used to set an retrieve the listeners that will recieve the events.
    public IStateListener getStateListener();
    public void setStateListener(IStateListener stateListener);
    public IStateFilter getStateFilter();
    public void setStateFilter(IStateFilter stateFilter);
    public ITokenProcessor getTokenProcessor();
    public void setTokenProcessor(ITokenProcessor tokenProcessor);
    public ITokenFilter getTokenFilter();
    public void setTokenFilter(ITokenFilter tokenFilter);
    public IErrorListener getErrorListener();
    public void setErrorListener(IErrorListener errorListener);

     // Public Interface
    public void stateBegin(String stateName);
    public void stateBegin(String stateName, AST tok, String type);
    public void stateEnd();
    public void tokenFound(AST tok, String type);
    public void errorFound(String msg, int line, int column, String filename);

    public void setFilename(String filename);
    public String getFilename();

    public void setLanguageName(String name);
    public String getLanguageName();
}
