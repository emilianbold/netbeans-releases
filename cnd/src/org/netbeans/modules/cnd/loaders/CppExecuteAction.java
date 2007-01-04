/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.cnd.loaders;

import java.io.IOException;
import java.io.File;
import java.io.*;

import org.netbeans.modules.cnd.execution41.org.openide.actions.ExecuteAction;

import org.openide.actions.*;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.UniFileLoader;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.FileEntry;
import org.openide.loaders.ExtensionList;
import org.openide.nodes.Node;
import org.openide.util.actions.SystemAction;
import org.openide.util.NbBundle;

import org.netbeans.modules.cnd.api.utils.CppUtils;
import org.netbeans.modules.cnd.MIMENames;


public class CppExecuteAction extends ExecuteAction {
    // Override only performAction and set compileBefore to false. If not set to false,
    // execute does nothing....
    protected void performAction (final Node[] activatedNodes) {
        execute(activatedNodes, false);
    }

    public String getName() {
	return NbBundle.getMessage(CppExecuteAction.class, "ExecuteActionName"); // NOI18N
    }
}
