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

package org.netbeans.test.xml.schema.general.designview;

import java.awt.event.KeyEvent;
import org.netbeans.junit.NbModuleSuite;
import junit.framework.Test;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.operators.AbstractButtonOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.JTextComponentOperator;

/**
 *
 * @author michaelnazarov@netbeans.org
 */

public class DesignView_0002 extends DesignView {

    static final String TEST_JAVA_APP_NAME = "java4designview_0002";

    static final String SCHEMA_SHORT_NAME_1 = "newXmlSchema1";
    static final String SCHEMA_SHORT_NAME_2 = "newXmlSchema2";
    static final String SCHEMA_SHORT_NAME_3 = "newXmlSchema3";
    static final String SCHEMA_SHORT_NAME_4 = "newXmlSchema4";
    static final String SCHEMA_SHORT_NAME_5 = "newXmlSchema5";
    static final String SCHEMA_SHORT_NAME_6 = "newXmlSchema6";
    static final String SCHEMA_SHORT_NAME_7 = "newXmlSchema7";
    static final String SCHEMA_SHORT_NAME_8 = "newXmlSchema8";
    static final String SCHEMA_SHORT_NAME_9 = "newXmlSchema9";

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

    public DesignView_0002(String arg0) {
        super(arg0);
    }

    public static Test suite( )
    {
      return NbModuleSuite.create(
          NbModuleSuite.createConfiguration( DesignView_0002.class ).addTest(
              "CreateJavaApplication",
              "CreateXMLSchema1",
              "RenameUsingF2"

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

    public void CreateXMLSchema1( )
    {
      startTest( );

      CreateSchemaInternal(
          TEST_JAVA_APP_NAME + "|Source Packages|" + TEST_JAVA_APP_NAME,
          SCHEMA_SHORT_NAME_1
        );

      endTest( );
    }

  public void RenameUsingF2( )
  {
    startTest( );

    // Switch to DV
    TopComponentOperator top = new TopComponentOperator( SCHEMA_SHORT_NAME_1 + SCHEMA_EXTENSION );
    AbstractButtonOperator design = new AbstractButtonOperator( top, "Design" );
    design.push( );
    design.clickMouse( );

    // Add element
    // TODO : use DnD
    top.clickForPopup( );
    JPopupMenuOperator popup = new JPopupMenuOperator( );
    popup.pushMenu( "Add|Element" );
    JTextComponentOperator text = new JTextComponentOperator( MainWindowOperator.getDefault( ), 0 );
    top.clickMouse( );//pushKey( KeyEvent.VK_ENTER );
    Sleep( 1500 );
    try
    {
      text = new JTextComponentOperator( MainWindowOperator.getDefault( ), 0 );
      if( !text.getText( ).startsWith( "Search " ) )
        fail( "Text component should gone from DesignView." );
    }
    catch( JemmyException ex )
    {
      // It's okey
    }
    top.pushKey( KeyEvent.VK_RIGHT );
    top.pushKey( KeyEvent.VK_RIGHT );
    top.pushKey( KeyEvent.VK_F2 );
    text = new JTextComponentOperator( MainWindowOperator.getDefault( ), 0 );
    String sText = text.getText( );
    top.clickMouse( );//pushKey( KeyEvent.VK_ENTER );
    if( !sText.equals( "newElement" ) )
      fail( "Unknown Element opened for renaming: \"" + sText + "\"" );

    endTest( );
  }
}
