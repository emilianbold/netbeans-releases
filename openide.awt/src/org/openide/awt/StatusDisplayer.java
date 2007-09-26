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
package org.openide.awt;

import javax.swing.event.ChangeListener;
import org.openide.util.Lookup;
import org.openide.util.ChangeSupport;


/** Permits control of a status line.
 * The default instance may correspond to the NetBeans status line in the main window.
 * @author Jesse Glick
 * @since 3.14
 */
public abstract class StatusDisplayer {
    private static StatusDisplayer INSTANCE = null;

    /** Subclass constructor. */
    protected StatusDisplayer() {
    }

    /** Get the default status displayer.
     * @return the default instance from lookup
     */
    public static synchronized StatusDisplayer getDefault() {
        if (INSTANCE == null) {
            INSTANCE = Lookup.getDefault().lookup(StatusDisplayer.class);

            if (INSTANCE == null) {
                INSTANCE = new Trivial();
            }
        }

        return INSTANCE;
    }

    /** Get the currently displayed text.
     * <p>Modules should <strong>not</strong> need to call this method.
     * If you think you really do, please explain why on nbdev.
     * The implementation of the GUI component (if any) which displays
     * the text naturally needs to call it.
     * @return some text
     */
    public abstract String getStatusText();

    /** Show text in the status line.
     * Can be called at any time, but remember the text may not be updated
     * until the AWT event queue is ready for it - so if you are hogging
     * the event queue the text will not appear until you release it
     * (finish your work or display a modal dialog, for example).
     *  <p class="nonnormative">Default implementation of status line in NetBeans
     * displays the text in status line and clears it after a while. 
     * Also there is no guarantee how long the text will be displayed as 
     * it can be replaced with new call to this method at any time.
     * @param text the text to be shown
     */
    public abstract void setStatusText(String text);

    /** Add a listener for when the text changes.
     * @param l a listener
     */
    public abstract void addChangeListener(ChangeListener l);

    /** Remove a listener for the text.
     * @param l a listener
     */
    public abstract void removeChangeListener(ChangeListener l);

    /**
     * Trivial default impl for standalone usage.
     * @see "#32154"
     */
    private static final class Trivial extends StatusDisplayer {
        private final ChangeSupport cs = new ChangeSupport(this);
        private String text = ""; // NOI18N

        public synchronized String getStatusText() {
            return text;
        }

        public synchronized void setStatusText(String text) {
            if (text.equals(this.text)) {
                return;
            }

            this.text = text;

            if (text.length() > 0) {
                System.err.println("(" + text + ")"); // NOI18N
            }

            cs.fireChange();
        }

        public void addChangeListener(ChangeListener l) {
            cs.addChangeListener(l);
        }

        public void removeChangeListener(ChangeListener l) {
            cs.removeChangeListener(l);
        }

    }
}
