/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 2, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 2] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 2 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 2 code and therefore, elected the GPL
Version 2 license, then the option applies only if the new code is
made subject to such option by the copyright holder.
If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 2, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 2] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 2 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 2 code and therefore, elected the GPL
Version 2 license, then the option applies only if the new code is
made subject to such option by the copyright holder.
 */
package org.netbeans.modules.db.dataview.editor;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;

import com.sun.org.apache.xerces.internal.util.XMLChar;
import org.openide.filesystems.FileUtil;

/**
 * Represents a dataview file.
 *
 * @author  radval
 */
public class DataViewDataObject extends MultiDataObject {

    private FileObject fileObj;
    private static final long serialVersionUID = 6338889116068357651L;
    private transient DataViewEditorSupport editorSupport;
    private transient AtomicReference<Lookup> myLookup = new AtomicReference<Lookup>();
    private transient AtomicBoolean isLookupInit = new AtomicBoolean(false);
    private Node mNode = null;

    public DataViewDataObject(FileObject fObj, MultiFileLoader loader) throws DataObjectExistsException {
        super(fObj, loader);
        CookieSet set = getCookieSet();
        fileObj = fObj;
        editorSupport = new DataViewEditorSupport(this);
        // editor support defines MIME type understood by EditorKits registry
        
        set.add(editorSupport);
    }

    @Override
    protected Node createNodeDelegate() {
        if (this.mNode == null) {
            this.mNode = new DataViewNode(this);
        }
        return this.mNode;
    }

    /**
     * subclasses should look updateServices() and additionalInitialLookup()
     */
    @Override
    public final Lookup getLookup() {
        if (myLookup.get() == null) {

            Lookup lookup;
            List<Lookup> list = new LinkedList<Lookup>();

            list.add(Lookups.fixed(new Object[]{this}));
            lookup = new ProxyLookup(list.toArray(new Lookup[list.size()]));
            myLookup.compareAndSet(null, lookup);
            isLookupInit.compareAndSet(false, true);
        }
        return myLookup.get();
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    // If you add context help, change to:
    // return new HelpCtx (MyDataObject.class);
    }

    @Override
    protected FileObject handleRename(String name) throws IOException {
        DataViewEditorSupport weSupport = getDataViewEditorSupport();
        String oldName = this.getName();
        weSupport.updateTitles();


        //logic to keep the status of the save badge (*) intact.
        boolean modified = weSupport.getEnv().isModified();
        if (modified) {
            weSupport.getEnv().unmarkModified();
        }
        FileObject fo = super.handleRename(name);
        if (modified) {
            weSupport.getEnv().markModified();
        }
        firePropertyChange(DataObject.PROP_NAME, oldName, name);
        return fo;
    }

    @Override
    protected void handleDelete() throws IOException {
        //this is work around when file is modified in editor and
        //editor has lock
        getDataViewEditorSupport().getEnv().unmarkModified();

        super.handleDelete();
    }

    @Override
    protected DataObject handleCreateFromTemplate(DataFolder df, String name) throws IOException {
        DataViewDataObject dataObject = (DataViewDataObject) super.handleCreateFromTemplate(df, name);
        String doName = dataObject.getName();
        //make sure the the name is a valid NMTOKEN.
        if (!XMLChar.isValidNmtoken(doName)) {
            return dataObject;
        }

        SaveCookie sCookie = (SaveCookie) dataObject.getCookie(SaveCookie.class);
        if (sCookie != null) {
            sCookie.save();
        }
        return dataObject;
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
                getDataViewEditorSupport().saveDocument();
            }

            @Override
            public int hashCode() {
                return getClass().hashCode();
            }

            @Override
            public boolean equals(Object other) {
                return getClass().equals(other.getClass());
            }
        };
    }

    public DataViewEditorSupport getDataViewEditorSupport() {
        return editorSupport;
    }

    public String getPath() {
        String path = null;
        try {
            path = FileUtil.toFile(fileObj).getParentFile().getParentFile().getAbsolutePath();
        } catch (Exception ex) {
        }
        return path;
    }
}

