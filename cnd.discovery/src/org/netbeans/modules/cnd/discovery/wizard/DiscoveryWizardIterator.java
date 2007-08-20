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

package org.netbeans.modules.cnd.discovery.wizard;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.cnd.discovery.wizard.bridge.DiscoveryProjectGenerator;
import org.openide.WizardDescriptor;

/**
 *
 * @author Alexander Simon
 */
public class DiscoveryWizardIterator implements WizardDescriptor.InstantiatingIterator {
    private DiscoveryWizardDescriptor wizard;
    private WizardDescriptor.Panel[] panels ;
    private WizardDescriptor.Panel[] simple ;
    private int index = 0;
    /** Creates a new instance of DiscoveryWizardIterator */
    public DiscoveryWizardIterator(WizardDescriptor.Panel[] panels, WizardDescriptor.Panel[] simple) {
        this.panels = panels;
        this.simple = simple;
    }
    
    public Set instantiate() throws IOException {
        if (wizard.isSimpleMode()){
            new DiscoveryExtension().canApply(wizard);
        }
        DiscoveryProjectGenerator generator = new DiscoveryProjectGenerator(wizard);
        return generator.makeProject();
    }
    
    public void initialize(WizardDescriptor wizard) {
        this.wizard = (DiscoveryWizardDescriptor) wizard;
    }
    
    public void uninitialize(WizardDescriptor wizard) {
        DiscoveryWizardDescriptor wiz = (DiscoveryWizardDescriptor)wizard;
        wiz.clean();
        wizard = null;
        panels = null;
        simple = null;
    }
    
    public WizardDescriptor.Panel current() {
        if (wizard.isSimpleMode()){
            return simple[index];
        } else {
            return panels[index];
        }
    }
    
    public String name() {
        return null;
    }
    
    public boolean hasNext() {
        if (wizard.isSimpleMode()){
            return index < (simple.length - 1);
        } else {
            return index < (panels.length - 1);
        }
    }
    
    public boolean hasPrevious() {
        return index > 0;
    }
    
    public synchronized void nextPanel() {
        if (wizard.isSimpleMode()){
            if ((index + 1) == simple.length) {
                throw new NoSuchElementException();
            }
        } else {
            if ((index + 1) == panels.length) {
                throw new NoSuchElementException();
            }
        }
        index++;
    }
    
    public synchronized void previousPanel() {
        if (index == 0) {
            throw new NoSuchElementException();
        }
        index--;
    }
    
    public void addChangeListener(ChangeListener l) {
    }
    
    public void removeChangeListener(ChangeListener l) {
    }
}