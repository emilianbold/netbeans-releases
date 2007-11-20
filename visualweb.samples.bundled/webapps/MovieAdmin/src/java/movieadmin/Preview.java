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

package movieadmin;

import com.sun.data.provider.RowKey;
import com.sun.rave.web.ui.appbase.AbstractPageBean;
import com.sun.webui.jsf.component.Body;
import com.sun.webui.jsf.component.DropDown;
import com.sun.webui.jsf.component.Form;
import com.sun.webui.jsf.component.Head;
import com.sun.webui.jsf.component.Html;
import com.sun.webui.jsf.component.Hyperlink;
import com.sun.webui.jsf.component.ImageComponent;
import com.sun.webui.jsf.component.Label;
import com.sun.webui.jsf.component.Link;
import com.sun.webui.jsf.component.Message;
import com.sun.webui.jsf.component.Page;
import com.sun.webui.jsf.component.StaticText;
import com.sun.webui.jsf.model.SingleSelectOptionsList;
import javax.faces.FacesException;
import javax.faces.component.html.HtmlPanelGrid;
import javax.faces.event.ValueChangeEvent;
import movieslib.Movie;
import movieslib.MovieListDataProvider;
import movieslib.MovieListDataProvider;

/**
 * <p>Page bean that corresponds to a similarly named JSP page.  This
 * class contains component definitions (and initialization code) for
 * all components that you have defined on this page, as well as
 * lifecycle methods and event handlers where you may add behavior
 * to respond to incoming events.</p>
 */
public class Preview extends AbstractPageBean {
    // <editor-fold defaultstate="collapsed" desc="Managed Component Definition">
    private int __placeholder;

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
    private HtmlPanelGrid mainPanel = new HtmlPanelGrid();

    public HtmlPanelGrid getMainPanel() {
        return mainPanel;
    }

    public void setMainPanel(HtmlPanelGrid hpg) {
        this.mainPanel = hpg;
    }
    private HtmlPanelGrid navigationPanel = new HtmlPanelGrid();

    public HtmlPanelGrid getNavigationPanel() {
        return navigationPanel;
    }

    public void setNavigationPanel(HtmlPanelGrid hpg) {
        this.navigationPanel = hpg;
    }
    private HtmlPanelGrid genrePanel = new HtmlPanelGrid();

    public HtmlPanelGrid getGenrePanel() {
        return genrePanel;
    }

    public void setGenrePanel(HtmlPanelGrid hpg) {
        this.genrePanel = hpg;
    }
    private HtmlPanelGrid moviePanel = new HtmlPanelGrid();

    public HtmlPanelGrid getMoviePanel() {
        return moviePanel;
    }

    public void setMoviePanel(HtmlPanelGrid hpg) {
        this.moviePanel = hpg;
    }
    private Hyperlink returnLink = new Hyperlink();

    public Hyperlink getReturnLink() {
        return returnLink;
    }

    public void setReturnLink(Hyperlink h) {
        this.returnLink = h;
    }
    private StaticText title = new StaticText();

    public StaticText getTitle() {
        return title;
    }

    public void setTitle(StaticText st) {
        this.title = st;
    }
    private StaticText year = new StaticText();

    public StaticText getYear() {
        return year;
    }

    public void setYear(StaticText st) {
        this.year = st;
    }
    private ImageComponent image = new ImageComponent();

    public ImageComponent getImage() {
        return image;
    }

    public void setImage(ImageComponent ic) {
        this.image = ic;
    }
    private HtmlPanelGrid detailsPanel = new HtmlPanelGrid();

    public HtmlPanelGrid getDetailsPanel() {
        return detailsPanel;
    }

    public void setDetailsPanel(HtmlPanelGrid hpg) {
        this.detailsPanel = hpg;
    }
    private Label genre1 = new Label();

    public Label getGenre1() {
        return genre1;
    }

    public void setGenre1(Label l) {
        this.genre1 = l;
    }
    private DropDown genre = new DropDown();

    public DropDown getGenre() {
        return genre;
    }

    public void setGenre(DropDown dd) {
        this.genre = dd;
    }
    private Message message1 = new Message();

    public Message getMessage1() {
        return message1;
    }

    public void setMessage1(Message m) {
        this.message1 = m;
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
    private Label label3 = new Label();

    public Label getLabel3() {
        return label3;
    }

    public void setLabel3(Label l) {
        this.label3 = l;
    }
    private Label label4 = new Label();

    public Label getLabel4() {
        return label4;
    }

    public void setLabel4(Label l) {
        this.label4 = l;
    }
    private StaticText detailsGenre = new StaticText();

    public StaticText getDetailsGenre() {
        return detailsGenre;
    }

    public void setDetailsGenre(StaticText st) {
        this.detailsGenre = st;
    }
    private StaticText detailsRating = new StaticText();

    public StaticText getDetailsRating() {
        return detailsRating;
    }

    public void setDetailsRating(StaticText st) {
        this.detailsRating = st;
    }
    private StaticText detailsLength = new StaticText();

    public StaticText getDetailsLength() {
        return detailsLength;
    }

    public void setDetailsLength(StaticText st) {
        this.detailsLength = st;
    }
    private StaticText detailsDescription = new StaticText();

    public StaticText getDetailsDescription() {
        return detailsDescription;
    }

    public void setDetailsDescription(StaticText st) {
        this.detailsDescription = st;
    }

    // </editor-fold>

    /**
     * <p>Construct a new Page bean instance.</p>
     */
    public Preview() {
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
            log("Preview Initialization Failure", e);
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
        RowKey newRowToPreview = getRequestBean1().getPreviewRow();
        if (newRowToPreview != null) {
            getSessionBean1().getMovieListDataProvider().setCursorRow(newRowToPreview);
        }
        if ( this.genre.getSelected() == null ) {
            MovieListDataProvider dp = getSessionBean1().getMovieListDataProvider();
            String selectedGenre = (String) dp.getValue(dp.getFieldKey("genre"));
            this.genre.setSelected(selectedGenre);
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

    /**
     * <p>Return a reference to the scoped data bean.</p>
     */
    protected SessionBean1 getSessionBean1() {
        return (SessionBean1) getBean("SessionBean1");
    }

    public String returnLink_action() {
        // TODO: Process the action. Return value is a navigation
        // case name where null will return to the same page.
        return "page1";
    }

    public void genre_processValueChange(ValueChangeEvent event) {
        MovieListDataProvider dp = getSessionBean1().getMovieListDataProvider();
        RowKey movieRowKey = dp.getCursorRow();
        Movie movie = (Movie) dp.getObject(movieRowKey);
        try {
            getSessionBean1().reviseGenre(movie, (String)genre.getSelected());
        }
        catch (Exception e) {
            error("Could not revise movie genre");
            log("Could not revise genre of movie " + (movie == null ? "(null movie)" : movie.getId().toString()) + ": " + e, e);
        }
    }
}

