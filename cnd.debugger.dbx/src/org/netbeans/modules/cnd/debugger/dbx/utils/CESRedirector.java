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

/*
 * This file was transplanted from the toolshg/sside/core (SS core) module.
 * It no longer requires com.sun.tools.swdev.sunstudio.stat.SSStat but gets
 * class Stat directly from the sunstudio.base module.
 *
 * Our CESRedirector gets registered in the layers of dbxfacade.ide and
 * dbxfacade.tool.
 */ 

package org.netbeans.modules.cnd.debugger.dbx.utils;

import com.sun.tools.swdev.toolscommon.base.Stat;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.modules.ModuleInfo;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.CloneableEditorSupportRedirector;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;

/**
 * SS side of fix for IZ 51690.
 *
 * @author gordonp
 */
@ServiceProvider(service=CloneableEditorSupportRedirector.class)
public class CESRedirector extends CloneableEditorSupportRedirector implements PropertyChangeListener {
    
    /** CES's we want to override an initial CES */
    private Map<String, CloneableEditorSupport> map = new HashMap<String, CloneableEditorSupport>(50);
    
    /** A map of inode-keyed paths */
    private Map<Long, Set<String>> imap = new HashMap<Long, Set<String>>();
    
    /* implements CloneableEditorSupportRedirector */
    
    /**
     * Look for a replacement CES. The main reason we might want to replace the original CES
     * would be to ensure multiple paths to the same file result in a single editor pane
     * being opened.
     */
    protected synchronized CloneableEditorSupport redirect(Lookup lookup) {
        // disable on windows for now
        if (Utilities.isWindows()) {
            return null;
        }
        DataObject dobj = lookup.lookup(DataObject.class);
        String path = FileUtil.getFileDisplayName(dobj.getPrimaryFile());
        String orig = path;    // save a copy to see if its changed...
        CloneableEditorSupport ces = map.get(path);  // Use cached value...
        
        if (!isDbxGuiEnabled())
            return null;
        
//        if (ces != null) {
//            System.err.println("redirect:: Cache for " + path);
//        }
        if (ces == null) {
            File file = new File(path);
            try {
                path = file.getCanonicalPath();
            } catch (IOException ex) {
            }
            String bestPath = getBestPath(path);
            if (bestPath != null) {
                ces = getCloneableEditorSupport(bestPath);
                if (ces != null) {
                    map.put(orig, ces);
		    dobj.addPropertyChangeListener(this);
//                    System.err.println("redirect:: Redirect " + orig + "  to " + bestPath);
                } else {
//                    System.err.println("no redirect:: Redirect " + orig + "  to " + bestPath);
                }
            }
        }
        
        return ces;
    }
    
    private CloneableEditorSupport getCloneableEditorSupport(String path) {
        FileObject fo = FileUtil.toFileObject(new File(path));
        if (fo == null)
            return null;
        DataObject dobj = null;
        try {
            dobj = DataObject.find(fo);
        } catch(DataObjectNotFoundException ex) {
            return null;
        }
        CloneableEditorSupport ces = dobj.getLookup().lookup(CloneableEditorSupport.class);
        return ces;
    }

    public void propertyChange(PropertyChangeEvent evt) {
	if (evt.getPropertyName().equals("valid")) { // NOI18N
	    //System.out.println("File Deleted ............." + evt.getSource());
	    if (!(evt.getSource() instanceof DataObject)) {
		return;
	    }
	    DataObject toBeRemoved = (DataObject)evt.getSource();
	    toBeRemoved.removePropertyChangeListener(this);
	    map.remove(FileUtil.getFileDisplayName(toBeRemoved.getPrimaryFile()));
	}
    }
    
    /**
     * Compare path to previously opened files. If the stat structures (at least the
     * fields we're interested in) are the same, use the alt version because it may
     * already be open.
     */
    private String getBestPath(String path) {
        try {
            long now = new Date().getTime();
            Stat pathStat = new Stat(path);
            Set<String> set = imap.get(pathStat.inode());
            
            if (set == null) {
                set = new HashSet<String>();
                set.add(path);
                imap.put(pathStat.inode(), set);
            } else {
                Iterator<String> iter = set.iterator();
                while (iter.hasNext()) {
                    String alt = iter.next();
                    Stat altStat = new Stat(alt);
                    if (altStat.ctime() < now && pathStat.ctime() < now) {
                        // Safe to compare...
                        if (altStat.inode() == pathStat.inode() &&
                                altStat.ctime() == pathStat.ctime()) {
                            return alt;
                        }
                    } else {
                        // Not really safe to compare. The stat values could differ because the
                        // file has recently been modified.
                    }
                }
                set.add(path);
            }
        } catch (IOException ioe) {
            // Nothing
        }
        return null;
    }
    
    private static boolean dbxGuiEnabled = false;
    private static boolean dbxGuiEnabledChecked = false;
    protected static boolean isDbxGuiEnabled() {
        if (dbxGuiEnabledChecked)
            return dbxGuiEnabled;
        dbxGuiEnabled = false;
        Iterator<? extends ModuleInfo> iter = Lookup.getDefault().lookupAll(ModuleInfo.class).iterator();
        while (iter.hasNext()) {
            ModuleInfo info = iter.next();
            if (info.getCodeNameBase().equals("org.netbeans.modules.cnd.debugger.dbx") && info.isEnabled()) { // NOI18N
                dbxGuiEnabled = true;
                break;
            }
        }
        dbxGuiEnabledChecked = true;
        return dbxGuiEnabled;
    }
}
