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

import com.sun.rave.web.ui.appbase.AbstractFragmentBean;
import com.sun.webui.jsf.component.Hyperlink;
import com.sun.webui.jsf.component.ImageComponent;
import com.sun.webui.jsf.component.StaticText;
import javax.faces.FacesException;
import javax.faces.component.html.HtmlPanelGrid;

/**
 * <p>Fragment bean that corresponds to a similarly named JSP page
 * fragment.  This class contains component definitions (and initialization
 * code) for all components that you have defined on this fragment, as well as
 * lifecycle methods and event handlers where you may add behavior
 * to respond to incoming events.</p>
 */
public class Header extends AbstractFragmentBean {
    // <editor-fold defaultstate="collapsed" desc="Managed Component Definition">
    private int __placeholder;

    /**
     * <p>Automatically managed component initialization. <strong>WARNING:</strong>
     * This method is automatically generated, so any user-specified code inserted
     * here is subject to being replaced.</p>
     */
    private void _init() throws Exception {
    }
    private HtmlPanelGrid header = new HtmlPanelGrid();

    public HtmlPanelGrid getHeader() {
        return header;
    }

    public void setHeader(HtmlPanelGrid hpg) {
        this.header = hpg;
    }
    private HtmlPanelGrid subHeader = new HtmlPanelGrid();

    public HtmlPanelGrid getSubHeader() {
        return subHeader;
    }

    public void setSubHeader(HtmlPanelGrid hpg) {
        this.subHeader = hpg;
    }
    private HtmlPanelGrid headerGrid = new HtmlPanelGrid();

    public HtmlPanelGrid getHeaderGrid() {
        return headerGrid;
    }

    public void setHeaderGrid(HtmlPanelGrid hpg) {
        this.headerGrid = hpg;
    }
    private HtmlPanelGrid subHeaderGrid = new HtmlPanelGrid();

    public HtmlPanelGrid getSubHeaderGrid() {
        return subHeaderGrid;
    }

    public void setSubHeaderGrid(HtmlPanelGrid hpg) {
        this.subHeaderGrid = hpg;
    }
    private HtmlPanelGrid logoGrid = new HtmlPanelGrid();

    public HtmlPanelGrid getLogoGrid() {
        return logoGrid;
    }

    public void setLogoGrid(HtmlPanelGrid hpg) {
        this.logoGrid = hpg;
    }
    private HtmlPanelGrid appNameGrid = new HtmlPanelGrid();

    public HtmlPanelGrid getAppNameGrid() {
        return appNameGrid;
    }

    public void setAppNameGrid(HtmlPanelGrid hpg) {
        this.appNameGrid = hpg;
    }
    private ImageComponent logoImage = new ImageComponent();

    public ImageComponent getLogoImage() {
        return logoImage;
    }

    public void setLogoImage(ImageComponent ic) {
        this.logoImage = ic;
    }
    private StaticText appNameDark = new StaticText();

    public StaticText getAppNameDark() {
        return appNameDark;
    }

    public void setAppNameDark(StaticText st) {
        this.appNameDark = st;
    }
    private StaticText appNameBar = new StaticText();

    public StaticText getAppNameBar() {
        return appNameBar;
    }

    public void setAppNameBar(StaticText st) {
        this.appNameBar = st;
    }
    private StaticText appNameLight = new StaticText();

    public StaticText getAppNameLight() {
        return appNameLight;
    }

    public void setAppNameLight(StaticText st) {
        this.appNameLight = st;
    }
    private HtmlPanelGrid searchCell = new HtmlPanelGrid();

    public HtmlPanelGrid getSearchCell() {
        return searchCell;
    }

    public void setSearchCell(HtmlPanelGrid hpg) {
        this.searchCell = hpg;
    }
    private HtmlPanelGrid profileCell = new HtmlPanelGrid();

    public HtmlPanelGrid getProfileCell() {
        return profileCell;
    }

    public void setProfileCell(HtmlPanelGrid hpg) {
        this.profileCell = hpg;
    }
    private HtmlPanelGrid vehiclesCell = new HtmlPanelGrid();

    public HtmlPanelGrid getVehiclesCell() {
        return vehiclesCell;
    }

    public void setVehiclesCell(HtmlPanelGrid hpg) {
        this.vehiclesCell = hpg;
    }
    private HtmlPanelGrid loginCell = new HtmlPanelGrid();

    public HtmlPanelGrid getLoginCell() {
        return loginCell;
    }

