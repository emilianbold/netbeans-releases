/*
 * EjbLoaderHelper.java
 *
 * Created on March 9, 2005, 10:10 PM
 */

package org.netbeans.modules.visualweb.ejb.load;

import org.netbeans.modules.visualweb.ejb.datamodel.EjbGroup;
import org.netbeans.modules.visualweb.ejb.util.JarExploder;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * @author  cao
 */
public class EjbLoaderHelper {
    
    public EjbLoaderHelper() {
    }
    
    public static URLClassLoader getEjbGroupClassLoader( EjbGroup ejbGroup ) {
        
        try {
            // Class loader for loading the classes in the client jar files
            // and the ejb-2.0.jar (it contains the super classes/interfaces for the EJBs)
            
            URL[] clientJarURLs = new URL[ ejbGroup.getClientJarFiles().size() + 1 ];
            
            // Client jars
            ArrayList jarFileNames = ejbGroup.getClientJarFiles();
            for( int i = 0; i < jarFileNames.size(); i ++ ) {
                String fileName = (String)jarFileNames.get( i );
                clientJarURLs[i] = new File(fileName).toURL();
                
            }
            
            // ejb-2.0.jar
            clientJarURLs[jarFileNames.size()] = new File(EjbLoader.ejb20Jar).toURL();
            
            URLClassLoader classloader = URLClassLoader.newInstance( clientJarURLs );
            
            return classloader;
            
        }
        catch( java.net.MalformedURLException e ) {
            // Log error
            String logMsg = "Error occurred when trying load classes from " + ejbGroup.getClientJarFiles().toString();
            
            e.printStackTrace();
            
            return null;
        }
    }
    
    public static ArrayList getAllClazz( EjbGroup ejbGroup ) {
        
        try {
           
            ArrayList allClazz = new ArrayList();

            for( Iterator iter = ejbGroup.getClientJarFiles().iterator(); iter.hasNext(); ) {
                String jarFile = (String)iter.next();
                allClazz.addAll( JarExploder.getAllClasses( jarFile ) );
            } 
            
            // Remove the invalid ones
            //URLClassLoader classloader = getEjbGroupClassLoader( ejbGroup );
            for( Iterator iter = allClazz.iterator(); iter.hasNext(); ) {
                String className = (String)iter.next();
                
                // No internal classes for now
                if( className.indexOf( '$' ) != -1 )
                {
                    iter.remove();
                    continue;
                }
                
                /*try {
                    Class.forName( className, false, classloader );
                } catch( java.lang.ClassNotFoundException e ) {
                    iter.remove();
                }*/
            }

            return allClazz;
        } catch( Exception e )
        {
            e.printStackTrace();
            
            return new ArrayList();
        }
    }
}
