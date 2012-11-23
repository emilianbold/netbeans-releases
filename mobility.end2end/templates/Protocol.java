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
# import org.netbeans.mobility.end2end.core.model.protocol.Serializer;
# import org.netbeans.mobility.end2end.core.model.protocol.binary.ComplexTypeSerializer;
# import org.netbeans.mobility.end2end.core.model.protocol.binary.PrimitiveTypeSerializer;
# import java.io.*;
# import java.util.*;
# import java.text.*;
#
# final ProtocolSupport support = new ProtocolSupport(data, this, true);
# final String EMPTY_ARRAY_FIELD = "_";
# final String INVOCATION_CODE = "1";
# final String RESULT_SUCCESSFUL = "1";
# final String RESULT_EXCEPTION = "2";
# final ClassData VOID_TYPE = data.getRegistry().getClassData(Void.TYPE);
# // Set up a protocol version string
# DateFormat dateFormat = new SimpleDateFormat("d-MMM-yyyy/HH:mm:ss-z", Locale.US);
# final String VERSION_STRING = dateFormat.format(new Date());
# data.set("VERSION", VERSION_STRING);
#
# setOut(support.getClientPath(data.getClientClassName()));
# getOutput().addCreatedFile(support.getClientPath(data.getClientClassName()));
#
# // The output file should be recognized by the development
# // environment as a J2ME file
# getOutput().setMIDlet(true);
#
# // Set the output package
${support.clientPackageLine()}
#
# // Imports
import java.util.*;
import java.io.*;
import javax.microedition.io.*;
#
# // A MIDlet needs extra imports
# if (data.isMIDlet()) {
import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;

/**
 * This class provides J2ME client access to J2EE services. It extends MIDlet,
 * and should be modified in order to actually use the methods that it defines.
 * Note that any changes to this class will be erased if this class is
 * regenerated. For this reason it is recommended that for general use you
 * generate a stand-alone class instead of a MIDlet.
 *
 * The methods in this class are not thread-safe.
 */
# } else {

/**
 * This class provides J2ME client access to J2EE services. It is created
 * by a code-generator; you should note that any changes you make will
 * be lost if you re-generate the code.
 *
 * The methods in this class are not thread-safe.
 */
# }

# // Determine the modifier used for methods such as "invokeServer"
# String dynamicMethodModifier = data.isDynamicInvocationSupported()
#   ? "public"
#   : "private";
#
#if(data.isMIDlet()) {
public class ${data.getClientClassName()} extends MIDlet {
#} else {
public class ${data.getClientClassName()} {
#}

# final ClassData[] supportedReturnTypes = data.getReturnTypes();
# final ClassData[] supportedParamTypes = data.getParameterTypes();
# final int ARRAY_TYPE = -2;
# for (int i = 0 ; i < supportedReturnTypes.length; i++) {
#     ClassData classData = supportedReturnTypes[i];
#     while (classData.isArray()) {
#       classData = classData.getComponentType();
#     }
#     classData.getSerializer().setType(String.valueOf(data.getValueForType(classData)));
# }
# for (int i = 0 ; i < supportedParamTypes.length; i++) {
#     ClassData classData = supportedParamTypes[i];
#     while (classData.isArray()) {
#       classData = classData.getComponentType();
#     }
#     classData.getSerializer().setType(String.valueOf(data.getValueForType(classData)));
# }
#
# // generate method ID constants, if they are supported
# if(data.isDynamicInvocationSupported()) {
#   MethodData[] methods = data.getMethods();
#   String[] constantNames = support.computeConstantNames(methods);
#   for(int i = 0 ; i < methods.length ; i++) {
        public static final int ${constantNames[i]} = ${methods[i].getRequestID()};
#   }
# } // end if isDynamicInvocationSupported()
#
    /** The URL of the servlet gateway */
    private String serverURL;

    /** The session cookie of this client */
    private String sessionCookie;
    
# // We don't need the following unless we actually make no-argument
# // calls.
# if (support.noArgumentCallExists(data.getMethods()) || data.isReturnTypeSupported(Void.TYPE)) {
    /**
     * Empty array used for no-argument calls, and to represent the value "void"
     */
    
    private final static Object[] ${EMPTY_ARRAY_FIELD} = new Object[0];

#}
#
#if(data.isGroupingSupported()){
    /**
     * This vector contains the request ids for queued calls.
     */
    private Vector groupRequestID;

