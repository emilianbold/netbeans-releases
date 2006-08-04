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

package org.netbeans.modules.ant.freeform.ui;

import java.awt.Component;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.ant.freeform.FreeformProjectGenerator;
import org.netbeans.modules.ant.freeform.Util;
import org.netbeans.modules.ant.freeform.spi.ProjectConstants;
import org.netbeans.modules.ant.freeform.spi.TargetDescriptor;
import org.netbeans.modules.ant.freeform.spi.support.NewFreeformProjectSupport;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * @author  David Konecny
 */
public class TargetMappingWizardPanel implements WizardDescriptor.Panel {

    public static final String PROP_TARGET_MAPPINGS = "targetMappings"; // <List> NOI18N
    
    private TargetMappingPanel component;
    private WizardDescriptor wizardDescriptor;
    private List<TargetDescriptor> targets;
    private List<String> targetNames;
    
    public TargetMappingWizardPanel(List<TargetDescriptor> targets) {
        this.targets = targets;
        getComponent().setName(NbBundle.getMessage(TargetMappingWizardPanel.class, "WizardPanel_BuildAndRunActions"));
    }
    
    public Component getComponent() {
        if (component == null) {
            component = new TargetMappingPanel(targets, false);
            component.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(TargetMappingWizardPanel.class, "ACSD_TargetMappingWizardPanel"));
        }
        return component;
    }
    
    public HelpCtx getHelp() {
        return new HelpCtx( TargetMappingWizardPanel.class );
    }
    
    public boolean isValid() {
        getComponent();
        return true;
    }
    
    private final Set<ChangeListener> listeners = new HashSet<ChangeListener>(1);
    public final void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }
    public final void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }
    protected final void fireChangeEvent() {
        Set<ChangeListener> ls;
        synchronized (listeners) {
            ls = new HashSet<ChangeListener>(listeners);
        }
        ChangeEvent ev = new ChangeEvent(this);
        for (ChangeListener l : ls) {
            l.stateChanged(ev);
        }
    }
    
    public void readSettings(Object settings) {
        wizardDescriptor = (WizardDescriptor)settings;        
        wizardDescriptor.putProperty("NewProjectWizard_Title", component.getClientProperty("NewProjectWizard_Title")); // NOI18N
        File f = (File)wizardDescriptor.getProperty(NewFreeformProjectSupport.PROP_ANT_SCRIPT);
        FileObject fo = FileUtil.toFileObject(f);
        // Util.getAntScriptTargetNames can return null when script is 
        // invalid but first panel checks script validity so it is OK here.
        List<String> l = Util.getAntScriptTargetNames(fo);
        // #47784 - update panel only once or when Ant script has changed
        if (targetNames == null || !targetNames.equals(l)) {
            targetNames = new ArrayList<String>(l);
            component.setTargetNames(l, true);
        }
        File projDir = (File)wizardDescriptor.getProperty(NewFreeformProjectSupport.PROP_PROJECT_FOLDER);
        File antScript = (File)wizardDescriptor.getProperty(NewFreeformProjectSupport.PROP_ANT_SCRIPT);
        if (!(antScript.getParentFile().equals(projDir) && antScript.getName().equals("build.xml"))) { // NOI18N
            // NON-DEFAULT location of build file
            component.setScript("${"+ProjectConstants.PROP_ANT_SCRIPT+"}"); // NOI18N
        } else {
            component.setScript(null);
        }
    }
    
    public void storeSettings(Object settings) {
        wizardDescriptor = (WizardDescriptor)settings;        
        wizardDescriptor.putProperty(PROP_TARGET_MAPPINGS, component.getMapping());
        wizardDescriptor.putProperty("NewProjectWizard_Title", null); // NOI18N
    }
    
}
