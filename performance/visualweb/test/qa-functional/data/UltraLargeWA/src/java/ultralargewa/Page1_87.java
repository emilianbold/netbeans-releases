/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
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
 
package ultralargewa;

import com.sun.rave.web.ui.appbase.AbstractPageBean;
import com.sun.webui.jsf.component.Body;
import com.sun.webui.jsf.component.Form;
import com.sun.webui.jsf.component.Head;
import com.sun.webui.jsf.component.Html;
import com.sun.webui.jsf.component.Link;
import com.sun.webui.jsf.component.Page;
import com.sun.webui.jsf.component.StaticText;
import com.sun.webui.jsf.component.Table;
import com.sun.webui.jsf.component.TableColumn;
import com.sun.webui.jsf.component.TableRowGroup;
import com.sun.webui.jsf.model.DefaultTableDataProvider;
import javax.faces.FacesException;

/**
 * <p>Page bean that corresponds to a similarly named JSP page.  This
 * class contains component definitions (and initialization code) for
 * all components that you have defined on this page, as well as
 * lifecycle methods and event handlers where you may add behavior
 * to respond to incoming events.</p>
 *
 * @author Administrator
 */
public class Page1_87 extends AbstractPageBean {
    // <editor-fold defaultstate="collapsed" desc="Managed Component Definition">

    /**
     * <p>Automatically managed component initialization.  <strong>WARNING:</strong>
     * This method is automatically generated, so any user-specified code inserted
     * here is subject to being replaced.</p>
     */
    private void _init() throws Exception {
    }

    private Page page1 = new Page();
    
    public Page getPage1() {
        return page1;
    }
    
    public void setPage1(Page p) {
        this.page1 = p;
    }
    
    private Html html1 = new Html();
    
    public Html getHtml1() {
        return html1;
    }
    
    public void setHtml1(Html h) {
        this.html1 = h;
    }
    
    private Head head1 = new Head();
    
    public Head getHead1() {
        return head1;
    }
    
    public void setHead1(Head h) {
        this.head1 = h;
    }
    
    private Link link1 = new Link();
    
    public Link getLink1() {
        return link1;
    }
    
    public void setLink1(Link l) {
        this.link1 = l;
    }
    
    private Body body1 = new Body();
    
    public Body getBody1() {
        return body1;
    }
    
    public void setBody1(Body b) {
        this.body1 = b;
    }
    
    private Form form1 = new Form();
    
    public Form getForm1() {
        return form1;
    }
    
    public void setForm1(Form f) {
        this.form1 = f;
    }
    private Table table1 = new Table();

    public Table getTable1() {
        return table1;
    }

    public void setTable1(Table t) {
        this.table1 = t;
    }
    private TableRowGroup tableRowGroup1 = new TableRowGroup();

    public TableRowGroup getTableRowGroup1() {
        return tableRowGroup1;
    }

    public void setTableRowGroup1(TableRowGroup trg) {
        this.tableRowGroup1 = trg;
    }
    private DefaultTableDataProvider defaultTableDataProvider = new DefaultTableDataProvider();

    public DefaultTableDataProvider getDefaultTableDataProvider() {
        return defaultTableDataProvider;
    }

    public void setDefaultTableDataProvider(DefaultTableDataProvider dtdp) {
        this.defaultTableDataProvider = dtdp;
    }
    private TableColumn tableColumn1 = new TableColumn();

    public TableColumn getTableColumn1() {
        return tableColumn1;
    }

    public void setTableColumn1(TableColumn tc) {
        this.tableColumn1 = tc;
    }
    private StaticText staticText1 = new StaticText();

    public StaticText getStaticText1() {
        return staticText1;
    }

    public void setStaticText1(StaticText st) {
        this.staticText1 = st;
    }
    private TableColumn tableColumn2 = new TableColumn();

    public TableColumn getTableColumn2() {
        return tableColumn2;
    }

    public void setTableColumn2(TableColumn tc) {
        this.tableColumn2 = tc;
    }
    private StaticText staticText2 = new StaticText();

    public StaticText getStaticText2() {
        return staticText2;
    }

    public void setStaticText2(StaticText st) {
        this.staticText2 = st;
    }
    private TableColumn tableColumn3 = new TableColumn();

    public TableColumn getTableColumn3() {
        return tableColumn3;
    }

    public void setTableColumn3(TableColumn tc) {
        this.tableColumn3 = tc;
    }
    private StaticText staticText3 = new StaticText();

    public StaticText getStaticText3() {
        return staticText3;
    }

    public void setStaticText3(StaticText st) {
        this.staticText3 = st;
    }
    private Table table2 = new Table();

    public Table getTable2() {
        return table2;
    }

    public void setTable2(Table t) {
        this.table2 = t;
    }
    private TableRowGroup tableRowGroup2 = new TableRowGroup();

    public TableRowGroup getTableRowGroup2() {
        return tableRowGroup2;
    }

    public void setTableRowGroup2(TableRowGroup trg) {
        this.tableRowGroup2 = trg;
    }
    private TableColumn tableColumn4 = new TableColumn();

