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
package com.sun.rave.web.ui.component;

import java.io.IOException;
import java.io.Serializable;

import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import org.apache.commons.fileupload.FileItem;

import com.sun.rave.web.ui.model.UploadedFile;
import com.sun.rave.web.ui.util.ComponentUtilities;
import com.sun.rave.web.ui.util.ThemeUtilities;


/**
 * Use this component to allow web application users to upload a file.
 * If a web application uses this component, it must be configured to use 
 * {@link com.sun.rave.web.ui.util.UploadFilter }
 * @author avk
 */
public class Upload extends UploadBase implements Serializable {


    /**
     * A string concatenated with the component ID to form the ID and
     * name of the HTML input element. 
     */
    public static final String INPUT_ID = "_com.sun.rave.web.ui.upload"; //NOI18N
    public static final String INPUT_PARAM_ID = "_com.sun.rave.web.ui.uploadParam"; //NOI18N
    public static final String SCRIPT_ID="_script"; 
    public static final String SCRIPT_FACET="script"; 
    public static final String TEXT_ID="_text"; 
    public static final String UPLOAD_ERROR_KEY="upload_error_key";
    public static final String FILE_SIZE_KEY="file_size_key";
    
    private static final boolean DEBUG = false;
    
    /** Creates a new instance of Upload */
    public Upload() {
    }

    /**
     * Log an error - only used during development time.
     */
    protected void log(String s) {
        System.out.println(this.getClass().getName() + "::" + s); //NOI18N
    }
    
    /**
     * <p>Converts the submitted value. Returns an object of type 
     *  UploadedFile.</p>
     * @param context The FacesContext
     * @param value An object representing the submitted value
     * @return An Object representation of the value (a
     * java.lang.String or a java.io.File, depending on how the
     * component is configured 
     */
    public Object getConvertedValue(FacesContext context, Object value) { 

        if(DEBUG) log("getConvertedValue"); 
        UploadedFileImpl uf = new UploadedFileImpl();   
        uf.setAttribute(value);
        if(DEBUG) { 
            log("\tSize is " + String.valueOf(uf.getSize())); 
            log("\tName is " + uf.getOriginalName()); 
            log("\tValue is required " + String.valueOf(isRequired())); 
        }
        if(isRequired() && uf.getSize() == 0) { 
            String name = uf.getOriginalName(); 
            if(name == null || name.trim().length() == 0) { 
                if(DEBUG) log("No file specified");
                setValue("");
                if(DEBUG) log("Set value to empty string");
                return "";
                //FacesMessage msg = new FacesMessage("Enter a file to upload");
                //throw new ConverterException(msg);
            }          
        }      
        return uf;   
    } 

     /**
     * <p>Return the value to be rendered, as a String (converted
     * if necessary), or <code>null</code> if the value is null.</p>
     * @param context FacesContext for the current request
     * @return A String value of the component
     */
    public String getValueAsString(FacesContext context) { 
        String valueString = null;
        Object value = getValue(); 
        if(value instanceof UploadedFile) {
            valueString = ((UploadedFile)value).getOriginalName();
        }
        if(valueString == null) {
            valueString = ThemeUtilities.getTheme(context)
                .getMessage("FileUpload.noFile");
        }
        return valueString;
    } 
    
    /**
     * Overrides getType in the FileInput class, to always return
     * "file" 
     * @return "file"
     */
    public String getType() {
        return "file";
    }

    /**
     * This method overrides getText() in Field. It always returns null. 
     */
    public Object getText() {
        return null;
    }
    
    /**
     * This method overrides setText() in Field. It is a noop.
     */
    public void setText(Object text) {
        // do nothing
    }
    
    public int getColumns() {

        int columns = super.getColumns();
        if(columns < 1) { 
            columns = 40; 
            super.setColumns(40); 
        }
        return columns;
    }
    
    // <RAVE>
    // Merged INF http://inf.central/inf/integrationReport.jsp?id=82661 from braveheart.
    // It fixes bug 6349156: File upload label's "for" attribute value is incorrect
    
    /** 
     * Retrieves the DOM ID for the HTML input element. To be used by 
     * Label component as a value for the "for" attribute. 
     */
    // Overrides the method in Field.java as a workaround for an 
    // apparent compiler problem (?). The renderer (for Upload as well 
    // as TextField etc) casts the component to Field. It invokes 
    // Field.getPrimaryElementID, and even though this.getClass()
    // returns Upload, this.INPUT_ID returns Field.INPUT_ID and 
    // not Upload.INPUT_ID. 
    public String getPrimaryElementID(FacesContext context) {
     
        // Check for a public facet
	String clntId = this.getClientId(context);
	UIComponent facet = getFacet(LABEL_FACET);
	if (facet != null) {
	    return clntId.concat(this.INPUT_ID);
	}
	// Need to check for the private facet as well. Note that 
        // this is not ideal - unless getLabelComponent has been invoked
        // first, the private facet will be null. (Is there a reason 
        // we can't just invoke getLabelComponent instead of getFacet?). 
        
	// Pass "false" since we don't want to get null if the id's 
	// don't match. We don't care at this point, in fact they
	// should match because getLabelComponent must have been
	// called in order for this to even work.
	//
	facet = ComponentUtilities.getPrivateFacet(this, LABEL_FACET, false);
	if (facet == null) {
	    return getClientId(context); 
	}
	return this.getClientId(context).concat(this.INPUT_ID);
    }
    // <RAVE>
     
