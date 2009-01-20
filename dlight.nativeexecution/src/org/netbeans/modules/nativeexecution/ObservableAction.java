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
package org.netbeans.modules.nativeexecution;

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
 * Extension of the <tt>AbstractAction</tt> implementation that allows to attach
 * listeners to get notifications on task start / finish events.
 * 
 * @author ak119685
 * @param <T> result type of the action
 */
public abstract class ObservableAction<T> extends AbstractAction implements Callable<T> {

    private static ExecutorService executorService = Executors.newCachedThreadPool();
    private final List<ObservableActionListener<T>> listeners =
            Collections.synchronizedList(new ArrayList<ObservableActionListener<T>>());
    private volatile T result = null;
    private volatile Future<T> task = null;
    private ActionEvent event;

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
    public void addObservableActionListener(ObservableActionListener<T> listener) {
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
    public void removeObservableActionListener(ObservableActionListener<T> listener) {
        listeners.remove(listener);
    }

    /**
     * Must be implemented in descendant class to perform an action.
     * Should not be invoked directly.
     *
     * @param e <tt>ActionEvent</tt>
     * @return result of <tt>ObservableAction</tt> execution.
     * 
     * @see #actionPerformed(java.awt.event.ActionEvent) 
     */
    abstract protected T performAction(ActionEvent e);

    public final void actionPerformed(final ActionEvent e) {
        // Will not start the task if it is already started.
        if (task != null) {
            System.out.println("Ignore...");
            return;
        }
        
        // Will execute task unsynchronously ... Post the task
        event = e;
        task = executorService.submit(this);
    }

    public T call() throws Exception {
        fireStarted();
        result = performAction(event);
        task = null;
        fireCompleted();
        
        return result;
    }

    private void fireStarted() {
        List<ObservableActionListener<T>> ll = new ArrayList<ObservableActionListener<T>>(listeners);

        for (ObservableActionListener l : ll) {
            l.actionStarted(this);
        }
    }

    private void fireCompleted() {
        List<ObservableActionListener<T>> ll = new ArrayList<ObservableActionListener<T>>(listeners);

        for (ObservableActionListener<T> l : ll) {
            l.actionCompleted(this, result);
        }
    }

    /**
     * Invokes action and waits for it's completion.
     *
     * @return result of action execution.
     */
    public T invokeAndWait() {
        actionPerformed(null);
        if (task != null) {
            // i.e. it is still running - wait for it's completion
            try {
                task.get();
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            } catch (ExecutionException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        return result;
    }

    /**
     * It is allowed to start action multiply times (though request to start
     * action will be disregarded in case action is in progress). The object
     * stores a value of result from the last finished invokation.
     *
     * @return result of most recent finished invokation.
     */
    public T getLastResult() {
        return result;
    }
}
