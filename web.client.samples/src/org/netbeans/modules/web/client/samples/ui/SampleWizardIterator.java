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
package org.netbeans.modules.web.client.samples.ui;

import java.awt.Component;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.event.ChangeListener;

import org.netbeans.api.progress.ProgressHandle;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.Panel;
import org.openide.WizardDescriptor.ProgressInstantiatingIterator;
import org.openide.util.NbBundle;


/**
 * @author ads
 *
 */
public class SampleWizardIterator implements ProgressInstantiatingIterator<WizardDescriptor> {
    
    public static final String HELP_CTX = "html5.samples";      // NOI18N

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.Iterator#addChangeListener(javax.swing.event.ChangeListener)
     */
    @Override
    public void addChangeListener( ChangeListener listener ) {
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.Iterator#current()
     */
    @Override
    public Panel<WizardDescriptor> current() {
        return myPanels[myIndex];
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.Iterator#hasNext()
     */
    @Override
    public boolean hasNext() {
        return myIndex < myPanels.length - 1;
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.Iterator#hasPrevious()
     */
    @Override
    public boolean hasPrevious() {
        return myIndex >0 ;
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.Iterator#name()
     */
    @Override
    public String name() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.Iterator#nextPanel()
     */
    @Override
    public void nextPanel() {
        if (! hasNext()) {
            throw new NoSuchElementException();
        }
        myIndex++;        
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.Iterator#previousPanel()
     */
    @Override
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        myIndex--;       
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.Iterator#removeChangeListener(javax.swing.event.ChangeListener)
     */
    @Override
    public void removeChangeListener( ChangeListener listener ) {
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.InstantiatingIterator#initialize(org.openide.WizardDescriptor)
     */
    @Override
    public void initialize( WizardDescriptor descriptor ) {
        myPanels = new Panel[1];
        myPanels[0]=new SamplePanel(descriptor);
        
        String[] steps = createSteps();
        for (int i=0; i < myPanels.length; i++) {
            Component c = myPanels[i].getComponent();
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                // Step #.
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, 
                        Integer.valueOf(i));
                // Step name (actually the whole list for reference).
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.InstantiatingIterator#instantiate()
     */
    @Override
    public Set<?> instantiate() throws IOException {
        assert false;
        return null;
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.ProgressInstantiatingIterator#instantiate(org.netbeans.api.progress.ProgressHandle)
     */
    @Override
    public Set<?> instantiate( ProgressHandle handle ) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }
    
    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.InstantiatingIterator#uninitialize(org.openide.WizardDescriptor)
     */
    @Override
    public void uninitialize( WizardDescriptor descriptor ) {
        myPanels = null;
    }
    
    private String[] createSteps() {
        return new String[]{NbBundle.getMessage(SamplePanel.class, "LBL_NameNLocation")};// NOI18N
    }

    private transient WizardDescriptor.Panel[] myPanels;
    private transient int myIndex;

}
