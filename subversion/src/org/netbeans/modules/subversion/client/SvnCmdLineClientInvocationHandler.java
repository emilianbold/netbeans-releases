/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.subversion.client;    

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import org.netbeans.modules.subversion.client.parser.LocalSubversionException;
import org.netbeans.modules.subversion.client.parser.SvnWcParser;
import org.netbeans.modules.subversion.config.SvnConfigFiles;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;

/**
 *
 *
 * @author Tomas Stupka 
 */
public class SvnCmdLineClientInvocationHandler extends SvnClientInvocationHandler {

    private static final String ISVNSTATUS_IMPL = System.getProperty("ISVNStatus.impl", ""); // NOI18N
    private static final String GET_SINGLE_STATUS = "getSingleStatus"; // NOI18N
    private static final String GET_STATUS = "getStatus"; // NOI18N
    private static final String GET_INFO_FROM_WORKING_COPY = "getInfoFromWorkingCopy"; // NOI18N

    
    private SvnWcParser wcParser = new SvnWcParser();    
    
    public SvnCmdLineClientInvocationHandler (ISVNClientAdapter adapter, SvnClientDescriptor desc, SvnProgressSupport support, int handledExceptions) {
        super(adapter, desc, support, handledExceptions);
    }
   
    protected Object invokeMethod(Method proxyMethod, Object[] args)
    throws NoSuchMethodException, IllegalAccessException, InvocationTargetException
    {
        Object ret = null;        
        if (isHandledIntern(proxyMethod, args)) {
            try {
                ret = handleIntern(proxyMethod, args);
            } catch (LocalSubversionException ex) {
                //Exception thrown.  Call out to the default adapter
            }
        } else {            
            ret = handle(proxyMethod, args);    
        }
        return ret;
    }

    private static boolean isHandledIntern(Method method, Object[] args) {
        boolean exec = ISVNSTATUS_IMPL.equals("exec"); // NOI18N
        if(exec) {
            return false;
        }        
        
        String methodName = method.getName();
        return methodName.equals(GET_SINGLE_STATUS) || 
               methodName.equals(GET_INFO_FROM_WORKING_COPY) ||  
               (method.getName().equals(GET_STATUS) && method.getParameterTypes().length == 3); 
    }

    private Object handleIntern(Method method, Object[] args) throws LocalSubversionException {
        Object returnValue = null;

        if (GET_SINGLE_STATUS.equals(method.getName())) {
            returnValue = wcParser.getSingleStatus((File) args[0]);
        } else if (GET_INFO_FROM_WORKING_COPY.equals(method.getName())) {
            returnValue= wcParser.getInfoFromWorkingCopy((File) args[0]);
        } else if (GET_STATUS.equals(method.getName())) {
            returnValue= wcParser.getStatus(
                    (File) args[0], 
                    ((Boolean) args[1]).booleanValue(), 
                    ((Boolean) args[2]).booleanValue()
            );
        }
        return returnValue;
    }

    /**
     * @return false for methods that perform calls over network
     */
    protected boolean noRemoteCallinAWT(String methodName, Object[] args) {
        boolean ret = super.noRemoteCallinAWT(methodName, args);
        if(!ret) {
            if ("getStatus".equals(methodName)) { // NOI18N
                ret = args.length != 4 || (Boolean.TRUE.equals(args[3]) == false);
            }
        }                
        return ret;
    }
    
}

