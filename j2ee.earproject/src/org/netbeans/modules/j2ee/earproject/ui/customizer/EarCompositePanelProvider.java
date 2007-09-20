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

package org.netbeans.modules.j2ee.earproject.ui.customizer;

import java.util.ResourceBundle;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author mkleint, tmysik
 */
public class EarCompositePanelProvider implements ProjectCustomizer.CompositeCategoryProvider {
    
    private static final String SOURCES = "Sources";
    static final String LIBRARIES = "Libraries";
    
    private static final String EAR = "Ear";
    public static final String RUN = "Run";
//    private static final String RUN_TESTS = "RunTests";

    private String name;
    
    /** Creates a new instance of EarCompositePanelProvider */
    public EarCompositePanelProvider(String name) {
        this.name = name;
    }

    public ProjectCustomizer.Category createCategory(Lookup context) {
        ResourceBundle bundle = NbBundle.getBundle(CustomizerProviderImpl.class);
        ProjectCustomizer.Category toReturn = null;
        if (SOURCES.equals(name)) {
            toReturn = ProjectCustomizer.Category.create(
                    SOURCES,
                    bundle.getString("LBL_Config_Sources"), // NOI18N
                    null);
        } else if (LIBRARIES.equals(name)) {
            toReturn = ProjectCustomizer.Category.create(
                    LIBRARIES,
                    bundle.getString("LBL_Config_Libraries"), // NOI18N
                    null);
        } else if (EAR.equals(name)) {
            toReturn = ProjectCustomizer.Category.create(
                    EAR,
                    bundle.getString("LBL_Config_Ear"), // NOI18N
                    null);
        } else if (RUN.equals(name)) {
            toReturn = ProjectCustomizer.Category.create(
                    RUN,
                    bundle.getString("LBL_Config_Run"), // NOI18N
                    null);
        }
        assert toReturn != null : "No category for name:" + name;
        return toReturn;
    }

    public JComponent createComponent(ProjectCustomizer.Category category, Lookup context) {
        String nm = category.getName();
        EarProjectProperties uiProps = context.lookup(EarProjectProperties.class);
        if (SOURCES.equals(nm)) {
            return new CustomizerGeneral(uiProps);
        } else if (LIBRARIES.equals(nm)) {
            return new CustomizerLibraries(uiProps);
        } else if (EAR.equals(nm)) {
            return new CustomizerJarContent(uiProps);
        } else if (RUN.equals(nm)) {
            return new CustomizerRun(uiProps);
        }
        return new JPanel();
    }

    public static EarCompositePanelProvider createSources() {
        return new EarCompositePanelProvider(SOURCES);
    }

    public static EarCompositePanelProvider createLibraries() {
        return new EarCompositePanelProvider(LIBRARIES);
    }

    public static EarCompositePanelProvider createEar() {
        return new EarCompositePanelProvider(EAR);
    }

    public static EarCompositePanelProvider createRun() {
        return new EarCompositePanelProvider(RUN);
    }
}
