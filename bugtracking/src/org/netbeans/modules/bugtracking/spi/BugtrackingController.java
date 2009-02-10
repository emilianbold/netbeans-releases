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

package org.netbeans.modules.bugtracking.spi;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.JComponent;
import org.openide.util.HelpCtx;

/**
 *
 * @author tomas
 */
public abstract class BugtrackingController {

    public static String EVENT_COMPONENT_DATA_CHANGED   = "bugtracking.data.changed";
    public static String EVENT_COMPONENT_DATA_SAVED     = "bugtracking.data.saved"; // XXX
    public static String EVENT_COMPONENT_DATA_REMOVED   = "bugtracking.data.removed"; // XXX

    private final PropertyChangeSupport support;

    public BugtrackingController() {
        support = new PropertyChangeSupport(this);
    }
    
    public abstract JComponent getComponent() ;

    public abstract HelpCtx getHelpContext();

    public abstract boolean isValid();
    
    public abstract void applyChanges(); // XXX thow exception

    public void opened() {
        
    }

    public void closed() {

    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        support.addPropertyChangeListener(l);
        fireDataChanged();
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        support.removePropertyChangeListener(l);
    }

    protected void fireDataChanged() {
        support.firePropertyChange(EVENT_COMPONENT_DATA_CHANGED, null, null);
    }

    protected void fireDataSaved() {
        support.firePropertyChange(EVENT_COMPONENT_DATA_SAVED, null, null);
    }
    
    protected void fireDataRemoved() {
        support.firePropertyChange(EVENT_COMPONENT_DATA_REMOVED, null, null);
    }

}
