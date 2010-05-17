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

import org.netbeans.modules.visualweb.api.designer.Designer;
import com.sun.rave.designtime.DesignProperty;
import java.util.ArrayList;
import org.netbeans.modules.visualweb.api.designer.DomProvider;
import org.netbeans.modules.visualweb.api.designer.DomProviderService;
import org.netbeans.modules.visualweb.api.designer.markup.MarkupService;
import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.markup.MarkupDesignBean;
import org.netbeans.modules.visualweb.insync.Util;
import org.netbeans.modules.visualweb.insync.beans.BeansUnit;
import org.netbeans.modules.visualweb.insync.faces.FacesBean;
import org.netbeans.modules.visualweb.insync.faces.FacesPageUnit;
import org.netbeans.modules.visualweb.insync.faces.HtmlBean;
import org.netbeans.modules.visualweb.insync.live.LiveUnit;
import org.netbeans.modules.visualweb.insync.markup.MarkupUnit;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.net.URL;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.Project;
import org.netbeans.modules.visualweb.api.designer.cssengine.CssProvider;
import org.netbeans.modules.visualweb.designer.jsf.virtualforms.ComponentGroupSupport;
import org.netbeans.modules.visualweb.spi.designer.Decoration;
import org.netbeans.spi.palette.PaletteController;
import org.openide.windows.TopComponent;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Implementation of <code>DomProvider</code>
 *
 * @author Peter Zavadsky
 */
class DomProviderImpl implements DomProvider {

    private final JsfForm jsfForm;

    private static final DomProviderService domProviderService = new DomProviderServiceImpl();

    /** Creates a new instance of DomProviderImpl */
    public DomProviderImpl(JsfForm jsfForm) {
        this.jsfForm = jsfForm;
    }

    public JsfForm getJsfForm() {
        return jsfForm;
    }

//    public void addDomProviderListener(DomProvider.DomProviderListener l) {
//        jsfForm.addDomProviderListener(l);
//    }
//
//    public void removeDomProviderListener(DomProvider.DomProviderListener l) {
//        jsfForm.removeDomProviderListener(l);
//    }

    public Document getHtmlDom() {
//        return getFacesModel().getHtmlDom();
        return jsfForm.getHtmlDom();
    }

//    public DocumentFragment getHtmlDocumentFragment() {
////        return getFacesModel().getHtmlDomFragment();
//        return jsfForm.getHtmlDomFragment();
//    }

    public Element getHtmlBody() {
//        return getFacesModel().getHtmlBody();
        return jsfForm.getHtmlBody();
    }

    public PaletteController getPaletteController() {
        return jsfForm.getPaletteController();
    }

//    public void requestRefresh() {
//        jsfForm.requestRefresh();
//    }

//    public void refreshModel(boolean deep) {
//        jsfForm.refreshModel(deep);
//    }

//    public void setUpdatesSuspended(MarkupDesignBean markupDesignBean, boolean suspend) {

//    public void setUpdatesSuspended(MarkupDesignBean markupDesignBean, boolean suspend) {

//    public void setUpdatesSuspended(MarkupDesignBean markupDesignBean, boolean suspend) {

//    public void setUpdatesSuspended(MarkupDesignBean markupDesignBean, boolean suspend) {
//        jsfForm.setUpdatesSuspended(markupDesignBean, suspend);
//    }

//    public boolean isRefreshPending() {
//        return jsfForm.isRefreshPending();
//    }

//    public void attachContext() {
//        DesignContext context = getFacesModel().getLiveUnit();
//        if (context != null) {
//            jsfForm.attachContext(context);
//        }
//    }

    /*public*/
    /*public*/
    /*public*/
    /*public*/
    /*public*/
    /*public*/
    /*public*/
    /*public*/ DocumentFragment createSourceFragment(MarkupDesignBean bean) {
        return jsfForm.createSourceFragment(bean);
    }

    /*public*/
    /*public*/
    /*public*/
    /*public*/
    /*public*/
    /*public*/
    /*public*/
    /*public*/ void requestChange(MarkupDesignBean bean) {
        jsfForm.requestChange(bean);
    }

    /*public*/
    /*public*/
    /*public*/
    /*public*/
    /*public*/
    /*public*/
    /*public*/
    /*public*/ void beanChanged(MarkupDesignBean bean) {
        jsfForm.beanChanged(bean);
    }

    /*public*/
    /*public*/
    /*public*/
    /*public*/
    /*public*/
    /*public*/
    /*public*/
    /*public*/ void requestTextUpdate(MarkupDesignBean bean) {
        jsfForm.requestTextUpdate(bean);
    }

//    public DataFlavor getImportFlavor(DataFlavor[] flavors) {
//        return getDndSupport().getImportFlavor(flavors);
//    }

    public boolean canImport(JComponent comp, DataFlavor[] transferFlavors, Transferable transferable) {
//        return getFacesModel().getDnDSupport().canImport(comp, transferFlavors);
        return getDndSupport().canImport(comp, transferFlavors, transferable);
    }

//    public DesignBean[] pasteBeans(Transferable t, DesignBean parent, MarkupPosition pos, Point location, DomProvider.CoordinateTranslator coordinateTranslator) {
//        return getDndSupport().pasteBeans(t, parent, pos, location, coordinateTranslator, jsfForm.getUpdateSuspender());
//    }
    // XXX Moved to FacesDnDSupport
//    private /*public*/ Element[] pasteComponents(Transferable t, Element parentComponentRootElement, Point location) {
//        MarkupDesignBean parent = MarkupUnit.getMarkupDesignBeanForElement(parentComponentRootElement);
//        DesignBean[] designBeans = getDndSupport().pasteBeans(t, parent, null, location, jsfForm.getUpdateSuspender());
//        
//        if (designBeans == null) {
//            return new Element[0];
//        }
//        
//        List<Element> elements = new ArrayList<Element>();
//        for (DesignBean designBean : designBeans) {
//            if (designBean instanceof MarkupDesignBean) {
//                Element element = getComponentRootElementForMarkupDesignBean((MarkupDesignBean)designBean);
//                if (element != null) {
//                    elements.add(element);
//                }
//            }
//        }
//        return elements.toArray(new Element[elements.size()]);
//    }

//    public void importData(JComponent comp, Transferable t, Object transferData, Dimension dimension,

//    public void importData(JComponent comp, Transferable t, Object transferData, Dimension dimension,

//    public void importData(JComponent comp, Transferable t, Object transferData, Dimension dimension,

//    public void importData(JComponent comp, Transferable t, Object transferData, Dimension dimension,
//            DomProvider.Location location, DomProvider.CoordinateTranslator coordinateTranslator, int dropAction) {
//        getDndSupport().importData(comp, t, transferData, dimension, location, coordinateTranslator, dropAction);
//    }
    
//    public void importString(String string, DomProvider.Location location, DomProvider.CoordinateTranslator coordinateTranslator) {
//        getDndSupport().importString(string, location, coordinateTranslator);
//    }

//    public String[] getClassNames(DisplayItem[] displayItems) {
//        return getDndSupport().getClassNames(displayItems);
//    }

//    public boolean importBean(DisplayItem[] items, DesignBean origParent, int nodePos, String facet, List createdBeans, DomProvider.Location location, DomProvider.CoordinateTranslator coordinateTranslator) throws IOException {
//        return getDndSupport().importBean(items, origParent, nodePos, facet, createdBeans, location, coordinateTranslator);
//    }

//    public MarkupPosition getDefaultPositionUnderParent(DesignBean parent) {
//        return getDndSupport().getDefaultMarkupPositionUnderParent(parent);
//    }

//    public int computeActions(DesignBean droppee, Transferable transferable, boolean searchUp, int nodePos) {
    public int computeActions(Element dropeeComponentRootElement, Transferable transferable) {
//        MarkupDesignBean droppee = MarkupUnit.getMarkupDesignBeanForElement(dropeeComponentRootElement);
//        return getDndSupport().computeActions(droppee, transferable);
        return jsfForm.computeActions(dropeeComponentRootElement, transferable);
    }

//    public DesignBean findParent(String className, DesignBean droppee, Node parentNode, boolean searchUp) {

//    public DesignBean findParent(String className, DesignBean droppee, Node parentNode, boolean searchUp) {

//    public DesignBean findParent(String className, DesignBean droppee, Node parentNode, boolean searchUp) {

//    public DesignBean findParent(String className, DesignBean droppee, Node parentNode, boolean searchUp) {
//        return Util.findParent(className, droppee, parentNode, searchUp, getFacesModel());
//    }

