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
// import org.netbeans.api.project.ProjectInformation;
import javax.swing.ListModel;
import org.netbeans.test.xml.schema.lib.SchemaMultiView;
import java.awt.Rectangle;
import javax.swing.text.BadLocationException;

/**
 *
 * @author michaelnazarov@netbeans.org
 */

public class AcceptanceTestCaseBPEL2XSLT extends AcceptanceTestCaseXMLCPR {
    
    static final String [] m_aTestMethods = {
        "CreateBluePrint1Sample",
        "AddProjectReference",
        "DeleteProjectReference",
        "AddSampleSchema",

        "ImportReferencedSchema",
        "ImportReferencedSchema2",
        "DeleteReferencedSchema",
        "FindUsages", // TODO : How to find find usages output?
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
        "DeleteSimple",
        "UndoRedoSimple",
        "RenameSampleSchema",
        "UndoRenameSampleSchema",
        "RedoRenameSampleSchema",
        "FindUsages2",
        "ValidateAndBuild",

        // Move, fix

        "ValidateAndBuild",
        "BuildCompositeApplication",
        "DeployCompositeApplication", // This will failed, followed skipped
        "CreateNewTest",
        "RunNewTest",
    };

    static final String SAMPLE_CATEGORY_NAME = "Samples|SOA|BPEL BluePrints";
    static final String SAMPLE_PROJECT_NAME = "BluePrint 1";
    static final String SAMPLE_NAME = "SampleApplication2Xslt";
    static final String COMPOSITE_APPLICATION_NAME = SAMPLE_NAME + "Application";

    static final String MODULE_NAME = "NotifyManager";

    static final String SAMPLE_SCHEMA_PATH = "Transformation Files";

    public AcceptanceTestCaseBPEL2XSLT(String arg0) {
        super(arg0);
    }
    
    public static TestSuite suite() {
        TestSuite testSuite = new TestSuite(AcceptanceTestCaseBPEL2XSLT.class.getName());
        
        for (String strMethodName : m_aTestMethods) {
            testSuite.addTest(new AcceptanceTestCaseBPEL2XSLT(strMethodName));
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
      if( null == ( new Node( prn, SAMPLE_SCHEMA_PATH + "|newLoanApplication.xsd" ) ) )
      {
        fail( "Unable to check created sample schema." );
      }

      endTest( );
    }

    private CImportClickData[] acliImport =
    {
      new CImportClickData( true, 0, 0, 2, 4, "Unknown import table state after first click, number of rows: ", null ),
      new CImportClickData( true, 1, 0, 2, 5, "Unknown import table state after second click, number of rows: ", null ),
      new CImportClickData( true, 2, 0, 2, 7, "Unknown import table state after third click, number of rows: ", null ),
      new CImportClickData( true, 5, 0, 2, 8, "Unknown import table state after forth click, number of rows: ", null ),
      new CImportClickData( true, 6, 0, 2, 12, "Unknown import table state after third click, number of rows: ", null ),
      new CImportClickData( false, 3, 1, 1, 12, "Unknown to click on checkbox. #", null ),
      new CImportClickData( true, 9, 1, 1, 12, "Unknown to click on checkbox. #", null )
    };

    private CImportClickData[] acliCheck =
    {
      new CImportClickData( true, 0, 0, 2, 4, "Unknown import table state after first click, number of rows: ", null ),
      new CImportClickData( true, 1, 0, 2, 5, "Unknown import table state after second click, number of rows: ", null ),
      new CImportClickData( true, 2, 0, 2, 7, "Unknown import table state after third click, number of rows: ", null ),
      new CImportClickData( true, 5, 0, 2, 8, "Unknown import table state after forth click, number of rows: ", null ),
      new CImportClickData( true, 6, 0, 2, 12, "Unknown import table state after third click, number of rows: ", null ),
      new CImportClickData( true, 3, 1, 1, 12, "Unknown to click on checkbox. #", "Selected document is already referenced." ),
      new CImportClickData( true, 4, 1, 1, 12, "Unknown to click on checkbox. #", "Document cannot reference itself." ),
      new CImportClickData( true, 9, 1, 1, 12, "Unknown to click on checkbox. #", "Selected document is already referenced." )
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
