/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ddloaders.multiview;

import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.netbeans.modules.xml.multiview.SectionNode;
import org.netbeans.modules.xml.multiview.ui.SectionInnerPanel;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;

/**
 * @author pfiala
 */
class EntityNode extends EjbSectionNode {

    private Entity entity;
    private EntityHelper entityHelper;

    EntityNode(SectionNodeView sectionNodeView, Entity entity) {
        super(sectionNodeView, false, entity, entity.getDefaultDisplayName(), Utils.ICON_BASE_ENTITY_NODE);
        this.entity = entity;
        entityHelper = new EntityHelper(sectionNodeView.getDataObject().getPrimaryFile(), entity);
        addChild(new EntityOverviewNode(sectionNodeView, entity, entityHelper));
        addChild(new EjbImplementationAndInterfacesNode(sectionNodeView, entity, entityHelper));
        if (Entity.PERSISTENCE_TYPE_CONTAINER.equals(entity.getPersistenceType())) {
            addChild(new CmpFieldsNode(sectionNodeView, entity, entityHelper));
            addChild(new FinderMethodsNode(sectionNodeView, entity, entityHelper));
            addChild(new SelectMethodsNode(sectionNodeView, entity, entityHelper));
        }
        addChild(new BeanDetailNode(sectionNodeView, entity));
    }

    protected SectionInnerPanel createNodeInnerPanel() {
        return null;
    }

    public Entity getEntity() {
        return entity;
    }

    public EntityHelper getEntityHelper() {
        return entityHelper;
    }
}