    public int processLinks(Element origElement, Element componentRootElement) {
        MarkupDesignBean markupDesignBean = MarkupUnit.getMarkupDesignBeanForElement(componentRootElement);
        List<DesignBean> beans = new ArrayList<DesignBean>();
        if (markupDesignBean != null) {
            beans.add(markupDesignBean);
        }
        return getDndSupport().processLinks(origElement, beans);
    }

//    public boolean setDesignProperty(DesignBean bean, String attribute, int length) {

//    public boolean setDesignProperty(DesignBean bean, String attribute, int length) {

//    public boolean setDesignProperty(DesignBean bean, String attribute, int length) {

//    public boolean setDesignProperty(DesignBean bean, String attribute, int length) {
//        return Util.setDesignProperty(bean, attribute, length);
//    }

//    public boolean isBraveheartPage() {
//        return Util.isBraveheartPage(jsfForm.getJspDom());
//    }
//
//    public boolean isWoodstockPage() {
//        return Util.isWoodstockPage(jsfForm.getJspDom());
//    }

//    private /*public*/ FacesModel getFacesModel() {
//        return jsfForm.getFacesModel();
//    }
    
    private DndSupport getDndSupport() {
        return jsfForm.getDndSupport();
    }

//    public boolean isFragment() {
//        return jsfForm.isFragment();
//    }
//
//    public boolean isPortlet() {
//        return jsfForm.isPortlet();
//    }

//    public DataObject getJspDataObject() {
//////        FileObject file = getFacesModel().getMarkupFile();
////        FileObject file = jsfForm.getMarkupFile();
////
////        try {
////            return DataObject.find(file);
////        } catch (DataObjectNotFoundException ex) {
////            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
////
////            return null;
////        }
//        return jsfForm.getJspDataObject();
//    }

    public URL getBaseUrl() {
//        MarkupUnit markupUnit = getFacesModel().getMarkupUnit();
        MarkupUnit markupUnit = jsfForm.getMarkupUnit();
        if (markupUnit == null) {
            // #6457856 NPE
            return null;
        }
        return markupUnit.getBase();
    }

    public URL resolveUrl(String urlString) {
        return Util.resolveUrl(getBaseUrl(), jsfForm.getJspDom(), urlString);
    }

    /*public*/
    /*public*/
    /*public*/
    /*public*/
    /*public*/
    /*public*/
    /*public*/
    /*public*/ DocumentFragment renderHtmlForMarkupDesignBean(MarkupDesignBean markupDesignBean) {
//        return FacesPageUnit.renderHtml(getFacesModel(), markupDesignBean);
        return jsfForm.renderMarkupDesignBean(markupDesignBean);
    }

//    public Document getJspDom() {
//        return getFacesModel().getJspDom();
//    }

//    public void clearHtml() {
//        getFacesModel().clearHtml();
//    }

//    public List<FileObject> getWebPageFileObjectsInThisProject() {
//        return Util.getWebPages(getFacesModel().getProject(), true, false);
//    }

////    public boolean editEventHandlerForDesignBean(DesignBean designBean) {
//    public boolean editEventHandlerForComponent(Element componentRootElement) {
////        DesignBean designBean = MarkupUnit.getMarkupDesignBeanForElement(componentRootElement);
////        if (designBean == null) {
//////            webform.getModel().openDefaultHandler(component);
////            getFacesModel().openDefaultHandler();
////            return false;
////        } else {
////            // See if it's an XHTML element; if so just show it in
////            // the JSP source
//////            if (FacesSupport.isXhtmlComponent(component)) {
////            if (isXhtmlComponent(designBean)) {
//////                MarkupBean mb = FacesSupport.getMarkupBean(component);
////                MarkupBean mb = Util.getMarkupBean(designBean);
////                
//////                MarkupUnit unit = webform.getMarkup();
////                MarkupUnit unit = getFacesModel().getMarkupUnit();
////                // <markup_separation>
//////                Util.show(null, unit.getFileObject(),
//////                    unit.computeLine((RaveElement)mb.getElement()), 0, true);
////                // ====
//////                MarkupService.show(unit.getFileObject(), unit.computeLine((RaveElement)mb.getElement()), 0, true);
////                showLineAt(unit.getFileObject(), unit.computeLine(mb.getElement()), 0);
////                // </markup_separation>
////            } else {
//////                webform.getModel().openDefaultHandler(component);
////                getFacesModel().openDefaultHandler(designBean);
////            }
////
////            return true;
////        }
//        return jsfForm.editEventHandlerForComponent(componentRootElement);
//    }

//    public boolean canDropDesignBeansAtNode(DesignBean[] designBeans, Node node) {
    public boolean canDropComponentsAtNode(Element[] componentRootElements, Node node) {
        DesignBean parent = null;
        while (node != null) {
//            if (curr instanceof RaveElement) {
//                parent = ((RaveElement)curr).getDesignBean();
            if (node instanceof Element) {
//                parent = InSyncService.getProvider().getMarkupDesignBeanForElement((Element)curr);
//                parent = WebForm.getDomProviderService().getMarkupDesignBeanForElement((Element)curr);
                parent = MarkupUnit.getMarkupDesignBeanForElement((Element)node);

                if (parent != null) {
                    break;
                }
            }

            node = node.getParentNode();
        }

        if (parent == null) {
            return true;
        }

        // See if ALL the beans being dragged can be dropped here
//        LiveUnit unit = webform.getModel().getLiveUnit();
//        LiveUnit unit = getFacesModel().getLiveUnit();
        LiveUnit unit = jsfForm.getLiveUnit();

        List<DesignBean> beans = new ArrayList<DesignBean>();
        for (Element componentRootElement : componentRootElements) {
            MarkupDesignBean markupDesignBean = MarkupUnit.getMarkupDesignBeanForElement(componentRootElement);
            if (markupDesignBean != null) {
                beans.add(markupDesignBean);
            }
        }
        DesignBean[] designBeans = beans.toArray(new DesignBean[beans.size()]);
        
//        for (int i = 0, n = beans.size(); i < n; i++) {
//            DesignBean bean = (DesignBean)beans.get(i);
        for (DesignBean bean : designBeans) {
            String className = bean.getInstance().getClass().getName();

            if (!unit.canCreateBean(className, parent, null)) {
                return false;
            }

            // Ensure that we're not trying to drop a html bean on a
            // renders-children parent
            boolean isHtmlBean = className.startsWith(HtmlBean.PACKAGE);

            if (isHtmlBean) {
                // We can't drop anywhere below a "renders children" JSF
                // component
//                if (parent != FacesSupport.findHtmlContainer(webform, parent)) {
//                if (parent != webform.findHtmlContainer(parent)) {
                if (parent != Util.findHtmlContainer(parent)) {
                    return false;
                }
            }
        }

        return true;
    }

//    public boolean handleMouseClickForElement(Element element, int clickCount) {
//        MarkupMouseRegion region = findRegion(element);
//
//        if ((region != null) && region.isClickable()) {
//            Result r = region.regionClicked(clickCount);
////            ResultHandler.handleResult(r, getFacesModel());
//            jsfForm.handleResult(r);
//            // #6353410 If there was performed click on the region
//            // then do not perform other actions on the same click.
//            return true;
//        }
//        return false;
//    }

//    public boolean isNormalAndHasFacesBean(MarkupDesignBean markupDesignBean) {

//    public boolean isNormalAndHasFacesBean(MarkupDesignBean markupDesignBean) {

//    public boolean isNormalAndHasFacesBean(MarkupDesignBean markupDesignBean) {

//    public boolean isNormalAndHasFacesBean(MarkupDesignBean markupDesignBean) {
//    public boolean isNormalAndHasFacesComponent(Element componentRootElement) {
//        MarkupDesignBean markupDesignBean = MarkupUnit.getMarkupDesignBeanForElement(componentRootElement);
//        if (markupDesignBean != null) {
//            FacesBean fb = Util.getFacesBean(markupDesignBean);
//            if ((fb != null) && !Util.isSpecialBean(markupDesignBean)) {
//                return true;
//            }
//        }
//        return false;
//    }

