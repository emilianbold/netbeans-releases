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
import org.netbeans.jemmy.Timeouts;

/**
 *
 * @author michaelnazarov@netbeans.org
 */

public class GeneralPHP extends JellyTestCase {
    
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

    public GeneralPHP( String arg0 )
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
  protected String CreatePHPApplicationInternal( )
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

    String sResult = jtName.getText( );

    String sProjectPath = GetWorkDir( ) + File.separator + sResult;

    JComboBoxOperator jcPath = new JComboBoxOperator( jdNew, 0 );

    Timeouts t =  jcPath.getTimeouts( );
    long lBack = t.getTimeout( "JTextComponentOperator.TypeTextTimeout" );
    t.setTimeout( "JTextComponentOperator.TypeTextTimeout", 30000 );
    jcPath.setTimeouts( t );

    jcPath.enterText( sProjectPath );

    t.setTimeout( "JTextComponentOperator.TypeTextTimeout", lBack );
    jcPath.setTimeouts( t );

    //NewProjectNameLocationStepOperator opNewProjectNameLocationStep = new NewProjectNameLocationStepOperator( );
    //opNewProjectNameLocationStep.txtProjectLocation( ).setText( GetWorkDir( ) );

    //opNewProjectWizard.next( );

    //opNewProjectNameLocationStep.txtProjectName( ).setText( sName );
    opNewProjectWizard.finish( );

    return sResult;
  }

  // All defaults including name
  protected void CreatePHPApplicationInternal( String sProjectName )
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

    if( null != sProjectName )
      jtName.setText( sProjectName );

    String sProjectPath = GetWorkDir( ) + File.separator + jtName.getText( );

    JComboBoxOperator jcPath = new JComboBoxOperator( jdNew, 0 );

    Timeouts t =  jcPath.getTimeouts( );
    long lBack = t.getTimeout( "JTextComponentOperator.TypeTextTimeout" );
    t.setTimeout( "JTextComponentOperator.TypeTextTimeout", 30000 );
    jcPath.setTimeouts( t );

    jcPath.enterText( sProjectPath );

    t.setTimeout( "JTextComponentOperator.TypeTextTimeout", lBack );
    jcPath.setTimeouts( t );

    //NewProjectNameLocationStepOperator opNewProjectNameLocationStep = new NewProjectNameLocationStepOperator( );
    //opNewProjectNameLocationStep.txtProjectLocation( ).setText( GetWorkDir( ) );

    //opNewProjectWizard.next( );

    //opNewProjectNameLocationStep.txtProjectName( ).setText( sName );
    opNewProjectWizard.finish( );
  }

  protected void TypeCode( EditorOperator edit, String code )
  {
    for( int i = 0; i < code.length( ); i++ )
    {
      edit.typeKey( code.charAt( i ) );
      Sleep( 100 );
    }
  }

  protected void CheckResult(
      EditorOperator eoPHP,
      String sCheck
    )
  {
    CheckResult( eoPHP, sCheck, 0 );
  }

  protected void CheckResult(
      EditorOperator eoPHP,
      String sCheck,
      int iOffset
    )
  {
    String sText = eoPHP.getText( eoPHP.getLineNumber( ) + iOffset );

    // Check code completion list
    if( -1 == sText.indexOf( sCheck ) )
      fail( "Invalid completion: \"" + sText + "\", should be: \"" + sCheck + "\"" );
  }

  protected void CheckResultRegex(
      EditorOperator eoPHP,
      String sCheck
    )
  {
    String sText = eoPHP.getText( eoPHP.getLineNumber( ) );

    // Check code completion list
    if( !sText.matches( sCheck ) )
      fail( "Invalid completion: \"" + sText + "\", should be: \"" + sCheck + "\"" );
  }

  protected void TypeCodeCheckResult(
      EditorOperator eoPHP,
      String sType,
      String sCheck
    )
  {
    TypeCodeCheckResult( eoPHP, sType, sCheck, 0 );
  }

  protected void TypeCodeCheckResult(
      EditorOperator eoPHP,
      String sType,
      String sCheck,
      int iOffset
    )
  {
    TypeCode( eoPHP, sType );
    CheckResult( eoPHP, sCheck, iOffset );
  }

  protected void TypeCodeCheckResultRegex(
      EditorOperator eoPHP,
      String sType,
      String sCheck
    )
  {
    TypeCode( eoPHP, sType );
    CheckResultRegex( eoPHP, sCheck );
  }

  protected void CheckResult( EditorOperator eoCode, String[] asCode, int iOffset )
  {
    for( int i = 0; i < asCode.length; i++ )
      CheckResult( eoCode, asCode[ i ], iOffset + i );
  }

  private class dummyClick implements Runnable
  {
    private JListOperator list;
    private int index, count;
    public dummyClick( JListOperator l, int i, int j )
    {
      list = l;
      index = i;
      count = j;
    }

    public void run( )
    {
      list.clickOnItem( index, count );
    }
  }

  protected void ClickListItemNoBlock(
      JListOperator jlList,
      int iIndex,
      int iCount
    )
  {
    ( new Thread( new dummyClick( jlList, iIndex, iCount ) ) ).start( );
  }

    protected void ClickForTextPopup( EditorOperator eo, String menu )
    {
      JEditorPaneOperator txt = eo.txtEditorPane( );
      JEditorPane epane =  ( JEditorPane )txt.getSource( );
      try
      {
        Rectangle rct = epane.modelToView( epane.getCaretPosition( ) );
        txt.clickForPopup( rct.x, rct.y );
        JPopupMenuOperator popup = new JPopupMenuOperator( );
        popup.pushMenu( menu );
      }
      catch( BadLocationException ex )
      {
        System.out.println( "=== Bad location" );
      }

      return;
    }
}
