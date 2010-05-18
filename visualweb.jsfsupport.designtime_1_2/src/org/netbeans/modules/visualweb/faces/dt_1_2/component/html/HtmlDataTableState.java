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
package org.netbeans.modules.visualweb.faces.dt_1_2.component.html;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.faces.application.Application;
import javax.faces.component.UIColumn;
import javax.faces.component.UICommand;
import javax.faces.component.UISelectItems;
import javax.faces.component.html.HtmlCommandButton;
import javax.faces.component.html.HtmlCommandLink;
import javax.faces.component.html.HtmlMessage;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.component.html.HtmlPanelGrid;
import javax.faces.component.html.HtmlPanelGroup;
import javax.faces.component.html.HtmlSelectOneMenu;
import javax.faces.component.html.HtmlSelectOneRadio;
import javax.faces.context.FacesContext;
import javax.faces.el.ReferenceSyntaxException;
import javax.faces.el.ValueBinding;
import javax.sql.RowSet;
import com.sun.rave.faces.data.DefaultSelectItemsArray;
import com.sun.rave.faces.data.CachedRowSetDataModel;
import org.netbeans.modules.visualweb.faces.dt.util.ComponentBundle;
import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignContext;
import com.sun.rave.designtime.DesignEvent;
import com.sun.rave.designtime.DesignProperty;
import com.sun.rave.designtime.Position;
import com.sun.rave.designtime.faces.FacesDesignBean;
import com.sun.rave.designtime.faces.FacesDesignContext;

public class HtmlDataTableState {

    private static final ComponentBundle bundle = ComponentBundle.getBundle(HtmlDataTableState.class);

    public static final String DEFAULT_VAR_NAME = "currentRow"; //NOI18N
    private static final String COLUMN_ITEM_TEXT = bundle.getMessage("column"); //NOI18N
    private static final String JSFEL_START = "#{"; //NOI18N
    private static final String JSFEL_END = "}"; //NOI18N
    private static final String JSFEL_OPENQUOT = "['"; //NOI18N
    private static final String JSFEL_CLOSEQUOT = "']"; //NOI18N
    private static final String DOT = "."; //NOI18N
    private static final String ACTION_LIVE_EVENT = "action"; //NOI18N
    private static final String[] PAGING_HANDLER_SUFFIXES = {
        "_firstPageAction", "_previousPageAction", "_nextPageAction", "_lastPageAction"}; //NOI18N

    private static final String STYLE_CLASS_HEADER = "list-paging-header"; //NOI18N
    private static final String STYLE_CLASS_FOOTER = "list-paging-footer"; //NOI18N

    private static final String DEFAULT_FIRST_TEXT = "|<"; //NOI18N
    private static final String DEFAULT_PREVIOUS_TEXT = "<-"; //NOI18N
    private static final String DEFAULT_NEXT_TEXT = "->"; //NOI18N
    private static final String DEFAULT_LAST_TEXT = ">|"; //NOI18N
    private static final String DEFAULT_FIRST_URL = "resources/paging_first.gif"; //NOI18N
    private static final String DEFAULT_PREVIOUS_URL = "resources/paging_previous.gif"; //NOI18N
    private static final String DEFAULT_NEXT_URL = "resources/paging_next.gif"; //NOI18N
    private static final String DEFAULT_LAST_URL = "resources/paging_last.gif"; //NOI18N
    private static final String DEFAULT_FIRST_IMG = "paging_first.gif"; //NOI18N
    private static final String DEFAULT_PREVIOUS_IMG = "paging_previous.gif"; //NOI18N
    private static final String DEFAULT_NEXT_IMG = "paging_next.gif"; //NOI18N
    private static final String DEFAULT_LAST_IMG = "paging_last.gif"; //NOI18N

    static final String TOP = bundle.getMessage("top"); //NOI18N
    static final String BOTTOM = bundle.getMessage("bottom"); //NOI18N
    static final String TOP_AND_BOTTOM = bundle.getMessage("topAndBottom"); //NOI18N
    static final String BUTTON_TEXT = bundle.getMessage("navBtnText"); //NOI18N
    static final String BUTTON_IMAGE = bundle.getMessage("navBtnImage"); //NOI18N
    static final String BUTTON_NONE = bundle.getMessage("navBtnNotDispl"); //NOI18N
    static final String ALIGN_CENTER = bundle.getMessage("center"); //NOI18N
    static final String ALIGN_LEFT = bundle.getMessage("left"); //NOI18N
    static final String ALIGN_RIGHT = bundle.getMessage("right"); //NOI18N

    private DesignBean tableBean;

    public HtmlDataTableState(DesignBean tableBean) {
        this.tableBean = tableBean;
        loadState();
    }

    public DesignBean getTableBean() {
        return this.tableBean;
    }

    public static String maybeGenerateRowsetColumns(DesignBean tableBean, String valueExpr) {
        DesignContext context = tableBean.getDesignContext();
        if (context instanceof FacesDesignContext) {
            FacesDesignContext fcontext = (FacesDesignContext)context;
            Object o = fcontext.resolveBindingExpr(valueExpr);
            if (o instanceof RowSet) {
                // ok - we know the user has bound to a RowSet, so do the necessary "magic", and
                // return the value expr that points to the CachedRowSetDataModel
                RowSet rs = (RowSet)o;
                HtmlDataTableState state = new HtmlDataTableState(tableBean);
                state.setSourceInstance(rs);
                state.refreshColumnInfo();
                valueExpr = state.saveStateExceptModelValueExpr();
            }
        }
        return valueExpr;
    }

    /////////////////////////////////// CLASS STRUCTURES ///////////////////////////////////////////

    //////Columns tab///////
    //private String instanceName;                      //instance name to be persisted in source code (such as dataTable1) of the tableBean's underlying HtmlDataTable instance
    private DesignBean sourceBean; //the live bean chosen as the source of data for the table
    public String varName; //the value of the var attribute for this table in the JSP
    public ResultSetInfo rsinfo; //information about a ResultSet, if the sourceBean is of that type
    public DisplayInfo display = new DisplayInfo(); //information about the columns to be displayed
    private DesignBean savedSourceBean; //the sourceBean set by loadState

    ///////Paging tab///////
    public PagingInfo paging = new PagingInfo(); //information gathered from the Paging tab

    public void setSourceBean(DesignBean bean) {
        sourceBean = bean;
    }

    public void refreshColumnInfo() {
        display.clearColumns();
        if (getSourceInstance() instanceof ResultSet) {
            if (rsinfo == null) {
                rsinfo = new ResultSetInfo();
            }
            rsinfo.refreshColumns();
            for (int i = 0; i < rsinfo.getColumnCount(); i++) {
                display.addColumn(rsinfo.getColumn(i));
            }
        } else {
            rsinfo = null;
            display.addNewColumn();
        }
    }

    public DesignBean getSourceBean() {
        return sourceBean;
    }

    public void setSourceInstance(Object sourceInst) {
        DesignBean designBean = getDesignBeanForSourceInstance(sourceInst);
        setSourceBean(designBean);
    }