    private /*public*/ boolean canHighlightMarkupDesignBean(MarkupDesignBean markupDesignBean) {
        if (markupDesignBean == null) {
            return false;
        }
//        FacesPageUnit facesUnit = getFacesModel().getFacesUnit();
        FacesPageUnit facesUnit = jsfForm.getFacesPageUnit();
        if ((facesUnit == null)
        || ((facesUnit.getDefaultParent() != Util.getMarkupBean(markupDesignBean))
            && (markupDesignBean.getElement() != MarkupService.getSourceElementForElement(getHtmlBody())))
        ) {
            return true;
        }
        return false;
    }
    
    public boolean canHighlightComponentRootElmenet(Element componentRootElement) {
        if (componentRootElement == null) {
            return false;
        }
        MarkupDesignBean markupDesignBean = MarkupUnit.getMarkupDesignBeanForElement(componentRootElement);
        return canHighlightMarkupDesignBean(markupDesignBean);
    }

    
    // XXX Copy also in insync/FacesDnDSupport.

    
    // XXX Copy also in insync/FacesDnDSupport.

    
    // XXX Copy also in insync/FacesDnDSupport.

    
//    // XXX Copy also in insync/FacesDnDSupport.
//    /*public*/ private DesignBean createBean(String className, Node parent, Node before) {
//        MarkupPosition pos = new MarkupPosition(parent, before);
//        DesignBean parentBean = /*FacesSupport.*/Util.findParentBean(parent);
//        LiveUnit unit = getFacesModel().getLiveUnit();
//        return unit.createBean(className, parentBean, pos);
//    }
    
//    // XXX Get rid of too.
//    public Element createComponent(String className, Node parent, Node before) {
//        DesignBean designBean = createBean(className, parent, before);
//        return designBean instanceof MarkupDesignBean ? ((MarkupDesignBean)designBean).getElement() : null;
//    }

//    public boolean isFormBean(DesignBean designBean) {

//    public boolean isFormBean(DesignBean designBean) {

//    public boolean isFormBean(DesignBean designBean) {

//    public boolean isFormBean(DesignBean designBean) {
//        return Util.isFormBean(getFacesModel(), designBean);
//    }

//    public Element getDefaultParentMarkupBeanElement() {
//        FacesPageUnit unit = getFacesModel().getFacesUnit();
//        if (unit != null) {
//            MarkupBean mb = unit.getDefaultParent();
////            RaveElement element = (RaveElement)mb.getElement();
//            return mb.getElement();
//        }
//        return null;
//    }

//    public boolean moveBean(DesignBean bean, Node parentNode, Node before) {
//        LiveUnit lu = getFacesModel().getLiveUnit();
//        MarkupPosition markupPos = new MarkupPosition(parentNode, before);
//        DesignBean parentBean = null;
//        Node e = parentNode;
//
//        while (e != null) {
////            if (e instanceof RaveElement) {
////                parentBean = ((RaveElement)e).getDesignBean();
//            if (e instanceof Element) {
////                parentBean = InSyncService.getProvider().getMarkupDesignBeanForElement((Element)e);
//                parentBean = MarkupUnit.getMarkupDesignBeanForElement((Element)e);
//                
//                if (parentBean != null) {
//                    break;
//                }
//            }
//
//            e = e.getParentNode();
//        }
//
//        if (bean == parentBean) {
//            return false;
//        }
//
//        return lu.moveBean(bean, parentBean, markupPos);
//    }

