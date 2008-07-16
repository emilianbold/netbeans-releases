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

import java.awt.event.KeyEvent;
import org.netbeans.junit.NbModuleSuite;
import junit.framework.Test;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.operators.AbstractButtonOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.JTextComponentOperator;
import org.netbeans.jellytools.EditorOperator;
import java.awt.event.InputEvent;
import java.awt.Component;

/**
 *
 * @author michaelnazarov@netbeans.org
 */

public class DesignView_0002 extends DesignView {

    static final String TEST_JAVA_APP_NAME = "java4designview_0002";

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

    public DesignView_0002(String arg0) {
        super(arg0);
    }

    public static Test suite( )
    {
      return NbModuleSuite.create(
          NbModuleSuite.createConfiguration( DesignView_0002.class ).addTest(
              "CreateJavaApplication",
              "CreateXMLSchema1",
              "RenameUsingF2",
              "CreateXMLSchema2",
              "RenameUsingSpace",
              "CreateXMLSchema3",
              //"RenameUsingShiftClick"
              "CreateLoanApplicationSchema",
              "NavigateAndFold",
              "CreateXMLSchema4",
              "AddUsingKeyboard"
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

  protected String GetElementName( TopComponentOperator top, String sNewName )
  {
    return GetElementName( top, sNewName, -1 );
  }

  protected String GetElementName( TopComponentOperator top, String sNewName, int iOriginalHeight )
  {
    top.clickMouse( );
    Sleep( 500 );
    top.pushKey( KeyEvent.VK_RIGHT );
    Sleep( 500 );
    top.pushKey( KeyEvent.VK_RIGHT );
    Sleep( 500 );
    top.pushKey( KeyEvent.VK_F2 );
    Sleep( 500 );
    JTextComponentOperator text = new JTextComponentOperator( MainWindowOperator.getDefault( ), 0 );
    int iHeight = text.getHeight( );
    String sPrevious = text.getText( );
    if( null != sNewName )
    {
      text.setText( sNewName );
      text.pushKey( KeyEvent.VK_ENTER );
    }
    else
    {
      top.clickMouse( );
    }
    if( -1 != iOriginalHeight && iHeight != iOriginalHeight )
    {
      System.out.println( "Font size affected, issue #135561" );
      // TODO : fail( "Font size affected, issue #135561" );
    }
    return sPrevious;
  }

  public void RenameUsingF2( )
  {
    startTest( );

    // Switch to DV
    TopComponentOperator top = new TopComponentOperator( SCHEMA_SHORT_NAME_1 + SCHEMA_EXTENSION );
    SwitchToDesignView( top );

    // Add element
    // TODO : use DnD
    top.clickForPopup( );
    JPopupMenuOperator popup = new JPopupMenuOperator( );
    popup.pushMenu( "Add|Element" );
    JTextComponentOperator text = new JTextComponentOperator( MainWindowOperator.getDefault( ), 0 );
    int iOriginalHeight = text.getHeight( );
    top.clickMouse( );
    Sleep( 1500 );
    try
    {
      text = new JTextComponentOperator( MainWindowOperator.getDefault( ), 0 );
      if( !text.getText( ).startsWith( "Search " ) )
        fail( "Text component should gone from DesignView." );
    }
    catch( JemmyException ex )
    {
      // It's okey
    }
    String sOriginal = GetElementName( top, "renamedElement" );
    if( !sOriginal.equals( "newElement" ) )
      fail( "Unknown Element opened for renaming: \"" + sOriginal + "\"" );
    String sRenamed = GetElementName( top, null );
    if( !sRenamed.equals( "renamedElement" ) )
      fail( "Unknown Element opened after renaming: \"" + sRenamed + "\"" );

    // Undo from keyboard
    top.pushKey( KeyEvent.VK_Z, InputEvent.CTRL_MASK );
    Sleep( 1500 );

    // Check original name
    String sUndo = GetElementName( top, null );
    if( !sUndo.equals( "newElement" ) )
      fail( "Element failed to undo rename with Ctrl+Z: \"" + sUndo + "\"" );

    // Redo from keyboard
    top.pushKey( KeyEvent.VK_Y, InputEvent.CTRL_MASK );
    Sleep( 1500 );

    String sRedo = GetElementName( top, null );
    if( !sRedo.equals( "renamedElement" ) )
      fail( "Element failed to redo rename with Ctrl+Y: \"" + sRedo + "\"" );

    // Undo from menu
    new JMenuBarOperator(MainWindowOperator.getDefault()).pushMenu("Edit|Undo");
    Sleep( 1500 );

    // Check original name
    sUndo = GetElementName( top, null );
    if( !sUndo.equals( "newElement" ) )
      fail( "Element failed to undo rename with menu: \"" + sUndo + "\"" );

    // Redo from menu
    CallUnchangedSubmenu( "Edit", "Redo" );
    Sleep( 1500 );

    sRedo = GetElementName( top, null, iOriginalHeight );
    if( !sRedo.equals( "renamedElement" ) )
      fail( "Element failed to redo rename with menu: \"" + sRedo + "\"" );

    // TODO add checking for font size increasing

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

  public void RenameUsingSpace( )
  {
    startTest( );

    // Switch to DV
    TopComponentOperator top = new TopComponentOperator( SCHEMA_SHORT_NAME_2 + SCHEMA_EXTENSION );
    SwitchToDesignView( top );

    // Add element
    // TODO : use DnD
    top.clickForPopup( );
    JPopupMenuOperator popup = new JPopupMenuOperator( );
    popup.pushMenu( "Add|Element" );
    JTextComponentOperator text = new JTextComponentOperator( MainWindowOperator.getDefault( ), 0 );
    top.clickMouse( );//pushKey( KeyEvent.VK_ENTER );
    Sleep( 500 );
    try
    {
      text = new JTextComponentOperator( MainWindowOperator.getDefault( ), 0 );
      if( !text.getText( ).startsWith( "Search " ) )
        fail( "Text component should gone from DesignView." );
    }
    catch( JemmyException ex )
    {
      // It's okey
    }
    top.clickMouse( );
    Sleep( 500 );
    top.pushKey( KeyEvent.VK_RIGHT );
    Sleep( 500 );
    top.pushKey( KeyEvent.VK_RIGHT );
    Sleep( 500 );
    top.typeKey( ' ' );//pushKey( KeyEvent.VK_SPACE );
    Sleep( 500 );
    text = new JTextComponentOperator( MainWindowOperator.getDefault( ), 0 );
    String sText = text.getText( );
    top.clickMouse( );//pushKey( KeyEvent.VK_ENTER );
    if( !sText.equals( "newElement" ) )
      fail( "Unknown Element opened for renaming: \"" + sText + "\"" );

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

  public void RenameUsingShiftClick( )
  {
    startTest( );

    // Switch to DV
    TopComponentOperator top = new TopComponentOperator( SCHEMA_SHORT_NAME_3 + SCHEMA_EXTENSION );
    SwitchToDesignView( top );

    // Add element
    // TODO : use DnD
    top.clickForPopup( );
    JPopupMenuOperator popup = new JPopupMenuOperator( );
    popup.pushMenu( "Add|Element" );
    JTextComponentOperator text = new JTextComponentOperator( MainWindowOperator.getDefault( ), 0 );
    // Store text rect
      // ToDo

    int x = text.getX( );
    int y = text.getY( );
    top.clickMouse( );//pushKey( KeyEvent.VK_ENTER );
    Sleep( 1500 );
    try
    {
      text = new JTextComponentOperator( MainWindowOperator.getDefault( ), 0 );
      if( !text.getText( ).startsWith( "Search " ) )
        fail( "Text component should gone from DesignView." );
    }
    catch( JemmyException ex )
    {
      // It's okey
    }

    top./*MainWindowOperator.getDefault( ).*/clickMouse(
        46,
        88,
        1,
        0,
        InputEvent.SHIFT_MASK
      );

    text = new JTextComponentOperator( MainWindowOperator.getDefault( ), 1 );
    String sText = text.getText( );
    top.clickMouse( );//pushKey( KeyEvent.VK_ENTER );
    if( !sText.equals( "newElement" ) )
      fail( "Unknown Element opened for renaming: \"" + sText + "\"" );

    endTest( );
  }

  public void CreateLoanApplicationSchema( )
  {
    startTest( );

    AddLoanApplicationSchemaInternal(
        TEST_JAVA_APP_NAME,
        TEST_JAVA_APP_NAME
      );

    endTest( );
  }

  private void Right( TopComponentOperator top, int iCount )
  {
    for( int i = 0; i < iCount; i++ )
    {
      top.pushKey( KeyEvent.VK_RIGHT );
      Sleep( 500 );
    }
  }

  private void Left( TopComponentOperator top, int iCount )
  {
    for( int i = 0; i < iCount; i++ )
    {
      top.pushKey( KeyEvent.VK_LEFT );
      Sleep( 500 );
    }
  }

  public void NavigateAndFold( )
  {
    startTest( );

    // Switch to DV
    TopComponentOperator top = new TopComponentOperator( SAMPLE_SCHEMA_NAME );
    SwitchToDesignView( top );

    Right( top, 31 );

    new TopComponentOperator( "areaCode [Element] - Navigator" );
    new TopComponentOperator( SAMPLE_SCHEMA_NAME );

    Right( top, 26 );
    Left( top, 17 );

    new TopComponentOperator( "autoLoanApplication [Element] - Navigator" );

    endTest( );
  }

    public void CreateXMLSchema4( )
    {
      startTest( );

      CreateSchemaInternal(
          TEST_JAVA_APP_NAME + "|Source Packages|" + TEST_JAVA_APP_NAME,
          SCHEMA_SHORT_NAME_4
        );

      endTest( );
    }

  public void AddUsingKeyboard( )
  {
    startTest( );

    // Switch to DV
    TopComponentOperator top = new TopComponentOperator( SCHEMA_NAME_4 );
    SwitchToDesignView( top );

    top.clickForPopup( );
    JPopupMenuOperator popup = new JPopupMenuOperator( );
    popup.pushMenu( "Add|Element" );
    JTextComponentOperator text = new JTextComponentOperator( MainWindowOperator.getDefault( ), 0 );
    top.clickMouse( );//pushKey( KeyEvent.VK_ENTER );
    Sleep( 500 );

    Right( top, 2 );

    top.typeKey( 'E' );//pushKey( KeyEvent.VK_E, InputEvent.SHIFT_MASK );
    top.clickMouse( );
    Right( top, 4 );
    top.typeKey( 'A' );//pushKey( KeyEvent.VK_A, InputEvent.SHIFT_MASK );
    top.clickMouse( );

    SwitchToSourceView( top );

    EditorOperator code = new EditorOperator( SCHEMA_NAME_4 );
    String sText = code.getText( );
    if( -1 == sText.indexOf( "<xsd:attribute name=\"newAttribute\" type=\"xsd:string\"/>" ) )
      fail( "Added element and attribute were not found in source code." );

    endTest( );
  }
}
