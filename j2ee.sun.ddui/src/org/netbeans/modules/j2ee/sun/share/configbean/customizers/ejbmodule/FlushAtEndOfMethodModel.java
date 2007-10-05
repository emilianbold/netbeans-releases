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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
/*
 * FlushAtEndOfMethodModel.java
 *
 * Created on February 4, 2005, 12:26 AM
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.ejbmodule;

import java.util.Map;
import java.util.ResourceBundle;

import org.netbeans.modules.j2ee.sun.dd.api.ejb.Method;
import org.netbeans.modules.j2ee.sun.dd.api.common.MethodParams;
import org.netbeans.modules.j2ee.sun.share.configbean.CmpEntityEjb;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.MultiMethodTableModel;
import org.netbeans.modules.j2ee.sun.share.configbean.StorageBeanFactory;

/**
 *
 * @author Rajeshwar Patil
 */
public class FlushAtEndOfMethodModel extends MultiMethodTableModel{

    private final ResourceBundle bundle = 
        ResourceBundle.getBundle(
            "org.netbeans.modules.j2ee.sun.share.configbean.customizers.ejbmodule.Bundle"); //NOI18N

    private CmpEntityEjb cmpEntityEjb;

    private final String[] columnNames = {
        bundle.getString("LBL_Method"),                                 //NOI18N
        bundle.getString("LBL_Flush"),                                  //NOI18N
        bundle.getString("LBL_Description")                             //NOI18N
    };


    /** Creates a new instance of FlushAtEndOfMethodModel */
    public FlushAtEndOfMethodModel(CmpEntityEjb cmpEntityEjb, Map selectionToMethodsMap, Map selectionToDDMethodsMap) {
        super(selectionToMethodsMap, selectionToDDMethodsMap);
        this.cmpEntityEjb = cmpEntityEjb;
    }


    public void setData(CmpEntityEjb cmpEntityEjb, Map selectionToMethodsMap, Map selectionToDDMethodsMap){
        this.cmpEntityEjb = cmpEntityEjb;
        setData(selectionToMethodsMap, selectionToDDMethodsMap);
    }


    public FlushAtEndOfMethodModel() {
    }


    protected String[] getColumnNames(){
        return columnNames;
    }


    protected Class getColumnType(int column){
        if(column == 2){
            return String.class; 
        }else{
            //Control should never reach here.
            assert(false);
        }
        return Object.class;
    }

    ///revert later(post Netbeans 4.1); since method is a mockup object and 
    //does not have unique method name available to display, using row number instead
    /*
    protected Object getValueAt(int column, Object object){
        if(column == 0){
            //XXX implemented for DummyMethod replace with real *Method*
            //System.out.println("FlushAtEndOfMethodModel getValueAt return value :" + object.toString() );
            DummyMethod method = (DummyMethod) object;
            return method.toString( );
        } else {
            //Control should never reach here.
            //System.out.println("FlushAtEndOfMethodModel getValueAt return value null :" );
            assert(false);
            return null;
        }
    }
   */
    protected Object getValueAt(int column, Object object, int row){
        if(column == 0){
            //XXX implemented for DummyMethod replace with real *Method*
            //System.out.println("FlushAtEndOfMethodModel getValueAt return value :" + object.toString() );
            DummyMethod method = (DummyMethod) object;
            return "Method" + row;
        } else {
            //Control should never reach here.
            //System.out.println("FlushAtEndOfMethodModel getValueAt return value null :" );
            assert(false);
            return null;
        }
    }


    protected Object getDDValueAt(int column, Object ddObject){
        if(column == 2){
            Method ddMethod = (Method) ddObject;
            return ddMethod.getDescription( );
        } else {
            //Control should never reach here.
            assert(false);
            return null;
        }
    }


    protected void setDDValueAt(int column, Object ddObject, Object value){
        if(column == 2){
            Method ddMethod = (Method) ddObject;
            ddMethod.setDescription((String)value);
        } else {
            //Control should never reach here.
            assert(false);
        }
    }