    public TableColumn getTableColumn4() {
        return tableColumn4;
    }

    public void setTableColumn4(TableColumn tc) {
        this.tableColumn4 = tc;
    }
    private StaticText staticText4 = new StaticText();

    public StaticText getStaticText4() {
        return staticText4;
    }

    public void setStaticText4(StaticText st) {
        this.staticText4 = st;
    }
    private TableColumn tableColumn5 = new TableColumn();

    public TableColumn getTableColumn5() {
        return tableColumn5;
    }

    public void setTableColumn5(TableColumn tc) {
        this.tableColumn5 = tc;
    }
    private StaticText staticText5 = new StaticText();

    public StaticText getStaticText5() {
        return staticText5;
    }

    public void setStaticText5(StaticText st) {
        this.staticText5 = st;
    }
    private TableColumn tableColumn6 = new TableColumn();

    public TableColumn getTableColumn6() {
        return tableColumn6;
    }

    public void setTableColumn6(TableColumn tc) {
        this.tableColumn6 = tc;
    }
    private StaticText staticText6 = new StaticText();

    public StaticText getStaticText6() {
        return staticText6;
    }

    public void setStaticText6(StaticText st) {
        this.staticText6 = st;
    }
    private Table table3 = new Table();

    public Table getTable3() {
        return table3;
    }

    public void setTable3(Table t) {
        this.table3 = t;
    }
    private TableRowGroup tableRowGroup3 = new TableRowGroup();

    public TableRowGroup getTableRowGroup3() {
        return tableRowGroup3;
    }

    public void setTableRowGroup3(TableRowGroup trg) {
        this.tableRowGroup3 = trg;
    }
    private TableColumn tableColumn7 = new TableColumn();

    public TableColumn getTableColumn7() {
        return tableColumn7;
    }

    public void setTableColumn7(TableColumn tc) {
        this.tableColumn7 = tc;
    }
    private StaticText staticText7 = new StaticText();

    public StaticText getStaticText7() {
        return staticText7;
    }

    public void setStaticText7(StaticText st) {
        this.staticText7 = st;
    }
    private TableColumn tableColumn8 = new TableColumn();

    public TableColumn getTableColumn8() {
        return tableColumn8;
    }

    public void setTableColumn8(TableColumn tc) {
        this.tableColumn8 = tc;
    }
    private StaticText staticText8 = new StaticText();

    public StaticText getStaticText8() {
        return staticText8;
    }

    public void setStaticText8(StaticText st) {
        this.staticText8 = st;
    }
    private TableColumn tableColumn9 = new TableColumn();

    public TableColumn getTableColumn9() {
        return tableColumn9;
    }

    public void setTableColumn9(TableColumn tc) {
        this.tableColumn9 = tc;
    }
    private StaticText staticText9 = new StaticText();

    public StaticText getStaticText9() {
        return staticText9;
    }

    public void setStaticText9(StaticText st) {
        this.staticText9 = st;
    }
    private Table table4 = new Table();

    public Table getTable4() {
        return table4;
    }

    public void setTable4(Table t) {
        this.table4 = t;
    }
    private TableRowGroup tableRowGroup4 = new TableRowGroup();

    public TableRowGroup getTableRowGroup4() {
        return tableRowGroup4;
    }

    public void setTableRowGroup4(TableRowGroup trg) {
        this.tableRowGroup4 = trg;
    }
    private TableColumn tableColumn10 = new TableColumn();

    public TableColumn getTableColumn10() {
        return tableColumn10;
    }

    public void setTableColumn10(TableColumn tc) {
        this.tableColumn10 = tc;
    }
    private StaticText staticText10 = new StaticText();

    public StaticText getStaticText10() {
        return staticText10;
    }

    public void setStaticText10(StaticText st) {
        this.staticText10 = st;
    }
    private TableColumn tableColumn11 = new TableColumn();

    public TableColumn getTableColumn11() {
        return tableColumn11;
    }

    public void setTableColumn11(TableColumn tc) {
        this.tableColumn11 = tc;
    }
    private StaticText staticText11 = new StaticText();

    public StaticText getStaticText11() {
        return staticText11;
    }

    public void setStaticText11(StaticText st) {
        this.staticText11 = st;
    }
    private TableColumn tableColumn12 = new TableColumn();

    public TableColumn getTableColumn12() {
        return tableColumn12;
    }

    public void setTableColumn12(TableColumn tc) {
        this.tableColumn12 = tc;
    }
    private StaticText staticText12 = new StaticText();

    public StaticText getStaticText12() {
        return staticText12;
    }

    public void setStaticText12(StaticText st) {
        this.staticText12 = st;
    }
    private Table table5 = new Table();

    public Table getTable5() {
        return table5;
    }

    public void setTable5(Table t) {
        this.table5 = t;
    }
    private TableRowGroup tableRowGroup5 = new TableRowGroup();

    public TableRowGroup getTableRowGroup5() {
        return tableRowGroup5;
    }

