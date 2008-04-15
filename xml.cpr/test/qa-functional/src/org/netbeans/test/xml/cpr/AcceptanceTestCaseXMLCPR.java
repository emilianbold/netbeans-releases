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
import javax.swing.tree.TreePath;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NewProjectNameLocationStepOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.SaveAllAction;
import org.netbeans.jellytools.nodes.ProjectRootNode;
//import org.netbeans.test.xml.schema.lib.util.Helpers;

import java.io.File;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.*;
import javax.swing.ListModel;
import org.netbeans.api.project.ProjectInformation;
import javax.swing.text.BadLocationException;
import java.awt.Rectangle;
import javax.swing.JEditorPane;
import javax.swing.JTable;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jellytools.OutputOperator;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.test.xml.schema.lib.SchemaMultiView;
import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;

/**
 *
 * @author michaelnazarov@netbeans.org
 */

public class AcceptanceTestCaseXMLCPR extends JellyTestCase {

    protected final String PURCHASE_SCHEMA_FILE_NAME = "purchaseOrder.xsd";

    protected final String LOAN_SCHEMA_FILE_NAME_ORIGINAL = "newLoanApplication.xsd";
    protected final String LOAN_SCHEMA_FILE_NAME_RENAMED = "myLoanApplication.xsd";
    
    static final String BUILD_SUCCESSFUL = "BUILD SUCCESSFUL";
    static final String BUILD_FAILED = "BUILD FAILED";

    protected final String ATTRIBUTES_NAMES[] =
    {
      "newAttributeA", "newAttributeB", "newAttributeC"
    };
    
    protected final String SIMPLE_NAMES[] =
    {
      "newSimpleTypeA", "newSimpleTypeB", "newSimpleTypeC"
    };
    
    // Ideal code lines
    protected final String[] asIdealAttributeLines =
    {
      "<xs:attribute name=\"" + ATTRIBUTES_NAMES[ 0 ] + "\" type=\"ns2:StateType\"/>",
      "<xs:attribute name=\"" + ATTRIBUTES_NAMES[ 1 ] + "\" type=\"ns2:StateType\"/>"
    };

    protected final String[] asIdealSimpleLines =
    {
      "<xs:simpleType name=\"newSimpleTypeA\">",
      "<xs:simpleType name=\"newSimpleTypeB\">",
    };

    class CFulltextStringComparator implements Operator.StringComparator
    {
      public boolean equals( java.lang.String caption, java.lang.String match )
      {
        return caption.equals( match );
      }
    }
    
    public class CImportClickData
    {
      public boolean inshort;
      public int row;
      public int col;
      public int count;
      public int result;
      public String error;
      public String checker;
      
      public CImportClickData(
          boolean _inshort,
          int _row,
          int _col,
          int _count,
          int _result,
          String _error,
          String _checker
        )
      {
        inshort = _inshort;
        row = _row;
        col = _col;
        count = _count;
        result = _result;
        error = _error;
        checker = _checker;
      }
    }

    public AcceptanceTestCaseXMLCPR(String arg0) {
        super(arg0);
    }
    
    public void Dummy( )
    {
        startTest( );
        
        endTest( );
    }
    
    public void CreateBluePrint1SampleInternal(
        String sCategory,
        String sProject,
        String sName
      )
    {
        // Create BluePrint1 Sample
        NewProjectWizardOperator opNewProjectWizard = NewProjectWizardOperator.invoke( );
        opNewProjectWizard.selectCategory( sCategory );
        opNewProjectWizard.selectProject( sProject );
        opNewProjectWizard.next( );

        NewProjectNameLocationStepOperator opNewProjectNameLocationStep = new NewProjectNameLocationStepOperator( );
        opNewProjectNameLocationStep.txtProjectLocation( ).setText( System.getProperty( "xtest.ide.open.projects" ) );
        opNewProjectNameLocationStep.txtProjectName( ).setText( sName );
        opNewProjectWizard.finish( );
    }
    
