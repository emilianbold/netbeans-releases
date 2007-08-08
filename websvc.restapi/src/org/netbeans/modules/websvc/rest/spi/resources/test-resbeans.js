/*
* Supporting js for testing resource beans
*/
var wadlDoc;            
var app;
var wadlURL = baseURL+"/application.wadl";  
var wadlErr = 'MSG_TEST_RESBEANS_wadlErr';
var currentUrl;
var currentValidUrl;
var breadCrumbs = new Array();
var currentContainer;
var currentMethod;
var currentMimeType;
var treeHook;
var myTree;
var topUrls = new Array();
var paramNumber = 1;

var expand = new Image();
expand.src = "expand.gif";
var collapse = new Image();
collapse.src = "collapse.gif";
var og = new Image();
og.src = "og.gif";
var cg = new Image();
cg.src = "cg.gif";

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
                alert("MSG_TEST_RESBEANS_No_AJAX");
            }
        }
    }
    return xmlHttpReq;
 }
 function open(method, url, mimeType, paramLen, async) {
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
    log("mimeType: "+mimeType);
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
    currentValidUrl = url;
    return xmlHttpReq;
}
function updateMenu(xmlHttpReq) {                
    try {
        if (xmlHttpReq.readyState == 4) {
            var rtext = xmlHttpReq.responseText;                   
            if(rtext == undefined || rtext == "" || rtext.indexOf("HTTP Status") != -1) {   
                var newUrl = prompt(wadlErr, baseURL);
                if(newUrl != null && baseURL != newUrl) {
                    baseURL = newUrl;
                    wadlURL = baseURL+"/application.wadl";
                    init();
                }
                return;
            }
            setvisibility('main', 'inherit');
            document.getElementById('subheader').innerHTML = '<br/><span class=bld>WADL: </span>'+wadlURL;
            wadlDoc = loadXml(rtext);
            if(wadlDoc != null) {                
                initTree(wadlDoc);
            }
        } else {
            log("state: "+xmlHttpReq.readyState);
        }
    } catch( e ) {
        alert('Caught Exception; name: [' + e.name + '] message: [' + e.message+']');
    }
}
function initTree(wadlDoc) {
    var myTree = createTree(wadlDoc);
    var treeString = myTree.toString();
    document.getElementById('leftSidebar').innerHTML = treeString;
    showCategory('resources');
}
function refreshTree(wadlDoc) {
    var myTree = createTree(wadlDoc);
    var treeString = myTree.toString();                            
    document.getElementById('leftSidebar').innerHTML = treeString;
}
function createTree(wadlDoc) {
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
        for(i=0;i<rarr.length;i++) {
            var r = rarr[i];   
            var path = r.attributes.getNamedItem('path');
            var pathVal = path.nodeValue;
            var cName = trimSeperator(pathVal);
            topUrls[i] = pathVal;
            var start = new item(pathVal, cName, i);
            resources.add(start);
        }
    }
    return myTree;
}
function trimSeperator(cName) {
    if(cName != null) {
        if(cName.substring(0, 1) == '/')
            cName = cName.substring(1);
        //alert(cName.substring(cName.length-1, cName.length));
        if(cName.substring(cName.length-1, cName.length) == '/')
            cName = cName.substring(0, cName.length-1);
    }
    return cName;
}
//get mediatype from method
function getMediaType(m) {
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
}
function setvisibility(id, state) {
    try {
        document.getElementById(id).style.visibility = state;
    } catch(e) {}
}
function changeMethod()
{    
    var methodNode = document.getElementById("methodSel");
    var method = methodNode.options[methodNode.selectedIndex].value;
    var mimeNode = document.getElementById("mimeSel");
    if(mimeNode == null || mimeNode == undefined) {
        currentMimeType = getMimeType(method);
        //alert(currentMimeType);
    }
    currentMethod = getMethod(method);
    var formSubmittal = document.getElementById("formSubmittal");
    if(formSubmittal != null) {
        var content = formSubmittal.innerHTML;
        var index = content.indexOf('method');
        if(index != -1) {
            var index2 = content.indexOf('name');
            formSubmittal.innerHTML = content.substring(0, index)+" method='"+currentMethod+"' "+content.substring(index2);
        }
    }
    document.getElementById("method").value = currentMethod;
    var request = null;
    var resource = currentResource;
    if(resource != null) {
        var m = resource.getElementsByTagName("method")[methodNode.selectedIndex];
        request = m.getElementsByTagName("request");
    }
    var paramRep = getParamRep(request, currentMethod);
    document.getElementById("paramHook").innerHTML = paramRep;
    document.getElementById("mimeType").value = currentMimeType;
    //alert(formSubmittal.innerHTML);
    updatepage('result', '');
    updatepage('resultheaders', '');
};
function changeMimeType()
{
    var mimeNode = document.getElementById("mimeSel");
    var mime = mimeNode.options[mimeNode.selectedIndex].value;
    document.getElementById("mimeType").value = mime;
};
function getMethodMimeTypeCombo(resource) {
    var methods = resource.getElementsByTagName('method');
    var str = "<span class=bld>Method: </span>";
    str += "<select id='methodSel' name='methodSel' onchange='javascript:changeMethod();'>";
    for(j=0;j<methods.length;j++) {
        var m = methods[j];                            
        var mName = m.attributes.getNamedItem("name").nodeValue;
        var mediaType = getMediaType(m);
        var mimeTypes = mediaType.split(',');
        var k=0;
        for(k=0;k<mimeTypes.length;k++) {
            var mimeType = mimeTypes[k];
            var dispName = getMethodNameForDisplay(mName, mimeType);
            if(mName == 'GET')
                str += "  <option selected value='"+dispName+"' selected>"+dispName+"</option>";
            else
                str += "  <option selected value='"+dispName+"'>"+dispName+"</option>";
        }
    }   
    str += "</select>";
    return str;
}
function getMethodNameForDisplay(mName, mediaType) {
    var m = mName;
    if(mediaType == null && (mName == 'PUT' || mName == 'POST'))
        mediaType = getDefaultMime();
    if(mediaType != null)
        m += '(' + mediaType + ')';    
    return m;
}
function getDefaultMime() {
    return "application/xml";
}
function getDefaultMethod() {
    return "GET";
}
function findResource(uri) {
    var r = null;
    var len = baseURL.length;
    if(uri.length > len) {
        var u = uri.substring(len, uri.length);
        var ri = lookupIndex(u);
        if(ri == -1) {//look for reference resource
            var li = u.lastIndexOf('/');
            if(li != -1) {
                var u2 = u.substring(0, li);
                var li2 = u2.lastIndexOf('/');
                u = u.substring(li2, u.length);
                ri = lookupIndex(u);
            }
        }
        if(ri > -1) {
            var app1 = wadlDoc.documentElement;
            var rs = app1.getElementsByTagName('resources')[0];
            r = rs.getElementsByTagName('resource')[ri];
        }
    }
    return r;
}
function lookupIndex(u)
{
    var ri = -1;
    for(i=0;i<topUrls.length;i++) {
        if(topUrls[i] == u) {
            ri = i;
            break;
        }
    }
    return ri;
}
function doShowContent(uri) {
    var r = findResource(uri);
    if(r != null) {
        var app1 = wadlDoc.documentElement;
        var rs = app1.getElementsByTagName('resources')[0];        
        currentResource = r;
        doShowStaticResource(uri, r);
    } else {
        currentResource = null;
        doShowDynamicResource(uri, getDefaultMethod(), getDefaultMime());
    }
}
function doShowDynamicResource(uri, mName, mediaType) {
    updatepage('result', '');
    updatepage('resultheaders', '');
    paramNumber = 1;
    var qmName = '';
    if(mediaType != null)
        qmName = qmName + "("+mediaType+")";
    else
        mediaType = getDefaultMime();
    showBreadCrumbs(uri);
    var str = "<span class=bld>Method: </span>";
    str += "<select id='methodSel' name='methodSel' onchange='javascript:changeMethod();'>";
    str += "  <option selected value='GET'>GET</option>";
    str += "  <option value='PUT'>PUT</option>";
    str += "  <option value='DELETE'>DELETE</option>";
    str += "</select>";
    str += "&nbsp;&nbsp;<span class=bld>MIME: </span>";
    str += "<select id='mimeSel' name='mimeSel' onchange='javascript:changeMimeType();'>";
    str += "  <option value='application/xml'>application/xml</option>";
    str += "  <option value='application/json'>application/json</option>";
    str += "  <option value='text/xml'>text/xml</option>";
    str += "  <option value='text/plain'>text/plain</option>";
    str += "  <option value='text/html'>text/html</option>";    
    str += "</select>";
    str += "&nbsp;&nbsp;<input value='MSG_TEST_RESBEANS_AddParamButton' type='button' onclick='addParam()'>";
    str += "<br/><br/>";
    str += getFormRep(null, uri, mName, mediaType);
    document.getElementById('testres').innerHTML = str;
    var req = uri;
    var disp = getDisplayUri(req);
    var uriLink = "<a id='"+req+"' href=javascript:doShowContent('"+req+"') >"+getDisplayURL(disp, 80)+"</a>";
    updatepage('request', '<span class=bld>Resource:</span> '+uriLink+' <br/>(<a href="'+req+'" target="_blank"><span class=font10>'+getDisplayURL(req, 90)+'</span></a>)<hr>');
}
function doShowStaticResource(uri, r) {
    updatepage('result', '');
    updatepage('resultheaders', '');
    paramNumber = 1;
    showBreadCrumbs(uri);
    var mName = getDefaultMethod();
    var mediaType = getDefaultMime();    
    var str = getMethodMimeTypeCombo(r);
    //str += "&nbsp;&nbsp;<input value='MSG_TEST_RESBEANS_AddParamButton' onclick='addParam()'>";
    str += "<br/><br/>";
    str += getFormRep(null, uri, mName, mediaType);
    document.getElementById('testres').innerHTML = str;
    var methodNode = document.getElementById("methodSel");
    var options = methodNode.options;
    for(i=0;i<options.length;i++) {
        if(options[i].value.substring(0, 3) == 'GET') {
            methodNode.selectedIndex = i;
        }
    }
    changeMethod();    
    var req = uri;
    var disp = getDisplayUri(req);
    var uriLink = "<a id='"+req+"' href=javascript:doShowContent('"+req+"') >"+getDisplayURL(disp, 80)+"</a>";
    updatepage('request', '<span class=bld>Resource:</span> '+uriLink+' <br/>(<a href="'+req+'" target="_blank"><span class=font10>'+getDisplayURL(req, 90)+'</span></a>)<hr>');
}
function getFormRep(req, uri, mName, mediaType) {
    if(mName == null || mName == 'undefined')
        mName = getDefaultMethod();
    if(mediaType == null || mediaType == 'undefined')
        mediaType = getDefaultMime();
    //alert(req + uri + mName + mediaType);
    var str = "<div id='formSubmittal'>";
    str += "<form action='' method="+mName+" name='form1'>";
    str += "<div id='paramHook'></div>";
    //str += getParamRep(req, mName);
    str += "<input name='path' value='"+uri+"' type='hidden'>";
    str += "<input id='method' name='method' value='"+mName+"' type='hidden'>";
    str += "<input id='mimeType' name='mimeType' value='"+mediaType+"' type='hidden'>";
    str += "<br/><input value='MSG_TEST_RESBEANS_TestButton' type='button' onclick='testResource()'>";
    str += "</form>";
    str += "</div>";
    return str;
}
function addParam() {
    var str = "<span class=bld>MSG_TEST_RESBEANS_NewParamName:&nbsp;</span>"+
        "<input id='newParamNames' name='param"+paramNumber+"' type='text' value='param"+paramNumber+"' size='25'>"+
        "&nbsp;&nbsp;&nbsp;<span class=bld>MSG_TEST_RESBEANS_NewParamValue:&nbsp;</span>"+
        "<input id='newParamValues' name='value"+paramNumber+"' type='text' value='value"+paramNumber+"' size='55'>"+"<br>";
    var prevParam = document.getElementById("paramHook").innerHTML;
    document.getElementById("paramHook").innerHTML = prevParam + "<br>" + str;
    paramNumber++;
}
function showBreadCrumbs(uri) {
    var nav = document.getElementById('navigation');
    var disp = getDisplayUri(uri);
    var count = 0;
    for(i=0;i<breadCrumbs.length;i++) {
        if(breadCrumbs[i] == disp) {
            count++;
        }
    }
    if(count == 0) {
        breadCrumbs[breadCrumbs.length+1] = disp;
        var uriLink = "<a id='"+uri+"' href=javascript:doShowContent('"+uri+"') >"+getDisplayURL(disp, 90)+"</a>";
        if(nav.innerHTML != '') {
            bcCount++;
            var uriPerLine = 4;
            //alert(bcCount);
            //alert(Math.round(bcCount/uriPerLine)+ ' ' + bcCount/uriPerLine);
            if(Math.round(bcCount/uriPerLine) == bcCount/uriPerLine)
                nav.innerHTML = nav.innerHTML + " , <br/>" + uriLink;
            else
                nav.innerHTML = nav.innerHTML + " , " + uriLink;
        } else
            nav.innerHTML = uriLink;
    }
}
var bcCount = 0;
function getParamRep(req, mName) {
    var str = "";
    if(mName == 'GET') {
        if(req != null && req.length > 0) {       
            //alert(req.length);             
            for(i=0;i<req.length;i++) {
                var params = req[i].getElementsByTagName('param');
                if(params != null) {
                    //alert(params.length);
                    for(j=0;j<params.length;j++) {
                        var pname = params[j].attributes.getNamedItem('name').nodeValue;
                        var num = j+1;
                        str += "<span class=bld>"+pname+":</span>"+"<input id='params' name='"+pname+"' type='text' value='"+pname+"'>"+"<br><br>";
                    }
                }
            }
        } else {
            str = "";
        }
    }
    else if(mName == 'DELETE')
        str = "";
    else
        str = "<textarea id='blobParam' name='params' rows='6' cols='70'>MSG_TEST_RESBEANS_Insert</textarea><br/>";        
    if(str != "")
        str = "<span class=bld>MSG_TEST_RESBEANS_ResourceInputs</span><br><br><div class='ml20'>"+str+"</div><br/>";
    return str;
}
function testResource() {
    updatepage('result', 'Loading...');
    var mimetype = getFormMimeType();
    var method = getFormMethod();
    //alert('method: '+method+'mimetype: '+mimetype);
    var p = '';
    var path = document.forms[0].path.value;
    var found = path.indexOf( "{" );
    if (found != -1){
        if(document.forms[0].params != null) {
            var len = document.forms[0].params.length;
            for(j=0;j<len;j++) {
                var param = document.forms[0].params[j]
                path = path.replace("{"+param.name+"}", param.value);
            }
        }
    } else {
        if(document.forms[0].params != null) {
            var len = document.forms[0].params.length;
            for(j=0;j<len;j++) {
                var param = document.forms[0].params[j]
                if(len == 1 || len-j == 1)
                    p += param.name+"="+param.value;
                else
                    p += param.name+"="+param.value+"&";
            }
        }
    }
    if(document.forms[0].newParamNames != null) {
        var len = document.forms[0].newParamNames.length;
        for(j=0;j<len;j++) {
            var paramName = document.forms[0].newParamNames[j].value;
            var paramValue = document.forms[0].newParamValues[j].value;
            if(len == 1 || len-j == 1)
                p += paramName+"="+paramValue;
            else
                p += paramName+"="+paramValue+"&";
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
        req = baseURL+path
    if(method == 'GET' && p.length > 0)
        req+= "?"+p;
    var disp = getDisplayUri(req);
    //alert('method: '+method+'mimetype: '+mimetype+' length: '+paramLength+'params: '+params);
    var xmlHttpReq4 = open(method, req, mimetype, paramLength, true);
    xmlHttpReq4.onreadystatechange = function() { updateContent(xmlHttpReq4); };
    xmlHttpReq4.send(params);
}
function createIFrame(url) {
    var c = '<iframe src="'+url+'" class="frame" width="600" height="400" align="left">'+
        '<p>See <a href="'+url+'">"'+url+'"</a>.</p>'+
        '</iframe>';
    return c;
}
function showViews(name) {
    if(name == 'raw' && currentMethod == 'GET' && currentMimeType == 'application/xml') //This step is needed for Firefox to show content as xml
    	updatepage('rawContent', createIFrame(currentValidUrl));
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
}

function isResponseReady(xmlHttpReq5, param) {
    if (xmlHttpReq5.readyState == 4) {
        monitor(xmlHttpReq5, param);
        return true;
    } else
        return false;
}

var currMonitorText = null;

function monitor(xmlHttpReq, param) {
    var nodisp = ' class="nodisp" ';
    var rawViewStyle = ' ';
    var headerViewStyle = nodisp;
    var rawContent = 'Received:\n'+xmlHttpReq.responseText+'\n';
    if(param != null && param != undefined)
        rawContent = 'Sent:\n'+param + '\n\n' + rawContent;
    var prev = document.getElementById('monitorText');
    var cURL = getURL(xmlHttpReq);
    if(cURL == null || cURL == '')
        cURL = currentValidUrl;
    var s = 'Request: ' + currentMethod + ' ' + cURL + 
            '\n\nStatus: ' + xmlHttpReq.status + ' (' + xmlHttpReq.statusText + ')';
    var prevs = '';
    if(currMonitorText != null && currMonitorText != undefined) {
        prevs = currMonitorText;        
        currMonitorText = 
            s + '\n\n' + rawContent+
            '\n-----------------------------------------------------------------------\n\n'+
            prevs;  
    } else {
        currMonitorText = s + '\n\n' + rawContent;
    }
}

function getURL(xmlHttpReq5) {
    var url = '';
    try {
        doc2 = loadXml(xmlHttpReq5.responseText);
    } catch(e) {alert('err: '+e.name+e.message);}
    if(doc2 != null && doc2.documentElement.nodeName != 'parsererror') {
        try {
            var container=doc2.documentElement;
            if(container == null || container.nodeName == 'html')
                return url;
            var playListId = container.getElementsByTagName('playlistId')[0];
            var title = container.getElementsByTagName('title')[0];                  
            var desc = container.getElementsByTagName('description')[0];
            return container.attributes.getNamedItem('uri').nodeValue;
        } catch(e) {
            //alert('err: '+e.name+e.message);
            return null;
        }
    } 
    return url;   
}

function updateContent(xmlHttpReq) {
    try {
        if (isResponseReady(xmlHttpReq)) {
            var content = xmlHttpReq.responseText;
            var ndx = content.indexOf('HTTP Status');
            var showRaw = 'false';
            if(ndx != -1) {
                showRaw = 'true';
            }
            var ndx2 = content.indexOf('Caused by: java.lang.');
            if(ndx2 != -1) {
                showRaw = 'true';
            }
            if(content != null && content != undefined) {
                content = content.replace(/'/g,"\'");
                if(content == '')
                    content = 'MSG_TEST_RESBEANS_NoContents'
                try {
                    //alert(content);
                    var cErr = '<table border=1><tr><td class=tableW>Content may not have Container-Containee Relationship. See Raw View for content.</td></tr></table>';
                    var tableContent = cErr;
                    if(content.indexOf("<?xml ") != -1 || 
                            content.indexOf('{"') != -1) {
                        var tc = getContainerTable(content);
                        if(tc != null)
                            tableContent = tc;
                        else
                            showRaw = 'true';
                    }
                    var rawContent = content;
                    var tableViewStyle = ' ';
                    var nodisp = ' class="nodisp" ';
                    var rawViewStyle = nodisp;
                    var headerViewStyle = nodisp;
                    var monitorViewStyle = nodisp;
                    if(showRaw == 'true') {
                        tableViewStyle = nodisp;
                        rawViewStyle = ' ';
                        headerViewStyle = nodisp;
                        monitorViewStyle = nodisp;
                    }
                    updatepage('result', '<span class=bld>MSG_TEST_RESBEANS_Status</span> '+ xmlHttpReq.status+' ('+xmlHttpReq.statusText+')<br/><br/>'+
                        '<span class=bld>MSG_TEST_RESBEANS_Content</span> '+
                        getTab('table', tableViewStyle)+getTab('raw', rawViewStyle)+
                        getTab('header', headerViewStyle)+getTab('monitor', monitorViewStyle)+                     
                        '<div id="menu_bottom" class="stab tabsbottom"></div>'+
                        '<div id="headerInfo"'+headerViewStyle+'>'+getHeaderAsTable(xmlHttpReq)+'</div>'+
                        '<div id="tableContent"'+tableViewStyle+'>'+tableContent+'</div>'+
                        '<div id="rawContent"'+rawViewStyle+'>'+
                            '<textarea rows=15 cols=72 align=left readonly>'+rawContent+'</textarea></div>'+ 
                        '<div id="monitorContent"'+monitorViewStyle+'>'+
                            '<textarea id="monitorText" rows=15 cols=72 align=left readonly>'+currMonitorText+'</textarea></div>');
                    if(showRaw == 'true')
                        showViews('raw');
                    else
                        showViews('table');
                } catch( e ) {
                    //alert(e.name+e.message);
                    var c = createIFrame(currentValidUrl);
                    updatepage('result', '<span class=bld>MSG_TEST_RESBEANS_Content</span> '+c);
                    updatepage('resultheaders', '<span class=bld>MSG_TEST_RESBEANS_ResponseHeaders</span> '+getHeaderAsTable(xmlHttpReq));                    
                }  
            }
        } else {
            log("state: "+xmlHttpReq.readyState);
        }
    } catch( e ) {
        alert('Caught Exception; name: [' + e.name + '] message: ]' + e.message+"]");
    }
} 
var viewIds = new Array()
viewIds[0] = "table"
viewIds[1] = "raw"
viewIds[2] = "header"
viewIds[3] = "monitor"
var viewNames = new Array()
viewNames[0] = "MSG_TEST_RESBEANS_TabularView"
viewNames[1] = "MSG_TEST_RESBEANS_RawView"
viewNames[2] = "MSG_TEST_RESBEANS_Headers"
viewNames[3] = "MSG_TEST_RESBEANS_Monitor"
function getTab(id, style) {
    var c = '<div id="'+id+'"'+style+'><table class="result"><tr>';
    var style = 'otab';
    for(i=0;i<viewIds.length;i++) {
        if(id == viewIds[i])
            style = 'stab';
        else
            style = 'otab';
        c += '<td class="tab '+style+'"><a href="javascript:showViews(\''+
            viewIds[i]+'\')" class="tab"><span class="stext">'+viewNames[i]+'</span></a></td>';
    }
    c += '</tr></table></div>';
    return c;
}
function getHeaderAsTable(xmlHttpReq) {
    //alert(header);    
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
    for(i=0;i<rows.length;i++) {
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
function getContainerTable(content) {
    //alert('getContainerTable: '+content);
    if(content == null)
        return null;
    var ret = null;    
    var container = null;
    try {
        if(content.indexOf("<?xml ") != -1) {
            var doc2 = loadXml(content);
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
            str2 = findUriFromXml(container);
        else
            str2 = findUriFromJSON(content);
        if(str2 == null || str2 == '')
            return null;
        str += str2;
        str += "</tbody></table>";
        ret = str;
    } catch(e) {
        alert('err: '+e.name+e.message);
    }

    return ret;
}
function findUriFromXml(container) {
    tcStr = '';
    getChildUriFromXml(container);
    return tcStr;
}
var tcStr = '';
function getChildUriFromXml(refChild) {
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
                tcStr += createRowForUriFromXml(subChild);
            }
            getChildUriFromXml(subChild);
        }
    }
}
function createRowForUriFromXml(refChild) {
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
    var disp = getDisplayUri(uri);
    str += "<a id='"+uri+"' href=javascript:doShowContent('"+uri+"') >"+getDisplayURL(disp, 70)+"</a>";
    str += "<br/>(<a href='"+uri+"' target='_blank'><span class=font10>"+getDisplayURL(uri, 70)+"</span></a>)";
    str += "</td>";
    str += "</tr>";
    return str;
}
function findUriFromJSON(content) {
    var str = ''; 
    
    //Check if Container    
    var i = content.indexOf('Ref');
    if(i == -1)
        return str;
    
    var temp = new Array();
    temp = content.split(':');
    var j = 0;
    var count = 0;
    for(j=0;j<temp.length;j++) {
        if(temp[j] == '{"$"')
            count++;  
    }
        
    //Find container name
    var cName = '';
    var j = content.indexOf('"');
    if(j != -1)
        cName = content.substring(j+1, content.indexOf('"', j+1));
    
    var c = content.replace(/\\\//g,"/");
    /*var myObj = 
        {"playlists":
            {"@uri":"http://localhost:8080/F/restbean/playlists/",
            "playlistRef":[
                    {"@uri":"http://localhost:8080/F/restbean/playlists/1/", "playlistId":{"$":"1"}},
                    {"@uri":"http://localhost:8080/F/restbean/playlists/2/", "playlistId":{"$":"2"}},
                    {"@uri":"http://localhost:8080/F/restbean/playlists/3/", "playlistId":{"$":"3"}}
                ]
            }
        };*/    
    var myObj = eval('(' +c+')');
        
    var x = cName.substring(0, cName.length-1);
    var y = x + 'Ref';
    var z = x + 'Id';
    for(j=0;j<count;j++) {
        str += "<tr>"; 
        str += "<td>"+eval('myObj.'+x+'s.'+y+'['+j+'].'+z+'.$')+"</td>";
        var uri = eval('myObj.'+x+'s.'+y+'['+j+'].@uri');
        var disp = getDisplayUri(uri);
        str += "<td>";    
        str += "<a id='"+uri+"' href=javascript:doShowContent('"+uri+"') >"+getDisplayURL(disp, 70)+"</a>";
        str += "<br/>(<a href='"+uri+"' target='_blank'><span class=font10>"+getDisplayURL(uri, 70)+"</span></a>)";
        str += "</td>";
        str += "</tr>";
    }
    return str;
}
function getDisplayUri(uri) {
    var disp = uri;
    if(disp.length > baseURL.length)
        disp = disp.substring(baseURL.length);
    return disp;
}
function getDisplayURL(url, len) {
    return url.substring(0, len);
}
function updatepage(id, str){
    document.getElementById(id).innerHTML = str;
}
function log(msg) {
    //document.getElementById('log').innerHTML = msg;
}
function getFormMimeType() {
    var resource = document.getElementById('mimeType');
    if(resource != null)
        return getMimeType(resource.value);
    else
        return getDefaultMime();
}
function getMimeType(mime) {
    //alert(mime);
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
}
function getFormMethod() {
    var resource = document.getElementById('method');
    if(resource != null)
        return getMethod(resource.value);
    else
        return getDefaultMethod();
}
function getMethod(method) {
    if(method != null) {
        var i = method.indexOf('(');
        if(i == -1)
            return method;
        else
            return method.substring(0, i);
    } else
        return getDefaultMethod();
}
function init() {
    var params = new Array();
    var method = getDefaultMethod();
    var xmlHttpReq = open(method, wadlURL, null, 0, true);
    if(xmlHttpReq != null) {
        xmlHttpReq.onreadystatechange = function() { updateMenu(xmlHttpReq); };
        xmlHttpReq.send(null);
    } else {
        setvisibility('main', 'inherit');
        var str = 'MSG_TEST_RESBEANS_Help';
        document.getElementById('content').innerHTML = str;
    }            
}

function tree(){
    this.categories = new Array();
    this.add = addCategory;
    this.toString = getTreeString;
    this.getCategories = listCategories;
}

function category(id, text, ndx){
    this.id = id;
    this.text = text;
    this.ndx = ndx;
    this.write = writeCategory;
    this.add = addItem;
    this.items = new Array();
    this.getItems = listItems;
}

function item(id, text, ndx){    
    this.id = id;
    this.text = text;
    this.ndx = ndx;
    this.write = writeItem;
}

function getTreeString(){
    var treeString = '';
    for(var i=0;i<this.categories.length;i++) {
        treeString += this.categories[i].write();
    }
    return treeString;
}

function addCategory(category){
    this.categories[this.categories.length] = category;
}

function listCategories(){
    return this.categories;
}

function writeCategory(){
    var uri = baseURL + this.id;
    if(this.id == 'resources')
        uri = null;
    var categoryString = '<span class="category"';
    categoryString += '><img src="cg.gif" id="I1' + this.id + '" onClick="updateTree(\'' + this.id + '\')">';
    categoryString += '<img src="app.gif" id="I' + this.id + '">';
    if(uri != null)
        categoryString += "<div class='item2'><a href=javascript:doShowContent('"+uri+"') >"+ this.text + "</a></div>";
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
}

function addItem(item){
    this.items[this.items.length] = item;
}

function listItems(){
    return this.items;
}

function writeItem(){
    var uri = baseURL + this.id;
    var itemString = '<img src="cc.gif" border="0">';
    itemString += '<img src="item.gif" border="0">';
    if(uri != null)
        itemString += "<a href=javascript:doShowContent('"+uri+"') >"+ this.text + "</a>";
    else
        itemString += this.text;
    itemString += '<br>';
    return itemString;
}

function showCategory(category){
    var categoryNode = document.getElementById(category).style;
    if(categoryNode.display=="block")
        categoryNode.display="none";
    else
        categoryNode.display="block";
    toggleCategory(category);
}

function updateTree(catId){
    //alert(catId);
    if(catId == 'resources') {//return if top level
        showCategory('resources');
        return;
    }
    var myTree = createTree(wadlDoc);
    document.getElementById('leftSidebar').innerHTML = myTree.toString();
    childrenContent = '';
    getChildren(catId);
    currentCategory = catId;
    setTimeout("alertIt()",1000);
}

function alertIt(){
    var catId = currentCategory;
    var categoryNode = document.getElementById(catId);
    categoryNode.innerHTML = childrenContent;
    showCategory('resources');
    showCategory(catId);
}

function getChildren(uri) {
    var xmlHttpReq5 = open('GET', baseURL+uri, getDefaultMime(), 0, true);
    xmlHttpReq5.onreadystatechange = function() { getChildrenContent(xmlHttpReq5); };
    xmlHttpReq5.send(null);
}

var childrenContent = '';

function getChildrenContent(xmlHttpReq5) {
    var content = xmlHttpReq5.responseText;
    //alert(content);
    if(content.indexOf('HTTP Status') == -1) {
        var ret = getChildrenAsItems(content);
        //alert(ret);
        childrenContent = ret;
    } else {
        childrenContent = '';
    }
}

function getChildrenAsItems(xmlStr) {
    var ret = xmlStr;  
    if(ret == null)
        return '';
    var doc2 = null;
    try {
        doc2 = loadXml(ret);
    } catch(e) { return null;}
    if(doc2 != null && doc2.documentElement.nodeName != 'parsererror') {
        try {
            var container=doc2.documentElement;
            if(container == null || container.nodeName == 'html')
                return ret;
            var str = "";
            var refs = container.childNodes;
            var count = 0;
            for(i=0;i<refs.length;i++) {
                var refsChild = refs[i];
                if(refsChild.nodeValue == null) {//DOM Elements only
                    var ref = refsChild;             
                    var refChilds = ref.childNodes;
                    for(j=0;j<refChilds.length;j++) {
                        var refChild = refChilds[j];
                        if(refChild.nodeValue == null) {//DOM Elements only
                            var id = refChild;
                            if(ref.attributes != null && ref.attributes.length > 0 && 
                                    ref.attributes.getNamedItem('uri') != null) {
                                var uri = ref.attributes.getNamedItem('uri').nodeValue;
                                var idval = id.childNodes[0].nodeValue;
                                var disp = getDisplayUri(uri);
                                str += getItemString(idval, uri);
                            }
                        }
                    }
                    count++;
                }
            }
            ret = str;
        } catch(e) {
            //alert('err: '+e.name+e.message);
            return null;
        }
    }
    return ret;
}

function getItemString(name, uri){
    var itemString = '<img src="cc.gif" border="0">';
    itemString += '<img src="item.gif" border="0">';
    itemString += '<a href="javascript:doShowContent(\''+uri+'\')">';
    itemString += name;
    itemString += '</a><br>';
    return itemString;
}

function toggleCategory(img){
    ImageNode = document.getElementById('I' + img);
    ImageNode1 = document.getElementById('I1' + img);
    if(ImageNode1.src.indexOf('cg.gif')>-1) {
        //ImageNode.src = expand.src;
        ImageNode1.src = og.src;
    } else {
        //ImageNode.src = collapse.src;
        ImageNode1.src = cg.src;
    }
}
