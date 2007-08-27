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

package org.netbeans.modules.j2ee.ejbcore.ejb.wizard.cmp;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import org.netbeans.api.progress.aggregate.ProgressContributor;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.persistence.wizard.fromdb.PersistenceGenerator;
import org.netbeans.modules.j2ee.persistence.wizard.fromdb.ProgressPanel;
import org.netbeans.modules.j2ee.persistence.wizard.fromdb.RelatedCMPHelper;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;


/**
 *
 * @author Pavel Buzek
 */
public class CmpGenerator implements PersistenceGenerator {

    private CmpFromDbGenerator generator;
    private org.netbeans.modules.j2ee.api.ejbjar.EjbJar ejbModule;
    
    public CmpGenerator() {
    }
    
    public CmpGenerator(Project project) {
        this.ejbModule = org.netbeans.modules.j2ee.api.ejbjar.EjbJar.getEjbJars(project)[0];
        try {
            this.generator = new CmpFromDbGenerator(project, ejbModule.getDeploymentDescriptor());
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
    }
    
    public void generateBeans(final ProgressPanel progressPanel,
                              RelatedCMPHelper helper, FileObject dbschemaFile,
                              final ProgressContributor handle) throws IOException {
        CmpFromDbGenerator.ProgressNotifier progressNotifier = new CmpFromDbGenerator.ProgressNotifier() {
            public void switchToDeterminate(int workunits) {
                handle.start(workunits);
            }
            public void progress(int workunit) {
                handle.progress(workunit);
            }
            public void progress(String message) {
                handle.progress(message);
                progressPanel.setText(message);
            }
        };
        generator.generateBeans(helper, dbschemaFile, progressNotifier);
    }

    public String generateEntityName(String name) {
        return name;
    }
    
    public Set<FileObject> createdObjects() {
        return Collections.<FileObject>singleton(ejbModule.getDeploymentDescriptor());
    }
    
    public void init(WizardDescriptor wiz) {
        Project project = Templates.getProject(wiz);
        this.ejbModule = org.netbeans.modules.j2ee.api.ejbjar.EjbJar.getEjbJars(project)[0];
        try {
            this.generator = new CmpFromDbGenerator(project, ejbModule.getDeploymentDescriptor());
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
    }
    
    public void uninit() {
    }

    public String getFQClassName(String tableName) {
        return null;
    }
    
}
