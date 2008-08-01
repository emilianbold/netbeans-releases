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
import org.netbeans.test.xml.schema.lib.SchemaMultiView;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.junit.NbModuleSuite;
import junit.framework.Test;
import org.netbeans.jemmy.operators.AbstractButtonOperator;

/**
 *
 * @author michaelnazarov@netbeans.org
 */

public class SourceView_0001 extends SourceView {
    
    static final String TEST_JAVA_APP_NAME = "java4sourceview_0001";

    static final String SAMPLE_SCHEMA_NAME = "newLoanApplication.xsd";

    static final String SCHEMA_SHORT_NAME_1 = "newXMLSchema1";
    static final String SCHEMA_SHORT_NAME_2 = "newXMLSchema2";
    static final String SCHEMA_SHORT_NAME_3 = "newXMLSchema3";

    static final String SCHEMA_NAME_1 = "newXMLSchema1.xsd";
    static final String SCHEMA_NAME_2 = "newXMLSchema2.xsd";
    static final String SCHEMA_NAME_3 = "newXMLSchema3.xsd";

    static final String [] m_aTestMethods = {
      "CreateJavaApplication",

      "CreateSchema1",
      "FindSearch",

      "CreateSchema2",
      "Bookmarks",

      "CreateSchema3",
      "IndentSelect",

      "CreateSchema4",
      "CheckXML",
      "ValidateXML",
      "Transformation"
    };

    public SourceView_0001(String arg0) {
        super(arg0);
    }

    /*    
    public static TestSuite suite() {
        TestSuite testSuite = new TestSuite(SourceView_0001.class.getName());
        
        for (String strMethodName : m_aTestMethods) {
            testSuite.addTest(new SourceView_0001(strMethodName));
        }
        
        return testSuite;
    }
    */

