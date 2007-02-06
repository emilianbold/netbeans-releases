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
import com.sun.source.tree.ClassTree;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.j2ee.common.source.GenerationUtils;
import org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.PersistenceUnit;

/**
 * This interface represents a generation strategy for generating
 * the code needed to access an EntityManager.
 * 
 * @author Erno Mononen
 */
public interface EntityManagerGenerationStrategy {

    void setTreeMaker(TreeMaker treeMaker);

    void setClassTree(ClassTree classTree);

    void setWorkingCopy(WorkingCopy workingCopy);

    void setGenUtils(GenerationUtils genUtils);

    void setPersistenceUnit(PersistenceUnit persistenceUnit);

    void setGenerationOptions(GenerationOptions generationOptions);

    /**
     * Generate the code needed to access an EntityManager. 
     * @return the modified ClassTree. 
     */ 
    ClassTree generate();
}
