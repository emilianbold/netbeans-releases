/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.vmd.api.inspector;

import java.awt.Image;
import javax.swing.Action;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignEvent;
import org.netbeans.modules.vmd.api.model.DesignEventFilter;
import org.netbeans.modules.vmd.api.model.PresenterEvent;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.api.model.presenters.actions.AddAction;

/**
 *
 * @author Karol Harezlak
 */
public final class InspectorFolderCategoryPresenter extends InspectorFolderPresenter {

    private CategoryFolder folder;
    private TypeID typeID;
    private Image icon;
    private TypeID[] filtersTypeID;
    private InspectorOrderingController[] orderingControllers;
    private String displayName;
    private TypeID parentTypeID;

    public InspectorFolderCategoryPresenter(String displayName, TypeID typeID, Image icon, TypeID[] filtersTypeID, TypeID parentTypeID, InspectorOrderingController... orderingControllers) {

        this.displayName = displayName;
        this.typeID = typeID;
        this.icon = icon;
        this.filtersTypeID = filtersTypeID;
        this.orderingControllers = orderingControllers;
        this.parentTypeID = parentTypeID;
    }

    public InspectorFolder getFolder() {
        if (folder == null) {
            folder = new CategoryFolder(displayName, typeID, icon, filtersTypeID, orderingControllers);
        }
        return folder;
    }

    protected void notifyAttached(DesignComponent component) {
    }

    protected void notifyDetached(DesignComponent component) {
    }

    protected DesignEventFilter getEventFilter() {
        return null;
    }

    protected void designChanged(DesignEvent event) {
    }

    protected void presenterChanged(PresenterEvent event) {
    }

    private class CategoryFolder extends InspectorFolder {

        private Image icon;
        private String displayName;
        private InspectorOrderingController[] orderingControllers;
        private TypeID typeID;
        private AddAction[] addAction;
        private TypeID[] filtersTypeID;

        public CategoryFolder(String displayName, TypeID typeID, Image icon, TypeID[] filtersTypeID, InspectorOrderingController[] orderingControllers) {

            if (typeID == null) {
                throw new IllegalArgumentException("TypeID cant be null InspectorFolderPresenter: " + getComponent()); //NOI18N
            }
            this.displayName = displayName;
            this.icon = icon;
            this.orderingControllers = orderingControllers;
            this.typeID = typeID;
            this.filtersTypeID = filtersTypeID;
        }

        public TypeID getTypeID() {
            return typeID;
        }

        public Long getComponentID() {
            return getComponent().getComponentID();
        }

        public Image getIcon() {
            return icon;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getName() {
            return displayName;
        }

        public Action[] getActions() {
            if (addAction == null) {
                addAction = new AddAction[]{AddAction.getInstance(filtersTypeID)};
            }
            addAction[0].setComponent(getComponent());
            return addAction;
        }

        public boolean canRename() {
            return false;
        }

        public InspectorOrderingController[] getOrderingControllers() {
            return orderingControllers;
        }

        public boolean isInside(InspectorFolderPath path, InspectorFolder folder, DesignComponent component) {
            if (parentTypeID != null && component.getParentComponent().getType().equals(parentTypeID)) {
                return false;
            }
            if (getComponent().getType().equals(path.getLastElement().getTypeID()) && path.getLastElement().getComponentID().equals(getComponentID())) {
                return true;
            }
            return false;
        }

        public String getHtmlDisplayName() {
            return displayName;
        }
    }
}