    /*public*/ boolean setPrerenderedBean(MarkupDesignBean markupDesignBean, DocumentFragment documentFragment) {
//        LiveUnit lu = getFacesModel().getLiveUnit();
        LiveUnit lu = jsfForm.getLiveUnit();
        BeansUnit bu = lu.getBeansUnit();

        FacesPageUnit facesPageUnit;
        if (bu instanceof FacesPageUnit) {
            facesPageUnit = (FacesPageUnit)bu;
        } else {
            return false;
        }

        facesPageUnit.setPreRendered(markupDesignBean, documentFragment);
        return true;
    }

//    public org.openide.nodes.Node getRootBeanNode() {
////        FacesModel facesModel = getFacesModel();
////        DesignBean rootBean = facesModel.getRootBean();
////        if (rootBean == null) {
////            // XXX If the model is busted then it is supposed to be OK, there is an error, see e.g. #6478860.
////            if (!facesModel.isBusted()) {
////                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
////                        new IllegalStateException("Invalid FacesModel, it is not busted and its root design bean is null, facesModel=" + facesModel)); // NOI18N
////            }
////            return null;
////        } else {
////            return DesigntimeIdeBridgeProvider.getDefault().getNodeRepresentation(rootBean);
////        }
//        return jsfForm.getRootBeanNode();
//    }

//    public void deleteBean(DesignBean designBean) {

//    public void deleteBean(DesignBean designBean) {

//    public void deleteBean(DesignBean designBean) {

//    public void deleteBean(DesignBean designBean) {
//    public void deleteComponent(Element componentRootElement) {
//        DesignBean designBean = MarkupUnit.getMarkupDesignBeanForElement(componentRootElement);
//        if (designBean == null) {
//            return;
//        }
//        getFacesModel().getLiveUnit().deleteBean(designBean);
//    }

//    // XXX TODO Get rid of this.

//    // XXX TODO Get rid of this.

//    // XXX TODO Get rid of this.

//    // XXX TODO Get rid of this.
//    public DesignBean getDefaultParentBean() {
//        LiveUnit liveUnit = getFacesModel().getLiveUnit();
//        if (liveUnit != null) {
//            MarkupBean bean = getFacesModel().getFacesUnit().getDefaultParent();
//
//            if (bean != null) {
//                return liveUnit.getDesignBean(bean);
//            }
//        }
//
//        return null;
//    }
//    // XXX Replacing the above
//    public Element getDefaultParentComponent() {
//////        LiveUnit liveUnit = getFacesModel().getLiveUnit();
////        LiveUnit liveUnit = jsfForm.getLiveUnit();
////        if (liveUnit != null) {
//////            MarkupBean bean = getFacesModel().getFacesUnit().getDefaultParent();
////            MarkupBean bean = jsfForm.getFacesPageUnit().getDefaultParent();
////
////            if (bean != null) {
////                DesignBean designBean = liveUnit.getDesignBean(bean);
////                return designBean instanceof MarkupDesignBean ? getComponentRootElementForMarkupDesignBean((MarkupDesignBean)designBean) : null;
////            }
////        }
////
////        return null;
//        return jsfForm.getDefaultParentComponent();
//    }

//    private /*public*/ Exception getRenderFailure() {
//        FacesPageUnit facesPageUnit = getFacesModel().getFacesUnit();
//        if (facesPageUnit == null) {
//            return null;
//        }
//        return facesPageUnit.getRenderFailure();
//    }
//
//    private /*public*/ MarkupDesignBean getRenderFailureMarkupDesignBean() {
//        FacesPageUnit facesPageUnit = getFacesModel().getFacesUnit();
//        if (facesPageUnit == null) {
//            return null;
//        }
//        DesignBean designBean = facesPageUnit.getRenderFailureComponent();
//        if (designBean instanceof MarkupDesignBean) {
//            return (MarkupDesignBean)designBean;
//        } else {
//            return null;
//        }
//    }

//    public void setRenderFailedValues(MarkupDesignBean renderFailureComponent, Exception renderFailureException) {

//    public void setRenderFailedValues(MarkupDesignBean renderFailureComponent, Exception renderFailureException) {

//    public void setRenderFailedValues(MarkupDesignBean renderFailureComponent, Exception renderFailureException) {

//    public void setRenderFailedValues(MarkupDesignBean renderFailureComponent, Exception renderFailureException) {
//        jsfForm.setRenderFailureValues(renderFailureComponent, renderFailureException);
//    }
    
//    public void setRenderFailureValues() {
//        Exception failure = getRenderFailure();
//        MarkupDesignBean renderFailureComponent = getRenderFailureMarkupDesignBean();
//        jsfForm.setRenderFailureValues(renderFailureComponent, failure);
//    }
    
//    public boolean hasRenderFailure() {
//        return jsfForm.getRenderFailureException() != null;
//    }

    // >>> XXX RenderFailureProvider >>>

    // >>> XXX RenderFailureProvider >>>

    // >>> XXX RenderFailureProvider >>>

    // >>> XXX RenderFailureProvider >>>
//    public Exception getRenderFailureException() {
//        return jsfForm.getRenderFailureException();
//    }

//    public MarkupDesignBean getRenderFailureComponent() {
//        return jsfForm.getRenderFailureComponent();
//    }
    // >>> XXX RenderFailureProvider <<<
    // >>> XXX RenderFailureProvider <<<
    // >>> XXX RenderFailureProvider <<<
    // >>> XXX RenderFailureProvider <<<
    
//    public boolean hasRenderingErrors() {
//        return jsfForm.getRenderFailureComponent() != null;
//    }

//    public void syncModel() {
//        getFacesModel().sync();
//    }
    
//    public DomProvider.ErrorPanel getErrorPanel(DomProvider.ErrorPanelCallback errorPanelCallback) {
//        FacesModel facesModel = getFacesModel();
//        if (facesModel.isBusted()) {
//            return new ErrorPanelImpl(facesModel, facesModel.getErrors(), errorPanelCallback);
//        } else {
//            return new RenderErrorPanelImpl(facesModel, errorPanelCallback, this);
//        }
//    }


//    public boolean isSourceDirty() {
//        MarkupUnit markupUnit = getFacesModel().getMarkupUnit();
//        if (markupUnit != null) {
//            return markupUnit.getState() == Unit.State.SOURCEDIRTY;
//        } else {
//            // XXX #6478973 Model could be corrupted, until #6480764 is fixed.
//            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
//                    new IllegalStateException("The FacesModel is corrupted, its markup unit is null, facesModel=" + getFacesModel())); // NOI18N
//        }
//        return false;
//    }
    
//    public Transferable copyBeans(DesignBean[] beans) {
    
//    public Transferable copyBeans(DesignBean[] beans) {
    
//    public Transferable copyBeans(DesignBean[] beans) {
    
//    public Transferable copyBeans(DesignBean[] beans) {
//    public Transferable copyComponents(Element[] componentRootElements) {
//        List<DesignBean> beans = new ArrayList<DesignBean>();
//        for (Element componentRootElement : componentRootElements) {
//            DesignBean bean = MarkupUnit.getMarkupDesignBeanForElement(componentRootElement);
//            if (bean != null) {
//                beans.add(bean);
//            }
//        }
//        LiveUnit liveUnit = getFacesModel().getLiveUnit();
//        return liveUnit.copyBeans(beans.toArray(new DesignBean[beans.size()]));
//    }
    

//    public DomProvider.WriteLock writeLock(String message) {
//        UndoEvent undoEvent = getFacesModel().writeLock(message);
//        return new WriteLockImpl(undoEvent);
//    }
//
//    public void writeUnlock(DomProvider.WriteLock writeLock) {
//        if (writeLock instanceof WriteLockImpl) {
//            WriteLockImpl wl = (WriteLockImpl)writeLock;
//            getFacesModel().writeUnlock(wl.getUndoEvent());
//        } else {
//            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
//                    new IllegalStateException("WriteLock is not of expected impl type, writeLock=" + writeLock)); // NOI18N
//        }
//    }
//
//    public boolean isWriteLocked() {
////        return getFacesModel().isWriteLocked();
//        return jsfForm.isWriteLocked();
//    }

//    public void readLock() {
////        MarkupUnit markupUnit = getFacesModel().getMarkupUnit();
//        MarkupUnit markupUnit = jsfForm.getMarkupUnit();
//        if (markupUnit != null) {
//            markupUnit.readLock();
//        }
//    }
//
//    public void readUnlock() {
////        MarkupUnit markupUnit = getFacesModel().getMarkupUnit();
//        MarkupUnit markupUnit = jsfForm.getMarkupUnit();
//        if (markupUnit != null) {
//            markupUnit.readUnlock();
//        }
//    }

//    public boolean isModelValid() {
////        MarkupUnit markupUnit = getFacesModel().getMarkupUnit();
////        if (markupUnit == null) {
////            return false;
////        }
////        return getFacesModel().isValid();
//        return jsfForm.isModelValid();
//    }
    
//    public boolean isModelBusted() {
////        return getFacesModel().isBusted();
//        return jsfForm.isModelBusted();
//    }

//    public void setModelActivated(boolean activated) {
//        getFacesModel().setActivated(activated);
//    }

//    public UndoRedo getUndoManager() {
//        return getFacesModel().getUndoManager();
//    }

//    public DesignBean[] getBeansOfType(Class clazz) {

//    public DesignBean[] getBeansOfType(Class clazz) {

//    public DesignBean[] getBeansOfType(Class clazz) {

//    public DesignBean[] getBeansOfType(Class clazz) {
//        LiveUnit liveUnit = getFacesModel().getLiveUnit();
//        return liveUnit.getBeansOfType(clazz);
//    }

//    public Project getProject() {
//        return getFacesModel().getProject();
//    }

//    public boolean isPage() {
////        return getFacesModel().getFacesUnit().isPage();
//        return jsfForm.getFacesPageUnit().isPage();
//    }

//    public boolean isAlive() {
////        return getFacesModel().getLiveUnit() != null;
//        return jsfForm.getLiveUnit() != null;
//    }

