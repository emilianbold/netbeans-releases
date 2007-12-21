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

package org.netbeans.modules.iep.editor;

import java.io.IOException;

import org.netbeans.modules.iep.editor.PlanEditorSupport.WSDLEditorEnv;
import org.netbeans.modules.iep.editor.designer.actions.PlanDesignViewOpenAction;
import org.openide.actions.OpenAction;
import org.openide.actions.FileSystemAction;
import org.openide.actions.CutAction;
import org.openide.actions.CopyAction;
import org.openide.actions.PasteAction;
import org.openide.actions.DeleteAction;
import org.openide.actions.RenameAction;
import org.openide.actions.PropertiesAction;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.ExtensionList;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.UniFileLoader;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
 * Recognizes .iep files as a single DataObject.
 *
 * @author Bing Lu
 */
public class PlanDataLoader extends UniFileLoader {
    /**
     * Extension for the file to be loaded.
     */
    public static final String PLAN_EXTENSION = "iep";

    public static final String MIME_TYPE = "text/x-iep+xml";                 // NOI18N

    private static final long serialVersionUID = -4579746482156152493L;

    public PlanDataLoader() {
        super("org.netbeans.modules.iep.editor.PlanDataObject");
    }

    protected String defaultDisplayName() {
        return NbBundle.getMessage(PlanDataLoader.class, "LBL_loaderName");
    }

    protected SystemAction[] defaultActions() {
        return new SystemAction[] {
            SystemAction.get(PlanDesignViewOpenAction.class),
            SystemAction.get(FileSystemAction.class),
            null,
            SystemAction.get(CutAction.class),
            SystemAction.get(CopyAction.class),
            SystemAction.get(PasteAction.class),
            null,
            SystemAction.get(DeleteAction.class),
            SystemAction.get(RenameAction.class),
            null,
            // GenWsdlAction.getInstance(),
            SystemAction.get(PropertiesAction.class),
        };
    }

    protected MultiDataObject createMultiObject(FileObject primaryFile)
        throws DataObjectExistsException, IOException {
        return new PlanDataObject(primaryFile, this);
    }

    /** @return The list of extensions this loader recognizes. */
    public ExtensionList getExtensions() {
        ExtensionList extensions = (ExtensionList) getProperty(PROP_EXTENSIONS);
        if (extensions == null) {
            extensions = new ExtensionList();
            extensions.addExtension(PLAN_EXTENSION);
            putProperty(PROP_EXTENSIONS, extensions, false);
        }
        return extensions;
    }
}