    //convert the given Method object into the schama2beans DD (Method) object
    protected Object getDDMethod(Object object, String selection){
        StorageBeanFactory storageFactory = cmpEntityEjb.getConfig().getStorageFactory();
        //System.out.println("FlushAtEndOfMethodModel getDDMethod");
        //System.out.println("FlushAtEndOfMethodModel getDDMethod method :" + object );
        //System.out.println("FlushAtEndOfMethodModel getDDMethod selection :" + selection );
        //XXX implemented for DummyMethod replace with real *Method*
        DummyMethod method = (DummyMethod)object;
        //Method ddMethod = new Method();
        Method ddMethod = storageFactory.createMethod();
        //ddMethod.setDescription(method.toString());
        ddMethod.setMethodName(method.getName());
        ddMethod.setMethodIntf(selection);
        ddMethod.setEjbName(cmpEntityEjb.getEjbName());

        String[] params = method.getParameterTypes();
        //MethodParams ddParams = new MethodParams();
        MethodParams ddParams = storageFactory.createMethodParams();
        for(int i=0; i<params.length; i++){
            ddParams.addMethodParam(params[i]);
        }

        ddMethod.setMethodParams(ddParams);
        
        //System.out.println("FlushAtEndOfMethodModel getDDMethod ddMethod:" + ddMethod);
        //System.out.println("FlushAtEndOfMethodModel getDDMethod name :" + ddMethod.getMethodName() );
        //System.out.println("FlushAtEndOfMethodModel getDDMethod interface :" + ddMethod.getMethodIntf() );
        //System.out.println("FlushAtEndOfMethodModel getDDMethod ejb name :" + ddMethod.getEjbName() );
        //System.out.println("FlushAtEndOfMethodModel getDDMethod params :" + ddMethod.getMethodParams() );
        
        return ddMethod;
    }


    protected void addDDMethod(Object ddObject){
        Method ddMethod = (Method)ddObject;
        //printMethod(ddMethod);
        cmpEntityEjb.addMethod(ddMethod);
    }


    protected void removeDDMethod(Object ddObject){
        Method ddMethod = (Method)ddObject;
        cmpEntityEjb.removeMethod(ddMethod);
    }


    //determine whether the given schema2bean DD (Method) object is equal to 
    //the given Method object
    protected boolean areEqual(Object ddObject, Object object){
        //XXX implemented for DummyMethod replace with real *Method*
        Method ddMethod = (Method)ddObject;
        DummyMethod method = (DummyMethod) object;
        boolean returnValue = false;


        String ddMethodName = ddMethod.getMethodName();
        //System.out.println("FlushAtEndOfMethodModel areEqual ddMethodName " + ddMethodName);
        String methodName = method.getName();
        //System.out.println("FlushAtEndOfMethodModel areEqual methodName " + methodName);
        if(ddMethodName.equals(methodName)){
            //check for parameters
            MethodParams methodParams = ddMethod.getMethodParams();
            String[] params = method.getParameterTypes();

            String ddParam;
            String param;
            for(int i=0; i<params.length; i++){
                param = params[i];
                ddParam = methodParams.getMethodParam(i);
                //System.out.println("FlushAtEndOfMethodModel areEqual param " + param);
                //System.out.println("FlushAtEndOfMethodModel areEqual ddParam " + ddParam);
                if(!param.equals(ddParam)){
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }


    private void printMethod(Method ddMethod){
        System.out.println("FlushAtEndOfMethodModel ddMethod:" + ddMethod);                         //NOI18N
        System.out.println("FlushAtEndOfMethodModel ddMethod:" + ddMethod.toString());              //NOI18N
        System.out.println("FlushAtEndOfMethodModel name :" + ddMethod.getMethodName() );           //NOI18N
        System.out.println("FlushAtEndOfMethodModel interface :" + ddMethod.getMethodIntf() );      //NOI18N
        System.out.println("FlushAtEndOfMethodModel ejb name :" + ddMethod.getEjbName() );          //NOI18N
        System.out.println("FlushAtEndOfMethodModel params :" + ddMethod.getMethodParams() );       //NOI18N
    }
}
