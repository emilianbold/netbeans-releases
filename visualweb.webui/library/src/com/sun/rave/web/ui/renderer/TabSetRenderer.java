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
package com.sun.rave.web.ui.renderer;

import com.sun.rave.web.ui.util.ConversionUtilities;
import java.util.List;
import java.util.Iterator;
import java.io.IOException;
import java.lang.NullPointerException;

import javax.faces.el.MethodBinding;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionListener;
import javax.faces.component.UIComponent;
import javax.faces.context.ResponseWriter;
import javax.faces.component.UIForm;

import com.sun.rave.web.ui.util.LogUtil;
import com.sun.rave.web.ui.component.Tab;
import com.sun.rave.web.ui.component.Icon;
import com.sun.rave.web.ui.component.TabSet;
import com.sun.rave.web.ui.component.SkipHyperlink;
import com.sun.rave.web.ui.theme.Theme;
import com.sun.rave.web.ui.theme.ThemeImages;
import com.sun.rave.web.ui.theme.ThemeStyles;
import com.sun.rave.web.ui.component.util.Util;
import com.sun.rave.web.ui.util.ThemeUtilities;
import com.sun.rave.web.ui.util.RenderingUtilities;

/**
 * Renders a TabSet component. 
 * 
 * @author  Sean Comerford
 */
public class TabSetRenderer extends AbstractRenderer {
    
    /** Default constructor */
    public TabSetRenderer() {
        super();
    }
    
    /**
     * <p>Return a flag indicating whether this Renderer is responsible
     * for rendering the children the component it is asked to render.
     * The default implementation returns <code>false</code>.</p>
     */
    public boolean getRendersChildren() {
        return true;
    }
        
    public void renderEnd(FacesContext context, UIComponent component, 
            ResponseWriter writer) throws IOException {
        // render any kids of the selecte tab component now
        TabSet tabSet = (TabSet) component;
        String selectedTabId = tabSet.getSelected();        
        
        Theme theme = ThemeUtilities.getTheme(context);
        String lite = theme.getStyleClass(ThemeStyles.TABGROUPBOX);
 
        if (selectedTabId == null) { 
            if (tabSet.isMini() && tabSet.isLite()) {
                writer.startElement("div", tabSet); //NOI18N
                writer.writeAttribute("class", lite, null); //NOI18N
                writer.endElement("div"); //NOI18N
            }
            writer.endElement("div"); //NOI18N
            
            return;
        }
        
        Tab selectedTab = tabSet.findChildTab(selectedTabId);        
        if (selectedTab == null) {            
            if (tabSet.isMini() && tabSet.isLite()) {
                writer.startElement("div", tabSet); //NOI18N
                writer.writeAttribute("class", lite, null); //NOI18N
                writer.endElement("div"); //NOI18N
            }
            writer.endElement("div"); //NOI18N
            return;
        }
                
        if (tabSet.isMini() && tabSet.isLite()) {
            writer.startElement("div", tabSet); //NOI18N
            writer.writeAttribute("class", lite, null); //NOI18N
        }
        
        while (selectedTab.hasTabChildren()) {
            selectedTabId = selectedTab.getSelectedChildId();
            if (selectedTabId == null) {
                selectedTabId = ((Tab) selectedTab.getChildren().get(0)).getId();
            }
            selectedTab = (Tab) selectedTab.findComponent(selectedTabId);
        }
        
        int numKids = selectedTab.getChildCount();
        if (numKids > 0) {
            // render the contentHeader facet if specified
            UIComponent facet = tabSet.getFacet("contentHeader");
            if (facet != null) {
                RenderingUtilities.renderComponent(facet, context);
            }
            
            // render the children of the selected Tab component
            List kids = selectedTab.getChildren();            
            for (int i = 0; i < numKids; i++) {
                UIComponent kid = (UIComponent) kids.get(i);
                RenderingUtilities.renderComponent(kid, context);
            }
            
            facet = tabSet.getFacet("contentFooter");
            if (facet != null) {
                RenderingUtilities.renderComponent(facet, context);
            }
        }
        if (tabSet.isMini() && tabSet.isLite()) {
            writer.endElement("div"); //NOI18N
        }
        writer.endElement("div"); //NOI18N

    }
    
