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
import org.netbeans.test.xml.schema.lib.SchemaMultiView;
import org.netbeans.junit.NbModuleSuite;
import junit.framework.Test;

/**
 *
 * @author michaelnazarov@netbeans.org
 */

public class Pattern_0002 extends Pattern {
    
    static final String TEST_BPEL_APP_NAME = "TravelReservationService";
    static final String SCHEMA_NAME = "newLoanApplication.xsd";

    static final String TEST_JAVA_APP_NAME = "java4pattern_0002";

    static final String [] m_aTestMethods = {
        "CreateJavaApplication",
        "CreateSchema",
        "ApplyPattern"
    };

    public Pattern_0002(String arg0) {
        super(arg0);
    }

    /*    
    public static TestSuite suite() {
        TestSuite testSuite = new TestSuite(Pattern_0002.class.getName());
        
        for (String strMethodName : m_aTestMethods) {
            testSuite.addTest(new Pattern_0002(strMethodName));
        }
        
        return testSuite;
    }
    */

    public static Test suite( )
    {
      return NbModuleSuite.create(
          NbModuleSuite.createConfiguration( Pattern_0002.class ).addTest(
              "CreateJavaApplication",
              "CreateSchema",
              "ApplyPattern"
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

      AddLoanApplicationSchemaInternal( TEST_JAVA_APP_NAME, TEST_JAVA_APP_NAME );

      endTest( );
    }

    public void ApplyPattern( )
    {
      startTest( );

      // Open schema
      ProjectsTabOperator pto = new ProjectsTabOperator( );
      ProjectRootNode prn = pto.getProjectRootNode(
          TEST_JAVA_APP_NAME + "|Source Packages|" + TEST_JAVA_APP_NAME + "|" + SCHEMA_NAME
        );
      prn.select( );
      prn.performPopupAction( "Open" );

      // Check original
      SchemaMultiView xml = new SchemaMultiView( SCHEMA_NAME );
      String sOriginal = Dump( xml, 0, "" );
      if( !data.sOriginal.equals( sOriginal ) )
        fail( "Invalid original dump." );

      ApplySalami(
          TEST_JAVA_APP_NAME + "|Source Packages|" + TEST_JAVA_APP_NAME + "|" + SCHEMA_NAME,
          SCHEMA_NAME
        );
      String sSalami= Dump( xml, 0, "" );
      if( !data.sSalami.equals( sSalami ) )
        fail( "Invalid salami dump." );

      ApplyGoE(
          TEST_JAVA_APP_NAME + "|Source Packages|" + TEST_JAVA_APP_NAME + "|" + SCHEMA_NAME,
          SCHEMA_NAME
        );
      String sGoE = Dump( xml, 0, "" );
      if( !data.sGoE.equals( sGoE ) )
        fail( "Invalid GoE dump." + sGoE  );

      ApplyRussianDoll(
          TEST_JAVA_APP_NAME + "|Source Packages|" + TEST_JAVA_APP_NAME + "|" + SCHEMA_NAME,
          SCHEMA_NAME
        );
      String sRussian = Dump( xml, 0, "" );
      if( !data.sRussian.equals( sRussian ) )
        fail( "Invalid russian dump." );

      ApplyVenetian(
          TEST_JAVA_APP_NAME + "|Source Packages|" + TEST_JAVA_APP_NAME + "|" + SCHEMA_NAME,
          SCHEMA_NAME
        );
      String sVenetian = Dump( xml, 0, "" );
      if( !data.sVenetian.equals( sVenetian ) )
        fail( "Invalid venetian dump." );

      //if( !sOriginal.equals( sVenetian ) )
        //fail( "Invalid original-venetian dump." );

      endTest( );
    }
}
