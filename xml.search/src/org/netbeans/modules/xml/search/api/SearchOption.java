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
 * License. When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP. Sun designates this
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
package org.netbeans.modules.xml.search.api;

import org.netbeans.modules.xml.search.spi.SearchProvider;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2007.05.25
 */
public interface SearchOption {

    /**
     * Returns text to be found.
     * @return text to be found
     */
    String getText();

    /**
     * Returns provider.
     * @return provider
     */
    SearchProvider getProvider();

    /**
     * Returns target to be found.
     * @return target to be found
     */
    SearchTarget getTarget();

    /**
     * Returns search match.
     * @return search match
     */
    SearchMatch getSearchMatch();

    /**
     * Returns true if search is case sensitive.
     * @return true if search is case sensitive
     */
    boolean isCaseSensitive();

    /**
     * Returns true if search will be performed in selection.
     * @return true if search will be performed in selection
     */
    boolean useSelection();

    // ------------------------------------------
    public class Adapter implements SearchOption {

        public Adapter(String text, SearchProvider provider, SearchTarget target, SearchMatch match, boolean caseSensitive, boolean useSelection) {
            myText = text;
            myProvider = provider;
            myTarget = target;
            mySearchMatch = match;
            myCaseSensitive = caseSensitive;
            myUseSelection = useSelection;
        }

        public String getText() {
            return myText;
        }

        public SearchProvider getProvider() {
            return myProvider;
        }

        public SearchTarget getTarget() {
            return myTarget;
        }

        public SearchMatch getSearchMatch() {
            return mySearchMatch;
        }

        public boolean isCaseSensitive() {
            return myCaseSensitive;
        }

        public boolean useSelection() {
            return myUseSelection;
        }

        private String myText;
        private SearchTarget myTarget;
        private SearchProvider myProvider;
        private SearchMatch mySearchMatch;
        private boolean myCaseSensitive;
        private boolean myUseSelection;
    }
}
