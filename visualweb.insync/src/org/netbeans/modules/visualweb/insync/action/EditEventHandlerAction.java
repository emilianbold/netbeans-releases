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

import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignContext;
import com.sun.rave.designtime.DesignEvent;
import org.netbeans.modules.visualweb.insync.live.LiveUnit;
import org.netbeans.modules.visualweb.insync.models.FacesModel;
import org.netbeans.modules.visualweb.spi.designtime.idebridge.action.AbstractDesignBeanAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.event.ChangeListener;
import org.openide.ErrorManager;
import org.openide.awt.Actions;
import org.openide.cookies.EditCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Action allowing to edit the event handlers.
 *
 * @author Peter Zavadsky
 * @author Tor Norbye (old functionality implementation -> performActionAt impl)
 */
public class EditEventHandlerAction  extends AbstractDesignBeanAction {

    /** Creates a new instance of EditEventHandlerAction. */
    public EditEventHandlerAction() {
    }

    protected String getDisplayName(DesignBean[] designBeans) {
        List handlersAndLabels = getHandlersAndLabels(designBeans);
        String name;
        // XXX Missused list, 2 items mean 1 handler = 1 handler + 1 label.
        if (handlersAndLabels.size() == 2) {
            name = (String)handlersAndLabels.get(1);
        } else {
            name = null;
        }
        if (name == null) {
            return NbBundle.getMessage(EditEventHandlerAction.class, "LBL_EditEventHandlerAction");
        }
        return NbBundle.getMessage(EditEventHandlerAction.class, "LBL_EditEventHandlerActionName", name);
    }

    protected String getIconBase(DesignBean[] designBeans) {
        return null;
    }

    protected boolean isEnabled(DesignBean[] designBeans) {
        return !getHandlersAndLabels(designBeans).isEmpty();
    }

    protected void performAction(DesignBean[] designBeans) {
        // XXX Strange impl of the Actions.SubMenu(action, model, isPopup). If the model provides one item,
        // it doesn't call the performAt(0), but this method.
        new EventHandlersMenuModel(designBeans).performActionAt(0);
    }

    protected JMenuItem getMenuPresenter(Action contextAwareAction, Lookup.Result result) {
        return new Actions.SubMenu(contextAwareAction, new EventHandlersMenuModel(getDesignBeans(result)), false);
    }

    protected JMenuItem getPopupPresenter(Action contextAwareAction, Lookup.Result result) {
        return new Actions.SubMenu(contextAwareAction, new EventHandlersMenuModel(getDesignBeans(result)), true);
    }

    private static List getHandlersAndLabels(DesignBean[] designBeans) {
        if (designBeans.length == 0) {
            return Collections.EMPTY_LIST;
        }

        // XXX TODO take only the first one?
        // XXX Also very strange List content, mixing apples with pears (DesignEvents and Strings).
        // EAT: Tor changed it to use a call that takes isHidden into account
        return FacesModel.getVisibleEventsWithHandlerNames(designBeans[0]);
    }


    /** Implementation of the actions submenu model. */
    private static class EventHandlersMenuModel implements Actions.SubMenuModel {

        private final DesignBean[] designBeans;
        private final DesignEvent[] designEvents;
        private final String[] labels;

        public EventHandlersMenuModel(DesignBean[] designBeans) {
            this.designBeans = designBeans;

            List handlersAndLabels = getHandlersAndLabels(designBeans);

            List designEventList = new ArrayList();
            List labelList = new ArrayList();
            
            for (int i = 0, max = handlersAndLabels.size(); i < max; i += 2) {
                DesignEvent event = (DesignEvent)handlersAndLabels.get(i);
                String label = (String)handlersAndLabels.get(i + 1);
                designEventList.add(event);
                labelList.add(label);
            }
            
            this.designEvents = (DesignEvent[])designEventList.toArray(new DesignEvent[designEventList.size()]);
            this.labels = (String[])labelList.toArray(new String[labelList.size()]);
        }
        
        
        public int getCount() {
            return designEvents.length;
        }

        public String getLabel(int i) {
            return labels[i];
        }

        public HelpCtx getHelpCtx(int i) {
            // XXX Implement?
            return null;
        }

        public void performActionAt(int i) {
            if (designBeans.length == 0) {
                return;
            }
            
            DesignBean bean = designBeans[0];
            DesignContext context = bean.getDesignContext();
            // XXX Casting is error-prone.
            FacesModel facesModel = ((LiveUnit)context).getModel();
            
            facesModel.openEventHandler(designEvents[i]);
        }

        public void addChangeListener(ChangeListener changeListener) {
            // this model is not mutable.
        }

        public void removeChangeListener(ChangeListener changeListener) {
            // this model is not mutable.
        }
        
    } // End of EventHandlerMenuModel.
}
