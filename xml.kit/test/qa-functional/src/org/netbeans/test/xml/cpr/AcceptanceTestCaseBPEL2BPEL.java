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

package org.netbeans.test.xml.cpr;

import javax.swing.tree.TreePath;
import junit.framework.TestSuite;
import org.netbeans.jellytools.NewProjectNameLocationStepOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;

import org.netbeans.jemmy.operators.*;
// import org.netbeans.api.project.ProjectInformation;
import org.netbeans.test.xml.schema.lib.util.Helpers;
import org.netbeans.test.xml.schema.lib.SchemaMultiView;
import org.netbeans.jellytools.ProjectsTabOperator;
import javax.swing.tree.TreeNode;
import org.netbeans.jellytools.nodes.Node;

import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.EditorOperator;
import java.awt.event.KeyEvent;
import java.awt.Point;
import java.awt.event.InputEvent;
import javax.swing.ListModel;
import org.netbeans.jellytools.TopComponentOperator;

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
        "ValidateAndBuild",
        "AddAttribute",
        "ExploreAttribute",
        "ManipulateAttribute",
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
        "ManipulateSimple",
        "RenameSampleSchema",
        "UndoRenameSampleSchema",
        "RedoRenameSampleSchema",
        "FindUsages2",
        "ValidateAndBuild",

        //"MoveSchema",
        //"FixInMoved",
        //"FixInReferenced",

        "ValidateAndBuild",
        "BuildCompositeApplication",
        "DeployCompositeApplication", // This will failed, followed skipped
        "CreateNewTest",
        "RunNewTest",
    };

    static final String SAMPLE_CATEGORY_NAME = "Samples|SOA|BPEL BluePrints";
    static final String SAMPLE_PROJECT_NAME = "BluePrint 1";
    static final String SAMPLE_NAME = "SampleApplication2Bpel";
    static final String COMPOSITE_APPLICATION_NAME = SAMPLE_NAME + "Application";

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

      AddSampleSchemaInternal( MODULE_NAME, SAMPLE_SCHEMA_PATH );

      endTest( );
    }

    class CFulltextStringComparator implements Operator.StringComparator
    {
      public boolean equals( java.lang.String caption, java.lang.String match )
      {
        return caption.equals( match );
      }
    }

    private CImportClickData[] acliImport =
    {
      new CImportClickData( true, 0, 0, 2, 4, "Unknown import table state after first click, number of rows: ", null ),
      new CImportClickData( true, 1, 0, 2, 5, "Unknown import table state after second click, number of rows: ", null ),
      new CImportClickData( true, 2, 0, 2, 7, "Unknown import table state after third click, number of rows: ", null ),
      new CImportClickData( true, 5, 0, 2, 8, "Unknown import table state after forth click, number of rows: ", null ),
      new CImportClickData( true, 6, 0, 2, 9, "Unknown import table state after third click, number of rows: ", null ),
      new CImportClickData( false, 3, 1, 1, 9, "Unknown to click on checkbox. #", null ),
      new CImportClickData( true, 7, 1, 1, 9, "Unknown to click on checkbox. #", null )
    };

    private CImportClickData[] acliCheck =
    {
      new CImportClickData( true, 0, 0, 2, 4, "Unknown import table state after first click, number of rows: ", null ),
      new CImportClickData( true, 1, 0, 2, 5, "Unknown import table state after second click, number of rows: ", null ),
      new CImportClickData( true, 2, 0, 2, 7, "Unknown import table state after third click, number of rows: ", null ),
      new CImportClickData( true, 5, 0, 2, 8, "Unknown import table state after forth click, number of rows: ", null ),
      new CImportClickData( true, 6, 0, 2, 9, "Unknown import table state after third click, number of rows: ", null ),
      new CImportClickData( true, 3, 1, 1, 9, "Unknown to click on checkbox. #", "Selected document is already referenced." ),
      new CImportClickData( true, 4, 1, 1, 9, "Unknown to click on checkbox. #", "Document cannot reference itself." ),
      new CImportClickData( true, 7, 1, 1, 9, "Unknown to click on checkbox. #", "Selected document is already referenced." )
    };

    public void ImportReferencedSchema( )
    {
      startTest( );

      ImportReferencedSchemaInternal(
          SAMPLE_NAME,
          PURCHASE_SCHEMA_FILE_PATH,
          PURCHASE_SCHEMA_FILE_NAME,
          MODULE_NAME,
          false,
          acliImport
        );

      endTest( );
    }

    public void ImportReferencedSchema2( )
    {
      startTest( );

      ImportReferencedSchema2Internal( acliCheck );
      
      endTest( );
    }

    public void DeleteReferencedSchema( )
    {
      startTest( );

      DeleteReferencedSchemaInternal( MODULE_NAME );

      ImportReferencedSchemaInternal(
          SAMPLE_NAME,
          PURCHASE_SCHEMA_FILE_PATH,
          PURCHASE_SCHEMA_FILE_NAME,
          MODULE_NAME,
          true,
          acliImport
        );

      endTest( );
    }

    public void FindUsages( )
    {
      startTest( );

      FindUsagesInternal(
          MODULE_NAME,
          SAMPLE_SCHEMA_PATH,
          LOAN_SCHEMA_FILE_NAME_ORIGINAL,
          5
        );

      endTest( );
    }

    public void FindUsages2( )
    {
      startTest( );

      FindUsagesInternal(
          MODULE_NAME,
          SAMPLE_SCHEMA_PATH,
          LOAN_SCHEMA_FILE_NAME_RENAMED,
          5
        );

      endTest( );
    }

    public void ValidateAndBuild( )
    {
      startTest( );
      
      ValidateAndBuildInternal( SAMPLE_NAME );

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

      AddItInternal(
          PURCHASE_SCHEMA_FILE_NAME,
          "Attributes",
          "Add Attribute",
          null, 
          "Referenced Schemas|import|Simple Types|StateType",
          ATTRIBUTES_NAMES[ 0 ]
        );

      endTest( );
    }

    public void ExploreAttribute( )
    {
      startTest( );

      ExploreSimpleInternal(
          ATTRIBUTES_NAMES[ 0 ],
          "StateType",
          "attribute",
          "ns2:"
        );

      endTest( );
    }

    public void ManipulateAttribute( )
    {
      startTest( );

      ManipulateAttributeInternal( SAMPLE_NAME );

      endTest( );
    }

    public void AddComplex( )
    {
      startTest( );

      AddItInternal(
          PURCHASE_SCHEMA_FILE_NAME,
          "Complex Types",
          "Add Complex Type",
          "Use Existing Definition", 
          "Referenced Schemas|import|Complex Types|CarType",
          COMPLEX_NAMES[ 0 ]
        );

      endTest( );
    }

    public void ExploreComplex( )
    {
      startTest( );

      ExploreComplexInternal(
          "Complex Types",
          COMPLEX_NAMES[ 0 ],
          "CarType",
          "<xs:complexType name=\"" + COMPLEX_NAMES[ 0 ] + "\">"
        );

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

      AddItInternal(
          PURCHASE_SCHEMA_FILE_NAME,
          "Elements",
          "Add Element",
          "Use Existing Type",
          "Referenced Schemas|import|Complex Types|AddressType",
          ELEMENT_NAMES[ 0 ]
        );

      endTest( );
    }

    public void ExploreElement( )
    {
      startTest( );

      ExploreComplexInternal(
          "Elements",
          ELEMENT_NAMES[ 0 ],
          "AddressType",
          "<xs:element name=\"" + ELEMENT_NAMES[ 0 ] + "\" type=\"ns2:AddressType\"></xs:element>"
        );

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

      AddItInternal(
          PURCHASE_SCHEMA_FILE_NAME,
          "Simple Types",
          "Add Simple Type",
          null, 
          "Referenced Schemas|import|Simple Types|LoanType",
          SIMPLE_NAMES[ 0 ]
        );

      endTest( );
    }

    public void ExploreSimple( )
    {
      startTest( );

      ExploreSimpleInternal(
          SIMPLE_NAMES[ 0 ],
          "LoanType",
          "simpleType",
          null
        );

      endTest( );
    }

    public void ManipulateSimple( )
    {
      startTest( );

      ManipulateSimpleInternal( SAMPLE_NAME );

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

    public void MoveSchema( )
    {
      startTest( );

      // Ensure correct file usages exists
      FindUsagesInternal(
          SAMPLE_NAME,
          "Process Files",
          "purchaseOrder.xsd",
          12
        );

      // Select file
      //SAMPLE_NAME | Process Files | purchaseOrder.xsd
      ProjectsTabOperator pto = ProjectsTabOperator.invoke( );
      JTreeOperator tree = pto.tree();

      Node nodeDestination = new Node(
          tree,
          MODULE_NAME + "|" + SAMPLE_SCHEMA_PATH
        );
      nodeDestination.select( );
      TreePath path = tree.getSelectionPath( );
      Point poDestination = tree.getPointToClick( path );

      Node nodeSource = new Node(
          tree,
          SAMPLE_NAME + "|" + PURCHASE_SCHEMA_FILE_PATH + "|" + PURCHASE_SCHEMA_FILE_NAME
        );
      nodeSource.select( );
      path = tree.getSelectionPath( );
      Point poSource= tree.getPointToClick( path );

      System.out.println( "*** " + poSource.x + " " + poSource.y + " / " + poDestination.x + " " + poDestination.y + " ***" );
      
      // Get coordinates of source and destination
      Point po = tree.getPointToClick( path );

      // Press mouse down in source
      pto.dragNDrop(
          poSource.x,
          poSource.y,
          poDestination.x,
          poDestination.y
        );
      /*
      tree.moveMouse( poSource.x, poSource.y );
      tree.pressMouse( poSource.x, poSource.y );

      // Drag to destination
      tree.dragMouse( poDestination.x, poDestination.y );

      // Release mouse in destination
      tree.releaseMouse( poDestination.x, poDestination.y );
      */

      // Refactoring
      JDialogOperator jdMove = new JDialogOperator( "Move File" );

      JComboBoxOperator jcProject = new JComboBoxOperator( jdMove, 0 );
      jcProject.selectItem( MODULE_NAME );
      JButtonOperator jbRefactor = new JButtonOperator( jdMove, "Refactor" );
      jbRefactor.pushNoBlock( );
      WaitDialogClosed( jdMove );

      // Check new tree path to file
      Node nodeCheck = new Node(
          tree,
          MODULE_NAME + "|" + SAMPLE_SCHEMA_PATH + "|" + PURCHASE_SCHEMA_FILE_NAME
        );
      nodeCheck.select( );

      endTest( );
    }

    public void FixInMoved( )
    {
      startTest( );

      // REMOVE FROM SPEC???
      // ALWAYS FIXED AUTOMATICALLY???

      // Open moved file
      ProjectsTabOperator pto = new ProjectsTabOperator( );
      JTreeOperator tree = pto.tree();
      Node nodeCheck = new Node(
          tree,
          MODULE_NAME + "|" + SAMPLE_SCHEMA_PATH + "|" + PURCHASE_SCHEMA_FILE_NAME
        );
      nodeCheck.select( );
      nodeCheck.performPopupAction( "Open" );

      // Select referenced schemas
      SchemaMultiView opMultiView = new SchemaMultiView( PURCHASE_SCHEMA_FILE_NAME );
      opMultiView.switchToSchema( );
      opMultiView.switchToSchemaColumns( );
      JListOperator opList = opMultiView.getColumnListOperator( 0 );
      opList.selectItem( "Referenced Schemas" );

      // Select reference
      opList = opMultiView.getColumnListOperator( 1 );
      opList.selectItem( "import" );

      // Check broken reference
      opList = opMultiView.getColumnListOperator( 2 );
      ListModel lmd = opList.getModel( );
      for( int i = 0; i < lmd.getSize( ); i++ )
        System.out.println( "****" + lmd.getElementAt( i ) );

      // Fix broken if any
        // ToDo

      endTest( );
    }

    public void FixInReferenced( )
    {
      startTest( );

      // REMOVE FROM SPEC???
      // ALWAYS FIXED AUTOMATICALLY???

      // Open wsdl file
      ProjectsTabOperator pto = new ProjectsTabOperator( );
      JTreeOperator tree = pto.tree();
      Node nodeCheck = new Node(
          tree,
          SAMPLE_NAME + "|" + PURCHASE_SCHEMA_FILE_PATH + "|POService.wsdl"
        );
      nodeCheck.select( );
      nodeCheck.performPopupAction( "Open" );

      // Get tree
      TopComponentOperator top = new TopComponentOperator( "POService.wsdl" );
      JTreeOperator jt = new JTreeOperator( top, 0 );
      Node n = new Node( jt, "Types|http://manufacturing.org/wsdl/purchase/bp1|Referenced Schemas|import" );
      n.select( );

      endTest( );
    }

    public void BuildCompositeApplication( )
    {
      startTest( );
      
      BuildInternal(
          COMPOSITE_APPLICATION_NAME,
          false,
          "jbi-build"
        );

      endTest( );
    }

    public void DeployCompositeApplication( )
    {
      startTest( );

      DeployCompositeApplicationInternal( COMPOSITE_APPLICATION_NAME );
      
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
