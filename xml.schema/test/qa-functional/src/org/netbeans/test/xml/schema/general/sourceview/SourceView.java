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

package org.netbeans.test.xml.schema.general.sourceview;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.OutputOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.test.xml.schema.general.GeneralXMLTest;

/**
 *
 * @author michaelnazarov@netbeans.org
 */

public class SourceView extends GeneralXMLTest {
    
    public SourceView( String arg0 )
    {
      super( arg0 );
    }

    public void CallPopup( EditorOperator code, String name )
    {
      ClickForTextPopup( code );
      JPopupMenuOperator popup = new JPopupMenuOperator( );
      popup.pushMenu( name );
    }

    public void CallPopupNoBlock( EditorOperator code, String name )
    {
      ClickForTextPopup( code );
      JPopupMenuOperator popup = new JPopupMenuOperator( );
      popup.pushMenuNoBlock( name );
    }

    protected void CheckCheckXMLOutput( )
    {
      // "Output - XML Check"
      OutputOperator out = new OutputOperator( );
      String sText = out.getText( );

      String[] asIdealOutputCheck =
      {
        "XML checking started.",
        "Checking file:",
        "XML checking finished."
      };
      // wait till stop line will appear
      while( -1 == sText.indexOf( asIdealOutputCheck[ asIdealOutputCheck.length - 1 ] ) )
      {
        try{ Thread.sleep( 100 ); } catch( InterruptedException ex ) { }
        sText = out.getText( );
      }
      // Find errors
      Pattern p = Pattern.compile( "\\[[0-9]+\\]" );
      Matcher m = p.matcher( sText );
      if( m.find( ) )
        fail( "Errors in the checker output." );

      // Check ideal
      for( String sChecker : asIdealOutputCheck )
      {
        if( -1 == sText.indexOf( sChecker ) )
          fail( "Unable to find ideal XML checker output: \"" + sChecker + "\"\n\"" + sText + "\"" );
      }
    }

    protected void CheckValidateXMLOutput( )
    {
      // "Output - XML Check"
      OutputOperator out = new OutputOperator( );
      String sText = out.getText( );

      String[] asIdealOutputValidate =
      {
        "XML validation started.",
        "0 Error(s),  0 Warning(s).",
        "XML validation finished."
      };
      // wait till stop line will appear
      while( -1 == sText.indexOf( asIdealOutputValidate[ asIdealOutputValidate.length - 1 ] ) )
      {
        try{ Thread.sleep( 100 ); } catch( InterruptedException ex ) { }
        sText = out.getText( );
      }
      // Check ideal
      for( String sValidator: asIdealOutputValidate )
      {
        if( -1 == sText.indexOf( sValidator ) )
          fail( "Unable to find ideal XML validate output: \"" + sValidator + "\"\n\"" + sText + "\"" );
      }

      out.close( );
    }

    protected void CheckTransformationDialog( )
    {
      JDialogOperator jdTrans = new JDialogOperator( "XSL Transformation" );
      JButtonOperator jbCancel = new JButtonOperator( jdTrans, "Cancel" );
      jbCancel.push( );
      jdTrans.waitClosed( );
    }
}
