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

package org.netbeans.test.xml.schema.general.pattern;

import org.netbeans.jemmy.JemmyException;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JToggleButtonOperator;
import org.netbeans.test.xml.schema.general.GeneralXMLTest;

/**
 *
 * @author michaelnazarov@netbeans.org
 */

public class Pattern extends GeneralXMLTest {
    
    public Pattern( String arg0 )
    {
      super( arg0 );
    }

    public JToggleButtonOperator button( JDialogOperator dlg, String name )
    {
      for( int i = 0; ; i++ )
      {
        JToggleButtonOperator b = new JToggleButtonOperator( dlg, name, i );
        String text = b.getText( );
        if( text.equals( name ) )
          return b;
      }
    }

    public void ApplyPatternInternal(
        String path,
        String element,
        String type,
        String schema
      )
    {
      ProjectsTabOperator pto = new ProjectsTabOperator( );
      ProjectRootNode prn = pto.getProjectRootNode( path );
      prn.select( );

      // Apply pattern
      prn.performPopupActionNoBlock( "Apply Design Pattern..." );
      JDialogOperator jdPattern = new JDialogOperator( "Apply Design Pattern" );
      if( null != element )
      {
        JToggleButtonOperator jtb = button( jdPattern, element );
        jtb.setSelected( true );
        jtb.clickMouse( );
      }
      if( null != type )
      {
        JToggleButtonOperator jtb = button( jdPattern, type );
        jtb.setSelected( true );
        jtb.clickMouse( );
      }
      JButtonOperator jbFinish = new JButtonOperator( jdPattern, "Finish" );
      jbFinish.push( );
      jdPattern.waitClosed( );
      JDialogOperator jdProcess = new JDialogOperator( "Applying Design Pattern \"" + schema + "\"..." );

      boolean bRedo = true;
      while( bRedo )
      {
        try
        {
          jdProcess.waitClosed( );
          bRedo = false;
        }
        catch( JemmyException ex )
        {
          System.out.println( "Waiting apply completion..." );
        }
      }
    }

    public void ApplySalami( String path, String schemaName )
    {
      ApplyPatternInternal(
          path,
          "Create Multiple Global Elements",
          "Do not Create Type(s)",
          schemaName
        );
    }

    public void ApplyGoE( String path, String schemaName )
    {
      ApplyPatternInternal(
          path,
          "Create Multiple Global Elements",
          "Create Type(s)",
          schemaName
        );
    }

    public void ApplyRussianDoll( String path, String schemaName )
    {
      ApplyPatternInternal(
          path,
          "Create a Single Global Element",
          "Do not Create Type(s)",
          schemaName
        );
    }

    public void ApplyVenetian( String path, String schemaName )
    {
      ApplyPatternInternal(
          path,
          "Create a Single Global Element",
          "Create Type(s)",
          schemaName
        );
    }
}
