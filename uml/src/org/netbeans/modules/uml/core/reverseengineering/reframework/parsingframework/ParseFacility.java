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
 * File       : ParseFacility.java
 * Created on : Oct 23, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework;

/**
 * @author Aztec
 */
public class ParseFacility extends Facility implements IParseFacility
{
    String m_LanguageName;
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IParseFacility#getLanguage()
     */
    public String getLanguage()
    {
        return m_LanguageName;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IParseFacility#processStreamByType(java.lang.String, java.lang.String, int)
     */
    public void processStreamByType(
        String stream,
        String langName,
        int processTypeKind)
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IParseFacility#processStreamFromFile(java.lang.String, org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguageParserSettings)
     */
    public void processStreamFromFile(
        String fileName,
        ILanguageParserSettings pSettings)
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IParseFacility#processStreamFromFile(java.lang.String)
     */
    public void processStreamFromFile(String fileName)
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IParseFacility#setLanguage(java.lang.String)
     */
    public void setLanguage(String language)
    {
        m_LanguageName = language;
    }

}
