/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.navigation.hierarchy;

import java.awt.event.ActionEvent;
import java.util.EnumSet;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.java.source.ui.ElementIcons;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.actions.Presenter;

/**
 *
 * @author Tomas Zezula
 */
class NameActions {

    private NameActions() {
        throw new IllegalStateException();
    }


    static Action createFullyQualifiedNameAction(@NonNull final HierarchyFilters filters) {
        assert filters != null;
        return new FullyQualifiedNameAction(filters);
    }


    static Action createSimpleNameAction(@NonNull final HierarchyFilters filters) {
        assert filters != null;
        return new SimpleNameAction(filters);
    }



    private abstract static class BaseNameAction extends AbstractAction implements Presenter.Popup {

        protected final HierarchyFilters filters;
        private JRadioButtonMenuItem menuItem;

        /** Creates a new instance of SortByNameAction */
        public BaseNameAction (@NonNull final HierarchyFilters filters) {
            assert filters != null;
            this.filters = filters;
        }

        @Override
        @NonNull
        public final JMenuItem getPopupPresenter() {
            JMenuItem result = obtainMenuItem();
            updateMenuItem();
            return result;
        }

        @NonNull
        protected final JRadioButtonMenuItem obtainMenuItem () {
            if (menuItem == null) {
                menuItem = new JRadioButtonMenuItem((String)getValue(Action.NAME));
                menuItem.setAction(this);
            }
            return menuItem;
        }

        protected abstract void updateMenuItem();
    }


    private static final class SimpleNameAction extends BaseNameAction {

        @NbBundle.Messages({
            "LBL_SimpleName=Simple Names"
        })
        public SimpleNameAction (@NonNull final HierarchyFilters filters) {
            super(filters);
            putValue(Action.NAME, Bundle.LBL_SimpleName());
            putValue(Action.SMALL_ICON,
                ElementIcons.getElementIcon(ElementKind.CLASS,EnumSet.of(Modifier.PUBLIC)));
        }

        @Override
        public void actionPerformed (ActionEvent e) {
            filters.setFqn(false);
            updateMenuItem();
        }

        @Override
        protected void updateMenuItem () {
            JRadioButtonMenuItem mi = obtainMenuItem();
            mi.setSelected(!filters.isFqn());
        }
    }


    private static final class FullyQualifiedNameAction extends BaseNameAction {

        @StaticResource
        private static final String ICON = "org/netbeans/modules/java/navigation/resources/fqn.gif";  //NOI18N

        @NbBundle.Messages({
            "LBL_FullyQualifiedName=Fully Qualified Names"
        })
        public FullyQualifiedNameAction (@NonNull final HierarchyFilters filters ) {
            super(filters);
            putValue(Action.NAME, Bundle.LBL_FullyQualifiedName());
            putValue(Action.SMALL_ICON, ImageUtilities.loadImageIcon(ICON, false)); //NOI18N
        }

        @Override
        public void actionPerformed (ActionEvent e) {
            filters.setFqn(true);
            updateMenuItem();
        }

        @Override
        protected void updateMenuItem () {
            JRadioButtonMenuItem mi = obtainMenuItem();
            mi.setSelected(filters.isFqn());
        }
    }
}
