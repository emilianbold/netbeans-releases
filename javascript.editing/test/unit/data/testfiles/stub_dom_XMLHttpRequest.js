/**
* <p>
* <code>XMLHttpRequest</code> is a <a href="/en/docs/JavaScript" shape="rect" title="JavaScript">JavaScript</a> object that was created by Microsoft and adopted by Mozilla.  You can use it to easily retrieve data via HTTP.  Despite its name, it can be used for more than just XML documents.  In Gecko, this object implements the <code><a href="/en/docs/nsIJSXMLHttpRequest" shape="rect" title="nsIJSXMLHttpRequest">nsIJSXMLHttpRequest</a></code> and <code><a href="/en/docs/nsIXMLHttpRequest" shape="rect" title="nsIXMLHttpRequest">nsIXMLHttpRequest</a></code> interfaces.  Recent versions of Gecko have some changes to this object, see <a href="/en/docs/XMLHttpRequest_changes_for_Gecko1.8" shape="rect" title="XMLHttpRequest changes for Gecko1.8">XMLHttpRequest changes for Gecko1.8</a>.
* </p>
* 
* <h2> <span>Basic Usage</span></h2>
* <p>Using <code>XMLHttpRequest</code> is very simple.  You create an instance of the object, open a URL, and send the request.  The HTTP status code of the result, as well as the result document are available in the request object afterwards.
* </p>
* <div><b>Note:</b> Versions of Firefox prior to version 3 always send the request using UTF-8 encoding; <a href="/en/docs/Firefox_3" shape="rect" title="Firefox 3">Firefox 3</a> properly sends the document using the encoding specified by <code>data.xmlEncoding</code>, or UTF-8 if no encoding is specified.</div>
* <h3> <span>Example</span></h3>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* var req = new XMLHttpRequest();
* req.open('GET', 'http://www.mozilla.org/', false);
* req.send(null);
* if(req.status == 200)
* dump(req.responseText);
* </pre>
* <div><b>Note:</b> This example works synchronously, so it will block the user interface if you call this from your JavaScript.  You should not use this in practice.</div>
* <h3> <span>Example with non http protocol</span></h3>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* var req = new XMLHttpRequest();
* req.open('GET', 'file:///home/user/file.json', false);
* req.send(null);
* if(req.status == 0)
* dump(req.responseText);
* </pre>
* <div><b>Note:</b> file:/// and ftp:// do not return HTTP status, which is why they return zero for <code>status</code> and an empty string for <code>statusText</code>. Refer to <a href="https://bugzilla.mozilla.org/show_bug.cgi?id=331610" rel="nofollow" shape="rect" title="https://bugzilla.mozilla.org/show_bug.cgi?id=331610">bug 331610</a> for more insight.</div>
* <h2> <span>Asynchronous Usage</span></h2>
* <p>If you intend to use <code>XMLHttpRequest</code> from an extension, you should let it load asynchronously.  In asynchronous usage, you get a callback when the data has been received, which lets the browser continue to work as normal while your request is happening.
* </p>
* <h3> <span>Example</span></h3>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* var req = new XMLHttpRequest();
* req.open('GET', 'http://www.mozilla.org/', true);
* req.onreadystatechange = function (aEvt) {
* if (req.readyState == 4) {
* if(req.status == 200)
* dump(req.responseText);
* else
* dump("Error loading page\n");
* }
* };
* req.send(null);
* </pre>
* <h3> <span>Monitoring progress</span></h3>
* <p><code>XMLHttpRequest</code> provides the ability to listen to various events that can occur while the request is being processed.  This includes periodic progress notifications, error notifications, and so forth.
* </p><p>If, for example, you wish to provide progress information to the user while the document is being received, you can use code like this:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* function onProgress(e) {
* var percentComplete = (e.position / e.totalSize)*100;
* ...
* }
* 
* function onError(e) {
* alert("Error " + e.target.status + " occurred while receiving the document.");
* }
* 
* function onLoad(e) {
* // ...
* }
* // ...
* var req = new XMLHttpRequest();
* req.onprogress = onProgress;
* req.open("GET", url, true);
* req.onload = onLoad;
* req.onerror = onError;
* req.send(null);
* </pre>
* <p>The <code>onprogress</code> event's attributes, <code>position</code> and <code>totalSize</code>, indicate the current number of bytes received and the total number of bytes expected, respectively.
* </p><p>All of these events have their <code>target</code> attribute set to the <code>XMLHttpRequest</code> they correspond to.
* </p>
* <div><b>Note:</b> <a href="/en/docs/Firefox_3" shape="rect" title="Firefox 3">Firefox 3</a> properly ensures that the values of the <code>target</code>, <code>currentTarget</code>, and <code>this</code> fields of the event object are set to reference the correct objects when calling event handlers for XML documents represented by <code>XMLDocument</code>.  See <a href="https://bugzilla.mozilla.org/show_bug.cgi?id=198595" rel="nofollow" shape="rect" title="https://bugzilla.mozilla.org/show_bug.cgi?id=198595">bug 198595</a> for details.</div>
* <h2> <span>Other Properties and Methods</span></h2>
* <p>In addition to the properties and methods shown above, there are other useful properties and methods on the request object.
* </p>
* <h3> <span>responseXML</span></h3>
* <p>If you load an <a href="/en/docs/XML" shape="rect" title="XML">XML</a> document, the <code>responseXML</code> property will contain the document as an <code>XmlDocument</code> object that you can manipulate using DOM methods.  If the server sends well-formed XML but does not specify an XML Content-Type header, you can use <code><a href="/en/docs/XMLHttpRequest#overrideMimeType.28.29" shape="rect" title="XMLHttpRequest">overrideMimeType()</a></code> to force the document to be parsed as XML. If the server does not send well-formed XML, <code>responseXML</code> will be null regardless of any content type override.
* </p>
* <h3> <span>overrideMimeType()</span></h3>
* This method can be used to force a document to be handled as a particular content type.  You will generally want to use when you want to use <code>responseXML</code> and the server sends you <a href="/en/docs/XML" shape="rect" title="XML">XML</a>, but does not send the correct Content-Type header.  <div><b>Note:</b> This method must be called before calling <code>send()</code>.</div>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* var req = new XMLHttpRequest();
* req.open('GET', 'http://www.mozilla.org/', true);
* req.overrideMimeType('text/xml');
* req.send(null);
* </pre>
* <h3> <span>setRequestHeader()</span></h3>
* <p>This method can be used to set an HTTP header on the request before you send it.
* </p>
* <div><b>Note:</b> You must call <code>open()</code> before calling this method.</div>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* var req = new XMLHttpRequest();
* req.open('GET', 'http://www.mozilla.org/', true);
* req.setRequestHeader("X-Foo", "Bar");
* req.send(null);
* </pre>
* <h3> <span>getResponseHeader()</span></h3>
* <p>This method can be used to get an HTTP header from the server response.
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* var req = new XMLHttpRequest();
* req.open('GET', 'http://www.mozilla.org/', true);
* req.send(null);
* dump("Content-Type: " + req.getResponseHeader("Content-Type") + "\n");
* </pre>
* <h2> <span> Using from XPCOM components </span></h2>
* <div><b>Note:</b> Changes are required if you use XMLHttpRequest from a JavaScript XPCOM component.</div>
* <p>XMLHttpRequest cannot be instantiated using the <code>XMLHttpRequest()</code> constructor from a JavaScript XPCOM component. The constructor is not defined inside components and the code results in an error. You'll need to create and use it using a different syntax.
* </p><p>Instead of this:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* var req = new XMLHttpRequest();
* req.onprogress = onProgress;
* req.onload = onLoad;
* req.onerror = onError;
* req.open("GET", url, true);
* req.send(null);
* </pre>
* <p>Do this:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* var request = Components.
* classes["@mozilla.org/xmlextras/xmlhttprequest;1"].
* createInstance();
* 
* // QI the object to nsIDOMEventTarget to set event handlers on it:
* 
* request.QueryInterface(Components.interfaces.nsIDOMEventTarget);
* request.addEventListener("progress", function(evt) { ... }, false);
* request.addEventListener("load", function(evt) { ... }, false);
* request.addEventListener("error", function(evt) { ... }, false);
* 
* // QI it to nsIXMLHttpRequest to open and send the request:
* 
* request.QueryInterface(Components.interfaces.nsIXMLHttpRequest);
* request.open("GET", "http://www.example.com/", true);
* request.send(null);
* </pre>
* <h2> <span>Limited Number Of Simultaneous xmlHttpRequest Connections</span></h2>
* <p>The about:config preference: network.http.max-persistent-connections-per-server limits the number of connections to 2 by default.  Some interactive web pages using xmlHttpRequest may keep a connection open.  Opening two or three of these pages in different tabs or on different windows may cause the browser to hang in such a way that the window no longer repaints and browser controls don't respond.
* </p>
* <h2> <span>Cross-site Requests</span></h2>
* <p><a href="/en/docs/Cross-Site_XMLHttpRequest" shape="rect" title="Cross-Site XMLHttpRequest">Cross-site XMLHttpRequests</a> <span style="border: 1px solid #818151; background-color: #FFFFE1; font-size: 9px; vertical-align: text-top;">New in <a href="/en/docs/Firefox_3_for_developers" shape="rect" title="Firefox 3 for developers">Firefox 3</a></span> are available using the <a href="http://www.w3.org/TR/access-control/" rel="nofollow" shape="rect" title="http://www.w3.org/TR/access-control/">W3C Access Control</a> working draft specification. There are two different ways to use this functionality. The first is through the addition of a new Access-Control header (which is usable for all resource types and when you have a greater of level control over your web server). The second is an access-control processing instruction (which is only useful for XML documents). More information can be found <a href="/en/docs/Cross-Site_XMLHttpRequest" shape="rect" title="Cross-Site XMLHttpRequest">here</a>.
* </p>
* <h2> <span>References</span></h2>
* <ol><li> <a href="/en/docs/AJAX:Getting_Started" shape="rect" title="AJAX:Getting Started">MDC AJAX introduction</a>
* </li><li> <a href="http://www.peej.co.uk/articles/rich-user-experience.html" rel="nofollow" shape="rect" title="http://www.peej.co.uk/articles/rich-user-experience.html">XMLHttpRequest - REST and the Rich User Experience</a>
* </li><li> <a href="http://www.xulplanet.com/references/objref/XMLHttpRequest.html" rel="nofollow" shape="rect" title="http://www.xulplanet.com/references/objref/XMLHttpRequest.html">XULPlanet documentation</a>
* </li><li> <a href="http://msdn.microsoft.com/library/default.asp?url=/library/en-us/xmlsdk/html/xmobjxmlhttprequest.asp" rel="nofollow" shape="rect" title="http://msdn.microsoft.com/library/default.asp?url=/library/en-us/xmlsdk/html/xmobjxmlhttprequest.asp">Microsoft documentation</a>
* </li><li> <a href="http://developer.apple.com/internet/webcontent/xmlhttpreq.html" rel="nofollow" shape="rect" title="http://developer.apple.com/internet/webcontent/xmlhttpreq.html">Apple developers' reference</a>
* </li><li> <a href="http://jibbering.com/2002/4/httprequest.html" rel="nofollow" shape="rect" title="http://jibbering.com/2002/4/httprequest.html">"Using the XMLHttpRequest Object" (jibbering.com)</a>
* </li><li> <a href="http://www.w3.org/TR/XMLHttpRequest/" rel="nofollow" shape="rect" title="http://www.w3.org/TR/XMLHttpRequest/">The XMLHttpRequest Object: W3C Working Draft</a>
* </li></ol>
* 
* <div id="catlinks"><p><a href="/en/docs/Special:Categories" shape="rect" title="Special:Categories">Categories</a>: <span dir="ltr"><a href="/en/docs/Category:AJAX" shape="rect" title="Category:AJAX">AJAX</a></span> | <span dir="ltr"><a href="/en/docs/Category:XMLHttpRequest" shape="rect" title="Category:XMLHttpRequest">XMLHttpRequest</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
var XMLHttpRequest = {  // COMPAT=IE7|FF1|FF2|FF3|OPERA|SAFARI2|SAFARI3|KONQ
  // This is just a stub for a builtin native JavaScript object.
/** Cancel the current HTTP request */
abort: function() {
  // This is just a stub for a builtin native JavaScript object.
},
/** Returns the complete set of HTTP headers as a string.
@type String
 */