    public Object getSourceInstance() {
        return sourceBean == null ? null : sourceBean.getInstance();
    }

    public DesignBean getSavedSourceBean() {
        return savedSourceBean;
    }

    private DesignBean getDesignBeanForSourceInstance(Object sourceInst) {
        if (sourceInst == null) {
            return null;
        }
        DesignBean designBean = null;
        DesignContext[] contexts = tableBean.getDesignContext().getProject().getDesignContexts();
        for (int i = 0; i < contexts.length; i++) {
            designBean = contexts[i].getBeanForInstance(sourceInst);
            if (designBean != null) {
                break;
            }
        }
        return designBean;
    }

    private String getStrippedSourceBeanValueRef() {
        if (sourceBean == null) {
            return null;
        }
        DesignContext context = sourceBean.getDesignContext();
        String outer = ((FacesDesignContext)context).getReferenceName();
        return outer + DOT + sourceBean.getInstanceName();
    }

    public class ResultSetInfo {
        private List resultSetColumnList = new ArrayList();

        public void refreshColumns() {
            clearColumns();
            Object sourceInst = getSourceInstance();
            if (!(sourceInst instanceof ResultSet)) {
                return;
            }
            ResultSet rs = (ResultSet)sourceInst;
            try {
                ResultSetMetaData rsmd = rs.getMetaData();
                int cols = rsmd.getColumnCount();
                for (int c = 1; c <= cols; c++) {
                    ResultSetColumn rsc = new ResultSetColumn();
                    rsc.tableName = rsmd.getTableName(c);
                    rsc.columnName = rsmd.getColumnName(c);
                    //rsc.columnClassName = rsmd.getColumnClassName(c);
                    //rsc.columnSqlType = rsmd.getColumnType(c);
                    //rsc.columnSqlTypeName = rsmd.getColumnTypeName(c);
                    addColumn(rsc);
                }
            } catch (Exception x) {
                System.err.println(
                    "HtmlDataTableState.ResultSetInfo.refreshColumns(): probable ResultSetMetaData problem:"); //NOI18N
                x.printStackTrace();
            }
        }

        public ResultSetColumn createColumn() {
            return new ResultSetColumn();
        }

        public void addColumn(ResultSetColumn rsc) {
            resultSetColumnList.add(rsc);
        }

        public void removeColumn(ResultSetColumn rsc) {
            resultSetColumnList.remove(rsc);
        }

        public void clearColumns() {
            resultSetColumnList.clear();
        }

        public ResultSetColumn[] getColumns() {
            return (ResultSetColumn[])resultSetColumnList.toArray(new ResultSetColumn[
                resultSetColumnList.size()]);
        }

        public int getColumnCount() {
            return resultSetColumnList.size();
        }

        public ResultSetColumn getColumn(int index) {
            return (ResultSetColumn)resultSetColumnList.get(index);
        }
    }

    public class ResultSetColumn {
        public String tableName;
        public String columnName;
        //public String columnClassName;
        //public int columnSqlType;
        //public String columnSqlTypeName;
        public String toString() {
            if (tableName != null && tableName.length() > 0) {
                return tableName + DOT + columnName;
            } else {
                return columnName;
            }
        }

        public String getValueRef() {
            return JSFEL_START + varName + JSFEL_OPENQUOT +
                columnName + JSFEL_CLOSEQUOT + JSFEL_END;
        }
    }

    public class PagingInfo {
        //public boolean paging;
        public int rows;

        public String navigation = BUTTON_NONE;
        public boolean navOnTop;
        public boolean navOnBottom;
        public String align = ALIGN_CENTER;

        public boolean firstButton;
        public String firstButtonText = DEFAULT_FIRST_TEXT;
        public String firstButtonUrl = DEFAULT_FIRST_URL;
        public boolean previousButton;
        public String previousButtonText = DEFAULT_PREVIOUS_TEXT;
        public String previousButtonUrl = DEFAULT_PREVIOUS_URL;
        public boolean nextButton;
        public String nextButtonText = DEFAULT_NEXT_TEXT;
        public String nextButtonUrl = DEFAULT_NEXT_URL;
        public boolean lastButton;
        public String lastButtonText = DEFAULT_LAST_TEXT;
        public String lastButtonUrl = DEFAULT_LAST_URL;
    }

    public class DisplayInfo {
        private static final int MAX_DISPLAY_COLUMNS = 9999;
        private List displayColumnList = new ArrayList();
        private DisplayColumn[] savedColumns;

        public void addColumn(ResultSetColumn rsc) {
            DisplayColumn dc = new DisplayColumn();
            //let dc.columnInstanceName be null
            dc.compClassName = HtmlOutputText.class.getName();
            dc.compValueRef = rsc.getValueRef();
            dc.headerText = rsc.columnName;
            //let dc.footerText be null
            dc.itemText = rsc.toString();
            dc.hasResultSetColumnPeer = true;
            addColumn(dc);
        }

        public DisplayColumn addNewColumn() {
            DisplayColumn dc = new DisplayColumn();
            List names = new ArrayList();
            for (int i = 0; i < displayColumnList.size(); i++) {
                String aColumnInstanceName = ((DisplayColumn)displayColumnList.get(i)).
                    columnInstanceName;
                if (aColumnInstanceName != null) {
                    names.add(aColumnInstanceName);
                }
            }
            for (int i = 1; i < MAX_DISPLAY_COLUMNS; i++) {
                if (!names.contains(COLUMN_ITEM_TEXT + i)) {
                    dc.columnInstanceName = COLUMN_ITEM_TEXT + i;
                    break;
                }
            }
            dc.compClassName = HtmlOutputText.class.getName();
            //let dc.compValueRef be null
            dc.headerText = dc.columnInstanceName;
            //let dc.footerText be null
            dc.itemText = dc.columnInstanceName;
            addColumn(dc);
            return dc;
        }

        public DisplayColumn[] getColumns() {
            return (DisplayColumn[])displayColumnList.toArray(new DisplayColumn[displayColumnList.
                size()]);
        }

        public void addColumn(DisplayColumn dc) {
            displayColumnList.add(dc);
        }

        public void removeColumn(DisplayColumn dc) {
            displayColumnList.remove(dc);
        }

        public void clearColumns() {
            displayColumnList.clear();
        }

        public int getColumnCount() {
            return displayColumnList.size();
        }

        public DisplayColumn getColumn(int index) {
            return (DisplayColumn)displayColumnList.get(index);
        }

        public boolean canMoveUp(DisplayColumn dc) {
            return displayColumnList.indexOf(dc) > 0;
        }

        public void moveColumnUp(DisplayColumn dc) {
            int idx = displayColumnList.indexOf(dc);
            if (idx > 0) {
                displayColumnList.remove(dc);
                displayColumnList.add(idx - 1, dc);
            }
        }

        public boolean canMoveDown(DisplayColumn dc) {
            int idx = displayColumnList.indexOf(dc);
            return idx > -1 && idx < displayColumnList.size() - 1;
        }

