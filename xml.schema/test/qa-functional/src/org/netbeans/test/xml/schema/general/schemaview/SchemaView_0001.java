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

package org.netbeans.test.xml.schema.general.schemaview;

import javax.swing.tree.TreePath;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.OutputOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.jellytools.actions.AttachWindowAction;
import org.netbeans.jemmy.operators.JMenuBarOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.jellytools.MainWindowOperator;
import java.awt.event.KeyEvent;
import org.netbeans.test.xml.schema.lib.SchemaMultiView;
import org.netbeans.jellytools.properties.PropertySheetOperator;
import org.netbeans.jellytools.properties.Property;
import org.netbeans.jellytools.TopComponentOperator;
import javax.swing.JPopupMenu;
import org.netbeans.jellytools.modules.web.NavigatorOperator;
import org.netbeans.junit.NbModuleSuite;
import junit.framework.Test;
import org.netbeans.jemmy.operators.JTextComponentOperator;

/**
 *
 * @author michaelnazarov@netbeans.org
 */

public class SchemaView_0001 extends SchemaView {
    
    static final String TEST_JAVA_APP_NAME = "java4schemaview_0001";

    static final String SCHEMA_SHORT_NAME_1 = "newXmlSchema1";
    static final String SCHEMA_SHORT_NAME_2 = "newXmlSchema2";
    static final String SCHEMA_SHORT_NAME_3 = "newXmlSchema3";
    static final String SCHEMA_SHORT_NAME_4 = "newXmlSchema4";
    static final String SCHEMA_SHORT_NAME_5 = "newXmlSchema5";
    static final String SCHEMA_SHORT_NAME_6 = "newXmlSchema6";
    static final String SCHEMA_SHORT_NAME_7 = "newXmlSchema7";
    static final String SCHEMA_SHORT_NAME_8 = "newXmlSchema8";
    static final String SCHEMA_SHORT_NAME_9 = "newXmlSchema9";

    static final String SCHEMA_NAME_1 = SCHEMA_SHORT_NAME_1 + SCHEMA_EXTENSION;
    static final String SCHEMA_NAME_2 = SCHEMA_SHORT_NAME_2 + SCHEMA_EXTENSION;
    static final String SCHEMA_NAME_3 = SCHEMA_SHORT_NAME_3 + SCHEMA_EXTENSION;
    static final String SCHEMA_NAME_4 = SCHEMA_SHORT_NAME_4 + SCHEMA_EXTENSION;
    static final String SCHEMA_NAME_5 = SCHEMA_SHORT_NAME_5 + SCHEMA_EXTENSION;
    static final String SCHEMA_NAME_6 = SCHEMA_SHORT_NAME_6 + SCHEMA_EXTENSION;
    static final String SCHEMA_NAME_7 = SCHEMA_SHORT_NAME_7 + SCHEMA_EXTENSION;
    static final String SCHEMA_NAME_8 = SCHEMA_SHORT_NAME_8 + SCHEMA_EXTENSION;
    static final String SCHEMA_NAME_9 = SCHEMA_SHORT_NAME_9 + SCHEMA_EXTENSION;

    static final String SAMPLE_SCHEMA_NAME = "newLoanApplication.xsd";

    public SchemaView_0001(String arg0) {
        super(arg0);
    }

    public static Test suite( )
    {
      return NbModuleSuite.create(
          NbModuleSuite.createConfiguration( SchemaView_0001.class ).addTest(
              "CreateJavaApplication",

              "CreateXMLSchema",
              "AddComponents",

              "CreateXMLSchema2",
              "ImportSchema",

              "CreateXMLSchema3",
              "CreateXMLSchema4",
              "IncludeSchema",

              "CreateXMLSchema5",
              "CreateXMLSchema6",
              "RedefineSchema",

              "CreateXMLSchema7",
              "ImportInternalSchema",

              "ModifyAttributesProperties",

              "CreateXMLSchema8",
              "AddComponents8",
              "ModifyAttributesCustomizer",

              "NavigateThroughSchema",
              "CheckAndValidate",
              "SyncronizationBetweenViews",
              "GotoSourceDesign",

              "CreateXMLSchema9",
              "DeleteComponents"
           )
           .enableModules( ".*" )
           .clusters( ".*" )
           //.gui( true )
        );
    }

    public void CreateJavaApplication( )
    {
      startTest( );

      CreateJavaApplicationInternal( TEST_JAVA_APP_NAME );

      endTest( );
    }

