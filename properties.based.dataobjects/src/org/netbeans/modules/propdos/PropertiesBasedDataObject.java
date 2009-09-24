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
package org.netbeans.modules.propdos;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import org.netbeans.api.project.ProjectManager;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbCollections;
import org.openide.util.WeakListeners;

/**
 * A dataobject base class which can read a Properties file (its own primary
 * file, whatever its extension - it should be in properties file format) to
 * produce some object of type T which should be found in its lookup.
 * <p>
 * A PropertiesBasedDataObject contains a PropertiesAdapter in its Lookup.
 * The Properties object obtained from it is live - if written to, it will
 * cause the file to be saved after a delay (currently 500ms but subject to
 * change).  The base class takes care of removing the stale object from its
 * lookup, replacing it with an up-to-date one.
 * <p>
 * The <code>createFrom(Properties)</code> method must be overridden to
 * create an instance of the object of type T expected in this object's
 * lookup.
 * <p>
 * The object in question can be gotten directly from the lookup (triggering
 * a file read on first-call), or via an InstanceCookie.
 *
 * @author Tim Boudreau
 */
public abstract class PropertiesBasedDataObject<T> extends MultiDataObject {

    protected static final Logger LOGGER = Logger.getLogger(PropertiesBasedDataObject.class.
            getPackage().getName());

    private final InstanceContent internalContent = new InstanceContent();
    /**
     * This instanceContent can be used by subclasses to add/remove objects from this
     * DataObject's lookup.
     */
    protected final InstanceContent content = new InstanceContent();
    private final Issue164431Workaround lkp;
    private final Class<T> type;
    private final PropsAdapter propsAdapter;
    private final L l = new L();

    protected PropertiesBasedDataObject(FileObject pf, MultiFileLoader loader, Class<T> type) throws DataObjectExistsException, IOException {
        super(pf, loader);
        this.type = type;
        lkp = new Issue164431Workaround(
                new C(), new AbstractLookup(internalContent),
                getCookieSet().getLookup(),
                new AbstractLookup(content));
        internalContent.add(propsAdapter = new PropsAdapter());
        internalContent.add(this);
        getCookieSet().add(new IC());
        pf.addFileChangeListener(FileUtil.weakFileChangeListener(l, pf));
    }

