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
package org.netbeans.modules.visualweb.web.ui.dt.component;

import java.util.regex.Pattern;
import javax.faces.component.UIPanel;
import javax.faces.component.UIComponent;

import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignContext;
import com.sun.rave.designtime.DesignProperty;
import com.sun.rave.designtime.Result;
import com.sun.rave.designtime.faces.FacesDesignProperty;
import com.sun.rave.designtime.markup.BasicMarkupMouseRegion;
import com.sun.rave.designtime.markup.MarkupDesignBean;
import com.sun.rave.designtime.markup.MarkupDesignInfo;
import com.sun.rave.designtime.markup.MarkupMouseRegion;
import com.sun.rave.designtime.markup.MarkupPosition;
import com.sun.rave.designtime.markup.MarkupRenderContext;
import com.sun.rave.web.ui.component.Body;
import com.sun.rave.web.ui.component.PanelGroup;
import com.sun.rave.web.ui.component.PanelLayout;
import com.sun.rave.web.ui.component.Tab;
import com.sun.rave.web.ui.component.TabSet;
import org.netbeans.modules.visualweb.web.ui.dt.component.util.DesignUtil;
import org.netbeans.modules.visualweb.web.ui.dt.AbstractDesignInfo;


/**
 * DesignInfo for the <code>Tab</code> component. The following behavior is
 * implemented:
 * <ul>
 * <li>Upon component creation, pre-set <code>text</code> property to the
 * component's display name, "Tab".</li>
 * <li>Clicking on the tab names will switch the containing TabSet's selected
 * property to the clicked tab, unless the selected property contains a value
 * binding expression, in which case nothing happens.</li>
 * </ul>
 *
 * @author gjmurphy
 * @author Tor Norbye
 */
public class TabDesignInfo extends AbstractDesignInfo implements MarkupDesignInfo {

    public TabDesignInfo() {
        super(Tab.class);
    }

    public Result beanCreatedSetup(DesignBean bean) {
        super.beanCreatedSetup(bean);
        DesignContext context = bean.getDesignContext();
        DesignProperty textProperty = bean.getProperty("text"); //NOI18N
        String suffix = DesignUtil.getNumericalSuffix(bean.getInstanceName());
        textProperty.setValue(
                bean.getBeanInfo().getBeanDescriptor().getDisplayName() + " " + suffix);
        selectTab(bean);
        if (context.canCreateBean(PanelLayout.class.getName(), bean, null)) {
            DesignBean panelBean = context.createBean(PanelLayout.class.getName(), bean, null);
            panelBean.getDesignInfo().beanCreatedSetup(panelBean);
            panelBean.getProperty("panelLayout").setValue(getParentLayout(bean));
        }
        DesignBean parentBean = bean.getBeanParent();
        if (parentBean.getInstance() instanceof Tab) {
            DesignBean childBean = parentBean.getChildBean(0);
            if (childBean.getInstance() instanceof PanelLayout)
                context.deleteBean(childBean);
        }
        return Result.SUCCESS;
    }

    public Result beanPastedSetup(DesignBean bean) {
        DesignContext context = bean.getDesignContext();
        DesignBean parentBean = bean.getBeanParent();
        if (parentBean.getInstance() instanceof Tab) {
            DesignBean childBean = parentBean.getChildBean(0);
            if (childBean.getInstance() instanceof PanelLayout)
                context.deleteBean(childBean);
        }
        return Result.SUCCESS;
    }

    public Result beanDeletedCleanup(DesignBean bean) {
        unselectTab(bean);
        return Result.SUCCESS;
    }

    public boolean acceptChild(DesignBean parentBean, DesignBean childBean, Class childClass) {
        if (parentBean.getInstance() instanceof Tab) {
            Tab tab = (Tab) parentBean.getInstance();
            // Do not allow tabs, once added to a tabset, to be reparented to a
            // different tab, as this requrires some property clean-up, but there
            // is currently no way to handle the reparent event.
            if (childBean != null && childBean.getBeanParent() != null)
                return false;
            // Allow tabs inside of childless tabs, tabs with only one panel child, or
            // tabs with one or more tab children
            if (childClass.equals(Tab.class)) {
                if (tab.getChildCount() != 1)
                    return true;
                UIComponent component = (UIComponent) tab.getChildren().get(0);
                if (!(component instanceof PanelLayout) || component.getChildCount() == 0)
                    return true;
            }
            // Allow panels inside of childless tabs only
            if (UIPanel.class.isAssignableFrom(childClass) || PanelGroup.class.isAssignableFrom(childClass)
            || PanelLayout.class.isAssignableFrom(childClass)) {
                if (tab.getChildCount() == 0)
                    return true;
            }
        }
        return false;
    }
    
    public boolean acceptParent(DesignBean parentBean, DesignBean childBean, Class childClass) {
        Object parent = parentBean.getInstance();
        if (parent instanceof TabSet)
            return true;
        if (parent instanceof Tab && !(parentBean.getBeanParent().getBeanParent().getInstance() instanceof Tab))
            return true;
        return false;
    }
    
