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

package org.netbeans.test.xml.schema.general.schemaview;

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
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.operators.JListOperator;
import java.awt.event.InputEvent;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.jemmy.operators.JTableOperator;
//import org.netbeans.test.xml.schema.lib.SchemaMultiView;
//import org.netbeans.test.xml.schema.lib.util.Helpers;
import org.netbeans.jellytools.actions.AttachWindowAction;

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
import java.util.List;
import org.netbeans.jellytools.OutputTabOperator;
import org.netbeans.jellytools.properties.PropertySheetOperator;
import org.netbeans.jellytools.properties.Property;
import javax.swing.ListModel;
import org.netbeans.jellytools.TopComponentOperator;
import javax.swing.JPopupMenu;
import org.netbeans.jellytools.modules.web.NavigatorOperator;

import org.netbeans.junit.NbModuleSuite;
import junit.framework.Test;

/**
 *
 * @author michaelnazarov@netbeans.org
 */

public class SchemaView_0002 extends SchemaView {
    
    static final String TEST_JAVA_APP_NAME = "java4schemaview_0002";

    static final String SCHEMA_SHORT_NAME_1 = "newXmlSchema1";
    static final String SCHEMA_SHORT_NAME_2 = "newXmlSchema2";
    static final String SCHEMA_SHORT_NAME_3 = "newXmlSchema3";
    static final String SCHEMA_SHORT_NAME_4 = "newXmlSchema4";
    static final String SCHEMA_SHORT_NAME_5 = "newXmlSchema5";
    static final String SCHEMA_SHORT_NAME_6 = "newXmlSchema6";
    static final String SCHEMA_SHORT_NAME_7 = "newXmlSchema7";
    static final String SCHEMA_SHORT_NAME_8 = "newXmlSchema8";
    static final String SCHEMA_SHORT_NAME_9 = "newXmlSchema9";

    static final String SCHEMA_EXTENSION = ".xsd";

    static final String SCHEMA_NAME_1 = SCHEMA_SHORT_NAME_1 + SCHEMA_EXTENSION;
    static final String SCHEMA_NAME_2 = SCHEMA_SHORT_NAME_2 + SCHEMA_EXTENSION;
    static final String SCHEMA_NAME_3 = SCHEMA_SHORT_NAME_3 + SCHEMA_EXTENSION;
    static final String SCHEMA_NAME_4 = SCHEMA_SHORT_NAME_4 + SCHEMA_EXTENSION;
    static final String SCHEMA_NAME_5 = SCHEMA_SHORT_NAME_5 + SCHEMA_EXTENSION;
    static final String SCHEMA_NAME_6 = SCHEMA_SHORT_NAME_6 + SCHEMA_EXTENSION;
    static final String SCHEMA_NAME_7 = SCHEMA_SHORT_NAME_7 + SCHEMA_EXTENSION;
    static final String SCHEMA_NAME_8 = SCHEMA_SHORT_NAME_8 + SCHEMA_EXTENSION;
    static final String SCHEMA_NAME_9 = SCHEMA_SHORT_NAME_9 + SCHEMA_EXTENSION;

    static final String SAMPLE_SCHEMA_NAME = "newLoanApplication.xsd";

    static final String [] m_aTestMethods = {
      "CreateJavaApplication",
      "AddSchema",
      "InvokeSearch",
      "SearchForComponentName",
      "NavigateResults",
    };

    public SchemaView_0002(String arg0) {
        super(arg0);
    }

    /*    
    public static TestSuite suite() {
        TestSuite testSuite = new TestSuite(SchemaView_0002.class.getName());
        
        for (String strMethodName : m_aTestMethods) {
            testSuite.addTest(new SchemaView_0002(strMethodName));
        }
        
        return testSuite;
    }
    */

    public static Test suite( )
    {
      return NbModuleSuite.create(
          NbModuleSuite.createConfiguration( SchemaView_0002.class ).addTest(
              "CreateJavaApplication",
              "AddSchema",
              "InvokeSearch",
              "SearchForComponentName",
              "NavigateResults"
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

    public void AddSchema( )
    {
      startTest( );

      AddLoanApplicationSchemaInternal(
          TEST_JAVA_APP_NAME,
          TEST_JAVA_APP_NAME
        );

      endTest( );
    }

  protected boolean CheckFindBar( TopComponentOperator top, boolean bPresent )
  {
    String[] asButtons = { "Find Next", "Find Previous", "Clear" };
    for( String s : asButtons )
    {
      try
      {
        JButtonOperator but = new JButtonOperator( top, s );
        if( !bPresent )
          return false;
      }
      catch( JemmyException ex )
      {
        if( bPresent )
          return false;
      }
    }
    return true;
  }

  public void InvokeSearch( )
  {
    startTest( );

    TopComponentOperator top = new TopComponentOperator( SAMPLE_SCHEMA_NAME );
    // Check there is no find bar
    if( !CheckFindBar( top, false ) )
      fail( "First find check failed." );
    // Invioke menu
    new JMenuBarOperator(MainWindowOperator.getDefault()).pushMenu("Edit|Find...");
    // Check there is find bar
    if( !CheckFindBar( top, true ) )
      fail( "First find check failed." );
    // Press Escape
    top.pushKey( KeyEvent.VK_ESCAPE );
    // Check there is no find bar
    if( !CheckFindBar( top, false ) )
      fail( "First find check failed." );
    // Preff Ctrl+F
    top.pushKey( KeyEvent.VK_F, InputEvent.CTRL_MASK );
    // Check there is find bar
    if( !CheckFindBar( top, true ) )
      fail( "First find check failed." );
    // Press Escape
    top.pushKey( KeyEvent.VK_ESCAPE );
    // Check there is no find bar
    if( !CheckFindBar( top, false ) )
      fail( "First find check failed." );

    endTest( );
  }

  public void SearchForComponentName( )
  {
    startTest( );

    new JMenuBarOperator(MainWindowOperator.getDefault()).pushMenu("Edit|Find...");
    TopComponentOperator top = new TopComponentOperator( SAMPLE_SCHEMA_NAME );
    JTextComponentOperator text = new JTextComponentOperator( top, 0 );
    text.setText( "Address" );
    text.pushKey( KeyEvent.VK_ENTER );

    // Check label
    JLabelOperator label = new JLabelOperator( top, "Found 6 occurrences." );

    // Check view
    SchemaMultiView xml = new SchemaMultiView( SAMPLE_SCHEMA_NAME );
    JListOperator list = xml.getColumnListOperator( 3 );
    String sSelected = list.getSelectedValue( ).toString( );
    if( !sSelected.equals( "emailAddress" ) )
        fail( "Wrong line selected from find: \"" + sSelected + "\"" );

    xml.switchToSchemaTree( );
    text.pushKey( KeyEvent.VK_ENTER );
    label = new JLabelOperator( top, "Found 6 occurrences." );

    top = new TopComponentOperator( SAMPLE_SCHEMA_NAME );
    JTreeOperator tree = new JTreeOperator( top, 0 );
    TreePath path = tree.getSelectionPath( );
    Object[] oo = path.getPath( );
    for( Object o : oo )
      System.out.println( o );

    endTest( );
  }
}
