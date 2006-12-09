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

package org.netbeans.modules.j2ee.persistence.spi.support;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.logging.Logger;
import org.netbeans.modules.j2ee.persistence.api.PersistenceScope;
import org.netbeans.modules.j2ee.persistence.api.PersistenceScopes;
import org.netbeans.modules.j2ee.persistence.spi.PersistenceScopesFactory;
import org.netbeans.modules.j2ee.persistence.spi.PersistenceScopesImplementation;
import org.netbeans.modules.j2ee.persistenceapi.FileChangeSupport;
import org.netbeans.modules.j2ee.persistenceapi.FileChangeSupportEvent;
import org.netbeans.modules.j2ee.persistenceapi.FileChangeSupportListener;
import org.openide.filesystems.FileUtil;

/**
 * Helper class for implementing
 * {@link org.netbeans.modules.j2ee.persistence.spi.PersistenceScopesProvider}.
 * It creates and maintains a {@link org.netbeans.modules.j2ee.persistence.api.PersistenceScopes}
 * instance containing a single {@link org.netbeans.modules.j2ee.persistence.api.PersistenceScope}
 * or an empty array of <code>PersistenceScope</code> depending on whether the persistence.xml
 * file corresponding to that <code>PersistenceScope</code> exists or not, firing property changes
 * as the persistence.xml file is created/deleted.
 *
 * @author Andrei Badea
 */
public final class PersistenceScopesHelper {

    private static final Logger LOG = Logger.getLogger(PersistenceScopesHelper.class.getName());

    private final PersistenceScopes persistenceScopes = PersistenceScopesFactory.createPersistenceScopes(new PersistenceScopesImpl());
    private final PropertyChangeSupport propChangeSupport = new PropertyChangeSupport(this);
    private final FileListener fileListener = new FileListener();

    private PersistenceScope persistenceScope;
    private File persistenceXml;
    private boolean persistenceExists;

    public PersistenceScopesHelper() {
    }

    /**
     * Call this method in order to change the persistence scope returned by the
     * <code>PersistenceScopes</code> instance returned by {@link #getPersistenceScopes}
     * or the corresponding persistence.xml file.
     *
     * @param  newPersistenceScope the new persistence scope; can be null, but in this case
     *         the <code>newPersistenceXml</code> parameter must be null too.
     * @param  newPersistenceXml the new persistence.xml file; can be null.
     *
     * @throws IllegalArgumentException if <code>newPersistenceScope</code> is null
     *         and <code>newPersistenceXml</code> is not.
     */
    public void changePersistenceScope(PersistenceScope newPersistenceScope, File newPersistenceXml) {
        if (newPersistenceScope == null && newPersistenceXml != null) {
            throw new IllegalArgumentException("The persistenceScope parameter cannot be null when newPersistenceXml is non-null"); // NOI18N
        }

        boolean oldPersistenceExists, newPersistenceExists;
        PersistenceScope oldPersistenceScope;

        synchronized (this) {
            oldPersistenceExists = persistenceExists;
            oldPersistenceScope = persistenceScope;

            if (persistenceXml != null) {
                FileChangeSupport.DEFAULT.removeListener(fileListener, persistenceXml);
            }
            if (newPersistenceXml != null) {
                persistenceXml = newPersistenceXml;
                FileChangeSupport.DEFAULT.addListener(fileListener, persistenceXml);
            } else {
                persistenceXml = null;
            }

            persistenceScope = newPersistenceScope;
            persistenceXml = newPersistenceXml;

            change();

            newPersistenceExists = persistenceExists;
        }

        if (oldPersistenceExists != newPersistenceExists || (oldPersistenceScope != newPersistenceScope && newPersistenceExists)) {
            LOG.fine("changePersistenceScope: firing PROP_PERSISTENCE_SCOPES change"); // NOI18N
            propChangeSupport.firePropertyChange(PersistenceScopes.PROP_PERSISTENCE_SCOPES, null, null);
        }
    }

    /**
     * Returns the <code>PersistenceScopes</code> created by this helper. Usually
     * an implementor of <code>PersistenceScopesProvider</code> will delegate
     * its <code>getPersistenceScopes</code> method to this method.
     *
     * @return a <code>PersistenceScopes</code> instance; never null.
     */
    public PersistenceScopes getPersistenceScopes() {
        return persistenceScopes;
    }

    /**
     * Called when anything has changed (the persistence scope has changed, the path of the
     * persistence.xml file has changes, the persistence.xml file has been created or deleted).
     */
    private void change() {
        synchronized (this) {
            persistenceExists = false;
            if (persistenceXml != null) {
                persistenceExists = FileUtil.toFileObject(persistenceXml) != null;
            }
        }
    }

    /**
     * Called when something happened to persistence.xml (created, deleted).
     */
    private void fileEvent() {
        boolean oldPersistenceExists, newPersistenceExists;

        synchronized (this) {
            oldPersistenceExists = persistenceExists;
            change();
            newPersistenceExists = persistenceExists;
        }

        LOG.fine("fileEvent: oldPersistenceExists=" + oldPersistenceExists + ", newPersistenceExists=" + newPersistenceExists); // NOI18N

        if (oldPersistenceExists != newPersistenceExists) {
            LOG.fine("fileEvent: firing PROP_PERSISTENCE_SCOPES change"); // NOI18N
            propChangeSupport.firePropertyChange(PersistenceScopes.PROP_PERSISTENCE_SCOPES, null, null);
        }
    }

    private PersistenceScope[] getPersistenceScopeList() {
        synchronized (this) {
            if (persistenceExists) {
                return new PersistenceScope[] { persistenceScope };
            } else {
                return new PersistenceScope[0];
            }
        }
    }

    private void addPropertyChangeListener(PropertyChangeListener listener) {
        propChangeSupport.addPropertyChangeListener(listener);
    }

    private void removePropertyChangeListener(PropertyChangeListener listener) {
        propChangeSupport.removePropertyChangeListener(listener);
    }

    /**
     * Listener on the persistence.xml file.
     */
    private class FileListener implements FileChangeSupportListener {

        public void fileCreated(FileChangeSupportEvent event) {
            LOG.fine("fileCreated: " + event.getPath());
            fileEvent();
        }

        public void fileDeleted(FileChangeSupportEvent event) {
            LOG.fine("fileDeleted: " + event.getPath());
            fileEvent();
        }

        public void fileModified(FileChangeSupportEvent event) {
            LOG.fine("fileModified: " + event.getPath());
        }
    }

    /**
     * Implementation of <code>PersistenceScopesImplementation</code>.
     * The <code>PersistenceScopes</code> instance maintained by the helper
     * delegates to this.
     */
    private class PersistenceScopesImpl implements PersistenceScopesImplementation {

        public void addPropertyChangeListener(PropertyChangeListener listener) {
            PersistenceScopesHelper.this.addPropertyChangeListener(listener);
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
            PersistenceScopesHelper.this.removePropertyChangeListener(listener);
        }

        public PersistenceScope[] getPersistenceScopes() {
            return PersistenceScopesHelper.this.getPersistenceScopeList();
        }
    }
}