    class UploadedFileImpl  implements UploadedFile {
        
        transient Object attribute = null;
        
        /** Creates a new instance of UploadedFileImpl */
        UploadedFileImpl() {
        }
        
        void setAttribute(Object attribute) {
            this.attribute = attribute;
        }
        
        private FileItem getFileItemObject() {
            
            FacesContext context = FacesContext.getCurrentInstance();
            Object fileItemObject =
                    context.getExternalContext().getRequestMap().get(attribute);
            
            // <RAVE>
            if( fileItemObject == null )
                return null;
            // <RAVE>
            
            if(!(fileItemObject instanceof FileItem)) {
                String message = "Did you install the upload filter?";
                throw new FacesException(message);
            }
            return (FileItem)fileItemObject;
        }
        
        /**
         * Write the contents of the uploaded file to a file on the
         * server host. Note that writing files outside of the web
         * server's tmp directory must be explicitly permitted through
         * configuration of the server's security policy.
         *
         * This method is not guaranteed to succeed if called more
         * than once for the same item.
         * @param file The <code>File</code> where the contents should
         * be written
         *
         * @exception Exception the
         */
        public void write(java.io.File file) throws Exception {
            // <RAVE>
            //getFileItemObject().write(file);
            if( getFileItemObject() != null )
                getFileItemObject().write(file);
            // <RAVE>
        }
        
        /**
         * The size of the file in bytes
         *
         * @return The size of the file in bytes.
         */
        public long getSize() {
            // <RAVE>
            //return getFileItemObject().getSize();return getFileItemObject().getSize();
            if( getFileItemObject() != null )
                return getFileItemObject().getSize();
            else
                return 0;
            // <RAVE>
        }
        
        /**
         * Use this method to retrieve the name that the file has on the web
         * application user's local system.
         *
         * @return the name of the file on the web app user's system
         */
        public String getOriginalName() {
            // <RAVE>
            //return getFileItemObject().getName();
            if( getFileItemObject() != null )
                return getFileItemObject().getName();
            else
                return null;
            // <RAVE>
        }
        
        /**
         * Returns a {@link java.io.InputStream InputStream} for
         * reading the file.
         *
         * @return An {@link java.io.InputStream InputStream} for
         * reading the file.
         *
         * @exception IOException if there is a problem while reading
         * the file
         */
        public java.io.InputStream getInputStream() throws java.io.IOException {
            // <RAVE>
            //return getFileItemObject().getInputStream();
            if( getFileItemObject() != null )
                return getFileItemObject().getInputStream();
            else
                return null;
            // <RAVE>
        }
        
        /**
         * Get the content-type that the browser communicated with the
         * request that included the uploaded file. If the browser did
         * not specify a content-type, this method returns null.
         *
         * @return  the content-type that the browser communicated
         * with the request that included the uploaded file
         */
        public String getContentType() {
            // <RAVE>
            //return getFileItemObject().getContentType();
            if( getFileItemObject() != null )
                return getFileItemObject().getContentType();
            else
                return null;
            // <RAVE>
        }
        
        /**
         * Use this method to retrieve the contents of the file as an
         * array of bytes.
         * @return The contents of the file as a byte array
         */
        public byte[] getBytes() {
            // <RAVE>
            //return getFileItemObject().get();
            if( getFileItemObject() != null )
                return getFileItemObject().get();
            else
                return null;
            // <RAVE>
        }
        
        /**
         * Use this method to retrieve the contents of the file as a
         * String
         *
         * @return the contents of the file as a String
         */
        public String getAsString() {
            // <RAVE>
            //return getFileItemObject().getString();
            if( getFileItemObject() != null )
                return getFileItemObject().getString();
            else
                return null;
            // <RAVE>
        }
        
        /**
         * Dispose of the resources associated with the file upload
         * (this will happen automatically when the resource is
         * garbage collected).
         */
        public void dispose() {
            // <RAVE>
            //getFileItemObject().delete();
            if( getFileItemObject() != null )
                getFileItemObject().delete();
            // <RAVE>
        }
    }
}
