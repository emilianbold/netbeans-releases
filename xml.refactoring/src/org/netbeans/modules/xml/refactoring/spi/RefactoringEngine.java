/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
 * lookup of the service through entries declared via {@link org.openide.util.lookup.ServiceProvider}.
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
     
