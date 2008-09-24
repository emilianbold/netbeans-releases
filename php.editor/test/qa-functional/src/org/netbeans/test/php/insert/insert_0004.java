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

package org.netbeans.test.php.insert;

import javax.swing.tree.TreePath;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.JemmyException;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.junit.NbModuleSuite;
import junit.framework.Test;
import org.netbeans.jemmy.operators.JToggleButtonOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jellytools.modules.editor.CompletionJListOperator;
import java.util.List;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.operators.ComponentOperator;
//import org.netbeans.jemmy.util.Dumper;


/**
 *
 * @author michaelnazarov@netbeans.org
 */

public class insert_0004 extends insert
{
  static final String TEST_PHP_NAME = "PhpProject_insert_0004";

  public insert_0004( String arg0 )
  {
    super( arg0 );
  }

  public static Test suite( )
  {
    return NbModuleSuite.create(
      NbModuleSuite.createConfiguration( insert_0004.class ).addTest(
          "CreateApplication",
          "InsertGetterAndSetter"
        )
        .enableModules( ".*" )
        .clusters( ".*" )
        //.gui( true )
      );
  }

  public void CreateApplication( )
  {
    startTest( );

    CreatePHPApplicationInternal( TEST_PHP_NAME );

    endTest( );
  }

  public void InsertGetterAndSetter( ) throws Exception
  {
    startTest( );

    // Get editor
    EditorOperator eoPHP = new EditorOperator( "index.php" );
    // Locate comment
    eoPHP.setCaretPosition( "// put your code here\n", false );
    eoPHP.insert( "\nclass name\n{\npublic $a;\nprotected $b;\nprivate $c, $d;\n}" );
    eoPHP.setCaretPosition( "$d;\n", false );
    Sleep( 1000 );
    InvokeInsert( eoPHP );
    Sleep( 1000 );

    JDialogOperator jdInsetter = new JDialogOperator( );
    JListOperator jlList = new JListOperator( jdInsetter );

    ClickListItemNoBlock( jlList, 3, 1 );

    JDialogOperator jdGenerator = new JDialogOperator( "Generate Getters and Setters" );

    // Sleect all but $c
    JTreeOperator jtTree = new JTreeOperator( jdGenerator, 0 );
    jtTree.clickOnPath( jtTree.findPath( "a" ) );
    jtTree.clickOnPath( jtTree.findPath( "b" ) );
    jtTree.clickOnPath( jtTree.findPath( "d" ) );

    JButtonOperator jbOk = new JButtonOperator( jdGenerator, "OK" );
    jbOk.pushNoBlock( );
    jdGenerator.waitClosed( );

    // Check result
    String[] asResult =
    {
      "public function getA() {",
      "return $this->a;",
      "}",
      "",
      "public function setA($a) {",
      "$this->a = $a;",
      "}",
      "",
      "public function getB() {",
      "return $this->b;",
      "}",
      "",
      "public function setB($b) {",
      "$this->b = $b;",
      "}",
      "",
      "public function getD() {",
      "return $this->d;",
      "}",
      "",
      "public function setD($d) {",
      "$this->d = $d;",
      "}",
      "",
    };
    CheckResult( eoPHP, asResult, -24 );

    endTest( );
  }
}
