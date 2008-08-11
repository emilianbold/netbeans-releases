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

package org.netbeans.test.xml.schema.general.codecompletion;

import junit.framework.TestSuite;

import org.netbeans.junit.NbTestCase;
import java.util.Properties;
import org.netbeans.junit.RandomlyFails;
import junit.framework.Test;
import org.netbeans.junit.NbModuleSuite;

/**
 *
 * @author michaelnazarov@netbeans.org
 */

public class XMLCodeCompletion_0002 extends XMLCodeCompletion {
    
    static final String TEST_JAVA_APP_NAME = "java4xmlcodecompletion_0002";

    public XMLCodeCompletion_0002(String arg0) {
        super(arg0);
    }
    
    public static Test suite( )
    {
      return NbModuleSuite.create(
          NbModuleSuite.createConfiguration( XMLCodeCompletion_0002.class ).addTest(
            "CreateJavaApplication",
            "AddSampleSchema",
            "CreateConstrained",
            "StartTag"
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

    public void AddSampleSchema( )
    {
      startTest( );

      AddPurchaseOrderSchemaInternal(
          TEST_JAVA_APP_NAME,
          null//TEST_JAVA_APP_NAME
        );
      //AddSampleSchemaInternal( TEST_JAVA_APP_NAME, null );

      endTest( );
    }

    public void CreateConstrained( )
    {
      startTest( );

      CImportClickData[] aimpData =
      {
        new CImportClickData( true, 1, 0, 2, 3, "Unknown import table state after first click, number of rows: ", null, 5000 ),
        new CImportClickData( true, 2, 0, 2, 4, "Unknown import table state after second click, number of rows: ", null, 5000 ),
        new CImportClickData( true, 3, 1, 1, 4, "Unknown to click on checkbox. #", null )
      };

      CreateConstrainedInternal(
          TEST_JAVA_APP_NAME,
          aimpData,
          null,
          "purchaseOrder",
          0
        );

      endTest( );
    }

    public void StartTag( )
    {
      startTest( );

      StartTagInternal( "newXMLDocument.xml", "</ns1:purchaseOrder>", true, null );

      endTest( );
    }
}
