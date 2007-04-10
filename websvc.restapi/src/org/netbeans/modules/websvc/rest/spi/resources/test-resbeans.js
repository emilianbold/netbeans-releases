/*
* Supporting js for testing resource beans
*/
var doc;            
var app;
var wadlURL = baseURL+"/application.wadl";  
var wadlErr = 'Cannot access WADL: Please restart your REST application, and refresh this page.';
var currentUrl;
var currentValidUrl;

function getHttpRequest() {
    var xmlHttpReq;
    try
    {    // Firefox, Opera 8.0+, Safari, IE7.0+
        xmlHttpReq=new XMLHttpRequest();
        try {
            netscape.security.PrivilegeManager.enablePrivilege ("UniversalBrowserRead");
        } catch (e) {
            //alert("Permission UniversalBrowserRead denied.");
        }
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
 function open(method, url, mimeType, paramLen) {
    currentUrl = url;
    var xmlHttpReq = getHttpRequest();
    if(xmlHttpReq == null) {
    	alert('Error: Cannot create XMLHttpRequest');
        return null;
    }
    try {
        netscape.security.PrivilegeManager.enablePrivilege ("UniversalBrowserRead");
    } catch (e) {
        //alert("Permission UniversalBrowserRead denied.");
    }
    try {
        xmlHttpReq.open(method, url, true);
    } catch( e ) {
        //alert('Error: XMLHttpRequest.open failed for: '+strURL+' Error name: '+e.name+' Error message: '+e.message);
        return null;
    }
    log("mimeType: "+mimeType);
    if (mimeType != null) {
        if(method == 'GET') {
            //alert("setting accept: "+mimeType);
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
            //alert("[here]");
            setvisibility('main', 'inherit');
            document.getElementById('subheader').innerHTML = '<br/><b>WADL: </b>'+wadlURL;
            doc = loadXml(rtext);
            if(doc != null) {
                app=doc.documentElement;
                var myTree = new tree();  
                var rs;
                if(app != null) {
                    rs = app.getElementsByTagName('resources')[0];
                    var context = rs.attributes.getNamedItem('base').nodeValue;
                    var begin = context.indexOf('/', 7);
                    if(begin != -1)
                        context = context.substring(begin, context.length);
                    var resources = new category(rs.nodeName, context);
                    myTree.add(resources);
                    var rarr = rs.getElementsByTagName('resource');
                    for(i=0;i<rarr.length;i++) {
                        var r = rarr[i];   
                        var path = r.attributes.getNamedItem('path');
                        var start = new category(path.nodeValue,path.nodeValue);                                        
                        var methods = r.getElementsByTagName('method');
                        for(j=0;j<methods.length;j++) {
                            var m = methods[j];                            
                            var mName = m.attributes.getNamedItem("name").nodeValue;
                            var response = m.getElementsByTagName('response');
                            var mediaType = getMediaType(response);
                            if(mediaType != null)
                                mName = mName + '(' + mediaType + ')';
                            var methodst = new item(mName,'showRightSideBar(\''+app.nodeName+'/'+rs.nodeName+'/'+r.nodeName+'\', '+i+', '+j+')');
                            start.add(methodst);
                        }
                        resources.add(start);
                    }
                }                            
                var treeString = myTree.toString();                            
                document.getElementById('leftSidebar').innerHTML = treeString;
                showCategory('resources');
            }
        } else {
            log("state: "+xmlHttpReq.readyState);
        }
    } catch( e ) {
        alert('Caught Exception; name: ' + e.name + ' message: ' + e.message);
    }
}
function getMediaType(response) {
    var mediaType = null;                
    if(response != null && response.length > 0) {
        var rep = response[0].getElementsByTagName('representation');
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
    var formSubmittal = document.getElementById("formSubmittal");
    if(formSubmittal != null) {
        var content = formSubmittal.innerHTML;
        var index = content.indexOf('method');
        if(index != -1) {
            var index2 = content.indexOf('name');
            formSubmittal.innerHTML = content.substring(0, index)+" method='"+method+"' "+content.substring(index2);
        }
    }
    document.getElementById("method").value = method;
};
function showRightSideBar2(uri) {
    updatepage('result', 'Loading...');
    var mName = 'POST';
    var qmName = '';
    var mediaType = null;
    if(mediaType != null)
        qmName = qmName + "("+mediaType+")";
    else
        mediaType = "application/x-www-form-urlencoded";
    var str = "<b>Resource:</b> <a href='"+uri+"' target='_blank'>"+uri+"</a><br/><br/><b>Method: </b>";
    str += "<select id='methodSel' name='methodSel' onchange='javascript:changeMethod();'>";
    str += "  <option value='GET'>GET</option>";
    str += "  <option value='PUT'>PUT</option>";
    str += "  <option value='DELETE'>DELETE</option>";
    str += "</select>";
    str += "<br/><br/><div id='formSubmittal'>";
    str += "<form action='' method="+mName+" name='form1'>";
    str += getParamRep(null, mName, mediaType);
    str += "<input name='path' value='"+uri+"' type='hidden'>";
    if(mName != null) {
        str += "<input id='method' name='method' value='"+mName+"' type='hidden'>";
    }
    if(mediaType != null) {
        str += "<input id='mimeType' name='mimeType' value='"+mediaType+"' type='hidden'>";
        str += "<b>MimeType(readonly):</b> "+mediaType+"<br/>";
    }
    str += "<br/><input value='Test...' type='button' onclick='testResource()'>";
    str += "</form>";
    str += "</div>";
    document.getElementById('testres').innerHTML = str;
    //alert(str);
    try {
        testResource();
    } catch(e) { alert(e.name+e.message);}       
}
function showRightSideBar(path, ri, mi) {
    updatepage('result', 'Loading...');
    var app1 = doc.documentElement;
    var rs = app1.getElementsByTagName('resources')[0];
    var r = rs.getElementsByTagName('resource')[ri];
    var m = r.getElementsByTagName('method')[mi];
    var mName = m.attributes.getNamedItem("name").nodeValue;
    var qmName = mName;
    var req = m.getElementsByTagName('request');
    var response = m.getElementsByTagName('response');
    var mediaType = getMediaType(response);
    if(mediaType != null)
        qmName = qmName + "("+mediaType+")";
    else
        mediaType = "application/x-www-form-urlencoded";
    var uri = r.attributes.getNamedItem('path').nodeValue;
    var str = "<b>Resource:</b> <a href='"+uri+"' target='_blank'>"+uri+"</a>&nbsp;&nbsp;&nbsp;<b>Method: </b>";
    str += "<select id='methodSel' name='methodSel' onchange='javascript:changeMethod();'>";
    str += "  <option value='GET'>GET</option>";
    str += "  <option selected value='POST'>POST</option>";
    str += "</select>";
    str += "<br/><br/><div id='formSubmittal'>";
    str += "<form action='' method="+mName+" name='form1'>";
    str += getParamRep(req, mName, mediaType);
    var path = r.attributes.getNamedItem('path').nodeValue;
    str += "<input name='path' value='"+path+"' type='hidden'>";
    if(mName != null) {
        str += "<input id='method' name='method' value='"+mName+"' type='hidden'>";
    }
    if(mediaType != null) {
        str += "<input id='mimeType' name='mimeType' value='"+mediaType+"' type='hidden'>";
        str += "<b>MimeType(readonly):</b> "+mediaType+"<br/>";
    }
    str += "<br/><input value='Test...' type='button' onclick='testResource()'>";
    str += "</form>";
    str += "</div>";
    document.getElementById('testres').innerHTML = str;
    try {
        testResource();
    } catch(e) { alert(e.name+e.message);}       
}
function getParamRep(req, mName, mediaType) {
    //alert(mName+", "+mediaType+", "+pname);
    var str = "";
    if(req == null || mName == 'PUT' || mName == 'POST')
        str = "<textarea id='params' name='params' rows='8' cols='70'></textarea><br/>";
    else if(mName == 'GET') {
        if(req != null && req.length > 0) {                    
            for(i=0;i<req.length;i++) {
                var params = req[i].getElementsByTagName('param');
                if(params != null) {
                    for(j=0;j<params.length;j++) {
                        var pname = params[j].attributes.getNamedItem('name').nodeValue;
                        var num = j+1;
                        str += "<b>Param"+num+":</b>"+"<input id='params' name='"+pname+"' type='text' value='"+pname+"'>"+"<br><br>";
                    }
                }
            }
        } else {
            str = "<textarea id='params' name='params' rows='8' cols='70'></textarea><br/>";
        }
    }
    else if(mName == 'DELETE')
        str = "";
    if(str != "")
        str = "<b>Resource Inputs:</b><br><br><div style='margin-left:20px'>"+str+"</div><br/>";
    return str;
}
function testResource() {
    updatepage('result', '');
    var params = '';
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
                if(len == 1 || len-j>1)
                    params += param.name+"="+param.value;
                else
                    params += param.name+"="+param.value+"&";
            }
        }
    }
    var req;
    if(path.indexOf('http:') != -1)
        req = path;
    else
        req = baseURL+path
    if(method == 'GET' && params.length > 0)
        req+= "?"+params;
    var mimetype = getRep();
    var method = getMethod();   
    updatepage('request', '<a href="'+req+'" target="_blank">'+req+'</a>');    
    //alert("mimetype "+mimetype);
    if (mimetype == 'image/jpg') {//image
        alert('The image/jpg MimeType currently does not work with the MimeType selection method.\nInstead of seeing the image, you will see the image data');
    } else {
        var xmlHttpReq4 = open(method, req, mimetype, params.length);
        var p = null;
        if(method == 'POST' || method == 'PUT' ) {
            p = params;
            updatepage('amimetype', 'Content-Type: '+mimetype);
        } else if(method == 'GET') {
            updatepage('amimetype', 'Accept: '+mimetype);
        } else if(method == 'DELETE') {
            updatepage('amimetype', 'N/A');
        }
        xmlHttpReq4.onreadystatechange = function() { updateContent(xmlHttpReq4); };
        xmlHttpReq4.send(p);
    }
}
function updateContent(xmlHttpReq) {
    try {
        if (xmlHttpReq.readyState == 4) {
            var content = xmlHttpReq.responseText;
            if(content != null && content != undefined) {
                try {
                    content = getContainerTable(content);
                    updatepage('result', content);
                    var containerTable = document.getElementById('containerTable');
                    var cellnum = document.getElementById('cellnum');                    
                    if(containerTable != null && containerTable.childNodes != null) {
                        var rows = containerTable.childNodes;
                        for(i=0;i<rows.length;i++) {
                            var row = rows[i];
                            var tds = row.childNodes;
                            if(tds != null && tds.length > 1) {
                                var id = tds[0].innerHTML;
                                var tdChilds = tds[1].childNodes;
                                var link = tdChilds[0];
                                var uri = link.id;
                                link.onclick = function() {showRightSideBar2(uri)}
                            }
                        }
                    }
                } catch( e ) {
                    //alert('upd err '+e.name+e.mesage);
                    var c = '<iframe  class="details" src="'+currentValidUrl+'" width="600" height="300" align="left">'+
                                '<p>See <a href="'+currentValidUrl+'">"'+currentValidUrl+'"</a>.</p>'+
                            '</iframe>';
                    updatepage('result', c);
                }  
            }                 
            updatepage('resultheaders', xmlHttpReq.getAllResponseHeaders());
        } else {
            log("state: "+xmlHttpReq.readyState);
        }
    } catch( e ) {
        alert('Caught Exception; name: ' + e.name + ' message: ' + e.message);
    }
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
        doc2=parser.parseFromString(xmlStr,"text/xml");
    }
    return doc2;
}
function getContainerTable(xmlStr) {
    var ret = '';  
    if(xmlStr != null)
        ret = xmlStr.replace(/~lt~/g, "<");
    else
        return ret;
    var doc2 = null;
    try {
        doc2 = loadXml(ret);
    } catch(e) {}
    if(doc2 != null && doc2.documentElement.nodeName != 'parsererror') {
        try {
            var container=doc2.documentElement;
            if(container == null || container.nodeName == 'html')
                return ret;
            var str = "<h4>"+container.nodeName+"</h4>";
            str += "<table class='results' border='1'>";
            str += "<thead class='resultHeader'>";
            var colNames = new Array()
            colNames[0] = "ID"
            colNames[1] = "URI"
            var colSizes = new Array()
            colSizes[0] = "80"
            colSizes[1] = "350"
            for (i=0;i<colNames.length;i++) {
                str += "<th width='"+colSizes[i]+"' align='left'><font color='#FFFFFF'><b>"+colNames[i]+"</b></font></th>";
            }
            str += "</thead>";
            str += "<tbody id='containerTable'>";
            var refs = container.childNodes;
            var count = 0;
            for(i=0;i<refs.length;i++) {
                var refsChild = refs[i];
                if(refsChild.nodeValue == null) {//DOM Elements only
                    var ref = refsChild;
                    str += "<tr style='font-size: 9px;'>";                
                    var refChilds = ref.childNodes;
                    for(j=0;j<refChilds.length;j++) {
                        var refChild = refChilds[j];
                        if(refChild.nodeValue == null) {//DOM Elements only
                            var id = refChild;
                            if(ref.attributes != null && ref.attributes.length > 0 && 
                                    ref.attributes.getNamedItem('uri') != null) {
                                var uri = ref.attributes.getNamedItem('uri').nodeValue;
                                str += "<td>"+id.childNodes[0].nodeValue+"</td>";
                                str += "<td>";
                                str += "<a id='"+uri+"' href=javascript:showRightSideBar2('"+uri+"') >"+uri+"</a>";
                                str += "</td>";
                                str += "</tr>";
                            }
                        }
                    }
                    count++;
                }
            }
            var cellnum = document.getElementById('cellnum');
            cellnum.innerHTML = count;
            str += "</tbody></table>";
            ret = str;
        } catch(e) {
            //alert('err: '+e.name+e.message);
        }
    }
    return ret;
}
function updatepage(id, str){
    document.getElementById(id).innerHTML = str;
}
function log(msg) {
    document.getElementById('log').innerHTML = msg;
}
function getRep() {
    var resource = document.getElementById('mimeType');
    return resource.value;
}
function getMethod() {
    var resource = document.getElementById('method');
    return resource.value;
}
function init() {
    var params = new Array();
    var method = 'GET';
    var xmlHttpReq = open(method, wadlURL, null, 0);
    if(xmlHttpReq != null) {
        xmlHttpReq.onreadystatechange = function() { updateMenu(xmlHttpReq); };
        xmlHttpReq.send(null);
    } else {
        setvisibility('main', 'inherit');
        var str = '<b>Help Page</b><br/><br/>'+
            '<p>Cannot access WADL: Please restart your REST application, and refresh this page.</p>' +
            '<p>If you still see this error and if you are accessing this page using Firefox with Firebug plugin, then'+
            '<br/>you need to disable firebug for local files. That is from Firefox menubar, check '+
            '<br/>Tools > Firebug > Disable Firebug for Local Files</p>';
        document.getElementById('content').innerHTML = str;
    }            
}

