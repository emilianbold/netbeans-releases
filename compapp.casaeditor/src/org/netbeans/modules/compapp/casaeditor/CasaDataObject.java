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
package org.netbeans.modules.compapp.casaeditor;

import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.nodes.Node;

import java.io.IOException;
import org.netbeans.modules.compapp.casaeditor.design.CasaModelGraphUtilities;
import org.netbeans.modules.compapp.casaeditor.nodes.CasaNode;
import org.netbeans.modules.compapp.projects.jbi.api.JbiBuildListener;
import org.netbeans.spi.xml.cookies.CheckXMLSupport;
import org.netbeans.spi.xml.cookies.DataObjectAdapters;
import org.netbeans.spi.xml.cookies.ValidateXMLSupport;
import org.openide.cookies.SaveCookie;
import org.openide.loaders.DataFolder;
import org.openide.loaders.MultiFileLoader;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.xml.sax.InputSource;

/**
 * @author tli
 */
public class CasaDataObject extends MultiDataObject {

    static final long serialVersionUID = 7527025549386876556L;
    public static final String CASA_ICON_BASE_WITH_EXT = "org/netbeans/modules/compapp/casaeditor/resources/service_composition_16.png"; // NOI18N
    private transient CasaDataEditorSupport editorSupport;
    private transient Node.Cookie mBuildCookie = new JbiBuildCookie();
    private transient boolean mIsBuilding;
    private SaveCookie saveCookie;

    public CasaDataObject(FileObject pf, MultiFileLoader loader)
            throws DataObjectExistsException {
        super(pf, loader);

        editorSupport = new CasaDataEditorSupport(this);

        getCookieSet().add(editorSupport);
        getCookieSet().add(mBuildCookie);

        InputSource in = DataObjectAdapters.inputSource(this);
        getCookieSet().add(new CheckXMLSupport(in));
        getCookieSet().add(new ValidateXMLSupport(in));

        saveCookie = new SaveCookie() {

            public void save() throws IOException {
                getEditorSupport().saveDocument();
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
    
    public CasaDataEditorSupport getEditorSupport() {
        return editorSupport;
    }

    @Override
    protected Node createNodeDelegate() {
        return new CasaDataNode(this);
    }

    @Override
    public Lookup getLookup() {
        return getCookieSet().getLookup();
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(CasaDataObject.class);
    }

    @Override
    protected void handleDelete() throws IOException {
        if (isModified()) {
            setModified(false);
        }
        getEditorSupport().getEnv().unmarkModified();
        super.handleDelete();
    }

    @Override
    protected FileObject handleMove(DataFolder df) throws IOException {
        //TODO:make sure we save file before moving This is what jave move does.
        //It also launch move refactoring dialog which we should be doing
        //as well
        if (isModified()) {
            SaveCookie sCookie = this.getCookie(SaveCookie.class);
            if (sCookie != null) {
                sCookie.save();
            }
        }
        return super.handleMove(df);
    }

    @Override
    public void setModified(boolean modified) {
        super.setModified(modified);

        if (modified) {
            getCookieSet().add(saveCookie);
        } else {
            getCookieSet().remove(saveCookie);
        }

        updateTopComponentActivatedNodesSaveCookie();
    }

    // #162336
    // In the current version (6.5.1), SaveCookie needs to be associated with the
    // TopComponent's activated nodes. In the latest trunk (6.8), the SaveCookie
    // seems to be divorced from the Node. See http://blogs.sun.com/geertjan/date/20090713
    public void updateTopComponentActivatedNodesSaveCookie() {
        TopComponent tc = WindowManager.getDefault().getRegistry().getActivated();
        if (tc != null) {
            Node[] nodes = tc.getActivatedNodes();

            if (nodes != null) {
                for (Node node : nodes) {
                    if (node instanceof CasaNode) {
                        if (isModified()) {
                            ((CasaNode) node).addContent(saveCookie);
                        } else {
                            ((CasaNode) node).removeContent(saveCookie);
                        }
                    }
                }
            }
        }
    }

    public boolean isBuilding() {
        return mIsBuilding;
    }

    private class JbiBuildCookie implements Node.Cookie, JbiBuildListener {

        public void buildStarted() {
            mIsBuilding = true;
            if (editorSupport != null && editorSupport.getScene() != null) {
                // Immediately disable edits to the scene.
                CasaModelGraphUtilities.setSceneEnabled(editorSupport.getScene());
            }
        }

        public void buildCompleted(boolean isSuccessful) {
            mIsBuilding = false;
            if (!isSuccessful) {
                if (editorSupport != null && editorSupport.getScene() != null) {
                    CasaModelGraphUtilities.setSceneEnabled(editorSupport.getScene(), true);
                }
            } else {
                // Do not set the scene enabled at this point, because the build
                // is still processing the model. We simply set the scene enabled
                // at render time, which is a safe time to allow editability.
            }
        }
    }
}
