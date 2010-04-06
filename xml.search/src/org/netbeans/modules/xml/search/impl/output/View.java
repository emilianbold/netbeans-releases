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
package org.netbeans.modules.xml.search.impl.output;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import org.openide.util.HelpCtx;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

import org.netbeans.modules.xml.search.api.SearchElement;
import org.netbeans.modules.xml.search.api.SearchEvent;
import org.netbeans.modules.xml.search.spi.SearchListener;
import static org.netbeans.modules.xml.util.UI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2006.11.24
 */
public final class View extends TopComponent implements SearchListener {

    public View() {
        setIcon(icon(View.class, "find").getImage()); // NOI18N
        setLayout(new GridBagLayout());
        setFocusable(true);
        myList = new Tree();
        myTree = new Tree();
    }

    public void searchStarted(SearchEvent event) {
//out();
        myFoundCount = 0;
    }

    public void searchFound(SearchEvent event) {
//out("Found: " + element);
        SearchElement element = event.getSearchElement();
        myTree.addElement(element);
        myList.addElement(new Element(element));
        myFoundCount++;
    }

    public void searchFinished(SearchEvent event) {
        String text = event.getSearchOption().getText();
        String target = event.getSearchOption().getTarget().toString();

        myList.finished(target, text, myFoundCount);
        myTree.finished(target, text, myFoundCount);

        View view = (View) WindowManager.getDefault().findTopComponent(NAME);
//out();
//out("VIEW: " + view.getClass().getName() + " " + view.hashCode());
        view.show(myList, myTree);
    }

    private void show(Tree list, Tree tree) {
        createTabbed();
        myTabbed.addTrees(list, tree);
        open();
        requestActive();
    }

    private void createTabbed() {
        if (myTabbed != null) {
            return;
        }
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;

        c.weightx = 1.0;
        c.weighty = 1.0;
        myTabbed = new Tabbed();
        add(myTabbed, c);
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    public int getPersistenceType() {
        return PERSISTENCE_ALWAYS;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDisplayName() {
        return i18n(View.class, "LBL_Search_Results"); // NOI18N
    }

    @Override
    public String getToolTipText() {
        return i18n(View.class, "TLT_Search_Results"); // NOI18N
    }

    @Override
    protected void componentClosed() {
        super.componentClosed();

        if (myTabbed != null) {
            myTabbed.closeAllTabs();
        }
        myList = null;
        myTree = null;
    }

    @Override
    protected String preferredID() {
        return NAME;
    }

    private Tree myList;
    private Tree myTree;
    private Tabbed myTabbed;
    private int myFoundCount;
    private static final String NAME = "xml.search"; // NOI18N
}
