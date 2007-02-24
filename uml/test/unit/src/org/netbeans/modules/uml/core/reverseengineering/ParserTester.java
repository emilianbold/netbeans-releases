
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
