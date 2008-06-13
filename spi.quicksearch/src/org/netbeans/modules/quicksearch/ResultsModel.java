/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.quicksearch;

import java.util.*;
import javax.swing.AbstractListModel;
import javax.swing.KeyStroke;

/**
 * Model of search results. Works as ListModel for JList which is displaying
 * results. Actual results data are stored in List of CategoryResult objects.
 * 
 * @author Jan Becicka
 */
public final class ResultsModel extends AbstractListModel {

    private static ResultsModel instance;
    
    private List<? extends CategoryResult> results;

    /** Singleton */
    private ResultsModel () {
    }
    
    public static ResultsModel getInstance () {
        if (instance == null) {
            instance = new ResultsModel();
        }
        return instance;
    }
    
    public void setContent (List<? extends CategoryResult> categories) {
        this.results = categories;
        fireContentsChanged(this, 0, getSize());
    }

    /******* AbstractListModel impl ********/
    
    public int getSize() {
        if (results == null) {
            return 0;
        }
        int size = 0;
        for (CategoryResult cr : results) {
            size += cr.getItems().size();
        }
        return size;
    }

    public Object getElementAt (int index) {
        if (results == null) {
            return null;
        }
        // TBD - should probably throw AIOOBE if invalid index is on input
        int catIndex = index;
        int catSize = 0;
        List<ItemResult> catItems = null;
        for (CategoryResult cr : results) {
            catItems = cr.getItems();
            catSize = catItems.size();
            if (catIndex < catSize) {
                return catIndex >= 0 ? catItems.get(catIndex) : null;
            }
            catIndex -= catSize;
        }
        return null;
    }
    
    public static final class ItemResult {
    
        private CategoryResult category;
        private Runnable action;
        private String displayName;
        private KeyStroke shortcut;
        private String displayHint;

        public ItemResult (CategoryResult category, Runnable action, String displayName) {
            this(category, action, displayName, null, null);
        }

        public ItemResult (CategoryResult category, Runnable action, String displayName, KeyStroke shortcut, String displayHint) {
            this.category = category;
            this.action = action;
            this.displayName = displayName;
            this.shortcut = shortcut;
            this.displayHint = displayHint;
        }

        public Runnable getAction() {
            return action;
        }

        public String getDisplayName () {
            return displayName;
        }

        public String getDisplayHint() {
            return displayHint;
        }

        public KeyStroke getShortcut() {
            return shortcut;
        }

        public CategoryResult getCategory() {
            return category;
        }
    
    }

    void categoryChanged (CategoryResult cr) {
        // fire change only if category is contained in model
        if (results != null && results.contains(cr)) {
            // TBD - fine tune to use fireIntervalAdded
            fireContentsChanged(this, 0, getSize());
        }
    }
    
}
