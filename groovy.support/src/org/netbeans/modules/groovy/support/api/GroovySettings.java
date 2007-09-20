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

package org.netbeans.modules.groovy.support.api;

import java.util.prefs.Preferences;
import org.netbeans.modules.groovy.support.options.SupportOptionsPanelController;
import org.netbeans.spi.options.AdvancedOption;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 * Groovy settings
 *
 * @author Martin Adamek
 */
public final class GroovySettings extends AdvancedOption {

    private static final String GROOVY_HOME = "groovyHome"; // NOI18N

    public String getGroovyHome() { 
        return prefs().get(GROOVY_HOME, ""); // NOI18N
    }
    
    public void setGroovyHome(String groovyHome) {
        prefs().put(GROOVY_HOME, groovyHome);
    }

    public String getDisplayName() {
        return NbBundle.getMessage(GroovySettings.class, "AdvancedOption_DisplayName_Support");
    }

    public String getTooltip() {
        return NbBundle.getMessage(GroovySettings.class, "AdvancedOption_Tooltip_Support");
    }

    public OptionsPanelController create() {
        return new SupportOptionsPanelController();
    }

    private Preferences prefs() {
        return NbPreferences.forModule(GroovySettings.class);
    }

}