/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

package org.netbeans.test.xml.schema;

import org.netbeans.test.xml.schema.sequential.PropertyVerifier;
import org.netbeans.test.xml.schema.lib.sequential.SequentialTestSuite;
import org.netbeans.test.xml.schema.lib.util.Helpers;

import org.netbeans.junit.NbModuleSuite;
import junit.framework.Test;
import junit.framework.TestResult;

import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;
import java.util.Enumeration;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.io.IOException;

import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.Timeouts;

/**
 *
 * @author ca@netbeans.org
 */
public class Synt01PropertyVerifier extends PropertyVerifier {
    
    /*
    public static junit.framework.TestSuite suite() {
        junit.framework.TestSuite testSuite = new SequentialTestSuite("Synt01 Property Verifier", new Synt01PropertyVerifier());
        
        return testSuite;
    }
    */
    
    public static Test suite( )
    {
      return NbModuleSuite.create(
          NbModuleSuite.createConfiguration( Synt01PropertyVerifier.class ).addTest(
              "TestStarter"
           )
           .enableModules( ".*" )
           .clusters( ".*" )
           .gui( true )
        );
    }

    protected String getSchemaName() {
        return  "Synt01";
    }
    
    protected int getFirstLine() {
        return 5;
    }

    protected void startTest(){
        super.startTest();
        Helpers.closeUMLWarningIfOpened();
    }

  public Synt01PropertyVerifier( String s )
  {
    super( s );
  }

  public Synt01PropertyVerifier( )
  {
    super( );
  }

  public void TestStarter( )
  {
    junit.framework.TestSuite testSuite = new SequentialTestSuite("Synt01 Property Verifier", new Synt01PropertyVerifier());
    TestResult t = new TestResult( );
    testSuite.run( t );
  }    

    public void setUp( )
    {
      try
      {
        String sBase = System.getProperty( "nbjunit.workdir" ) + File.separator + ".." + File.separator + "data" + File.separator;
        // Extract zip data
        ZipFile zf = new ZipFile( sBase + "projects.zip" );
        Enumeration<? extends ZipEntry> ent = zf.entries( );
        while( ent.hasMoreElements( ) )
        {
          ZipEntry e = ent.nextElement( );
          String name = e.getName( );
          if( e.isDirectory( ) )
          {
            ( new File( sBase + name ) ).mkdirs( );
          }
          else
          {
            InputStream is = zf.getInputStream( e );
            //File f = new File( name );
            //System.out.println( "-->" + f.getPath( ) );
            OutputStream os = new FileOutputStream( sBase + name );
            int r;
            byte[] b = new byte[ 1024 ];
            while( -1 != ( r = is.read( b ) ) )
              os.write( b, 0, r );
            is.close( );
            os.flush( );
            os.close( );
          }
        }
        zf.close( );

        // Open project
        openDataProjects( "XSDTestProject" );
      }
      catch( IOException ex )
      {
        System.out.println( "+++ Projects failed +++" );
      }
    }
}
