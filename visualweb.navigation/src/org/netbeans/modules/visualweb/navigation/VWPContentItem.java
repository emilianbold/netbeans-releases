/*
 * VWPContentItem.java
 *
 * Created on April 13, 2007, 2:12 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.visualweb.navigation;

import com.sun.rave.designtime.DesignBean;
import java.awt.Image;
import java.lang.ref.WeakReference;
import javax.swing.Action;
import org.netbeans.modules.web.jsf.navigation.pagecontentmodel.PageContentItem;
import org.openide.cookies.OpenCookie;
import org.openide.nodes.Node.Cookie;

/**
 *
 * @author joelle
 */
public class VWPContentItem extends PageContentItem {

    DesignBean designBean;

//    /** Creates a new instance of VWPContentItem */
//    public VWPContentItem( VWPContentModel model, DesignBean bean, String name, String fromString, Image icon, boolean isOutcome )  {
//        super(name, fromString, icon, isOutcome);
//        this.designBean = bean;
//        this.model = model;
//    }
    public VWPContentItem(VWPContentModel model, DesignBean bean, String name, String fromOutcome, Image icon) {
        super(name, fromOutcome, icon);
        assert bean != null;
        assert model != null;

        this.designBean = bean;
        setModel( model );
    }


    @Override
    public void setFromAction(String fromAction) {
//        model.setCaseAction(this, fromOutcome, false);
        super.setFromAction(fromAction);
    }

    @Override
    public void setFromOutcome(String fromOutcome) {
        if (fromOutcome == null) {
            getModel().deleteCaseOutcome(this);
        } else {
            getModel().setCaseOutcome(this, fromOutcome, false);
        }
        super.setFromOutcome(fromOutcome);
    }



    public DesignBean getDesignBean() {
        return designBean;
    }

    private Action[] actions;

    @Override
    public Action[] getActions() {
        if (actions == null) {
            actions = getModel().getActionsFactory().getVWPContentItemActions(this);
        }
        return actions;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Cookie> T getCookie(Class<T> type) {
        final PageContentItem item = this;
        if (type.equals(OpenCookie.class)) {
            return (T) new OpenCookie() {
                public void open() {
                    getModel().openPageHandler(item);
                }
            };
        } 
        return super.getCookie(type);
    }

    private WeakReference<VWPContentModel> refVWPContentModel;
    private VWPContentModel getModel() {
        VWPContentModel model = null;
        if( refVWPContentModel != null ) {
            model = refVWPContentModel.get();
        }
        return model;
    }

    private void setModel(VWPContentModel model) {
        this.refVWPContentModel = new WeakReference<VWPContentModel>( model );
    }
}
