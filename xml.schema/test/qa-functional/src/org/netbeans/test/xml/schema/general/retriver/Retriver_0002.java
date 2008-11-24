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

import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jellytools.OutputOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;
import org.netbeans.jemmy.operators.JFileChooserOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;
import java.io.File;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.junit.NbModuleSuite;
import junit.framework.Test;

/**
 *
 * @author michaelnazarov@netbeans.org
 */

public class Retriver_0002 extends Retriver {
    
    static final String TEST_BPEL_APP_NAME = "TravelReservationService_retriver_0002";
    static final String TEST_BPEL_MODULE_NAME = "BpelModule_retriver_0002";
    static final String SCHEMA_NAME = "OTA_TravelItinerary.xsd";

    static final String [] m_aTestMethods = {
        "CreateBPELs",
        "CreateSchema",
    };

    public Retriver_0002(String arg0) {
        super(arg0);
    }
    
    /*
    public static TestSuite suite() {
        TestSuite testSuite = new TestSuite(Retriver_0002.class.getName());
        
        for (String strMethodName : m_aTestMethods) {
            testSuite.addTest(new Retriver_0002(strMethodName));
        }
        
        return testSuite;
    }
    */

    public static Test suite( )
    {
      return NbModuleSuite.create(
          NbModuleSuite.createConfiguration( Retriver_0002.class ).addTest(
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
          + TEST_BPEL_MODULE_NAME
          + File.separator + "src";


      opFileChooser.setCurrentDirectory( new File( sPathSrc ) );
      opFileChooser.chooseFile( SCHEMA_NAME );

      fwNew.finish( );

      Sleep( 10000 );

      // Check tree
      pto = new ProjectsTabOperator( );
      prn = pto.getProjectRootNode(
          TEST_BPEL_MODULE_NAME + "|Process Files|" + SCHEMA_NAME
        );
      prn.select( );

      Sleep( 10000 );

      // Check outpupt
      OutputOperator out = OutputOperator.invoke( );
      String sText = out.getText( );

      String[] asIdeal =
      {
        //"List of files retrieved :",
        " : Retrieving Location: file:/" + sPathSrc.replaceAll( "\\\\", "/" ).replaceAll( "^/", "" ) + "/" + SCHEMA_NAME,
        "Retrieved :    file:/" + sPathSrc.replaceAll( "\\\\", "/" ).replaceAll( "^/", "" ) + "/" + SCHEMA_NAME,
        "Saved at: " + sPathDst + File.separator + SCHEMA_NAME
      };

      for( String sIdeal : asIdeal )
      {
        if( -1 == sText.indexOf( sIdeal ) )
        {
          System.out.println( sText );
          fail( "Unable to check retriver output: \"" + sIdeal + "\"" );
        }
      }

      out.close( );

      endTest( );
    }
}
