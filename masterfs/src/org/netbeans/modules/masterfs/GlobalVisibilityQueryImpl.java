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

package org.netbeans.modules.masterfs;

import java.io.File;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import org.openide.filesystems.FileObject;
import javax.swing.event.ChangeListener;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;
import org.netbeans.spi.queries.VisibilityQueryImplementation2;
import org.openide.util.ChangeSupport;
import org.openide.util.NbPreferences;

// XXX - one would expect this class in core but there is problem that 
//core can't depened on project/queries at the moment 

/**
 * 
 * Implemenent VisibilityQueryImplementation based on regular expression provided
 * by users via property PROP_IGNORED_FILES in Tools/Options/Miscellaneous/Files.
 * 
 * This class has hidden dependency on IgnoredFilesPreferences module org.netbeans.core.ui.
 */ 
public class GlobalVisibilityQueryImpl implements VisibilityQueryImplementation2 {
    static GlobalVisibilityQueryImpl INSTANCE;
    private final ChangeSupport cs = new ChangeSupport(this);
    
    /**
     * Keep it synchronized with IgnoredFilesPreferences.PROP_IGNORED_FILES
     */ 
    private static final String PROP_IGNORED_FILES = "IgnoredFiles"; // NOI18N
    private Pattern ignoreFilesPattern = null;

    /** Default instance for lookup. */
    public GlobalVisibilityQueryImpl() {
        INSTANCE = this;
    }

    private static Preferences getPreferences() {
        return NbPreferences.root().node("/org/netbeans/core");
    }
    
    public boolean isVisible(FileObject file) {
        return isVisible(file.getNameExt());
    }
    
    public boolean isVisible(File file) {
        return isVisible(file.getName());
    }
    

    boolean isVisible(final String fileName) {
        Pattern pattern = getIgnoreFilesPattern();
        return (pattern != null) ? !(pattern.matcher(fileName).find()) : true;
    }

    /**
     * Add a listener to changes.
     * @param l a listener to add
     */
    public void addChangeListener(ChangeListener l) {
        cs.addChangeListener(l);
    }

    /**
     * Stop listening to changes.
     * @param l a listener to remove
     */
    public void removeChangeListener(ChangeListener l) {
        cs.removeChangeListener(l);
    }

    private Pattern getIgnoreFilesPattern() {
        if (ignoreFilesPattern == null) {
            String ignoredFiles = getIgnoredFiles();
            ignoreFilesPattern = (ignoredFiles != null && ignoredFiles.length() > 0) ? Pattern.compile(ignoredFiles) : null;
        }
        return ignoreFilesPattern;
    }

    protected String getIgnoredFiles() {
        // \.(cvsignore|svn|DS_Store) is covered by ^\..*$
        String retval = getPreferences().get(PROP_IGNORED_FILES, "^(CVS|SCCS|vssver.?\\.scc|#.*#|%.*%|_svn)$|~$|^\\.(?!htaccess$).*$");//NOI18N;
        getPreferences().addPreferenceChangeListener(new PreferenceChangeListener() {
            public void preferenceChange(PreferenceChangeEvent evt) {
                if (PROP_IGNORED_FILES.equals(evt.getKey())) {
                    ignoreFilesPattern = null;
                    cs.fireChange();
                }
                
            }
        });                
        return retval;
    }

}