getAllResponseHeaders: function() {
  // This is just a stub for a builtin native JavaScript object.
},
/** Returns the value of the specified header.
* @param header The name of the header
@type String
 */
getResponseHeader: function(header) {
  // This is just a stub for a builtin native JavaScript object.
},
/** Specifies a reference to an event handler for an event that fires at every state change. */
onreadystatechange: undefined,
/** Specifies the method, URL, and other optional attributes of a request.
* @param method HTTP method such as GET or POST.
* @param url A relative or complete URL.
* @param async "true" for asynchronous requests.
* @param user Authentication user name
* @param password Authentication password. */
open: function(method,url) {
  // This is just a stub for a builtin native JavaScript object.
},
/** Specifies the method, URL, and other optional attributes of a request.
* @param method HTTP method such as GET or POST.
* @param url A relative or complete URL.
* @param async "true" for asynchronous requests.
* @param user Authentication user name
* @param password Authentication password. */
open: function(method,url,async) {
  // This is just a stub for a builtin native JavaScript object.
},
/** Specifies the method, URL, and other optional attributes of a request.
* @param method HTTP method such as GET or POST.
* @param url A relative or complete URL.
* @param async "true" for asynchronous requests.
* @param user Authentication user name
* @param password Authentication password. */
open: function(method,url,async,user) {
  // This is just a stub for a builtin native JavaScript object.
},
/** Specifies the method, URL, and other optional attributes of a request.
* @param method HTTP method such as GET or POST.
* @param url A relative or complete URL.
* @param async "true" for asynchronous requests.
* @param user Authentication user name
* @param password Authentication password. */
open: function(method,url,async,user,password) {
  // This is just a stub for a builtin native JavaScript object.
},
/** The XMLHttpRequest  object can be in five states: UNSENT (0), OPENED (1), HEADERS_RECEIVED (2), LOADING (3) and DONE (4). The current state is exposed through the readyState attribute.
 */
