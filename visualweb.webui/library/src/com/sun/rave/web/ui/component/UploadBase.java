/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package com.sun.rave.web.ui.component;

import java.io.IOException;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.el.MethodBinding;
import javax.faces.el.ValueBinding;

/**
 * <p>Use the ui:upload tag to create a component that can be used to
 * browse the local file system for a file, and upload a copy of the
 * file's contents to the web application.</p> 
 * 
 * 
 * <h3>HTML Elements and Layout</h3> 
 * 
 * <p>The Upload component produces an XHTML &lt;input type="file"&gt;
 *     element, which displays a text input field with an adjacent Browse button.
 *     The user can type a file name or click the Browse button to select
 *     a file.  When the form is submitted, the file is uploaded.  Note
 *     that this tag requires the use of a filter.</p> 
 * 
 * 
 * <h3>Configuring the UploadFilter</h3> 
 * 
 * <p>In order for the <code>ui:upload</code> tag to work, you must
 *     configure the web application to use the
 *     <code>com.sun.rave.web.ui.util.UploadFilter</code>. 
 * Configure the filter by declaring a filter element in the web application's
 *  deployment descriptor, <code>web.xml</code>.</p> 
 *  <pre> 
 *   &lt;filter&gt;
 *     &lt;filter-name&gt;UploadFilter&lt;/filter-name&gt;
 *     &lt;filter-class&gt;com.sun.rave.web.ui.util.UploadFilter&lt;/filter-class&gt;
 *   &lt;/filter&gt;
 *  </pre>
 * <p>Map the filter to the FacesServlet by adding the following filter
 *     mapping in the same file, for example</p>
 * <pre>
 *   &lt;filter-mapping&gt;
 *     &lt;filter-name&gt;UploadFilter&lt;/filter-name&gt;
 *     &lt;servlet-name&gt;FacesServlet&lt;/servlet-name&gt;
 *   &lt;/filter-mapping&gt;
 *  </pre> 
 * <p>The UploadFilter uses the Apache commons fileupload package. You
 *     can optionally configure the parameters of the DiskFileUpload
 *     class by specifying init parameters on the UploadFilter. The
 *     following parameters are available: 
 * <ul> 
 * <li><code>maxSize</code> The maximum allowed upload size in bytes. 
 * If negative, there is no maximum. The default value is 1,000,000.</li> 
 * 
 * <li><code>sizeThreshold</code>The implementation of the uploading 
 *  functionality uses temporary storage of the file contents before the 
 *  Upload component stores them per its configuration. In the temporary 
 *  storage, smaller files are stored in memory while larger files are 
 *  written directly to disk . Use this parameter 
 *  to specify an integer value of the cut-off where files should be 
 *  written to disk. The default value is 4096 bytes.</li> 
 * <li><code>tmpDir</code> Use this directory to specify the directory to 
 * be used for temporary storage of files. The default behaviour is to use
 * the directory specified in the system property "java.io.tmpdir". </li> 
 * </ul> 
 * 
 *     <h3>The <code>UploadedFile</code> model object</h3>
 * 
 * <p>The contents of the uploaded file, together with some information
 * about it are stored in an instance of
 * <code>com.sun.rave.web.ui.model.UploadedFile</code>. Using this object you
 * can get the content of the file as a String or write the contents to
 * disk, as well as get properties such as the name and the size of the
 * file. In the interest of conserving memory, the contents and file data
 * are only available during the HTTP request in which the file was
 * uploaded.</p>
 * 
 * <TABLE BORDER="1" WIDTH="100%" CELLPADDING="3" CELLSPACING="0" SUMMARY="">
 * <TR BGCOLOR="#CCCCFF">
 * <TH ALIGN="left" COLSPAN="2">
 * <B>UploadedFile Method Summary</B></TH>
 * </TR>
 * <TR BGCOLOR="white">
 * <TD ALIGN="right" VALIGN="top" WIDTH="1%"><FONT SIZE="-1">
 * <CODE>&nbsp;void</CODE></FONT></TD>
 * <TD><CODE><B><code>dispose</code></B>()</CODE>
 * 
 * <BR>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Dispose of the resources associated with the file upload (this will
 *  happen automatically when the resource is garbage collected).</TD>
 * </TR>
 * <TR BGCOLOR="white">
 * <TD ALIGN="right" VALIGN="top" WIDTH="1%"><FONT SIZE="-1">
 * <CODE>&nbsp;java.lang.String</CODE></FONT></TD>
 * <TD><CODE><B><code>getAsString</code></B>()</CODE>
 * 
 * <BR>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Use this method to retrieve the contents of the file as a String</TD>
 * </TR>
 * <TR BGCOLOR="white">
 * <TD ALIGN="right" VALIGN="top" WIDTH="1%"><FONT SIZE="-1">
 * <CODE>&nbsp;byte[]</CODE></FONT></TD>
 * <TD><CODE><B><code>getBytes</code></B>()</CODE>
 * 
 * <BR>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Use this method to retrieve the contents of the file as an array of bytes.</TD>
 * </TR>
 * <TR BGCOLOR="white">
 * <TD ALIGN="right" VALIGN="top" WIDTH="1%"><FONT SIZE="-1">
 * <CODE>&nbsp;java.lang.String</CODE></FONT></TD>
 * <TD><CODE><B><code>getContentType</code></B>()</CODE>
 * 
 * <BR>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Get the content-type that the browser communicated with the request
 *  that included the uploaded file.</TD>
 * </TR>
 * <TR BGCOLOR="white">
 * <TD ALIGN="right" VALIGN="top" WIDTH="1%"><FONT SIZE="-1">
 * <CODE>&nbsp;java.io.InputStream</CODE></FONT></TD>
 * <TD><CODE><B><code>getInputStream</code></B>()</CODE>
 * 
 * <BR>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Returns a <CODE>InputStream</CODE> for reading the file.</TD>
 * </TR>
 * <TR BGCOLOR="white">
 * <TD ALIGN="right" VALIGN="top" WIDTH="1%"><FONT SIZE="-1">
 * <CODE>&nbsp;java.lang.String</CODE></FONT></TD>
 * <TD><CODE><B><code>getOriginalName</code></B>()</CODE>
 * 
 * <BR>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Use this method to retrieve the name that the file has on the web 
 *  application user's local system.</TD>
 * </TR>
 * <TR BGCOLOR="white">
 * <TD ALIGN="right" VALIGN="top" WIDTH="1%"><FONT SIZE="-1">
 * <CODE>&nbsp;long</CODE></FONT></TD>
 * <TD><CODE><B><code>getSize</code></B>()</CODE>
 * 
 * <BR>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;The size of the file in bytes</TD>
 * </TR>
 * <TR BGCOLOR="white">
 * <TD ALIGN="right" VALIGN="top" WIDTH="1%"><FONT SIZE="-1">
 * <CODE>&nbsp;void</CODE></FONT></TD>
 * <TD><CODE><B><code>write</code></B>(java.io.File&nbsp;file)</CODE>
 * 
 * <BR>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Write the contents of the uploaded file to a file on the server host.</TD>
 * </TR>
 * </TABLE>
 * &nbsp;
 * <P>
 * 
 *     <h3>Configuring the  <code>ui:upload</code> tag</h3>
 * 
 * 
 * <p>To access the contents of the uploaded file from the
 *     <code>ui:upload</code> tag you have two
 *     options:</p> 
 * <ul> 
 * <li>Bind the <code>uploadedFile</code> attribute to managed bean
 *     property of type <code>com.sun.rave.web.ui.model.UploadedFile</code>. 
 *     Have the setter or an action method process the file.</li> 
 * <li>Specify a valueChangeListener on the Upload component. 
 *     The method invoked by the value change listener has access to the
 *     new value of the component.</code>
 *     method. </li> 
 * </ul> 
 * 
 *     <p>To optionally specify a label for the component, use the
 *     <code>label</code> attribute, or specify a label facet. </p>
 * 
 * <h3>Client Side Javascript Functions</h3>
 * 
 *     <p>In all the functions below, <code>&lt;id&gt;</code> should be
 *     the generated id of the Upload component. 
 * 
 *     <table cellpadding="2" cellspacing="2" border="1" 
 *            style="text-align: left; width: 100%;">
 *     <tbody>
 *     <tr>
 *     <td style="vertical-align">
 *     <code>field_setDisabled(&lt;id&gt;, &lt;disabled&gt;)</code>
 *     </td>
 *     <td style="vertical-align: top">
 *     Enable/disable the field. Set <code>&lt;disabled&gt;</code>
 *     to true to disable the component, or false to enable it.
 *     </td>
 *     </tr>
 *     <tr>
 *     <td style="vertical-align: top">
 *     <code>field_setValue(&lt;id&gt;, &lt;newValue&gt;)</code>
 *     </td>
 *     <td style="vertical-align: top">
 *     Set the value of the field to <code>&lt;newValue&gt;</code>.
 *     </td>
 *     </tr>
 *     <tr>
 *       <td style="vertical-align: top">
 *     <code>field_getValue(&lt;id&gt;)</code>
 *   </td>
 *     <td style="vertical-align: top">Get the value of the field.</td>
 *     </tr>
 *     <tr>
 *       <td style="vertical-align: top">
 *     <code>field_getInputElement(&lt;id&gt;)</code></td>
 *     <td style="vertical-align: top">
 *     Get hold of a reference to the input element rendered by this
 *     component.
 *     </td>
 *     </tr>
 *     <tr>
 *       <td style="vertical-align: top">
 *     <code>component_setVisible(&lt;id&gt;)</code>
 *   </td>
 *       <td style="vertical-align: top">Hide or show this component.
 *       </td>
 *     </tr>
 *   </tbody>
 * </table>
 * 
 * 
 * <h3>Examples</h3>
 * 
 * <h4>Get the contents of the file as a String (using a managed bean)</h4>
 * 
 * <p>On the form that controls the upload:</p> 
 * <pre> 
 * &lt;ui:upload id="upload2"
 *            uploadedFile = "#{FileUploadBean.uploadedFile}"
 *            label="Choose a file: "
 *            required="true"/&gt;
 * </pre> 
 * 
 * <p>On the page that displays the results of the upload:</p> 
 * <pre> 
 * &lt;ui:staticText id="text"
 *                text ="File contents are bound to string: " &gt;
 * &lt;ui:staticText id="text"
 *                text ="#{FileUploadBean.stringContent}"/&gt;
 * 
 * </pre> 
 * <p> The managed bean looks like this:</p> 
 * <pre> 
 * import java.io.Serializable;
 * import com.sun.rave.web.ui.model.UploadedFile;
 * 
 * public class FileUploadBean implements Serializable {
 *      //
 *      // Holds value of property uploadedFile.
 *      //
 *     transient private UploadedFile uploadedFile;
 * 
 *      //
 *      // Getter for property stringContent.
 *      // @return Value of property stringContent.
 *      //
 *     public String getStringContent() {
 *         return uploadedFile.getAsString();
 *     }
 * 
 *      //
 *      // Getter for property uploadedFile.
 *      // @return Value of property uploadedFile.
 *      //
 *     public UploadedFile getUploadedFile() {
 *         return this.uploadedFile;
 *     }
 * 
 *      //
 *      // Setter for property uploadedFile.
 *      // @param uploadedFile New value of property uploadedFile.
 *      //
 *     public void setUploadedFile(UploadedFile uploadedFile) {
 *         this.uploadedFile = uploadedFile;
 *     }
 * }
 * </pre> 
 * 
 * <h4>Write the contents of the file to disk (using a ValueChangeListener)</h4>
 * 
 * <p>On the form that controls the upload:</p> 
 * <pre> 
 * &lt;ui:upload id="upload1"
 *            label="Choose a file: "
 *            valueChangeListener="#{FileUploadedListener.processValueChange}"/&gt;
 * </pre> 
 * 
 * <p>Code for the ValueChangeListener</p> 
 * <pre>
 * import java.io.File; 
 * import java.io.Serializable;
 * import javax.faces.event.AbortProcessingException; 
 * import javax.faces.event.ValueChangeEvent;
 * import com.sun.rave.web.ui.model.UploadedFile;
 * 
 * public class FileUploadedListener implements ValueChangeListener, Serializable {
 *         public void processValueChange(ValueChangeEvent event) 
 *         throws AbortProcessingException {  Object value = event.getNewValue(); 
 *         if(value != null && value instanceof UploadedFile) {
 *             UploadedFile uploadedFile = (UploadedFile)value;
 *             String name = uploadedFile.getOriginalName();
 *             if(name == null || name.length() == 0) {
 *                 name = "tmp.tmp";
 *             }
 *             String suffix = name.substring(name.indexOf("."));
 *             if(suffix.length() == 0) {
 *                 suffix = ".tmp";
 *             }
 *             String prefix = name.substring(0, name.indexOf("."));
 *             try {
 *                 File tmpFile = File.createTempFile(prefix, suffix);
 *                 uploadedFile.write(tmpFile);
 *             } catch(Exception ex) {
 *                 // report the problem
 *             }         
 *         }
 *     }
 * }
 * </pre>
 * <p>Auto-generated component class.
 * Do <strong>NOT</strong> modify; all changes
 * <strong>will</strong> be lost!</p>
 */

