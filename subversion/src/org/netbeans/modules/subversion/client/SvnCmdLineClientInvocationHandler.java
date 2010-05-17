/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
package org.netbeans.modules.subversion.client;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.netbeans.modules.subversion.client.parser.LocalSubversionException;
import org.netbeans.modules.subversion.client.parser.SvnWcParser;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;

/**
 *
 *
 * @author Tomas Stupka 
 */
public class SvnCmdLineClientInvocationHandler extends SvnClientInvocationHandler {

    private static final String ISVNSTATUS_IMPL = System.getProperty("ISVNStatus.impl", ""); // NOI18N    
    
    private SvnWcParser wcParser = new SvnWcParser();    
    
    public SvnCmdLineClientInvocationHandler (ISVNClientAdapter adapter, SvnClientDescriptor desc, SvnProgressSupport support, int handledExceptions) {
        super(adapter, desc, support, handledExceptions);
    }
   
    @Override
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

    private boolean isHandledIntern(Method method, Object[] args) {
        boolean exec = ISVNSTATUS_IMPL.equals("exec"); // NOI18N
        if(exec) {
            return false;
        }                
        return isLocalReadCommand(method, args);
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

    @Override
    protected boolean isCommandLine () {
        return true;
    }
    
}

