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

package org.netbeans.modules.editor.options;

import java.beans.beancontext.BeanContextChild;
import java.beans.beancontext.BeanContext;
import java.beans.beancontext.BeanContextMembershipListener;
import java.beans.beancontext.BeanContextMembershipEvent;
import java.util.Iterator;
import java.util.Arrays;
import org.netbeans.editor.Settings;
import org.openide.options.ContextSystemOption;
import org.openide.options.SystemOption;

/**
 * Listener that adds/removes the initializers corresponding
 * to the options to the Settings and performs the resetting
 * of the Settings.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */
class ContextOptionsListener implements BeanContextMembershipListener {
    
    /** Whether debug messages should be displayed */
    private static final boolean debug
        = Boolean.getBoolean("netbeans.debug.editor.options"); // NOI18N
    
    /** Only one shared instance is used. The side-effect advantage is
     * that BeanContextSupport will not add the same listener twice.
     */
    private static final ContextOptionsListener sharedListener
        = new ContextOptionsListener();

    /** Process the existing options added to the context system option
     * and start listening on the changes.
     */
    static void processExistingAndListen(ContextSystemOption cso) {
        BeanContextChild bcc = cso.getBeanContextProxy();
        
        // Start listening first
        if (bcc instanceof BeanContext) {
            ((BeanContext)bcc).addBeanContextMembershipListener(sharedListener);
        }
        
        // Process all the currently added options
        SystemOption[] sos = cso.getOptions();
        if (sos != null) {
            sharedListener.processInitializers(Arrays.asList(sos).iterator(), false);
        }
    }
    
    private ContextOptionsListener() {
    }

    public void childrenAdded(BeanContextMembershipEvent bcme) {
        processInitializers(bcme.iterator(), false);
    }

    public void childrenRemoved(BeanContextMembershipEvent bcme) {
        processInitializers(bcme.iterator(), true);
    }

    private void processInitializers(Iterator it, boolean remove) {
        while (it.hasNext()) {
            Object o = it.next();
            if (o instanceof OptionSupport) {
                OptionSupport os = (OptionSupport)o;
                Settings.Initializer si = os.getSettingsInitializer();
                // Remove the old one
                Settings.removeInitializer(si.getName());
                if (!remove) { // add the new one
                    Settings.addInitializer(si, Settings.OPTION_LEVEL);
                }

                if (debug) {
                    System.err.println((remove ? "Removed" : "Refreshed") // NOI18N
                        + " initializer=" + si.getName()); // NOI18N
                }
            }
        }
        
        /* Reset the settings so that the new initializers take effect
         * or the old are removed.
         */
        Settings.reset();
    }

}