    public boolean isFormComponent(Element componentRootElement) {
//        MarkupDesignBean bean = MarkupUnit.getMarkupDesignBeanForElement(componentRootElement);
//        if (bean == null) {
//            return false;
//        }
////        return Util.isFormBean(getFacesModel(), bean);
//        return jsfForm.isFormDesignBean(bean);
        return jsfForm.isFormComponent(componentRootElement);
    }

    public int getDropType(/*DesignBean origDroppee,*/Element origDropeeComponentRootElement, Element droppeeElement, Transferable t, boolean linkOnly) {
        MarkupDesignBean origDroppee;
        if (origDropeeComponentRootElement == null && jsfForm.isGridMode()) {
            origDroppee = jsfForm.getDefaultParentBean();
        } else {
            origDroppee = MarkupUnit.getMarkupDesignBeanForElement(origDropeeComponentRootElement);
        }
        return getDndSupport().getDropType(origDroppee, droppeeElement, t, linkOnly);
    }

//    public int getDropTypeForClassNames(DesignBean origDroppee, Element droppeeElement, String[] classNames, DesignBean[] beans, boolean linkOnly) {

//    public int getDropTypeForClassNames(DesignBean origDroppee, Element droppeeElement, String[] classNames, DesignBean[] beans, boolean linkOnly) {

//    public int getDropTypeForClassNames(DesignBean origDroppee, Element droppeeElement, String[] classNames, DesignBean[] beans, boolean linkOnly) {

//    public int getDropTypeForClassNames(DesignBean origDroppee, Element droppeeElement, String[] classNames, DesignBean[] beans, boolean linkOnly) {
    public int getDropTypeForComponent(/*DesignBean origDroppee,*/Element origDropeeComponentRootElement, Element droppeeElement, Element componentRootElement, boolean linkOnly) {
        MarkupDesignBean origDroppee;
        if (origDropeeComponentRootElement == null && jsfForm.isGridMode()) {
            origDroppee = jsfForm.getDefaultParentBean();
        } else {
            origDroppee = MarkupUnit.getMarkupDesignBeanForElement(origDropeeComponentRootElement);
        }
        MarkupDesignBean markupDesignBean = MarkupUnit.getMarkupDesignBeanForElement(componentRootElement);
        // XXX Like the original old code.
        String[] classNames;
        DesignBean[] beans;
        if (markupDesignBean == null) {
            classNames = new String[0];
            beans = new DesignBean[0];
        } else {
            classNames = new String[] {markupDesignBean.getInstance().getClass().getName()};
            beans = new DesignBean[] {markupDesignBean};
        }
        return getDndSupport().getDropTypeForClassNames(origDroppee, droppeeElement, classNames, beans, linkOnly);
    }

    public Element getComponentRootElementEquivalentTo(Element oldComponentRootElement) {
        MarkupDesignBean oldMarkupDesignBean = MarkupUnit.getMarkupDesignBeanForElement(oldComponentRootElement);
        if (oldMarkupDesignBean == null) {
            return null;
        }
        MarkupDesignBean newMarkupDesignBean = getMarkupDesignBeanEquivalentTo(oldMarkupDesignBean);
//        if (oldMarkupDesignBean == newMarkupDesignBean) {
//            return oldComponentRootElement;
//        }
        return getComponentRootElementForMarkupDesignBean(newMarkupDesignBean);
    }
    
    private /*public*/ MarkupDesignBean getMarkupDesignBeanEquivalentTo(MarkupDesignBean oldBean) {
//        LiveUnit liveUnit = getFacesModel().getLiveUnit();
        LiveUnit liveUnit = jsfForm.getLiveUnit();
        if (liveUnit == null) {
            return null;
        }
        DesignBean newBean = liveUnit.getBeanEquivalentTo(oldBean);
        return newBean instanceof MarkupDesignBean ? (MarkupDesignBean)newBean : null;
    }

    static Element getComponentRootElementForMarkupDesignBean(MarkupDesignBean markupDesignBean) {
        if (markupDesignBean == null) {
            return null;
        }
        Element sourceElement = markupDesignBean.getElement();
        return MarkupService.getRenderedElementForElement(sourceElement);
    }
    
    public boolean moveComponent(Element componentRootElement, Node parentNode, Node before) {
//        MarkupDesignBean bean = MarkupUnit.getMarkupDesignBeanForElement(componentRootElement);
//        if (bean == null) {
//            return false;
//        }
//        
////        LiveUnit lu = getFacesModel().getLiveUnit();
//        LiveUnit lu = jsfForm.getLiveUnit();
//        MarkupPosition markupPos = new MarkupPosition(parentNode, before);
//        DesignBean parentBean = null;
//        Node e = parentNode;
//
//        while (e != null) {
////            if (e instanceof RaveElement) {
////                parentBean = ((RaveElement)e).getDesignBean();
//            if (e instanceof Element) {
////                parentBean = InSyncService.getProvider().getMarkupDesignBeanForElement((Element)e);
//                parentBean = MarkupUnit.getMarkupDesignBeanForElement((Element)e);
//                
//                if (parentBean != null) {
//                    break;
//                }
//            }
//
//            e = e.getParentNode();
//        }
//
//        if (bean == parentBean) {
//            return false;
//        }
//
//        return lu.moveBean(bean, parentBean, markupPos);
        return jsfForm.moveComponent(componentRootElement, parentNode, before);
    }

//    public void setUpdatesSuspended(Element componentRootElement, boolean suspend) {
//        MarkupDesignBean markupDesignBean = MarkupUnit.getMarkupDesignBeanForElement(componentRootElement);
//        jsfForm.setUpdatesSuspended(markupDesignBean, suspend);
//    }
    
    //////////
    
    //////////
    
    //////////
    
    //////////
    // Helpers
    // XXX Copied from DesignerActions.
//    /** Return true iff the given DesignBean is an XHTML markup "component" */
//    private static boolean isXhtmlComponent(DesignBean bean) {
////        MarkupBean mb = FacesSupport.getMarkupBean(bean);
//        MarkupBean mb = Util.getMarkupBean(bean);
//
//        return (mb != null) && !(mb instanceof FacesBean);
//    }
    
