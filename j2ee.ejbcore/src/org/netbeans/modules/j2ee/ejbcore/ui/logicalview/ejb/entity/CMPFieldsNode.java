/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.entity;

import javax.swing.Action;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.openide.actions.*;
import org.openide.cookies.OpenCookie;
import org.openide.loaders.DataObject;
import org.openide.nodes.*;
import org.openide.util.actions.SystemAction;
import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.action.AddCmpFieldAction;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.EntityMethodController;
import org.netbeans.modules.j2ee.common.DDEditorNavigator;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;


/**
 *
 * @author Martin Adamek
 */
public class CMPFieldsNode extends AbstractNode implements OpenCookie {
    private final EntityMethodController controller;
    private final EjbJar ejbJar;
    private final FileObject ddFile;
    private Entity entity;

    public CMPFieldsNode(EntityMethodController controller, Entity model, EjbJar jar, FileObject ddFile) {
        this(new InstanceContent(), controller, model, jar, ddFile);
    }
    
    private CMPFieldsNode(InstanceContent content, EntityMethodController controller, Entity model, EjbJar jar, FileObject ddFile) {
        super(new CMFieldChildren(controller, model, jar, ddFile), new AbstractLookup(content));
        entity = model;
        this.controller = controller;
        this.ejbJar = jar;
        this.ddFile = ddFile;
        content.add(this);
        content.add(controller.getBeanClass());
    }
    
    public Action[] getActions(boolean context) {
        return new SystemAction[] {
            SystemAction.get(AddCmpFieldAction.class)
        };
    }
    
    public Action getPreferredAction() {
        return SystemAction.get(OpenAction.class);
    }
    
    public void open() {
        try {
            DataObject ddFileDO = DataObject.find(ddFile);
            Object c = ddFileDO.getCookie(DDEditorNavigator.class);
            if (c != null) {
                ((DDEditorNavigator) c).showElement(entity.getCmpField());
            }
        } catch (DataObjectNotFoundException donf) {
            // do nothing
        }
    }
}
