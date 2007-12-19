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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.php.rt.providers.impl.local.apache;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;




/**
 * @author ads
 *
 */
class SolarisPackageFinder {
    
    static final String PKGINFO                 = "pkginfo";    // NOI18N
    
    static final String GREP                    = "grep";       // NOI18N

    static final String USR_BIN                 = "/usr/bin";   // NOI18N
    
    static final String WHICH                   = "which";      // NOI18N
    
    static final String CATEGORY                = "application";// NOI18N
    
    static final String PKG_KEY                 = "apache";     // NOI18N
    
    static final String SADM_CONTENTS          = 
                        "/var/sadm/install/contents";           // NOI18N

    private static final String SXDE_CONF_LOCATION          = 
                        "/etc/apache2/2.2/httpd.conf";           // NOI18N
    
    private static Logger LOGGER = Logger.getLogger(SolarisPackageFinder.class.getName());

    public String[] getPlatformLocations() {
        if ( !whichExists() ) {
            return null;
        }
        Collection<String> pkgNames = getPkgNames();
        if ( pkgNames == null ) {
            return null;
        }
        List<String> list = new ArrayList<String>();
        for ( String name : pkgNames ) {
            Collection<String> location = getConfigsLocations( name );
            list.addAll(location);
        }
        // hack for SXDE to show default config at the top
        if (list.contains(SXDE_CONF_LOCATION)){
            list.remove(SXDE_CONF_LOCATION);
            list.add(0, SXDE_CONF_LOCATION);
        }
        return list.toArray( new String[ list.size()] );
    }

    private Collection<String> getConfigsLocations( String name ) {
        String grep = getGrep();
        Runtime runtime = Runtime.getRuntime(); 
        List<String> list = new ArrayList<String>();
        try {
            Process process = 
                runtime.exec( new String[] { grep , name , SADM_CONTENTS} );
            LOGGER.info(">>>> "+grep+" "+name+" in "+SADM_CONTENTS);
            InputStream stream = process.getInputStream();
            BufferedReader reader = new BufferedReader( 
                    new InputStreamReader( stream ) );
            String line;
            while ( (line = reader.readLine()) != null ) {
                /* Do not use PATH_TO_HTTPD_CONF because it's place in SAMP 
                 * is differnt.
                 */
                //String httpdConf = ServerChooserVisual.PATH_TO_HTTPD_CONF;
                String httpdConf = ServerChooserVisual.HTTPD_CONF;
                if ( line.contains( httpdConf )) {
                    LOGGER.info(">>>>> line contains httpd");
                    line = line.replace( '\t', ' ');
                    int httpdIndex = line.indexOf(httpdConf);
                    int locationEndIndex = line.indexOf(' ', httpdIndex);
                    // if name is 'httpd.conf', not e.g. 'httpd.conf-example'
                    if (locationEndIndex == httpdIndex+httpdConf.length()){
                        list.add( line.substring( 0, locationEndIndex) );
                    }
                }
            }
        }
        catch (IOException e) {
            // just do not fill list with locations
        }
        return list;
    }

    private boolean whichExists() {
        String which = getProgramPath(WHICH);
        if (which != null) {
            myWhich = which;
            return true;
        }
        else {
            // try to check which in specified place
            myWhich = USR_BIN+ File.separator +WHICH;
            myWhich = getProgramPath( myWhich );
            if ( myWhich == null ) {
                return false;
            }
            else {
                return true;
            }
        }
    }
    
    private String getPkgInfo() {
        if ( myPkgInfo == null ) {
            myPkgInfo = getProgramPath( PKGINFO );
            if ( myPkgInfo == null ) {
                myPkgInfo = getProgramPath( USR_BIN + File.separator +PKGINFO);
            }
        }
        return myPkgInfo;
    }
    
    private String getGrep() {
        if ( myGrep == null ) {
            myGrep = getProgramPath( GREP );
            if ( myGrep == null ) {
                myGrep = getProgramPath( USR_BIN +File.separator +GREP );
            }
        }
        return myGrep;
    }
    
    private Collection<String> getPkgNames( ) {
        Runtime runtime = Runtime.getRuntime();
        String pkgInfo = getPkgInfo();
        String grep = getGrep();
        if ( pkgInfo == null || grep == null ) {
            return null;
        }
        try {
            /* do not filter by category 
             * because it is different in different solaris versions
             * system if installed as SAMP with SXDE, application otherwise.
             * -c application,system has no sence because number of result 
             * is the same as without category.
             */
            //Process process = runtime.exec( pkgInfo +" -c " +CATEGORY + " -i ");    // NOI18N
            Process process = runtime.exec( pkgInfo +" -i ");    // NOI18N
            BufferedReader reader = new BufferedReader( 
                    new InputStreamReader( process.getInputStream()));
            String line;
            Collection<String> collection = new LinkedList<String>();
            while( (line = reader.readLine())!=null ) {
                if ( !line.toLowerCase().contains(PKG_KEY)) {
                    continue;
                }
                line = cutPkgName(line);
                if ( line != null ) {
                    collection.add( line );
                }
            }
            return collection;
        }
        catch (IOException e) {
            return null;
        }
    }
    
    private String getProgramPath( String program ) {
        Runtime runtime = Runtime.getRuntime();
        String which = myWhich == null? WHICH : myWhich;
        try {
            Process process = runtime.exec( new String[] { which , program} );
            BufferedReader reader = new BufferedReader( 
                    new InputStreamReader( process.getInputStream()));
            String line;
            while( (line = reader.readLine())!=null ) {
                if ( line.endsWith( program )) {
                    return line;
                }
            }
            return null;
        }
        catch (IOException e) {
            return null;
        }
    }
    
    private static String cutPkgName( String str ) {
        //if ( str.startsWith( CATEGORY )) {
            str = str.replace( '\t', ' ');
            int index = str.indexOf( ' ');
            str = str.substring( index );
            str = str.trim();
            index = str.indexOf( ' ');
            str = str.substring( 0, index );
            return str.trim();
        //}
        //return null;
    }
    
    private String myPkgInfo;
    
    private String myGrep;
    
    private String myWhich;
}