    // XXX Copied from MarkupUtilities.
    
    // XXX Copied from MarkupUtilities.
    
    // XXX Copied from MarkupUtilities.
    
    // XXX Copied from MarkupUtilities.
//    // XXX Copied from DesignerActions.
//    private static void showLineAt(FileObject fo, int lineno, int column) {
//        DataObject dobj;
//        try {
//            dobj = DataObject.find(fo);
//        }
//        catch (DataObjectNotFoundException ex) {
//            ErrorManager.getDefault().notify(ex);
//            return;
//        }
//
//        // Try to open doc before showing the line. This SHOULD not be
//        // necessary, except without this the IDE hangs in its attempt
//        // to open the file when the file in question is a CSS file.
//        // Probably a bug in the xml/css module's editorsupport code.
//        // This has the negative effect of first flashing the top
//        // of the file before showing the destination line, so
//        // this operation is made conditional so only clients who
//        // actually need it need to use it.
//        EditorCookie ec = (EditorCookie)dobj.getCookie(EditorCookie.class);
//        if (ec != null) {
//            try {
//                ec.openDocument(); // ensure that it has been opened - REDUNDANT?
//                //ec.open();
//            }
//            catch (IOException ex) {
//                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
//            }
//        }
//
//        LineCookie lc = (LineCookie)dobj.getCookie(LineCookie.class);
//        if (lc != null) {
//            Line.Set ls = lc.getLineSet();
//            if (ls != null) {
//                // -1: convert line numbers to be zero-based
//                Line line = ls.getCurrent(lineno-1);
//                // TODO - pass in a column too?
//                line.show(Line.SHOW_GOTO, column);
//            }
//        }
//    }

    // XXX Moved from FacesSupport.
//    /** Locate the closest mouse region to the given element */
//    private static MarkupMouseRegion findRegion(Element element) {
//        while (element != null) {
////            if (element.getMarkupMouseRegion() != null) {
////                return element.getMarkupMouseRegion();
////            }
////            MarkupMouseRegion region = InSyncService.getProvider().getMarkupMouseRegionForElement(element);
//            MarkupMouseRegion region = FacesPageUnit.getMarkupMouseRegionForElement(element);
//            if (region != null) {
//                return region;
//            }
//
//            if (element.getParentNode() instanceof Element) {
//                element = (Element)element.getParentNode();
//            } else {
//                break;
//            }
//        }
//
//        return null;
//    }

    
//    private static class WriteLockImpl implements DomProvider.WriteLock {
//        private final UndoEvent undoEvent;
//        
//        public WriteLockImpl(UndoEvent undoEvent) {
//            this.undoEvent = undoEvent;
//        }
//        
//        public UndoEvent getUndoEvent() {
//            return undoEvent;
//        }
//        
//    } // End of WriteLockImpl.
    
    
    public DomProvider.InlineEditorSupport createInlineEditorSupport(Element componentRootElement, String propertyName, String xpath) {
        MarkupDesignBean markupDesignBean = MarkupUnit.getMarkupDesignBeanForElement(componentRootElement);
        if (markupDesignBean == null) {
//            return InlineEditorSupportImpl.createDummyInlineEditorSupport();
            return null;
        }
        DesignProperty designProperty = markupDesignBean.getProperty(propertyName);
        if (designProperty == null) {
//            return InlineEditorSupportImpl.createDummyInlineEditorSupport();
            return null;
        }
        
        return new InlineEditorSupportImpl(jsfForm, this, markupDesignBean, designProperty, xpath);
    }

//    public void dumpHtmlMarkupForNode(org.openide.nodes.Node node) {
//        DesignBean designBean = (DesignBean)node.getLookup().lookup(DesignBean.class);
//        if (designBean instanceof MarkupDesignBean) {
//            MarkupDesignBean markupDesignBean = (MarkupDesignBean)designBean;
//            Element sourceElement = markupDesignBean.getElement();
//            Element renderedElement = MarkupService.getRenderedElementForElement(sourceElement);
//            if (renderedElement == null || sourceElement == renderedElement) {
//                System.err.println("\nMarkup design bean not renderable, markup design bean=" + markupDesignBean); // NOI18N
//                dumpHtmlMarkupDesignBeanHtml();
//                return;
//            }
//            System.err.println("\nRendered markup design bean=" + markupDesignBean); // NOI18N
//            System.err.println(Util.getHtmlStream(renderedElement));
//        } else {
//            System.err.println("\nDesign bean not renderable, design bean=" + designBean); // NOI18N
//            dumpHtmlMarkupDesignBeanHtml();
//        }
//    }
//    
//    private void dumpHtmlMarkupDesignBeanHtml() {
//        DocumentFragment df = getHtmlDocumentFragment();
//        Element html = Util.findDescendant(HtmlTag.HTML.name, df);
//        if (html == null) {
//            return;
//        }
//        System.err.println("\nRendered html element markup design bean=" + MarkupUnit.getMarkupDesignBeanForElement(html)); // NOI18N
//        System.err.println(Util.getHtmlStream(html)); // NOI18N
//    }

//    private static final DataFlavor FLAVOR_DISPLAY_ITEM = new DataFlavor(
//            DataFlavor.javaJVMLocalObjectMimeType + "; class=" + DisplayItem.class.getName(), // NOI18N
//            "RAVE_PALETTE_ITEM"); // TODO get rid of such name.

//    public boolean canPasteTransferable(Transferable trans) {
////        if (trans != null) {
////            DataFlavor[] df = trans.getTransferDataFlavors();
////            int n = 0;
////
////            if (df != null) {
////                n = df.length;
////            }
////
////            for (int i = 0; i < n; i++) {
////                DataFlavor flavor = df[i];
////
////		// XXX TODO Get rid of this dep, you can specify your own data flavor
////		// which can match, there will be created new data flavors avoiding
////		// usage of .
////                if (FLAVOR_DISPLAY_ITEM.equals(flavor)
////		|| (flavor.getRepresentationClass() == String.class)
////		|| flavor.getMimeType().startsWith("application/x-creator-")) { // NOI18N
////                    // Yes!
////                    return true;
////                }
////            }
////        }
////        return false;
//        return jsfForm.canPasteTransferable(trans);
//    }

    public void importString(Designer designer, String string, Point canvasPos, Node documentPosNode, int documentPosOffset, Dimension dimension, boolean isGrid,
            Element droppeeElement, Element dropeeComponentRootElement/*, Element defaultParentComponentRootElement, DomProvider.CoordinateTranslator coordinateTranslator*/) {
        DesignBean droppeeBean;
        if (dropeeComponentRootElement == null && jsfForm.isGridMode()) {
            droppeeBean = jsfForm.getDefaultParentBean();
        } else {
            droppeeBean = MarkupUnit.getMarkupDesignBeanForElement(dropeeComponentRootElement);
        }
//        DesignBean defaultParent = MarkupUnit.getMarkupDesignBeanForElement(defaultParentComponentRootElement);
        DesignBean defaultParent = jsfForm.getDefaultParentBean();
        getDndSupport().importString(designer, string, canvasPos, documentPosNode, documentPosOffset, dimension, isGrid,
                droppeeElement, droppeeBean, defaultParent/*, coordinateTranslator*/);
    }

