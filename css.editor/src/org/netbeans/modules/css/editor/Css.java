/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.css.editor;

import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Exceptions;

/**
 *
 * @author marek
 */
public class Css {
    
    public static final String CSS_MIME_TYPE = "text/x-css";

    /** finds first ResultIterator of the given mimetype */
    public static ResultIterator getResultIterator(ResultIterator ri, String mimetype) {
	if(ri.getSnapshot().getMimeType().equals(mimetype)) {
	    return ri;
	}
        for(Embedding e : ri.getEmbeddings() ) {
            ResultIterator eri = ri.getResultIterator(e);
            if(e.getMimeType().equals(mimetype)) {
                return eri;
            } else {
                ResultIterator eeri = getResultIterator(eri, mimetype);
                if(eeri != null) {
                    return eeri;
                }
            }
        }
        return null;
    }


    public static CloneableEditorSupport findCloneableEditorSupport(FileObject fo) {
	try {
	    DataObject dob = DataObject.find(fo);
	    Object obj = dob.getCookie(org.openide.cookies.OpenCookie.class);
	    if (obj instanceof CloneableEditorSupport) {
		return (CloneableEditorSupport)obj;
	    }
	    obj = dob.getCookie(org.openide.cookies.EditorCookie.class);
	    if (obj instanceof CloneableEditorSupport) {
		return (CloneableEditorSupport)obj;
	    }
	} catch (DataObjectNotFoundException ex) {
	    Exceptions.printStackTrace(ex);
	}
        return null;
    }

}
