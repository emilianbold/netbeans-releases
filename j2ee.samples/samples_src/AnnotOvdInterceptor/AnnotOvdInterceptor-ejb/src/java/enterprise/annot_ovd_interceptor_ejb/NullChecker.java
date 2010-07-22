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

import java.lang.reflect.Method;

import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

/**
 * This is the default interceptor
 */
public class NullChecker {

    @AroundInvoke
    public Object checkIfNull(InvocationContext ctx)
        throws Exception {

        Map<String, Object> ctxData = ctx.getContextData();
        List<String> interceptorNameList = (List<String>)
            ctxData.get("interceptorNameList");
        if (interceptorNameList == null) {
            interceptorNameList = new ArrayList<String>();
            ctxData.put("interceptorNameList", interceptorNameList);
        }

        //Now add this interceptor name to the list
        interceptorNameList.add("NullChecker");

        Method method = ctx.getMethod();
        Object param = ctx.getParameters()[0];
        if (param == null) {
            // An interceptor can throw any runtime exception or
            // application exceptions that are allowed in the
            // throws clause of the business method
            throw new BadArgumentException("Illegal argument: null");
        }

        // Proceed to the next interceptor OR business method
        return ctx.proceed();
    }

}
