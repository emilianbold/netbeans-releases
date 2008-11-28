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

package org.netbeans.test.php.navigation;

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

public class navigation_0001 extends navigation
{
  static final String TEST_PHP_NAME = "PhpProject_navigation_0001";

  public navigation_0001( String arg0 )
  {
    super( arg0 );
  }

  public static Test suite( )
  {
    return NbModuleSuite.create(
      NbModuleSuite.createConfiguration( navigation_0001.class ).addTest(
          "CreateApplication",
          "Create_a_PHP_web_page",
          "Navigate_to_a_specified_line",
          "Navigate_to_an_invalid_line"
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

  protected void GoToLine( int iLineToGo, int iLineToCome )
  {
    EditorOperator eoPHP = new EditorOperator( "EmptyPHPWebPage.php" );
    eoPHP.clickForPopup( );
    JPopupMenuOperator menu = new JPopupMenuOperator( );
    menu.pushMenuNoBlock( "Navigate|Line..." );
    JDialogOperator jdGoto = new JDialogOperator( "Go To Line" );
    JComboBoxOperator jcLine = new JComboBoxOperator( jdGoto, 0 );
    jcLine.enterText( "" + iLineToGo );
    JButtonOperator jbGoto = new JButtonOperator( jdGoto, "Go To" );
    jbGoto.push( );
    jdGoto.waitClosed( );
    int iLine = eoPHP.getLineNumber( );
    if( iLineToCome != iLine )
    {
      fail( "Navigate go to line came to incorrect one. Found: " + iLine + ", expected: " + iLineToCome );
    }
  }

  public void Navigate_to_a_specified_line( )
  {
    startTest( );

    GoToLine( 8, 8 );

    endTest( );
  }

  public void Navigate_to_an_invalid_line( )
  {
    startTest( );

    GoToLine( 100, 17 );

    endTest( );
  }

}
