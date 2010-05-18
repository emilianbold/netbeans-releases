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

package org.netbeans.modules.search;

import java.util.ArrayList;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.search.SearchScope;
import org.openide.filesystems.FileObject;
import org.openidex.search.SearchInfo;
import org.openidex.search.SearchInfoFactory;

/**
 * Base class for implementations of search scopes.
 *
 * @author  Marian Petras
 */
public abstract class AbstractSearchScope extends SearchScope {
    
    private List<ChangeListener> changeListeners;
    private Boolean applicable;

    protected AbstractSearchScope() { }

    protected final boolean isApplicable() {

        /* thread: <any> */

        boolean currState;
        synchronized (getListenersLock()) {
            if ((applicable == null) && isListening()) {
                applicable = Boolean.valueOf(checkIsApplicable());
            }
            if (applicable != null) {
                return applicable.booleanValue();
            }
        }

        return checkIsApplicable();
    }

    protected final void setApplicable(boolean applicable) {
        List<ChangeListener> listeners;

        synchronized (getListenersLock()) {
            if (!isListening()) {
                return;
            }
            if ((this.applicable != null)
                    && (applicable == this.applicable.booleanValue())) {
                return;
            }
            this.applicable = Boolean.valueOf(applicable);
            listeners = (changeListeners != null) && !changeListeners.isEmpty()
                        ? new ArrayList<ChangeListener>(changeListeners)
                        : null;
        }

        if (listeners != null) {
            final ChangeEvent e = new ChangeEvent(this);
            for (ChangeListener l : listeners) {
                l.stateChanged(e);
            }
        }
    }

    protected final void updateIsApplicable() {
        setApplicable(checkIsApplicable());
    }
    
    protected abstract boolean checkIsApplicable();
    
    protected abstract void startListening();
    
    protected abstract void stopListening();
    
    protected final boolean isListening() {

        /* thread: <any> */

        synchronized (getListenersLock()) {
            return changeListeners != null;
        }
    }

    protected void addChangeListener(ChangeListener l) {
        if (l == null) {
            throw new IllegalArgumentException("null");                 //NOI18N
        }
        
        /* thread: <any> */

        synchronized (getListenersLock()) {
            boolean firstListener = !isListening();
            if (changeListeners == null) {
                changeListeners = new ArrayList<ChangeListener>(1);
            }
            changeListeners.add(l);
            if (firstListener) {
                assert applicable == null;
                startListening();
            }
            assert isListening();
        }
    }

    protected void removeChangeListener(ChangeListener l) {
        if (l == null) {
            throw new IllegalArgumentException("null");                 //NOI18N
        }
        
        /* thread: <any> */

        synchronized (getListenersLock()) {
            if (changeListeners.remove(l) && changeListeners.isEmpty()) {
                changeListeners = null;
                stopListening();
                applicable = null;
                assert !isListening();
            }
        }
    }
        
    
    protected SearchInfo createEmptySearchInfo() {
        return SearchInfoFactory.createSearchInfo(
                new FileObject[0], false, null);
    }

}
