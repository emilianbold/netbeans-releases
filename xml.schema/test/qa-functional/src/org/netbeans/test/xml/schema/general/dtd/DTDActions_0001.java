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

package org.netbeans.test.xml.schema.general.dtd;

import javax.swing.table.TableModel;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.junit.NbModuleSuite;
import junit.framework.Test;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.jemmy.operators.JToggleButtonOperator;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.JMenuBarOperator;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.JTextAreaOperator;

import java.io.*;
import java.net.*;

/**
 *
 * @author michaelnazarov@netbeans.org
 */

public class DTDActions_0001 extends DTDActions {
    
    static final String TEST_JAVA_APP_NAME = "java4dtdactions_0001";

    public DTDActions_0001(String arg0) {
        super(arg0);
    }

    public static Test suite( )
    {
      return NbModuleSuite.create(
          NbModuleSuite.createConfiguration( DTDActions_0001.class ).addTest(
              "CreateJavaApplication",
              "CreateSampleSchema",
              "GenerateConstrained",
              "GenerateDTD",
              "GenerateCSS",
              "CheckDTD",
              "GenerateDocumentation",
              "GenerateDOMTS",
              "GenerateSAXSimple",
              "GenerateSAXDetailed"
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

    public void GenerateConstrained( )
    {
      startTest( );

      /*
      CImportClickData[] aimpData =
      {
        new CImportClickData( true, 0, 0, 2, 3, "Unknown import table state after first click, number of rows: ", null ),
        new CImportClickData( true, 1, 0, 2, 5, "Unknown import table state after second click, number of rows: ", null ),
        new CImportClickData( true, 2, 0, 2, 6, "Unknown import table state after third click, number of rows: ", null ),
        new CImportClickData( true, 3, 0, 2, 7, "Unknown import table state after forth click, number of rows: ", null ),
        new CImportClickData( true, 4, 1, 1, 7, "Unknown to click on checkbox. #", null ),
      };

      CreateConstrainedInternal( TEST_JAVA_APP_NAME, aimpData, "purchaseOrder", 0, 0 );
      */

      ProjectsTabOperator pto = new ProjectsTabOperator( );
      ProjectRootNode prn = pto.getProjectRootNode(
          TEST_JAVA_APP_NAME + "|Source Packages|" + TEST_JAVA_APP_NAME + "|" + "newPurchaseOrder.xsd"
        );
      prn.select( );

      prn.performPopupActionNoBlock( "Generate Sample XML" );

      JDialogOperator jdNew = new JDialogOperator( "Generate Sample XML" );
      JComboBoxOperator combo = new JComboBoxOperator( jdNew, 0 );
      combo.selectItem( "purchaseOrder" );
      JButtonOperator finish = new JButtonOperator( jdNew, "Finish" );
      finish.push( );
      jdNew.waitClosed( );

      prn = pto.getProjectRootNode(
          TEST_JAVA_APP_NAME + "|Source Packages|" + TEST_JAVA_APP_NAME + "|" + "newPurchaseOrder.xml"
        );
      prn.select( );

      endTest( );
    }

    public void GenerateDTD( )
    {
      startTest( );

      SimpleGenerateInternal(
          TEST_JAVA_APP_NAME,
          "newPurchaseOrder.xml",
          "Generate DTD...",
          "dtd"
        );

      endTest( );
    }

    public void GenerateCSS( )
    {
      startTest( );

      SimpleGenerateInternal(
          TEST_JAVA_APP_NAME,
          "newPurchaseOrder.dtd",
          "Generate CSS...",
          "css"
        );

      endTest( );
    }

    public void CheckDTD( )
    {
      startTest( );

      ProjectsTabOperator pto = new ProjectsTabOperator( );
      ProjectRootNode prn = pto.getProjectRootNode(
          TEST_JAVA_APP_NAME + "|Source Packages|" + TEST_JAVA_APP_NAME + "|" + "newPurchaseOrder.dtd"
        );
      prn.select( );

      prn.performPopupActionNoBlock( "Check DTD" );

      String[] asIdeals =
      {
        "DTD checking started.",
        "Checking file:",
        "DTD checking finished."
      };

      CheckOutputLines( "XML check", asIdeals );

      endTest( );
    }

  private ServerSocket sBackServer = null;

  public void SetCustomBrowserPath( )
  {
    startTest( );

    // Test execution
    new JMenuBarOperator(MainWindowOperator.getDefault()).pushMenuNoBlock("Tools|Options");

    JDialogOperator jdOptions = new JDialogOperator( "Options" );
    JButtonOperator jbEdit = new JButtonOperator( jdOptions, "Edit..." );
    jbEdit.pushNoBlock( );
    JDialogOperator jdBrowsers = new JDialogOperator( "Web Browsers" );
    JButtonOperator jbAdd = new JButtonOperator( jdBrowsers, "Add..." );
    jbAdd.push( );
    JTextFieldOperator jtPath = new JTextFieldOperator( jdBrowsers, 1 );
    jtPath.setText( "java" );

    //try{ Dumper.dumpAll( "c:\\aaa.zzz" ); } catch( IOException ex ) { }

    JTextAreaOperator jtArgs = new JTextAreaOperator( jdBrowsers, 0 );

    //try { System.getProperties( ).store( new FileOutputStream( "c:\\prop.txt" ), "COMMENT" ); } catch( IOException ex ) { }

    // Establish server and get port number
    try
    {
    sBackServer = new ServerSocket( 0 );

    jtArgs.setText(
        "-cp \"" + System.getProperty( "java.class.path" ) + "\" org.netbeans.test.xml.schema.general.dtd.DTDActions_0001 {URL} " + sBackServer.getLocalPort( )
      );
    Sleep( 5000 );
    JButtonOperator jbOk = new JButtonOperator( jdBrowsers, "OK" );
    jbOk.push( );
    jdBrowsers.waitClosed( );
    Sleep( 5000 );
    jbOk = new JButtonOperator( jdOptions, "OK" );
    jbOk.push( );
    jdOptions.waitClosed( );
    }
    catch( IOException ex )
    {
      fail( "Exception: " + ex.getMessage( ) );
    }

    endTest( );
  }

  public void GenerateDocumentation( )
  {
    SetCustomBrowserPath( );

    startTest( );

    SimpleGenerateInternal(
        TEST_JAVA_APP_NAME,
        "newPurchaseOrder.dtd",
        "Generate Documentation...",
        "html"
      );

    try
    {
      sBackServer.setSoTimeout( 30000 ); // 30 second wait should be enough
      Socket sBackClient = sBackServer.accept( );
      InputStream is = sBackClient.getInputStream( );
      String sContent = "";
      byte[] b = new byte[ 1024 ];
      int iReaden;
      while( -1 != ( iReaden = is.read( b ) ) )
      {
        sContent = sContent + new String( b, 0, iReaden );
      }
      is.close( );
      sBackClient.close( );
      sBackServer.close( );

      sContent = sContent.replaceAll( "[ \t\r\n]", "" );

      // Check result
      if( !sContent.equals( data.sIdealDocumentation ) )
      {
        System.out.println( ">>1>" + data.sIdealDocumentation + "<<<" );
        System.out.println( ">>2>" + sContent + "<<<" );
        fail( "Invalid documentation created." );
        // ToDo
      }
    }
    catch( IOException ex )
    {
      fail( "Exception: " + ex.getMessage( ) );
    }

    endTest( );

  }

    public void GenerateDOMTS( )
    {
      startTest( );

      SimpleGenerateInternal(
          TEST_JAVA_APP_NAME,
          "newPurchaseOrder.dtd",
          "Generate DOM Tree Scanner...",
          "java"
        );

      endTest( );
    }

    protected void GenerateSAXInternal(
        String sJ,  // JAXP Version
        String sP,  // SAX Parser Version
        boolean bP, // Propagate
        boolean bS, // Save Customized Bindings,
        int iIndex  // name_iIndex.ext
      )
    {
      ProjectsTabOperator pto = new ProjectsTabOperator( );
      ProjectRootNode prn = pto.getProjectRootNode(
          TEST_JAVA_APP_NAME + "|Source Packages|" + TEST_JAVA_APP_NAME + "|" + "newPurchaseOrder.dtd"
        );
      prn.select( );

      prn.performPopupActionNoBlock( "SAX Document Handler Wizard" );

      JDialogOperator jdNew = new JDialogOperator( "SAX Document Handler Wizard" );

      if( null != sJ )
      {
        JComboBoxOperator combo1 = new JComboBoxOperator( jdNew, 0 );
        combo1.selectItem( sJ );
      }

      if( null != sP )
      {
        JComboBoxOperator combo2 = new JComboBoxOperator( jdNew, 1 );
        combo2.selectItem( sP );
      }

      JToggleButtonOperator butProp = new JToggleButtonOperator( jdNew, 0 );
      butProp.setSelected( bP );

      JButtonOperator jbNext = new JButtonOperator( jdNew, "Next" );
      jbNext.push( );

      // Change empty to ignore
      JTableOperator table = new JTableOperator( jdNew, 0 );
      TableModel model = table.getModel( );
      int iSize = model.getRowCount( );
      for( int i = 0; i < iSize; i++ )
      {
        String sText = ( String )model.getValueAt( i, 1 );
        if( sText.equals( "Empty" ) )
        {
          table.clickOnCell( i, 1 );
          JComboBoxOperator combo = new JComboBoxOperator( jdNew, 0 );
          combo.selectItem( "Ignore" );
        }
      }

      jbNext = new JButtonOperator( jdNew, "Next" );
      jbNext.push( );
      jbNext = new JButtonOperator( jdNew, "Next" );
      jbNext.push( );

      JToggleButtonOperator butSave = new JToggleButtonOperator( jdNew, 0 );
      butSave.setSelected( bS );

      JButtonOperator jbFinish = new JButtonOperator( jdNew, "Finish" );
      jbFinish.push( );
      jdNew.waitClosed( );

      // Check generated files

      String[] asNames =
      {
        "NewPurchaseOrderHandler",
        "NewPurchaseOrderHandlerImpl",
        "NewPurchaseOrderParser",
      };

      if( 0 != iIndex )
        for( int i = 0; i < asNames.length; i++ )
          asNames[ i ] = asNames[ i ] + "_" + iIndex;

      for( String sName : asNames )
      {
        prn = pto.getProjectRootNode(
            TEST_JAVA_APP_NAME + "|Source Packages|" + TEST_JAVA_APP_NAME + "|" + sName + ".java"
          );
        prn.select( );
      }
      if( bS )
      {
        String sName = "NewPurchaseOrderSAXBindings";
        if( 0 != iIndex )
          sName = sName + "_" + iIndex;
        sName = sName + ".xml";

        int iLimit = 3;
        boolean bRedo = true;
        while( bRedo )
        {
          try
          {
            prn = pto.getProjectRootNode(
                TEST_JAVA_APP_NAME + "|Source Packages|" + TEST_JAVA_APP_NAME + "|" + sName
              );
            prn.select( );
            bRedo = false;
          }
          catch( TimeoutExpiredException ex )
          {
            if( 0 == iLimit-- )
              throw ex;
          }
        }
      }

    }

    public void GenerateSAXSimple( )
    {
      startTest( );

      GenerateSAXInternal( null, null, false, true, 0 );

      endTest( );
    }

    public void GenerateSAXDetailed( )
    {
      startTest( );

      GenerateSAXInternal( "JAXP 1.0", "SAX 1.0", false, true, 1 );
      GenerateSAXInternal( "JAXP 1.0", "SAX 2.0", false, true, 2 );
      GenerateSAXInternal( "JAXP 1.1", "SAX 1.0", false, true, 3 );
      GenerateSAXInternal( "JAXP 1.1", "SAX 2.0", false, true, 4 );
      GenerateSAXInternal( "JAXP 1.0", "SAX 1.0", true, true, 5 );
      GenerateSAXInternal( "JAXP 1.0", "SAX 2.0", true, true, 6 );
      GenerateSAXInternal( "JAXP 1.1", "SAX 1.0", true, true, 7 );
      GenerateSAXInternal( "JAXP 1.1", "SAX 2.0", true, true, 8 );

      GenerateSAXInternal( "JAXP 1.0", "SAX 1.0", false, false, 9 );
      GenerateSAXInternal( "JAXP 1.0", "SAX 2.0", false, false, 10 );
      GenerateSAXInternal( "JAXP 1.1", "SAX 1.0", false, false, 11 );
      GenerateSAXInternal( "JAXP 1.1", "SAX 2.0", false, false, 12 );
      GenerateSAXInternal( "JAXP 1.0", "SAX 1.0", true, false, 13 );
      GenerateSAXInternal( "JAXP 1.0", "SAX 2.0", true, false, 14 );
      GenerateSAXInternal( "JAXP 1.1", "SAX 1.0", true, false, 15 );
      GenerateSAXInternal( "JAXP 1.1", "SAX 2.0", true, false, 16 );

      endTest( );
    }

  public static void main( String[] args ) throws IOException, URISyntaxException
  {
    // Out own web browser
    // First parameter  :  URL / File in this case
    // Second parameter : callback port

    //System.out.println( "===" + args[ 0 ] );
    /*
    FileWriter fw = new FileWriter( "c:\\aaa.zzz" );
    fw.write( args[ 0 ] );
    fw.flush( );
    fw.close( );
    */

    // Connect to server
    // Get data
    String sContent = "";
    boolean bRedo = true;
    int iRecount = 0;
    while( bRedo )
    {
      try
      {
        InputStream is = new FileInputStream( new File( new URI( args[ 0 ] ) ) );
        byte[] b = new byte[ 1024 ];
        int iReaden;
        sContent = "";
        while( -1 != ( iReaden = is.read( b ) ) )
        {
          sContent = sContent + new String( b, 0, iReaden );
        }
        is.close( );

        bRedo = false;
      }
      catch( IOException ex )
      {
        System.out.println( "Error: " + ex.getMessage( ) + "\n" );
      }
    }
    // Send result back to test
    Socket sSocketBack = new Socket( "127.0.0.1", Integer.parseInt( args[ 1 ] ) );
    OutputStream sStreamBack = sSocketBack.getOutputStream( );
    sStreamBack.write( sContent.getBytes( ) );
    sStreamBack.flush( );
    sStreamBack.close( );

    return;
  }
}
