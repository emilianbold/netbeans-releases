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

package org.netbeans.test.xml.schema.codecompletion;

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
import org.netbeans.jellytools.modules.editor.CompletionJListOperator;
import java.util.List;
import javax.swing.SwingUtilities;
import junit.framework.AssertionFailedError;
import org.netbeans.api.java.source.ui.ScanDialog;

/**
 *
 * @author michaelnazarov@netbeans.org
 */

public class XMLCodeCompletion extends JellyTestCase {
    
    static final String JAVA_CATEGORY_NAME = "Java";
    static final String JAVA_PROJECT_NAME = "Java Application";

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

    protected CompletionJListOperator GetCompletion( )
    {
      CompletionJListOperator comp = null;
      while( true )
      {
        comp = new CompletionJListOperator( );
        try
        {
          Object o = comp.getCompletionItems( ).get( 0 );
          if( !o.toString( ).contains( "No suggestions" ) )
            return comp;
        }
        catch( java.lang.Exception ex )
        {
          return null;
        }
        try{ Thread.sleep( 100 ); } catch( InterruptedException ex ) {}
      }
    }

    protected void ExpandByClicks(
        JTableOperator table,
        int row,
        int col,
        int count,
        int result,
        String error
      )
    {
      // Normal version
      // just click
      table.clickOnCell( row, col, count );
      table.pushKey( KeyEvent.VK_RIGHT );

      // HaCk version
      /*
      Point pt = table.getPointToClick( row, col );
      //table.enterMouse( );
      //try { Thread.sleep( 50 ); } catch( InterruptedException ex ) { }
      table.pressMouse( pt.x, pt.y );
      try { Thread.sleep( 50 ); } catch( InterruptedException ex ) { }
      table.releaseMouse( pt.x, pt.y );
      try { Thread.sleep( 50 ); } catch( InterruptedException ex ) { }
      table.pressMouse( pt.x, pt.y );
      try { Thread.sleep( 50 ); } catch( InterruptedException ex ) { }
      table.releaseMouse( pt.x, pt.y );
      */

      try { Thread.sleep( 750 ); } catch( InterruptedException ex ) { }
      int iRows = table.getRowCount( );
      if( result != iRows )
        fail( error + iRows );

      return;
    }

    public XMLCodeCompletion(String arg0) {
        super(arg0);
    }

    public void CreateJavaApplicationInternal(
        String sName
      )
    {
        // Create Java application
        NewProjectWizardOperator opNewProjectWizard = NewProjectWizardOperator.invoke( );
        opNewProjectWizard.selectCategory( JAVA_CATEGORY_NAME );
        opNewProjectWizard.selectProject( JAVA_PROJECT_NAME );
        opNewProjectWizard.next( );

        NewProjectNameLocationStepOperator opNewProjectNameLocationStep = new NewProjectNameLocationStepOperator( );
        opNewProjectNameLocationStep.txtProjectLocation( ).setText( System.getProperty( "xtest.workdir" ) );
        opNewProjectNameLocationStep.txtProjectName( ).setText( sName );
        opNewProjectWizard.finish( );

        waitScanFinished( );
    }

    public void CreateJavaPackageInternal( String sProject )
    {
      ProjectsTabOperator pto = new ProjectsTabOperator( );
      ProjectRootNode prn = pto.getProjectRootNode( sProject );
      prn.select( );

      NewFileWizardOperator opNewFileWizard = NewFileWizardOperator.invoke( );
      opNewFileWizard.selectCategory( "Java" );
      opNewFileWizard.selectFileType( "Java Package" );
      opNewFileWizard.next( );
      opNewFileWizard.finish( );

      // Check created schema in project tree
      if( null == ( new Node( prn, "Source Packages|newpackage" ) ) )
      {
        fail( "Unable to check created package." );
      }
    }

    public void CreateSchemaInternal( String sProject )
    {
      ProjectsTabOperator pto = new ProjectsTabOperator( );
      ProjectRootNode prn = pto.getProjectRootNode( sProject );
      prn.select( );

      NewFileWizardOperator opNewFileWizard = NewFileWizardOperator.invoke( );
      opNewFileWizard.selectCategory( "XML" );
      opNewFileWizard.selectFileType( "XML Schema" );
      opNewFileWizard.next( );
      opNewFileWizard.finish( );

      // Check created schema in project tree
      if( null == ( new Node( prn, "Source Packages|<default package>|newXmlSchema.xsd" ) ) )
      {
        fail( "Unable to check created schema." );
      }
    }