    public void instanceNameChanged(DesignBean bean, String oldInstanceName) {
        // If instance name is changed, check to see whether this tab is the tabSet's
        // currently selected tab. If so, updated the tabSet's selected property.
        DesignBean tabSetBean = bean.getBeanParent();
        while(tabSetBean != null && !(tabSetBean.getInstance() instanceof TabSet)) {
            tabSetBean.getProperty("selectedChildId").setValue(null);
            tabSetBean = tabSetBean.getBeanParent();
        }
        if( tabSetBean != null) {
            DesignProperty selectedProperty = tabSetBean.getProperty("selected"); //NOI18N
            if (oldInstanceName != null && oldInstanceName.equals(selectedProperty.getValue()))
                selectedProperty.setValue(bean.getInstanceName());
        }
    }
    
    public void customizeRender(final MarkupDesignBean bean, MarkupRenderContext renderContext) {
        DocumentFragment documentFragment = renderContext.getDocumentFragment();
        MarkupPosition begin = renderContext.getBeginPosition();
        MarkupPosition end = renderContext.getEndPosition();
        
        if (begin == end) {
            return;
        }
        
        assert begin.getUnderParent() == end.getUnderParent();
        
        Node n = begin.getBeforeSibling();
        Element tabElement = null;
        
        while (n != null && tabElement == null) {
            if (n.getNodeType() == Node.ELEMENT_NODE && n.getLocalName().equals("td"))
                tabElement = (Element) n;
            n = n.getParentNode();
        }
        
        if (tabElement != null) {
            FacesDesignProperty selectedProperty = getTabSetSelectedProperty(bean);
            if (selectedProperty != null && !bean.getProperty("id").getValue().equals(selectedProperty.getValue()))
                registerTab(bean, renderContext, tabElement);
        }
        
    }
    
    private void registerTab(final DesignBean bean, MarkupRenderContext context, Element e) {
        String styleClass = e.getAttribute("class"); // NOI18N
        
        if (styleClass.indexOf("disabled") == -1) { // NOI18N
            MarkupMouseRegion region =
                    new BasicMarkupMouseRegion() {
                public boolean isClickable() {
                    return true;
                }
                
                public Result regionClicked(int clickCount) {
                    selectTab(bean);
                    return Result.SUCCESS;
                }
            };
            
            context.associateMouseRegion(e, region);
            
            return;
        }
    }
    
    /**
     * "Selects" the tab wrapped by the bean specified, in the designer, by walking
     * up the component tree and setting the selected property of the ancestor
     * tabSet to the tab's id. If the "selected" property is value bound, then
     * no changes are made.
     */
    private static void selectTab(DesignBean bean) {
        FacesDesignProperty property = getTabSetSelectedProperty(bean);
        assert property != null;
        if (property != null && !property.isBound()) {
            property.setValue(bean.getInstanceName());
        }
    }
    
    /**
     * "De-selects" the tab wrapped by the bean specified, in the designer, by walking
     * up the component tree and setting the selected property of the ancestor
     * tabSet to the id of the next selectable sibling tab.
     */
    private static void unselectTab(DesignBean bean) {
        // Find a suitable sibling tab to select
        DesignBean tabSet = null;
        String id = null;
        DesignBean parentBean = bean.getBeanParent();
        if (parentBean.getInstance() instanceof TabSet) {
            tabSet = bean.getBeanParent();
        } else {
            tabSet = getTabSetBean(bean);
            DesignBean[] childrenBeans = parentBean.getChildBeans();
            int index = 0;
            while (index < childrenBeans.length && bean != childrenBeans[index])
                index++;
            if (index > 0) {
                id = childrenBeans[index-1].getInstanceName();
            } else if (childrenBeans.length > 1) {
                id = childrenBeans[1].getInstanceName();
            }
        }
        assert tabSet != null;
        if (tabSet != null) {
            FacesDesignProperty property =
                    (FacesDesignProperty) tabSet.getProperty("selected"); // NOI18N
            assert property != null;
            if (property != null && !property.isBound() && property.getValue().equals(bean.getInstanceName())) {
                property.setValue(id);
            }
        }
    }
    
    private static FacesDesignProperty getTabSetSelectedProperty(DesignBean tabBean) {
        DesignBean tabSetBean = getTabSetBean(tabBean);
        if (tabSetBean == null)
            return null;
        return (FacesDesignProperty) tabSetBean.getProperty("selected");
    }
    
    /**
     * Walk up the component tree, starting with the tab bean specified, until
     * the ancestor tabSet is found, and return it. If no parent tabSet is found
     * returns null.
     */
    private static DesignBean getTabSetBean(DesignBean tabBean) {
        DesignBean bean = tabBean.getBeanParent();
        while (bean != null && !(bean.getInstance() instanceof TabSet))
            bean = bean.getBeanParent();
        return bean;
    }
    
    private static Pattern gridPattern = Pattern.compile(".*-rave-layout\\s*:\\s*grid.*");
    
    /**
     * Walk up the component tree, starting with the tab bean specified, until
     * the first enclosing body or div element with a "style" property is
     * encountered. If there is a rave grid positioning style set, return a
     * keyword indicating grid layout. Otherwise, returns a keyword indicating
     * flow layout.
     */
    private static String getParentLayout(DesignBean bean) {
        while (bean != null) {
            if (bean.getInstance() instanceof Body ||
                    (bean instanceof MarkupDesignBean && ((MarkupDesignBean) bean).getElement().getTagName().equals("div"))) {
                String style = (String) bean.getProperty("style").getValue(); //NOI18N
                if (style != null && gridPattern.matcher(style).matches())
                    return PanelLayout.GRID_LAYOUT;
            }
            bean = bean.getBeanParent();
        }
        return PanelLayout.FLOW_LAYOUT;
    }
    
}
