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

package org.netbeans.modules.mobility.project.queries;
import org.netbeans.modules.mobility.project.DefaultPropertiesDescriptor;
import org.netbeans.modules.mobility.project.J2MEProjectUtils;
import org.netbeans.spi.java.queries.SourceLevelQueryImplementation;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileObject;

/**
 * Returns source level of project sources.
 * @author Adam Sotona
 */
public class SourceLevelQueryImpl implements SourceLevelQueryImplementation {
    
    private final AntProjectHelper helper;
    
    public SourceLevelQueryImpl(AntProjectHelper helper) {
        this.helper = helper;
    }
    
    public String getSourceLevel(@SuppressWarnings("unused")
	final FileObject javaFile) {
        final String version = J2MEProjectUtils.evaluateProperty(helper, DefaultPropertiesDescriptor.JAVAC_SOURCE);
        return version == null ? "1.3" : version; //NOI18N
    }
    
}