    /**
     * <p>Encode the Tab children of this TabSet component.</p>
     *
     * @param context The current FacesContext
     * @param component The current TabSet component
     */
    public void encodeChildren(FacesContext context, UIComponent component) 
            throws IOException {
        TabSet tabSet = (TabSet) component;
        ResponseWriter writer = context.getResponseWriter();
        
        List level1 = tabSet.getChildren();
        
        if (level1.size() < 1) {
            if (LogUtil.infoEnabled()) {
                LogUtil.info(TabSetRenderer.class, "WEBUI0005",
                    new String[] { tabSet.getId() });
            }
            return;
        }
        
        String style = tabSet.getStyle();
        String styleClass = tabSet.getStyleClass();
        
        Theme theme = ThemeUtilities.getTheme(context);
        String lite = theme.getStyleClass(ThemeStyles.TABGROUP);
        
        if (tabSet.isMini() && tabSet.isLite()) {
            if (styleClass != null) {
                styleClass = styleClass + " " + lite;
            } else {
                styleClass = lite;
            }
        }
        
        // <RAVE>
        if(!tabSet.isVisible()) {
            String hiddenStyle = theme.getStyleClass(ThemeStyles.HIDDEN); 
            if(styleClass == null) {
                styleClass = hiddenStyle;
            } else {
                styleClass = style + " " + hiddenStyle; //NOI18N
            }
        }
        // </RAVE>
        
        writer.startElement("div", tabSet);

        if (style != null) {
            writer.writeAttribute("style", style, null); // NOI18N
        }
        if (styleClass != null) {
            writer.writeAttribute("class", styleClass, null); // NOI18N
        }        
        
        Tab selectedTab = tabSet.findChildTab(tabSet.getSelected());
        // <RAVE>
        // String[] args = new String[] {
        //     selectedTab != null ? selectedTab.getText() : ""
        // };
        String selectedTabText = "";
        if (selectedTab != null)
                ConversionUtilities.convertValueToString(selectedTab, selectedTab.getText());
        String[] args = new String[] {selectedTabText};
        // </RAVE>
        
        // render the skip link for a11y
        SkipHyperlink skipLink = new SkipHyperlink();
        skipLink.setId(tabSet.getId() + "skipLink");
        skipLink.setDescription(theme.getMessage("tab.skipTagAltText", args));
        skipLink.encodeBegin(context);

        List level2Tabs = 
            renderLevel(context, tabSet, writer, 1, tabSet.getChildren());
        
        // if there are any level 2 tabs render those now
        if (level2Tabs != null) {
            List level3Tabs =
                renderLevel(context, tabSet, writer, 2, level2Tabs);
            
            // if there are any level 3 tabs render those now
            if (level3Tabs != null) {
                renderLevel(context, tabSet, writer, 3, level3Tabs);
            }
        }
        
        // output the bookmark for the SkipHyperlink
        skipLink.encodeEnd(context);
    }
    
