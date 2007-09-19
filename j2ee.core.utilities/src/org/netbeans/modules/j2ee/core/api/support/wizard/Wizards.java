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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.j2ee.core.api.support.wizard;

import javax.swing.JComponent;
import org.openide.WizardDescriptor;

/**
 * This class consists of static utility methods for manipulating wizards.
 */ 
public final class Wizards {
    
    private static final String WIZARD_PANEL_CONTENT_DATA = "WizardPanel_contentData"; // NOI18N
    private static final String WIZARD_PANEL_CONTENT_SELECTED_INDEX = "WizardPanel_contentSelectedIndex"; //NOI18N;

    private Wizards() {
    }
    
    /**
     * Merges the given <code>steps</code> into the given <code>panels</code>.
     */ 
    public static void mergeSteps(WizardDescriptor wizard, WizardDescriptor.Panel[] panels, String[] steps) {
        Object prop = wizard.getProperty(WIZARD_PANEL_CONTENT_DATA);
        String[] beforeSteps;
        int offset;
        if (prop instanceof String[]) {
            beforeSteps = (String[]) prop;
            offset = beforeSteps.length;
            if (offset > 0 && ("...".equals(beforeSteps[offset - 1]))) {// NOI18N
                offset--;
            }
        } else {
            beforeSteps = null;
            offset = 0;
        }
        String[] resultSteps = new String[ (offset) + panels.length];
        for (int i = 0; i < offset; i++) {
            resultSteps[i] = beforeSteps[i];
        }
        setSteps(panels, steps, resultSteps, offset);
    }
    
    private static void setSteps(WizardDescriptor.Panel[] panels, String[] steps, String[] resultSteps, int offset) {
        int n = steps == null ? 0 : steps.length;
        for (int i = 0; i < panels.length; i++) {
            final JComponent component = (JComponent) panels[i].getComponent();
            String step = i < n ? steps[i] : null;
            if (step == null) {
                step = component.getName();
            }
            component.putClientProperty(WIZARD_PANEL_CONTENT_DATA, resultSteps);
            component.putClientProperty(WIZARD_PANEL_CONTENT_SELECTED_INDEX, new Integer(i));
            component.getAccessibleContext().setAccessibleDescription(step);
            resultSteps[i + offset] = step;
        }
    }
    
}