    public boolean  importData(Designer designer, JComponent comp, Transferable t, /*Object transferData,*/ Point canvasPos, Node documentPosNode, int documentPosOffset, Dimension dimension, boolean isGrid,
            Element droppeeElement, Element dropeeComponentRootElement/*, Element defaultParentComponentRootElement, DomProvider.CoordinateTranslator coordinateTranslator*/, int dropAction) {
        DesignBean droppeeBean;
        if (dropeeComponentRootElement == null && jsfForm.isGridMode()) {
            droppeeBean = jsfForm.getDefaultParentBean();
        } else {
            droppeeBean = MarkupUnit.getMarkupDesignBeanForElement(dropeeComponentRootElement);
        }
//        DesignBean defaultParent = MarkupUnit.getMarkupDesignBeanForElement(defaultParentComponentRootElement);
        DesignBean defaultParent = jsfForm.getDefaultParentBean();
        return getDndSupport().importData(designer, comp, t, /*transferData,*/ canvasPos, documentPosNode, documentPosOffset, dimension, isGrid,
                droppeeElement, droppeeBean, defaultParent, /*coordinateTranslator,*/ dropAction);
    }

    public DomProvider.DomDocument getDomDocument() {
        return jsfForm.getDomDocumentImpl();
    }

    public int compareBoundaryPoints(Node endPointA, int offsetA, Node endPointB, int offsetB) {
        return jsfForm.getDomDocumentImpl().compareBoudaryPoints(endPointA, offsetA, endPointB, offsetB);
    }

    public DomProvider.DomPosition createDomPosition(Node node, int offset, DomPosition.Bias bias) {
        return jsfForm.getDomDocumentImpl().createDomPosition(node, offset, bias);
    }

    public DomProvider.DomPosition createDomPosition(Node node, boolean after) {
        return jsfForm.getDomDocumentImpl().createNextDomPosition(node, after);
    }

    public DomProvider.DomRange createDomRange(Node dotNode, int dotOffset, Node markNode, int markOffset) {
        return jsfForm.getDomDocumentImpl().createRange(dotNode, dotOffset, markNode, markOffset);
    }

    public DomProvider.DomPosition first(DomProvider.DomPosition dot,org.netbeans.modules.visualweb.api.designer.DomProvider.DomPosition mark) {
        return jsfForm.getDomDocumentImpl().first(dot, mark);
    }

    public DomProvider.DomPosition last(DomProvider.DomPosition dot,org.netbeans.modules.visualweb.api.designer.DomProvider.DomPosition mark) {
        return jsfForm.getDomDocumentImpl().last(dot, mark);
    }

    public Designer[] getExternalDesigners(URL url) {
        return jsfForm.getExternalDesigners(url);
    }

    public boolean hasCachedExternalFrames() {
        return jsfForm.hasCachedExternalFrames();
    }

//    public void reuseCssStyle(DomProvider domProvider) {
//        // XXX
//        CssProvider.getEngineService().reuseCssEngineForDocument(jsfForm.getJspDom(),((DomProviderImpl)domProvider).getJsfForm().getJspDom());
//    }

    public boolean isGridMode() {
        return jsfForm.isGridMode();
    }

    
//    public void tcUpdateErrors(Designer designer) {
//        JsfMultiViewElement jsfMultiViewElement = JsfForm.findJsfMultiViewElementForDesigner(designer);
//        if (jsfMultiViewElement == null) {
//            return;
//        }
//        jsfMultiViewElement.getJsfTopComponent().updateErrors();
//    }

//    public void tcDesignContextGenerationChanged(Designer designer) {
//        JsfMultiViewElement jsfMultiViewElement = JsfForm.findJsfMultiViewElementForDesigner(designer);
//        if (jsfMultiViewElement == null) {
//            return;
//        }
//        jsfMultiViewElement.getJsfTopComponent().designContextGenerationChanged();
//    }

//    public void tcRequestActive(Designer designer) {
//        JsfMultiViewElement jsfMultiViewElement = JsfForm.findJsfMultiViewElementForDesigner(designer);
//        if (jsfMultiViewElement == null) {
//            return;
//        }
//        jsfMultiViewElement.getJsfTopComponent().requestActive();
//    }

//    public void tcEnableCutCopyDelete(Designer designer) {
//        JsfMultiViewElement jsfMultiViewElement = JsfForm.findJsfMultiViewElementForDesigner(designer);
//        if (jsfMultiViewElement == null) {
//            return;
//        }
//        jsfMultiViewElement.getJsfTopComponent().enableCutCopyDelete();
//    }

//    public void tcDisableCutCopyDelete(Designer designer) {
//        JsfMultiViewElement jsfMultiViewElement = JsfForm.findJsfMultiViewElementForDesigner(designer);
//        if (jsfMultiViewElement == null) {
//            return;
//        }
//        jsfMultiViewElement.getJsfTopComponent().disableCutCopyDelete();
//    }

//    public void tcSetActivatedNodes(Designer designer, org.openide.nodes.Node[] nodes) {
//        JsfMultiViewElement jsfMultiViewElement = JsfForm.findJsfMultiViewElementForDesigner(designer);
//        if (jsfMultiViewElement == null) {
//            return;
//        }
//        setActivatedNodes(jsfMultiViewElement.getJsfTopComponent(), nodes);
//    }

//    public org.openide.nodes.Node[] tcGetActivatedNodes(Designer designer) {
//        JsfMultiViewElement jsfMultiViewElement = JsfForm.findJsfMultiViewElementForDesigner(designer);
//        if (jsfMultiViewElement == null) {
//            return new org.openide.nodes.Node[0];
//        }
//        return jsfMultiViewElement.getJsfTopComponent().getActivatedNodes();
//    }

//    public void tcShowPopupMenu(Designer designer, int x, int y) {
//        JsfMultiViewElement jsfMultiViewElement = JsfForm.findJsfMultiViewElementForDesigner(designer);
//        if (jsfMultiViewElement == null) {
//            return;
//        }
//        jsfMultiViewElement.getJsfTopComponent().showPopupMenu(x, y);
//    }
//
//    public void tcShowPopupMenu(Designer designer, JPopupMenu popup, int x, int y) {
//        JsfMultiViewElement jsfMultiViewElement = JsfForm.findJsfMultiViewElementForDesigner(designer);
//        if (jsfMultiViewElement == null) {
//            return;
//        }
//        popup.show(jsfMultiViewElement.getJsfTopComponent(), x, y);
//    }
//
//    public void tcShowPopupMenuForEvent(Designer designer, MouseEvent evt) {
//        JsfMultiViewElement jsfMultiViewElement = JsfForm.findJsfMultiViewElementForDesigner(designer);
//        if (jsfMultiViewElement == null) {
//            return;
//        }
//        // #6442386
//        JsfTopComponent jsfTopComponent = jsfMultiViewElement.getJsfTopComponent();
//        Point p = SwingUtilities.convertPoint(evt.getComponent(), evt.getX(), evt.getY(), jsfTopComponent);
//        jsfTopComponent.showPopupMenu(p.x, p.y);
//    }

