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


package org.netbeans.modules.j2ee.archive.ui;

import javax.swing.Action;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.openide.loaders.DataFolder;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author ludo
 */
public class ModuleNode extends ConfigFilesNode  {
    

    final transient private String name;
    
    public ModuleNode(DataFolder configFolder,String nodeName) {
        super(configFolder);
        this.name = nodeName;
    }
    
    public String getDisplayName() {
        return name; //NOI18N
    }

    public Action[] getActions(final boolean context) {
        return new Action[] {
            CommonProjectActions.newFileAction(),
            null,
            SystemAction.get(org.openide.actions.FindAction.class),
        };
    }
        
}
