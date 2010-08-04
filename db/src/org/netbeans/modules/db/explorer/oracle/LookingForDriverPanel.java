/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.db.explorer.oracle;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.api.db.explorer.JDBCDriverManager;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.db.explorer.oracle.PredefinedWizard.Type;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

public class LookingForDriverPanel implements PredefinedWizard.Panel {

    private static final String DRIVERS_DIR = "drivers"; // NOI18N
    private final Type type;
    private static final String ORACLE_DRIVER_NAME = "oracle.driver.name"; // NOI18N
    private static final String MYSQL_DRIVER_NAME = "mysql.driver.name"; // NOI18N
    private static final String ORACLE_DOWNLOAD_FROM = "oracle.from"; // NOI18N
    private static final String MYSQL_DOWNLOAD_FROM = "mysql.from"; // NOI18N

    public LookingForDriverPanel(PredefinedWizard.Type type) {
        this.type = type;
    }

    private void init() {
        switch (type) {
            case ORACLE:
                driverName = NbBundle.getMessage(LookingForDriverPanel.class, ORACLE_DRIVER_NAME);
                downloadFrom = NbBundle.getMessage(LookingForDriverPanel.class, ORACLE_DOWNLOAD_FROM);
                break;
            case MYSQL:
                driverName = NbBundle.getMessage(LookingForDriverPanel.class, MYSQL_DRIVER_NAME);
                downloadFrom = NbBundle.getMessage(LookingForDriverPanel.class, MYSQL_DOWNLOAD_FROM);
                break;
            default:
                assert false;
        }
        FileObject fo = getLibraryFO(driverName);
        if (fo == null) {
            fo = getDriverFO(driverName);
        }
        driverFound = fo != null;
        if (driverFound) {
            driverPath = fo.getParent().getPath();
        } else {
            driverPath = getDefaultDriverPath();
        }
    }

    private static FileObject getLibraryFO(String name) {
        Library lib = LibraryManager.getDefault().getLibrary(name);
        if (lib == null) {
            Logger.getLogger(LookingForDriverPanel.class.getName()).log(Level.FINE, "Library not found for driver {0}.", new Object[]{name});
            return null;
        }
        Logger.getLogger(LookingForDriverPanel.class.getName()).log(Level.FINE, "Library found for driver {0}.", new Object[]{name});
        List<FileObject> libs = new ArrayList<FileObject>();
        for (URL url : lib.getContent("classpath")) { //NOI18N
            FileObject fo = URLMapper.findFileObject(url);
            Logger.getLogger(LookingForDriverPanel.class.getName()).log(Level.FINE, "Libray {0} for driver {1} has jar: {2}", new Object[]{lib.getName(), name, fo});
            FileObject jarFO = null;
            if ("jar".equals(url.getProtocol())) {  //NOI18N
                jarFO = FileUtil.getArchiveFile(fo);
            }
            if (jarFO == null) {
                throw new IllegalStateException("No file object on " + url);
            }
            libs.add(jarFO);
        }
        assert libs.size() == 1 : "Only one jar part of library " + lib;
        return libs.get(0);
    }

    private static FileObject getDriverFO(String name) {
        JDBCDriver[] drivers = JDBCDriverManager.getDefault().getDrivers();
        URL foundURL = null;
        for (JDBCDriver d : drivers) {
            Logger.getLogger(LookingForDriverPanel.class.getName()).log(Level.FINEST, "JDBC Driver: {0}.", new Object[]{d});
            for (URL url : d.getURLs()) {
                if (url.toExternalForm().endsWith(name)) {
                    foundURL = url;
                    break;
                }
            }
        }
        if (foundURL != null) {
            try {
                URI uri = foundURL.toURI();
                File f = new File(uri);
                if (f != null && f.exists()) {
                    return FileUtil.toFileObject(f);
                }
            } catch (URISyntaxException ex) {
                Logger.getLogger(LookingForDriverPanel.class.getName()).log(Level.INFO, ex.getLocalizedMessage(), ex);
            }
        }
        return null;
    }
    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private LookingForDriverUI component;
    private String driverName;
    private String driverPath;
    private String downloadFrom;
    private boolean driverFound;

    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public Component getComponent() {
        if (component == null) {
            init();
            component = new LookingForDriverUI(this, driverName, driverPath, downloadFrom, driverFound);
        }
        return component;
    }

    @Override
    public HelpCtx getHelp() {
        // Show no Help button for this panel:
        return HelpCtx.DEFAULT_HELP;
        // If you have context help:
        // return new HelpCtx(SampleWizardPanel1.class);
    }

    @Override
    public boolean isValid() {
        return component.driverFound();
    }
    private final Set<ChangeListener> listeners = new HashSet<ChangeListener>(1);

    @Override
    public final void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }

    @Override
    public final void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }

    protected final void fireChangeEvent() {
        Iterator<ChangeListener> it;
        synchronized (listeners) {
            it = new HashSet<ChangeListener>(listeners).iterator();
        }
        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            it.next().stateChanged(ev);
        }
    }

    @Override
    public void readSettings(WizardDescriptor settings) {
    }

    @Override
    public void storeSettings(WizardDescriptor settings) {
    }

    boolean found() {
        return driverFound;
    }

    private static File getUserDir() {
        // bugfix #50242: the property "netbeans.user" can return dir with non-normalized file e.g. duplicate //
        // and path and value of this property wrongly differs
        String user = System.getProperty("netbeans.user");
        File userDir = null;
        if (user != null) {
            userDir = new File(user);
            if (userDir.getPath().startsWith("\\\\")) {
                // Do not use URI.normalize for UNC paths because of http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4723726 (URI.normalize() ruins URI built from UNC File)
                try {
                    userDir = userDir.getCanonicalFile();
                } catch (IOException ex) {
                    // fallback when getCanonicalFile fails
                    userDir = userDir.getAbsoluteFile();
                }
            } else {
                userDir = new File(userDir.toURI().normalize()).getAbsoluteFile();
            }
        }

        return userDir;
    }

    private static String getDefaultDriverPath() {
        return getUserDir().getPath() + File.separator + DRIVERS_DIR;
    }
}