    public void CreateXMLSchema( )
    {
      startTest( );

      CreateSchemaInternal(
          TEST_JAVA_APP_NAME + "|Source Packages|" + TEST_JAVA_APP_NAME,
          SCHEMA_SHORT_NAME_1
        );

      endTest( );
    }

    public void AddComponents( )
    {
      startTest( );

      AddComponentsInternal( SCHEMA_NAME_1 );

      endTest( );
    }

    public void CreateXMLSchema2( )
    {
      startTest( );

      CreateSchemaInternal(
          TEST_JAVA_APP_NAME + "|Source Packages|" + TEST_JAVA_APP_NAME,
          SCHEMA_SHORT_NAME_2
        );

      endTest( );
    }

    public void ImportSchema( )
    {
      startTest( );

      // Get schema
      SchemaMultiView xml = new SchemaMultiView( SCHEMA_NAME_1 );
      CallPopupOnListItemNoBlock( xml, 0, "Referenced Schemas", "Add|Import..." );

      JDialogOperator jdImport = new JDialogOperator( "Add Import" );
      JTableOperator jtTable = new JTableOperator( jdImport, 0 );

      CImportClickData[] aimpData =
      {
        new CImportClickData( true, 0, 0, 2, 3, "Unknown import table state after first click, number of rows: ", null ),
        new CImportClickData( true, 1, 0, 2, 5, "Unknown import table state after second click, number of rows: ", null ),
        new CImportClickData( true, 2, 0, 2, 6, "Unknown import table state after third click, number of rows: ", null ),
        new CImportClickData( true, 3, 0, 2, 8, "Unknown import table state after forth click, number of rows: ", null ),
        new CImportClickData( true, 5, 1, 1, 8, "Unknown to click on checkbox. #", null )
      };

      for( CImportClickData cli : aimpData )
      {
        Sleep( 1000 );
        ExpandByClicks( jtTable, cli.row, cli.col, cli.count, cli.result, cli.error, cli.timeout );
      }

      JButtonOperator jbOk = new JButtonOperator( jdImport, "OK" );
      jbOk.push( );
      jdImport.waitClosed( );

      // Check
      JListOperator list = xml.getColumnListOperator( 1 );
      int iIndex = list.findItemIndex( "import" );
      if( -1 == iIndex )
        fail( "Unable to add import." );

      endTest( );
    }

    public void CreateXMLSchema3( )
    {
      startTest( );

      CreateSchemaInternal(
          TEST_JAVA_APP_NAME + "|Source Packages|" + TEST_JAVA_APP_NAME,
          SCHEMA_SHORT_NAME_3
        );

      endTest( );
    }

    public void CreateXMLSchema4( )
    {
      startTest( );

      CreateSchemaInternal(
          TEST_JAVA_APP_NAME + "|Source Packages|" + TEST_JAVA_APP_NAME,
          SCHEMA_SHORT_NAME_4,
          ""
        );

      endTest( );
    }

    public void IncludeSchema( )
    {
      startTest( );

      // Get schema
      SchemaMultiView xml = new SchemaMultiView( SCHEMA_NAME_3 );
      CallPopupOnListItemNoBlock( xml, 0, "Referenced Schemas", "Add|Include..." );

      JDialogOperator jdInclude = new JDialogOperator( "Add Include" );
      JTableOperator jtTable = new JTableOperator( jdInclude, 0 );

      CImportClickData[] aincData =
      {
        new CImportClickData( true, 0, 0, 2, 3, "Unknown include table state after first click, number of rows: ", null ),
        new CImportClickData( true, 1, 0, 2, 5, "Unknown include table state after second click, number of rows: ", null ),
        new CImportClickData( true, 2, 0, 2, 6, "Unknown include table state after third click, number of rows: ", null ),
        new CImportClickData( true, 3, 0, 2, 10, "Unknown include table state after forth click, number of rows: ", null ),
        new CImportClickData( true, 4, 1, 1, 10, "Unknown to click on checkbox. #", "Include/Redefine must have same namespace, or none." ),
        new CImportClickData( true, 5, 1, 1, 10, "Unknown to click on checkbox. #", "Include/Redefine must have same namespace, or none." ),
        new CImportClickData( true, 6, 1, 1, 10, "Unknown to click on checkbox. #", "Document cannot reference itself." ),
        new CImportClickData( true, 7, 1, 1, 10, "Unknown to click on checkbox. #", null )
      };

      for( CImportClickData cli : aincData )
      {
        Sleep( 1000 );
        ExpandByClicks( jtTable, cli.row, cli.col, cli.count, cli.result, cli.error, cli.timeout );
      }

      JButtonOperator jbOk = new JButtonOperator( jdInclude, "OK" );
      jbOk.push( );
      jdInclude.waitClosed( );

      // Check
      JListOperator list = xml.getColumnListOperator( 1 );
      int iIndex = list.findItemIndex( "include" );
      if( -1 == iIndex )
        fail( "Unable to add include." );

      endTest( );
    }

