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

package org.netbeans.test.xml.schema.general.pattern;

import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.test.xml.schema.lib.SchemaMultiView;
import javax.swing.ListModel;
import org.netbeans.junit.NbModuleSuite;
import junit.framework.Test;

/**
 *
 * @author michaelnazarov@netbeans.org
 */

public class Pattern_0001 extends Pattern {
    
    static final String TEST_BPEL_APP_NAME = "TravelReservationService_pattern_0001";
    static final String SCHEMA_NAME = "OTA_TravelItinerary.xsd";

    static final String [] m_aTestMethods = {
        "CreateBPELs",
        "ApplyPattern",
    };

    public Pattern_0001(String arg0) {
        super(arg0);
    }

    /*    
    public static TestSuite suite() {
        TestSuite testSuite = new TestSuite(Pattern_0001.class.getName());
        
        for (String strMethodName : m_aTestMethods) {
            testSuite.addTest(new Pattern_0001(strMethodName));
        }
        
        return testSuite;
    }
    */

    public static Test suite( )
    {
      return NbModuleSuite.create(
          NbModuleSuite.createConfiguration( Pattern_0001.class ).addTest(
              "CreateBPELs",
              "ApplyPattern"
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

      endTest( );
    }

    public void CheckStructure( SchemaMultiView xml, String name, int count )
    {
      SelectInFirstColumn( xml, name );
      JListOperator list = xml.getColumnListOperator( 1 );
      if( null == list )
      {
        if( -1 == count )
          return;
        fail( "No list required for " + name );
      }
      ListModel lmd = list.getModel( );
      int iCount = lmd.getSize( );
      if( count != iCount )
        fail( "Invalid original structure " + name + ": " + iCount );
    }

    public void ApplyPattern( )
    {
      startTest( );

      // Open schema
      ProjectsTabOperator pto = new ProjectsTabOperator( );
      ProjectRootNode prn = pto.getProjectRootNode(
          TEST_BPEL_APP_NAME + "|Process Files|" + SCHEMA_NAME
        );
      prn.select( );
      prn.performPopupAction( "Open" );

      // Check original
      SchemaMultiView xml = new SchemaMultiView( SCHEMA_NAME );
      CheckStructure( xml, "Attribute Groups", 81 );
      CheckStructure( xml, "Elements", 6 );

      
      ApplySalami( 
          TEST_BPEL_APP_NAME + "|Process Files|" + SCHEMA_NAME,
          SCHEMA_NAME
        );

      /*
      // Apply pattern
      prn.performPopupActionNoBlock( "Apply Design Pattern..." );
      JDialogOperator jdPattern = new JDialogOperator( "Apply Design Pattern" );
      JToggleButtonOperator jtb = new JToggleButtonOperator( jdPattern, "Do not Create Type(s)" );
      jtb.setSelected( true );
      jtb.clickMouse( );
      JButtonOperator jbFinish = new JButtonOperator( jdPattern, "Finish" );
      jbFinish.push( );
      jdPattern.waitClosed( );
      JDialogOperator jdProcess = new JDialogOperator( "Applying Design Pattern \"" + SCHEMA_NAME + "\"..." );

      boolean bRedo = true;
      while( bRedo )
      {
        try
        {
          jdProcess.waitClosed( );
          bRedo = false;
        }
        catch( JemmyException ex )
        {
          System.out.println( "Waiting apply completion..." );
        }
      }
      */

      // Check new dump
      xml = new SchemaMultiView( SCHEMA_NAME );
      CheckStructure( xml, "Attribute Groups", -1 );
      CheckStructure( xml, "Elements", 558 );

      endTest( );
    }
}
