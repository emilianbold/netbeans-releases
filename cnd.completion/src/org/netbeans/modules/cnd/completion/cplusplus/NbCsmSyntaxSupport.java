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

package org.netbeans.modules.cnd.completion.cplusplus;

import org.netbeans.modules.cnd.completion.cplusplus.ext.CsmSyntaxSupport;
import org.netbeans.modules.cnd.completion.cplusplus.ext.CsmFinder;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmField;
import org.netbeans.modules.cnd.api.model.CsmMethod;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmParameter;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.ArrayList;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.completion.impl.xref.FileReferencesContext;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

/**
* Support methods for syntax analyzes
*
* @author Miloslav Metelka, Vladimir Voskresensky
* @version 1.00
*/

public class NbCsmSyntaxSupport extends CsmSyntaxSupport {

    protected static final String PACKAGE_SUMMARY = "package-summary"; // NOI18N
    
    public NbCsmSyntaxSupport(BaseDocument doc) {
        super(doc);        
    }
    
    public CsmFinder getSupportJCFinder(){
        return getFinder();
    }
    
    public CsmFinder getFinder() {
        DataObject dobj = NbEditorUtilities.getDataObject(getDocument());
        assert dobj != null;
        FileObject fo = dobj.getPrimaryFile();
        return CsmFinderFactory.getDefault().getFinder(fo);
    }

    protected FileReferencesContext getFileReferencesContext() {
        return null;
    }
    
    protected DataObject getDataObject(FileObject fo) {
        DataObject dob = null;
        if (fo != null) {
            try {
                dob = DataObject.find(fo);
            } catch (DataObjectNotFoundException e) {
            }
        }
        return dob;
    }

    protected URL getDocFileObjects(String fqName, String javadocFilename) {
        DataObject dobj = NbEditorUtilities.getDataObject(getDocument());
        if (dobj == null) return null;
        FileObject fo = dobj.getPrimaryFile();
        
        
        // try to find class/package on classpath of the file being editted
        FileObject cpfo = null;
//        ClassPath cp = getMergedClassPath(fo);
//
//        // now ask the Javadoc query to get Javadoc for the object
//        String search = fqName.replace('.', '/');
//        if (javadocFilename != null) {
//            search += "/" + javadocFilename; // NOI18N
//        }
//        search += ".html"; // NOI18N
//        for (Iterator it = cp.entries().iterator(); it.hasNext();) {
//            ClassPath.Entry entry = (ClassPath.Entry)it.next();
//            URL urls[] = JavadocForBinaryQuery.findJavadoc(entry.getURL()).getRoots();
//            URL url = findResource(search, urls);            
//            if (url != null)
//                return url;
//        }
        return null;
    }
    
    public URL[] getJavaDocURLs(Object obj) {
        ArrayList urlList = new ArrayList();
        if (obj instanceof CsmNamespace) {
            CsmNamespace pkg = (CsmNamespace)obj;
            URL u = getDocFileObjects(pkg.getName().toString(), PACKAGE_SUMMARY);
            if (u != null) {
                urlList.add(u);
            }
        } else if (obj instanceof CsmClass) {
            CsmClass cls = (CsmClass)obj;
            URL u = getDocFileObjects(cls.getQualifiedName().toString(), null);
            if (u != null) {
                urlList.add(u);
            }
        } else if (obj instanceof CsmMethod) { // covers CsmConstructor too
            CsmMethod ctr = (CsmMethod)obj;
            CsmClass cls = ctr.getContainingClass();
            URL url = getDocFileObjects(cls.getQualifiedName().toString(), null);
            if (url != null) {
                    StringBuilder sb = new StringBuilder("#"); // NOI18N
                    sb.append((obj instanceof CsmMethod) ? ((CsmMethod)ctr).getName() : cls.getName());
                    sb.append('('); //NOI18N
                    CsmParameter[] parms = (CsmParameter[]) ctr.getParameters().toArray(new CsmParameter[0]);
                    int cntM1 = parms.length - 1;
                    for (int j = 0; j <= cntM1; j++) {
			//XXX
                        //sb.append(parms[j].getType().format(true));
                        if (parms[j].isVarArgs()) {
                            sb.append("...");// NOI18N
                        } else {
                            sb.append(parms[j].getType().getText());
                        }
                        if (j < cntM1) {
                            sb.append(", "); // NOI18N
                        }
                    }
                    sb.append(')'); //NOI18N
                    try {
                        urlList.add(new URL(url.toExternalForm() + sb));
                    } catch (MalformedURLException e) {
                        ErrorManager.getDefault().log(ErrorManager.ERROR, e.toString());
                    }
            }
        } else if (obj instanceof CsmField) {
            CsmField fld = (CsmField)obj;
            CsmClass cls = fld.getContainingClass();
            URL u = getDocFileObjects(cls.getQualifiedName().toString(), null);
            if (u != null) {
                try {
                    urlList.add(new URL(u.toExternalForm() + '#' + fld.getName()));
                } catch (MalformedURLException e) {
                    ErrorManager.getDefault().log(ErrorManager.ERROR, e.toString());
                }
            }
        }

        URL[] ret = new URL[urlList.size()];
        urlList.toArray(ret);
        return ret;
    }



}
