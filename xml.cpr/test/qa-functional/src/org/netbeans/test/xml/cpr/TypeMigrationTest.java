package org.netbeans.test.xml.cpr;

import org.netbeans.jemmy.JemmyException;
import org.netbeans.jellytools.TopComponentOperator;
import java.awt.Window;
import java.awt.Point;
import javax.swing.tree.TreePath;
import junit.framework.TestSuite;
import org.netbeans.jellytools.NewProjectNameLocationStepOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.test.xml.schema.lib.SchemaMultiView;

public class TypeMigrationTest extends AcceptanceTestCaseXMLCPR
{
  static final String SAMPLE_CATEGORY_NAME = "Samples|SOA";
  static final String SAMPLE_PROJECT_NAME = "Synchronous BPEL Process";
  static final String SAMPLE_NAME = "SynchronousSample2Bpel";
  static final String COMPOSITE_APPLICATION_NAME = SAMPLE_NAME + "Application";

  static final String MODULE_CATEGORY_NAME = "SOA";
  static final String MODULE_PROJECT_NAME = "BPEL Module";
  static final String MODULE_NAME = "BpelModule";

  static final String SAMPLE_SCHEMA_PATH = "Process Files";

  static final String SAMPLE_SCHEMA_NAME = "SynchronousSample";
  static final String SAMPLE_SCHEMA_EXT = ".xsd";

  static final String SAMPLE_BPEL_NAME = "SynchronousSample";
  static final String SAMPLE_BPEL_EXT = ".bpel";

