package org.netbeans.test.xml.cpr;

import junit.framework.TestSuite;

public class TypeMigrationTest extends AcceptanceTestCaseXMLCPR
{
  static final String [] m_aTestMethods = {
      "Dummy"
    };

  public TypeMigrationTest( String arg0 )
  {
    super( arg0 );
  }

  public static TestSuite suite()
  {
    TestSuite testSuite = new TestSuite( TypeMigrationTest.class.getName( ) );
        
    for (String strMethodName : m_aTestMethods)
    {
      testSuite.addTest( new TypeMigrationTest( strMethodName ) );
    }
        
    return testSuite;
  }
}
