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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * J2MEDataObject.java
 *
 * Created on February 20, 2004, 1:05 PM
 */
package org.netbeans.modules.mobility.editor.pub;

import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.java.JavaDataObject;
import org.netbeans.modules.java.JavaEditor;
import org.netbeans.modules.mobility.editor.J2MENode;
import org.netbeans.modules.mobility.project.ApplicationDescriptorHandler;
import org.netbeans.modules.mobility.project.ProjectConfigurationsHelper;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiFileLoader;
import org.openide.nodes.Node;

import java.io.IOException;

/**
 *
 * @author  gc149856
 */
public class J2MEDataObject extends JavaDataObject {
    static final long serialVersionUID = 8090017233591568305L;
    
    public J2MEDataObject(FileObject pf, MultiFileLoader loader)  throws DataObjectExistsException {
        super(pf,loader);
        setCookieClasses();
    }
    
    private void setCookieClasses() {
        getCookieSet().remove(JavaEditor.class,this);
        getCookieSet().add(J2MEEditorSupport.class,this);
        getCookieSet().add(EditorCookie.class,this);
    }
    
    
    
    protected JavaEditor createJavaEditor() {
        final Project p = FileOwnerQuery.getOwner(getPrimaryFile());
        if (p != null) {
            final ProjectConfigurationsHelper pch = p.getLookup().lookup(ProjectConfigurationsHelper.class);
            if (pch != null) return new J2MEEditorSupport(this, pch);
        }
        return super.createJavaEditor();
    }
    
    @SuppressWarnings("unchecked")
	public Node.Cookie createCookie(final Class klass) {
        if (klass.isAssignableFrom(J2MEEditorSupport.class))
            return getJavaEditor();
        return super.createCookie(klass);
    }
    
    public Node createNodeDelegate() {
        return new J2MENode(this, super.createNodeDelegate());
    }
    
    protected FileObject handleRename(final String name) throws IOException {
        ApplicationDescriptorHandler.getDefault().handleRename(getPrimaryFile(), name);
        return super.handleRename(name);
    }
    
    protected FileObject handleMove(final DataFolder df) throws IOException {
        ApplicationDescriptorHandler.getDefault().handleMove(getPrimaryFile(), df.getPrimaryFile());
        return super.handleMove(df);
    }
    
    protected void handleDelete() throws java.io.IOException {
        ApplicationDescriptorHandler.getDefault().handleDelete(getPrimaryFile());
        super.handleDelete();
    }
}
