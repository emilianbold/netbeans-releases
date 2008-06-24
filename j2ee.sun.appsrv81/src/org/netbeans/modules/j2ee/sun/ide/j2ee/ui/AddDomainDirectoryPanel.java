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

package org.netbeans.modules.j2ee.sun.ide.j2ee.ui;

import java.awt.Component;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.j2ee.sun.ide.j2ee.Utils;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/** Panel to query for a domain directory.
 * Used to query the user for a local instance's domain directory.
 */
class AddDomainDirectoryPanel implements WizardDescriptor.FinishablePanel,
        ChangeListener {
    
    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private AddInstanceVisualDirectoryPanel component;
    private WizardDescriptor wiz;
    final private boolean creatingPersonalInstance;
    
    AddDomainDirectoryPanel(boolean creatingPersonalInstance) {
        this.creatingPersonalInstance = creatingPersonalInstance;
    }
    
    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    public Component getComponent() {
        if (component == null) {
            component = new AddInstanceVisualDirectoryPanel(creatingPersonalInstance);
            component.addChangeListener(this);
        }
        return component;
    }
    
    public HelpCtx getHelp() {
        if (creatingPersonalInstance)
            return new HelpCtx("AS_RegServ_EnterPIDir"); //NOI18N
        else 
            return new HelpCtx("AS_RegServ_EnterDomainDir");
    }
        
    /** Is the directory usable.
     *
     * see Util.rootOfUsableDomain(File)
     * 
     */
    public boolean isValid() {
        if (null == wiz) {
            return false;
        }
        File domainDir = new File(((AddInstanceVisualDirectoryPanel)getComponent()).getInstanceDirectory());
        if (!domainDir.isAbsolute()) {
            wiz.putProperty(AddDomainWizardIterator.PROP_ERROR_MESSAGE,
                    NbBundle.getMessage(AddDomainDirectoryPanel.class,
                    "Msg_EneterValidDomainDir",                                 //NOI18N
                    ((AddInstanceVisualDirectoryPanel)getComponent()).getInstanceDirectory()));
            ((AddInstanceVisualDirectoryPanel)getComponent()).setAdminPort("");
            return false;                
        }
        if (!creatingPersonalInstance) {
            if (((AddInstanceVisualDirectoryPanel)getComponent()).getInstanceDirectory().length() < 1) {
                wiz.putProperty(AddDomainWizardIterator.PROP_ERROR_MESSAGE,
                        NbBundle.getMessage(AddDomainDirectoryPanel.class,
                        "Msg_EneterValidDomainDir",                                 //NOI18N
                        ((AddInstanceVisualDirectoryPanel)getComponent()).getInstanceDirectory()));
                ((AddInstanceVisualDirectoryPanel)getComponent()).setAdminPort("");
                return false;                
            }
            String mess = Util.rootOfUsableDomain(domainDir);
            if (null != mess) {
                wiz.putProperty(AddDomainWizardIterator.PROP_ERROR_MESSAGE,
                        mess);
                ((AddInstanceVisualDirectoryPanel)getComponent()).setAdminPort("");                                     // NOI18N
                return false;
            }
            Util.fillDescriptorFromDomainXml(wiz, domainDir);
            String port = (String)wiz.getProperty(AddDomainWizardIterator.PORT);
            ((AddInstanceVisualDirectoryPanel)getComponent()).setAdminPort(port);
            if ("".equals(port)) {                                              // NOI18N
                wiz.putProperty(AddDomainWizardIterator.PROP_ERROR_MESSAGE,
                        NbBundle.getMessage(AddDomainDirectoryPanel.class,
                        "Msg_UnsupportedDomain",                                 //NOI18N
                        ((AddInstanceVisualDirectoryPanel)getComponent()).getInstanceDirectory()));   
                return false;
            }
            return true;
        } else {
            File parent = domainDir.getParentFile();
            if (domainDir.exists()) {
                wiz.putProperty(AddDomainWizardIterator.PROP_ERROR_MESSAGE,
                        NbBundle.getMessage(AddDomainDirectoryPanel.class,
                        "Msg_ExistingDomainDir",                                //NOI18N
                        domainDir.getAbsolutePath()));
                return false;                
            }
            if (null == parent) {
                wiz.putProperty(AddDomainWizardIterator.PROP_ERROR_MESSAGE,
                        NbBundle.getMessage(AddDomainDirectoryPanel.class,
                        "Msg_InValidDomainDir",                                 //NOI18N
                        ((AddInstanceVisualDirectoryPanel)getComponent()).getInstanceDirectory()));
                return false;
            }
            if (!parent.exists()) {
                wiz.putProperty(AddDomainWizardIterator.PROP_ERROR_MESSAGE,
                        NbBundle.getMessage(AddDomainDirectoryPanel.class,
                        "Msg_InValidDomainDirParent",                           //NOI18N
                        parent.getAbsolutePath()));
                return false;
            }
            if (!Utils.canWrite(parent)) {
                wiz.putProperty(AddDomainWizardIterator.PROP_ERROR_MESSAGE,
                        NbBundle.getMessage(AddDomainDirectoryPanel.class,
                        "Msg_InValidDomainDirParent",                                 //NOI18N
                        parent.getAbsolutePath()));
                return false;
            }
            String path = domainDir.getAbsolutePath();
            byte bytes[] = path.getBytes();
            byte utf8[] = bytes;
            try {
                utf8 = path.getBytes("UTF-8");
            } catch (java.io.UnsupportedEncodingException uee) {
                // if we get to here... creating a domain will be the least
                // of the users worries.  A Java VM has to support UTF-8 encoding.
                Logger.getLogger(AddDomainDirectoryPanel.class.getName()).log(Level.FINER,
                        null, uee);
            }
            if (bytes.length != utf8.length) {
                wiz.putProperty(AddDomainWizardIterator.PROP_ERROR_MESSAGE,
                        NbBundle.getMessage(AddDomainDirectoryPanel.class,
                        "Msg_Utf8Required"));                                 //NOI18N
                return false;
            }
            wiz.putProperty(AddDomainWizardIterator.DOMAIN,domainDir.getName());
            wiz.putProperty(AddDomainWizardIterator.INSTALL_LOCATION,
                    domainDir.getParentFile().getAbsolutePath());
            wiz.putProperty(AddDomainWizardIterator.PROP_ERROR_MESSAGE,null);
            return true;
        }
    }
    
    // Event handling
    //
    private final Set/*<ChangeListener>*/ listeners = 
            new HashSet/*<ChangeListener>*/(1);
    public final void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }
    public final void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }
    protected final void fireChangeEvent() {
        Iterator/*<ChangeListener>*/ it;
        synchronized (listeners) {
            it = new HashSet/*<ChangeListener>*/(listeners).iterator();
        }
        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            ((ChangeListener)it.next()).stateChanged(ev);
        }
    }

    // You can use a settings object to keep track of state. Normally the
    // settings object will be the WizardDescriptor, so you can use
    // WizardDescriptor.getProperty & putProperty to store information entered
    // by the user.
    public void readSettings(Object settings) {
        wiz = (WizardDescriptor) settings;
    }
    
    public void storeSettings(Object settings) {
        // TODO implement?
    }

    public void stateChanged(ChangeEvent e) {
        fireChangeEvent();
    }

    /** This panel is a finishable panel for registering an existing instance.
     *
     * If the user is trying to create an instance we may be in trouble
     */
    public boolean isFinishPanel() {
        return !creatingPersonalInstance;
    }    
}