    /**
     * This method renders each of the Tab components in the given level.
     *
     * @param context The current FacesContext
     * @param tabSet The current TabSet component
     * @param writer The current ResponseWriter
     * @param level The level (1, 2 or 3) of the Tab set to be rendered
     * @param currentLevelTabs A List containing the Tab objects for the current
     *  level
     */
    protected List renderLevel(FacesContext context, TabSet tabSet, 
            ResponseWriter writer, int level, List currentLevelTabs) 
            throws IOException {      
        int numTabs = currentLevelTabs.size();
        
        if (numTabs == 0) {
            // no tabs in given level            
            return null;
        }
        
        Theme theme = ThemeUtilities.getTheme(context);
        String selectedTabId = tabSet.getSelected();
        if (selectedTabId == null) {
            // set the first tab child as the selected
            try {
                selectedTabId =
                    ((UIComponent) tabSet.getChildren().get(0)).getId();
                tabSet.setSelected(selectedTabId);
            } catch (Exception e) {
                // gave it a shot but failed... no tab will be selected
            }
        }
        List nextLevelToRender = null;
        
        // get the various level specific tab styles we'll need
        String divStyle = "";
        String tableStyle = "";
        String linkStyle = "";
        String selectedTdStyle = "";
        String selectedTextStyle = "";        
        
        String hidden = theme.getStyleClass(ThemeStyles.HIDDEN);
        
        switch (level) {
            case 1: // get the level 1 tab styles
                if (tabSet.isMini()) {
                    divStyle = theme.getStyleClass(ThemeStyles.MINI_TAB_DIV);
                    tableStyle =
                        theme.getStyleClass(ThemeStyles.MINI_TAB_TABLE);
                    linkStyle = theme.getStyleClass(ThemeStyles.MINI_TAB_LINK);
                    selectedTdStyle = theme.getStyleClass(
                        ThemeStyles.MINI_TAB_TABLE_SELECTED_TD);
                    selectedTextStyle =
                        theme.getStyleClass(ThemeStyles.MINI_TAB_SELECTED_TEXT);
                } else {
                    divStyle = theme.getStyleClass(ThemeStyles.TAB1_DIV);
                    tableStyle =
                        theme.getStyleClass(ThemeStyles.TAB1_TABLE3_NEW);
                    linkStyle = theme.getStyleClass(ThemeStyles.TAB1_LINK);
                    selectedTdStyle =
                        theme.getStyleClass(ThemeStyles.TAB1_TABLE_SELECTED_TD);
                    selectedTextStyle = 
                        theme.getStyleClass(ThemeStyles.TAB1_SELECTED_TEXT_NEW);
                }
                break;
            case 2: // get the level 2 tab styles                
                divStyle = theme.getStyleClass(ThemeStyles.TAB2_DIV);
                tableStyle = theme.getStyleClass(ThemeStyles.TAB2_TABLE3_NEW);
                linkStyle = theme.getStyleClass(ThemeStyles.TAB2_LINK);
                selectedTdStyle =
                    theme.getStyleClass(ThemeStyles.TAB2_TABLE_SELECTED_TD);
                selectedTextStyle = 
                    theme.getStyleClass(ThemeStyles.TAB2_SELECTED_TEXT);
                break;                
            case 3: // get the level 3 tab styles
                divStyle = theme.getStyleClass(ThemeStyles.TAB3_DIV);
                tableStyle = theme.getStyleClass(ThemeStyles.TAB3_TABLE_NEW);
                linkStyle = theme.getStyleClass(ThemeStyles.TAB3_LINK);
                selectedTdStyle =
                    theme.getStyleClass(ThemeStyles.TAB3_TABLE_SELECTED_TD);
                selectedTextStyle = 
                    theme.getStyleClass(ThemeStyles.TAB3_SELECTED_TEXT);
                break;
        }
        
        writer.startElement("div", tabSet);
        writer.writeAttribute("class", divStyle, null); // NOI18N
        writer.startElement("table", tabSet); // NOI18N
        writer.writeAttribute("border", "0", null); // NOI18N
        writer.writeAttribute("cellspacing", "0", null); // NOI18N
        writer.writeAttribute("cellpadding", "0", null); // NOI18N
        writer.writeAttribute("class", tableStyle, null); // NOI18N
        writer.writeAttribute("title", "", null); // NOI18N
        writer.startElement("tr", tabSet);
                
        MethodBinding binding = tabSet.getActionListener();
        
        // render each tab in this level
        for (int i = 0; i < numTabs; i++) {            
            Tab tab = null;
            
            try {
                tab = (Tab) currentLevelTabs.get(i);
            } catch (ClassCastException cce) {
                // expected if a child of current Tab is not another Tab
                continue;
            }
            
            if (!tab.isRendered()) {
                // don't render this tab
                continue;
            }
            
            // apply TabSet ActionListener to each tab that doesn't have one
            if (binding != null && tab.getActionListener() == null) {
                tab.setActionListener(binding);
            }            
            
            // each tab goes in its own table cell
            writer.startElement("td", tabSet);
            
            String newSelectedClass = null;
            String newNonSelectedClass = null;
            if (!tab.isVisible()) {
                newSelectedClass = selectedTdStyle + " " + hidden;
                newNonSelectedClass =  hidden;
            } else {
                newSelectedClass = selectedTdStyle;
                newNonSelectedClass = null;
            }
                
            
            if (selectedTabId != null && isSelected(tab, selectedTabId)) {
                // ensure that the parent tab knows this one is selected
                UIComponent parent = tab.getParent();
                
                if (parent != null && parent instanceof Tab) {
                    ((Tab) parent).setSelectedChildId(tab.getId());
                }
                
                // this tab or one of it's children is selected
                // <RAVE>
                // String label = tab.getText();
                String label = 
                        ConversionUtilities.convertValueToString(tab, tab.getText());
                // </RAVE>
                if (label == null) {
                    label = "";
                }
                writer.writeAttribute("class", newSelectedClass, null); // NOI18N
                writer.startElement("div", tabSet);
                writer.writeAttribute("class", selectedTextStyle, null); // NOI18N
                String titleString = theme.getMessage(
                    "tabSet.selectedTab", new Object[] { label });
                writer.writeAttribute("title", titleString, null);
                
                // record the old, developer specified disabled value
                boolean wasDisabled = tab.isDisabled();
                // selected tab MUST be disabled
                tab.setDisabled(true);
                // render the selected tab
                RenderingUtilities.renderComponent(tab, context);
                // reset with developer specified disabled value
                tab.setDisabled(wasDisabled);
                
                writer.endElement("div");
                
                // if selected has any Tab children, render those as next level
                if (tab.hasTabChildren()) {
                    nextLevelToRender = tab.getChildren();
                }
            } else {
                if (!tab.isVisible()) {
                    writer.writeAttribute("class", newNonSelectedClass, null); // NOI18N
                }
                // not part of current selection
                tab.setStyleClass(linkStyle);
                
                RenderingUtilities.renderComponent(tab, context);      
            }
            
            writer.endElement("td");
            
            // test if a level 3 divider needed
            if (selectedTabId != null && level == 3 && i < numTabs - 1) {
                Tab nextTab = (Tab) currentLevelTabs.get(i + 1);
                
                if (!nextTab.isRendered()) {
                    // the next tab is NOT rendered - check tab after next
                    try {
                        nextTab = (Tab) currentLevelTabs.get(i + 2);
                    } catch (IndexOutOfBoundsException e) {
                        // no more rendered Tabs
                        nextTab = null;
                    }
                }
                
                if (nextTab != null && !tab.getId().equals(selectedTabId) &&
                        !nextTab.getId().equals(selectedTabId)) {
                    String dividerSrc = theme.getIcon(
                            ThemeImages.TAB_DIVIDER).getUrl();
                    writeDivider(tabSet, writer, dividerSrc);
                }                
            }
        }
        
        writer.endElement("tr");
        writer.endElement("table");
        writer.endElement("div");
        
        return nextLevelToRender;
    }
    
