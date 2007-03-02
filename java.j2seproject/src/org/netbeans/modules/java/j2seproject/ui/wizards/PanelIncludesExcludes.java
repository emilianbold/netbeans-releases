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

package org.netbeans.modules.java.j2seproject.ui.wizards;

import java.awt.Component;
import java.io.File;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.java.j2seproject.ui.customizer.J2SEProjectProperties;
import org.netbeans.spi.java.project.support.ui.IncludeExcludeVisualizer;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

/**
 * New project from existing sources panel to configure includes and excludes.
 */
class PanelIncludesExcludes implements WizardDescriptor.FinishablePanel {

    private final IncludeExcludeVisualizer viz;

    public PanelIncludesExcludes() {
        viz = new IncludeExcludeVisualizer();
    }

    public boolean isFinishPanel() {
        return true;
    }

    public Component getComponent() {
        return viz.getVisualizerPanel();
    }

    public void addChangeListener(ChangeListener l) {}

    public void removeChangeListener(ChangeListener l) {}

    public boolean isValid() {
        return true;
    }

    public void storeSettings(Object wiz) {
        WizardDescriptor w = (WizardDescriptor) wiz;
        w.putProperty(J2SEProjectProperties.INCLUDES, viz.getIncludePattern());
        w.putProperty(J2SEProjectProperties.EXCLUDES, viz.getExcludePattern());
    }

    public void readSettings(Object wiz) {
        WizardDescriptor w = (WizardDescriptor) wiz;
        String includes = (String) w.getProperty(J2SEProjectProperties.INCLUDES);
        if (includes == null) {
            includes = "**"; // NOI18N
        }
        viz.setIncludePattern(includes);
        String excludes = (String) w.getProperty(J2SEProjectProperties.EXCLUDES);
        if (excludes == null) {
            excludes = ""; // NOI18N
        }
        viz.setExcludePattern(excludes);
        File[] sourceRoots = (File[]) w.getProperty("sourceRoot");
        File[] testRoots = (File[]) w.getProperty("testRoot");
        File[] roots = new File[sourceRoots.length + testRoots.length];
        System.arraycopy(sourceRoots, 0, roots, 0, sourceRoots.length);
        System.arraycopy(testRoots, 0, roots, sourceRoots.length, testRoots.length);
        viz.setRoots(roots);
    }

    public HelpCtx getHelp() {
        return new HelpCtx(PanelIncludesExcludes.class);
    }

}
