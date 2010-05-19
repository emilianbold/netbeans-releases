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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.javacard.ri.platform;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.netbeans.modules.javacard.spi.JavacardPlatformKeyNames;
import org.netbeans.modules.propdos.AntStyleResolvingProperties;
import org.netbeans.modules.propdos.ObservableProperties;
import org.openide.util.NbCollections;

/**
 * ObservableProperties which merges two other ObservableProperties.  The
 * constructor is passed two instances.  Properties in the second (b) ObservableProperties
 * take precedence over the first.
 * <p/>
 * The first ObservableProperties can have its contents merged with the second
 * by specifying keys with the prefix "merge".  The delimiter used in merging
 * is either a comma or a file path separator, depending on whether the property
 * is one that is a file path, as defined in JavacardPlatformKeyNames.getPropertyPathNames().
 * <p/>
 * So, if a. contains
 * <pre>
 * foo=bar
 * </pre>
 * and b. contains
 * <pre>
 * merge.foo=baz
 * </pre>
 * then the value of <code>get("foo")</code> is <code>bar,baz</code>.
 * <p/>
 * Instances of MergeProperties are non-mutatable, although they will reflect
 * mutations in the wrapped ObservableProperties.
 * <p/>
 * Dereferencing ${propname} delimited properties is supported, including
 * cross-references between the two passed ObservableProperties.
 * <p/>
 * Note that on save, any keys starting with APPEND_PREFIX or PREPEND_PREFIX are
 * <i>not</i> saved - saving this object results in a file that represents
 * the combined state.
 *
 * @author Tim Boudreau
 */
public final class MergeProperties extends AntStyleResolvingProperties implements PropertyChangeListener {
    private final PropertyChangeSupport supp = new PropertyChangeSupport(this);
    private final ObservableProperties a;
    private final ObservableProperties b;
    public static final String APPEND_PREFIX = "append."; //NOI18N
    public static final String PREPEND_PREFIX = "prepend."; //NOI18N
    public MergeProperties (ObservableProperties a, ObservableProperties b) {
        super(true);
        this.a = a;
        this.b = b;
        //XXX naive implementation, but non-verbose
        addAll (a);
        addAll (b);
    }

    private void addAll(Properties p) {
        for (Map.Entry<Object, Object> e : p.entrySet()) {
            super.put (e.getKey(), e.getValue());
        }
    }

    @Override
    protected boolean isSavableProperty (String key) {
        return key != null && !key.startsWith(APPEND_PREFIX) && !key.startsWith(PREPEND_PREFIX);
    }

    private String getProperty (ObservableProperties props, String key) {
        return props instanceof AntStyleResolvingProperties ? ((AntStyleResolvingProperties) props).getProperty(key, false) :
            props.getProperty(key);
    }

    @Override
    public String getProperty (String key) {
        return getProperty (key, true);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getClass().getName() + "@" + System.identityHashCode(this)); //NOI18N
        for (Object key : keySet()) {
            String k = (String) key;
            if (isSavableProperty(k)) {
                if (sb.length() > 0) {
                    sb.append('\n'); //NOI18N
                }
                sb.append (k);
                sb.append ("="); //NOI18N
                sb.append (getProperty(k));
            }
        }
        return sb.toString();
    }

    @Override
    public String getProperty (String key, boolean resolve) {
        Set<String> pathProps = JavacardPlatformKeyNames.getPathPropertyNames(NbCollections.checkedMapByFilter(a, String.class, String.class, false));
        pathProps.addAll(JavacardPlatformKeyNames.getPathPropertyNames(NbCollections.checkedMapByFilter(b, String.class, String.class, false)));
        char delimiter = pathProps.contains(key) ? File.pathSeparatorChar : ','; //NOI18N
        
        String appendKey = APPEND_PREFIX + key;
        String append = b.getProperty(appendKey);
        String result = null;
        if (append != null) {
            String val = getProperty (a, key);
            result = merge (val, append, delimiter);
            return result;
        }
        String prependKey = PREPEND_PREFIX + key;
        String prepend = b.getProperty(prependKey);
        if (prepend != null) {
            String val = result != null ? result : getProperty(a, key);
            result = merge (prepend, val, delimiter);
            if (result == null) {
                result = prepend;
            }
        }
        if (result == null) {
            result = getProperty(b, key);
            if (result == null) {
                result = getProperty(a, key);
                if (result == null) {
                    result = merge (prepend, append, delimiter);
                }
            }
        }
        if (resolve && result != null && result.indexOf("${") >= 0) { //NOI18N
            result = resolve (key, result);
        }
        return result;
    }

    static String merge (String as, String bs, char delimiter) {
        boolean aEmpty = empty(as);
        boolean bEmpty = empty(bs);
        if (aEmpty && bEmpty) {
            return null;
        } else if (aEmpty != bEmpty) {
            return aEmpty ? bs : as;
        } else {
            StringBuilder sb = new StringBuilder (as);
            if (sb.length() > 0) {
                sb.append (delimiter);
            }
            sb.append (bs);
            return sb.toString();
        }
    }

    private static boolean empty (String s) {
        return s == null || "".equals(s.trim()); //NOI18N
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        synchronized (supp) {
            boolean had = supp.getPropertyChangeListeners().length > 0;
            supp.addPropertyChangeListener(pcl);
            if (!had) {
                addNotify();
            }
        }
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        synchronized (supp) {
            boolean had = supp.getPropertyChangeListeners().length > 0;
            supp.removePropertyChangeListener(pcl);
            if (had && supp.getPropertyChangeListeners().length == 0) {
                removeNotify();
            }
        }
    }

    @Override
    public Object remove(Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(Map<? extends Object, ? extends Object> t) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object put(Object key, Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    private void addNotify() {
        a.addPropertyChangeListener(this);
        b.addPropertyChangeListener(this);
    }

    private void removeNotify() {
        a.removePropertyChangeListener(this);
        b.removePropertyChangeListener(this);
    }

    public void propertyChange(PropertyChangeEvent evt) {
        boolean fire = false;
        if (a == evt.getSource()) {
            Object ob = b.get(evt.getPropertyName());
            fire = ob == null;
        } else {
            fire = true;
        }
        if (fire) {
            if (evt.getNewValue() == null) {
                super.remove (evt.getPropertyName());
            } else {
                super.put (evt.getPropertyName(), evt.getNewValue());
            }
            supp.firePropertyChange(evt.getPropertyName(), evt.getOldValue(),
                    evt.getNewValue());
        }
    }
}