    public void setLoginCell(HtmlPanelGrid hpg) {
        this.loginCell = hpg;
    }
    private HtmlPanelGrid welcomeCell = new HtmlPanelGrid();

    public HtmlPanelGrid getWelcomeCell() {
        return welcomeCell;
    }

    public void setWelcomeCell(HtmlPanelGrid hpg) {
        this.welcomeCell = hpg;
    }
    private HtmlPanelGrid logoutCell = new HtmlPanelGrid();

    public HtmlPanelGrid getLogoutCell() {
        return logoutCell;
    }

    public void setLogoutCell(HtmlPanelGrid hpg) {
        this.logoutCell = hpg;
    }
    private HtmlPanelGrid helpCell = new HtmlPanelGrid();

    public HtmlPanelGrid getHelpCell() {
        return helpCell;
    }

    public void setHelpCell(HtmlPanelGrid hpg) {
        this.helpCell = hpg;
    }
    private Hyperlink searchLink = new Hyperlink();

    public Hyperlink getSearchLink() {
        return searchLink;
    }

    public void setSearchLink(Hyperlink h) {
        this.searchLink = h;
    }
    private Hyperlink profileLink = new Hyperlink();

    public Hyperlink getProfileLink() {
        return profileLink;
    }

    public void setProfileLink(Hyperlink h) {
        this.profileLink = h;
    }
    private Hyperlink vehiclesLink = new Hyperlink();

    public Hyperlink getVehiclesLink() {
        return vehiclesLink;
    }

    public void setVehiclesLink(Hyperlink h) {
        this.vehiclesLink = h;
    }
    private Hyperlink loginLink = new Hyperlink();

    public Hyperlink getLoginLink() {
        return loginLink;
    }

    public void setLoginLink(Hyperlink h) {
        this.loginLink = h;
    }
    private Hyperlink logoutLink = new Hyperlink();

    public Hyperlink getLogoutLink() {
        return logoutLink;
    }

    public void setLogoutLink(Hyperlink h) {
        this.logoutLink = h;
    }
    private Hyperlink helpLink = new Hyperlink();

    public Hyperlink getHelpLink() {
        return helpLink;
    }

    public void setHelpLink(Hyperlink h) {
        this.helpLink = h;
    }
    private StaticText welcome = new StaticText();

    public StaticText getWelcome() {
        return welcome;
    }

    public void setWelcome(StaticText st) {
        this.welcome = st;
    }
    // </editor-fold>

    public Header() {
    }

    /**
     * <p>Callback method that is called whenever a page containing
     * this page fragment is navigated to, either directly via a URL,
     * or indirectly via page navigation.  Override this method to acquire
     * resources that will be needed for event handlers and lifecycle methods.</p>
     * 
     * <p>The default implementation does nothing.</p>
     */
    public void init() {
        // Perform initializations inherited from our superclass
        super.init();
        // Perform application initialization that must complete
        // *before* managed components are initialized
        // TODO - add your own initialiation code here
        
        
        // <editor-fold defaultstate="collapsed" desc="Visual-Web-managed Component Initialization">
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
     * <p>Callback method that is called after rendering is completed for
     * this request, if <code>init()</code> was called.  Override this
     * method to release resources acquired in the <code>init()</code>
     * resources that will be needed for event handlers and lifecycle methods.</p>
     * 
     * <p>The default implementation does nothing.</p>
     */
    public void destroy() {
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

    public String searchLink_action() {
        // TODO: Process the action. Return value is a navigation
        // case name where null will return to the same page.
        return "search";
    }

    public String profileLink_action() {
        // TODO: Process the action. Return value is a navigation
        // case name where null will return to the same page.
        String result = "login";
        if ( getSessionBean1().isLoggedIn() ) {
            result = "profile";
        }
        return result;
    }

    public String vehiclesLink_action() {
        // TODO: Process the action. Return value is a navigation
        // case name where null will return to the same page.
        String result = "login";
        if ( getSessionBean1().isLoggedIn() ) {
            result = "vehicles";
        }
        return result;
    }

    public String loginLink_action() {
        // TODO: Process the action. Return value is a navigation
        // case name where null will return to the same page.
        return "login";
    }

    public String logoutLink_action() {
        // TODO: Process the action. Return value is a navigation
        // case name where null will return to the same page.
        String result = "search";
        getSessionBean1().setLoggedIn(false);
        getSessionBean1().setLoggedInUserName(null);
        return result;
    }

    public String helpLink_action() {
        // TODO: Process the action. Return value is a navigation
        // case name where null will return to the same page.
        return "help";
    }
}
