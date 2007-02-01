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

package org.netbeans.modules.j2ee.jpa.verification.fixes;

import java.util.logging.Level;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.jpa.verification.JPAProblemFinder;
import org.netbeans.modules.j2ee.persistence.provider.InvalidPersistenceXmlException;
import org.netbeans.modules.j2ee.persistence.wizard.Util;
import org.netbeans.modules.j2ee.persistence.wizard.unit.PersistenceUnitWizardPanel.TableGeneration;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomasz.Slota@Sun.COM
 */
public class CreatePersistenceUnit implements Fix {
    private Project project;
    
    /** Creates a new instance of CreatePersistenceUnit */
    public CreatePersistenceUnit(Project project) {
        this.project = project;
    }
    
    public ChangeInfo implement(){
        SwingUtilities.invokeLater(new Runnable(){
            public void run() {
                try{
                    Util.createPersistenceUnitUsingWizard(project, null, TableGeneration.CREATE);
                } catch(InvalidPersistenceXmlException e){
                    JPAProblemFinder.LOG.log(Level.WARNING, e.getMessage(), e);
                }
            }
        });
        
        return null;
    }
    
    public int hashCode(){
        return this.getClass().getName().hashCode();
    }
    
    public boolean equals(Object o){
        // TODO: implement equals properly
        return super.equals(o);
    }
    
    public String getText(){
        return NbBundle.getMessage(CreatePersistenceUnit.class, "LBL_CreatePersistenceUnitHint");
    }
}
