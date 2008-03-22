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

/**
 *
 * @author michaelnazarov@netbeans.org
 */

public class AcceptanceTestCaseXMLCPR extends JellyTestCase {
    
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

    public void RenameSampleSchemaInternal( String sModule )
    {
      // Select schema
      ProjectsTabOperator pto = new ProjectsTabOperator( );

      ProjectRootNode prn = pto.getProjectRootNode(
          sModule + "|Process Files|newLoanApplication.xsd"
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

      // Wait status
      //MainWindowOperator.StatusTextTracer stt = MainWindowOperator.getDefault( ).getStatusTextTracer( );
      //stt.start( );
      //stt.waitText( "Refactored nax" );
      //stt.stop( );

      // Check result
      pto = new ProjectsTabOperator( );

      prn = pto.getProjectRootNode(
          sModule + "|Process Files|myLoanApplication.xsd"
        );
      if( null == prn )
      {
        fail( "Unable to rename sample schema!" );
      }
    }

    public void UndoRenameSampleSchemaInternal( String sModule )
    {
      // Undo
      new JMenuBarOperator(MainWindowOperator.getDefault()).pushMenu("Refactor|Undo [File Rename]");

      // Check result
      ProjectsTabOperator pto = new ProjectsTabOperator( );

      ProjectRootNode prn = pto.getProjectRootNode(
          sModule + "|Process Files|newLoanApplication.xsd"
        );
      if( null == prn )
      {
        fail( "Unable to undo rename sample schema!" );
      }
    }

    public void RedoRenameSampleSchemaInternal( String sModule )
    {
      // Redo
      new JMenuBarOperator(MainWindowOperator.getDefault()).pushMenu("Refactor|Redo [File Rename]");

      ProjectsTabOperator pto = new ProjectsTabOperator( );

      ProjectRootNode prn = pto.getProjectRootNode(
          sModule + "|Process Files|myLoanApplication.xsd"
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