    // XXX Moved to FacesDnDSupport.
//    public boolean tcImportComponentData(Designer designer, JComponent comp, Transferable t) {
////        JsfMultiViewElement jsfMultiViewElement = JsfForm.findJsfMultiViewElementForDesigner(designer);
////        if (jsfMultiViewElement == null) {
////            return false;
////        }
//        
//        JsfTopComponent jsfTopComponent;
//
//        if (comp instanceof JsfTopComponent) {
//            jsfTopComponent = (JsfTopComponent)comp;
//        } else {
//            jsfTopComponent = (JsfTopComponent)SwingUtilities.getAncestorOfClass(JsfTopComponent.class, comp);
//        }
//
//        if (jsfTopComponent == null) {
//            // XXX
//            return false;
//        }
//
////                DesignBean parent = selectionTopComp.getPasteParent();
//        Element parentComponentRootElement = jsfTopComponent.getPasteParentComponent();
////                MarkupPosition pos = selectionTopComp.getPasteMarkupPosition();
////        Point location = jsfTopComponent.getPastePosition();
//        Point location = designer.getPastePoint();
////                DesignBean[] beans = selectionTopComp.pasteBeans(webform, t, parent, pos, location);
////                Element[] componentRootElements = SelectionTopComp.pasteComponents(webform, t, parentComponentRootElement, location);
//
//        if (location != null) {
////            GridHandler gridHandler = webform.getGridHandler();
////            location.x = gridHandler.snapX(location.x);
////            location.y = gridHandler.snapY(location.y);
//            location.x = designer.snapX(location.x, null);
//            location.y = designer.snapY(location.y, null);
//        }
////        Element[] componentRootElements = webform.pasteComponents(t, parentComponentRootElement, location);
//        Element[] componentRootElements = pasteComponents(t, parentComponentRootElement, location);
//
////                if ((beans != null) && (beans.length > 0)) {
////                    selectionTopComp.selectBeans(beans);
////                }
//        if (componentRootElements.length > 0) {
////            selectionTopComp.selectComponents(componentRootElements);
//            jsfTopComponent.selectComponents(componentRootElements);
//        }
//        return true;
//        
//    }

//    public Point tcGetPastePosition(Designer designer) {
////        JsfMultiViewElement jsfMultiViewElement = JsfForm.findJsfMultiViewElementForDesigner(designer);
////        if (jsfMultiViewElement == null) {
////            return new Point(0, 0);
////        }
////        return jsfMultiViewElement.getJsfTopComponent().getPastePosition();
//        return designer.getPastePoint();
//    }

//    public void tcRepaint(Designer designer) {
////        JsfMultiViewElement jsfMultiViewElement = JsfForm.findJsfMultiViewElementForDesigner(designer);
////        if (jsfMultiViewElement == null) {
////            return;
////        }
////        jsfMultiViewElement.getJsfTopComponent().repaint();
//        JsfSupportUtilities.tcRepaint(designer);
//    }

//    public boolean tcSeenEscape(Designer designer, ActionEvent evt) {
//        JsfMultiViewElement jsfMultiViewElement = JsfForm.findJsfMultiViewElementForDesigner(designer);
//        if (jsfMultiViewElement == null) {
//            return false;
//        }
//        return jsfMultiViewElement.getJsfTopComponent().seenEscape(evt.getWhen());
//    }

//    public void tcDeleteSelection(Designer designer) {
//        JsfMultiViewElement jsfMultiViewElement = JsfForm.findJsfMultiViewElementForDesigner(designer);
//        if (jsfMultiViewElement == null) {
//            return;
//        }
//        jsfMultiViewElement.getJsfTopComponent().deleteSelection();
//    }

    // XXX Copy also in designer/../DesignerUtils.
    /** Thread-safe method to set the activated nodes of a TopComponent;
     * this can only be done from the event dispatch thread. If called
     * from another thread it will post a runnable on the event dispatch
     * thread instead.
     */
    private static void setActivatedNodes(final TopComponent tc, final org.openide.nodes.Node[] nodes) {
//        if(DEBUG) {
//            debugLog(DesignerUtils.class.getName() + ".setActivatedNodes(TopComponent, Node[])");
//        }
        if(nodes == null) {
            throw(new IllegalArgumentException("Null node array."));// NOI18N
        }
        if(tc == null) {
            throw(new IllegalArgumentException("Null TopComponent."));// NOI18N
        }
        if (SwingUtilities.isEventDispatchThread()) {
            tc.setActivatedNodes(nodes);
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    tc.setActivatedNodes(nodes);
                }
            });
        }
    }

    public boolean isRenderedNode(Node node) {
        return jsfForm.isRenderedNode(node);
    }

    // XXX
    public void paintDesignerDecorations(Graphics2D g2d, Designer designer) {
        boolean showVirtualForms = jsfForm.isVirtualFormsSupportEnabled();
        boolean showAjaxTransactions = jsfForm.isAjaxTransactionsSupportEnabled();
        
        if (showVirtualForms || showAjaxTransactions) {
    //        DesignContext designContext = renderContext.getDesignContext();
    //        if (!(designContext instanceof LiveUnit)) {
    //            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
    //                    new IllegalStateException("DesignContext is not of LiveUnit type, designContext=" + designContext)); // NOI18N
    //        }
    //        LiveUnit liveUnit = (LiveUnit)designContext;
    //        Project project = liveUnit.getModel().getProject();
//            Project project = getFacesModel().getProject();
//            LiveUnit liveUnit = getFacesModel().getLiveUnit();
            Project project = jsfForm.getProject();
            LiveUnit liveUnit = jsfForm.getLiveUnit();
            if (liveUnit == null) {
                // XXX Log problem?
                return;
            }

            ComponentGroupSupport.paint(liveUnit, designer.createRenderContext(), g2d, showVirtualForms, showAjaxTransactions);
        }
    }

    public Decoration getDecoration(Element element) {
        return DecorationManager.getDefault().getDecoration(element);
    }

//    public boolean isShowDecorations() {
//        return JsfDesignerPreferences.getInstance().isShowDecorations();
//    }
//
//    public int getDefaultFontSize() {
//        return JsfDesignerPreferences.getInstance().getDefaultFontSize();
//    }
//
//    public int getPageSizeWidth() {
//        return JsfDesignerPreferences.getInstance().getPageSizeWidth();
//    }
//
//    public int getPageSizeHeight() {
//        return JsfDesignerPreferences.getInstance().getPageSizeHeight();
//    }
//
//    public boolean isGridShow() {
//        return GridHandler.getDefault().isGrid();
//    }
//
//    public boolean isGridSnap() {
//        return GridHandler.getDefault().isSnap();
//    }
//
//    public int getGridWidth() {
//        return GridHandler.getDefault().getGridWidth();
//    }
//
//    public int getGridHeight() {
//        return GridHandler.getDefault().getGridHeight();
//    }

//    public int getGridTraceWidth() {
//        return GridHandler.getDefault().getGridTraceWidth();
//    }
//
//    public int getGridTraceHeight() {
//        return GridHandler.getDefault().getGridTraceHeight();
//    }
//
//    public int getGridOffset() {
//        return GridHandler.getDefault().getGridOffset();
//    }
    
    @Override
    public String toString() {
        return super.toString() + "[jsfForm=" + jsfForm + "]"; // NOI18N
    }

    public DomProviderService getDomProviderService() {
        return domProviderService;
    }
}
