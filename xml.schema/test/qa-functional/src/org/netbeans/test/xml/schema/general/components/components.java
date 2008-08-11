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

package org.netbeans.test.xml.schema.general.components;

import java.awt.Point;
import javax.swing.tree.TreePath;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import javax.swing.JToggleButton;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jellytools.properties.PropertySheetOperator;
import org.netbeans.jellytools.properties.Property;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.test.xml.schema.lib.SchemaMultiView;
import org.netbeans.test.xml.schema.general.GeneralXMLTest;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;
import java.util.Enumeration;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.io.IOException;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JToggleButtonOperator;

/**
 *
 * @author michaelnazarov@netbeans.org
 */

public class components extends GeneralXMLTest {

  protected final String TEST_JAVA_APP_NAME = "XSDTestProject";
  protected final String TEST_SCHEMA_NAME = "Synt01.xsd";
    
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
        if( !s.equals( ( ( JToggleButton )comp ).getText( ) ) )
          return false;
        return true;
      }
    }

    public components( String arg0 )
    {
      super( arg0 );
    }

    private static boolean bUnzipped = false;

    public void setUp( )
    {
      if( !bUnzipped )
      {
      try
      {
        String sBase = GetWorkDir( );//System.getProperty( "nbjunit.workdir" ) + File.separator + ".." + File.separator + "data" + File.separator;
        // Extract zip data
        ZipFile zf = new ZipFile( sBase + "projects.zip" );
        Enumeration<? extends ZipEntry> ent = zf.entries( );
        while( ent.hasMoreElements( ) )
        {
          ZipEntry e = ent.nextElement( );
          String name = e.getName( );
          if( e.isDirectory( ) )
          {
            ( new File( sBase + name ) ).mkdirs( );
          }
          else
          {
            InputStream is = zf.getInputStream( e );
            //File f = new File( name );
            //System.out.println( "-->" + f.getPath( ) );
            OutputStream os = new FileOutputStream( sBase + name );
            int r;
            byte[] b = new byte[ 1024 ];
            while( -1 != ( r = is.read( b ) ) )
              os.write( b, 0, r );
            is.close( );
            os.flush( );
            os.close( );
          }
        }
        zf.close( );

        // Open project
        openDataProjects( "XSDTestProject" );

        bUnzipped = true;
      }
      catch( IOException ex )
      {
        System.out.println( "ERROR: Unzipping projects.zip failed: " + ex.getMessage( ) );
      }
      }
    }

    public void CheckingProperty(
        String sName,
        String[] asValues,
        boolean bCorrect
      )
    {
      PropertySheetOperator prop = new PropertySheetOperator( );
      Property p = new Property( prop, sName );

      for( String sOriginalValue : asValues )
      {
        String[] asSplitted = sOriginalValue.split( "\\|" );
        String sValue = asSplitted[ 0 ];
        String sResult = asSplitted[ asSplitted.length - 1 ];
        p.setValue( sValue );
        if( !bCorrect )
        {
          // Check warning dialog
          // Failed to show sometimes
          JDialogOperator jdInfo = null;
          try
          {
            jdInfo = new JDialogOperator( "Information" );
          }
          catch( JemmyException ex )
          {
            System.out.println( "Warnig information was not showed!" );
          }
          if( null != jdInfo )
          {
            JLabelOperator jlLabel = new JLabelOperator( jdInfo, 0 );
            String sText = jlLabel.getText( );

            // MacOS workaround
            if( null == sText )
            {
              jlLabel = new JLabelOperator( jdInfo, 1 );
              if( null == ( sText = jlLabel.getText( ) ) )
                fail( "No label text even for MacOS." );
            }

            if(
                !sText.equals( "Not a legal value: " + sValue )
                && !sText.equals( "Enter a positive integer, \"unbounded\", or *." )
              )
            {
              fail( "Unknown text on warning dialog: \"" + sText + "\"" );
            }
            JButtonOperator jbOk = new JButtonOperator( jdInfo, "OK" );
            jbOk.push( );
            jdInfo.waitClosed( );
          }
        }
        String s = p.getValue( );
        if( s.equals( sResult ) ^ bCorrect )
          fail( "Unable to set/fail value: \"" + sOriginalValue + "\"" );
      }
    }

  protected boolean FindInArray( String s, String[] ss )
  {
    for( String sss : ss )
      if( sss.equals( s ) )
        return true;
    return false;
  }

  protected boolean CompareSophisticated( String a, String b )
  {
    String[] aa = a.split( " " );
    String[] bb = b.split( " " );
    if( aa.length != bb.length )
      return false;
    for( int i = 0; i < aa.length; i++ )
    {
      if( !FindInArray( aa[ i ], bb ) )
        return false;
      if( !FindInArray( bb[ i ], aa ) )
        return false;
    }
    return true;
  }

  public void CheckPropertiesInternal( String[] asProps )
  {
    PropertySheetOperator prop = PropertySheetOperator.invoke( );

    for( String sProp : asProps )
    {
      String[] asPair = sProp.split( "\\|" );
      Property p = new Property( prop, asPair[ 0 ] );
      if( null == p )
      {
        for( int i = 0; ; i++ )
        {
          Property pp = new Property( prop, i );
          if( null == pp )
            fail( "No sich property: " + sProp );
          System.out.println( "***" + getName( ) );
        }
      }
      String sValue = p.getValue( );
      if( null == sValue )
        sValue = "";
      if( !sValue.equals( asPair[ 1 ] ) )
      {
        if( !CompareSophisticated( sValue, asPair[ 1 ] ) )
        fail( "Invalid property value. Name: \"" + asPair[ 0 ] + "\", value: \"" + p.getValue( ) + "\", required: \"" + asPair[ 1 ] + "\"." );
      }
    }
  }

    public void OpenSchemaInternal( String sPathInSchema )
    {
      // Get schema path
      ProjectsTabOperator pto = new ProjectsTabOperator( );
      ProjectRootNode prn = pto.getProjectRootNode(
          TEST_JAVA_APP_NAME + "|Source Packages|qa.xmltools.samples|" + TEST_SCHEMA_NAME
        );
      prn.select( );

      // Open
      prn.performPopupAction( "Open" );

      // Switch to tree view
      SchemaMultiView opMultiView = new SchemaMultiView( TEST_SCHEMA_NAME );
      opMultiView.switchToSchemaTree( );

      // Top component
      TopComponentOperator top = new TopComponentOperator( TEST_SCHEMA_NAME );
      JTreeOperator tree = new JTreeOperator( top, 0 );
      TreePath tp = tree.findPath( sPathInSchema, new CStartsStringComparator( ) );
      Node nd = new Node( tree, tp );
      nd.select( );
    }

    public void CheckSourceInternal(
        String sPathInSchema,
        String sMenu,
        String sIdealCode
      )
    {
      // Switch to tree view
      SchemaMultiView opMultiView = new SchemaMultiView( TEST_SCHEMA_NAME );
      //opMultiView.switchToSchemaTree( );

      // Top component
      TopComponentOperator top = new TopComponentOperator( TEST_SCHEMA_NAME );
      JTreeOperator tree = new JTreeOperator( top, 0 );
      TreePath tp = tree.findPath( sPathInSchema, new CStartsStringComparator( ) );
      Node nd = new Node( tree, tp );
      nd.select( );

      // Goto
      nd.performPopupAction( sMenu );

      // Check source
      EditorOperator eoCode = new EditorOperator( TEST_SCHEMA_NAME );
      String sChoosenLine = eoCode.getText( eoCode.getLineNumber( ) );
      if( -1 == sChoosenLine.toLowerCase( ).indexOf( sIdealCode.toLowerCase( ) ) )
        fail( "Incorect source code: \"" + sChoosenLine + "\", required: \"" + sIdealCode + "\"." );

      eoCode.close( false );
    }

    protected void CheckClicked(
        String sDialog,
        String sRadio,
        int row,
        int col
      )
    {
      // Click
      PropertySheetOperator prop = new PropertySheetOperator( );
      JTableOperator tbl = prop.tblSheet( );
      //tbl.clickOnCell( row, col );
      Point pt = tbl.getPointToClick( row, col );
      tbl.pressMouse( pt.x, pt.y );
      // Handle dialog
      JDialogOperator jdGE = new JDialogOperator( sDialog );
      String[] asNames = sRadio.split( "\\|" );
      for( String sName : asNames )
      {
        JToggleButtonOperator jbSel = new JToggleButtonOperator(
            jdGE,
            new CComponentChooser( sName )
          );
        if( !jbSel.isSelected( ) )
        {
          fail( "Wrong radio button [not] selected/checked for Prohibited Derivations (Final): " + sName );
        }
      }
      jdGE.close( );
      jdGE.waitClosed( );
      tbl.releaseMouse( );
    }

    protected void Click( String s )
    {
      TopComponentOperator top = new TopComponentOperator( TEST_SCHEMA_NAME );
      JTreeOperator tree = new JTreeOperator( top, 0 );
      TreePath tp = tree.findPath( s, new CStartsStringComparator( ) );
      Node nd = new Node( tree, tp );
      nd.select( );
    }

    protected void CheckProperty(
        String[] asReplaceData
      )
    {
      SchemaMultiView mvSchema = new SchemaMultiView( TEST_SCHEMA_NAME );
      for( String sReplaceData : asReplaceData )
      {
        String[] asInternal = sReplaceData.split( "~" );

        // Correct source
        mvSchema.switchToSource( );

        EditorOperator eoCode = new EditorOperator( TEST_SCHEMA_NAME );
        eoCode.replace( asInternal[ 0 ], asInternal[ 1 ] );

        // Check in tree
        mvSchema.switchToSchema( );
        Click( asInternal[ 2 ] );
        String[] asCheckName = { asInternal[ 3 ] };
        CheckPropertiesInternal( asCheckName );
      }
    }

    public void CheckBooleanProperty(
        String sType,
        String sName,
        String sVariableInCode,
        String sVariableInSheet,
        String sPath
      )
    {
      String[] data =
      {
        "<xsd:" + sType + " name=\"" + sName + "\">~<xsd:" + sType + " name=\"" + sName + "\" " + sVariableInCode + "=\"true\">~" + sPath + "~" + sVariableInSheet + "|True",
        //" " + sVariableInCode + "=\"true\"~ " + sVariableInCode + "=\"true12345\"~" + sPath + "~" + sVariableInSheet + "|True12345",
        " " + sVariableInCode + "=\"true\"~ " + sVariableInCode + "=\"false\"~" + sPath + "~" + sVariableInSheet + "|False",
        //" " + sVariableInCode + "=\"true12345\"~ " + sVariableInCode + "=\"false\"~" + sPath + "~" + sVariableInSheet + "|False",
        " " + sVariableInCode + "=\"false\"~~" + sPath + "~" + sVariableInSheet + "|False (not set)",
      };

      CheckProperty( data );
    }
}
