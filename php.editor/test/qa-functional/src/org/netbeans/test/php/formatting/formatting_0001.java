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

package org.netbeans.test.php.formatting;

import javax.swing.tree.TreePath;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;
import org.netbeans.jellytools.MainWindowOperator;
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
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jellytools.modules.editor.CompletionJListOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import java.util.List;
import org.netbeans.jemmy.util.Dumper;
import java.io.*;
import org.netbeans.jemmy.operators.JEditorPaneOperator;
import org.netbeans.jemmy.operators.WindowOperator;
import org.netbeans.jemmy.Timeouts;

/**
 *
 * @author michaelnazarov@netbeans.org
 */

public class formatting_0001 extends formatting
{
  static final String TEST_PHP_NAME = "PhpProject_formatting_0001";

  public formatting_0001( String arg0 )
  {
    super( arg0 );
  }

  public static Test suite( )
  {
    return NbModuleSuite.create(
      NbModuleSuite.createConfiguration( formatting_0001.class ).addTest(
          "CreateApplication",
          "Create_a_PHP_web_page",
          "Format_default_code_of_PHP_web_page",
          "Undo_Formatting_of_PHP_web_page",
          "Create_a_PHP_file",
          "Format_default_code_of_PHP_file",
          "Undo_Formatting_of_PHP_file",
          "Formatting_of_folded_code"
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

  public void Create_a_PHP_web_page( )
  {
    startTest( );

    CreatePHPFile( TEST_PHP_NAME, "PHP Web Page", null );

    endTest( );
  }

  public void Format_default_code_of_PHP_web_page( )
  {
    startTest( );

    EditorOperator eoPHP = new EditorOperator( "EmptyPHPWebPage.php" );
    String sTextOriginal = eoPHP.getText( );
    eoPHP.clickForPopup( );
    JPopupMenuOperator menu = new JPopupMenuOperator( );
    menu.pushMenu( "Format" );
    String sTextFormatted = eoPHP.getText( );

    if( !sTextOriginal.equals( sTextFormatted ) )
    {
      fail( "Default formatting is not valid." );
    }

    endTest( );
  }

  public void Undo_Formatting_of_PHP_web_page( )
  {
    startTest( );

    EditorOperator eoPHP = new EditorOperator( "EmptyPHPWebPage.php" );
    String sTextOriginal = eoPHP.getText( );
    eoPHP.setCaretPosition( 0 );
    eoPHP.insert( "                          " );
    String sTextChanged = eoPHP.getText( );
    eoPHP.clickForPopup( );
    JPopupMenuOperator menu = new JPopupMenuOperator( );
    menu.pushMenu( "Format" );
    String sTextFormatted = eoPHP.getText( );

    if( !sTextOriginal.equals( sTextFormatted ) )
    {
      fail( "Default formatting is not valid." );
    }

    new JMenuBarOperator(MainWindowOperator.getDefault()).pushMenu("Edit|Undo");
    String sTextUndo = eoPHP.getText( );
    if( !sTextChanged.equals( sTextUndo ) )
    {
      fail( "Undo formatting is not valid." );
    }

    endTest( );
  }

  public void Create_a_PHP_file( )
  {
    startTest( );

    CreatePHPFile( TEST_PHP_NAME, "PHP File", null );

    endTest( );
  }

  public void Format_default_code_of_PHP_file( )
  {
    startTest( );

    EditorOperator eoPHP = new EditorOperator( "EmptyPHP.php" );
    String sTextOriginal = eoPHP.getText( );
    eoPHP.clickForPopup( );
    JPopupMenuOperator menu = new JPopupMenuOperator( );
    menu.pushMenu( "Format" );
    String sTextFormatted = eoPHP.getText( );

    if( !sTextOriginal.equals( sTextFormatted ) )
    {
      fail( "Default formatting is not valid." );
    }

    endTest( );
  }

  public void Undo_Formatting_of_PHP_file( )
  {
    startTest( );

    EditorOperator eoPHP = new EditorOperator( "EmptyPHP.php" );
    String sTextOriginal = eoPHP.getText( );
    eoPHP.setCaretPosition( 0 );
    eoPHP.insert( "                          " );
    String sTextChanged = eoPHP.getText( );
    eoPHP.clickForPopup( );
    JPopupMenuOperator menu = new JPopupMenuOperator( );
    menu.pushMenu( "Format" );
    String sTextFormatted = eoPHP.getText( );

    if( !sTextOriginal.equals( sTextFormatted ) )
    {
      fail( "Default formatting is not valid." );
    }

    new JMenuBarOperator(MainWindowOperator.getDefault()).pushMenu("Edit|Undo");
    String sTextUndo = eoPHP.getText( );
    if( !sTextChanged.equals( sTextUndo ) )
    {
      fail( "Undo formatting is not valid." );
    }

    endTest( );
  }

  public void Formatting_of_folded_code( )
  {
    startTest( );

    // Human oriented test.
    // No reasons to automate.

    endTest( );
  }
}
