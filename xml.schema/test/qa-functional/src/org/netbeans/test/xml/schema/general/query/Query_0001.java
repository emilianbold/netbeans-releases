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

package org.netbeans.test.xml.schema.general.query;

import javax.swing.tree.TreePath;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.junit.NbModuleSuite;
import junit.framework.Test;
import org.netbeans.jemmy.operators.JToggleButtonOperator;

/**
 *
 * @author michaelnazarov@netbeans.org
 */

public class Query_0001 extends Query {
    
    static final String TEST_JAVA_APP_NAME = "java4query_0001";
    static final String SCHEMA_NAME = "newPurchaseOrder.xsd";

    static final String TEST_BPEL_APP_NAME = "TravelReservationService_query_0001";
    static final String OTA_SCHEMA_NAME = "OTA_TravelItinerary.xsd";

    static final String [] m_aTestMethods = {
        "CreateJavaApplication",
        "CreateSchema",
        "QueryUnused1",
        "QueryUnused2",
        "QuerySubstitutions",
        "CreateBPELSample",
        "QueryDerivations"
    };

    public Query_0001(String arg0) {
        super(arg0);
    }

    /*    
    public static TestSuite suite() {
        TestSuite testSuite = new TestSuite(Query_0001.class.getName());
        
        for (String strMethodName : m_aTestMethods) {
            testSuite.addTest(new Query_0001(strMethodName));
        }
        
        return testSuite;
    }
    */

    public static Test suite( )
    {
      return NbModuleSuite.create(
          NbModuleSuite.createConfiguration( Query_0001.class ).addTest(
              "CreateJavaApplication",
              "CreateSchema",
              "QueryUnused1",
              "QueryUnused2",
              "QuerySubstitutions",
              "CreateBPELSample",
              "QueryDerivations"
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

    public void CreateSchema( )
    {
      startTest( );

      AddPurchaseOrderSchemaInternal( TEST_JAVA_APP_NAME, TEST_JAVA_APP_NAME );

      endTest( );
    }

    public void CallPopup( String sPath, String sName )
    {
      ProjectsTabOperator pto = new ProjectsTabOperator( );
      ProjectRootNode prn = pto.getProjectRootNode(
          sPath
        );
      prn.select( );

      prn.performPopupAction( sName );
    }

    public void CallPopupNoBlock( String sPath, String sName )
    {
      ProjectsTabOperator pto = new ProjectsTabOperator( );
      ProjectRootNode prn = pto.getProjectRootNode(
          sPath
        );
      prn.select( );

      prn.performPopupActionNoBlock( sName );
    }

    public void QueryUnused1( )
    {
      startTest( );

      CallPopupNoBlock(
          TEST_JAVA_APP_NAME + "|Source Packages|" + TEST_JAVA_APP_NAME + "|" + SCHEMA_NAME,
          "Query|Find Unused Components"
        );

      JDialogOperator jdFind = new JDialogOperator( "Find Unused Global Components" );
      JButtonOperator jbOk = new JButtonOperator( jdFind, "OK" );

      // Ensure we will catch all with any slowness
      MainWindowOperator.StatusTextTracer stt = MainWindowOperator.getDefault( ).getStatusTextTracer( );
      stt.start( );

      jbOk.push( );
      jdFind.waitClosed( );

      TopComponentOperator top = new TopComponentOperator( "XML Schema Query" );
      JTreeOperator tree = new JTreeOperator( top, 0 );

      String sTreeDump = Dump( tree, tree.getRoot( ), -1, "" );

      String sIdeal = " 0:Unused Global Components 0:Attributes 0:Attribute Groups 0:Complex Types 0:Elements 1:purchaseOrder 0:Groups 0:Simple Types";
      if( !sIdeal.equals( sTreeDump ) )
        fail( "Invalid query result: \"" + sTreeDump + "\"" );

      stt.waitText( "Found 1 unused global component" );
      stt.stop( );

      top.close( );

      endTest( );
    }

    public void QueryUnused2( )
    {
      startTest( );

      CallPopupNoBlock(
          TEST_JAVA_APP_NAME + "|Source Packages|" + TEST_JAVA_APP_NAME + "|" + SCHEMA_NAME,
          "Query|Find Unused Components"
        );

      JDialogOperator jdFind = new JDialogOperator( "Find Unused Global Components" );
      JToggleButtonOperator jtbExclude = new JToggleButtonOperator(
          jdFind,
          "Exclude Global Elements"
        );
      jtbExclude.setSelected( true );
      JButtonOperator jbOk = new JButtonOperator( jdFind, "OK" );

      // Ensure we will catch all with any slowness
      MainWindowOperator.StatusTextTracer stt = MainWindowOperator.getDefault( ).getStatusTextTracer( );
      stt.start( );

      jbOk.push( );
      jdFind.waitClosed( );

      TopComponentOperator top = new TopComponentOperator( "XML Schema Query" );
      JTreeOperator tree = new JTreeOperator( top, 0 );

      String sTreeDump = Dump( tree, tree.getRoot( ), -1, "" );

      String sIdeal = " 0:Unused Global Components 0:Attributes 0:Attribute Groups 0:Complex Types 0:Groups 0:Simple Types";
      if( !sIdeal.equals( sTreeDump ) )
        fail( "Invalid query result: \"" + sTreeDump + "\"" );

      stt.waitText( "Found 0 unused global components" );
      stt.stop( );

      top.close( );

      endTest( );
    }

    public void QuerySubstitutions( )
    {
      startTest( );

      // Ensure we will catch all with any slowness
      MainWindowOperator.StatusTextTracer stt = MainWindowOperator.getDefault( ).getStatusTextTracer( );
      stt.start( );

      CallPopup(
          TEST_JAVA_APP_NAME + "|Source Packages|" + TEST_JAVA_APP_NAME + "|" + SCHEMA_NAME,
          "Query|Find Substitution Groups"
        );

      stt.waitText( "0 Substitution Groups found" );
      stt.stop( );

      TopComponentOperator top = new TopComponentOperator( "XML Schema Query" );
      top.close( );

      endTest( );
    }

    public void CreateBPELSample( )
    {
      startTest( );

      CreateSimpleProjectInternal(
          "Samples|SOA",
          "Travel Reservation Service",
          TEST_BPEL_APP_NAME
        );

      endTest( );
    }

    public void QueryDerivations( )
    {
      startTest( );

      CallPopupNoBlock(
          TEST_BPEL_APP_NAME + "|Process Files|" + OTA_SCHEMA_NAME,
          "Query|Find Derivations of Complex Type"
        );

      JDialogOperator jdFind = new JDialogOperator( "Find Derivations" );

      JTreeOperator tree = new JTreeOperator( jdFind, 0 );
      TreePath path = tree.findPath( "AddressType" );
      tree.selectPath( path );

      JButtonOperator jbOk = new JButtonOperator( jdFind, "OK" );

      // Ensure we will catch all with any slowness
      MainWindowOperator.StatusTextTracer stt = MainWindowOperator.getDefault( ).getStatusTextTracer( );
      stt.start( );

      jbOk.push( );
      jdFind.waitClosed( );

      stt.waitText( "Found 3 extensions and 0 restrictions on AddressType" );
      stt.stop( );

      TopComponentOperator top = new TopComponentOperator( "XML Schema Query" );
      top.close( );

      endTest( );
    }

}
