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

package org.netbeans.test.xml.schema.codecompletion;

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

/**
 *
 * @author michaelnazarov@netbeans.org
 */

public class XMLCodeCompletion_0005 extends XMLCodeCompletion {
    
    static final String TEST_JAVA_APP_NAME = "java4xmlcodecompletion_0005";

    static final String [] m_aTestMethods = {
        "CreateJavaApplication",
        "CreateConstrained",
        "StartTag"
    };

    public XMLCodeCompletion_0005(String arg0) {
        super(arg0);
    }
    
    public static TestSuite suite() {
        TestSuite testSuite = new TestSuite(XMLCodeCompletion_0005.class.getName());
        
        for (String strMethodName : m_aTestMethods) {
            testSuite.addTest(new XMLCodeCompletion_0005(strMethodName));
        }
        
        return testSuite;
    }
    
    public void CreateJavaApplication( )
    {
        startTest( );

        CreateJavaApplicationInternal( TEST_JAVA_APP_NAME );

        endTest( );
    }

    public void CreateConstrained( )
    {
      startTest( );

      String sApplication = TEST_JAVA_APP_NAME;
      String sPackage = TEST_JAVA_APP_NAME;

      ProjectsTabOperator pto = new ProjectsTabOperator( );
      ProjectRootNode prn = pto.getProjectRootNode( sApplication + "|Source Packages|" + sPackage );
      prn.select( );

      NewFileWizardOperator opNewFileWizard = NewFileWizardOperator.invoke( );

      // PAGE ===========================================================
      opNewFileWizard.selectCategory( "XML" );
      opNewFileWizard.selectFileType( "XML Document" );
      opNewFileWizard.next( );

      // PAGE ===========================================================
      opNewFileWizard.next( );

      // PAGE ===========================================================
      JDialogOperator jnew = new JDialogOperator( "New File" );
      JRadioButtonOperator jbut = new JRadioButtonOperator( jnew, "XML Schema-Constrained Document" );
      jbut.setSelected( true );
      jbut.clickMouse( );
      opNewFileWizard.next( );

      // PAGE ===========================================================
      jnew = new JDialogOperator( "New File" );
      JTableOperator jtable = new JTableOperator( jnew, 0 );
      jtable.clickOnCell( 0, 1, 2 );

      JTextComponentOperator jcom = new JTextComponentOperator( jnew, 0 );
      jcom.setText( "http://www.springframework.org/schema/beans/spring-beans-2.5.xsd" );
      jcom.pushKey( KeyEvent.VK_ENTER );

      jtable.clickOnCell( 0, 2, 1 );

      JComboBoxOperator jcombo = new JComboBoxOperator( jnew, 0 );
      jcombo.selectItem( "bean" );

      //jtable.clickOnCell( 0, 0, 1 );

      opNewFileWizard.next( );

      // PAGE ===========================================================
      opNewFileWizard.finish( );

      // Check created schema in project tree
      prn = pto.getProjectRootNode( sApplication );
      if( null == ( new Node( prn, "Source Packages|" + sPackage + "|newXMLDocument.xml" ) ) )
      {
        fail( "Unable to check created sample schema." );
      }

      // Check there is newly created schema opened in editor
      EditorOperator xmlCode = new EditorOperator( "newXMLDocument.xml" );

      endTest( );
    }

    public void StartTag( )
    {
      startTest( );

      String[] asCases =
      {
        "ns1:constructor-arg", "ns1:description", "ns1:lookup-method",
        "ns1:meta", "ns1:property", "ns1:qualifier", "ns1:replaced-method"
      };

      StartTagInternal( "newXMLDocument.xml", "</ns1:bean>", true, asCases );

      endTest( );
    }

}