    public void CreateXMLSchema5( )
    {
      startTest( );

      CreateSchemaInternal(
          TEST_JAVA_APP_NAME + "|Source Packages|" + TEST_JAVA_APP_NAME,
          SCHEMA_SHORT_NAME_5
        );

      endTest( );
    }

    public void CreateXMLSchema6( )
    {
      startTest( );

      CreateSchemaInternal(
          TEST_JAVA_APP_NAME + "|Source Packages|" + TEST_JAVA_APP_NAME,
          SCHEMA_SHORT_NAME_6,
          ""
        );

      endTest( );
    }

    public void RedefineSchema( )
    {
      startTest( );

      // Get schema
      SchemaMultiView xml = new SchemaMultiView( SCHEMA_NAME_5 );
      CallPopupOnListItemNoBlock( xml, 0, "Referenced Schemas", "Add|Redefine..." );

      JDialogOperator jdRedefine = new JDialogOperator( "Add Redefine" );
      JTableOperator jtTable = new JTableOperator( jdRedefine, 0 );

      CImportClickData[] aredData =
      {
        new CImportClickData( true, 0, 0, 2, 3, "Unknown include table state after first click, number of rows: ", null ),
        new CImportClickData( true, 1, 0, 2, 5, "Unknown include table state after second click, number of rows: ", null ),
        new CImportClickData( true, 2, 0, 2, 6, "Unknown include table state after third click, number of rows: ", null ),
        new CImportClickData( true, 3, 0, 2, 12, "Unknown include table state after forth click, number of rows: ", null ),
        new CImportClickData( true, 4, 1, 1, 12, "Unknown to click on checkbox. #", "Include/Redefine must have same namespace, or none." ),
        new CImportClickData( true, 5, 1, 1, 12, "Unknown to click on checkbox. #", "Include/Redefine must have same namespace, or none." ),
        new CImportClickData( true, 6, 1, 1, 12, "Unknown to click on checkbox. #", "Include/Redefine must have same namespace, or none." ),
        //new CImportClickData( true, 7, 1, 1, 12, "Unknown to click on checkbox. #", null ),
        new CImportClickData( true, 8, 1, 1, 12, "Unknown to click on checkbox. #", "Document cannot reference itself." ),
        new CImportClickData( true, 9, 1, 1, 12, "Unknown to click on checkbox. #", null )
      };

      for( CImportClickData cli : aredData )
      {
        Sleep( 1000 );
        ExpandByClicks( jtTable, cli.row, cli.col, cli.count, cli.result, cli.error, cli.timeout );
      }

      JButtonOperator jbOk = new JButtonOperator( jdRedefine, "OK" );
      jbOk.push( );
      jdRedefine.waitClosed( );

      // Check
      JListOperator list = xml.getColumnListOperator( 1 );
      int iIndex = list.findItemIndex( "redefine" );
      if( -1 == iIndex )
        fail( "Unable to add redefine." );

      endTest( );
    }

    public void CreateXMLSchema7( )
    {
      startTest( );

      CreateSchemaInternal(
          TEST_JAVA_APP_NAME + "|Source Packages|" + TEST_JAVA_APP_NAME,
          SCHEMA_SHORT_NAME_7
        );

      endTest( );
    }

    public void ImportInternalSchema( )
    {
      startTest( );

      SchemaMultiView xml = new SchemaMultiView( SCHEMA_NAME_7 );
      xml.switchToSource( );
      EditorOperator code = new EditorOperator( SCHEMA_NAME_7 );
      code.setCaretPosition( "</xsd:schema>", true );
      code.insert( "<xsd:import schemaLocation=\"http://schemas.xmlsoap.org/wsdl\" namespace=\"http://schemas.xmlsoap.org/wsdl\"/>" );
      xml.switchToSchema( );

      SelectInFirstColumn( xml, "Referenced Schemas" );
      /*
      JListOperator list = xml.getColumnListOperator( 0 );
      list.selectItem( "Referenced Schemas" );
      */

      JListOperator list = xml.getColumnListOperator( 1 );
      int iIndex = list.findItemIndex( "import" );
      if( -1 == iIndex )
        fail( "Unable to import remote schema using code insertion." );

      endTest( );
    }

