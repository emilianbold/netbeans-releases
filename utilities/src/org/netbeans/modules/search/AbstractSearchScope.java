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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.search;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.search.SearchScope;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
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

    private static java.lang.reflect.Method compoundSICreator;

    protected SearchInfo createCompoundSearchInfo(SearchInfo[] delegates) {
        if (compoundSICreator == null) {
            try {
                compoundSICreator = SearchInfoFactory.class.getDeclaredMethod(
                        "createCompoundSearchInfo", SearchInfo[].class);//NOI18N
                compoundSICreator.setAccessible(true);
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        if (compoundSICreator != null) {
            try {
                return (SearchInfo) compoundSICreator.invoke(
                                                        null,
                                                        (Object) delegates);
            } catch (InvocationTargetException ex) {
                Throwable t = ex.getTargetException();
                if (t instanceof RuntimeException) {
                    throw (RuntimeException) t;
                }
            } catch (RuntimeException ex) {
                throw ex;
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return null;
    }
    
}
