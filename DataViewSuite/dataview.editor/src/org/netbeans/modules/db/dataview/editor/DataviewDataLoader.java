/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 2, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 2] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 2 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 2 code and therefore, elected the GPL
Version 2 license, then the option applies only if the new code is
made subject to such option by the copyright holder.

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 2, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 2] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 2 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 2 code and therefore, elected the GPL
Version 2 license, then the option applies only if the new code is
made subject to such option by the copyright holder.
 */
package org.netbeans.modules.db.dataview.editor;

import java.io.IOException;

import org.openide.actions.CopyAction;
import org.openide.actions.CutAction;
import org.openide.actions.DeleteAction;
import org.openide.actions.FileSystemAction;
import org.openide.actions.OpenAction;
import org.openide.actions.PasteAction;
import org.openide.actions.RenameAction;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.ExtensionList;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.UniFileLoader;
import org.openide.util.actions.SystemAction;

/** 
 * Recognizes .sql files as a single DataObject.
 *
 * @author radval
 */
public class DataviewDataLoader extends UniFileLoader {

    public static final String MIME_TYPE = "text/x-dv";                 // NOI18N

    private static final long serialVersionUID = -4579746482156152493L;

    public DataviewDataLoader() {
        super("org.netbeans.modules.db.dataview.editor.DataViewDataObject");

    }

    /** Does initialization. Initializes display name,
     * extension list and the actions. */
    @Override
    protected void initialize() {
        super.initialize();
        ExtensionList ext = getExtensions();
        ext.addMimeType(MIME_TYPE);
    }

    @Override
    protected String defaultDisplayName() {
        String nbBundle1 = "DataView Loader";
        return nbBundle1;
    }

    @Override
    protected SystemAction[] defaultActions() {
        return new SystemAction[]{
                    SystemAction.get(OpenAction.class),
                    SystemAction.get(FileSystemAction.class),
                    null,
                    SystemAction.get(CutAction.class),
                    SystemAction.get(CopyAction.class),
                    SystemAction.get(PasteAction.class),
                    null,
                    SystemAction.get(DeleteAction.class),
                    SystemAction.get(RenameAction.class),
                };
    }

    protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, IOException {
        return new DataViewDataObject(primaryFile, this);
    }
}