    private void replaceCreatedObject() {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.log(Level.FINEST, "replaceCreatedObject() on " + //NOI18N
                    getPrimaryFile().getPath() + " for " + type().getName() +
                    " discarding old InstanceContent.Converter and its" +
                    " instance, and invoking onReplaceObject()"); //NOI18N
        }
        lkp.replaceConverter(new C());
        propsAdapter.clear(); //XXX shouldn't be necessary
        onReplaceObject();
    }

    @Override
    protected final void handleDelete() throws IOException {
        final FileObject parent = getPrimaryFile().getParent();
        super.handleDelete();
        try {
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {

                public Void run() throws Exception {
                    onDelete(parent);
                    return null;
                }
            });
        } catch (Exception e) {
            if (e instanceof IOException) {
                throw (IOException) e;
            } else {
                IOException ioe = new IOException();
                ioe.initCause(e);
                throw ioe;
            }
        }
    }

    /**
     * Called after super.handleDelete(), from within ProjectManager.mutex().writeAccess
     * @throws java.lang.Exception
     */
    protected abstract void onDelete(FileObject parentFolder) throws Exception;

    protected void propertyChanged (String propertyName, String newValue) {
        //do nothing - subclasses that expose the properties object as
        //Node properties can use this to fire changes
    }

    /**
     * Get the type this DataObject should posess in its lookup
     * @return
     */
    protected final Class<T> type() {
        return type;
    }

    /**
     * Create the object that should be in this object's lookup which can
     * be constructed from the file's content.  This method will be called
     * automatically if an instance of T is requested from the lookup.
     * <p>
     * Do not make changes to the passed properties object, it will have
     * no effect.
     * @param properties
     * @return
     */
    protected abstract T createFrom(ObservableProperties properties);

    @Override
    public final Lookup getLookup() {
        return lkp;
    }

    /**
     * By default, returns false
     * @return false
     */
    @Override
    public boolean isCopyAllowed() {
        return false;
    }

    /**
     * By default, returns false
     * @return false
     */
    @Override
    public boolean isMoveAllowed() {
        return false;
    }

    /**
     * By default, returns false
     * @return false
     */
    @Override
    public boolean isRenameAllowed() {
        return false;
    }

    /**
     * Get the properties from the underlying properties object, as a
     * PropertySet which can be displayed in a property sheet.  Returns
     * a result produced by Sheet.createExpertSet().  If your node already
     * has a property set called Expert, rename the result to something different
     * or this one will hide it (or vice-versa).
     * @return
     */
    public final Sheet.Set getPropertiesAsPropertySet() {
        Sheet.Set result = Sheet.createExpertSet();
        Properties p = propsAdapter.asProperties();
        List <String> props = new ArrayList<String>(NbCollections.checkedSetByFilter(p.keySet(), String.class, false));
        Collections.sort(props);
        for (String s : props) {
            result.put(new SP(s));
        }
        return result;
    }

    /**
     * Called after the object of type T in the lookup has been replaced,
     * usually due to a modification of the ObservableProperties, or external
     * modification of the underlying file.<p/>
     * Useful if you need to, say, notify the node to update its display name.
     * The new object of type T has not necessarily bee
     */
    protected void onReplaceObject() {

    }

    /**
     * Discard any cached instance of type T in the lookup, causing it
     * to be recreated the next time something queries for it.
     */
    protected final void refreshObject() {
        replaceCreatedObject();
    }

    private final class L extends FileChangeAdapter implements PropertyChangeListener {

        public void propertyChange(PropertyChangeEvent evt) {
            refreshObject();
            PropertiesBasedDataObject.this.propertyChanged (
                    evt.getPropertyName(), (String) evt.getNewValue());
        }

        @Override
        public void fileChanged(FileEvent fe) {
            if (!fe.isExpected()) {
                //XXX diff & fire property change events from ObservableProperties?
//                Properties old = propsAdapter.copy();
                propsAdapter.clear();
                //XXX may trigger 2 calls to replace lookup contents?
                refreshObject();
                propertyChanged (null, null);
            }
        }
    }

    private final class SP extends PropertySupport.ReadOnly<String> {
        SP(String name) {
            super (name, String.class, name, null);
        }

        @Override
        public String getValue() throws IllegalAccessException, InvocationTargetException {
            return propsAdapter.asProperties().getProperty(getName());
        }

        S s;
        @Override
        public PropertyEditor getPropertyEditor() {
            return (s == null) ? (s = new S()) : s;
        }

        private final class S extends PropertyEditorSupport implements PropertyChangeListener {
            S() {
                super (SP.this);
                ObservableProperties p = propsAdapter.asProperties();
                p.addPropertyChangeListener(WeakListeners.propertyChange(this, p));
            }

            @Override
            public Object getValue() {
                return super.getValue();
            }

            public void propertyChange(PropertyChangeEvent evt) {
                if (getName().equals(evt.getPropertyName())) {
                    super.firePropertyChange();
                }
            }
        }
    }

    private final class PropsAdapter implements PropertiesAdapter {

        private SelfSavingProperties props;

        synchronized void clear() {
            props = null;
        }

        synchronized Properties copy() {
            return props == null ? new Properties() : new Properties (props);
        }

        public synchronized ObservableProperties asProperties() {
            if (LOGGER.isLoggable(Level.FINEST)) {
                LOGGER.log(Level.FINEST, "Fetch " + getPrimaryFile().getPath() //NOI18N
                        + " as observable properties"); //NOI18N
            }
            if (!isValid()) {
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.log(Level.FINE, getPrimaryFile().getPath() +
                            " is invalid, returning null"); //NOI18N
                }
                return null;
            }
            if (props == null) {
                props = new JCSelfSavingProperties();
                props.addPropertyChangeListener (WeakListeners.propertyChange(l, props));
                if (LOGGER.isLoggable(Level.FINEST)) {
                    LOGGER.log(Level.FINEST, "First load of " + //NOI18N
                            getPrimaryFile().getPath() +
                            " as observable properties"); //NOI18N
                }
                try {
                    InputStream in = getPrimaryFile().getInputStream();
                    try {
                        props.load(in);
                    } finally {
                        in.close();
                    }
                } catch (IOException ex) {
                    if (LOGGER.isLoggable(Level.FINEST)) {
                        LOGGER.log(Level.FINEST, "Load of " + //NOI18N
                                getPrimaryFile().getPath() + " failed"); //NOI18N
                    }
                    Exceptions.printStackTrace(ex);
                }
            }
            return props;
        }
    }

    private final class JCSelfSavingProperties extends SelfSavingProperties {

        JCSelfSavingProperties() {
            super(PropertiesBasedDataObject.this);
        }

        @Override
        protected void onWriteCompleted() throws IOException {
            //The file has been rewritten.  Destroy the object in our lookup
            //and let it be replaced as needed
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "Write of " + getPrimaryFile().getPath() //NOI18N
                        + " observable properties completed.  Invoking " + //NOI18N
                        "replaceCreatedObject() for " + getClass().getName()); //NOI18N
            } else if (LOGGER.isLoggable(Level.FINEST)) {
                LOGGER.log(Level.FINE, "Write of " + getPrimaryFile().getPath() //NOI18N
                        + " observable properties completed.  Invoking " + //NOI18N
                        "replaceCreatedObject() for " + getClass().getName() +
                        " - data now" + this, new Exception()); //NOI18N
            }
            replaceCreatedObject();
        }
    }

    private final class C implements InstanceContent.Convertor<C, T> {
        //This class delays reading the Properties object until it is
        //really needed (which is, unfortunately, when the node display name
        //is needed)

        public T convert(C arg0) {
            if (!isValid()) { //if deleted, don't try to create an instance
                return null;
            }
            ObservableProperties props = propsAdapter.asProperties();
            if (LOGGER.isLoggable(Level.FINEST)) {
                LOGGER.log(Level.FINEST, "Request for " +
                        PropertiesBasedDataObject.this.type().getName() + " from " //NOI18N
                        + getPrimaryFile().getPath() //NOI18N
                        + " observable properties completed.  Invoking " + //NOI18N
                        "createFrom() for " + getClass().getName() + " with " //NOI18N
                        + props); //NOI18N
            }
            return props == null ? null : createFrom(props);
        }

        public Class<? extends T> type(C arg0) {
            return type;
        }

        public String id(C arg0) {
            return "" + System.identityHashCode(this);
        }

        public String displayName(C arg0) {
            return getName();
        }
    }

    private final class IC implements InstanceCookie.Of, InstanceCookie {
        //No idea why this is necessary, but the platforms dialog logs a
        //warning that there is no InstanceCookie in our node otherwise

        public boolean instanceOf(Class<?> type) {
            return type.equals(PropertiesBasedDataObject.this.type);
        }

        public String instanceName() {
            return getName();
        }

        public Class<?> instanceClass() throws IOException, ClassNotFoundException {
            return type;
        }

        public Object instanceCreate() throws IOException, ClassNotFoundException {
            return getLookup().lookup(type);
        }
    }
}