    public void ModifyAttributesProperties( )
    {
      startTest( );

      // Open properties
      PropertySheetOperator prop = PropertySheetOperator.invoke( );

      // Modify name attribute
      String[] asData = {
          "Attribute",
          "Attribute Group",
          "Complex Type",
          "Element",
          "Group",
          "Simple Type"
        };

      for( String sBase : asData )
      {
        String sCategory = sBase + "s";
        String sName = "new" + sBase.replaceAll( " ", "" );
        String sRename = "renamed" + sBase.replaceAll( " ", "" );

        // Get schema view
        SchemaMultiView xml = new SchemaMultiView( SCHEMA_NAME_1 );
        // Get first column
        xml.switchToSchema( );
        xml.switchToSchemaColumns( );

        SelectInFirstColumn( xml, sCategory );
        /*
        JListOperator list_1 = xml.getColumnListOperator( 0 );
        // Select category
        int iIndex = list_1.findItemIndex( sCategory, new CStartsStringComparator( ) );
        list_1.selectItem( iIndex );
        */
        // Get second column
        JListOperator list_2 = xml.getColumnListOperator( 1 );
        list_2.selectItem( sName );
        // Change property
        Property p = new Property( prop, "Name" );
        p.setValue( sRename );
        if( !sRename.equals( p.getValue( ) ) )
          fail( "Unable to rename " + sBase );
        // Check second column
        list_2 = xml.getColumnListOperator( 1 );
        list_2.selectItem( sRename );

        // Check source view
        xml.switchToSource( );
        EditorOperator code = new EditorOperator( SCHEMA_NAME_1 );
        String sText = code.getText( );
        if( -1 == sText.indexOf( "name=\"" + sRename + "\"" ) )
          fail( "Rename failed for source view" );

        // Check Design view if applicable
          // ToDo

        // Switch back (?)
        xml.switchToSchema( );
      }

      prop.close( );

      endTest( );
    }

    public void CreateXMLSchema8( )
    {
      startTest( );

      CreateSchemaInternal(
          TEST_JAVA_APP_NAME + "|Source Packages|" + TEST_JAVA_APP_NAME,
          SCHEMA_SHORT_NAME_8
        );

      endTest( );
    }

    public void AddComponents8( )
    {
      startTest( );

      AddComponentsInternal( SCHEMA_NAME_8 );

      endTest( );
    }

    public void CallCustomizer(
        SchemaMultiView xml,
        String sCategory,
        String sName
      )
    {
      // Select in first column
      SelectInFirstColumn( xml, sCategory );

      // Popup in second column
      CallPopupOnListItemNoBlock( xml, 1, sName, "Customize" );
    }

    public void CheckSource(
        SchemaMultiView xml,
        String[] asIdeal
      )
    {
      // Check source
      xml.switchToSource( );
      EditorOperator code = new EditorOperator( SCHEMA_NAME_8 );
      code.setCaretPosition( asIdeal[ 0 ], true );
      int iLine = code.getLineNumber( );
      for( String sIdeal : asIdeal )
      {
        String sCode = code.getText( iLine++ );
        if( -1 == sCode.indexOf( sIdeal ) )
          fail( "Unable to find ideal customized code \"" + sIdeal + "\"." );
      }
      xml.switchToSchema( );
    }

    public void CustomizeAttribute( SchemaMultiView xml )
    {
      CallCustomizer( xml, "Attributes", "newAttribute" );

      // Handle dialog
      JDialogOperator jdCustom = new JDialogOperator( "Global Attribute Customizer" );
      JRadioButtonOperator jrbType = new JRadioButtonOperator( jdCustom, "Inline Simple Type" );
      jrbType.setSelected( true );
      jrbType.clickMouse( );

      JButtonOperator jbOk = new JButtonOperator( jdCustom, "OK" );
      jbOk.push( );
      jdCustom.waitClosed( );

      // Check source
      String[] asIdeal =
      {
        "<xsd:attribute name=\"newAttribute\">",
        "<xsd:simpleType>",
        "<xsd:restriction base=\"xsd:string\"/>",
        "</xsd:simpleType>",
        "</xsd:attribute>"
      };
      CheckSource( xml, asIdeal );
    }

