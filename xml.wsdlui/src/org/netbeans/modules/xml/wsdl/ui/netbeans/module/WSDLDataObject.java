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
package org.netbeans.modules.xml.wsdl.ui.netbeans.module;

import java.io.IOException;
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

        // editor support defines MIME type understood by EditorKits registry
        set.add(new WSDLEditorSupport(this));

        // Add check and validate cookies
        InputSource is = DataObjectAdapters.inputSource(this);
        set.add(new CheckXMLSupport(is));
        //set.add(new ValidateSchemaSupport(is));

        set.add(new WSDLMultiViewSupport(this));
        //add validate action here
        set.add(new WSDLValidateXMLCookie(this));

        set.assign(SearchProvider.class, new SearchProvider(this));
        set.assign(XmlFileEncodingQueryImpl.class, XmlFileEncodingQueryImpl.singleton());
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
        return getCookie(WSDLEditorSupport.class);
    }

    @Override
    public Lookup getLookup() {
        return getCookieSet().getLookup();
    }
    
    private static final long serialVersionUID = 6338889116068357651L;
    public static final String WSDL_ICON_BASE_WITH_EXT = "org/netbeans/modules/xml/wsdl/ui/netbeans/module/resources/wsdl_file.png";
}
