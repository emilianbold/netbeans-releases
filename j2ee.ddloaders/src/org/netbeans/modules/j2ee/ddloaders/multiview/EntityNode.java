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
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;

/**
 * @author pfiala
 */
public class EntityNode extends EjbNode {

    private EntityHelper entityHelper;

    EntityNode(SectionNodeView sectionNodeView, Entity entity) {
        super(sectionNodeView, entity, Utils.ICON_BASE_ENTITY_NODE);
        EjbJarMultiViewDataObject dataObject = (EjbJarMultiViewDataObject) sectionNodeView.getDataObject();
        entityHelper = dataObject.getEntityHelper(entity);
        addChild(new EntityOverviewNode(sectionNodeView, entity, entityHelper));
        addChild(new EjbImplementationAndInterfacesNode(sectionNodeView, entity, entityHelper));
        if (Entity.PERSISTENCE_TYPE_CONTAINER.equals(entity.getPersistenceType())) {
            addChild(new CmpFieldsNode(sectionNodeView, entityHelper.cmpFields));
            addChild(new FinderMethodsNode(sectionNodeView, entityHelper.queries));
            addChild(new SelectMethodsNode(sectionNodeView, entityHelper.queries));
        }
        addChild(new BeanEnvironmentNode(sectionNodeView, entity));
        addChild(new BeanDetailNode(sectionNodeView, entity));
    }
}
