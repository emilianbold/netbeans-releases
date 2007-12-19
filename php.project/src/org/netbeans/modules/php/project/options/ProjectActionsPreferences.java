/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.project.options;

import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import org.netbeans.modules.php.rt.utils.ServerActionsPreferences;
import org.openide.util.NbPreferences;

/**
 *
 * @author avk
 */
public class ProjectActionsPreferences {
    
    private static final ProjectActionsPreferences INSTANCE = new ProjectActionsPreferences();
    
    private static final String CL_PRINT_TO_OUTPUT = "cl-print-to-output"; // NOI18N
   
    private static final String CL_OPEN_IN_BROWSER = "cl-open-in-browser"; // NOI18N
    
    private ProjectActionsPreferences() {}
    
    public static ProjectActionsPreferences getInstance() {
        return INSTANCE;
    }

    public void setCommandLineRunPrintToOutput(final boolean printToOutput){
        getPreferences().putBoolean(CL_PRINT_TO_OUTPUT, printToOutput);
    }
    
    public boolean getCommandLineRunPrintToOutput(){
        return getPreferences().getBoolean(CL_PRINT_TO_OUTPUT, true);
    }
    
    public void setCommandLineRunOpenInBrowser(final boolean openInBrowser){
        getPreferences().putBoolean(CL_OPEN_IN_BROWSER, openInBrowser);
    }
    
    public boolean getCommandLineRunOpenInBrowser(){
        return getPreferences().getBoolean(CL_OPEN_IN_BROWSER, false);
    }
    
    public void setFileOverwrite(final Boolean overwrite){
        ServerActionsPreferences.getInstance().setFileOverwrite(overwrite);
    }
    
    public Boolean getFileOverwrite(){
        return ServerActionsPreferences.getInstance().getFileOverwrite();
    }
    
    public void addPreferenceChangeListener(PreferenceChangeListener pcl){
        getPreferences().addPreferenceChangeListener(pcl);
    }

    public void removePreferenceChangeListener(PreferenceChangeListener pcl){
        getPreferences().removePreferenceChangeListener(pcl);
    }
    
    
    private Preferences getPreferences() {
        return NbPreferences.forModule(ProjectActionsPreferences.class);
    }
    
    
}