        public void moveColumnDown(DisplayColumn dc) {
            int idx = displayColumnList.indexOf(dc);
            if (idx > -1 && idx < displayColumnList.size() - 1) {
                displayColumnList.remove(dc);
                displayColumnList.add(idx + 1, dc);
            }
        }

        private void saveDisplayColumns() {
            savedColumns = getColumns();
        }
    }

    public class DisplayColumn {
        public DesignBean columnDesignBean; //live bean holding a UIColumn object
        public String columnInstanceName; //instance name of UIColumn in source code, like column1.
        //public String compInstanceName;          //the field component instance's instance name in source code, like outputText2 (instance of type HtmlOutputText)
        public String compClassName; //like javax.faces.component.html.HtmlOutputText or other UIComponent subclass. used to create a DesignBean to wrap the component
        public String compValueRef; //like #{currentRow['PERSONID']} or #{currentRow.donkey}
        public String compSIValueRef; //if comp class is HtmlSelectOneMenu, then the value attribute of its select items is this jsf-el expression
        public String headerText;
        //public boolean headerLink;               //whether header text is an hypertext link to sort that column
        public String footerText;
        public String itemText; //what gets displayed in the list
        public boolean hasResultSetColumnPeer;
        public String toString() {
            return itemText;
        }
    }

    private String getStrippedJsfEL(String valueRef) {
        if (valueRef == null) {
            return null;
        }
        if (!valueRef.startsWith(JSFEL_START) || !valueRef.endsWith(JSFEL_END)) {
            return valueRef;
        }
        return valueRef.substring(JSFEL_START.length(), valueRef.indexOf(JSFEL_END));
    }

    private String getTextFromDesignProperty(DesignProperty vp) {
        return vp == null ? null : vp.getValueSource();
    }

    boolean isJsfELSyntax(String s) {
        if (s == null) {
            return false;
        }
        return s.startsWith(JSFEL_START) && s.endsWith(JSFEL_END);
    }

    boolean validateJsfEL(String s) {
        //right now it makes sense for the dialog to relax its restrictions, since QE has found cases where
        //this strategy is prohibitively restrictive, i.e., it returns false though the expression would work at runtime.
        //for instance, see 6183023
        FacesDesignContext flc = (FacesDesignContext)tableBean.getDesignContext();
        FacesContext facesContext = flc.getFacesContext();
        Application facesApp = facesContext.getApplication();
        try {
            ValueBinding vb = facesApp.createValueBinding(s);
            Object result = vb.getValue(facesContext);
            return true;
        } catch (ReferenceSyntaxException rse) {
            return false;
        }
        /*
        //tableBean.getDesignContext() must be an instance of FacesDesignContext. otherwise it makes no sense to call this method.
        if (!isJsfELSyntax(s)) {
            return false;
        }

        if (!(tableBean.getDesignContext() instanceof FacesDesignContext)) {
            return false;
        }
        FacesDesignContext flc = (FacesDesignContext)tableBean.getDesignContext();
        FacesContext facesContext = flc.getFacesContext();
        Application facesApp = facesContext.getApplication();
        //if sourceInstance is a ResultSet and is relative to varName, then we know the absolute expression and can use it for more stringent validation
        if (getSourceInstance() instanceof ResultSet && s.startsWith(JSFEL_START + varName)) {
            DesignContext sbContext = sourceBean.getDesignContext();
            s = JSFEL_START + ((FacesDesignContext)sbContext).getReferenceName() + DOT +
                sourceBean.getInstanceName() + DOT + getStrippedJsfEL(s) + JSFEL_END;
        }
        try {
            ValueBinding vb = facesApp.createValueBinding(s);
            Object result = vb.getValue(facesContext);
            return true;
        } catch (EvaluationException ee) {
            return false;
        }
        */
    }

    private DesignEvent getActionEvent(DesignBean commandBean) {
        DesignEvent[] events = commandBean.getEvents();
        for (int e = 0; e < events.length; e++) {
            if (ACTION_LIVE_EVENT.equals(events[e].getEventDescriptor().getName())) {
                return events[e];
            }
        }
        return null;
    }

    private DesignBean[] getPagingComponents(DesignBean panel) {
        //facet can be null
        DesignBean[] pagingComponents = new DesignBean[PAGING_HANDLER_SUFFIXES.length]; //a place to put the paging buttons or links for the panel
        if (panel == null) {
            return pagingComponents;
        }
        Object panelInstance = panel.getInstance();
        if (!(panelInstance instanceof HtmlPanelGrid || panelInstance instanceof HtmlPanelGroup)) {
            return pagingComponents;
        }
        boolean panelContainsPagingComponents = false;
        DesignBean[] children = panel.getChildBeans();
        childrenLoop:
            for (int i = 0; i < children.length; i++) { //go through the components within the panel

            if (!(children[i].getInstance() instanceof UICommand)) {
                continue; //if this component is not a button or link, go to the next component
            }
            DesignEvent actionEvent = getActionEvent(children[i]); //get the action event associated with this component
            if (actionEvent == null) {
                continue; //go to the next component
            }
            String actionEventHandler = actionEvent.getHandlerName(); //the handler method name for the action event
            if (actionEventHandler == null) {
                continue; //go to the next component
            }
            for (int j = 0; j < PAGING_HANDLER_SUFFIXES.length; j++) {
                //if we already have a component for this paging handler suffix, go to the next suffix
                if (pagingComponents[j] != null) {
                    continue;
                }
                if (actionEventHandler.equals(tableBean.getInstanceName() +
                    PAGING_HANDLER_SUFFIXES[j])) {
                    //children[i]'s action handler method is the paging handler for this suffix
                    pagingComponents[j] = children[i];
                    panelContainsPagingComponents = true;
                    continue childrenLoop;
                }
            }
        }
        if (panelContainsPagingComponents) {
            if (panelInstance instanceof HtmlPanelGroup) {
                DesignProperty panelStyleProp = panel.getProperty("style");   //NOI18N
                if (panelStyleProp != null) {
                    String panelStyle = panelStyleProp.getValueSource();
                    if (panelStyle != null) {
                        String patternPrefix = ".*?text-align\\s*:\\s*";   //NOI18N
                        if (panelStyle.matches(patternPrefix + "left"  //NOI18N
                            )) {
                            paging.align = ALIGN_LEFT;
                        }
                        else if (panelStyle.matches(patternPrefix + "center"   //NOI18N
                            )) {
                            paging.align = ALIGN_CENTER;
                        }
                        else if (panelStyle.matches(patternPrefix + "right"    //NOI18N
                            )) {
                            paging.align = ALIGN_RIGHT;
                        }
                    }
                }
            }
        }
        return pagingComponents;
    }

