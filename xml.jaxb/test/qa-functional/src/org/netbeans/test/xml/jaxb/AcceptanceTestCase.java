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
import org.netbeans.jemmy.operators.JEditorPaneOperator;

/**
 *
 * @author michaelnazarov@netbeans.org
 */

public class AcceptanceTestCase extends JellyTestCase {
    
    public AcceptanceTestCase(String arg0) {
        super(arg0);
    }
    
    public void CodeCompletion1Internal( )
    {
        startTest( );

        // Access java code with editor
        EditorOperator eoJavaCode = new EditorOperator( "Main.java" );

        eoJavaCode.setCaretPosition(
            "// TODO code application logic here",
            0,
            false
          );
        eoJavaCode.pushKey( KeyEvent.VK_ENTER );

        // TODO : REMOVE TEMPORARY
        eoJavaCode.insert( "CreditReport cr = newCreditReport( )\n" );
        eoJavaCode.insert( "cr.setFirstName( \"Hello\" );\n" );
        eoJavaCode.insert( "cr.setLastName( \"World\" );\n" );
        eoJavaCode.insert( "cr.setScore( 999 );\n" );
        eoJavaCode.insert( "cr.setSsn( \"123-456-ABC\" );\n" );

        /*
        eoJavaCode.insert( "Cred" );
        JEditorPaneOperator editor = eoJavaCode.txtEditorPane( );
        editor.typeKey( ' ', InputEvent.CTRL_MASK );

        CompletionJListOperator jCompl = new CompletionJListOperator( );
        //jCompl.
        System.out.println( "**** 1 ****" );
        try{Thread.sleep( 30000 );}catch(InterruptedException ex){}
        jCompl.clickOnItem( "CreditReport" );
        System.out.println( "**** 2 ****" );
        jCompl.hideAll( );

        System.out.println( "**** 3 ****" );

        String sCompletedText = eoJavaCode.getText( eoJavaCode.getLineNumber( ) );
        if( !sCompletedText.matches( "^[ \\t]*CreditReport$" ) )
        {
          fail( "Wrong completion of Cred: \"" + sCompletedText + "\"" );
        }
        eoJavaCode.insert( " cr = new CreditReport( );\ncr" );

        // TODO : Wait till suggestions will come.
        // How to access them?

        editor.typeKey( '.' );

        jCompl = new CompletionJListOperator( );
        jCompl.clickOnItem( "setLastName" );

        // TODO : Check result
        */

        endTest( );
    }

    public void CodeCompletion2Internal( ) {

        // Access java code with editor
        EditorOperator eoJavaCode = new EditorOperator( "Main.java" );
        eoJavaCode.setCaretPosition(
            "// TODO code application logic here",
            0,
            false
          );
        eoJavaCode.pushKey( KeyEvent.VK_ENTER );
        // Use jaxbm template
        JEditorPaneOperator editor = eoJavaCode.txtEditorPane( );
        String sCode = "jaxbm\t";
        for( int i = 0; i < sCode.length( ); i++ )
          editor.typeKey( sCode.charAt( i ) );

        // Check result
        eoJavaCode.pushUpArrowKey( );
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

        for( String sIdealCodeLine : asIdealCodeLines )
        {
          String sCodeLine = eoJavaCode.getText( eoJavaCode.getLineNumber( ) );
          if( -1 == sCodeLine.indexOf( sIdealCodeLine ) )
          {
            // Test <suite> failed
            fail(
                "Ideal code was not found at line #" + eoJavaCode.getLineNumber( ) +
                " : " + sIdealCodeLine
              );
          }
          eoJavaCode.pushDownArrowKey( );
        }

        // Replace args with cr
        eoJavaCode.setCaretPosition( "marshaller.marshal(", 0, true );
        eoJavaCode.pushKey( KeyEvent.VK_DELETE );
        eoJavaCode.pushKey( KeyEvent.VK_DELETE );
        eoJavaCode.insert( "cr" );
        
    }

    public void RunTheProjectInternal( String sAppName ) {
        // Run
        new JMenuBarOperator(MainWindowOperator.getDefault()).pushMenu("Run|Run Main Project");

        MainWindowOperator.StatusTextTracer stt = MainWindowOperator.getDefault( ).getStatusTextTracer( );
        stt.start( );
        stt.waitText( "Finished building " + sAppName + " (run)." );
        stt.stop( );

        // Check output
        OutputOperator out = new OutputOperator( );
        String sText = out.getText( );
        System.out.println( "***************\n" + sText + "***************" );
          // TODO : is there XML?
    }
    
    public void tearDown() {
        new SaveAllAction().performAPI();
    }

    protected void startTest(){
        super.startTest();
        //Helpers.closeUMLWarningIfOpened();
    }

}
