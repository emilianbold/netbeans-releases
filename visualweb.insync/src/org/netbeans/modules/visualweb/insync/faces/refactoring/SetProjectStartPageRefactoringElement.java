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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.visualweb.insync.faces.refactoring;

import org.netbeans.api.project.Project;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
import org.netbeans.modules.visualweb.project.jsf.api.JsfProjectUtils;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.openide.filesystems.FileObject;
import org.openide.text.PositionBounds;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;


public class SetProjectStartPageRefactoringElement extends SimpleRefactoringElementImplementation {

    private final Project project;
    private final String oldStartPageName;
    private final String newStartPageName;
    private final String displayText;

    public SetProjectStartPageRefactoringElement(Project project, String oldStartPageName, String newStartPageName) {
        this.project = project;
        this.oldStartPageName = oldStartPageName;
        this.newStartPageName = newStartPageName;
        displayText = NbBundle.getMessage(SetProjectStartPageRefactoringElement.class, "LBL_SetProjectStartPage", newStartPageName); // NOI18N
    }

    public String getDisplayText() {
        return displayText;
    }

    public Lookup getLookup() {
        return Lookup.EMPTY;
    }

    public FileObject getParentFile() {
        WebModule webModule = WebModule.getWebModule(project.getProjectDirectory());
        if (webModule != null){
            FileObject webXml = webModule.getDeploymentDescriptor ();
            if (webXml != null) {
                return webXml;
            }
        }
        return project.getProjectDirectory();
    }

    public PositionBounds getPosition() {
        return null;
    }

    public String getText() {
        return null;
    }

    public void performChange() {
        JsfProjectUtils.setStartPage(project, newStartPageName);
    }
    
    @Override
    public void undoChange() {
        JsfProjectUtils.setStartPage(project, oldStartPageName);
    }
}

