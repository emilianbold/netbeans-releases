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

package org.netbeans.modules.debugger.jpda.ui.actions;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Action;
import javax.swing.JMenuItem;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.openide.awt.Actions;
import org.openide.util.ContextAwareAction;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.Presenter.Popup;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author Martin
 */
public class SetAsCurrentThreadGutterAction extends SystemAction implements ContextAwareAction  {
    
    public SetAsCurrentThreadGutterAction() {
        setEnabled(false);
    }

    public String getName() {
        return NbBundle.getMessage(SetAsCurrentThreadGutterAction.class, "CTL_setAsCurrentThread");
    }

    public HelpCtx getHelpCtx() {
        return null;
    }
    
    public void actionPerformed(ActionEvent ev) {
    }
    
    public Action createContextAwareInstance(Lookup actionContext) {
        Lookup.Provider annotationLookupProvider = actionContext.lookup(Lookup.Provider.class);
        if (annotationLookupProvider != null) {
            JPDAThread thread = annotationLookupProvider.getLookup().lookup(JPDAThread.class);
            return new ThreadAwareAction(thread);
        } else {
            //Exceptions.printStackTrace(new IllegalStateException("expecting BreakpointAnnotation object in lookup "+actionContext));
            return this;
        }
    }
    
    private static class ThreadAwareAction implements Action, Popup {
        
        private JPDAThread thread;
        
        public ThreadAwareAction(JPDAThread thread) {
            this.thread = thread;
        }

        public Object getValue(String key) {
            if (Action.NAME.equals(key)) {
                return NbBundle.getMessage(SetAsCurrentThreadGutterAction.class, "CTL_setAsCurrentThreadT", thread.getName());
            } else {
                return null;
            }
        }

        public void putValue(String key, Object value) {}

        public void setEnabled(boolean b) {}

        public boolean isEnabled() {
            return true;
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {}

        public void removePropertyChangeListener(PropertyChangeListener listener) {}

        public void actionPerformed(ActionEvent e) {
            thread.makeCurrent();
        }

        public JMenuItem getPopupPresenter() {
            return new Actions.MenuItem (this, false);
        }
        
    }
    
}