    /**
     * This vector contains the parameters for the queued calls.
     */
    private Vector groupParameters;

#} // end of grouping constants

#if(data.isMIDlet()) {

    public void startApp() {
        serverURL = "${data.getServletURL()}";
# support.traceLight("Initializing MIDlet, serverURL = {0}", "serverURL");
    }

    public void destroyApp(boolean b) {
    }

    public void pauseApp() {
    }
#} else {
    
    /**
     * Constructs a new ${data.getClientClassName()}
     * and initializes the URL to the servlet gateway from a hard-coded value.
     */
    public ${data.getClientClassName()}() {
        this("${data.getServletURL()}");
    }
    
    /**
     * Constructs a new ${data.getClientClassName()}
     * and initializes the URL to the servlet gateway from the given value
     *
     * @param serverURL URL of the deployed servlet
     */
    public ${data.getClientClassName()}(String serverURL) {
        this.serverURL = serverURL;
# support.traceLight("Creating " + data.getClientClassName() + ", serverURL = {0}", "serverURL");       
    }
#}
#
#if (data.isStubGeneration()) { // generate stub methods
#  MethodData[] methods = data.getMethods();
# // compute parameter line string (e.g "(int a0, char a1, String[] a2)")
# // at the same time, compute the object array contents
# // (e.g. { new Integer(a0), new Character(a1), s2 }
#  for (int i = 0; i < methods.length; i++) {
#    MethodData method = methods[i];
#    ClassData returnType = method.getReturnType();
#    ClassData[] paramTypes = method.getParameterTypes();
#    String[] names = method.getParameterNames();
#    String methodName = method.getStubMethodName();
#    StringBuffer parameterLine = new StringBuffer();
#    for (int j = 0; j < paramTypes.length; j++) {
#      if (j > 0) {
#        parameterLine.append(", ");
#      }
#      parameterLine.append(paramTypes[j].getShortClassName() + " " + names[j]);
#    }
#    String[] argArrayElements = new String[paramTypes.length];
#    for (int j = 0; j < paramTypes.length; j++) {
#      ClassData type = paramTypes[j];
#      String comma = (j == paramTypes.length - 1) ? "" : ",";
#      argArrayElements[j] = support.castToReference(names[j], type) + comma;
#    }
#    String methodString;
#    if (data.isDynamicInvocationSupported()) {
#      methodString = method.getMethodConstantName();
#    } else {
#      methodString = String.valueOf(method.getRequestID());
#    }
#    if (data.isSynchronousSupported()) {
public ${returnType.getShortClassName()} ${methodName}(${parameterLine}) throws IOException {
#    String args = "args";
#    if (argArrayElements.length > 0) {
    Object[] args = new Object[] {
#      for (int j = 0; j < argArrayElements.length; j++) {
${argArrayElements[j]}
#      }
    };
#    } else {
#      args = EMPTY_ARRAY_FIELD;
#    }
#    support.traceMedium("Calling server method " + method.getClassData().getCanonicalName() + "." + method.getName() + "()");
#    for (int k = 0; k < argArrayElements.length; k++) {
#      support.traceHeavy("args[" + k + "] = {0}", "args[" + k + "]");
#    }
#    String callString = "invokeServer(" + methodString + ", " + args + ")";
# if (returnType.equalsClass(Void.TYPE)) {
    ${callString};
# } else {
    return ${support.castFromReference(callString, returnType)};
# }
# // close the stub method
}

#    } // end synchronous stub
#    if (data.isGroupingSupported()) {
public void ${methodName}Grouped(${parameterLine}) throws IOException {
#    String args = "args";
#    if (argArrayElements.length > 0) {
    Object[] args = new Object[] {
#      for (int j = 0; j < argArrayElements.length; j++) {
${argArrayElements[j]}
#      }
    };
#    } else {
#      args = EMPTY_ARRAY_FIELD;
#    }
#    support.traceMedium("Queuing call to server method " + method.getClassData().getCanonicalName() + "." + method.getName() + "()");
#    for (int k = 0; k < argArrayElements.length; k++) {
#      support.traceHeavy("args[" + k + "] = {0}", "args[" + k + "]");
#    }
    invokeGrouped(${methodString}, ${args});
}

#    } // end grouped stub
#  } // end loop over methods
#} // finish generating stubs
#
# TemplateMacro openInputStream = new TemplateMacro(this) {
#   public void define() {
        int response;
        try {
            response = connection.getResponseCode();
        } catch (IOException e) {
            throw new IOException("No response from " + serverURL);
        }
        if (response != 200) {
            throw new IOException(response + " " + connection.getResponseMessage());
        }
        DataInputStream input = connection.openDataInputStream();
        String sc = connection.getHeaderField("set-cookie");
        if (sc != null) {
            sessionCookie = sc;
        }
        short errorCode = input.readShort();
        if (errorCode != ${RESULT_SUCCESSFUL}) {
            // there was a remote exception
            throw new IOException((String) readObject(input));
        }
#  }
# };
#
# // if both grouping and single calls are used, it is best to put the contents
# // of openInputStream into a separate method
# if (data.isGroupingSupported() && data.isSynchronousSupported()) {
private DataInputStream openInput(HttpConnection connection) throws IOException {
#   openInputStream.write();
    return input;
}
# // redefine openInputStream to call the method we just created.
#   openInputStream = new TemplateMacro(this) { public void define() {
DataInputStream input = openInput(connection);
#   }};
# }
#if(data.isGroupingSupported()){
    /**
     *  This method performes caches the parameters and invokes the method only
     *  upon the getGroupedResults() method. This allows several calls to
     *  services on the server to be made in a single HTTP request/response.
     *
     *@param  requestID   The id of the server service (method) we wish to invoke.
     *@param  parameters  The parameters that should be passed to the server (type
     *      safety is not checked by this method!)
     *@param  returnType  Description of Parameter
     */
${dynamicMethodModifier} void invokeGrouped(int requestID, Object[] parameters) {
        if ( groupParameters == null ) {
            groupParameters = new Vector();
            groupRequestID = new Vector();
        }

        if ( groupParameters.size() == Short.MAX_VALUE ) {
            throw new ArrayIndexOutOfBoundsException("Exceeded grouping limit");
        }

        groupParameters.addElement(parameters);
        groupRequestID.addElement(new Integer(requestID));
    }

