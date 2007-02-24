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

package org.netbeans.modules.uml.core.reverseengineering.reintegration;

import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.ILanguageFacilityFactory;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParser;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserEventDispatcher;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserOperationEventSink;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.LanguageFacilityFactory;

public class UMLParserRegistrar
{
    private IUMLParserEventDispatcher m_cpUMLParserEventDispatcher;
    private IOperationRE m_cpOperationRE;
    private IUMLParserOperationEventSink m_lRegistrationCookie;

    /// Member variable used for parsing the operations.
    /// Only access via GetUMLParser()
    private static IUMLParser m_pUMLParser;

    public UMLParserRegistrar()
    {
        IUMLParser cpUMLParser = getUMLParser();
        if(cpUMLParser != null)
        {
            m_cpUMLParserEventDispatcher 
                = cpUMLParser.getUMLParserDispatcher();
            if(m_cpUMLParserEventDispatcher != null)
            {
                m_cpOperationRE = new OperationRE();
                IUMLParserOperationEventSink cpSink 
                    = (m_cpOperationRE instanceof IUMLParserOperationEventSink)
                    ? (IUMLParserOperationEventSink)m_cpOperationRE : null;
                if(cpSink != null)
                {
                    m_cpUMLParserEventDispatcher
                        .registerForOperationDetailsEvent(cpSink);
                    m_lRegistrationCookie = cpSink;
                }
            }
        }
    }

    public void finalize()
    {
        m_cpUMLParserEventDispatcher
            .revokeOperationDetailsSink(m_lRegistrationCookie);
    }

    /**
    * Retrieve the UML parser member variable
    */
    public static IUMLParser getUMLParser()
    {
        IUMLParser cpUMLParser = null;
        if( m_pUMLParser == null )
        {
            ILanguageFacilityFactory cpLangFacFactory
                = new LanguageFacilityFactory();
            if(cpLangFacFactory != null)
            {
                cpUMLParser = cpLangFacFactory.getUMLParser();
            }
        }
        return cpUMLParser;
    }

    /**
    * Must be called to clear the UML Parser COM pointer before the DLL 
    * is unloaded
    */
    public void cleanUp()
    {
        // CLEAN, causes a crash?   m_pUMLParser = NULL;
    }

    /**
    * Gets the operation event sink
    */
    public IOperationRE getOperationRE()
    {
        return m_cpOperationRE;
    }
}
