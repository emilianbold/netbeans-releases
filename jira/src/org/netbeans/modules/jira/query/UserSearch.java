/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.jira.query;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.ListModel;
import org.eclipse.mylyn.internal.jira.core.model.filter.CurrentUserFilter;
import org.eclipse.mylyn.internal.jira.core.model.filter.NobodyFilter;
import org.eclipse.mylyn.internal.jira.core.model.filter.SpecificUserFilter;
import org.eclipse.mylyn.internal.jira.core.model.filter.UserFilter;
import org.eclipse.mylyn.internal.jira.core.model.filter.UserInGroupFilter;

/**
 *
 * @author tomas
 */
public class UserSearch implements ItemListener {
    private String savedText;
    private final JTextField txt;
    private final JComboBox combo;
    
    public UserSearch(JComboBox combo, JTextField txt, String nobodyDisplayName) {
        this.txt = txt;
        this.combo = combo;
        combo.addItemListener(this);

        DefaultComboBoxModel model = new DefaultComboBoxModel();
        AnyUserSearch anyUser = new AnyUserSearch();
        model.addElement(anyUser);
        model.addElement(new NobodySearch(nobodyDisplayName));
        model.addElement(new CurrentUserSearch());
        model.addElement(new SpecificUserSearch());
        model.addElement(new SpecificGroupSearch());
        combo.setModel(model);
        ((UserSearchItem)combo.getSelectedItem()).selected(this);

    }
    public void itemStateChanged(ItemEvent e) {
        if(e.getStateChange() == ItemEvent.SELECTED) {
            ((UserSearchItem)e.getItem()).selected(this);
        }
    }
    void disable() {
        if(!txt.getText().equals("")) {
            savedText = txt.getText();
        }
        txt.setText("");
        txt.setEnabled(false);
    }
    void enable() {
        txt.setText(savedText);
        txt.setEnabled(true);
    }
    public JTextField getTextField() {
        return txt;
    }
    public UserFilter getFilter() {
        Object item = combo.getSelectedItem();
        if(item == null) {
            return null;
        }
        return ((UserSearchItem)item).getFilter(this);
    }
    public void setFilter (UserFilter filter) {
        ListModel model = combo.getModel();
        for (int i = 0; i < model.getSize(); ++i) {
            UserSearchItem usItem = (UserSearchItem) model.getElementAt(i);
            if (usItem.reconstructFrom(filter, this)) {
                combo.setSelectedItem(usItem);
                break;
            }
        }
    }

    abstract class UserSearchItem  {
        private final String displayName;
        public UserSearchItem(String displayName) {
            this.displayName = displayName;
        }
        public abstract UserFilter getFilter(UserSearch us);
        public abstract void selected(UserSearch us);
        public String getDisplayName() {
            return displayName;
        }
        /**
         * Reconstructs itself from the given filter if is owner of the filter
         * @param filter
         * @return true if is owner of the filter and successfully reconstructed
         */
        protected abstract boolean reconstructFrom (UserFilter filter, UserSearch us);
    }

    private class AnyUserSearch extends UserSearchItem {
        public AnyUserSearch() {
            super("Any User");
        }
        @Override
        public UserFilter getFilter(UserSearch us) {
            return null;
        }
        @Override
        public void selected(UserSearch us) {
            us.disable();
        }
        @Override
        protected boolean reconstructFrom(UserFilter filter, UserSearch us) {
            return false;
        }
    }

    private class NobodySearch extends UserSearchItem {
        private UserFilter filter = new NobodyFilter();
        public NobodySearch(String displayName) {
            super(displayName);
        }
        @Override
        public UserFilter getFilter(UserSearch us) {
            return filter;
        }
        @Override
        public void selected(UserSearch us) {
            us.disable();
        }
        @Override
        protected boolean reconstructFrom(UserFilter filter, UserSearch us) {
            return filter instanceof NobodyFilter;
        }
    }
    private class CurrentUserSearch extends UserSearchItem {
        private UserFilter filter = new CurrentUserFilter();
        public CurrentUserSearch() {
            super("Current User");
        }
        @Override
        public UserFilter getFilter(UserSearch us) {
            return filter;
        }
        @Override
        public void selected(UserSearch us) {
            us.disable();
        }
        @Override
        protected boolean reconstructFrom(UserFilter filter, UserSearch us) {
            return filter instanceof CurrentUserFilter;
        }
    }
    private class SpecificUserSearch extends UserSearchItem {
        public SpecificUserSearch() {
            super("Specify User");
        }
        @Override
        public UserFilter getFilter(UserSearch us) {
            return new SpecificUserFilter(us.getTextField().getText());
        }
        @Override
        public void selected(UserSearch us) {
            us.enable();
        }
        @Override
        protected boolean reconstructFrom(UserFilter filter, UserSearch us) {
            boolean retval = false;
            if (filter instanceof SpecificUserFilter) {
                us.savedText = ((SpecificUserFilter) filter).getUser();
                retval = true;
            }
            return retval;
        }
    }
    private class SpecificGroupSearch extends UserSearchItem {
        public SpecificGroupSearch() {
            super("Specify Group");
        }
        @Override
        public UserFilter getFilter(UserSearch us) {
            return new UserInGroupFilter(us.getTextField().getText());
        }
        @Override
        public void selected(UserSearch us) {
            us.enable();
        }
        @Override
        protected boolean reconstructFrom(UserFilter filter, UserSearch us) {
            boolean retval = false;
            if (filter instanceof UserInGroupFilter) {
                us.savedText = ((UserInGroupFilter) filter).getGroup();
                retval = true;
            }
            return retval;
        }
    }
}
