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
package org.netbeans.modules.apisupport.project.queries;

import org.netbeans.spi.java.queries.SourceLevelQueryImplementation;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileObject;
import org.netbeans.modules.apisupport.project.*;

/**
 * Returns source level of NB module sources.
 * @author David Konecny
 */
public class SourceLevelQueryImpl implements SourceLevelQueryImplementation {

    private NbModuleProject project;
    private PropertyEvaluator evaluator;

    public SourceLevelQueryImpl(NbModuleProject project, PropertyEvaluator evaluator) {
        this.project = project;
        this.evaluator = evaluator;
    }

    public String getSourceLevel(FileObject javaFile) {
        return project.getJavacSource();
    }
    
}
