/*
 * ClassPathProviderImpl.java
 *
 * Created on 22 November 2006, 13:55
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.j2ee.persistence.sourcetestsupport;

import java.net.URL;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * An implementation of ClassPathProvider for running tests. Includes <tt>toplink-essentials.jar</tt> that contains
 * <code>javax.persistence.*</code> stuff. 
 *
 * @author Erno Mononen
 */
public class ClassPathProviderImpl implements ClassPathProvider {
    
    public ClassPathProviderImpl(){
    }
    
    public ClassPath findClassPath(FileObject file, String type) {
        if (type == ClassPath.SOURCE) {
            return null;
        }
        if (type == ClassPath.COMPILE){
            try {
                URL toplinkJarUrl = Class.forName("javax.persistence.EntityManager").getProtectionDomain().getCodeSource().getLocation();
                return ClassPathSupport.createClassPath(new URL[]{FileUtil.getArchiveRoot(toplinkJarUrl)});
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
        if (type == ClassPath.BOOT){
            return null;
        }
        return null;
    }
}
