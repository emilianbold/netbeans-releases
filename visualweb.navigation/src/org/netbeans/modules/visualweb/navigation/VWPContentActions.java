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
/*
 * VWPContentActions.java
 *
 * Created on April 15, 2007, 10:24 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.visualweb.navigation;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.lang.ref.WeakReference;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;
import org.netbeans.modules.web.jsf.navigation.pagecontentmodel.PageContentItem;
import org.openide.util.NbBundle;

/**
 *
 * @author joelle
 */
public class VWPContentActions {
    //
    //    /** Creates a new instance of VWPContentActions
    //     * @param facesModel
    //     */
    public VWPContentActions(VWPContentModel vwpContentModel) {
        setVwpContentModel(vwpContentModel);
//        handleAddCommandButton.putValue("NAME", addButton);
//        handleAddCommandLink.putValue("NAME", addHyperlink);
//        handleAddImageHyperLink.putValue("NAME", addImageHyperlink);
    }
    
    public  Action[]  getVWPContentModelActions(  ) {
        Action[] actions = new Action[] { handleAddCommandButton, handleAddCommandLink, handleAddImageHyperLink};
        return actions;
    }
    
    public PageContentItem item;
    public Action[] getVWPContentItemActions( PageContentItem item ) {
        Action openHandleAction = new OpenHandleAction(item);
        return new Action[]{new OpenHandleAction(item)};
    }
    
    /*PageContentModel Actions*/
    private final static String addButton = NbBundle.getMessage(VWPContentActions.class, "MSG_AddButton");
    private final static String addHyperlink = NbBundle.getMessage(VWPContentActions.class, "MSG_AddHyperlink");
    private final static String addImageHyperlink = NbBundle.getMessage(VWPContentActions.class, "MSG_AddImageHyperlink");
    
    /*PageContentItem Actions*/
    private final static String openHandler = NbBundle.getMessage(VWPContentActions.class, "MSG_OpenHandler");
    
    private  Action handleAddCommandButton = new HandleAddCommandButton();
    public final class HandleAddCommandButton extends AbstractAction {
        public HandleAddCommandButton(){
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_B,0));
            putValue(NAME, addButton);
        }
                
        public void actionPerformed(ActionEvent ev) { 
            
            getVwpContentModel().addPageBean(VWPContentUtilities.BUTTON);
        }
    };
    
    private Action handleAddCommandLink = new HandleAddCommandLink();
    public final class HandleAddCommandLink extends AbstractAction {
        public HandleAddCommandLink(){
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_L,0));
            putValue(NAME, addHyperlink);
        }
        public void actionPerformed(ActionEvent e) {
            getVwpContentModel().addPageBean(VWPContentUtilities.HYPERLINK);
        } 
    };
    
    private Action handleAddImageHyperLink = new HandleAddImageHyperLink();
    public final class HandleAddImageHyperLink extends AbstractAction {
        public HandleAddImageHyperLink(){            
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_I,0));
            putValue(NAME, addImageHyperlink);
        }
        public void actionPerformed(ActionEvent e) {
            getVwpContentModel().addPageBean(VWPContentUtilities.IMAGE_HYPERLINK);
        }
    };
    
    public final class OpenHandleAction extends AbstractAction {
        private final PageContentItem item;
        public OpenHandleAction(PageContentItem item){
            this.item = item;            
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O,0));
            putValue( NAME, openHandler);
        }                
        public void actionPerformed(ActionEvent ev) {
            getVwpContentModel().openPageHandler(item);
            
        }
    }

    private WeakReference<VWPContentModel> refVWPContentModel;
    private VWPContentModel getVwpContentModel() {
        VWPContentModel vwpContentModel = null;
        if ( refVWPContentModel != null ){
            vwpContentModel = refVWPContentModel.get();
        }
        return vwpContentModel;
    }

    private void setVwpContentModel(VWPContentModel vwpContentModel) {
        refVWPContentModel = new WeakReference<VWPContentModel>(vwpContentModel);
    }
    
}
