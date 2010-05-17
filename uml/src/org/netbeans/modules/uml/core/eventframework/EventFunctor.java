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


package org.netbeans.modules.uml.core.eventframework;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Vector;

import org.netbeans.modules.uml.core.support.umlsupport.Log;

/**
 * @author sumitabhk
 *
 */
public class EventFunctor
{

	//it stores the <className|methodName, Method >
	private static Hashtable < String, Method > m_Functors = new Hashtable < String, Method >();
	private String m_ClassName = null;
	private Class  m_Class     = null;
	private String m_MethodName = null;
	private Method m_Method = null;
	private Object[] m_Parameters = null;
	/**
	 * 
	 */
	private EventFunctor() {
		super();
	}
	
	public void setParameters(Object[] parms)
	{
        if (parms != null && parms.length > 0)
        {
        	Object obj = parms[0];
        	if (obj instanceof Vector)
        	{
        		
            // If the first parameter is a Vector, expand it. This is necessary
            // because the EventDispatchers pack all their parameters into a 
            // Vector before giving it to us.
//            if (parms[0] instanceof Vector<Object>)
//            {
                Vector<Object> vec = new Vector<Object>((Collection)parms[0]);
                Object[] newArray = new Object[vec.size() + parms.length - 1];
                Object[] temp = vec.toArray();
                System.arraycopy(temp, 0, newArray, 0, temp.length);
                if (parms.length > 1)
                {
                    System.arraycopy(parms, 1, newArray, temp.length, 
                                     parms.length - 1);
                }
                parms = newArray;
            }
        }
		m_Parameters = parms;
	}
	
	public EventFunctor(String className, String methodName)
	{
		m_ClassName = className;
		m_MethodName = methodName;
		m_Method = retrieveMethod(className, methodName);
	}
	
	public EventFunctor(Class cl, String methodName)
	{
		m_Class = cl;
		m_MethodName = methodName;
		m_Method = retrieveMethod(cl, methodName);
	}
	
	public Object execute(Object[] parms, Object objInstance)
	{
		return invokeMethod(m_Method, parms, objInstance);
	}

	public Object execute(Object objInstance)
	{
		return invokeMethod(m_Method, m_Parameters, objInstance);
	}
	
	public boolean isResultOK()
	{
		return true;
	}
	
	//here we assume that there will be only one method with this name in the class
	public static Method retrieveMethod(Class clazz, String methodName)
	{
		String hashKey = clazz.getName() + "|" + methodName;
		Method retMeth = m_Functors.get(hashKey);
		if (retMeth == null)
		{
			//try to get the Method, return it and put it in the hashtable.
			Method[] meths = clazz.getMethods();
			if (meths != null)
			{
				for (int i=0; i<meths.length; i++)
				{
					Method meth = meths[i];
					if (meth.getName().equals(methodName))
					{
						retMeth = meth;
						m_Functors.put(hashKey, meth);
						break;
					}
				}
			}
		}
		return retMeth;
	}
    
	//here we assume that there will be only one method with this name in the class
	public static Method retrieveMethod(String className, String methodName)
	{
		Method retMeth = m_Functors.get(className+"|"+methodName);
		if (retMeth == null)
		{
			//try to get the Method, return it and put it in the hashtable.
			try 
			{
				Class clazz = Class.forName(className);
				Method[] meths = clazz.getMethods();
				if (meths != null)
				{
					for (int i=0; i<meths.length; i++)
					{
						Method meth = meths[i];
						if (meth.getName().equals(methodName))
						{
							retMeth = meth;
							m_Functors.put(className+"|"+methodName, meth);
							break;
						}
					}
				}
			} catch (ClassNotFoundException e) 
			{
			}
		}
		return retMeth;
	}
	
	public static Object retrieveAndInvokeMethod(String className, String methodName, Object[] parms, Object objInstance)
	{
		Object retObj = null;
		Method meth = retrieveMethod(className, methodName);
		if (meth != null)
		{
			try {
				retObj = meth.invoke(objInstance, parms);
			} catch (IllegalArgumentException e) {
			} catch (IllegalAccessException e) {
			} catch (InvocationTargetException e) {
			}
		}
		return retObj;
	}
	
	private static String sayParams(Object[] objs)
	{
		StringBuffer par = new StringBuffer();
		for (int i = 0; i < objs.length; ++i)
		{    
			if (i > 0)
				par.append(", ");
			String parType = objs[i] != null? objs[i].getClass().getName() : "null";
			par.append(parType.substring(parType.lastIndexOf('.') + 1));
		}
		return par.toString();
	}
    
	private static String sayParams(Method m)
	{
		StringBuffer par = new StringBuffer();
		Class[] c = m.getParameterTypes();
		for (int i = 0; i < c.length; ++i)
		{    
			if (i > 0)
				par.append(", ");
			String parType = c[i].getName();
			par.append(parType.substring(parType.lastIndexOf('.') + 1));
		}
		return par.toString();
	}
    
	public static Object invokeMethod(Method meth, Object[] parms, Object objInstance)
	{
		Object retObj = null;
		if (meth != null)
		{
			try {
				retObj = meth.invoke(objInstance, parms);
            }
            catch (Exception e) 
            {
				Log.err(e.getMessage() + " when invoking "
						+ meth.getName() + "(" + sayParams(meth) + ") on " +
						objInstance.getClass().getName()
						+ " with parameters: " + sayParams(parms));
                Log.stackTrace(e);
                System.out.println(e.getMessage() + " when invoking "
						+ meth.getName() + "(" + sayParams(meth) + ") on " +
						objInstance.getClass().getName()
						+ " with parameters: " + sayParams(parms));
                e.printStackTrace();
            }
		}
		return retObj;
	}
	
}



