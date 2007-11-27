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
* Supporting js for testing resource beans
*/

function TestSupport() {
    this.wadlDoc = null;
    this.wadlURL = baseURL+"/application.wadl";
    this.wadlErr = 'MSG_TEST_RESBEANS_wadlErr';
    this.currentValidUrl = '';
    this.breadCrumbs = [];
    this.currentMethod = '';
    this.currentMimeType = '';
    this.topUrls = [];
    this.paramNumber = 1;
    this.bcCount = 0;
    this.currMonitorText = null;
    this.childrenContent = '';
    this.currentXmlHttpReq = '';
    this.tcStr = '';
    
    this.expand = new Image();
    this.expand.src = "expand.gif";
    this.collapse = new Image();
    this.collapse.src = "collapse.gif";
    this.og = new Image();
    this.og.src = "og.gif";
    this.cg = new Image();
    this.cg.src = "cg.gif";

    this.viewIds = [
        { "id" : "table" , "value":"MSG_TEST_RESBEANS_TabularView"}, 
        { "id" : "raw" , "value":"MSG_TEST_RESBEANS_RawView"}, 
        { "id" : "header" , "value":"MSG_TEST_RESBEANS_Headers"},
        { "id" : "monitor" , "value":"MSG_TEST_RESBEANS_Monitor"}];
    
    this.xhr = new XHR();
    this.wdr = new WADLParser();
}

