/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.xml.wsdl.ui.netbeans.module;

import java.io.IOException;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.ExtensionList;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.UniFileLoader;
import org.openide.util.NbBundle;

/** 
 * Recognizes .wsdl files as a single DataObject.
 *
 * @author Jerry Waldorf
 */
public class WSDLDataLoader extends UniFileLoader {
//        if we use text/*xml mime type then data editor support 
//        automatically recognize this mime type and show xml editor
//        but there is another mime resolver registered with web svc module
//        which has text/xml-wsdl as mime type and even though
//        we install out wsdl editor data object before their data object
//        it still picks mime resolver registered by web servc module
//        so work around is to use same mime resolver as websvc
//        we need to ask them to disable mime resolver there-->
        
    public static final String MIME_TYPE = "text/x-wsdl+xml";                 // NOI18N
//    public static final String MIME_TYPE = "text/xml-wsdl";                 // NOI18N
    
    private static final long serialVersionUID = -4579746482156152493L;
    
    public WSDLDataLoader() {
        super("org.netbeans.modules.xml.wsdl.ui.netbeans.module.WSDLDataObject");
    }
    
   /** Does initialization. Initializes display name,
     * extension list and the actions. */
    @Override
    protected void initialize () {
        super.initialize();
        ExtensionList ext = getExtensions();
        ext.addMimeType (MIME_TYPE);
    }
    
    
    @Override
    protected String defaultDisplayName () {
        return NbBundle.getMessage(WSDLDataLoader.class, "LBL_loaderName");
    }

    @Override
    protected String actionsContext() {
        // Load actions from layer to avoid direct dependencies on
        // modules with non-public API.
    return "Loaders/" + MIME_TYPE + "/Actions/";
    }

    @Override
    protected MultiDataObject createMultiObject (FileObject primaryFile)
    throws DataObjectExistsException, IOException {
        return new WSDLDataObject(primaryFile, this);
    }
  
//    /** 
//     * For a given file find the primary file.
//     * @param fo the file to find the primary file for
//     * @return the primary file for this file or null if this file 
//     * is not recognized by this loader.
//     */
//    protected FileObject findPrimaryFile(FileObject fo) {
//        // never recognize folders.
//        if (fo.isFolder()) return null;
//        
//        if (getExtensions().isRegistered(fo)) {
//            return fo;
//        }
//        return null;
//    }
    
//    /** @return The list of extensions this loader recognizes. */
//    public ExtensionList getExtensions() {
//        ExtensionList extensions = (ExtensionList)getProperty(PROP_EXTENSIONS);
//        if (extensions == null) {
//            extensions = new ExtensionList();
//            extensions.addExtension(WSDL_EXTENSION);
//            putProperty(PROP_EXTENSIONS, extensions, false);
//        }
//        return extensions;
//    }
    
//    /** 
//     * Sets the extension list for this data loader.
//     * @param ext new list of extensions.
//     */
//    public void setExtensions(ExtensionList ext) {
//        putProperty(PROP_EXTENSIONS, ext, true);
//    }
}
