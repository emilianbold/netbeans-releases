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
 * PrefetchDisabledModel.java
 *
 * Created on February 23, 2005, 11:46 PM
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.ejbmodule;

import java.util.List;
import java.util.ResourceBundle;

import org.netbeans.modules.j2ee.sun.dd.api.ejb.QueryMethod;
import org.netbeans.modules.j2ee.sun.dd.api.common.MethodParams;
import org.netbeans.modules.j2ee.sun.share.configbean.ConfigQuery;
import org.netbeans.modules.j2ee.sun.share.configbean.CmpEntityEjb;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.MethodTableModel;
import org.netbeans.modules.j2ee.sun.share.configbean.StorageBeanFactory;


/**
 *
 * @author Rajeshwar Patil
 */
public class PrefetchDisabledModel extends MethodTableModel {
    
    static final ResourceBundle bundle = 
        ResourceBundle.getBundle(
            "org.netbeans.modules.j2ee.sun.share.configbean.customizers.ejbmodule.Bundle"); //NOI18N

    private CmpEntityEjb cmpEntityEjb;

    private static final String[] columnNames = {
        bundle.getString("LBL_Method"),                                 //NOI18N
        bundle.getString("LBL_Disable"),                                //NOI18N
    };


    /** Creates a new instance of PrefetchDisabledModel */
    public PrefetchDisabledModel(CmpEntityEjb cmpEntityEjb, List methodList,
            List prefetchedMethodList) {
        super(methodList, prefetchedMethodList);
        this.cmpEntityEjb = cmpEntityEjb;
    }


    public void setData(CmpEntityEjb cmpEntityEjb, List methodList, 
            List prefetchedMethodList){
        this.cmpEntityEjb = cmpEntityEjb;
        setData(methodList, prefetchedMethodList);
    }


    public PrefetchDisabledModel() {
    }


    protected String[] getColumnNames(){
        return columnNames;
    }


    protected Class getColumnType(int column){
        return Object.class;
    }


    protected Object getValueAt(int column, Object object, int row){
        if(column == 0){
            ConfigQuery.MethodData method = (ConfigQuery.MethodData) object;
            return method.getOperationName( );
        } else {
            //Control should never reach here.
            assert(false);
            return null;
        }
    }


    protected Object getDDValueAt(int column, Object ddObject){
        QueryMethod queryMethod = (QueryMethod) ddObject;
        switch(column){
            default: {
                //Control should never reach here.
                assert(false);
                return null;
            }
        }
    }


    protected void setDDValueAt(int column, Object ddObject, Object value){
        QueryMethod queryMethod = (QueryMethod) ddObject;
        switch(column){
            default: {
                //Control should never reach here.
                assert(false);
            }
        }
    }


    //convert the given Method object into the schama2beans DD (Method) object
    protected Object getDDMethod(Object object){
        ConfigQuery.MethodData method = (ConfigQuery.MethodData) object;
        QueryMethod queryMethod = StorageBeanFactory.getDefault().createQueryMethod();

        queryMethod.setMethodName(method.getOperationName());
        List params = method.getParameters();
        MethodParams methodParams = queryMethod.newMethodParams();
        for(int i=0; i<params.size(); i++){
            methodParams.addMethodParam((String)params.get(i));
        }
        queryMethod.setMethodParams(methodParams);

        //printMethod(queryMethod);
        return queryMethod;
    }


    protected void addDDMethod(Object ddObject){
        QueryMethod ddMethod = (QueryMethod)ddObject;
        //printMethod(ddMethod);
        cmpEntityEjb.addQueryMethod(ddMethod);
    }


    protected void removeDDMethod(Object ddObject){
        QueryMethod ddMethod = (QueryMethod)ddObject;
        cmpEntityEjb.removeQueryMethod(ddMethod);
    }


    //determine whether the given schema2bean DD (Method) object is equal to 
    //the given Method object
    protected boolean areEqual(Object ddObject, Object object){
        QueryMethod ddMethod = (QueryMethod)ddObject;
        ConfigQuery.MethodData method = (ConfigQuery.MethodData) object;
        boolean returnValue = false;


        String ddMethodName = ddMethod.getMethodName();
        String methodName = method.getOperationName();
        if(ddMethodName.equals(methodName)){
            //check for parameters
            MethodParams methodParams = ddMethod.getMethodParams();
            List params = method.getParameters();

            String ddParam;
            String param;
            for(int i=0; i<params.size(); i++){
                param = (String)params.get(i);
                ddParam = methodParams.getMethodParam(i);
                if(!param.equals(ddParam)){
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }


    private void printMethod(QueryMethod queryMethod){
        System.out.println("PrefetchDisabledModel queryMethod:" + queryMethod);                         //NOI18N
        System.out.println("PrefetchDisabledModel queryMethod:" + queryMethod.toString());              //NOI18N
        System.out.println("PrefetchDisabledModel name :" + queryMethod.getMethodName() );              //NOI18N
        System.out.println("PrefetchDisabledModel params :" + queryMethod.getMethodParams() );          //NOI18N
    }
}
