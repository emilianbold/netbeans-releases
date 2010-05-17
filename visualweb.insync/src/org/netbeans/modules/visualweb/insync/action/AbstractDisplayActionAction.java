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

package org.netbeans.modules.visualweb.insync.action;


import com.sun.rave.designtime.CheckedDisplayAction;
import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignContext;
import com.sun.rave.designtime.DisplayAction;
import com.sun.rave.designtime.DisplayActionSet;
import com.sun.rave.designtime.Result;
// XXX FIXME this shouldn't depend on insync.
import org.netbeans.modules.visualweb.insync.ResultHandler;
import org.netbeans.modules.visualweb.insync.UndoEvent;
import org.netbeans.modules.visualweb.insync.live.LiveUnit;
import org.netbeans.modules.visualweb.insync.models.FacesModel;
import org.netbeans.modules.visualweb.spi.designtime.idebridge.action.AbstractDesignBeanAction;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.event.ChangeListener;
import org.openide.ErrorManager;
import org.openide.awt.Actions;
import org.openide.awt.DynamicMenuContent;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;


/**
 * XXX This should be ideally out of insync, in the designtime/idebridge.
 * But it has a big architectural flaw, that it needs insync ResultHandler to process tha action.
 * 
 * Abstract support action encapsulating <code>DisplayAction</code>s
 * and providing inline presenters for menu and popup.
 * Subclasses need to implement the <code>getDisplayActions</code>.
 * <p>
 * Note: Do not use for toolbars, the presenter is not implemented.
 * </p>
 * <p>
 * Note: XXX There is dependency on the insync (invocation of the action)
 * which points out incorrect designtime API arch. That should be fixed
 * before this support class could be moved into designtime/idebridge.
 * </p>
 *
 * @author Peter Zavadsky
 * @author Tor Norbye (old functionality implementation -> invokeDisplayAction impl)
 */
public abstract class AbstractDisplayActionAction extends AbstractDesignBeanAction {

    /** Creates a new instance of DisplayActionAction */
    public AbstractDisplayActionAction() {
    }


    protected abstract DisplayAction[] getDisplayActions(DesignBean[] designBeans);

    protected abstract String getDefaultDisplayName();

    protected String getDisplayName(DesignBean[] designBeans) {
        DisplayAction[] displayActions = getDisplayActions(designBeans);
        if (displayActions.length == 0) {
            return getDefaultDisplayName();
        } else {
            return displayActions[0].getDisplayName();
        }
    }

    protected String getIconBase(DesignBean[] designBeans) {
        return null;
    }

    protected boolean isEnabled(DesignBean[] designBeans) {
        return getDisplayActions(designBeans).length > 0;
    }

    protected void performAction(DesignBean[] designBeans) {
        DisplayAction[] displayActions = getDisplayActions(designBeans);
        if (designBeans.length == 0 || displayActions.length == 0) {
            return;
        }

        // XXX Are those always connected?
        DesignBean designBean = designBeans[0];
        DisplayAction displayAction = displayActions[0];

        invokeDisplayAction(displayAction, designBean);
    }

    // XXX FIXME This depends on insync internal, why it shouldn't.
    private static void invokeDisplayAction(DisplayAction displayAction, DesignBean designBean) {
        DesignContext context = designBean.getDesignContext();
        // XXX Retrieving the model this way (casting to LiveUnit) smells incorrect architecture.
        FacesModel facesModel = ((LiveUnit)context).getModel();
//        webform.getDocument().writeLock("\"" + displayAction.getLabel() + "\""); // NOI18N
        UndoEvent undoEvent = facesModel.writeLock("\"" + displayAction.getDisplayName() + "\""); // NOI18N
        try {
            Result result = displayAction.invoke();
        // XXX FIXME Postprocessing the action invocation makes the API unusable for other clients.
            ResultHandler.handleResult(result, facesModel);
        } finally {
//            webform.getDocument().writeUnlock();
            facesModel.writeUnlock(undoEvent);
        }
    }
    
    
    // Presenters
    protected JMenuItem getMenuPresenter(Action contextAwareAction, Lookup.Result result) {
        return new PresenterProvider(PresenterProvider.MENU, this, result);
    }

    protected JMenuItem getPopupPresenter(Action contextAwareAction, Lookup.Result result) {
        return new PresenterProvider(PresenterProvider.POPUP, this, result);
    }

// XXX No toolbar presenter, it shouldn't be needed.
//    protected Component getToolbarPresenter(Action contextAwareAction, DesignBean[] designBeans) {
////        return new Actions.ToolbarButton(contextAwareAction);
//        return new PresenterProvider(PresenterProvider.TOOLBAR, getDisplayActions(designBeans), designBeans[0]);
//    }

    
    