    /**
     *  Sends queued methods calls to the server in a single connection.
     *
     *@return                  An enumeration of the results returned from the
     *      server VOID methods are skipped automatically.
     *@exception  IOException  When a communication error or a remote exception
     * occurs
     */
    public Enumeration getGroupedResults() throws IOException {
        HttpConnection connection = (HttpConnection) Connector.open( serverURL );
        connection.setRequestMethod("POST");

        if (sessionCookie == null) {
            // if this is the first time this client contatcs the server,
            // verify that the version matches
            connection.setRequestProperty("version", "${VERSION_STRING}");
        } else {
            connection.setRequestProperty("cookie", sessionCookie);
        }
        
        DataOutputStream output = connection.openDataOutputStream();
        
        // write the client ID and invocation code
        writeObject(output, this);

        Vector results = new Vector();
        int calls = groupParameters.size();
        Enumeration parameterEnum = groupParameters.elements();
        Enumeration requestEnum = groupRequestID.elements();

        // write the size of the group
# support.traceMedium("Executing \" + groupParameters.size() + \" queued calls");
        output.writeShort(( short ) groupParameters.size());

        for (int i = 0; i < calls; i++) {
            Object[] parameters = (Object[]) groupParameters.elementAt(i);
            int requestID = ((Integer) groupRequestID.elementAt(i)).intValue();
            output.writeInt(requestID);
            for (int j = 0; j < parameters.length; j++ ) {
# support.traceHeavy("Invoking server with requestID {0}", "requestID");
                writeObject(output, parameters[j]);
            }
        }
        
        output.close();
        
        groupParameters.removeAllElements();
        groupRequestID.removeAllElements();

# openInputStream.write();

        for (int i = 0; i < calls; i++) {
# support.traceHeavy("Reading return value");
            Object returnValue = readObject(input);
# support.traceMedium("Returned value: {0}", "returnValue");
# if (data.isReturnTypeSupported(VOID_TYPE)) {
            if (returnValue != _ /* void return type */) {
                results.addElement(returnValue);
            }
#} else {
            results.addElement(returnValue);
#}
        }

        input.close();
        connection.close();
        return results.elements();
    }
#}
#
#if (data.isSynchronousSupported()){

