<%--
	/*
	 * Copyright (c) 2007, Sun Microsystems, Inc. All rights reserved.
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
	 * * Neither the name of Sun Microsystems, Inc. nor the names of its contributors
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
--%>
<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>

<%@taglib prefix="f" uri="http://java.sun.com/jsf/core"%>
<%@taglib prefix="h" uri="http://java.sun.com/jsf/html"%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Create Account</title>
    </head>
    <body>

    <h1>Create a New Account</h1>
    
    <f:view>
        <h:form id="create">            
            <h:panelGrid columns="3" border="0">
                First Name: <h:inputText id="fname"       
                                         requiredMessage="*"
                                         value="#{usermanager.fname}"
                                         required="true"/>  
                            <h:message for="create:fname" style="color: red"/>
                Last Name: <h:inputText id="lname"  
                                        requiredMessage="*"
                                        value="#{usermanager.lname}"
                                        required="true"/>
                           <h:message for="create:lname" style="color: red"/>
                Username: <h:inputText id="username" 
                                       requiredMessage="*"
                                       value="#{usermanager.username}"
                                       required="true"/>
                          <h:message for="create:username" style="color: red"/>
                Password: <h:inputSecret id="password"    
                                         requiredMessage="*"
                                         value="#{usermanager.password}"
                                         required="true"/>
                          <h:message for="create:password" style="color: red"/>
                Password (verify): <h:inputSecret id="passwordv"   
                                                  requiredMessage="*"
                                                  value="#{usermanager.passwordv}"
                                                  required="true"/>
                                   <h:message for="create:passwordv" style="color: red"/>
            </h:panelGrid>
            <h:commandButton id="submit" 
                             value="Create"
                             action="#{usermanager.createUser}"/>
            <h:messages style="color: red" globalOnly="true"/>
        </h:form>
    </f:view>
    
    </body>
</html>
