/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