    /**
     *  This method performes a dynamic invocation on the server. It is generic in
     *  order to reduce the code size.
     *
     *@param  requestID        The id of the server service (method) we wish to
     *      invoke.
     *@param  parameters       The parameters that should be passed to the server
     *      (type safety is not checked by this method!)
     *@param  returnType       Is used to indicate the return type we should read
     *      from the server
     *@return                  The return value from the invoked service
     *@exception  IOException  When a communication error or a remove exception
     * occurs
     */
    ${dynamicMethodModifier} Object invokeServer(int requestID, Object[] parameters) throws IOException {
        HttpConnection connection = (HttpConnection) Connector.open( serverURL );
        connection.setRequestMethod(HttpConnection.POST);
        connection.setRequestProperty("Content-Type", "application/octet-stream");
        connection.setRequestProperty("Accept", "application/octet-stream");

        if (sessionCookie == null) {
            // if this is the first time this client contatcs the server,
            // verify that the version matches
            connection.setRequestProperty("version", "${VERSION_STRING}");
        } else {
            connection.setRequestProperty("cookie", sessionCookie);
        }
        
        DataOutputStream output = connection.openDataOutputStream();
        
# support.traceHeavy("Invoking server with requestID {0}", "requestID");
        writeObject(output, this);

        /* Write the byte signifying that only one call
         * is being made.
         */
        output.writeShort(1 /* one call to be made to the server */);

        output.writeInt(requestID);
        for (int i = 0; i < parameters.length; i++ ) {
            writeObject(output, parameters[i]);
        }

        output.close();
        
        ${openInputStream.get()}
# support.traceHeavy("Reading return value");
        Object returnValue = readObject(input);
# support.traceMedium("Returned value: {0}", "returnValue");

        input.close();
        connection.close();
        return returnValue;
    }
#}

    private void writeObject(DataOutputStream out, Object o) throws IOException {
# String _output = "out";
# String varName = "o";
        if (o == this) {
            out.writeShort(${INVOCATION_CODE} /* invocation code */);
        } else if (o == null) {
            out.writeShort(-1);
# if (data.isArrayParameterSupported()) {
        } else if (o.getClass().isArray()) {
            out.writeShort(${ARRAY_TYPE} /* ARRAY TYPE */);
#   Set primitiveArrayParamTypes = new HashSet();
#   Set objectArrayParamTypes = new HashSet();
#   for (int i = 0; i < supportedParamTypes.length; i++) {
#     ClassData type = supportedParamTypes[i];
#     if (type.isArray()) {
#       type = type.getComponentType();
#       if (type.isPrimitive()) {
#         primitiveArrayParamTypes.add(type);
#       } else {
#         if( type.getSerializer() instanceof PrimitiveTypeSerializer ) {
#           continue;   
#         }
#         objectArrayParamTypes.add(type);
#       }
#     }
#   }
#   String elseClause = "";
#   for (Iterator i = primitiveArrayParamTypes.iterator(); i.hasNext(); ) {
#     ClassData type = (ClassData) i.next();
#     Serializer s = type.getSerializer();
                ${elseClause}if (o instanceof ${type.getShortClassName()}[]) {
                    ${type.getShortClassName()}[] array = (${type.getShortClassName()}[]) o;
                    ${s.writeType("out")}
                    out.writeInt(array.length);
                    for (int i = 0; i < array.length; i++) {
                        ${s.writeObject("out", "array[i]")}
                    }
                }
#     elseClause = "else ";
#   } // end for loop
#   if (!objectArrayParamTypes.isEmpty()) {
#     if (elseClause.length() > 0) {
                else {
#     }
                Object[] array = (Object[]) o;
#   if (objectArrayParamTypes.size() == 1) {
#     ClassData type = (ClassData) objectArrayParamTypes.iterator().next();
                ${type.getSerializer().writeType("out")}
#   } else {
#     Iterator i = objectArrayParamTypes.iterator();
#     ClassData type = (ClassData) i.next();
                if (o instanceof ${type.getShortClassName()}[]) {
                    ${type.getSerializer().writeType("out")}
#     while (i.hasNext()) {
#       type = (ClassData) i.next();
                } else if ( o instanceof ${type.getShortClassName()}[]) {
                    ${type.getSerializer().writeType("out")}
#     } // end while
                }
#   } // end if size == 1
                out.writeInt(array.length);
                for (int i = 0; i < array.length; i++) {
                    writeObject(out, array[i]);
                }
#     if (elseClause.length() > 0) {
                }
#     }
#   } // end if support object array
# }
# Set writableTypes = new HashSet();
# for( int i = 0; i < supportedParamTypes.length; i++ ) {
#   ClassData type = supportedParamTypes[i];
#   if( !type.isPrimitive() && type.getSerializer() instanceof PrimitiveTypeSerializer ) {
#     continue;   
#   }
#   writableTypes.add( type ); 
# }
# for( Iterator it = writableTypes.iterator(); it.hasNext(); ) {
#   ClassData classData = (ClassData)it.next();
#   if (classData.equals(VOID_TYPE)) {
#     continue;
#   }
#   if (!classData.isArray()) {
#       String className;
#       if (classData.isPrimitive()) {
#           className = classData.getWrapperType().getShortClassName();
#       } else {
#           className = classData.getShortClassName();
#       }
#     Serializer s = classData.getSerializer();
#     if( s instanceof ComplexTypeSerializer ) {
#         ((ComplexTypeSerializer)s).setClient( support.isClient());
#     }
        } else if (o instanceof ${className}) {
            ${s.writeType(_output)}
            ${s.writeObject(_output, support.castFromReference(varName, classData))}
#   }
# }
        } else {
            // default if a data type is not supported
            throw new IllegalArgumentException(
                "Unsupported parameter type: " + o.getClass());
        }
    }

