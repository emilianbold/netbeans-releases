/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */


package org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework;

import org.netbeans.modules.uml.core.reverseengineering.reframework.IREOperation;

/**
 * @author sumitabhk
 *
 */
public class LanguageParser implements ILanguageParser
{

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguageParser#getErrorListener()
     */
    public IErrorListener getErrorListener()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguageParser#getStateFilter()
     */
    public IStateFilter getStateFilter()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguageParser#getStateListener()
     */
    public IStateListener getStateListener()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguageParser#getTokenFilter()
     */
    public ITokenFilter getTokenFilter()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguageParser#getTokenProcessor()
     */
    public ITokenProcessor getTokenProcessor()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguageParser#parseFile(java.lang.String)
     */
    public void parseFile(String filename)
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguageParser#parseFile(java.lang.String, java.lang.String)
     */
    public void parseFile(String filename, String charset)
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguageParser#parseOperation(java.lang.String, org.netbeans.modules.uml.core.reverseengineering.reframework.IREOperation)
     */
    public void parseOperation(String filename, IREOperation operation)
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguageParser#parseOperation(java.lang.String, java.lang.String, org.netbeans.modules.uml.core.reverseengineering.reframework.IREOperation)
     */
    public void parseOperation(String filename, String charset, IREOperation operation)
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguageParser#processStreamByType(java.lang.String, int)
     */
    public void processStreamByType(String stream, int kind)
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguageParser#setErrorListener(org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IErrorListener)
     */
    public void setErrorListener(IErrorListener errorListener)
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguageParser#setStateFilter(org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IStateFilter)
     */
    public void setStateFilter(IStateFilter filter)
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguageParser#setStateListener(org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IStateListener)
     */
    public void setStateListener(IStateListener stateListener)
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguageParser#setTokenFilter(org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenFilter)
     */
    public void setTokenFilter(ITokenFilter filter)
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguageParser#setTokenProcessor(org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenProcessor)
     */
    public void setTokenProcessor(ITokenProcessor tokenProcessor)
    {
        // TODO Auto-generated method stub

    }

}


