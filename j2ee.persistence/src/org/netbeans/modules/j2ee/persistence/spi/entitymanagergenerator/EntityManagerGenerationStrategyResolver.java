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

package org.netbeans.modules.j2ee.persistence.spi.entitymanagergenerator;

import org.netbeans.modules.j2ee.persistence.action.*;
import org.openide.filesystems.FileObject;

/**
 * This interface is meant to be implemented in projects where you 
 * can invoke the Use Entity Manager action. The code needed for accessing an EntityManager
 * depends on the context where it is needed, such as whether the target class
 * is a managed class or not.
 * 
 * @author Erno Mononen
 */
public interface EntityManagerGenerationStrategyResolver {
    
    /**
     * Resolves a generation strategy for the given <code>target</code>.
     * @param target the file object representing the java file that needs an
     * EntityManager. 
     * @return the class of the generation strategy or null if no appropriate strategy 
     * could be resolved.
     */ 
    Class<? extends EntityManagerGenerationStrategy> resolveStrategy(FileObject target);
    
}
