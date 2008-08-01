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

package org.netbeans.modules.groovy.support.api;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.prefs.Preferences;
import org.netbeans.modules.groovy.support.options.SupportOptionsPanelController;
import org.netbeans.spi.options.AdvancedOption;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbCollections;
import org.openide.util.NbPreferences;
import org.openide.util.Utilities;

/**
 * Groovy settings
 *
 * @author Martin Adamek
 */
public final class GroovySettings extends AdvancedOption {

    private static final String GROOVY_HOME = "groovyHome"; // NOI18N
    private static final String GROOVY_DOC  = "groovyDoc"; // NOI18N
    private static final String GROOVY_HOME_PATH = "GROOVY_HOME"; //NOI18N    
    private static final String GROOVY_EXECUTABLE = "groovy"; //NOI18N

    public String getGroovyHome() {
        String groovyHome = prefs().get(GROOVY_HOME, ""); // NOI18N
        if (!(groovyHome != null && groovyHome.length() > 0)) {
            groovyHome = findGroovyPlatform();
        }
        return groovyHome;
    }
    
    public void setGroovyHome(String groovyHome) {
        prefs().put(GROOVY_HOME, groovyHome);
    }

    public String getGroovyDoc() { 
        return prefs().get(GROOVY_DOC, ""); // NOI18N
    }
    
    public void setGroovyDoc(String groovyDoc) {
        prefs().put(GROOVY_DOC, groovyDoc);
    }

    public String getDisplayName() {
        return NbBundle.getMessage(GroovySettings.class, "AdvancedOption_DisplayName_Support");
    }

    public String getTooltip() {
        return NbBundle.getMessage(GroovySettings.class, "AdvancedOption_Tooltip_Support");
    }

    public OptionsPanelController create() {
        return new SupportOptionsPanelController();
    }

    private Preferences prefs() {
        return NbPreferences.forModule(GroovySettings.class);
    }
    
    private String findGroovyPlatform() {
        String groovyPath = System.getenv(GROOVY_HOME_PATH);        
        if (groovyPath == null) {            
            for (String dir : dirsOnPath()) {                
                File f = null;
                if (Utilities.isWindows()) {
                    f = new File(dir, GROOVY_EXECUTABLE + ".exe");
                } else {
                    f = new File(dir, GROOVY_EXECUTABLE);
                }                
                if (f.isFile()) {
                    try {
                        groovyPath = f.getCanonicalFile().getParentFile().getParent();                        
                        break;
                    } catch (Exception e) {
                        Exceptions.printStackTrace(e);
                    }                    
                }
            }
        }
        return groovyPath;
    }        
    
    /**     
     * Returns an {@link Iterable} which will uniquely traverse all valid
     * elements on the <em>PATH</em> environment variables. That means,
     * duplicates and elements which are not valid, existing directories are
     * skipped.
     * 
     * @return an {@link Iterable} which will traverse all valid elements on the
     * <em>PATH</em> environment variables.
     */
    
    /*FIXME: This method has been copied from the ruby.platform module. 
     *  ruby.platform/src/org/netbeans/modules/ruby/platform/Util.java
     * 
     * I don't know if it could be included into a shared module.
    */
    public static Iterable<String> dirsOnPath() {
        String rawPath = System.getenv("PATH"); // NOI18N
        if (rawPath == null) {
            rawPath = System.getenv("Path"); // NOI18N
        }
        if (rawPath == null) {
            return Collections.emptyList();
        }
        Set<String> candidates = new LinkedHashSet<String>(Arrays.asList(rawPath.split(File.pathSeparator)));
        for (Iterator<String> it = candidates.iterator(); it.hasNext();) {
            String dir = it.next();
            if (!new File(dir).isDirectory()) { // remove non-existing directories (#124562)                
                it.remove();
            }
        }
        return NbCollections.iterable(candidates.iterator());
    }

}