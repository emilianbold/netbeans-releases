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

/*
* Support js
*/

function getHttpRequest() {
    var xmlHttpReq;
    try
    {    // Firefox, Opera 8.0+, Safari, IE7.0+
        xmlHttpReq=new XMLHttpRequest();
    }
    catch (e)
    {    // Internet Explorer 6.0+, 5.0+
        try
        {
            xmlHttpReq=new ActiveXObject("Msxml2.XMLHTTP");
        }
        catch (e)
        {
            try
            {
                xmlHttpReq=new ActiveXObject("Microsoft.XMLHTTP");
            }
            catch (e)
            {
                alert("Your browser does not support AJAX!");
            }
        }
    }
    return xmlHttpReq;
 }
  
 function open2(method, url, mimeType, paramLen, async) {
    var xmlHttpReq = getHttpRequest();
    if(xmlHttpReq == null) {
    	//alert('Error: Cannot create XMLHttpRequest');
        return null;
    }
    try {
        netscape.security.PrivilegeManager.enablePrivilege ("UniversalBrowserRead");
    } catch (e) {
        //alert("Permission UniversalBrowserRead denied.");
    }
    try {
        xmlHttpReq.open(method, url, async);
    } catch( e ) {
        //alert('Error: XMLHttpRequest.open failed for: '+strURL+' Error name: '+e.name+' Error message: '+e.message);
        return null;
    }
    if (mimeType != null) {
        if(method == 'GET') {
            //alert("setting GET accept: "+mimeType);
            xmlHttpReq.setRequestHeader('Accept', mimeType);
        } else if(method == 'POST' || method == 'PUT'){
            //alert("setting content-type: "+mimeType);
            //Send the proper header information along with the request
            xmlHttpReq.setRequestHeader("Content-Type", mimeType);
            xmlHttpReq.setRequestHeader("Content-Length", paramLen);
            xmlHttpReq.setRequestHeader("Connection", "close");
        }
    }
    return xmlHttpReq;
}

function loadXml(xmlStr) {
    var doc2;
    // code for IE
    if (window.ActiveXObject)
    {
        doc2=new ActiveXObject("Microsoft.XMLDOM");
        doc2.async="false";
        doc2.loadXML(xmlStr);
    }
    // code for Mozilla, Firefox, Opera, etc.
    else
    {
        var parser=new DOMParser();
        doc2=parser.parseFromString(xmlStr,getDefaultMime());
    }
    return doc2;
}

function findIdFromUrl(u) {
    var li = u.lastIndexOf('/');
    if(li != -1) {
        var u2 = u.substring(0, li);      
        var li2 = u2.lastIndexOf('/');
        u2 = u.substring(0, li2);
        return u.substring(li2+1, li);
    }
    return -1;
}

function get_(url, mime) {
    var xmlHttpReq = open2('GET', url, mime, 0, false);
    xmlHttpReq.send(null);
    try {
      if (xmlHttpReq.readyState == 4) {
          var rtext = xmlHttpReq.responseText;
          if(rtext == undefined || rtext == '' || rtext.indexOf('HTTP Status') != -1) {
              return '-1';
          }
          return rtext;           
       }
    } catch( e ) {
      alert('Caught Exception; name: [' + e.name + '] message: [' + e.message+']');
    }
    return '-1';
}

function post_(url, mime, content) {
  var xmlHttpReq = open2('POST', url, mime, content.length, false);
  xmlHttpReq.send(content);
  try {
       if (xmlHttpReq.readyState == 4) {
           var status = xmlHttpReq.status;
          if(status == 201)
              return true;
       }
  } catch( e ) {
      alert('Caught Exception; name: [' + e.name + '] message: [' + e.message+']');
  }
  return false;
}
   
function put_(url, mime, content) {
    var xmlHttpReq = open2('PUT', url, mime, content.length, false);
    xmlHttpReq.send(content);
    try {
      if (xmlHttpReq.readyState == 4) {
          var status = xmlHttpReq.status;
          if(status == 204)
              return true;
      }
    } catch( e ) {
      alert('Caught Exception; name: [' + e.name + '] message: [' + e.message+']');
    }
    return false;
}

function delete__(url) {
    var xmlHttpReq = open2('DELETE', url, 'application/xml', 0, false);
    xmlHttpReq.send(null);
    try {
      if (xmlHttpReq.readyState == 4) {
          var status = xmlHttpReq.status;
          if(status == 204)
              return true;
      }
    } catch( e ) {
      alert('Caught Exception; name: [' + e.name + '] message: [' + e.message+']');
    }
    return false;
}