    public void CustomizeComplexType( SchemaMultiView xml )
    {
      CallCustomizer( xml, "Complex Types", "newComplexType" );

      // Handle dialog
      JDialogOperator jdCustom = new JDialogOperator( "Global Complex Type Customizer" );
      JRadioButtonOperator jrbType = new JRadioButtonOperator( jdCustom, "Use Existing Definition" );
      jrbType.setSelected( true );
      jrbType.clickMouse( );

      JTreeOperator jtTree = new JTreeOperator( jdCustom, 0 );
      TreePath path = jtTree.findPath(
          "Built-in Types|string",
          new CStartsStringComparator( )
        );
      jtTree.selectPath( path );

      JButtonOperator jbOk = new JButtonOperator( jdCustom, "OK" );
      jbOk.push( );
      jdCustom.waitClosed( );

      // Check source
      String[] asIdeal =
      {
        "<xsd:complexType name=\"newComplexType\">",
        "<xsd:simpleContent>",
        "<xsd:extension base=\"xsd:string\"/>",
        "</xsd:simpleContent>",
        "</xsd:complexType>"
      };
      CheckSource( xml, asIdeal );
    }

    public void CustomizeElement( SchemaMultiView xml )
    {
      CallCustomizer( xml, "Elements", "newElement" );

      // Handle dialog
      JDialogOperator jdCustom = new JDialogOperator( "Global Element Customizer" );
      JRadioButtonOperator jrbType = new JRadioButtonOperator( jdCustom, "Inline Simple Type" );
      jrbType.setSelected( true );
      jrbType.clickMouse( );

      JButtonOperator jbOk = new JButtonOperator( jdCustom, "OK" );
      jbOk.push( );
      jdCustom.waitClosed( );

      // Check source
      String[] asIdeal =
      {
        "<xsd:element name=\"newElement\">",
        "<xsd:simpleType>",
        "<xsd:restriction base=\"xsd:string\"/>",
        "</xsd:simpleType>",
        "</xsd:element>"
      };
      CheckSource( xml, asIdeal );
    }

    public void CustomizeSimpleType( SchemaMultiView xml )
    {
      CallCustomizer( xml, "Simple Types", "newSimpleType" );

      // Handle dialog
      JDialogOperator jdCustom = new JDialogOperator( "Global Simple Type Customizer" );
      JRadioButtonOperator jrbType = new JRadioButtonOperator( jdCustom, "Inline Definition" );
      jrbType.setSelected( true );
      jrbType.clickMouse( );

      JButtonOperator jbOk = new JButtonOperator( jdCustom, "OK" );
      jbOk.push( );
      jdCustom.waitClosed( );

      // Check source
      String[] asIdeal =
      {
        "<xsd:simpleType name=\"newSimpleType\">",
        "<xsd:restriction>",
        "<xsd:simpleType>",
        "<xsd:restriction base=\"xsd:string\"/>",
        "</xsd:simpleType>",
        "</xsd:restriction>",
        "</xsd:simpleType>"
      };
      CheckSource( xml, asIdeal );
    }

    public void ModifyAttributesCustomizer( )
    {
      startTest( );

      SchemaMultiView xml = new SchemaMultiView( SCHEMA_NAME_8 );

      CustomizeAttribute( xml );
      CustomizeComplexType( xml );
      CustomizeElement( xml );
      CustomizeSimpleType( xml );

      xml.close( );

      endTest( );
    }

    public void NavigateThroughSchema( )
    {
      startTest( );

      AddLoanApplicationSchemaInternal(
          TEST_JAVA_APP_NAME,
          TEST_JAVA_APP_NAME
        );

      SchemaMultiView xml = new SchemaMultiView( SAMPLE_SCHEMA_NAME );
      String sColumnDump = Dump( xml, 0, "" );
      if( !data.m_sData.equals( sColumnDump ) )
        fail( "Wrong column dump: \"" + sColumnDump + "\"" );
      xml.switchToSchemaTree( );
      TopComponentOperator top = new TopComponentOperator( "newLoanApplication.xsd" );
      JTreeOperator tree = new JTreeOperator( top, 0 );
      String sTreeDump = Dump( tree, tree.getRoot( ), -1, "" );
      if( !data.m_sData.equals( sTreeDump ) )
        fail( "Wrong tree dump: \"" + sTreeDump + "\"" );
      //System.out.println( sTreeDump.replaceAll( "\"", "\\\"" ) );
      xml.switchToSchema( );
      xml.switchToSchemaColumns( );

      endTest( );
    }

