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

package org.netbeans.modules.j2ee.persistence.unit;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.persistence.dd.PersistenceUtils;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Entity;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Dialog for adding entities into persistence unit.
 *
 * @author Erno Mononen
 */
public class AddEntityDialog {
    
    private Project project;
    private List existingClassNames;
    
    /** Creates a new instance of AddClassDialog */
    public AddEntityDialog(Project project, String[] existingClassNames) {
        this.project = project;
        this.existingClassNames = Arrays.asList(existingClassNames);
    }
    
    /**
     * Opens dialog for adding entities.
     * @return fully qualified names of the selected entities' classes.
     */
    public List<String> open(){
        Set<Entity> entities = null;
        try {
            entities = new HashSet(PersistenceUtils.getEntityClasses(project));
            for (Iterator<Entity> i = entities.iterator(); i.hasNext();) {
                if (existingClassNames.contains(i.next().getClass2())) {
                    i.remove();
                }
            }
        } catch (IOException e) {
            ErrorManager.getDefault().notify(e);
            entities = Collections.emptySet();
        }
        AddEntityPanel panel = new AddEntityPanel(entities);
        
        final DialogDescriptor nd = new DialogDescriptor(
                panel,
                NbBundle.getMessage(AddEntityDialog.class, "LBL_AddEntity"),
                true,
                DialogDescriptor.OK_CANCEL_OPTION,
                DialogDescriptor.OK_OPTION,
                DialogDescriptor.DEFAULT_ALIGN,
                new HelpCtx(AddEntityPanel.class),
                null
                );
        
        Object button = DialogDisplayer.getDefault().notify(nd);
        if (button != NotifyDescriptor.OK_OPTION) {
            return Collections.emptyList();
        }
        
        return panel.getSelectedEntityClasses();
    }
    
}
