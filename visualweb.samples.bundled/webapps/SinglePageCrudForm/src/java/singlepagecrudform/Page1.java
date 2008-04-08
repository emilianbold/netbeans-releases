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

package singlepagecrudform;

import com.sun.data.provider.RowKey;
import com.sun.data.provider.TableCursorVetoException;
import com.sun.data.provider.impl.CachedRowSetDataProvider;
import com.sun.rave.web.ui.appbase.AbstractPageBean;
import com.sun.sql.rowset.CachedRowSetXImpl;
import com.sun.webui.jsf.component.Body;
import com.sun.webui.jsf.component.Button;
import com.sun.webui.jsf.component.Calendar;
import com.sun.webui.jsf.component.DropDown;
import com.sun.webui.jsf.component.Form;
import com.sun.webui.jsf.component.Head;
import com.sun.webui.jsf.component.Html;
import com.sun.webui.jsf.component.Label;
import com.sun.webui.jsf.component.Link;
import com.sun.webui.jsf.component.Message;
import com.sun.webui.jsf.component.MessageGroup;
import com.sun.webui.jsf.component.Page;
import com.sun.webui.jsf.component.StaticText;
import com.sun.webui.jsf.component.TextField;
import java.sql.SQLException;
import javax.faces.FacesException;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.html.HtmlPanelGrid;
import javax.faces.context.FacesContext;
import javax.faces.convert.IntegerConverter;
import javax.faces.event.ValueChangeEvent;
import javax.faces.validator.ValidatorException;

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
        triptypeDataProvider.setCachedRowSet((javax.sql.rowset.CachedRowSet) getValue("#{SessionBean1.triptypeRowSet}"));
    }
    
    private Form form1 = new Form();
    
    public Form getForm1() {
        return form1;
    }
    
    public void setForm1(Form f) {
        this.form1 = f;
    }
    private Button create = new Button();

    public Button getCreate() {
        return create;
    }

    public void setCreate(Button b) {
        this.create = b;
    }
    private Button save = new Button();

    public Button getSave() {
        return save;
    }

    public void setSave(Button b) {
        this.save = b;
    }
    private Button cancel = new Button();

    public Button getCancel() {
        return cancel;
    }

    public void setCancel(Button b) {
        this.cancel = b;
    }
    private Button delete = new Button();

    public Button getDelete() {
        return delete;
    }

    public void setDelete(Button b) {
        this.delete = b;
    }
    private Button first = new Button();

    public Button getFirst() {
        return first;
    }

    public void setFirst(Button b) {
        this.first = b;
    }
    private Button prev = new Button();

    public Button getPrev() {
        return prev;
    }

    public void setPrev(Button b) {
        this.prev = b;
    }
    private Button next = new Button();

    public Button getNext() {
        return next;
    }

    public void setNext(Button b) {
        this.next = b;
    }
    private Button last = new Button();

    public Button getLast() {
        return last;
    }

    public void setLast(Button b) {
        this.last = b;
    }
    private Calendar depDateCalendar = new Calendar();

    public Calendar getDepDateCalendar() {
        return depDateCalendar;
    }

    public void setDepDateCalendar(Calendar c) {
        this.depDateCalendar = c;
    }
    private DropDown tripType = new DropDown();

    public DropDown getTripType() {
        return tripType;
    }

    public void setTripType(DropDown dd) {
        this.tripType = dd;
    }
    private DropDown personId = new DropDown();

    public DropDown getPersonId() {
        return personId;
    }

    public void setPersonId(DropDown dd) {
        this.personId = dd;
    }
    private CachedRowSetDataProvider personDataProvider = new CachedRowSetDataProvider();

    public CachedRowSetDataProvider getPersonDataProvider() {
        return personDataProvider;
    }

    public void setPersonDataProvider(CachedRowSetDataProvider crsdp) {
        this.personDataProvider = crsdp;
    }
    private IntegerConverter personIdConverter = new IntegerConverter();

    public IntegerConverter getPersonIdConverter() {
        return personIdConverter;
    }

    public void setPersonIdConverter(IntegerConverter ic) {
        this.personIdConverter = ic;
    }
    private CachedRowSetDataProvider tripDataProvider = new CachedRowSetDataProvider();

    public CachedRowSetDataProvider getTripDataProvider() {
        return tripDataProvider;
    }

    public void setTripDataProvider(CachedRowSetDataProvider crsdp) {
        this.tripDataProvider = crsdp;
    }
    private CachedRowSetDataProvider triptypeDataProvider = new CachedRowSetDataProvider();

    public CachedRowSetDataProvider getTriptypeDataProvider() {
        return triptypeDataProvider;
    }

    public void setTriptypeDataProvider(CachedRowSetDataProvider crsdp) {
        this.triptypeDataProvider = crsdp;
    }
    private IntegerConverter tripTypeConverter = new IntegerConverter();

    public IntegerConverter getTripTypeConverter() {
        return tripTypeConverter;
    }

    public void setTripTypeConverter(IntegerConverter ic) {
        this.tripTypeConverter = ic;
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
        Integer pid = getSessionBean1().getPersonId();
        Integer tid = getSessionBean1().getTripId();
        if ( pid == null ) {
            // i.e., first time this page is rendered
            personDataProvider.cursorFirst();
            pid = (Integer) personDataProvider.getValue("PERSON.PERSONID");
            try {
                // get trips for this person
                getSessionBean1().getTripRowSet().setObject(1, pid);
                getSessionBean1().getTripRowSet().execute();
            } catch (SQLException sqle) {
                error("Page1::init() -- problem getting trips for the current personId");
                throw  new FacesException(sqle);
            }
            tripDataProvider.cursorFirst();
            //tid = (Integer) tripDataProvider.getValue("TRIP.TRIPID");
        } else {
            RowKey personRowKey = personDataProvider.findFirst("PERSON.PERSONID", pid);
            personDataProvider.setCursorRow(personRowKey);
            // make sure the person has trips, before looking for data
            if ( getSessionBean1().getTripRowSet().size() > 0 ) {
                if ( tid == null ) {
                    tripDataProvider.cursorFirst();
                } else {
                    RowKey tripRowKey = tripDataProvider.findFirst("TRIP.TRIPID", tid);
                    tripDataProvider.setCursorRow(tripRowKey);
                }
            }
        }
        personId.setSelected(pid);    }

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
        boolean tripsFlag = getSessionBean1().getTripRowSet().size() > 0;
        // make sure there are trips for the current person before accessing
        // data in the rowset
        if ( tripsFlag ) {
            java.sql.Date date = (java.sql.Date) tripDataProvider.getValue("TRIP.DEPDATE");
            depDateCalendar.setValue(date);
        } else {
            depDateCalendar.setValue(null);
        }
        boolean addingTrip = getSessionBean1().isAddingTrip();
        // stop user from appending two rows
        create.setDisabled(addingTrip);
        save.setDisabled( ! tripsFlag );
        // the reset button only makes sense while inserting a record
        // and before committing the new record
        cancel.setDisabled(! addingTrip);
        // don't use delete while adding a record
        delete.setDisabled(addingTrip);
        // disable the VCR controls while we're inserting a record
        // to keep the user from navigating
        first.setDisabled(addingTrip);
        prev.setDisabled(addingTrip);
        next.setDisabled(addingTrip);
        last.setDisabled(addingTrip);
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
        // get PKs for each current record and store them in Session Bean
        Integer pid = (Integer) personDataProvider.getValue("PERSON.PERSONID");
        getSessionBean1().setPersonId(pid);
        Integer tid = (Integer) tripDataProvider.getValue("TRIP.TRIPID");
        getSessionBean1().setTripId(tid);
        personDataProvider.close();
        tripDataProvider.close();
        triptypeDataProvider.close();
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
    protected RequestBean1 getRequestBean1() {
        return (RequestBean1) getBean("RequestBean1");
    }

    /**
     * <p>Return a reference to the scoped data bean.</p>
     */
    protected ApplicationBean1 getApplicationBean1() {
        return (ApplicationBean1) getBean("ApplicationBean1");
    }

    public void personId_processValueChange(ValueChangeEvent event) {
        // TODO: Replace with your code
        if ( getSessionBean1().isAddingTrip() ) {
            getSessionBean1().setAddingTrip(false);
            try {
                tripDataProvider.revertChanges();
            } catch (Exception ex) {
                error("Cannot revert changes to TRAVEL.TRIP table.");
            }
        }
        Integer newPersonId = (Integer) personId.getSelected();
        // Find the datatable's row that matches the dropdown's selected person
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
                error("Cannot change to personId " + newPersonId);
            } catch (SQLException sqle) {
                error("SQL excpetion");
            }
        } else {
            // exceptional event we might want to know about
            error("Missing person for personId " + newPersonId);
        }
        form1.discardSubmittedValues("saveVForm");
    }

    public String first_action() {
        // TODO: Process the action. Return value is a navigation
        // case name where null will return to the same page.
        getTripDataProvider().cursorFirst();
        form1.discardSubmittedValues("saveVForm");
        return null;
    }

    public String prev_action() {
        // TODO: Process the action. Return value is a navigation
        // case name where null will return to the same page.
        getTripDataProvider().cursorPrevious();
        form1.discardSubmittedValues("saveVForm");
        return null;
    }

    public String next_action() {
        // TODO: Process the action. Return value is a navigation
        // case name where null will return to the same page.
        getTripDataProvider().cursorNext();
        form1.discardSubmittedValues("saveVForm");
        return null;
    }

    public String last_action() {
        // TODO: Process the action. Return value is a navigation
        // case name where null will return to the same page.
        getTripDataProvider().cursorLast();
        form1.discardSubmittedValues("saveVForm");
        return null;
    }

    public String create_action() {
        // TODO: Process the action. Return value is a navigation
        // case name where null will return to the same page.
        try {
            if ( tripDataProvider.canAppendRow() ) {
                RowKey rowKey = tripDataProvider.appendRow();
                tripDataProvider.setCursorRow(rowKey);
                Integer tripPK =  nextPK();
                tripDataProvider.setValue("TRIP.TRIPID", tripPK);
                getSessionBean1().setAddingTrip(true);
            } else {
                error("Page1::createButton_action() -- cannot append trip record");
            }
        } catch (Exception e) {
            error("Page1::createButton_action() -- something's wrong trying to append trip record");
        }
        form1.discardSubmittedValues("saveVForm");
        return null;
    }

    // Generate an integer Primary Key
    // Return next primary key for TRAVEL.TRIP table
    private Integer nextPK() throws SQLException {
        // create a new rowset
	CachedRowSetXImpl pkRowSet = new CachedRowSetXImpl();
	try {
            // set the rowset to use the Travel database
	    pkRowSet.setDataSourceName("java:comp/env/jdbc/TRAVEL_ApacheDerby");
            // find the highest person id and add one to it
	    pkRowSet.setCommand("SELECT MAX(TRAVEL.TRIP.TRIPID) + 1 FROM TRAVEL.TRIP");
            pkRowSet.setTableName("TRAVEL.TRIP");
            // execute the rowset -- which will contain a single row and single column
            pkRowSet.execute();    
            pkRowSet.next();
            // get the key
            int counter = pkRowSet.getInt(1);
            return new Integer(counter);
	} catch (Exception e) {
            error("Error fetching Max(TRAVEL.TRIP.TRIPID) + 1 : " + e.getMessage());
        } finally {
            pkRowSet.close();
	}
        return null;
    }

    public String save_action() {
        // TODO: Process the action. Return value is a navigation
        // case name where null will return to the same page.
        try {
            Integer tid = (Integer) tripDataProvider.getValue("TRIP.TRIPID");
            java.util.Date uDate = (java.util.Date) depDateCalendar.getValue();
            if ( uDate != null ) {
                java.sql.Date date = new java.sql.Date( uDate.getTime() );
                tripDataProvider.setValue("TRIP.DEPDATE", date);
            } else {
                tripDataProvider.setValue("TRIP.DEPDATE", null);
            }
            if ( getSessionBean1().isAddingTrip() ) {
                Integer pid = (Integer) personId.getValue();
                tripDataProvider.setValue("TRIP.PERSONID", pid);
                tripDataProvider.setValue("TRIP.TRIPTYPEID", tripType.getSelected());
                getSessionBean1().setAddingTrip(false);
            }
            tripDataProvider.commitChanges();
            tripDataProvider.refresh();
            // find the trip just added, and show it in the form
            RowKey rk = tripDataProvider.findFirst("TRIP.TRIPID", tid);
            tripDataProvider.setCursorRow(rk);
        } catch (Exception e) {
            error("Save failed -- cannot commit changes -- " + e);
        }
        return null;
    }

    public String cancel_action() {
        // TODO: Process the action. Return value is a navigation
        // case name where null will return to the same page.
        try {
            if ( getSessionBean1().isAddingTrip() ) {
                getSessionBean1().setAddingTrip(false);
            }
            tripDataProvider.revertChanges();
            tripDataProvider.refresh();
            tripDataProvider.cursorFirst();
        } catch (Exception ex) {
            error("Cannot reset form");
        }
        form1.discardSubmittedValues("saveVForm");
        return null;
    }

    public String delete_action() {
        // TODO: Process the action. Return value is a navigation
        // case name where null will return to the same page.
        Integer tid = getSessionBean1().getTripId();
        RowKey rk = null;
        // are there any trips for this person?
        if ( getSessionBean1().getTripRowSet().size() > 0 ) {
            rk = tripDataProvider.findFirst("TRIP.TRIPID", tid);
            if ( tripDataProvider.canRemoveRow( rk ) ) {
                try {
                    tripDataProvider.cursorFirst();
                    tripDataProvider.removeRow( rk );
                    tripDataProvider.commitChanges();
                    tripDataProvider.refresh();
                    tripDataProvider.cursorFirst();
                } catch (Exception ex) {
                    error("Cannot delete trip record " + tripDataProvider.getValue("TRIP.TRIPTYPEID", rk));
                }
            } else {
                error("Cannot delete trip records from database");
            }
        }
        form1.discardSubmittedValues("saveVForm");
        return null;
    }


    private void excludeDigits(UIComponent component, Object value) {
        String cityName = (String) value;
        for (int i = 0; i < cityName.length(); i++) {
            if (Character.isDigit(cityName.charAt(i)) ) {
                ((UIInput)component).setValid(false);
                throw new ValidatorException(new FacesMessage("Error: City must contain no digits"));            
            }
        }
    }
    
    public void fromCity_validate(FacesContext context, UIComponent component, Object value) {
        // TODO: Replace with your code
        excludeDigits(component, value);
    }


    public void toCity_validate(FacesContext context, UIComponent component, Object value) {
        // TODO: Replace with your code
        excludeDigits(component, value);
    }
}