    public void setTableRowGroup5(TableRowGroup trg) {
        this.tableRowGroup5 = trg;
    }
    private TableColumn tableColumn13 = new TableColumn();

    public TableColumn getTableColumn13() {
        return tableColumn13;
    }

    public void setTableColumn13(TableColumn tc) {
        this.tableColumn13 = tc;
    }
    private StaticText staticText13 = new StaticText();

    public StaticText getStaticText13() {
        return staticText13;
    }

    public void setStaticText13(StaticText st) {
        this.staticText13 = st;
    }
    private TableColumn tableColumn14 = new TableColumn();

    public TableColumn getTableColumn14() {
        return tableColumn14;
    }

    public void setTableColumn14(TableColumn tc) {
        this.tableColumn14 = tc;
    }
    private StaticText staticText14 = new StaticText();

    public StaticText getStaticText14() {
        return staticText14;
    }

    public void setStaticText14(StaticText st) {
        this.staticText14 = st;
    }
    private TableColumn tableColumn15 = new TableColumn();

    public TableColumn getTableColumn15() {
        return tableColumn15;
    }

    public void setTableColumn15(TableColumn tc) {
        this.tableColumn15 = tc;
    }
    private StaticText staticText15 = new StaticText();

    public StaticText getStaticText15() {
        return staticText15;
    }

    public void setStaticText15(StaticText st) {
        this.staticText15 = st;
    }
    private Table table6 = new Table();

    public Table getTable6() {
        return table6;
    }

    public void setTable6(Table t) {
        this.table6 = t;
    }
    private TableRowGroup tableRowGroup6 = new TableRowGroup();

    public TableRowGroup getTableRowGroup6() {
        return tableRowGroup6;
    }

    public void setTableRowGroup6(TableRowGroup trg) {
        this.tableRowGroup6 = trg;
    }
    private TableColumn tableColumn16 = new TableColumn();

    public TableColumn getTableColumn16() {
        return tableColumn16;
    }

    public void setTableColumn16(TableColumn tc) {
        this.tableColumn16 = tc;
    }
    private StaticText staticText16 = new StaticText();

    public StaticText getStaticText16() {
        return staticText16;
    }

    public void setStaticText16(StaticText st) {
        this.staticText16 = st;
    }
    private TableColumn tableColumn17 = new TableColumn();

    public TableColumn getTableColumn17() {
        return tableColumn17;
    }

    public void setTableColumn17(TableColumn tc) {
        this.tableColumn17 = tc;
    }
    private StaticText staticText17 = new StaticText();

    public StaticText getStaticText17() {
        return staticText17;
    }

    public void setStaticText17(StaticText st) {
        this.staticText17 = st;
    }
    private TableColumn tableColumn18 = new TableColumn();

    public TableColumn getTableColumn18() {
        return tableColumn18;
    }

    public void setTableColumn18(TableColumn tc) {
        this.tableColumn18 = tc;
    }
    private StaticText staticText18 = new StaticText();

    public StaticText getStaticText18() {
        return staticText18;
    }

    public void setStaticText18(StaticText st) {
        this.staticText18 = st;
    }
    private Table table7 = new Table();

    public Table getTable7() {
        return table7;
    }

    public void setTable7(Table t) {
        this.table7 = t;
    }
    private TableRowGroup tableRowGroup7 = new TableRowGroup();

    public TableRowGroup getTableRowGroup7() {
        return tableRowGroup7;
    }

    public void setTableRowGroup7(TableRowGroup trg) {
        this.tableRowGroup7 = trg;
    }
    private TableColumn tableColumn19 = new TableColumn();

    public TableColumn getTableColumn19() {
        return tableColumn19;
    }

    public void setTableColumn19(TableColumn tc) {
        this.tableColumn19 = tc;
    }
    private StaticText staticText19 = new StaticText();

    public StaticText getStaticText19() {
        return staticText19;
    }

    public void setStaticText19(StaticText st) {
        this.staticText19 = st;
    }
    private TableColumn tableColumn20 = new TableColumn();

    public TableColumn getTableColumn20() {
        return tableColumn20;
    }

    public void setTableColumn20(TableColumn tc) {
        this.tableColumn20 = tc;
    }
    private StaticText staticText20 = new StaticText();

    public StaticText getStaticText20() {
        return staticText20;
    }

    public void setStaticText20(StaticText st) {
        this.staticText20 = st;
    }
    private TableColumn tableColumn21 = new TableColumn();

    public TableColumn getTableColumn21() {
        return tableColumn21;
    }

    public void setTableColumn21(TableColumn tc) {
        this.tableColumn21 = tc;
    }
    private StaticText staticText21 = new StaticText();

    public StaticText getStaticText21() {
        return staticText21;
    }

    public void setStaticText21(StaticText st) {
        this.staticText21 = st;
    }
    private Table table8 = new Table();

    public Table getTable8() {
        return table8;
    }

    public void setTable8(Table t) {
        this.table8 = t;
    }
    private TableRowGroup tableRowGroup8 = new TableRowGroup();

