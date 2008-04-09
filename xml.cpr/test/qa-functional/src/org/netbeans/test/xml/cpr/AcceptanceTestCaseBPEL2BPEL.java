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

import javax.swing.text.Caret;

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
        //"FindUsages", // TODO : How to find find usages output?
        "ValidateAndBuild",
        "AddAttribute",
        "ExploreAttribute",
        "DeleteAttribute",
        "UndoRedoAttribute",
        "AddComplex",
        "ExploreComplex",
        "DeleteComplex",
        "UndoRedoComplex",
        "AddElement",
        "ExploreElement",
        "DeleteElement",
        "UndoRedoElement",
        "AddSimple",
        "ExploreSimple",
        "DeleteSimple",
        "UndoRedoSimple",




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

    private void ImportReferencedSchemaInternal( boolean bShort )
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

      if( !bShort )
        ExpandByClicks( jto, 3, 1, 1, 9, "Unknown to click on checkbox. #" );
      ExpandByClicks( jto, 7, 1, 1, 9, "Unknown to click on checkbox. #" );

      // Close
      JButtonOperator jOk = new JButtonOperator( jImport, "OK" );
      if( !jOk.isEnabled( ) )
        fail( "OK button disabled after clicking in imports." );
      jOk.push( );

      if( !bShort )
      {
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
    }

    public void ImportReferencedSchema( )
    {
      startTest( );

      ImportReferencedSchemaInternal( false );

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

        try{ Thread.sleep( 1500 ); } catch( InterruptedException ex ) { }
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

      ImportReferencedSchemaInternal( true );

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

    public void ValidateAndBuild( )
    {
      startTest( );

      // Validate
      new JMenuBarOperator(MainWindowOperator.getDefault()).pushMenu("Build|Validate XML");

      // "Output - XML Check"
      OutputOperator out = new OutputOperator( );
      String sText = out.getText( );

      String[] asIdealOutput =
      {
          "XML validation started.",
          "0 Error(s),  0 Warning(s).",
          "XML validation finished."
      };
      // wait till stop line will appear
      while( -1 == sText.indexOf( asIdealOutput[ asIdealOutput.length - 1 ] ) )
      {
        try{ Thread.sleep( 100 ); } catch( InterruptedException ex ) { }
        sText = out.getText( );
      }
      for( String sChecker : asIdealOutput )
      {
        if( -1 == sText.indexOf( sChecker ) )
          fail( "Unable to find ideal XML validate output: \"" + sChecker + "\"\n\"" + sText + "\"" );
      }

      // Build
      ProjectsTabOperator pto = new ProjectsTabOperator( );

      ProjectRootNode prn = pto.getProjectRootNode(
          SAMPLE_NAME
        );
      prn.select( );

      // Ensure we will catch all with any slowness
      MainWindowOperator.StatusTextTracer stt = MainWindowOperator.getDefault( ).getStatusTextTracer( );
      stt.start( );

      prn.performPopupActionNoBlock( "Build" );

      // Wait till JAXB really created
      stt.waitText( "Finished building build.xml (dist_se)." );
      stt.stop( );

      // Get output
      out = new OutputOperator( );
      sText = out.getText( );
      if( -1 == sText.indexOf( "BUILD SUCCESSFUL" ) )
        fail( "Unable to find BUILD SUCCESSFUL mark.\n" );
      if( -1 != sText.indexOf( "BUILD FAILED" ) )
        fail( "BUILD FAILED mark fopund:\n" + sText + "\n" );

      out.close( );

      endTest( );
    }

    private boolean HasChild( JTreeOperator tree, TreePath path, String child )
    {
      int iCnt = tree.getChildCount( path );
      for( int i = 0; i < iCnt; i++ )
      {
        TreePath subpath = tree.getChildPath( path, i );
        Object o = subpath.getLastPathComponent( );
        if( o.toString( ).startsWith( child ) )
          return true;
      }
      return false;
    }

    private TreePath GetSubPath( JTreeOperator tree, TreePath path, String name )
    {
      String[] asPaths = name.split( "\\|" );
      for( String chunk : asPaths )
      {
        int iCnt = tree.getChildCount( path );
        for( int i = 0; i < iCnt; i++ )
        {
          System.out.println( "*** Checking chunk " + chunk );
          TreePath subpath = tree.getChildPath( path, i );
          Object o = subpath.getLastPathComponent( );
          if( o.toString( ).startsWith( chunk ) )
          {
            System.out.println( "*** Chunk found" );
            path = subpath;

            tree.selectPath( path );
            tree.clickOnPath( path );

            break;
          }
        }
      }
      return path;
    }

    private TreePath FindMultiwayPath(
        JTreeOperator tree,
        String schema,
        String name
      )
    {
      // First level is always Referenced schemas
      TreePath path = tree.findPath( "Referenced Schemas" );
      System.out.println( "*** Referenced Schemas found" );
      // Then find path with required subpath (tricky but...)
      int iChild = tree.getChildCount( path );
      TreePath subpath = null;
      for( int i = 0; i < iChild; i++ )
      {
        subpath = tree.getChildPath( path, i );
        if( HasChild( tree, subpath, schema ) )
        {
          System.out.println( "*** Correct import found" );
          return GetSubPath( tree, subpath, name );
        }
      }
      return null;
    }

    public void AddAttribute( )
    {
      startTest( );

      // Swicth to Schema view
      new JMenuBarOperator(MainWindowOperator.getDefault()).pushMenu("View|Editors|Schema");

      // Select first column, Attributes
      SchemaMultiView opMultiView = new SchemaMultiView( PURCHASE_SCHEMA_FILE_NAME );
      opMultiView.switchToSchema( );
      opMultiView.switchToSchemaColumns( );
      JListOperator opList = opMultiView.getColumnListOperator( 0 );
      opList.selectItem( "Attributes" );

      // Right click on Reference Schemas
      int iIndex = opList.findItemIndex( "Attributes" );
      Point pt = opList.getClickPoint( iIndex );
      opList.clickForPopup( pt.x, pt.y );

      // Click Add Attribute...
      JPopupMenuOperator popup = new JPopupMenuOperator( );
      popup.pushMenuNoBlock( "Add Attribute..." );

      // Get dialog
      JDialogOperator jadd = new JDialogOperator( "Add Attribute" );

      // Use existing type by default

      // Get tree
      JTreeOperator jtree = new JTreeOperator( jadd, 0 );
      TreePath path = jtree.findPath( "Referenced Schemas|import|Simple Types|StateType" );
      
      jtree.selectPath( path );
      jtree.clickOnPath( path );

      // Close
      JButtonOperator jOK = new JButtonOperator( jadd, "OK" ); // TODO : OK
      jOK.push( );
      jadd.waitClosed( );

      // Check attribute was added successfully
      opList = opMultiView.getColumnListOperator( 1 );
      iIndex = opList.findItemIndex( "newAttribute" );
      if( -1 == iIndex )
        fail( "Attribute was not added." );

      endTest( );
    }

    private boolean CheckSchemaView( String sView )
    {
      for( int i = 0; i < 2; i++ )
      {
        JMenuBarOperator bar = new JMenuBarOperator( MainWindowOperator.getDefault( ) );
        JMenuItemOperator menu = bar.showMenuItem("View|Editors|" + sView );
        boolean bres = menu.isSelected( );
        bar.closeSubmenus( );
        if( bres )
          return true;
      }
      return false;
    }

    public void ExploreAttribute( )
    {
      startTest( );

      // Explore added with Go to <> menus

      // Select newAttribute
      SchemaMultiView opMultiView = new SchemaMultiView( PURCHASE_SCHEMA_FILE_NAME );
      JListOperator opList = opMultiView.getColumnListOperator( 1 );
      opList.selectItem( "newAttribute" );

      // Right click on Reference Schemas
      int iIndex = opList.findItemIndex( "newAttribute" );
      Point pt = opList.getClickPoint( iIndex );
      opList.clickForPopup( pt.x, pt.y );

      // Click go to definition
      JPopupMenuOperator popup = new JPopupMenuOperator( );
      popup.pushMenu( "Go To|Definition" );

      // Check opened view
      if( !CheckSchemaView( "Schema" ) )
        fail( "Go To Definition option for Schema view opened not on schema view." );

      // Check selected schema item
      SchemaMultiView opMultiViewDef = new SchemaMultiView( LOAN_SCHEMA_FILE_NAME_ORIGINAL );
      JListOperator opListDef = opMultiViewDef.getColumnListOperator( 1 );
      if( !opListDef.getSelectedValue( ).toString( ).startsWith( "StateType" ) )
        fail( "StateType did not selected with Go To Definition option." );

      // Close definition
      opMultiViewDef.close( );

      // Click go to code
      opList.clickForPopup( pt.x, pt.y );
      popup = new JPopupMenuOperator( );
      popup.pushMenu( "Go To|Source" );

      // Check selected code line
      EditorOperator eoXsdCode = new EditorOperator( PURCHASE_SCHEMA_FILE_NAME );
      String sSelectedText = eoXsdCode.getText( eoXsdCode.getLineNumber( ) );
      if( -1 == sSelectedText.indexOf( "<xs:attribute name=\"newAttribute\" type=\"ns2:StateType\"/>" ) )
        fail( "Go To Source feature selected wrong line of code: \"" + sSelectedText + "\"" );

      // Click go to definition
      JEditorPaneOperator txt = eoXsdCode.txtEditorPane( );
      Caret crt = txt.getCaret( );
      pt = crt.getMagicCaretPosition( );
      eoXsdCode.clickForPopup( pt.x, pt.y );
      popup = new JPopupMenuOperator( );
      popup.pushMenu( "Go To|Definition" );

      // Check opened view
      if( !CheckSchemaView( "Source" ) )
        fail( "Go To Definition option for Source view opened not on source view." );

      // Check selected code line
      EditorOperator eoXsdCodeDef = new EditorOperator( LOAN_SCHEMA_FILE_NAME_ORIGINAL );
      sSelectedText = eoXsdCodeDef.getText( eoXsdCodeDef.getLineNumber( ) );
      if( -1 == sSelectedText.indexOf( "<xs:simpleType name=\"StateType\">" ) )
        fail( "StateType did not selected with Go To Definition option: \"" + sSelectedText + "\"" );

      // Close definition
      eoXsdCodeDef.close( );

      // Click go to schema
      txt = eoXsdCode.txtEditorPane( );
      crt = txt.getCaret( );
      pt = crt.getMagicCaretPosition( );
      eoXsdCode.clickForPopup( pt.x, pt.y );
      popup = new JPopupMenuOperator( );
      popup.pushMenu( "Go To|Schema" );
      //try { Thread.sleep( 2000 ); } catch( InterruptedException ex ) { }

      // Check sche,a view opened
      if( !CheckSchemaView( "Schema" ) )
        fail( "Go To Schema option for Source view opened not on source view." );

      // Check selected schema item
      opMultiView = new SchemaMultiView( PURCHASE_SCHEMA_FILE_NAME );
      if( null == ( opList = opMultiView.getColumnListOperator( 1 ) ) )
        fail( "Incorrect (no) selection after Go To Schema option." );
      if( !opList.getSelectedValue( ).toString( ).startsWith( "newAttribute" ) )
        fail( "newAttribute did not selected with Go To Schema option." );

      endTest( );
    }

    public void DeleteAttribute( )
    {
      startTest( );



      endTest( );
    }

    public void UndoRedoAttribute( )
    {
      startTest( );



      endTest( );
    }

    public void AddComplex( )
    {
      startTest( );

      // Swicth to Schema view
      new JMenuBarOperator(MainWindowOperator.getDefault()).pushMenu("View|Editors|Schema");

      // Select first column, Attributes
      SchemaMultiView opMultiView = new SchemaMultiView( PURCHASE_SCHEMA_FILE_NAME );
      opMultiView.switchToSchema( );
      opMultiView.switchToSchemaColumns( );
      JListOperator opList = opMultiView.getColumnListOperator( 0 );
      opList.selectItem( "Complex Types" );

      // Right click on Reference Schemas
      int iIndex = opList.findItemIndex( "Complex Types" );
      Point pt = opList.getClickPoint( iIndex );
      opList.clickForPopup( pt.x, pt.y );

      // Click Add Complex Type...
      JPopupMenuOperator popup = new JPopupMenuOperator( );
      popup.pushMenuNoBlock( "Add Complex Type..." );

      // Get dialog
      JDialogOperator jadd = new JDialogOperator( "Add Complex Type" );

      // Use existing definition
      JRadioButtonOperator jex = new JRadioButtonOperator( jadd, "Use Existing Definition" );
      jex.setSelected( true );

      // Get tree
      JTreeOperator jtree = new JTreeOperator( jadd, 0 );
      /*
      TreePath path = FindMultiwayPath(
          jtree,
          "http://xml.netbeans.org/examples/LoanApplication",
          "Simple Types|StateType"
        );
      */
      TreePath path = jtree.findPath( "Referenced Schemas|import|Complex Types|CarType" );
      
      jtree.selectPath( path );
      jtree.clickOnPath( path );

      // Close
      JButtonOperator jOK = new JButtonOperator( jadd, "OK" ); // TODO : OK
      jOK.push( );
      jadd.waitClosed( );

      endTest( );
    }

    public void ExploreComplex( )
    {
      startTest( );



      endTest( );
    }

    public void DeleteComplex( )
    {
      startTest( );



      endTest( );
    }

    public void UndoRedoComplex( )
    {
      startTest( );



      endTest( );
    }

    public void AddElement( )
    {
      startTest( );

      // Swicth to Schema view
      new JMenuBarOperator(MainWindowOperator.getDefault()).pushMenu("View|Editors|Schema");

      // Select first column, Attributes
      SchemaMultiView opMultiView = new SchemaMultiView( PURCHASE_SCHEMA_FILE_NAME );
      opMultiView.switchToSchema( );
      opMultiView.switchToSchemaColumns( );
      JListOperator opList = opMultiView.getColumnListOperator( 0 );
      opList.selectItem( "Elements" );

      // Right click on Reference Schemas
      int iIndex = opList.findItemIndex( "Elements" );
      Point pt = opList.getClickPoint( iIndex );
      opList.clickForPopup( pt.x, pt.y );

      // Click Add Elements...
      JPopupMenuOperator popup = new JPopupMenuOperator( );
      popup.pushMenuNoBlock( "Add Element..." );

      // Get dialog
      JDialogOperator jadd = new JDialogOperator( "Add Element" );

      // Use existing definition
      JRadioButtonOperator jex = new JRadioButtonOperator( jadd, "Use Existing Type" );
      jex.setSelected( true );

      // Get tree
      JTreeOperator jtree = new JTreeOperator( jadd, 0 );
      /*
      TreePath path = FindMultiwayPath(
          jtree,
          "http://xml.netbeans.org/examples/LoanApplication",
          "Simple Types|StateType"
        );
      */
      TreePath path = jtree.findPath( "Referenced Schemas|import|Complex Types|AddressType" );
      
      jtree.selectPath( path );
      jtree.clickOnPath( path );

      // Close
      JButtonOperator jOK = new JButtonOperator( jadd, "OK" ); // TODO : OK
      jOK.push( );
      jadd.waitClosed( );

      endTest( );
    }

    public void ExploreElement( )
    {
      startTest( );



      endTest( );
    }

    public void DeleteElement( )
    {
      startTest( );



      endTest( );
    }

    public void UndoRedoElement( )
    {
      startTest( );



      endTest( );
    }

    public void AddSimple( )
    {
      startTest( );

      // Swicth to Schema view
      new JMenuBarOperator(MainWindowOperator.getDefault()).pushMenu("View|Editors|Schema");

      // Select first column, Attributes
      SchemaMultiView opMultiView = new SchemaMultiView( PURCHASE_SCHEMA_FILE_NAME );
      opMultiView.switchToSchema( );
      opMultiView.switchToSchemaColumns( );
      JListOperator opList = opMultiView.getColumnListOperator( 0 );
      opList.selectItem( "Simple Types" );

      // Right click on Reference Schemas
      int iIndex = opList.findItemIndex( "Simple Types" );
      Point pt = opList.getClickPoint( iIndex );
      opList.clickForPopup( pt.x, pt.y );

      // Click Add Attribute...
      JPopupMenuOperator popup = new JPopupMenuOperator( );
      popup.pushMenuNoBlock( "Add Simple Type..." );

      // Get dialog
      JDialogOperator jadd = new JDialogOperator( "Add Simple Type" );

      // Use default type

      // Get tree
      JTreeOperator jtree = new JTreeOperator( jadd, 0 );
      /*
      TreePath path = FindMultiwayPath(
          jtree,
          "http://xml.netbeans.org/examples/LoanApplication",
          "Simple Types|StateType"
        );
      */
      TreePath path = jtree.findPath( "Referenced Schemas|import|Simple Types|LoanType" );
      
      jtree.selectPath( path );
      jtree.clickOnPath( path );

      // Close
      JButtonOperator jOK = new JButtonOperator( jadd, "OK" ); // TODO : OK
      jOK.push( );
      jadd.waitClosed( );

      endTest( );
    }

    public void ExploreSimple( )
    {
      startTest( );



      endTest( );
    }

    public void DeleteSimple( )
    {
      startTest( );



      endTest( );
    }

    public void UndoRedoSimple( )
    {
      startTest( );



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
