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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.php.model;

import java.io.IOException;

import javax.swing.text.Document;

import org.netbeans.editor.BaseDocument;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Lookup;
import org.openide.util.UserQuestionException;
import org.openide.util.lookup.Lookups;



/**
 * @author ads
 *
 */
public abstract class ModelAccess {
    
    protected ModelAccess(){
    }
    
    public static ModelAccess getAccess(){
        ModelAccess access = Lookup.getDefault().lookup( ModelAccess.class );
        return access;
    }
    
    public static ModelOrigin getModelOrigin( FileObject fileObject ) {
        final DataObject dobj;
        try {
            dobj = DataObject.find(fileObject);
        }
        catch (DataObjectNotFoundException e) {
            return null;
        }
        Lookup proxyLookup = Lookups.proxy(new Lookup.Provider() {

            public Lookup getLookup() {
                try {
                    Document document = null;
                    document = getDocument(dobj);
                    if (document != null) {
                        return Lookups.fixed(new Object[] {
                                dobj.getPrimaryFile(), document, dobj, });
                    }
                    else {
                        return Lookups.fixed(new Object[] {
                                dobj.getPrimaryFile(), dobj, });
                    }
                }
                catch (IOException e) {
                    // TODO : log exception
                    return Lookups.fixed(new Object[] { dobj, });
                }
            }
        });
        return new ModelOrigin(proxyLookup);
    }
    
    public static Document getDocument(FileObject fileObject){
        Document result = null;
        try {
            DataObject dObject = DataObject.find(fileObject);
            EditorCookie ec = (EditorCookie)dObject.getCookie(EditorCookie.class);
            Document doc = ec.openDocument();
            if(doc instanceof BaseDocument){
                return doc;
            }

            // TODO : need to somehow access to PhpKit  

            //result = new org.netbeans.editor.BaseDocument(PhpKit.class, false);
            String str = doc.getText(0, doc.getLength());
            result.insertString(0,str,null);

        } catch (Exception dObjEx) {
            return null;
        }
        return result;
    }
    
    private static Document getDocument(DataObject dataObject) 
        throws IOException 
    {
        Document result = null;
        if (dataObject != null && dataObject.isValid()) {
            EditorCookie ec = (EditorCookie)
            dataObject.getCookie(EditorCookie.class);
            assert ec != null : "Data object "+
            dataObject.getPrimaryFile().getPath()+
                " has no editor cookies.";
            Document doc = null;
            try {
                doc = ec.openDocument();
            } catch (UserQuestionException uce) {
                // this exception is thrown if the document is to large
                // lets just confirm that it is ok
                uce.confirmed();
                doc = ec.openDocument();
            }
            assert doc instanceof BaseDocument;
            result = doc;
        }
        return result;
    }
    
    public abstract PhpModel getModel( ModelOrigin origin );
    
}
