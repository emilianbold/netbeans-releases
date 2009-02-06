/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.nativeexecution.api;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.swing.AbstractAction;
import org.openide.util.Exceptions;

/**
 * Extension of the {@link AbstractAction} implementation that allows to attach
 * listeners to get notifications on task start / completion events.
 *
 * @param <T> result type of the action
 */
public abstract class ObservableAction<T> extends AbstractAction {

    private static ExecutorService executorService =
            Executors.newCachedThreadPool();
    private final List<ObservableActionListener<T>> listeners =
            Collections.synchronizedList(new ArrayList<ObservableActionListener<T>>());
    private volatile Future<T> task = null;
    private ActionEvent event;


    static {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

            public void run() {
                if (executorService != null) {
                    executorService.shutdown();
                }
            }
        }));
    }

    /**
     * Constructor
     * @param name name of the action that is passed to super constructor.
     */
    public ObservableAction(String name) {
        super(name);
    }

    /**
     * Adds an <tt>ObservableAction</tt> listener. Listener should be specified
     * with the same type parameter as <tt>ObservableAction</tt> does.
     *
     * It is guarantied that the same listener will not be added more than once.
     *
     * @param listener a <tt>ObservableActionListener</tt> object
     */
    public void addObservableActionListener(
            ObservableActionListener<T> listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * Removes an <tt>ObservableAction</tt> listener. Removing not previously
     * added listener has no effect.
     *
     * @param listener a <tt>ObservableActionListener</tt> object
     */
    public void removeObservableActionListener(
            ObservableActionListener<T> listener) {
        listeners.remove(listener);
    }

    /**
     * Must be implemented in descendant class to perform an action.
     * Normally it should not be invoked directly.
     *
     * @return result of <tt>ObservableAction</tt> execution.
     *
     * @see #invoke()
     * @see #actionPerformed(java.awt.event.ActionEvent)
     */
    abstract protected T performAction();

    /**
     * Invoked when an action occurs.
     * @param e event that causes the action. May be <tt>NULL</tt>
     */
    public final void actionPerformed(final ActionEvent e) {
        // Will not start the task if it is already started.
        if (task != null) {
            return;
        }

        // Will execute task unsynchronously ... Post the task
        event = e;
        task = executorService.submit(new Callable<T>() {

            public T call() throws Exception {
                fireStarted();
                T result = performAction();
                task = null;
                fireCompleted(result);

                return result;
            }
        });
    }

    /**
     * Performs synchronous execution of the action.
     * @return result ofaction execution.
     */
    public final T invoke() {
        actionPerformed(null);

        try {
            return task.get();
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (ExecutionException ex) {
            Exceptions.printStackTrace(ex);
        }

        return null;
    }

    private void fireStarted() {
        List<ObservableActionListener<T>> ll =
                new ArrayList<ObservableActionListener<T>>(listeners);

        for (ObservableActionListener l : ll) {
            l.actionStarted(this);
        }
    }

    private void fireCompleted(T result) {
        List<ObservableActionListener<T>> ll =
                new ArrayList<ObservableActionListener<T>>(listeners);

        for (ObservableActionListener<T> l : ll) {
            l.actionCompleted(this, result);
        }
    }
}
