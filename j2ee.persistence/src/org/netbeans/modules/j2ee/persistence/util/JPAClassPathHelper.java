/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.persistence.util;

import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.modules.j2ee.persistence.provider.ProviderUtil;
import org.netbeans.modules.j2ee.persistence.wizard.library.PersistenceLibrarySupport;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.util.Parameters;

/**
 * A helper class for ensuring that the class path is correctly
 * set for generating classes that require the Java Persistence API, such 
 * as entity classes.
 */
public class JPAClassPathHelper {
    
    private final Set<ClassPath> boot;
    private final Set<ClassPath> compile;
    private final Set<ClassPath> source;
    
    /**
     * Creates a new JPAClassPathHelper. 
     * 
     * @param boot the boot class paths. Must not be null.
     * @param compile the compile class paths. Must not be null.
     * @param source the source class paths. Must not be null.
     */ 
    public JPAClassPathHelper(Set<ClassPath> boot, Set<ClassPath> compile, Set<ClassPath> source){
        Parameters.notNull("boot", boot);
        Parameters.notNull("compile", compile);
        Parameters.notNull("source", source);
        this.boot = new HashSet<ClassPath>(boot);
        this.compile = new HashSet<ClassPath>(compile);
        this.source = new HashSet<ClassPath>(source);
    }
    
    /**
     * Creates a ClassPathInfo (based on our class paths) that can be used for generating entities.
     * Ensures that the compile class path has the Java Persistence API present by checking
     * whether JPA is already present in the compile class path and if it isn't, adds
     * an appropriate JPA library to the compile class path. It is the client's responsibility
     * to make sure that the IDE has a library that contains the Java Persistence API. If no
     * appropriate library could be found, an IllegalStateException is thrown. 
     * 
     * @return the ClassPathInfo for generating entities.
     * @throws IllegalStateException if there were no libraries in the IDE that 
     * contain the Java Persistence API.
     */ 
    public ClasspathInfo createClasspathInfo(){
        
        if (!ensureJPA()){
            throw new IllegalStateException("Cannot find a Java Persistence API library"); // NOI18N
        }
        
        return ClasspathInfo.create(
                createProxyClassPath(boot),
                createProxyClassPath(compile),
                createProxyClassPath(source)
                );
    }
    
    /**
     * Ensure that the compile class path has the Java Persistence API present. Checks
     * whether JPA is already present in the compile class path and if not, tries to 
     * find an appropriate JPA library and add it to the compile class path.
     * 
     * @return true if the compile class path contained or could be made to contain
     * the Java Persistence API.
     */  
    private boolean ensureJPA() {
        for (ClassPath classPath : compile) {
            if (classPath.findResource("javax/persistence/Entity.class") != null) { // NOI18N
                return true;
            }
        }
        ClassPath jpaClassPath = findJPALibrary();
        if (jpaClassPath != null) {
            compile.add(jpaClassPath);
            return true;
        }
        
        return false;
    }

    private ClassPath findJPALibrary() {
        Library library = PersistenceLibrarySupport.getFirstProviderLibrary();
        if (library == null) {
            return null;
        }
        List<URL> urls = library.getContent("classpath"); // NOI18N
        return ClassPathSupport.createClassPath(urls.toArray(new URL[urls.size()]));
    }
    
    
    private ClassPath createProxyClassPath(Set<ClassPath> classPaths) {
        return ClassPathSupport.createProxyClassPath(classPaths.toArray(new ClassPath[classPaths.size()]));
    }
}
