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
import org.openide.ErrorManager;
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
        //TODO: RETOUCHE
//        JavaClass jc = controller.getBeanClass();
//        if (jc != null)
//            content.add(jc);
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
            Object cookie = ddFileDO.getCookie(DDEditorNavigator.class);
            if (cookie != null) {
                ((DDEditorNavigator) cookie).showElement(entity.getCmpField());
            }
        } catch (DataObjectNotFoundException donf) {
            ErrorManager.getDefault().notify(donf);
        }
    }
}
