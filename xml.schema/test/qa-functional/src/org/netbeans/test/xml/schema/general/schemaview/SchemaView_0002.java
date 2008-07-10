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
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.operators.JListOperator;
import java.awt.event.InputEvent;
import org.netbeans.jemmy.operators.JMenuBarOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.jellytools.MainWindowOperator;
import java.awt.event.KeyEvent;
import org.netbeans.test.xml.schema.lib.SchemaMultiView;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.junit.NbModuleSuite;
import junit.framework.Test;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JTextComponentOperator;

/**
 *
 * @author michaelnazarov@netbeans.org
 */

public class SchemaView_0002 extends SchemaView {
    
    static final String TEST_JAVA_APP_NAME = "java4schemaview_0002";

    static final String SCHEMA_SHORT_NAME_1 = "newXmlSchema1";
    static final String SCHEMA_SHORT_NAME_2 = "newXmlSchema2";
    static final String SCHEMA_SHORT_NAME_3 = "newXmlSchema3";
    static final String SCHEMA_SHORT_NAME_4 = "newXmlSchema4";
    static final String SCHEMA_SHORT_NAME_5 = "newXmlSchema5";
    static final String SCHEMA_SHORT_NAME_6 = "newXmlSchema6";
    static final String SCHEMA_SHORT_NAME_7 = "newXmlSchema7";
    static final String SCHEMA_SHORT_NAME_8 = "newXmlSchema8";
    static final String SCHEMA_SHORT_NAME_9 = "newXmlSchema9";

    static final String SCHEMA_EXTENSION = ".xsd";

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

    public SchemaView_0002(String arg0) {
        super(arg0);
    }

