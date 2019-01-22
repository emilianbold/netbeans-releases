/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.kenai;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * RestResponse
 *
 * 
 */
public class RestResponse {

    private ByteArrayOutputStream os;
    private String contentType = "text/plain";
    private String contentEncoding;
    private int responseCode;
    private String responseMsg;
    private long lastModified;

    public RestResponse() {
        os = new ByteArrayOutputStream();

        String testUrl = System.getProperty("netbeans.t9y.kenai.testUrl");
        if (testUrl != null && testUrl.length() > 0) {
            byte[] buff = testUrl.getBytes();
            int r = 0;
            ByteArrayInputStream bis = new ByteArrayInputStream(buff);
            while ((r = bis.read()) != -1) {
                os.write(r);
            }
        }

    }

    public RestResponse(byte[] bytes) throws IOException {
        this();

        byte[] buffer = new byte[1024];
        int count = 0;
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        while ((count = bis.read(buffer)) != -1) {
            write(buffer, 0, count);
        }
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentEncoding(String contentEncoding) {
        this.contentEncoding = contentEncoding;
    }

    public void setResponseMessage(String msg) {
        this.responseMsg = msg;
    }

    public String getResponseMessage() {
        return responseMsg;
    }

    public void setResponseCode(int code) {
        this.responseCode = code;
    }

    public int getResponseCode() {
        String testUrl = System.getProperty("netbeans.t9y.kenai.testUrl");
        if (testUrl != null && testUrl.length() > 0) {
            return 200;
        }

        return responseCode;
        
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    public long getLastModified() {
        return lastModified;
    }

    public void write(byte[] bytes, int start, int length) {
        os.write(bytes, start, length);
    }

    public byte[] getDataAsByteArray() {
        return os.toByteArray();
    }

    public String getDataAsString() {
        try {
            return os.toString("UTF-8");
        } catch (Exception ex) {
            Logger.getLogger(RestConnection.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    public OutputStream getOutputStream() {
        return os;
    }

//    public <T> T getDataAsObject(Class<T> jaxbClass) throws JAXBException {
//        return getDataAsObject(jaxbClass, jaxbClass.getPackage().getName());
//    }
//
//    public <T> T getDataAsObject(Class<T> clazz, String packageName) throws JAXBException {
//        JAXBContext jc = JAXBContext.newInstance(packageName);
//        Unmarshaller u = jc.createUnmarshaller();
//        Object obj = u.unmarshal(new StreamSource(new StringReader(getDataAsString())));
//
//        if (obj instanceof JAXBElement) {
//            return (T) ((JAXBElement) obj).getValue();
//        } else {
//            return (T) obj;
//        }
//    }
}
