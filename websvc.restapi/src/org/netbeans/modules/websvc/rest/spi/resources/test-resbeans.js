/*
* Supporting js for testing resource beans
*/
var doc;            
var app;
var wadlURL = baseURL+"/application.wadl";  
var wadlErr = 'Cannot access WADL: Please restart your REST application or specify correct base URL, and refresh this page.';

function xmlhttpGet(strURL, mimeType, type) {
    var xmlHttpReq;// = getXmlHttpRequest();
    try
    {    // Firefox, Opera 8.0+, Safari
        xmlHttpReq=new XMLHttpRequest();
        try {
            netscape.security.PrivilegeManager.enablePrivilege("UniversalBrowserRead");
        } catch (e) {
            //alert("Permission UniversalBrowserRead denied.");
        }
    }
    catch (e)
    {    // Internet Explorer
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
                return false;
            }
        }
    }
    try {
        xmlHttpReq.open('GET', strURL, true);
    } catch( e ) {
        alert('Error: Permission denied to call method XMLHttpRequest.open: ' + strURL+'. Click OK and see instructions on this page.');
        return false;
    }
    log("mimeType: "+mimeType);
    if (mimeType != null)
        xmlHttpReq.setRequestHeader('Accept', mimeType);
    if(type == 'menu')
        xmlHttpReq.onreadystatechange = function() { updateMenu(xmlHttpReq); };
    else
        xmlHttpReq.onreadystatechange = function() { updateContent(xmlHttpReq); };
    xmlHttpReq.send(null);
    return true;
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
                            var methodst = new item(mName,'showRightSideBar(\''+app.nodeName+'/'+rs.nodeName+'/'+r.nodeName+'\', '+i+', '+j+')');
                            start.add(methodst);
                        }
                        resources.add(start);
                    }
                }                            
                var treeString = myTree.toString();                            
                document.getElementById('leftSidebar').innerHTML = treeString;
            }
        } else {
            log("state: "+xmlHttpReq.readyState);
        }
    } catch( e ) {
        alert('Caught Exception1: ' + e.description);
    }
}
function setvisibility(id, state) {
    try {
        document.getElementById(id).style.visibility = state;
    } catch(e) {}
}
function showRightSideBar(path, ri, mi) {
    updatepage('result', '');
    var app1 = doc.documentElement;
    var rs = app1.getElementsByTagName('resources')[0];
    var r = rs.getElementsByTagName('resource')[ri];
    var m = r.getElementsByTagName('method')[mi];
    var req = m.getElementsByTagName('request');
    var response = m.getElementsByTagName('response');
    var str = "Resource: "+r.attributes.getNamedItem('path').nodeValue+"&nbsp;&nbsp;&nbsp;Method: "+
        m.attributes.getNamedItem("name").nodeValue;
    str += "<br/><br/>";
    if(req != null && req.length > 0) 
        str += "<b>Resource Inputs:</b><br><br>";
    str += "<form action='' method=GET name='form1'>";              
    if(req != null) {                    
        for(i=0;i<req.length;i++) {
            var params = req[i].getElementsByTagName('param');
            if(params != null) {
                for(j=0;j<params.length;j++) {
                    var pname = params[j].attributes.getNamedItem('name').nodeValue;
                    var num = j+1;
                    str += "<b>Param"+num+":</b><input id='params' name='"+pname+"' type='text' value='"+pname+"'><br><br>";
                }
            }
        }
    }
    var path = r.attributes.getNamedItem('path').nodeValue;
    str += "<input name='path' value='"+path+"' type='hidden'>";
    var mediaType = 'text/html';                
    if(response != null && response.length > 0) {
        var rep = response[0].getElementsByTagName('representation');
        if(rep != null && rep.length > 0) {                        
            if(rep[0].attributes.length > 0) {
                var att = rep[0].attributes.getNamedItem('mediaType');
                if(att != null)
                    mediaType = att.nodeValue
            }
        }
        str += "<b>MimeType(readonly):</b><input id='mimeType' name='mimeType' value='"+mediaType+"' type='text' readonly><br/>";
    }
    str += "<br/><input value='Test Resource' type='button' onclick='testResource()'>";
    str += "</form>";
    document.getElementById('test3').innerHTML = str;
    try {
        testResource();
    } catch(e) {}                
}
function testResource() {
    var path = document.forms[0].path.value;
    var found = path.indexOf( "{" );
    if (found != -1){
        if(document.forms[0].params != null) {
            var len = document.forms[0].params.length;
            for(j=0;j<len;j++) {
                var param = document.forms[0].params[j]
                var pname = param.value;
                path = path.replace("{"+param.name+"}", pname);
            }
        }
    }
    var req = baseURL+path;
    var mimetype = getRep();
    updatepage('request', req);
    updatepage('amimetype', mimetype);
    //alert("mimetype "+mimetype);
    if (mimetype == 'image/jpg') {//image
        alert('The image/jpg MimeType currently does not work with the MimeType selection method.\nInstead of seeing the image, you will see the image data');
        //xmlhttpGet(req, mimetype);
    } else 
        xmlhttpGet(req, mimetype, '');                
}
function testResource2(i) {
    var path2 = document.getElementById('path2_'+i);
    document.getElementById('customer').innerHTML = i;
    xmlhttpGet(baseURL+path2.value, 'application/xml', '');
}
function updateContent(xmlHttpReq) {
    try {
        if (xmlHttpReq.readyState == 4) {
            var content = xmlHttpReq.responseText;
            if(content.indexOf("customer-ref") != -1) {
                content = getCustomersTable(content);
                updatepage('result', content);
            } else if(content.indexOf("address-ref") != -1) {
                var custSel = document.getElementById('customer');
                if(custSel != null) {
                    var i = custSel.innerHTML;                                
                    content = content.replace(/~lt~/g, "<");
                    var cell = document.getElementById('cell'+i);
                    if(cell != null)
                        cell.innerHTML = content;
                    else
                        updatepage('result', content);
                }
            } else {
                updatepage('result', content);
            }                        
            updatepage('resultheaders', xmlHttpReq.getAllResponseHeaders());
        } else {
            log("state: "+xmlHttpReq.readyState);
        }
    } catch( e ) {
        alert('Caught Exception1: ' + e.description);
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
function getCustomersTable(xmlStr) {
    var ret = xmlStr.replace(/~lt~/g, "<");
    var doc2 = loadXml(ret);
    if(doc2 != null) {
        var customers=doc2.documentElement;
        var str = "<h4>"+customers.nodeName+"</h4>";
        str += "<form action='' method=GET name='form2'>";
        str += "<table class='results' border='1'>";
        str += "<thead class='resultHeader'>";
        var colNames = new Array()
        colNames[0] = "Customer ID"
        colNames[1] = "Customer URI"
        colNames[2] = "Description"
        var colSizes = new Array()
        colSizes[0] = "80"
        colSizes[1] = "250"
        colSizes[2] = "350"
        for (i=0;i<colNames.length;i++) {
            str += "<th width='"+colSizes[i]+"' align='left'><font color='#FFFFFF'><b>"+colNames[i]+"</b></font></th>";
        }
        str += "</thead>";
        str += "<tbody>";
        var custrefs = customers.getElementsByTagName('customer-ref');
        for(i=0;i<custrefs.length;i++) {
            str += "<tr style='font-size: 9px;'>";
            var custref = custrefs[i];                       
            var custid = custref.getElementsByTagName('customer_id')[0];
            var uri = custref.attributes.getNamedItem('uri').nodeValue;
            str += "<td>"+custid.childNodes[0].nodeValue+"</td>";
            str += "<td>";                        
            str += "<input id='path2_"+i+"' name='path' value='"+uri+"' type='hidden'>";
            str += uri+"<input value=Go type=button onclick='testResource2("+i+")'>";
            str += "</td>";
            str += "<td id='cell"+i+"'>&nbsp;</td>";
            str += "</tr>";
        }
        str += "</tbody></table></form>";
        ret = str;
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
function getSelMethod() {
    for (counter = 0; counter < form1.selMethod.length; counter++) {
        // If a radio button has been selected it will return true
        // (If not it will return false)
        if (form1.selMethod[counter].checked)
            return form1.selMethod[counter].value;
    }
    return null;
}
function getSelectorValue(selectorId) {
    var resource = document.getElementById(selectorId);
    log("selector "+resource.name);
    var myIndex = resource.selectedIndex;
    log("index: " +myIndex);
    log(resource.options);
    log(resource.options[myIndex].value);
    return resource.options[myIndex].value 
}
function init() {
    var status = xmlhttpGet(wadlURL, null, 'menu');
    if(status) {
        showCategory('resources');
    } else {
        setvisibility('main', 'inherit');
        var str = '<b>Help Page</b><br/><br/>'+wadlErr;
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