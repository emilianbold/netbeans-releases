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

package org.netbeans.test.xml.schema.general.components.element;

import org.netbeans.junit.NbModuleSuite;
import junit.framework.Test;
import org.netbeans.jellytools.TopComponentOperator;

/**
 *
 * @author michaelnazarov@netbeans.org
 */

public class ElementComponent_0001 extends ElementComponent {
    
    String sPathInTree = "Elements|Element-1";

    String[] asProperties =
    {
      "Kind|Global Element",
      "ID|| ",
      "Name|Element-1",
      "Structure|Click to customize...",
      "Abstract|False (not set)",
      "Nillable|False (not set)",
      "Fixed Value|| ",
      "Default Value|| ",
      "Prohibited Derivations (Final)|| ",
      "Prohibited Substitutions (Block)|| ",
      "Substitution Group|| "
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

    public ElementComponent_0001(String arg0) {
        super(arg0);
    }

    public static Test suite( )
    {
      return NbModuleSuite.create(
          NbModuleSuite.createConfiguration( ElementComponent_0001.class ).addTest(
              "OpenSchema",
              "CheckProperties",
              "CheckingIDProperty",
              "CheckingNameProperty",
              "CheckingAbstractProperty",
              "CheckingNillableProperty",
              "CheckingFixedProperty",
              "CheckingDefaultProperty",
              "CheckingDerivationsProperty",
              "CheckingSubstitutionsProperty",
              "CheckingSubstitutionGroupProperty",
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
        "<xsd:element name=\"Element-1\">~<xsd:element name=\"Element-1\" id=\"qwerty\">~" + sPathInTree + "~ID|qwerty",
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
        "<xsd:element name=\"Element-1\">~<xsd:element name=\"Element-12345\">~Elements|Element-12345~Name|Element-12345",
        "<xsd:element name=\"Element-12345\">~<xsd:element name=\"12345\">~Elements|12345~Name|12345",
        "<xsd:element name=\"12345\">~<xsd:element name=\"Element-1\">~" + sPathInTree + "~Name|Element-1",
      };

      CheckProperty( data );

      endTest( );
    }

    public void CheckingAbstractProperty( )
    {
      startTest( );

      CheckBooleanProperty(
          "element",
          "Element-1",
          "abstract",
          "Abstract",
          sPathInTree
        );

      /*
      String[] data =
      {
        "<xsd:element name=\"Element-1\">~<xsd:element name=\"Element-1\" abstract=\"true\">~" + sPathInTree + "~Abstract|True",
        //" abstract=\"true\"~ abstract=\"true12345\"~" + sPathInTree + "~Abstract|True12345",
        " abstract=\"true\"~ abstract=\"false\"~" + sPathInTree + "~Abstract|False",
        //" abstract=\"true12345\"~ abstract=\"false\"~" + sPathInTree + "~Abstract|False",
        " abstract=\"false\"~~" + sPathInTree + "~Abstract|False (not set)",
      };

      CheckProperty( data );
      */

      endTest( );
    }

    public void CheckingNillableProperty( )
    {
      startTest( );

      CheckBooleanProperty(
          "element",
          "Element-1",
          "nillable",
          "Nillable",
          sPathInTree
        );

      /*
      String[] data =
      {
        "<xsd:element name=\"Element-1\">~<xsd:element name=\"Element-1\" nillable=\"true\">~" + sPathInTree + "~Nillable|True",
        //" nillable=\"true\"~ nillable=\"true12345\"~" + sPathInTree + "~Nillable|True12345",
        " nillable=\"true\"~ nillable=\"false\"~" + sPathInTree + "~Nillable|False",
        //" nillable=\"true12345\"~ nillable=\"false\"~" + sPathInTree + "~Nillable|False",
        " nillable=\"false\"~~" + sPathInTree + "~Nillable|False (not set)",
      };

      CheckProperty( data );
      */

      endTest( );
    }

    public void CheckingFixedProperty( )
    {
      startTest( );

      String[] data =
      {
        "<xsd:element name=\"Element-1\">~<xsd:element name=\"Element-1\" fixed=\"12345\">~" + sPathInTree + "~Fixed Value|12345",
        " fixed=\"12345\"~ fixed=\"qwerty\"~" + sPathInTree + "~Fixed Value|qwerty",
        " fixed=\"qwerty\"~ fixed=\"qwerty\" default=\"12345\"~" + sPathInTree + "~Fixed Value|qwerty",
        "~~" + sPathInTree + "~Default Value|12345",
        " default=\"12345\"~~" + sPathInTree + "~Fixed Value|qwerty",
        " fixed=\"qwerty\"~~" + sPathInTree + "~Fixed Value|| ",
      };

      CheckProperty( data );

      endTest( );
    }

    public void CheckingDefaultProperty( )
    {
      startTest( );

      String[] data =
      {
        "<xsd:element name=\"Element-1\">~<xsd:element name=\"Element-1\" default=\"12345\">~" + sPathInTree + "~Default Value|12345",
        " default=\"12345\"~ default=\"12345\" fixed=\"12345\"~" + sPathInTree + "~Default Value|12345",
        "~~" + sPathInTree + "~Fixed Value|12345",
        " fixed=\"12345\"~~" + sPathInTree + "~Fixed Value|| ",
        " default=\"12345\"~ default=\"qwerty\"~" + sPathInTree + "~Default Value|qwerty",
        " default=\"qwerty\"~~" + sPathInTree + "~Default Value|| ",
      };

      CheckProperty( data );

      endTest( );
    }

    public void CheckingDerivationsProperty( )
    {
      startTest( );

      String[] data =
      {
        "<xsd:element name=\"Element-1\">~<xsd:element name=\"Element-1\" final=\"#all\">~" + sPathInTree + "~Prohibited Derivations (Final)|#all",
      };
      CheckProperty( data );

      CheckClicked(
          "Global Element - Prohibited Derivations (Final)",
          "Prevent all type derivations (#all)",
          9,
          1
        );

      String[] data1 =
      {
        " final=\"#all\">~ final=\"restriction extension\">~" + sPathInTree + "~Prohibited Derivations (Final)|extension restriction",
      };
      CheckProperty( data1 );

      CheckClicked(
          "Global Element - Prohibited Derivations (Final)",
          "Prevent type derivations of the following kinds:|Extension|Restriction",
          9,
          1
        );

      String[] data2 =
      {
        " final=\"restriction extension\"~~" + sPathInTree + "~Prohibited Derivations (Final)|| ",
      };
      CheckProperty( data2 );

      endTest( );
    }

    // TODO : check how it works
    public void CheckingSubstitutionsProperty( )
    {
      startTest( );

      /* TODO : make normal test after spec changes
      String[] data =
      {
        "<xsd:element name=\"Element-1\">~<xsd:element name=\"Element-1\" block=\"#all\">~" + sPathInTree + "~Prohibited Substitutions (Block)|#all",
      };
      CheckProperty( data );

      CheckClicked(
          "Global Element - Prohibited Substitutions (Block)",
          "Block all substitutions (#all)",
          10,
          1
        );

      String[] data1 =
      {
        " block=\"#all\">~ block=\"restriction substitution extension\">~" + sPathInTree + "~Prohibited Substitutions (Block)|substitution restriction extension",
      };
      CheckProperty( data1 );

      CheckClicked(
          "Global Element - Prohibited Substitutions (Block)",
          "Block substitutions of the following kinds:|Extension|Restriction|Substitution",
          10,
          1
        );

      String[] data2 =
      {
        " block=\"restriction substitution extension\"~~" + sPathInTree + "~Prohibited Substitutions (Block)|| ",
      };
      CheckProperty( data2 );
      */

      endTest( );
    }

    protected void CheckTree(
        String sDialog,
        String sPath
      )
    {
      // Get dialog
      // Get tree
      // Get Path
      // Check
        // TODO
    }

    // TODO : check how it works
    public void CheckingSubstitutionGroupProperty( )
    {
      startTest( );

      /* TODO : make normal test after spec changes
      String[] data =
      {
        "<xsd:element name=\"Element-1\">~<xsd:element name=\"Element-1\" substitutionGroup=\"tns:sub\">~" + sPathInTree + "~Substitution Group|IncludedElement",
      };
      CheckProperty( data );

      CheckTree(
          "Global Element - Substitution Group",
          "IncludedElement"
        );

      String[] data1 =
      {
        " substitutionGroup=\"IncludedElement\">~ substitutionGroup=\"ImportedElement\">~" + sPathInTree + "~Substitution Group|ImportedElement",
      };
      CheckProperty( data1 );

      CheckTree(
          "Global Element - Substitution Group",
          "ImportedElement"
        );

      String[] data2 =
      {
        " substitutionGroup=\"ImportedElement\"~~" + sPathInTree + "~Substitution Group|| ",
      };
      CheckProperty( data2 );
      */

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
