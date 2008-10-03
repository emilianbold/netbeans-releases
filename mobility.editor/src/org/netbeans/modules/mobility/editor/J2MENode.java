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
import org.openide.util.ImageUtilities;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;

import java.awt.*;

/**
 * @author David Kaspar
 */
public class J2MENode extends FilterNode implements MIDletsCacheListener {
    
    public static final Image entryPointImage = ImageUtilities.loadImage("org/netbeans/modules/mobility/editor/resources/entrypoint.gif", true); //NOI18N
    
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
                original = ImageUtilities.mergeImages(original, entryPointImage, 0, 0);
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
