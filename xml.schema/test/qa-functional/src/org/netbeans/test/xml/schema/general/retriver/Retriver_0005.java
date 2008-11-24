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

package org.netbeans.test.xml.schema.general.retriver;

import javax.swing.tree.TreePath;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jellytools.OutputOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;
import org.netbeans.jemmy.operators.JFileChooserOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import java.io.File;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.FilesTabOperator;
import org.netbeans.junit.NbModuleSuite;
import junit.framework.Test;

/**
 *
 * @author michaelnazarov@netbeans.org
 */

public class Retriver_0005 extends Retriver {
    
    static final String TEST_BPEL_APP_NAME = "TravelReservationService_retriver_0005";
    static final String TEST_BPEL_MODULE_NAME = "BpelModule_retriver_0005";
    static final String SCHEMA_NAME = "OTA_TravelItinerary.xsd";

    public Retriver_0005(String arg0) {
        super(arg0);
    }

    public static Test suite( )
    {
      return NbModuleSuite.create(
          NbModuleSuite.createConfiguration( Retriver_0005.class ).addTest(
              "CreateBPELs",
              "CreateSchema"
           )
           .enableModules( ".*" )
           .clusters( ".*" )
           //.gui( true )
        );
    }

    public void CreateBPELs( )
    {
      startTest( );

      CreateSimpleProjectInternal(
          "Samples|SOA",
          "Travel Reservation Service",
          TEST_BPEL_APP_NAME
        );

      CreateSimpleProjectInternal(
          "SOA",
          "BPEL Module",
          TEST_BPEL_MODULE_NAME
        );

      endTest( );
    }

    public void CreateSchema( )
    {
      startTest( );

      ProjectsTabOperator pto = new ProjectsTabOperator( );
      ProjectRootNode prn = pto.getProjectRootNode( TEST_BPEL_MODULE_NAME );
      prn.select( );
      
      new JMenuBarOperator(MainWindowOperator.getDefault()).pushMenuNoBlock("File|New File...");

      // JDialogOperator jdNew = new JDialogOperator( "New File" );

      // Workaround for MacOS platform
      // TODO : check platform
      // TODO : remove after normal issue fix
      NewFileWizardOperator.invoke().cancel( );

      NewFileWizardOperator fwNew = new NewFileWizardOperator( "New File" );
      fwNew.selectCategory( "XML" );
      fwNew.selectFileType( "External XML Schema Document(s)" );
      fwNew.next( );

      JDialogOperator jnew = new JDialogOperator( "New File" );
      JRadioButtonOperator jrbLocal = new JRadioButtonOperator( jnew, "From Local File System" );
      jrbLocal.setSelected( true );
      jrbLocal.clickMouse( );

      JButtonOperator jbBrowse = new JButtonOperator( jnew, "Browse" );
      jbBrowse.pushNoBlock( );

      JFileChooserOperator opFileChooser = new JFileChooserOperator( );

      String sPathSrc = GetWorkDir( )
          + TEST_BPEL_APP_NAME
          + File.separator + TEST_BPEL_APP_NAME
          + File.separator + "src";

      String sPathDst = GetWorkDir( )
          + TEST_BPEL_MODULE_NAME;

      opFileChooser.setCurrentDirectory( new File( sPathSrc ) );
      opFileChooser.chooseFile( SCHEMA_NAME );

      // Select different location
      jbBrowse = new JButtonOperator( jnew, "Browse", 1 );
      jbBrowse.pushNoBlock( );

      JDialogOperator jdBrowse = new JDialogOperator( "Choose a Target Folder" );
      JTreeOperator jtTree = new JTreeOperator( jdBrowse, 0 );
      TreePath path = jtTree.findPath( TEST_BPEL_MODULE_NAME );
      jtTree.selectPath( path );

      JButtonOperator jbOk = new JButtonOperator( jdBrowse, "OK" );
      jbOk.push( );
      jdBrowse.waitClosed( );

      fwNew.finish( );

      // Check tree
      FilesTabOperator fto = FilesTabOperator.invoke( );

      Sleep( 10000 );

      JTreeOperator files = fto.tree( );
      path = files.findPath( TEST_BPEL_MODULE_NAME + "|" + SCHEMA_NAME );
      files.selectPath( path );

      // Check outpupt
      OutputOperator out = OutputOperator.invoke( );
      String sText = out.getText( );

      String[] asIdeal =
      {
        //"List of files retrieved :",
        //"From: " + sPathSrc + File.separator + SCHEMA_NAME,
        //"Copied To: " + sPathDst + File.separator + SCHEMA_NAME 

        " : Retrieving Location: file:/" + sPathSrc.replaceAll( "\\\\", "/" ).replaceAll( "^/", "" ) + "/" + SCHEMA_NAME,
        "Retrieved :    file:/" + sPathSrc.replaceAll( "\\\\", "/" ).replaceAll( "^/", "" ) + "/" + SCHEMA_NAME,
        "Saved at: " + sPathDst + File.separator + SCHEMA_NAME
      };

      for( String sIdeal : asIdeal )
      {
        if( -1 == sText.indexOf( sIdeal ) )
        {
          System.out.println( sText );
          fail( "Unable to check retriver output: \"" + sIdeal + "\". Output: \"" + sText + "\"" );
        }
      }

      out.close( );

      ProjectsTabOperator.invoke( );

      endTest( );
    }
}
