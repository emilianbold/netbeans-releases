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
package org.netbeans.modules.web.project.queries;

import org.openide.filesystems.FileObject;

import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.spi.java.queries.SourceLevelQueryImplementation;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.support.ant.EditableProperties;

/**
 * Returns source level of project sources.
 * @author David Konecny
 */
public class SourceLevelQueryImpl implements SourceLevelQueryImplementation {

    private final PropertyEvaluator evaluator;

    public SourceLevelQueryImpl(PropertyEvaluator evaluator) {
        this.evaluator = evaluator;
    }

    public String getSourceLevel(FileObject javaFile) {
        boolean platformExists = false;
        String activePlatform = evaluator.getProperty ("platform.active");  //NOI18N
        if (activePlatform != null && activePlatform.length()>0) {
            JavaPlatform[] j2sePlatforms = JavaPlatformManager.getDefault().getPlatforms(null, new Specification("j2se",null)); //NOI18N
            for (int i=0; i< j2sePlatforms.length; i++) {
                String antName = (String) j2sePlatforms[i].getProperties().get("platform.ant.name");        //NOI18N
                if (antName != null && antName.equals(activePlatform)) {
                    platformExists = true;
                    break;
                }
            }
        }
        if (platformExists) {
            String sl = evaluator.getProperty("javac.source");  //NOI18N
            if (sl != null && sl.length() > 0) {
                return sl;
            } else {
                return null;
            }
        }
        else {
            EditableProperties props = PropertyUtils.getGlobalProperties();
            String sl = (String) props.get("default.javac.source"); //NOI18N
            if (sl != null && sl.length() > 0) {
                return sl;
            } else {
                return null;
            }
        }
    }
    
}
