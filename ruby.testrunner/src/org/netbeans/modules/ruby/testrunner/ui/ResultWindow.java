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

package org.netbeans.modules.ruby.testrunner.ui;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.lang.ref.WeakReference;
import javax.accessibility.AccessibleContext;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.JSplitPane;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author Marian Petras, Erno Mononen
 */
public final class ResultWindow extends TopComponent {
    
    /** unique ID of <code>TopComponent</code> (singleton) */
    private static final String ID = "ruby-testrunner-results";              //NOI18N
    /**
     * instance/singleton of this class
     *
     * @see  #getInstance
     */
    private static WeakReference<ResultWindow> instance = null;
    
    /**
     * Returns a singleton of this class.
     *
     * @return  singleton of this class
     */
    static synchronized ResultWindow getInstance() {
        ResultWindow window;
        window = (ResultWindow) WindowManager.getDefault().findTopComponent(ID);
        if (window == null) {
            window = getDefault();
        }
        return window;
    }
    
    /**
     * Singleton accessor reserved for the window system only.
     * The window system calls this method to create an instance of this
     * <code>TopComponent</code> from a <code>.settings</code> file.
     * <p>
     * <em>This method should not be called anywhere except from the window
     * system's code.</em>
     *
     * @return  singleton - instance of this class
     */
    public static synchronized ResultWindow getDefault() {
        ResultWindow window = (instance != null) ? instance.get() : null;
        if (window == null) {
            window = new ResultWindow();
            window.initActions();
            instance = new WeakReference<ResultWindow>(window);
        }
        return window;
    }

    private void initActions() {
        ActionMap actions = getActionMap();
        actions.put("jumpNext", new PrevNextFailure(true));  //NOI18N
        actions.put("jumpPrev", new PrevNextFailure(false));  //NOI18N
    }

    /** */
    private JSplitPane view;
    
    
    /** Creates a new instance of ResultWindow */
    public ResultWindow() {
        super();
        setFocusable(true);
        setLayout(new BorderLayout());
        //add(tabbedPanel = new JTabbedPane(), BorderLayout.CENTER);
        
        setName(ID);
        setDisplayName(NbBundle.getMessage(ResultWindow.class,
                                           "TITLE_TEST_RESULTS"));      //NOI18N
        setIcon(ImageUtilities.loadImage(
                "org/netbeans/modules/ruby/testrunner/ui/res/testResults.png",//NOI18N
	        true));
        
        AccessibleContext accessibleContext = getAccessibleContext();
        accessibleContext.setAccessibleName(
                NbBundle.getMessage(getClass(), "ACSN_TestResults"));   //NOI18N
        accessibleContext.setAccessibleDescription(
                NbBundle.getMessage(getClass(), "ACSD_TestResults"));   //NOI18N
    }

    /**
     */
    void addDisplayComponent(JSplitPane displayComp) {
        assert EventQueue.isDispatchThread();

        removeAll();
        addView(displayComp);
        revalidate();
    }
    
    /**
     */
    private void addView(final JSplitPane view) {
        assert EventQueue.isDispatchThread();
        
        this.view = view;
        add(view);
    }
    
    /**
     */
    private boolean isActivated() {
        return TopComponent.getRegistry().getActivated() == this;
    }
    
    /**
     */
    void promote() {
        assert EventQueue.isDispatchThread();
        
        open();
        requestVisible();
        requestActive();
    }
    
    /**
     * Sets the layout orientation of the contained result pane.
     * 
     * @param orientation the orientation (see {@link JSplitPane#VERTICAL_SPLIT} 
     * and {@link JSplitPane#HORIZONTAL_SPLIT}) to set.
     */
    void setOrientation(int orientation) {
        if (view == null) {
            return;
        }
        if (view.getOrientation() != orientation) {
            view.setOrientation(orientation);
        }
    }
    
    /**
     */
    @Override
    protected String preferredID() {
        return ID;
    }
    
    /**
     */
    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(getClass());
    }
    
    /**
     */
    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }
    
    /**
     * Resolves to the {@linkplain #getDefault default instance} of this class.
     *
     * This method is necessary for correct functinality of window system's
     * mechanism of persistence of top components.
     */
    private Object readResolve() throws java.io.ObjectStreamException {
        return ResultWindow.getDefault();
    }

    private static final class PrevNextFailure extends AbstractAction {

        private final boolean next;

        public PrevNextFailure(boolean next) {
            this.next = next;
        }

        public void actionPerformed(ActionEvent e) {
            JSplitPane view = ResultWindow.getInstance().view;
            if (view == null || !(view.getLeftComponent() instanceof StatisticsPanel)) {
                return;
            }
            StatisticsPanel statisticsPanel = (StatisticsPanel) view.getLeftComponent();
            if (next) {
                statisticsPanel.selectNextFailure();
            } else {
                statisticsPanel.selectPreviousFailure();
            }
        }
    }
}
