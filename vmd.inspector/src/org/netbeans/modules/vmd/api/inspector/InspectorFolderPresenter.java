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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.vmd.api.inspector;

import java.awt.Image;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.Action;

import org.netbeans.modules.vmd.api.model.Debug;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignEvent;
import org.netbeans.modules.vmd.api.model.DesignEventFilter;
import org.netbeans.modules.vmd.api.model.DynamicPresenter;
import org.netbeans.modules.vmd.api.model.PresenterEvent;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.api.model.presenters.InfoPresenter;
import org.netbeans.modules.vmd.api.model.presenters.actions.ActionsSupport;
import org.netbeans.modules.vmd.api.model.presenters.actions.AddAction;


/**
 *
 * @author Karol Harezlak
 */

public abstract class InspectorFolderPresenter extends DynamicPresenter {
    
    public static InspectorFolderPresenter create(final boolean canRename,final String... propertyNames) {
        
        return new InspectorFolderPresenter() {
            private ComponentFolder folder;
            
            public InspectorFolder getFolder() {
                if (folder == null) {
                    folder = new ComponentFolder(canRename);
                }
                
                return folder;
            }
            
            protected DesignEventFilter getEventFilter() {
                List<DesignEventFilter> filters = new ArrayList<DesignEventFilter>();
                for (String propertyName : propertyNames) {
                    filters.add(new DesignEventFilter().addDescentFilter(super.getComponent(), propertyName));
                }
                return  new DesignEventFilter(filters.toArray(new DesignEventFilter[filters.size()]));
            }
            
            protected void designChanged(DesignEvent event) {
                if (event.isStructureChanged())
                    InspectorRegistry.addComponent(super.getComponent());
            }
        };
        
    }
    
    public static InspectorFolderPresenter create(final boolean canRename) {
        
        return new InspectorFolderPresenter() {
            private ComponentFolder folder;
            
            public InspectorFolder getFolder() {
                if (folder == null) {
                    folder = new ComponentFolder(canRename);
                }
                
                return folder;
            }
            
        };
    }
    
    public static InspectorFolderPresenter create(final String displayName,
        final TypeID typeID,
        final Image icon,
        final TypeID[] filtersTypeID,
        final InspectorOrderingController... orderingControllers) {
        
        return new InspectorFolderPresenter() {
            private CategoryFolder folder;
            
            public InspectorFolder getFolder() {
                if (folder == null) {
                    folder = new CategoryFolder(displayName, typeID, icon, filtersTypeID, orderingControllers);
                }
                
                return folder;
            }
            
        };
    }
    
    public abstract InspectorFolder getFolder();
    
    protected void notifyAttached(DesignComponent component) {}
    
    protected void notifyDetached(DesignComponent component) {}
    
    protected void designChanged(DesignEvent event) {}
    
    protected void presenterChanged(PresenterEvent event) {}
    
    protected DesignEventFilter getEventFilter() {
        getComponent();
        return null;
    }
    
    private class ComponentFolder implements InspectorFolder {
        
        private String displayName;
        private Image icon;
        private boolean canRename;
        private String name;
        
        public ComponentFolder(boolean canRename) {
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
                    InfoPresenter presenter =  getComponent().getPresenter(InfoPresenter.class);
                    if (presenter == null)
                        throw new IllegalStateException("No InfoPresenter for this component"); //NOI18N
                    
                    icon = presenter.getIcon(InfoPresenter.IconType.COLOR_16x16);
                }
            });
            
            return icon;
        }
        
        public String getDisplayName() {
            getComponent().getDocument().getTransactionManager().readAccess(new Runnable() {
                public void run() {
                    if (getComponent().getParentComponent() != null)
                        displayName = InfoPresenter.getHtmlDisplayName(getComponent());
                    else if (getComponent() == getComponent().getDocument().getRootComponent())
                        displayName = InfoPresenter.getHtmlDisplayName(getComponent());
                }
            });
            
            return displayName;
        }
        
        public boolean isInside(final InspectorFolderPath path, final InspectorFolder folder, final  DesignComponent component) {
            for (InspectorPositionPresenter presenter : getComponent().getPresenters(InspectorPositionPresenter.class)){
                for (InspectorPositionController pc : presenter.getFolderPositionControllers()) {
                    if (pc != null && pc.isInside(path, folder, getComponent()))
                        return true;
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
                        if (presenter.isEditable())
                            name = presenter.getEditableName();
                    } else
                        Debug.warning("No info presenter for component :"  + getComponent()); //NOI18N
                }
            });
            return name;
        }
        
        public InspectorOrderingController[] getOrderingControllers() {
            if (getComponent().getPresenters(InspectorOrderingPresenter.class) == null
                || getComponent().getPresenters(InspectorOrderingPresenter.class).isEmpty() )
                return null;
            
            List<InspectorOrderingController> ocs = new ArrayList<InspectorOrderingController>();
            
            for (InspectorOrderingPresenter presenter : getComponent().getPresenters(InspectorOrderingPresenter.class)){
                ocs.addAll(Arrays.asList(presenter.getFolderOrderingControllers()));
            }
            
            return ocs.toArray(new InspectorOrderingController[ocs.size()]);
            
        }
        protected DesignEventFilter getEventFilter() {
            return null;
        }
    }
    
    private class CategoryFolder implements InspectorFolder {
        
        private Image icon;
        private String displayName;
        private InspectorOrderingController[] orderingControllers;
        private TypeID typeID;
        private AddAction[] addAction;
        private TypeID[] filtersTypeID;
        
        public CategoryFolder(String displayName,
            TypeID typeID,
            Image icon,
            TypeID[] filtersTypeID,
            InspectorOrderingController[] orderingControllers ) {
            if (typeID == null)
                throw new IllegalArgumentException("TypeID cant be null InspectorFolderPresenter: "+ getComponent()); //NOI18N
            
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
            if (addAction == null)
                addAction = new AddAction[]{AddAction.getInstance(filtersTypeID)};
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
            if (getComponent().getType() == path.getLastElement().getTypeID() &&  path.getLastElement().getComponentID() == getComponentID())
                return true;
            
            return false;
        }
        
    }
}