public abstract class UploadBase extends com.sun.rave.web.ui.component.Field {

    /**
     * <p>Construct a new <code>UploadBase</code>.</p>
     */
    public UploadBase() {
        super();
        setRendererType("com.sun.rave.web.ui.Upload");
    }

    /**
     * <p>Return the identifier of the component family to which this
     * component belongs.  This identifier, in conjunction with the value
     * of the <code>rendererType</code> property, may be used to select
     * the appropriate {@link Renderer} for this component instance.</p>
     */
    public String getFamily() {
        return "com.sun.rave.web.ui.Upload";
    }

    /**
     * <p>Return the <code>ValueBinding</code> stored for the
     * specified name (if any), respecting any property aliases.</p>
     *
     * @param name Name of value binding to retrieve
     */
    public ValueBinding getValueBinding(String name) {
        if (name.equals("uploadedFile")) {
            return super.getValueBinding("value");
        }
        return super.getValueBinding(name);
    }

    /**
     * <p>Set the <code>ValueBinding</code> stored for the
     * specified name (if any), respecting any property
     * aliases.</p>
     *
     * @param name    Name of value binding to set
     * @param binding ValueBinding to set, or null to remove
     */
    public void setValueBinding(String name,ValueBinding binding) {
        if (name.equals("uploadedFile")) {
            super.setValueBinding("value", binding);
            return;
        }
        super.setValueBinding(name, binding);
    }