    public TableRowGroup getTableRowGroup8() {
        return tableRowGroup8;
    }

    public void setTableRowGroup8(TableRowGroup trg) {
        this.tableRowGroup8 = trg;
    }
    private TableColumn tableColumn22 = new TableColumn();

    public TableColumn getTableColumn22() {
        return tableColumn22;
    }

    public void setTableColumn22(TableColumn tc) {
        this.tableColumn22 = tc;
    }
    private StaticText staticText22 = new StaticText();

    public StaticText getStaticText22() {
        return staticText22;
    }

    public void setStaticText22(StaticText st) {
        this.staticText22 = st;
    }
    private TableColumn tableColumn23 = new TableColumn();

    public TableColumn getTableColumn23() {
        return tableColumn23;
    }

    public void setTableColumn23(TableColumn tc) {
        this.tableColumn23 = tc;
    }
    private StaticText staticText23 = new StaticText();

    public StaticText getStaticText23() {
        return staticText23;
    }

    public void setStaticText23(StaticText st) {
        this.staticText23 = st;
    }
    private TableColumn tableColumn24 = new TableColumn();

    public TableColumn getTableColumn24() {
        return tableColumn24;
    }

    public void setTableColumn24(TableColumn tc) {
        this.tableColumn24 = tc;
    }
    private StaticText staticText24 = new StaticText();

    public StaticText getStaticText24() {
        return staticText24;
    }

    public void setStaticText24(StaticText st) {
        this.staticText24 = st;
    }
    private Table table9 = new Table();

    public Table getTable9() {
        return table9;
    }

    public void setTable9(Table t) {
        this.table9 = t;
    }
    private TableRowGroup tableRowGroup9 = new TableRowGroup();

    public TableRowGroup getTableRowGroup9() {
        return tableRowGroup9;
    }

    public void setTableRowGroup9(TableRowGroup trg) {
        this.tableRowGroup9 = trg;
    }
    private TableColumn tableColumn25 = new TableColumn();

    public TableColumn getTableColumn25() {
        return tableColumn25;
    }

    public void setTableColumn25(TableColumn tc) {
        this.tableColumn25 = tc;
    }
    private StaticText staticText25 = new StaticText();

    public StaticText getStaticText25() {
        return staticText25;
    }

    public void setStaticText25(StaticText st) {
        this.staticText25 = st;
    }
    private TableColumn tableColumn26 = new TableColumn();

    public TableColumn getTableColumn26() {
        return tableColumn26;
    }

    public void setTableColumn26(TableColumn tc) {
        this.tableColumn26 = tc;
    }
    private StaticText staticText26 = new StaticText();

    public StaticText getStaticText26() {
        return staticText26;
    }

    public void setStaticText26(StaticText st) {
        this.staticText26 = st;
    }
    private TableColumn tableColumn27 = new TableColumn();

    public TableColumn getTableColumn27() {
        return tableColumn27;
    }

    public void setTableColumn27(TableColumn tc) {
        this.tableColumn27 = tc;
    }
    private StaticText staticText27 = new StaticText();

    public StaticText getStaticText27() {
        return staticText27;
    }

    public void setStaticText27(StaticText st) {
        this.staticText27 = st;
    }
    private Table table10 = new Table();

    public Table getTable10() {
        return table10;
    }

    public void setTable10(Table t) {
        this.table10 = t;
    }
    private TableRowGroup tableRowGroup10 = new TableRowGroup();

    public TableRowGroup getTableRowGroup10() {
        return tableRowGroup10;
    }

    public void setTableRowGroup10(TableRowGroup trg) {
        this.tableRowGroup10 = trg;
    }
    private TableColumn tableColumn28 = new TableColumn();

    public TableColumn getTableColumn28() {
        return tableColumn28;
    }

    public void setTableColumn28(TableColumn tc) {
        this.tableColumn28 = tc;
    }
    private StaticText staticText28 = new StaticText();

    public StaticText getStaticText28() {
        return staticText28;
    }

    public void setStaticText28(StaticText st) {
        this.staticText28 = st;
    }
    private TableColumn tableColumn29 = new TableColumn();

    public TableColumn getTableColumn29() {
        return tableColumn29;
    }

    public void setTableColumn29(TableColumn tc) {
        this.tableColumn29 = tc;
    }
    private StaticText staticText29 = new StaticText();

    public StaticText getStaticText29() {
        return staticText29;
    }

    public void setStaticText29(StaticText st) {
        this.staticText29 = st;
    }
    private TableColumn tableColumn30 = new TableColumn();

    public TableColumn getTableColumn30() {
        return tableColumn30;
    }

    public void setTableColumn30(TableColumn tc) {
        this.tableColumn30 = tc;
    }
    private StaticText staticText30 = new StaticText();

    public StaticText getStaticText30() {
        return staticText30;
    }

    public void setStaticText30(StaticText st) {
        this.staticText30 = st;
    }
    private Table table11 = new Table();

    public Table getTable11() {
        return table11;
    }

