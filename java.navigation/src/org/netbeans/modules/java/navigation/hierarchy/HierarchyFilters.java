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

import java.util.Collection;
import javax.swing.AbstractButton;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.java.navigation.base.Filters;
import org.netbeans.modules.java.navigation.base.FiltersDescription;
import org.netbeans.modules.java.navigation.base.FiltersManager;
import org.openide.util.ChangeSupport;
import org.openide.util.NbPreferences;

/**
 *
 * @author Tomas Zezula
 */
final class HierarchyFilters extends Filters<Object> {

    private static final String PROP_FQN = "fqn";   //NOI18N

    private final ChangeSupport support;
    private volatile boolean fqn;
    private JToggleButton simpleNameButton;
    private JToggleButton fqNameButton;


    HierarchyFilters() {
        fqn = NbPreferences.forModule(HierarchyFilters.class).getBoolean(PROP_FQN, false);
        this.support = new ChangeSupport(this);
    }

    @Override
    public Collection<Object> filter(Collection<?> original) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void addChangeListener(@NonNull final ChangeListener listener) {
        support.addChangeListener(listener);
    }

    public void removeChangeListener(@NonNull final ChangeListener listener) {
        support.removeChangeListener(listener);
    }

    boolean isFqn() {
        return fqn;
    }

    void setFqn(final boolean fqn) {
        this.fqn = fqn;
        NbPreferences.forModule(HierarchyFilters.class).putBoolean(PROP_FQN, fqn);
        if(null != simpleNameButton) {
            simpleNameButton.setSelected(!fqn);
        }
        if(null != fqNameButton) {
            fqNameButton.setSelected(fqn);
        }
        support.fireChange();
    }

    @Override
    protected void sortUpdated() {
        support.fireChange();
    }

    @Override
    protected FiltersManager createFilters() {
        FiltersDescription desc = new FiltersDescription();
        return FiltersDescription.createManager(desc);
    }

    @Override
    protected AbstractButton[] createCustomButtons() {
        assert SwingUtilities.isEventDispatchThread();
        AbstractButton[] res = new AbstractButton[2];
        if( null == simpleNameButton ) {
            simpleNameButton = new JToggleButton(NameActions.createSimpleNameAction(this));
            simpleNameButton.setToolTipText(simpleNameButton.getText());
            simpleNameButton.setText(null);
            simpleNameButton.setSelected( !isFqn());
            simpleNameButton.setFocusable( false );
        }
        res[0] = simpleNameButton;

        if( null == fqNameButton ) {
            fqNameButton = new JToggleButton(NameActions.createFullyQualifiedNameAction(this));
            fqNameButton.setToolTipText(fqNameButton.getText());
            fqNameButton.setText(null);
            fqNameButton.setSelected(isFqn());
            fqNameButton.setFocusable( false );
        }
        res[1] = fqNameButton;
        return res;
    }
}
