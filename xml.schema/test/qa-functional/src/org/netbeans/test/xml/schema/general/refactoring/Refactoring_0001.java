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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.test.xml.schema.general.refactoring;

import org.netbeans.jemmy.JemmyException;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeModel;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.jemmy.operators.JTextComponentOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.test.xml.schema.lib.SchemaMultiView;
import org.netbeans.junit.NbModuleSuite;
import junit.framework.Test;

/**
 *
 * @author michaelnazarov@netbeans.org
 */
public class Refactoring_0001 extends Refactoring
{

    static final String TEST_JAVA_APP_NAME = "java4refactoring_0001";
    static final String SAMPLE_SCHEMA_NAME = "newPurchaseOrder.xsd";
    static final String SCHEMA_NAME_1 = "newSchema1";

    static final String ITEM_ORIGINAL_NAME = "Items";
    static final String ITEM_CHANGED_NAME = "renamed_one";

    public Refactoring_0001( String arg0 )
    {
        super(arg0);
    }

    public static Test suite( )
    {
      return NbModuleSuite.create(
          NbModuleSuite.createConfiguration( Refactoring_0001.class ).addTest(
            "CreateJavaApplication",
            "CreateSchemas",
            "RenameContextMenuNoPreview",
            "UndoRename",
            "RenameContextMenuPreview",
            "SafeDeleteNoPreview",
            "UndoDelete",
            "SafeDeletePreview",
            "RenameMenuNoPreview",
            "UndoRename1",
            "RenameMenuPreview",
            "ManipulateAttributes",
            "ManipulateComponents"
           )
           .enableModules( ".*" )
           .clusters( ".*" )
           //.gui( true )
        );
    }

    // TODO : add more checking through all views

    public void CreateJavaApplication( )
    {
      startTest( );

      CreateJavaApplicationInternal( TEST_JAVA_APP_NAME );

      endTest( );
    }

    public void CreateSchemas( )
    {
      startTest( );

      // Create sample schema
      AddPurchaseOrderSchemaInternal( TEST_JAVA_APP_NAME, TEST_JAVA_APP_NAME );

      // Create new schema
      CreateSchemaInternal(
          TEST_JAVA_APP_NAME + "|Source Packages|" + TEST_JAVA_APP_NAME,
          SCHEMA_NAME_1
        );

      endTest( );
    }

    public void RenameContextMenuNoPreview( )
    {
      startTest( );

      SchemaMultiView xml = new SchemaMultiView( SAMPLE_SCHEMA_NAME );
      SelectInFirstColumn( xml, "Complex Types" );
      CallPopupOnListItemNoBlock(
          xml,
          1,
          ITEM_ORIGINAL_NAME,
          "Refactor|Rename..."
        );

      // Set new name
      JDialogOperator jdRename = new JDialogOperator( "Rename" );
      JTextComponentOperator txt = new JTextComponentOperator( jdRename, 0 );
      txt.setText( ITEM_CHANGED_NAME );
      JButtonOperator jbRename = new JButtonOperator( jdRename, "Refactor" );
      jbRename.push( );
      jdRename.waitClosed( );

      SelectItemInColumn( xml, 1, ITEM_CHANGED_NAME );

      endTest( );
    }

    public void UndoRename( )
    {
      startTest( );

      SchemaMultiView xml = new SchemaMultiView( SAMPLE_SCHEMA_NAME );
      SelectItemInColumn( xml, 1, ITEM_CHANGED_NAME );
      CallPopupOnListItem(
          xml,
          1,
          ITEM_CHANGED_NAME,
          new CStartsStringComparator( ),
          "Refactor|Undo [Rename]"
        );

      // Wait for long process...
      try
      {
        JDialogOperator jdInProgress = new JDialogOperator( "Refactoring..." );
        jdInProgress.waitClosed( );
      }
      catch( JemmyException ex )
      {
      }

      Sleep( 2000 );

      SelectItemInColumn( xml, 1, ITEM_ORIGINAL_NAME );

      endTest( );
    }

