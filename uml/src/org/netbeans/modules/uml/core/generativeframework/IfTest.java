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
 * File       : IfTest.java
 * Created on : Oct 30, 2003
 * Author     : aztec
 */
package org.netbeans.modules.uml.core.generativeframework;

/**
 * @author aztec
 */
public class IfTest implements IIfTest
{
    private String m_Test, m_Action;

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.generativeframework.IIfTest#getTest()
     */
    public String getTest()
    {
        return m_Test;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.generativeframework.IIfTest#setTest(java.lang.String)
     */
    public void setTest(String testName)
    {
        m_Test = testName;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.generativeframework.IIfTest#getResultAction()
     */
    public String getResultAction()
    {
        return m_Action;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.generativeframework.IIfTest#setResultAction(java.lang.String)
     */
    public void setResultAction(String varOrTemplateName)
    {
        m_Action = varOrTemplateName;
    }
}
