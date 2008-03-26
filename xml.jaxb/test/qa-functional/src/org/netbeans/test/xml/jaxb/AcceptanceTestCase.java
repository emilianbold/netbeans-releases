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

package org.netbeans.test.xml.jaxb;

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
import java.io.File;
import org.netbeans.jellytools.MainWindowOperator;
import java.awt.event.KeyEvent;
import java.awt.Robot;

/**
 *
 * @author michaelnazarov@netbeans.org
 */

public class AcceptanceTestCase extends JellyTestCase {
    
    static final String [] m_aTestMethods = {
        "CreateJavaApplication",
        "Compile"
        //"CreateJAXBBinding",
        //"AddJAXBMarshaling",
        //"ExecuteJavaApplication"
        /*
        "createNewSchema",
                "createSchemaComponents",
                "customizeSchema",
                "checkSourceCRC",
                "refactorComplexType",
                "applyDesignPattern"
        */
    };
    
    static final String TEST_JAVA_APP_NAME = "java4jaxb";
    //static final String SCHEMA_EXTENSION = ".xsd";

    private String sApplicationPath = null;
    
    public AcceptanceTestCase(String arg0) {
        super(arg0);
    }
    
    public static TestSuite suite() {
        TestSuite testSuite = new TestSuite(AcceptanceTestCase.class.getName());
        
        for (String strMethodName : m_aTestMethods) {
            testSuite.addTest(new AcceptanceTestCase(strMethodName));
        }
        
        return testSuite;
    }
    
    public void Dummy() {
        startTest();
        
        endTest();
    }
    
    public void CreateJavaApplication() {
        startTest();

        // Create Java application
        NewProjectWizardOperator opNewProjectWizard = NewProjectWizardOperator.invoke();
        opNewProjectWizard.selectCategory("Java");
        opNewProjectWizard.selectProject("Java Application");
        opNewProjectWizard.next();
        
        NewProjectNameLocationStepOperator opNewProjectNameLocationStep = new NewProjectNameLocationStepOperator();
        opNewProjectNameLocationStep.txtProjectLocation( ).setText( System.getProperty( "work.dir" )  );
        opNewProjectNameLocationStep.txtProjectName( ).setText( TEST_JAVA_APP_NAME );
        opNewProjectWizard.finish();

        endTest();
    }
    
    public void Compile() {
        startTest();

        new JMenuBarOperator(MainWindowOperator.getDefault()).pushMenu("Build|Build Main Project");

        // Wait till JAXB really deleted
        MainWindowOperator.StatusTextTracer stt = MainWindowOperator.getDefault( ).getStatusTextTracer( );
        stt.start( );
        stt.waitText( "Finished building " + TEST_JAVA_APP_NAME + " (jar)." );
        stt.stop( );

        new JMenuBarOperator(MainWindowOperator.getDefault()).pushMenu("Run|Run Main Project");

        endTest();
    }
    
    public void CreateJAXBBinding() {
        startTest();

        // Create JAXB Binding
        NewFileWizardOperator opNewFileWizard = NewFileWizardOperator.invoke();
        opNewFileWizard.selectCategory("XML");
        opNewFileWizard.selectFileType("JAXB Binding");
        opNewFileWizard.next();

        JDialogOperator opCustomizer = new JDialogOperator( );
        new JTextFieldOperator( opCustomizer, 0 ).setText( "aaa" );

        new JButtonOperator( opCustomizer, 0 ).pushNoBlock( );
        JFileChooserOperator opFileChooser = new JFileChooserOperator( );
        opFileChooser.chooseFile( System.getProperty( "work.dir" ) + File.separator + "data" + File.separator + "CreditReport.xsd" );
        new JTextFieldOperator( opCustomizer, 4 ).setText( "bbb" );

        opNewFileWizard.finish( );

        endTest();
    }
    
    public void AddJAXBMarshaling( ) {
        startTest();

        // Access java code with editor
        EditorOperator eoJavaCode = new EditorOperator( "Main.java" );
        eoJavaCode.setCaretPosition(
            "// TODO code application logic here",
            0,
            false
          );
        eoJavaCode.pushKey( KeyEvent.VK_ENTER );
        // Use jaxbm pattern
        //eoJavaCode.insert( "jaxbm" );
        // Press tab key
        eoJavaCode.pushKey( KeyEvent.VK_J );
        eoJavaCode.pushKey( KeyEvent.VK_A );
        eoJavaCode.pushKey( KeyEvent.VK_X );
        eoJavaCode.pushKey( KeyEvent.VK_B );
        eoJavaCode.pushKey( KeyEvent.VK_M );
        eoJavaCode.pushTabKey( );
        /*
        try
        {
        Robot r = new Robot( );
        r.keyPress( KeyEvent.VK_TAB );
        r.keyRelease( KeyEvent.VK_TAB );
        }
        catch( java.awt.AWTException ex )
        {
        }
        */

        eoJavaCode.pushUpArrowKey( );
        // Check result
        String[] asIdealCodeLines =
        {
          "try {",
          "javax.xml.bind.JAXBContext jaxbCtx = javax.xml.bind.JAXBContext.newInstance(args.getClass().getPackage().getName());",
          "javax.xml.bind.Marshaller marshaller = jaxbCtx.createMarshaller();",
          "marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_ENCODING, \"UTF-8\"); //NOI18N",
          "",
          "marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);",
          "marshaller.marshal(args, System.out);",
          "} catch (javax.xml.bind.JAXBException ex) {",
          "// XXXTODO Handle exception",
          "java.util.logging.Logger.getLogger(\"global\").log(java.util.logging.Level.SEVERE, null, ex); //NOI18N",
          "",
          "}",
          "}"
        };
        for( int i = 0; i < asIdealCodeLines.length; i++ )
        {
          String sCodeLine = eoJavaCode.getText( eoJavaCode.getLineNumber( ) );
          if( -1 == sCodeLine.indexOf( asIdealCodeLines[ i ] ) )
          {
            // Test <suite> failed
            fail(
                "Ideal code was not found at line #" + eoJavaCode.getLineNumber( ) +
                " : " + asIdealCodeLines[ i ]
              );
          }
          eoJavaCode.pushDownArrowKey( );
        }

        endTest();
    }

    public void ExecuteJavaApplication( ) {
        startTest();

        // Run
        new JMenuBarOperator(MainWindowOperator.getDefault()).pushMenu("Run|Run Main Project");
          // ToDo
        
        endTest();
    }
    
    public void tearDown() {
        new SaveAllAction().performAPI();
    }

    protected void startTest(){
        super.startTest();
        //Helpers.closeUMLWarningIfOpened();
    }

}