/*
* Function to support uri navigation
*/
var expand = new Image();
expand.src = "expand.gif";
var collapse = new Image();
collapse.src = "collapse.gif";

function tree(){
    this.categories = new Array();
    this.add = addCategory;
    this.toString = getTreeString;
}

function category(id, text){
    this.id = id;
    this.text = text;
    this.write = writeCategory;
    this.add = addItem;
    this.items = new Array();
}

function item(text, link){
    this.text = text;
    this.link = link;
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

function writeCategory(){
    var categoryString = '<span class="category" onClick="showCategory(\'' + this.id + '\')"';
    categoryString += '><img src="collapse.gif" id="I' + this.id + '">' + this.text;
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

function writeItem(){
    var itemString = '<a href="#" onClick="' + this.link + '">';
    itemString += '<img src="item.gif" border="0">';
    itemString += this.text;
    itemString += '</a><br>';
    return itemString;
}

function showCategory(category){
    var categoryNode = document.getElementById(category).style;
    if(categoryNode.display=="block")
        categoryNode.display="none";
    else
        categoryNode.display="block";
    changeGesture('I' + category);
}

function changeGesture(img){
    ImageNode = document.getElementById(img);
    if(ImageNode.src.indexOf('collapse.gif')>-1)
        ImageNode.src = expand.src;
    else
        ImageNode.src = collapse.src;
}