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

package org.netbeans.modules.debugger.jpda.models;

import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.ClassType;
import com.sun.jdi.Field;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.PrimitiveValue;
import com.sun.jdi.Value;

import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.JPDAClassType;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.jdi.ClassNotPreparedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ClassTypeWrapper;
import org.netbeans.modules.debugger.jpda.jdi.FieldWrapper;
import org.netbeans.modules.debugger.jpda.jdi.IllegalArgumentExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectCollectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectReferenceWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ReferenceTypeWrapper;
import org.netbeans.modules.debugger.jpda.jdi.TypeComponentWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ValueWrapper;
import org.openide.util.Exceptions;


/**
 * @author   Jan Jancura
 */
class FieldVariable extends AbstractVariable implements
org.netbeans.api.debugger.jpda.Field {

    protected Field field;
    private ObjectReference objectReference;
    

    FieldVariable (
        JPDADebuggerImpl debugger,
        PrimitiveValue value,
    //    String className,
        Field field,
        String parentID,
        ObjectReference objectReference
    ) {
        super (
            debugger, 
            value, 
            getID(parentID, field)
        );
        this.field = field;
        //this.className = className;
        this.objectReference = objectReference;
    }

    private static String getID(String parentID, Field field) {
        try {
            return parentID + '.' + TypeComponentWrapper.name(field);
        } catch (InternalExceptionWrapper ex) {
            return parentID + '.' + ex.getCause().getLocalizedMessage();
        } catch (VMDisconnectedExceptionWrapper ex) {
            return parentID + ".0";
        }
    }

    // LocalVariable impl.......................................................
    

    /**
    * Returns string representation of type of this variable.
    *
    * @return string representation of type of this variable.
    */
    public String getName () {
        try {
            return TypeComponentWrapper.name(field);
        } catch (InternalExceptionWrapper ex) {
            return ex.getCause().getLocalizedMessage();
        } catch (VMDisconnectedExceptionWrapper ex) {
            return "";
        }
    }

    /**
     * Returns name of enclosing class.
     *
     * @return name of enclosing class
     */
    public String getClassName () {
        try {
            return ReferenceTypeWrapper.name(TypeComponentWrapper.declaringType(field)); //className;
        } catch (InternalExceptionWrapper ex) {
            return ex.getCause().getLocalizedMessage();
        } catch (VMDisconnectedExceptionWrapper ex) {
            return "";
        }
    }
    
    public JPDAClassType getDeclaringClass() {
        try {
            return new JPDAClassTypeImpl(getDebugger(), (ReferenceType) ValueWrapper.type(objectReference));
        } catch (InternalExceptionWrapper ex) {
            throw ex.getCause();
        } catch (VMDisconnectedExceptionWrapper ex) {
            throw ex.getCause();
        } catch (ObjectCollectedExceptionWrapper ex) {
            throw ex.getCause();
        }
    }

    /**
    * Returns string representation of type of this variable.
    *
    * @return string representation of type of this variable.
    */
    public String getDeclaredType () {
        try {
            return FieldWrapper.typeName(field);
        } catch (InternalExceptionWrapper ex) {
            return ex.getLocalizedMessage();
        } catch (VMDisconnectedExceptionWrapper ex) {
            return "";
        }
    }

    /**
     * Returns <code>true</code> for static fields.
     *
     * @return <code>true</code> for static fields
     */
    public boolean isStatic () {
        return TypeComponentWrapper.isStatic0(field);
    }
    
    protected void setValue (Value value) throws InvalidExpressionException {
        try {
            boolean set = false;
            if (objectReference != null) {
                ObjectReferenceWrapper.setValue(objectReference, field, value);
                set = true;
            } else {
                ReferenceType rt = TypeComponentWrapper.declaringType(field);
                if (rt instanceof ClassType) {
                    ClassType ct = (ClassType) rt;
                    ClassTypeWrapper.setValue(ct, field, value);
                    set = true;
                }
            }
            if (!set) {
                throw new InvalidExpressionException(field.toString());
            }
        } catch (IllegalArgumentExceptionWrapper ex) {
            throw new InvalidExpressionException (ex.getCause());
        } catch (InvalidTypeException ex) {
            throw new InvalidExpressionException (ex);
        } catch (ClassNotLoadedException ex) {
            throw new InvalidExpressionException (ex);
        } catch (InternalExceptionWrapper ex) {
            throw new InvalidExpressionException (ex);
        } catch (VMDisconnectedExceptionWrapper ex) {
            throw new InvalidExpressionException (ex);
        } catch (ClassNotPreparedExceptionWrapper ex) {
            throw new InvalidExpressionException (ex);
        } catch (ObjectCollectedExceptionWrapper ex) {
            throw new InvalidExpressionException (ex);
        }
    }

    public FieldVariable clone() {
        String name;
        try {
            name = TypeComponentWrapper.name(field);
        } catch (InternalExceptionWrapper ex) {
            name = ex.getCause().getLocalizedMessage();
        } catch (VMDisconnectedExceptionWrapper ex) {
            name = "0";
        }
        FieldVariable clon;
        clon = new FieldVariable(getDebugger(), (PrimitiveValue) getJDIValue(), field,
                getID().substring(0, getID().length() - ("." + name + (getJDIValue() instanceof ObjectReference ? "^" : "")).length()),
                objectReference);
        return clon;
    }

    // other methods ...........................................................

    public String toString () {
        try {
            return "FieldVariable " + TypeComponentWrapper.name(field);
        } catch (InternalExceptionWrapper ex) {
            return ex.getLocalizedMessage();
        } catch (VMDisconnectedExceptionWrapper ex) {
            return "Disconnected";
        }
    }
}

