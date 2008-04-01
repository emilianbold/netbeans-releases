/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

package org.netbeans.test.xml.cpr;

import java.awt.Point;
import java.util.zip.CRC32;
import javax.swing.tree.TreePath;
import junit.framework.TestSuite;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NewProjectNameLocationStepOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jellytools.OutputOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.WizardOperator;
import org.netbeans.jellytools.actions.SaveAllAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
//import org.netbeans.test.xml.schema.lib.SchemaMultiView;
//import org.netbeans.test.xml.schema.lib.util.Helpers;

import org.netbeans.jemmy.operators.JFileChooserOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import java.io.File;
import org.netbeans.jellytools.MainWindowOperator;
import java.awt.event.KeyEvent;
//import java.awt.Robot;
import org.netbeans.jellytools.FilesTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jemmy.operators.*;
import javax.swing.ListModel;
//import org.netbeans.modules.bpel.project.BpelproProject;
import org.netbeans.api.project.ProjectInformation;
import java.lang.reflect.Method;
import org.netbeans.test.xml.schema.lib.util.Helpers;
import javax.swing.JList;
import org.netbeans.test.xml.schema.lib.SchemaMultiView;
import java.awt.Point;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.jemmy.ComponentChooser;
import javax.swing.JTable;
import javax.swing.tree.TreeNode;
//import org.openide.explorer.view.VisualizerNode;

import org.netbeans.test.xml.cpr.lib.FindUsagesOperator;

import org.netbeans.jemmy.JemmyException;

/**
 *
 * @author michaelnazarov@netbeans.org
 */

public class AcceptanceTestCaseBPEL2BPEL extends AcceptanceTestCaseXMLCPR {
    
    static final String [] m_aTestMethods = {
        "CreateBluePrint1Sample",
        "CreateBPELModule",
        "AddProjectReference",
        "DeleteProjectReference",
        "AddSampleSchema",
        "ImportReferencedSchema",
        "ImportReferencedSchema2",
        "DeleteReferencedSchema",
        "FindUsages",

        "RenameSampleSchema",
        "UndoRenameSampleSchema",
        "RedoRenameSampleSchema",
    };

    static final String SAMPLE_CATEGORY_NAME = "Samples|SOA|BPEL BluePrints";
    static final String SAMPLE_PROJECT_NAME = "BluePrint 1";
    static final String SAMPLE_NAME = "SampleApplication2Bpel";

    static final String MODULE_CATEGORY_NAME = "SOA";
    static final String MODULE_PROJECT_NAME = "BPEL Module";
    static final String MODULE_NAME = "BpelModule";

    static final String SAMPLE_SCHEMA_PATH = "Process Files";

    public AcceptanceTestCaseBPEL2BPEL(String arg0) {
        super(arg0);
    }
    
    public static TestSuite suite() {
        TestSuite testSuite = new TestSuite(AcceptanceTestCaseBPEL2BPEL.class.getName());
        
        for (String strMethodName : m_aTestMethods) {
            testSuite.addTest(new AcceptanceTestCaseBPEL2BPEL(strMethodName));
        }
        
        return testSuite;
    }
    