    private void setBasicPagingVariables(DesignBean[] pagingComponents, String facetStr) {
        //find out if there's at least one paging component for this facet. pagingComponents should not be null.
        boolean atLeastOne = false;
        for (int j = 0; j < pagingComponents.length; j++) {
            if (pagingComponents[j] != null) {
                atLeastOne = true;
                break;
            }
        }
        //set paging variables
        if (atLeastOne) {
            if ("header".equals(facetStr)) { //NOI18N
                paging.navOnTop = true;
            } else {
                paging.navOnBottom = true;
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void loadState() {
        try {
            ////set instance vars
            //instanceName = tableBean.getInstanceName();
            Object sourceInst = tableBean.getProperty("value").getValue(); //NOI18N
            if (sourceInst instanceof CachedRowSetDataModel) {
                sourceInst = ((CachedRowSetDataModel)sourceInst).getWrappedData();
            }
            sourceBean = getDesignBeanForSourceInstance(sourceInst); //do not call setSourceBean(...) here! use sourceBean = ...!
            savedSourceBean = sourceBean;
            varName = tableBean.getProperty("var").getValueSource(); //NOI18N
            if (varName == null || varName.length() == 0) {
                varName = DEFAULT_VAR_NAME;

                //paging stuff
            }
            Object pagingRows = tableBean.getProperty("rows").getValue(); //NOI18N
            if (pagingRows != null) {
                paging.rows = ((Integer)pagingRows).intValue(); //number of rows per "page" if paging is enabled on this table
            }
            if (tableBean instanceof FacesDesignBean) {
                paging.align = ALIGN_CENTER;
                paging.navOnTop = false;
                paging.navOnBottom = false;
                FacesDesignBean ftableBean = (FacesDesignBean)tableBean;
                DesignBean header = ftableBean.getFacet("header"); //can be null //NOI18N
                DesignBean footer = ftableBean.getFacet("footer"); //can be null //NOI18N
                DesignBean[] headerPagingComponents = getPagingComponents(header); //won't be null
                setBasicPagingVariables(headerPagingComponents, "header"); //NOI18N
                DesignBean[] footerPagingComponents = getPagingComponents(footer); //won't be null
                setBasicPagingVariables(footerPagingComponents, "footer"); //NOI18N

                //merge the header and footer paging component arrays
                DesignBean[] mergedPagingComponents = headerPagingComponents;
                headerPagingComponents = null;
                for (int i = 0; i < footerPagingComponents.length; i++) {
                    if (footerPagingComponents[i] != null) {
                        mergedPagingComponents[i] = footerPagingComponents[i];
                    }
                }
                footerPagingComponents = null;

                /*
                 System.err.println("HtmlDataTableState.loadState: mergedPagingComponents:");  //NOI18N
                                 for (int m = 0; m < mergedPagingComponents.length; m++) {
                    System.err.println("\t" + m + ":" + mergedPagingComponents[m] + "!");  //NOI18N
                                 }
                 */

                paging.navigation = BUTTON_NONE;
                if (mergedPagingComponents[0] != null) {
                    paging.firstButton = true;
                    //we have a button. upgrade to BUTTON_TEXT.
                    paging.navigation = BUTTON_TEXT;
                    String buttonText = (String)mergedPagingComponents[0].getProperty("value").getValueSource(); //NOI18N
                    paging.firstButtonText = buttonText == null || buttonText.length() < 1 ? DEFAULT_FIRST_TEXT : buttonText;
                    String buttonImageUrl = (String)mergedPagingComponents[0].getProperty("image").getValueSource(); //NOI18N
                    boolean buttonImageUrlEmpty = buttonImageUrl == null || buttonImageUrl.length() < 1;
                    paging.firstButtonUrl = buttonImageUrlEmpty ? DEFAULT_FIRST_URL : buttonImageUrl;
                    if (!buttonImageUrlEmpty) {
                        paging.navigation = BUTTON_IMAGE;
                    }
                }
                else {
                	paging.firstButton = false;
                }
                if (mergedPagingComponents[1] != null) {
                    paging.previousButton = true;
                    //we have a button. so if paging.navigation was BUTTON_NONE, upgrade to BUTTON_TEXT.
                    if (BUTTON_NONE.equals(paging.navigation)) {
                        paging.navigation = BUTTON_TEXT;
                    }
                    String buttonText = (String)mergedPagingComponents[1].getProperty("value").getValueSource(); //NOI18N
                    paging.previousButtonText = buttonText == null || buttonText.length() < 1 ? DEFAULT_PREVIOUS_TEXT : buttonText;
                    String buttonImageUrl = (String)mergedPagingComponents[1].getProperty("image").getValueSource(); //NOI18N
                    boolean buttonImageUrlEmpty = buttonImageUrl == null || buttonImageUrl.length() < 1;
                    paging.previousButtonUrl = buttonImageUrlEmpty ? DEFAULT_PREVIOUS_URL : buttonImageUrl;
                    if (!buttonImageUrlEmpty) {
                        paging.navigation = BUTTON_IMAGE;
                    }
                }
                else {
                	paging.previousButton = false;
                }
                if (mergedPagingComponents[2] != null) {
                    paging.nextButton = true;
                    if (BUTTON_NONE.equals(paging.navigation)) {
                        paging.navigation = BUTTON_TEXT;
                    }
                    String buttonText = (String)mergedPagingComponents[2].getProperty("value").getValueSource(); //NOI18N
                    paging.nextButtonText = buttonText == null || buttonText.length() < 1 ? DEFAULT_NEXT_TEXT : buttonText;
                    String buttonImageUrl = (String)mergedPagingComponents[2].getProperty("image").getValueSource(); //NOI18N
                    boolean buttonImageUrlEmpty = buttonImageUrl == null || buttonImageUrl.length() < 1;
                    paging.nextButtonUrl = buttonImageUrlEmpty ? DEFAULT_NEXT_URL : buttonImageUrl;
                    if (!buttonImageUrlEmpty) {
                        paging.navigation = BUTTON_IMAGE;
                    }
                }
                else {
                	paging.nextButton = false;
                }
                if (mergedPagingComponents[3] != null) {
                    paging.lastButton = true;
                    if (BUTTON_NONE.equals(paging.navigation)) {
                        paging.navigation = BUTTON_TEXT;
                    }
                    String buttonText = (String)mergedPagingComponents[3].getProperty("value").getValueSource(); //NOI18N
                    paging.lastButtonText = buttonText == null || buttonText.length() < 1 ? DEFAULT_LAST_TEXT : buttonText;
                    String buttonImageUrl = (String)mergedPagingComponents[3].getProperty("image").getValueSource(); //NOI18N
                    boolean buttonImageUrlEmpty = buttonImageUrl == null || buttonImageUrl.length() < 1;
                    paging.lastButtonUrl = buttonImageUrlEmpty ? DEFAULT_LAST_URL : buttonImageUrl;
                    if (!buttonImageUrlEmpty) {
                        paging.navigation = BUTTON_IMAGE;
                    }
                }
                else {
                	paging.lastButton = false;
                }
            }
            loadColumnState();
        } catch (Exception x) {
            System.err.println("error in HtmlDataTableState.loadState():"); //NOI18N
            x.printStackTrace();
            //System.err.println("dumping state of HtmlDataTableState:");    //NOI18N
            //dumpState(System.err);
        }
    }

    void loadColumnState() {
        try {
            Object sourceInst = getSourceInstance();

            //update rsinfo if applicable
            if (sourceInst instanceof ResultSet) {
                if (rsinfo == null) {
                    rsinfo = new ResultSetInfo();
                }
                rsinfo.refreshColumns();
            } else {
                rsinfo = null;
            }

            //update display instance variable
            display.clearColumns();
            DesignBean[] uicolumns = tableBean.getChildBeans();
            for (int i = 0; uicolumns != null && i < uicolumns.length; i++) {
                if (!(uicolumns[i].getInstance() instanceof UIColumn)) {
                    continue;
                }
                DisplayColumn dc = new DisplayColumn();
                dc.columnDesignBean = uicolumns[i];
                dc.columnInstanceName = uicolumns[i].getInstanceName();
                DesignBean[] comps = uicolumns[i].getChildBeans();

                DesignBean header = null;
                DesignBean footer = null;

                // get the header and footer text from the facets
                if (uicolumns[i] instanceof FacesDesignBean) {
                    FacesDesignBean flb = (FacesDesignBean)uicolumns[i];
                    header = flb.getFacet("header"); //NOI18N
                    if (header != null && header.getInstance() instanceof HtmlOutputText) {
                        DesignProperty vp = header.getProperty("value"); //NOI18N
                        dc.headerText = getTextFromDesignProperty(vp);
                    }
                    footer = flb.getFacet("footer"); //NOI18N
                    if (footer != null && footer.getInstance() instanceof HtmlOutputText) {
                        DesignProperty vp = footer.getProperty("value"); //NOI18N
                        dc.footerText = getTextFromDesignProperty(vp);
                    }
                }

                if (comps != null && comps.length >= 1) {
                    DesignBean comp = null;
                    for (int c = 0; c < comps.length; c++) {
                        if (comps[c] != null && comps[c] != header && comps[c] != footer) {
                            comp = comps[c];
                            break;
                        }
                    }
                    if (comp != null) {
                        //dc.compInstanceName = comp.getInstanceName();
                        dc.compClassName = comp.getInstance().getClass().getName();
                        DesignProperty valProp = comp.getProperty("value"); //NOI18N
                        dc.compValueRef = valProp != null ? valProp.getValueSource() : null; //NOI18N
                        if (dc.compClassName.equals(HtmlSelectOneMenu.class.getName()) || dc.compClassName.equals(HtmlSelectOneRadio.class.getName())) {
                            DesignBean[] compChildren = comp.getChildBeans();
                            if (compChildren != null && compChildren.length > 0) {
                                if (compChildren[0] != null &&
                                    (compChildren[0].getInstance() instanceof UISelectItems)) {
                                    DesignProperty siValueProp = compChildren[0].getProperty("value"); //NOI18N
                                    if (siValueProp != null) {
                                        dc.compSIValueRef = siValueProp.getValueSource();
                                    }
                                }
                            }
                        }
                        else if (dc.compClassName.equals(HtmlCommandLink.class.getName())) {
                            DesignBean[] compChildren = comp.getChildBeans();
                            if (compChildren != null && compChildren.length > 0) {
                                if (compChildren[0] != null &&
                                    (compChildren[0].getInstance() instanceof HtmlOutputText)) {
                                    DesignProperty childValueProp = compChildren[0].getProperty("value"); //NOI18N
                                    if (childValueProp != null) {
                                        dc.compValueRef = childValueProp.getValueSource();
                                    }
                                }
                            }
                        }
                    }
                }

                //set dc.itemText
                if (sourceInst instanceof ResultSet && dc.compValueRef != null) {
                    ResultSetColumn[] rsca = rsinfo.getColumns();
                    for (int j = 0; j < rsca.length; j++) {
                        if (dc.compValueRef.equals(rsca[j].getValueRef())) {
                            dc.itemText = rsca[j].toString();
                            dc.hasResultSetColumnPeer = true;
                            break;
                        }
                    }
                }
                if (dc.itemText == null) {
                    dc.itemText = dc.columnInstanceName;
                }

                display.addColumn(dc);
            }

            display.saveDisplayColumns();
        } catch (Exception x) {
            System.err.println("error in HtmlDataTableState.loadColumnState():"); //NOI18N
            x.printStackTrace();
            //System.err.println("dumping state of HtmlDataTableState:");    //NOI18N
            //dumpState(System.err);
        }
    }

    protected boolean dontSaveModelValueExpr = false;
    protected String modelValueExpr = null;
    public String saveStateExceptModelValueExpr() {
        this.dontSaveModelValueExpr = true;
        saveState();
        this.dontSaveModelValueExpr = false;
        return this.modelValueExpr;
    }

    public void saveState() {
        //System.err.println("in HtmlDataTableState.saveState(): dumping state of HtmlDataTableState:");    //NOI18N
        //dumpState(System.err);

        DesignContext context = tableBean.getDesignContext();
        String outer = ((FacesDesignContext)context).getReferenceName();
        FacesDesignContext flc = null;
        if (context instanceof FacesDesignContext) {
            flc = (FacesDesignContext)context;
        }

        DisplayColumn[] dca = display.getColumns();

        /*
                 //copy over columnDesignBean from savedColumns to corresponding display columns as needed
                 if (sourceBean == savedSourceBean && getSourceInstance() instanceof ResultSet) {
            for (int d = 0; d < dca.length; d++) {
                DisplayColumn dc = dca[d];
                if (dc == null || dc.columnDesignBean != null || dc.compValueRef == null) continue;
                DisplayColumn[] sca = display.savedColumns;
                for (int s = 0; s < sca.length; s++) {
                    DisplayColumn sc = sca[s];
                    if (sc == null) continue;   //just defensive
                    if (dc.compValueRef.equals(sc.compValueRef)) {
                        dc.columnDesignBean = sc.columnDesignBean;
                    }
                }
            }
                 }
         */

        //nuke just the deleted columns (kids)
        DesignBean[] kids = tableBean.getChildBeans();
        for (int k = 0; kids != null && k < kids.length; k++) {
            DesignBean kid = kids[k];
            if (kid == null) {
                continue;
            }
            boolean keep = false;
            //go through display columns. keep this kid if we need it.
            for (int d = 0; d < dca.length; d++) {
                DisplayColumn dc = dca[d];
                if (kid == dc.columnDesignBean) {
                    keep = true;
                    break;
                }
            }
            if (!keep) {
                context.deleteBean(kid);
            }
        }

        /*
         * also nuke the facets. Note: This will irretrievably nuke any components the user has hand-coded in the facets!
         * the alternative is to make getPagingComponents return something that holds more than 4 buttons/links.
         * (For instance, there might be 10 buttons in the facet with 3 of them having the action event handler method dataTable1_previousPageAction.)
         * and then only nuke the *paging* components in the facet and then the whole facet only if there are no components left
         */
        if (tableBean instanceof FacesDesignBean) {
            DesignBean facet = ((FacesDesignBean)tableBean).getFacet("header"); //NOI18N
            if (facet != null) {
                context.deleteBean(facet);
            }
            facet = ((FacesDesignBean)tableBean).getFacet("footer"); //NOI18N
            if (facet != null) {
                context.deleteBean(facet);
                //System.err.println("HtmlDataTableState.saveState: header exists?: " + ((FacesDesignBean)tableBean).getFacet("header")); //NOI18N
                //System.err.println("HtmlDataTableState.saveState: footer exists?: " + ((FacesDesignBean)tableBean).getFacet("footer")); //NOI18N
            }
        }

        // now create new children...
        for (int i = 0; i < dca.length; i++) {
            DisplayColumn dc = dca[i];
            DesignBean uicolumn = dc.columnDesignBean;
            if (uicolumn == null) {
                uicolumn = context.createBean(UIColumn.class.getName(), tableBean, new Position(i));
                uicolumn.setInstanceName(dc.columnInstanceName, true);
            } else {
                context.moveBean(uicolumn, tableBean, new Position(i));
            }

            DesignBean comp = null;
            DesignBean header = null;
            DesignBean footer = null;

            if (uicolumn instanceof FacesDesignBean) {
                FacesDesignBean fuicolumn = (FacesDesignBean)uicolumn;
                header = fuicolumn.getFacet("header"); //NOI18N
                footer = fuicolumn.getFacet("footer"); //NOI18N
            }

            DesignBean[] comps = uicolumn.getChildBeans();
            for (int c = 0; comps != null && c < comps.length; c++) {
                if (comps[c] != null && comps[c] != header && comps[c] != footer) {
                    comp = comps[c];
                    break;
                }
            }

            // if we have a first comp of the wrong type, wipe it out
            if (comp != null) {
                Object compInstance = comp.getInstance();
                if (compInstance == null ||
                    !compInstance.getClass().getName().equals(dc.compClassName)) {
                    //first comp of wrong type. wipe it out.
                    context.deleteBean(comp);
                    comp = null;
                }
            }

            if (comp == null) {
                if (dc.compClassName == null) {
                    dc.compClassName = HtmlOutputText.class.getName();
                }
                comp = context.createBean(dc.compClassName, uicolumn, new Position(0));
                if (dc.compClassName.equals(HtmlSelectOneMenu.class.getName()) || dc.compClassName.equals(HtmlSelectOneRadio.class.getName())) {
                    DesignBean selectitems = context.createBean(UISelectItems.class.getName(), comp, null);
                    selectitems.setInstanceName(comp.getInstanceName() + "SelectItems", true); //NOI18N
                    DesignBean items = context.createBean(DefaultSelectItemsArray.class.getName(), null, null);
                    items.setInstanceName(comp.getInstanceName() + "DefaultItems", true); //NOI18N
                    selectitems.getProperty("value").setValueSource(JSFEL_START + outer + DOT +  //NOI18N
                        items.getInstanceName() + JSFEL_END); //NOI18N
                }
                else if (dc.compClassName.equals(HtmlCommandLink.class.getName())) {
                    DesignBean childText = comp.getDesignContext().createBean(HtmlOutputText.class.getName(), comp, null);
                    childText.setInstanceName(comp.getInstanceName() + "Text", true);  //NOI18N
                }
                else if (dc.compClassName.equals(HtmlMessage.class.getName())) {
                    comp.getProperty("for").setValue(""); //NOI18N
                    comp.getProperty("showDetail").setValue(Boolean.FALSE); //NOI18N
                    comp.getProperty("showSummary").setValue(Boolean.TRUE); //NOI18N
                    comp.getProperty("infoClass").setValue("infoMessage"); // NOI18N
                    comp.getProperty("warnClass").setValue("warnMessage"); // NOI18N
                    comp.getProperty("errorClass").setValue("errorMessage"); // NOI18N
                    comp.getProperty("fatalClass").setValue("fatalMessage"); // NOI18N
                }
            }

            DesignProperty valProp = comp.getProperty("value");  //NOI18N
            if (dc.compClassName.equals(HtmlCommandLink.class.getName())) {
                if (valProp != null) {
                    valProp.unset();
                }
                DesignBean childText = null;    //the HtmlOutputText child of the link action, if it exists
                DesignBean[] compChildren = comp.getChildBeans();
                if (compChildren != null && compChildren.length > 0) {
                    if (compChildren[0] != null &&
                        (compChildren[0].getInstance() instanceof HtmlOutputText)) {
                        childText = compChildren[0];
                    }
                }
                if (childText != null) {
                    String childTextValue = bundle.getMessage("linkAction"); //NOI18N
                    if (dc.compValueRef != null) {
                        childTextValue = dc.compValueRef;
                    }
                    if (isJsfELSyntax(childTextValue)){
                        childText.getProperty("value").setValueSource(childTextValue);  //NOI18N
                    }else{
                        childText.getProperty("value").setValue(childTextValue);  //NOI18N
                    }
                }
            } else if (dc.compValueRef == null || dc.compValueRef.length() == 0) {
                if (valProp != null) {
                    valProp.unset();
                }
            } else {
                if (valProp != null) {
                    if (isJsfELSyntax(dc.compValueRef)){
                        valProp.setValueSource(dc.compValueRef);  //NOI18N
                    }else{
                        valProp.setValue(dc.compValueRef);
                    }
                }
            }

            // setup the header and footer facets
            if (header == null && dc.headerText != null && flc != null) {
                if (flc.canCreateFacet("header", HtmlOutputText.class.getName(), uicolumn)) { //NOI18N
                    header = flc.createFacet("header", HtmlOutputText.class.getName(), uicolumn); //NOI18N
                }
            }
            if (header != null) {
                DesignProperty vp = header.getProperty("value"); //NOI18N
                if (vp != null) {
                    if (isJsfELSyntax(dc.headerText)) {
                        vp.setValueSource(dc.headerText);
                    } else {
                        vp.setValue(dc.headerText);
                    }
                }
            }
            if (footer == null && dc.footerText != null && flc != null) {
                if (flc.canCreateFacet("footer", HtmlOutputText.class.getName(), uicolumn)) { //NOI18N
                    footer = flc.createFacet("footer", HtmlOutputText.class.getName(), uicolumn); //NOI18N
                }
            }
            if (footer != null) {
                DesignProperty vp = footer.getProperty("value"); //NOI18N
                if (vp != null) {
                    if (isJsfELSyntax(dc.footerText)) {
                        vp.setValueSource(dc.footerText);
                    } else {
                        vp.setValue(dc.footerText);
                    }
                }
            }
        }

        // If the source instance is a RowSet ...
        DesignBean dm = null;
        if (sourceBean != null) {
            Object sourceInst = sourceBean.getInstance();
            if (sourceInst instanceof RowSet) {

                // Create or replace the corresponding datamodel
                DesignContext lc = tableBean.getDesignContext();
                String dmName = tableBean.getInstanceName() + "Model"; //NOI18N
                dm = lc.getBeanByName(dmName);
                if (dm != null) {
                    dm.getDesignContext().deleteBean(dm);
                }
                dm = lc.createBean(CachedRowSetDataModel.class.getName(), null, null);
                dm.setInstanceName(dmName);

                // Configure properties on the new datamodel
                dm.getProperty("cachedRowSet").setValue(sourceBean.getInstance()); // NOI18N

            }
        }

        //now properties
        //System.err.println("HtmlTableDataState.saveState: getStrippedSourceBeanValueRef():" + getStrippedSourceBeanValueRef() + "!");    //NOI18N
        if (sourceBean == null) { // No value binding at all
            if (dontSaveModelValueExpr) {
                this.modelValueExpr = null;
            } else {
                tableBean.getProperty("value").unset(); //NOI18N
            }
        } else if (dm != null) { // Bound to a RowSet
            if (dontSaveModelValueExpr) {
                DesignContext dmc = dm.getDesignContext();
                if (dmc instanceof FacesDesignContext) {
                    this.modelValueExpr =
                        JSFEL_START + ((FacesDesignContext)dmc).getReferenceName() + DOT +
                        dm.getInstanceName() + JSFEL_END;
                } else {
                    this.modelValueExpr =
                        JSFEL_START + dmc.getDisplayName() + DOT +
                        dm.getInstanceName() + JSFEL_END;
                }
            } else {
                tableBean.getProperty("value").setValue(dm.getInstance()); // NOI18N
            }
        } else { // Bound to something else
            String vref = JSFEL_START + getStrippedSourceBeanValueRef() + JSFEL_END;
            if (dontSaveModelValueExpr) {
                this.modelValueExpr = vref;
            } else {
                if (isJsfELSyntax(vref)) {
                    tableBean.getProperty("value").setValueSource(vref); //NOI18N
                } else {
                    tableBean.getProperty("value").setValue(vref); //NOI18N
                }
            }
        }
        savedSourceBean = sourceBean;
        display.saveDisplayColumns();
        tableBean.getProperty("var").setValue(varName); //NOI18N
        try {
            tableBean.getProperty("rows").setValue(new Integer(paging.rows)); //NOI18N
        } catch (Exception x) {}

        //now navigation buttons
        boolean atLeastOneButtonRequested = paging.firstButton || paging.previousButton ||
            paging.nextButton || paging.lastButton;
        if (paging.rows > 0 && (BUTTON_TEXT.equals(paging.navigation) || BUTTON_IMAGE.equals(paging.navigation)) && atLeastOneButtonRequested) {
            if (paging.navOnTop) {
                createNavPanel("header"); //NOI18N
            }
            if (paging.navOnBottom) {
                createNavPanel("footer"); //NOI18N
            }
        }
    }

    /**
     * Create a facet as a DesignBean as well as all buttons within the facet and set the buttons' action event handler method name and source code. Called by saveState.
     */
    private void createNavPanel(String facetStr) {
        DesignContext context = tableBean.getDesignContext();
        FacesDesignContext flc = null;
        if (context instanceof FacesDesignContext) {
            flc = (FacesDesignContext)context;
        }
        if (!flc.canCreateFacet(facetStr, HtmlPanelGrid.class.getName(), tableBean)) {
            System.err.println(
                "HtmlTableDataState.createNavPanel: error: could not create facet of type " +  //NOI18N
                facetStr + " for tableBean " + tableBean); //NOI18N
            return;
        }

        //create a facet
        DesignBean facet = flc.createFacet(facetStr, HtmlPanelGroup.class.getName(), tableBean);

        //give it a style
        DesignProperty facetProp = facet.getProperty("style");  //NOI18N
        if (facetProp != null) {
            String stylePrefix = "display: block; text-align: ";  //NOI18N
            String alignValue = "center"; //NOI18N
            if (ALIGN_LEFT.equals(paging.align)) {
                alignValue = "left";    //NOI18N
            }
            else if (ALIGN_RIGHT.equals(paging.align)) {
                alignValue = "right";   //NOI18N
            }
            facetProp.setValue(stylePrefix + alignValue);
           
        }
        //set the style class
        facetProp = facet.getProperty("styleClass");    //NOI18N
        if (facetProp != null) {
            facetProp.setValue("footer".equals(facetStr) ? STYLE_CLASS_FOOTER : STYLE_CLASS_HEADER);  //NOI18N
        }

        boolean[] buttonRequests = {
            paging.firstButton, paging.previousButton, paging.nextButton, paging.lastButton};
        String[] buttonLabels = {
            paging.firstButtonText, paging.previousButtonText, paging.nextButtonText, paging.lastButtonText};
        String[] buttonUrls = {
                paging.firstButtonUrl, paging.previousButtonUrl, paging.nextButtonUrl, paging.lastButtonUrl};
        String[] navButtonNames = {
            "FirstButton", "PreviousButton", "NextButton", "LastButton"}; //NOI18N
        String[] defaultButtonUrls = {DEFAULT_FIRST_URL, DEFAULT_PREVIOUS_URL, DEFAULT_NEXT_URL, DEFAULT_LAST_URL};
        String[] defaultButtonImgs = {DEFAULT_FIRST_IMG, DEFAULT_PREVIOUS_IMG, DEFAULT_NEXT_IMG, DEFAULT_LAST_IMG};
        String facetPrefix = facetStr.equals("header") ? "Header" : "Footer"; //NOI18N

        for (int i = 0; i < buttonRequests.length; i++) {
            if (buttonRequests[i]) {
                //create a button
                DesignBean btn = context.createBean(HtmlCommandButton.class.getName(), facet, null);
                btn.setInstanceName(tableBean.getInstanceName() + facetPrefix + navButtonNames[i]); //NOI18N
                btn.getProperty("immediate").setValue(Boolean.TRUE); //NOI18N
                if (BUTTON_TEXT.equals(paging.navigation) && buttonLabels[i] != null && buttonLabels[i].length() > 0) {
                    if (isJsfELSyntax(buttonLabels[i])) {
                        btn.getProperty("value").setValueSource(buttonLabels[i]); //NOI18N
                    } else {
                        btn.getProperty("value").setValue(buttonLabels[i]); //NOI18N
                    }
                }
                if (BUTTON_IMAGE.equals(paging.navigation) && buttonUrls[i] != null && buttonUrls[i].length() > 0) {
            		String urlToUse = buttonUrls[i];
                    if (buttonUrls[i].equals(defaultButtonUrls[i])) {
                    	boolean resourceAlreadyInProject = false;
                    	
                    	//HACK? The designtime API should probably expose whether the resource is already in the project
                    	URL localFileUrl = context.resolveResource(buttonUrls[i]);	// like file://C:/Documents and Settings/blah/blah/.../paging_first.png
                    	String localFileUrlStr = localFileUrl.toString();
                    	final String protocol = "file:/";	//NOI18N
                    	if (localFileUrlStr.startsWith(protocol)) {
	                    	String localFilePath = localFileUrlStr.substring(protocol.length()); // like C:/Documents and Settings/blah/blah/.../paging_first.png
                                                                                                    //possibly with leading /
                                localFilePath = localFilePath.replaceAll("%20", " ");
                                if (localFilePath.startsWith("/") && localFilePath.length() > 1) {
                                    localFilePath = localFilePath.substring(1, localFilePath.length()); //trim leading /
                                }
	                    	File localFile = new File(localFilePath);
	                    	resourceAlreadyInProject = localFile.exists();
                    	}
                    	
                    	if (!resourceAlreadyInProject) {
	                		URL urlToDefaultImage = HtmlDataTableState.class.getResource(defaultButtonImgs[i]);
	                    	try {
	                    		urlToUse = context.addResource(urlToDefaultImage, true);
	                    	}
	                    	catch (IOException ioe) {
	                    		ioe.printStackTrace();
	                        }
                    	}
                    }
                    if (isJsfELSyntax(urlToUse)) {
                        btn.getProperty("image").setValueSource(urlToUse);
                    } else {
                        btn.getProperty("image").setValue(urlToUse);
                    }
                }

                //get the btn's action event
                DesignEvent actionEvent = getActionEvent(btn);

                //set the handler name. a NullPointerException will be thrown if insync did not provide an action event.
                actionEvent.setHandlerName(tableBean.getInstanceName() + PAGING_HANDLER_SUFFIXES[i]); //like dataTable47_previousPageAction

                //set the handler source.
                String handlerMethodSource = "//not implemented\n"; //NOI18N
                switch (i) {
                    case 0:
                        handlerMethodSource = getFirstButtonHandlerSource();
                        break;
                    case 1:
                        handlerMethodSource = getPreviousButtonHandlerSource();
                        break;
                    case 2:
                        handlerMethodSource = getNextButtonHandlerSource();
                        break;
                    case 3:
                        handlerMethodSource = getLastButtonHandlerSource();
                        break;
                }
                //System.err.println("HtmlDataTableState.createNavPanel(" + facetStr + "): actionEvent.handlerMethodSource will be:\n" + handlerMethodSource + "!");      //NOI18N
                actionEvent.setHandlerMethodSource(handlerMethodSource);
                //System.err.println("HtmlDataTableState.createNavPanel(" + facetStr + "): actionEvent.handlerName:" + actionEvent.getHandlerName() + "!");     //NOI18N
                //System.err.println("HtmlDataTableState.createNavPanel(" + facetStr + "): actionEvent.handlerMethodSource:\n" + actionEvent.getHandlerMethodSource() + "!");      //NOI18N
            }
        }
    }

    private String getFirstButtonHandlerSource() {
        StringBuffer sb = new StringBuffer("\n"); //NOI18N
        sb.append("        " + tableBean.getInstanceName() + ".setFirst(0);\n"); //NOI18N
        sb.append("        return null;"); //NOI18N
        return sb.toString();
    }

    private String getPreviousButtonHandlerSource() {
        StringBuffer sb = new StringBuffer("\n"); //NOI18N
        String tbInstName = tableBean.getInstanceName();
        sb.append("        int first = " + tbInstName + ".getFirst() - " + tbInstName +  //NOI18N
            ".getRows();\n"); //NOI18N
        sb.append("        if (first < 0) {\n"); //NOI18N
        sb.append("            first = 0;\n"); //NOI18N
        sb.append("        }\n"); //NOI18N
        sb.append("        " + tbInstName + ".setFirst(first);\n"); //NOI18N
        sb.append("        return null;"); //NOI18N
        return sb.toString();
    }

    private String getNextButtonHandlerSource() {
        StringBuffer sb = new StringBuffer("\n"); //NOI18N
        String tbInstName = tableBean.getInstanceName();
        sb.append("        int first = " + tbInstName + ".getFirst() + " + tbInstName +  //NOI18N
            ".getRows();\n"); //NOI18N
        sb.append("        " + tbInstName + ".setRowIndex(first);\n"); //NOI18N
        sb.append("        if (" + tbInstName + ".isRowAvailable()) {\n"); //NOI18N
        sb.append("            " + tbInstName + ".setFirst(first);\n"); //NOI18N
        sb.append("        }\n"); //NOI18N
        sb.append("        return null;"); //NOI18N
        return sb.toString();
    }

    private String getLastButtonHandlerSource() {
        StringBuffer sb = new StringBuffer("\n"); //NOI18N
        String tbInstName = tableBean.getInstanceName();
        sb.append("        int first = " + tbInstName + ".getFirst();\n"); //NOI18N
        sb.append("        while (true) {\n"); //NOI18N
        sb.append("            " + tbInstName + ".setRowIndex(first + 1);\n"); //NOI18N
        sb.append("            if (" + tbInstName + ".isRowAvailable()) {\n"); //NOI18N
        sb.append("                first++;\n"); //NOI18N
        sb.append("            } else {\n"); //NOI18N
        sb.append("                break;\n"); //NOI18N
        sb.append("            }\n"); //NOI18N
        sb.append("        }\n"); //NOI18N
        sb.append("        " + tbInstName + ".setFirst(first - (first % " + tbInstName +  //NOI18N
            ".getRows()));\n"); //NOI18N
        sb.append("        return null;"); //NOI18N
        return sb.toString();
    }

    public void dumpState(PrintStream out) {
        //out.println("datatable: " + instanceName);    //NOI18N
        out.println("        sourceBean: " + sourceBean); //NOI18N
        out.println("        var: " + varName); //NOI18N
        ResultSetColumn[] rsca = rsinfo.getColumns();
        out.println("        columns: (" + rsca.length + " total)"); //NOI18N
        for (int i = 0; i < rsca.length; i++) {
            ResultSetColumn rsc = rsca[i];
            out.println("            column: [" + rsc.tableName + DOT + rsc.columnName + "]"); //NOI18N
            //out.println("                class: " + rsc.columnClassName);    //NOI18N
            //out.println("                sqlType: " + rsc.columnSqlType + ": " + rsc.columnSqlTypeName);    //NOI18N
        }
        out.println("    display:"); //NOI18N
        out.println("        rows: " + paging.rows); //NOI18N
        DisplayColumn[] dca = display.getColumns();
        out.println("        columns: (" + dca.length + " total)"); //NOI18N
        for (int i = 0; i < dca.length; i++) {
            DisplayColumn dc = dca[i];
            out.println("            column: " + dc.columnInstanceName); //NOI18N
            //out.println("              compInstanceName: " + dc.compInstanceName);    //NOI18N
            out.println("                class: " + dc.compClassName); //NOI18N
            out.println("                valueRef: " + dc.compValueRef); //NOI18N
            //out.println("              converter: " + dc.compConverter);    //NOI18N
            //out.println("              validator: " + dc.compValidator);    //NOI18N
            out.println("                header: " + dc.headerText); //NOI18N
            //out.println("              headerLink: " + dc.headerLink);    //NOI18N
            out.println("                footer: " + dc.footerText); //NOI18N
            out.println("                itemText: " + dc.itemText); //NOI18N
        }
    }
}
