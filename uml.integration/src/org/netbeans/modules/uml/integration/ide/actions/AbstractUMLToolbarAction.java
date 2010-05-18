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

package org.netbeans.modules.uml.integration.ide.actions;

import org.openide.util.HelpCtx;
import org.openide.util.actions.CallableSystemAction;

/**
 * Base class for UML toolbar action that has toolbar presenter and listens to
 * project open/close events for enablement.
 * Subclass needs to implement shouldEnable and performAction logic and overwrite
 * getName and iconResource
 */
public abstract class AbstractUMLToolbarAction extends CallableSystemAction 
    // IZ# 78917 - conover: UML toolbar removed 
    // implements PropertyChangeListener
{
	
	/**
     * name of a shared variable - is this the first call of method
     * <code>isEnabled()</code>?
     * Value of this variable is non-<code>null</code> only until method
     * {@link #isEnabled()} is called for the first time.
     */
    private static final String VAR_FIRST_ISENABLED
                                = "first call of isEnabled()";          //NOI18N
    /**
     * name of a shared variable - reference to the toolbar presenter
     */
    private static final String VAR_TOOLBAR_COMP_REF
                                = "toolbar presenter ref";              //NOI18N
    /**
     * name of a shared variable - are we listening on the set of open projects?
     * It contains <code>Boolean.TRUE</code> if we are listening,
     * and <code>null</code> if we are not listening.
     */
    private static final String VAR_LISTENING
                                = "listening";                          //NOI18N
    
    /**
     */
    protected void initialize() {
        super.initialize();
        putProperty(VAR_FIRST_ISENABLED, Boolean.TRUE);
    }

    /**
     */
// IZ# 78917 - conover: UML toolbar removed
//    public Component getToolbarPresenter() {
//        synchronized (getLock()) {
//            Component presenter = getStoredToolbarPresenter();
//            if (putProperty(VAR_LISTENING, Boolean.TRUE) == null) {
//                OpenProjects.getDefault().addPropertyChangeListener(this);
//                putProperty(VAR_FIRST_ISENABLED, null);
//                updateState();
//            }
//            return presenter;
//        }
//    }

    /**
     * Returns a toolbar presenter.
     * If the toolbar presenter already exists, returns the existing instance.
     * If it does not exist, creates a new toolbar presenter, stores
     * a reference to it to shared variable <code>VAR_TOOLBAR_BTN_REF</code>
     * and returns the presenter.
     *
     * @return  existing presenter; or a new presenter if it did not exist
     */
// IZ# 78917 - conover: UML toolbar removed
//    private Component getStoredToolbarPresenter() {
//        Object refObj = getProperty(VAR_TOOLBAR_COMP_REF);
//        if (refObj != null) {
//            Reference ref = (Reference) refObj;
//            Object presenterObj = ref.get();
//            if (presenterObj != null) {
//                return (Component) presenterObj;
//            }
//        }
//        
//        Component presenter = super.getToolbarPresenter();
//        putProperty(VAR_TOOLBAR_COMP_REF, new WeakReference(presenter));
//        return presenter;
//    }
    
    /**
     * Checks whether the stored toolbar presenter exists but does not create
     * one if it does not exist.
     *
     * @return  <code>true</code> if the reference to the toolbar presenter
     *          is not <code>null</code> and has not been cleared yet;
     *          <code>false</code> otherwise
     * @see  #getStoredToolbarPresenter
     */
// IZ# 78917 - conover: UML toolbar removed
//    private boolean checkToolbarPresenterExists() {
//        Object refObj = getProperty(VAR_TOOLBAR_COMP_REF);
//        if (refObj == null) {
//            return false;
//        }
//        return ((Reference) refObj).get() != null;
//    }
    
    /**
     * This method is called if we are listening for changes on the set
     * of open projecst and some project(s) is opened/closed.
     */
// IZ# 78917 - conover: UML toolbar removed
//    public void propertyChange(PropertyChangeEvent e) {
//        synchronized (getLock()) {
//            
//            /*
//             * Check whether listening on open projects is active.
//             * This block of code may be called even if listening is off.
//             * It can happen if this method's synchronized block contended
//             * for the lock with another thread which just switched listening
//             * off.
//             */
//            if (getProperty(VAR_LISTENING) == null) {
//                return;
//            }
//            
//            if (checkToolbarPresenterExists()) {
//                updateState();
//            } else {
//                OpenProjects.getDefault().removePropertyChangeListener(this);
//                putProperty(VAR_LISTENING, null);
//                putProperty(VAR_TOOLBAR_COMP_REF, null);
//            }
//        }
//        
//    }

    /**
     */
    public boolean isEnabled() {
        synchronized (getLock()) {
            if (getProperty(VAR_LISTENING) != null) {
                return super.isEnabled();
            } else if (getProperty(VAR_FIRST_ISENABLED) == null) {
                return shouldEnable();
            } else {
                
                /* first call of this method */
                putProperty(VAR_FIRST_ISENABLED, null);
                return false;
            }
        }
    }
    
    /**
     */
// IZ# 78917 - conover: UML toolbar removed
//    private synchronized void updateState() {
//        
//        /*
//         * no extra synchronization needed - the method is called
//         * only from synchronized blocks of the following methods:
//         *    propertyChange(...)
//         *    getToolbarPresenter()
//         */
//        
//        final boolean enabled = shouldEnable();
//        Mutex.EVENT.writeAccess(new Runnable() {
//            public void run() {
//                setEnabled(enabled);
//            }
//        });
//    }

	public abstract boolean shouldEnable();
	
	public abstract void performAction();
	
	
	public String getName() {
		return "";
	}
	
        // Fixed iz=112200. The API of super.iconResource() clearly says
        // "... If you do not want an icon, do not override this to return a blank 
        // icon. Leave it null..."; hence commented out this method.  Sub classes
        // that want to have an icon, just overide this mehod and return a proper
        // resource name.
//	protected String iconResource() {
//		return "";
//	}
	
	public HelpCtx getHelpCtx() {
		return HelpCtx.DEFAULT_HELP;
	}
	
	protected boolean asynchronous() {
		return false;
	}
}
