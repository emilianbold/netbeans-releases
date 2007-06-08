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
package org.netbeans.modules.form.j2ee;

import org.netbeans.modules.form.CreationDescriptor;
import org.netbeans.modules.form.CreationFactory;
import org.openide.modules.ModuleInstall;

/**
 * Management of form/j2ee module's lifecycle.
 *
 * @author Jan Stola
 */
public class Installer extends ModuleInstall {
    
    /**
     * Registers creation descriptors.
     */
    @Override
    public void restored() {
        // Install creator for EntityManager
        CreationDescriptor cd = new CreationDescriptor() {
            @Override
            public String getDescribedClassName() {
                return "javax.persistence.EntityManager";  // NOI18N
            }
        };
        cd.addCreator(new EntityManagerCreator(), new Object[] {"pu"}); // NOI18N
        CreationFactory.registerDescriptor(cd);

        // Install creator for Query
        cd = new CreationDescriptor() {
            @Override
            public String getDescribedClassName() {
                return "javax.persistence.Query";  // NOI18N
            }
        };
        cd.addCreator(new QueryCreator(), new Object[] {null, null, 0, -1}); // NOI18N
        CreationFactory.registerDescriptor(cd);

        cd = CreationFactory.getDescriptor(java.util.List.class);
        if (cd == null) {
            cd = new CreationDescriptor() {
                @Override
                public Class getDescribedClass() {
                    return java.util.List.class;
                }
            };
            CreationFactory.registerDescriptor(cd);
        }
        cd.addCreator(new QueryResultListCreator(), new Object[] {null, false});
    }
    
}