    public void setTable11(Table t) {
        this.table11 = t;
    }
    private TableRowGroup tableRowGroup11 = new TableRowGroup();

    public TableRowGroup getTableRowGroup11() {
        return tableRowGroup11;
    }

    public void setTableRowGroup11(TableRowGroup trg) {
        this.tableRowGroup11 = trg;
    }
    private TableColumn tableColumn31 = new TableColumn();

    public TableColumn getTableColumn31() {
        return tableColumn31;
    }

    public void setTableColumn31(TableColumn tc) {
        this.tableColumn31 = tc;
    }
    private StaticText staticText31 = new StaticText();

    public StaticText getStaticText31() {
        return staticText31;
    }

    public void setStaticText31(StaticText st) {
        this.staticText31 = st;
    }
    private TableColumn tableColumn32 = new TableColumn();

    public TableColumn getTableColumn32() {
        return tableColumn32;
    }

    public void setTableColumn32(TableColumn tc) {
        this.tableColumn32 = tc;
    }
    private StaticText staticText32 = new StaticText();

    public StaticText getStaticText32() {
        return staticText32;
    }

    public void setStaticText32(StaticText st) {
        this.staticText32 = st;
    }
    private TableColumn tableColumn33 = new TableColumn();

    public TableColumn getTableColumn33() {
        return tableColumn33;
    }

    public void setTableColumn33(TableColumn tc) {
        this.tableColumn33 = tc;
    }
    private StaticText staticText33 = new StaticText();

    public StaticText getStaticText33() {
        return staticText33;
    }

    public void setStaticText33(StaticText st) {
        this.staticText33 = st;
    }
    private Table table12 = new Table();

    public Table getTable12() {
        return table12;
    }

    public void setTable12(Table t) {
        this.table12 = t;
    }
    private TableRowGroup tableRowGroup12 = new TableRowGroup();

    public TableRowGroup getTableRowGroup12() {
        return tableRowGroup12;
    }

    public void setTableRowGroup12(TableRowGroup trg) {
        this.tableRowGroup12 = trg;
    }
    private TableColumn tableColumn34 = new TableColumn();

    public TableColumn getTableColumn34() {
        return tableColumn34;
    }

    public void setTableColumn34(TableColumn tc) {
        this.tableColumn34 = tc;
    }
    private StaticText staticText34 = new StaticText();

    public StaticText getStaticText34() {
        return staticText34;
    }

    public void setStaticText34(StaticText st) {
        this.staticText34 = st;
    }
    private TableColumn tableColumn35 = new TableColumn();

    public TableColumn getTableColumn35() {
        return tableColumn35;
    }

    public void setTableColumn35(TableColumn tc) {
        this.tableColumn35 = tc;
    }
    private StaticText staticText35 = new StaticText();

    public StaticText getStaticText35() {
        return staticText35;
    }

    public void setStaticText35(StaticText st) {
        this.staticText35 = st;
    }
    private TableColumn tableColumn36 = new TableColumn();

    public TableColumn getTableColumn36() {
        return tableColumn36;
    }

    public void setTableColumn36(TableColumn tc) {
        this.tableColumn36 = tc;
    }
    private StaticText staticText36 = new StaticText();

    public StaticText getStaticText36() {
        return staticText36;
    }

    public void setStaticText36(StaticText st) {
        this.staticText36 = st;
    }
    private Table table13 = new Table();

    public Table getTable13() {
        return table13;
    }

    public void setTable13(Table t) {
        this.table13 = t;
    }
    private TableRowGroup tableRowGroup13 = new TableRowGroup();

    public TableRowGroup getTableRowGroup13() {
        return tableRowGroup13;
    }

    public void setTableRowGroup13(TableRowGroup trg) {
        this.tableRowGroup13 = trg;
    }
    private TableColumn tableColumn37 = new TableColumn();

    public TableColumn getTableColumn37() {
        return tableColumn37;
    }

    public void setTableColumn37(TableColumn tc) {
        this.tableColumn37 = tc;
    }
    private StaticText staticText37 = new StaticText();

    public StaticText getStaticText37() {
        return staticText37;
    }

    public void setStaticText37(StaticText st) {
        this.staticText37 = st;
    }
    private TableColumn tableColumn38 = new TableColumn();

    public TableColumn getTableColumn38() {
        return tableColumn38;
    }

    public void setTableColumn38(TableColumn tc) {
        this.tableColumn38 = tc;
    }
    private StaticText staticText38 = new StaticText();

    public StaticText getStaticText38() {
        return staticText38;
    }

    public void setStaticText38(StaticText st) {
        this.staticText38 = st;
    }
    private TableColumn tableColumn39 = new TableColumn();

    public TableColumn getTableColumn39() {
        return tableColumn39;
    }

    public void setTableColumn39(TableColumn tc) {
        this.tableColumn39 = tc;
    }
    private StaticText staticText39 = new StaticText();

    public StaticText getStaticText39() {
        return staticText39;
    }

    public void setStaticText39(StaticText st) {
        this.staticText39 = st;
    }
    private Table table14 = new Table();

