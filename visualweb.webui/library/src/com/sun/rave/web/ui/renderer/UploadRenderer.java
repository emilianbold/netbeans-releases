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
package com.sun.rave.web.ui.renderer;

import java.io.IOException;
import java.util.Map;
import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.context.ResponseWriter;
import javax.faces.context.FacesContext;
import com.sun.rave.web.ui.component.Upload;
import com.sun.rave.web.ui.theme.Theme;
import com.sun.rave.web.ui.util.MessageUtil;
import com.sun.rave.web.ui.util.RenderingUtilities;
import com.sun.rave.web.ui.util.ThemeUtilities;
import javax.faces.application.FacesMessage;

/**
 * <p>Renderer for a {@link Upload} component.</p>
 */

public class UploadRenderer extends FieldRenderer {

    private static final boolean DEBUG = false;

    /**
     * <p>Override the default implementation to conditionally trim the
     * leading and trailing spaces from the submitted value.</p>
     *
     * @param context <code>FacesContext</code> for the current request
     * @param component <code>Upload</code> component being processed
     */
    public void decode(FacesContext context, UIComponent component) {
        
        boolean DEBUG = true;
        if(DEBUG) log("decode()"); 
        Upload upload = (Upload)component; 
        String id = component.getClientId(context).concat(upload.INPUT_ID); 
        if(DEBUG) log("\tLooking for id " + id); 
        Map map = context.getExternalContext().getRequestMap(); 
        if(map.containsKey(id)) { 
           if(DEBUG) log("\tFound id " + id); 
           upload.setSubmittedValue(id);
        }
        /*
        else if(map.containsKey(Upload.ERROR)) { 
            if(DEBUG) log("\tFound error "); 
            upload.setSubmittedValue(Upload.ERROR);
        }
         */
        return;
    }

    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
          
        if(!(component instanceof Upload)) { 
            Object[] params = { component.toString(), 
                                this.getClass().getName(), 
                                Upload.class.getName() };
            String message = MessageUtil.getMessage
                ("com.sun.rave.web.ui.resources.LogMessages", //NOI18N
                 "Upload.renderer", params);              //NOI18N
            throw new FacesException(message);  
        }
        
        Theme theme = ThemeUtilities.getTheme(context);
        Map map = context.getExternalContext().getRequestMap(); 
        Object error =  map.get(Upload.UPLOAD_ERROR_KEY);
        if (error != null) {
            if (error instanceof Throwable) {
                if( error instanceof org.apache.commons.fileupload.FileUploadBase.SizeLimitExceededException ) {
                    // Caused by the file size is too big
                    String maxSize = (String)map.get(Upload.FILE_SIZE_KEY);
                    String [] detailArgs = {maxSize};
                    String summaryMsg = theme.getMessage("FileUpload.noFile");
                    String detailMsg = theme.getMessage("Upload.error", detailArgs);
                    FacesMessage fmsg = new FacesMessage(summaryMsg, detailMsg);
                    context.addMessage(((Upload)component).getClientId(context), fmsg);
                } else {   
                    String summaryMsg = theme.getMessage("FileUpload.noFile");
                    FacesException fe = new FacesException(summaryMsg);
                    fe.initCause((Throwable)error);
                    throw fe;
                }
            }
        }

        boolean spanRendered = 
	    super.renderField(context, (Upload)component, 
			      "file", getStyles(context));
        
        StringBuffer jsString = new StringBuffer(200);
	String id = component.getClientId(context); 
        jsString.append("upload_setEncodingType(\'"); //NOI18N
        jsString.append(id);
        jsString.append("\');"); //NOI18N

        ResponseWriter writer = context.getResponseWriter(); 
        writer.writeText("\n", null); //NOI18N
        writer.startElement("script", component); //NOI18N
        writer.writeText(jsString.toString(), null); 
        writer.endElement("script"); //NOI18N
	if(!spanRendered) { 
	    String param = id.concat(Upload.INPUT_PARAM_ID); 
	    RenderingUtilities.renderHiddenField(component, writer, param, id); 
	} 
     }
}
