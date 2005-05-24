/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * CheckpointAtEndOfMethodModel.java
 *
 * Created on February 23, 2005, 4:03 PM
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.ejbmodule;

import java.util.Map;
import java.util.ResourceBundle;

import org.netbeans.modules.j2ee.sun.dd.api.ejb.Method;
import org.netbeans.modules.j2ee.sun.dd.api.common.MethodParams;
import org.netbeans.modules.j2ee.sun.share.configbean.StatefulEjb;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.MultiMethodTableModel;
import org.netbeans.modules.j2ee.sun.share.configbean.StorageBeanFactory;

/**
 *
 * @author Rajeshwar Patil
 */
public class CheckpointAtEndOfMethodModel extends MultiMethodTableModel{

    static final ResourceBundle bundle = 
        ResourceBundle.getBundle(
            "org.netbeans.modules.j2ee.sun.share.configbean.customizers.ejbmodule.Bundle"); //NOI18N

    private StatefulEjb statefulEjb;

    private static final String[] columnNames = {
        bundle.getString("LBL_Method"),                                 //NOI18N
        bundle.getString("LBL_Checkpoint"),                             //NOI18N
        bundle.getString("LBL_Description")                             //NOI18N
    };


    /** Creates a new instance of CheckpointAtEndOfMethodModel */
    public CheckpointAtEndOfMethodModel(StatefulEjb statefulEjb, Map selectionToMethodsMap, Map selectionToDDMethodsMap) {
        super(selectionToMethodsMap, selectionToDDMethodsMap);
        this.statefulEjb = statefulEjb;
    }


    public void setData(StatefulEjb statefulEjb, Map selectionToMethodsMap, Map selectionToDDMethodsMap){
        this.statefulEjb = statefulEjb;
        setData(selectionToMethodsMap, selectionToDDMethodsMap);
    }


    public CheckpointAtEndOfMethodModel() {
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
            //System.out.println("CheckpointAtEndOfMethodModel getValueAt return value :" + object.toString() );
            DummyMethod method = (DummyMethod) object;
            return method.toString( );
        } else {
            //Control should never reach here.
            //System.out.println("CheckpointAtEndOfMethodModel getValueAt return value null :" );
            assert(false);
            return null;
        }
    }
    */
    protected Object getValueAt(int column, Object object, int row){
        if(column == 0){
            //XXX implemented for DummyMethod replace with real *Method*
            //System.out.println("CheckpointAtEndOfMethodModel getValueAt return value :" + object.toString() );
            DummyMethod method = (DummyMethod) object;
            return "Method" + row;
        } else {
            //Control should never reach here.
            //System.out.println("CheckpointAtEndOfMethodModel getValueAt return value null :" );
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
        //System.out.println("CheckpointAtEndOfMethodModel getDDMethod");
        //System.out.println("CheckpointAtEndOfMethodModel getDDMethod method :" + object );
        //System.out.println("CheckpointAtEndOfMethodModel getDDMethod selection :" + selection );
        //XXX implemented for DummyMethod replace with real *Method*
        DummyMethod method = (DummyMethod)object;
        //Method ddMethod = new Method();
        Method ddMethod = StorageBeanFactory.getDefault().createMethod();
        //ddMethod.setDescription(method.toString());
        ddMethod.setMethodName(method.getName());
        ddMethod.setMethodIntf(selection);
        ddMethod.setEjbName(statefulEjb.getEjbName());

        String[] params = method.getParameterTypes();
        //MethodParams ddParams = new MethodParams();
        MethodParams ddParams = StorageBeanFactory.getDefault().createMethodParams();
        for(int i=0; i<params.length; i++){
            ddParams.addMethodParam(params[i]);
        }

        ddMethod.setMethodParams(ddParams);
        
        //System.out.println("CheckpointAtEndOfMethodModel getDDMethod ddMethod:" + ddMethod);
        //System.out.println("CheckpointAtEndOfMethodModel getDDMethod name :" + ddMethod.getMethodName() );
        //System.out.println("CheckpointAtEndOfMethodModel getDDMethod interface :" + ddMethod.getMethodIntf() );
        //System.out.println("CheckpointAtEndOfMethodModel getDDMethod ejb name :" + ddMethod.getEjbName() );
        //System.out.println("CheckpointAtEndOfMethodModel getDDMethod params :" + ddMethod.getMethodParams() );
        
        return ddMethod;
    }


    protected void addDDMethod(Object ddObject){
        Method ddMethod = (Method)ddObject;
        //printMethod(ddMethod);
        statefulEjb.addMethod(ddMethod);
    }


    protected void removeDDMethod(Object ddObject){
        Method ddMethod = (Method)ddObject;
        statefulEjb.removeMethod(ddMethod);
    }


    //determine whether the given schema2bean DD (Method) object is equal to 
    //the given Method object
    protected boolean areEqual(Object ddObject, Object object){
        //XXX implemented for DummyMethod replace with real *Method*
        Method ddMethod = (Method)ddObject;
        DummyMethod method = (DummyMethod) object;
        boolean returnValue = false;


        String ddMethodName = ddMethod.getMethodName();
        //System.out.println("CheckpointAtEndOfMethodModel areEqual ddMethodName " + ddMethodName);
        String methodName = method.getName();
        //System.out.println("CheckpointAtEndOfMethodModel areEqual methodName " + methodName);
        if(ddMethodName.equals(methodName)){
            //check for parameters
            MethodParams methodParams = ddMethod.getMethodParams();
            String[] params = method.getParameterTypes();

            String ddParam;
            String param;
            for(int i=0; i<params.length; i++){
                param = params[i];
                ddParam = methodParams.getMethodParam(i);
                //System.out.println("CheckpointAtEndOfMethodModel areEqual param " + param);
                //System.out.println("CheckpointAtEndOfMethodModel areEqual ddParam " + ddParam);
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
        System.out.println("CheckpointAtEndOfMethodModel ddMethod:" + ddMethod);                         //NOI18N
        System.out.println("CheckpointAtEndOfMethodModel ddMethod:" + ddMethod.toString());              //NOI18N
        System.out.println("CheckpointAtEndOfMethodModel name :" + ddMethod.getMethodName() );           //NOI18N
        System.out.println("CheckpointAtEndOfMethodModel interface :" + ddMethod.getMethodIntf() );      //NOI18N
        System.out.println("CheckpointAtEndOfMethodModel ejb name :" + ddMethod.getEjbName() );          //NOI18N
        System.out.println("CheckpointAtEndOfMethodModel params :" + ddMethod.getMethodParams() );       //NOI18N
    }
}
