/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 */
# import org.netbeans.mobility.end2end.core.model.*;
# import org.netbeans.mobility.end2end.core.model.classdata.*;
# import java.util.*;
#
# final String VERSION_STRING = (String) data.get("VERSION");
# ProtocolSupport support = new ProtocolSupport(data, this, false);
# setOut(support.getServerPath(data.getServerClassName()));
# getOutput().setServlet(true);
# getOutput().addCreatedFile( support.getServerPath(data.getServerClassName()));
${support.serverPackageLine()}
import ${support.serverSupportPackage()}.EndToEndGateways;
import ${support.serverSupportPackage()}.InvocationAbstraction;
import ${support.serverSupportPackage()}.Utility;
import javax.servlet.http.*;
import javax.servlet.*;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * An automatically-generated servlet gateway. This servlet provides
 * J2ME access to the following server-side methods and classes:
 *
 * <ul>
# MethodData[] methods = data.getMethods();
# for (int i = 0; i < methods.length; i++) {
 *  <li> ${methods[i].getClassData().getClassName()}.${methods[i].getName()}
# }
 * </ul>
 */
public class ${data.getServerClassName()} extends HttpServlet {
    
    /**
     * Tracing flag
     */
    private static int traceOutput = ${data.getServerTraceLevel()};

    /**
     *  This constant indicates the command code for an invocation in the standard
     *  protocol.
     */
    private final static short INVOCATION_CODE = 1;

    /**
     *  This member indicates a successful result
     */
    private final static short RESULT_SUCCESSFUL = 1;

    /**
     *  This member indicates a server side exception
     */
    private final static short RESULT_EXCEPTION = 2;

    /**
     * The version string for the protocol. This must match the client's
     * version
     */
    private final static String PROTOCOL_VERSION = "${VERSION_STRING}";
    
    private boolean traceProtocol = true;
    
    /**
     *  This member contains the method abstractions that can be invoked by the
     *  servlet.
     */
    private final static InvocationAbstraction[] METHODS = new InvocationAbstraction[] {
        // the naming convention for method delegates is the method name followed
        // by the word Gateway
#   ArrayList methodList = new ArrayList();
#   Object EMPTY_VALUE = "";
#   for (int i = 0; i < methods.length; i++) {
#     int id = methods[i].getRequestID();
#     while (methodList.size() <= id) {
#       methodList.add(EMPTY_VALUE);
#     }
#     methodList.set(id, methods[i]);
#   } // end iterating over methods[]
#
#   // populate METHODS array
#   for (Iterator i = methodList.iterator(); i.hasNext();) {
#     Object next = i.next();
#     if (next == EMPTY_VALUE) {
    null,
#     } else {
#       MethodData method = (MethodData) next;
    new EndToEndGateways.${method.getImplementingClassName()}(),
#     }
#   } // finish populating METHODS array
  };

    /**
     * Called to handle a GET request. Returns a message that the servlet
     * is deployed
     */
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
                     throws ServletException, IOException {
      resp.setContentType("text/html; charset=iso-8859-1");
      String title = "${data.getServerClassName()} servlet status";
      String message = "${data.getServerClassName()} servlet active";
      OutputStream os = resp.getOutputStream();
      String response = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\"\n" +
                        "\"http://www.w3.org/TR/REC-html40/loose.dtd\">\n" +
                        "<html>\n<head>\n" +
                        "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\">" +
                        "<title>" + title + "</title>\n" +
                        "</head>\n<body>\n" +
                        "<center><h1>" + message + "</h1></center>\n" +
                        "</body>\n</html>\n";
      os.write(response.getBytes("ISO-8859-1"));
      os.close();
    }
    
    /**
     *  Called by the server (via the service method) to allow a servlet to handle
     *  a POST request. This method dispatches the calls to the underlying class.
     *
     *@param  req                   an HttpServletRequest object that contains the
     *      request the client has made of the servlet
     *@param  resp                  an HttpServletResponse object that contains the
     *      response the servlet sends to the client
     *@exception  ServletException  - if the request for the POST could not be
     *      handled
     *@exception  IOException       Description of Exception
     */
    protected void doPost( HttpServletRequest req, HttpServletResponse resp )
                     throws ServletException, IOException {
        resp.setContentType("application/octet-stream");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream outputStream = new DataOutputStream(baos);
        DataOutput out = getDataOutput(outputStream);
        try {
            short resultCode = RESULT_SUCCESSFUL;
            DataInputStream in = new DataInputStream(req.getInputStream());

            HttpSession session = req.getSession(true);
            
            String versionString = req.getHeader("version");
            if (versionString != null) {
                if (!versionString.equals(PROTOCOL_VERSION)) {
                    throw new IOException("Incompatible protocol version: "
                        + "Client's version '" + versionString + "' "
                        + "does not match server's version '"
                        + PROTOCOL_VERSION + "'");
                }
                if (traceProtocol) {
                    System.out.println("Protocol versions match: " + PROTOCOL_VERSION);
                }
            }

            short commandCode = in.readShort();

            if ( commandCode == INVOCATION_CODE ) {
                // read the number of invocations that are grouped within this call
                short groupCount = in.readShort();
                Object[] returnValues = new Object[groupCount];
                for ( short iter = 0; iter < groupCount; iter++ ) {
                    int requestID = in.readInt();
                    if (traceProtocol) {
                        System.out.println("Calling method " + requestID
                            + " on server ("
                            + METHODS[requestID].getClass().getName()
                            + ")");
                    }
                    returnValues[iter] = invokeMethod(session, requestID, in);
                }
                out.writeShort(resultCode);
                Utility.writeResults(out, returnValues);
            }

            in.close();
        } catch ( Exception err ) {
            if (outputStream == null) {
                outputStream = new DataOutputStream(resp.getOutputStream());
                out = getDataOutput(outputStream);
            }

            out.writeShort(RESULT_EXCEPTION);
            err.printStackTrace();
            String errMessage = err.getMessage();
            if (errMessage == null) {
                errMessage = "";
            }
            if (err instanceof IOException) {
                Utility.writeObject(out, err.getMessage());
            } else {
                if (errMessage.length() > 0) {
                    errMessage = ": " + errMessage;
                }
                 Utility.writeObject(out, err.getClass().getName() + err.getMessage());
            }
        }
        outputStream.close();
        byte[] outputData = baos.toByteArray();
        resp.setContentLength(outputData.length);
        OutputStream httpOutputStream = resp.getOutputStream();
        try {
            httpOutputStream.write(outputData);
        } finally {
            httpOutputStream.close();
        }
    }

    protected DataOutput getDataOutput(DataOutput out) {
        if (traceOutput > 0) {
            return new Utility.CompositeDataOutput(new DataOutput[] {
                new Utility.TracedDataOutput(System.out),
                out
            });
        } else {
            return out;
        }
    }
    
    /**
     *  This method performs the actual invocation of server functionality.
     *
     *@param  session          The http session
     *@param  requestID        The id of a specific method invocation
     *@param  input            The stream from which we should read the parameters
     *      for the methods
     *@return                  Description of the Returned Value
     *@exception  IOException  Thrown when a protocol/response error occurs
     */
    private Object invokeMethod(HttpSession session,
        int requestID,
        DataInput input) throws Exception {

        return METHODS[requestID].invoke(session, input);
    }
}