    private static class PresenterProvider extends JMenuItem/*just a fake*/ implements DynamicMenuContent {
        // FIXME Once moved to jdk5, replace with enum.
        private static final int MENU    = 0;
        private static final int POPUP   = 1;
//        private static final int TOOLBAR = 2;

        private final int type;
        private final AbstractDisplayActionAction delegate;
        private final Lookup.Result result;
        
        
        public PresenterProvider(int type, AbstractDisplayActionAction delegate, Lookup.Result result) {
            this.type = type;
            this.delegate = delegate;
            this.result = result;
        }
        
        
        public JComponent[] synchMenuPresenters(JComponent[] items) {
            if (items.length > 0) {
                // This is not dynamic.
                return items;
            }
            
            List components = new ArrayList();
            
            DesignBean[] designBeans = getDesignBeans(result);
            if (designBeans.length > 0) {
                DesignBean designBean = designBeans[0];
                DisplayAction[] displayActions = delegate.getDisplayActions(designBeans);
                for (int i = 0; i < displayActions.length; i ++) {
                    components.addAll(Arrays.asList(getPresenterComponents(type, displayActions[i], designBean)));
                }
            }
            return (JComponent[])components.toArray(new JComponent[components.size()]);
        }
        
        
        public JComponent[] getMenuPresenters() {
            return synchMenuPresenters(new JComponent[0]);
        }        
    } // End of PresenterProvider.

    
    private static JComponent[] getPresenterComponents(int type, DisplayAction displayAction, DesignBean designBean) {
        boolean isMenu;
        if (PresenterProvider.MENU == type) {
            isMenu = true;
        } else if (PresenterProvider.POPUP == type) {
            isMenu = false;
        } else {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                    new IllegalStateException("Invalid type=" + type)); // NOI18N
            return new JComponent[0];
        }
        
        if (displayAction instanceof DisplayActionSet) {
            DisplayActionSet displayActionSet = (DisplayActionSet)displayAction;
            DisplayAction[] items = displayActionSet.getDisplayActions();
//                if (items.length == 0) {
//                    return new JComponent[0];
//                } else if (items.length == 1) {
//                    return getPresenterComponents(type, items[0], designBean);
//                }
            if (displayActionSet.isPopup()) {
                return new JComponent[] {
                    new Actions.SubMenu(
                            new SingleDisplayActionAction(displayAction, designBean),
                            new DisplayActionSetMenuModel(items, designBean),
                            !isMenu)
                };
            } else {
                List components = new ArrayList();
                for (int i = 0; i < items.length; i++) {
                    components.addAll(Arrays.asList(getPresenterComponents(type, items[0], designBean)));
                }
                return (JComponent[])components.toArray(new JComponent[components.size()]);
            }
        } else if (displayAction instanceof CheckedDisplayAction) {
            CheckedDisplayAction checkedDisplayAction = (CheckedDisplayAction)displayAction;
            JMenuItem menuItem = new JCheckBoxMenuItem();
            Actions.connect(menuItem, new SingleDisplayActionAction(displayAction, designBean), !isMenu);
            menuItem.setSelected(checkedDisplayAction.isChecked());
            return new JComponent[] {menuItem};
        } else {
            return new JComponent[] {new Actions.MenuItem(new SingleDisplayActionAction(displayAction, designBean), isMenu)};
        }
    }
    
    
    private static class SingleDisplayActionAction extends AbstractAction {
        private final DisplayAction displayAction;
        private final DesignBean designBean;
        
        public SingleDisplayActionAction(DisplayAction displayAction, DesignBean designBean) {
            this.displayAction = displayAction;
            this.designBean = designBean;
            
            putValue(Action.NAME, displayAction.getDisplayName());
        }
        
        public void actionPerformed(ActionEvent evt) {
            invokeDisplayAction(displayAction, designBean);
        }
    } // End of SingleDisplayActionAction.
    
    
    /** Implementation of the actions submenu model.
     * XXX TODO This model is not recursive, again NB bad support. */
    private static class DisplayActionSetMenuModel implements Actions.SubMenuModel {

        private final DisplayAction[] displayActions;
        private final DesignBean designBean;
        
        public DisplayActionSetMenuModel(DisplayAction[] displayActions, DesignBean designBean) {
            this.displayActions = displayActions;
            this.designBean = designBean;
        }
        
        
        public int getCount() {
            return displayActions.length;
        }

        public String getLabel(int i) {
            return displayActions[i].getDisplayName();
        }

        public HelpCtx getHelpCtx(int i) {
            // XXX Implement?
            return null;
        }

        public void performActionAt(int i) {
            invokeDisplayAction(displayActions[i], designBean);
        }

        public void addChangeListener(ChangeListener changeListener) {
            // this model is not mutable.
        }

        public void removeChangeListener(ChangeListener changeListener) {
            // this model is not mutable.
        }
        
    } // End of DisplayActionSetMenuModel.
}
