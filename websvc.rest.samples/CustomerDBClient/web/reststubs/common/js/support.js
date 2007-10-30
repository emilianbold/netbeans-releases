/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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