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

package org.netbeans.modules.java.freeform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.modules.ant.freeform.spi.ProjectNature;
import org.netbeans.modules.ant.freeform.spi.TargetDescriptor;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.support.GenericSources;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileObject;

/**
 * General hook for registration of the Java nature for freeform projects.
 * @author David Konecny
 */
public class JavaProjectNature implements ProjectNature {

    public static final String NS_JAVA_1 = "http://www.netbeans.org/ns/freeform-project-java/1"; // NOI18N
    public static final String NS_JAVA_2 = "http://www.netbeans.org/ns/freeform-project-java/2"; // NOI18N
    public static final String EL_JAVA = "java-data"; // NOI18N
    private static final String SCHEMA_1 = "nbres:/org/netbeans/modules/java/freeform/resources/freeform-project-java.xsd"; // NOI18N
    private static final String SCHEMA_2 = "nbres:/org/netbeans/modules/java/freeform/resources/freeform-project-java-2.xsd"; // NOI18N
    public static final String STYLE_PACKAGES = "packages"; // NOI18N
    
    
    public JavaProjectNature() {}
    
    public List<TargetDescriptor> getExtraTargets(Project project, AntProjectHelper projectHelper, PropertyEvaluator projectEvaluator, AuxiliaryConfiguration aux) {
        return new ArrayList<TargetDescriptor>();
    }

    public Set<String> getSchemas() {
        return new HashSet<String>(Arrays.asList(SCHEMA_1, SCHEMA_2));
    }

    public Set<String> getSourceFolderViewStyles() {
        return Collections.singleton(STYLE_PACKAGES);
    }
    
    public org.openide.nodes.Node createSourceFolderView(Project project, FileObject folder, String style, String name, String displayName) throws IllegalArgumentException {
        if (style.equals(STYLE_PACKAGES)) {
            if (displayName == null) {
                // Don't use folder.getNodeDelegate().getDisplayName() since we are not listening to changes anyway.
                displayName = folder.getNameExt();
            }
            return PackageView.createPackageView(GenericSources.group(project, folder, name, displayName, null, null));
        } else {
            throw new IllegalArgumentException();
        }
    }

    public org.openide.nodes.Node findSourceFolderViewPath(Project project, org.openide.nodes.Node root, Object target) {
        return PackageView.findPath(root, target);
    }

    





    
}
