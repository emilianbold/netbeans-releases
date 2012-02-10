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
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2007 Sun
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
package org.netbeans.spi.search.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.api.search.SearchRoot;
import org.netbeans.api.search.SearchScopeOptions;
import org.netbeans.api.search.provider.SearchInfo;
import org.netbeans.api.search.provider.SearchListener;
import org.netbeans.api.search.provider.impl.*;
import org.netbeans.spi.search.SearchInfoDefinition;
import org.netbeans.spi.search.provider.TerminationFlag;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Marian Petras
 */
public class CompoundSearchInfoDefinition extends SearchInfoDefinition {

    /**
     *
     */
    private final SearchInfoDefinition[] elements;

    /**
     * Creates a new instance of CompoundSearchInfo
     *
     * @param elements elements of this
     * <code>SearchInfo</code>
     * @exception java.lang.IllegalArgumentException if the argument was
     * <code>null</code>
     */
    public CompoundSearchInfoDefinition(SearchInfoDefinition... elements) {
        if (elements == null) {
            throw new IllegalArgumentException();
        }

        this.elements = elements.length != 0 ? elements
                : null;
    }

    /**
     */
    @Override
    public boolean canSearch() {
        if (elements != null) {
            for (SearchInfoDefinition element : elements) {
                if (element.canSearch()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     */
    @Override
    public Iterator<FileObject> filesToSearch(SearchScopeOptions options,
            SearchListener listener, TerminationFlag terminationFlag) {
        if (elements == null) {
            return Collections.<FileObject>emptyList().iterator();
        }

        List<SearchInfo> searchableElements =
                new ArrayList<SearchInfo>(elements.length);
        for (SearchInfoDefinition element : elements) {
            if (element.canSearch()) {
                searchableElements.add(new DelegatingSearchInfo(element));
            }
        }
        return new CompoundSearchIterator(
                searchableElements.toArray(
                new SearchInfo[searchableElements.size()]),
                options, listener, terminationFlag);
    }

    @Override
    public List<SearchRoot> getSearchRoots() {
        List<SearchRoot> allRoots = new LinkedList<SearchRoot>();
        for (SearchInfoDefinition si : elements) {
            allRoots.addAll(si.getSearchRoots());
        }
        return allRoots;
    }
}
