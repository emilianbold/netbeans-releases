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

package org.netbeans.test.xml.schema.general.components.simple;

import org.netbeans.junit.NbModuleSuite;
import junit.framework.Test;
import org.netbeans.jellytools.TopComponentOperator;

/**
 *
 * @author michaelnazarov@netbeans.org
 */

public class SimpleComponent_0001 extends SimpleComponent {
    
    static final String [] m_aTestMethods = {
        "OpenSchema",
        "CheckProperties",
        "CheckingIDProperty",
        "CheckingNameProperty",
        "CheckingDerivationsProperty",
      };

    String sPathInTree = "Simple Types|SimpleType-0";

    String[] asProperties =
    {
      "Kind|Global Simple Type",
      "ID|| ",
      "Name|SimpleType-0",
      "Structure|Click to customize...",
      "Prohibited Derivations (Final)|| ",
      "Length|| ",
      "Min Length|| ",
      "Max Length|| ",
      "Pattern|null",
      "Enumerations|A, B, C",
      "Whitespace|Optional (not set)"
    };

    //String[] asCorrectIDValues = { "qwerty", "asdfg" };
    //String[] asIncorrectIDValues = { "12345" };

    //String[] asCorrectMxOValues = { "*|unbounded", "5", "2" };
    //String[] asIncorrectMxOValues = { "-5" };

    //String[] asCorrectMnOValues = { "5", "0" };
    //String[] asIncorrectMnOValues = { "-5" };

    String[] asCorrectProcessValues = { "Lax", "Skip", "Strict" };
    //String[] asIncorrectProcessValues = { "-5" };

    String[] asCorrectNamespaceValues = { "Local", "Target Namespace", "Other", "Any" };
    //String[] asIncorrectNamespaceValues = { "-5" };

    public SimpleComponent_0001(String arg0) {
        super(arg0);
    }

    /*    
    public static TestSuite suite() {
        TestSuite testSuite = new TestSuite(SimpleComponent_0001.class.getName());
        
        for (String strMethodName : m_aTestMethods) {
            testSuite.addTest(new SimpleComponent_0001(strMethodName));
        }
        
        return testSuite;
    }
    */

    public static Test suite( )
    {
      return NbModuleSuite.create(
          NbModuleSuite.createConfiguration( SimpleComponent_0001.class ).addTest(
              "OpenSchema",
              "CheckProperties",
              "CheckingIDProperty",
              "CheckingNameProperty",
              "CheckingDerivationsProperty",
              "CloseSchema"
           )
           .enableModules( ".*" )
           .clusters( ".*" )
           //.gui( true )
        );
    }

    public void OpenSchema( )
    {
      startTest( );

      OpenSchemaInternal( sPathInTree );

      endTest( );
    }

    public void CheckProperties( )
    {
      startTest( );

      CheckPropertiesInternal( asProperties );

      endTest( );
    }

    public void CheckingIDProperty( )
    {
      startTest( );

      String[] data =
      {
        "<xsd:simpleType name=\"SimpleType-0\">~<xsd:simpleType name=\"SimpleType-0\" id=\"qwerty\">~" + sPathInTree + "~ID|qwerty",
        " id=\"qwerty\"~ id=\"12345\"~" + sPathInTree + "~ID|12345",
        " id=\"12345\"~~" + sPathInTree + "~ID|| ",
      };

      CheckProperty( data );

      endTest( );
    }

    public void CheckingNameProperty( )
    {
      startTest( );

      String[] data =
      {
        "<xsd:simpleType name=\"SimpleType-0\">~<xsd:simpleType name=\"SimpleType-012345\">~Simple Types|SimpleType-012345~Name|SimpleType-012345",
        "<xsd:simpleType name=\"SimpleType-012345\">~<xsd:simpleType name=\"12345\">~Simple Types|12345~Name|12345",
        "<xsd:simpleType name=\"12345\">~<xsd:simpleType name=\"SimpleType-0\">~" + sPathInTree + "~Name|SimpleType-0",
      };

      CheckProperty( data );

      endTest( );
    }

    public void CheckingDerivationsProperty( )
    {
      startTest( );

      String[] data =
      {
        "<xsd:simpleType name=\"SimpleType-0\">~<xsd:simpleType name=\"SimpleType-0\" final=\"#all\">~" + sPathInTree + "~Prohibited Derivations (Final)|#all",
      };
      CheckProperty( data );

      CheckClicked(
          "Global Simple Type - Prohibited Derivations (Final)",
          "Prevent all type derivations (#all)",
          5,
          1
        );

      String[] data1 =
      {
        " final=\"#all\">~ final=\"union list restriction\">~" + sPathInTree + "~Prohibited Derivations (Final)|union list restriction",
      };
      CheckProperty( data1 );

      CheckClicked(
          "Global Simple Type - Prohibited Derivations (Final)",
          "Prevent type derivations of the following kinds:|Union|List|Restriction",
          5,
          1
        );

      String[] data2 =
      {
        " final=\"union list restriction\"~~" + sPathInTree + "~Prohibited Derivations (Final)|| ",
      };
      CheckProperty( data2 );

      endTest( );
    }

  public void CloseSchema( )
  {
    startTest( );

    TopComponentOperator top = new TopComponentOperator( TEST_SCHEMA_NAME );
    top.closeDiscard( );

    endTest( );
  }
}
