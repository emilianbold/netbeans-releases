/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.uml.project.ui.customizer.uiapi;

import org.openide.util.Lookup;

/**
 * Way of getting implementations of UI components defined in projects/projectui.
 * @author Petr Hrebejk, Jesse Glick
 */
public class Utilities {

    private Utilities() {}

    /** Gets action factory from the global Lookup.
     */
    public static ActionsFactory getActionsFactory() {
        ActionsFactory instance = (ActionsFactory) Lookup.getDefault().lookup(ActionsFactory.class);
        assert instance != null;
        return instance;
    }
    
    /** Gets the projectChooser fatory from the global Lookup
     */
    public static ProjectChooserFactory getProjectChooserFactory() {
        ProjectChooserFactory instance = (ProjectChooserFactory) Lookup.getDefault().lookup(ProjectChooserFactory.class);
        assert instance != null;
        return instance;
    }
    
    /** Gets an object the OpenProjects can delegate to
     */
    public static OpenProjectsTrampoline getOpenProjectsTrampoline() {
        OpenProjectsTrampoline instance = (OpenProjectsTrampoline) Lookup.getDefault().lookup(OpenProjectsTrampoline.class);
        assert instance != null;
        return instance;
    }
    
}
