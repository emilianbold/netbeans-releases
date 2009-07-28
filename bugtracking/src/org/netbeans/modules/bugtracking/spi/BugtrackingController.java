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
import java.io.IOException;
import javax.swing.JComponent;
import org.openide.util.HelpCtx;

/**
 *
 * @author Tomas Stupka
 */
public abstract class BugtrackingController {

    /**
     * Some data in the controllers component where changed
     */
    public static final String EVENT_COMPONENT_DATA_CHANGED   = "bugtracking.data.changed";   // NOI18N

    private final PropertyChangeSupport support = new PropertyChangeSupport(this);

    /**
     * Returns a visual component representing the bugtracking entity this controller is meant for
     * e.g. Repository, Query, ...
     * @return a visual component representing a bugtracking entity
     */
    public abstract JComponent getComponent() ;

    /**
     * Returns the help context assotiated with this controllers visual component
     * @return
     */
    public abstract HelpCtx getHelpCtx();

    /**
     * Returns true if data in this controllers visual component are valid
     * @return
     */
    public abstract boolean isValid();

    /**
     * Should return a message in case the controller isn't valid
     * @return
     */
    public String getErrorMessage() {
        return null;
    }
    
    /**
     * Is called when the changes made in the
     * controllers visual component are confirmed
     */
    public abstract void applyChanges() throws IOException; 

    /**
     * Should be called when this controllers parent component is openened
     */
    public void opened() {

    }

    /**
     * Should be called when this controllers parent component is closed
     */
    public void closed() {

    }

    /**
     * Registers a PropertyChangeListener
     * @param l
     */
    public void addPropertyChangeListener(PropertyChangeListener l) {
        support.addPropertyChangeListener(l);
    }

    /**
     * Unregisters a PropertyChangeListener
     * @param l
     */
    public void removePropertyChangeListener(PropertyChangeListener l) {
        support.removePropertyChangeListener(l);
    }

    /**
     * Signals a change in this controlers visual components data
     */
    protected void fireDataChanged() {
        support.firePropertyChange(EVENT_COMPONENT_DATA_CHANGED, null, null);
    }

}
