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

package twopagecrudtable;

import com.sun.data.provider.RowKey;
import com.sun.data.provider.TableCursorVetoException;
import com.sun.data.provider.impl.CachedRowSetDataProvider;
import com.sun.rave.web.ui.appbase.AbstractPageBean;
import com.sun.webui.jsf.component.Body;
import com.sun.webui.jsf.component.Button;
import com.sun.webui.jsf.component.DropDown;
import com.sun.webui.jsf.component.Form;
import com.sun.webui.jsf.component.Head;
import com.sun.webui.jsf.component.Html;
import com.sun.webui.jsf.component.Label;
import com.sun.webui.jsf.component.Link;
import com.sun.webui.jsf.component.MessageGroup;
import com.sun.webui.jsf.component.Page;
import com.sun.webui.jsf.component.StaticText;
import com.sun.webui.jsf.component.Table;
import com.sun.webui.jsf.component.TableColumn;
import com.sun.webui.jsf.component.TableRowGroup;
import com.sun.webui.jsf.model.DefaultTableDataProvider;
import java.sql.SQLException;
import java.util.Date;
import javax.faces.FacesException;
import javax.faces.component.html.HtmlPanelGrid;
import javax.faces.convert.IntegerConverter;
import javax.faces.event.ValueChangeEvent;

/**
 * <p>Page bean that corresponds to a similarly named JSP page.  This
 * class contains component definitions (and initialization code) for
 * all components that you have defined on this page, as well as
 * lifecycle methods and event handlers where you may add behavior
 * to respond to incoming events.</p>
 */
public class Page1 extends AbstractPageBean {
    // <editor-fold defaultstate="collapsed" desc="Managed Component Definition">
    private int __placeholder;

