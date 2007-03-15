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
package org.netbeans.modules.xml.refactoring.spi;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.refactoring.api.RefactoringElement;
import org.netbeans.modules.xml.refactoring.ErrorItem;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.Referenceable;
import org.openide.filesystems.FileObject;

/**
 * Provides capabilities of searching and refactoring usages of a certain 
 * class of components in a certain set of models.  Refactoring manager will
 * lookup of the service through entries declared in META-INF/services files.
 *
 * @author Nam Nguyen
 */

public abstract class RefactoringEngine {
    /**
     * @returns the component where the search for usages should start;
     * or null if the given file is not applicable source or does not
     * contains an applicable search entry point.  
     * @exception IOException if could not load the model source.
     */
    public abstract Component getSearchRoot(FileObject file) throws IOException;
    
    /**
     * Returns usages of the given target component or null if target 
     * is not applicable to current engine.  If target is root component, the 
     * search is for model references through import, include, redefine...
     *
     * @param target the component for which usage is search for.
     * @param searchRoot the scope of the search.
     * @return list of usages; or empty list if no usages found; or null if not applicable.
     */
    public abstract List<RefactoringElement> findUsages(Component target, Component searchRoot);

    /**
     * Returns usages of the given target component or null if target 
     * is not applicable to current engine.  If target is root component, the 
     * search is for model references through import, include, redefine...
     *
     * @param target the model for which usage is search for.
     * @param searchRoot the scope of the search.
     * @return list of usages; or empty list if no usages found; or null if not applicable.
     */
    public abstract List<RefactoringElement> findUsages(Model target, Component searchRoot);

     
    /**
     * Returns UI helper in displaying the usages.  Implementation could override
     * the default UI to help display usages in a more intuitive way than the 
     * generic helper.
     */
    public UIHelper getUIHelper() {
        return new UIHelper();
    }

    

    /**
     * @param component the component to check for model reference.
     * @return the reference string if this component is a reference to an 
     * external model, for example, the schema <import> component, 
     * otherwise returns null.
     */
    public String getModelReference(Component component) {
        return null;
    }
}
     