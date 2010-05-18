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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
package org.netbeans.modules.versioning;

import javax.swing.event.ChangeListener;
import org.netbeans.modules.versioning.spi.VersioningSystem;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.ChangeEvent;
import org.netbeans.modules.versioning.spi.VCSVisibilityQuery;
import org.netbeans.spi.queries.VisibilityQueryImplementation2;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Delegates the work to the owner of files in query.
 * 
 * @author Tomas Stupka
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.queries.VisibilityQueryImplementation.class)
public class VcsVisibilityQueryImplementation implements VisibilityQueryImplementation2 {

    private List<ChangeListener> listeners = new ArrayList<ChangeListener>();
    private static VcsVisibilityQueryImplementation instance;

    public VcsVisibilityQueryImplementation() {
        instance = this;
    }

    public static VcsVisibilityQueryImplementation getInstance() {
        return instance;
    }

    public boolean isVisible(File file) {
        VersioningSystem system = VersioningManager.getInstance().getOwner(file);
        if(system == null) {
            return true;
        }
        VCSVisibilityQuery vqi = system.getVisibilityQuery();
        return vqi == null ? true : vqi.isVisible(file);
    }

    public boolean isVisible(FileObject fileObject) {
        File file = FileUtil.toFile(fileObject);
        if(file == null) {
            return true;
        }
        return isVisible(file);
    }

    public synchronized void addChangeListener(ChangeListener l) {
        ArrayList<ChangeListener> newList = new ArrayList<ChangeListener>(listeners);
        newList.add(l);
        listeners = newList;
    }

    public synchronized void removeChangeListener(ChangeListener l) {
        ArrayList<ChangeListener> newList = new ArrayList<ChangeListener>(listeners);
        newList.remove(l);
        listeners = newList;
    }

    public void fireVisibilityChanged() {
        ChangeListener[] ls;
        synchronized(this) {
            ls = listeners.toArray(new ChangeListener[listeners.size()]);
        }
        ChangeEvent event = new ChangeEvent(this);
        for (ChangeListener l : ls) {
            l.stateChanged(event);
        }
    }

}
