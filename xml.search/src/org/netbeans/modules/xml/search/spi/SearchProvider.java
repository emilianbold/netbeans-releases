/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License. When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP. Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.xml.search.spi;

import java.beans.BeanInfo;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.openide.loaders.DataObject;
import org.openide.nodes.Node;

import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.Named;
import org.netbeans.modules.xml.xam.dom.DocumentComponent;
import org.netbeans.modules.xml.xam.ui.ModelCookie;
import org.netbeans.modules.xml.xam.ui.highlight.Highlight;
import org.netbeans.modules.xml.xam.ui.highlight.HighlightGroup;
import org.netbeans.modules.xml.xam.ui.highlight.HighlightManager;

import org.netbeans.modules.xml.search.api.SearchElement;
import org.netbeans.modules.xml.search.api.SearchTarget;
import static org.netbeans.modules.xml.util.UI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2008.04.16
 */
public interface SearchProvider {

    /**
     * Returns root.
     * @return root
     */
    Object getRoot();

    /**
     * Returns targets.
     * @return targets
     */
    SearchTarget[] getTargets();

    /**
     * Returns element by the given object.
     * @param object given object
     * @return element by the given object
     */
    SearchElement getElement(Object object);

    // ---------------------------------------------
    public class Adapter implements SearchProvider {

        public Adapter(Object root) {
            this(root, null);
        }

        public Adapter(Object root, DataObject data) {
            myData = data;
            myRoot = root;
            myElements = new WeakHashMap<Component, SearchElement>();
        }

        protected Component getRoot(DataObject data) {
            return null;
        }

        protected String getType(Component component) {
            return null;
        }

        protected Node getNode(Component component) {
            return null;
        }

        protected void gotoVisual(Component component) {
        }

        protected void gotoSource(Component component) {
        }

        protected Icon getIcon(Component component) {
            return new ImageIcon(getNode(component).getIcon(BeanInfo.ICON_COLOR_16x16));
        }

        protected String getName(Component component) {
            String name = null;

            if (component instanceof Named) {
                name = ((Named) component).getName();
            }
            if (name == null && component instanceof DocumentComponent) {
                name = getDocumentName((DocumentComponent) component);
            }
            if (name == null) {
                name = ""; // NOI18N
            }
            return name;
        }

        private String getDocumentName(DocumentComponent component) {
            org.w3c.dom.Element element = component.getPeer();

            if (element == null) {
                return null;
            }
            return element.getTagName();
        }

        protected String getToolTip(Component component) {
            String type = getType(component);

            if (type != null) {
                int k = type.lastIndexOf("."); // NOI18N

                if (k != -1) {
                    type = type.substring(k + 1);
                }
                return "<html>" + type + " <b>" + getName(component) + "</b></html>"; // NOI18N
            }
            return getName(component);
        }

        public final Object getRoot() {
            if (myRoot == null) {
                myRoot = getRoot(myData);
            }
            return myRoot;
        }

        public SearchTarget[] getTargets() {
            return null;
        }

        public final SearchElement getElement(Object object) {
            if (!(object instanceof Component)) {
                return null;
            }
            Component component = (Component) object;
            SearchElement element = myElements.get(component);

            if (element != null) {
                return element;
            }
            Component father = component.getParent();
            SearchElement parent = null;

            if (father != null) {
                parent = getElement(father);
            }
            element = new Element(component, parent);
            myElements.put(component, element);

            return element;
        }

        protected final DataObject getDataObject() {
            return myData;
        }

        protected final Model getModel(DataObject data) {
            ModelCookie cookie = data.getCookie(ModelCookie.class);

            if (cookie == null) {
                return null;
            }
            try {
                return cookie.getModel();
            } catch (IOException e) {
                return null;
            }
        }

        protected final void highlight(Component component) {
            HighlightManager manager = HighlightManager.getDefault();
            List<HighlightGroup> groups = manager.getHighlightGroups(HighlightGroup.SEARCH);

            if (groups != null) {
                for (HighlightGroup group : groups) {
                    manager.removeHighlightGroup(group);
                }
            }
            HighlightGroup group = new HighlightGroup(HighlightGroup.SEARCH);
            Highlight highlight = new Highlight(component, Highlight.SEARCH_RESULT);
            group.addHighlight(highlight);
            manager.addHighlightGroup(group);
        }

        // --------------------------------------------------------
        private final class Element extends SearchElement.Adapter {

            private Element(Component component, SearchElement parent) {
                super(SearchProvider.Adapter.this.getName(component), SearchProvider.Adapter.this.getToolTip(component), SearchProvider.Adapter.this.getIcon(component), parent);
                myComponent = component;
            }

            @Override
            public void gotoSource() {
                SearchProvider.Adapter.this.gotoSource(myComponent);
            }

            @Override
            public void gotoVisual() {
                SearchProvider.Adapter.this.gotoVisual(myComponent);
            }

            @Override
            public boolean isDeleted() {
                return myComponent.getModel() == null;
            }
            private Component myComponent;
        }

        private Object myRoot;
        private DataObject myData;
        private Map<Component, SearchElement> myElements;
    }
}
