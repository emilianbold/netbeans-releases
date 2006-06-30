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
package org.netbeans.modules.java.j2seproject.queries;

import org.netbeans.modules.java.j2seproject.J2SEProjectUtil;
import org.netbeans.spi.java.queries.SourceLevelQueryImplementation;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.api.java.platform.Specification;
import org.openide.filesystems.FileObject;

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
        final String activePlatform = evaluator.getProperty ("platform.active");  //NOI18N        
        if (J2SEProjectUtil.getActivePlatform(activePlatform) != null) {
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
