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


/*
* Support js
*/


var rjsSupport = {

    proxy : "",
    
    getHttpProxy: function() {
        return this.proxy;
    },
    
    setHttpProxy: function(proxy_) {
        this.proxy = proxy_;
    },
    
    isSetHttpProxy: function() {
        return this.getHttpProxy().length > 0;
    },

    getHttpRequest: function() {
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
                    this.debug("Your browser does not support AJAX!");
                }
            }
        }
        return xmlHttpReq;
     },
    
     findUrl : function(url, method) {
        var url2 = url;
        if(this.isSetHttpProxy())
            url2 = this.getHttpProxy()+"?method="+method+"&url="+url2;
        return url2;
     },

     findMethod : function(method) {
        var method2 = method;
        if(method != "GET" && this.isSetHttpProxy())
            method2 = "POST";
        return method2;
     },

     open : function(method2, url2, mimeType, paramLen, async) {

        //Change url and method if using http proxy
        var url = this.findUrl(url2, method2);
        var method = this.findMethod(method2);

        //add timestamp to make url unique in case of IE7
        var timestamp = new Date().getTime();
        if(url.indexOf("?") != -1)
            url = url+"&timestamp="+timestamp;
        else
            url = url+"?timestamp="+timestamp;

        var xmlHttpReq = this.getHttpRequest();
        if(xmlHttpReq == null) {
            this.debug('Error: Cannot create XMLHttpRequest');
            return null;
        }
        try {
            netscape.security.PrivilegeManager.enablePrivilege ("UniversalBrowserRead");
        } catch (e) {
            //this.debug("Permission UniversalBrowserRead denied.");
        }
        try {
            xmlHttpReq.open(method, url, async);
        } catch( e ) {
            this.debug('Error: XMLHttpRequest.open failed for: '+url+' Error name: '+e.name+' Error message: '+e.message);
            return null;
        }
        if (mimeType != null) {
            if(method == 'GET') {
                //this.debug("setting GET accept: "+mimeType);
                xmlHttpReq.setRequestHeader('Accept', mimeType);
            } else if(method == 'POST' || method == 'PUT'){
                //this.debug("setting content-type: "+mimeType);
                //Send the proper header information along with the request
                xmlHttpReq.setRequestHeader("Content-Type", mimeType);
                xmlHttpReq.setRequestHeader("Content-Length", paramLen);
                xmlHttpReq.setRequestHeader("Connection", "close");
            }
        }
        //For cache control on IE7
        xmlHttpReq.setRequestHeader("Cache-Control", "no-cache");
        xmlHttpReq.setRequestHeader("Pragma", "no-cache");
        xmlHttpReq.setRequestHeader("Expires", "-1");

        return xmlHttpReq;
    },

    loadXml : function(xmlStr) {
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
    },

    findIdFromUrl : function(u) {
        var li = u.lastIndexOf('/');
        if(li != -1) {
            var u2 = u.substring(0, li);      
            var li2 = u2.lastIndexOf('/');
            u2 = u.substring(0, li2);
            return u.substring(li2+1, li);
        }
        return -1;
    },

    get : function(url, mime) {
        var xmlHttpReq = this.open('GET', url, mime, 0, false);
        try {
          xmlHttpReq.send(null);
          if (xmlHttpReq.readyState == 4) {
              var rtext = xmlHttpReq.responseText;
              if(rtext == undefined || rtext == '' || rtext.indexOf('HTTP Status') != -1) {
                  if(rtext != undefined)
                      this.debug('Failed XHR(GET, '+url+'): Server returned --> ' + rtext);
                  return '-1';
              }
              return rtext;           
           }
        } catch( e ) {
          this.debug('Caught Exception; name: [' + e.name + '] message: [' + e.message+']');
        }
        return '-1';
    },

    post : function(url, mime, content) {
        var xmlHttpReq = this.open('POST', url, mime, content.length, false);
        try {
            xmlHttpReq.send(content);
            if (xmlHttpReq.readyState == 4) {
                var status = xmlHttpReq.status;
                if(status == 201) {
                    return true;
                } else {
                    this.debug('Failed XHR(POST, '+url+'): Server returned --> ' + status);
                }
            }
        } catch( e ) {
          this.debug('Caught Exception; name: [' + e.name + '] message: [' + e.message+']');
        }
        return false;
    },

    put : function(url, mime, content) {
        var xmlHttpReq = this.open('PUT', url, mime, content.length, false);
        try {
          xmlHttpReq.send(content);
          if (xmlHttpReq.readyState == 4) {
              var status = xmlHttpReq.status;
              if(status == 204) {
                  return true;
              } else {
                  this.debug('Failed XHR(PUT, '+url+'): Server returned --> ' + status);
              }
          }
        } catch( e ) {
          this.debug('Caught Exception; name: [' + e.name + '] message: [' + e.message+']');
        }
        return false;
    },

    delete_ : function(url) {
        var xmlHttpReq = this.open('DELETE', url, 'application/xml', 0, false);
        try {
          xmlHttpReq.send(null);
          if (xmlHttpReq.readyState == 4) {
              var status = xmlHttpReq.status;
              if(status == 204) {
                  return true;
              } else {
                  this.debug('Failed XHR(DELETE, '+url+'): Server returned --> ' + status);
              }
          }
        } catch( e ) {
          this.debug('Caught Exception; name: [' + e.name + '] message: [' + e.message+']');
        }
        return false;
    },

    debug : function(message) {
        var dbgComp = document.getElementById("dbgComp");
        if(dbgComp == null) {
            dbgComp = document.createElement("div");
            dbgComp.setAttribute("id", "dbgComp");
            dbgComp.style.border = "#2574B7 1px solid";
            dbgComp.style.font = "12pt/14pt sans-serif";
            var br = document.createElement("div");
            document.getElementsByTagName("body")[0].appendChild(br);
            br.innerHTML = '<br/><br/><br/>';
            document.getElementsByTagName("body")[0].appendChild(dbgComp);
            if((typeof rjsConfig!="undefined") && rjsConfig.isDebug) {
                dbgComp.style.display = "";
            } else {
                dbgComp.style.display = "none";
            }
            var tab = 'width: 20px; border-right: #2574B7 1px solid; border-top: #2574B7 1px solid; border-left: #2574B7 1px solid; border-bottom: #2574B7 1px solid; color: #000000; text-align: center;';
            var addActionStr = '<div style="'+tab+'"><a style="text-decoration: none" href="javascript:rjsSupport.closeDebug()"><span style="color: red">X</span></a></div>';        
            dbgComp.innerHTML = '<table><tr><td><span style="color: blue">Rest Debug Window</span></td><td>'+addActionStr + '</td></tr></table><br/>';
        }
        var s = dbgComp.innerHTML;
        var now = new Date();
        var dateStr = now.getHours()+':'+now.getMinutes()+':'+now.getSeconds();
        dbgComp.innerHTML = s + '<span style="color: red">rest debug('+dateStr+'): </span>' + message + "<br/>";
    },
    
    closeDebug : function() {
        var dbgComp = document.getElementById("dbgComp");
        if(dbgComp != null) {
            dbgComp.style.display = "none";
            dbgComp.innerHTML = '';
        }
    }
}