    /**
     * <p>Automatically managed component initialization.  <strong>WARNING:</strong>
     * This method is automatically generated, so any user-specified code inserted
     * here is subject to being replaced.</p>
     */
    private void _init() throws Exception {
        personDataProvider.setCachedRowSet((javax.sql.rowset.CachedRowSet) getValue("#{SessionBean1.personRowSet}"));
        tripDataProvider.setCachedRowSet((javax.sql.rowset.CachedRowSet) getValue("#{SessionBean1.tripRowSet}"));
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
    private HtmlPanelGrid mainPanel = new HtmlPanelGrid();

    public HtmlPanelGrid getMainPanel() {
        return mainPanel;
    }

    public void setMainPanel(HtmlPanelGrid hpg) {
        this.mainPanel = hpg;
    }
    private HtmlPanelGrid personPanel = new HtmlPanelGrid();

    public HtmlPanelGrid getPersonPanel() {
        return personPanel;
    }

    public void setPersonPanel(HtmlPanelGrid hpg) {
        this.personPanel = hpg;
    }
    private HtmlPanelGrid tablePanel = new HtmlPanelGrid();

    public HtmlPanelGrid getTablePanel() {
        return tablePanel;
    }

    public void setTablePanel(HtmlPanelGrid hpg) {
        this.tablePanel = hpg;
    }
    private Label label1 = new Label();

    public Label getLabel1() {
        return label1;
    }

    public void setLabel1(Label l) {
        this.label1 = l;
    }
    private Label label2 = new Label();

    public Label getLabel2() {
        return label2;
    }

    public void setLabel2(Label l) {
        this.label2 = l;
    }
    private DropDown personDropDown = new DropDown();

    public DropDown getPersonDropDown() {
        return personDropDown;
    }

    public void setPersonDropDown(DropDown dd) {
        this.personDropDown = dd;
    }
    private StaticText jobTitleText = new StaticText();

    public StaticText getJobTitleText() {
        return jobTitleText;
    }

    public void setJobTitleText(StaticText st) {
        this.jobTitleText = st;
    }
    private Table tripsTable = new Table();

    public Table getTripsTable() {
        return tripsTable;
    }

    public void setTripsTable(Table t) {
        this.tripsTable = t;
    }
    private TableRowGroup tableRowGroup1 = new TableRowGroup();

    public TableRowGroup getTableRowGroup1() {
        return tableRowGroup1;
    }

    public void setTableRowGroup1(TableRowGroup trg) {
        this.tableRowGroup1 = trg;
    }
    private Button createButton = new Button();

    public Button getCreateButton() {
        return createButton;
    }

    public void setCreateButton(Button b) {
        this.createButton = b;
    }
    private MessageGroup messageGroup1 = new MessageGroup();

    public MessageGroup getMessageGroup1() {
        return messageGroup1;
    }

    public void setMessageGroup1(MessageGroup mg) {
        this.messageGroup1 = mg;
    }
    private CachedRowSetDataProvider personDataProvider = new CachedRowSetDataProvider();

    public CachedRowSetDataProvider getPersonDataProvider() {
        return personDataProvider;
    }

    public void setPersonDataProvider(CachedRowSetDataProvider crsdp) {
        this.personDataProvider = crsdp;
    }
    private IntegerConverter personDropDownConverter = new IntegerConverter();

    public IntegerConverter getPersonDropDownConverter() {
        return personDropDownConverter;
    }

    public void setPersonDropDownConverter(IntegerConverter ic) {
        this.personDropDownConverter = ic;
    }
    private CachedRowSetDataProvider tripDataProvider = new CachedRowSetDataProvider();

    public CachedRowSetDataProvider getTripDataProvider() {
        return tripDataProvider;
    }

    public void setTripDataProvider(CachedRowSetDataProvider crsdp) {
        this.tripDataProvider = crsdp;
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
    private Button deleteButton = new Button();

    public Button getDeleteButton() {
        return deleteButton;
    }

    public void setDeleteButton(Button b) {
        this.deleteButton = b;
    }
    private Button updateButton = new Button();

    public Button getUpdateButton() {
        return updateButton;
    }

    public void setUpdateButton(Button b) {
        this.updateButton = b;
    }

    // </editor-fold>

    /**
     * <p>Construct a new Page bean instance.</p>
     */
    public Page1() {
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
        //set minimum and maximum dates allowed for trips
        Date minDate = new Date (99, 0, 1);
        getSessionBean1().setMinDate(minDate);
        
        Date maxDate = new Date(108, 11, 31);
        getSessionBean1().setMaxDate(maxDate);
       
        
        // Restore the current person and trip
        Integer pid = getSessionBean1().getCurrentPersonId();
        tripDataProvider.refresh();
        if (pid == null) {
            personDataProvider.cursorFirst();
            pid = (Integer) personDataProvider.getValue("PERSON.PERSONID");
            try {
                getSessionBean1().getTripRowSet().setObject(1, pid);
                getSessionBean1().getTripRowSet().execute();
                tripDataProvider.cursorFirst();
            } catch (Exception ex) {
                log("Error Description", ex);
            }
        } else {
            RowKey personRowKey = personDataProvider.findFirst("PERSON.PERSONID", pid);
            personDataProvider.setCursorRow(personRowKey);
        }
        personDropDown.setSelected(pid);    }

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
        // Save away the current cursor position  
        Integer pid = (Integer) personDataProvider.getValue("PERSON.PERSONID");
        getSessionBean1().setCurrentPersonId(pid);
        personDataProvider.close();
        tripDataProvider.close();
    }

    /**
     * <p>Return a reference to the scoped data bean.</p>
     */
    protected SessionBean1 getSessionBean1() {
        return (SessionBean1) getBean("SessionBean1");
    }

    /**
     * <p>Return a reference to the scoped data bean.</p>
     */
    protected ApplicationBean1 getApplicationBean1() {
        return (ApplicationBean1) getBean("ApplicationBean1");
    }

    /**
     * <p>Return a reference to the scoped data bean.</p>
     */
    protected RequestBean1 getRequestBean1() {
        return (RequestBean1) getBean("RequestBean1");
    }

    public void personDropDown_processValueChange(ValueChangeEvent event) {
        Integer newPersonId = (Integer) personDropDown.getSelected();
        // Find the datatable's row that matches the dropdown's
        // selected person
        RowKey rowKey = personDataProvider.findFirst("PERSON.PERSONID", newPersonId);
        // just in case someone deleted row after dropdown was populated
        if (rowKey != null) {
            try {
                // change cursor position
                personDataProvider.setCursorRow(rowKey);
                getSessionBean1().getTripRowSet().setObject(1, newPersonId);
                getSessionBean1().getTripRowSet().execute();
                tripDataProvider.cursorFirst();
            } catch (TableCursorVetoException tcve) {
                error("Cannot change to personDropDown " + newPersonId);
            } catch (SQLException sqle) {
                error("Cannot retrieve trips");
            }
        } else {
            // exceptional event we might want to know about
            error("Missing person for personDropDown " + newPersonId);
        }
    }

    public String updateButton_action() {
        // TODO: Process the action. Return value is a navigation
        // case name where null will return to the same page.
        RowKey rowKey = tableRowGroup1.getRowKey();
        Integer tid = (Integer) tripDataProvider.getValue("TRIP.TRIPID", rowKey);
        getSessionBean1().setCurrentTripId(tid);
        return "updateCase";
    }

    public String deleteButton_action() {
        // TODO: Process the action. Return value is a navigation
        // case name where null will return to the same page.travel
        RowKey rowKey = tableRowGroup1.getRowKey();
        try {
            tripDataProvider.removeRow(rowKey);
            tripDataProvider.commitChanges();
        } catch (Exception e) {
            error("Cannot delete trip with row key " + rowKey + e);
        }
        return null;
    }

    public String createButton_action() {
        // TODO: Process the action. Return value is a navigation
        // case name where null will return to the same page.
        getSessionBean1().setCurrentPersonId((Integer) personDropDown.getValue());
        return "createCase";
    }
}