    /**
     * Helper function to write a tab dividier in a table cell
     *
     * @param tabSet The current TabSet component
     * @param writer The current ResponseWriter
     * @param src The image src to use for the tab divider
     */
    protected void writeDivider(TabSet tabSet, ResponseWriter writer, String src) 
            throws IOException {
        writer.startElement("td", tabSet);
        writer.startElement("img", tabSet);
        
        writer.writeAttribute("src", src, null); // NOI18N
        writer.writeAttribute("alt", "", null); // NOI18N
        writer.writeAttribute("border", "0", null); // NOI18N
        writer.writeAttribute("height", "20", null); // NOI18N
        writer.writeAttribute("width", "5", null); // NOI18N
        
        writer.endElement("img");
        writer.endElement("td");
    }
    
    /** 
     * Recursive function that determines if the given Tab component or any one
     * of its descendants is the selected tab.
     *
     * @param tab The Tab component to check for selection
     * @param selectedTabId The id of the currently selected Tab
     */
    protected boolean isSelected(Tab tab, String selectedTabId) {        
        if (tab.getId().equals(selectedTabId)) {
            // this tab is the selected tab
            return true;
        }
        
        // check if a descendant is the selected tab
        List subTabs = tab.getChildren();
        boolean descendantSelected = false;
        
        for (int i = 0; i < subTabs.size(); i++) {
            Tab subTab = null;
            
            try {
                subTab = (Tab) subTabs.get(i);
            } catch (ClassCastException cce) {
                // some children may not actually be Tabs
                continue;
            }
            
            descendantSelected = isSelected(subTab, selectedTabId);
            
            if (descendantSelected) {
                break;
            }
        }
        
        return descendantSelected;
    }
}
