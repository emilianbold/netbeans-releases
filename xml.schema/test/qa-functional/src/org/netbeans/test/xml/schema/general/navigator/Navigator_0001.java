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

package org.netbeans.test.xml.schema.general.navigator;

import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.jellytools.MainWindowOperator;
import java.awt.event.KeyEvent;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.test.xml.schema.lib.SchemaMultiView;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.junit.NbModuleSuite;
import junit.framework.Test;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JTextComponentOperator;
import org.netbeans.jemmy.operators.JToggleButtonOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.ProjectRootNode;

/**
 *
 * @author michaelnazarov@netbeans.org
 */

public class Navigator_0001 extends Navigator {
    
    static final String TEST_JAVA_APP_NAME = "java4navigator_0001";
    static final String SCHEMA_NAME_LA = "newLoanApplication.xsd";
    static final String SCHEMA_SHORT_NAME = "newXmlSchema";
    static final String SCHEMA_NAME = SCHEMA_SHORT_NAME + ".xsd";

    static final String TEST_BPEL_APP_NAME = "TravelReservationService";
    static final String OTA_SCHEMA_NAME = "OTA_TravelItinerary.xsd";

    static final String [] m_aTestMethods = {
        "CreateJavaApplication",
        "CreateSchema1",
        "NavigateGoTo",
        "Attributes",
        "CreateSchema2",
        "Content"
    };

    public Navigator_0001(String arg0) {
        super(arg0);
    }

    /*    
    public static TestSuite suite() {
        TestSuite testSuite = new TestSuite(Navigator_0001.class.getName());
        
        for (String strMethodName : m_aTestMethods) {
            testSuite.addTest(new Navigator_0001(strMethodName));
        }
        
        return testSuite;
    }
    */

    public static Test suite( )
    {
      return NbModuleSuite.create(
          NbModuleSuite.createConfiguration( Navigator_0001.class ).addTest(
              "CreateJavaApplication",
              "CreateSchema1",
              "NavigateGoTo",
              "Attributes",
              "CreateSchema2",
              "Content"
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

      AddLoanApplicationSchemaInternal(
          TEST_JAVA_APP_NAME,
          TEST_JAVA_APP_NAME
        );

      endTest( );
    }

    public void NavigateGoTo( )
    {
      startTest( );

      TopComponentOperator top = new TopComponentOperator( " - Navigator" );
      JComboBoxOperator jcb = new JComboBoxOperator( top );
      String sComboSelection = jcb.getSelectedItem( ).toString( );
      if( !sComboSelection.equals( "Design View" ) )
        fail( "Invalid initial combo seletion: \"" + sComboSelection + "\"" );

      JTreeOperator tree = new JTreeOperator( top, 0 );
      Node node = new Node( tree, "autoLoanApplication" );
      node.performPopupAction( "Go To|Source" );

      EditorOperator code = new EditorOperator( SCHEMA_NAME_LA );
      String sSelected = code.getText( code.getLineNumber( ) );
      if( -1 == sSelected.indexOf( "<xs:element name=\"autoLoanApplication\">" ) )
        fail( "Wrong line selected from navigator to source: \"" + sSelected + "\"" );

      node.performPopupAction( "Go To|Schema" );

      SchemaMultiView xml = new SchemaMultiView( SCHEMA_NAME_LA );
      JListOperator list = xml.getColumnListOperator( 1 );
      sSelected = list.getSelectedValue( ).toString( );
      if( !sSelected.equals( "autoLoanApplication [Global Element]" ) )
        fail( "Wrong line selected from navigator to schema: \"" + sSelected + "\"" );

      node.performPopupAction( "Go To|Design" );
      TopComponentOperator design = new TopComponentOperator( SCHEMA_NAME_LA );
      top.pushKey( KeyEvent.VK_F2 );
      try { Thread.sleep( 500 ); } catch( InterruptedException ex ) { }

      // Get text area
      JTextComponentOperator jtText = new JTextComponentOperator( MainWindowOperator.getDefault( ), 0 );

      // Get text from
      sSelected = jtText.getText( );

      // Escape
      top.pushKey( KeyEvent.VK_ESCAPE );

      if( !sSelected.equals( "autoLoanApplication" ) )
        fail( "Wrong line selected from navigator to design: \"" + sSelected + "\"" );

      endTest( );
    }

    public void Attributes( )
    {
      startTest( );

      TopComponentOperator top = new TopComponentOperator( " - Navigator" );
      JComboBoxOperator jcb = new JComboBoxOperator( top );
      jcb.selectItem( "XML View" );

      // Get toggle button
      JToggleButtonOperator button = new JToggleButtonOperator( top, 0 );

      JTreeOperator tree = new JTreeOperator( top, 0 );
      String sOriginal = Dump( tree, tree.getRoot( ), -1, "" );
      if( !sOriginal.equals( data.sOriginal ) )
      {
        // TODO : check tree
        System.out.println( "+++1:" + data.sOriginal );
        System.out.println( "+++2:" + sOriginal );
        fail( "Invalid orignal data" );//: \"" + sOriginal + "\"" );
      }

      button.setSelected( false );

      String sAttributes = Dump( tree, tree.getRoot( ), -1, "" );
      if( !sAttributes.equals( data.sAttributes ) )
      {
        // TODO : check tree
        System.out.println( "+++1:" + data.sAttributes );
        System.out.println( "+++2:" + sAttributes );
        fail( "Invalid attributes data" );//: \"" + sAttributes + "\"" );
      }

      endTest( );
    }

    public void CreateSchema2( )
    {
      startTest( );

      CreateSchemaInternal(
          TEST_JAVA_APP_NAME + "|Source Packages|" + TEST_JAVA_APP_NAME,
          SCHEMA_SHORT_NAME
        );

      endTest( );
    }

    public void Content( )
    {
      startTest( );

      SchemaMultiView xml = new SchemaMultiView( SCHEMA_NAME );
      xml.switchToSource( );
      EditorOperator code = new EditorOperator( SCHEMA_NAME );
      code.setCaretPosition( "</xsd:schema>", true );
      code.insert( "helloworld" );

      TopComponentOperator top = new TopComponentOperator( " - Navigator" );
      JComboBoxOperator jcb = new JComboBoxOperator( top );
      jcb.selectItem( "XML View" );
      Sleep( 3000 );

      // Get toggle button
      JToggleButtonOperator button = new JToggleButtonOperator( top, 1 );

      JTreeOperator tree = new JTreeOperator( top, 0 );
      String sOriginal = Dump( tree, tree.getRoot( ), -1, "" );
      System.out.println( sOriginal );
      if( !sOriginal.equals( data.sOriginalContent ) )
        fail( "Invalid orignal data: \"" + sOriginal + "\"" );

      button.clickMouse( );

      String sContent = Dump( tree, tree.getRoot( ), -1, "" );
      System.out.println( sContent );
      if( !sContent.equals( data.sTurnedContent ) )
        fail( "Invalid content data: \"" + sContent + "\"" );

      endTest( );
    }

}