    protected void AddProjectReferenceInternal(
        String sSample,
        String sModule
      )
    {
      // Access to projects page
      ProjectsTabOperator pto = new ProjectsTabOperator( );

      ProjectRootNode prn = pto.getProjectRootNode( sSample );
      prn.select( );
      prn.performPopupActionNoBlock( "Properties" );

      JDialogOperator dProperties = new JDialogOperator( "Properties" );

      JTreeOperator cTree = new JTreeOperator( dProperties, 0 );
      Node node = new Node( cTree, "Project References" );
      node.select( );

      JButtonOperator addButton = new JButtonOperator(
          dProperties,
          "Add Project..."
        );
      addButton.pushNoBlock( );

      JFileChooserOperator opFileChooser = new JFileChooserOperator( );
      opFileChooser.chooseFile( System.getProperty( "xtest.ide.open.projects" ) + File.separator + sModule );

      JListOperator pList = new JListOperator( dProperties, 0 );
      ListModel lm = pList.getModel( );
      ProjectInformation pi = ( ProjectInformation )lm.getElementAt( 0 );

      if( !pi.getName( ).equals( sModule ) )
      {
        fail( "Unable to find " + sModule + " in the references list." );
      }

      JButtonOperator okButton = new JButtonOperator( dProperties, "OK" );
      okButton.pushNoBlock( );
    }

    public void DeleteProjectReferenceInternal(
        String sSample,
        String sModule
      )
    {
      // Access to projects page
      ProjectsTabOperator pto = new ProjectsTabOperator( );

      ProjectRootNode prn = pto.getProjectRootNode( sSample );
      prn.select( );
      prn.performPopupActionNoBlock( "Properties" );

      JDialogOperator dProperties = new JDialogOperator( "Properties" );

      JTreeOperator cTree = new JTreeOperator( dProperties, 0 );
      Node node = new Node( cTree, "Project References" );
      node.select( );

      JListOperator pList = new JListOperator( dProperties, 0 );
      pList.clickOnItem( 0, 1 );

      JButtonOperator addButton = new JButtonOperator(
          dProperties,
          "Remove"
        );
      addButton.push( );

      ListModel lm = pList.getModel( );
      if( 0 != lm.getSize( ) )
      {
        fail( "Unable to delete " + sModule + " from the references list." );
      }

      JButtonOperator okButton = new JButtonOperator( dProperties, "OK" );
      okButton.pushNoBlock( );
    }

    protected void AddSampleSchemaInternal(
        String sModule,
        String sPath
      )
    {
      ProjectsTabOperator pto = new ProjectsTabOperator( );
      ProjectRootNode prn = pto.getProjectRootNode( sModule );
      prn.select( );

      NewFileWizardOperator opNewFileWizard = NewFileWizardOperator.invoke( );
      opNewFileWizard.selectCategory( "XML" );
      opNewFileWizard.selectFileType( "Loan Application Sample Schema" );
      opNewFileWizard.next( );
      opNewFileWizard.finish( );

      // Check created schema in project tree
      if( null == ( new Node( prn, sPath + "|" + LOAN_SCHEMA_FILE_NAME_ORIGINAL ) ) )
      {
        fail( "Unable to check created sample schema." );
      }
    }

