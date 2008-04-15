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
import org.netbeans.api.project.ProjectInformation;
import javax.swing.text.BadLocationException;
import java.awt.Rectangle;
import javax.swing.JEditorPane;
import org.netbeans.test.xml.schema.lib.SchemaMultiView;

/**
 *
 * @author michaelnazarov@netbeans.org
 */

public class AcceptanceTestCaseXMLCPR extends JellyTestCase {

    protected final String PURCHASE_SCHEMA_FILE_NAME = "purchaseOrder.xsd";

    protected final String LOAN_SCHEMA_FILE_NAME_ORIGINAL = "newLoanApplication.xsd";
    protected final String LOAN_SCHEMA_FILE_NAME_RENAMED = "myLoanApplication.xsd";
    
    class CFulltextStringComparator implements Operator.StringComparator
    {
      public boolean equals( java.lang.String caption, java.lang.String match )
      {
        return caption.equals( match );
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

      // Right click on Reference Schemas
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
      if( -1 == sSelectedText.indexOf( "<xs:" + sIncode + " name=\"" + sName + "\" type=\"" + sNamespace + sType+ "\"/>" ) )
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

    public void tearDown() {
        new SaveAllAction().performAPI();
    }

    protected void startTest(){
        super.startTest();
        //Helpers.closeUMLWarningIfOpened();
    }

}