readyState: undefined,
/** Returns the response as a binary encoded string (an array of unsigned bytes) */
responseBody: undefined,
/** Returns the response as a String
@type String
 */
responseText: undefined,
/** Returns the response as XML. This property returns an XML document object, which can be examined and parsed using W3C DOM node tree methods and properties. */
responseXML: undefined,
/** Send the request
* @param content The content to be sent
 */
send: function(content) {
  // This is just a stub for a builtin native JavaScript object.
},
/** Sets the given header with the given value.
* @param header The name of the header
* @param value The value of the header */
setRequestHeader: function(header,value) {
  // This is just a stub for a builtin native JavaScript object.
},
/** Returns the HTTP status code (such as 404 for not found and 200 for OK)
@type Number */
status: undefined,
/** Returns the HTTP status as a String (such as "OK" or "Not Found")
@type String
 */
statusText: undefined,
};
/**
* <p>
* <code>XMLHttpRequest</code> is a <a href="/en/docs/JavaScript" shape="rect" title="JavaScript">JavaScript</a> object that was created by Microsoft and adopted by Mozilla.  You can use it to easily retrieve data via HTTP.  Despite its name, it can be used for more than just XML documents.  In Gecko, this object implements the <code><a href="/en/docs/nsIJSXMLHttpRequest" shape="rect" title="nsIJSXMLHttpRequest">nsIJSXMLHttpRequest</a></code> and <code><a href="/en/docs/nsIXMLHttpRequest" shape="rect" title="nsIXMLHttpRequest">nsIXMLHttpRequest</a></code> interfaces.  Recent versions of Gecko have some changes to this object, see <a href="/en/docs/XMLHttpRequest_changes_for_Gecko1.8" shape="rect" title="XMLHttpRequest changes for Gecko1.8">XMLHttpRequest changes for Gecko1.8</a>.
* </p>
* 
* <h2> <span>Basic Usage</span></h2>
* <p>Using <code>XMLHttpRequest</code> is very simple.  You create an instance of the object, open a URL, and send the request.  The HTTP status code of the result, as well as the result document are available in the request object afterwards.
* </p>
* <div><b>Note:</b> Versions of Firefox prior to version 3 always send the request using UTF-8 encoding; <a href="/en/docs/Firefox_3" shape="rect" title="Firefox 3">Firefox 3</a> properly sends the document using the encoding specified by <code>data.xmlEncoding</code>, or UTF-8 if no encoding is specified.</div>
* <h3> <span>Example</span></h3>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* var req = new XMLHttpRequest();
* req.open('GET', 'http://www.mozilla.org/', false);
* req.send(null);
* if(req.status == 200)
* dump(req.responseText);
* </pre>
* <div><b>Note:</b> This example works synchronously, so it will block the user interface if you call this from your JavaScript.  You should not use this in practice.</div>
* <h3> <span>Example with non http protocol</span></h3>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* var req = new XMLHttpRequest();
* req.open('GET', 'file:///home/user/file.json', false);
* req.send(null);
* if(req.status == 0)
* dump(req.responseText);
* </pre>
* <div><b>Note:</b> file:/// and ftp:// do not return HTTP status, which is why they return zero for <code>status</code> and an empty string for <code>statusText</code>. Refer to <a href="https://bugzilla.mozilla.org/show_bug.cgi?id=331610" rel="nofollow" shape="rect" title="https://bugzilla.mozilla.org/show_bug.cgi?id=331610">bug 331610</a> for more insight.</div>
* <h2> <span>Asynchronous Usage</span></h2>
* <p>If you intend to use <code>XMLHttpRequest</code> from an extension, you should let it load asynchronously.  In asynchronous usage, you get a callback when the data has been received, which lets the browser continue to work as normal while your request is happening.
* </p>
* <h3> <span>Example</span></h3>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* var req = new XMLHttpRequest();
* req.open('GET', 'http://www.mozilla.org/', true);
* req.onreadystatechange = function (aEvt) {
* if (req.readyState == 4) {
* if(req.status == 200)
* dump(req.responseText);
* else
* dump("Error loading page\n");
* }
* };
* req.send(null);
* </pre>
* <h3> <span>Monitoring progress</span></h3>
* <p><code>XMLHttpRequest</code> provides the ability to listen to various events that can occur while the request is being processed.  This includes periodic progress notifications, error notifications, and so forth.
* </p><p>If, for example, you wish to provide progress information to the user while the document is being received, you can use code like this:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* function onProgress(e) {
* var percentComplete = (e.position / e.totalSize)*100;
* ...
* }
* 
* function onError(e) {
* alert("Error " + e.target.status + " occurred while receiving the document.");
* }
* 
* function onLoad(e) {
* // ...
* }
* // ...
* var req = new XMLHttpRequest();
* req.onprogress = onProgress;
* req.open("GET", url, true);
* req.onload = onLoad;
* req.onerror = onError;
* req.send(null);
* </pre>
* <p>The <code>onprogress</code> event's attributes, <code>position</code> and <code>totalSize</code>, indicate the current number of bytes received and the total number of bytes expected, respectively.
* </p><p>All of these events have their <code>target</code> attribute set to the <code>XMLHttpRequest</code> they correspond to.
* </p>
* <div><b>Note:</b> <a href="/en/docs/Firefox_3" shape="rect" title="Firefox 3">Firefox 3</a> properly ensures that the values of the <code>target</code>, <code>currentTarget</code>, and <code>this</code> fields of the event object are set to reference the correct objects when calling event handlers for XML documents represented by <code>XMLDocument</code>.  See <a href="https://bugzilla.mozilla.org/show_bug.cgi?id=198595" rel="nofollow" shape="rect" title="https://bugzilla.mozilla.org/show_bug.cgi?id=198595">bug 198595</a> for details.</div>
* <h2> <span>Other Properties and Methods</span></h2>
* <p>In addition to the properties and methods shown above, there are other useful properties and methods on the request object.
* </p>
* <h3> <span>responseXML</span></h3>
* <p>If you load an <a href="/en/docs/XML" shape="rect" title="XML">XML</a> document, the <code>responseXML</code> property will contain the document as an <code>XmlDocument</code> object that you can manipulate using DOM methods.  If the server sends well-formed XML but does not specify an XML Content-Type header, you can use <code><a href="/en/docs/XMLHttpRequest#overrideMimeType.28.29" shape="rect" title="XMLHttpRequest">overrideMimeType()</a></code> to force the document to be parsed as XML. If the server does not send well-formed XML, <code>responseXML</code> will be null regardless of any content type override.
* </p>
* <h3> <span>overrideMimeType()</span></h3>
* This method can be used to force a document to be handled as a particular content type.  You will generally want to use when you want to use <code>responseXML</code> and the server sends you <a href="/en/docs/XML" shape="rect" title="XML">XML</a>, but does not send the correct Content-Type header.  <div><b>Note:</b> This method must be called before calling <code>send()</code>.</div>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* var req = new XMLHttpRequest();
* req.open('GET', 'http://www.mozilla.org/', true);
* req.overrideMimeType('text/xml');
* req.send(null);
* </pre>
* <h3> <span>setRequestHeader()</span></h3>
* <p>This method can be used to set an HTTP header on the request before you send it.
* </p>
* <div><b>Note:</b> You must call <code>open()</code> before calling this method.</div>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* var req = new XMLHttpRequest();
* req.open('GET', 'http://www.mozilla.org/', true);
* req.setRequestHeader("X-Foo", "Bar");
* req.send(null);
* </pre>
* <h3> <span>getResponseHeader()</span></h3>
* <p>This method can be used to get an HTTP header from the server response.
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* var req = new XMLHttpRequest();
* req.open('GET', 'http://www.mozilla.org/', true);
* req.send(null);
* dump("Content-Type: " + req.getResponseHeader("Content-Type") + "\n");
* </pre>
* <h2> <span> Using from XPCOM components </span></h2>
* <div><b>Note:</b> Changes are required if you use XMLHttpRequest from a JavaScript XPCOM component.</div>
* <p>XMLHttpRequest cannot be instantiated using the <code>XMLHttpRequest()</code> constructor from a JavaScript XPCOM component. The constructor is not defined inside components and the code results in an error. You'll need to create and use it using a different syntax.
* </p><p>Instead of this:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* var req = new XMLHttpRequest();
* req.onprogress = onProgress;
* req.onload = onLoad;
* req.onerror = onError;
* req.open("GET", url, true);
* req.send(null);
* </pre>
* <p>Do this:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* var request = Components.
* classes["@mozilla.org/xmlextras/xmlhttprequest;1"].
* createInstance();
* 
* // QI the object to nsIDOMEventTarget to set event handlers on it:
* 
* request.QueryInterface(Components.interfaces.nsIDOMEventTarget);
* request.addEventListener("progress", function(evt) { ... }, false);
* request.addEventListener("load", function(evt) { ... }, false);
* request.addEventListener("error", function(evt) { ... }, false);
* 
* // QI it to nsIXMLHttpRequest to open and send the request:
* 
* request.QueryInterface(Components.interfaces.nsIXMLHttpRequest);
* request.open("GET", "http://www.example.com/", true);
* request.send(null);
* </pre>
* <h2> <span>Limited Number Of Simultaneous xmlHttpRequest Connections</span></h2>
* <p>The about:config preference: network.http.max-persistent-connections-per-server limits the number of connections to 2 by default.  Some interactive web pages using xmlHttpRequest may keep a connection open.  Opening two or three of these pages in different tabs or on different windows may cause the browser to hang in such a way that the window no longer repaints and browser controls don't respond.
* </p>
* <h2> <span>Cross-site Requests</span></h2>
* <p><a href="/en/docs/Cross-Site_XMLHttpRequest" shape="rect" title="Cross-Site XMLHttpRequest">Cross-site XMLHttpRequests</a> <span style="border: 1px solid #818151; background-color: #FFFFE1; font-size: 9px; vertical-align: text-top;">New in <a href="/en/docs/Firefox_3_for_developers" shape="rect" title="Firefox 3 for developers">Firefox 3</a></span> are available using the <a href="http://www.w3.org/TR/access-control/" rel="nofollow" shape="rect" title="http://www.w3.org/TR/access-control/">W3C Access Control</a> working draft specification. There are two different ways to use this functionality. The first is through the addition of a new Access-Control header (which is usable for all resource types and when you have a greater of level control over your web server). The second is an access-control processing instruction (which is only useful for XML documents). More information can be found <a href="/en/docs/Cross-Site_XMLHttpRequest" shape="rect" title="Cross-Site XMLHttpRequest">here</a>.
* </p>
* <h2> <span>References</span></h2>
* <ol><li> <a href="/en/docs/AJAX:Getting_Started" shape="rect" title="AJAX:Getting Started">MDC AJAX introduction</a>
* </li><li> <a href="http://www.peej.co.uk/articles/rich-user-experience.html" rel="nofollow" shape="rect" title="http://www.peej.co.uk/articles/rich-user-experience.html">XMLHttpRequest - REST and the Rich User Experience</a>
* </li><li> <a href="http://www.xulplanet.com/references/objref/XMLHttpRequest.html" rel="nofollow" shape="rect" title="http://www.xulplanet.com/references/objref/XMLHttpRequest.html">XULPlanet documentation</a>
* </li><li> <a href="http://msdn.microsoft.com/library/default.asp?url=/library/en-us/xmlsdk/html/xmobjxmlhttprequest.asp" rel="nofollow" shape="rect" title="http://msdn.microsoft.com/library/default.asp?url=/library/en-us/xmlsdk/html/xmobjxmlhttprequest.asp">Microsoft documentation</a>
* </li><li> <a href="http://developer.apple.com/internet/webcontent/xmlhttpreq.html" rel="nofollow" shape="rect" title="http://developer.apple.com/internet/webcontent/xmlhttpreq.html">Apple developers' reference</a>
* </li><li> <a href="http://jibbering.com/2002/4/httprequest.html" rel="nofollow" shape="rect" title="http://jibbering.com/2002/4/httprequest.html">"Using the XMLHttpRequest Object" (jibbering.com)</a>
* </li><li> <a href="http://www.w3.org/TR/XMLHttpRequest/" rel="nofollow" shape="rect" title="http://www.w3.org/TR/XMLHttpRequest/">The XMLHttpRequest Object: W3C Working Draft</a>
* </li></ol>
* 
* <div id="catlinks"><p><a href="/en/docs/Special:Categories" shape="rect" title="Special:Categories">Categories</a>: <span dir="ltr"><a href="/en/docs/Category:AJAX" shape="rect" title="Category:AJAX">AJAX</a></span> | <span dir="ltr"><a href="/en/docs/Category:XMLHttpRequest" shape="rect" title="Category:XMLHttpRequest">XMLHttpRequest</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
function XMLHttpRequest() {}; // COMPAT=IE7|FF1|FF2|FF3|OPERA|SAFARI2|SAFARI3|KONQ

