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

package org.openide.loaders;

import java.io.IOException;
import java.util.Set;
import javax.swing.event.ChangeListener;
import org.netbeans.api.progress.ProgressHandle;
import org.openide.WizardDescriptor;

/**
 *
 * @author Jiri Rechtacek
 */
class TemplateWizardIteratorWrapper implements WizardDescriptor.Iterator<WizardDescriptor>, ChangeListener {

    private TemplateWizardIterImpl iterImpl;
    
    /** Creates a new instance of TemplateWizardIteratorWrapper */
    private TemplateWizardIteratorWrapper (TemplateWizardIterImpl iterImpl) {
        this.iterImpl = iterImpl;
    }
    
    public TemplateWizardIterImpl getOriginalIterImpl () {
        return iterImpl;
    }
    
    /** Resets the iterator to first screen.
    */
    public void first () {
        iterImpl.first ();
    }

    /** Change the additional iterator.
    */
    public void setIterator (TemplateWizard.Iterator it, boolean notify) {
        iterImpl.setIterator (it, notify);
    }

    /** Getter for current iterator.
    */
    public TemplateWizard.Iterator getIterator () {
        return iterImpl.getIterator ();
    }

    /** Get the current panel.
     * @return the panel
     */
    public WizardDescriptor.Panel<WizardDescriptor> current() {
        return iterImpl.current ();
    }


    /** Get the name of the current panel.
     * @return the name
     */
    public String name() {
        return iterImpl.name ();
    }

    /** Test whether there is a next panel.
     * @return <code>true</code> if so
     */
    public boolean hasNext() {
        return iterImpl.hasNext ();
    }

    /** Test whether there is a previous panel.
     * @return <code>true</code> if so
     */
    public boolean hasPrevious() {
        return iterImpl.hasPrevious ();
    }

    /** Move to the next panel.
     * I.e. increment its index, need not actually change any GUI itself.
     * @exception NoSuchElementException if the panel does not exist
     */
    public void nextPanel() {
        iterImpl.nextPanel ();
    }

    /** Move to the previous panel.
     * I.e. decrement its index, need not actually change any GUI itself.
     * @exception NoSuchElementException if the panel does not exist
     */
    public void previousPanel() {
        iterImpl.previousPanel ();
    }

    /** Refires the info to listeners */
    public void stateChanged(final javax.swing.event.ChangeEvent p1) {
        iterImpl.stateChanged (p1);
    }

    /** Registers ChangeListener to receive events.
     *@param listener The listener to register.
     */
    public synchronized void addChangeListener(javax.swing.event.ChangeListener listener) {
        iterImpl.addChangeListener (listener);
    }
    /** Removes ChangeListener from the list of listeners.
     *@param listener The listener to remove.
     */
    public synchronized void removeChangeListener (javax.swing.event.ChangeListener listener) {
        iterImpl.removeChangeListener (listener);
    }
    
    public void initialize (WizardDescriptor wiz) {
        iterImpl.initialize (wiz);
    }
    
    public void uninitialize() {
        iterImpl.uninitialize ();
    }
    
    public void uninitialize (WizardDescriptor wiz) {
        iterImpl.uninitialize (wiz);
    }
    
    public Set<DataObject> instantiate () throws IOException {
        return iterImpl.instantiate ();
    }
    
    public Set<DataObject> instantiate (ProgressHandle handle) throws IOException {
        return iterImpl.instantiate (handle);
    }
    
    static class InstantiatingIterator extends TemplateWizardIteratorWrapper implements WizardDescriptor.InstantiatingIterator<WizardDescriptor> {
        public InstantiatingIterator (TemplateWizardIterImpl it) {
            super (it);
        }
    }
    
    static class AsynchronousInstantiatingIterator extends InstantiatingIterator implements WizardDescriptor.AsynchronousInstantiatingIterator<WizardDescriptor> {
        public AsynchronousInstantiatingIterator (TemplateWizardIterImpl it) {
            super (it);
        }
    }

    static class BackgroundInstantiatingIterator extends InstantiatingIterator implements WizardDescriptor.BackgroundInstantiatingIterator<WizardDescriptor> {
        public BackgroundInstantiatingIterator (TemplateWizardIterImpl it) {
            super (it);
        }
    }

    static class ProgressInstantiatingIterator extends InstantiatingIterator implements WizardDescriptor.ProgressInstantiatingIterator<WizardDescriptor> {
        private TemplateWizardIterImpl itImpl;
        public ProgressInstantiatingIterator (TemplateWizardIterImpl it) {
            super (it);
            itImpl = it;
        }
    }
    
}
