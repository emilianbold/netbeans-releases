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


package org.netbeans.modules.visualweb.designer.jsf;


import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignInfo;
import com.sun.rave.designtime.DisplayAction;
import com.sun.rave.designtime.DisplayActionSet;
import com.sun.rave.designtime.ext.DesignInfoExt;
import java.awt.Image;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import javax.swing.ImageIcon;
import org.netbeans.modules.visualweb.api.designtime.idebridge.DesigntimeIdeBridgeProvider;
import org.netbeans.modules.visualweb.insync.action.AbstractDisplayActionAction;
import org.netbeans.modules.visualweb.insync.markup.MarkupUnit;
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

        private DisplayAction displayAction;
        
        
        public DisplayActionWrapperAction(DisplayAction displayAction) {
            this.displayAction = displayAction;
        }
        
        protected DisplayAction[] getDisplayActions(DesignBean[] designBeans) {
            if (displayAction instanceof DisplayActionSet) {
                return ((DisplayActionSet)displayAction).getDisplayActions();
            } else {
                return new DisplayAction[] {displayAction};
            }
        }

        protected String getDefaultDisplayName() {
            return displayAction.getDisplayName();
        }
        
    } // End of DisplayActionWrapperAction
}
