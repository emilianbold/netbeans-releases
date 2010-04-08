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
package org.netbeans.modules.xml.search.impl.ui;

import java.awt.Event;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

import org.openide.actions.FindAction;
import org.netbeans.modules.xml.xam.ui.search.SearchControlPanel;
import org.netbeans.modules.xml.xam.ui.search.Query;

import org.netbeans.modules.xml.search.api.SearchElement;
import org.netbeans.modules.xml.search.api.SearchEvent;
import org.netbeans.modules.xml.search.api.SearchException;
import org.netbeans.modules.xml.search.api.SearchMatch;
import org.netbeans.modules.xml.search.api.SearchOption;
import org.netbeans.modules.xml.search.spi.SearchEngine;
import org.netbeans.modules.xml.search.spi.SearchListener;
import org.netbeans.modules.xml.search.spi.SearchProvider;
import static org.netbeans.modules.xml.util.UI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2006.11.20
 */
public final class Find extends SearchControlPanel {

    public Find(List<SearchEngine> engines, Object root, JComponent parent) {
        super();
        bindAction(parent);
        myProviders = new ArrayList<Provider>();
        SearchProvider provider = new SearchProvider.Adapter(root);

        for (SearchEngine engine : engines) {
            myProviders.add(new Provider(engine, provider));
        }
        setProviders(myProviders);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        if (enabled) {
            return;
        }
        for (Provider provider : myProviders) {
            provider.release();
        }
    }

    @Override
    protected void hideResults() {
//out("Hide selection");
        if (myElements == null) {
            return;
        }
        for (Object element : myElements) {
            ((SearchElement) element).unhighlight();
        }
        myElements = null;
    }

    @Override
    protected void showSearchResult(Object object) {
//out("show result");
        if (!(object instanceof SearchElement)) {
            return;
        }
        ((SearchElement) object).gotoVisual();
    }

    private void bindAction(JComponent parent) {
        FindAction findAction = (FindAction) FindAction.get(FindAction.class);
        Object key = findAction.getActionMapKey();
        parent.getActionMap().put(key, new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                setVisible(true);
            }
        });
        InputMap keys = parent.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        KeyStroke stroke = (KeyStroke) findAction.getValue(Action.ACCELERATOR_KEY);

        if (stroke == null) {
            stroke = KeyStroke.getKeyStroke(KeyEvent.VK_F, Event.CTRL_MASK);
        }
        keys.put(stroke, key);
    }

    // ------------------------------------------------------------------------------------------------------------
    private final class Provider implements org.netbeans.modules.xml.xam.ui.search.SearchProvider, SearchListener {

        private Provider(SearchEngine engine, SearchProvider provider) {
            mySearchEngine = engine;
            mySearchEngine.addSearchListener(this);
            myProvider = provider;
        }

        void release() {
            mySearchEngine.removeSearchListeners();
        }

        public String getDisplayName() {
            return mySearchEngine.getDisplayName();
        }

        public String getShortDescription() {
            return mySearchEngine.getShortDescription();
        }

        public String getInputDescription() {
            return i18n(Provider.class, "LBL_Input_Description"); // NOI18N
        }

        public List<Object> search(Query query) throws org.netbeans.modules.xml.xam.ui.search.SearchException {
            SearchMatch match = getMatch(query);
            String text = getText(query, match);

            SearchOption option = new SearchOption.Adapter(
                text,
                myProvider,
                null, // target
                match,
                false, // case sensitive
                query.useSelected()
            );
            try {
                mySearchEngine.search(option);
            } catch (SearchException e) {
                throw new org.netbeans.modules.xml.xam.ui.search.SearchException(e.getMessage(), e);
            }
//out("returned: " + myElements);
            return myElements;
        }

        private SearchMatch getMatch(Query query) {
            if (query.isRegularExpression()) {
                return SearchMatch.REGULAR_EXPRESSION;
            }
            return SearchMatch.PATTERN;
        }

        private String getText(Query query, SearchMatch match) {
            String text = query.getQuery().trim();

            if (match == SearchMatch.PATTERN) {
                return "*" + text + "*"; // NOI18N
            }
            return text;
        }

        public void searchStarted(SearchEvent event) {
            myElements = new ArrayList<Object>();
        }

        public void searchFound(SearchEvent event) {
            myElements.add(event.getSearchElement());
        }

        public void searchFinished(SearchEvent event) {
        }
    
        private SearchProvider myProvider;
        private SearchEngine mySearchEngine;
    }

    private List<Object> myElements;
    private List<Provider> myProviders;
}
