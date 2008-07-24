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

package org.netbeans.test.xml.schema.general.designview;

import org.netbeans.junit.NbModuleSuite;
import junit.framework.Test;
import org.netbeans.jemmy.operators.AbstractButtonOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.Timeout;
import java.awt.event.InputEvent;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jemmy.ComponentChooser;
import javax.swing.JToggleButton;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jemmy.operators.JTextComponentOperator;
import org.netbeans.jellytools.EditorOperator;
import javax.swing.ListModel;
import java.awt.Point;
import org.netbeans.jemmy.drivers.input.MouseRobotDriver;

/**
 *
 * @author michaelnazarov@netbeans.org
 */

public class DesignView_0001 extends DesignView {
    
    static final String TEST_JAVA_APP_NAME = "java4designview_0001";

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

    public DesignView_0001(String arg0) {
        super(arg0);
    }

    public static Test suite( )
    {
      return NbModuleSuite.create(
          NbModuleSuite.createConfiguration( DesignView_0001.class ).addTest(
              "CreateJavaApplication",
              "CreateXMLSchema1",
              "DoDragAndDrop"
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

    public void CreateXMLSchema1( )
    {
      startTest( );

      CreateSchemaInternal(
          TEST_JAVA_APP_NAME + "|Source Packages|" + TEST_JAVA_APP_NAME,
          SCHEMA_SHORT_NAME_1
        );

      endTest( );
    }

    public class CComponentChooser implements ComponentChooser
    {
      String s;
      public CComponentChooser( String _s )
      {
        super( );
        s = _s;
      }

      public java.lang.String getDescription() { return "looking for happy"; }
      public boolean checkComponent( java.awt.Component comp )
      {
        //System.out.println( "+++" + comp );
        //if( !s.equals( ( ( JToggleButton )comp ).getText( ) ) )
          return false;
        //return true;
      }
    }

  // TODO : add constant for destination
  // TODO : calculate point right way without constants
  // 0 - top, 1 - elements
  protected void DragSomething(
      int iListIndex,
      String sElementName,
      int iDestination,
      String sResult
    )
  {
    TopComponentOperator top = new TopComponentOperator( SCHEMA_NAME_1 );
    TopComponentOperator pal = new TopComponentOperator( "Palette" );
    JListOperator list = new JListOperator( pal, iListIndex );

    ListModel lmd = list.getModel( );
    int iIndex = list.findItemIndex( sElementName );
    list.selectItem( iIndex );
    Point pt = list.getClickPoint( iIndex );

    int[] yy = { 40, 60, 150, 90, 175, 200, 235 };

    MouseRobotDriver m_mouseDriver = new MouseRobotDriver(new Timeout("", 500));
    m_mouseDriver.moveMouse( list, pt.x, pt.y );
    m_mouseDriver.pressMouse( InputEvent.BUTTON1_MASK, 0 );
    m_mouseDriver.enterMouse( top );
    m_mouseDriver.dragMouse( top, 50, yy[ iDestination ], InputEvent.BUTTON1_MASK, 0 );
    m_mouseDriver.releaseMouse( InputEvent.BUTTON1_MASK, 0 );

    Sleep( 1000 );

    if( null != sResult )
    {
      // Check text box
      JTextComponentOperator text = new JTextComponentOperator( MainWindowOperator.getDefault( ), 0 );
      String sText = text.getText( );
      if( !sText.equals( sResult ) )
        fail( "Invalid new element name, expected \"" + sResult + "\", found \"" + sText + "\"" );
    }
  }

  public void DoDragAndDrop( )
  {
    startTest( );

    // Switch to DV
    TopComponentOperator top = new TopComponentOperator( SCHEMA_NAME_1 );
    SwitchToDesignView( top );

    TopComponentOperator pal = new TopComponentOperator( "Palette" );
    AbstractButtonOperator but = new AbstractButtonOperator( pal, "XML Schema Components" );
    but.clickMouse( );
    Sleep( 1000 );

    // Add elements
    DragSomething( 0, "Element", 0, "newElement" );
    DragSomething( 0, "Element", 1, "newElement1" );

    // Add complex types
    DragSomething( 1, "Complex Type", 0, "newComplexType" );
    DragSomething( 1, "Complex Type", 2, "newComplexType1" );

    // Add attributes
    DragSomething( 0, "Attribute", 3, "newAttribute" );
    DragSomething( 0, "Attribute", 4, "newAttribute" );

    // Add more CT
    DragSomething( 1, "Complex Type", 2, "newComplexType2" );

    // Add all
    DragSomething( 1, "All", 4, null );
    DragSomething( 1, "Choice", 5, null );
    DragSomething( 1, "Sequence", 6, null );

    String[] asIdeal =
    {
      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>",
      "<xsd:schema xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"",
      "targetNamespace=\"http://xml.netbeans.org/schema/newXmlSchema1\"",
      "xmlns:tns=\"http://xml.netbeans.org/schema/newXmlSchema1\"",
      "elementFormDefault=\"qualified\">",
      "<xsd:element name=\"newElement\">",
      "<xsd:complexType>",
      "<xsd:attribute name=\"newAttribute\" type=\"xsd:string\"/>",
      "</xsd:complexType>",
      "</xsd:element>",
      "<xsd:element name=\"newElement1\"/>",
      "<xsd:complexType name=\"newComplexType\">",
      "<xsd:all/>",
      "<xsd:attribute name=\"newAttribute\" type=\"xsd:string\"/>",
      "</xsd:complexType>",
      "<xsd:complexType name=\"newComplexType1\">",
      "<xsd:choice/>",
      "</xsd:complexType>",
      "<xsd:complexType name=\"newComplexType2\">",
      "<xsd:sequence/>",
      "</xsd:complexType>",
      "</xsd:schema>"
    };

    SwitchToSourceView( top );
    EditorOperator code = new EditorOperator( SCHEMA_NAME_1 );
    int iLine = 1;
    for( String sIdeal : asIdeal )
    {
      String sLine = code.getText( iLine++ );
      while( sLine.matches( "^[ \t]*\r?\n?$" ) )
        sLine = code.getText( iLine++ );
      if( -1 == sLine.indexOf( sIdeal ) )
        fail( "Invalid code line #" + iLine + ": \"" + sLine + "\", ideal: \"" + sIdeal + "\"" );
    }

    /*
    ComponentOperator jbSel = new ComponentOperator(
        pal,
        new CComponentChooser( "xxx" )
      );
    */
    /*
    for( int i = 0; i < 100; i++ )
    {
    ComponentOperator but = new ComponentOperator(
        pal,
        i
      );
      //System.out.println( "" + i + " : \"" + but.getClass( ).getName( ) + "\"" );
    }
    */
    //jbSel.push( );
    //jbSel.clickMouse( );

    endTest( );
  }
}