  static final String [] m_aTestMethods = {
      "CreateSynchronousSample",
      "CreateBPELModule",
      "CreateNewXmlSchema",
      "AddNewComplexType",
      "AddProjectReference",
      "OpenSchema",
      "ImportFererencedSchema",
      "CustomizeType",
      "OpenBPELMapper",
      "CorrectAssignment",
      "Deploy",
      "RunExistingTest",
      "CreateNewTest",
      "RunNewTest"
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

  class CStarttextStringComparator implements Operator.StringComparator
  {
    public boolean equals( java.lang.String caption, java.lang.String match )
    {
      return caption.startsWith( match );
    }
  }
    
  public void CreateSynchronousSample( )
  {
    startTest( );

    // Create BluePrint1 Sample
    NewProjectWizardOperator opNewProjectWizard = NewProjectWizardOperator.invoke( );
    opNewProjectWizard.selectCategory( SAMPLE_CATEGORY_NAME );

    //Operator.StringComparator op = opNewProjectWizard.getDefaultStringComparator( );
    //opNewProjectWizard.setDefaultStringComparator( new CFulltextStringComparator( ) );
    //opNewProjectWizard.selectProject( SAMPLE_PROJECT_NAME );
    //opNewProjectWizard.setDefaultStringComparator( op );

    JListOperator lst = opNewProjectWizard.lstProjects( );
    lst.clickOnItem( SAMPLE_PROJECT_NAME, new CFulltextStringComparator( ) );

    opNewProjectWizard.next( );

    NewProjectNameLocationStepOperator opNewProjectNameLocationStep = new NewProjectNameLocationStepOperator( );
    opNewProjectNameLocationStep.txtProjectLocation( ).setText( System.getProperty( "xtest.ide.open.projects" ) );
    opNewProjectNameLocationStep.txtProjectName( ).setText( SAMPLE_NAME );
    opNewProjectWizard.finish( );

    endTest( );
  }
    
  public void CreateBPELModule( )
  {
    startTest( );

    // Create BluePrint1 Sample
    NewProjectWizardOperator opNewProjectWizard = NewProjectWizardOperator.invoke( );
    opNewProjectWizard.selectCategory( MODULE_CATEGORY_NAME );
    opNewProjectWizard.selectProject( MODULE_PROJECT_NAME );
    opNewProjectWizard.next( );

    NewProjectNameLocationStepOperator opNewProjectNameLocationStep = new NewProjectNameLocationStepOperator( );
    opNewProjectNameLocationStep.txtProjectLocation( ).setText( System.getProperty( "xtest.ide.open.projects" ) );
    opNewProjectNameLocationStep.txtProjectName( ).setText( MODULE_NAME );
    opNewProjectWizard.finish( );

    endTest( );
  }

  public void CreateNewXmlSchema( )
  {
    startTest( );

    ProjectsTabOperator pto = new ProjectsTabOperator( );
    ProjectRootNode prn = pto.getProjectRootNode( MODULE_NAME );
    prn.select( );

    NewFileWizardOperator opNewFileWizard = NewFileWizardOperator.invoke( );
    opNewFileWizard.selectCategory( "XML" );
    opNewFileWizard.selectFileType( "XML Schema" );
    opNewFileWizard.next( );
    opNewFileWizard.finish( );

    // Check created schema in project tree
    if( null == ( new Node( prn, "Process Files|newXmlSchema.xsd" ) ) )
    {
      fail( "Unable to check created schema." );
    }

    endTest( );
  }

  public void AddNewComplexType( )
  {
    startTest( );

    Operator.StringComparator op = Operator.getDefaultStringComparator( );
    Operator.setDefaultStringComparator( new CStarttextStringComparator( ) );

    AddItInternal(
        "newXmlSchema.xsd",
        "Complex Types",
        "Add Complex Type",
        "Use Existing Definition",
        "Built-in Types|string",
        "newComplexType"
      );

    Operator.setDefaultStringComparator( op );


    // PURCHASE_SCHEMA_FILE_NAME

    endTest( );
  }

  public void AddProjectReference( )
  {
    startTest( );

    AddProjectReferenceInternal( SAMPLE_NAME, MODULE_NAME );

    endTest( );
  }

  public void OpenSchema( )
  {
    startTest( );

    ProjectsTabOperator pto = new ProjectsTabOperator( );

    ProjectRootNode prn = pto.getProjectRootNode(
        SAMPLE_NAME + "|" + SAMPLE_SCHEMA_PATH + "|" + SAMPLE_SCHEMA_NAME + SAMPLE_SCHEMA_EXT
      );
    prn.select( );

    JTreeOperator tree = pto.tree( );
    tree.clickOnPath(
        tree.findPath( SAMPLE_NAME + "|" + SAMPLE_SCHEMA_PATH + "|" + SAMPLE_SCHEMA_NAME + SAMPLE_SCHEMA_EXT ),
        2
      );

    // Check was it opened or no
    EditorOperator eoSchemaEditor = new EditorOperator( "SynchronousSample.xsd" );
    if( null == eoSchemaEditor )
    {
      fail( SAMPLE_SCHEMA_NAME + SAMPLE_SCHEMA_EXT + " was not opened after double click." );
    }

    endTest( );
  }

  public void ImportFererencedSchema( )
  {
    startTest( );

    CImportClickData[] acliImport =
    {
      new CImportClickData( true, 0, 0, 2, 4, "Unknown import table state after first click, number of rows: ", null ),
      new CImportClickData( true, 2, 0, 2, 5, "Unknown import table state after second click, number of rows: ", null ),
      new CImportClickData( true, 3, 0, 2, 6, "Unknown import table state after third click, number of rows: ", null ),
      new CImportClickData( true, 4, 1, 1, 6, "Unknown import table state after forth click, number of rows: ", null ),
    };

    ImportReferencedSchemaInternal(
        SAMPLE_NAME,
        SAMPLE_SCHEMA_PATH,
        SAMPLE_SCHEMA_NAME + SAMPLE_SCHEMA_EXT,
        MODULE_NAME,
        true,
        acliImport
      );

    endTest( );
  }

  public void CustomizeType( )
  {
    startTest( );

    // Get elements
    SchemaMultiView schema = WaitSchemaMultiView( "SynchronousSample.xsd" );
    JListOperator opList = schema.getColumnListOperator( 0 );
    opList.selectItem( "Elements" );

    // Select typeA
    opList = schema.getColumnListOperator( 1 );
    opList.selectItem( "typeA" );

    // Popup -> Customize
    int iIndex = opList.findItemIndex( "typeA" );
    Point pt = opList.getClickPoint( iIndex );
    opList.clickForPopup( pt.x, pt.y );

    // Click Add / Import...
    JPopupMenuOperator popup = new JPopupMenuOperator( );
    popup.pushMenuNoBlock( "Customize" );

    // Global Element Customizer
    JDialogOperator jCustom = new JDialogOperator( "Global Element Customizer" );

    // Use Existing Type
    JRadioButtonOperator jex = new JRadioButtonOperator( jCustom, "Use Existing Type" );
    jex.setSelected( true );

    // Referenced Schemas
      // import
        // Complex Types
          // newComplexTypes
    
    JTreeOperator jtree = new JTreeOperator( jCustom, 0 );
    TreePath path = jtree.findPath( "Referenced Schemas|import|Complex Types|newComplexType" );
      
    jtree.selectPath( path );
    jtree.clickOnPath( path );

    // Close
    JButtonOperator jOK = new JButtonOperator( jCustom, "OK" );
    jOK.push( );
    WaitDialogClosed( jCustom );

    endTest( );
  }

  public void OpenBPELMapper( )
  {
    startTest( );

    ProjectsTabOperator pto = new ProjectsTabOperator( );

    ProjectRootNode prn = pto.getProjectRootNode(
        SAMPLE_NAME + "|Process Files|" + SAMPLE_BPEL_NAME + SAMPLE_BPEL_EXT
      );
    prn.select( );

    JTreeOperator tree = pto.tree( );
    tree.clickOnPath(
        tree.findPath( SAMPLE_NAME + "|Process Files|" + SAMPLE_BPEL_NAME + SAMPLE_BPEL_EXT ),
        2
      );

    endTest( );
  }

  public void CorrectAssignment( )
  {
    startTest( );

    // Find navigator
    TopComponentOperator top = new TopComponentOperator( SAMPLE_BPEL_NAME + " [Process] - Navigator" );
    JTreeOperator jtNavi = new JTreeOperator( top, 0 );

    // Open tree at:
    Node node = new Node( jtNavi, "Sequence|Assign1" );
    node.select( );

    jtNavi.clickOnPath(
        jtNavi.findPath( "Sequence|Assign1" ),
        2
      );

    // Close infiormation x3
    int iCount = 3;
    while( 0 < iCount )
    {
      for( int i = 0; i < iCount; i++ )
      {
        JDialogOperator ji = new JDialogOperator( "Information", i );
        JButtonOperator jok = new JButtonOperator( ji, "OK" );
        jok.push( );
        try
        {
          ji.waitClosed( );
          iCount--;
          break;
        }
        catch( JemmyException ex )
        {
          System.out.println( "**** Wait failed ****" );
        }
      }
    }

    // Show all existing top components
    top = new TopComponentOperator( "SynchronousSample.bpel" );
    JTreeOperator jts = new JTreeOperator( top, 0 );
    Node nSource = new Node( jts, "Variables|inputVar|inputType|paramA" );
    nSource.select( );
    //JTreeOperator jtd = new JTreeOperator( top, 1 );

    // Create link

    // Save all

    endTest( );
  }

  public void Deploy( )
  {
    startTest( );

    DeployCompositeApplicationInternal( COMPOSITE_APPLICATION_NAME );

    endTest( );
  }

  public void RunExistingTest( )
  {
    startTest( );

    RunTestInternal( COMPOSITE_APPLICATION_NAME, "TestCase0" );

    endTest( );
  }

  public void CreateNewTest( )
  {
    startTest( );

    CreateNewTestInternal( SAMPLE_NAME, COMPOSITE_APPLICATION_NAME );

    endTest( );
  }

  public void RunNewTest( )
  {
    startTest( );

    RunTestInternal( COMPOSITE_APPLICATION_NAME, "TestCase1" );

    endTest( );
  }

}