    protected void AddSampleSchemaInternal(
        String sProject,
        String sPackage
      )
    {
      ProjectsTabOperator pto = new ProjectsTabOperator( );
      ProjectRootNode prn = pto.getProjectRootNode( sProject );
      prn.select( );

      NewFileWizardOperator opNewFileWizard = NewFileWizardOperator.invoke( );
      opNewFileWizard.selectCategory( "XML" );
      opNewFileWizard.selectFileType( "Purchase Order Sample Schema" );
      opNewFileWizard.next( );

      if( null != sPackage )
      {
        JDialogOperator jdNew = new JDialogOperator( "New Purchase Order Sample Schema" );
        JButtonOperator jbBrowse = new JButtonOperator( jdNew, "Browse..." );
        jbBrowse.pushNoBlock( );
        JDialogOperator jdBrowse = new JDialogOperator( "Browse Folders" );
        JTreeOperator jtBrowse = new JTreeOperator( jdBrowse, 0 );
        jtBrowse.selectPath( jtBrowse.findPath( sProject + "|src|" + sPackage ) );
        JButtonOperator jbSelect = new JButtonOperator( jdBrowse, "Select Folder" );
        jbSelect.push( );
        jdBrowse.waitClosed( );
      }

      opNewFileWizard.finish( );

      // Check created schema in project tree
      String sPath = "Source Packages|"
          + ( ( null == sPackage ) ? "<default package>" : sPackage )
          + "|newPurchaseOrder.xsd";
      if( null == ( new Node( prn, sPath ) ) )
      {
        fail( "Unable to check created sample schema." );
      }
    }

    public void CreateConstrainedInternal(
        String sApplication,
        CImportClickData[] aimpData,
        String sPackage,
        String sRoot,
        int iPrimary
      )
    {
      if( null == sPackage )
        sPackage = "<default package>";

      ProjectsTabOperator pto = new ProjectsTabOperator( );
      ProjectRootNode prn = pto.getProjectRootNode( sApplication + "|Source Packages|" + sPackage );
      prn.select( );

      NewFileWizardOperator opNewFileWizard = NewFileWizardOperator.invoke( );

      // PAGE ===========================================================
      opNewFileWizard.selectCategory( "XML" );
      opNewFileWizard.selectFileType( "XML Document" );
      opNewFileWizard.next( );

      // PAGE ===========================================================
      opNewFileWizard.next( );

      // PAGE ===========================================================
      JDialogOperator jnew = new JDialogOperator( "New File" );
      JRadioButtonOperator jbut = new JRadioButtonOperator( jnew, "XML Schema-Constrained Document" );
      jbut.setSelected( true );
      jbut.clickMouse( );
      opNewFileWizard.next( );

      // PAGE ===========================================================
      jnew = new JDialogOperator( "New File" );
      JButtonOperator jBrowse = new JButtonOperator( jnew, "Browse" );
      jBrowse.pushNoBlock( );

      JDialogOperator jBrowser = new JDialogOperator( "Schema Browser" );
      JTableOperator jto = new JTableOperator( jBrowser, 0 );

      for( CImportClickData cli : aimpData )
      {
        try { Thread.sleep( 1000 ); } catch( InterruptedException ex ) { }
        ExpandByClicks( jto, cli.row, cli.col, cli.count, cli.result, cli.error );
      }

      JButtonOperator jOk = new JButtonOperator( jBrowser, "OK" );
      jOk.push( );
      jBrowser.waitClosed( );

      jnew = new JDialogOperator( "New File" );
      JTableOperator jtable = new JTableOperator( jnew, 0 );
      jtable.clickOnCell( iPrimary, 0, 1 );

      if( null != sRoot )
      {
        jtable.clickOnCell( 0, 2, 1 );
        JComboBoxOperator jcom = new JComboBoxOperator( jnew, 0 );
        jcom.selectItem( sRoot );
      }

      opNewFileWizard.next( );

      // PAGE ===========================================================
      opNewFileWizard.finish( );

      // Check created schema in project tree
      prn = pto.getProjectRootNode( sApplication );
      if( null == ( new Node( prn, "Source Packages|" + sPackage + "|newXMLDocument.xml" ) ) )
      {
        fail( "Unable to check created sample schema." );
      }

      // Check there is newly created schema opened in editor
      EditorOperator xmlCode = new EditorOperator( "newXMLDocument.xml" );
    }

