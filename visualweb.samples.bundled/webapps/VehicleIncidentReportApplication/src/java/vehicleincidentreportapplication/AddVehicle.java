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

package vehicleincidentreportapplication;

import com.sun.data.provider.RowKey;
import com.sun.data.provider.impl.CachedRowSetDataProvider;
import com.sun.rave.web.ui.appbase.AbstractPageBean;
import com.sun.sql.rowset.CachedRowSetXImpl;
import com.sun.sql.rowset.CachedRowSetXImpl;
import com.sun.webui.jsf.component.Body;
import com.sun.webui.jsf.component.Button;
import com.sun.webui.jsf.component.DropDown;
import com.sun.webui.jsf.component.Form;
import com.sun.webui.jsf.component.Head;
import com.sun.webui.jsf.component.Html;
import com.sun.webui.jsf.component.Label;
import com.sun.webui.jsf.component.Link;
import com.sun.webui.jsf.component.Message;
import com.sun.webui.jsf.component.MessageGroup;
import com.sun.webui.jsf.component.Page;
import com.sun.webui.jsf.component.TextField;
import java.sql.SQLException;
import javax.faces.FacesException;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlPanelGrid;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import javax.sql.rowset.CachedRowSet;

/**
 * <p>Page bean that corresponds to a similarly named JSP page.  This
 * class contains component definitions (and initialization code) for
 * all components that you have defined on this page, as well as
 * lifecycle methods and event handlers where you may add behavior
 * to respond to incoming events.</p>
 */
public class AddVehicle extends AbstractPageBean {
    // <editor-fold defaultstate="collapsed" desc="Managed Component Definition">
    private int __placeholder;

    /**
     * <p>Automatically managed component initialization.  <strong>WARNING:</strong>
     * This method is automatically generated, so any user-specified code inserted
     * here is subject to being replaced.</p>
     */
    private void _init() throws Exception {
        stateDataProvider.setCachedRowSet((javax.sql.rowset.CachedRowSet) getValue("#{SessionBean1.stateRowSet}"));
    }
    private DropDown state = new DropDown();

    public DropDown getState() {
        return state;
    }

    public void setState(DropDown dd) {
        this.state = dd;
    }
    private TextField licensePlate = new TextField();

    public TextField getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(TextField tf) {
        this.licensePlate = tf;
    }
    private TextField make = new TextField();

    public TextField getMake() {
        return make;
    }

    public void setMake(TextField tf) {
        this.make = tf;
    }
    private TextField model = new TextField();

    public TextField getModel() {
        return model;
    }

    public void setModel(TextField tf) {
        this.model = tf;
    }
    private TextField color = new TextField();

    public TextField getColor() {
        return color;
    }

    public void setColor(TextField tf) {
        this.color = tf;
    }
    private CachedRowSetDataProvider stateDataProvider = new CachedRowSetDataProvider();

    public CachedRowSetDataProvider getStateDataProvider() {
        return stateDataProvider;
    }

    public void setStateDataProvider(CachedRowSetDataProvider crsdp) {
        this.stateDataProvider = crsdp;
    }

    // </editor-fold>

    /**
     * <p>Construct a new Page bean instance.</p>
     */
    public AddVehicle() {
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
            log("AddVehicle Initialization Failure", e);
            throw e instanceof FacesException ? (FacesException) e: new FacesException(e);
        }
        
        // </editor-fold>
        // Perform application initialization that must complete
        // *after* managed components are initialized
        // TODO - add your own initialization code here
        try {
            getSessionBean1().getVehicleRowSet().setObject(1, getSessionBean1().getLoggedInUserId());
        } catch (SQLException sqe) {
            error(sqe.getMessage());
        }
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
        if (
            this.state.getSelected() == null) {
            // arbitrarily select California
            this.state.setSelected("CA");
        }
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
        stateDataProvider.close();
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
    protected SessionBean1 getSessionBean1() {
        return (SessionBean1) getBean("SessionBean1");
    }

    /**
     * <p>Return a reference to the scoped data bean.</p>
     */
    protected RequestBean1 getRequestBean1() {
        return (RequestBean1) getBean("RequestBean1");
    }

    public String addButton_action() {
        // TODO: Process the action. Return value is a navigation
        // case name where null will return to the same page.
        Integer loggedInUserId = getSessionBean1().getLoggedInUserId();
        String theStateId = (String) this.state.getSelected();
        String theLicensePlate = (String) this.licensePlate.getText();
        String theMake = (String) this.make.getText();
        String theModel = (String) this.model.getText();
        String theColor = (String) this.color.getText();
        try {
            CachedRowSet vehicleRowSet = new CachedRowSetXImpl();
            vehicleRowSet.setDataSourceName("java:comp/env/jdbc/VIR_ApacheDerby");
            vehicleRowSet.setTableName("VEHICLE");
            vehicleRowSet.setCommand("SELECT * FROM VIR.VEHICLE");
            CachedRowSetDataProvider vehicleDataProvider = new CachedRowSetDataProvider();
            vehicleDataProvider.setCachedRowSet(vehicleRowSet);
            RowKey rowKey = vehicleDataProvider.appendRow();
            vehicleDataProvider.setValue("VEHICLE.stateId", rowKey, theStateId);
            vehicleDataProvider.setValue("VEHICLE.licensePlate", rowKey, theLicensePlate);
            vehicleDataProvider.setValue("VEHICLE.make", rowKey, theMake);
            vehicleDataProvider.setValue("VEHICLE.model", rowKey, theModel);
            vehicleDataProvider.setValue("VEHICLE.color", rowKey, theColor);
            
            CachedRowSet ownerRowSet = new CachedRowSetXImpl();
            ownerRowSet.setDataSourceName("java:comp/env/jdbc/VIR_ApacheDerby");
            ownerRowSet.setTableName("OWNER");
            ownerRowSet.setCommand("SELECT * FROM VIR.OWNER");
            CachedRowSetDataProvider ownerDataProvider = new CachedRowSetDataProvider();
            ownerDataProvider.setCachedRowSet(ownerRowSet);
            rowKey = ownerDataProvider.appendRow();
            ownerDataProvider.setValue("OWNER.stateId", rowKey, theStateId);
            ownerDataProvider.setValue("OWNER.licensePlate", rowKey, theLicensePlate);
            ownerDataProvider.setValue("OWNER.employeeId", rowKey, loggedInUserId);
            
            vehicleDataProvider.commitChanges();
            ownerDataProvider.commitChanges();
        } catch (Exception e) {
            error(e.getMessage());
        }
        return "vehicles";
    }

    public void state_validate(FacesContext context, UIComponent component, Object value) {
        String theStateId = (String) value;
        if ( theStateId == null || theStateId.equals("any")) {
            throw new ValidatorException( new FacesMessage("Select a valid state.") );
        }
    }
}

