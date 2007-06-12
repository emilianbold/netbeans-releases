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

package org.netbeans.modules.j2ee.persistence.wizard.fromdb;

import java.io.IOException;
import java.util.Set;
import org.netbeans.api.progress.aggregate.ProgressContributor;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;

/** This interface allows project implementation to provide a custom
 * generator of ORM Java classes from a DB model. An instance of this
 * interface should be registered in project lookup. 
 *
 * If there is no instance the default generator will be used.
 *
 * @author Pavel Buzek
 */
public interface PersistenceGenerator {
    
    void init(WizardDescriptor wiz);
    
    void uninit();
    
    String getFQClassName(String tableName);
    
    String generateEntityName(String className);
    
    /** 
     * Generates entity beans / entity classes based on the model represented
     * by the given <code>helper</code>.
     * 
     * @param progressPanel the panel for displaying progress during the generation, 
     * or null if no panel should be displayed.
     * @param helper the helper that specifies the generation options 
     * @param dcschemafile the schema for generating.
     * @param progressContributor the progress contributor for the generation process.
     * 
     */
    void generateBeans(final ProgressPanel progressPanel,
            final RelatedCMPHelper helper,
            final FileObject dbschemaFile,
            final ProgressContributor progressContributor) throws IOException;
    
    /**
     * @return a set of <code>FileObject</code>s representing 
     * the generated classes or an empty set if no classes were generated, never null.
     */ 
    Set<FileObject> createdObjects();
}