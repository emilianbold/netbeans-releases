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

package org.netbeans.modules.web.freeform;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.modules.ant.freeform.spi.ProjectNature;
import org.netbeans.modules.ant.freeform.spi.TargetDescriptor;
import org.netbeans.modules.web.api.webmodule.WebProjectConstants;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 * @author David Konecny
 */
public class WebProjectNature implements ProjectNature {

    public static final String NS_WEB_1 = "http://www.netbeans.org/ns/freeform-project-web/1"; // NOI18N
    private static final String SCHEMA_1 = "nbres:/org/netbeans/modules/web/freeform/resources/freeform-project-web.xsd"; // NOI18N
    public static final String EL_WEB = "web-data";
    public static final String NS_WEB_2 = "http://www.netbeans.org/ns/freeform-project-web/2"; // NOI18N
    private static final String SCHEMA_2 = "nbres:/org/netbeans/modules/web/freeform/resources/freeform-project-web-2.xsd"; // NOI18N
    
  
    public WebProjectNature() {}
    
    public List getExtraTargets(Project project, AntProjectHelper projectHelper, PropertyEvaluator projectEvaluator, AuxiliaryConfiguration aux) {
        ArrayList l = new ArrayList();
        if (!LookupProviderImpl.isMyProject(aux)) {
            return l;
        }
        l.add(getExtraTarget());
        return l;
    }
    
    public Set<String> getSchemas() {
        return new HashSet<String>(Arrays.asList(SCHEMA_1, SCHEMA_2));
    }

    public Set/*<String>*/ getSourceFolderViewStyles() {
        return Collections.EMPTY_SET;
    }
    
    public Node createSourceFolderView(Project project, FileObject folder, String includes, String excludes, String style, String name, String displayName) throws IllegalArgumentException {
        throw new IllegalArgumentException();
    }

    public Node findSourceFolderViewPath(Project project, Node root, Object target) {
        return null;
    }
    
    public static TargetDescriptor getExtraTarget() {
        return new TargetDescriptor(WebProjectConstants.COMMAND_REDEPLOY, Arrays.asList(new String[]{"deploy", ".*deploy.*"}),  // NOI18N
            NbBundle.getMessage(WebProjectNature.class, "LBL_TargetMappingPanel_Deploy"), // NOI18N
            NbBundle.getMessage(WebProjectNature.class, "ACSD_TargetMappingPanel_Deploy")); // NOI18N
    }
}