    public void RenameContextMenuPreview( )
    {
      startTest( );

      SchemaMultiView xml = new SchemaMultiView( SAMPLE_SCHEMA_NAME );
      SelectInFirstColumn( xml, "Complex Types" );
      CallPopupOnListItemNoBlock(
          xml,
          1,
          ITEM_ORIGINAL_NAME,
          "Refactor|Rename..."
        );

      // Set new name
      JDialogOperator jdRename = new JDialogOperator( "Rename" );
      JTextComponentOperator txt = new JTextComponentOperator( jdRename, 0 );
      txt.setText( ITEM_CHANGED_NAME );
      JButtonOperator jbRename = new JButtonOperator( jdRename, "Preview" );
      jbRename.push( );
      jdRename.waitClosed( );

      // Check preview
      TopComponentOperator top = new TopComponentOperator( "Refactoring" );
      JTreeOperator jTree = new JTreeOperator( top, 0 );

      Sleep( 5000 );

      TreeModel tm = jTree.getModel( );
      Object o[] = new Object[ 7 ];
      o[ 0 ] = tm.getRoot( );
      for( int i = 1; i < 7; i++ )
        o[ i ] = tm.getChild( o[ i - 1 ], 0 );
      TreePath tpp = new TreePath( o );
      jTree.selectPath( tpp );

      JButtonOperator btn = new JButtonOperator( top, "Do Refactoring" );

      MainWindowOperator.StatusTextTracer stt = MainWindowOperator.getDefault( ).getStatusTextTracer( );
      stt.start( );

      btn.push( );

      stt.waitText( "Save All finished." );
      stt.stop( );

      // Check result
      SelectItemInColumn( xml, 1, ITEM_CHANGED_NAME );

      //xml.close( );

      endTest( );
    }

    public void SafeDeleteNoPreview( )
    {
      startTest( );

      // Add new CT
      SchemaMultiView xml = new SchemaMultiView( SCHEMA_NAME_1 + SCHEMA_EXTENSION );
      SelectItemInColumn( xml, 0, "Complex Types" );
      CallPopupOnListItemNoBlock(
          xml,
          0,
          "Complex Types",
          new CStartsStringComparator( ),
          "Add Complex Type..."
        );
      JDialogOperator jdAdd = new JDialogOperator( "Add Complex Type" );
      JButtonOperator jbOk = new JButtonOperator( jdAdd, "OK" );
      jbOk.push( );
      jdAdd.waitClosed( );
      SelectItemInColumn( xml, 1, "newComplexType" );

      // Refactor -> Safe Delete
      CallPopupOnListItemNoBlock(
          xml,
          1,
          "newComplexType",
          "Refactor|Safely Delete..."
        );

      // Refactor
      JDialogOperator jdRefactor = new JDialogOperator( "Safe Delete" );
      JButtonOperator jbRefactor = new JButtonOperator( jdRefactor, "Refactor" );
      jbRefactor.push( );
      jdRefactor.waitClosed( );

      // Check removed
      JListOperator list = xml.getColumnListOperator( 1 );
      if( null != list )
      {
        int iIndex = list.findItemIndex( "newComplexType" );
        if( -1 != iIndex )
          fail( "newComplexType was not remove using Safe Delete without preview." );
      }

      endTest( );
    }

    public void UndoDelete( )
    {
      startTest( );

      new JMenuBarOperator(MainWindowOperator.getDefault()).pushMenuNoBlock("Refactor|Undo [Delete newComplexType]");
      SchemaMultiView xml = new SchemaMultiView( SCHEMA_NAME_1 + SCHEMA_EXTENSION );

      // Wait list
      int i;
      for( i = 0; i < 5; i++ )
        if( null != xml.getColumnListOperator( 1 ) )
          break;
      if( 5 == i )
        fail( "Column with undeleted complex type did not appear after undo." );

      SelectItemInColumn( xml, 1, "newComplexType" );

      endTest( );
    }

    public void SafeDeletePreview( )
    {
      startTest( );

      // Add new CT
      SchemaMultiView xml = new SchemaMultiView( SCHEMA_NAME_1 + SCHEMA_EXTENSION );
      SelectItemInColumn( xml, 0, "Complex Types" );
      CallPopupOnListItemNoBlock(
          xml,
          0,
          "Complex Types",
          new CStartsStringComparator( ),
          "Add Complex Type..."
        );
      JDialogOperator jdAdd = new JDialogOperator( "Add Complex Type" );
      JButtonOperator jbOk = new JButtonOperator( jdAdd, "OK" );
      jbOk.push( );
      jdAdd.waitClosed( );
      SelectItemInColumn( xml, 1, "newComplexType" );

      // Refactor -> Safe Delete
      CallPopupOnListItemNoBlock(
          xml,
          1,
          "newComplexType1",
          "Refactor|Safely Delete..."
        );

      // Refactor
      JDialogOperator jdRefactor = new JDialogOperator( "Safe Delete" );
      JButtonOperator jbPreview = new JButtonOperator( jdRefactor, "Preview" );
      jbPreview.push( );
      jdRefactor.waitClosed( );

      // Check preview
      TopComponentOperator top = new TopComponentOperator( "Refactoring" );
      JTreeOperator jTree = new JTreeOperator( top, 0 );

      TreeModel tm = jTree.getModel( );
      Object o[] = new Object[ 4 ];
      o[ 0 ] = tm.getRoot( );
      for( int i = 1; i < 4; i++ )
        o[ i ] = tm.getChild( o[ i - 1 ], 0 );
      TreePath tpp = new TreePath( o );
      jTree.selectPath( tpp );

      JButtonOperator btn = new JButtonOperator( top, "Do Refactoring" );

      MainWindowOperator.StatusTextTracer stt = MainWindowOperator.getDefault( ).getStatusTextTracer( );
      stt.start( );

      btn.push( );

      stt.waitText( "Save All finished." );
      stt.stop( );

      // Check removed
      JListOperator list = xml.getColumnListOperator( 1 );
      if( null != list )
      {
        int iIndex = list.findItemIndex( "newComplexType1" );
        if( -1 != iIndex )
          fail( "newComplexType1 was not remove using Safe Delete with preview." );
      }

      //xml.close( );

      endTest( );
    }

