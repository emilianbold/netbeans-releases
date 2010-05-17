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

import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignProperty;
import com.sun.rave.designtime.Result;
import com.sun.rave.designtime.faces.FacesDesignBean;
import com.sun.rave.designtime.faces.FacesDesignContext;
import com.sun.rave.designtime.markup.BasicMarkupMouseRegion;
import com.sun.rave.designtime.markup.MarkupDesignBean;
import com.sun.rave.designtime.markup.MarkupDesignInfo;
import com.sun.rave.designtime.markup.MarkupMouseRegion;
import com.sun.rave.designtime.markup.MarkupPosition;
import com.sun.rave.designtime.markup.MarkupRenderContext;
import com.sun.rave.web.ui.component.ImageComponent;
import com.sun.rave.web.ui.component.TreeNode;
import org.netbeans.modules.visualweb.web.ui.dt.AbstractDesignInfo;
import org.netbeans.modules.visualweb.web.ui.dt.component.util.DesignUtil;
import javax.faces.component.UIGraphic;

import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Design time behavior for a <code>TreeNode</code> component.
 *
 * @author gjmurphy
 * @author Edwin Goei
 */

public class TreeNodeDesignInfo extends AbstractDesignInfo implements MarkupDesignInfo {

    /** Name of the image facet */
    private static final String IMAGE_FACET = "image"; //NOI18N

    /** Name of the content facet */
    private static final String CONTENT_FACET = "content"; //NOI18N

    /** Name of theme icon for external tree nodes */
    private static final String TREE_DOCUMENT_ICON = "TREE_DOCUMENT"; //NOI18N

    /** Name of theme icon for internal tree nodes */
    private static final String TREE_FOLDER_ICON = "TREE_FOLDER"; //NOI18N

    public TreeNodeDesignInfo() {
        super(TreeNode.class);
    }

    protected DesignProperty getDefaultBindingProperty(DesignBean targetBean) {
        return targetBean.getProperty("text"); //NOI18N
    }

    public boolean acceptParent(DesignBean parentBean, DesignBean childBean,
            Class childClass) {
        Class parentClass = parentBean.getInstance().getClass();
        if (TreeNode.class.isAssignableFrom(parentClass))
            return true;
        return false;
    }

    public boolean acceptChild(DesignBean parentBean, DesignBean childBean,
            Class childClass) {
        if (childClass.equals(TreeNode.class) || UIGraphic.class.isAssignableFrom(childClass))
            return true;
        return false;
    }

    public Result beanCreatedSetup(DesignBean bean) {
        DesignProperty prop;

        // Set a default display name for the text node
        String suffix = DesignUtil.getNumericalSuffix(bean.getInstanceName());
        String displayName = bean.getBeanInfo().getBeanDescriptor().getDisplayName();
        bean.getProperty("text").setValue(displayName + " " + suffix); //NOI18N

        // Add an image component to the image facet, set by default to display
        // the theme icon for tree nodes
        FacesDesignContext fdc = (FacesDesignContext) bean.getDesignContext();
        String imageComponentName = ImageComponent.class.getName();
        if (fdc.canCreateFacet(IMAGE_FACET, imageComponentName, bean)) {
            DesignBean imageFacet = fdc.createFacet(IMAGE_FACET,
                    imageComponentName, bean);
            imageFacet.getProperty("icon").setValue(TREE_DOCUMENT_ICON); //NOI18N
        } else {
            return Result.FAILURE;
        }

        // If parent componet is a tree node, make it expanded. Also, if it has
        // an image facet, and the facet's component's icon is set to the document
        // icon, change it to folder.
        DesignBean parent = bean.getBeanParent();
        if (parent.getInstance().getClass().equals(TreeNode.class)) {
            parent.getProperty("expanded").setValue(Boolean.TRUE); //NOI18N
            DesignBean imageFacet = ((FacesDesignBean)parent).getFacet(IMAGE_FACET);
            if (imageFacet != null && imageFacet.getInstance().getClass().equals(ImageComponent.class)) {
                DesignProperty iconProperty = imageFacet.getProperty("icon"); //NOI18N
                if (iconProperty.getValue() != null && iconProperty.getValue().equals(TREE_DOCUMENT_ICON))
                    iconProperty.setValue(TREE_FOLDER_ICON);
            }
        }
        
        return Result.SUCCESS;
    }
    
    public void customizeRender(final MarkupDesignBean bean, MarkupRenderContext renderContext) {
        
        DocumentFragment documentFragment = renderContext.getDocumentFragment();
        MarkupPosition begin = renderContext.getBeginPosition();
        MarkupPosition end = renderContext.getEndPosition();
        if (begin == end)
            return;
        
        // Look for div element that wraps the image and hyperlink which implement
        // the tree node toggle widget (./div[@class='TreeRow']/div[@class='float'])
        Node thisNode = begin.getBeforeSibling();
        Node lastNode = end.getBeforeSibling();
        Element div = null;
        while (thisNode != lastNode && div == null) {
            if (thisNode instanceof Element) {
                Element e = (Element)thisNode;
                if (e.getLocalName().equals("div") && e.getAttribute("class") != null
                        && e.getAttribute("class").indexOf("TreeRow") != -1)
                    div = e;
            }
            thisNode = thisNode.getNextSibling();
        }
        if (div == null)
            return;
        NodeList nodeList = div.getElementsByTagName("div");
        div = null;
        for (int i = 0; i < nodeList.getLength(); i++) {
            Element e = (Element) nodeList.item(i);
            if (e.getAttribute("class") != null && e.getAttribute("class").indexOf("float") != -1)
                div = e;
        }
        if (div == null)
            return;
        
        // Associate a markup mouse region with the tree node toggle markup, which
        // expands or contracts the node when clicked
        MarkupMouseRegion region = new BasicMarkupMouseRegion() {
            
            public boolean isClickable() {
                return true;
            }
            
            public Result regionClicked(int clickCount) {
                DesignProperty property = bean.getProperty("expanded"); // NOI18N
                property.setValue(property.getValue().equals(Boolean.TRUE) ? Boolean.FALSE : Boolean.TRUE);
                return Result.SUCCESS;
            }
        };
        
        renderContext.associateMouseRegion(div, region);
    }
    
    public Result beanDeletedCleanup(DesignBean bean) {
        // If parent component is a tree node, it has an image facet, and it's
        // image facet's icon is set to the folder icon, and this is its only
        // child, change icon to document icon
        DesignBean parent = bean.getBeanParent();
        if (parent.getInstance().getClass().equals(TreeNode.class) && parent.getChildBeanCount() == 1) {
            DesignBean imageFacet = ((FacesDesignBean)parent).getFacet(IMAGE_FACET);
            if (imageFacet != null && imageFacet.getInstance().getClass().equals(ImageComponent.class)) {
                DesignProperty iconProperty = imageFacet.getProperty("icon"); //NOI18N
                if (iconProperty.getValue() != null && iconProperty.getValue().equals(TREE_FOLDER_ICON))
                    iconProperty.setValue(TREE_DOCUMENT_ICON);
            }
        }
        return Result.SUCCESS;
    }
    
}
