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
import com.sun.webui.jsf.component.Button;
import com.sun.webui.jsf.component.DropDown;
import com.sun.webui.jsf.component.Form;
import com.sun.webui.jsf.component.Head;
import com.sun.webui.jsf.component.Html;
import com.sun.webui.jsf.component.Label;
import com.sun.webui.jsf.component.Link;
import com.sun.webui.jsf.component.Message;
import com.sun.webui.jsf.component.Page;
import com.sun.webui.jsf.component.StaticText;
import com.sun.webui.jsf.component.Table;
import com.sun.webui.jsf.component.TableColumn;
import com.sun.webui.jsf.component.TableRowGroup;
import com.sun.webui.jsf.component.TextArea;
import com.sun.webui.jsf.component.TextField;
import com.sun.webui.jsf.component.Upload;
import com.sun.webui.jsf.model.UploadedFile;
import java.io.File;
import javax.faces.FacesException;
import javax.faces.component.html.HtmlPanelGrid;
import javax.faces.convert.IntegerConverter;
import javax.faces.event.ValueChangeEvent;
import javax.faces.validator.LongRangeValidator;
import javax.servlet.ServletContext;
import movieslib.Movie;
import movieslib.MovieListDataProvider;

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
        yearRangeValidator.setMaximum(2010);
        yearRangeValidator.setMinimum(1895);
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
    private HtmlPanelGrid currentViewPanel = new HtmlPanelGrid();

    public HtmlPanelGrid getCurrentViewPanel() {
        return currentViewPanel;
    }

    public void setCurrentViewPanel(HtmlPanelGrid hpg) {
        this.currentViewPanel = hpg;
    }
    private HtmlPanelGrid addMoviePanel = new HtmlPanelGrid();

    public HtmlPanelGrid getAddMoviePanel() {
        return addMoviePanel;
    }

    public void setAddMoviePanel(HtmlPanelGrid hpg) {
        this.addMoviePanel = hpg;
    }
    private HtmlPanelGrid moviesTablePanel = new HtmlPanelGrid();

    public HtmlPanelGrid getMoviesTablePanel() {
        return moviesTablePanel;
    }

    public void setMoviesTablePanel(HtmlPanelGrid hpg) {
        this.moviesTablePanel = hpg;
    }
    private Label label1 = new Label();

    public Label getLabel1() {
        return label1;
    }

    public void setLabel1(Label l) {
        this.label1 = l;
    }
    private DropDown currentGenre = new DropDown();

    public DropDown getCurrentGenre() {
        return currentGenre;
    }

    public void setCurrentGenre(DropDown dd) {
        this.currentGenre = dd;
    }
    private Table movies = new Table();

    public Table getMovies() {
        return movies;
    }

    public void setMovies(Table t) {
        this.movies = t;
    }
    private TableRowGroup tableRowGroup1 = new TableRowGroup();

    public TableRowGroup getTableRowGroup1() {
        return tableRowGroup1;
    }

    public void setTableRowGroup1(TableRowGroup trg) {
        this.tableRowGroup1 = trg;
    }
    private Button update = new Button();

    public Button getUpdate() {
        return update;
    }

    public void setUpdate(Button b) {
        this.update = b;
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
    private Label label5 = new Label();

    public Label getLabel5() {
        return label5;
    }

    public void setLabel5(Label l) {
        this.label5 = l;
    }
    private Label label6 = new Label();

    public Label getLabel6() {
        return label6;
    }

    public void setLabel6(Label l) {
        this.label6 = l;
    }
    private Label label7 = new Label();

    public Label getLabel7() {
        return label7;
    }

    public void setLabel7(Label l) {
        this.label7 = l;
    }
    private Label label8 = new Label();

    public Label getLabel8() {
        return label8;
    }

    public void setLabel8(Label l) {
        this.label8 = l;
    }
    private DropDown addGenre = new DropDown();

    public DropDown getAddGenre() {
        return addGenre;
    }

    public void setAddGenre(DropDown dd) {
        this.addGenre = dd;
    }
    private Message message1 = new Message();

    public Message getMessage1() {
        return message1;
    }

    public void setMessage1(Message m) {
        this.message1 = m;
    }
    private TextField addTitle = new TextField();

    public TextField getAddTitle() {
        return addTitle;
    }

    public void setAddTitle(TextField tf) {
        this.addTitle = tf;
    }
    private Message message2 = new Message();

    public Message getMessage2() {
        return message2;
    }

    public void setMessage2(Message m) {
        this.message2 = m;
    }
    private TextField addYear = new TextField();

    public TextField getAddYear() {
        return addYear;
    }

    public void setAddYear(TextField tf) {
        this.addYear = tf;
    }
    private Message message3 = new Message();

    public Message getMessage3() {
        return message3;
    }

    public void setMessage3(Message m) {
        this.message3 = m;
    }
    private TextField addLength = new TextField();

    public TextField getAddLength() {
        return addLength;
    }

    public void setAddLength(TextField tf) {
        this.addLength = tf;
    }
    private Message message4 = new Message();

    public Message getMessage4() {
        return message4;
    }

    public void setMessage4(Message m) {
        this.message4 = m;
    }
    private DropDown addRating = new DropDown();

    public DropDown getAddRating() {
        return addRating;
    }

    public void setAddRating(DropDown dd) {
        this.addRating = dd;
    }
    private Message message5 = new Message();

    public Message getMessage5() {
        return message5;
    }

    public void setMessage5(Message m) {
        this.message5 = m;
    }
    private Message message6 = new Message();

    public Message getMessage6() {
        return message6;
    }

    public void setMessage6(Message m) {
        this.message6 = m;
    }
    private TextArea addDescription = new TextArea();

    public TextArea getAddDescription() {
        return addDescription;
    }

    public void setAddDescription(TextArea ta) {
        this.addDescription = ta;
    }
    private Message message7 = new Message();

    public Message getMessage7() {
        return message7;
    }

    public void setMessage7(Message m) {
        this.message7 = m;
    }
    private StaticText staticText4 = new StaticText();

    public StaticText getStaticText4() {
        return staticText4;
    }

    public void setStaticText4(StaticText st) {
        this.staticText4 = st;
    }
    private StaticText staticText5 = new StaticText();

    public StaticText getStaticText5() {
        return staticText5;
    }

    public void setStaticText5(StaticText st) {
        this.staticText5 = st;
    }
    private Button add = new Button();

    public Button getAdd() {
        return add;
    }

    public void setAdd(Button b) {
        this.add = b;
    }
    private StaticText staticText6 = new StaticText();

    public StaticText getStaticText6() {
        return staticText6;
    }

    public void setStaticText6(StaticText st) {
        this.staticText6 = st;
    }
    private LongRangeValidator yearRangeValidator = new LongRangeValidator();

    public LongRangeValidator getYearRangeValidator() {
        return yearRangeValidator;
    }

    public void setYearRangeValidator(LongRangeValidator lrv) {
        this.yearRangeValidator = lrv;
    }
    private LongRangeValidator lengthRangeValidator = new LongRangeValidator();

    public LongRangeValidator getLengthRangeValidator() {
        return lengthRangeValidator;
    }

    public void setLengthRangeValidator(LongRangeValidator lrv) {
        this.lengthRangeValidator = lrv;
    }
    private TableColumn tableColumn1 = new TableColumn();

    public TableColumn getTableColumn1() {
        return tableColumn1;
    }

    public void setTableColumn1(TableColumn tc) {
        this.tableColumn1 = tc;
    }
    private TextField textField1 = new TextField();

    public TextField getTextField1() {
        return textField1;
    }

    public void setTextField1(TextField tf) {
        this.textField1 = tf;
    }
    private TableColumn tableColumn2 = new TableColumn();

    public TableColumn getTableColumn2() {
        return tableColumn2;
    }

    public void setTableColumn2(TableColumn tc) {
        this.tableColumn2 = tc;
    }
    private DropDown dropDown1 = new DropDown();

    public DropDown getDropDown1() {
        return dropDown1;
    }

    public void setDropDown1(DropDown dd) {
        this.dropDown1 = dd;
    }
    private TableColumn tableColumn3 = new TableColumn();

    public TableColumn getTableColumn3() {
        return tableColumn3;
    }

    public void setTableColumn3(TableColumn tc) {
        this.tableColumn3 = tc;
    }
    private TextField textField2 = new TextField();

    public TextField getTextField2() {
        return textField2;
    }

    public void setTextField2(TextField tf) {
        this.textField2 = tf;
    }
    private TableColumn tableColumn4 = new TableColumn();

    public TableColumn getTableColumn4() {
        return tableColumn4;
    }

    public void setTableColumn4(TableColumn tc) {
        this.tableColumn4 = tc;
    }
    private TextField textField3 = new TextField();

    public TextField getTextField3() {
        return textField3;
    }

    public void setTextField3(TextField tf) {
        this.textField3 = tf;
    }
    private TableColumn tableColumn5 = new TableColumn();

    public TableColumn getTableColumn5() {
        return tableColumn5;
    }

    public void setTableColumn5(TableColumn tc) {
        this.tableColumn5 = tc;
    }
    private TextField textField4 = new TextField();

    public TextField getTextField4() {
        return textField4;
    }

    public void setTextField4(TextField tf) {
        this.textField4 = tf;
    }
    private TableColumn tableColumn6 = new TableColumn();

    public TableColumn getTableColumn6() {
        return tableColumn6;
    }

    public void setTableColumn6(TableColumn tc) {
        this.tableColumn6 = tc;
    }
    private TextArea textArea1 = new TextArea();

    public TextArea getTextArea1() {
        return textArea1;
    }

    public void setTextArea1(TextArea ta) {
        this.textArea1 = ta;
    }
    private TableColumn tableColumn7 = new TableColumn();

    public TableColumn getTableColumn7() {
        return tableColumn7;
    }

    public void setTableColumn7(TableColumn tc) {
        this.tableColumn7 = tc;
    }
    private Button preview = new Button();

    public Button getPreview() {
        return preview;
    }

    public void setPreview(Button b) {
        this.preview = b;
    }
    private Button remove = new Button();

    public Button getRemove() {
        return remove;
    }

    public void setRemove(Button b) {
        this.remove = b;
    }
    private Upload uploadIImage = new Upload();

    public Upload getUploadIImage() {
        return uploadIImage;
    }

    public void setUploadIImage(Upload u) {
        this.uploadIImage = u;
    }
    private StaticText imagePad1 = new StaticText();

    public StaticText getImagePad1() {
        return imagePad1;
    }

    public void setImagePad1(StaticText st) {
        this.imagePad1 = st;
    }
    private StaticText imagePad2 = new StaticText();

    public StaticText getImagePad2() {
        return imagePad2;
    }

    public void setImagePad2(StaticText st) {
        this.imagePad2 = st;
    }
    private Button uploadImage = new Button();

    public Button getUploadImage() {
        return uploadImage;
    }

    public void setUploadImage(Button b) {
        this.uploadImage = b;
    }
    private Label label9 = new Label();

    public Label getLabel9() {
        return label9;
    }

    public void setLabel9(Label l) {
        this.label9 = l;
    }
    private TextField addImage = new TextField();

    public TextField getAddImage() {
        return addImage;
    }

    public void setAddImage(TextField tf) {
        this.addImage = tf;
    }
    private Message message8 = new Message();

    public Message getMessage8() {
        return message8;
    }

    public void setMessage8(Message m) {
        this.message8 = m;
    }
    private IntegerConverter yearIntegerConverter = new IntegerConverter();

    public IntegerConverter getYearIntegerConverter() {
        return yearIntegerConverter;
    }

    public void setYearIntegerConverter(IntegerConverter ic) {
        this.yearIntegerConverter = ic;
    }
    private IntegerConverter lengthIntegerConverter = new IntegerConverter();

    public IntegerConverter getLengthIntegerConverter() {
        return lengthIntegerConverter;
    }

    public void setLengthIntegerConverter(IntegerConverter ic) {
        this.lengthIntegerConverter = ic;
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
        if ( this.currentGenre.getValue() == null ) {
            this.currentGenre.setValue(getSessionBean1().getCurrentGenre());
        }
        if ( this.addGenre.getSelected() == null ) {
            this.addGenre.setSelected(getApplicationBean1().getGenreOptions()[0]);
        }
        if ( this.addRating.getSelected() == null ) {
            this.addRating.setSelected(getApplicationBean1().getRatingOptions()[0]);
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
    protected SessionBean1 getSessionBean1() {
        return (SessionBean1) getBean("SessionBean1");
    }

    /**
     * <p>Return a reference to the scoped data bean.</p>
     */
    protected RequestBean1 getRequestBean1() {
        return (RequestBean1) getBean("RequestBean1");
    }

    public String update_action() {
        // TODO: Process the action. Return value is a navigation
        // case name where null will return to the same page.
        MovieListDataProvider movieListDataProvider = getSessionBean1().getMovieListDataProvider();
        try {
            movieListDataProvider.commitChanges();
        }
        catch (Exception e) {
            error("Could not update movies");
            log("Could not update movies: " + e, e);
            movieListDataProvider.revertChanges();
        }
        return null;
    }

    public String add_action() {
        // TODO: Process the action. Return value is a navigation
        // case name where null will return to the same page.
        Movie movie = new Movie();
        movie.setGenre((String) addGenre.getSelected());
        movie.setTitle((String) addTitle.getText());
        movie.setYear((Integer) addYear.getText());
        movie.setLength((Integer) addLength.getText());
        movie.setRating((String) addRating.getSelected());
        movie.setImage((String) addImage.getText());
        movie.setDescription((String) addDescription.getText());
        try {
            getSessionBean1().addMovie(movie);
        } catch (Exception e) {
            error("Could not add movie");
            log("Could not add movie " + movie.getId() + ": " + e, e);
        }
        tableRowGroup1.setFirst(0);
        addGenre.setSelected(null);
        addTitle.setText(null);
        addYear.setText(null);
        addLength.setText(null);
        addRating.setSelected(null);
        uploadIImage.setText(null);
        addDescription.setText(null);
        form1.discardSubmittedValues("update");
        return null;
    }

    public void currentGenre_processValueChange(ValueChangeEvent event) {
        // change current genre
        getSessionBean1().setCurrentGenre((String) event.getNewValue());       
        // the genre virtual form, in which only the currentGenreDropDown participates, has been submitted
        // make sure the input fields in the movies table do not retain their submitted values
        form1.discardSubmittedValues("update");
    }

    public String preview_action() {
        // TODO: Process the action. Return value is a navigation
        // case name where null will return to the same page.
        RowKey rowWhereButtonClicked = tableRowGroup1.getRowKey();
        getRequestBean1().setPreviewRow(rowWhereButtonClicked);
        return "preview";
    }

    public String remove_action() {
        // TODO: Process the action. Return value is a navigation
        // case name where null will return to the same page.
        RowKey rowToRemove = tableRowGroup1.getRowKey();
        MovieListDataProvider movieListDataProvider = getSessionBean1().getMovieListDataProvider();
        Integer movieId = (Integer)movieListDataProvider.getValue(movieListDataProvider.getFieldKey("id"), rowToRemove);
        try {
            movieListDataProvider.removeRow(rowToRemove);
            movieListDataProvider.commitChanges();
        }
        catch (Exception e) {
            error("Could not remove movie");
            log("Could not remove movie " + movieId + ": " + e, e);
            movieListDataProvider.revertChanges();
        }
        tableRowGroup1.setFirst(0);
        //work around limitation in the dataprovider (uses IndexRowKeys)
        //the preview/remove virtual form, which has no participants, has been submitted
        //make sure the input fields in the movies table do not retain their submitted values
        form1.discardSubmittedValues("update");
        return null;
    }

    public String uploadImage_action() {
        // TODO: Process the action. Return value is a navigation
        // case name where null will return to the same page.
        UploadedFile uploadedFile = this.uploadIImage.getUploadedFile();
        String uploadedFileName = uploadedFile.getOriginalName();
        // Some browsers return complete path name, some don't
        // Make sure we only have the file name        
        // First, try forward slash
        int index = uploadedFileName.lastIndexOf('/');
        String justFileName;
        if ( index >= 0) {
            justFileName = uploadedFileName.substring( index + 1 );
        } else {
            // Try backslash
            index = uploadedFileName.lastIndexOf('\\');
            if (index >= 0) {
                justFileName = uploadedFileName.substring( index + 1 );
            } else {
                // No forward or back slashes
                justFileName = uploadedFileName;
            }
        }
        String uploadedFileType = uploadedFile.getContentType();
        if ( uploadedFileType.equals("image/jpeg")
                || uploadedFileType.equals("image/pjpeg")
                || uploadedFileType.equals("image/gif")
                || uploadedFileType.equals("image/png")
                || uploadedFileType.equals("image/x-png")) {
            try {
                // get the path to the /resources/images directory
                // from the servlet context
                ServletContext theApplicationsServletContext = (ServletContext) this.getExternalContext().getContext();
                String realPath = theApplicationsServletContext.getRealPath("/resources");
                File file = new File(realPath + File.separatorChar + justFileName);
                uploadedFile.write(file);
                this.addImage.setText("/resources/" + justFileName);
            } catch (Exception ex) {
                error("Cannot upload file: " + justFileName);
            }
        } else {
            error("You must upload a JPEG, PJPEG, GIF, PNG, or X-PNG file.");
        }
        return null;
    }
}