    public Table getTable14() {
        return table14;
    }

    public void setTable14(Table t) {
        this.table14 = t;
    }
    private TableRowGroup tableRowGroup14 = new TableRowGroup();

    public TableRowGroup getTableRowGroup14() {
        return tableRowGroup14;
    }

    public void setTableRowGroup14(TableRowGroup trg) {
        this.tableRowGroup14 = trg;
    }
    private TableColumn tableColumn40 = new TableColumn();

    public TableColumn getTableColumn40() {
        return tableColumn40;
    }

    public void setTableColumn40(TableColumn tc) {
        this.tableColumn40 = tc;
    }
    private StaticText staticText40 = new StaticText();

    public StaticText getStaticText40() {
        return staticText40;
    }

    public void setStaticText40(StaticText st) {
        this.staticText40 = st;
    }
    private TableColumn tableColumn41 = new TableColumn();

    public TableColumn getTableColumn41() {
        return tableColumn41;
    }

    public void setTableColumn41(TableColumn tc) {
        this.tableColumn41 = tc;
    }
    private StaticText staticText41 = new StaticText();

    public StaticText getStaticText41() {
        return staticText41;
    }

    public void setStaticText41(StaticText st) {
        this.staticText41 = st;
    }
    private TableColumn tableColumn42 = new TableColumn();

    public TableColumn getTableColumn42() {
        return tableColumn42;
    }

    public void setTableColumn42(TableColumn tc) {
        this.tableColumn42 = tc;
    }
    private StaticText staticText42 = new StaticText();

    public StaticText getStaticText42() {
        return staticText42;
    }

    public void setStaticText42(StaticText st) {
        this.staticText42 = st;
    }
    private Table table15 = new Table();

    public Table getTable15() {
        return table15;
    }

    public void setTable15(Table t) {
        this.table15 = t;
    }
    private TableRowGroup tableRowGroup15 = new TableRowGroup();

    public TableRowGroup getTableRowGroup15() {
        return tableRowGroup15;
    }

    public void setTableRowGroup15(TableRowGroup trg) {
        this.tableRowGroup15 = trg;
    }
    private TableColumn tableColumn43 = new TableColumn();

    public TableColumn getTableColumn43() {
        return tableColumn43;
    }

    public void setTableColumn43(TableColumn tc) {
        this.tableColumn43 = tc;
    }
    private StaticText staticText43 = new StaticText();

    public StaticText getStaticText43() {
        return staticText43;
    }

    public void setStaticText43(StaticText st) {
        this.staticText43 = st;
    }
    private TableColumn tableColumn44 = new TableColumn();

    public TableColumn getTableColumn44() {
        return tableColumn44;
    }

    public void setTableColumn44(TableColumn tc) {
        this.tableColumn44 = tc;
    }
    private StaticText staticText44 = new StaticText();

    public StaticText getStaticText44() {
        return staticText44;
    }

    public void setStaticText44(StaticText st) {
        this.staticText44 = st;
    }
    private TableColumn tableColumn45 = new TableColumn();

    public TableColumn getTableColumn45() {
        return tableColumn45;
    }

    public void setTableColumn45(TableColumn tc) {
        this.tableColumn45 = tc;
    }
    private StaticText staticText45 = new StaticText();

    public StaticText getStaticText45() {
        return staticText45;
    }

    public void setStaticText45(StaticText st) {
        this.staticText45 = st;
    }
    private Table table16 = new Table();

    public Table getTable16() {
        return table16;
    }

    public void setTable16(Table t) {
        this.table16 = t;
    }
    private TableRowGroup tableRowGroup16 = new TableRowGroup();

    public TableRowGroup getTableRowGroup16() {
        return tableRowGroup16;
    }

    public void setTableRowGroup16(TableRowGroup trg) {
        this.tableRowGroup16 = trg;
    }
    private TableColumn tableColumn46 = new TableColumn();

    public TableColumn getTableColumn46() {
        return tableColumn46;
    }

    public void setTableColumn46(TableColumn tc) {
        this.tableColumn46 = tc;
    }
    private StaticText staticText46 = new StaticText();

    public StaticText getStaticText46() {
        return staticText46;
    }

    public void setStaticText46(StaticText st) {
        this.staticText46 = st;
    }
    private TableColumn tableColumn47 = new TableColumn();

    public TableColumn getTableColumn47() {
        return tableColumn47;
    }

    public void setTableColumn47(TableColumn tc) {
        this.tableColumn47 = tc;
    }
    private StaticText staticText47 = new StaticText();

    public StaticText getStaticText47() {
        return staticText47;
    }

    public void setStaticText47(StaticText st) {
        this.staticText47 = st;
    }
    private TableColumn tableColumn48 = new TableColumn();

    public TableColumn getTableColumn48() {
        return tableColumn48;
    }

    public void setTableColumn48(TableColumn tc) {
        this.tableColumn48 = tc;
    }
    private StaticText staticText48 = new StaticText();

    public StaticText getStaticText48() {
        return staticText48;
    }

