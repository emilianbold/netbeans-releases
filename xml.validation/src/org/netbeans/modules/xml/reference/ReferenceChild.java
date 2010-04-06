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
 * License. When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP. Sun designates this
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
package org.netbeans.modules.xml.reference;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import java.net.URLDecoder;
import javax.swing.Action;

import org.openide.awt.HtmlBrowser;
import org.openide.cookies.EditCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.Utilities;

import org.netbeans.api.project.Project;
import org.netbeans.modules.xml.retriever.catalog.CatalogEntry;
import org.netbeans.modules.xml.retriever.catalog.CatalogWriteModel;
import static org.netbeans.modules.xml.util.UI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2008.06.09
 */
public final class ReferenceChild extends AbstractNode {

    ReferenceChild(CatalogEntry system, CatalogWriteModel catalog, Project project) {
        super(Children.LEAF);
        mySystem = system;
        myProject = project;
        myCatalog = catalog;
        myReferenceHelper = new ReferenceHelper(project);
        myLocalActions  = new Action[] { new OpenAction(), null, new EditAction(), null, new DeleteAction() };
        myRemoteActions = new Action[] { new OpenAction(), null, new EditAction(), new ReloadAction(), null, new DeleteAction() };
    }

    @Override
    public void destroy() throws IOException {
        deleteNode();
    }

    private void deleteNode() {
        for (Action action : myRemoteActions) {
            if (action instanceof DeleteAction) {
                action.actionPerformed(null);
                return;
            }
        }
    }

    @Override
    public boolean canDestroy() {
        return true;
    }

    @Override
    public Image getIcon(int type) {
        return icon(ReferenceNode.class, isRemoteResource() ? "remote" : "local").getImage(); // NOI18N
    }

    @Override
    public boolean canRename() {
        return false;
    }

    @Override
    public String getDisplayName() {
        String name = getFileName(mySystem.getSource());

        if (name == null) {
            return name;
        }
        try {
            return URLDecoder.decode(name, "UTF-8"); // NOI18N
        }
        catch (UnsupportedEncodingException e) {
            return name;
        }
    }

    private String getFileName(String file) {
        if (file == null) {
            return null;
        }
        file = file.replaceAll("%20", " "); // NOI18N

        if (file.startsWith("file:")) { // NOI18N
            file = file.substring(2*2+1);
        }
        if (file.startsWith("/") && Utilities.isWindows()) {
            file = file.substring(1);
        }
        return file.replace("\\", "/"); // NOI18N
    }

    @Override
    public Action getPreferredAction() {
        return myRemoteActions[0];
    }

    @Override
    public String getName() {
        return getDisplayName();
    }

    @Override
    public Action[] getActions(boolean context) {
        return isRemoteResource() ? myRemoteActions : myLocalActions;
    }

    public String getSystemId() {
        return mySystem.getSource();
    }

    public String getLocation() {
        return mySystem.getTarget();
    }

    public boolean isRemoteResource() {
        return mySystem.getSource().contains("://") && myReferenceHelper.getFromProjectOrLocal(mySystem.getTarget()) == null; // NOI18N
    }

    public boolean hasExtension(String extension) {
        return mySystem.getSource().endsWith("." + extension) || mySystem.getTarget().endsWith("." + extension); // NOI18N
    }

    public FileObject getFileObject() {
        FileObject file = getFileFromProjectOrLocal(getName());

        if (file == null) {
            file = getFromRemote(false);
        }
        return file;
    }

    public String getURL() {
        return getDisplayName();
    }

    private FileObject getFileFromProjectOrLocal(String name) {
//out();
//out("getFileFromProjectOrLocal: " + name);
        FileObject file = FileUtil.toFileObject(FileUtil.normalizeFile(new File(name)));
        String target = mySystem.getTarget();

        if (file == null && target.startsWith("src/")) { // NOI18N
//out("target: " + target);
            file = myCatalog.getCatalogFileObject().getParent().getFileObject(target);
//out("file1: " + file);
        }
        return myReferenceHelper.getFromProjectOrLocal(target);
    }

    FileObject getFromRemote(boolean forceReload) {
        for (Action action : myRemoteActions) {
            if (action instanceof ReloadAction) {
                return ((ReloadAction) action).reload(forceReload);
            }
        }
        return null;
    }

    // -----------------------------------------------
    private class OpenAction extends ReferenceAction {

        @Override
        protected String getKeyName() {
            return isRemoteResource() ? "LBL_Open_URL" : "LBL_Open_Resource"; // NOI18N
        }

        public void actionPerformed(ActionEvent event) {
            String name = getName();

            if (name.contains("://")) { // NOI18N
                openInBrowser(name);
            }
            else {
                openInEditor(name);
            }
        }

        private void openInBrowser(String name) {
//out();
//out("Browser: " + name);
            try {
                HtmlBrowser.URLDisplayer.getDefault().showURL(new URL(name));
            }
            catch (MalformedURLException e) {
                return;
            }
        }

        private void openInEditor(String name) {
//out();
            FileObject file = getFileFromProjectOrLocal(name);
//out("Editor: " + file);
            if (file == null) {
                return;
            }
            DataObject data;

            try {
                data = DataObject.find(file);
            }
            catch (DataObjectNotFoundException e) {
//out();
//out("EXCEPTION: " + e.getMessage());
//out();
                return;
            }
            EditCookie edit = data.getCookie(EditCookie.class);

            if (edit == null) {
//out();
//out("e.cookie is null");
//out();
                return;
            }
            edit.edit();
        }
    }

    // -----------------------------------------------
    private class EditAction extends ReferenceAction {

        @Override
        protected String getKeyName() {
            return isRemoteResource() ? "LBL_Edit_URL" : "LBL_Replace_with"; // NOI18N
        }

        public void actionPerformed(ActionEvent event) {
            Object object;

            if (isRemoteResource()) {
                object = myReferenceHelper.addURLAction(getDisplayName());
            }
            else {
                object = myReferenceHelper.addFileAction();
            }
//out("EDIT: " + object);
            if (object == null) {
                return;
            }
            deleteNode();
        }
    }

    // -------------------------------------------------
    private class ReloadAction extends ReferenceAction {

        @Override
        protected String getKeyName() {
            return "LBL_Reload"; // NOI18N
        }

        @Override
        public boolean isEnabled() {
            return isRemoteResource();
        }

        public void actionPerformed(ActionEvent event) {
            reload(true);
        }

        FileObject reload(boolean forceReload) {
//out("RELOAD: ");
            if ( !isRemoteResource()) {
                return null;
            }
            return myReferenceHelper.getRemote(myCatalog, mySystem, forceReload);
        }
    }

    // -------------------------------------------------
    private class DeleteAction extends ReferenceAction {

        @Override
        protected String getKeyName() {
            return isRemoteResource() ? "LBL_Delete_URL" : "LBL_Delete_Resource"; // NOI18N
        }

        public void actionPerformed(ActionEvent event) {
//out("REMOVE: " + myCatalog.getClass());
            try {
                myCatalog.removeURI(new URI(mySystem.getSource()));
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
    }

    private Project myProject;
    private CatalogEntry mySystem;
    private Action[] myLocalActions;
    private Action[] myRemoteActions;
    private CatalogWriteModel myCatalog;
    private ReferenceHelper myReferenceHelper;
}
