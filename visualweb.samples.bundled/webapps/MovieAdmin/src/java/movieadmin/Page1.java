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
    
    private Form form1 = new Form();
    
    public Form getForm1() {
        return form1;
    }
    
    public void setForm1(Form f) {
        this.form1 = f;
    }
    private DropDown currentGenre = new DropDown();

    public DropDown getCurrentGenre() {
        return currentGenre;
    }

    public void setCurrentGenre(DropDown dd) {
        this.currentGenre = dd;
    }
    private TableRowGroup tableRowGroup1 = new TableRowGroup();

    public TableRowGroup getTableRowGroup1() {
        return tableRowGroup1;
    }

    public void setTableRowGroup1(TableRowGroup trg) {
        this.tableRowGroup1 = trg;
    }
    private DropDown addGenre = new DropDown();

    public DropDown getAddGenre() {
        return addGenre;
    }

    public void setAddGenre(DropDown dd) {
        this.addGenre = dd;
    }
    private TextField addTitle = new TextField();

    public TextField getAddTitle() {
        return addTitle;
    }

    public void setAddTitle(TextField tf) {
        this.addTitle = tf;
    }
    private TextField addYear = new TextField();

    public TextField getAddYear() {
        return addYear;
    }

    public void setAddYear(TextField tf) {
        this.addYear = tf;
    }
    private TextField addLength = new TextField();

    public TextField getAddLength() {
        return addLength;
    }

    public void setAddLength(TextField tf) {
        this.addLength = tf;
    }
    private DropDown addRating = new DropDown();

    public DropDown getAddRating() {
        return addRating;
    }

    public void setAddRating(DropDown dd) {
        this.addRating = dd;
    }
    private TextArea addDescription = new TextArea();

    public TextArea getAddDescription() {
        return addDescription;
    }

    public void setAddDescription(TextArea ta) {
        this.addDescription = ta;
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
    private Upload uploadIImage = new Upload();

    public Upload getUploadIImage() {
        return uploadIImage;
    }

    public void setUploadIImage(Upload u) {
        this.uploadIImage = u;
    }
    private TextField addImage = new TextField();

    public TextField getAddImage() {
        return addImage;
    }

    public void setAddImage(TextField tf) {
        this.addImage = tf;
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

