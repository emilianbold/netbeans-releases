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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import javax.swing.event.ChangeEvent;
import org.netbeans.modules.versioning.spi.VCSVisibilityQuery;
import org.netbeans.spi.queries.VisibilityQueryImplementation2;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

/**
 * Delegates the work to the owner of files in query.
 * 
 * @author Tomas Stupka
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.queries.VisibilityQueryImplementation.class)
public class VcsVisibilityQueryImplementation implements VisibilityQueryImplementation2 {

    private final InvisibleFiles cache = new InvisibleFiles(25);
    private List<ChangeListener> listeners = new ArrayList<ChangeListener>();
    private static VcsVisibilityQueryImplementation instance;
    private static RequestProcessor rp = new RequestProcessor(VcsVisibilityQueryImplementation.class.getName(), 1, false, false);
    private RequestProcessor.Task refreshTask = rp.create(new RefreshTask());
    private RequestProcessor.Task vsChangedTask = rp.create(new VisibilityChangedTask());
    private final HashMap<File, Boolean> refreshedFiles = new HashMap<File, Boolean>(20);
    private static final int MAX_CACHE_SIZE = 500;
    private Map<File, FileObject> fileObjects = Collections.synchronizedMap(new HashMap<File, FileObject>(8));

    public VcsVisibilityQueryImplementation() {
        instance = this;
    }

    public static VcsVisibilityQueryImplementation getInstance() {
        return instance;
    }

    @Override
    public boolean isVisible(File file) {

        if(isHiddenMetadata(file)) {
            return false;
        }
        
        boolean visible;
        synchronized (cache) {
            cache.clearOldValues();
            visible = !cache.keySet().contains(file); // get cached value
        }
        boolean refresh;
        synchronized (refreshedFiles) {
            refresh = refreshedFiles.isEmpty();
            refreshedFiles.put(file, visible);
        }
        if (refresh) {
            refreshTask.schedule(100);
        }
        return visible;
    }

    @Override
    public boolean isVisible(FileObject fileObject) {
        File file = FileUtil.toFile(fileObject);
        if(file == null) {
            return true;
        }
        fileObjects.put(file, fileObject);
        return isVisible(file);
    }

    @Override
    public synchronized void addChangeListener(ChangeListener l) {
        ArrayList<ChangeListener> newList = new ArrayList<ChangeListener>(listeners);
        newList.add(l);
        listeners = newList;
    }

    @Override
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

    private static final String SVN_ADMIN_DIR;
    private static final Pattern svnmetadataPattern;
    static {
        if (Utilities.isWindows()) {
            String env = System.getenv("SVN_ASP_DOT_NET_HACK");
            if (env != null) {
                SVN_ADMIN_DIR = "_svn";
            } else {
                SVN_ADMIN_DIR = ".svn";
            }
        } else {
            SVN_ADMIN_DIR = ".svn";
        }
        svnmetadataPattern = Pattern.compile(".*\\" + File.separatorChar + SVN_ADMIN_DIR + "(\\" + File.separatorChar + ".*|$)");
    }
    private static final Pattern hgmetadataPattern = Pattern.compile(".*\\" + File.separatorChar + "(\\.)hg(\\" + File.separatorChar + ".*|$)"); // NOI18N
    private static final Pattern cvsmetadataPattern = Pattern.compile(".*\\" + File.separatorChar + "CVS(\\" + File.separatorChar + ".*|$)");    
    private static final Pattern gitmetadatapattern = Pattern.compile(".*\\" + File.separatorChar + "(\\.)git(\\" + File.separatorChar + ".*|$)"); // NOI18N
    
    // temporary hack to fix issue #195985
    // should be replaced by a change in VCSVisibilityQuery
    private boolean isHiddenMetadata(File file) {
        return svnmetadataPattern.matcher(file.getAbsolutePath()).matches() ||
               hgmetadataPattern.matcher(file.getAbsolutePath()).matches()  ||
               cvsmetadataPattern.matcher(file.getAbsolutePath()).matches() ||
               gitmetadatapattern.matcher(file.getAbsolutePath()).matches();
    }

    private class VisibilityChangedTask implements Runnable {
        @Override
        public void run() {
            fireVisibilityChanged();
        }
    }
    
    private class RefreshTask implements Runnable {
        @Override
        public void run() {
            File file = null;
            boolean originalValue = false;
            // get another file
            synchronized (refreshedFiles) {
                Iterator<Entry<File, Boolean>> it = refreshedFiles.entrySet().iterator();
                if (it.hasNext()) {
                    Entry<File, Boolean> e = it.next();
                    file = e.getKey();
                    originalValue = e.getValue();
                    it.remove();
                }
            }
            if (file == null) {
                return; // no files to refresh, finish
            }
            boolean visible = true;
            FileObject fo = fileObjects.remove(file);
            VersioningSystem system = VersioningManager.getInstance().getOwner(file, fo != null ? !fo.isFolder() : null);
            if (system != null) {
                VCSVisibilityQuery vqi = system.getVisibilityQuery();
                visible = vqi == null ? true : vqi.isVisible(file);
            }
            synchronized (cache) {
                cache.remove(file);
                if (!visible) {
                    cache.put(file, System.currentTimeMillis());
                }
            }
            if (originalValue != visible) {
                vsChangedTask.schedule(1000);
            }
            refreshTask.schedule(0);
        }
    }

    private static class InvisibleFiles extends LinkedHashMap<File, Long> {

        public InvisibleFiles (int initialCapacity) {
            super(initialCapacity);
        }

        @Override
        protected boolean removeEldestEntry(Entry<File, Long> eldest) {
            return size() >= MAX_CACHE_SIZE;
        }

        public void clearOldValues () {
            if (size() > MAX_CACHE_SIZE >> 2) { // remove old entries only for the size being 1/4 it's max capacity
                Iterator<Entry<File, Long>> it = entrySet().iterator();
                long threshold = System.currentTimeMillis() - 30 * 60 * 1000; // default max age is 30 minutes
                while (it.hasNext() && it.next().getValue() < threshold) {
                    it.remove();
                }
            }
        }
    }
}
