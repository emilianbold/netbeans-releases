<!--
  Copyright (c) 2011, Oracle. All rights reserved.

  Redistribution and use in source and binary forms, with or without
  modification, are permitted provided that the following conditions are met:

  * Redistributions of source code must retain the above copyright notice,
    this list of conditions and the following disclaimer.

  * Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

  * Neither the name of Oracle nor the names of its contributors
    may be used to endorse or promote products derived from this software without
    specific prior written permission.

  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
  THE POSSIBILITY OF SUCH DAMAGE.
-->

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <link rel="stylesheet" type="text/css" href="stylesheet.css" />
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">

    </head>
    <body>
        <h1 align="center"> Create a new entry in telephone directory<p/></h1><p/>
        <h3> Fill in the records </h3>

        <form action="AddNewEntry" method="POST">
            <table cellspacing=10>
                <tr>
                    <td><label for="name">Name </label></td>
                    <td><input type="text" name="name" id="name" class="textValue" value="" /></td>
                </tr>
                <tr>
                    <td><label for="address">Address </label></td>
                    <td><input type="text" name="address" id="address" class="textValue" value="" /></td>
                </tr>
                <tr>
                    <td><label for="location">Location </label></td>
                    <td><input type="text" name="location" id="location" class="textValue" value="" /></td>
                </tr>
                <tr>
                    <td><label for="country">Country </label></td>
                    <td><input type="text" name="country" id="country" class="textValue" value="" /></td>
                </tr>
                <tr>
                    <td><label for="phone">Phone Number </label></td>
                    <td><input type="text" name="phone" id="phone" class="textValue" value="" /></td>
                </tr>
                <tr>
                    <td/><td style="align:left"><p/><input type="submit" value="Create">
                        <a href="index.jsp"><input type="button" name="cancel" value="Cancel" /></a></td>
                </tr>
            </table>
            <hr/>
        </form>
        <br>

    </body>
</html>
