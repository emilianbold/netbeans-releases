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
    this.wadlErr = 'Cannot access WADL: Please restart your RESTful application, and refresh this page.';
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
    this.tcCount = 0;
    this.prettyContent = '';
    this.colSize = "86";
    this.rowSize = "20";
    this.iframeWidth = "530";
    this.iframeHeight = "400";
    this.allcat = [];
    this.projectName = '';
    
    this.expand = new Image();
    this.expand.src = "expand.gif";
    this.collapse = new Image();
    this.collapse.src = "collapse.gif";
    this.og = new Image();
    this.og.src = "og.gif";
    this.cg = new Image();
    this.cg.src = "cg.gif";

    this.viewIds = [
        { "id" : "table" , "name":"Tabular View", "type":"tableContent"}, 
        { "id" : "raw" , "name":"Raw View", "type":"rawContent"}, 
        { "id" : "structure" , "name":"Sub-Resource", "type":"structureInfo"},
        { "id" : "header" , "name":"Headers", "type":"headerInfo"},
        { "id" : "monitor" , "name":"Http Monitor", "type":"monitorContent"}];
    
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
            this.updatepage('content', '<span class=bld>Help Page</span><br/><br/><p>Cannot access WADL: Please restart your REST application, and refresh this page.</p><p>If you still see this error and if you are accessing this page using Firefox with Firebug plugin, then<br/>you need to disable firebug for local files. That is from Firefox menubar, check <br/>Tools > Firebug > Disable Firebug for Local Files</p>');
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
        this.updatepage('paramHook', paramRep);
        document.getElementById("mimeType").value = this.currentMimeType;
        ts.clearOutput();
    },
    
    changeMimeType : function ()
    {
        var mimeNode = document.getElementById("mimeSel");
        var mime = mimeNode.options[mimeNode.selectedIndex].value;
        document.getElementById("mimeType").value = mime;
    },
    
    getMethodMimeTypeCombo : function (resource) {
        var methods = resource.getElementsByTagName('method');
        var str = '<table border=0><tbody><tr><td valign="top"><span id="j_id14"><label for="methodSel" class="LblLev2Txt_sun4">'+
                            '<span>Choose method to test: </span></label></span></td>';
        str += "<td><span id=j_id14><select id='methodSel' class=MnuJmp_sun4 name='methodSel' onchange='javascript:ts.changeMethod();'>";
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
                    str += "  <option class=MnuJmp_sun4 selected value='"+dispName+"["+j+"]' selected>"+dispName+"</option>";
                else
                    str += "  <option class=MnuJmp_sun4 selected value='"+dispName+"["+j+"]'>"+dispName+"</option>";
            }
        }   
        str += "</select></span></td><td width=46/><td><a class='Btn1_sun4 Btn1Hov_sun4' onclick='ts.testResource()'>Test</a></td></tr></tbody></table>";
        return str;
    },
    
    doShowContent : function (uri) {
        this.clearInput();
        var r = this.wdr.findResource(uri);
        if(r != null) {
            var app1 = this.wadlDoc.documentElement;     
            this.currentResource = r;
            this.doShowStaticResource(uri, r);
        } else {
            this.currentResource = null;
            this.doShowDynamicResource(uri, this.wdr.getDefaultMethod(), this.wdr.getDefaultMime());
        }
    },

    doShowContentForId : function (ndx) {
        this.clearInput();
        var cat = ts.allcat[ndx];
        var r = cat.r;
        var uri = cat.uri;
        if(r != null && !ts.wdr.isTemplateResource(r)) {
            var app1 = this.wadlDoc.documentElement;     
            this.currentResource = r;
            this.doShowStaticResource(uri, r);
        } else {
            this.currentResource = null;
            this.doShowDynamicResource(uri, this.wdr.getDefaultMethod(), this.wdr.getDefaultMime());
        }
    },
    
    getPath : function (n, pathVal) {
        if(n.parentNode == null || n.attributes.getNamedItem('path') == null)
            return pathVal.replace(/\/\//g,"\/");
        else {
            var path = n.attributes.getNamedItem('path');
            var pathElem = path.nodeValue;
            pathElem = pathElem.replace(/\/\//g,"\/");
            pathElem = ts.wdr.trimSeperator(pathElem);
            pathElem = ts.wdr.prependSeperator(pathElem);
            return this.getPath(n.parentNode, pathElem+'/'+pathVal);
        }
    },
    
    doShowDynamicResource : function (uri, mName, mediaType) {
        ts.clearOutput();
        paramNumber = 1;
        var qmName = '';
        if(mediaType != null)
            qmName = qmName + "("+mediaType+")";
        else
            mediaType = this.getDefaultMime();
        this.showBreadCrumbs(uri);
        
        var str = '<br/><table border=0><tbody><tr><td valign="top"><span id="j_id14"><label for="methodSel" class="LblLev2Txt_sun4">'+
                            '<span>Choose method to test: </span></label></span></td>';
        str += "<td><span id=j_id14><select id='methodSel' class=MnuJmp_sun4 name='methodSel' onchange='javascript:ts.changeMethod();'>";
        str += "  <option class=MnuJmp_sun4 selected value='GET'>GET</option>";
        str += "  <option class=MnuJmp_sun4 value='PUT'>PUT</option>";
        str += "  <option class=MnuJmp_sun4 value='DELETE'>DELETE</option>";
        str += "</select></span></td>";
        str += '<td valign="top"><span id="j_id14"><label for="methodSel" style="padding-left: 6px;" class="LblLev2Txt_sun4">'+
            '<span>MIME: </span></label></span></td>';
        str += "<td><span id=j_id14><select id='mimeSel' class=MnuJmp_sun4 name='mimeSel' onchange='javascript:ts.changeMimeType();'>";
        str += "  <option class=MnuJmp_sun4 value='application/xml'>application/xml</option>";
        str += "  <option class=MnuJmp_sun4 value='application/json'>application/json</option>";
        str += "  <option class=MnuJmp_sun4 value='text/xml'>text/xml</option>";
        str += "  <option class=MnuJmp_sun4 value='text/plain'>text/plain</option>";
        str += "  <option class=MnuJmp_sun4 value='text/html'>text/html</option>";
        str += "  <option class=MnuJmp_sun4 value='image/*'>image/*</option>"; 
        str += "</select></span></td>";
        str += "<td width=30/>"
        str += "<td><span id=j_id14><a class='Btn2_sun4 Btn1Hov_sun4' onclick='ts.addParam()'>Add Parameter</a>";
        str += "</span></td><td><a class='Btn1_sun4 Btn1Hov_sun4' onclick='ts.testResource()'>Test</a></td></tr></tbody></table><br/>";
        str += this.getFormRep(null, uri, mName, mediaType);
        ts.updatepage('testaction', str);
        var paramRep = "";
        var req = this.getDisplayUri(uri);
        var paths = req.split('/');
        for(var i=0;i<paths.length;i++) {
            var path = paths[i];
            if(path.indexOf('{') > -1) {
                var pname = path.substring(1, path.length-1);
                paramRep += '<td valign="top"><span id="j_id14"><label for="tparams" class="LblLev2Txt_sun4">';
                paramRep += '<span>'+pname+': </span></label></span></td>';
                paramRep += '<td><span id="j_id14"><input id=tparams name="'+pname+'" type=text value="" size=40 title="'+pname+'" class="TxtFld_sun4 TxtFldVld_sun4"/></span></td>';
            }
        }
        if(paramRep != "") {
            paramRep = '<tr><td valign="top"><span id="j_id14"><label for="dummy" class="LblLev2Txt_sun4">'+
                            '<span>Click \'Test\' to continue:</span></label></span></td>'+
                            '<td><span id="j_id14"></span></td></tr>' + paramRep;
            ts.updatepage('paramHook', "<table border=0><tbody><tr>"+paramRep+"</tr></tbody></table>");
        }
        var req = uri;
        var disp = this.getDisplayUri(req);
        var uriLink = "<a id='"+req+"' class=Hyp_sun4 href=javascript:ts.doShowContent('"+req+"') >"+this.getDisplayURL(disp, 80)+"</a>";
        this.updatepage('request', '<span class=bld>Resource:</span> '+uriLink+' <br/>(<a href="'+req+'" class=Hyp_sun4 target="_blank"><span>'+this.getDisplayURL(req, 90)+'</span></a>)');
    },
    
    doShowStaticResource : function (uri, r) {
        ts.clearOutput();
        this.paramNumber = 1;
        this.showBreadCrumbs(uri);
        var mName = this.wdr.getDefaultMethod();
        var mediaType = this.wdr.getDefaultMime();    
        this.updatepage('testaction', '<br/>'+this.getMethodMimeTypeCombo(r)+'<br/>');
        this.updatepage('testinput', this.getFormRep(null, uri, mName, mediaType));
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
        var uriLink = "<a id='"+req+"' class=Hyp_sun4 href=javascript:ts.doShowContent('"+req+"') >"+this.getDisplayURL(disp, 80)+"</a>";
        this.updatepage('request', '<span class=bld>Resource:</span> '+uriLink+' <br/>(<a href="'+req+'" class=Hyp_sun4 target="_blank"><span>'+this.getDisplayURL(req, 90)+'</span></a>)');
    },
    
    getFormRep : function (req, uri, mName, mediaType) {
        if(mName == null || mName == 'undefined')
            mName = this.getDefaultMethod();
        if(mediaType == null || mediaType == 'undefined')
            mediaType = this.getDefaultMime();
        //ts.debug(req + uri + mName + mediaType);
        var str = "<div id='formSubmittal'>";
        str += "<form action='javascript:ts.dummyMethod()' method="+mName+" id='form1' name='form1'>";
        str += "<div id='paramHook'></div>";
        str += "<input name='path' value='"+uri+"' type='hidden'>";
        str += "<input id='methodName' name='methodName' value='"+mName+"' type='hidden'>";
        str += "<input id='mimeType' name='mimeType' value='"+mediaType+"' type='hidden'>";
        str += "</form>";
        str += "</div>";
        return str;
    },
    
    dummyMethod : function() {
    },
    
    addParam : function () {
        var str = '<tr><td valign="top"><span id="j_id14"><label for="newParamNames" class="LblLev2Txt_sun4">';
        str += '<span>param'+paramNumber+': </span></label></span></td>';
        str += '<td><span id="j_id14"><input id=newParamNames name="param'+paramNumber+'" type=text value="param'+paramNumber+'" size=40 title="param'+paramNumber+'" class="TxtFld_sun4 TxtFldVld_sun4"/></span></td></tr>';
        
        str += '<tr><td valign="top"><span id="j_id14"><label for="newParamValues" class="LblLev2Txt_sun4">';
        str += '<span>value'+paramNumber+': </span></label></span></td>';
        str += '<td><span id="j_id14"><input id=newParamValues name="param'+paramNumber+'" type=text value="value'+paramNumber+'" size=40 title="value'+paramNumber+'" class="TxtFld_sun4 TxtFldVld_sun4"/></span></td></tr>';
        var prevParam = document.getElementById("paramHook").innerHTML;
        if(prevParam.indexOf('Additional parameters') == -1) {
            str = '<tr><td valign="top"><span id="j_id14"><label for="dummy" class="LblLev2Txt_sun4">'+
                            '<span>Additional parameters:</span></label></span></td>'+
                            '<td><span id="j_id14"></span></td></tr>'+str;
        }
        document.getElementById("paramHook").innerHTML = prevParam + str;
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

    clearAll : function() {
        this.clearOutput();
        this.updatepage('request', 'Select a node on the navigation bar (on the left side of this page) to test.');
        this.updatepage('testaction', '');
        this.updatepage('testinput', '');
        this.updatepage('navigation', '');
    },
    
    clearInput : function() {
        this.updatepage('paramHook', '');
        var testInput = document.getElementById('testinput');
        testInput.className = 'ConMgn_sun4';
    },
    
    clearOutput : function() {
        this.updatepage('result', '');
        this.updatepage('resultheaders', '');
    },
    
    showBreadCrumbs : function (uri) {
        var disp = this.getDisplayUri(uri);
        this.breadCrumbs[1] = disp;
        var str = "<a class=Hyp_sun4 href=javascript:ts.clearAll() >"+ts.projectName+"</a>";
        var req = this.getDisplayUri(uri);
        var currPath = baseURL;
        if(req.substring(req.length-1) == '/')
            req = req.substring(0, req.length-1);
        var paths = req.split('/');
        for(var i=0;i<paths.length-1;i++) {
            var pname = paths[i];
            if(pname == '')
                continue;
            currPath += '/'+pname;
            var ndx = 0;
            var jsmethod = "ts.doShowContent('"+currPath+"')";
            for(var j=0;j<ts.allcat.length;j++) {
                if(ts.allcat[j].uri == currPath) {
                    ndx = j;
                    jsmethod = "ts.doShowContentForId('"+ndx+"')";
                }
            }
            str += "&nbsp;&gt; <a id='"+currPath+"' class=Hyp_sun4 href=javascript:"+jsmethod+" >"+pname+"</a>";
        }
        str += "<span>&nbsp;&gt; "+paths[paths.length-1]+"</span>";
        this.updatepage('navigation', str);
    },

    getParamRep : function (req, mName) {
        var str = '<table border="0"><tbody>';        
        if(req != null && req.length > 0) {      
            for(var i=0;i<req.length;i++) {
                var params = req[i].childNodes;
                if(params != null) {
                    str += '<tr><td valign="top"><span id="j_id14"><label for="dummy" class="LblLev2Txt_sun4">'+
                            '<span>Click \'Test\' to continue:</span></label></span></td>'+
                            '<td><span id="j_id14"></span></td></tr>';
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
                        str += '<tr><td valign="top"><span id="j_id14"><label for="'+paramsId+'" class="LblLev2Txt_sun4">'+
                            '<span>'+pname+'</span></label></span></td>'+
                            '<td><span id="j_id14"><input type="text" id="'+paramsId+'" name="'+pname+'" value="'+defaultVal+'" size=40 title="'+pname+'" class="TxtFld_sun4 TxtFldVld_sun4"/></span></td></tr>';
                    }
                }
            }
        }
        if(mName == 'PUT' || mName == 'POST') {   
            str += '<tr><td valign="top"><span id="j_id14"><label for="blobParam" class="LblLev2Txt_sun4">'+
                '<span>Content: </span></label></span></td>'+
                '<td><span id="j_id14"><textarea class="TxtAra_sun4 TxtAraVld_sun4" id=blobParam name=params rows=6 cols=65>Insert content here.</textarea></span></td></tr>';
        }
        str += '</tbody></table>';
        return str;
    },
    
    testResource : function () {
        this.updatepage('result', 'Loading...');
        var testInput = document.getElementById('testinput');
        testInput.className = 'ConMgn_sun4 fxdHeight';
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
    
    createIFrameForUrl : function (url) {
        var c = 
            '<iframe id="iFrame_" src="'+url+'" class="frame" width="'+ts.iframeWidth+'" align="left">'+
                '<p>See <a class=Hyp_sun4 href="'+url+'">"'+url+'"</a>.</p>'+
            '</iframe>';
        return c;
    },
    
    createIFrameForContent : function (content) {
        var iframe;
        if (document.createElement && (iframe =
            document.createElement('iframe'))) {
            iframe.name = iframe.id = 'iFrame_';
            iframe.width = ts.iframeWidth;
            iframe.height = ts.iframeHeight;
            iframe.src = 'about:blank';
            document.getElementById('rawContent').appendChild(iframe);
        }
        if (iframe) {
            var iframeDoc;
            if (iframe.contentDocument) {
                iframeDoc = iframe.contentDocument;
            }
            else if (iframe.contentWindow) {
                iframeDoc = iframe.contentWindow.document;
            }
            else if (window.frames[iframe.name]) {
                iframeDoc = window.frames[iframe.name].document;
            }
            if (iframeDoc) {
                iframeDoc.open();
                iframeDoc.write(content);
                iframeDoc.close();
            }
        }
    },
    
    showViews : function (name) {
        var c = '';
        for(var i in this.viewIds) {
            var vid = this.viewIds[i]['id'];
            var tabMain = document.getElementById(this.viewIds[i]['type']);
            if(name == vid) {
                c += this.getTab(vid, true);
                tabMain.style.display="block";
            } else {
                c += this.getTab(vid, false);
                tabMain.style.display="none";
            }
        }
        this.updatepage('tabRow', c);
    },

    monitor : function (xmlHttpReq, param) {
        var nodisp = ' class="nodisp" ';
        var rawViewStyle = ' ';
        var headerViewStyle = nodisp;
        var rawContent = 'Received:\n<br/>'+this.printPretty(xmlHttpReq.responseText)+'\n<br/>';
        if(param != null && param != undefined)
            rawContent = 'Sent:\n<br/>'+this.printPretty(param) + '\n\n<br/><br/>' + rawContent;
        var prev = document.getElementById('monitorText');
        var cURL = this.currentValidUrl;
        var params = '';
        if(cURL.indexOf('?') > 0) {
            params = cURL.substring(cURL.indexOf('?')+1);
            cURL = cURL.substring(0, cURL.indexOf('?')+1);
        }
        var s = 'Request: ' + this.currentMethod + ' ' + cURL + '\n<br/>' + params +
                    '\n\n<br/><br/>Status: ' + xmlHttpReq.status + ' (' + xmlHttpReq.statusText + ')'+
                    '\n\n<br/><br/>Time-Stamp: ' + ' ' + xmlHttpReq.getResponseHeader('Date') + '';
        var prevs = '';
        if(this.currMonitorText != null && this.currMonitorText != undefined) {
            prevs = this.currMonitorText;        
            this.currMonitorText = 
                s + '\n\n<br/><br/>' + rawContent+
                '\n<br/>-----------------------------------------------------------------------\n\n<br/><br/>'+
                prevs;  
        } else {
            this.currMonitorText = s + '\n\n<br/><br/>' + rawContent;
        }
    },

    updateContent : function (content) {
        var showRaw = true;
        if(content != null && content != undefined) {
            if(content == '')
                content = '---No Content---'
            else 
                content = content.replace(/'/g,"\'");
            try {
                var cErr = 'Content may not have Container-Containee Relationship. See Raw View for content.';
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
                if(showRaw) {
                    tableViewStyle = nodisp;
                    rawViewStyle = ' ';
                }
                var structure = this.xhr.options(this.currentValidUrl, 'application/vnd.sun.wadl+xml');
                var subResources = this.getContainerTable(ts.wdr.evaluateWADLUpdate(this.currentValidUrl, structure));
                if(subResources == null)
                    subResources = 'No Sub-Resources available.';
                this.updatepage('result', '<br/><span class=bld>Status:</span> '+ this.currentXmlHttpReq.status+' ('+this.currentXmlHttpReq.statusText+')<br/><br/>'+
                    '<span class=bld>Response:</span> '+
                    '<div class="Tab1Div_sun4">'+
                        '<table cellspacing="0" cellpadding="0" border="0" title="" class="Tab1TblNew_sun4">'+
                            '<tbody>'+
                                '<tr id="tabRow">'+
                                '</tr>'+
                            '</tbody>'+
                        '</table>'+
                    '</div>'+
                    '<div class="tabMain">'+
                    '<div id="menu_bottom" class="stab tabsbottom"></div>'+
                    '<div id="headerInfo"'+nodisp+'>'+this.getHeaderAsTable(this.currentXmlHttpReq)+'</div>'+
                    '<div id="tableContent"'+tableViewStyle+'>'+tableContent+'</div>'+
                    '<div id="structureInfo"'+nodisp+'>'+subResources+'</div>'+
                    '<div id="rawContent"'+rawViewStyle+'>'+this.printPretty(rawContent)+'</div>'+ 
                    '<div id="monitorContent"'+nodisp+'>'+this.currMonitorText+'</div>'+
                    '</div>');
                if(showRaw) {
                    if(content.length > 7 && content.substring(0, 7) == "http://")
                        this.currentValidUrl = content;
                    if(this.currentMethod == 'GET' && this.currentMimeType == 'text/html') {
                        this.updatepage('rawContent', '');
                        this.createIFrameForContent(content);
                    } else if(content.indexOf("<?xml ") != -1) {
                        this.updatepage('rawContent', '');
                        this.updatepage('rawContent', this.printPretty(content));
                    } else {
                        if(this.currentMethod == 'GET')
                            this.updatepage('rawContent', this.createIFrameForUrl(this.currentValidUrl));
                        else
                            this.updatepage('rawContent', content);
                    }
                    this.showViews('raw');
                } else {
                    this.showViews('table');
                }
            } catch( e ) {
                ts.debug('updateContent() err name: [' + e.name + '] message: [' + e.message+"]");
                var c = this.createIFrameForUrl(this.currentValidUrl);
                this.updatepage('result', '<span class=bld>Response:</span> '+c);
                this.updatepage('resultheaders', '<span class=bld>Response Headers:</span> '+this.getHeaderAsTable(this.currentXmlHttpReq));                    
            }  
        }
    },
    
    printPretty : function(content) {
        if(content.indexOf("<?xml ") != -1) {
            var doc2 = this.xhr.loadXml(content);
            if(doc2 != null && doc2.documentElement.nodeName == 'parsererror')
                return content;
            prettyContent = "";
            this.prettyPrint(doc2);
            return prettyContent;
        }
        return content;
    },
    
    prettyPrint : function (/*Node*/ node) {
       var lineBrk = 30;
       printIndented(node, 0);

       function printIndented(/*Node*/ node, /*int*/ indent) {
         if(node.nodeValue != null) {
             prettyContent += node.nodeValue;
         } else {
             var nd = getIndent(indent);
             prettyContent += nd + breakLine(getContent(node, true), this.lineBrk, nd);
             if(node.childNodes != null && node.childNodes.length > 0) {
                 for (var i = 0; i < node.childNodes.length; ++i) {
                   printIndented(node.childNodes[i], indent+2);
                 }
                 if(node.childNodes[0].nodeValue == null)
                    prettyContent += nd + breakLine(getContent(node, false), this.lineBrk, nd);
                 else
                    prettyContent += breakLine(getContent(node, false), this.lineBrk, nd);
             }
         }
       }
       
       function getContent(/*Node*/ n, start) {
         var c = '';
         if(n.nodeValue == null) {//DOM Elements only
            if(n.nodeName == '#document') {
                if(start)
                    c += '&lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF-8&quot;?&gt;    ';
            } else {
                if(start) {
                    c += '&lt;'+n.nodeName;
                    if(n.attributes != null && n.attributes.length > 0) {
                        for (var i = 0; i < n.attributes.length; ++i) {
                           var attr = n.attributes[i];
                           c += ' ' + attr.nodeName + '="' + attr.nodeValue+'"';
                        }
                    }
                    c += '>';
                } else {
                    c += '&lt;/'+n.nodeName+'&gt;';
                }
            }
         } else {
            if(start)
                c += n.nodeValue;
         }
         return c;
       }

       function getIndent(/*int*/ indent) {
         var s = "";
         if(indent < 0)
             return s;
         while (indent) {
           s+=' &nbsp;';
           --indent; 
         }
         return "\n<br/>"+s;
       }
       
       function breakLine(line, len, indent) {
         //var c = breakLine2(line, len, indent);
         var c = line;
         return c;
       }
       
       function breakLine2(line, len, indent) {
         var c = line;
         if(indent.length + c.length > 100) {
             var len2 = 55;
             c = c.substring(0, len2) + indent + '&nbsp;&nbsp;&nbsp;' + breakLine2(c.substring(len2), len, indent);
         }
         return c;
       }
    },

    getTab : function (id, actived) {
        var name = '';
        for(var i in this.viewIds) {
            var vid = this.viewIds[i]['id'];
            if(id == vid) {
                name = this.viewIds[i]['name'];
                break;
            }
        }
        if(actived)
            return '<td class="Tab1TblSelTd_sun4"><div title="Current Selection: Text Field" class="Tab1SelTxtNew_sun4"><a name="selectedTabAnchor" id="tab'+id+'"/>'+name+'</div></td>';
        else {
            if(id == '')
                return '<td style="visibility: hidden;"><a href="javascript:ts.showViews(\''+id+'\')" class="Tab1Lnk_sun4" id="tab'+id+'">'+name+'</a></td>';
            else
                return '<td><a href="javascript:ts.showViews(\''+id+'\')" class="Tab1Lnk_sun4" id="tab'+id+'">'+name+'</a></td>';
        }
    },
    
    getHeaderAsTable : function (xmlHttpReq) { 
        var header = xmlHttpReq.getAllResponseHeaders();
        var colNames = new Array()
        colNames[0] = "Name"
        colNames[1] = "Value"
        var colSizes = new Array()
        colSizes[0] = ""
        colSizes[1] = ""
        var rows = header.split('\r\n');
        if(rows.length == 1)
            rows = header.split('\n');
        var count = 0;
        var str2 = '';
        for(var i=0;i<rows.length;i++) {
            var index = rows[i].indexOf(':');
            var name = rows[i].substring(0, index);
            if(name == '')
                continue;
            count++;
            var val = rows[i].substring(index+1);
            str2 += '<tr><th align="left" scope="row" class="TblTdLyt_sun4" ><span id="j_id9"><span class="">'+name+'</span></span></th>';    
            str2 += '<td align="left" class="TblTdLyt_sun4" ><span id="j_id10"><span class="">'+val+'</span></span></td></tr>';
        }    
        var req = this.currentValidUrl;
        if(req.indexOf('?') > 0)
            req = req.substring(0, req.indexOf('?'));
        var str = '<table width="100%" cellspacing="0" cellpadding="0" border="0" class="Tbl_sun4">'+
                    '<caption class="TblTtlTxt_sun4">'+this.getDisplayUri(req)+' ('+count+')</caption>';
        str += '<tbody><tr>';
        for (i=0;i<colNames.length;i++) {
            str += '<th align="left" scope="col" class="TblColHdr_sun4"><span class="TblHdrTxt_sun4">'+colNames[i]+'</span></th>';
        }
        str += "</tr>";
        str += str2;
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
            colSizes[0] = ""
            colSizes[1] = ""
            var str2 = null;
            if(container != null)
                str2 = this.findUriFromXml(container);
            else
                str2 = this.findUriFromContent(content);
            if(str2 == null || str2 == '')
                return null;
            var req = this.currentValidUrl;
            if(req.indexOf('?') > 0)
                req = req.substring(0, req.indexOf('?'));
            var str = '<table width="100%" cellspacing="0" cellpadding="0" border="0" class="Tbl_sun4">'+
                    '<caption class="TblTtlTxt_sun4">'+this.getDisplayUri(req)+' ('+this.tcCount+')</caption>';
            str += '<tbody><tr>';
            for (i=0;i<colNames.length;i++) {
                str += '<th align="left" scope="col" class="TblColHdr_sun4"><span class="TblHdrTxt_sun4">'+colNames[i]+'</span></th>';
            }
            str += "</tr>";
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
        this.tcCount = 0;
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
        this.tcCount++;
        str += '<tr><th align="left" scope="row" class="TblTdLyt_sun4" ><span id="j_id9"><span class="">'+id+'</span></span></th>';
        var uri = refChild.attributes.getNamedItem('uri').nodeValue;
        str += '<td align="left" class="TblTdLyt_sun4" ><span id="j_id10"><span class="">';
        var disp = this.getDisplayUri(uri);
        var subsUri = this.getSubstitutedUri(uri);
        str += "<a id='"+uri+"' href=javascript:ts.doShowContent('"+uri+"') >"+this.getDisplayURL(disp, 70)+"</a>";
        str += "<br/>(<a href='"+subsUri+"' target='_blank'><span>"+this.getDisplayURL(uri, 70)+"</span></a>)";
        str += '</span></span></td></tr>';
        return str;
    },
    
    getSubstitutedUri : function (uri) {
        var subsUri = uri;
        var ndx = subsUri.indexOf('{');
        if(ndx > 0)
            subsUri = subsUri.substring(0, ndx) + '1' + subsUri.substring(subsUri.indexOf('}', ndx)+1);
        return subsUri;
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
        this.tcCount = uris.length;
        for(var i=0;i<uris.length;i++) {
            var uri = uris[i];
            if(uri.indexOf(baseURL) == 0 && uri.length > cvl) {
                str += '<tr><th align="left" scope="row" class="TblTdLyt_sun4" ><span id="j_id9"><span class="">'+(count++)+'</span></span></th>';    
                str += '<td align="left" class="TblTdLyt_sun4" ><span id="j_id10"><span class="">';
                var disp = this.getDisplayUri(uri);
                str += "<a id='"+uri+"' href=javascript:ts.doShowContent('"+uri+"') >"+this.getDisplayURL(disp, 70)+"</a>";
                str += "<br/>(<a href='"+uri+"' target='_blank'><span>"+this.getDisplayURL(uri, 70)+"</span></a>)";
                str += '</span></span></td></tr>';           
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
        var n = document.getElementById(id);
        if(n != null)
            n.innerHTML = str;
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
            ImageNode.src = ts.expand.src;
            ImageNode1.src = ts.og.src;
        } else {
            ImageNode.src = ts.collapse.src;
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
        ts.updatepage('subheader', '<br/><span class=bld>WADL: </span>'+ts.wadlURL);
        ts.wadlDoc = ts.xhr.loadXml(rtext);
        if(ts.wadlDoc != null) {                
            this.initTree(ts.wadlDoc);
        }
    },
    
    initTree : function (wadlDoc) {
        var myTree = this.createTree(wadlDoc);
        var treeString = myTree.toString();
        ts.updatepage('leftSidebar', treeString);
        this.showCategory('resources');
    },
    
    refreshTree : function (wadlDoc) {
        var myTree = this.createTree(wadlDoc);
        var treeString = myTree.toString();                            
        ts.updatepage('leftSidebar', treeString);
    },
    
    createTree : function (wadlDoc) {
        var app=wadlDoc.documentElement;
        var myTree = new tree();
        var rs;
        if(app != null) {
            rs = app.getElementsByTagName('resources')[0];
            ts.projectName = rs.attributes.getNamedItem('base').nodeValue;
            var begin = ts.projectName.indexOf('/', 7);
            if(begin != -1)
                ts.projectName = ts.projectName.substring(begin, ts.projectName.length);
            var index = ts.projectName.indexOf('/', 1);
            if(ts.projectName.length > 1 && index != -1)
                ts.projectName = ts.projectName.substring(1, index);
            var resources = new category(rs, rs.nodeName, baseURL, ts.projectName);
            myTree.add(resources);
            this.createChildNodes(rs, resources);
        }
        return myTree;
    },

    createChildNodes : function (/*Node*/ node, parentCat) {
       if(node.childNodes != null && node.childNodes.length > 0) {
          for (var i = 0; i < node.childNodes.length; ++i) {
            var n = node.childNodes[i];
            if(ts.wdr.isResource(n) /*&& !isTemplateResource(ch)*/) {
                var pathVal = ts.wdr.getNormailizedPath(n);
                ts.topUrls.push(pathVal);
            }
          } 
       }
       createChildNodes2(node, parentCat);

       function createChildNodes2(/*Node*/ node, parentCat) {
         if(node.nodeValue == null){
             if(node.childNodes != null && node.childNodes.length > 0) {
                 for (var i = 0; i < node.childNodes.length; ++i) {
                    var ch = node.childNodes[i];
                    if(ts.wdr.isResource(ch) /*&& !isTemplateResource(ch)*/) {
                        var n = createNode(ch, parentCat);
                        parentCat.add(n);
                        createChildNodes2(ch, n);
                    }
                 } 
             }
         }
       }
       
       function createNode(/*Node*/ n, parentCat) {  
         var pathVal = ts.wdr.getNormailizedPath(n);
         var pathElem = '';
         if(pathVal.substring(0,1) == '/')
             pathElem = pathVal.substring(1);
         else
             pathElem = pathVal;
         var parentUri = parentCat.uri;
         var uri ='';
         if(parentUri.substring(parentUri.length-1) == '/')
             uri = parentCat.uri+pathElem;
         else
             uri = parentCat.uri+'/'+pathElem;
         var cName = ts.wdr.trimSeperator(pathVal);
         if(ts.wdr.hasResource(n)) {
            return new category(n, pathVal, uri, cName);
         } else {
            var methods = n.getElementsByTagName('method');
            if(methods != null && methods.length > 0) {
                return new item(n, pathVal, uri, cName);
            } else {
                var n2 = ts.wdr.findResource(baseURL+pathVal);
                if(n2 == null) {
                    return new item(n, pathVal, uri, cName);
                } else {
                    if(ts.wdr.hasResource(n2)) {
                        var cat = new category(n, pathVal+'_1', uri, cName);
                        createChildNodes2(n2, cat);
                        return cat;
                    } else {
                        return new item(n2, pathVal, uri, cName);
                    }
                }
            }
         }
       }
    },
    
    getNormailizedPath : function (n) {
        var path = n.attributes.getNamedItem('path');
        var pathVal = path.nodeValue;
        return this.prependSeperator(pathVal);
    },
    
   isResource : function (/*Node*/ n) {  
        if(n.nodeValue == null && n.nodeName == 'resource') {
            return true;
        }
        return false;
   },

   isTemplateResource : function (/*Node*/ n) {
        if (this.getNormailizedPath(n).indexOf('{') > -1) {
            return true;
        }
        return false;
   },

   hasResource : function (/*Node*/ node) {
     if(node.nodeValue == null){
         if(node.childNodes != null && node.childNodes.length > 0) {
             for (var i = 0; i < node.childNodes.length; ++i) {
                var ch = node.childNodes[i];
                if(this.isResource(ch) /*&& !isTemplateResource(ch)*/) {
                    return true;
                }
             } 
         }
     }
     return false;
   },
    
    prependSeperator : function (cName) {
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
            if(cName.substring(cName.length-1, cName.length) == '/')
                cName = cName.substring(0, cName.length-1);
        }
        return cName;
    },

    evaluateWADLUpdate : function (uri, content) {
        
        function nsResolver(prefix) {
            var ns = {
                'xmlns' : 'http://research.sun.com/wadl/2006/10'
            };
            return ns[prefix] || null;
        }

        var xmlDoc = ts.xhr.loadXml(content);
        var iterator = xmlDoc.evaluate('//xmlns:resource', xmlDoc, nsResolver, XPathResult.ORDERED_NODE_ITERATOR_TYPE, null );

        var str = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?><root>';
        try {
          var thisNode = iterator.iterateNext();
          thisNode = iterator.iterateNext();
          while (thisNode) {
            str += '<node uri="'+baseURL+ts.getPath(thisNode, '')+'"/>';
            thisNode = iterator.iterateNext();
          }	
          str += '</root>';
        }
        catch (e) {
          dump( 'Error: Document tree modified during iteration ' + e );
        }
        return str;
    },
    
    showCategory : function (category){
        var categoryChildNodes = document.getElementById(category).style;
        if(categoryChildNodes.display=="block")
            categoryChildNodes.display="none";
        else
            categoryChildNodes.display="block";
        ts.toggleCategory(category);
    },

    updateTree : function (catId){
        if(catId == 'resources') {//return if top level
            this.showCategory('resources');
            return;
        }
        var myTree = this.createTree(ts.wadlDoc);
        ts.updatepage('leftSidebar', myTree.toString());
        childrenContent = '';
        this.getChildren(catId);
        currentCategory = catId;
        setTimeout("ts.wdr.refreshCategory()",1000);
    },

    refreshCategory : function(){
        var catId = currentCategory;
        ts.updatepage(catId, childrenContent);
        this.showCategory('resources');
        this.showCategory(catId);
    },

    //get mediatype from method
    getMediaType : function (m) {
        var mName = m.attributes.getNamedItem("name").nodeValue;
        var request = m.getElementsByTagName('request');
        var response = m.getElementsByTagName('response');
        var mediaType = '';
        var io = request;
        if(mName == 'GET')
            io = response;
        if(io != null && io.length > 0) {
            var rep = io[0].getElementsByTagName('representation');
            if(rep != null) {    
                for(var i=0;i<rep.length;i++) {
                    if(rep[i].attributes.length > 0) {
                        var att = rep[i].attributes.getNamedItem('mediaType');
                        if(att != null)
                            mediaType += att.nodeValue + ',';
                    }
                }
            }
        }
        if(mediaType.length > 1)
            mediaType = mediaType.substring(0, mediaType.length-1);
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
                var rlist = rs.childNodes;
                if(rlist != null && rlist.length > 0) {
                    for(var i=0;i<rlist.length;i++) {
                        var rr = rlist[i];
                        if(rr.nodeValue == null && rr.nodeName == 'resource') {
                            var path = rr.attributes.getNamedItem('path');
                            if(ts.wdr.trimSeperator(path.nodeValue) == ts.wdr.trimSeperator(u)) {
                                return rr;
                            }
                        }
                    }
                }
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

function category(resource, id, uri, text){
    this.r = resource;
    this.id = id;
    this.text = text;

    this.items = [];
    this.ndx = ts.allcat.length;
    ts.allcat.push(this);
    this.uri = uri;
}

category.prototype = {
    write : function (){
        var categoryString = '<span id="nodeSel' + this.id + '" class="category TreeContent_sun4"';
        if(this.uri != baseURL) {
            categoryString += '><img src="cg.gif" id="I1' + this.id + '" onClick="ts.wdr.showCategory(\'' + this.id + '\')">';
            categoryString += '<img src="collapse.gif" id="I' + this.id + '">';
            categoryString += "<div class='item2'><a class=Hyp_sun4 href=javascript:ts.doShowContentForId('"+this.ndx+"') >"+ this.text + "</a></div>";
        } else {
            categoryString += '><img src="cg.gif" id="I1' + this.id + '" onClick="ts.wdr.updateTree(\'' + this.id + '\')">';
            categoryString += '<img src="app.gif" id="I' + this.id + '">';
            categoryString += "<div class='item2'><a class=Hyp_sun4 href=javascript:ts.clearAll() >"+ this.text + "</a></div>";
        }
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

function item(resource, id, uri, text, ndx){
    this.r = resource;
    this.id = id;
    this.text = text;
    this.ndx = ts.allcat.length;
    ts.allcat.push(this);
    this.uri = uri;//baseURL + this.id;
}

item.prototype = {
    write : function (){
        var itemString = '<span class="category TreeContent_sun4"><img src="cc.gif" border="0">';

        itemString += '<img src="item.gif" border="0">';
        if(this.uri != null)
            itemString += "<div class=item1><a class=Hyp_sun4 href=javascript:ts.doShowContentForId('"+this.ndx+"') >"+ this.text + "</a></div>";
        else
            itemString += this.text;
        itemString += '</span>';
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
                    ts.debug("Your browser does not support AJAX!");
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
            if(method == 'GET' || method == 'OPTIONS') {
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
            if (this.isResponseReady(xmlHttpReq, '', true)) {
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
            if (this.isResponseReady(xmlHttpReq, content, true)) {
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
            if (this.isResponseReady(xmlHttpReq, content, true)) {
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
            if (this.isResponseReady(xmlHttpReq, '', true)) {
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
    
    options : function(url, mime) {
        var xmlHttpReq = this.connect('OPTIONS', url, mime, 0, false);
        try {
            xmlHttpReq.send(null);
            if (this.isResponseReady(xmlHttpReq, '', false)) {
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
    
    isResponseReady : function (xmlHttpReq, param, monitor) {
        if (xmlHttpReq.readyState == 4) {
            if(monitor) {
                ts.currentXmlHttpReq = xmlHttpReq;
                ts.monitor(xmlHttpReq, param);
            }
            return true;
        } else
            return false;
    }
}