    public void setStaticText48(StaticText st) {
        this.staticText48 = st;
    }
    private Table table17 = new Table();

    public Table getTable17() {
        return table17;
    }

    public void setTable17(Table t) {
        this.table17 = t;
    }
    private TableRowGroup tableRowGroup17 = new TableRowGroup();

    public TableRowGroup getTableRowGroup17() {
        return tableRowGroup17;
    }

    public void setTableRowGroup17(TableRowGroup trg) {
        this.tableRowGroup17 = trg;
    }
    private TableColumn tableColumn49 = new TableColumn();

    public TableColumn getTableColumn49() {
        return tableColumn49;
    }

    public void setTableColumn49(TableColumn tc) {
        this.tableColumn49 = tc;
    }
    private StaticText staticText49 = new StaticText();

    public StaticText getStaticText49() {
        return staticText49;
    }

    public void setStaticText49(StaticText st) {
        this.staticText49 = st;
    }
    private TableColumn tableColumn50 = new TableColumn();

    public TableColumn getTableColumn50() {
        return tableColumn50;
    }

    public void setTableColumn50(TableColumn tc) {
        this.tableColumn50 = tc;
    }
    private StaticText staticText50 = new StaticText();

    public StaticText getStaticText50() {
        return staticText50;
    }

    public void setStaticText50(StaticText st) {
        this.staticText50 = st;
    }
    private TableColumn tableColumn51 = new TableColumn();

    public TableColumn getTableColumn51() {
        return tableColumn51;
    }

    public void setTableColumn51(TableColumn tc) {
        this.tableColumn51 = tc;
    }
    private StaticText staticText51 = new StaticText();

    public StaticText getStaticText51() {
        return staticText51;
    }

    public void setStaticText51(StaticText st) {
        this.staticText51 = st;
    }
    private Table table18 = new Table();

    public Table getTable18() {
        return table18;
    }

    public void setTable18(Table t) {
        this.table18 = t;
    }
    private TableRowGroup tableRowGroup18 = new TableRowGroup();

    public TableRowGroup getTableRowGroup18() {
        return tableRowGroup18;
    }

    public void setTableRowGroup18(TableRowGroup trg) {
        this.tableRowGroup18 = trg;
    }
    private TableColumn tableColumn52 = new TableColumn();

    public TableColumn getTableColumn52() {
        return tableColumn52;
    }

    public void setTableColumn52(TableColumn tc) {
        this.tableColumn52 = tc;
    }
    private StaticText staticText52 = new StaticText();

    public StaticText getStaticText52() {
        return staticText52;
    }

    public void setStaticText52(StaticText st) {
        this.staticText52 = st;
    }
    private TableColumn tableColumn53 = new TableColumn();

    public TableColumn getTableColumn53() {
        return tableColumn53;
    }

    public void setTableColumn53(TableColumn tc) {
        this.tableColumn53 = tc;
    }
    private StaticText staticText53 = new StaticText();

    public StaticText getStaticText53() {
        return staticText53;
    }

    public void setStaticText53(StaticText st) {
        this.staticText53 = st;
    }
    private TableColumn tableColumn54 = new TableColumn();

    public TableColumn getTableColumn54() {
        return tableColumn54;
    }

    public void setTableColumn54(TableColumn tc) {
        this.tableColumn54 = tc;
    }
    private StaticText staticText54 = new StaticText();

    public StaticText getStaticText54() {
        return staticText54;
    }

    public void setStaticText54(StaticText st) {
        this.staticText54 = st;
    }
    private Table table19 = new Table();

    public Table getTable19() {
        return table19;
    }

    public void setTable19(Table t) {
        this.table19 = t;
    }
    private TableRowGroup tableRowGroup19 = new TableRowGroup();

    public TableRowGroup getTableRowGroup19() {
        return tableRowGroup19;
    }

    public void setTableRowGroup19(TableRowGroup trg) {
        this.tableRowGroup19 = trg;
    }
    private TableColumn tableColumn55 = new TableColumn();

    public TableColumn getTableColumn55() {
        return tableColumn55;
    }

    public void setTableColumn55(TableColumn tc) {
        this.tableColumn55 = tc;
    }
    private StaticText staticText55 = new StaticText();

    public StaticText getStaticText55() {
        return staticText55;
    }

    public void setStaticText55(StaticText st) {
        this.staticText55 = st;
    }
    private TableColumn tableColumn56 = new TableColumn();

    public TableColumn getTableColumn56() {
        return tableColumn56;
    }

    public void setTableColumn56(TableColumn tc) {
        this.tableColumn56 = tc;
    }
    private StaticText staticText56 = new StaticText();

    public StaticText getStaticText56() {
        return staticText56;
    }

    public void setStaticText56(StaticText st) {
        this.staticText56 = st;
    }
    private TableColumn tableColumn57 = new TableColumn();

    public TableColumn getTableColumn57() {
        return tableColumn57;
    }

    public void setTableColumn57(TableColumn tc) {
        this.tableColumn57 = tc;
    }
    private StaticText staticText57 = new StaticText();