    public void CreateBluePrint1Sample( )
    {
        startTest( );

        CreateBluePrint1SampleInternal(
            SAMPLE_CATEGORY_NAME,
            SAMPLE_PROJECT_NAME,
            SAMPLE_NAME
          );

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

    public void AddProjectReference( )
    {
      startTest( );

      AddProjectReferenceInternal( SAMPLE_NAME, MODULE_NAME );

      endTest( );
    }
    
    public void DeleteProjectReference( )
    {
      startTest( );

      DeleteProjectReferenceInternal( SAMPLE_NAME, MODULE_NAME );

      AddProjectReferenceInternal( SAMPLE_NAME, MODULE_NAME );

      endTest( );
    }

    public void AddSampleSchema( )
    {
      startTest( );

      ProjectsTabOperator pto = new ProjectsTabOperator( );
      ProjectRootNode prn = pto.getProjectRootNode( MODULE_NAME );
      prn.select( );

      NewFileWizardOperator opNewFileWizard = NewFileWizardOperator.invoke( );
      opNewFileWizard.selectCategory( "XML" );
      opNewFileWizard.selectFileType( "Loan Application Sample Schema" );
      opNewFileWizard.next( );
      opNewFileWizard.finish( );

      // Check created schema in project tree
      if( null == ( new Node( prn, "Process Files|newLoanApplication.xsd" ) ) )
      {
        fail( "Unable to check created sample schema." );
      }

      endTest( );
    }

    private void ExpandByClicks(
        JTableOperator table,
        int row,
        int col,
        int count,
        int result,
        String error
      )
    {
      table.clickOnCell( row, col, count );
       int iRows = table.getRowCount( );
       if( result != iRows )
         fail( error + iRows );
      return;
    }

    class CFulltextStringComparator implements Operator.StringComparator
    {
      public boolean equals( java.lang.String caption, java.lang.String match )
      {
        return caption.equals( match );
      }
    }

    private void ImportReferencedSchemaInternal( )
    {
      ProjectsTabOperator pto = new ProjectsTabOperator( );

      ProjectRootNode prn = pto.getProjectRootNode(
          SAMPLE_NAME + "|Process Files|" + PURCHASE_SCHEMA_FILE_NAME
        );
      prn.select( );

      JTreeOperator tree = pto.tree( );
      tree.clickOnPath(
          tree.findPath( SAMPLE_NAME + "|Process Files|" + PURCHASE_SCHEMA_FILE_NAME ),
          2
        );

      // Check was it opened or no
      EditorOperator eoSchemaEditor = new EditorOperator( PURCHASE_SCHEMA_FILE_NAME );
      if( null == eoSchemaEditor )
      {
        fail( PURCHASE_SCHEMA_FILE_NAME + " was not opened after double click." );
      }

      // Switch to schema view
      new JMenuBarOperator(MainWindowOperator.getDefault()).pushMenu("View|Editors|Schema");
      // ^ - remove???

      // Select first column
      SchemaMultiView opMultiView = new SchemaMultiView( PURCHASE_SCHEMA_FILE_NAME );
      opMultiView.switchToSchema( );
      opMultiView.switchToSchemaColumns( );
      JListOperator opList = opMultiView.getColumnListOperator( 0 );
      opList.selectItem( "Referenced Schemas" );

      // Right click on Reference Schemas
      int iIndex = opList.findItemIndex( "Referenced Schemas" );
      Point pt = opList.getClickPoint( iIndex );
      opList.clickForPopup( pt.x, pt.y );

      // Click Add / Import...
      JPopupMenuOperator popup = new JPopupMenuOperator( );
      popup.pushMenuNoBlock( "Add|Import..." );

      // Get import dialog
      JDialogOperator jImport = new JDialogOperator( "Add Import" );

      // Import required files
      JTableOperator jto = new JTableOperator( jImport, new ComponentChooser( ) {
            public java.lang.String getDescription() { return "getDesriptor: looking for happy"; }
            public boolean checkComponent( java.awt.Component comp ) { if( comp instanceof JTable ) return true; return false; }
          }
      );

      int iRows = jto.getRowCount( );
      if( 2 != iRows )
        fail( "Unknown initial import table state, number of rows: " + iRows );

      ExpandByClicks( jto, 0, 0, 2, 4, "Unknown import table state after first click, number of rows: " );
      ExpandByClicks( jto, 1, 0, 2, 5, "Unknown import table state after second click, number of rows: " );
      ExpandByClicks( jto, 2, 0, 2, 7, "Unknown import table state after third click, number of rows: " );
      ExpandByClicks( jto, 5, 0, 2, 8, "Unknown import table state after forth click, number of rows: " );
      ExpandByClicks( jto, 6, 0, 2, 9, "Unknown import table state after third click, number of rows: " );

      ExpandByClicks( jto, 3, 1, 1, 9, "Unknown to click on checkbox. #" );
      ExpandByClicks( jto, 7, 1, 1, 9, "Unknown to click on checkbox. #" );

      // Close
      JButtonOperator jOk = new JButtonOperator( jImport, "OK" );
      if( !jOk.isEnabled( ) )
        fail( "OK button disabled after clicking in imports." );
      jOk.push( );

      int iResult = 0;
      for( int i = 0; i <= 1; i++ )
      {
        // Check imported files in list
        opList = opMultiView.getColumnListOperator( 1 );

        // Go to source
        iIndex = i;//opList.findItemIndex( asImported[ 0 ] );
        opList.selectItem( iIndex );
        pt = opList.getClickPoint( iIndex );
        opList.clickForPopup( pt.x, pt.y );

        // Click Add / Import...
        popup = new JPopupMenuOperator( );
        popup.pushMenu( "Go To Source" );

        // Check text
        String[] asRequiredLines =
        {
          "<xs:import schemaLocation=\"" + MODULE_NAME + "/newLoanApplication.xsd\" namespace=\"http://xml.netbeans.org/examples/LoanApplication\"/>",
          "<xs:import schemaLocation=\"inventory.xsd\" namespace=\"http://manufacturing.org/xsd/inventory\"/>"
        };
        EditorOperator eoXMLSource = new EditorOperator( PURCHASE_SCHEMA_FILE_NAME );
        int iLineNumber = eoXMLSource.getLineNumber( );
        String sChoosenLine = eoXMLSource.getText( iLineNumber );
        for( int j = 0; j < asRequiredLines.length; j++ )
          if( -1 != sChoosenLine.indexOf( asRequiredLines[ j ] ) )
            iResult += ( j + 1 );//fail( "Go to source came to unknown line: #" + iLineNumber + ", \"" + sChoosenLine + "\"" );

        // Switch back
        opMultiView.switchToSchema( );
        opMultiView.switchToSchemaColumns( );
      }
      if( 3 != iResult )
        fail( "Go to source works incorrect way, not all elements recognized: " + iResult );
    }

    public void ImportReferencedSchema( )
    {
      startTest( );

      ImportReferencedSchemaInternal( );

      endTest( );
    }

    public void ImportReferencedSchema2( )
    {
      startTest( );

      // Select first column
      SchemaMultiView opMultiView = new SchemaMultiView( PURCHASE_SCHEMA_FILE_NAME );
      opMultiView.switchToSchema( );
      opMultiView.switchToSchemaColumns( );
      JListOperator opList = opMultiView.getColumnListOperator( 0 );
      opList.selectItem( "Referenced Schemas" );

      // Right click on Reference Schemas
      int iIndex = opList.findItemIndex( "Referenced Schemas" );
      Point pt = opList.getClickPoint( iIndex );
      opList.clickForPopup( pt.x, pt.y );

      // Click Add / Import...
      JPopupMenuOperator popup = new JPopupMenuOperator( );
      popup.pushMenuNoBlock( "Add|Import..." );

      // Get import dialog
      JDialogOperator jImport = new JDialogOperator( "Add Import" );

      // Import required files
      JTableOperator jto = new JTableOperator( jImport, new ComponentChooser( ) {
            public java.lang.String getDescription() { return "getDesriptor: looking for happy"; }
            public boolean checkComponent( java.awt.Component comp ) { if( comp instanceof JTable ) return true; return false; }
          }
      );

      int iRows = jto.getRowCount( );
      if( 2 != iRows )
        fail( "Unknown initial import table state, number of rows: " + iRows );

      ExpandByClicks( jto, 0, 0, 2, 4, "Unknown import table state after first click, number of rows: " );
      ExpandByClicks( jto, 1, 0, 2, 5, "Unknown import table state after second click, number of rows: " );
      ExpandByClicks( jto, 2, 0, 2, 7, "Unknown import table state after third click, number of rows: " );
      ExpandByClicks( jto, 5, 0, 2, 8, "Unknown import table state after forth click, number of rows: " );
      ExpandByClicks( jto, 6, 0, 2, 9, "Unknown import table state after third click, number of rows: " );

      ExpandByClicks( jto, 3, 1, 1, 9, "Unknown to click on checkbox. #" );
      JLabelOperator jl = new JLabelOperator( jImport, "Selected document is already referenced." );
      ExpandByClicks( jto, 4, 1, 1, 9, "Unknown to click on checkbox. #" );
      jl = new JLabelOperator( jImport, "Document cannot reference itself." );
      ExpandByClicks( jto, 7, 1, 1, 9, "Unknown to click on checkbox. #" );
      jl = new JLabelOperator( jImport, "Selected document is already referenced." );

      // Close
      JButtonOperator jOk = new JButtonOperator( jImport, "OK" );
      if( jOk.isEnabled( ) )
        fail( "OK button enabled after clicking on existing imports." );
      JButtonOperator jCancel = new JButtonOperator( jImport, "Cancel" );
      jCancel.push( );

      endTest( );
    }

    public void DeleteReferencedSchema( )
    {
      startTest( );

      // Select first column
      SchemaMultiView opMultiView = new SchemaMultiView( PURCHASE_SCHEMA_FILE_NAME );
      opMultiView.switchToSchema( );
      opMultiView.switchToSchemaColumns( );
      JListOperator opList = opMultiView.getColumnListOperator( 0 );
      opList.selectItem( "Referenced Schemas" );

      opList = opMultiView.getColumnListOperator( 1 );

      // Click Delete
      for( int i = 0; i < 2; i++ )
      {
        // Right click on Reference Schemas
        opList.selectItem( 0 );

        // Right click on Reference Schemas
        Point pt = opList.getClickPoint( 0 );
        opList.clickForPopup( pt.x, pt.y );

        JPopupMenuOperator popup = new JPopupMenuOperator( );
        popup.pushMenu( "Delete" );

        try{ Thread.sleep( 500 ); } catch( InterruptedException ex ) { }
      }

      // Go to source
      opMultiView.switchToSource( );

      // Check there is no more imports
      EditorOperator eoXMLSource = new EditorOperator( PURCHASE_SCHEMA_FILE_NAME );
      String sCompleteCode = eoXMLSource.getText( );
      String[] asRequiredLines =
      {
        "<xs:import schemaLocation=\"" + MODULE_NAME + "/newLoanApplication.xsd\" namespace=\"http://xml.netbeans.org/examples/LoanApplication\"/>",
        "<xs:import schemaLocation=\"inventory.xsd\" namespace=\"http://manufacturing.org/xsd/inventory\"/>"
      };
      for( String sImport : asRequiredLines )
      {
        if( -1 != sCompleteCode.indexOf( sImport ) )
          fail( "Import statement was not removed: \"" + sCompleteCode + "\"" );
      }

      ImportReferencedSchemaInternal( );

      endTest( );
    }

    public void FindUsages( )
    {
      startTest( );

      // Select schema
      ProjectsTabOperator pto = new ProjectsTabOperator( );

      ProjectRootNode prn = pto.getProjectRootNode(
          MODULE_NAME + "|" + "Process Files" + "|" + LOAN_SCHEMA_FILE_NAME_ORIGINAL
        );
      prn.select( );

      // Refactor rename
      prn.performPopupAction( "Find Usages" );

      // Wait window
      JDialogOperator opFindProgress = null;
      try
      {
        opFindProgress = new JDialogOperator( "Find XML Usages" );
      }
      catch( JemmyException ex )
      {
        // No window found. Too fast?
      }
      if( null != opFindProgress )
      {
        System.out.println( "****** USAGES HERE" );
        opFindProgress.waitClosed( );
      }

      // Check result
      FindUsagesOperator fuop = new FindUsagesOperator( );
      
      System.out.println( "******" + ( null == fuop ) );

      endTest( );
    }

    public void RenameSampleSchema( )
    {
      startTest( );

      RenameSampleSchemaInternal( MODULE_NAME, SAMPLE_SCHEMA_PATH );

      endTest( );
    }

    public void UndoRenameSampleSchema( )
    {
      startTest( );

      UndoRenameSampleSchemaInternal( MODULE_NAME, SAMPLE_SCHEMA_PATH );

      endTest( );
    }

    public void RedoRenameSampleSchema( )
    {
      startTest( );

      RedoRenameSampleSchemaInternal( MODULE_NAME, SAMPLE_SCHEMA_PATH );

      endTest( );
    }

    public void tearDown() {
        new SaveAllAction().performAPI();
    }

    protected void startTest(){
        super.startTest();
        //Helpers.closeUMLWarningIfOpened();
    }

}
