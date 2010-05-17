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
/*
 * EjbDataSourcesImportor.java
 *
 * Created on September 9, 2004, 6:20 PM
 */

package org.netbeans.modules.visualweb.ejb.load;
import org.netbeans.modules.visualweb.ejb.datamodel.EjbGroup;
import org.netbeans.modules.visualweb.ejb.util.JarExploder;
import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;

/**
 * Import the EJB Groups from the given file
 *
 * @author  cao
 */
public class EjbDataSourcesImportor {
    
    private static final String tmpDir = System.getProperty("java.io.tmpdir");
    private String filePath;
    
    /** Creates a new instance of EjbDataSourcesImportor */
    public EjbDataSourcesImportor( String filePath ) {
        this.filePath = filePath;
    }
    
    public Collection getEjbGroups()
    {
        try
        {
            // Explode the jar file first
            explodeJar();

            // Parse the ejbsource.xml to get the ejb groups
            EjbDataSourceXmlParser parser = new EjbDataSourceXmlParser( tmpDir + File.separator + "ejbsources.xml" ); // NOI18N
            Collection groups = parser.parse();
            
            // Fix the jar location
            for( Iterator iter = groups.iterator(); iter.hasNext(); )
            {
                EjbGroup grp = (EjbGroup)iter.next();
                
                grp.fixJarDir( tmpDir );
            }
            
            return groups;
        }
        catch( Exception ee )
        {
            ee.printStackTrace();
            return null;
        }
    }
    
    private void explodeJar() throws EjbLoadException
    {
        // Explode the jar file into the temp directory
        try {
            File destDir = new File( tmpDir );

            if( !destDir.exists() )
                destDir.mkdirs();
            
            JarExploder exploder = new JarExploder();
            exploder.explodeJar( destDir.getAbsolutePath(), filePath );
            
        } catch( java.io.FileNotFoundException e ) {
            // Log eror
            String logMsg = "Error occurred when trying to import EJB sets. Cannot find file " + filePath;
            ErrorManager.getDefault().getInstance( "org.netbeans.modules.visualweb.ejb.load.DeploymentDescriptorExtractor").log( ErrorManager.ERROR, logMsg );
            
            // Throw up as USER_ERROR
            String errMsg = NbBundle.getMessage( EjbDataSourcesImportor.class, "FILE_NOT_FOUND", filePath );
            throw new EjbLoadException( EjbLoadException.USER_ERROR, errMsg );
        }
        catch( java.io.IOException e )
        {
            // Log eror
            String logMsg = "Error occurred when trying to import EJB sets. Cannot read file " + filePath;
            ErrorManager.getDefault().getInstance( "org.netbeans.modules.visualweb.ejb.load.DeploymentDescriptorExtractor").log( ErrorManager.ERROR, logMsg );
            
            // Throw up as USER_ERROR
            String errMsg = NbBundle.getMessage( EjbDataSourcesImportor.class, "CANNOT_READ_FILE", filePath );
            throw new EjbLoadException( EjbLoadException.USER_ERROR, errMsg );
        }
    }
}
