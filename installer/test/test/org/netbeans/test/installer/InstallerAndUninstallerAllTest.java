/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance
 * with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html or
 * http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file and
 * include the License file at http://www.netbeans.org/cddl.txt. If applicable, add
 * the following below the CDDL Header, with the fields enclosed by brackets []
 * replaced by your own identifying information:
 *
 *     "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 *
 * $Id$
 *
 */

package org.netbeans.test.installer;

import java.util.logging.Logger;
import java.util.*;

/**
 *
 * @author Mikhail Vaysman
 */
public class InstallerAndUninstallerAllTest
{
  @org.junit.Test
  public void testInstaller( )
  {
    String sInstallerType = System.getProperty(
        "test.installer.bundle.name.suffix",
        "all"
      );

    //Properties pp = System.getProperties( );
    //pp.list( System.out );

    Installer I = null;

    if( sInstallerType.equals( "all" ) )
      I = new Installer( );
    else
    if( sInstallerType.equals( "javase" ) )
      I = new TestInstallerAndUninstallerJavaSE( );
    else
    if( sInstallerType.equals( "java" ) )
      I = new TestInstallerAndUninstallerJava( );
    else
    if( sInstallerType.equals( "ruby" ) )
      I = new TestInstallerAndUninstallerRuby( );
    else
    if( sInstallerType.equals( "cpp" ) )
      I = new TestInstallerAndUninstallerCPP( );
    else
    if( sInstallerType.equals( "php" ) )
      I = new TestInstallerAndUninstallerPHP( );

    I.testInstaller( );
  }

    public static void main(String[] args) {
        org.junit.runner.JUnitCore.runClasses(InstallerAndUninstallerAllTest.class);
    }
}

/*
Sample start:
set WORKSPACE=path_to_workspace
ant -Djavac.classpath=.:../../jemmy/external/jemmy-2.3.0.0.jar -Dtest-sys-prop.test.installer.url.prefix=http://smetiste.czech.sun.com/builds/netbeans/trunk/latest_daily -Dtest-sys-prop.test.installer.bundle.name.prefix=netbeans-trunk-nightly -Dtest-sys-prop.test.use.build.number=true -Dtest-sys-prop.test.installer.bundle.name.suffix=php test
*/
