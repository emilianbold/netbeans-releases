/*
 * VWPContentModel.java
 *
 * Created on April 12, 2007, 9:26 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.visualweb.navigation;

import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignEvent;
import com.sun.rave.designtime.DesignProperty;
import java.awt.EventQueue;
import java.awt.Image;
import java.beans.BeanInfo;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.faces.component.ActionSource;
import javax.faces.component.ActionSource2;
import javax.swing.Action;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.visualweb.insync.Model;
import org.netbeans.modules.visualweb.insync.ModelSetListener;
import org.netbeans.modules.visualweb.insync.UndoEvent;
import org.netbeans.modules.visualweb.insync.faces.HtmlBean;
import org.netbeans.modules.visualweb.insync.live.MethodBindDesignEvent;
import org.netbeans.modules.visualweb.insync.live.MethodBindDesignProperty;
import org.netbeans.modules.visualweb.insync.models.FacesModel;
import org.netbeans.modules.visualweb.insync.models.FacesModelSet;
import org.netbeans.modules.visualweb.project.jsf.api.JsfProjectUtils;
import org.netbeans.modules.web.jsf.navigation.pagecontentmodel.PageContentItem;
import org.netbeans.modules.web.jsf.navigation.pagecontentmodel.PageContentModel;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;

/**
 *
 * @author joelle
 */
public class VWPContentModel extends PageContentModel{
    private FacesModel facesModel;
    private Collection<PageContentItem> pageContentItems = new ArrayList<PageContentItem>();
    private String pageName;
    
    /** Creates a new instance of VWPContentModel
     * @param facesModel can not be null
     * @param pageName can not be null
     */
    public VWPContentModel(FacesModel facesModel, String pageName) {
        this.facesModel = facesModel;
        this.pageName = pageName;
        updatePageContentItems();
        initListeners();
        
    }
    
    @Override
    protected void finalize() throws Throwable {
        destroy();
        super.finalize();
    }
    
    public void destroy() throws IOException {
        destroyListeners();
    }
    
    
    
    
    
    public VWPContentModel() {
    }
    
    
    public Collection<PageContentItem> getPageContentItems() {
        return pageContentItems;
    }
    
    public void addPageContentItem(PageContentItem pageContentItem) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void removePageContentItem(PageContentItem pageContentItem) {
        pageContentItems.remove(pageContentItem);
    }
    
    private FacesModelSetListener msl;
    public void initListeners() {
        
        if( msl == null ){
            msl = new FacesModelSetListener(this);
            facesModel.getOwner().addModelSetListener(msl);
            DesignBean designBean = facesModel.getRootBean();
            
        }
    }
    
    public void destroyListeners() {
        if ( msl != null ){
            facesModel.getFacesModelSet().removeModelSetListener(msl);
            msl = null;
        }
    }
    
    
    /**
     * Class which listens to DOM and project events
     */
    private class FacesModelSetListener implements ModelSetListener {
        final VWPContentModel vwpContentModel;
        
        public FacesModelSetListener( VWPContentModel vwpContentModel ) {
            this.vwpContentModel = vwpContentModel;
        }
        
        public void modelAdded(Model model) {
            //DO NOTHING
        }
        
