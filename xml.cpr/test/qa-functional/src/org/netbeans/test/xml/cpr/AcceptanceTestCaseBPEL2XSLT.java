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
import org.netbeans.api.project.ProjectInformation;
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
    static final String SAMPLE_NAME = "SampleApplication2Xslt";

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

    public void ImportReferencedSchema( )
    {
      startTest( );

      ProjectsTabOperator pto = new ProjectsTabOperator( );

      ProjectRootNode prn = pto.getProjectRootNode(
          SAMPLE_NAME + "|Process Files|purchaseOrder.xsd"
        );
      prn.select( );

      JTreeOperator tree = pto.tree( );
      tree.clickOnPath(
          tree.findPath( SAMPLE_NAME + "|Process Files|purchaseOrder.xsd" ),
          2
        );

      // Check was it opened or no
      if( null == new EditorOperator( "purchaseOrder.xsd" ) )
      {
        fail( "purchaseOrder.xsd was not opened after double click." );
      }

      endTest( );
    }

    public void AddAttribute( )
    {
      startTest( );

      AddItInternal(
          "Attributes",
          "Add Attribute",
          null, 
          "Referenced Schemas|import|Simple Types|StateType",
          "newAttribute"
        );

      endTest( );
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
      ClickForTextPopup( eoXsdCode );
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

      AddItInternal(
          "Complex Types",
          "Add Complex Type",
          "Use Existing Definition", 
          "Referenced Schemas|import|Complex Types|CarType",
          "newComplexType"
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
          "Elements",
          "Add Element",
          "Use Existing Type", 
          "Referenced Schemas|import|Complex Types|AddressType",
          "newElement"
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
          "Simple Types",
          "Add Simple Type",
          null, 
          "Referenced Schemas|import|Simple Types|LoanType",
          "newSimpleType"
        );

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

}
