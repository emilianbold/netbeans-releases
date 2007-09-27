/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
