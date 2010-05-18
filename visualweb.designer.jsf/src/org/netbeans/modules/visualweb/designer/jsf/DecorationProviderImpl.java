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


package org.netbeans.modules.visualweb.designer.jsf;


import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignContext;
import com.sun.rave.designtime.DesignInfo;
import com.sun.rave.designtime.DisplayAction;
import com.sun.rave.designtime.DisplayActionSet;
import com.sun.rave.designtime.Result;
import com.sun.rave.designtime.ext.DesignInfoExt;
import java.awt.Image;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import javax.swing.ImageIcon;
import org.netbeans.modules.visualweb.api.designtime.idebridge.DesigntimeIdeBridgeProvider;
import org.netbeans.modules.visualweb.insync.ResultHandler;
import org.netbeans.modules.visualweb.insync.UndoEvent;
import org.netbeans.modules.visualweb.insync.action.AbstractDisplayActionAction;
import org.netbeans.modules.visualweb.insync.live.LiveUnit;
import org.netbeans.modules.visualweb.insync.markup.MarkupUnit;
import org.netbeans.modules.visualweb.insync.models.FacesModel;
import org.netbeans.modules.visualweb.spi.designer.Decoration;
import org.netbeans.modules.visualweb.spi.designer.DecorationProvider;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.w3c.dom.Element;


/**
 *
 * @author Peter Zavadsky
 */
public class DecorationProviderImpl implements DecorationProvider {

    public DecorationProviderImpl() {
    }

    
    public Decoration getDecoration(Element element) {
        DesignBean designBean = MarkupUnit.getMarkupDesignBeanForElement(element);
        if (designBean == null) {
            return null;
        }
        DesignInfo designInfo = designBean.getDesignInfo();
        
        if (designInfo instanceof DesignInfoExt) {
            DesignInfoExt designInfoExt = (DesignInfoExt)designInfo;
            
            DisplayActionSet decorationItems = designInfoExt.getContextItemsExt(designBean);
            if (decorationItems != null) {
                return new DisplayActionSetDecoration(designBean, decorationItems);
            }
        }
        return null;
    }

    
    private static class DisplayActionSetDecoration implements Decoration {

        private final DesignBean designBean;
        private final DisplayActionSet displayActionSet;
        
        public DisplayActionSetDecoration(DesignBean designBean, DisplayActionSet displayActionSet) {
            this.designBean = designBean;
            this.displayActionSet = displayActionSet;
        }
        
        public int getWidth() {
            Image image = getImage();
            return image == null ? 0 : new ImageIcon(image).getIconWidth();
        }

        public int getHeight() {
            Image image = getImage();
            return image == null ? 0 : new ImageIcon(image).getIconHeight();
        }

        public Image getImage() {
            return displayActionSet.getSmallIcon();
        }

        public Action[] getActions() {
            return retrieveActions(displayActionSet.getDisplayActions());
        }

        public Action getDefaultAction() {
            Action[] actions = getActions();
            return actions.length > 0 ? actions[0] : null;
        }

        public Lookup getContext() {
            Node node = DesigntimeIdeBridgeProvider.getDefault().getNodeRepresentation(designBean);
            return node == null ? Lookup.EMPTY : node.getLookup();
        }
        
    } // End of DisplayActionSetDecoration.
    
    
    private static Action[] retrieveActions(DisplayAction[] displayActions) {
        List<Action> actions = new ArrayList<Action>();
        for (DisplayAction displayAction : displayActions) {
            if (displayAction != null) {
                actions.add(new DisplayActionWrapperAction(displayAction));
            }
        }
        return actions.toArray(new Action[actions.size()]);
    }
    
    private static class DisplayActionWrapperAction extends AbstractDisplayActionAction {

        private final DisplayAction displayAction;
        
        
        public DisplayActionWrapperAction(DisplayAction displayAction) {
            this.displayAction = displayAction;
        }
        
        protected DisplayAction[] getDisplayActions(DesignBean[] designBeans) {
            if (displayAction == null) {
                return new DisplayAction[0];
            }
            if (displayAction instanceof DisplayActionSet) {
                return ((DisplayActionSet)displayAction).getDisplayActions();
            } else {
                return new DisplayAction[] {displayAction};
            }
        }

        protected String getDefaultDisplayName() {
            if (displayAction == null) {
                return null;
            }
            return displayAction.getDisplayName();
        }
        
    } // End of DisplayActionWrapperAction
    
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
    
}
