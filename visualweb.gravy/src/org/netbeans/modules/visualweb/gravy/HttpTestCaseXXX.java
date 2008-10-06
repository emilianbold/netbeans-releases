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

package org.netbeans.modules.visualweb.gravy;

////import com.meterware.httpunit.*;
import junit.framework.Test;
////import junit.framework.TestCase;
////import junit.framework.TestSuite;
////import java.io.File;
import org.netbeans.junit.NbTestCase;

/**
 * This class allows to check a real execution of deployed web-application 
 * inside running automated test.
 */
public class HttpTestCaseXXX extends NbTestCase implements Test {
////    private WebConversation conversation;
////    private WebRequest request;
////    private WebResponse response;
////    private long waitTime = 10000;
////    private long waitDelta = 100;
////    TestProperties props;
////    
    /** 
     * Creates a new instance of this class. 
     * @param testName a name of new created test.
     */
    public HttpTestCaseXXX(String testName) {
        super(testName);
////        conversation = new WebConversation();
////        props = new TestProperties();
    }
////    
////    /**
////     * Initializes an instance of class WebConversation, which will
////     * check an execution of web-application.
////     * @return the created instance of WebConversation
////     */
////    public WebConversation initConversation() {
////        getLog().println("Creating WebConversation");
////        conversation = new WebConversation();
////        return(conversation);
////    }
////    
////    /**
////     * Returns an instance of initialized WebConversation.
////     * @return the instance of WebConversation
////     */
////    public WebConversation getConversation() {
////        return(conversation);
////    }
////    
////    /**
////     * Initializes an instance of http-request.
////     * @param path an absolute URL to a deployed web-application
////     * @return the instance of http-request
////     */
////    public WebRequest initRequest(String path) {
////        getLog().println("Creating request for \"" + path + "\"");
////        request = new GetMethodWebRequest(path);
////        return(request);
////    }
////    
////    /**
////     * Returns an instance of initialized http-request.
////     * @return the instance of http-request
////     */
////    public WebRequest getRequest() {
////        return(request);
////    }
////
////    /**
////     * Returns a http-response from web-server.
////     * @return the instance of http-response
////     */
////    public WebResponse getResponse() throws Exception {
////        getLog().println("Getting response for " + request);
////        Exception exc = null;
////        long startTime = System.currentTimeMillis();
////        do {
////            try {
////                response = conversation.getResponse(request);
////            } catch(Exception e) {
////                exc = e;
////                Thread.sleep(waitDelta);
////            }
////        } while(exc != null && (System.currentTimeMillis() - startTime) < waitTime);
////        if(exc != null) {
////            System.out.println("WebRequest.getResponse() did not succed in 1 minute");
////            System.out.println("Ended up with exception:");
////            exc.printStackTrace();
////            throw(exc);
////        } 
////        return(response);
////    }
////    
////    /**
////     * Returns the last received web-response or waits, until the first web-response
////     * is received, and returns it.
////     * @return the instance of WebResponse
////     */
////    public WebResponse lastResponse() throws Exception {
////        if(response == null) {
////            return(getResponse());
////        } else {
////            return(response);
////        }
////    }
////    
////    /**
////     * Returns test properties (TestProperties object).
////     * @return test properties
////     */
////    protected TestProperties getTestProperties() {
////        return(props);
////    }
}
