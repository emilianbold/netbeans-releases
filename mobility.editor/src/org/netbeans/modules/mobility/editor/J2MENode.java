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

package org.netbeans.modules.mobility.editor;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.mobility.project.MIDletsCacheHelper;
import org.netbeans.modules.mobility.project.MIDletsCacheListener;
import org.netbeans.modules.mobility.editor.pub.J2MEDataObject;
import org.openide.filesystems.FileObject;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;

import java.awt.*;

/**
 * @author David Kaspar
 */
public class J2MENode extends FilterNode implements MIDletsCacheListener {
    
    public static final Image entryPointImage = Utilities.loadImage("org/netbeans/modules/mobility/editor/resources/entrypoint.gif", true); //NOI18N
    
    final private J2MEDataObject dao;
    final private Node node;
    private MIDletsCacheListener listener;
    
    public J2MENode(J2MEDataObject dao, Node node) {
        super(node);
        this.node = node;
        this.dao = dao;
    }
    
    public Image createIcon(final int type) {
        Image original = node.getIcon(type);
        final Project p = FileOwnerQuery.getOwner(dao.getPrimaryFile());
        if (p == null)
            return original;
        final MIDletsCacheHelper cache = p.getLookup().lookup(MIDletsCacheHelper.class);
        if (cache == null)
            return original;
        final FileObject file = dao.getPrimaryFile();
        if (file == null)
            return original;
        if (listener == null) {
            listener = WeakListeners.create(MIDletsCacheListener.class, this, cache);
            cache.addMIDletsCacheListener(listener);
        }
        final ClassPath cp = ClassPath.getClassPath(file, ClassPath.SOURCE);
        if (cp == null)
            return original;
        final String midlet = cp.getResourceName(file, '.', false);
        if (cache.contains(midlet))
            if (original != null  &&  entryPointImage != null)
                original = Utilities.mergeImages(original, entryPointImage, 0, 0);
        return original;
    }
    
    public Image getIcon(final int type) {
        return createIcon(type);
    }
    
    public Image getOpenedIcon(final int type) {
        return getIcon(type);
    }
    
    public void cacheChanged() {
        fireIconChange();
        fireOpenedIconChange();
    }
}