    public void CheckAndValidate( )
    {
      startTest( );

      // Set focus to file for validation to ensure Validate menu enabled
      new JMenuBarOperator(MainWindowOperator.getDefault()).pushMenu("Window|Editor");
      
      // Validate
      new JMenuBarOperator(MainWindowOperator.getDefault()).pushMenu("Run|Validate XML");

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

      out.close( );

      endTest( );
    }

    public void SyncronizationBetweenViews( )
    {
      startTest( );

      // Clone sample two times
      TopComponentOperator code = new TopComponentOperator( SAMPLE_SCHEMA_NAME );
      code.pushMenuOnTab( "Close Other Documents" );
      code.cloneDocument( );
      code.cloneDocument( );

      NavigatorOperator navi = new NavigatorOperator( );
      navi.close( );

      // To ensure cloned documents did not rewite 0 index
      SchemaMultiView sampleSchema = new SchemaMultiView( SAMPLE_SCHEMA_NAME, 0 );
      SchemaMultiView sampleSource = new SchemaMultiView( SAMPLE_SCHEMA_NAME, 1 );
      SchemaMultiView sampleDesign = new SchemaMultiView( SAMPLE_SCHEMA_NAME, 2 );

      // Select different views
      sampleSource.switchToSource( );
      sampleDesign.switchToDesign( );

      // Tile
      sampleSchema.getTopComponentOperator().attachTo( sampleSource.getTopComponentOperator(), AttachWindowAction.BOTTOM );
      sampleDesign.getTopComponentOperator().attachTo( sampleSchema.getTopComponentOperator(), AttachWindowAction.BOTTOM );

      // Modify property (ies?)

      // TODO

      // Check sync

      sampleSource.close( );
      //sampleSchema.close( );
      sampleDesign.close( );

      endTest( );
    }

    public void GotoSourceDesign( )
    {
      startTest( );

      SchemaMultiView xml = new SchemaMultiView( SAMPLE_SCHEMA_NAME );
      xml.switchToSchema( );
      xml.switchToSchemaTree( );
      TopComponentOperator top = new TopComponentOperator( SAMPLE_SCHEMA_NAME );
      JTreeOperator tree = new JTreeOperator( top, 0 );
      TreePath path = tree.findPath( "Complex Types|AddressType" );
      JPopupMenu menu = tree.callPopupOnPath( path );
      JPopupMenuOperator popup = new JPopupMenuOperator( menu );
      popup.pushMenu( "Go To|Source" );

      EditorOperator code = new EditorOperator( SAMPLE_SCHEMA_NAME );
      String sCode = code.getText( code.getLineNumber( ) );
      if( -1 == sCode.indexOf( "<xs:complexType name=\"AddressType\">" ) )
        fail( "Wrong goto source result: \"" + sCode + "\"" );

      ClickForTextPopup( code );
      popup = new JPopupMenuOperator( );
      popup.pushMenu( "Go To|Design" );

      // Get DV
      top = new TopComponentOperator( SAMPLE_SCHEMA_NAME );

      // Press F2
      top.pushKey( KeyEvent.VK_F2 );
      try { Thread.sleep( 500 ); } catch( InterruptedException ex ) { }

      // Get text area
      JTextComponentOperator jtText = new JTextComponentOperator( MainWindowOperator.getDefault( ), 0 );

      // Get text from
      String sText = jtText.getText( );
      if( !sText.equals( "AddressType" ) )
      {
        fail( "Goto source-design error, required: AddressType; found: \"" + sText + "\"" );
      }

      // Escape
      top.pushKey( KeyEvent.VK_ESCAPE );

      xml.close( );
      //xml.switchToSchema( );
      //xml.switchToSchemaColumns( );

      endTest( );
    }

    public void CreateXMLSchema9( )
    {
      startTest( );

      CreateSchemaInternal(
          TEST_JAVA_APP_NAME + "|Source Packages|" + TEST_JAVA_APP_NAME,
          SCHEMA_SHORT_NAME_9
        );

      endTest( );
    }

    public void DeleteComponents( )
    {
      startTest( );

      AddComponentsInternal( SCHEMA_NAME_9 );
      DeleteComponentsInternal( SCHEMA_NAME_9 );

      endTest( );
    }
}