    public void StartTagInternal(
        String sFileName,
        String sAnchor,
        boolean bBefore,
        String[] asCases
      )
    {
      EditorOperator eoXMLCode = new EditorOperator( sFileName );
      String sToFind;
      if( bBefore )
        sToFind = "\n" + sAnchor;
      else
        sToFind = sAnchor + "\n";
      eoXMLCode.setCaretPosition(
          sToFind,
          bBefore
        );

      eoXMLCode.typeKey( '<' );
      CompletionJListOperator jCompl = GetCompletion( );

      String[] asCCode = { "billTo", "comment", "items", "shipTo" };
      if( null != asCases )
        asCCode = asCases;
      for( String sCode : asCCode )
      {
        int iIndex = jCompl.findItemIndex( sCode );
        if( -1 == iIndex )
        {
          try
          {
          List list = jCompl.getCompletionItems();
          for( int i = 0; i < list.size( ); i++ )
            System.out.println( "******" + list.get( i ) );
          }
          catch( java.lang.Exception ex )
          {
            System.out.println( "#" + ex.getMessage( ) );
          }
          fail( "Unable to find " + sCode + " completion." );
        }
      }
      jCompl.hideAll( );

      eoXMLCode.close( false );
    }

    protected void CheckCompletionItems(
        CompletionJListOperator jlist,
        String[] asIdeal
      )
    {
      for( String sCode : asIdeal )
      {
        int iIndex = jlist.findItemIndex( sCode );
        if( -1 == iIndex )
        {
          try
          {
          List list = jlist.getCompletionItems();
          for( int i = 0; i < list.size( ); i++ )
            System.out.println( "******" + list.get( i ) );
          }
          catch( java.lang.Exception ex )
          {
            System.out.println( "#" + ex.getMessage( ) );
          }
          fail( "Unable to find " + sCode + " completion." );
        }
      }
    }

    public void StartAndContinueTagInternal(
        String sFileName,
        String sAnchor,
        boolean bBefore,
        String[] asCases,
        String sSelectItem,
        String[] asCasesClose
      )
    {
      EditorOperator eoXMLCode = new EditorOperator( sFileName );
      String sToFind;
      if( bBefore )
        sToFind = "\n" + sAnchor;
      else
        sToFind = sAnchor + "\n";
      eoXMLCode.setCaretPosition(
          sToFind,
          bBefore
        );

      eoXMLCode.typeKey( '<' );
      CompletionJListOperator jCompl = GetCompletion( );

      String[] asCCode = { "billTo", "comment", "items", "shipTo" };
      if( null != asCases )
        asCCode = asCases;
      CheckCompletionItems( jCompl, asCCode );

      // Select element
      JEditorPaneOperator editor = eoXMLCode.txtEditorPane( );
      eoXMLCode.insert( sSelectItem );
      editor.typeKey( '>' );
      //jCompl.clickOnItem( sSelectItem );
      // Close with '>'
      // Check close completion
      jCompl = GetCompletion( );
      CheckCompletionItems( jCompl, asCasesClose );

      jCompl.hideAll( );

      eoXMLCode.close( false );
    }

    public void AddElementInternal( String sSchema )
    {
      new JMenuBarOperator(MainWindowOperator.getDefault()).pushMenu("View|Editors|Design");
      new JMenuBarOperator(MainWindowOperator.getDefault()).pushMenu("View|Editors|Design");

      TopComponentOperator op = new TopComponentOperator( sSchema );
      op.clickForPopup( );
      JPopupMenuOperator menu = new JPopupMenuOperator( );
      menu.pushMenu( "Add|Element" );
      op.clickMouse( );
      try { Thread.sleep( 500 ); } catch( InterruptedException ex ) { }
      op.pushKey( KeyEvent.VK_RIGHT );
      try { Thread.sleep( 500 ); } catch( InterruptedException ex ) { }
      op.pushKey( KeyEvent.VK_RIGHT );
      try { Thread.sleep( 500 ); } catch( InterruptedException ex ) { }
      op.typeKey( 'E' );
      try { Thread.sleep( 500 ); } catch( InterruptedException ex ) { }
      op.clickMouse( );

      new JMenuBarOperator(MainWindowOperator.getDefault()).pushMenu("View|Editors|Source");

      EditorOperator opxml = new EditorOperator( sSchema );
      opxml.setCaretPosition( "<xsd:element name=\"newElement\"/>", true );
      opxml.insert( "<xsd:any/>" );

      opxml.close( true );
    }
    private static void waitScanFinished() {
        try {
            class Wait implements Runnable {

                boolean initialized;
                boolean ok;

                public void run() {
                    if (initialized) {
                        ok = true;
                        return;
                    }
                    initialized = true;
                    boolean canceled = ScanDialog.runWhenScanFinished(this, "tests");
                    assertFalse("Dialog really finished", canceled);
                    assertTrue("Runnable run", ok);
                }
            }
            Wait wait = new Wait();
            if (SwingUtilities.isEventDispatchThread()) {
                wait.run();
            } else {
                SwingUtilities.invokeAndWait(wait);
            }
        } catch (Exception ex) {
            throw (AssertionFailedError)new AssertionFailedError().initCause(ex);
        }
    }
}