TestSupport.prototype = {    

    init : function () {
        this.debug('Initializing scripts...');
        var wadlData = this.xhr.get(this.wadlURL);
        if(wadlData != "-1") {
            this.wdr.updateMenu(wadlData);
        } else {
            this.setvisibility('main', 'inherit');
            document.getElementById('content').innerHTML = 'MSG_TEST_RESBEANS_Help';
        }            
    },
    
    setvisibility : function (id, state) {
        try {
            document.getElementById(id).style.visibility = state;
        } catch(e) {}
    },
    
    changeMethod : function ()
    {    
        var methodNode = document.getElementById("methodSel");
        var method = methodNode.options[methodNode.selectedIndex].value;  
        var mIndex = -1;
        if(method.indexOf('[') != -1) {
            mIndex = method.substring(method.indexOf('[')+1, method.lastIndexOf(']'));
            method = method.substring(0, method.indexOf('['));
        }
        var mimeNode = document.getElementById("mimeSel");
        if(mimeNode == null || mimeNode == undefined) {
            this.currentMimeType = this.wdr.getMimeType(method);
            //ts.debug(currentMimeType);
        }
        this.currentMethod = this.wdr.getMethod(method);
        document.getElementById("methodName").value = this.currentMethod;
        var request = null;
        var resource = this.currentResource;
        if(resource != null && mIndex != -1) {
            var m = resource.getElementsByTagName("method")[mIndex];
            request = m.getElementsByTagName("request");
        }
        var paramRep = this.getParamRep(request, this.currentMethod);
        document.getElementById("paramHook").innerHTML = paramRep;
        document.getElementById("mimeType").value = this.currentMimeType;
        this.updatepage('result', '');
        this.updatepage('resultheaders', '');
    },
    
    changeMimeType : function ()
    {
        var mimeNode = document.getElementById("mimeSel");
        var mime = mimeNode.options[mimeNode.selectedIndex].value;
        document.getElementById("mimeType").value = mime;
    },
    
    getMethodMimeTypeCombo : function (resource) {
        var methods = resource.getElementsByTagName('method');
        var str = "<span class=bld>Method: </span>";
        str += "<select id='methodSel' name='methodSel' onchange='javascript:ts.changeMethod();'>";
        for(var j=0;j<methods.length;j++) {
            var m = methods[j];                            
            var mName = m.attributes.getNamedItem("name").nodeValue;
            var mediaType = this.wdr.getMediaType(m);
            if(mediaType == null)
                mediaType = this.wdr.getDefaultMime();
            var mimeTypes = mediaType.split(',');
            for(var k=0;k<mimeTypes.length;k++) {
                var mimeType = mimeTypes[k];
                var dispName = this.wdr.getMethodNameForDisplay(mName, mimeType);
                if(mName == 'GET')
                    str += "  <option selected value='"+dispName+"["+j+"]' selected>"+dispName+"</option>";
                else
                    str += "  <option selected value='"+dispName+"["+j+"]'>"+dispName+"</option>";
            }
        }   
        str += "</select>";
        return str;
    },
    
    doShowContent : function (uri) {
        var r = this.wdr.findResource(uri);
        if(r != null) {
            var app1 = this.wadlDoc.documentElement;
            var rs = app1.getElementsByTagName('resources')[0];        
            this.currentResource = r;
            this.doShowStaticResource(uri, r);
        } else {
            this.currentResource = null;
            this.doShowDynamicResource(uri, this.wdr.getDefaultMethod(), this.wdr.getDefaultMime());
        }
    },
    
    doShowDynamicResource : function (uri, mName, mediaType) {
        this.updatepage('result', '');
        this.updatepage('resultheaders', '');
        paramNumber = 1;
        var qmName = '';
        if(mediaType != null)
            qmName = qmName + "("+mediaType+")";
        else
            mediaType = this.getDefaultMime();
        this.showBreadCrumbs(uri);
        var str = "<span class=bld>Method: </span>";
        str += "<select id='methodSel' name='methodSel' onchange='javascript:ts.changeMethod();'>";
        str += "  <option selected value='GET'>GET</option>";
        str += "  <option value='PUT'>PUT</option>";
        str += "  <option value='DELETE'>DELETE</option>";
        str += "</select>";
        str += "&nbsp;&nbsp;<span class=bld>MIME: </span>";
        str += "<select id='mimeSel' name='mimeSel' onchange='javascript:ts.changeMimeType();'>";
        str += "  <option value='application/xml'>application/xml</option>";
        str += "  <option value='application/json'>application/json</option>";
        str += "  <option value='text/xml'>text/xml</option>";
        str += "  <option value='text/plain'>text/plain</option>";
        str += "  <option value='text/html'>text/html</option>";    
        str += "</select>";
        str += "&nbsp;&nbsp;<input value='MSG_TEST_RESBEANS_AddParamButton' type='button' onclick='ts.addParam()'>";
        str += "<br/><br/>";
        str += this.getFormRep(null, uri, mName, mediaType);
        document.getElementById('testres').innerHTML = str;
        var req = uri;
        var disp = this.getDisplayUri(req);
        var uriLink = "<a id='"+req+"' href=javascript:ts.doShowContent('"+req+"') >"+this.getDisplayURL(disp, 80)+"</a>";
        this.updatepage('request', '<span class=bld>Resource:</span> '+uriLink+' <br/>(<a href="'+req+'" target="_blank"><span class=font10>'+this.getDisplayURL(req, 90)+'</span></a>)<hr>');
    },
    
    doShowStaticResource : function (uri, r) {
        this.updatepage('result', '');
        this.updatepage('resultheaders', '');
        this.paramNumber = 1;
        this.showBreadCrumbs(uri);
        var mName = this.wdr.getDefaultMethod();
        var mediaType = this.wdr.getDefaultMime();    
        var str = this.getMethodMimeTypeCombo(r);
        str += "<br/><br/>";
        str += this.getFormRep(null, uri, mName, mediaType);
        document.getElementById('testres').innerHTML = str;
        var methodNode = document.getElementById("methodSel");
        var options = methodNode.options;
        for(var i=0;i<options.length;i++) {
            if(options[i].value.substring(0, 3) == 'GET') {
                methodNode.selectedIndex = i;
            }
        }
        this.changeMethod();    
        var req = uri;
        var disp = this.getDisplayUri(req);
        var uriLink = "<a id='"+req+"' href=javascript:ts.doShowContent('"+req+"') >"+this.getDisplayURL(disp, 80)+"</a>";
        this.updatepage('request', '<span class=bld>Resource:</span> '+uriLink+' <br/>(<a href="'+req+'" target="_blank"><span class=font10>'+this.getDisplayURL(req, 90)+'</span></a>)<hr>');
    },
    
    getFormRep : function (req, uri, mName, mediaType) {
        if(mName == null || mName == 'undefined')
            mName = this.getDefaultMethod();
        if(mediaType == null || mediaType == 'undefined')
            mediaType = this.getDefaultMime();
        //ts.debug(req + uri + mName + mediaType);
        var str = "<div id='formSubmittal'>";
        str += "<form action='' method="+mName+" id='form1' name='form1'>";
        str += "<div id='paramHook'></div>";
        str += "<input name='path' value='"+uri+"' type='hidden'>";
        str += "<input id='methodName' name='methodName' value='"+mName+"' type='hidden'>";
        str += "<input id='mimeType' name='mimeType' value='"+mediaType+"' type='hidden'>";
        str += "<br/><input value='MSG_TEST_RESBEANS_TestButton' type='button' onclick='ts.testResource()'>";
        str += "</form>";
        str += "</div>";
        return str;
    },
    
    addParam : function () {
        var str = "<span class=bld>MSG_TEST_RESBEANS_NewParamName:&nbsp;</span>"+
            "<input id='newParamNames' name='param"+paramNumber+"' type='text' value='param"+paramNumber+"' size='25'>"+
            "&nbsp;&nbsp;&nbsp;<span class=bld>MSG_TEST_RESBEANS_NewParamValue:&nbsp;</span>"+
            "<input id='newParamValues' name='value"+paramNumber+"' type='text' value='value"+paramNumber+"' size='25'>"+"<br>";
        var prevParam = document.getElementById("paramHook").innerHTML;
        document.getElementById("paramHook").innerHTML = prevParam + "<br>" + str;
        this.saveFormInput('form1', 'resttest-');
        paramNumber++;
    },
    
    setCookie : function (name, value, expires, path, domain, secure) {
        var today = new Date();
        today.setTime( today.getTime() );
        if(expires) {
            expires = expires * 1000 * 60 * 60 * 24;
        }
        var expires_date = new Date( today.getTime() + (expires) );
        document.cookie = name+"="+escape( value ) +
            ( ( expires ) ? ";expires="+expires_date.toGMTString() : "" ) +
            ( ( path ) ? ";path=" + path : "" ) +
            ( ( domain ) ? ";domain=" + domain : "" ) +
            ( ( secure ) ? ";secure" : "" );
    },
    
    getCookie : function ( name ) {
        var start = document.cookie.indexOf( name + "=" );
        var len = start + name.length + 1;
        if((!start) && (name != document.cookie.substring(0, name.length))) {
            return null;
        }
        if(start == -1) 
            return null;
        var end = document.cookie.indexOf( ";", len );
        if(end == -1) 
            end = document.cookie.length;
        return unescape(document.cookie.substring(len, end));
    },

    saveFormInput : function (form_id, pfx) {
        var form = document.getElementById(form_id);
        var els = document.getElementsByTagName('input');
        for (var i = 0; i < els.length; i++) {
            var el = els.item(i);
            if (el.type == 'text') {
                el.onblur = function() {
                    var name = this.name;
                    var value = this.value;
                    ts.setCookie( pfx + name, value);
                };
                var old_value = this.getCookie(pfx + el.name);
                if (old_value && old_value != '') {
                    el.value = old_value;
                }
            }
        }
    },

    showBreadCrumbs : function (uri) {
        var nav = document.getElementById('navigation');
        var disp = this.getDisplayUri(uri);
        var count = 0;
        for(var i=0;i<this.breadCrumbs.length;i++) {
            if(this.breadCrumbs[i] == disp) {
                count++;
            }
        }
        if(count == 0) {
            this.breadCrumbs[this.breadCrumbs.length+1] = disp;
            var uriLink = "<a id='"+uri+"' href=javascript:ts.doShowContent('"+uri+"') >"+this.getDisplayURL(disp, 90)+"</a>";
            if(nav.innerHTML != '') {
                this.bcCount++;
                var uriPerLine = 4;
                if(Math.round(this.bcCount/uriPerLine) == this.bcCount/uriPerLine)
                    nav.innerHTML = nav.innerHTML + " , <br/>" + uriLink;
                else
                    nav.innerHTML = nav.innerHTML + " , " + uriLink;
            } else
                nav.innerHTML = uriLink;
        }
    },

    getParamRep : function (req, mName) {
        var str = "";
        if(req != null && req.length > 0) {       
            //ts.debug(req.length);             
            for(var i=0;i<req.length;i++) {
                var params = req[i].childNodes;
                if(params != null) {
                    for(var j=0;j<params.length;j++) {
                        var param = params[j];
                        if(param.nodeName == null || param.nodeName != 'param')
                            continue;
                        var pname = param.attributes.getNamedItem('name').nodeValue;
                        var defaultVal = '';
                        if(param.attributes.getNamedItem('default') != null)
                            defaultVal = param.attributes.getNamedItem('default').nodeValue;
                        var type = 'query';
                        if(param.attributes.getNamedItem('style') != null)
                            type = param.attributes.getNamedItem('style').nodeValue;
                        var paramsId = 'qparams';
                        if(type == 'template')
                            paramsId = 'tparams';
                        else if(type == 'matrix')
                            paramsId = 'mparams';
                        str += "<span class=bld>"+pname+":</span>"+"<input id='"+paramsId+"' name='"+pname+"' type='text' value='"+defaultVal+"'>"+"<br><br>";
                    }
                }
            }
        }
        if(mName == 'PUT' || mName == 'POST')
            str += "<span class=bld>Content:</span>: <br><textarea id='blobParam' name='params' rows='6' cols='70'>MSG_TEST_RESBEANS_Insert</textarea><br/>";       
        if(str != "")
            str = "<span class=bld>MSG_TEST_RESBEANS_ResourceInputs</span><br><br><div class='ml20'>"+str+"</div><br/>";
        return str;
    },
    
    testResource : function () {
        this.updatepage('result', 'MSG_TEST_RESBEANS_Loading');
        var mimetype = this.getFormMimeType();
        var method = this.getFormMethod();
        var p = '';
        var path = document.forms[0].path.value;
        
        //filter template parameters that show up on the path
        var tps = document.forms[0].tparams;
        var tparams = [];
        var found = path.indexOf( "{" );
        if (found != -1){
            if(tps != null) {
                if(tps.length == undefined) {
                    if(path.indexOf("{"+tps.name+"}"))
                        path = path.replace("{"+tps.name+"}", tps.value);
                    else
                        tparams.push(tps);
                } else {
                    var len = tps.length;
                    for(var j=0;j<len;j++) {
                        var param = tps[j];
                        if(path.indexOf("{"+param.name+"}"))
                            path = path.replace("{"+param.name+"}", param.value);
                        else
                            tparams.push(param);
                    }
                }
            }
        }
        
        var qparams = document.forms[0].qparams;
        if(qparams != null) {
            if(qparams.length == undefined) {
                p += qparams.name+"="+qparams.value;
            } else {
                var len = qparams.length;
                for(var j=0;j<len;j++) {
                    var param = qparams[j]
                    if(len == 1 || len-j == 1)
                            p += escape(param.name)+"="+escape(param.value);
                    else
                            p += escape(param.name)+"="+escape(param.value)+"&";
                }
            }
        }

        //process user added parameters
        var newParamNames = document.forms[0].newParamNames;
        var newParamValues = document.forms[0].newParamValues;
        if(newParamNames != null) {
            if(newParamNames.length == undefined) {
                p += newParamNames.value+"="+newParamValues.value;
            } else {
                var len = newParamNames.length;
                for(var j=0;j<len;j++) {
                    var paramName = newParamNames[j].value;
                    var paramValue = newParamValues[j].value;
                    if(len == 1 || len-j == 1)
                        p += escape(paramName)+"="+escape(paramValue);
                    else
                        p += escape(paramName)+"="+escape(paramValue)+"&";
                }
            }
        }
        
        var params = null;
        var paramLength = 0;
        if(method == 'POST' || method == 'PUT'){
            var blobParam = document.getElementById('blobParam').value;
            if(blobParam != null && blobParam != undefined)
                params = blobParam;
            else if(p != null && p != undefined)
                params = p;
            if(params != null)
                paramLength = params.length;
        } else if(method == 'GET' || method == 'DELETE') {
            paramLength = 0;
        }
        var req;
        if(path.indexOf('http:') != -1)
            req = path;
        else
            req = baseURL+escape(path);
        
        //change url if there are template params
        if(tparams != null) {
            if(tparams.length == undefined) {
                req += "/" + escape(tparams.value);
            } else {
                var len = tparams.length;
                for(var j=0;j<len;j++) {
                    req += "/" + escape(tparams[j].value);
                }
            }
        }

        if(method == 'GET' && p.length > 0)
            req+= "?"+p;
        
        //process matrix parameters
        var mparams = document.forms[0].mparams;
        if(mparams != null) {
            if(mparams.length == undefined) {
                req += ";"+escape(mparams.name)+"="+escape(mparams.value);
            } else {
                var len = mparams.length;
                for(var j=0;j<len;j++) {
                    var param = mparams[j]
                    req += ";"+escape(param.name)+"="+escape(param.value);
                }
            }
        }
        
        var disp = this.getDisplayUri(req);
        this.currentMethod = method;
        this.currentMimeType = mimetype;
        
        //add timestamp to make url unique in case of IE7
        var timestamp = new Date().getTime();
        if(req.indexOf("?") != -1)
            req = req+"&timestamp="+timestamp;
        else
            req = req+"?timestamp="+timestamp;
        
        var c = '';
        if(method == 'POST') {
            c = this.xhr.post(req, mimetype, params);
        } else if(method == 'PUT') {
            c = this.xhr.put(req, mimetype, params);
        } else if(method == 'GET') {
            c = this.xhr.get(req, mimetype);
        } else if(method == 'DELETE') {
            c = this.xhr.delete_(req);
        }
        ts.updateContent(c);
    },
    
    createIFrame : function (url) {
        var c = '<iframe src="'+url+'" class="frame" width="600" height="400" align="left">'+
            '<p>See <a href="'+url+'">"'+url+'"</a>.</p>'+
            '</iframe>';
        return c;
    },
    
    showViews : function (name) {
        if(name == 'raw' && this.currentMethod == 'GET' && this.currentMimeType == 'text/html')
            this.updatepage('rawContent', this.createIFrame(this.currentValidUrl));
        var tableNode = document.getElementById('tableContent').style;
        var rawNode = document.getElementById('rawContent').style;
        var headerNode = document.getElementById('headerInfo').style;
        var monitorNode = document.getElementById('monitorContent').style;
        var tabs1 = document.getElementById('table').style;
        var tabs2 = document.getElementById('raw').style;
        var tabs3 = document.getElementById('header').style;
        var tabs4 = document.getElementById('monitor').style;
        if(name == 'table') {
            tableNode.display="block";
            rawNode.display="none";
            headerNode.display="none";
            monitorNode.display="none";
            tabs1.display="block";
            tabs2.display="none";
            tabs3.display="none";
            tabs4.display="none";
        } else if(name == 'raw') {
            tableNode.display="none";
            rawNode.display="block";
            headerNode.display="none";
            monitorNode.display="none";
            tabs2.display="block";
            tabs3.display="none";
            tabs4.display="none";
            tabs1.display="none";
        } else if(name == 'header') {
            tableNode.display="none";
            rawNode.display="none";
            headerNode.display="block";
            monitorNode.display="none";
            tabs3.display="block";
            tabs4.display="none";
            tabs1.display="none";
            tabs2.display="none";
        } else if(name == 'monitor') {
            tableNode.display="none";
            rawNode.display="none";
            headerNode.display="none";
            monitorNode.display="block";
            tabs4.display="block";
            tabs1.display="none";
            tabs2.display="none";
            tabs3.display="none";
        }
    },

    monitor : function (xmlHttpReq, param) {
        var nodisp = ' class="nodisp" ';
        var rawViewStyle = ' ';
        var headerViewStyle = nodisp;
        var rawContent = 'Received:\n'+xmlHttpReq.responseText+'\n';
        if(param != null && param != undefined)
            rawContent = 'Sent:\n'+param + '\n\n' + rawContent;
        var prev = document.getElementById('monitorText');
        var cURL = this.currentValidUrl;
        var s = 'Request: ' + this.currentMethod + ' ' + cURL + 
                    '\n\nStatus: ' + xmlHttpReq.status + ' (' + xmlHttpReq.statusText + ')'+
                    '\n\nTimeStamp: ' + ' ' + xmlHttpReq.getResponseHeader('Date') + '';
        var prevs = '';
        if(this.currMonitorText != null && this.currMonitorText != undefined) {
            prevs = this.currMonitorText;        
            this.currMonitorText = 
                s + '\n\n' + rawContent+
                '\n-----------------------------------------------------------------------\n\n'+
                prevs;  
        } else {
            this.currMonitorText = s + '\n\n' + rawContent;
        }
    },

    updateContent : function (content) {
        var showRaw = true;
        if(content != null && content != undefined) {
            if(content == '')
                content = 'MSG_TEST_RESBEANS_NoContents'
            else 
                content = content.replace(/'/g,"\'");
            try {
                //ts.debug(content);
                var cErr = '<table border=1><tr><td class=tableW>Content may not have Container-Containee Relationship. See Raw View for content.</td></tr></table>';
                var tableContent = cErr;
                if(content.indexOf("<?xml ") != -1 || 
                        content.indexOf('{"') != -1) {
                    var tc = this.getContainerTable(content);
                    if(tc != null) {
                        tableContent = tc;
                        showRaw = false;
                    }
                }
                var rawContent = content;
                var tableViewStyle = ' ';
                var nodisp = ' class="nodisp" ';
                var rawViewStyle = nodisp;
                var headerViewStyle = nodisp;
                var monitorViewStyle = nodisp;
                if(showRaw) {
                    tableViewStyle = nodisp;
                    rawViewStyle = ' ';
                    headerViewStyle = nodisp;
                    monitorViewStyle = nodisp;
                }
                this.updatepage('result', '<span class=bld>MSG_TEST_RESBEANS_Status</span> '+ this.currentXmlHttpReq.status+' ('+this.currentXmlHttpReq.statusText+')<br/><br/>'+
                    '<span class=bld>MSG_TEST_RESBEANS_Content</span> '+
                    this.getTab('table', tableViewStyle)+this.getTab('raw', rawViewStyle)+
                    this.getTab('header', headerViewStyle)+this.getTab('monitor', monitorViewStyle)+                     
                    '<div id="menu_bottom" class="stab tabsbottom"></div>'+
                    '<div id="headerInfo"'+headerViewStyle+'>'+this.getHeaderAsTable(this.currentXmlHttpReq)+'</div>'+
                    '<div id="tableContent"'+tableViewStyle+'>'+tableContent+'</div>'+
                    '<div id="rawContent"'+rawViewStyle+'>'+
                        '<textarea rows=15 cols=72 align=left readonly>'+rawContent+'</textarea></div>'+ 
                    '<div id="monitorContent"'+monitorViewStyle+'>'+
                        '<textarea id="monitorText" rows=15 cols=72 align=left readonly>'+this.currMonitorText+'</textarea></div>');
                if(showRaw) {
                    if(content.length > 7 && content.substring(0, 7) == "http://")
                        this.currentValidUrl = content;
                    this.showViews('raw');
                } else {
                    this.showViews('table');
                }
            } catch( e ) {
                ts.debug('updateContent() err name: [' + e.name + '] message: [' + e.message+"]");
                var c = this.createIFrame(this.currentValidUrl);
                this.updatepage('result', '<span class=bld>MSG_TEST_RESBEANS_Content</span> '+c);
                this.updatepage('resultheaders', '<span class=bld>MSG_TEST_RESBEANS_ResponseHeaders</span> '+this.getHeaderAsTable(this.currentXmlHttpReq));                    
            }  
        }
    },

    getTab : function (id, style) {
        var c = '<div id="'+id+'"'+style+'><table class="result"><tr>';
        var style = 'otab';
        for(var i in this.viewIds) {
            var vid = this.viewIds[i]['id'];
            if(id == vid)
                style = 'stab';
            else
                style = 'otab';
            c += '<td class="tab '+style+'"><a href="javascript:ts.showViews(\''+
                vid+'\')" class="tab"><span class="stext">'+this.viewIds[i]['value']+'</span></a></td>';
        }
        c += '</tr></table></div>';
        return c;
    },
    
    getHeaderAsTable : function (xmlHttpReq) { 
        var header = xmlHttpReq.getAllResponseHeaders();    
        var str = "<table class='results' border='1'>";
        str += "<thead class='resultHeader'>";
        var colNames = new Array()
        colNames[0] = "MSG_TEST_RESBEANS_HeaderName"
        colNames[1] = "MSG_TEST_RESBEANS_HeaderValue"
        var colSizes = new Array()
        colSizes[0] = "class='tableW1 lfa'"
        colSizes[1] = "class='tableW2 lfa'"
        for (i=0;i<colNames.length;i++) {
            str += "<th width='"+colSizes[i]+"'><span class='bld wht'>"+colNames[i]+"</span></th>";
        }
        str += "</thead>";
        str += "<tbody>";
        var rows = header.split('\r\n');
        if(rows.length == 1)
            rows = header.split('\n');
        var count = 0;
        for(var i=0;i<rows.length;i++) {
            var index = rows[i].indexOf(':');
            var name = rows[i].substring(0, index);
            if(name == '')
                continue;
            count++;
            var val = rows[i].substring(index+1);
            str += "<tr class='font9'>";
            str += "<td>"+name+"</td>";
            str += "<td>"+val+"</td>";
            str += "</tr>";
        }    
        if(count == 0)
            str = "<textarea rows=15 cols=72 align=left readonly>MSG_TEST_RESBEANS_NoHeaders</textarea>";
        else
            str += "</tbody></table>";
        return str;
    },

    getContainerTable : function (content) {
        if(content == null)
            return null;
        var ret = null;    
        var container = null;
        try {
            if(content.indexOf("<?xml ") != -1) {
                var doc2 = this.xhr.loadXml(content);
                if(doc2 != null && doc2.documentElement.nodeName == 'parsererror')
                    return null;
                container=doc2.documentElement;
                if(container == null || container.nodeName == 'html')
                    return null;
            }
            var colNames = new Array()
            colNames[0] = "ID"
            colNames[1] = "URI"
            var colSizes = new Array()
            colSizes[0] = "class='tableW1 lfa'"
            colSizes[1] = "class='tableW2 lfa'"
            var str = "<table class='results' border='1'>";
            str += "<thead class='resultHeader'>";
            for (i=0;i<colNames.length;i++) {
                str += "<th "+colSizes[i]+"><span class='bld wht'>"+colNames[i]+"</span></th>";
            }
            str += "</thead>";
            str += "<tbody id='containerTable'>";
            var str2 = null;
            if(container != null)
                str2 = this.findUriFromXml(container);
            else
                str2 = this.findUriFromContent(content);
            if(str2 == null || str2 == '')
                return null;
            str += str2;
            str += "</tbody></table>";
            ret = str;
        } catch(e) {
            ts.debug('getContainerTable() err name: [' + e.name + '] message: [' + e.message+"]");
            return null;
        }

        return ret;
    },
    
    findUriFromXml : function (container) {
        this.tcStr = '';
        this.getChildUriFromXml(container);
        return this.tcStr;
    },

    getChildUriFromXml : function (refChild) {
        if(refChild == null)
            return;
        var subChilds = refChild.childNodes;
        if(subChilds == null || subChilds.length == 0)
            return;
        var j = 0;
        for(j=0;j<subChilds.length;j++) {
            var subChild = subChilds[j];            
            if(subChild.nodeValue == null) {//DOM Elements only
                if(subChild.attributes != null && subChild.attributes.length > 0 && 
                    subChild.attributes.getNamedItem('uri') != null) {
                    this.tcStr += this.createRowForUriFromXml(subChild);
                }
                this.getChildUriFromXml(subChild);
            }
        }
    },
    
    createRowForUriFromXml : function (refChild) {
        var str = '';    
        var id = '-';
        if(refChild.childNodes != null && refChild.childNodes.length > 0 && 
                refChild.childNodes[0].childNodes != null && 
                    refChild.childNodes[0].childNodes.length > 0) {
            id = refChild.childNodes[0].childNodes[0].nodeValue;
        }
        if(id == null)
            id = '-';
        str += "<tr>";    
        str += "<td>"+id+"</td>";
        var uri = refChild.attributes.getNamedItem('uri').nodeValue;
        str += "<td>";
        var disp = this.getDisplayUri(uri);
        str += "<a id='"+uri+"' href=javascript:ts.doShowContent('"+uri+"') >"+this.getDisplayURL(disp, 70)+"</a>";
        str += "<br/>(<a href='"+uri+"' target='_blank'><span class=font10>"+this.getDisplayURL(uri, 70)+"</span></a>)";
        str += "</td>";
        str += "</tr>";
        return str;
    },
    
    findUriFromContent : function (content) {
        if(content == null || content == '')
            return '';
        var c = content.replace(/\\\//g,"/");   
        var uris = c.split('\"');
        var str = '';
        var count = 1;
        var cvl = this.currentValidUrl.indexOf("?");
        if(cvl == -1)
            cvl = this.currentValidUrl.length;
        for(var i=0;i<uris.length;i++) {
            var uri = uris[i];
            if(uri.indexOf(baseURL) == 0 && uri.length > cvl) {
                str += "<tr>";    
                str += "<td>"+(count++)+"</td>";
                str += "<td>";
                var disp = this.getDisplayUri(uri);
                str += "<a id='"+uri+"' href=javascript:ts.doShowContent('"+uri+"') >"+this.getDisplayURL(disp, 70)+"</a>";
                str += "<br/>(<a href='"+uri+"' target='_blank'><span class=font10>"+this.getDisplayURL(uri, 70)+"</span></a>)";
                str += "</td>";
                str += "</tr>";            
            }            
        }
        return str;
    },
    
    getDisplayUri : function (uri) {
        var disp = uri;
        if(disp.length > baseURL.length)
            disp = disp.substring(baseURL.length);
        return disp;
    },
    
    getDisplayURL : function (url, len) {
        return url.substring(0, len);
    },
    
    updatepage : function (id, str){
        document.getElementById(id).innerHTML = str;
    },
    
    getFormMimeType : function () {
        var resource = document.getElementById('mimeType');
        if(resource != null)
            return this.wdr.getMimeType(resource.value);
        else
            return this.wdr.getDefaultMime();
    },
    
    getFormMethod : function () {
        var resource = document.getElementById('methodName');
        if(resource != null)
            return this.wdr.getMethod(resource.value);
        else
            return this.wdr.getDefaultMethod();
    },

    getItemString : function (name, uri){
        var itemString = '<img src="cc.gif" border="0">';
        itemString += '<img src="item.gif" border="0">';
        itemString += '<a href="javascript:ts.doShowContent(\''+uri+'\')">';
        itemString += name;
        itemString += '</a><br>';
        return itemString;
    },

    toggleCategory : function (img){
        var ImageNode = document.getElementById('I' + img);
        var ImageNode1 = document.getElementById('I1' + img);
        if(ImageNode1.src.indexOf('cg.gif')>-1) {
            //ImageNode.src = expand.src;
            ImageNode1.src = ts.og.src;
        } else {
            //ImageNode.src = collapse.src;
            ImageNode1.src = ts.cg.src;
        }
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
            var addActionStr = '<div style="'+tab+'"><a style="text-decoration: none" href="javascript:ts.closeDebug()"><span style="color: red">X</span></a></div>';        
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

function WADLParser() {    
}

WADLParser.prototype = {
    updateMenu : function (rtext) {                                
        if(rtext == undefined || rtext == "" || rtext.indexOf("HTTP Status") != -1) {   
            var newUrl = prompt(ts.wadlErr, baseURL);
            if(newUrl != null && baseURL != newUrl) {
                baseURL = newUrl;
                ts.wadlURL = baseURL+"/application.wadl";
                ts.init();
            }
            return;
        }
        ts.setvisibility('main', 'inherit');
        document.getElementById('subheader').innerHTML = '<br/><span class=bld>WADL: </span>'+ts.wadlURL;
        ts.wadlDoc = ts.xhr.loadXml(rtext);
        if(ts.wadlDoc != null) {                
            this.initTree(ts.wadlDoc);
        }
    },
    
    initTree : function (wadlDoc) {
        var myTree = this.createTree(wadlDoc);
        var treeString = myTree.toString();
        document.getElementById('leftSidebar').innerHTML = treeString;
        this.showCategory('resources');
    },
    
    refreshTree : function (wadlDoc) {
        var myTree = this.createTree(wadlDoc);
        var treeString = myTree.toString();                            
        document.getElementById('leftSidebar').innerHTML = treeString;
    },
    
    createTree : function (wadlDoc) {
        var app=wadlDoc.documentElement;
        var myTree = new tree();
        var rs;
        if(app != null) {
            rs = app.getElementsByTagName('resources')[0];
            var context = rs.attributes.getNamedItem('base').nodeValue;
            var begin = context.indexOf('/', 7);
            if(begin != -1)
                context = context.substring(begin, context.length);
            var index = context.indexOf('/', 1);
            if(context.length > 1 && index != -1)
                context = context.substring(1, index);
            var resources = new category(rs.nodeName, context);
            myTree.add(resources);
            var rarr = rs.getElementsByTagName('resource');
            for(var i=0;i<rarr.length;i++) {
                var r = rarr[i];   
                var path = r.attributes.getNamedItem('path');
                var pathVal = path.nodeValue;
                pathVal = this.addSeperator(pathVal);
                var cName = this.trimSeperator(pathVal);
                ts.topUrls[i] = pathVal;
                var start = new item(pathVal, cName, i);
                resources.add(start);
            }
        }
        return myTree;
    },
    
    addSeperator : function (cName) {
        if(cName != null) {
            if(cName.substring(0, 1) != '/')
                cName = '/' + cName;
        }
        return cName;
    },
    
    trimSeperator : function (cName) {
        if(cName != null) {
            if(cName.substring(0, 1) == '/')
                cName = cName.substring(1);
            //ts.debug(cName.substring(cName.length-1, cName.length));
            if(cName.substring(cName.length-1, cName.length) == '/')
                cName = cName.substring(0, cName.length-1);
        }
        return cName;
    },

    showCategory : function (category){
        var categoryNode = document.getElementById(category).style;
        if(categoryNode.display=="block")
            categoryNode.display="none";
        else
            categoryNode.display="block";
        ts.toggleCategory(category);
    },

    updateTree : function (catId){
        //ts.debug(catId);
        if(catId == 'resources') {//return if top level
            showCategory('resources');
            return;
        }
        var myTree = this.createTree(wadlDoc);
        document.getElementById('leftSidebar').innerHTML = myTree.toString();
        childrenContent = '';
        this.getChildren(catId);
        currentCategory = catId;
        setTimeout("this.refreshCategory()",1000);
    },

    refreshCategory : function(){
        var catId = currentCategory;
        var categoryNode = document.getElementById(catId);
        categoryNode.innerHTML = childrenContent;
        this.showCategory('resources');
        this.showCategory(catId);
    },

    //get mediatype from method
    getMediaType : function (m) {
        var mName = m.attributes.getNamedItem("name").nodeValue;
        var request = m.getElementsByTagName('request');
        var response = m.getElementsByTagName('response');
        var mediaType = null;
        var io = request;
        if(mName == 'GET')
            io = response;
        if(io != null && io.length > 0) {
            var rep = io[0].getElementsByTagName('representation');
            if(rep != null && rep.length > 0) {                        
                if(rep[0].attributes.length > 0) {
                    var att = rep[0].attributes.getNamedItem('mediaType');
                    if(att != null)
                        mediaType = att.nodeValue
                }
            }
        }
        return mediaType;
    },
    
    findResource : function (uri) {
        var r = null;
        var len = baseURL.length;
        if(uri.length > len) {
            var u = uri.substring(len, uri.length);
            var ri = this.lookupIndex(u);
            if(ri == -1) {//look for reference resource
                var li = u.lastIndexOf('/');
                if(li != -1) {
                    var u2 = u.substring(0, li);
                    var li2 = u2.lastIndexOf('/');
                    u = u.substring(li2, u.length);
                    ri = this.lookupIndex(u);
                }
            }
            if(ri > -1) {
                var app1 = ts.wadlDoc.documentElement;
                var rs = app1.getElementsByTagName('resources')[0];
                r = rs.getElementsByTagName('resource')[ri];
            }
        }
        return r;
    },
    
    lookupIndex : function (u)
    {
        var ri = -1;
        for(var i=0;i<ts.topUrls.length;i++) {
            if(ts.topUrls[i] == u) {
                ri = i;
                break;
            }
        }
        return ri;
    },
    
    getMethodNameForDisplay : function (mName, mediaType) {
        var m = mName;
        if(mediaType == null && (mName == 'PUT' || mName == 'POST'))
            mediaType = getDefaultMime();
        if(mediaType != null)
            m += '(' + mediaType + ')';    
        return m;
    },
    
    getDefaultMime : function () {
        return "application/xml";
    },
    
    getDefaultMethod : function () {
        return "GET";
    },
    
    getMimeType : function (mime) {
        if(mime != null) {
            var i = mime.indexOf('(');
            if(i == -1) {
                if(mime == 'GET' || mime == 'POST' || mime == 'PUT' || mime == 'DELETE')
                    return getDefaultMime();
                else
                    return mime;
            } else
                return mime.substring(i+1, mime.length-1);
        } else
            return getDefaultMime();
    },
    
    getMethod : function (method) {
        if(method != null) {
            var i = method.indexOf('(');
            if(i == -1)
                return method;
            else
                return method.substring(0, i);
        } else
            return this.getDefaultMethod();
    },

    getChildren : function (uri) {
        var content = ts.xhr.get(baseURL+uri, this.getDefaultMime());
        this.getChildrenContent(content);
    },

    getChildrenContent : function (content) {
        if(content != -1) {
            var ret = this.getChildrenAsItems(content);
            childrenContent = ret;
        } else {
            childrenContent = '';
        }
    },

    getChildrenAsItems : function (xmlStr) {
        var ret = xmlStr;  
        if(ret == null)
            return '';
        var doc2 = null;
        try {
            doc2 = ts.xhr.loadXml(ret);
        } catch(e) { 
            ts.debug('getChildrenAsItems()->loadXml() err name: [' + e.name + '] message: [' + e.message+"]");
            return null;
        }
        if(doc2 != null && doc2.documentElement.nodeName != 'parsererror') {
            try {
                var container=doc2.documentElement;
                if(container == null || container.nodeName == 'html')
                    return ret;
                var str = "";
                var refs = container.childNodes;
                var count = 0;
                for(var i=0;i<refs.length;i++) {
                    var refsChild = refs[i];
                    if(refsChild.nodeValue == null) {//DOM Elements only
                        var ref = refsChild;             
                        var refChilds = ref.childNodes;
                        for(var j=0;j<refChilds.length;j++) {
                            var refChild = refChilds[j];
                            if(refChild.nodeValue == null) {//DOM Elements only
                                var id = refChild;
                                if(ref.attributes != null && ref.attributes.length > 0 && 
                                        ref.attributes.getNamedItem('uri') != null) {
                                    var uri = ref.attributes.getNamedItem('uri').nodeValue;
                                    var idval = id.childNodes[0].nodeValue;
                                    var disp = this.getDisplayUri(uri);
                                    str += this.getItemString(idval, uri);
                                }
                            }
                        }
                        count++;
                    }
                }
                ret = str;
            } catch(e) {
                ts.debug('getChildrenAsItems() err name: [' + e.name + '] message: [' + e.message+"]");
                return null;
            }
        }
        return ret;
    }
}

function tree(){
    this.categories = [];
}

tree.prototype = {
    add : function (category){
        this.categories[this.categories.length] = category;
    },

    list : function (){
        return this.categories;
    },

    toString : function (){
        var treeString = '';
        for(var i=0;i<this.categories.length;i++) {
            treeString += this.categories[i].write();
        }
        return treeString;
    }
}

function category(id, text, ndx){
    this.id = id;
    this.text = text;
    this.ndx = ndx;
    this.items = [];
}

category.prototype = {
    write : function (){
        var uri = baseURL + this.id;
        if(this.id == 'resources')
            uri = null;
        var categoryString = '<span class="category"';
        categoryString += '><img src="cg.gif" id="I1' + this.id + '" onClick="updateTree(\'' + this.id + '\')">';
        categoryString += '<img src="app.gif" id="I' + this.id + '">';
        if(uri != null)
            categoryString += "<div class='item2'><a href=javascript:ts.doShowContent('"+uri+"') >"+ this.text + "</a></div>";
        else
            categoryString += "<div class='item2'>"+this.text+"</div>";
        categoryString += '</span>';
        categoryString += '<span class="item" id="';
        categoryString += this.id + '">';
        var numitems = this.items.length;
        for(var j=0;j<numitems;j++)
            categoryString += this.items[j].write();
        categoryString += '</span>';
        return categoryString;
    },

    add : function (item){
        this.items[this.items.length] = item;
    },

    list : function (){
        return this.items;
    }
}

function item(id, text, ndx){    
    this.id = id;
    this.text = text;
    this.ndx = ndx;
}

item.prototype = {
    write : function (){
        var uri = baseURL + this.id;
        var itemString = '<img src="cc.gif" border="0">';
        itemString += '<img src="item.gif" border="0">';
        if(uri != null)
            itemString += "<a href=javascript:ts.doShowContent('"+uri+"') >"+ this.text + "</a>";
        else
            itemString += this.text;
        itemString += '<br>';
        return itemString;
    }
}

function XHR() {
}
 
XHR.prototype = {
    getHttpRequest : function() {
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
                    ts.debug("MSG_TEST_RESBEANS_No_AJAX");
                }
            }
        }
        return xmlHttpReq;
     },

    connect : function (method, url, mimeType, paramLen, async) {
        var xmlHttpReq = this.getHttpRequest();
        if(xmlHttpReq == null) {
            ts.debug('Error: Cannot create XMLHttpRequest');
            return null;
        }
        try {
            netscape.security.PrivilegeManager.enablePrivilege ("UniversalBrowserRead");
        } catch (e) {
            //ts.debug('connect(): Permission UniversalBrowserRead denied. err name: [' + e.name + '] message: [' + e.message+']');
        }
        try {
            xmlHttpReq.open(method, url, async);
        } catch( e ) {
            ts.debug('connect(): Error: XMLHttpRequest.open failed for: '+url+' Error name: '+e.name+' Error message: '+e.message);
            return null;
        }
        if (mimeType != null) {
            if(method == 'GET') {
                //ts.debug("setting GET accept: "+mimeType);
                xmlHttpReq.setRequestHeader('Accept', mimeType);
            } else if(method == 'POST' || method == 'PUT'){
                //ts.debug("setting content-type: "+mimeType);
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
        
        ts.currentValidUrl = url;
        return xmlHttpReq;
    },

    get : function(url, mime) {
        var xmlHttpReq = this.connect('GET', url, mime, 0, false);
        try {
            xmlHttpReq.send(null);
            if (this.isResponseReady(xmlHttpReq, '')) {
              var rtext = xmlHttpReq.responseText;
              if(rtext == undefined || rtext == '' || rtext.indexOf('HTTP Status') != -1) {
                  var err = 'Get failed: Server returned --> Status: (' + status+')\n'+
                      'Response: {' + xmlHttpReq.responseText + "}";
                  ts.debug('Failed XHR(GET, '+url+'): '+err);
                  return err;
              }
              return rtext;           
            }
        } catch( e ) {
           ts.debug('get(): Caught Exception; name: [' + e.name + '] message: [' + e.message+']');
        }
        return '-1';
    },

    post : function(url, mime, content) {
        var xmlHttpReq = this.connect('POST', url, mime, content.length, false);
        try {
            xmlHttpReq.send(content);
            if (this.isResponseReady(xmlHttpReq, content)) {
                var status = xmlHttpReq.status;
                if(status != 201) {
                  var err = 'Post failed: Server returned --> Status: (' + status+')\n'+
                      'Response: {' + xmlHttpReq.responseText + "}";
                  ts.debug('Failed XHR(POST, '+url+'): '+err);
                  return err;
                }
            }
        } catch( e ) {
          ts.debug('post(): Caught Exception; name: [' + e.name + '] message: [' + e.message+']');
        }
        return 'Post succeeded for: '+url+'. Server returned: '+xmlHttpReq.responseText;
    },

    put : function(url, mime, content) {
        var xmlHttpReq = this.connect('PUT', url, mime, content.length, false);
        try {
            xmlHttpReq.send(content);
            if (this.isResponseReady(xmlHttpReq, content)) {
              var status = xmlHttpReq.status;
              if(status != 204) {
                  var err = 'Put failed: Server returned --> Status: (' + status+')\n'+
                      'Response: {' + xmlHttpReq.responseText + "}";
                  ts.debug('Failed XHR(PUT, '+url+'): '+err);
                  return err;
              }
            }
        } catch( e ) {
          ts.debug('put(): Caught Exception; name: [' + e.name + '] message: [' + e.message+']');
        }
        return 'Put succeeded for: '+url+'. Server returned: '+xmlHttpReq.responseText;
    },

    delete_ : function(url) {
        var xmlHttpReq = this.connect('DELETE', url, 'application/xml', 0, false);
        try {
            xmlHttpReq.send(null);  
            if (this.isResponseReady(xmlHttpReq, '')) {
              var status = xmlHttpReq.status;
              if(status != 204) {
                  var err = 'Delete failed: Server returned --> Status: (' + status+')\n'+
                      'Response: {' + xmlHttpReq.responseText + "}";
                  ts.debug('Failed XHR(DELETE, '+url+'): '+err);
                  return err;
              }
            }
        } catch( e ) {
          ts.debug('delete(): Caught Exception; name: [' + e.name + '] message: [' + e.message+']');
        }
        return 'Delete succeeded for: '+url+'. Server returned: '+xmlHttpReq.responseText;
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
            doc2=parser.parseFromString(xmlStr,ts.wdr.getDefaultMime());
        }
        return doc2;
    },
    
    isResponseReady : function (xmlHttpReq, param) {
        if (xmlHttpReq.readyState == 4) {
            ts.currentXmlHttpReq = xmlHttpReq;
            ts.monitor(xmlHttpReq, param);
            return true;
        } else
            return false;
    }
}
