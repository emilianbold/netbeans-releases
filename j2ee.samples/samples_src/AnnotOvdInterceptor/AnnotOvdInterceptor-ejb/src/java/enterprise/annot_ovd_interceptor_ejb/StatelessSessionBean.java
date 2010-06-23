/*
 * Copyright (c) 2010, Oracle. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of Oracle nor the names of its contributors
 *   may be used to endorse or promote products derived from this software without
 *   specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

package enterprise.annot_ovd_interceptor_ejb;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import javax.ejb.Stateless;
import javax.interceptor.Interceptors;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

// This bean uses two interceptors to validate
// the input to its (only) business method.
// Note that a single interceptor would suffice
// but to demonstrate the use of interceptor
// chaining, we use two interceptors

@Stateless
@Interceptors({ArgumentsChecker.class})
public class StatelessSessionBean
    implements StatelessSession {

    private static final String KEY = "interceptorNameList";

    private static Map<String, List<String>> interceptorNamesForMethod
            = new HashMap<String, List<String>>();

    //The bean interceptor method just caches the interceptor names
    //  in a map which is queried in the getInterceptorNamesFor() method
    @AroundInvoke
    private Object intercept(InvocationContext invCtx)
    	throws Exception {
	System.out.println("Entered aroundInvoke for Bean");
        Map<String, Object> ctxData = invCtx.getContextData();
        List<String> interceptorNameList = (List<String>) ctxData.get(KEY);
        if (interceptorNameList == null) {
            interceptorNameList = new ArrayList<String>();
            ctxData.put(KEY, interceptorNameList);
        }

        //Add this interceptor also to the list of interceptors invoked!!
        interceptorNameList.add("StatelessSessionBean");
        
        //Cache the interceptor name list in a map that can be queried later
        String methodName = invCtx.getMethod().getName();
	synchronized (interceptorNamesForMethod) {
            interceptorNamesForMethod.put(methodName, interceptorNameList);
	}

	return invCtx.proceed();
    }

    // This business method is called after the interceptor methods.
    // Hence it is guaranteed that the argument to this method is not null
    // and it starts with a letter
    public String initUpperCase(String val) {
        String first = val.substring(0, 1);
        return first.toUpperCase() + val.substring(1);
    }

    // This business method is called after the interceptor methods.
    // Hence it is guaranteed that the argument to this method is not null
    // and it starts with a letter
    public String initLowerCase(String val) {
        String first = val.substring(0, 1);
        return first.toLowerCase() + val.substring(1);
    }

    //Note:-
    //  Since this method takes a int as a parameter, the ArgumentChecker
    //  inteceptor is disabled in the ejb-jar.xml for this method.
    public boolean isOddNumber(int val) {
        return ((val % 2) != 0);
    }

    /**
     * Only the default interceptor is used to intercept this method
     */
    public List<String> getInterceptorNamesFor(String methodName) {
        return interceptorNamesForMethod.get(methodName);
    }

}
