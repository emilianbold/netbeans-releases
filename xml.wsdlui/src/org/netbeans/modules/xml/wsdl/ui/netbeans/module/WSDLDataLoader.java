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
    public static final String PROP_EXTENSIONS = "extensions"; // NOI18N

//        if we use text/*xml mime type then data editor support 
//        automatically recognize this mime type and show xml editor
//        but there is another mime resolver registered with web svc module
//        which has text/xml-wsdl as mime type and even though
//        we install out wsdl editor data object before their data object
//        it still picks mime resolver registered by web servc module
//        so work around is to use same mime resovlver as websvc
//        we need to ask them to disable mime resolver there-->
        
//    public static final String MIME_TYPE = "text/x-wsdl+xml";                 // NOI18N
    public static final String MIME_TYPE = "text/xml-wsdl";                 // NOI18N
    
    private static final long serialVersionUID = -4579746482156152493L;
    
    public WSDLDataLoader() {
        super("org.netbeans.modules.xml.wsdl.ui.netbeans.module.WSDLDataObject");
    }
    
   /** Does initialization. Initializes display name,
     * extension list and the actions. */
    protected void initialize () {
        super.initialize();
        ExtensionList ext = getExtensions();
        ext.addMimeType (MIME_TYPE);
        //ext.addExtension("wsdl");
    }
    
    
    protected String defaultDisplayName () {
        return NbBundle.getMessage(WSDLDataLoader.class, "LBL_loaderName");
    }

    protected String actionsContext() {
        // Load actions from layer to avoid direct dependencies on
        // modules with non-public API.
	return "Loaders/text/xml-wsdl/Actions/";
    }

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
