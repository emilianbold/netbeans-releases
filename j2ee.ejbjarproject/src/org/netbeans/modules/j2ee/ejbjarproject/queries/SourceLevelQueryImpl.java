/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.j2ee.ejbjarproject.queries;

import org.netbeans.spi.java.queries.SourceLevelQueryImplementation;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileObject;

/**
 * Returns source level of project sources.
 * @author David Konecny
 */
public class SourceLevelQueryImpl implements SourceLevelQueryImplementation {

    private final AntProjectHelper helper;
    private final PropertyEvaluator evaluator;

    public SourceLevelQueryImpl(AntProjectHelper helper, PropertyEvaluator evaluator) {
        this.helper = helper;
        this.evaluator = evaluator;
    }

    public String getSourceLevel(FileObject javaFile) {
        String sl = evaluator.getProperty("javac.source");
        if (sl != null && sl.length() > 0) {
            return sl;
        } else {
            return null;
        }
    }
    
}
