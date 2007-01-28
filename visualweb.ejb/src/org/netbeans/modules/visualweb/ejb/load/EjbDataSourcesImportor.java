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
