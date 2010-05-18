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
package org.netbeans.modules.vmd.api.inspector.common;

import java.awt.Image;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javax.swing.Action;
import org.netbeans.modules.vmd.api.inspector.InspectorFolder;
import org.netbeans.modules.vmd.api.inspector.InspectorFolderPath;
import org.netbeans.modules.vmd.api.inspector.InspectorOrderingController;
import org.netbeans.modules.vmd.api.inspector.InspectorOrderingPresenter;
import org.netbeans.modules.vmd.api.inspector.InspectorPositionController;
import org.netbeans.modules.vmd.api.inspector.InspectorPositionPresenter;
import org.netbeans.modules.vmd.api.model.Debug;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignEventFilter;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.api.model.presenters.InfoPresenter;
import org.netbeans.modules.vmd.api.model.presenters.actions.ActionsSupport;

/**
 *
 * @author Karol Harezlak
 */
/**
 * This class suits as a descriptor for the Mobility Visual Designer Navigator node.
 * InspectorFolder in this class is created based on DesignComponent passed in the class constructor.
 * This class creates visual representation of DesignComponent in the Mobility Visual Designer Navigator.
 */
public class DesignComponentInspectorFolder extends InspectorFolder {

    private String displayName;
    private Image icon;
    private boolean canRename;
    private String name;
    private List<InspectorOrderingController> ocs;
    private DesignComponent component;

    /**
     * Creates  DesignComponentInspectorFolder
     * @param canRename - indicates if name of the InspectorFolder can be changed
     * @param component - InspectorFolder is created based on this parameter
     */
    public DesignComponentInspectorFolder(boolean canRename, DesignComponent component) {
        assert (component != null);
        this.component = component;
        this.canRename = canRename;
    }

    public TypeID getTypeID() {
        return getComponent().getType();
    }

    public Long getComponentID() {
        return getComponent().getComponentID();
    }

    public Image getIcon() {
        getComponent().getDocument().getTransactionManager().readAccess(new Runnable() {

            public void run() {
                InfoPresenter presenter = getComponent().getPresenter(InfoPresenter.class);
                if (presenter == null) {
                    throw new IllegalStateException("No InfoPresenter for this component"); //NOI18N
                }
                icon = presenter.getIcon(InfoPresenter.IconType.COLOR_16x16);
            }
        });
        return icon;
    }

    public String getDisplayName() {
        getComponent().getDocument().getTransactionManager().readAccess(new Runnable() {

            public void run() {
                if (getComponent().getParentComponent() != null) {
                    displayName = InfoPresenter.getDisplayName(getComponent());
                } else if (getComponent() == getComponent().getDocument().getRootComponent()) {
                    displayName = InfoPresenter.getDisplayName(getComponent());
                }
            }
        });
        return displayName;
    }

    public String getHtmlDisplayName() {
        getComponent().getDocument().getTransactionManager().readAccess(new Runnable() {

            public void run() {
                if (getComponent().getParentComponent() != null) {
                    displayName = InfoPresenter.getHtmlDisplayName(getComponent());
                } else if (getComponent() == getComponent().getDocument().getRootComponent()) {
                    displayName = InfoPresenter.getHtmlDisplayName(getComponent());
                }
            }
        });
        return displayName;
    }

    public boolean isInside(final InspectorFolderPath path, final InspectorFolder folder, final DesignComponent component) {
        for (InspectorPositionPresenter presenter : getComponent().getPresenters(InspectorPositionPresenter.class)) {
            for (InspectorPositionController pc : presenter.getFolderPositionControllers()) {
                if (pc != null && pc.isInside(path, folder, getComponent())) {
                    return true;
                }
            }
        }
        return false;
    }

    public Action[] getActions() {
        return ActionsSupport.createActionsArray(getComponent());
    }

    public boolean canRename() {
        return canRename;
    }

    public String getName() {
        getComponent().getDocument().getTransactionManager().readAccess(new Runnable() {

            public void run() {
                InfoPresenter presenter = getComponent().getPresenter(InfoPresenter.class);
                if (presenter != null) {
                    if (presenter.isEditable()) {
                        name = presenter.getEditableName();
                    }
                } else {
                    Debug.warning("No info presenter for component :" + getComponent()); //NOI18N
                }
            }
        });
        return name;
    }

    public InspectorOrderingController[] getOrderingControllers() {
        getComponent().getDocument().getTransactionManager().readAccess(new Runnable() {

            public void run() {
                Collection<? extends InspectorOrderingPresenter> presenters = getComponent().getPresenters(InspectorOrderingPresenter.class);
                if (presenters == null || presenters.isEmpty()) {
                    ocs = null;
                    return;
                }
                ocs = new ArrayList<InspectorOrderingController>();
                for (InspectorOrderingPresenter presenter : presenters) {
                    ocs.addAll(Arrays.asList(presenter.getFolderOrderingControllers()));
                }
            }
        });
        if (ocs == null) {
            return null;
        }
        return ocs.toArray(new InspectorOrderingController[ocs.size()]);
    }

    protected DesignEventFilter getEventFilter() {
        return null;
    }

    protected DesignComponent getComponent() {
        return component;
    }
}
