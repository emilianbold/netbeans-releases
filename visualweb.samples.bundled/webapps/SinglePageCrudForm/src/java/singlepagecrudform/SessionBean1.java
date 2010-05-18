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

package singlepagecrudform;

import com.sun.rave.web.ui.appbase.AbstractSessionBean;
import com.sun.sql.rowset.CachedRowSetXImpl;
import javax.faces.FacesException;

/**
 * <p>Session scope data bean for your application.  Create properties
 *  here to represent cached data that should be made available across
 *  multiple HTTP requests for an individual user.</p>
 *
 * <p>An instance of this class will be created for you automatically,
 * the first time your application evaluates a value binding expression
 * or method binding expression that references a managed bean using
 * this class.</p>
 */
public class SessionBean1 extends AbstractSessionBean {
    // <editor-fold defaultstate="collapsed" desc="Managed Component Definition">
    private int __placeholder;

    /**
     * <p>Automatically managed component initialization.  <strong>WARNING:</strong>
     * This method is automatically generated, so any user-specified code inserted
     * here is subject to being replaced.</p>
     */
    private void _init() throws Exception {
        personRowSet.setDataSourceName("java:comp/env/jdbc/TRAVEL_ApacheDerby");
        personRowSet.setCommand("SELECT * FROM TRAVEL.PERSON");
        personRowSet.setTableName("PERSON");
        tripRowSet.setDataSourceName("java:comp/env/jdbc/TRAVEL_ApacheDerby");
        tripRowSet.setCommand("SELECT ALL \"TRAVEL\".\"TRIP\".\"TRIPID\", \r\n                    \"TRAVEL\".\"TRIP\".\"PERSONID\", \r\n                    \"TRAVEL\".\"TRIP\".\"DEPDATE\", \r\n                    \"TRAVEL\".\"TRIP\".\"DEPCITY\", \r\n                    \"TRAVEL\".\"TRIP\".\"DESTCITY\", \r\n                    \"TRAVEL\".\"TRIP\".\"TRIPTYPEID\", \r\n                    \"TRAVEL\".\"TRIP\".\"LASTUPDATED\" \r\nFROM TRAVEL.TRIP\r\nWHERE \"TRAVEL\".\"TRIP\".\"PERSONID\" = ? ");
        tripRowSet.setTableName("TRIP");
        triptypeRowSet.setDataSourceName("java:comp/env/jdbc/TRAVEL_ApacheDerby");
        triptypeRowSet.setCommand("SELECT * FROM TRAVEL.TRIPTYPE");
        triptypeRowSet.setTableName("TRIPTYPE");
    }
    private CachedRowSetXImpl personRowSet = new CachedRowSetXImpl();

    public CachedRowSetXImpl getPersonRowSet() {
        return personRowSet;
    }

    public void setPersonRowSet(CachedRowSetXImpl crsxi) {
        this.personRowSet = crsxi;
    }
    private CachedRowSetXImpl tripRowSet = new CachedRowSetXImpl();

    public CachedRowSetXImpl getTripRowSet() {
        return tripRowSet;
    }

    public void setTripRowSet(CachedRowSetXImpl crsxi) {
        this.tripRowSet = crsxi;
    }
    private CachedRowSetXImpl triptypeRowSet = new CachedRowSetXImpl();

    public CachedRowSetXImpl getTriptypeRowSet() {
        return triptypeRowSet;
    }

    public void setTriptypeRowSet(CachedRowSetXImpl crsxi) {
        this.triptypeRowSet = crsxi;
    }
    // </editor-fold>

    /**
     * <p>Construct a new session data bean instance.</p>
     */
    public SessionBean1() {
    }

    /**
     * <p>This method is called when this bean is initially added to
     * session scope.  Typically, this occurs as a result of evaluating
     * a value binding or method binding expression, which utilizes the
     * managed bean facility to instantiate this bean and store it into
     * session scope.</p>
     * 
     * <p>You may customize this method to initialize and cache data values
     * or resources that are required for the lifetime of a particular
     * user session.</p>
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
            log("SessionBean1 Initialization Failure", e);
            throw e instanceof FacesException ? (FacesException) e: new FacesException(e);
        }
        
        // </editor-fold>
        // Perform application initialization that must complete
        // *after* managed components are initialized
        // TODO - add your own initialization code here
    }

    /**
     * <p>This method is called when the session containing it is about to be
     * passivated.  Typically, this occurs in a distributed servlet container
     * when the session is about to be transferred to a different
     * container instance, after which the <code>activate()</code> method
     * will be called to indicate that the transfer is complete.</p>
     * 
     * <p>You may customize this method to release references to session data
     * or resources that can not be serialized with the session itself.</p>
     */
    @Override
    public void passivate() {
    }

    /**
     * <p>This method is called when the session containing it was
     * reactivated.</p>
     * 
     * <p>You may customize this method to reacquire references to session
     * data or resources that could not be serialized with the
     * session itself.</p>
     */
    @Override
    public void activate() {
    }

    /**
     * <p>This method is called when this bean is removed from
     * session scope.  Typically, this occurs as a result of
     * the session timing out or being terminated by the application.</p>
     * 
     * <p>You may customize this method to clean up resources allocated
     * during the execution of the <code>init()</code> method, or
     * at any later time during the lifetime of the application.</p>
     */
    @Override
    public void destroy() {
    }

    /**
     * <p>Return a reference to the scoped data bean.</p>
     */
    protected ApplicationBean1 getApplicationBean1() {
        return (ApplicationBean1) getBean("ApplicationBean1");
    }
    
    private Integer personId;

    public Integer getPersonId() {
        return personId;
    }

    public void setPersonId(Integer personId) {
        this.personId = personId;
    }
    
    private Integer tripId;

    public Integer getTripId() {
        return tripId;
    }

    public void setTripId(Integer tripId) {
        this.tripId = tripId;
    }
    
    private boolean addingTrip;

    public boolean isAddingTrip() {
        return addingTrip;
    }

    public void setAddingTrip(boolean addingTrip) {
        this.addingTrip = addingTrip;
    }
}