    public StaticText getStaticText57() {
        return staticText57;
    }

    public void setStaticText57(StaticText st) {
        this.staticText57 = st;
    }
    private Table table20 = new Table();

    public Table getTable20() {
        return table20;
    }

    public void setTable20(Table t) {
        this.table20 = t;
    }
    private TableRowGroup tableRowGroup20 = new TableRowGroup();

    public TableRowGroup getTableRowGroup20() {
        return tableRowGroup20;
    }

    public void setTableRowGroup20(TableRowGroup trg) {
        this.tableRowGroup20 = trg;
    }
    private TableColumn tableColumn58 = new TableColumn();

    public TableColumn getTableColumn58() {
        return tableColumn58;
    }

    public void setTableColumn58(TableColumn tc) {
        this.tableColumn58 = tc;
    }
    private StaticText staticText58 = new StaticText();

    public StaticText getStaticText58() {
        return staticText58;
    }

    public void setStaticText58(StaticText st) {
        this.staticText58 = st;
    }
    private TableColumn tableColumn59 = new TableColumn();

    public TableColumn getTableColumn59() {
        return tableColumn59;
    }

    public void setTableColumn59(TableColumn tc) {
        this.tableColumn59 = tc;
    }
    private StaticText staticText59 = new StaticText();

    public StaticText getStaticText59() {
        return staticText59;
    }

    public void setStaticText59(StaticText st) {
        this.staticText59 = st;
    }
    private TableColumn tableColumn60 = new TableColumn();

    public TableColumn getTableColumn60() {
        return tableColumn60;
    }

    public void setTableColumn60(TableColumn tc) {
        this.tableColumn60 = tc;
    }
    private StaticText staticText60 = new StaticText();

    public StaticText getStaticText60() {
        return staticText60;
    }

    public void setStaticText60(StaticText st) {
        this.staticText60 = st;
    }

    // </editor-fold>

    /**
     * <p>Construct a new Page bean instance.</p>
     */
    public Page1_87() {
    }

    /**
     * <p>Callback method that is called whenever a page is navigated to,
     * either directly via a URL, or indirectly via page navigation.
     * Customize this method to acquire resources that will be needed
     * for event handlers and lifecycle methods, whether or not this
     * page is performing post back processing.</p>
     * 
     * <p>Note that, if the current request is a postback, the property
     * values of the components do <strong>not</strong> represent any
     * values submitted with this request.  Instead, they represent the
     * property values that were saved for this view when it was rendered.</p>
     */
    @Override
    public void init() {
        // Perform initializations inherited from our superclass
        super.init();
        // Perform application initialization that must complete
        // *before* managed components are initialized
        // TODO - add your own initialiation code here
        
        // <editor-fold defaultstate="collapsed" desc="Managed Component Initialization">
        // Initialize automatically managed components
        // *Note* - this logic should NOT be modified
        try {
            _init();
        } catch (Exception e) {
            log("Page1 Initialization Failure", e);
            throw e instanceof FacesException ? (FacesException) e: new FacesException(e);
        }
        
        // </editor-fold>
        // Perform application initialization that must complete
        // *after* managed components are initialized
        // TODO - add your own initialization code here
    }

    /**
     * <p>Callback method that is called after the component tree has been
     * restored, but before any event processing takes place.  This method
     * will <strong>only</strong> be called on a postback request that
     * is processing a form submit.  Customize this method to allocate
     * resources that will be required in your event handlers.</p>
     */
    @Override
    public void preprocess() {
    }

    /**
     * <p>Callback method that is called just before rendering takes place.
     * This method will <strong>only</strong> be called for the page that
     * will actually be rendered (and not, for example, on a page that
     * handled a postback and then navigated to a different page).  Customize
     * this method to allocate resources that will be required for rendering
     * this page.</p>
     */
    @Override
    public void prerender() {
    }

    /**
     * <p>Callback method that is called after rendering is completed for
     * this request, if <code>init()</code> was called (regardless of whether
     * or not this was the page that was actually rendered).  Customize this
     * method to release resources acquired in the <code>init()</code>,
     * <code>preprocess()</code>, or <code>prerender()</code> methods (or
     * acquired during execution of an event handler).</p>
     */
    @Override
    public void destroy() {
    }
    
    /**
     * <p>Return a reference to the scoped data bean.</p>
     *
     * @return reference to the scoped data bean
     */
    protected SessionBean1 getSessionBean1() {
        return (SessionBean1) getBean("SessionBean1");
    }

    /**
     * <p>Return a reference to the scoped data bean.</p>
     *
     * @return reference to the scoped data bean
     */
    protected RequestBean1 getRequestBean1() {
        return (RequestBean1) getBean("RequestBean1");
    }

    /**
     * <p>Return a reference to the scoped data bean.</p>
     *
     * @return reference to the scoped data bean
     */
    protected ApplicationBean1 getApplicationBean1() {
        return (ApplicationBean1) getBean("ApplicationBean1");
    }

}