    public void DeleteReferencedSchemaInternal( String sModule )
    {
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
        "<xs:import schemaLocation=\"" + sModule + "/newLoanApplication.xsd\" namespace=\"http://xml.netbeans.org/examples/LoanApplication\"/>",
        "<xs:import schemaLocation=\"inventory.xsd\" namespace=\"http://manufacturing.org/xsd/inventory\"/>"
      };
      for( String sImport : asRequiredLines )
      {
        if( -1 != sCompleteCode.indexOf( sImport ) )
          fail( "Import statement was not removed: \"" + sCompleteCode + "\"" );
      }

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
      //table.selectCell( row, col );
      table.clickOnCell( row, col, count );
      try { Thread.sleep( 500 ); } catch( InterruptedException ex ) { }
      int iRows = table.getRowCount( );
      if( result != iRows )
        fail( error + iRows );
      return;
    }

    protected void ImportReferencedSchemaInternal(
        String sSample,
        String sModule,
        boolean bShort,
        CImportClickData[] aimpData
      )
    {
      ProjectsTabOperator pto = new ProjectsTabOperator( );

      ProjectRootNode prn = pto.getProjectRootNode(
          sSample + "|Process Files|" + PURCHASE_SCHEMA_FILE_NAME
        );
      prn.select( );

      JTreeOperator tree = pto.tree( );
      tree.clickOnPath(
          tree.findPath( sSample + "|Process Files|" + PURCHASE_SCHEMA_FILE_NAME ),
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

      for( CImportClickData cli : aimpData )
      {
        try { Thread.sleep( 1000 ); } catch( InterruptedException ex ) { }
        if( cli.inshort || !bShort )
          ExpandByClicks( jto, cli.row, cli.col, cli.count, cli.result, cli.error );
        // ExpandByClicks( jto, 0, 0, 2, 4, "Unknown import table state after first click, number of rows: " );
        // ExpandByClicks( jto, 1, 0, 2, 5, "Unknown import table state after second click, number of rows: " );
        // ExpandByClicks( jto, 2, 0, 2, 7, "Unknown import table state after third click, number of rows: " );
        // ExpandByClicks( jto, 5, 0, 2, 8, "Unknown import table state after forth click, number of rows: " );
        // ExpandByClicks( jto, 6, 0, 2, 9, "Unknown import table state after third click, number of rows: " );

        // if( !bShort )
        //   ExpandByClicks( jto, 3, 1, 1, 9, "Unknown to click on checkbox. #" );
        // ExpandByClicks( jto, 7, 1, 1, 9, "Unknown to click on checkbox. #" );
      }

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
           "<xs:import schemaLocation=\"" + sModule + "/newLoanApplication.xsd\" namespace=\"http://xml.netbeans.org/examples/LoanApplication\"/>",
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

    protected void ImportReferencedSchema2Internal(
        CImportClickData[] aimpData
      )
    {
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

      for( CImportClickData cli : aimpData )
      {
        ExpandByClicks( jto, cli.row, cli.col, cli.count, cli.result, cli.error );
        if( null != cli.checker )
          new JLabelOperator( jImport, cli.checker );
      }

      // Close
      JButtonOperator jOk = new JButtonOperator( jImport, "OK" );
      if( jOk.isEnabled( ) )
        fail( "OK button enabled after clicking on existing imports." );
      JButtonOperator jCancel = new JButtonOperator( jImport, "Cancel" );
      jCancel.push( );
    }

    public void FindUsagesInternal( String sModule, String sPath )
    {
      // Select schema
      ProjectsTabOperator pto = new ProjectsTabOperator( );

      ProjectRootNode prn = pto.getProjectRootNode(
          sModule + "|" + sPath + "|" + LOAN_SCHEMA_FILE_NAME_ORIGINAL
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
      //FindUsagesOperator fuop = new FindUsagesOperator( );
      
      //System.out.println( "******" + ( null == fuop ) );
    }

    protected void AddItInternal(
        String sItName,
        String sMenuToAdd,
        String sRadioName,
        String sTypePath,
        String sAddedName
      )
    {
      // Swicth to Schema view
      new JMenuBarOperator(MainWindowOperator.getDefault()).pushMenu("View|Editors|Schema");

      // Select first column, Attributes
      SchemaMultiView opMultiView = new SchemaMultiView( PURCHASE_SCHEMA_FILE_NAME );
      opMultiView.switchToSchema( );
      opMultiView.switchToSchemaColumns( );
      JListOperator opList = opMultiView.getColumnListOperator( 0 );
      opList.selectItem( sItName );

      // Right click on Reference Schemas
      int iIndex = opList.findItemIndex( sItName );
      Point pt = opList.getClickPoint( iIndex );
      opList.clickForPopup( pt.x, pt.y );

      // Click Add Attribute...
      JPopupMenuOperator popup = new JPopupMenuOperator( );
      popup.pushMenuNoBlock( sMenuToAdd + "..." );

      // Get dialog
      JDialogOperator jadd = new JDialogOperator( sMenuToAdd );

      // Set unique name
      JTextFieldOperator txt = new JTextFieldOperator( jadd, 0 );
      txt.setText( sAddedName );

      // Use existing definition
      if( null != sRadioName )
      {
        JRadioButtonOperator jex = new JRadioButtonOperator( jadd, sRadioName );
        jex.setSelected( true );
      }

      // Get tree
      JTreeOperator jtree = new JTreeOperator( jadd, 0 );
      TreePath path = jtree.findPath( sTypePath );
      
      jtree.selectPath( path );
      jtree.clickOnPath( path );

      // Close
      JButtonOperator jOK = new JButtonOperator( jadd, "OK" ); // TODO : OK
      jOK.push( );
      jadd.waitClosed( );

      // Check attribute was added successfully
      opList = opMultiView.getColumnListOperator( 1 );
      iIndex = opList.findItemIndex( sAddedName );
      if( -1 == iIndex )
        fail( "Attribute was not added." );

    }

    protected void ExploreSimpleInternal(
        String sName,
        String sType,
        String sIncode,
        String sNamespace
      )
    {
      // Explore added with Go to <> menus

      // Select newAttribute
      SchemaMultiView opMultiView = new SchemaMultiView( PURCHASE_SCHEMA_FILE_NAME );
      JListOperator opList = opMultiView.getColumnListOperator( 1 );
      opList.selectItem( sName );

      // Right click on item
      int iIndex = opList.findItemIndex( sName );
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
      if( !opListDef.getSelectedValue( ).toString( ).startsWith( sType ) )
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

      String sRequiredText = "<xs:" + sIncode + " name=\"" + sName + "\">";
      if( null != sNamespace )
        sRequiredText = "<xs:" + sIncode + " name=\"" + sName + "\" type=\"" + sNamespace + sType+ "\"/>";

      if( -1 == sSelectedText.indexOf( sRequiredText ) )
        fail( "Go To Source feature selected wrong line of code. Selected: \"" + sSelectedText + "\"\nRequired: " + sRequiredText );

      // Click go to definition
      ClickForTextPopup( eoXsdCode );
      popup = new JPopupMenuOperator( );
      popup.pushMenu( "Go To|Definition" );

      // Check opened view
      if( !CheckSchemaView( "Source" ) )
        fail( "Go To Definition option for Source view opened not on source view." );

      // Check selected code line
      EditorOperator eoXsdCodeDef = new EditorOperator( LOAN_SCHEMA_FILE_NAME_ORIGINAL );
      sSelectedText = eoXsdCodeDef.getText( eoXsdCodeDef.getLineNumber( ) );
      if( -1 == sSelectedText.indexOf( "<xs:simpleType name=\"" + sType + "\">" ) )
        fail( "StateType did not selected with Go To Definition option: \"" + sSelectedText + "\"" );

      // Close definition
      eoXsdCodeDef.close( );

      // Click go to schema
      ClickForTextPopup( eoXsdCode );
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
      if( !opList.getSelectedValue( ).toString( ).startsWith( sName ) )
        fail( sName + " did not selected with Go To Schema option." );
    }

    public void ManipulateAttributeInternal( String sSample )
    {
      // Add one more attribute
      AddItInternal(
          "Attributes",
          "Add Attribute",
          null, 
          "Referenced Schemas|import|Simple Types|StateType",
          ATTRIBUTES_NAMES[ 1 ]
        );

      SchemaMultiView opMultiView = new SchemaMultiView( PURCHASE_SCHEMA_FILE_NAME );

      // Check both attributes present in code view
      new JMenuBarOperator(MainWindowOperator.getDefault()).pushMenu("View|Editors|Source");
      EditorOperator eoXMLCode = new EditorOperator( PURCHASE_SCHEMA_FILE_NAME );
      String sCompleteText = eoXMLCode.getText( );
      if(
          -1 == sCompleteText.indexOf( asIdealAttributeLines[ 0 ] )
          || -1 == sCompleteText.indexOf( asIdealAttributeLines[ 1 ] )
        )
      {
        fail( "Attributes code was not added into schema." );
      }

      // Remove code line for first attribute
      eoXMLCode.setCaretPosition(
          asIdealAttributeLines[ 0 ],
          0,
          true
        );
      JEditorPaneOperator editor = eoXMLCode.txtEditorPane( );
      editor.pushKey( KeyEvent.VK_E, InputEvent.CTRL_MASK );
      editor.releaseKey( KeyEvent.VK_E, InputEvent.CTRL_MASK );

      // Check attribute deleted from schema view
      new JMenuBarOperator(MainWindowOperator.getDefault()).pushMenu("View|Editors|Schema");
      JListOperator opList = opMultiView.getColumnListOperator( 0 );
      opList.selectItem( "Attributes" );
      opList = opMultiView.getColumnListOperator( 1 );
      if( 1 != opList.getModel( ).getSize( ) )
        fail( "Invalid number of attribute items in schema view." );
      if( -1 != opList.findItemIndex( ATTRIBUTES_NAMES[ 0 ] ) )
        fail( "Atribute was not removed from schema view" );
      int iIndex = opList.findItemIndex( ATTRIBUTES_NAMES[ 1 ] );
      if( -1 == iIndex )
        fail( "Wrong attribute removed from schema view." );

      // Delete attribute from schema view
      opList.selectItem( iIndex );
      Point pt = opList.getClickPoint( iIndex );
      opList.clickForPopup( pt.x, pt.y );
      JPopupMenuOperator popup = new JPopupMenuOperator( );
      popup.pushMenu( "Delete" );
      JDialogOperator jsafe = new JDialogOperator( "Safe Delete" );
      JButtonOperator jbut = new JButtonOperator( jsafe, "Refactor" );
      jbut.push( );
      jsafe.waitClosed( );

      // Check number of remained items
      int iSize = opList.getModel( ).getSize( );
      if( 0 != iSize )
        fail( "Invalid number of attribute items in schema view: " + iSize );

      // Check attribute deleted from code view
      new JMenuBarOperator(MainWindowOperator.getDefault()).pushMenu("View|Editors|Source");
      eoXMLCode = new EditorOperator( PURCHASE_SCHEMA_FILE_NAME );
      sCompleteText = eoXMLCode.getText( );
      if(
          -1 != sCompleteText.indexOf( asIdealAttributeLines[ 0 ] )
          || -1 != sCompleteText.indexOf( asIdealAttributeLines[ 1 ] )
        )
      {
        fail( "Attributes code was not removed into schema." );
      }

      ////////////////////////////////////////////////////////////////
      // Undo delete from schema
      ProjectsTabOperator pto = new ProjectsTabOperator( );
      ProjectRootNode prn = pto.getProjectRootNode(
          sSample + "|Process Files|" + PURCHASE_SCHEMA_FILE_NAME
        );
      prn.performPopupAction( "Refactor|Undo [Delete " + ATTRIBUTES_NAMES[ 1 ] + "]" );
      //new JMenuBarOperator(MainWindowOperator.getDefault()).pushMenu("Refactor|Undo [Delete " + ATTRIBUTES_NAMES[ 1 ] + "]");

      // Check schema view
      //SchemaMultiView opMultiView = new SchemaMultiView( PURCHASE_SCHEMA_FILE_NAME );
      opList = opMultiView.getColumnListOperator( 0 );
      opList.selectItem( "Attributes" );
      opList = opMultiView.getColumnListOperator( 1 );
      if( -1 != opList.findItemIndex( ATTRIBUTES_NAMES[ 0 ] ) )
        fail( "Wrong attribute restored after deletion." );
      if( -1 == opList.findItemIndex( ATTRIBUTES_NAMES[ 1 ] ) )
        fail( "Correct attribute did not restor after deletion." );

      // Check code view
      new JMenuBarOperator(MainWindowOperator.getDefault()).pushMenu("View|Editors|Source");
      eoXMLCode = new EditorOperator( PURCHASE_SCHEMA_FILE_NAME );
      sCompleteText = eoXMLCode.getText( );
      if( -1 != sCompleteText.indexOf( asIdealAttributeLines[ 0 ] ) )
        fail( "Wrong attribute source restored after deletion." );
      if( -1 == sCompleteText.indexOf( asIdealAttributeLines[ 1 ] ) )
        fail( "Correct attribute source did not restor after deletion." );

      // Redo delete
      pto = new ProjectsTabOperator( );
      prn = pto.getProjectRootNode(
          sSample + "|Process Files|" + PURCHASE_SCHEMA_FILE_NAME
        );
      prn.performPopupAction( "Refactor|Redo [Delete " + ATTRIBUTES_NAMES[ 1 ] + "]" );
      //new JMenuBarOperator(MainWindowOperator.getDefault()).pushMenu("Refactor|Redo [Delete " + ATTRIBUTES_NAMES[ 1 ] + "]");

      // Check source
      sCompleteText = eoXMLCode.getText( );
      if(
          -1 != sCompleteText.indexOf( asIdealAttributeLines[ 0 ] )
          || -1 != sCompleteText.indexOf( asIdealAttributeLines[ 1 ] )
        )
        fail( "Redo attribute deletion failed for source code." );

      // Check schema
      new JMenuBarOperator(MainWindowOperator.getDefault()).pushMenu("View|Editors|Schema");
      if(
          -1 != opList.findItemIndex( ATTRIBUTES_NAMES[ 0 ] )
          || -1 != opList.findItemIndex( ATTRIBUTES_NAMES[ 1 ] )
        )
        fail( "Redo attribute deletion failed for schema view." );
    }

    protected void ManipulateSimpleInternal( String sSample )
    {
      // Add one more attribute
      AddItInternal(
          "Simple Types",
          "Add Simple Type",
          null, 
          "Referenced Schemas|import|Simple Types|LoanType",
          SIMPLE_NAMES[ 1 ]
        );

      SchemaMultiView opMultiView = new SchemaMultiView( PURCHASE_SCHEMA_FILE_NAME );

      // Check both attributes present in code view
      new JMenuBarOperator(MainWindowOperator.getDefault()).pushMenu("View|Editors|Source");
      EditorOperator eoXMLCode = new EditorOperator( PURCHASE_SCHEMA_FILE_NAME );
      String sCompleteText = eoXMLCode.getText( );
      if(
          -1 == sCompleteText.indexOf( asIdealSimpleLines[ 0 ] )
          || -1 == sCompleteText.indexOf( asIdealSimpleLines[ 1 ] )
        )
      {
        fail( "Simple type code was not added into schema." );
      }

      // Remove code line for first attribute
      eoXMLCode.setCaretPosition(
          asIdealSimpleLines[ 0 ],
          0,
          true
        );
      JEditorPaneOperator editor = eoXMLCode.txtEditorPane( );
      for( int ip = 0; ip < 3; ip++ )
      {
        editor.pushKey( KeyEvent.VK_E, InputEvent.CTRL_MASK );
        editor.releaseKey( KeyEvent.VK_E, InputEvent.CTRL_MASK );
      }

      // Check attribute deleted from schema view
      new JMenuBarOperator(MainWindowOperator.getDefault()).pushMenu("View|Editors|Schema");
      JListOperator opList = opMultiView.getColumnListOperator( 0 );
      opList.selectItem( "Simple Types" );
      opList = opMultiView.getColumnListOperator( 1 );
      if( 1 != opList.getModel( ).getSize( ) )
        fail( "Invalid number of simple type items in schema view." );
      if( -1 != opList.findItemIndex( ATTRIBUTES_NAMES[ 0 ] ) )
        fail( "Simple type was not removed from schema view" );
      int iIndex = opList.findItemIndex( SIMPLE_NAMES[ 1 ] );
      if( -1 == iIndex )
        fail( "Wrong simple type removed from schema view." );

      // Delete attribute from schema view
      opList.selectItem( iIndex );
      Point pt = opList.getClickPoint( iIndex );
      opList.clickForPopup( pt.x, pt.y );
      JPopupMenuOperator popup = new JPopupMenuOperator( );
      popup.pushMenu( "Delete" );
      JDialogOperator jsafe = new JDialogOperator( "Safe Delete" );
      JButtonOperator jbut = new JButtonOperator( jsafe, "Refactor" );
      jbut.push( );
      jsafe.waitClosed( );

      // Check number of remained items
      int iSize = opList.getModel( ).getSize( );
      if( 0 != iSize )
        fail( "Invalid number of simple type items in schema view: " + iSize );

      // Check attribute deleted from code view
      new JMenuBarOperator(MainWindowOperator.getDefault()).pushMenu("View|Editors|Source");
      eoXMLCode = new EditorOperator( PURCHASE_SCHEMA_FILE_NAME );
      sCompleteText = eoXMLCode.getText( );
      if(
          -1 != sCompleteText.indexOf( asIdealSimpleLines[ 0 ] )
          || -1 != sCompleteText.indexOf( asIdealSimpleLines[ 1 ] )
        )
      {
        fail( "Simple type code was not removed into schema." );
      }

      ////////////////////////////////////////////////////////////////

      // Undo delete from schema
      ProjectsTabOperator pto = new ProjectsTabOperator( );
      ProjectRootNode prn = pto.getProjectRootNode(
          sSample + "|Process Files|" + PURCHASE_SCHEMA_FILE_NAME
        );
      prn.performPopupAction( "Refactor|Undo [Delete " + SIMPLE_NAMES[ 1 ] + "]" );
      //new JMenuBarOperator(MainWindowOperator.getDefault()).pushMenu("Refactor|Undo [Delete " + SIMPLE_NAMES[ 1 ] + "]");

      // Check schema view
      //SchemaMultiView opMultiView = new SchemaMultiView( PURCHASE_SCHEMA_FILE_NAME );
      opList = opMultiView.getColumnListOperator( 0 );
      opList.selectItem( "Simple Types" );
      opList = opMultiView.getColumnListOperator( 1 );
      if( -1 != opList.findItemIndex( SIMPLE_NAMES[ 0 ] ) )
        fail( "Wrong simple type restored after deletion." );
      if( -1 == opList.findItemIndex( SIMPLE_NAMES[ 1 ] ) )
        fail( "Correct simple type did not restor after deletion." );

      // Check code view
      new JMenuBarOperator(MainWindowOperator.getDefault()).pushMenu("View|Editors|Source");
      eoXMLCode = new EditorOperator( PURCHASE_SCHEMA_FILE_NAME );
      sCompleteText = eoXMLCode.getText( );
      if( -1 != sCompleteText.indexOf( asIdealSimpleLines[ 0 ] ) )
        fail( "Wrong simple type source restored after deletion." );
      if( -1 == sCompleteText.indexOf( asIdealSimpleLines[ 1 ] ) )
        fail( "Correct simple typesource did not restor after deletion." );

      // Redo delete
      pto = new ProjectsTabOperator( );
      prn = pto.getProjectRootNode(
          sSample + "|Process Files|" + PURCHASE_SCHEMA_FILE_NAME
        );
      prn.performPopupAction( "Refactor|Redo [Delete " + SIMPLE_NAMES[ 1 ] + "]" );
      //new JMenuBarOperator(MainWindowOperator.getDefault()).pushMenu("Refactor|Redo [Delete " + ATTRIBUTES_NAMES[ 1 ] + "]");

      // Check source
      sCompleteText = eoXMLCode.getText( );
      if(
          -1 != sCompleteText.indexOf( asIdealSimpleLines[ 0 ] )
          || -1 != sCompleteText.indexOf( asIdealSimpleLines[ 1 ] )
        )
        fail( "Redo simple type deletion failed for source code." );

      // Check schema
      new JMenuBarOperator(MainWindowOperator.getDefault()).pushMenu("View|Editors|Schema");
      if(
          -1 != opList.findItemIndex( SIMPLE_NAMES[ 0 ] )
          || -1 != opList.findItemIndex( SIMPLE_NAMES[ 1 ] )
        )
        fail( "Redo simple type deletion failed for schema view." );
    }

    protected boolean CheckSchemaView( String sView )
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

    protected void ClickForTextPopup( EditorOperator eo )
    {
      JEditorPaneOperator txt = eo.txtEditorPane( );
      JEditorPane epane =  ( JEditorPane )txt.getSource( );
      try
      {
        Rectangle rct = epane.modelToView( epane.getCaretPosition( ) );
        txt.clickForPopup( rct.x, rct.y );
      }
      catch( BadLocationException ex )
      {
        System.out.println( "=== Bad location" );
      }

      return;
    }

    public void RenameSampleSchemaInternal( String sModule, String sPath )
    {
      // Select schema
      ProjectsTabOperator pto = new ProjectsTabOperator( );

      ProjectRootNode prn = pto.getProjectRootNode(
          sModule + "|" + sPath + "|" + LOAN_SCHEMA_FILE_NAME_ORIGINAL
        );
      prn.select( );

      // Refactor rename
      prn.performPopupActionNoBlock( "Refactor|Rename..." );

      // Refactor
      JDialogOperator jdRefactor = new JDialogOperator( "File Rename" );

      JTextFieldOperator jbName = new JTextFieldOperator( jdRefactor, 0 );
      jbName.setText( "myLoanApplication" );

      JButtonOperator jbRef = new JButtonOperator( jdRefactor, "Refactor" );
      jbRef.push( );

      jdRefactor.waitClosed( );

      // Check result
      pto = new ProjectsTabOperator( );

      prn = pto.getProjectRootNode(
          sModule + "|" + sPath + "|" + LOAN_SCHEMA_FILE_NAME_RENAMED
        );
      if( null == prn )
      {
        fail( "Unable to rename sample schema!" );
      }
    }

    public void UndoRenameSampleSchemaInternal( String sModule, String sPath )
    {
      // Undo
      new JMenuBarOperator(MainWindowOperator.getDefault()).pushMenu("Refactor|Undo [File Rename]");

      // Check result
      ProjectsTabOperator pto = new ProjectsTabOperator( );

      ProjectRootNode prn = pto.getProjectRootNode(
          sModule + "|" + sPath + "|" + LOAN_SCHEMA_FILE_NAME_ORIGINAL
        );
      if( null == prn )
      {
        fail( "Unable to undo rename sample schema!" );
      }
    }

    public void RedoRenameSampleSchemaInternal( String sModule, String sPath )
    {
      // Redo
      ProjectsTabOperator pto = new ProjectsTabOperator( );

      ProjectRootNode prn = pto.getProjectRootNode(
          sModule + "|" + sPath + "|" + LOAN_SCHEMA_FILE_NAME_ORIGINAL
        );

      prn.performPopupAction( "Refactor|Redo [File Rename]" );

      prn = pto.getProjectRootNode(
          sModule + "|" + sPath + "|" + LOAN_SCHEMA_FILE_NAME_RENAMED
        );
      if( null == prn )
      {
        fail( "Unable to redo rename sample schema!" );
      }
    }

    protected void ValidateAndBuildInternal( String sSample )
    {
      // Set focus to file for validation to ensure Validate menu enabled
      new JMenuBarOperator(MainWindowOperator.getDefault()).pushMenu("Window|Editor");
      
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
          sSample
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
      if( -1 == sText.indexOf( BUILD_SUCCESSFUL ) )
        fail( "Unable to find BUILD SUCCESSFUL mark.\n" );
      if( -1 != sText.indexOf( BUILD_FAILED ) )
        fail( "BUILD FAILED mark fopund:\n" + sText + "\n" );

      // Close output
      out.close( );
    }

    protected void DeployCompositeApplicationInternal( String sName )
    {
      // Access to projects page
      ProjectsTabOperator pto = new ProjectsTabOperator( );

      ProjectRootNode prn = pto.getProjectRootNode( sName );
      prn.select( );

      // Ensure we will catch all with any slowness
      MainWindowOperator.StatusTextTracer stt = MainWindowOperator.getDefault( ).getStatusTextTracer( );
      stt.start( );

      prn.performPopupActionNoBlock( "Deploy" );

      // Check window with AS selection
      try
      {
        JDialogOperator jselect = new JDialogOperator( "Warning - Select Server" );
        JButtonOperator jOk = new JButtonOperator( jselect, "OK" );
        jOk.push( );
        jselect.waitClosed( );
      }
      catch( JemmyException ex )
      {
          // No selection window.
          // It's strange but normal.
      }

      // Wait till JAXB really created
      stt.waitText( "Finished building build.xml (run)." );
      stt.stop( );

      // Check output for BUILD SUCCESSFUL
      // Get output
      OutputOperator out = new OutputOperator( );
      out.getOutputTab( "build.xml (run)" );
      String sText = out.getText( );
      if( -1 == sText.indexOf( BUILD_SUCCESSFUL ) )
        fail( "Unable to deploy composite application.\n" + sText + "\n" );
      if( -1 != sText.indexOf( BUILD_FAILED ) )
        fail( "BUILD FAILED mark found for deploy CA:\n" + sText + "\n" );
    }

    public void BuildCompositeApplicationInternal( String sName )
    {
      // Access to projects page
      ProjectsTabOperator pto = new ProjectsTabOperator( );

      ProjectRootNode prn = pto.getProjectRootNode( sName );
      prn.select( );

      // Ensure we will catch all with any slowness
      MainWindowOperator.StatusTextTracer stt = MainWindowOperator.getDefault( ).getStatusTextTracer( );
      stt.start( );

      prn.performPopupActionNoBlock( "Build" );

      // Wait till JAXB really created
      stt.waitText( "Finished building build.xml (jbi-build)." );
      stt.stop( );

      // Check output for BUILD SUCCESSFUL
      // Get output
      OutputOperator out = OutputOperator.invoke( );
      String sText = out.getText( );
      if( -1 == sText.indexOf( BUILD_SUCCESSFUL ) )
        fail( "Unable to build composite application.\n" + sText + "\n" );
      if( -1 != sText.indexOf( BUILD_FAILED ) )
        fail( "BUILD FAILED mark found for build CA:\n" + sText + "\n" );
    }

    public void tearDown() {
        new SaveAllAction().performAPI();
    }

}
