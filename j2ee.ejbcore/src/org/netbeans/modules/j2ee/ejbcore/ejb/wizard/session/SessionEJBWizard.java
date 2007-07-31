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

package org.netbeans.modules.j2ee.ejbcore.ejb.wizard.session;

import java.io.IOException;
import org.netbeans.modules.j2ee.ejbcore.api.codegeneration.SessionGenerator;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.filesystems.FileObject;
import org.netbeans.modules.j2ee.common.Util;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.ejbcore.Utils;
import org.netbeans.modules.j2ee.ejbcore.ejb.wizard.MultiTargetChooserPanel;
import org.openide.WizardDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Chris Webster
 * @author Martin Adamek
 */
public final class SessionEJBWizard implements WizardDescriptor.InstantiatingIterator{

    private WizardDescriptor.Panel[] panels;
    private int index = 0;
    private SessionEJBWizardDescriptor ejbPanel;
    private WizardDescriptor wiz;

    public static SessionEJBWizard create () {
        return new SessionEJBWizard ();
    }

    public String name () {
    return NbBundle.getMessage (SessionEJBWizard.class,
                     "LBL_SessionEJBWizardTitle");
    }

    public void uninitialize(WizardDescriptor wiz) {
    }

    public void initialize(WizardDescriptor wizardDescriptor) {
        wiz = wizardDescriptor;
        Project project = Templates.getProject(wiz);
        SourceGroup[] sourceGroups = Util.getJavaSourceGroups(project);
        ejbPanel = new SessionEJBWizardDescriptor();
        WizardDescriptor.Panel wizardDescriptorPanel = new MultiTargetChooserPanel(project, sourceGroups, ejbPanel, true);

        panels = new WizardDescriptor.Panel[] {wizardDescriptorPanel};
        Utils.mergeSteps(wiz, panels, null);
    }

    public Set instantiate () {
        FileObject pkg = Templates.getTargetFolder(wiz);
        EjbJar ejbModule = EjbJar.getEjbJar(pkg);
        // TODO: UI - add checkbox for Java EE 5 to create also EJB 2.1 style EJBs
        boolean isSimplified = ejbModule.getJ2eePlatformVersion().equals(J2eeModule.JAVA_EE_5);
        SessionGenerator sessionGenerator = SessionGenerator.create(
                Templates.getTargetName(wiz), 
                pkg, 
                ejbPanel.hasRemote(), 
                ejbPanel.hasLocal(), 
                ejbPanel.isStateful(), 
                isSimplified, 
                true, // TODO: UI - add checkbox for creation of business interface
                !isSimplified // TODO: UI - add checkbox for option XML (not annotation) usage
                );
        FileObject result = null;
        try {
            result = sessionGenerator.generate();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return result == null ? Collections.<FileObject>emptySet() : Collections.singleton(result);
    }

    public void addChangeListener(ChangeListener listener) {
    }

    public void removeChangeListener(ChangeListener listener) {
    }

    public boolean hasPrevious () {
        return index > 0;
    }

    public boolean hasNext () {
    return index < panels.length - 1;
    }

    public void nextPanel () {
        if (! hasNext ()) {
            throw new NoSuchElementException ();
        }
        index++;
    }
    public void previousPanel () {
        if (! hasPrevious ()) {
            throw new NoSuchElementException ();
        }
        index--;
    }
    public WizardDescriptor.Panel current () {
        return panels[index];
    }

}

