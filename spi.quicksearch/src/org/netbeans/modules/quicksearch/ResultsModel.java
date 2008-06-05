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
 * ListModel for SearchGroupResults 
 * @author Jan Becicka
 */
public final class ResultsModel extends AbstractListModel {

    private static final int MAX_RESULTS = 5;
    private Iterable<? extends CategoryResult> results;
    private ArrayList ar = new ArrayList();
    
    private Map<ItemResult, ProviderModel.Category> items2Cats = new HashMap<ItemResult, ProviderModel.Category>();
    
    private HashSet<ItemResult> isFirstInCat = new HashSet<ItemResult>();

    public ResultsModel(String text) {
        super();
        results = CommandEvaluator.evaluate(text);
        for (CategoryResult cr : results) {
            boolean first = true;
            Iterator<? extends ItemResult> it = cr.getItems().iterator();
            ItemResult curSr = null;
            for (int i = 0; i < Math.min(cr.getItems().size(), MAX_RESULTS); i++) {
                curSr = it.next();
                ar.add(curSr);
                items2Cats.put(curSr, cr.getCategory());
                if (first) {
                    isFirstInCat.add(curSr);
                    first=false;
                }
                
            }
        }
    }

    public int getSize() {
        int size = 0;
        for (CategoryResult cr : results) {
            size += Math.min(MAX_RESULTS, cr.getItems().size());
        }
        return size;
    }

    public Object getElementAt(int arg0) {
        return ar.get(arg0);
    }
    
    public ProviderModel.Category getCategory (ItemResult sr) {
        return items2Cats.get(sr);
    } 
    
    public boolean isFirstinCat(ItemResult sr) {
        return isFirstInCat.contains(sr);
    }

    public static final class ItemResult {
    
        private Runnable action;
        private String displayName;
        private KeyStroke shortcut;
        private String displayHint;

        public ItemResult (Runnable action, String displayName) {
            this.action = action;
            this.displayName = displayName;
        }

        public ItemResult (Runnable action, String displayName, KeyStroke shortcut, String displayHint) {
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
    
    }
    
}
