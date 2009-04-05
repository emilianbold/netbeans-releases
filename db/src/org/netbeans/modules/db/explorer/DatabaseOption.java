/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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

package org.netbeans.modules.db.explorer;

import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.net.URL;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;
import java.util.jar.JarFile;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.api.db.explorer.JDBCDriverManager;
import org.netbeans.modules.db.util.DriverListUtil;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/** Root system option. It stores a list of available drivers and open connections.
* These connections will be restored at startup, drivers will be placed in Drivers
* directory owned by Database node.
*/
public class DatabaseOption {
     /** The support for firing property changes */
    private PropertyChangeSupport propertySupport;

    private static boolean debugMode;
    private static Vector drivers;
    private static Vector connections;
    public static final String PROP_DEBUG_MODE = "debugMode"; //NOI18N
    private static DatabaseOption INSTANCE = new DatabaseOption();

    static final long serialVersionUID =-13629330831657810L;
    
    private DatabaseOption() {
        super();
        drivers = new Vector();
        connections = new Vector();
        debugMode = false;
        propertySupport = new PropertyChangeSupport(this);
        
        deleteAdaptorsFolder();
    }

    public static DatabaseOption getDefault() {
        return INSTANCE;
     }

    public PropertyChangeSupport getPropertySupport() {
        return propertySupport;
    }

    public boolean getDebugMode() {
        return debugMode;
    }

    public void setDebugMode(boolean flag) {
        if (debugMode == flag)
            return;
        
        debugMode = flag;
        propertySupport.firePropertyChange(PROP_DEBUG_MODE, !debugMode ? Boolean.TRUE : Boolean.FALSE, debugMode ? Boolean.TRUE : Boolean.FALSE);
    }

    /** Sets vector of available drivers.
    * @param c Vector with drivers
    */
    public void setAvailableDrivers(Vector c) {
        drivers = c;
    }

    /**
     * Returns vector of saved connections. Do not use this method to work with
     * the list of connections, use {@link org.netbeans.modules.db.explorer.ConnectionList}
     * instead.
     */
    Vector getConnections() {
        if (connections == null)
            connections = new Vector();

        return connections;
    }

    public void save() {
        propertySupport.firePropertyChange(null, null, null);
    }

    /** Name of the option */
    public String displayName() {
        return NbBundle.getMessage (DatabaseOption.class, "OptionName"); //NOI18N
    }

    /** Description of object */
    @Override
    public String toString() {
        return (drivers != null ? drivers.size() : 0) + " drivers, " + (connections != null ? connections.size() : 0) + " connections"; //NOI18N
    }

     /** Writes data
    * @param out ObjectOutputStream
    */
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(null);
        out.writeObject(getConnections());
    }

    /** Reads data
    * @param in ObjectInputStream
    */
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        drivers = (Vector) in.readObject();
        if (drivers != null)
            lookForDrivers();

        connections = (Vector) in.readObject();
    }

    private void lookForDrivers() {
        StringBuffer sb = new StringBuffer();
        sb.append(File.separator);
        sb.append("lib"); // NOI18N
        sb.append(File.separator);
        sb.append("ext"); // NOI18N
        String libext = sb.toString();        
        String nbhome = System.getProperty("netbeans.home"); // NOI18N
        
        preinstallDrivers(nbhome + libext);
    }
    
    private void preinstallDrivers(String dirPath) {
        File dir = new File(dirPath);
        if (!dir.isDirectory())
            return;
        
        File[] files = dir.listFiles(new FileFilter() {
            public boolean accept(File f) {
                return (f.isDirectory() || f.getName().endsWith(".jar") || f.getName().endsWith(".zip")); //NOI18N
            }
        });
        
        for (int i = 0; i < files.length; i++) {
            JarFile jf;
            String drv;

            try {
                jf = new JarFile(files[i]);
                Set drvs = DriverListUtil.getDrivers();
                Iterator it = drvs.iterator();

                while (it.hasNext()) {
                    drv = (String) it.next();
                    if (jf.getEntry(drv.replace('.', '/') + ".class") != null) {//NOI18N
                        String driverName = DriverListUtil.findFreeName(DriverListUtil.getName(drv));
                        JDBCDriver driver = JDBCDriver.create(driverName, driverName, drv, new URL[] {files[i].toURI().toURL()});
                        try {
                            JDBCDriverManager.getDefault().addDriver(driver);
                        } catch (DatabaseException e) {
                            Exceptions.printStackTrace(e);
                        }
                    }
                }
                jf.close();
            } catch (IOException exc) {
                //PENDING
            }
        }
    }
    
    private void deleteAdaptorsFolder() {    
        FileObject fo = FileUtil.getConfigFile("Database"); //NOI18N
        try {
            if (fo != null)
                fo.delete();
        } catch (IOException exc) {
            //delete action failed - ignore
        }
    }
}
