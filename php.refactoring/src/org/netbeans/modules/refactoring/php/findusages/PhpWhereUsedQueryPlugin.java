/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.refactoring.php.findusages;

import java.util.Collection;
import java.util.Set;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.ProgressEvent;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.netbeans.modules.refactoring.php.findusages.WhereUsedSupport.Results;
import org.netbeans.modules.refactoring.spi.ProgressProviderAdapter;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.openide.filesystems.FileObject;

/**
 * @todo index all identifiers inside project not to crawl the whole project for usages
 * @todo Scan comments!
 * 
 * @author  Radek Matous
 */
public class PhpWhereUsedQueryPlugin extends ProgressProviderAdapter implements RefactoringPlugin {

    private WhereUsedQuery refactoring;
    private WhereUsedSupport usages;

    public PhpWhereUsedQueryPlugin(WhereUsedQuery refactoring) {
        this.refactoring = refactoring;
        this.usages = refactoring.getRefactoringSource().lookup(WhereUsedSupport.class);
    }

    public Problem prepare(final RefactoringElementsBag elementsBag) {
        if (isFindSubclasses()) {
            usages.collectSubclasses();
        } else if (isFindDirectSubclassesOnly()) {
            usages.collectDirectSubclasses();
        } else if (isFindUsages()) {
            Set<FileObject> relevantFiles = usages.getRelevantFiles();
            fireProgressListenerStart(ProgressEvent.START, relevantFiles.size());
            for (FileObject fileObject : relevantFiles) {
                if (fileObject == null) {
                    continue;
                }
                usages.collectUsages(fileObject);
                fireProgressListenerStep();
            }
        }
        Results results = usages.getResults();
        Collection<WhereUsedElement> resultElements = results.getResultElements();
        for (WhereUsedElement whereUsedElement : resultElements) {
            elementsBag.add(refactoring, whereUsedElement);
        }
        fireProgressListenerStop();
        return null;
    }

    private boolean isFindSubclasses() {
        return refactoring.getBooleanValue(WhereUsedQueryConstants.FIND_SUBCLASSES);
    }

    private boolean isFindUsages() {
        return refactoring.getBooleanValue(refactoring.FIND_REFERENCES);
    }

    private boolean isFindDirectSubclassesOnly() {
        return refactoring.getBooleanValue(WhereUsedQueryConstants.FIND_DIRECT_SUBCLASSES);
    }

    private boolean isFindOverridingMethods() {
        return refactoring.getBooleanValue(WhereUsedQueryConstants.FIND_OVERRIDING_METHODS);
    }

    private boolean isSearchInComments() {
        return refactoring.getBooleanValue(refactoring.SEARCH_IN_COMMENTS);
    }

    public Problem preCheck() {
        return null;
    }

    public Problem checkParameters() {
        return null;
    }

    public Problem fastCheckParameters() {
        return null;
    }

    public void cancelRequest() {
    }
}