        public void modelChanged(Model model) {
            if( model == facesModel ) {
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        updatePageContentItems();
                        vwpContentModel.handleModelChangeEvent();
                    }
                });
                
            }
        }
        
        public void modelRemoved(Model model) {
            //DO NOTHING
        }
        
        public void modelProjectChanged() {
            //DO NOTHING
        }
    }
    
    /**
     * Recursively locate all UICommand beans and add them to the given list
     * @ fill beans with the list of designBeans
     */
    private static void findCommandBeans(FacesModel model, DesignBean container, List<DesignBean> beans,  boolean includeFragments) {
        if(container == null)
            return;
        
        
        for ( DesignBean designBean : container.getChildBeans()) {
            
            // To be more general, check if instance of ActionSource and ActionSource2 instead of UICommand.
            // Check if it extends actionsSource and/or is hidden.  Don't add otherwise.
            if( designBean.getInstance() instanceof ActionSource || designBean.getInstance() instanceof ActionSource2 ) {
                /**** HACK, HACK, HACK *****
                 * DropDown is an instance of ActionSource but does not completely define the ActionSource interface.
                 * Unfortunatley there is not enough time to redo this component, so we are having to put a hack into
                 * navigator.  I hate doing this.  -Joelle
                 */
                if (designBean.getInstance().getClass().getName().equals("com.sun.rave.web.ui.component.DropDown")
                        || (designBean.getInstance().getClass().getName().equals("com.sun.webui.jsf.component.DropDown"))){
                    continue;
                }
                beans.add(designBean);
            }
            String className = designBean.getInstance() != null ?
                designBean.getInstance().getClass().getName() : "";
            if (includeFragments && className.equals(HtmlBean.PACKAGE+"Jsp_Directive_Include")) { // NOI18N
                // directive include -- look for referenced beans too in the fragment
                FacesModel fragmentModel = getFragmentModel(model, designBean);
                if (fragmentModel != null) {
                    findCommandBeans(fragmentModel, fragmentModel.getRootBean(), beans, true);
                }
            } else if (designBean.isContainer()) {
                findCommandBeans(model, designBean, beans, includeFragments);
            }
        }
    }
    
    
    
    private final static Image commandIcon = org.openide.util.Utilities.loadImage("com/sun/rave/navigation/command.gif"); // NOI18N
    //     private boolean updateBeans() {
    private boolean updatePageContentItems() {
        if (facesModel != null && !facesModel.isBusted()) {
            DesignBean container = facesModel.getRootBean();
            List<DesignBean> zoomedBeans = new ArrayList<DesignBean>();
            if (container != null) {
                findCommandBeans(facesModel, container, zoomedBeans, true);
            }
            
            pageContentItems.clear();
            //            p.setBeans(new ArrayList());
            // Create page beans structure
            for( DesignBean bean : zoomedBeans ) {
                String name = bean.getInstanceName();
                
                /* designContextName may reveal a sub-page or fragement*/
                String designContextName = bean.getDesignContext().getDisplayName();
                
                /* If the page name does not match the designContext page name, then prepend it to the NavigableComponent name. */
                //                int lastIndex = pageName.lastIndexOf('.');
                //                if( !pageName.substring(0,lastIndex).equals(designContextName)) {
                if( !pageName.equals(designContextName)) {
                    name = designContextName + ":" + name;
                }
                
                BeanInfo bi = bean.getBeanInfo();
                // XXX Find  a way to cache the image icon (repaint of icon slow)
                Image icon = bi != null ? bi.getIcon(BeanInfo.ICON_COLOR_16x16) : null;
                if (icon == null) {
                    // use backup image
                    icon = commandIcon;
                }
                String javaeePlatform = JsfProjectUtils.getJ2eePlatformVersion(getProject());
                DesignProperty pr;
                if ((javaeePlatform != null) && JsfProjectUtils.JAVA_EE_5.equals(javaeePlatform)){
                    pr = bean.getProperty("actionExpression"); // NOI18N
                }else{
                    pr = bean.getProperty("action"); // NOI18N
                }
                
                String action = pr != null ? pr.getValueSource() : "Unknown"; // NOI18N
                Object actionO = pr != null ? pr.getValue() : null;
                
                /* TODO: support actionRefs  !CQ MethodBinding
                if (action == null || action.length() == 0) {
                    // See if we have an action ref, and if so visually
                    // indicate that this component binds to a page
                    // that is decided on the fly / dynamically
                    //!CQ this will need to be integrated with the above since all actions are now refs
                }
                 */
                
                PageContentItem pageContentItem = new VWPContentItem(this, bean, name, action, icon);
                //               NavigableComponent b = new NavigableComponent(bean, action, p, name, icon);
                if (action != null && action.startsWith("#{")) { // Looks like value binding: dynamic navigation.
                    //COMEBACKTO - b.dynamic = true;
                    if (pr instanceof MethodBindDesignProperty) {
                        MethodBindDesignProperty mpr = (MethodBindDesignProperty)pr;
                        MethodBindDesignEvent mev = mpr.getEventReference();
                        if (mev != null) {
                            Object ret = mev.getHandlerMethodReturn();
                            if (ret instanceof String) {
                                pageContentItem.setFromOutcome((String)ret);
                                //                                b.setAction((String)ret);
                                //b.dynamic = false;  //!CQ TODO: show both icon & link
                            }
                        }
                    }
                }
                pageContentItems.add(pageContentItem);
                //                p.getBeans().add(b);T
                
            }
            return true;
        } else {
            return false;
            // TODO: add some kind of error badge to the GUI
        }
    }
    
    private static FacesModel getFragmentModel(FacesModel model, DesignBean fragment) {
        
        DesignProperty prop = fragment.getProperty("file"); // NOI18N
        if( prop == null ){
            return null;
        }
        Object fileO = prop.getValue();
        if (!(fileO instanceof String)) {
            return null;
        }
        String file = (String)fileO;
        if ((file == null) || (file.length() == 0)) {
            return null;
        }
        URL reference = model.getMarkupUnit().getBase();
        URL url = null;
        try {
            url = new URL(reference, file); // XXX what if it's absolute?
            if (url == null) {
                return null;
            }
        } catch (MalformedURLException e) {
            ErrorManager.getDefault().notify(e);
            return null;
        }
        
        Project project = model.getProject();
        FacesModelSet models = FacesModelSet.getInstance(project);
        if (models == null) {
            return null;
        }
        FileObject fo = URLMapper.findFileObject(url);
        if (fo != null) {
            FacesModel fragmentModel = models.getFacesModel(fo);
            if (fragmentModel != null) {
                return fragmentModel;
            }
        }
        return null;
    }
    
    private Project project = null;
    public Project getProject() {
        if( project == null ) {
            project = FileOwnerQuery.getOwner(facesModel.getFile());
        }
        return project;
    }
    
    /**
     * Add a navigation link from page "from" to page "to"
     * this used to be setOutcome
     * @param contentItem
     * @param caseAction
     * @param rename boolean
     */
    public void setCaseOutcome(VWPContentItem contentItem, String caseOutcome, boolean rename) {
        assert caseOutcome != null  && caseOutcome.length() > 0;
        
        DesignProperty addLinkToDP = null;
        DesignBean designBean = contentItem.getDesignBean();
        if( designBean != null) {
            FileOwnerQuery.getOwner(facesModel.getFile());
            String javaeePlatform = JsfProjectUtils.getJ2eePlatformVersion(getProject());
            if ((javaeePlatform != null) && JsfProjectUtils.JAVA_EE_5.equals(javaeePlatform)){
                addLinkToDP = designBean.getProperty("actionExpression"); // NOI18N
            }else{
                addLinkToDP = designBean.getProperty("action"); // NOI18N
            }
        }
        if (setCaseOutcome(contentItem, caseOutcome, addLinkToDP, rename)) {
            //            updatePageContentItems();
        }
    }
    
    /**
     * Add a navigation link from page "from" to page "to"
     * This used to be setOutcomeNoLayout
     */
    private boolean setCaseOutcome(VWPContentItem contentItem, String caseOutcome, DesignProperty addLinkToDP, boolean rename) {
        String oldCaseOutcome;
        oldCaseOutcome = contentItem.getFromOutcome();
        String javaeePlatform = null;
        
        
        if( getPageContentItems() != null && getPageContentItems().size() > 0 ) {
            //            updatePageContentItems();  //just incase the user had made changes
            UndoEvent undo = null;
            try {
                undo = facesModel.writeLock(null);
                
                // Are there any beans on the page referring to that
                // action? If so, update their action handlers too!
                for( PageContentItem pageContentItem : getPageContentItems() ){
                    if (pageContentItem instanceof VWPContentItem) {
                        DesignBean designBean = ((VWPContentItem)pageContentItem).getDesignBean();
                        if( designBean != null ) {
                            DesignProperty actionDP = getActionProperty(designBean);
                            if (actionDP != null) {
                                // dom't check equals if oldOutcome is null.
                                boolean setValueSource = (oldCaseOutcome != null && oldCaseOutcome.equals(actionDP.getValueSource()));
                                if (actionDP instanceof MethodBindDesignProperty) {
                                    MethodBindDesignProperty mpr = (MethodBindDesignProperty) actionDP;
                                    MethodBindDesignEvent mev = mpr.getEventReference();
                                    if (mev != null) {
                                        boolean modify = false;
                                        // If rename, set the  value of all the beans action property to new outcome,
                                        // if the action property has current value equal to outcome
                                        if(rename){
                                            if((oldCaseOutcome != null) && oldCaseOutcome.equals(mev.getHandlerMethodReturn())){
                                                modify = true;
                                            }
                                        }else{ // Modify only the current bean
                                            if (addLinkToDP == actionDP) {
                                                modify = true;
                                            }
                                        }
                                        //Does this modify the java method?
                                        if (modify){
                                            if (mev.getHandlerName() == null) {
                                                setValueSource = true;
                                            } else {
                                                //When link is created for the first time,
                                                //oldOutcome and outcome are same
                                                //But what if a link was never there and there is already an assigned return case
                                                if(oldCaseOutcome != null && oldCaseOutcome.equals(caseOutcome))  {
                                                    if (mev.getHandlerMethodReturn() != null ){
                                                        oldCaseOutcome = mev.getHandlerMethodReturn().toString();
                                                    } else {
                                                        oldCaseOutcome = null;
                                                    }
                                                }
                                                mev.updateReturnStrings(oldCaseOutcome, caseOutcome);
                                                setValueSource = false;
                                            }
                                        }
                                    }
                                }
                                //Do this simply modify the jsp property?
                                if (setValueSource) {
                                    actionDP.setValueSource(caseOutcome);
                                }
                            }
                        }
                        
                    }
                    
                }
            }finally {
                facesModel.writeUnlock(undo);
                addLinkToDP = null;
            }
        }
        return true;
    }
    
    private DesignProperty getActionProperty( DesignBean designBean ) {
        String javaeePlatform;
        javaeePlatform = JsfProjectUtils.getJ2eePlatformVersion(getProject());
        DesignProperty pr;
        if ((javaeePlatform != null) && JsfProjectUtils.JAVA_EE_5.equals(javaeePlatform)){
            pr = designBean.getProperty("actionExpression"); // NOI18N
            if(pr!= null ){
                DesignEvent event = facesModel.getDefaultEvent(designBean);
                if (event != null) {
                    facesModel.createEventHandler(event);
                }
            }
        }else{
            pr = designBean.getProperty("action"); // NOI18N
        }
        return pr;
    }
    
    public VWPContentActions actions;
    public Action[] getActions() {
        if( actions == null ){
            actions = new VWPContentActions(this);
        }
        return actions.getVWPContentModelActions();
    }
    
    public VWPContentActions getActionsFactory(){
        if( actions == null ){
            actions = new VWPContentActions(this);
        }
        return actions;
    }
    
    public  PageContentItem addPageBean(int type) {
        
        String javaeePlatform = JsfProjectUtils.getJ2eePlatformVersion(facesModel.getProject());
        DesignBean designBean = addComponent("createComponent", VWPContentUtilities.getBeanClassName(javaeePlatform,type));
        
        PageContentItem item = solveNavComponent(designBean);
        
        return item;
    }
    
    
    private PageContentItem solveNavComponent(DesignBean designBean){
        if( designBean == null || pageName == null ) {
            return null;
        }
        
        //To figure out navigable component.
        String name = designBean.getInstanceName();
        
        /* designContextName may reveal a sub-page or fragement*/
        String designContextName = designBean.getDesignContext().getDisplayName();
        
        /* If the page name does not match the designContext page name, then prepend it to the NavigableComponent name. */
        
        if( !pageName.equals(designContextName)) {
            name = designContextName + ":" + name;
        }
        
        BeanInfo bi = designBean.getBeanInfo();
        // XXX Find  a way to cache the image icon (repaint of icon slow)
        Image icon = bi != null ? bi.getIcon(BeanInfo.ICON_COLOR_16x16) : null;
        if (icon == null) {
            // use backup image
            icon = commandIcon;
        }
        String javaeePlatform = JsfProjectUtils.getJ2eePlatformVersion(getProject());
        DesignProperty pr;
        if ((javaeePlatform != null) && JsfProjectUtils.JAVA_EE_5.equals(javaeePlatform)){
            pr = designBean.getProperty("actionExpression"); // NOI18N
        }else{
            pr = designBean.getProperty("action"); // NOI18N
        }
        
        String action = pr != null ? pr.getValueSource() : "Unknown"; // NOI18N
        
        PageContentItem item = new VWPContentItem(this, designBean, name, action, icon);
        //        NavigableComponent navComp = new NavigableComponent(designBean, action, page, name, icon);
        return item;
        
    }
    
    public void deleteCaseOutcome(VWPContentItem item ){
        UndoEvent undo = null;
        //        DesignBean designBean = item.getDesignBean();
        String fromOutcome = item.getFromOutcome();
        DesignBean container = facesModel.getRootBean();
        List<DesignBean> beans = new ArrayList<DesignBean>();
        findCommandBeans( facesModel, container, beans, false);
        try {
            for( DesignBean designBean : beans ){
                undo = facesModel.writeLock(null);  //!CQ TODO: nice description
                DesignProperty pr = null;
                String javaeePlatform = JsfProjectUtils.getJ2eePlatformVersion(getProject());
                if ((javaeePlatform != null) && JsfProjectUtils.JAVA_EE_5.equals(javaeePlatform)){
                    pr = designBean.getProperty("actionExpression");
                }else{
                    pr = designBean.getProperty("action");
                }
                //                        DesignProperty pr = designBean.getProperty("action"); // NOI18N
                if (pr != null && fromOutcome.equals(pr.getValueSource())) {
                    // Yes, this action bound to this port
                    pr.unset(); // means "reset" despite the name
                }
                //update the java source
                if (pr instanceof MethodBindDesignProperty) {
                    MethodBindDesignProperty mpr = (MethodBindDesignProperty) pr;
                    MethodBindDesignEvent mev = mpr.getEventReference();
                    if (mev != null && mev.getHandlerName() != null) {
                        mev.updateReturnStrings(fromOutcome, null);
                    }
                }
            }
        } finally {
            facesModel.writeUnlock(undo);
        }
    }
    
    
    
    private DesignBean addComponent(String lockDesc, String className) {
        UndoEvent undo = null;
        DesignBean bean = null;
        try {
            undo = facesModel.writeLock(lockDesc);
            bean = facesModel.getLiveUnit().createBean(className, null, null);
            if (bean == null) {
                return bean;
            }
            facesModel.beanCreated(bean);
        } catch (Exception e) {
            ErrorManager.getDefault().notify(e);
        } finally {
            facesModel.writeUnlock(undo);
        }
        facesModel.flush();
        return bean;
        
    }
    
    public void openPageHandler( PageContentItem item ){
        if( item instanceof VWPContentItem ){
            VWPContentItem vwpItem = (VWPContentItem) item;
            facesModel.openDefaultHandler(vwpItem.getDesignBean());
        }
    }
    
    
    
}
