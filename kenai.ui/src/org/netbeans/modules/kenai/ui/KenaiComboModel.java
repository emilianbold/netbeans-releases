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

package org.netbeans.modules.kenai.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.List;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import org.netbeans.modules.kenai.api.Kenai;
import org.netbeans.modules.kenai.api.KenaiManager;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

/**
 *
 * @author Jan Becicka
 */
public class KenaiComboModel extends AbstractListModel implements ComboBoxModel {

    private Object selected = getElementAt(0);
    private List<Kenai.Status> statuses;
    private int addNew = 1;

    private PropertyChangeListener listener = new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            fireContentsChanged(evt.getSource(), 0, getSize());
        }
    };

    public KenaiComboModel(Kenai.Status... statuses) {
        this(true, statuses);
    }

    public KenaiComboModel(boolean addNew, Kenai.Status... statuses) {
        this();
        this.statuses = Arrays.asList(statuses);
        this.addNew = addNew?1:0;
    }

    public KenaiComboModel() {
        KenaiManager.getDefault().addPropertyChangeListener(WeakListeners.propertyChange(listener, KenaiManager.getDefault()));
    }

    @Override
    public void setSelectedItem(Object anItem) {
        selected = anItem;
    }

    @Override
    public Object getSelectedItem() {
        return selected;
    }

    @Override
    public Object getElementAt(int index) {
        int i = 0;
        for (Kenai k: KenaiManager.getDefault().getKenais()) {
            if (statuses==null || statuses.contains(k.getStatus())) {
                i++;
            }
            if (i -1 == index) {
                return k;
            }
        }
        return NbBundle.getMessage(KenaiComboModel.class, "CTL_AddNew");
    }

    @Override
    public int getSize() {
        if (statuses==null) {
            return KenaiManager.getDefault().getKenais().size() + addNew;
        }

        int i=0;
        for (Kenai k: KenaiManager.getDefault().getKenais()) {
            if (statuses.contains(k.getStatus())) {
                i++;
            }
        }
        return i + addNew;
    }
}