    public static Test suite( )
    {
      return NbModuleSuite.create(
          NbModuleSuite.createConfiguration( SchemaView_0002.class ).addTest(
              "CreateJavaApplication",
              "AddSchema",
              "InvokeSearch",
              "SearchForComponentName",
              "NavigateResults",
              "SearchComponentKind",
              "SearchAttributeValue",
              "SearchSelected",
              "SearchNonExistent",
              "AdvancedSearch",
              "FindUsages"
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

    public void AddSchema( )
    {
      startTest( );

      AddLoanApplicationSchemaInternal(
          TEST_JAVA_APP_NAME,
          TEST_JAVA_APP_NAME
        );

      endTest( );
    }

  protected boolean CheckFindBar( TopComponentOperator top, boolean bPresent )
  {
    String[] asButtons = { "Find Next", "Find Previous", "Clear" };
    System.out.println( "+++ Enter." );
    for( String s : asButtons )
    {
      try
      {
        System.out.println( "+++ Checking: " + s );
        JButtonOperator but = new JButtonOperator( top, s );
        System.out.println( "+++ Present." );
        if( !bPresent )
        {
          System.out.println( "+++ Should not be present." );
          return false;
        }
      }
      catch( JemmyException ex )
      {
        System.out.println( "+++ Not present." );
        if( bPresent )
        {
          System.out.println( "+++ Should be resent." );
          return false;
        }
      }
    }
    System.out.println( "+++ Done." );
    return true;
  }

  public void InvokeSearch( )
  {
    startTest( );

    TopComponentOperator top = new TopComponentOperator( SAMPLE_SCHEMA_NAME );
    // Check there is no find bar
    if( !CheckFindBar( top, false ) )
      fail( "First find check failed." );
    // Invioke menu
    new JMenuBarOperator(MainWindowOperator.getDefault()).pushMenu("Edit|Find...");
    // Check there is find bar
    if( !CheckFindBar( top, true ) )
      fail( "Second find check failed." );
    // Press Escape
    top.pushKey( KeyEvent.VK_ESCAPE );
    Sleep( 1000 );
    // Check there is no find bar
    if( !CheckFindBar( top, false ) )
      fail( "Third find check failed." );
    // Preff Ctrl+F
    top.pushKey( KeyEvent.VK_F, InputEvent.CTRL_MASK );
    // Check there is find bar
    if( !CheckFindBar( top, true ) )
      fail( "Forth find check failed." );
    // Press Escape
    top.pushKey( KeyEvent.VK_ESCAPE );
    Sleep( 1000 );
    // Check there is no find bar
    if( !CheckFindBar( top, false ) )
      fail( "Fifth find check failed." );

    endTest( );
  }

  public void SearchForComponentName( )
  {
    startTest( );

    new JMenuBarOperator(MainWindowOperator.getDefault()).pushMenu("Edit|Find...");
    TopComponentOperator top = new TopComponentOperator( SAMPLE_SCHEMA_NAME );
    JTextComponentOperator text = new JTextComponentOperator( top, 0 );
    text.setText( "Address" );
    text.pushKey( KeyEvent.VK_ENTER );

    // Check label
    JLabelOperator label = new JLabelOperator( top, "Found 6 occurrences." );

    // Check view
    SchemaMultiView xml = new SchemaMultiView( SAMPLE_SCHEMA_NAME );
    JListOperator list = xml.getColumnListOperator( 3 );
    String sSelected = list.getSelectedValue( ).toString( );
    if( !sSelected.equals( "emailAddress [Local Element]" ) )
        fail( "Wrong line selected from find: \"" + sSelected + "\"" );

    xml.switchToSchemaTree( );

    new JMenuBarOperator(MainWindowOperator.getDefault()).pushMenu("Edit|Find...");
    top = new TopComponentOperator( SAMPLE_SCHEMA_NAME );
    text = new JTextComponentOperator( top, 0 );
    text.setText( "Address" );
    text.pushKey( KeyEvent.VK_ENTER );
    label = new JLabelOperator( top, "Found 6 occurrences." );

    top = new TopComponentOperator( SAMPLE_SCHEMA_NAME );
    JTreeOperator tree = new JTreeOperator( top, 0 );
    TreePath path = tree.getSelectionPath( );
    Object[] oo = path.getPath( );
    String[] asIdealPath =
    {
      "http://xml.netbeans.org/examples/LoanApplication [Schema]",
      "Complex Types",
      "ApplicantType [Global Complex Type]",
      "sequence [Sequence]",
      "emailAddress [Local Element]"
    };
    if( oo.length != asIdealPath.length )
      fail( "Incorrect path selected." );
    for( int i = 0; i < oo.length; i++ )
    {
      if( !asIdealPath[ i ].equals( oo[ i ].toString( ) ) )
        fail( "Invalid path component, expected: \"" + asIdealPath[ i ] + "\", found: \"" + oo[ i ] + "\"" );
    }

    endTest( );
  }

  public void NavigateResults( )
  {
    startTest( );

    // Tree
    TopComponentOperator top = new TopComponentOperator( SAMPLE_SCHEMA_NAME );
    JTreeOperator tree = new JTreeOperator( top, 0 );
    JButtonOperator prev = new JButtonOperator( top, "Find Previous" );
    JButtonOperator next = new JButtonOperator( top, "Find Next" );

    String[] asIdealSelection =
    {
      "http://xml.netbeans.org/examples/LoanApplication [Schema]|Complex Types|ApplicantType [Global Complex Type]|sequence [Sequence]|emailAddress [Local Element]",
      "http://xml.netbeans.org/examples/LoanApplication [Schema]|Complex Types|ApplicantType [Global Complex Type]|sequence [Sequence]|employment [Local Element]|complexType [Local Complex Type]|sequence [Sequence]|detail [Local Element]|complexType [Local Complex Type]|sequence [Sequence]|employer [Local Element]|complexType [Local Complex Type]|sequence [Sequence]|address [Local Element]",
      "http://xml.netbeans.org/examples/LoanApplication [Schema]|Complex Types|AddressType [Global Complex Type]",
      "http://xml.netbeans.org/examples/LoanApplication [Schema]|Complex Types|AddressType [Global Complex Type]|sequence [Sequence]|address1 [Local Element]",
      "http://xml.netbeans.org/examples/LoanApplication [Schema]|Complex Types|AddressType [Global Complex Type]|sequence [Sequence]|address2 [Local Element]",
      "http://xml.netbeans.org/examples/LoanApplication [Schema]|Complex Types|ResidenceType [Global Complex Type]|sequence [Sequence]|address [Local Element]"
    };

    int i;
    for( i = 0; i < 10; i++ )
    {
      TreePath path = tree.getSelectionPath( );
      Object[] oo = path.getPath( );
      String[] asIdeal = asIdealSelection[ i % asIdealSelection.length ].split( "[|]" );
      if( oo.length != asIdeal.length )
        fail( "Selected path doesn't match ideal one, #" + i );
      for( int j = 0; j < oo.length; j++ )
        if( !asIdeal[ j ].equals( oo[ j ].toString( ) ) )
          fail( "Selected path doesn't match ideal one, #" + i + ", ##" + j );
      next.push( );
    }
    for( ; i >= 0; i-- )
    {
      TreePath path = tree.getSelectionPath( );
      Object[] oo = path.getPath( );
      String[] asIdeal = asIdealSelection[ i % asIdealSelection.length ].split( "[|]" );
      if( oo.length != asIdeal.length )
        fail( "Selected path doesn't match ideal one, #" + i );
      for( int j = 0; j < oo.length; j++ )
        if( !asIdeal[ j ].equals( oo[ j ].toString( ) ) )
          fail( "Selected path doesn't match ideal one, #" + i + ", ##" + j );
      prev.push( );
    }

    // Columns
    SchemaMultiView xml = new SchemaMultiView( SAMPLE_SCHEMA_NAME );
    xml.switchToSchemaColumns( );

    new JMenuBarOperator(MainWindowOperator.getDefault()).pushMenu("Edit|Find...");
    top = new TopComponentOperator( SAMPLE_SCHEMA_NAME );
    JTextComponentOperator text = new JTextComponentOperator( top, 0 );
    text.setText( "Address" );
    text.pushKey( KeyEvent.VK_ENTER );

    prev = new JButtonOperator( top, "Find Previous" );
    next = new JButtonOperator( top, "Find Next" );

    String[] asIdealColumns =
    {
      "3|emailAddress [Local Element]",
      "12|address [Local Element]",
      "1|AddressType [Global Complex Type]",
      "3|address1 [Local Element]",
      "3|address2 [Local Element]",
      "3|address [Local Element]"
    };
    for( i = 0; i < 10; i++ )
    {
      String[] asPath = asIdealColumns[ i % asIdealColumns.length ].split( "[|]" );
      int iIndex = Integer.parseInt( asPath[ 0 ] );
      JListOperator list = xml.getColumnListOperator( iIndex );
      if( null == list )
        fail( "No such list index." );
      Object o = list.getSelectedValue( );
      if( null == o )
        fail( "No selected element, " + asPath[ 1 ] );
      if( !asPath[ 1 ].equals( o.toString( ) ) )
        fail( "Invalid selection, expected: \"" + asPath[ 1 ] + "\", found: \"" + o.toString( ) + "\"" );
      next.push( );
      Sleep( 1000 );
    }
    for( ; i >= 0; i-- )
    {
      String[] asPath = asIdealColumns[ i % asIdealColumns.length ].split( "[|]" );
      int iIndex = Integer.parseInt( asPath[ 0 ] );
      JListOperator list = xml.getColumnListOperator( iIndex );
      if( null == list )
        fail( "No such list index." );
      Object o = list.getSelectedValue( );
      if( !asPath[ 1 ].equals( o.toString( ) ) )
        fail( "Invalid selection, expected: \"" + asPath[ 1 ] + "\", found: \"" + o.toString( ) + "\"" );
      prev.push( );
    }

    endTest( );
  }

  public void SearchComponentKind( )
  {
    startTest( );

    endTest( );
  }

  public void SearchAttributeValue( )
  {
    startTest( );

    endTest( );
  }

  public void SearchSelected( )
  {
    startTest( );

    endTest( );
  }

  public void SearchNonExistent( )
  {
    startTest( );

    endTest( );
  }
  
  public void AdvancedSearch( )
  {
    startTest( );

    endTest( );
  }

  public void FindUsages( )
  {
    startTest( );

    endTest( );
  }
}
