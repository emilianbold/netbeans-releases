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
import java.util.concurrent.atomic.AtomicReference;
import org.netbeans.modules.xml.api.XmlFileEncodingQueryImpl;

import org.netbeans.spi.xml.cookies.CheckXMLSupport;
import org.netbeans.spi.xml.cookies.DataObjectAdapters;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.xml.sax.InputSource;

/**
 * Represents a WSDL file.
 *
 * @author  Jerry Waldorf
 */
public class WSDLDataObject extends MultiDataObject {

    public WSDLDataObject(FileObject fObj, MultiFileLoader loader) throws
            DataObjectExistsException {
        super(fObj, loader);
        CookieSet set = getCookieSet();

        editorSupport = new WSDLEditorSupport(this);
        // editor support defines MIME type understood by EditorKits registry
        set.add(editorSupport);

        // Add check and validate cookies
        InputSource is = DataObjectAdapters.inputSource(this);
        set.add(new CheckXMLSupport(is));
        //set.add(new ValidateSchemaSupport(is));

        set.add(new WSDLMultiViewSupport(this));
        //add validate action here
        set.add(new WSDLValidateXMLCookie(this));
    }

    @Override
    protected Node createNodeDelegate() {
        return new WSDLNode(this);
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected void handleDelete() throws IOException {
        if (isModified()) {
            setModified(false);
        }
        getWSDLEditorSupport().getEnv().unmarkModified();
        super.handleDelete();
    }

    @Override
	protected FileObject handleMove(DataFolder df) throws IOException {
        //TODO:make sure we save file before moving This is what jave move does.
        //It also launch move refactoring dialog which we should be doing
        //as well
        if(isModified()) {
            SaveCookie sCookie = this.getCookie(SaveCookie.class);
            if(sCookie != null) {
                sCookie.save();
            }
        }

        return super.handleMove(df);
    }


    @Override
    public void setModified(boolean modified) {
        super.setModified(modified);
        if (modified) {
            getCookieSet().add(getSaveCookie());
        } else {
            getCookieSet().remove(getSaveCookie());
        }
    }

    private SaveCookie getSaveCookie() {
        return new SaveCookie() {
            public void save() throws IOException {
                getWSDLEditorSupport().saveDocument();
            }

            @Override
            public int hashCode() {
                return getClass().hashCode();
            }

            @Override
            public boolean equals(Object other) {
                return other != null && getClass().equals(other.getClass());
            }
        };
    }

    public WSDLEditorSupport getWSDLEditorSupport() {
        return editorSupport;
    }

    public Lookup getLookup() {
        if (myLookup.get() == null) {
            Lookup superLookup = super.getLookup();
            //
            Lookup[] lookupArr = new Lookup[] {
                Lookups.fixed(XmlFileEncodingQueryImpl.singleton()), 
                superLookup};
            //
            Lookup newLookup = new ProxyLookup(lookupArr);
            myLookup.compareAndSet(null, newLookup);
        }
        return myLookup.get();
    }
    
    private transient AtomicReference<Lookup> myLookup = 
        new AtomicReference<Lookup>();
    
    private static final long serialVersionUID = 6338889116068357651L;
    private transient WSDLEditorSupport editorSupport;

    public static final String WSDL_ICON_BASE_WITH_EXT = "org/netbeans/modules/xml/wsdl/ui/netbeans/module/resources/wsdl_file.png";
}
