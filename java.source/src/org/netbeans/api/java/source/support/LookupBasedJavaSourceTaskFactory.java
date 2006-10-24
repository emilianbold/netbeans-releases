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
package org.netbeans.api.java.source.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Result;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/**A {@link JavaSourceTaskFactorySupport} that registers tasks to all files that are
 * found in the given {@link Lookup}.
 *
 * This factory searches for {@link FileObject}, {@link DataObject} and {@link Node}
 * in the lookup. If {@link Node}(s) are found, its/their lookup is searched for
 * {@link FileObject} and {@link DataObject}.
 *
 * @author Jan Lahoda
 */
public abstract class LookupBasedJavaSourceTaskFactory extends JavaSourceTaskFactorySupport {

    private Result<FileObject> fileObjectResult;
    private Result<DataObject> dataObjectResult;
    private Result<Node> nodeResult;
    
    private List<FileObject> currentFiles;
    private LookupListener listener;

    public LookupBasedJavaSourceTaskFactory() {
        currentFiles = Collections.emptyList();
        listener = new LookupListenerImpl();
    }

    /**Sets a new {@link Lookup} to search.
     *
     * @param lookup new {@link Lookup}
     */
    protected synchronized void setLookup(Lookup lookup) {
        if (fileObjectResult != null) {
            fileObjectResult.removeLookupListener(listener);
        }
        if (dataObjectResult != null) {
            dataObjectResult.removeLookupListener(listener);
        }
        if (nodeResult != null) {
            nodeResult.removeLookupListener(listener);
        }
        fileObjectResult = lookup.lookupResult(FileObject.class);
        dataObjectResult = lookup.lookupResult(DataObject.class);
        nodeResult = lookup.lookupResult(Node.class);

        fileObjectResult.addLookupListener(listener);
        dataObjectResult.addLookupListener(listener);
        nodeResult.addLookupListener(listener);

        updateCurrentFiles();
        fireChangeEvent();
    }

    private synchronized void updateCurrentFiles() {
        Set<FileObject> newCurrentFiles = new HashSet();

        newCurrentFiles.addAll(fileObjectResult.allInstances());

        for (DataObject d : dataObjectResult.allInstances()) {
            newCurrentFiles.add(d.getPrimaryFile());
        }

        for (Node n : nodeResult.allInstances()) {
            newCurrentFiles.addAll(n.getLookup().lookupAll(FileObject.class));

            for (DataObject d : n.getLookup().lookupAll(DataObject.class)) {
                newCurrentFiles.add(d.getPrimaryFile());
            }
        }

        currentFiles = new ArrayList<FileObject>(newCurrentFiles);
    }
    
    /**@inheritDoc*/
    public synchronized List<FileObject> getFileObjects() {
        return currentFiles;
    }

    private class LookupListenerImpl implements LookupListener {
        public void resultChanged(LookupEvent ev) {
            updateCurrentFiles();
            fireChangeEvent();
        }
    }

}
