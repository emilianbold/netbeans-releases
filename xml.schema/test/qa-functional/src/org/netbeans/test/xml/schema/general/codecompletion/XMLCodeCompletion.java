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

package org.netbeans.test.xml.schema.general.codecompletion;

import org.netbeans.test.xml.schema.general.GeneralXMLTest;

import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jellytools.MainWindowOperator;
import java.awt.event.KeyEvent;
//import java.awt.Robot;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.*;
import org.netbeans.jellytools.modules.editor.CompletionJListOperator;
import java.util.List;
import javax.swing.ListModel;

/**
 *
 * @author michaelnazarov@netbeans.org
 */

public class XMLCodeCompletion extends GeneralXMLTest {
    
    static final String JAVA_CATEGORY_NAME = "Java";
    static final String JAVA_PROJECT_NAME = "Java Application";

    protected CompletionJListOperator GetCompletion( )
    {
      CompletionJListOperator comp = null;
      int iRedo = 5;
      while( true )
      {
        try
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
        }
        catch( JemmyException ex )
        {
          System.out.println( "Wait completion timeout." );
          if( 0 == --iRedo )
            return null;
        }
        Sleep( 100 );//try{ Thread.sleep( 100 ); } catch( InterruptedException ex ) {}
      }
    }

    public XMLCodeCompletion(String arg0) {
        super(arg0);
    }

    public void CreateJavaPackageInternal( String sProject )
    {
      ProjectsTabOperator pto = new ProjectsTabOperator( );
      ProjectRootNode prn = pto.getProjectRootNode( sProject );
      prn.select( );

      // Workaround for MacOS platform
      // TODO : check platform
      // TODO : remove after normal issue fix
      NewFileWizardOperator.invoke().cancel( );

      NewFileWizardOperator opNewFileWizard = NewFileWizardOperator.invoke( );

      // There is no comparator support in this class,
      // but we need it because "Java Server Faces" is before "Java"
      // and there is no way to find "Java" using standard method,
      // so find by ourself.
      // +++ start of hack
      JDialogOperator jd = new JDialogOperator( "New File" );
      Sleep( 1500 );
      JTreeOperator jt = new JTreeOperator( jd, 0 );
      jt.clickOnPath( jt.findPath( "Java", new CFulltextStringComparator( ) ) );
      //opNewFileWizard.selectCategory( "Java" );
      // --- end of hack

      opNewFileWizard.selectFileType( "Java Package" );
      opNewFileWizard.next( );
      opNewFileWizard.finish( );

      // Check created schema in project tree
      if( null == ( new Node( prn, "Source Packages|newpackage" ) ) )
      {
        fail( "Unable to check created package." );
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

      // Workaround for MacOS platform
      // TODO : check platform
      // TODO : remove after normal issue fix
      NewFileWizardOperator.invoke().cancel( );

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
        Sleep( 1000 );
        ExpandByClicks( jto, cli.row, cli.col, cli.count, cli.result, cli.error, cli.timeout );
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
}
