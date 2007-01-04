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

package  org.netbeans.modules.cnd.editor;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Set;
import org.openide.ErrorManager;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

public class CCClassFileIterator extends CCFSrcFileIterator {
    public Set instantiate (TemplateWizard wiz) throws IOException {
        DataFolder targetFolder = wiz.getTargetFolder ();
        DataObject template = wiz.getTemplate ();
	String ext = template.getPrimaryFile().getExt();

	String filename = wiz.getTargetName();
	if (filename != null && ext != null) {
	    if (filename.endsWith("." + ext)) { // NOI18N
		// strip extension, it will be added later ...
		filename = filename.substring(0, filename.length()-(ext.length()+1));
	    }

	    // We use the name for the template in the file, and that's why it has to be an identifier...
	    if (!Utilities.isJavaIdentifier(filename)) {
		String msg = MessageFormat.format(getString("NOT_A_VALID_CPP_IDENTIFIER"), 
		new Object[] {
		    filename
		});
		IllegalStateException x = (IllegalStateException)ErrorManager.getDefault().annotate(
		new IllegalStateException(msg),
		ErrorManager.USER, null, msg,
		null, null
		);
		throw x;
	    }
	}

	return super.instantiate(wiz);
    }

    String getString(String key) {
	return NbBundle.getBundle(CCClassFileIterator.class).getString(key);
    }
}