    public static Test suite( )
    {
      return NbModuleSuite.create(
          NbModuleSuite.createConfiguration( SourceView_0001.class ).addTest(
              "CreateJavaApplication",

              "CreateSchema1",
              "FindSearch",

              "CreateSchema2",
              "Bookmarks",

              "CreateSchema3",
              "IndentSelect",

              "CreateSchema4",
              "CheckXML",
              "ValidateXML",
              "Transformation"
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

    public void CreateSchema1( )
    {
      startTest( );

      CreateSchemaInternal(
          TEST_JAVA_APP_NAME + "|Source Packages|" + TEST_JAVA_APP_NAME,
          SCHEMA_SHORT_NAME_1
        );

      endTest( );
    }

    public void FindSearch( )
    {
      startTest( );

      SchemaMultiView xml = new SchemaMultiView( SCHEMA_NAME_1 );
      xml.switchToSource( );

      EditorOperator code = new EditorOperator( SCHEMA_NAME_1 );
      TopComponentOperator top = new TopComponentOperator( SCHEMA_NAME_1 );


      //for( int i = 0; ; i++ )
        //System.out.println( "" + i + " : " + ( new AbstractButtonOperator( top, i ) ).getX( ) );

      AbstractButtonOperator find = new AbstractButtonOperator( top, 6 );//"Find Selection (Ctrl+F3)" );
      AbstractButtonOperator prev = new AbstractButtonOperator( top, 7 );//"Find Previous Occurence (Shift+F3)" );
      AbstractButtonOperator next = new AbstractButtonOperator( top, 8 );//"Find Next Occurence (F3)" );
      AbstractButtonOperator togg = new AbstractButtonOperator( top, 9 );//"Toggle Highlight Search (Alt+Shift+H)" );

      code.select( "schema" );

      // Highlight
      // TODO

      if( 3 != code.getLineNumber( ) ) fail( "Search: select failed." );
      find.clickMouse( );
      next.clickMouse( );
      if( 4 != code.getLineNumber( ) ) fail( "Search: next failed." );
      prev.clickMouse( );
      prev.clickMouse( );
      prev.clickMouse( );
      if( 8 != code.getLineNumber( ) ) fail( "Search: prev failed." );

      // Toggle highlight off
      // TODO

      code.close( );

      endTest( );
    }

    public void CreateSchema2( )
    {
      startTest( );

      CreateSchemaInternal(
          TEST_JAVA_APP_NAME + "|Source Packages|" + TEST_JAVA_APP_NAME,
          SCHEMA_SHORT_NAME_2
        );

      endTest( );
    }

    public void Bookmarks( )
    {
      startTest( );

      SchemaMultiView xml = new SchemaMultiView( SCHEMA_NAME_2 );
      xml.switchToSource( );

      EditorOperator code = new EditorOperator( SCHEMA_NAME_2 );

      TopComponentOperator top = new TopComponentOperator( SCHEMA_NAME_2 );
      AbstractButtonOperator prev = new AbstractButtonOperator( top, 10 );// "Previous Bookmark" );
      AbstractButtonOperator next = new AbstractButtonOperator( top, 11 );//  "Next Bookmark" );
      AbstractButtonOperator togg = new AbstractButtonOperator( top, 12 );//  "Toggle Bookmark" );

      int[] aiLines = { 3, 8 };

      // Set marks
      int iMax = -1;
      for( int i : aiLines )
      {
        if( iMax < i )
          iMax = i;
        code.setCaretPositionToLine( i );
        togg.clickMouse( );
      }

      // Walk prev
      code.setCaretPosition( 1, 1 );
      for( int i = 0; i < aiLines.length; i++ )
      {
        prev.clickMouse( );
        int iLine = code.getLineNumber( );
        int iExpected = aiLines[ aiLines.length - i - 1 ];
        if( iLine != iExpected )
          fail( "Bookmark prev walk failed at step #" + i + ": found line #" + iLine + ", expected #" + iExpected );
      }

      // Walk next
      code.setCaretPositionToEndOfLine( iMax );
      for( int i = 0; i < aiLines.length; i++ )
      {
        next.clickMouse( );
        int iLine = code.getLineNumber( );
        int iExpected = aiLines[ i ];
        if( iLine != iExpected )
          fail( "Bookmark next walk failed at step #" + i + ": found line #" + iLine + ", expected #" + iExpected );
      }

      code.close( );

      endTest( );
    }

    public void CreateSchema3( )
    {
      startTest( );

      CreateSchemaInternal(
          TEST_JAVA_APP_NAME + "|Source Packages|" + TEST_JAVA_APP_NAME,
          SCHEMA_SHORT_NAME_3
        );

      endTest( );
    }

    public void IndentSelect( )
    {
      startTest( );

      SchemaMultiView xml = new SchemaMultiView( SCHEMA_NAME_3 );
      xml.switchToSource( );

      EditorOperator code = new EditorOperator( SCHEMA_NAME_3 );

      TopComponentOperator top = new TopComponentOperator( SCHEMA_NAME_3 );
      AbstractButtonOperator left = new AbstractButtonOperator( top, 13 );//code.getToolbarButton( "Shift Line Left" );
      AbstractButtonOperator right = new AbstractButtonOperator( top, 14 );//code.getToolbarButton( "Shift Line Right" );

      int iLine = 5;

      code.setCaretPositionToEndOfLine( iLine );
      String sOriginal = code.getText( iLine );
      right.clickMouse( );
      String sRight = code.getText( iLine );
      left.clickMouse( );
      String sLeft = code.getText( iLine );

      if( !sOriginal.equals( sLeft ) )
        fail( "Indentation: sOriginal != sLeft" );
      if( !sRight.equals( "    " + sOriginal ) )
        fail( "Indentation: sRight != spaces + sOriginal" );

      code.closeDiscard( );

      endTest( );
    }

    public void CreateSchema4( )
    {
      startTest( );

      AddLoanApplicationSchemaInternal( TEST_JAVA_APP_NAME, TEST_JAVA_APP_NAME );

      endTest( );
    }

    public void CheckXML( )
    {
      startTest( );

      SchemaMultiView xml = new SchemaMultiView( SAMPLE_SCHEMA_NAME );
      xml.switchToSource( );

      EditorOperator code = new EditorOperator( SAMPLE_SCHEMA_NAME );

      TopComponentOperator top = new TopComponentOperator( SAMPLE_SCHEMA_NAME );
      AbstractButtonOperator check = new AbstractButtonOperator( top, 17 );//code.getToolbarButton( "Check XML" );
      check.clickMouse( );

      CheckCheckXMLOutput( );

      endTest( );
    }

    public void ValidateXML( )
    {
      startTest( );

      //SchemaMultiView xml = new SchemaMultiView( SAMPLE_SCHEMA_NAME );
      //xml.switchToSource( );

      EditorOperator code = new EditorOperator( SAMPLE_SCHEMA_NAME );

      TopComponentOperator top = new TopComponentOperator( SAMPLE_SCHEMA_NAME );
      AbstractButtonOperator validate = new AbstractButtonOperator( top, 18 );//AbstractButtonOperator validate = code.getToolbarButton( "Validate XML" );
      validate.clickMouse( );

      CheckValidateXMLOutput( );

      endTest( );
    }

    public void Transformation( )
    {
      startTest( );

      //SchemaMultiView xml = new SchemaMultiView( SAMPLE_SCHEMA_NAME );
      //xml.switchToSource( );

      EditorOperator code = new EditorOperator( SAMPLE_SCHEMA_NAME );

      TopComponentOperator top = new TopComponentOperator( SAMPLE_SCHEMA_NAME );
      AbstractButtonOperator trans = new AbstractButtonOperator( top, 19 );//      AbstractButtonOperator trans = code.getToolbarButton( "XSL Transformation..." );
      trans.pushNoBlock( );

      CheckTransformationDialog( );

      code.close( );

      endTest( );
    }
}