    // columns
    private int columns = Integer.MIN_VALUE;
    private boolean columns_set = false;

    /**
 * <p>Number of character columns used to render this field.</p>
     */
    public int getColumns() {
        if (this.columns_set) {
            return this.columns;
        }
        ValueBinding _vb = getValueBinding("columns");
        if (_vb != null) {
            Object _result = _vb.getValue(getFacesContext());
            if (_result == null) {
                return Integer.MIN_VALUE;
            } else {
                return ((Integer) _result).intValue();
            }
        }
        return 40;
    }

    /**
 * <p>Number of character columns used to render this field.</p>
     * @see #getColumns()
     */
    public void setColumns(int columns) {
        this.columns = columns;
        this.columns_set = true;
    }

    // uploadedFile
    /**
 * <p>The value of this attribute must be a JSF EL expression, and
 * 	     	     it must resolve to an object of type
 * <code>com.sun.rave.web.ui.model.UploadedFile</code>. See the JavaDoc for
 * this class for details.</p>
     */
    public com.sun.rave.web.ui.model.UploadedFile getUploadedFile() {
        return (com.sun.rave.web.ui.model.UploadedFile) getValue();
    }

    /**
 * <p>The value of this attribute must be a JSF EL expression, and
 * 	     	     it must resolve to an object of type
 * <code>com.sun.rave.web.ui.model.UploadedFile</code>. See the JavaDoc for
 * this class for details.</p>
     * @see #getUploadedFile()
     */
    public void setUploadedFile(com.sun.rave.web.ui.model.UploadedFile uploadedFile) {
        setValue((Object) uploadedFile);
    }

    /**
     * <p>Restore the state of this component.</p>
     */
    public void restoreState(FacesContext _context,Object _state) {
        Object _values[] = (Object[]) _state;
        super.restoreState(_context, _values[0]);
        this.columns = ((Integer) _values[1]).intValue();
        this.columns_set = ((Boolean) _values[2]).booleanValue();
    }

    /**
     * <p>Save the state of this component.</p>
     */
    public Object saveState(FacesContext _context) {
        Object _values[] = new Object[3];
        _values[0] = super.saveState(_context);
        _values[1] = new Integer(this.columns);
        _values[2] = this.columns_set ? Boolean.TRUE : Boolean.FALSE;
        return _values;
    }

}
