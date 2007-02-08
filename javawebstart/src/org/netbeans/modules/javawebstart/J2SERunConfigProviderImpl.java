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

package org.netbeans.modules.javawebstart;

import java.util.Map;

import javax.swing.JComponent;

import org.netbeans.api.project.Project;

import org.netbeans.modules.java.j2seproject.api.J2SERunConfigProvider;
import org.netbeans.modules.java.j2seproject.api.J2SEPropertyEvaluator;

import org.netbeans.modules.javawebstart.ui.customizer.JWSCustomizerPanel;

import org.netbeans.spi.project.support.ant.PropertyEvaluator;

/**
 *
 * @author Milan Kubec
 */
public class J2SERunConfigProviderImpl implements J2SERunConfigProvider {
    
    public J2SERunConfigProviderImpl() {}
    
    public JComponent createComponent(Project p, J2SERunConfigProvider.ConfigChangeListener listener) {
        J2SEPropertyEvaluator j2sePropEval = p.getLookup().lookup(J2SEPropertyEvaluator.class);
        PropertyEvaluator evaluator = j2sePropEval.evaluator();
        String enabled = evaluator.getProperty("jnlp.enabled");
        JWSCustomizerPanel.runComponent.addListener(listener);
        if ("true".equals(enabled)) {
            JWSCustomizerPanel.runComponent.setCheckboxEnabled(true);
            JWSCustomizerPanel.runComponent.setHintVisible(false);
        } else {
            JWSCustomizerPanel.runComponent.setCheckboxEnabled(false);
            JWSCustomizerPanel.runComponent.setHintVisible(true);
        }
        return JWSCustomizerPanel.runComponent;
    }
    
    public void configUpdated(Map<String,String> m) {
        if ((m.get("$target.run") != null) && (m.get("$target.debug") != null)) {
            JWSCustomizerPanel.runComponent.setCheckboxSelected(true);
        } else {
            JWSCustomizerPanel.runComponent.setCheckboxSelected(false);
        }
    }
    
}
