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
# import java.io.File;
# import java.util.*;
# import org.netbeans.mobility.end2end.core.model.*;
# import org.netbeans.mobility.end2end.core.model.classdata.*;
# import org.netbeans.mobility.end2end.core.model.protocol.Serializer;
# import org.netbeans.mobility.end2end.core.model.protocol.binary.ComplexTypeSerializer;
#
# String outputDir = data.getServerOutputDirectory();
# String packageName = data.getServerPackageName();
# ProtocolSupport support = new ProtocolSupport(data, this, false);
# setOut(support.getServletSupportPath("EndToEndGateways"));
# getOutput().addCreatedFile(support.getServletSupportPath("EndToEndGateways"));
package ${support.serverSupportPackage()};

# Set imports = new HashSet();
# MethodData[] methods = data.getMethods();
# for (int methodIndex = 0; methodIndex < methods.length; methodIndex++) {
#    MethodData currentMethod = methods[methodIndex];  
#    imports.add(currentMethod.getClassData().getClassName());
#    imports.addAll(Arrays.asList(currentMethod.getRequiredImports()));
# }
# Iterator iter = imports.iterator();
# while (iter.hasNext()) {
#   String importClassName = (String) iter.next();
#   // only import the class if it is in a package
#   if (importClassName.indexOf('.') != -1) {
import ${importClassName};
#   }
# }

import java.io.*;
import javax.servlet.http.HttpSession;

