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

package org.netbeans.test.xml.schema.general.components.any;

import java.awt.Point;
import java.util.zip.CRC32;
import javax.swing.tree.TreePath;
import junit.framework.TestSuite;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NewProjectNameLocationStepOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jellytools.OutputOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.WizardOperator;
import org.netbeans.jellytools.actions.SaveAllAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
//import org.netbeans.test.xml.schema.lib.SchemaMultiView;
//import org.netbeans.test.xml.schema.lib.util.Helpers;
import org.netbeans.jellytools.properties.PropertySheetOperator;
import org.netbeans.jellytools.properties.Property;

import org.netbeans.jemmy.operators.JFileChooserOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import java.io.File;
import org.netbeans.jellytools.MainWindowOperator;
import java.awt.event.KeyEvent;
//import java.awt.Robot;
import org.netbeans.jellytools.FilesTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jemmy.operators.*;
import org.netbeans.jellytools.modules.editor.CompletionJListOperator;
import org.netbeans.test.xml.schema.lib.SchemaMultiView;

import org.netbeans.junit.NbModuleSuite;
import junit.framework.Test;

/**
 *
 * @author michaelnazarov@netbeans.org
 */

public class AnyComponent_0001 extends AnyComponent {
    
    static final String [] m_aTestMethods = {
        "OpenSchema",
        "CheckProperties",
        "CheckingIDProperty",
        "CheckingMaxOccursProperty",
        "CheckingMinOccursProperty",
        "CheckingProcess",
        "CheckingNamespace",
        "CheckSource"
      };

    String sPathInTree = "Groups|Group-0|sequence|any";

    String[] asCorrectIDValues = { "qwerty", "asdfg" };
    String[] asIncorrectIDValues = { "12345" };

    String[] asCorrectMxOValues = { "*|unbounded", "5", "2" };
    String[] asIncorrectMxOValues = { "-5" };

    String[] asCorrectMnOValues = { "5", "0" };
    String[] asIncorrectMnOValues = { "-5" };

    String[] asCorrectProcessValues = { "Lax", "Skip", "Strict" };
    //String[] asIncorrectProcessValues = { "-5" };

    String[] asCorrectNamespaceValues = { "Local", "Target Namespace", "Other", "Any" };
    //String[] asIncorrectNamespaceValues = { "-5" };

    public AnyComponent_0001(String arg0) {
        super(arg0);
    }

    /*    
    public static TestSuite suite() {
        TestSuite testSuite = new TestSuite(AnyComponent_0001.class.getName());
        
        for (String strMethodName : m_aTestMethods) {
            testSuite.addTest(new AnyComponent_0001(strMethodName));
        }
        
        return testSuite;
    }
    */

    public static Test suite( )
    {
      return NbModuleSuite.create(
          NbModuleSuite.createConfiguration( AnyComponent_0001.class ).addTest(
              "OpenSchema",
              "CheckProperties",
              "CheckingIDProperty",
              "CheckingMaxOccursProperty",
              "CheckingMinOccursProperty",
              "CheckingProcess",
              "CheckingNamespace",
              "CheckSource"
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

      String[] asProperties =
      {
        "Kind|Any",
        "ID|| ",
        "Max Occurs|unbounded",
        "Min Occurs|1",
        "Process Contents|Strict (not set)",
        "Namespace|Any"
      };

      CheckPropertiesInternal( asProperties );

      endTest( );
    }

    public void CheckingIDProperty( )
    {
      startTest( );


      CheckingProperty( "ID", asCorrectIDValues, true );
      CheckingProperty( "ID", asIncorrectIDValues, false );

      // TODO : Undo, Redo

      endTest( );
    }

    public void CheckingMaxOccursProperty( )
    {
      startTest( );

      CheckingProperty( "Max Occurs", asCorrectMxOValues, true );
      CheckingProperty( "Max Occurs", asIncorrectMxOValues, false );

      // TODO : Undo, Redo

      endTest( );
    }

    public void CheckingMinOccursProperty( )
    {
      startTest( );

      CheckingProperty( "Min Occurs", asCorrectMnOValues, true );
      CheckingProperty( "Min Occurs", asIncorrectMnOValues, false );

      // TODO : Undo, Redo

      endTest( );
    }

    public void CheckingProcess( )
    {
      startTest( );

      CheckingProperty( "Process Contents", asCorrectProcessValues, true );
      //CheckingProperty( "Process Contents", asIncorrectProcessValues, false );

      // TODO : Undo, Redo

      endTest( );
    }

    public void CheckingNamespace( )
    {
      startTest( );

      CheckingProperty( "Namespace", asCorrectNamespaceValues, true );
      //CheckingProperty( "Process Contents", asIncorrectProcessValues, false );

      // TODO : Undo, Redo

      endTest( );
    }

    public void CheckSource( )
    {
      startTest( );

      CheckSourceInternal(
          sPathInTree,
          "Go To Source",
          "<xsd:any minOccurs=\"" + asCorrectMnOValues[ asCorrectMnOValues.length - 1 ] + "\" maxOccurs=\"" + asCorrectMxOValues[ asCorrectMxOValues.length - 1 ] + "\" id=\"" + asCorrectIDValues[ asCorrectIDValues.length - 1 ] + "\" processContents=\"" + asCorrectProcessValues[ asCorrectProcessValues.length - 1 ] + "\" namespace=\"##" + asCorrectNamespaceValues[ asCorrectNamespaceValues.length - 1 ] + "\">"
        );

      endTest( );
    }
}
