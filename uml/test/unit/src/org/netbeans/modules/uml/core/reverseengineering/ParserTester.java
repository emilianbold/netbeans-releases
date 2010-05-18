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



/*
 * File       : ParserTester.java
 * Created on : Feb 3, 2004
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.reverseengineering;

import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.ILanguageFacilityFactory;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IREClassLoader;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParser;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserEventDispatcher;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserEventsSink;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserOperationEventSink;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.LanguageFacilityFactory;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.REClassLoader;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IREOperation;

/**
 * @author Aztec
 */
public class ParserTester
{
    public ParserTester()
    {
        new AbstractUMLTestCase() { };
        m_pParser = m_factory.getUMLParser();

        m_pDispatcher = m_pParser.getUMLParserDispatcher();
        
        m_pSink = m_pListener;        

        m_pOpSink = m_pOpListener;

        m_pDispatcher.registerForUMLParserEvents(m_pSink, " ");
    }

    public void xmlDump(String inFile, String outFile)
    {
        m_pClassLoader.loadFile(inFile);

        String xml = m_pListener.getXML();

        TestUtils.dumpToFile(xml,outFile);
    }
    
    public static void main(String args[])
    {
        ParserTester p = new ParserTester();
        p.xmlDump(args[0], args[1]);
    }
    
    public IREOperation xmlDump(String inFile, String outFile, String operationName)
    {
        return null;
    }
    
    public void xmlDump(String inFile, String outFile, String operationName, IREOperation pOperation)
    {
        return;
    }


    private void loadOperation(String filename, IREOperation pOper)
    {
        
    }

    private UMLTestListener m_pListener = new UMLTestListener();
    private UMLTestListener m_pOpListener = new UMLTestListener();
    
    private ILanguageFacilityFactory m_factory = new LanguageFacilityFactory();
    private IUMLParser m_pParser;
    private IUMLParserEventDispatcher  m_pDispatcher;
    private IUMLParserEventsSink m_pSink;
    private IUMLParserOperationEventSink m_pOpSink;

    private IREClassLoader m_pClassLoader = new REClassLoader();
}
