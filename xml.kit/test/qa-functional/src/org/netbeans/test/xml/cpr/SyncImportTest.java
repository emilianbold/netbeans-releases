package org.netbeans.test.xml.cpr;

import junit.framework.TestSuite;

public class SyncImportTest extends AcceptanceTestCaseXMLCPR
{
  static final String [] m_aTestMethods = {
      "Dummy"
    };

  public SyncImportTest( String arg0 )
  {
    super( arg0 );
  }

  public static TestSuite suite()
  {
    TestSuite testSuite = new TestSuite( SyncImportTest.class.getName( ) );
        
    for (String strMethodName : m_aTestMethods)
    {
      testSuite.addTest(new SyncImportTest( strMethodName ) );
    }
        
    return testSuite;
  }
}
