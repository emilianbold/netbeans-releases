/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.spi.java.project.classpath.support;

import java.io.File;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;

/**
 * ProjectClassPathSupport is a support class for creating classpath based
 * on the list of ant properties.
 * @since org.netbeans.modules.java.project/1 1.3 
 * @author Tomas Zezula
 */
public class ProjectClassPathSupport {
        
    /** Creates a new instance of NewClass */
    private ProjectClassPathSupport() {
    }
    
    
    /**
     * Creates new classpath based on the ant property. The returned classpath
     * listens on changes of property value.
     * @param projectFolder {@link File} the project folder used to resolve relative paths
     * @param evaluator {@link PropertyEvaluator} used for obtaining the value of
     * given property and listening on value changes.
     * @param propertyNames the names of ant properties the classpath will be build on,
     * can't be or contain null. It can contain duplicates, in this case the duplicated property
     * is used multiple times. The values of given properties are concatenated into a single path.
     * @return an {@link ClassPathImplementation} based on the given ant properties.
     */
    public static ClassPathImplementation createPropertyBasedClassPathImplementation (File projectFolder,
            PropertyEvaluator evaluator, String[] propertyNames) {
        return new ProjectClassPathImplementation (projectFolder, propertyNames, evaluator);
    }
    
}