    private static Object readObject(DataInput in) throws IOException {
#   String _input = "in";
# // Read primitive types
        int type = in.readShort(); 
        int length;
        switch (type) {
# Set readableTypes = new HashSet();
# for( int i = 0; i < supportedReturnTypes.length; i++ ) {
#   ClassData type = supportedReturnTypes[i];
#   if( !type.isPrimitive() && type.getSerializer() instanceof PrimitiveTypeSerializer ) {
#     continue;   
#   }
#   readableTypes.add( type ); 
# }
#   for( Iterator it = readableTypes.iterator(); it.hasNext(); ) {
#     ClassData supportedType = (ClassData)it.next();
#     if (!supportedType.isArray()) {
#       String typeName;
#       if (supportedType.isPrimitive()) {
#           typeName = supportedType.getWrapperType().getShortClassName();
#       } else {
#           typeName = supportedType.getShortClassName();
#       }
#         Serializer s = supportedType.getSerializer();
#       if( s instanceof ComplexTypeSerializer ) {
#           ((ComplexTypeSerializer)s).setClient( support.isClient());
#       }
            case ${s.getType()}:
#         if (supportedType.equals(VOID_TYPE)) {
                return ${EMPTY_ARRAY_FIELD};
#         } else {
                ${s.readAndReturnObject(_input)};
#         }
#     }
#   } // end iteration over supported return types
# if (data.isArrayReturnTypeSupported()) {
            case ${ARRAY_TYPE}: { /* ARRAY */
                short elementType = in.readShort();
                length = in.readInt();
                switch (elementType) {
#   for( Iterator it = readableTypes.iterator(); it.hasNext(); ) {
#     ClassData supportedType = (ClassData)it.next();
#     if (supportedType.isArray()) {
#       ClassData elementType = supportedType.getComponentType();
#       Serializer s = elementType.getSerializer();
#       if( s instanceof ComplexTypeSerializer ) {
#           ((ComplexTypeSerializer)s).setClient( support.isClient());
#       }
                case ${s.getType()}: {
                ${elementType.getShortClassName()}[] data = new ${elementType.getShortClassName()}[length];
#       if (elementType.isPrimitive()) {
#         // special optimization for byte[]
#         if (elementType.equalsClass(Byte.TYPE)) {
                        in.readFully(data);
#         } else {
                        for (int i = 0; i < length; i++) {
                            ${s.read("in", false, "data[i]")};
                        }
#         }
#       } else { // object array
                        for (int i = 0; i < length; i++) {
                            data[i] = (${elementType.getShortClassName()}) readObject(in);
                        }
#       } // end is element primitive
                        return data;
                }
#     }
#   }
                }
            }
# } // end is array return type supported
            case -1: /* NULL */
                return null;
        }
        throw new IllegalArgumentException("Unsupported return type (" + type + ")");
    }
}
