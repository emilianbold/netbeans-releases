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
    
    static class ProgressInstantiatingIterator extends InstantiatingIterator implements WizardDescriptor.ProgressInstantiatingIterator<WizardDescriptor> {
        private TemplateWizardIterImpl itImpl;
        public ProgressInstantiatingIterator (TemplateWizardIterImpl it) {
            super (it);
            itImpl = it;
        }
    }
    
}