    public void RenameMenuNoPreview( )
    {
      startTest( );

      // Open sample schema again
      /*
      ProjectsTabOperator pto = new ProjectsTabOperator( );
      ProjectRootNode prn = pto.getProjectRootNode( TEST_JAVA_APP_NAME + "|Source Packages|" + TEST_JAVA_APP_NAME + "|" + SAMPLE_SCHEMA_NAME );
      prn.select( );
      prn.performPopupAction( "Open" );
      */
     
      SchemaMultiView xml = new SchemaMultiView( SAMPLE_SCHEMA_NAME );
      SelectInFirstColumn( xml, "Complex Types" );
      SelectItemInColumn( xml, 1, ITEM_CHANGED_NAME );

      CallRefactorSubmenu( "Rename..." );

      // Set new name
      JDialogOperator jdRename = new JDialogOperator( "Rename" );
      JTextComponentOperator txt = new JTextComponentOperator( jdRename, 0 );
      txt.setText( ITEM_ORIGINAL_NAME );
      JButtonOperator jbRename = new JButtonOperator( jdRename, "Refactor" );
      jbRename.push( );
      jdRename.waitClosed( );

      SelectItemInColumn( xml, 1, ITEM_ORIGINAL_NAME );

      endTest( );
    }

    public void UndoRename1( )
    {
      startTest( );

      SchemaMultiView xml = new SchemaMultiView( SAMPLE_SCHEMA_NAME );
      SelectItemInColumn( xml, 1, ITEM_ORIGINAL_NAME );
      CallPopupOnListItem(
          xml,
          1,
          ITEM_ORIGINAL_NAME,
          new CStartsStringComparator( ),
          "Refactor|Undo [Rename]"
        );

      // Wait for long process...
      try
      {
        JDialogOperator jdInProgress = new JDialogOperator( "Refactoring..." );
        jdInProgress.waitClosed( );
      }
      catch( JemmyException ex )
      {
      }

      SelectItemInColumn( xml, 1, ITEM_CHANGED_NAME );

      endTest( );
    }

    public void RenameMenuPreview( )
    {
      startTest( );

      SchemaMultiView xml = new SchemaMultiView( SAMPLE_SCHEMA_NAME );
      SelectInFirstColumn( xml, "Complex Types" );
      SelectItemInColumn( xml, 1, ITEM_CHANGED_NAME );
      new JMenuBarOperator(MainWindowOperator.getDefault()).pushMenuNoBlock("Refactor|Rename...");

      // Set new name
      JDialogOperator jdRename = new JDialogOperator( "Rename" );
      JTextComponentOperator txt = new JTextComponentOperator( jdRename, 0 );
      txt.setText( ITEM_ORIGINAL_NAME );
      JButtonOperator jbRename = new JButtonOperator( jdRename, "Preview" );
      jbRename.push( );
      jdRename.waitClosed( );

      // Check preview
      TopComponentOperator top = new TopComponentOperator( "Refactoring" );
      JTreeOperator jTree = new JTreeOperator( top, 0 );

      TreeModel tm = jTree.getModel( );
      Object o[] = new Object[ 7 ];
      o[ 0 ] = tm.getRoot( );
      for( int i = 1; i < 7; i++ )
        o[ i ] = tm.getChild( o[ i - 1 ], 0 );
      TreePath tpp = new TreePath( o );
      jTree.selectPath( tpp );

      JButtonOperator btn = new JButtonOperator( top, "Do Refactoring" );

      MainWindowOperator.StatusTextTracer stt = MainWindowOperator.getDefault( ).getStatusTextTracer( );
      stt.start( );

      btn.push( );

      stt.waitText( "Save All finished." );
      stt.stop( );

      // Check result
      SelectItemInColumn( xml, 1, ITEM_ORIGINAL_NAME );

      endTest( );
    }

