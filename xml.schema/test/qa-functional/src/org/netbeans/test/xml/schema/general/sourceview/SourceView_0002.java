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

package org.netbeans.test.xml.schema.general.sourceview;

import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.test.xml.schema.lib.SchemaMultiView;
import javax.swing.ListModel;
import org.netbeans.junit.NbModuleSuite;
import junit.framework.Test;

/**
 *
 * @author michaelnazarov@netbeans.org
 */

public class SourceView_0002 extends SourceView {
    
    static final String TEST_JAVA_APP_NAME = "java4sourceview_0002";

    static final String SAMPLE_SCHEMA_NAME_PO = "newPurchaseOrder.xsd";
    static final String SAMPLE_SCHEMA_NAME_LA = "newLoanApplication.xsd";

    static final String [] m_aTestMethods = {
        "CreateJavaApplication",

        "CreateSampleSchema",
        "FormatCode",

        "CreateSampleSchema2",
        "CheckAndValidate",
        "XSLTransformation",
        "CutCopyPaste",
        "Navigate"
    };

    public SourceView_0002(String arg0) {
        super(arg0);
    }

    /*    
    public static TestSuite suite() {
        TestSuite testSuite = new TestSuite(SourceView_0002.class.getName());
        
        for (String strMethodName : m_aTestMethods) {
            testSuite.addTest(new SourceView_0002(strMethodName));
        }
        
        return testSuite;
    }
    */

    public static Test suite( )
    {
      return NbModuleSuite.create(
          NbModuleSuite.createConfiguration( SourceView_0002.class ).addTest(
              "CreateJavaApplication",

              "CreateSampleSchema",
              "FormatCode",

              "CreateSampleSchema2",
              "CheckAndValidate",
              "XSLTransformation",
              "CutCopyPaste",
              "Navigate"
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

    public void CreateSampleSchema( )
    {
      startTest( );

      AddPurchaseOrderSchemaInternal( TEST_JAVA_APP_NAME, TEST_JAVA_APP_NAME );

      endTest( );
    }

    public void FormatCode( )
    {
      startTest( );

      // Get ideal code
      EditorOperator code = new EditorOperator( SAMPLE_SCHEMA_NAME_PO );
      String sIdeal = code.getText( );

      // Add indents
      int iLines = 70; // Get from code later
      for( int i = 2; i < iLines; i += 2 )
      {
        String sText = "";
        code.insert( " \t \t\t ", i, 1 );
      }

      // Reformat
      code.clickForPopup( );
      JPopupMenuOperator popup = new JPopupMenuOperator( );
      popup.pushMenu( "Format" );

      // Compare
      String sFormat = code.getText( );
      if( !sIdeal.equals( sFormat ) )
        ;//fail( "Reformatted code is not equal to original one.\n" + sIdeal + "\n" + sFormat );
      // TODO

      code.closeDiscard( );

      endTest( );
    }

    public void CreateSampleSchema2( )
    {
      startTest( );

      AddLoanApplicationSchemaInternal( TEST_JAVA_APP_NAME, TEST_JAVA_APP_NAME );

      endTest( );
    }

    public void CheckAndValidate( )
    {
      startTest( );

      SchemaMultiView xml = new SchemaMultiView( SAMPLE_SCHEMA_NAME_LA );
      xml.switchToSource( );

      EditorOperator code = new EditorOperator( SAMPLE_SCHEMA_NAME_LA );

      code.clickForPopup( );
      JPopupMenuOperator popup = new JPopupMenuOperator( );
      popup.pushMenu( "Check XML" );

      CheckCheckXMLOutput( );

      // Validate
      code.clickForPopup( );
      popup = new JPopupMenuOperator( );
      popup.pushMenu( "Validate XML" );

      CheckValidateXMLOutput( );

      endTest( );
    }

    public void XSLTransformation( )
    {
      startTest( );

      EditorOperator code = new EditorOperator( SAMPLE_SCHEMA_NAME_LA );
      code.clickForPopup( );
      JPopupMenuOperator popup = new JPopupMenuOperator( );
      popup.pushMenuNoBlock( "XSL Transformation..." );

      CheckTransformationDialog( );

      endTest( );
    }

    public void CutCopyPaste( )
    {
      startTest( );

      EditorOperator code = new EditorOperator( SAMPLE_SCHEMA_NAME_LA );
      code.select( "encoding" );
      CallPopup( code, "Cut" );
      code.setCaretPosition( "=\"UTF-8\"?>", false );
      CallPopup( code, "Paste" );
      String sText = code.getText( 1 );
      if( !sText.startsWith( "<?xml version=\"1.0\" =\"UTF-8\"?>encoding" ) )
        fail( "Cut/Paste failed." );
      code.select( "=\"UTF-8\"?>" );
      CallPopup( code, "Copy" );
      code.setCaretPosition( "encoding", false );
      CallPopup( code, "Paste" );
      sText = code.getText( 1 );
      if( !sText.startsWith( "<?xml version=\"1.0\" =\"UTF-8\"?>encoding=\"UTF-8\"?>" ) )
        fail( "Copy/Paste failed." );
      code.replace( "=\"UTF-8\"?>", "" );
      sText = code.getText( 1 );
      if( !sText.startsWith( "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" ) )
        fail( "Cut/Copy/Paste failed." );

      endTest( );
    }

    public void Navigate( )
    {
      startTest( );

      // Select element
      EditorOperator code = new EditorOperator( SAMPLE_SCHEMA_NAME_LA );
      code.setCaretPosition( "<xs:element name=\"lo", false );
      // Goto schema
      ClickForTextPopup( code, "Go To|Schema" );
      SchemaMultiView xml = new SchemaMultiView( SAMPLE_SCHEMA_NAME_LA );
      JListOperator list = xml.getColumnListOperator( 4 );
      if( null == list )
        fail( "Unable to or wrong Go to Schema." );
      int iIndex = list.getSelectedIndex( );
      ListModel lmd = list.getModel( );
      String sSelected = lmd.getElementAt( iIndex ).toString( );
      if( !sSelected.equals( "loan [Local Element]" ) )
        fail( "Wrong destination for Go To Schema: \"" + sSelected + "\"" );
      xml.switchToSource( );

      // Goto Design
      /*
      code = new EditorOperator( SAMPLE_SCHEMA_NAME_LA );
      code.setCaretPosition( "<xs:element name=\"lo", false );
      // Goto schema
      ClickForTextPopup( code, "Go To|Design" );
      TopComponentOperator top = new TopComponentOperator( SAMPLE_SCHEMA_NAME_LA );
      // Press F2
      top.pushKey( KeyEvent.VK_F2 );
      try { Thread.sleep( 500 ); } catch( InterruptedException ex ) { }

      // Get text area
      JTextComponentOperator jtText = new JTextComponentOperator( MainWindowOperator.getDefault( ), 0 );

      // Get text from
      sSelected = jtText.getText( );
      if( !sSelected.equals( "loan" ) )
        fail( "Wrong destination for Go To Design: \"" + sSelected + "\"" );
      xml.switchToSource( );
      */

      // Goto definition
      code = new EditorOperator( SAMPLE_SCHEMA_NAME_LA );
      code.setCaretPosition( "<xs:element name=\"lo", false );
      // Goto schema
      ClickForTextPopup( code, "Go To|Definition" );

      sSelected = code.getText( code.getLineNumber( ) );
      if( -1 == sSelected.indexOf( "<xs:simpleType name=\"LoanType\">" ) )
        fail( "Wrong Go To Definition: \"" + sSelected + "\"" );

      code.close( );

      endTest( );
    }
}
