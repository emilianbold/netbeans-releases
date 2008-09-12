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

package org.netbeans.test.php;

import java.awt.Point;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jellytools.NewProjectNameLocationStepOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;
import javax.swing.ListModel;
import org.netbeans.jemmy.operators.JMenuBarOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.jellytools.MainWindowOperator;
import java.awt.event.KeyEvent;
import javax.swing.JEditorPane;
import java.awt.Rectangle;
import javax.swing.text.BadLocationException;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JEditorPaneOperator;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.jemmy.operators.JTextComponentOperator;
import org.netbeans.jemmy.operators.Operator;
import java.io.File;

/**
 *
 * @author michaelnazarov@netbeans.org
 */

public class GeneralPHPTest extends JellyTestCase {
    
    static final String PHP_CATEGORY_NAME = "PHP";
    static final String PHP_PROJECT_NAME = "PHP Application";

    protected static final String PHP_EXTENSION = ".php";

    public class CFulltextStringComparator implements Operator.StringComparator
    {
      public boolean equals( java.lang.String caption, java.lang.String match )
      {
        return caption.equals( match );
      }
    }

    public class CStartsStringComparator implements Operator.StringComparator
    {
      public boolean equals( java.lang.String caption, java.lang.String match )
      {
        return caption.startsWith( match );
      }
    }

    public GeneralPHPTest( String arg0 )
    {
      super( arg0 );
    }

    public void Dummy( )
    {
      startTest( );
      System.out.println( "=== DUMMY ===" );
      endTest( );
    }

  protected String GetWorkDir( )
  {
    return getDataDir( ).getPath( ) + File.separator;
  }

  protected void Sleep( int iTime )
  {
    try { Thread.sleep( iTime ); } catch( InterruptedException ex ) { }
  }

  // All defaults including name
  protected void CreatePHPApplicationInternal( )
  {
    // Create PHP application

    // Workaround for MacOS platform
    // TODO : check platform
    // TODO : remove after normal issue fix
    NewProjectWizardOperator.invoke().cancel( );

    NewProjectWizardOperator opNewProjectWizard = NewProjectWizardOperator.invoke( );
    opNewProjectWizard.selectCategory( PHP_CATEGORY_NAME );
    opNewProjectWizard.selectProject( PHP_PROJECT_NAME );

    opNewProjectWizard.next( );

    JDialogOperator jdNew = new JDialogOperator( "New PHP Project" );

    JTextComponentOperator jtName = new JTextComponentOperator( jdNew, 0 );
    String sProjectName = GetWorkDir( ) + File.separator + jtName.getText( );

    JComboBoxOperator jcPath = new JComboBoxOperator( jdNew, 0 );
    jcPath.enterText( sProjectName );
    //NewProjectNameLocationStepOperator opNewProjectNameLocationStep = new NewProjectNameLocationStepOperator( );
    //opNewProjectNameLocationStep.txtProjectLocation( ).setText( GetWorkDir( ) );

    //opNewProjectWizard.next( );

    //opNewProjectNameLocationStep.txtProjectName( ).setText( sName );
    opNewProjectWizard.finish( );
  }
}
