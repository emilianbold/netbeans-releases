<%--
 DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

 Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.

 Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 Other names may be trademarks of their respective owners.

 The contents of this file are subject to the terms of either the GNU
 General Public License Version 2 only ("GPL") or the Common
 Development and Distribution License("CDDL") (collectively, the
 "License"). You may not use this file except in compliance with the
 License. You can obtain a copy of the License at
 http://www.netbeans.org/cddl-gplv2.html
 or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 specific language governing permissions and limitations under the
 License.  When distributing the software, include this License Header
 Notice in each file and include the License file at
 nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 particular file as subject to the "Classpath" exception as provided
 by Oracle in the GPL Version 2 section of the License file that
 accompanied this code. If applicable, add the following below the
 License Header, with the fields enclosed by brackets [] replaced by
 your own identifying information:
 "Portions Copyrighted [year] [name of copyright owner]"
 
 Contributor(s):
 
 The Original Software is NetBeans. The Initial Developer of the Original
 Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 Microsystems, Inc. All Rights Reserved.
 
 If you wish your version of this file to be governed by only the CDDL
 or only the GPL Version 2, indicate your decision by adding
 "[Contributor] elects to include this software in this distribution
 under the [CDDL or GPL Version 2] license." If you do not indicate a
 single choice of license, a recipient has the option to distribute
 your version of this file under either the CDDL, the GPL Version 2 or
 to extend the choice of license to its licensees as provided above.
 However, if you add GPL Version 2 code and therefore, elected the GPL
 Version 2 license, then the option applies only if the new code is
 made subject to such option by the copyright holder.
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>REST Client Stubs</title>
        <!-- JS_DECLARE_START - DO NOT REMOVE-->
        <link rel="stylesheet" href="./restclient/ui/css/style.css" type="text/css"/>
        <script type="text/javascript" src="./restclient/ui/customerdbspring.js"></script>
        <script type="text/javascript" src="./restclient/Support.js"></script>
        <script type='text/javascript' src='./restclient/customerdbspring/CustomerDBSpring.js'></script>
	<script type='text/javascript' src='./restclient/customerdbspring/Customers.js'></script>
	<script type='text/javascript' src='./restclient/customerdbspring/DiscountCodes.js'></script>
	<script type='text/javascript' src='./restclient/customerdbspring/Customer.js'></script>
	<script type='text/javascript' src='./restclient/customerdbspring/DiscountCode.js'></script>
        <!-- JS_DECLARE_END - DO NOT REMOVE-->
	<!--<script type="text/javascript">
            //rest debug, uncomment to enable debugging
            var rjsConfig = {
                isDebug: true
            };
	</script>-->
    </head>
    <body>
        <div class="outerBorder">
            <div class="border2">
            <div class="header border2">
                <div class="banner">Customer App</div>
                <!--<div id="subheader" class="subheader"></div>--> <!-- sub-header -->
            </div> <!-- header -->
            <div id="main" class="main">
                <table>
                    <tr>
                        <td id="content" class="content  bkgclr" valign="top">
                            <div id="vw_pl" class="hide">
                                <div class="clr"></div>
                                <div class="heading mglf">Customers</div>
                                <div id="vw_pl_content" class="form-container"></div>
                            </div>
                            <div id="cr_pl" class="hide">
                                <div class="clr"></div>
                                <div class="heading mglf">Create Customer</div>
                                <div id="cr_pl_content" class="form-container"></div>
                            </div>
                            <div id="vw_pl_item" class="hide">
                                <div class="clr"></div>
                                <div class="heading mglf">Customers</div>
                                <div id="vw_pl_item_content" class="form-container"></div>
                                <br/><br/>
                                <div id="add_song_content" class="form-container"></div>
                            </div>
                        </td>
                    </tr>
                </table>
            </div> <!-- main -->
            </div> <!-- border2 -->
        </div> <!-- outerborder -->
        <script language="Javascript">
            showCustomers();
        </script>
    </body>
</html>