public class EndToEndGateways {
# for (int methodIndex = 0; methodIndex < methods.length; methodIndex++) {
#    MethodData currentMethod = methods[methodIndex];
#    String className = currentMethod.getImplementingClassName();

/**
 *  This class implements the application server connectivity specific to the needs.
 */
public static class ${className} implements InvocationAbstraction {
    /**
     *  This method performs the actual invocation of server functionality. It is
     *  used by the servlet to delegate functionality to external classes.
     *
     *@param  clientID         The unique identifier of a client
     *@param  input            The stream from which we should read the parameters
     *      for the methods
     *@return                  The return value for the method NULL IS NOT
     *      SUPPORTED!!!!
     *@exception  IOException  Thrown when a protocol error occurs
     */
    public Object invoke(HttpSession session, DataInput input) throws Exception {
#   String inputName = "input";
#       ClassData[] paramTypes = currentMethod.getParameterTypes();
#           for(int i = 1 ; i <= paramTypes.length ; i++){
#               if(paramTypes[i - 1].isArray()) {
#                   String arrayClassName = paramTypes[i - 1].getShortClassName();
                    ${arrayClassName} param${i};
#                   String typei = "type" + i;
        short ${typei} = ${inputName}.readShort(); // reading the type
        if (${typei} == -1) { // NULL_TYPE) {
            param${i} = null;
        } else {
#                   if (paramTypes[i - 1].getComponentType().isPrimitive()) {
            param${i} = (${arrayClassName}) Utility.readArray(input);
#                   } else {
#   String arrayName = paramTypes[i - 1].getShortClassName();
#   ClassData component = paramTypes[i - 1].getComponentType();
#   String componentName = component.getShortClassName();
            Object o = Utility.readArray(input);
            if (o instanceof ${arrayName}) {
                param${i} = (${arrayName}) o;
            } else {
                Object[] objArray = (Object[]) o;
#   if (component.isArray()) {
#       int componentBaseNameIndex = componentName.indexOf('[');
#       String componentBaseName = componentName.substring(0, componentBaseNameIndex);
#       String brackets = componentName.substring(componentBaseNameIndex);
                param${i} = new ${componentBaseName}[objArray.length]${brackets};
#   } else {
                param${i} = new ${componentName}[objArray.length];
#   }
                for (int i = 0; i < objArray.length; i++) {
                    param${i}[i] = (${componentName}) objArray[i];
                }
            }
#                   }
        }
#               } else {
#                   Serializer s = paramTypes[i - 1].getSerializer();
#                   
#                   if( s instanceof ComplexTypeSerializer ) {
#                       ((ComplexTypeSerializer)s).setClient( false );
#                   }
#                   String parami = "param" + i;
#                   int typeIndex = data.getValueForType(paramTypes[i - 1]);
#                   String typei = "type" + i;
        short ${typei} = ${inputName}.readShort(); // reading the type
        
#                   if (paramTypes[i - 1].isPrimitive()) {
            ${s.read(inputName, true, parami)};
#                   } else {
        ${paramTypes[i - 1].getShortClassName()} ${parami};
        if (${typei} == -1) { // NULL_TYPE) {
            ${parami} = null;
        } else {
            ${s.read(inputName, false, parami)};
        }
#                   }
#               }
#           }
#
# String implementingClassName = currentMethod.getClassData().getClassName();
# boolean returnsValue = !currentMethod.getReturnType().equalsClass(Void.TYPE);
# if (currentMethod.getClassData().getClassName().indexOf('.') != -1) {
${implementingClassName} instance = (${implementingClassName})
  session.getAttribute("${implementingClassName}");
if (instance == null) {
    instance = (${implementingClassName}) Class.forName("${implementingClassName}").newInstance();
    session.setAttribute("${implementingClassName}", instance);
}
#        String methodName = currentMethod.getName();
#        StringBuffer sb = new StringBuffer();
#        if (returnsValue) {
#            sb.append("return Utility.toObject(");
#        }
#        sb.append("instance.").append(methodName).append("(");
#        for (int i = 0; i < paramTypes.length; i++) {
#            if (i > 0) {
#                sb.append(", ");
#            }
#            sb.append("param").append(i + 1);
#        }
#        sb.append(")");
#        if (!returnsValue) {
#            sb.append(";\nreturn Utility.VOID_VALUE;\n");
#        } else {
#            sb.append(");\n");
#        }
        ${sb}
# } else {
  /*  The service class being referenced is in the root package, so it
     *  is not possible to import it. Instead, the service is accessed
     *  using Java's reflection API.
     */
Object instance = session.getAttribute("${implementingClassName}");
if (instance == null) {
    instance = Class.forName("${implementingClassName}").newInstance();
    session.setAttribute("${implementingClassName}", instance);
}
Class[] signature = {
# for (int i = 0; i < paramTypes.length; i++) {
#   ClassData type = paramTypes[i];
#   String typeString;
#   if (type.isPrimitive()) {
#     typeString = type.getWrapperType().getClassName() + ".TYPE";
#   } else {
#     typeString = type.getClassName() + ".class";
#   }
#   String comma = (i == paramTypes.length - 1) ? "" : ",";
  ${typeString}${comma}
# }
};
Class clazz = instance.getClass();
try {
  java.lang.reflect.Method method = clazz.getMethod("${currentMethod.getName()}", signature);
  Object[] args = {
# for (int i = 0; i < paramTypes.length; i++) {
#   String comma = (i == paramTypes.length - 1) ? "" : ",";
#   if (paramTypes[i].isPrimitive()) {
#     String cast = support.castToReference("param" + (i + 1), paramTypes[i]);
    ${cast}${comma}
#   } else {
    param${(i + 1)}${comma}
#   }
# }
  };
# if (returnsValue) {
      return method.invoke(instance, args);
# } else {
    method.invoke(instance, args);
    return Utility.VOID_VALUE;
# }
} catch (java.lang.reflect.InvocationTargetException e) {
    try {
      throw e.getTargetException();
    } catch (Exception te) {
        throw te;
    } catch (Error te) {
        throw te;
    } catch (Throwable te) {
        // won't reach here
        return null;
    }
}
# }
    }
}
# } // end for loop interating over methods

    private static Object readObject(DataInput in) throws IOException {
        return Utility.readObject(in);
    }
}
