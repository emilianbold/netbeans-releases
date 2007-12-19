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

package org.netbeans.modules.php.rt.providers.impl.local.apache;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.netbeans.modules.php.rt.ui.AddHostWizard;
import org.openide.WizardDescriptor.FinishablePanel;
import org.openide.WizardDescriptor.Panel;
import org.openide.util.HelpCtx;
import org.openide.util.Utilities;

/**
 *
 * @author ads
 */
public class ServerChooserPanel implements Panel, FinishablePanel {
    
    public static final String HOST                 = "Host";               // NOI18N
    /** path to apache configuration file (httpd.conf) */
    public static final String CONFIG_LOCATION    = "ConfigLocation";   // NOI18N
    /** manual or auto configuration way (How to find @see CONFIG_LOCATION ) */
    public static final String CONFIG_WAY    = "ConfigWay";   // NOI18N


    public Component getComponent() {
        if ( myComponent == null) { 
             myComponent = new ServerChooserVisual( this );
        }
        return  myComponent;
    }

    public HelpCtx getHelp() {
        return HelpCtx.DEFAULT_HELP;
    }

    public void readSettings(Object settings) {
        getVisual().read((AddHostWizard)settings);
    }

    public void storeSettings(Object settings) {
        getVisual().store((AddHostWizard)settings);
    }

    public boolean isValid() {
        return getVisual().isContentValid( );
    }

    public void addChangeListener(ChangeListener l) {
        synchronized (myListeners) {
            myListeners.add(l);
        }
    }

    public void removeChangeListener(ChangeListener l) {
        synchronized (myListeners) {
            myListeners.remove(l);
        }
    }

    public void stateChanged() {
        ChangeEvent event = new ChangeEvent(this);
        stateChanged(event);
    }
    
    public void stateChanged( ChangeEvent event ) {
        fireChange(event);
    }
    
    public void uninitialize() {
        /*
         * TODO : there can be better way to uninit : we can put 
         * myVisual into some Reference ( WeakReference or PhantomReference )
         * and set to null myVisual. In the method getComponent() one
         * can check myVisual, reference variable and in the case 
         * thet both are null instantiate new Component.
         * But in this case we need reset old values that are used 
         * in Visual component ( via setDefaults() ) each time when 
         * read() method is called with empty Host property.
         */
        myComponent = null;
    }
    
    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.FinishablePanel#isFinishPanel()
     */
    public boolean isFinishPanel() {
        return true;
    }

    public static boolean isSolaris() {
        return Utilities.getOperatingSystem() == Utilities.OS_SOLARIS 
                || Utilities.getOperatingSystem() == Utilities.OS_SUNOS;
    }
    
    private void fireChange( ChangeEvent event ) {
        ChangeListener[] listeners;
        synchronized (myListeners) {
            listeners = myListeners.toArray(new ChangeListener[myListeners
                    .size()]);
        }
        for (ChangeListener listener : listeners) {
            listener.stateChanged(event);
        }
    }

    private ServerChooserVisual getVisual() {
        return (ServerChooserVisual)getComponent();
    }
    
    private final List<ChangeListener> myListeners = new ArrayList<ChangeListener>();
    
    private ServerChooserVisual  myComponent;
    
}
