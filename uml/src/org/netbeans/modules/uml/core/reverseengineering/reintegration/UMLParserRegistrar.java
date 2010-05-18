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

    public void revokeOperationDetailsSink()
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
