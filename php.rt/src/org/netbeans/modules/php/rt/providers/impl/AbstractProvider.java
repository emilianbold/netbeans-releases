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
package org.netbeans.modules.php.rt.providers.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.php.rt.spi.providers.Host;
import org.netbeans.modules.php.rt.spi.providers.WebServerProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.util.Lookup;


/**
 * @author ads
 *
 */
public abstract class AbstractProvider<T extends Host> implements WebServerProvider {

    static final String DIR_INSTALLED_HOSTS = "/WebServers/Hosts"; // NOI18N
    protected static final String NAME = "name"; // NOI18N
    protected static final String HOST_NAME = "hostName"; // NOI18N
    protected static final String PORT = "port"; // NOI18N
    protected static final String HOST = "host"; // NOI18N
    protected static final String BASE_DIRECTORY_PATH = "baseDirectoryPath"; // NOI18N

    private static Logger LOGGER = Logger.getLogger(AbstractProvider.class.getName());

    public AbstractProvider() {
        init();
    }


    /* (non-Javadoc)
     * @see org.netbeans.modules.php.rt.spi.providers.WebServerProvider#getHosts()
     */
    public List<Host> getHosts() {
        /*
         * We want to prevent addition to inner storage of hosts ( myHosts )
         * without additional provider spceific actions.
         */

        return new ArrayList<Host>(doGetHosts());
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.rt.spi.providers.WebServerProvider#findHost(org.netbeans.modules.php.rt.spi.providers.UniqueID)
     */
    public Host findHost(String id) {
        if (id == null) {
            return null;
        }
        List<Host> list = getHosts();
        for (Host host : list) {
            if (id.equals(host.getId())) {
                return host;
            }
        }
        return null;
    }

    /**
     * <p>
     * Provider should recognize if provided FileObject stores data 
     * for host type sypported by this provider and create Host object.
     * <p>
     * Is used to load hosts in WebServerProvider.ServerFactory.initProviders()
     */
    protected abstract T configureHost(FileObject object);

    protected abstract void serializeAdded(T host);

    protected abstract void serializeUpdated(T oldHost, T newHost);

    /*
     * Basic implementation for recognizing host.
     * Providers could override this method to change bahavior.
     */
    protected boolean acceptHost(FileObject fileObject, Host host) {
        if (host instanceof HostImpl) {
            
            HostImpl impl = (HostImpl) host;
            
            String name = impl.getName();
            Object obj = fileObject.getAttribute(NAME);
            if (name == null || !name.equals(obj)) {
                return false;
            }
            return true;
            /*
            String domain = impl.getDomain();
            String port = impl.getPort();

            Object obj = fileObject.getAttribute(HOST_NAME);
            if (domain == null || !domain.equals(obj)) {
                return false;
            }
            obj = fileObject.getAttribute(PORT);
            if (obj == null && (port == null || port.equals(HostImpl.DEFAULT_PORT))) {
                return true;
            }
            return obj == null ? false : obj.equals(port);
             */
        } else {
            return false;
        }
    }

    protected void serializeRemoved(Host host) {
        LOGGER.info("------------------------");
        LOGGER.info(">>> will remove "+host);
        FileObject dir = getHostsDir();
        LOGGER.info(">>> will remove dir ="+dir);
        if (dir == null) {
            return;
        }

        FileObject hostObject = getHostObject(dir, host);
        LOGGER.info(">>> will remove hostObject"+hostObject);
        if (hostObject == null) {
            return;
        }
        try {
            hostObject.delete();
        LOGGER.info(">>>  hostObject REMOVED!!!");
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, null, e);
        }
    }

    protected FileObject getHostsDir() {
        Repository repository = (Repository) Lookup.getDefault().lookup(Repository.class);
        return repository.getDefaultFileSystem().findResource(DIR_INSTALLED_HOSTS);
    }


    protected LinkedList<T> doGetHosts() {
        return myHosts;
    }

    protected void addHost(T host) {
        doGetHosts().add(host);

        serializeAdded(host);
    }

    protected void updateHost(T oldHost, T newHost) {
        doGetHosts().remove(oldHost);
        doGetHosts().add(newHost);
        serializeUpdated(oldHost, newHost);
    }

    protected FileObject getHostObject(FileObject dir, Host host) {
        FileObject hostObject = null;
        FileObject[] hostObjects = dir.getChildren();
        for (FileObject object : hostObjects) {
            if (acceptHost(object, host)) {
                hostObject = object;
            }
        }
        return hostObject;
    }

    void remove(Host host) {
        doGetHosts().remove(host);

        serializeRemoved(host);
    }

    private void init() {
        myHosts = new LinkedList<T>();

        FileObject dir = getHostsDir();
        if (dir == null) {
            return;
        }
        // dir.addFileChangeListener(new InstanceInstallListener());
        FileObject[] children = dir.getChildren();

        for (FileObject fileObject : children) {
            T host = configureHost(fileObject);
            if (host != null) {
                doGetHosts().add(host);
            }
        }
    }

    private LinkedList<T> myHosts;
}
