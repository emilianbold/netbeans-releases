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
