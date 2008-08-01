<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>REST Client Stubs</title>
        <!-- JS_DECLARE_START - DO NOT REMOVE-->
        <link rel="stylesheet" href="./css/style.css" type="text/css"/>
        <script type="text/javascript" src="./customerdbclient.js"></script>
        <script type="text/javascript" src="./rest/Support.js"></script>
        <script type='text/javascript' src='./rest/customerdb/CustomerDB.js'></script>
	<script type='text/javascript' src='./rest/customerdb/Customers.js'></script>
	<script type='text/javascript' src='./rest/customerdb/DiscountCodes.js'></script>
	<script type='text/javascript' src='./rest/customerdb/Customer.js'></script>
	<script type='text/javascript' src='./rest/customerdb/DiscountCode.js'></script>
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
