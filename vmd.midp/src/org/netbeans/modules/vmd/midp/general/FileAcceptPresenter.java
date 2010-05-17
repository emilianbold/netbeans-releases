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

package org.netbeans.modules.vmd.midp.general;

import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.api.model.common.AcceptPresenter;
import org.netbeans.modules.vmd.api.model.common.AcceptSuggestion;
import org.netbeans.modules.vmd.api.model.common.DocumentSupport;
import org.netbeans.modules.vmd.midp.components.MidpArraySupport;
import org.netbeans.modules.vmd.midp.components.MidpProjectSupport;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.nodes.NodeTransfer;

import java.awt.datatransfer.Transferable;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Karol Harezlak
 */
public abstract class FileAcceptPresenter extends AcceptPresenter {
    
    private Map<String, TypeID> extensionsMap;
    private Map<String, String> propertyNamesMap;
    
    public FileAcceptPresenter(String propertyName, TypeID typeID, String... fileExtensions) {
        super(AcceptPresenter.Kind.TRANSFERABLE);
        extensionsMap = new HashMap<String, TypeID>();
        propertyNamesMap = new HashMap<String, String>();
        addFileExtensions(propertyName, typeID, fileExtensions);
    }
    
    public FileAcceptPresenter addFileExtensions(String propertyName, TypeID typeID, String... fileExtensions) {
        assert typeID != null;
        assert fileExtensions.length != 0;
        
        for (String fe : fileExtensions) {
            extensionsMap.put(fe, typeID);
            propertyNamesMap.put(fe, propertyName);
        }
        return this;
    }
    
    @Override
    public boolean isAcceptable (Transferable transferable, AcceptSuggestion suggestion) {
        assert (!extensionsMap.isEmpty());
        FileObject fileObject = getNodeFileObject(transferable);
        if (fileObject == null && !belongsToProject(fileObject))
            return false;
        DesignDocument document = getComponent().getDocument();
        Map<FileObject, String> mapFile = MidpProjectSupport.getAllFilesForProjectByExt(document, extensionsMap.keySet());
        if (mapFile.get(fileObject) == null)
            return false;
        TypeID typeID = getTypeForExtension(fileObject.getExt());
        if (typeID != null)
            return true;
        return false;
    }
    
    @Override
    public ComponentProducer.Result accept (Transferable transferable, AcceptSuggestion suggestion) {
        FileObject fileObject = getNodeFileObject(transferable);
        TypeID typeID = getTypeForExtension(fileObject.getExt());
        String propertyName = getPropertyNameForExtension(fileObject.getExt());
        if (propertyName == null)
            return super.accept(transferable, suggestion);
        DesignDocument document = getComponent().getDocument();
        final ComponentProducer producer = DocumentSupport.getComponentProducer(document, typeID.toString ());
        if (document == null || producer == null)
            return super.accept(transferable, suggestion);
        DesignComponent newComponent = producer.createComponent(document).getMainComponent ();
        if (getComponent().readProperty(propertyName).getKind() == PropertyValue.Kind.ARRAY)
            MidpArraySupport.append(getComponent(), propertyName, newComponent);
        else
            getComponent().writeProperty(propertyName, PropertyValue.createComponentReference(newComponent));
        return new ComponentProducer.Result(newComponent);
    }
    
    protected boolean belongsToProject(FileObject fileObject) {
        DesignDocument document = getComponent().getDocument();
        Map<FileObject, String> fileMap = MidpProjectSupport.getAllFilesForProjectByExt(document, extensionsMap.keySet());
        return fileMap.get(fileObject) != null;
    }
    
    protected InputStream getInputStream(Transferable transferable) {
        try {
            FileObject fileNode = getNodeFileObject(transferable);
            return fileNode != null ? fileNode.getInputStream() : null;
        } catch (FileNotFoundException ex) {
            Debug.warning(ex);
        }
        return null;
    }
    
    protected String getFileClasspath(FileObject fileObject) {
        DesignDocument document = getComponent().getDocument();
        Map<FileObject,String> fileMap = MidpProjectSupport.getAllFilesForProjectByExt(document, Arrays.asList(fileObject.getExt()));
        return fileMap.get(fileObject);
    }
    
    protected FileObject getNodeFileObject(Transferable transferable) {
        Node node = NodeTransfer.node(transferable, NodeTransfer.DND_COPY_OR_MOVE);
        if (node == null)
            return null;
        DataObject dataObject = (DataObject) node.getLookup().lookup(DataObject.class);
        if (dataObject == null)
            return null;
        FileObject file = dataObject.getPrimaryFile();
        return file;
    }
    
    private TypeID getTypeForExtension(String fe) {
        for (String key : extensionsMap.keySet()) {
            if (key.equalsIgnoreCase(fe))
                return extensionsMap.get(key);
        }
        return null;
    }
    
    private String getPropertyNameForExtension(String fe) {
        for (String key : propertyNamesMap.keySet()) {
            if (key.equalsIgnoreCase(fe))
                return propertyNamesMap.get(key);
        }
        return null;
    }
    
}
