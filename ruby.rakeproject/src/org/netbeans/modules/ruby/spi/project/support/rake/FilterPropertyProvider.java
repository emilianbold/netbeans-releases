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

package org.netbeans.modules.ruby.spi.project.support.rake;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.util.WeakListeners;

/**
 * Property provider that delegates to another source.
 * Useful, for example, when conditionally loading from one or another properties file.
 * @since org.netbeans.modules.ruby.modules.project.rake/1 1.14
 */
public abstract class FilterPropertyProvider implements PropertyProvider {

    private PropertyProvider delegate;
    private final List<ChangeListener> listeners = new ArrayList<ChangeListener>();
    private final ChangeListener strongListener = new ChangeListener() {
        public void stateChanged(ChangeEvent e) {
            //System.err.println("DPP: change from current provider " + delegate);
            fireChange();
        }
    };
    private ChangeListener weakListener = null; // #50572: must be weak

    /**
     * Initialize the proxy.
     * @param delegate the initial delegate to use
     */
    protected FilterPropertyProvider(PropertyProvider delegate) {
        assert delegate != null;
        setDelegate(delegate);
    }

    /**
     * Change the current delegate (firing changes as well).
     * @param delegate the initial delegate to use
     */
    protected final void setDelegate(PropertyProvider delegate) {
        if (delegate == this.delegate) {
            return;
        }
        if (this.delegate != null) {
            assert weakListener != null;
            this.delegate.removeChangeListener(weakListener);
        }
        this.delegate = delegate;
        weakListener = WeakListeners.change(strongListener, delegate);
        delegate.addChangeListener(weakListener);
        fireChange();
    }

    public final Map<String, String> getProperties() {
        return delegate.getProperties();
    }

    public final synchronized void addChangeListener(ChangeListener listener) {
        // XXX could listen to delegate only when this has listeners
        listeners.add(listener);
    }

    public final synchronized void removeChangeListener(ChangeListener listener) {
        listeners.add(listener);
    }

    private void fireChange() {
        ChangeListener[] ls;
        synchronized (this) {
            if (listeners.isEmpty()) {
                return;
            }
            ls = listeners.toArray(new ChangeListener[listeners.size()]);
        }
        ChangeEvent ev = new ChangeEvent(this);
        for(ChangeListener l : ls) {
            l.stateChanged(ev);
        }
    }

}
