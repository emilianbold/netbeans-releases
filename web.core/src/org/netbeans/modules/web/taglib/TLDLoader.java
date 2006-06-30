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

package  org.netbeans.modules.web.taglib;

/**
 *
 * @author  Milan Kuchtiak
 * @version 1.0
 */

import java.io.IOException;

import org.openide.loaders.UniFileLoader;
import org.openide.loaders.ExtensionList;
import org.openide.loaders.MultiDataObject;
import org.openide.filesystems.FileObject;
import org.openide.actions.*;
import org.openide.util.actions.SystemAction;
import org.openide.util.NbBundle;

/** Data loader which recognizes .tld files.
* This class is final only for performance reasons,
* can be unfinaled if desired.
*
*/

public final class TLDLoader extends UniFileLoader {
    
    public static final String tldExt = "tld"; //NOI18N
    private static final String REQUIRED_MIME_1 = "text/x-tld2.0"; //NOI18N
    private static final String REQUIRED_MIME_2 = "text/x-tld1.2"; //NOI18N
    private static final String REQUIRED_MIME_3 = "text/x-tld1.1"; //NOI18N
    
    private static final long serialVersionUID = -7367746798495347598L;
    
    /** Constructor */
    public TLDLoader() {
	super("org.netbeans.modules.web.taglib.TLDDataObject"); // NOI18N
    }
    
     /** Does initialization. Initializes display name,
     * extension list and the actions. */
    
    protected void initialize () {
    	super.initialize();
	ExtensionList ext = new ExtensionList();
	ext.addExtension(tldExt);
	setExtensions(ext);
        getExtensions().addMimeType(REQUIRED_MIME_1);
        getExtensions().addMimeType(REQUIRED_MIME_2);
        getExtensions().addMimeType(REQUIRED_MIME_3);
    }

    protected MultiDataObject createMultiObject(final FileObject fo)
	throws IOException {
	MultiDataObject obj = new TLDDataObject(fo, this);
	return obj;
    }

    protected String defaultDisplayName () {
	return NbBundle.getMessage (TLDLoader.class, "TLD_loaderName");
    }
    
    protected org.openide.util.actions.SystemAction[] defaultActions () {
        return new SystemAction[] {
	    SystemAction.get(OpenAction.class),
	    SystemAction.get (FileSystemAction.class),
	    null,
	    SystemAction.get(CutAction.class),
	    SystemAction.get(CopyAction.class),
	    SystemAction.get(PasteAction.class),
	    null,
	    SystemAction.get(DeleteAction.class),
	    SystemAction.get(RenameAction.class),
            null,
            SystemAction.get (SaveAsTemplateAction.class),
	    null,
	    SystemAction.get(ToolsAction.class),
	    SystemAction.get(PropertiesAction.class),
	};
    }

}
