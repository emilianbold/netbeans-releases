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

package org.netbeans.test.xml.schema.general.generation;

import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.junit.NbModuleSuite;
import junit.framework.Test;

/**
 *
 * @author michaelnazarov@netbeans.org
 */

// Sample XML generation from schema via context menu
// 

public class XMLGeneration_0001 extends XMLGeneration {
    
    static final String TEST_JAVA_APP_NAME = "java4xmlgeneration_0001";

    static final String [] m_aTestMethods = {
        "CreateJavaApplication",
        "CreateSampleSchema",
        "GenerateSampleXML",
        "CheckAndValidate"
    };

    public XMLGeneration_0001(String arg0) {
        super(arg0);
    }

    /*    
    public static TestSuite suite() {
        TestSuite testSuite = new TestSuite(XMLGeneration_0001.class.getName());
        
        for (String strMethodName : m_aTestMethods) {
            testSuite.addTest(new XMLGeneration_0001(strMethodName));
        }
        
        return testSuite;
    }
    */

    public static Test suite( )
    {
      return NbModuleSuite.create(
          NbModuleSuite.createConfiguration( XMLGeneration_0001.class ).addTest(
              "CreateJavaApplication",
              "CreateSampleSchema",
              "GenerateSampleXML",
              "CheckAndValidate"
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

      AddLoanApplicationSchemaInternal(
          TEST_JAVA_APP_NAME,
          TEST_JAVA_APP_NAME
        );

      endTest( );
    }

    public void GenerateSampleXML( )
    {
      startTest( );

      ProjectsTabOperator pto = new ProjectsTabOperator( );
      ProjectRootNode prn = pto.getProjectRootNode(
          TEST_JAVA_APP_NAME + "|Source Packages|" + TEST_JAVA_APP_NAME + "|newLoanApplication.xsd"
        );
      prn.select( );

      prn.performPopupActionNoBlock( "Generate Sample XML" );

      JDialogOperator jdNew = new JDialogOperator( "Generate Sample XML" );
      JButtonOperator jbFinish = new JButtonOperator( jdNew, "Finish" );
      jbFinish.push( );
      jdNew.waitClosed( );

      // Check tree
      prn = pto.getProjectRootNode(
          TEST_JAVA_APP_NAME + "|Source Packages|" + TEST_JAVA_APP_NAME + "|newLoanApplication.xml"
        );
      prn.select( );
      // Check opened
      EditorOperator eoXmlCode = new EditorOperator( "newLoanApplication.xml" );

      endTest( );
    }

    public void CheckAndValidate( )
    {
      startTest( );

      CheckInternal( );

      endTest( );
    }
}