    public void ManipulateAttributes( )
    {
      startTest( );

      // COPY HERE

      // Select potype
      SchemaMultiView xml = new SchemaMultiView( SAMPLE_SCHEMA_NAME );
      SelectItemInColumn( xml, 0, "Complex Types" );
      SelectItemInColumn( xml, 1, "PurchaseOrderType" );
      SelectItemInColumn( xml, 2, "orderDate" );

      // Copy attribute
      CallPopupOnListItem(
          xml,
          2,
          "orderDate",
          new CStartsStringComparator( ),
          "Copy"
        );

      // Select address
      SelectItemInColumn( xml, 1, "USAddress" );

      // Select paste
      CallPopupOnListItem(
          xml,
          1,
          "USAddress",
          new CStartsStringComparator( ),
          "Paste"
        );

      // Check old location still exissts 
      SelectItemInColumn( xml, 0, "Complex Types" );
      SelectItemInColumn( xml, 1, "PurchaseOrderType" );
      SelectItemInColumn( xml, 2, "orderDate" );

      // Check new location excists now
      SelectItemInColumn( xml, 0, "Complex Types" );
      SelectItemInColumn( xml, 1, "USAddress" );
      SelectItemInColumn( xml, 2, "orderDate" );

      // Remove pasted (?)
      /*
      CallPopupOnListItem(
          xml,
          2,
          "orderDate",
          new CStartsStringComparator( ),
          "Delete"
        );
      */

      // CUT HERE

      SelectItemInColumn( xml, 0, "Complex Types" );
      SelectItemInColumn( xml, 1, "PurchaseOrderType" );
      SelectItemInColumn( xml, 2, "orderDate" );

      // Copy attribute
      CallPopupOnListItem(
          xml,
          2,
          "orderDate",
          new CStartsStringComparator( ),
          "Cut"
        );

      // Select address
      SelectItemInColumn( xml, 1, "USAddress" );

      // Select paste
      CallPopupOnListItem(
          xml,
          1,
          "USAddress",
          new CStartsStringComparator( ),
          "Paste"
        );

      // Check old location still exissts 
      SelectItemInColumn( xml, 0, "Complex Types" );
      SelectItemInColumn( xml, 1, "PurchaseOrderType" );
      CheckNoItemInColumn( xml, 2, "orderDate" );

      // Check new location excists now
      SelectItemInColumn( xml, 0, "Complex Types" );
      SelectItemInColumn( xml, 1, "USAddress" );
      SelectItemInColumn( xml, 2, "orderDate1" );

      endTest( );
    }

    public void ManipulateComponents( )
    {
      startTest( );

      // Copy
      SchemaMultiView pur = new SchemaMultiView( SAMPLE_SCHEMA_NAME );

      SelectInFirstColumn( pur, "Complex Types" );
      CallPopupOnListItem(
          pur,
          1,
          "PurchaseOrderType",
          new CStartsStringComparator( ),
          "Copy"
        );

      // Paste
      SchemaMultiView xsd = new SchemaMultiView( SCHEMA_NAME_1 );
      CallPopupOnListItem(
          xsd,
          0,
          "Complex Types",
          new CStartsStringComparator( ),
          "Paste"
        );

      // Check result
      SelectItemInColumn( xsd, 1, "PurchaseOrderType" );
      pur = new SchemaMultiView( SAMPLE_SCHEMA_NAME );
      SelectItemInColumn( xsd, 0, "Complex Types" );
      SelectItemInColumn( xsd, 1, "PurchaseOrderType" );

      // CUT
      CallPopupOnListItem(
          pur,
          1,
          "PurchaseOrderType",
          new CStartsStringComparator( ),
          "Cut"
        );

      // Paste
      xsd = new SchemaMultiView( SCHEMA_NAME_1 );
      CallPopupOnListItem(
          xsd,
          0,
          "Complex Types",
          new CStartsStringComparator( ),
          "Paste"
        );

      Sleep( 1000 );

      // Check result
      SelectItemInColumn( xsd, 1, "PurchaseOrderType1" );
      pur = new SchemaMultiView( SAMPLE_SCHEMA_NAME );
      SelectItemInColumn( pur, 0, "Complex Types" );
      CheckNoItemInColumn( pur, 1, "PurchaseOrderType" );

      endTest( );
    }
}
