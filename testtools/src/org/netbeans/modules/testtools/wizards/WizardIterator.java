/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.testtools.wizards;

/*
 * WizardIterator.java
 *
 * Created on April 10, 2002, 1:51 PM
 */

import org.openide.loaders.TemplateWizard;
import org.openide.WizardDescriptor;
import javax.swing.event.ChangeListener;

/**
 *
 * @author  <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 */
public abstract class WizardIterator implements TemplateWizard.Iterator {
    
    protected transient WizardDescriptor.Panel[] panels;
    protected transient String[] names;
    protected transient int current = 0;
    protected transient TemplateWizard wizard;
    
    public void addChangeListener(javax.swing.event.ChangeListener changeListener) {
    }
    
    public org.openide.WizardDescriptor.Panel current() {
        return panels[current];
    }
    
    public boolean hasNext() {
        return (current+1)<panels.length;
    }
    
    public boolean hasPrevious() {
        return current>0;
    }
    
    public String name() {
        return names[current];
    }
    
    public void nextPanel() {
        current++;
    }
    
    public void previousPanel() {
        current--;
    }
    
    public void removeChangeListener(ChangeListener changeListener) {
    }
    
    public void uninitialize(TemplateWizard wizard) {
        panels=null;
        names=null;
    }
    
}
