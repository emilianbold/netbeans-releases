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

package org.netbeans.modules.ruby.rubyproject.templates;

import org.openide.WizardDescriptor;

/**
 * Default implementations of Java-specific template UI.
 * Based on org.netbeans.spi.gsfpath.project.support.ui.templates.*.
 *
 * @author Jesse Glick
 * @author Tor Norbye
 */
public class RubyTemplates {

    private RubyTemplates() {}
    
    /** Creates new WizardIterator containing standard Package chooser
     * @return WizardIterator consisting of one panel containing package chooser
     */
    public static WizardDescriptor.InstantiatingIterator createRubyTemplateIterator () {
        return new NewRubyFileWizardIterator ();
    }
    
    /** Creates new WizardIterator containing standard Package chooser
     * @return WizardIterator consisting of one panel containing package chooser
     */
    public static WizardDescriptor.InstantiatingIterator createRubyClassTemplateIterator() {
        return NewRubyFileWizardIterator.classWizard();
    }

    public static WizardDescriptor.InstantiatingIterator createRubyModuleTemplateIterator() {
        return NewRubyFileWizardIterator.moduleWizard();
    }
    
    public static WizardDescriptor.InstantiatingIterator createRubyTestTemplateIterator() {
        return NewRubyFileWizardIterator.testWizard();
    }
}
