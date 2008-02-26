/**
* <p>This chapter provides a brief reference for the general methods, properties, and events available to most HTML and XML elements in the Gecko DOM.
* </p><p>Various W3C specifications apply to elements:
* </p>
* <ul><li> <a href="http://www.w3.org/TR/DOM-Level-2-Core/" rel="nofollow" shape="rect" title="http://www.w3.org/TR/DOM-Level-2-Core/">DOM Core Specification</a>—describes the core interfaces shared by most DOM objects in HTML and XML documents
* </li><li> <a href="http://www.w3.org/TR/DOM-Level-2-HTML/" rel="nofollow" shape="rect" title="http://www.w3.org/TR/DOM-Level-2-HTML/">DOM HTML Specification</a>—describes interfaces for objects in HTML and XHTML documents that build on the core specification
* </li><li> <a href="http://www.w3.org/TR/DOM-Level-2-Events/" rel="nofollow" shape="rect" title="http://www.w3.org/TR/DOM-Level-2-Events/">DOM Events Specification</a>—describes events shared by most DOM objects, building on the DOM Core and <a href="http://www.w3.org/TR/DOM-Level-2-Views/" rel="nofollow" shape="rect" title="http://www.w3.org/TR/DOM-Level-2-Views/">Views</a> specifications
* </li></ul>
* <p>The articles listed here span the above and include links to the appropriate W3C DOM specification.
* </p><p>While these interfaces are generally shared by most HTML and XML elements, there are more specialized interfaces for particular objects listed in the DOM HTML Specification—for example the <a href="table" shape="rect" title="DOM:table">HTML Table Element</a> and <a href="form" shape="rect" title="DOM:form">HTML Form Element</a> interfaces.
* </p>
* 
* <h2> <span> Properties </span></h2>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <th colspan="1" rowspan="1">Name</th>
* <th colspan="1" rowspan="1">Description</th>
* <th colspan="1" rowspan="1">Type</th>
* <th colspan="1" rowspan="1">Availability</th>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="DOM:element.attributes" shape="rect" title="DOM:element.attributes">attributes</a></code></td>
* <td colspan="1" rowspan="1">All attributes associated with an element.</td>
* <td colspan="1" rowspan="1"><code><a href="http://developer.mozilla.org/en/docs/index.php?title=NamedNodeMap&amp;action=edit" shape="rect" title="DOM:NamedNodeMap">NamedNodeMap</a><a href="http://www.xulplanet.com/references/objref/NamedNodeMap.html" rel="nofollow" shape="rect" title="http://www.xulplanet.com/references/objref/NamedNodeMap.html">[1]</a></code></td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="DOM" shape="rect" title="DOM">All</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.childNodes" shape="rect" title="DOM:element.childNodes">childNodes</a></code></td>
* <td colspan="1" rowspan="1">All child nodes of an element.</td>
* <td colspan="1" rowspan="1"><code><a href="http://developer.mozilla.org/en/docs/index.php?title=NodeList&amp;action=edit" shape="rect" title="DOM:NodeList">NodeList</a><a href="http://www.xulplanet.com/references/objref/NodeList.html" rel="nofollow" shape="rect" title="http://www.xulplanet.com/references/objref/NodeList.html">[2]</a></code></td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="DOM" shape="rect" title="DOM">All</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.className" shape="rect" title="DOM:element.className">className</a></code></td>
* <td colspan="1" rowspan="1">Gets/sets the class of the element.</td>
* <td colspan="1" rowspan="1"><code><a href="http://developer.mozilla.org/en/docs/Global_Objects:String" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String">String</a></code></td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="http://developer.mozilla.org/en/docs/HTML" shape="rect" title="HTML">HTML</a>, <a href="http://developer.mozilla.org/en/docs/XUL" shape="rect" title="XUL">XUL</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.clientHeight" shape="rect" title="DOM:element.clientHeight">clientHeight</a></code></td>
* <td colspan="1" rowspan="1">The inner height of an element.</td>
* <td colspan="1" rowspan="1"><code><a href="http://developer.mozilla.org/en/docs/Global_Objects:Number" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Number">Number</a></code></td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="http://developer.mozilla.org/en/docs/HTML" shape="rect" title="HTML">HTML</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.clientLeft" shape="rect" title="DOM:element.clientLeft">clientLeft</a></code></td>
* <td colspan="1" rowspan="1">The width of the left border of an element.</td>
* <td colspan="1" rowspan="1"><code><a href="http://developer.mozilla.org/en/docs/Global_Objects:Number" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Number">Number</a></code></td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="http://developer.mozilla.org/en/docs/HTML" shape="rect" title="HTML">HTML</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.clientTop" shape="rect" title="DOM:element.clientTop">clientTop</a></code></td>
* <td colspan="1" rowspan="1">The width of the top border of an element.</td>
* <td colspan="1" rowspan="1"><code><a href="http://developer.mozilla.org/en/docs/Global_Objects:Number" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Number">Number</a></code></td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="http://developer.mozilla.org/en/docs/HTML" shape="rect" title="HTML">HTML</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.clientWidth" shape="rect" title="DOM:element.clientWidth">clientWidth</a></code></td>
* <td colspan="1" rowspan="1">The inner width of an element.</td>
* <td colspan="1" rowspan="1"><code><a href="http://developer.mozilla.org/en/docs/Global_Objects:Number" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Number">Number</a></code></td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="http://developer.mozilla.org/en/docs/HTML" shape="rect" title="HTML">HTML</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.dir" shape="rect" title="DOM:element.dir">dir</a></code></td>
* <td colspan="1" rowspan="1">Gets/sets the directionality of the element.</td>
* <td colspan="1" rowspan="1"><code><a href="http://developer.mozilla.org/en/docs/Global_Objects:String" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String">String</a></code></td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="http://developer.mozilla.org/en/docs/HTML" shape="rect" title="HTML">HTML</a>, <a href="http://developer.mozilla.org/en/docs/XUL" shape="rect" title="XUL">XUL</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.firstChild" shape="rect" title="DOM:element.firstChild">firstChild</a></code></td>
* <td colspan="1" rowspan="1">The first direct child node of an element, or <code>null</code> if this element has no child nodes.</td>
* <td colspan="1" rowspan="1"><code><a href="Node" shape="rect" title="DOM:Node">Node</a></code></td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="DOM" shape="rect" title="DOM">All</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.id" shape="rect" title="DOM:element.id">id</a></code></td>
* <td colspan="1" rowspan="1">Gets/sets the id of the element.</td>
* <td colspan="1" rowspan="1"><code><a href="http://developer.mozilla.org/en/docs/Global_Objects:String" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String">String</a></code></td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="http://developer.mozilla.org/en/docs/HTML" shape="rect" title="HTML">HTML</a>, <a href="http://developer.mozilla.org/en/docs/XUL" shape="rect" title="XUL">XUL</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.innerHTML" shape="rect" title="DOM:element.innerHTML">innerHTML</a></code></td>
* <td colspan="1" rowspan="1">Gets/sets the markup and content of the element.</td>
* <td colspan="1" rowspan="1"><code><a href="http://developer.mozilla.org/en/docs/Global_Objects:String" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String">String</a></code></td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="http://developer.mozilla.org/en/docs/HTML" shape="rect" title="HTML">HTML</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.lang" shape="rect" title="DOM:element.lang">lang</a></code></td>
* <td colspan="1" rowspan="1">Gets/sets the language of an element's attributes, text, and element contents.</td>
* <td colspan="1" rowspan="1"><code><a href="http://developer.mozilla.org/en/docs/Global_Objects:String" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String">String</a></code></td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="http://developer.mozilla.org/en/docs/HTML" shape="rect" title="HTML">HTML</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.lastChild" shape="rect" title="DOM:element.lastChild">lastChild</a></code></td>
* <td colspan="1" rowspan="1">The last direct child node of an element, or <code>null</code> if this element has no child nodes.</td>
* <td colspan="1" rowspan="1"><code><a href="Node" shape="rect" title="DOM:Node">Node</a></code></td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="DOM" shape="rect" title="DOM">All</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.localName" shape="rect" title="DOM:element.localName">localName</a></code></td>
* <td colspan="1" rowspan="1">The local part of the qualified name of an element.</td>
* <td colspan="1" rowspan="1"><code><a href="http://developer.mozilla.org/en/docs/Global_Objects:String" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String">String</a></code></td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="DOM" shape="rect" title="DOM">All</a></small></td>
* </tr>
* <tr>
* <th colspan="1" rowspan="1">Name</th>
* <th colspan="1" rowspan="1">Description</th>
* <th colspan="1" rowspan="1">Type</th>
* <th colspan="1" rowspan="1">Availability</th>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.name" shape="rect" title="DOM:element.name">name</a></code></td>
* <td colspan="1" rowspan="1">Gets/sets the name attribute of an element.</td>
* <td colspan="1" rowspan="1"><code><a href="http://developer.mozilla.org/en/docs/Global_Objects:String" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String">String</a></code></td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="http://developer.mozilla.org/en/docs/HTML" shape="rect" title="HTML">HTML</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.namespaceURI" shape="rect" title="DOM:element.namespaceURI">namespaceURI</a></code></td>
* <td colspan="1" rowspan="1">The namespace URI of this node, or <code>null</code> if it is unspecified.</td>
* <td colspan="1" rowspan="1"><code><a href="http://developer.mozilla.org/en/docs/Global_Objects:String" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String">String</a></code></td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="DOM" shape="rect" title="DOM">All</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.nextSibling" shape="rect" title="DOM:element.nextSibling">nextSibling</a></code></td>
* <td colspan="1" rowspan="1">The node immediately following the given one in the tree, or <code>null</code> if there is no sibling node.</td>
* <td colspan="1" rowspan="1"><code><a href="Node" shape="rect" title="DOM:Node">Node</a></code></td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="DOM" shape="rect" title="DOM">All</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.nodeName" shape="rect" title="DOM:element.nodeName">nodeName</a></code></td>
* <td colspan="1" rowspan="1">The name of the node.</td>
* <td colspan="1" rowspan="1"><code><a href="http://developer.mozilla.org/en/docs/Global_Objects:String" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String">String</a></code></td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="DOM" shape="rect" title="DOM">All</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.nodeType" shape="rect" title="DOM:element.nodeType">nodeType</a></code></td>
* <td colspan="1" rowspan="1">A number representing the type of the node. Is always equal to <code>1</code> for DOM elements.</td>
* <td colspan="1" rowspan="1"><code><a href="http://developer.mozilla.org/en/docs/Global_Objects:Number" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Number">Number</a></code></td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="DOM" shape="rect" title="DOM">All</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.nodeValue" shape="rect" title="DOM:element.nodeValue">nodeValue</a></code></td>
* <td colspan="1" rowspan="1">The value of the node. Is always equal to <code>null</code> for DOM elements.</td>
* <td colspan="1" rowspan="1"><code><a href="http://developer.mozilla.org/en/docs/Global_Objects:String" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String">String</a></code></td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="DOM" shape="rect" title="DOM">All</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.offsetHeight" shape="rect" title="DOM:element.offsetHeight">offsetHeight</a></code></td>
* <td colspan="1" rowspan="1">The height of an element, relative to the layout.</td>
* <td colspan="1" rowspan="1"><code><a href="http://developer.mozilla.org/en/docs/Global_Objects:Number" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Number">Number</a></code></td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="http://developer.mozilla.org/en/docs/HTML" shape="rect" title="HTML">HTML</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.offsetLeft" shape="rect" title="DOM:element.offsetLeft">offsetLeft</a></code></td>
* <td colspan="1" rowspan="1">The distance from this element's left border to its <code>offsetParent</code>'s left border.</td>
* <td colspan="1" rowspan="1"><code><a href="http://developer.mozilla.org/en/docs/Global_Objects:Number" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Number">Number</a></code></td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="http://developer.mozilla.org/en/docs/HTML" shape="rect" title="HTML">HTML</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.offsetParent" shape="rect" title="DOM:element.offsetParent">offsetParent</a></code></td>
* <td colspan="1" rowspan="1">The element from which all offset calculations are currently computed.</td>
* <td colspan="1" rowspan="1"><code><strong>Element</strong></code></td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="http://developer.mozilla.org/en/docs/HTML" shape="rect" title="HTML">HTML</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.offsetTop" shape="rect" title="DOM:element.offsetTop">offsetTop</a></code></td>
* <td colspan="1" rowspan="1">The distance from this element's top border to its <code>offsetParent</code>'s top border.</td>
* <td colspan="1" rowspan="1"><code><a href="http://developer.mozilla.org/en/docs/Global_Objects:Number" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Number">Number</a></code></td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="http://developer.mozilla.org/en/docs/HTML" shape="rect" title="HTML">HTML</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.offsetWidth" shape="rect" title="DOM:element.offsetWidth">offsetWidth</a></code></td>
* <td colspan="1" rowspan="1">The width of an element, relative to the layout.</td>
* <td colspan="1" rowspan="1"><code><a href="http://developer.mozilla.org/en/docs/Global_Objects:Number" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Number">Number</a></code></td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="http://developer.mozilla.org/en/docs/HTML" shape="rect" title="HTML">HTML</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.ownerDocument" shape="rect" title="DOM:element.ownerDocument">ownerDocument</a></code></td>
* <td colspan="1" rowspan="1">The document that this node is in, or <code>null</code> if the node is not inside of one.</td>
* <td colspan="1" rowspan="1"><code><a href="document" shape="rect" title="DOM:document">Document</a></code></td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="DOM" shape="rect" title="DOM">All</a></small></td>
* </tr>
* <tr>
* <th colspan="1" rowspan="1">Name</th>
* <th colspan="1" rowspan="1">Description</th>
* <th colspan="1" rowspan="1">Type</th>
* <th colspan="1" rowspan="1">Availability</th>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.parentNode" shape="rect" title="DOM:element.parentNode">parentNode</a></code></td>
* <td colspan="1" rowspan="1">The parent element of this node, or <code>null</code> if the node is not inside of a <a href="document" shape="rect" title="DOM:document">DOM Document</a>.</td>
* <td colspan="1" rowspan="1"><code><a href="Node" shape="rect" title="DOM:Node">Node</a></code></td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="DOM" shape="rect" title="DOM">All</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.prefix" shape="rect" title="DOM:element.prefix">prefix</a></code></td>
* <td colspan="1" rowspan="1">The namespace prefix of the node, or <code>null</code> if no prefix is specified.</td>
* <td colspan="1" rowspan="1"><code><a href="http://developer.mozilla.org/en/docs/Global_Objects:String" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String">String</a></code></td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="DOM" shape="rect" title="DOM">All</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.previousSibling" shape="rect" title="DOM:element.previousSibling">previousSibling</a></code></td>
* <td colspan="1" rowspan="1">The node immediately preceding the given one in the tree, or <code>null</code> if there is no sibling node.</td>
* <td colspan="1" rowspan="1"><code><a href="Node" shape="rect" title="DOM:Node">Node</a></code></td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="DOM" shape="rect" title="DOM">All</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.scrollHeight" shape="rect" title="DOM:element.scrollHeight">scrollHeight</a></code></td>
* <td colspan="1" rowspan="1">The scroll view height of an element.</td>
* <td colspan="1" rowspan="1"><code><a href="http://developer.mozilla.org/en/docs/Global_Objects:Number" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Number">Number</a></code></td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="http://developer.mozilla.org/en/docs/HTML" shape="rect" title="HTML">HTML</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.scrollLeft" shape="rect" title="DOM:element.scrollLeft">scrollLeft</a></code></td>
* <td colspan="1" rowspan="1">Gets/sets the left scroll offset of an element.</td>
* <td colspan="1" rowspan="1"><code><a href="http://developer.mozilla.org/en/docs/Global_Objects:Number" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Number">Number</a></code></td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="http://developer.mozilla.org/en/docs/HTML" shape="rect" title="HTML">HTML</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.scrollTop" shape="rect" title="DOM:element.scrollTop">scrollTop</a></code></td>
* <td colspan="1" rowspan="1">Gets/sets the top scroll offset of an element.</td>
* <td colspan="1" rowspan="1"><code><a href="http://developer.mozilla.org/en/docs/Global_Objects:Number" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Number">Number</a></code></td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="http://developer.mozilla.org/en/docs/HTML" shape="rect" title="HTML">HTML</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.scrollWidth" shape="rect" title="DOM:element.scrollWidth">scrollWidth</a></code></td>
* <td colspan="1" rowspan="1">The scroll view width of an element.</td>
* <td colspan="1" rowspan="1"><code><a href="http://developer.mozilla.org/en/docs/Global_Objects:Number" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Number">Number</a></code></td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="http://developer.mozilla.org/en/docs/HTML" shape="rect" title="HTML">HTML</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.style" shape="rect" title="DOM:element.style">style</a></code></td>
* <td colspan="1" rowspan="1">An object representing the declarations of an element's style attributes.</td>
* <td colspan="1" rowspan="1"><code><a href="http://developer.mozilla.org/en/docs/index.php?title=CSSStyleDeclaration&amp;action=edit" shape="rect" title="DOM:CSSStyleDeclaration">CSSStyleDeclaration</a><a href="http://www.xulplanet.com/references/objref/CSSStyleDeclaration.html" rel="nofollow" shape="rect" title="http://www.xulplanet.com/references/objref/CSSStyleDeclaration.html">[3]</a></code></td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="http://developer.mozilla.org/en/docs/HTML" shape="rect" title="HTML">HTML</a>, <a href="http://developer.mozilla.org/en/docs/XUL" shape="rect" title="XUL">XUL</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.tabIndex" shape="rect" title="DOM:element.tabIndex">tabIndex</a></code></td>
* <td colspan="1" rowspan="1">Gets/sets the position of the element in the tabbing order.</td>
* <td colspan="1" rowspan="1"><code><a href="http://developer.mozilla.org/en/docs/Global_Objects:Number" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Number">Number</a></code></td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="http://developer.mozilla.org/en/docs/HTML" shape="rect" title="HTML">HTML</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.tagName" shape="rect" title="DOM:element.tagName">tagName</a></code></td>
* <td colspan="1" rowspan="1">The name of the tag for the given element.</td>
* <td colspan="1" rowspan="1"><code><a href="http://developer.mozilla.org/en/docs/Global_Objects:String" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String">String</a></code></td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="DOM" shape="rect" title="DOM">All</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.textContent" shape="rect" title="DOM:element.textContent">textContent</a></code></td>
* <td colspan="1" rowspan="1">Gets/sets the textual contents of an element and all its descendants.</td>
* <td colspan="1" rowspan="1"><code><a href="http://developer.mozilla.org/en/docs/Global_Objects:String" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String">String</a></code></td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="DOM" shape="rect" title="DOM">All</a></small></td>
* </tr>
* </table>
* <h2> <span> Methods </span></h2>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <th colspan="1" rowspan="1">Name &amp; Description</th>
* <th colspan="1" rowspan="1">Return</th>
* <th colspan="1" rowspan="1">Availability</th>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="DOM:element.addEventListener" shape="rect" title="DOM:element.addEventListener">addEventListener</a>( <a href="http://developer.mozilla.org/en/docs/Global_Objects:String" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String">type</a>, <a href="http://developer.mozilla.org/en/docs/Global_Objects:Function" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Function">listener</a>, <a href="http://developer.mozilla.org/en/docs/Global_Objects:Boolean" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Boolean">useCapture</a> )</code>
* Register an event handler to a specific event type on the element.</td>
* <td colspan="1" rowspan="1">-</td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="event" shape="rect" title="DOM:event">All</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.appendChild" shape="rect" title="DOM:element.appendChild">appendChild</a>( <a href="Node" shape="rect" title="DOM:Node">appendedNode</a> )</code>
* Insert a node as the last child node of this element.</td>
* <td colspan="1" rowspan="1"><a href="Node" shape="rect" title="DOM:Node">Node</a></td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="DOM" shape="rect" title="DOM">All</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.blur" shape="rect" title="DOM:element.blur">blur</a>()</code>
* Removes keyboard focus from the current element.</td>
* <td colspan="1" rowspan="1">-</td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="http://developer.mozilla.org/en/docs/HTML" shape="rect" title="HTML">HTML</a>, <a href="http://developer.mozilla.org/en/docs/XUL" shape="rect" title="XUL">XUL</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.click" shape="rect" title="DOM:element.click">click</a>()</code>
* Simulates a click on the current element.</td>
* <td colspan="1" rowspan="1">-</td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="http://developer.mozilla.org/en/docs/HTML" shape="rect" title="HTML">HTML</a>, <a href="http://developer.mozilla.org/en/docs/XUL" shape="rect" title="XUL">XUL</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.cloneNode" shape="rect" title="DOM:element.cloneNode">cloneNode</a>( <a href="http://developer.mozilla.org/en/docs/Global_Objects:Boolean" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Boolean">deep</a> )</code>
* Clone a node, and optionally, all of its contents.</td>
* <td colspan="1" rowspan="1"><a href="Node" shape="rect" title="DOM:Node">Node</a></td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="DOM" shape="rect" title="DOM">All</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.dispatchEvent" shape="rect" title="DOM:element.dispatchEvent">dispatchEvent</a>( <a href="event" shape="rect" title="DOM:event">event</a> )</code>
* Dispatch an event to this node in the DOM.</td>
* <td colspan="1" rowspan="1"><a href="http://developer.mozilla.org/en/docs/Global_Objects:Boolean" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Boolean">Boolean</a></td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="DOM" shape="rect" title="DOM">All</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.focus" shape="rect" title="DOM:element.focus">focus</a>()</code>
* Gives keyboard focus to the current element.</td>
* <td colspan="1" rowspan="1">-</td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="http://developer.mozilla.org/en/docs/HTML" shape="rect" title="HTML">HTML</a>, <a href="http://developer.mozilla.org/en/docs/XUL" shape="rect" title="XUL">XUL</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.getAttribute" shape="rect" title="DOM:element.getAttribute">getAttribute</a>( <a href="http://developer.mozilla.org/en/docs/Global_Objects:String" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String">name</a> )</code>
* Retrieve the value of the named attribute from the current node.</td>
* <td colspan="1" rowspan="1"><a href="http://developer.mozilla.org/en/docs/Global_Objects:Object" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object">Object</a></td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="DOM" shape="rect" title="DOM">All</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.getAttributeNS" shape="rect" title="DOM:element.getAttributeNS">getAttributeNS</a>( <a href="http://developer.mozilla.org/en/docs/Global_Objects:String" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String">namespace</a>, <a href="http://developer.mozilla.org/en/docs/Global_Objects:String" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String">name</a> )</code>
* Retrieve the value of the attribute with the specified name and namespace, from the current node.</td>
* <td colspan="1" rowspan="1"><a href="http://developer.mozilla.org/en/docs/Global_Objects:Object" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object">Object</a></td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="DOM" shape="rect" title="DOM">All</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.getAttributeNode" shape="rect" title="DOM:element.getAttributeNode">getAttributeNode</a>( <a href="http://developer.mozilla.org/en/docs/Global_Objects:String" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String">name</a> )</code>
* Retrieve the node representation of the named attribute from the current node.</td>
* <td colspan="1" rowspan="1"><a href="http://developer.mozilla.org/en/docs/index.php?title=Attr&amp;action=edit" shape="rect" title="DOM:Attr">Attr</a></td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="DOM" shape="rect" title="DOM">All</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.getAttributeNodeNS" shape="rect" title="DOM:element.getAttributeNodeNS">getAttributeNodeNS</a>( <a href="http://developer.mozilla.org/en/docs/Global_Objects:String" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String">namespace</a>, <a href="http://developer.mozilla.org/en/docs/Global_Objects:String" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String">name</a> )</code>
* Retrieve the node representation of the attribute with the specified name and namespace, from the current node.</td>
* <td colspan="1" rowspan="1"><a href="http://developer.mozilla.org/en/docs/index.php?title=Attr&amp;action=edit" shape="rect" title="DOM:Attr">Attr</a></td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="DOM" shape="rect" title="DOM">All</a></small></td>
* </tr>
* <tr>
* <th colspan="1" rowspan="1">Name &amp; Description</th>
* <th colspan="1" rowspan="1">Return</th>
* <th colspan="1" rowspan="1">Availability</th>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.getElementsByTagName" shape="rect" title="DOM:element.getElementsByTagName">getElementsByTagName</a>( <a href="http://developer.mozilla.org/en/docs/Global_Objects:String" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String">name</a> )</code>
* Retrieve a set of all descendant elements, of a particular tag name, from the current element.</td>
* <td colspan="1" rowspan="1"><a href="http://developer.mozilla.org/en/docs/index.php?title=NodeSet&amp;action=edit" shape="rect" title="DOM:NodeSet">NodeSet</a></td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="DOM" shape="rect" title="DOM">All</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.getElementsByTagNameNS" shape="rect" title="DOM:element.getElementsByTagNameNS">getElementsByTagNameNS</a>( <a href="http://developer.mozilla.org/en/docs/Global_Objects:String" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String">namespace</a>, <a href="http://developer.mozilla.org/en/docs/Global_Objects:String" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String">name</a> )</code>
* Retrieve a set of all descendant elements, of a particular tag name and namespace, from the current element.</td>
* <td colspan="1" rowspan="1"><a href="http://developer.mozilla.org/en/docs/index.php?title=NodeSet&amp;action=edit" shape="rect" title="DOM:NodeSet">NodeSet</a></td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="DOM" shape="rect" title="DOM">All</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.hasAttribute" shape="rect" title="DOM:element.hasAttribute">hasAttribute</a>( <a href="http://developer.mozilla.org/en/docs/Global_Objects:String" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String">name</a> )</code>
* Check if the element has the specified attribute, or not.</td>
* <td colspan="1" rowspan="1"><a href="http://developer.mozilla.org/en/docs/Global_Objects:Boolean" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Boolean">Boolean</a></td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="DOM" shape="rect" title="DOM">All</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.hasAttributeNS" shape="rect" title="DOM:element.hasAttributeNS">hasAttributeNS</a>( <a href="http://developer.mozilla.org/en/docs/Global_Objects:String" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String">namespace</a>, <a href="http://developer.mozilla.org/en/docs/Global_Objects:String" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String">name</a> )</code>
* Check if the element has the specified attribute, in the specified namespace, or not.</td>
* <td colspan="1" rowspan="1"><a href="http://developer.mozilla.org/en/docs/Global_Objects:Boolean" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Boolean">Boolean</a></td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="DOM" shape="rect" title="DOM">All</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.hasAttributes" shape="rect" title="DOM:element.hasAttributes">hasAttributes</a>()</code>
* Check if the element has any attributes, or not.</td>
* <td colspan="1" rowspan="1"><a href="http://developer.mozilla.org/en/docs/Global_Objects:Boolean" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Boolean">Boolean</a></td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="DOM" shape="rect" title="DOM">All</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.hasChildNodes" shape="rect" title="DOM:element.hasChildNodes">hasChildNodes</a>()</code>
* Check if the element has any child nodes, or not.</td>
* <td colspan="1" rowspan="1"><a href="http://developer.mozilla.org/en/docs/Global_Objects:Boolean" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Boolean">Boolean</a></td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="DOM" shape="rect" title="DOM">All</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.insertBefore" shape="rect" title="DOM:element.insertBefore">insertBefore</a>( <a href="Node" shape="rect" title="DOM:Node">insertedNode</a>, <a href="Node" shape="rect" title="DOM:Node">adjacentNode</a> )</code>
* Inserts the first node before the second, child, Node in the DOM.</td>
* <td colspan="1" rowspan="1"><a href="Node" shape="rect" title="DOM:Node">Node</a></td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="DOM" shape="rect" title="DOM">All</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.normalize" shape="rect" title="DOM:element.normalize">normalize</a>()</code>
* Clean up all the text nodes under this element (merge adjacent, remove empty).</td>
* <td colspan="1" rowspan="1">-</td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="DOM" shape="rect" title="DOM">All</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.removeAttribute" shape="rect" title="DOM:element.removeAttribute">removeAttribute</a>( <a href="http://developer.mozilla.org/en/docs/Global_Objects:String" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String">name</a> )</code>
* Remove the named attribute from the current node.</td>
* <td colspan="1" rowspan="1">-</td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="DOM" shape="rect" title="DOM">All</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.removeAttributeNS" shape="rect" title="DOM:element.removeAttributeNS">removeAttributeNS</a>( <a href="http://developer.mozilla.org/en/docs/Global_Objects:String" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String">namespace</a>, <a href="http://developer.mozilla.org/en/docs/Global_Objects:String" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String">name</a> )</code>
* Remove the attribute with the specified name and namespace, from the current node.</td>
* <td colspan="1" rowspan="1">-</td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="DOM" shape="rect" title="DOM">All</a></small></td>
* </tr>
* <tr>
* <th colspan="1" rowspan="1">Name &amp; Description</th>
* <th colspan="1" rowspan="1">Return</th>
* <th colspan="1" rowspan="1">Availability</th>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.removeAttributeNode" shape="rect" title="DOM:element.removeAttributeNode">removeAttributeNode</a>( <a href="http://developer.mozilla.org/en/docs/Global_Objects:String" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String">name</a> )</code>
* Remove the node representation of the named attribute from the current node.</td>
* <td colspan="1" rowspan="1">-</td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="DOM" shape="rect" title="DOM">All</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.removeChild" shape="rect" title="DOM:element.removeChild">removeChild</a>( <a href="Node" shape="rect" title="DOM:Node">removedNode</a> )</code>
* Removes a child node from the current element.</td>
* <td colspan="1" rowspan="1"><a href="Node" shape="rect" title="DOM:Node">Node</a></td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="DOM" shape="rect" title="DOM">All</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.removeEventListener" shape="rect" title="DOM:element.removeEventListener">removeEventListener</a>( <a href="http://developer.mozilla.org/en/docs/Global_Objects:String" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String">type</a>, <a href="http://developer.mozilla.org/en/docs/Global_Objects:Function" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Function">handler</a>, <a href="http://developer.mozilla.org/en/docs/Global_Objects:Boolean" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Boolean">useCapture</a> )</code>
* Removes an event listener from the element.</td>
* <td colspan="1" rowspan="1">-</td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="event" shape="rect" title="DOM:event">All</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.replaceChild" shape="rect" title="DOM:element.replaceChild">replaceChild</a>( <a href="Node" shape="rect" title="DOM:Node">insertedNode</a>, <a href="Node" shape="rect" title="DOM:Node">replacedNode</a> )</code>
* Replaces one child node in the current element with another.</td>
* <td colspan="1" rowspan="1"><a href="Node" shape="rect" title="DOM:Node">Node</a></td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="DOM" shape="rect" title="DOM">All</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.scrollIntoView" shape="rect" title="DOM:element.scrollIntoView">scrollIntoView</a>( <a href="http://developer.mozilla.org/en/docs/Global_Objects:Boolean" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Boolean">alignWithTop</a> )</code>
* Scrolls the page until the element gets into the view.</td>
* <td colspan="1" rowspan="1">-</td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="http://developer.mozilla.org/en/docs/HTML" shape="rect" title="HTML">HTML</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.setAttribute" shape="rect" title="DOM:element.setAttribute">setAttribute</a>( <a href="http://developer.mozilla.org/en/docs/Global_Objects:String" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String">name</a>, <a href="http://developer.mozilla.org/en/docs/Global_Objects:Object" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object">value</a> )</code>
* Set the value of the named attribute from the current node.</td>
* <td colspan="1" rowspan="1">-</td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="DOM" shape="rect" title="DOM">All</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.setAttributeNS" shape="rect" title="DOM:element.setAttributeNS">setAttributeNS</a>( <a href="http://developer.mozilla.org/en/docs/Global_Objects:String" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String">namespace</a>, <a href="http://developer.mozilla.org/en/docs/Global_Objects:String" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String">name</a>, <a href="http://developer.mozilla.org/en/docs/Global_Objects:Object" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object">value</a> )</code>
* Set the value of the attribute with the specified name and namespace, from the current node.</td>
* <td colspan="1" rowspan="1">-</td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="DOM" shape="rect" title="DOM">All</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.setAttributeNode" shape="rect" title="DOM:element.setAttributeNode">setAttributeNode</a>( <a href="http://developer.mozilla.org/en/docs/Global_Objects:String" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String">name</a>, <a href="http://developer.mozilla.org/en/docs/index.php?title=Attr&amp;action=edit" shape="rect" title="DOM:Attr">attrNode</a> )</code>
* Set the node representation of the named attribute from the current node.</td>
* <td colspan="1" rowspan="1">-</td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="DOM" shape="rect" title="DOM">All</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.setAttributeNodeNS" shape="rect" title="DOM:element.setAttributeNodeNS">setAttributeNodeNS</a>( <a href="http://developer.mozilla.org/en/docs/Global_Objects:String" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String">namespace</a>, <a href="http://developer.mozilla.org/en/docs/Global_Objects:String" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String">name</a>, <a href="http://developer.mozilla.org/en/docs/index.php?title=Attr&amp;action=edit" shape="rect" title="DOM:Attr">attrNode</a> )</code>
* Set the node representation of the attribute with the specified name and namespace, from the current node.</td>
* <td colspan="1" rowspan="1">-</td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="DOM" shape="rect" title="DOM">All</a></small></td>
* </tr>
* </table>
* <h2> <span> Event Handlers </span></h2>
* <p>These are properties that correspond to the HTML 'on' event attributes.
* </p><p>Unlike the corresponding attributes, the values of these properties are functions (or any other object implementing the <a href="http://www.w3.org/TR/DOM-Level-2-Events/events.html#Events-EventListener" rel="nofollow" shape="rect" title="http://www.w3.org/TR/DOM-Level-2-Events/events.html#Events-EventListener">EventListener</a> interface) rather than a string. In fact, assigning an event attribute in HTML creates a wrapper function around the specified code. For example, given the following HTML:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* &lt;div onclick="foo();"&gt;click me!&lt;/div&gt;
* </pre>
* <p>If <code>element</code> is a reference to this <code>div</code>, the value of <code>element.onclick</code> is effectively:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* function onclick(event) {
* foo();
* }
* </pre>
* <p>Note how the <a href="event" shape="rect" title="DOM:event">event</a> object is passed as parameter <code>event</code> to this wrapper function.
* </p>
* <dl><dt style="font-weight:bold"> <a href="element.onblur" shape="rect" title="DOM:element.onblur">onblur</a>
* </dt><dd> Returns the event handling code for the blur event.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="element.onchange" shape="rect" title="DOM:element.onchange">onchange</a>
* </dt><dd> Returns the event handling code for the change event.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="element.onclick" shape="rect" title="DOM:element.onclick">onclick</a>
* </dt><dd> Returns the event handling code for the click event.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="element.ondblclick" shape="rect" title="DOM:element.ondblclick">ondblclick</a>
* </dt><dd> Returns the event handling code for the dblclick event.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="element.onfocus" shape="rect" title="DOM:element.onfocus">onfocus</a>
* </dt><dd> Returns the event handling code for the focus event.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="element.onkeydown" shape="rect" title="DOM:element.onkeydown">onkeydown</a>
* </dt><dd> Returns the event handling code for the keydown event.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="element.onkeypress" shape="rect" title="DOM:element.onkeypress">onkeypress</a>
* </dt><dd> Returns the event handling code for the keypress event.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="element.onkeyup" shape="rect" title="DOM:element.onkeyup">onkeyup</a>
* </dt><dd> Returns the event handling code for the keyup event.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="element.onmousedown" shape="rect" title="DOM:element.onmousedown">onmousedown</a>
* </dt><dd> Returns the event handling code for the mousedown event.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="element.onmousemove" shape="rect" title="DOM:element.onmousemove">onmousemove</a>
* </dt><dd> Returns the event handling code for the mousemove event.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="element.onmouseout" shape="rect" title="DOM:element.onmouseout">onmouseout</a>
* </dt><dd> Returns the event handling code for the mouseout event.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="element.onmouseover" shape="rect" title="DOM:element.onmouseover">onmouseover</a>
* </dt><dd> Returns the event handling code for the mouseover event.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="element.onmouseup" shape="rect" title="DOM:element.onmouseup">onmouseup</a>
* </dt><dd> Returns the event handling code for the mouseup event.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="element.onresize" shape="rect" title="DOM:element.onresize">onresize</a>
* </dt><dd> Returns the event handling code for the resize event.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="element.onscroll" shape="rect" title="DOM:element.onscroll">onscroll</a>
* </dt><dd> Returns the event handling code for the scroll event.
* </dd></dl>
* <p>
* </p>
* <h2> <span> Other Events </span></h2>
* <p>There are also other <a href="DOM_Events" shape="rect" title="DOM Events">DOM Events</a> like
* <code>DOMSubtreeModified</code>, <code>DOMAttrModified</code> etc. as well as
* <a href="http://developer.mozilla.org/en/docs/Gecko-Specific_DOM_Events" shape="rect" title="Gecko-Specific DOM Events">Gecko-Specific DOM Events</a> like
* <code>DOMContentLoaded</code>, <code>DOMTitleChanged</code> etc.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
var Element = {
  // This is just a stub for a builtin native JavaScript object.
/**
* <h2> <span> Summary </span></h2>
* <p><code>addEventListener</code> allows the registration of event listeners on the event target. An event target may be a node in a document, the <a href="document" shape="rect" title="DOM:document">document</a> itself, a <a href="window" shape="rect" title="DOM:window">window</a>, or an <a href="http://developer.mozilla.org/en/docs/XMLHttpRequest" shape="rect" title="XMLHttpRequest">XMLHttpRequest</a>.
* </p>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve"><i>target</i>.addEventListener(<i>type</i>, <i>listener</i>, <i>useCapture</i>);
* </pre>
* <dl><dt style="font-weight:bold"> type </dt><dd> A string representing the event type to listen for.
* </dd><dt style="font-weight:bold"> listener </dt><dd> The object that receives a notification when an event of the specified type occurs. This must be an object implementing the <a href="http://www.w3.org/TR/DOM-Level-2-Events/events.html#Events-EventListener" rel="nofollow" shape="rect" title="http://www.w3.org/TR/DOM-Level-2-Events/events.html#Events-EventListener"><code>EventListener</code></a> interface, or simply a JavaScript <a href="http://developer.mozilla.org/en/docs/Core_JavaScript_1.5_Guide:Functions" shape="rect" title="Core JavaScript 1.5 Guide:Functions">function</a>.
* </dd><dt style="font-weight:bold"> useCapture </dt><dd> If <code>true</code>, <code>useCapture</code> indicates that the user wishes to initiate capture. After initiating capture, all events of the specified type will be dispatched to the registered <code>listener</code> before being dispatched to any <code>EventTarget</code>s beneath it in the DOM tree. Events which are bubbling upward through the tree will not trigger a listener designated to use capture. See <a href="http://www.w3.org/TR/DOM-Level-3-Events/events.html#Events-flow" rel="nofollow" shape="rect" title="http://www.w3.org/TR/DOM-Level-3-Events/events.html#Events-flow">DOM Level 3 Events</a> for a detailed explanation.
* </dd></dl>
* <h2> <span> Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* &lt;html&gt;
* &lt;head&gt;
* &lt;title&gt;DOM Event Example&lt;/title&gt;
* &lt;style type="text/css"&gt;
* #t { border: 1px solid red }
* #t1 { background-color: pink; }
* &lt;/style&gt;
* &lt;script type="text/javascript"&gt;
* 
* // Function to change the content of t2
* function modifyText() {
* var t2 = document.getElementById("t2");
* t2.firstChild.nodeValue = "three";
* }
* 
* // Function to add event listener to t
* function load() {
* var el = document.getElementById("t");
* el.addEventListener("click", modifyText, false);
* }
* 
* &lt;/script&gt;
* &lt;/head&gt;
* &lt;body onload="load();"&gt;
* &lt;table id="t"&gt;
* &lt;tr&gt;&lt;td id="t1"&gt;one&lt;/td&gt;&lt;/tr&gt;
* &lt;tr&gt;&lt;td id="t2"&gt;two&lt;/td&gt;&lt;/tr&gt;
* &lt;/table&gt;
* &lt;/body&gt;
* &lt;/html&gt;
* </pre>
* <p>In the above example, <code>modifyText()</code> is a listener for <code>click</code> events registered using <code>addEventListener()</code>.  A click anywhere on the table will bubble up to the handler and run <code>modifyText()</code>.
* </p>
* <h2> <span> Notes </span></h2>
* <h3> <span> Why use <code>addEventListener</code>? </span></h3>
* <p><code>addEventListener</code> is the way to register an event listener as specified in W3C DOM. Its benefits are as follows:
* </p>
* <ul><li> It allows adding more than a single handler for an event. This is particularly useful for <a href="http://developer.mozilla.org/en/docs/DHTML" shape="rect" title="DHTML">DHTML</a> libraries or <a href="http://developer.mozilla.org/en/docs/Extensions" shape="rect" title="Extensions">Mozilla extensions</a> that need to work well even if other libraries/extensions are used.
* </li><li> It gives you finer-grained control of the phase when the listener gets activated (capturing vs. bubbling)
* </li><li> It works on any DOM element, not just HTML elements.
* </li></ul>
* <p>The alternative, older way to register event handlers is <a href="element.addEventListener#Older_way_to_attach_events" shape="rect" title="">described below</a>.
* </p>
* <h3> <span> Adding a listener during event dispatch </span></h3>
* <p>If an <code>EventListener</code> is added to an <code>EventTarget</code> while it is processing an event, it will not be triggered by the current actions but may be triggered during a later stage of event flow, such as the bubbling phase.
* </p>
* <h3> <span> Multiple identical event listeners </span></h3>
* <p>If multiple identical <code>EventListener</code>s are registered on the same <code>EventTarget</code> with the same parameters, the duplicate instances are discarded. They do not cause the <code>EventListener</code> to be called twice, and since the duplicates are discarded, they do not need to be removed manually with the <a href="element.removeEventListener" shape="rect" title="DOM:element.removeEventListener">removeEventListener</a> method.
* </p>
* <h3> <span> The value of <code>this</code> </span></h3>
* <p>Attaching a function using <code>addEventListener()</code> changes the value of <code>this</code>—note that the value of <code>this</code> is passed to a function from the caller.
* </p><p>In the example above, the value of <code>this</code> within <code>modifyText()</code> when called from the onclick event is a reference to the table 't'.  If the onclick handler is added in the HTML source:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* &lt;table id="t" onclick="modifyText();"&gt;
* ...
* &lt;/table&gt;
* </pre>
* <p>then value of <code>this</code> within <code>modifyText()</code> when called from the onclick event will be a reference to the global (window) object.
* </p>
* <h3> <span> Internet Explorer </span></h3>
* <p>In IE you have to use <code>attachEvent</code> rather than the standard <code>addEventListener</code>. To support IE, the example above can be modified to:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* if (el.addEventListener){
* el.addEventListener('click', modifyText, false);
* } else if (el.attachEvent){
* el.attachEvent('onclick', modifyText);
* }
* </pre>
* <h3> <span>Older way to attach events</span></h3>
* <p><code>addEventListener()</code> was introduced with the DOM 2 <a href="http://www.w3.org/TR/DOM-Level-2-Events" rel="nofollow" shape="rect" title="http://www.w3.org/TR/DOM-Level-2-Events">Events</a> specification. Before then, events were attached as follows:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* // Using a function reference—note lack of '()'
* el.onclick = modifyText;
* 
* // Using a function expression
* element.onclick = function(){
* ...statements...
* };
* </pre>
* <p>This method replaces the existing <code>onclick</code> event handler(s) on the element if there are any.  Similarly for other 'on' events such as <code>onblur</code>, <code>onkeypress</code>, and so on.
* </p><p>Because it was essentially part of DOM 0, this method is very widely supported and requires no special cross–browser code; hence it is normally used to attach events dynamically unless the extra features of <code>addEventListener()</code> are needed.
* </p>
* <h2> <span> Specification </span></h2>
* <p><a href="http://www.w3.org/TR/DOM-Level-2-Events/events.html#Events-EventTarget-addEventListener" rel="nofollow" shape="rect" title="http://www.w3.org/TR/DOM-Level-2-Events/events.html#Events-EventTarget-addEventListener">DOM Level 2 Events: addEventListener</a>
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
addEventListener: function(type, listener, useCapture) {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span> Summary </span></h2>
* <p>Adds a node to the end of the list of children of a specified parent node. If the node already exists it is removed from current parent node, then added to new parent node.
* </p>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var <i>appendedElement</i> = <i>element</i>.appendChild(<i>child</i>);
* </pre>
* <ul><li> <code>element</code> is the parent element.
* </li><li> <code>child</code> is the node to append underneath <code>element</code>.
* </li></ul>
* <h2> <span> Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">// Create a new paragraph element, and append it to the end of the document body
* var p = document.createElement("p");
* document.body.appendChild(p);
* </pre>
* <h2> <span> Notes </span></h2>
* <p>If <code>child</code> is a reference to an existing node in the document, <code>appendChild</code> moves it from its current position to the new position (i.e. there is no requirement to remove the node from its parent node before appending it to some other node).
* </p><p>This also means that a node can't be in two points of the document simultaneously. So if the node already has a parent, it is first removed, <i>then</i> appended at the new position.
* </p><p>You can use <a href="DOM:element.cloneNode" shape="rect" title="DOM:element.cloneNode">cloneNode</a> to make a copy of the node before appending it under the new parent. (Note that the copies made with <code>cloneNode</code> will not be automatically kept in sync.)
* </p><p>This method is not allowed to move nodes between different documents. If you want to append node from a different document (for example to display results from AJAX request) you must first use <a href="document.importNode" shape="rect" title="DOM:document.importNode">importNode</a>.
* </p><p>Related methods: <a href="element.insertBefore" shape="rect" title="DOM:element.insertBefore">insertBefore</a>, <a href="element.replaceChild" shape="rect" title="DOM:element.replaceChild">replaceChild</a> and <a href="element.removeChild" shape="rect" title="DOM:element.removeChild">removeChild</a>.
* </p><p>
* </p>
* <h2> <span> Specification </span></h2>
* <p><a href="http://www.w3.org/TR/DOM-Level-2-Core/core.html#ID-184E7107" rel="nofollow" shape="rect" title="http://www.w3.org/TR/DOM-Level-2-Core/core.html#ID-184E7107">DOM Level 2 Core: appendChild</a>
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
appendChild: function(child) {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span> Summary </span></h2>
* <p>Returns a collection of attributes of the given element.
* </p>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var <i>attrs</i> = <i>element</i>.attributes;
* </pre>
* <p>The returned object is of type <a href="http://www.w3.org/TR/DOM-Level-2-Core/core.html#ID-1780488922" rel="nofollow" shape="rect" title="http://www.w3.org/TR/DOM-Level-2-Core/core.html#ID-1780488922">NamedNodeMap</a>, containing <code>Attr</code> nodes. If the element has no specified attributes, then the returned object has a length of 0 (zero). This attribute is read-only.
* </p>
* <h2> <span> Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">// get the first &lt;p&gt; element in the document
* var para = document.getElementsByTagName("p")[0];
* var atts = para.attributes;
* </pre>
* <h2> <span> Notes </span></h2>
* <p>The items of the collection are accessible by their names and indices. Notice though, that unlike a <code>NodeList</code>, a <code>NamedNodeMap</code> doesn't maintain its items in any particular order.
* </p><p>You should only use access by index when enumerating over element's attributes, as in the following example, which prints the values of all the attributes of the "p1" element in the document:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* &lt;!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN"
* "http://www.w3.org/TR/html4/strict.dtd"&gt;
* 
* &lt;html&gt;
* 
* &lt;head&gt;
* &lt;title&gt;Attributes example&lt;/title&gt;
* &lt;script type="text/javascript"&gt;
* function showFirstAttr()
* {
* var firstPara = document.getElementById("p1");
* var outputText = document.getElementById("result");
* 
* // First, let's verify that the paragraph has some attributes
* if (firstPara.hasAttributes())
* {
* var attrs = firstPara.attributes;
* var text = "";
* for(var i=attrs.length-1; i&gt;=0; i--) {
* text += attrs[i].name + "-&gt;" + attrs[i].value;
* }
* outputText.value = text;
* } else {
* outputText.value = "No attributes to show"
* };
* }
* &lt;/script&gt;
* &lt;/head&gt;
* 
* &lt;body&gt;
* &lt;p id="p1" style="color: green;"&gt;Sample Paragraph&lt;/p&gt;
* &lt;form action=""&gt;
* &lt;p&gt;&lt;input type="button" value="Show first attribute name and value"
* onclick="showFirstAttr();"&gt;
* &lt;input id="result" type="text" value=""&gt;&lt;/p&gt;
* &lt;/form&gt;
* &lt;/body&gt;
* &lt;/html&gt;
* </pre>
* <p>While a <code>NamedNodeMap</code> can be iterated like an array, it doesn't have any of the special methods <code>Array</code> has, such as <code>join</code>, <code>split</code>, etc.
* </p><p>To access a specific attribute by its name, use the <a href="element.getAttribute" shape="rect" title="DOM:element.getAttribute">getAttribute</a> method.
* </p>
* <h2> <span> Specification </span></h2>
* <ul><li> <a href="http://www.w3.org/TR/DOM-Level-2-Core/core.html#ID-84CF096" rel="nofollow" shape="rect" title="http://www.w3.org/TR/DOM-Level-2-Core/core.html#ID-84CF096">W3C DOM Level 2 Core: attributes</a>
* </li><li> <a href="http://www.w3.org/TR/DOM-Level-3-Core/core.html#ID-84CF096" rel="nofollow" shape="rect" title="http://www.w3.org/TR/DOM-Level-3-Core/core.html#ID-84CF096">W3C DOM Level 3 Core: attributes</a>
* </li><li> <a href="http://www.w3.org/TR/DOM-Level-3-Core/core.html#ID-1780488922" rel="nofollow" shape="rect" title="http://www.w3.org/TR/DOM-Level-3-Core/core.html#ID-1780488922">W3C DOM Level 3 NamedNodeMap interface</a>
* </li></ul>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
attributes: undefined,
/**
* <h2> <span>Summary</span></h2>
* <p>The <b>blur</b> method removes keyboard focus from the current element.
* </p>
* <h2> <span>Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">element.blur()
* </pre>
* <h2> <span>Specification </span></h2>
* <p><a href="http://www.w3.org/TR/DOM-Level-2-HTML/html.html#ID-28216144" rel="nofollow" shape="rect" title="http://www.w3.org/TR/DOM-Level-2-HTML/html.html#ID-28216144">blur</a>
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
blur: function() {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span>Summary</span></h2>
* <p><b>childNodes </b> returns a <i>collection</i> of child nodes of the given element.
* </p>
* <h2> <span>Syntax and values</span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var <var>ndList</var> = elementNodeReference.childNodes;
* </pre>
* <p><var>ndList</var> is an ordered collection of node objects that are children of the current element. If the element has no children, then <var>ndList</var> contains no node.
* </p><p>The <var>ndList</var> is a variable storing the node list of childNodes. Such list is of type <a href="http://www.w3.org/TR/2004/REC-DOM-Level-3-Core-20040407/core.html#ID-536297177" rel="nofollow" shape="rect" title="http://www.w3.org/TR/2004/REC-DOM-Level-3-Core-20040407/core.html#ID-536297177">NodeList</a>. The childNodes attribute is read-only.
* </p>
* <h2> <span>Example</span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* // parg is an object reference to a &lt;p&gt; element
* if (parg.hasChildNodes())
* // So, first we check if the object is not empty, if the object has child nodes
* {
* var children = parg.childNodes;
* for (var i = 0; i &lt; children.length; i++)
* {
* // do something with each child as children[i]
* // NOTE: List is live, Adding or removing children will change the list
* };
* };
* </pre>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* // This is one way to remove all children from a node
* // box is an object refrence to an element with children
* while (box.firstChild)
* {
* //The list is LIVE so it will re-index each call
* box.removeChild(box.firstChild);
* };
* </pre>
* <h2> <span>Notes</span></h2>
* <p>The items in the collection of nodes are objects and not strings. To get data from those node objects, you must use their properties (e.g. <code>elementNodeReference.childNodes[1].nodeName</code> to get the name, etc.).
* </p><p>The <code>document</code> object itself has 2 children: the Doctype declaration and the root element, typically referred to as <code>documentElement</code>. (In (X)HTML documents this is the <code>HTML</code> element.)
* </p>
* <h3> <span>See Also</span></h3>
* <p><a href="DOM:element.firstChild" shape="rect" title="DOM:element.firstChild">firstChild</a>, <a href="element.lastChild" shape="rect" title="DOM:element.lastChild">lastChild</a> and <a href="element.previousSibling" shape="rect" title="DOM:element.previousSibling">previousSibling</a>
* </p>
* <h2> <span>Specification</span></h2>
* <ul><li> <a href="http://www.w3.org/TR/2000/REC-DOM-Level-2-Core-20001113/core.html#ID-1451460987" rel="nofollow" shape="rect" title="http://www.w3.org/TR/2000/REC-DOM-Level-2-Core-20001113/core.html#ID-1451460987">W3C DOM 2 Core: childNodes</a>
* </li><li> <a href="http://www.w3.org/TR/2004/REC-DOM-Level-3-Core-20040407/core.html#ID-1451460987" rel="nofollow" shape="rect" title="http://www.w3.org/TR/2004/REC-DOM-Level-3-Core-20040407/core.html#ID-1451460987">W3C DOM 3 Core: childNodes</a>
* </li><li> <a href="http://www.w3.org/TR/2004/REC-DOM-Level-3-Core-20040407/core.html#ID-536297177" rel="nofollow" shape="rect" title="http://www.w3.org/TR/2004/REC-DOM-Level-3-Core-20040407/core.html#ID-536297177">W3C DOM 3 NodeList interface</a>
* </li></ul>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
childNodes: undefined,
/**
* <h2> <span>Summary</span></h2>
* <p><b>className </b> gets and sets the value of the <code>class</code> attribute of the specified element.
* </p>
* <h2> <span> Syntax and values </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var <var>cName</var> = elementNodeReference.className;
* elementNodeReference.className = <var>cName</var>;
* </pre>
* <p><var>cName</var> is a string variable representing the class or space-separated classes of the current element.
* </p>
* <h2> <span> Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* var elementNodeReference = document.getElementById("div1");
* if (elementNodeReference.className == "fixed")
* {
* // skip a particular class of element
* goNextElement();
* };
* </pre>
* <h2> <span> Notes </span></h2>
* <p>The name <code>className</code> is used for this property instead of <code>class</code> because of conflicts with the "class" keyword in many languages which are used to manipulate the DOM.
* </p>
* <h2> <span> Specification </span></h2>
* <p><a href="http://www.w3.org/TR/DOM-Level-2-HTML/html.html#ID-95362176" rel="nofollow" shape="rect" title="http://www.w3.org/TR/DOM-Level-2-HTML/html.html#ID-95362176">DOM Level 2 HTML: className</a>
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
className: undefined,
/**
* <h2> <span>Summary</span></h2>
* <p>The <b>click</b> method simulates a click on an element.
* </p>
* <h2> <span>Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">element.click()
* </pre>
* <h2> <span>Notes </span></h2>
* <p>The click method is intended to be used with INPUT elements of type button, checkbox, radio, reset or submit.  Gecko does not implement the click method on other elements that might be expected to respond to mouse–clicks such as links (A elements), nor will it necessarily fire the click event of other elements.
* </p><p>Non–Gecko DOMs may behave differently.
* </p><p>When a click is used with elements that support it (e.g. one of the INPUT types listed above), it also fires the element's click event which will bubble up to elements higher up the document tree (or event chain) and fire their click events too.  However, bubbling of a click event will not cause an A element to initiate navigation as if a real mouse-click had been received.
* </p>
* <h2> <span>Specification </span></h2>
* <p><a href="http://www.w3.org/TR/DOM-Level-2-HTML/html.html#ID-2651361" rel="nofollow" shape="rect" title="http://www.w3.org/TR/DOM-Level-2-HTML/html.html#ID-2651361">click</a>
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
click: function() {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span> Summary </span></h2>
* <p>Returns the inner height of an element in pixels, including padding but not the horizontal scrollbar height, border, or margin.
* </p><p><code>clientHeight</code> can be calculated as CSS <code>height</code> + CSS <code>padding</code> - height of horizontal scrollbar (if present).
* </p>
* <h2> <span> Syntax and values </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var <var>h</var> = <var>element</var>.clientHeight;
* </pre>
* <p><var>h</var> is an integer representing the <code>clientHeight</code> of <var>element</var> in pixels.
* </p><p><code>clientHeight</code> is read–only.
* </p>
* <h2> <span> Example </span></h2>
* <div id="offsetContainer" style="margin: 26px 0px; background-color: rgb(255, 255, 204); border: 4px dashed black; color: black; position: absolute; left: 260px;"><div id="idDiv" style="margin: 24px 29px; border: 24px black solid; padding: 0px 28px; width: 199px; height: 102px; overflow: auto; background-color: white; font-size: 13px!important; font-family: Arial, sans-serif;"><p id="PaddingTopLabel" style="text-align: center; font-style: italic; font-weight: bold; font-size: 13px!important; font-family: Arial, sans-serif; margin: 0px;">padding-top</p>
* <p>Gentle, individualistic and very loyal, Birman cats fall between Siamese and Persian in character. If you admire cats that are non aggressive, that enjoy being with humans and tend to be on the quiet side, you may well find that Birman cats are just the felines for you.</p>
* <p><span style="float: right;"><a href="http://developer.mozilla.org/en/docs/Image:BirmanCat.jpg" shape="rect" title="Image:BirmanCat.jpg"/></span>All Birmans have colorpointed features, dark coloration of the face, ears, legs and tail.</p>
* <p>Cat image and text coming from <a href="http://www.best-cat-art.com/" rel="nofollow" shape="rect" title="http://www.best-cat-art.com/">http://www.best-cat-art.com/</a> </p>
* <p id="PaddingBottomLabel" style="text-align: center; font-style: italic; font-weight: bold; font-size: 13px!important; font-family: Arial, sans-serif; margin: 0px;">padding-bottom</p></div><span style="position: absolute; left: -32px; top: 85px; color: blue; font-weight: bold; font-size: 13px!important; font-family: Arial, sans-serif;">Left</span><span style="position: absolute; left: 170px; top: -20px; color: blue; font-weight: bold; font-size: 13px!important; font-family: Arial, sans-serif;">Top</span><span style="position: absolute; left: 370px; top: 85px; color: blue; font-weight: bold; font-size: 13px!important; font-family: Arial, sans-serif;">Right</span><span style="position: absolute; left: 164px; top: 203px; color: blue; font-weight: bold; font-size: 13px!important; font-family: Arial, sans-serif;">Bottom</span><span style="position: absolute; left: 143px; top: 5px; font-style: italic; font-weight: bold; font-size: 13px!important; font-family: Arial, sans-serif;">margin-top</span><span style="position: absolute; left: 138px; top: 175px; font-style: italic; font-weight: bold; font-size: 13px!important; font-family: Arial, sans-serif;">margin-bottom</span><span style="position: absolute; left: 143px; top: 27px; color: white; font-style: italic; font-weight: bold; font-size: 13px!important; font-family: Arial, sans-serif;">border-top</span><span style="position: absolute; left: 138px; top: 153px; color: white; font-style: italic; font-weight: bold; font-size: 13px!important; font-family: Arial, sans-serif;">border-bottom</span></div>
* <p style="margin-top: 270px;"><a href="http://developer.mozilla.org/en/docs/Image:clientHeight.png" shape="rect" title="Image:clientHeight.png"/></p>
* <h2> <span>Specification</span></h2>
* <p>Not part of any W3C specification.
* </p>
* <h2> <span>Notes</span></h2>
* <p><code>clientHeight</code> is a non-standard, HTML-specific property introduced in the Internet Explorer object model.
* </p><p><code>offsetLeft</code> returns the position the upper left edge of the element; not necessarily the 'real' left edge of the element.  This is important for <b>span</b> elements in flowed text that wraps from one line to the next.  The span may start in the middle of the page and wrap around to the beginning of the next line.  The <code>offsetLeft</code> will refer to the left edge of the start of the span, not the left edge of text at the start of the second line.  Therefore, a box with the left, top, width and height of <code>offsetLeft, offsetTop, offsetWidth</code> and <code>offsetHeight</code> will not be a bounding box for a span with wrapped text.  (And, I can't figure out how to find the leftmost edge of such a span, sigh.)
* </p>
* <h2> <span>References</span></h2>
* <ul><li> <a href="http://msdn.microsoft.com/workshop/author/dhtml/reference/properties/clientheight.asp?frame=true" rel="nofollow" shape="rect" title="http://msdn.microsoft.com/workshop/author/dhtml/reference/properties/clientheight.asp?frame=true">MSDN clientHeight definition</a>
* </li><li> <a href="http://msdn.microsoft.com/workshop/author/om/measuring.asp" rel="nofollow" shape="rect" title="http://msdn.microsoft.com/workshop/author/om/measuring.asp">MSDN Measuring Element Dimension and Location</a>
* </li><li> <a href="http://www.mozilla.org/docs/dom/domref/clientHeight.html" rel="nofollow" shape="rect" title="http://www.mozilla.org/docs/dom/domref/clientHeight.html">Gecko DOM Reference on clientHeight</a>
* </li></ul>
* <h2> <span>See Also</span></h2>
* <ul><li> <a href="DOM:element.offsetHeight" shape="rect" title="DOM:element.offsetHeight">DOM:element.offsetHeight</a>
* </li><li> <a href="element.scrollHeight" shape="rect" title="DOM:element.scrollHeight">DOM:element.scrollHeight</a>
* </li></ul>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
clientHeight: undefined,
/**
* <div style="border: 1px solid #818151; background-color: #FFFFE1; font-weight: bold; text-align: center; padding: 0px 10px 0px 10px; margin: 10px 0px 10px 0px;"><p style="margin: 4px 0px 4px 0px;">This article covers features introduced in <a href="http://developer.mozilla.org/en/docs/Firefox_3_for_developers" shape="rect" title="Firefox 3 for developers">Firefox 3</a></p></div>
* 
* <h2> <span>Summary</span></h2>
* <p>The width of the left border of an element in pixels. It includes the width of the vertical scrollbar if the text direction of the element is right–to–left and if there is an overflow causing a left vertical scrollbar to be rendered. <code>clientLeft</code> does not include the left margin or the left padding. <code>clientLeft</code> is read-only.
* </p><p><a href="http://developer.mozilla.org/en/docs/Gecko" shape="rect" title="Gecko">Gecko</a>-based applications support <code>clientLeft</code> starting with Gecko 1.9 (<a href="http://developer.mozilla.org/en/docs/Firefox_3" shape="rect" title="Firefox 3">Firefox 3</a>, implemented in <a href="https://bugzilla.mozilla.org/show_bug.cgi?id=111207" rel="nofollow" shape="rect" title="https://bugzilla.mozilla.org/show_bug.cgi?id=111207">bug 111207</a>). This property is not supported in Firefox 2 and earlier.
* </p>
* <h2> <span>Syntax</span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var <var>left</var> = <var>element</var>.clientLeft;
* </pre>
* <h2> <span>Example</span></h2>
* <div id="offsetContainer" style="margin: 26px 0px; background-color: rgb(255, 255, 204); border: 4px dashed black; color: black; position: absolute; left: 260px;"><div id="idDiv" style="margin: 24px 29px; border: 24px black solid; padding: 0px 28px; width: 199px; height: 102px; overflow: auto; background-color: white; font-size: 13px!important; font-family: Arial, sans-serif;"><p id="PaddingTopLabel" style="text-align: center; font-style: italic; font-weight: bold; font-size: 13px!important; font-family: Arial, sans-serif; margin: 0px;">padding-top</p>
* <p>Gentle, individualistic and very loyal, Birman cats fall between Siamese and Persian in character. If you admire cats that are non aggressive, that enjoy being with humans and tend to be on the quiet side, you may well find that Birman cats are just the felines for you.</p>
* <p><span style="float: right;"><a href="http://developer.mozilla.org/en/docs/Image:BirmanCat.jpg" shape="rect" title="Image:BirmanCat.jpg"/></span>All Birmans have colorpointed features, dark coloration of the face, ears, legs and tail.</p>
* <p>Cat image and text coming from <a href="http://www.best-cat-art.com/" rel="nofollow" shape="rect" title="http://www.best-cat-art.com/">www.best-cat-art.com</a></p>
* <p id="PaddingBottomLabel" style="text-align: center; font-style: italic; font-weight: bold; font-size: 13px!important; font-family: Arial, sans-serif; margin: 0px;">padding-bottom</p></div><span style="position: absolute; left: -32px; top: 85px; color: blue; font-weight: bold; font-size: 13px!important; font-family: Arial, sans-serif;">Left</span><span style="position: absolute; left: 170px; top: -24px; color: blue; font-weight: bold; font-size: 13px!important; font-family: Arial, sans-serif;">Top</span><span style="position: absolute; left: 370px; top: 85px; color: blue; font-weight: bold; font-size: 13px!important; font-family: Arial, sans-serif;">Right</span><span style="position: absolute; left: 164px; top: 203px; color: blue; font-weight: bold; font-size: 13px!important; font-family: Arial, sans-serif;">Bottom</span><span style="position: absolute; left: 143px; top: 5px; font-style: italic; font-weight: bold; font-size: 13px!important; font-family: Arial, sans-serif;">margin-top</span><span style="position: absolute; left: 138px; top: 175px; font-style: italic; font-weight: bold; font-size: 13px!important; font-family: Arial, sans-serif;">margin-bottom</span><span style="position: absolute; left: 143px; top: 27px; color: white; font-style: italic; font-weight: bold; font-size: 13px!important; font-family: Arial, sans-serif;">border-top</span><span style="position: absolute; left: 138px; top: 153px; color: white; font-style: italic; font-weight: bold; font-size: 13px!important; font-family: Arial, sans-serif;">border-bottom</span></div>
* <p style="margin-top: 270px;"><a href="http://developer.mozilla.org/en/docs/Image:clientLeft.png" shape="rect" title="Image:clientLeft.png"/></p>
* <p><a href="http://developer.mozilla.org/en/docs/Image:clientLeftRtl.png" shape="rect" title="Vertical scrollbar position when layout.scrollbar.side property is set to 1 or to 3"/></p>
* <p>When <a href="http://kb.mozillazine.org/Layout.scrollbar.side" rel="nofollow" shape="rect" title="http://kb.mozillazine.org/Layout.scrollbar.side"><i>layout.scrollbar.side</i> property</a> is set to 1 or to 3 and when the text-direction is set to RTL, <b>then the vertical scrollbar is positioned on the left</b> and this impacts the way clientLeft is computed.</p>
* <h2> <span>Specification</span></h2>
* <p>Not part of any W3C specification.
* </p>
* <h2> <span>Notes</span></h2>
* <p><code>clientLeft</code> was first introduced in the MS IE DHTML object model.
* </p><p>The position of the vertical scrollbar in right–to–left text direction set on the element will depend on the <a href="http://kb.mozillazine.org/Layout.scrollbar.side" rel="nofollow" shape="rect" title="http://kb.mozillazine.org/Layout.scrollbar.side"><i>layout.scrollbar.side</i> preference</a>
* </p>
* <h2> <span>References</span></h2>
* <ul><li> <a href="http://msdn.microsoft.com/workshop/author/dhtml/reference/properties/clientleft.asp?frame=true" rel="nofollow" shape="rect" title="http://msdn.microsoft.com/workshop/author/dhtml/reference/properties/clientleft.asp?frame=true">MSDN's clientLeft definition</a>
* </li><li> <a href="http://msdn.microsoft.com/workshop/author/om/measuring.asp" rel="nofollow" shape="rect" title="http://msdn.microsoft.com/workshop/author/om/measuring.asp">MSDN's Measuring Element Dimension and Location</a>
* </li></ul>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
clientLeft: undefined,
/**
* <div style="border: 1px solid #818151; background-color: #FFFFE1; font-weight: bold; text-align: center; padding: 0px 10px 0px 10px; margin: 10px 0px 10px 0px;"><p style="margin: 4px 0px 4px 0px;">This article covers features introduced in <a href="http://developer.mozilla.org/en/docs/Firefox_3_for_developers" shape="rect" title="Firefox 3 for developers">Firefox 3</a></p></div>
* 
* <h2> <span>Summary</span></h2>
* <p>The width of the top border of an element in pixels. It does not include the top margin or padding. <code>clientTop</code> is read-only.
* </p><p><a href="http://developer.mozilla.org/en/docs/Gecko" shape="rect" title="Gecko">Gecko</a>-based applications support <code>clientTop</code> starting with Gecko 1.9 (<a href="http://developer.mozilla.org/en/docs/Firefox_3" shape="rect" title="Firefox 3">Firefox 3</a>, implemented in <a href="https://bugzilla.mozilla.org/show_bug.cgi?id=111207" rel="nofollow" shape="rect" title="https://bugzilla.mozilla.org/show_bug.cgi?id=111207">bug 111207</a>). This property is not supported in Firefox 2 and earlier.
* </p>
* <h2> <span>Syntax</span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var <i>top</i> = <var>element</var>.clientTop;
* </pre>
* <h2> <span>Example</span></h2>
* <div id="offsetContainer" style="margin: 26px 0px; background-color: rgb(255, 255, 204); border: 4px dashed black; color: black; position: absolute; left: 260px;"><div id="idDiv" style="margin: 24px 29px; border: 24px black solid; padding: 0px 28px; width: 199px; height: 102px; overflow: auto; background-color: white; font-size: 13px!important; font-family: Arial, sans-serif;"><p id="PaddingTopLabel" style="text-align: center; font-style: italic; font-weight: bold; font-size: 13px!important; font-family: Arial, sans-serif; margin: 0px;">padding-top</p>
* <p>Gentle, individualistic and very loyal, Birman cats fall between Siamese and Persian in character. If you admire cats that are non aggressive, that enjoy being with humans and tend to be on the quiet side, you may well find that Birman cats are just the felines for you.</p>
* <p><span style="float: right;"><a href="http://developer.mozilla.org/en/docs/Image:BirmanCat.jpg" shape="rect" title="Image:BirmanCat.jpg"/></span>All Birmans have colorpointed features, dark coloration of the face, ears, legs and tail.</p>
* <p>Cat image and text coming from <a href="http://www.best-cat-art.com/" rel="nofollow" shape="rect" title="http://www.best-cat-art.com/">www.best-cat-art.com</a></p>
* <p id="PaddingBottomLabel" style="text-align: center; font-style: italic; font-weight: bold; font-size: 13px!important; font-family: Arial, sans-serif; margin: 0px;">padding-bottom</p></div><span style="position: absolute; left: -32px; top: 85px; color: blue; font-weight: bold; font-size: 13px!important; font-family: Arial, sans-serif;">Left</span><span style="position: absolute; left: 170px; top: -24px; color: blue; font-weight: bold; font-size: 13px!important; font-family: Arial, sans-serif;">Top</span><span style="position: absolute; left: 370px; top: 85px; color: blue; font-weight: bold; font-size: 13px!important; font-family: Arial, sans-serif;">Right</span><span style="position: absolute; left: 164px; top: 203px; color: blue; font-weight: bold; font-size: 13px!important; font-family: Arial, sans-serif;">Bottom</span><span style="position: absolute; left: 143px; top: 5px; font-style: italic; font-weight: bold; font-size: 13px!important; font-family: Arial, sans-serif;">margin-top</span><span style="position: absolute; left: 138px; top: 175px; font-style: italic; font-weight: bold; font-size: 13px!important; font-family: Arial, sans-serif;">margin-bottom</span><span style="position: absolute; left: 143px; top: 27px; color: white; font-style: italic; font-weight: bold; font-size: 13px!important; font-family: Arial, sans-serif;">border-top</span><span style="position: absolute; left: 138px; top: 153px; color: white; font-style: italic; font-weight: bold; font-size: 13px!important; font-family: Arial, sans-serif;">border-bottom</span></div>
* <p style="margin-top: 270px;"><a href="http://developer.mozilla.org/en/docs/Image:clientTop.png" shape="rect" title="Image:clientTop.png"/></p>
* <h2> <span>Specification</span></h2>
* <p>Not part of any W3C specification.
* </p>
* <h2> <span>Notes</span></h2>
* <p><code>clientTop</code> was first introduced in the MS IE DHTML object model.
* </p>
* <h2> <span>References</span></h2>
* <ul><li> <a href="http://msdn.microsoft.com/workshop/author/dhtml/reference/properties/clienttop.asp?frame=true" rel="nofollow" shape="rect" title="http://msdn.microsoft.com/workshop/author/dhtml/reference/properties/clienttop.asp?frame=true">MSDN's clientTop definition</a>
* </li><li> <a href="http://msdn.microsoft.com/workshop/author/om/measuring.asp" rel="nofollow" shape="rect" title="http://msdn.microsoft.com/workshop/author/om/measuring.asp">MSDN's Measuring Element Dimension and Location</a>
* </li></ul>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
clientTop: undefined,
/**
* <h2> <span>Summary</span></h2>
* <p><b>clientWidth </b> is the inner width of an element in pixels. It includes padding but not the vertical scrollbar (if present, if rendered), border or margin.
* </p>
* <h2> <span>Syntax and values</span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var <var>intElemClientWidth</var> = <var>element</var>.clientWidth;
* </pre>
* <p><var>intElemClientWidth</var> is an integer corresponding to the <b>clientWidth</b> of <var>element</var> in pixels.  <b>clientWidth</b> is read–only.
* </p><p>
* </p>
* <h2> <span>Example</span></h2>
* <div id="offsetContainer" style="margin: 26px 0px; background-color: rgb(255, 255, 204); border: 4px dashed black; color: black; position: absolute; left: 260px;"><div id="idDiv" style="margin: 24px 29px; border: 24px black solid; padding: 0px 28px; width: 199px; height: 102px; overflow: auto; background-color: white; font-size: 13px!important; font-family: Arial, sans-serif;"><p id="PaddingTopLabel" style="text-align: center; font-style: italic; font-weight: bold; font-size: 13px!important; font-family: Arial, sans-serif; margin: 0px;">padding-top</p>
* <p>Gentle, individualistic and very loyal, Birman cats fall between Siamese and Persian in character. If you admire cats that are non aggressive, that enjoy being with humans and tend to be on the quiet side, you may well find that Birman cats are just the felines for you.</p>
* <p><span style="float: right;"><a href="http://developer.mozilla.org/en/docs/Image:BirmanCat.jpg" shape="rect" title="Image:BirmanCat.jpg"/></span>All Birmans have colorpointed features, dark coloration of the face, ears, legs and tail.</p>
* <p>Cat image and text coming from <a href="http://www.best-cat-art.com/" rel="nofollow" shape="rect" title="http://www.best-cat-art.com/">www.best-cat-art.com</a></p>
* <p id="PaddingBottomLabel" style="text-align: center; font-style: italic; font-weight: bold; font-size: 13px!important; font-family: Arial, sans-serif; margin: 0px;">padding-bottom</p></div><span style="position: absolute; left: -32px; top: 85px; color: blue; font-weight: bold; font-size: 13px!important; font-family: Arial, sans-serif;">Left</span><span style="position: absolute; left: 170px; top: -24px; color: blue; font-weight: bold; font-size: 13px!important; font-family: Arial, sans-serif;">Top</span><span style="position: absolute; left: 370px; top: 85px; color: blue; font-weight: bold; font-size: 13px!important; font-family: Arial, sans-serif;">Right</span><span style="position: absolute; left: 164px; top: 203px; color: blue; font-weight: bold; font-size: 13px!important; font-family: Arial, sans-serif;">Bottom</span><span style="position: absolute; left: 143px; top: 5px; font-style: italic; font-weight: bold; font-size: 13px!important; font-family: Arial, sans-serif;">margin-top</span><span style="position: absolute; left: 138px; top: 175px; font-style: italic; font-weight: bold; font-size: 13px!important; font-family: Arial, sans-serif;">margin-bottom</span><span style="position: absolute; left: 143px; top: 27px; color: white; font-style: italic; font-weight: bold; font-size: 13px!important; font-family: Arial, sans-serif;">border-top</span><span style="position: absolute; left: 138px; top: 153px; color: white; font-style: italic; font-weight: bold; font-size: 13px!important; font-family: Arial, sans-serif;">border-bottom</span></div>
* <p style="margin-top: 270px;"><a href="http://developer.mozilla.org/en/docs/Image:clientWidth.png" shape="rect" title="Image:clientWidth.png"/></p>
* <h2> <span>Specification</span></h2>
* <p>Not part of any W3C specification.
* </p>
* <h2> <span>Notes</span></h2>
* <p><b>clientWidth</b> was first introduced in the MS IE DHTML object model.
* </p>
* <h2> <span>References</span></h2>
* <ul><li> <a href="http://msdn.microsoft.com/workshop/author/dhtml/reference/properties/clientwidth.asp?frame=true" rel="nofollow" shape="rect" title="http://msdn.microsoft.com/workshop/author/dhtml/reference/properties/clientwidth.asp?frame=true">MSDN clientWidth definition</a>
* </li><li> <a href="http://msdn.microsoft.com/workshop/author/om/measuring.asp" rel="nofollow" shape="rect" title="http://msdn.microsoft.com/workshop/author/om/measuring.asp">MSDN Measuring Element Dimension and Location</a>
* </li><li> <a href="http://www.mozilla.org/docs/dom/domref/clientWidth.html" rel="nofollow" shape="rect" title="http://www.mozilla.org/docs/dom/domref/clientWidth.html">Gecko DOM Reference on clientWidth</a>
* </li></ul>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
clientWidth: undefined,
/**
* <h2> <span> Summary </span></h2>
* <p>Returns a duplicate of the current node.
* </p>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve"><i>dupNode</i> = <i>element</i>.cloneNode(<i>deep</i>);
* </pre>
* <ul><li> <code>deep</code> is a required boolean value indicating whether the clone is a deep clone or not (see <a href="DOM:element.cloneNode#Notes" shape="rect" title="">notes</a> below).
* </li></ul>
* <h2> <span> Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* p = document.getElementById("para1");
* p_prime = p.cloneNode(true);
* </pre>
* <h2> <span> Notes </span></h2>
* <p>Cloning a node copies all of its attributes and their values.
* </p><p>The duplicate node returned by <code><b>cloneNode</b></code> is not part of the document until it is added to another node that is part of the document using <a href="element.appendChild" shape="rect" title="DOM:element.appendChild">appendChild</a> or a similar method.  It also has no parent until it is appended to another node.
* </p><p>If <code><b>deep</b></code> is set to <code>false</code>, none of the child nodes are cloned.  <i>Any text that the node contains is not cloned either</i>, as it is contained in one or more child <code>Text</code> nodes.
* </p><p>If <code><b>deep</b></code> evaluates to <code>true</code>, the whole subtree (including text that may be in child Text nodes) is copied too. For empty nodes (e.g. IMG and INPUT elements) it doesn't matter whether <code>deep</code> is set to true or false but you still have to provide a value.
* </p><p>Note that cloneNode may lead to duplicate element-ids in a document!
* </p><p>To clone node for appending to a different document, use <a href="document.importNode" shape="rect" title="DOM:document.importNode">importNode</a> instead.
* </p>
* <h2> <span>Specification </span></h2>
* <p><a href="http://w3.org/TR/DOM-Level-2-Core/core.html#ID-3A0ED0A4" rel="nofollow" shape="rect" title="http://w3.org/TR/DOM-Level-2-Core/core.html#ID-3A0ED0A4">DOM Level 2 Core: cloneNode</a>
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
cloneNode: function(deep) {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span>Summary</span></h2>
* <p>The <b>dir</b> attribute gets or sets the text writing directionality of the content of the current element.
* </p>
* <h2> <span>Syntax and values</span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var <var>CurrentWritingDirection</var> = elementNodeReference.dir;
* elementNodeReference.dir = <var>NewWritingDirection</var>;
* </pre>
* <p><var>CurrentWritingDirection</var> is a string variable representing the text writing direction of the current element. <var>NewWritingDirection</var> is a string variable representing the text writing direction value.
* </p><p>Possible values for <b>dir</b> are <b><code>ltr</code>, for Left-to-right</b>, and <b><code>rtl</code>, for Right-to-left</b>.
* </p>
* <h2> <span>Example</span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* var parg = document.getElementById("para1");
* parg.dir = "rtl";
* // change the text direction on a paragraph identified as "para1"
* </pre>
* <h2> <span>Notes</span></h2>
* <p>The text writing directionality of an element is which direction that text goes (for support of different language systems). Arabic languages and Hebrew are typical languages using the rtl directionality.
* </p><p>An image can have its dir attribute set to rtl in which case the HTML attributes title and alt will be formatted and defined as rtl.
* </p><p>When a table has its dir set to rtl, the column order are arranged from right to left.
* </p>
* <h2> <span>Specification</span></h2>
* <p><a href="http://www.w3.org/TR/DOM-Level-2-HTML/html.html#ID-52460740" rel="nofollow" shape="rect" title="http://www.w3.org/TR/DOM-Level-2-HTML/html.html#ID-52460740">W3C DOM Level 2 HTML: dir</a>
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
dir: undefined,
/**
* <h2> <span> Summary </span></h2>
* <p>Dispatches an event into the event system. The event is still subject to the same capturing and bubbling behavior as directly dispatched events.
* </p>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve"><i>bool</i> = <i>element</i>.dispatchEvent(<i>event</i>)
* </pre>
* <ul><li> <code>element</code> is the <code>target</code> of the event.
* </li><li> <code>event</code> is an <a href="DOM:event" shape="rect" title="DOM:event">event</a> object to be dispatched.
* </li><li> The return value is <code>false</code>, if at least one of the event handlers which handled this event, called <a href="event.preventDefault" shape="rect" title="DOM:event.preventDefault">preventDefault</a>. Otherwise it returns <code>true</code>.
* </li></ul>
* <h2> <span> Example </span></h2>
* <p>This example demonstrates simulating a click on a checkbox using DOM methods. You can view the example in action <a href="http://developer.mozilla.org/samples/domref/dispatchEvent.html" rel="nofollow" shape="rect" title="http://developer.mozilla.org/samples/domref/dispatchEvent.html">here</a>.
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">function simulateClick() {
* var evt = <a href="document.createEvent" shape="rect" title="DOM:document.createEvent">document.createEvent</a>("MouseEvents");
* evt.<a href="event.initMouseEvent" shape="rect" title="DOM:event.initMouseEvent">initMouseEvent</a>("click", true, true, window,
* 0, 0, 0, 0, 0, false, false, false, false, 0, null);
* var cb = document.getElementById("checkbox");
* var canceled = !cb.<strong>dispatchEvent</strong>(evt);
* if(canceled) {
* // A handler called preventDefault
* alert("canceled");
* } else {
* // None of the handlers called preventDefault
* alert("not canceled");
* }
* }
* </pre>
* <p>
* </p><p>
* </p>
* <h2> <span> Notes </span></h2>
* <p>As demonstrated in the above example, <code>dispatchEvent</code> is the last step of the create-init-dispatch process, which is used for manually dispatching events into the implementation's event model.
* </p><p>The event can be created using <a href="DOM:document.createEvent" shape="rect" title="DOM:document.createEvent">document.createEvent</a> method and initialized using <a href="event.initEvent" shape="rect" title="DOM:event.initEvent">initEvent</a> or other, more specific, initialization methods, such as <a href="event.initMouseEvent" shape="rect" title="DOM:event.initMouseEvent">initMouseEvent</a> or <a href="event.initUIEvent" shape="rect" title="DOM:event.initUIEvent">initUIEvent</a>.
* </p><p>See also the <a href="event" shape="rect" title="DOM:event">Event object reference</a>.
* </p>
* <h2> <span> Specification </span></h2>
* <p><a href="http://www.w3.org/TR/DOM-Level-2-Events/events.html#Events-EventTarget-dispatchEvent" rel="nofollow" shape="rect" title="http://www.w3.org/TR/DOM-Level-2-Events/events.html#Events-EventTarget-dispatchEvent">DOM Level 2 Events: dispatchEvent</a>
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
dispatchEvent: function(event) {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span> Summary </span></h2>
* <p>Returns the node's first child in the tree, or <code>null</code> if the node is childless.
* </p>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve"><i>childNode</i> = <i>node</i>.firstChild;
* </pre>
* <p><code>childNode</code> is a reference to the first child of <code>node</code> if there is one, otherwise it's <code>null</code>.
* </p>
* <h2> <span> Example </span></h2>
* <p>This example demonstrates the use of <code>firstChild</code> and how whitespace nodes might interfere with using this property. See the <a href="element.firstChild#Notes" shape="rect" title="">Notes</a> section for more information about whitespace handling in Gecko DOM.
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* &lt;p id="para-01"&gt;
* &lt;span&gt;First span&lt;/span&gt;
* &lt;/p&gt;
* 
* &lt;script type="text/javascript"&gt;
* var p01 = document.getElementById('para-01');
* alert(p01.firstChild.nodeName)
* &lt;/script&gt;
* </pre>
* <p>In the above, the alert will show '#text' because a text node is inserted to maintain the whitespace between the end of the opening &lt;p&gt; and &lt;span&gt; tags. <b>Any</b> whitespace will cause the #text node to be inserted, from a single space to any number of spaces, returns, tabs, and so on.
* </p><p>Another #text node is inserted between the closing &lt;/span&gt; and &lt;/p&gt;tags.
* </p><p>If this whitespace is removed from the source, the #text nodes are not inserted and the span element becomes the paragraph's first child.
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* &lt;p id="para-01"&gt;&lt;span&gt;First span&lt;/span&gt;&lt;/p&gt;
* 
* &lt;script type="text/javascript"&gt;
* var p01 = document.getElementById('para-01');
* alert(p01.firstChild.nodeName)
* &lt;/script&gt;
* </pre>
* <p>Now the alert will show 'SPAN'.
* </p>
* <h2> <span> Notes </span></h2>
* <p>Gecko-based browsers insert text nodes into a document to represent whitespace in the source markup.  Therefore a node obtained for example via <strong>firstChild</strong> or <a href="DOM:element.previousSibling" shape="rect" title="DOM:element.previousSibling">previousSibling</a> may refer to a whitespace text node, rather than the actual element the author intended to get.
* </p><p>See <a href="http://developer.mozilla.org/en/docs/Whitespace_in_the_DOM" shape="rect" title="Whitespace in the DOM">Whitespace in the DOM</a> and <a href="http://www.w3.org/DOM/faq.html#emptytext" rel="nofollow" shape="rect" title="http://www.w3.org/DOM/faq.html#emptytext">W3C DOM 3 FAQ: Why are some Text nodes empty?</a> for more information.
* </p><p>Sometimes <code>document.firstChild</code> is used to obtain the root element of a document. This is incorrect, as it will return a processing instruction or another node from the prolog if there are any, <code><a href="document.documentElement" shape="rect" title="DOM:document.documentElement">document.documentElement</a></code> should be used instead.
* </p>
* <h2> <span> Specification </span></h2>
* <p><a href="http://www.w3.org/TR/REC-DOM-Level-1/level-one-core.html#attribute-firstChild" rel="nofollow" shape="rect" title="http://www.w3.org/TR/REC-DOM-Level-1/level-one-core.html#attribute-firstChild">DOM Level 1 Core: firstChild</a>
* </p><p><a href="http://www.w3.org/TR/DOM-Level-2-Core/core.html#ID-169727388" rel="nofollow" shape="rect" title="http://www.w3.org/TR/DOM-Level-2-Core/core.html#ID-169727388">DOM Level 2 Core: firstChild</a>
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
firstChild: undefined,
/**
* <h2> <span> Summary </span></h2>
* <p>Sets focus on the specified element.
* </p>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve"><i>element</i>.focus();
* </pre>
* <h2> <span> Notes </span></h2>
* <p>Gives keyboard focus to the element. See also <code><a href="element.blur" shape="rect" title="DOM:element.blur">blur()</a></code>.
* </p>
* <h2> <span> Specification </span></h2>
* <p><a href="http://www.w3.org/TR/REC-DOM-Level-1/level-one-html.html#method-focus" rel="nofollow" shape="rect" title="http://www.w3.org/TR/REC-DOM-Level-1/level-one-html.html#method-focus">DOM Level 1 HTML: <i>various elements</i>.focus</a>
* </p><p><a href="http://www.w3.org/TR/DOM-Level-2-HTML/html.html#ID-32130014" rel="nofollow" shape="rect" title="http://www.w3.org/TR/DOM-Level-2-HTML/html.html#ID-32130014">DOM Level 2 HTML: <i>various elements</i>.focus</a>
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
focus: function() {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span> Summary </span></h2>
* <p><code>getAttribute()</code> returns the value of the named attribute on the specified element.  If the named attribute does not exist, the value returned will either be <code>null</code> or <code>""</code> (the empty string); see <a href="element.getAttribute#Notes" shape="rect" title="">Notes</a> for details.
* </p>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve"><i>attribute</i> = element.getAttribute(<i>attributeName</i>)
* </pre>
* <p>where
* </p>
* <ul><li> <code><i>attribute</i></code> is a string containing the value of <code><i>attributeName</i></code>.
* </li><li> <code><i>attributeName</i></code> is the name of the attribute whose value you want to get.
* </li></ul>
* <h2> <span> Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var div1 = document.getElementById("div1");
* var align = div1.getAttribute("align");
* alert(align); // shows the value of align for the element with id="div1"
* </pre>
* <h2> <span> Notes </span></h2>
* <p>Essentially all web browsers (Firefox, Internet Explorer, recent versions of Opera, Safari, Konqueror, and iCab, as a non-exhaustive list) return <code>null</code> when the specified attribute does not exist on the specified element.  The DOM specification says that the correct return value in this case is actually the <i>empty string</i>, and some DOM implementations implement this behavior.  Consequently, you should use <a href="DOM:element.hasAttribute" shape="rect" title="DOM:element.hasAttribute">hasAttribute</a> to check for an attribute's existence prior to calling <code>getAttribute()</code> if it is possible that the requested attribute does not exist on the specified element.
* </p><p>The <code>attributeName</code> parameter is usually case sensitive, but it is case-insensitive when used upon HTML elements.
* </p><p>DOM methods dealing with element's attributes:
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <th colspan="1" rowspan="1">Not namespace-aware, most commonly used methods</th>
* <th colspan="1" rowspan="1">Namespace-aware variants (DOM Level 2)</th>
* <th colspan="1" rowspan="1">DOM Level 1 methods for dealing with <code>Attr</code> nodes directly (seldom used)</th>
* <th colspan="1" rowspan="1">DOM Level 2 namespace-aware methods for dealing with <code>Attr</code> nodes directly (seldom used)</th>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><a href="element.setAttribute" shape="rect" title="DOM:element.setAttribute">setAttribute</a> (DOM 1)</td>
* <td colspan="1" rowspan="1"><a href="element.setAttributeNS" shape="rect" title="DOM:element.setAttributeNS">setAttributeNS</a></td>
* <td colspan="1" rowspan="1"><a href="element.setAttributeNode" shape="rect" title="DOM:element.setAttributeNode">setAttributeNode</a></td>
* <td colspan="1" rowspan="1"><a href="element.setAttributeNodeNS" shape="rect" title="DOM:element.setAttributeNodeNS">setAttributeNodeNS</a></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><strong>getAttribute</strong> (DOM 1)</td>
* <td colspan="1" rowspan="1"><a href="element.getAttributeNS" shape="rect" title="DOM:element.getAttributeNS">getAttributeNS</a></td>
* <td colspan="1" rowspan="1"><a href="element.getAttributeNode" shape="rect" title="DOM:element.getAttributeNode">getAttributeNode</a></td>
* <td colspan="1" rowspan="1"><a href="element.getAttributeNodeNS" shape="rect" title="DOM:element.getAttributeNodeNS">getAttributeNodeNS</a></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><a href="element.hasAttribute" shape="rect" title="DOM:element.hasAttribute">hasAttribute</a> (DOM 2)</td>
* <td colspan="1" rowspan="1"><a href="element.hasAttributeNS" shape="rect" title="DOM:element.hasAttributeNS">hasAttributeNS</a></td>
* <td colspan="1" rowspan="1">-</td>
* <td colspan="1" rowspan="1">-</td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><a href="element.removeAttribute" shape="rect" title="DOM:element.removeAttribute">removeAttribute</a> (DOM 1)</td>
* <td colspan="1" rowspan="1"><a href="element.removeAttributeNS" shape="rect" title="DOM:element.removeAttributeNS">removeAttributeNS</a></td>
* <td colspan="1" rowspan="1"><a href="element.removeAttributeNode" shape="rect" title="DOM:element.removeAttributeNode">removeAttributeNode</a></td>
* <td colspan="1" rowspan="1">-</td>
* </tr>
* </table>
* <h2> <span> Specification </span></h2>
* <p><a href="http://www.w3.org/TR/DOM-Level-2-Core/core.html#ID-666EE0F9" rel="nofollow" shape="rect" title="http://www.w3.org/TR/DOM-Level-2-Core/core.html#ID-666EE0F9">DOM Level 2 Core: getAttribute</a> (introduced in <a href="http://www.w3.org/TR/REC-DOM-Level-1/level-one-core.html#method-getAttribute" rel="nofollow" shape="rect" title="http://www.w3.org/TR/REC-DOM-Level-1/level-one-core.html#method-getAttribute">DOM Level 1 Core</a>)
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
getAttribute: function(attributeName) {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span> Summary </span></h2>
* <p><code>getAttributeNS</code> returns the string value of the attribute with the specified namespace and name.  If the named attribute does not exist, the value returned will either be <code>null</code> or <code>""</code> (the empty string); see <a href="element.getAttributeNS#Notes" shape="rect" title="">Notes</a> for details.
* </p>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve"><i>attrVal</i> = <i>element</i>.getAttributeNS(<i>namespace</i>, <i>name</i>)
* </pre>
* <h2> <span> Parameters </span></h2>
* <ul><li> <code><i>attrVal</i></code> is the string value of the specified attribute.
* </li><li> <code><i>namespace</i></code> is the namespace of the specified attribute.
* </li><li> <code><i>name</i></code> is the name of the specified attribute.
* </li></ul>
* <h2> <span> Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var div1 = document.getElementById("div1");
* var a = div1.getAttributeNS("www.mozilla.org/ns/specialspace/",
* "special-align");
* alert(a); // shows the value of align for that div
* </pre>
* <h2> <span> Notes </span></h2>
* <p><code>getAttributeNS</code> differs from <a href="element.getAttribute" shape="rect" title="DOM:element.getAttribute">getAttribute</a> in that it allows you to further specify the requested attribute as being part of a particular namespace, as in the example above, where the attribute is part of the fictional "specialspace" namespace on mozilla.
* </p><p>Essentially all web browsers (Firefox, Internet Explorer, recent versions of Opera, Safari, Konqueror, and iCab, as a non-exhaustive list) return <code>null</code> when the specified attribute does not exist on the specified element.  The DOM specification says that the correct return value in this case is actually the <i>empty string</i>, and some DOM implementations implement this behavior.  Consequently, you should use <a href="element.hasAttributeNS" shape="rect" title="DOM:element.hasAttributeNS">hasAttributeNS</a> to check for an attribute's existence prior to calling <code>getAttributeNS</code> if it is possible that the requested attribute does not exist on the specified element.
* </p><p>DOM methods dealing with element's attributes:
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <th colspan="1" rowspan="1">Not namespace-aware, most commonly used methods</th>
* <th colspan="1" rowspan="1">Namespace-aware variants (DOM Level 2)</th>
* <th colspan="1" rowspan="1">DOM Level 1 methods for dealing with <code>Attr</code> nodes directly (seldom used)</th>
* <th colspan="1" rowspan="1">DOM Level 2 namespace-aware methods for dealing with <code>Attr</code> nodes directly (seldom used)</th>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><a href="element.setAttribute" shape="rect" title="DOM:element.setAttribute">setAttribute</a> (DOM 1)</td>
* <td colspan="1" rowspan="1"><a href="element.setAttributeNS" shape="rect" title="DOM:element.setAttributeNS">setAttributeNS</a></td>
* <td colspan="1" rowspan="1"><a href="element.setAttributeNode" shape="rect" title="DOM:element.setAttributeNode">setAttributeNode</a></td>
* <td colspan="1" rowspan="1"><a href="element.setAttributeNodeNS" shape="rect" title="DOM:element.setAttributeNodeNS">setAttributeNodeNS</a></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><a href="element.getAttribute" shape="rect" title="DOM:element.getAttribute">getAttribute</a> (DOM 1)</td>
* <td colspan="1" rowspan="1"><strong>getAttributeNS</strong></td>
* <td colspan="1" rowspan="1"><a href="element.getAttributeNode" shape="rect" title="DOM:element.getAttributeNode">getAttributeNode</a></td>
* <td colspan="1" rowspan="1"><a href="element.getAttributeNodeNS" shape="rect" title="DOM:element.getAttributeNodeNS">getAttributeNodeNS</a></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><a href="element.hasAttribute" shape="rect" title="DOM:element.hasAttribute">hasAttribute</a> (DOM 2)</td>
* <td colspan="1" rowspan="1"><a href="element.hasAttributeNS" shape="rect" title="DOM:element.hasAttributeNS">hasAttributeNS</a></td>
* <td colspan="1" rowspan="1">-</td>
* <td colspan="1" rowspan="1">-</td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><a href="element.removeAttribute" shape="rect" title="DOM:element.removeAttribute">removeAttribute</a> (DOM 1)</td>
* <td colspan="1" rowspan="1"><a href="element.removeAttributeNS" shape="rect" title="DOM:element.removeAttributeNS">removeAttributeNS</a></td>
* <td colspan="1" rowspan="1"><a href="element.removeAttributeNode" shape="rect" title="DOM:element.removeAttributeNode">removeAttributeNode</a></td>
* <td colspan="1" rowspan="1">-</td>
* </tr>
* </table>
* <h2> <span> Specification </span></h2>
* <p><a href="http://www.w3.org/TR/DOM-Level-2-Core/core.html#ID-ElGetAttrNS" rel="nofollow" shape="rect" title="http://www.w3.org/TR/DOM-Level-2-Core/core.html#ID-ElGetAttrNS">DOM Level 2 Core: getAttributeNS</a>
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
getAttributeNS: function(namespace, name) {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span> Summary </span></h2>
* <p>Returns the specified attribute of the specified element, as an <code>Attr</code> node.
* </p>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve"><i>attrNode</i> = <i>element</i>.getAttributeNode(<i>attrName</i>)
* </pre>
* <ul><li> <code>attrNode</code> is an <code>Attr</code> node for the attribute.
* </li><li> <code>attrName</code> is a string containing the name of the attribute.
* </li></ul>
* <h2> <span> Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* // html: &lt;div id="top" /&gt;
* var t = document.getElementById("top");
* var idAttr = t.getAttributeNode("id");
* alert(idAttr.value == "top")
* 
* </pre>
* <h2> <span> Notes </span></h2>
* <p>The <code>Attr</code> node inherits from <code>Node</code>, but is not considered a part of the document tree. Common <code>Node</code> attributes like <a href="DOM:element.parentNode" shape="rect" title="DOM:element.parentNode">parentNode</a>, <a href="element.previousSibling" shape="rect" title="DOM:element.previousSibling">previousSibling</a>, and <a href="element.nextSibling" shape="rect" title="DOM:element.nextSibling">nextSibling</a> are <code>null</code> for an <code>Attr</code> node. You can, however, get the element to which the attribute belongs with the <code>ownerElement</code> property.
* </p><p><a href="element.getAttribute" shape="rect" title="DOM:element.getAttribute">getAttribute</a> is usually used instead of <code>getAttributeNode</code> to get the value of an element's attribute.
* </p><p>DOM methods dealing with element's attributes:
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <th colspan="1" rowspan="1">Not namespace-aware, most commonly used methods</th>
* <th colspan="1" rowspan="1">Namespace-aware variants (DOM Level 2)</th>
* <th colspan="1" rowspan="1">DOM Level 1 methods for dealing with <code>Attr</code> nodes directly (seldom used)</th>
* <th colspan="1" rowspan="1">DOM Level 2 namespace-aware methods for dealing with <code>Attr</code> nodes directly (seldom used)</th>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><a href="element.setAttribute" shape="rect" title="DOM:element.setAttribute">setAttribute</a> (DOM 1)</td>
* <td colspan="1" rowspan="1"><a href="element.setAttributeNS" shape="rect" title="DOM:element.setAttributeNS">setAttributeNS</a></td>
* <td colspan="1" rowspan="1"><a href="element.setAttributeNode" shape="rect" title="DOM:element.setAttributeNode">setAttributeNode</a></td>
* <td colspan="1" rowspan="1"><a href="element.setAttributeNodeNS" shape="rect" title="DOM:element.setAttributeNodeNS">setAttributeNodeNS</a></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><a href="element.getAttribute" shape="rect" title="DOM:element.getAttribute">getAttribute</a> (DOM 1)</td>
* <td colspan="1" rowspan="1"><a href="element.getAttributeNS" shape="rect" title="DOM:element.getAttributeNS">getAttributeNS</a></td>
* <td colspan="1" rowspan="1"><strong>getAttributeNode</strong></td>
* <td colspan="1" rowspan="1"><a href="element.getAttributeNodeNS" shape="rect" title="DOM:element.getAttributeNodeNS">getAttributeNodeNS</a></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><a href="element.hasAttribute" shape="rect" title="DOM:element.hasAttribute">hasAttribute</a> (DOM 2)</td>
* <td colspan="1" rowspan="1"><a href="element.hasAttributeNS" shape="rect" title="DOM:element.hasAttributeNS">hasAttributeNS</a></td>
* <td colspan="1" rowspan="1">-</td>
* <td colspan="1" rowspan="1">-</td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><a href="element.removeAttribute" shape="rect" title="DOM:element.removeAttribute">removeAttribute</a> (DOM 1)</td>
* <td colspan="1" rowspan="1"><a href="element.removeAttributeNS" shape="rect" title="DOM:element.removeAttributeNS">removeAttributeNS</a></td>
* <td colspan="1" rowspan="1"><a href="element.removeAttributeNode" shape="rect" title="DOM:element.removeAttributeNode">removeAttributeNode</a></td>
* <td colspan="1" rowspan="1">-</td>
* </tr>
* </table>
* <h2> <span> Specification </span></h2>
* <p><a href="http://www.w3.org/TR/DOM-Level-2-Core/core.html#ID-217A91B8" rel="nofollow" shape="rect" title="http://www.w3.org/TR/DOM-Level-2-Core/core.html#ID-217A91B8">DOM Level 2 Core: getAttributeNode</a> (introduced in <a href="http://www.w3.org/TR/REC-DOM-Level-1/level-one-core.html#method-getAttributeNode" rel="nofollow" shape="rect" title="http://www.w3.org/TR/REC-DOM-Level-1/level-one-core.html#method-getAttributeNode">DOM Level 1 Core</a>)
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
getAttributeNode: function(attrName) { // COMPAT=IE6|IE7|FF1|FF2|FF3|OPERA|SAFARI2|SAFARI3|KONQ
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span> Summary </span></h2>
* <p>Returns the <code>Attr</code> node for the attribute with the given namespace and name.
* </p>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve"><i>attributeNode</i> = element.getAttributeNodeNS(<i>namespace</i>, <i>nodeName</i>)
* </pre>
* <ul><li> <code>attributeNode</code> is the node for specified attribute.
* </li><li> <code>namespace</code> is a string specifying the namespace of the attribute.
* </li><li> <code>nodeName</code> is a string specifying the name of the attribute.
* </li></ul>
* <p>
* </p>
* <h2> <span> Notes </span></h2>
* <p><code>getAttributeNodeNS</code> is more specific than <a href="element.getAttributeNode" shape="rect" title="DOM:element.getAttributeNode">getAttributeNode</a> in that it allows you to specify attributes that are part of a particular namespace. The corresponding setter method is <a href="element.setAttributeNodeNS" shape="rect" title="DOM:element.setAttributeNodeNS">setAttributeNodeNS</a>.
* </p><p>DOM methods dealing with element's attributes:
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <th colspan="1" rowspan="1">Not namespace-aware, most commonly used methods</th>
* <th colspan="1" rowspan="1">Namespace-aware variants (DOM Level 2)</th>
* <th colspan="1" rowspan="1">DOM Level 1 methods for dealing with <code>Attr</code> nodes directly (seldom used)</th>
* <th colspan="1" rowspan="1">DOM Level 2 namespace-aware methods for dealing with <code>Attr</code> nodes directly (seldom used)</th>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><a href="element.setAttribute" shape="rect" title="DOM:element.setAttribute">setAttribute</a> (DOM 1)</td>
* <td colspan="1" rowspan="1"><a href="element.setAttributeNS" shape="rect" title="DOM:element.setAttributeNS">setAttributeNS</a></td>
* <td colspan="1" rowspan="1"><a href="element.setAttributeNode" shape="rect" title="DOM:element.setAttributeNode">setAttributeNode</a></td>
* <td colspan="1" rowspan="1"><a href="element.setAttributeNodeNS" shape="rect" title="DOM:element.setAttributeNodeNS">setAttributeNodeNS</a></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><a href="element.getAttribute" shape="rect" title="DOM:element.getAttribute">getAttribute</a> (DOM 1)</td>
* <td colspan="1" rowspan="1"><a href="element.getAttributeNS" shape="rect" title="DOM:element.getAttributeNS">getAttributeNS</a></td>
* <td colspan="1" rowspan="1"><a href="element.getAttributeNode" shape="rect" title="DOM:element.getAttributeNode">getAttributeNode</a></td>
* <td colspan="1" rowspan="1"><strong>getAttributeNodeNS</strong></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><a href="element.hasAttribute" shape="rect" title="DOM:element.hasAttribute">hasAttribute</a> (DOM 2)</td>
* <td colspan="1" rowspan="1"><a href="element.hasAttributeNS" shape="rect" title="DOM:element.hasAttributeNS">hasAttributeNS</a></td>
* <td colspan="1" rowspan="1">-</td>
* <td colspan="1" rowspan="1">-</td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><a href="element.removeAttribute" shape="rect" title="DOM:element.removeAttribute">removeAttribute</a> (DOM 1)</td>
* <td colspan="1" rowspan="1"><a href="element.removeAttributeNS" shape="rect" title="DOM:element.removeAttributeNS">removeAttributeNS</a></td>
* <td colspan="1" rowspan="1"><a href="element.removeAttributeNode" shape="rect" title="DOM:element.removeAttributeNode">removeAttributeNode</a></td>
* <td colspan="1" rowspan="1">-</td>
* </tr>
* </table>
* <h2> <span> Specification </span></h2>
* <p><a href="http://www.w3.org/TR/DOM-Level-2-Core/core.html#ID-ElGetAtNodeNS" rel="nofollow" shape="rect" title="http://www.w3.org/TR/DOM-Level-2-Core/core.html#ID-ElGetAtNodeNS">DOM Level 2 Core: getAttributeNodeNS</a>
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
getAttributeNodeNS: function(namespace, nodeName) {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <div style="border: 1px solid #818151; background-color: #FFFFE1; font-weight: bold; text-align: center; padding: 0px 10px 0px 10px; margin: 10px 0px 10px 0px;"><p style="margin: 4px 0px 4px 0px;">This article covers features introduced in <a href="http://developer.mozilla.org/en/docs/Firefox_3_for_developers" shape="rect" title="Firefox 3 for developers">Firefox 3</a></p></div>
* 
* <h2> <span>Summary</span></h2>
* <p>Returns a text rectangle object that encloses a group of text rectangles.
* </p>
* <h2> <span>Syntax</span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var <i>rectObject</i> = <var>object</var>.getBoundingClientRect();
* </pre>
* <h2> <span>Returns</span></h2>
* <p>The returned value is a TextRectangle object which is the union of the rectangles returned by <code>getClientRects()</code> for the element, i.e., the CSS border-boxes associated with the element.
* </p><p>Empty border-boxes are completely ignored. If all the element's border-boxes are empty, then a rectangle is returned with a width and height of zero and where the <code>top</code> and <code>left</code> are the top-left of the border-box for the first CSS box (in content order) for the element.
* </p><p>The amount of scrolling that has been done of the viewport area is taken into account when computing the bounding rectangle.
* </p>
* <h2> <span>Example</span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var rect = obj.getBoundingClientRect();
* </pre>
* <h2> <span>Specification</span></h2>
* <p>Not part of any W3C specification.
* </p>
* <h2> <span>Notes</span></h2>
* <p><code>getBoundingClientRect()</code> was first introduced in the MS IE DHTML object model.
* </p>
* <h2> <span>References</span></h2>
* <ul><li> <a href="http://msdn2.microsoft.com/en-us/library/ms536433.aspx" rel="nofollow" shape="rect" title="http://msdn2.microsoft.com/en-us/library/ms536433.aspx">MSDN's getBoundingClientRect definition</a>
* </li></ul>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
getBoundingClientRect: function() {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <div style="border: 1px solid #818151; background-color: #FFFFE1; font-weight: bold; text-align: center; padding: 0px 10px 0px 10px; margin: 10px 0px 10px 0px;"><p style="margin: 4px 0px 4px 0px;">This article covers features introduced in <a href="http://developer.mozilla.org/en/docs/Firefox_3_for_developers" shape="rect" title="Firefox 3 for developers">Firefox 3</a></p></div>
* 
* <h2> <span>Summary</span></h2>
* <p>Returns a collection of rectangles that indicate the bounding rectangles for each line of text in a client.
* </p>
* <h2> <span>Syntax</span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var <i>rectCollection</i> = <var>object</var>.getClientRects();
* </pre>
* <h2> <span>Returns</span></h2>
* <p>The returned value is a collection of TextRectangle objects, one for each CSS border-box associated with the element. Each TextRectangle object contains read-only <code>left</code>, <code>top</code>, <code>right</code> and <code>bottom</code> properties describing the border-box, in pixels, with the top-left relative to the top-left of the document's CSS canvas, unless the element is inside an SVG <code>foreignobject</code> element, in which case the top-left is relative to the nearest <code>foreignobject</code> ancestor and in the coordinate system of that <code>foreignobject</code>.
* </p><p>The amount of scrolling that has been done of the viewport area is taken into account when computing the rectangles.
* </p><p>The returned rectangles do not include the bounds of any child elements that
* might happen to overflow.
* </p><p>For HTML AREA elements, SVG elements that do not render anything themselves, <code>display:none</code> elements, and generally any elements that are not directly rendered, an empty list is returned.
* </p><p>Rectangles are returned even for CSS boxes that have empty border-boxes. The <code>left</code>, <code>top</code>, <code>right</code> and <code>bottom</code> coordinates can still be meaningful.
* </p><p>Fractional pixel offsets are possible.
* </p>
* <h2> <span>Example</span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var rects = obj.getClientRects();
* var numLines = rects.length;
* </pre>
* <h2> <span>Specification</span></h2>
* <p>Not part of any W3C specification.
* </p>
* <h2> <span>Notes</span></h2>
* <p><code>getClientRects()</code> was first introduced in the MS IE DHTML object model.
* </p>
* <h2> <span>References</span></h2>
* <ul><li> <a href="http://msdn2.microsoft.com/en-us/library/ms536435.aspx" rel="nofollow" shape="rect" title="http://msdn2.microsoft.com/en-us/library/ms536435.aspx">MSDN's getClientRects definition</a>
* </li></ul>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
getClientRects: function() {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span>Summary</span></h2>
* <p>Returns a list of elements with the given <a href="element.tagName" shape="rect" title="DOM:element.tagName">tag name</a>. The subtree underneath the specified element is searched, excluding the element itself.
* </p>
* <h2> <span>Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve"> <i>elements</i> = <i>element</i>.getElementsByTagName(<i>tagName</i>)
* </pre>
* <ul><li> <code>elements</code> is a live <code>NodeList</code> of found elements in the order they appear in the subtree.
* </li><li> <code>element</code> is the element from where the search should start. Note that only the descendants of this element are included in the search, but not the element itself.
* </li><li> <code>tagName</code> is the qualified name to look for. The special string <code>"*"</code> represents all elements.
* </li></ul>
* <div>
* <p>In Firefox 2 (Gecko 1.8.1) and earlier this method didn't work correctly if the subtree had elements with namespace prefix in the tag name (See <a href="https://bugzilla.mozilla.org/show_bug.cgi?id=206053" rel="nofollow" shape="rect" title="https://bugzilla.mozilla.org/show_bug.cgi?id=206053">bug 206053</a> for details.)
* </p><p>It's recommended to use <code><a href="element.getElementsByTagNameNS" shape="rect" title="DOM:element.getElementsByTagNameNS">element.getElementsByTagNameNS</a></code> when dealing with multi-namespace documents.
* </p>
* </div>
* <h2> <span>Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* // check the alignment on a number of cells in a table.
* var table = document.getElementById("forecast-table");
* var cells = table.getElementsByTagName("td");
* for (var i = 0; i &lt; cells.length; i++) {
* status = cells[i].getAttribute("status");
* if ( status == "open") {
* // grab the data
* }
* }
* </pre>
* <h2> <span>Notes </span></h2>
* <p><code>element.getElementsByTagName</code> is similar to <a href="document.getElementsByTagName" shape="rect" title="DOM:document.getElementsByTagName">document.getElementsByTagName</a>, except that its search is restricted to those elements which are descendants of the specified element.
* </p>
* <h2> <span>Specification </span></h2>
* <p><a href="http://www.w3.org/TR/DOM-Level-2-Core/core.html#ID-1938918D" rel="nofollow" shape="rect" title="http://www.w3.org/TR/DOM-Level-2-Core/core.html#ID-1938918D">DOM Level 2 Core: Element.getElementsByTagName </a>
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
getElementsByTagName: function(tagName) {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span> Summary </span></h2>
* <p>Returns a list of elements with the given tag name belonging to the given namespace.
* </p>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve"><i>elements</i> = <i>element</i>.getElementsByTagNameNS(<i>namespaceURI</i>, <i>localName</i>)
* </pre>
* <ul><li> <code>elements</code> is a live <code>NodeList</code> of found elements in the order they appear in the tree.
* </li><li> <code>element</code> is the element from where the search should start. Note that only the descendants of this element are included in the search, not the node itself.
* </li><li> <code>namespaceURI</code> is the namespace URI of elements to look for (see <code><a href="DOM:element.namespaceURI" shape="rect" title="DOM:element.namespaceURI">element.namespaceURI</a></code>). For example, if you need to look for XHTML elements, use the XHTML namespace URI, <code>http://www.w3.org/1999/xhtml</code>.
* </li><li> <code>localName</code> is either the local name of elements to look for or the special value <code>"*"</code>, which matches all elements (see <code><a href="element.localName" shape="rect" title="DOM:element.localName">element.localName</a></code>).
* </li></ul>
* <h2> <span> Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* // check the alignment on a number of cells in a table in an XHTML document.
* var table = document.getElementById("forecast-table");
* var cells = table.getElementsByTagNameNS("http://www.w3.org/1999/xhtml", "td");
* for (var i = 0; i &lt; cells.length; i++) {
* var axis = cells[i].getAttribute("axis");
* if (axis == "year") {
* // grab the data
* }
* }
* </pre>
* <h2> <span> Notes </span></h2>
* <p><code>element.getElementsByTagNameNS</code> is similar to <code><a href="document.getElementsByTagNameNS" shape="rect" title="DOM:document.getElementsByTagNameNS">document.getElementsByTagNameNS</a></code>, except that its search is restricted to descendants of the specified element.
* </p>
* <h2> <span> Specification </span></h2>
* <p><a href="http://www.w3.org/TR/DOM-Level-2-Core/core.html#ID-A6C90942" rel="nofollow" shape="rect" title="http://www.w3.org/TR/DOM-Level-2-Core/core.html#ID-A6C90942">DOM Level 2 Core: Element.getElementsByTagNameNS</a>
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
getElementsByTagNameNS: function(namespaceURI, localName) {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span> Summary </span></h2>
* <p><code>hasAttribute</code> returns a boolean value indicating whether the specified element has the specified attribute or not.
* </p>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve"><i>result</i> = <i>element</i>.hasAttribute(<i>attName</i>)
* </pre>
* <ul><li> <code>result</code> holds the return value <code>true</code> or <code>false</code>.
* </li><li> <code>attName</code> is a string representing the name of the attribute.
* </li></ul>
* <h2> <span> Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* // check that the attribute exists before setting a value
* var d = document.getElementById("div1");
* if d.hasAttribute("align") {
* d.setAttribute("align", "center");
* }
* </pre>
* <h2> <span> Notes </span></h2>
* <p>DOM methods dealing with element's attributes:
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <th colspan="1" rowspan="1">Not namespace-aware, most commonly used methods</th>
* <th colspan="1" rowspan="1">Namespace-aware variants (DOM Level 2)</th>
* <th colspan="1" rowspan="1">DOM Level 1 methods for dealing with <code>Attr</code> nodes directly (seldom used)</th>
* <th colspan="1" rowspan="1">DOM Level 2 namespace-aware methods for dealing with <code>Attr</code> nodes directly (seldom used)</th>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><a href="DOM:element.setAttribute" shape="rect" title="DOM:element.setAttribute">setAttribute</a> (DOM 1)</td>
* <td colspan="1" rowspan="1"><a href="element.setAttributeNS" shape="rect" title="DOM:element.setAttributeNS">setAttributeNS</a></td>
* <td colspan="1" rowspan="1"><a href="element.setAttributeNode" shape="rect" title="DOM:element.setAttributeNode">setAttributeNode</a></td>
* <td colspan="1" rowspan="1"><a href="element.setAttributeNodeNS" shape="rect" title="DOM:element.setAttributeNodeNS">setAttributeNodeNS</a></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><a href="element.getAttribute" shape="rect" title="DOM:element.getAttribute">getAttribute</a> (DOM 1)</td>
* <td colspan="1" rowspan="1"><a href="element.getAttributeNS" shape="rect" title="DOM:element.getAttributeNS">getAttributeNS</a></td>
* <td colspan="1" rowspan="1"><a href="element.getAttributeNode" shape="rect" title="DOM:element.getAttributeNode">getAttributeNode</a></td>
* <td colspan="1" rowspan="1"><a href="element.getAttributeNodeNS" shape="rect" title="DOM:element.getAttributeNodeNS">getAttributeNodeNS</a></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><strong>hasAttribute</strong> (DOM 2)</td>
* <td colspan="1" rowspan="1"><a href="element.hasAttributeNS" shape="rect" title="DOM:element.hasAttributeNS">hasAttributeNS</a></td>
* <td colspan="1" rowspan="1">-</td>
* <td colspan="1" rowspan="1">-</td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><a href="element.removeAttribute" shape="rect" title="DOM:element.removeAttribute">removeAttribute</a> (DOM 1)</td>
* <td colspan="1" rowspan="1"><a href="element.removeAttributeNS" shape="rect" title="DOM:element.removeAttributeNS">removeAttributeNS</a></td>
* <td colspan="1" rowspan="1"><a href="element.removeAttributeNode" shape="rect" title="DOM:element.removeAttributeNode">removeAttributeNode</a></td>
* <td colspan="1" rowspan="1">-</td>
* </tr>
* </table>
* <h2> <span> Specification </span></h2>
* <p><a href="http://www.w3.org/TR/DOM-Level-2-Core/core.html#ID-ElHasAttr" rel="nofollow" shape="rect" title="http://www.w3.org/TR/DOM-Level-2-Core/core.html#ID-ElHasAttr">DOM Level 2 Core: hasAttribute</a>
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
hasAttribute: function(attName) { // COMPAT=FF1|FF2|FF3|OPERA|SAFARI2|SAFARI3|KONQ
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span> Summary </span></h2>
* <p><code>hasAttributeNS</code> returns a boolean value indicating whether the current element has the specified attribute.
* </p>
* <h2> <span>Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve"><i>result</i> = <i>element</i>.hasAttributeNS(<i>namespace</i>, <i>localName</i>)
* </pre>
* <ul><li> <code>result</code> is the boolean value <code>true</code> or <code>false</code>.
* </li><li> <code>namespace</code> is a string specifying the namespace of the attribute.
* </li><li> <code>localName</code> is the name of the attribute.
* </li></ul>
* <h2> <span> Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* // check that the attribute exists
* // before you set a value
* var d = document.getElementById("div1");
* if (d.hasAttributeNS(
* "http://www.mozilla.org/ns/specialspace/",
* "special-align")) {
* d.setAttribute("align", "center");
* }
* </pre>
* <h2> <span> Notes </span></h2>
* <p>DOM methods dealing with element's attributes:
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <th colspan="1" rowspan="1">Not namespace-aware, most commonly used methods</th>
* <th colspan="1" rowspan="1">Namespace-aware variants (DOM Level 2)</th>
* <th colspan="1" rowspan="1">DOM Level 1 methods for dealing with <code>Attr</code> nodes directly (seldom used)</th>
* <th colspan="1" rowspan="1">DOM Level 2 namespace-aware methods for dealing with <code>Attr</code> nodes directly (seldom used)</th>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><a href="DOM:element.setAttribute" shape="rect" title="DOM:element.setAttribute">setAttribute</a> (DOM 1)</td>
* <td colspan="1" rowspan="1"><a href="element.setAttributeNS" shape="rect" title="DOM:element.setAttributeNS">setAttributeNS</a></td>
* <td colspan="1" rowspan="1"><a href="element.setAttributeNode" shape="rect" title="DOM:element.setAttributeNode">setAttributeNode</a></td>
* <td colspan="1" rowspan="1"><a href="element.setAttributeNodeNS" shape="rect" title="DOM:element.setAttributeNodeNS">setAttributeNodeNS</a></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><a href="element.getAttribute" shape="rect" title="DOM:element.getAttribute">getAttribute</a> (DOM 1)</td>
* <td colspan="1" rowspan="1"><a href="element.getAttributeNS" shape="rect" title="DOM:element.getAttributeNS">getAttributeNS</a></td>
* <td colspan="1" rowspan="1"><a href="element.getAttributeNode" shape="rect" title="DOM:element.getAttributeNode">getAttributeNode</a></td>
* <td colspan="1" rowspan="1"><a href="element.getAttributeNodeNS" shape="rect" title="DOM:element.getAttributeNodeNS">getAttributeNodeNS</a></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><a href="element.hasAttribute" shape="rect" title="DOM:element.hasAttribute">hasAttribute</a> (DOM 2)</td>
* <td colspan="1" rowspan="1"><strong>hasAttributeNS</strong></td>
* <td colspan="1" rowspan="1">-</td>
* <td colspan="1" rowspan="1">-</td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><a href="element.removeAttribute" shape="rect" title="DOM:element.removeAttribute">removeAttribute</a> (DOM 1)</td>
* <td colspan="1" rowspan="1"><a href="element.removeAttributeNS" shape="rect" title="DOM:element.removeAttributeNS">removeAttributeNS</a></td>
* <td colspan="1" rowspan="1"><a href="element.removeAttributeNode" shape="rect" title="DOM:element.removeAttributeNode">removeAttributeNode</a></td>
* <td colspan="1" rowspan="1">-</td>
* </tr>
* </table>
* <h2> <span> Specification </span></h2>
* <p><a href="http://www.w3.org/TR/DOM-Level-2-Core/core.html#ID-ElHasAttrNS" rel="nofollow" shape="rect" title="http://www.w3.org/TR/DOM-Level-2-Core/core.html#ID-ElHasAttrNS">DOM Level 2 Core: hasAttributeNS</a>
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
hasAttributeNS: function(namespace, localName) {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span>Summary</span></h2>
* <p><b>hasAttributes</b> returns a boolean value of <code>true</code> or <code>false</code>, indicating if the current element has any attributes or not.
* </p>
* <h2> <span>Syntax </span></h2>
* <p><i>result</i> = element.hasAttributes()
* </p>
* <h2> <span>Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* t1 = document.getElementById("table-data");
* if (t1.hasAttributes()) {
* // do something with
* // t1.attributes
* }
* </pre>
* <h2> <span>Specification </span></h2>
* <p><a href="http://www.w3.org/TR/2000/REC-DOM-Level-2-Core-20001113/core.html#ID-NodeHasAttrs" rel="nofollow" shape="rect" title="http://www.w3.org/TR/2000/REC-DOM-Level-2-Core-20001113/core.html#ID-NodeHasAttrs">hasAttributes </a>
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
hasAttributes: function() { // COMPAT=FF1|FF2|FF3|OPERA|SAFARI2|SAFARI3|KONQ
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span>Summary</span></h2>
* <p><b>hasChildNodes</b> returns a <a href="http://developer.mozilla.org/en/docs/Global_Objects:Boolean" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Boolean">Boolean</a> value indicating whether the current <a href="element" shape="rect" title="DOM:element">element</a> has <a href="element.childNodes" shape="rect" title="DOM:element.childNodes">child nodes</a> or not.
* </p>
* <h2> <span>Syntax </span></h2>
* <p><code>result</code> = element.hasChildNodes()
* </p>
* <h2> <span>Example </span></h2>
* <p>remove the first child node inside the element with the id "foo" if foo has child nodes
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* var foo = document.getElementById("foo")
* if (foo.hasChildNodes()) {
* foo.removeChild(foo.childNodes[0]);
* }
* </pre>
* <div>
* <p>Note that <code>element.hasChildNodes</code>, without the parenthesises, will return the hasChildNodes <a href="http://developer.mozilla.org/en/docs/Global_Objects:Function" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Function">Function</a>, and not a Boolean.
* </p>
* </div>
* <h2> <span>Specification </span></h2>
* <p><a href="http://www.w3.org/TR/2000/REC-DOM-Level-2-Core-20001113/core.html#ID-810594187" rel="nofollow" shape="rect" title="http://www.w3.org/TR/2000/REC-DOM-Level-2-Core-20001113/core.html#ID-810594187">hasChildNodes </a>
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
hasChildNodes: function() {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span> Summary </span></h2>
* <p>Gets or sets the element's identifier.
* </p>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve"><i>idStr</i> = <i>element</i>.id
* <i>element</i>.id = <i>idStr</i>
* </pre>
* <p>where <code>idStr</code> is the ID of the element.
* </p>
* <h2> <span> Notes </span></h2>
* <p>The ID must be unique in a document, and is often used to retrieve the element using <a href="document.getElementById" shape="rect" title="DOM:document.getElementById">document.getElementById</a>.
* </p><p>In some documents (in particular, HTML, XUL, and SVG), the <code>id</code> of an element can be specified as an attribute on the element like so: <code>&lt;div id="table-cell2"&gt;</code>.
* </p><p>However you can't use this attribute in a custom XML document without correctly specifying the type of the <code>id</code> attribute in the DOCTYPE. See <a href="http://blog.bitflux.ch/wiki/GetElementById_Pitfalls" rel="nofollow" shape="rect" title="http://blog.bitflux.ch/wiki/GetElementById_Pitfalls">getElementById Pitfalls</a> for details.
* </p><p>Other common usages of <code>id</code> include using the element's ID as a selector when styling the document with <a href="http://developer.mozilla.org/en/docs/CSS" shape="rect" title="CSS">CSS</a>.
* </p>
* <h2> <span> Specification </span></h2>
* <p><a href="http://www.w3.org/TR/DOM-Level-2-HTML/html.html#ID-63534901" rel="nofollow" shape="rect" title="http://www.w3.org/TR/DOM-Level-2-HTML/html.html#ID-63534901">DOM Level 2 HTML: id</a>
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
id: undefined,
/**
* <h2> <span>Summary</span></h2>
* <p>innerHTML sets or gets all of the markup and content within a given element.
* </p>
* <h2> <span>Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var <i>markup</i> = element.innerHTML;
* element.innerHTML = <i>markup</i>;
* </pre>
* <ul><li> <code>markup</code> is a string that contains the element's content (including child elements) as raw HTML. For example, <code>"&lt;p&gt;Some text&lt;/p&gt;"</code>.
* </li></ul>
* <h2> <span>Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* // HTML:
* // &lt;div id="d"&gt;&lt;p&gt;Content&lt;/p&gt;
* // &lt;p&gt;Further Elaborated&lt;/p&gt;
* // &lt;/div&gt;
* 
* d = document.getElementById("d");
* dump(d.innerHTML);
* 
* // the string "&lt;p&gt;Content&lt;/p&gt;&lt;p&gt;Further Elaborated&lt;/p&gt;"
* // is dumped to the console window
* </pre>
* <h2> <span>Notes </span></h2>
* <p>Though not actually a part of the W3C DOM specification, this property provides a simple way to completely replace the contents of an element.  For example, the entire contents of the document body can be deleted by:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">document.body.innerHTML = "";  // Replaces body content with an empty string.
* </pre>
* <p>The innerHTML property of many types of elements—including BODY or HTML—can be returned or replaced.  It can be used to view the source of a page that has been modified dynamically:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* // Copy and paste into address bar as a single line
* javascript:x=document.body.innerHTML.replace(/&lt;/g,'&amp;lt;').replace(/\n/g,'&lt;br&gt;'); document.body.innerHTML = x;
* 
* </pre>
* <p>As there is no public specification for this property, implementations differ widely.  For example, when text is entered into a text input, IE will change the value attribute of the input's innerHTML property but Gecko browsers do not.
* </p><p>It should never be used to write parts of a table—W3C DOM methods should be used for that—though it can be used to write an entire table or the contents of a cell.
* </p>
* <h2> <span>Specification</span></h2>
* <p>DOM Level 0. Not part of any standard.
* </p>
* <h2> <span>References</span></h2>
* <p><a href="http://msdn.microsoft.com/workshop/author/dhtml/reference/properties/innerhtml.asp" rel="nofollow" shape="rect" title="http://msdn.microsoft.com/workshop/author/dhtml/reference/properties/innerhtml.asp">MSDN innerHTML</a>
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Categories</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span> | <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:DOM_0" shape="rect" title="Category:DOM 0">DOM 0</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
innerHTML: undefined,
/**
* <h2> <span> Summary</span></h2>
* <p>Inserts the specified node before a reference element as a child of the current node.
* </p>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var <i>insertedElement</i> = <i>parentElement</i>.insertBefore(<i>newElement</i>, <i>referenceElement</i>)
* </pre>
* <p>If <var>referenceElement</var> is <code>null</code>, <var>newElement</var> is inserted at the end of the list of child nodes.
* </p>
* <ul><li><code>insertedElement</code> The node being inserted, that is <code>newElement</code>
* </li><li><code>parentElement</code> The parent of the newly inserted node.
* </li><li><code>newElement</code> The node to insert.
* </li><li><code>referenceElement</code> The node before which <code>newElement</code> is inserted.
* </li></ul>
* <h2> <span> Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* &lt;html&gt;
* 
* &lt;head&gt;
* &lt;title&gt;Gecko DOM insertBefore test&lt;/title&gt;
* &lt;/head&gt;
* 
* &lt;body&gt;
* &lt;div&gt;
* &lt;span id="childSpan"&gt;foo bar&lt;/span&gt;
* &lt;/div&gt;
* 
* &lt;script type="text/javascript"&gt;
* // create an empty element node
* // without an ID, any attributes, or any content
* var sp1 = document.createElement("span");
* 
* // give it an id attribute called 'newSpan'
* sp1.setAttribute("id", "newSpan");
* 
* // create some content for the newly created element.
* var sp1_content = document.createTextNode("This is a new span element. ");
* 
* // apply that content to the new element
* sp1.appendChild(sp1_content);
* 
* var sp2 = document.getElementById("childSpan");
* var parentDiv = sp2.parentNode;
* 
* // insert the new element into the DOM before sp2
* parentDiv.insertBefore(sp1, sp2);
* &lt;/script&gt;
* 
* &lt;/body&gt;
* &lt;/html&gt;
* </pre>
* <p>There is no <code>insertAfter</code> method, however it can be emulated using a combination of <code>insertBefore</code> and <code><a href="element.nextSibling" shape="rect" title="DOM:element.nextSibling">nextSibling</a></code>.
* </p><p>From the above example, <code>sp1</code> could be inserted after <code>sp2</code> using:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">parentDiv.insertBefore(sp1, sp2.nextSibling);
* </pre>
* <p>If <code>sp2</code> does not have a next sibling it must be the last child—<code>sp2.nextSibling</code> will return <code>null</code> so <code>sp1</code> will be inserted at the end of the child nodes list (i.e. immediately after <code>sp2</code>).
* </p>
* <h2> <span> Specification </span></h2>
* <p><a href="http://www.w3.org/TR/2000/REC-DOM-Level-2-Core-20001113/core.html#ID-952280727" rel="nofollow" shape="rect" title="http://www.w3.org/TR/2000/REC-DOM-Level-2-Core-20001113/core.html#ID-952280727">insertBefore </a>
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
insertBefore: function(newElement, referenceElement) {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span> Summary </span></h2>
* <p>Returns a node from a <a href="http://www.w3.org/TR/DOM-Level-2-Core/core.html#ID-536297177" rel="nofollow" shape="rect" title="http://www.w3.org/TR/DOM-Level-2-Core/core.html#ID-536297177"><code>NodeList</code></a> by index.
* </p>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve"><i>nodeItem</i> = <i>nodeList</i>.item(<i>index</i>)
* </pre>
* <ul><li> <code>nodeList</code> is a <code>NodeList</code>. This is usually obtained from another DOM property or method, such as <a href="DOM:element.childNodes" shape="rect" title="DOM:element.childNodes">childNodes</a>.
* </li><li> <code>index</code> is the index of the node to be fetched. The index is zero-based.
* </li><li> <code>nodeItem</code> is the <code>index</code>th node in the <code>nodeList</code> returned by the <code>item</code> method.
* </li></ul>
* <p>JavaScript has a special simpler syntax for obtaining an item from a NodeList by index:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve"><i>nodeItem</i> = <i>nodeList</i>[<i>index</i>]
* </pre>
* <h2> <span> Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var tables = document.getElementsByTagName("table");
* var firstTable = tables.item(1); // or simply tables[1] - returns the <b>second</b> table in the DOM
* </pre>
* <h2> <span> Notes </span></h2>
* <p>This method doesn't throw exceptions, a value of <code>null</code> is returned if the index is out of range.
* </p><p>Note that despite this article's name, <code>item()</code> is not a method of DOM <a href="element" shape="rect" title="DOM:element">Element</a> or Node.
* </p>
* <h2> <span> Specification </span></h2>
* <p><a href="http://www.w3.org/TR/REC-DOM-Level-1/level-one-core.html#method-item" rel="nofollow" shape="rect" title="http://www.w3.org/TR/REC-DOM-Level-1/level-one-core.html#method-item">DOM Level 1 Core: NodeList.item()</a>
* </p><p><a href="http://www.w3.org/TR/DOM-Level-2-Core/core.html#ID-844377136" rel="nofollow" shape="rect" title="http://www.w3.org/TR/DOM-Level-2-Core/core.html#ID-844377136">DOM Level 2 Core: NodeList.item()</a>
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
item: function(index) {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span>Summary</span></h2>
* <p>This property gets or sets the base language of an element's attribute values and text content.
* </p>
* <h2> <span>Syntax and values</span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var <var>languageUsed</var> = elementNodeReference.lang;
* elementNodeReference.lang = <var>NewLanguage</var>;
* </pre>
* <p><var>languageUsed</var> is a string variable that gets the language in which the text of the current element is written.
* <var>NewLanguage</var> is a string variable with its value setting the language in which the text of the current element is written.
* </p>
* <h2> <span>Example</span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* // this snippet compares the base language and
* // redirects to another url based on language
* if (document.documentElement.lang == "en")
* {
* window.location.href = "Some_document.html.en";
* }
* else if(document.documentElement.lang == "ru")
* {
* window.location.href = "Some_document.html.ru";
* };
* </pre>
* <h2> <span>Notes </span></h2>
* <p>The language code returned by this property is defined in <a href="http://www.ietf.org/rfc/rfc1766.txt" shape="rect" title="http://www.ietf.org/rfc/rfc1766.txt">RFC 1766</a>. Common examples include "en" for English, "ja" for Japanese, "es" for Spanish and so on. The default value of this attribute is <code>unknown</code>. Note that this attribute, though valid at the individual element level described here, is most often specified for the root element of the document.
* </p>
* <h2> <span>Specification </span></h2>
* <p><a href="http://www.w3.org/TR/DOM-Level-2-HTML/html.html#ID-59132807" rel="nofollow" shape="rect" title="http://www.w3.org/TR/DOM-Level-2-HTML/html.html#ID-59132807">W3C DOM Level 2 HTML: lang</a>
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
lang: undefined,
/**
* <h2> <span>Summary</span></h2>
* <p><b>lastChild</b> returns the last child of a node.
* </p>
* <h2> <span>Syntax and Values </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve"><i>last_child</i> = element.lastChild
* </pre>
* <p>The <code>last_child</code> returned is a node.  If its parent is an element, then the child is generally an Element node, a Text node, or a Comment node.
* </p>
* <h2> <span>Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* var tr = document.getElementById("row1");
* corner_td = tr.lastChild;
* </pre>
* <h2> <span>Notes </span></h2>
* <p>Returns <code>null</code> if there are no child elements.
* </p>
* <h2> <span>Specification </span></h2>
* <p><a href="http://www.w3.org/TR/2000/REC-DOM-Level-2-Core-20001113/core.html#ID-61AD09FB" rel="nofollow" shape="rect" title="http://www.w3.org/TR/2000/REC-DOM-Level-2-Core-20001113/core.html#ID-61AD09FB">lastChild </a>
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
lastChild: undefined,
/**
* <h2> <span> Summary </span></h2>
* <p><code>length</code> returns the number of items in a <code>NodeList</code>.
* </p>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve"><i>numItems</i> = <i>nodeList</i>.length
* </pre>
* <ul><li> <code>numItems</code> is an integer value representing the number of items in a <code>NodeList</code>.
* </li></ul>
* <h2> <span> Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* // all the paragraphs in the document
* var items = document.getElementsByTagName("p");
* // for each item in the list,
* // append the entire element as a string of HTML
* var gross = "";
* for (var i = 0; i &lt; items.length; i++) {
* gross += items[i].innerHTML;
* }
* // gross is now all the HTML for the paragraphs
* </pre>
* <h2> <span> Notes </span></h2>
* <p>Despite the location of this page in the reference, <code>length</code> is not a property of <a href="DOM:element" shape="rect" title="DOM:element">Element</a>, but rather of a <code>NodeList</code>. NodeList objects are returned from a number of DOM methods, such as <a href="document.getElementsByTagName" shape="rect" title="DOM:document.getElementsByTagName">document.getElementsByTagName</a>.
* </p><p><code>length</code> is a very common property in DOM programming. It's very common to test the length of a list (to see if it exists at all) and to use it as the iterator in a for loop, as in the example above.
* </p>
* <h2> <span> Specification </span></h2>
* <p><a href="http://www.w3.org/TR/2000/REC-DOM-Level-2-Core-20001113/core.html#ID-203510337" rel="nofollow" shape="rect" title="http://www.w3.org/TR/2000/REC-DOM-Level-2-Core-20001113/core.html#ID-203510337">length</a>
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
length: undefined,
/**
* <h2> <span> Summary </span></h2>
* <p>Returns the local part of the qualified name of this node.
* </p>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve"><i>name</i> = <i>element</i>.localName
* </pre>
* <ul><li> <code>name</code> is the local name as a string (see <a href="DOM:element.localName#Notes" shape="rect" title="">Notes</a> below for details)
* </li></ul>
* <h2> <span> Example </span></h2>
* <p>(Must be served with XML content type, such as <tt>text/xml</tt> or <tt>application/xhtml+xml</tt>.)
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">&lt;html xmlns="http://www.w3.org/1999/xhtml"
* xmlns:svg="http://www.w3.org/2000/svg"&gt;
* &lt;head&gt;
* &lt;script type="application/javascript"&gt;&lt;![CDATA[
* function test() {
* var text = document.getElementById('text');
* var circle = document.getElementById('circle');
* 
* text.value = "&lt;svg:circle&gt; has:\n" +
* "localName = '" + circle.localName + "'\n" +
* "namespaceURI = '" + circle.namespaceURI + "'";
* }
* ]]&gt;&lt;/script&gt;
* &lt;/head&gt;
* &lt;body onload="test()"&gt;
* &lt;svg:svg version="1.1"
* width="100px" height="100px"
* viewBox="0 0 100 100"&gt;
* &lt;svg:circle cx="50" cy="50" r="30" style="fill:#aaa" id="circle"/&gt;
* &lt;/svg:svg&gt;
* &lt;textarea id="text" rows="4" cols="55"/&gt;
* &lt;/body&gt;
* &lt;/html&gt;
* </pre>
* <h2> <span> Notes </span></h2>
* <p>The local name of a node is that part of the node's qualified name that comes after the colon. Qualified names are typically used in XML as part of the namespace(s) of the particular XML documents. For example, in the qualified name <code>ecomm:partners</code>, <code>partners</code> is the local name and <code>ecomm</code> is the prefix:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* &lt;ecomm:business id="soda_shop" type="brick_n_mortar"&gt;
* &lt;ecomm:partners&gt;
* &lt;ecomm:partner id="1001"&gt;Tony's Syrup Warehouse
* &lt;/ecomm:partner&gt;
* &lt;/ecomm:partner&gt;
* &lt;/ecomm:business&gt;
* </pre>
* <p>For nodes of any <a href="element.nodeType" shape="rect" title="DOM:element.nodeType">type</a> other than <code>ELEMENT_NODE</code> and <code>ATTRIBUTE_NODE</code> and nodes created with a DOM Level 1 method, such as <code><a href="document.createElement" shape="rect" title="DOM:document.createElement">document.createElement</a></code>, <code>localName</code> is always <code>null</code>.
* </p>
* <h2> <span> See Also</span></h2>
* <p><a href="DOM:element.namespaceURI" shape="rect" title="DOM:element.namespaceURI">element.namespaceURI</a>
* </p>
* <h2> <span> Specification </span></h2>
* <p><a href="http://www.w3.org/TR/DOM-Level-2-Core/core.html#ID-NodeNSLocalN" rel="nofollow" shape="rect" title="http://www.w3.org/TR/DOM-Level-2-Core/core.html#ID-NodeNSLocalN">DOM Level 2 Core: Node.localName</a>
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
localName: undefined,
/**
* <h2> <span>Summary</span></h2>
* <p><b>name</b> gets or sets the <code>name</code> attribute of an DOM object, it only applies to the following elements: anchor, applet, form, frame, iframe, image, input, map, meta, object, option, param, select and textarea.
* </p><p>Name can be used in the <a href="document.getElementsByName" shape="rect" title="DOM:document.getElementsByName">getElementsByName</a> method, a <a href="form" shape="rect" title="DOM:form">form</a> and with the form <a href="form.elements" shape="rect" title="DOM:form.elements">elements</a> collection.  When used with a form or elements collection, it may return a single element or a collection.
* </p>
* <h2> <span>Syntax</span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve"><i>HTMLElement</i>.name = <i>string</i>;
* var elName = <i>HTMLElement</i>.name;
* 
* var fControl = <i>HTMLFormElement</i>.<i>elementName</i>;
* var controlCollection = <i>HTMLFormElement</i>.elements.<i>elementName</i>;
* </pre>
* <h2> <span>Example</span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">&lt;form action="" name="formA"&gt;
* &lt;input type="text" value="foo"&gt;
* &lt;/form&gt;
* 
* &lt;script type="text/javascript"&gt;
* 
* // Get a reference to the first element in the form
* var formElement = document.forms['formA'].elements[0];
* 
* // Give it a name
* formElement.name = 'inputA';
* 
* // Show the value of the input
* alert(document.forms['formA'].elements['inputA'].value);
* 
* &lt;/script&gt;
* </pre>
* <h2> <span>Notes</span></h2>
* <p>In Internet Explorer (IE), the name property of DOM objects created using <code><a href="DOM:document.createElement" shape="rect" title="DOM:document.createElement">createElement</a></code> can't be set or modified.
* </p>
* <h2> <span>Specification</span></h2>
* <p>W3C DOM 2 HTML Specification:
* </p>
* <ul><li><a href="http://www.w3.org/TR/DOM-Level-2-HTML/html.html#ID-32783304" rel="nofollow" shape="rect" title="http://www.w3.org/TR/DOM-Level-2-HTML/html.html#ID-32783304">Anchor</a>
* </li><li><a href="http://www.w3.org/TR/DOM-Level-2-HTML/html.html#ID-39843695" rel="nofollow" shape="rect" title="http://www.w3.org/TR/DOM-Level-2-HTML/html.html#ID-39843695">Applet</a>
* </li><li><a href="http://www.w3.org/TR/DOM-Level-2-HTML/html.html#ID-22051454" rel="nofollow" shape="rect" title="http://www.w3.org/TR/DOM-Level-2-HTML/html.html#ID-22051454">Form</a>
* </li><li><a href="http://www.w3.org/TR/DOM-Level-2-HTML/html.html#ID-91128709" rel="nofollow" shape="rect" title="http://www.w3.org/TR/DOM-Level-2-HTML/html.html#ID-91128709">Frame</a>
* </li><li><a href="http://www.w3.org/TR/DOM-Level-2-HTML/html.html#ID-96819659" rel="nofollow" shape="rect" title="http://www.w3.org/TR/DOM-Level-2-HTML/html.html#ID-96819659">iFrame</a>
* </li><li><a href="http://www.w3.org/TR/DOM-Level-2-HTML/html.html#ID-47534097" rel="nofollow" shape="rect" title="http://www.w3.org/TR/DOM-Level-2-HTML/html.html#ID-47534097">Image</a>
* </li><li><a href="http://www.w3.org/TR/DOM-Level-2-HTML/html.html#ID-89658498" rel="nofollow" shape="rect" title="http://www.w3.org/TR/DOM-Level-2-HTML/html.html#ID-89658498">Input</a>
* </li><li><a href="http://www.w3.org/TR/DOM-Level-2-HTML/html.html#ID-52696514" rel="nofollow" shape="rect" title="http://www.w3.org/TR/DOM-Level-2-HTML/html.html#ID-52696514">Map</a>
* </li><li><a href="http://www.w3.org/TR/DOM-Level-2-HTML/html.html#ID-31037081" rel="nofollow" shape="rect" title="http://www.w3.org/TR/DOM-Level-2-HTML/html.html#ID-31037081">Meta</a>
* </li><li><a href="http://www.w3.org/TR/DOM-Level-2-HTML/html.html#ID-20110362" rel="nofollow" shape="rect" title="http://www.w3.org/TR/DOM-Level-2-HTML/html.html#ID-20110362">Object</a>
* </li><li><a href="http://www.w3.org/TR/DOM-Level-2-HTML/html.html#ID-89658498" rel="nofollow" shape="rect" title="http://www.w3.org/TR/DOM-Level-2-HTML/html.html#ID-89658498">Option</a>
* </li><li><a href="http://www.w3.org/TR/DOM-Level-2-HTML/html.html#ID-59871447" rel="nofollow" shape="rect" title="http://www.w3.org/TR/DOM-Level-2-HTML/html.html#ID-59871447">Param</a>
* </li><li><a href="http://www.w3.org/TR/DOM-Level-2-HTML/html.html#ID-41636323" rel="nofollow" shape="rect" title="http://www.w3.org/TR/DOM-Level-2-HTML/html.html#ID-41636323">Select</a>
* </li><li><a href="http://www.w3.org/TR/DOM-Level-2-HTML/html.html#ID-70715578" rel="nofollow" shape="rect" title="http://www.w3.org/TR/DOM-Level-2-HTML/html.html#ID-70715578">Textarea</a>
* </li></ul>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
name: undefined,
/**
* <h2> <span> Summary </span></h2>
* <p>The namespace URI of the node, or <code>null</code> if it is unspecified (read-only).
* </p>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve"><i>namespace</i> = <i>node</i>.namespaceURI
* </pre>
* <ul><li> <code>namespace</code> is a string that represents the namespace URI of the specified node.
* </li></ul>
* <h2> <span> Example </span></h2>
* <p>In this snippet, a node is being examined for its <a href="element.localName" shape="rect" title="DOM:element.localName">localName</a> and its <code>namespaceURI</code>. If the <code>namespaceURI</code> returns the XUL namespace and the <code>localName</code> returns "browser", then the node is understood to be a XUL <code>&lt;browser/&gt;</code>.
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* if (node.localName == "browser" &amp;&amp;
* node.namespaceURI == "http://www.mozilla.org/keymaster/gatekeeper/there.is.only.xul") {
* // this is a XUL browser
* }
* </pre>
* <h2> <span> Notes </span></h2>
* <p>This is not a computed value that is the result of a namespace lookup based on an examination of the namespace declarations in scope. It is merely the namespace URI given at creation time.
* </p><p>For nodes of any <a href="DOM:element.nodeType" shape="rect" title="DOM:element.nodeType">nodeType</a> other than <code>ELEMENT_NODE</code> and <code>ATTRIBUTE_NODE</code>, and nodes created with a DOM Level 1 method, such as <a href="document.createElement" shape="rect" title="DOM:document.createElement">document.createElement</a>, the value of <code>namespaceURI</code> is always <code>null</code>.
* </p><p>You can create an element with the specified <code>namespaceURI</code> using the DOM Level 2 method <a href="document.createElementNS" shape="rect" title="DOM:document.createElementNS">document.createElementNS</a>.
* </p><p>Per the <a href="http://www.w3.org/TR/xml-names11/" rel="nofollow" shape="rect" title="http://www.w3.org/TR/xml-names11/">Namespaces in XML</a> specification, an attribute does not inherit its namespace from the element it is attached to. If an attribute is not explicitly given a namespace, it has no namespace.
* </p>
* <h2> <span> Specification </span></h2>
* <p><a href="http://www.w3.org/TR/DOM-Level-2-Core/core.html#ID-NodeNSname" rel="nofollow" shape="rect" title="http://www.w3.org/TR/DOM-Level-2-Core/core.html#ID-NodeNSname">DOM Level 2 Core: namespaceURI</a>
* </p><p><a href="http://www.w3.org/TR/DOM-Level-2-Core/core.html#Namespaces-Considerations" rel="nofollow" shape="rect" title="http://www.w3.org/TR/DOM-Level-2-Core/core.html#Namespaces-Considerations">DOM Level 2 Core: XML Namespaces</a>
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
namespaceURI: undefined,
/**
* <h2> <span> Summary </span></h2>
* <p>Returns the node immediately following the specified one in its parent's <a href="element.childNodes" shape="rect" title="DOM:element.childNodes">childNodes</a> list, or <code>null</code> if the specified node is the last node in that list.
* </p>
* <h2> <span>Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve"><i>nextNode</i> = <i>node</i>.nextSibling
* </pre>
* <h2> <span> Notes </span></h2>
* <p>Gecko-based browsers insert text nodes into a document to represent whitespace in the source markup.  Therefore a node obtained for example via <a href="element.firstChild" shape="rect" title="DOM:element.firstChild">firstChild</a> or <a href="element.previousSibling" shape="rect" title="DOM:element.previousSibling">previousSibling</a> may refer to a whitespace text node, rather than the actual element the author intended to get.
* </p><p>See <a href="http://developer.mozilla.org/en/docs/Whitespace_in_the_DOM" shape="rect" title="Whitespace in the DOM">Whitespace in the DOM</a> and <a href="http://www.w3.org/DOM/faq.html#emptytext" rel="nofollow" shape="rect" title="http://www.w3.org/DOM/faq.html#emptytext">W3C DOM 3 FAQ: Why are some Text nodes empty?</a> for more information.
* </p>
* <h2> <span> Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* &lt;div id="div-01"&gt;Here is div-01&lt;/div&gt;
* &lt;div id="div-02"&gt;Here is div-02&lt;/div&gt;
* 
* &lt;script type="text/javascript"&gt;
* var el = document.getElementById('div-01').nextSibling;
* document.write('&lt;p&gt;Siblings of div-01&lt;/p&gt;&lt;ol&gt;');
* while (el) {
* document.write('&lt;li&gt;' + el.nodeName + '&lt;/li&gt;');
* el = el.nextSibling;
* }
* document.write('&lt;/ol&gt;');
* &lt;/script&gt;
* 
* / **************************************************
* The following is written to the page as it loads:
* 
* Siblings of div-01
* 
* 1. #text
* 2. DIV
* 3. #text
* 4. SCRIPT
* 5. P
* 6. OL
* ************************************************** /
* </pre>
* <p>In the above example, it can be seen that <code>#text</code> nodes are inserted in the DOM where whitespace occurs in the markup between tags (i.e. after the closing tag of an element and before the opening tag of the next).  No whitespace is created between the elements inserted by the <code>document.write</code> statement.
* </p><p>The possible inclusion of text nodes in the DOM must be allowed for when traversing the DOM using <code>nextSibling</code>. See the resources in the Notes section.
* </p>
* <h2> <span> Specification </span></h2>
* <p><a href="http://www.w3.org/TR/REC-DOM-Level-1/level-one-core.html#attribute-nextSibling" rel="nofollow" shape="rect" title="http://www.w3.org/TR/REC-DOM-Level-1/level-one-core.html#attribute-nextSibling">DOM Level 1 Core: nextSibling</a>
* </p><p><a href="http://www.w3.org/TR/DOM-Level-2-Core/core.html#ID-6AC54C2F" rel="nofollow" shape="rect" title="http://www.w3.org/TR/DOM-Level-2-Core/core.html#ID-6AC54C2F">DOM Level 2 Core: nextSibling</a>
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
nextSibling: undefined,
/**
* <h2> <span>Summary</span></h2>
* <p>Returns the name of the current node as a string.
* </p>
* <h2> <span>Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var <i>str</i> = <i>node</i>.nodeName;
* </pre>
* <p><code>str</code> is a string variable storing the name of the current element.
* </p><p><code>nodeName</code> is a read-only attribute.
* </p>
* <h2> <span>Notes</span></h2>
* <p>Here are the returned values for different types of node.
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" summary="DOM interfaces and their respective nodeName value" width="100%">
* <tr>
* <th colspan="1" rowspan="1" style="text-align: center;">Interface</th>
* <th colspan="1" rowspan="1" style="text-align: center;">nodeName</th>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><a href="http://developer.mozilla.org/en/docs/index.php?title=Attr&amp;action=edit" shape="rect" title="DOM:Attr">Attr</a></td>
* <td colspan="1" rowspan="1">same as <code><a href="http://developer.mozilla.org/en/docs/index.php?title=Attr.name&amp;action=edit" shape="rect" title="DOM:Attr.name">Attr.name</a></code></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><a href="http://developer.mozilla.org/en/docs/index.php?title=CDATASection&amp;action=edit" shape="rect" title="DOM:CDATASection">CDATASection</a></td>
* <td colspan="1" rowspan="1">"#cdata-section"</td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><a href="http://developer.mozilla.org/en/docs/index.php?title=Comment&amp;action=edit" shape="rect" title="DOM:Comment">Comment</a></td>
* <td colspan="1" rowspan="1">"#comment"</td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><a href="document" shape="rect" title="DOM:document">Document</a></td>
* <td colspan="1" rowspan="1">"#document"</td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><a href="http://developer.mozilla.org/en/docs/index.php?title=DocumentFragment&amp;action=edit" shape="rect" title="DOM:DocumentFragment">DocumentFragment</a></td>
* <td colspan="1" rowspan="1">"#document-fragment"</td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><a href="http://developer.mozilla.org/en/docs/index.php?title=DocumentType&amp;action=edit" shape="rect" title="DOM:DocumentType">DocumentType</a></td>
* <td colspan="1" rowspan="1">same as <code><a href="http://developer.mozilla.org/en/docs/index.php?title=DocumentType.name&amp;action=edit" shape="rect" title="DOM:DocumentType.name">DocumentType.name</a></code></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><a href="element" shape="rect" title="DOM:element">Element</a></td>
* <td colspan="1" rowspan="1">same as <code><a href="element.tagName" shape="rect" title="DOM:element.tagName">Element.tagName</a></code></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><a href="http://developer.mozilla.org/en/docs/index.php?title=Entity&amp;action=edit" shape="rect" title="DOM:Entity">Entity</a></td>
* <td colspan="1" rowspan="1">entity name</td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><a href="http://developer.mozilla.org/en/docs/index.php?title=EntityReference&amp;action=edit" shape="rect" title="DOM:EntityReference">EntityReference</a></td>
* <td colspan="1" rowspan="1">name of entity reference</td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><a href="http://developer.mozilla.org/en/docs/index.php?title=Notation&amp;action=edit" shape="rect" title="DOM:Notation">Notation</a></td>
* <td colspan="1" rowspan="1">notation name</td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><a href="http://developer.mozilla.org/en/docs/index.php?title=ProcessingInstruction&amp;action=edit" shape="rect" title="DOM:ProcessingInstruction">ProcessingInstruction</a></td>
* <td colspan="1" rowspan="1">same as <code><a href="http://developer.mozilla.org/en/docs/index.php?title=ProcessingInstruction.target&amp;action=edit" shape="rect" title="DOM:ProcessingInstruction.target">ProcessingInstruction.target</a></code></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">Text</td>
* <td colspan="1" rowspan="1">"#text"</td>
* </tr>
* </table>
* <h2> <span>Example </span></h2>
* <p>Given the following markup:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* &lt;div id="d1"&gt;hello world&lt;/div&gt;
* &lt;input type="text" id="t"/&gt;
* </pre>
* <p>and the following script:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* var div1 = document.getElementById("d1");
* var text_field = document.getElementById("t");
* text_field.value = div1.nodeName;
* </pre>
* <p>In XHTML (or any other XML format), <code>text_field</code>'s value would read "div".
* However, in HTML, <code>text_field</code>'s value would read "DIV".
* </p><p>Note that <code><a href="DOM:element.tagName" shape="rect" title="DOM:element.tagName">tagName</a></code> property could have been used instead, since <code>nodeName</code> has the same value as <code>tagName</code> for an element.  Bear in mind, however, that <code>nodeName</code> will return <code>#text</code> for text nodes while <code>tagName</code> will return <code>undefined</code>.
* </p>
* <h2> <span>Specification </span></h2>
* <p><a href="http://www.w3.org/TR/DOM-Level-2-Core/core.html#ID-F68D095" rel="nofollow" shape="rect" title="http://www.w3.org/TR/DOM-Level-2-Core/core.html#ID-F68D095">DOM Level 2 Core: Node.nodeName</a>
* </p><p><a href="http://www.w3.org/TR/DOM-Level-3-Core/core.html#ID-F68D095" rel="nofollow" shape="rect" title="http://www.w3.org/TR/DOM-Level-3-Core/core.html#ID-F68D095">DOM Level 3 Core: Node.nodeName</a>
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
nodeName: undefined,
/**
* <h2> <span> Summary </span></h2>
* <p>Returns an integer code representing the type of the node.
* </p>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve"><i>type</i> = <i>node</i>.nodeType
* </pre>
* <p><code>type</code> is an unsigned short with one of the following values:
* </p>
* <ul><li> Node.ELEMENT_NODE == 1
* </li><li> Node.ATTRIBUTE_NODE == 2
* </li><li> Node.TEXT_NODE == 3
* </li><li> Node.CDATA_SECTION_NODE == 4
* </li><li> Node.ENTITY_REFERENCE_NODE == 5
* </li><li> Node.ENTITY_NODE == 6
* </li><li> Node.PROCESSING_INSTRUCTION_NODE == 7
* </li><li> Node.COMMENT_NODE == 8
* </li><li> Node.DOCUMENT_NODE == 9
* </li><li> Node.DOCUMENT_TYPE_NODE == 10
* </li><li> Node.DOCUMENT_FRAGMENT_NODE == 11
* </li><li> Node.NOTATION_NODE == 12
* </li></ul>
* <h2> <span> Example </span></h2>
* <p>This example checks if the first node inside the document element is a comment node, and if it is not, displays a message.
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var node = document.documentElement.firstChild;
* if(node.nodeType != Node.COMMENT_NODE)
* alert("You should comment your code well!");
* </pre>
* <h2> <span> Specification </span></h2>
* <p><a href="http://www.w3.org/TR/DOM-Level-2-Core/core.html#ID-111237558" rel="nofollow" shape="rect" title="http://www.w3.org/TR/DOM-Level-2-Core/core.html#ID-111237558">DOM Level 2 Core: Node.nodeType</a>
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
nodeType: undefined,
/**
* <h2> <span> Summary </span></h2>
* <p>Returns the value of the current node.
* </p>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve"><i>value</i> = document.nodeValue
* </pre>
* <p><code>value</code> is a string containing the value of the current node, if any.
* </p>
* <h2> <span> Notes </span></h2>
* <p>For the document itself, <code>nodeValue</code> returns <code>null</code>. For text, comment, and CDATA nodes, <code>nodeValue</code> returns the content of the node. For attribute nodes, the value of the attribute is returned.
* </p><p>The following table shows the return values for different elements:
* </p>
* <table border="1" cellpadding="5" cellspacing="0" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <td colspan="1" rowspan="1">Attr</td>
* <td colspan="1" rowspan="1">value of attribute</td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">CDATASection</td>
* <td colspan="1" rowspan="1">content of the CDATA Section</td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">Comment</td>
* <td colspan="1" rowspan="1">content of the comment</td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">Document</td>
* <td colspan="1" rowspan="1">null</td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">DocumentFragment</td>
* <td colspan="1" rowspan="1">null</td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">DocumentType</td>
* <td colspan="1" rowspan="1">null</td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">Element</td>
* <td colspan="1" rowspan="1">null </td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">NamedNodeMap</td>
* <td colspan="1" rowspan="1">null</td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">EntityReference</td>
* <td colspan="1" rowspan="1">null </td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">Notation</td>
* <td colspan="1" rowspan="1">null </td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">ProcessingInstruction</td>
* <td colspan="1" rowspan="1">entire content excluding the target</td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">Text</td>
* <td colspan="1" rowspan="1">content of the text node</td>
* </tr>
* </table>
* <p>When <code>nodeValue</code> is defined to be <code>null</code>, setting it has no effect.
* </p>
* <h2> <span> Specification </span></h2>
* <ul><li> <a href="http://www.w3.org/TR/DOM-Level-2-Core/core.html#ID-F68D080" rel="nofollow" shape="rect" title="http://www.w3.org/TR/DOM-Level-2-Core/core.html#ID-F68D080">DOM Level 2 Core: Node.nodeValue</a>
* </li></ul>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
nodeValue: undefined,
/**
* <h2> <span> Summary </span></h2>
* <p>Puts the specified node and all of its subtree into a "normalized" form. In a normalized subtree, no text nodes in the subtree are empty and there are no adjacent text nodes.
* </p>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve"><i>element</i>.normalize();
* </pre>
* <h2> <span> Notes </span></h2>
* <h2> <span> Specification </span></h2>
* <p><a href="http://www.w3.org/TR/DOM-Level-2-Core/core.html#ID-normalize" rel="nofollow" shape="rect" title="http://www.w3.org/TR/DOM-Level-2-Core/core.html#ID-normalize">DOM Level 2 Core: Node.normalize</a>
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
normalize: function() {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span>Summary</span></h2>
* <p><abbr title="Dynamic HyperText Markup Language">DHTML</abbr> property that gets the height of an element relative to the layout.
* </p>
* <h2> <span>Syntax and values</span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var <i>intElemOffsetHeight</i> = document.getElementById(<i>id_attribute_value</i>).offsetHeight;
* </pre>
* <p><i>intElemOffsetHeight</i> is a variable storing an integer corresponding to the offsetHeight pixel value of the element.
* offsetHeight is a read-only property.
* </p>
* <h2> <span>Description</span></h2>
* <p>Typically, an element's <b>offsetHeight</b> is a measurement which includes the element borders, the element vertical padding, the element horizontal scrollbar (if present, if rendered) and the element CSS height.
* </p><p>For the document body object, the measurement includes total linear content height instead of the element CSS height.  Floated elements extending below other linear content are ignored.
* </p>
* <h2> <span>Example</span></h2>
* <div id="offsetContainer" style="margin: 26px 0px; background-color: rgb(255, 255, 204); border: 4px dashed black; color: black; position: absolute; left: 260px;"><div id="idDiv" style="margin: 24px 29px; border: 24px black solid; padding: 0px 28px; width: 199px; height: 102px; overflow: auto; background-color: white; font-size: 13px!important; font-family: Arial, sans-serif;"><p id="PaddingTopLabel" style="text-align: center; font-style: italic; font-weight: bold; font-size: 13px!important; font-family: Arial, sans-serif; margin: 0px;">padding-top</p>
* <p>Gentle, individualistic and very loyal, Birman cats fall between Siamese and Persian in character. If you admire cats that are non aggressive, that enjoy being with humans and tend to be on the quiet side, you may well find that Birman cats are just the felines for you.</p>
* <p><span style="float: right;"><a href="http://developer.mozilla.org/en/docs/Image:BirmanCat.jpg" shape="rect" title="Image:BirmanCat.jpg"/></span>All Birmans have colorpointed features, dark coloration of the face, ears, legs and tail.</p>
* <p>Cat image and text coming from <a href="http://www.best-cat-art.com/" rel="nofollow" shape="rect" title="http://www.best-cat-art.com/">www.best-cat-art.com</a></p>
* <p id="PaddingBottomLabel" style="text-align: center; font-style: italic; font-weight: bold; font-size: 13px!important; font-family: Arial, sans-serif; margin: 0px;">padding-bottom</p></div><span style="position: absolute; left: -32px; top: 85px; color: blue; font-weight: bold; font-size: 13px!important; font-family: Arial, sans-serif;">Left</span><span style="position: absolute; left: 170px; top: -24px; color: blue; font-weight: bold; font-size: 13px!important; font-family: Arial, sans-serif;">Top</span><span style="position: absolute; left: 370px; top: 85px; color: blue; font-weight: bold; font-size: 13px!important; font-family: Arial, sans-serif;">Right</span><span style="position: absolute; left: 164px; top: 203px; color: blue; font-weight: bold; font-size: 13px!important; font-family: Arial, sans-serif;">Bottom</span><span style="position: absolute; left: 143px; top: 5px; font-style: italic; font-weight: bold; font-size: 13px!important; font-family: Arial, sans-serif;">margin-top</span><span style="position: absolute; left: 138px; top: 175px; font-style: italic; font-weight: bold; font-size: 13px!important; font-family: Arial, sans-serif;">margin-bottom</span><span style="position: absolute; left: 143px; top: 27px; color: white; font-style: italic; font-weight: bold; font-size: 13px!important; font-family: Arial, sans-serif;">border-top</span><span style="position: absolute; left: 138px; top: 153px; color: white; font-style: italic; font-weight: bold; font-size: 13px!important; font-family: Arial, sans-serif;">border-bottom</span></div>
* <p style="margin-top: 270px;"><a href="http://developer.mozilla.org/en/docs/Image:offsetHeight.png" shape="rect" title="Image:offsetHeight.png"/></p>
* <h2> <span>Specification</span></h2>
* <p>offsetHeight is part of the MSIE's DHTML object model.
* offsetHeight is not part of any W3C specification or technical recommendation.
* </p>
* <h2> <span>Notes</span></h2>
* <p>offsetHeight is a property of the DHTML object model which was first introduced by MSIE. It is sometimes referred to as an element's physical/graphical dimensions, or an element's border-box height.
* </p>
* <h2> <span>References</span></h2>
* <ul><li> <a href="http://msdn.microsoft.com/workshop/author/dhtml/reference/properties/offsetheight.asp?frame=true" rel="nofollow" shape="rect" title="http://msdn.microsoft.com/workshop/author/dhtml/reference/properties/offsetheight.asp?frame=true">MSDN's offsetHeight definition</a>
* </li><li> <a href="http://msdn.microsoft.com/workshop/author/om/measuring.asp" rel="nofollow" shape="rect" title="http://msdn.microsoft.com/workshop/author/om/measuring.asp">MSDN's Measuring Element Dimension and Location</a>
* </li></ul>
* <h2> <span>See Also</span></h2>
* <ul><li> <a href="DOM:element.clientHeight" shape="rect" title="DOM:element.clientHeight">DOM:element.clientHeight</a>
* </li><li> <a href="element.scrollHeight" shape="rect" title="DOM:element.scrollHeight">DOM:element.scrollHeight</a>
* </li></ul>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
offsetHeight: undefined,
/**
* <h2> <span> Summary </span></h2>
* <p>Returns the number of pixels that the <i>upper left corner of the</i> current element is offset to the left within the <code><a href="element.offsetParent" shape="rect" title="DOM:element.offsetParent">offsetParent</a></code> node.
* </p>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve"><i>left</i> = <i>element</i>.offsetLeft;
* </pre>
* <p><code>left</code> is an integer representing the offset to the left in pixels.
* </p>
* <h2> <span> Note </span></h2>
* <p><code>offsetLeft</code> returns the position the upper left edge of the element; not necessarily the 'real' left edge of the element.  This is important for inline elements (such as <b>span</b>) in flowed text that wraps from one line to the next.  The span may start in the middle of the line and wrap around to the beginning of the next line.  The <code>offsetLeft</code> will refer to the left edge of the start of the span, not the left edge of text at the start of the second line.  Therefore, a box with the left, top, width and height of <code>offsetLeft, offsetTop, offsetWidth</code> and <code>offsetHeight</code> will not be a bounding box for a span with wrapped text.  (And, I can't figure out how to find the leftmost edge of such a span, sigh.)
* </p>
* <h2> <span> Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* var colorTable = document.getElementById("t1");
* var tOLeft = colorTable.offsetLeft;
* 
* if (tOLeft &gt; 5) {
* // large left offset: do something here
* }
* </pre>
* <h2> <span> Example </span></h2>
* <p>Per the note above, this example shows a 'long' sentence that wraps within a div with a blue border, and a red box that one might think should describe the boundaries of the span.
* </p><p><a href="http://developer.mozilla.org/en/docs/Image:offsetLeft.jpg" shape="rect" title="Image:offsetLeft.jpg"/>
* </p><p><small><font color="gray">Note: This is an image of the example, not a live rendering in the browser.  This was done because script elements can't be included in the wiki page.</font></small>
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* &lt;div style="width: 300px; border-color:blue;
* border-style:solid; border-width:1;"&gt;
* &lt;span&gt;Short span. &lt;/span&gt;
* &lt;span id="long"&gt;Long span that wraps withing this div.&lt;/span&gt;
* &lt;/div&gt;
* 
* &lt;div id="box" style="position: absolute; border-color: red;
* border-width: 1; border-style: solid; z-index: 10"&gt;
* &lt;/div&gt;
* 
* &lt;script&gt;
* var box = document.getElementById("box");
* var long = document.getElementById("long");
* box.style.left = long.offsetLeft + document.body.scrollLeft;
* box.style.top = long.offsetTop + document.body.scrollTop;
* box.style.width = long.offsetWidth;
* box.style.height = long.offsetHeight;
* &lt;/script&gt;
* </pre>
* <h2> <span> See also </span></h2>
* <p><code><a href="DOM:element.offsetParent" shape="rect" title="DOM:element.offsetParent">offsetParent</a></code>,
* <code><a href="element.offsetTop" shape="rect" title="DOM:element.offsetTop">offsetTop</a></code>,
* <code><a href="element.offsetWidth" shape="rect" title="DOM:element.offsetWidth">offsetWidth</a></code>,
* <code><a href="element.offsetHeight" shape="rect" title="DOM:element.offsetHeight">offsetHeight</a></code>
* </p>
* <h2> <span> Specification </span></h2>
* <p>Non-standard property.
* </p><p><a href="http://msdn.microsoft.com/workshop/author/dhtml/reference/properties/offsetleft.asp" rel="nofollow" shape="rect" title="http://msdn.microsoft.com/workshop/author/dhtml/reference/properties/offsetleft.asp">MSDN: offsetLeft</a>
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
offsetLeft: undefined,
/**
* <h2> <span>Summary</span></h2>
* <p><b>offsetParent</b> returns a reference to the object which is the closest (nearest in the containment hierarchy) positioned containing element. If the element is non-positioned, the root element (html in standards compliant mode; body in quirks rendering mode) is the <b>offsetParent</b>.
* </p>
* <h2> <span>Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve"><i>parentObj</i> = element.offsetParent
* </pre>
* <h2> <span>Parameters </span></h2>
* <ul><li><code>parentObj</code> is an object reference to the element in which the current element is offset.
* </li></ul>
* <h2> <span>Specification </span></h2>
* <p>DOM Level 0. <i>Not part of specification.</i>
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
offsetParent: undefined,
/**
* <h2> <span>Summary</span></h2>
* <p><b>offsetTop</b> returns the distance of the current element relative to the top of the <code><a href="http://developer.mozilla.org/en/docs/offsetParent" shape="rect" title="offsetParent">offsetParent</a></code> node.
* </p>
* <h2> <span>Syntax</span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve"><i>topPos</i> = element.offsetTop
* </pre>
* <h2> <span>Parameters</span></h2>
* <ul><li><code>topPos</code> is the number of pixels from the top of the parent element.
* </li></ul>
* <h2> <span>Example</span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* d = document.getElementById("div1");
* 
* topPos = d.offsetTop;
* 
* if (topPos &gt; 10) {
* 
* // object is offset more
* // than 10 pixels from its parent
* }
* </pre>
* <h2> <span>Specification</span></h2>
* <p>DOM Level 0. <i>Not part of specification.</i>
* </p>
* <h2> <span>References</span></h2>
* <p><a href="http://msdn2.microsoft.com/en-us/library/ms534303.aspx" rel="nofollow" shape="rect" title="http://msdn2.microsoft.com/en-us/library/ms534303.aspx">MSDN's offsetTop definition</a>
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
offsetTop: undefined,
/**
* <h2> <span> Summary </span></h2>
* <p>Returns the layout width of an element.
* </p>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var <i>offsetWidth</i> = <i>element</i>.offsetWidth;
* </pre>
* <p><code>offsetWidth</code> is a read-only property.
* </p>
* <h2> <span> Description </span></h2>
* <p>Typically, an element's <code>offsetWidth</code> is a measurement which includes the element borders, the element horizontal padding, the element vertical scrollbar (if present, if rendered) and the element CSS width.
* </p>
* <h2> <span> Example </span></h2>
* <div id="offsetContainer" style="margin: 26px 0px; background-color: rgb(255, 255, 204); border: 4px dashed black; color: black; position: absolute; left: 260px;"><div id="idDiv" style="margin: 24px 29px; border: 24px black solid; padding: 0px 28px; width: 199px; height: 102px; overflow: auto; background-color: white; font-size: 13px!important; font-family: Arial, sans-serif;"><p id="PaddingTopLabel" style="text-align: center; font-style: italic; font-weight: bold; font-size: 13px!important; font-family: Arial, sans-serif; margin: 0px;">padding-top</p>
* <p>Gentle, individualistic and very loyal, Birman cats fall between Siamese and Persian in character. If you admire cats that are non aggressive, that enjoy being with humans and tend to be on the quiet side, you may well find that Birman cats are just the felines for you.</p>
* <p><span style="float: right;"><a href="http://developer.mozilla.org/en/docs/Image:BirmanCat.jpg" shape="rect" title="Image:BirmanCat.jpg"/></span>All Birmans have colorpointed features, dark coloration of the face, ears, legs and tail.</p>
* <p>Cat image and text coming from <a href="http://www.best-cat-art.com/" rel="nofollow" shape="rect" title="http://www.best-cat-art.com/">www.best-cat-art.com</a></p>
* <p id="PaddingBottomLabel" style="text-align: center; font-style: italic; font-weight: bold; font-size: 13px!important; font-family: Arial, sans-serif; margin: 0px;">padding-bottom</p></div><span style="position: absolute; left: -32px; top: 85px; color: blue; font-weight: bold; font-size: 13px!important; font-family: Arial, sans-serif;">Left</span><span style="position: absolute; left: 170px; top: -24px; color: blue; font-weight: bold; font-size: 13px!important; font-family: Arial, sans-serif;">Top</span><span style="position: absolute; left: 370px; top: 85px; color: blue; font-weight: bold; font-size: 13px!important; font-family: Arial, sans-serif;">Right</span><span style="position: absolute; left: 164px; top: 203px; color: blue; font-weight: bold; font-size: 13px!important; font-family: Arial, sans-serif;">Bottom</span><span style="position: absolute; left: 143px; top: 5px; font-style: italic; font-weight: bold; font-size: 13px!important; font-family: Arial, sans-serif;">margin-top</span><span style="position: absolute; left: 138px; top: 175px; font-style: italic; font-weight: bold; font-size: 13px!important; font-family: Arial, sans-serif;">margin-bottom</span><span style="position: absolute; left: 143px; top: 27px; color: white; font-style: italic; font-weight: bold; font-size: 13px!important; font-family: Arial, sans-serif;">border-top</span><span style="position: absolute; left: 138px; top: 153px; color: white; font-style: italic; font-weight: bold; font-size: 13px!important; font-family: Arial, sans-serif;">border-bottom</span></div>
* <p style="margin-top: 270px;"><a href="http://developer.mozilla.org/en/docs/Image:offsetWidth.png" shape="rect" title="Image:offsetWidth.png"/></p>
* <h2> <span> Specification </span></h2>
* <p><code>offsetWidth</code> is part of the MSIE's <abbr title="Dynamic HyperText Markup Language">DHTML</abbr> object model.
* <code>offsetWidth</code> is not part of any W3C specification or technical recommendation.
* </p>
* <h2> <span>Notes</span></h2>
* <p><code>offsetWidth</code> is a property of the <abbr title="Dynamic HyperText Markup Language">DHTML</abbr> object model which was first introduced by MSIE. It is sometimes referred to as an element's physical/graphical dimensions, or an element's border-box width.
* </p>
* <h2> <span>References</span></h2>
* <ul><li> <a href="http://msdn.microsoft.com/workshop/author/dhtml/reference/properties/offsetwidth.asp?frame=true" rel="nofollow" shape="rect" title="http://msdn.microsoft.com/workshop/author/dhtml/reference/properties/offsetwidth.asp?frame=true">MSDN's offsetWidth definition</a>
* </li><li> <a href="http://msdn.microsoft.com/workshop/author/om/measuring.asp" rel="nofollow" shape="rect" title="http://msdn.microsoft.com/workshop/author/om/measuring.asp">MSDN's Measuring Element Dimension and Location</a>
* </li></ul>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
offsetWidth: undefined,
/**
* <h2> <span>Summary</span></h2>
* <p>The <b>onblur</b> property returns the onBlur event handler code, if any, that exists on the current element.
* </p>
* <h2> <span>Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">element.onblur = function;
* </pre>
* <ul><li> <code>function</code> is the name of a user-defined function, without the () suffix or any parameters, or an anonymous function declaration, such as
* </li></ul>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">element.onblur = function() { alert("onblur event detected!"); };
* </pre>
* <h2> <span>Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* &lt;html&gt;
* 
* &lt;head&gt;
* &lt;title&gt;onblur event example&lt;/title&gt;
* 
* &lt;script type="text/javascript"&gt;
* 
* var elem = null;
* 
* function initElement()
* {
* elem = document.getElementById("foo");
* // NOTE: doEvent(); or doEvent(param); will NOT work here.
* // Must be a reference to a function name, not a function call.
* elem.onblur = doEvent;
* };
* 
* function doEvent()
* {
* elem.value = 'Bye-Bye';
* alert("onblur Event detected!")
* }
* &lt;/script&gt;
* 
* &lt;style type="text/css"&gt;
* &lt;!--
* #foo {
* border: solid blue 2px;
* }
* --&gt;
* &lt;/style&gt;
* &lt;/head&gt;
* 
* &lt;body onload="initElement()";&gt;
* &lt;form&gt;
* &lt;input type="text" id="foo" value="Hello!" /&gt;
* &lt;/form&gt;
* 
* &lt;p&gt;Click on the above element to give it focus, then click outside the
* element.&lt;br /&gt; Reload the page from the NavBar.&lt;/p&gt;
* 
* &lt;/body&gt;
* &lt;/html&gt;
* </pre>
* <h2> <span>Notes </span></h2>
* <p>The blur event is raised when an element loses focus.
* </p><p>Oppsite to MSIE, which almost all kinds of elements receive blur event, almost all kinds of elements on gecko browsers do NOT work with this event.
* </p>
* <h2> <span>Specification </span></h2>
* <p>DOM Level 0. Not part of specification.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
onblur: undefined,
/**
* <h2> <span>Summary</span></h2>
* <p>The <code>onchange</code> property sets and returns the <code>onChange</code> event handler code for the current element.
* </p>
* <h2> <span>Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">element.onchange = event handling code
* </pre>
* <h2> <span>Notes </span></h2>
* <p>The following pseudo code illustrates how the change handler is implemented in Mozilla:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* control.onfocus = focus;
* control.onblur = blur;
* function focus () {
* original_value = control.value;
* }
* 
* function blur () {
* if (control.value != original_value)
* control.onchange();
* }
* </pre>
* <p>As a result, you might experience unexpected behavior from the <code>change</code> event if you alter the value of the control in your own <code>focus</code> or <code>blur</code> event handlers. Also, the <code>change</code> event is fired after the <code>blur</code> event. This behavior differs from IE.
* </p>
* <h2> <span>Specification</span></h2>
* <p>DOM Level 0 (pre-dates specifications)
* </p>
* <h2> <span>See Also</span></h2>
* <p><a href="http://www.w3.org/TR/DOM-Level-2-Events/events.html#Events-eventgroupings-htmlevents" rel="nofollow" shape="rect" title="http://www.w3.org/TR/DOM-Level-2-Events/events.html#Events-eventgroupings-htmlevents">DOM Level 2: HTML event types</a>
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
onchange: undefined,
/**
* <h2> <span>Summary</span></h2>
* <p>The <b>onclick</b> property returns the onClick event handler code on the current element.
* </p>
* <h2> <span>Syntax</span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve"><i>element</i>.onclick = <i>functionRef</i>;
* </pre>
* <p>where <i>functionRef</i> is a function - often a name of a function declared elsewhere or a <i>function expression</i>. See <a href="http://developer.mozilla.org/en/docs/Core_JavaScript_1.5_Reference:Functions" shape="rect" title="Core JavaScript 1.5 Reference:Functions">Core JavaScript 1.5 Reference:Functions</a> for details.
* </p>
* <h2> <span>Example</span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* &lt;html&gt;
* 
* &lt;head&gt;
* &lt;title&gt;onclick event example&lt;/title&gt;
* 
* &lt;script type="text/javascript"&gt;
* 
* function initElement()
* {
* var p = document.getElementById("foo");
* // NOTE: showAlert(); or showAlert(param); will NOT work here.
* // Must be a reference to a function name, not a function call.
* p.onclick = showAlert;
* };
* 
* function showAlert()
* {
* alert("onclick Event detected!")
* }
* &lt;/script&gt;
* 
* &lt;style type="text/css"&gt;
* &lt;!--
* #foo {
* border: solid blue 2px;
* }
* --&gt;
* &lt;/style&gt;
* &lt;/head&gt;
* 
* &lt;body onload="initElement()";&gt;
* &lt;span id="foo"&gt;My Event Element&lt;/span&gt;
* &lt;p&gt;click on the above element.&lt;/p&gt;
* &lt;/body&gt;
* &lt;/html&gt;
* </pre>
* <p>Or you can use an anonymous function, like this:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* p.onclick = function() { alert("moot!"); };
* </pre>
* <h2> <span> Notes </span></h2>
* <p>The click event is raised when the user clicks on an element. The click event will occur after the mousedown and mouseup events.
* </p><p>Only one onclick handler can be assigned to an object at a time with this property. You may be inclined to use the <a href="element.addEventListener" shape="rect" title="DOM:element.addEventListener"> addEventListener</a> method instead, since it is more flexible and part of the DOM Events specification.
* </p>
* <h2> <span>Specification</span></h2>
* <p>Not part of specification.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
onclick: undefined,
/**
* <h2> <span>Summary</span></h2>
* <p>The <b>ondblclick</b> property returns the onDblClick event handler code on the current element.
* </p>
* <h2> <span>Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">element.ondblclick = function;
* </pre>
* <ul><li> <code>function</code> is the name of a user-defined function, without the () suffix or any parameters, or an anonymous function declaration, such as
* </li></ul>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">element.ondblclick = function() { alert("ondblclick event detected!"); };
* </pre>
* <h2> <span>Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* &lt;html&gt;
* 
* &lt;head&gt;
* &lt;title&gt;ondblclick event example&lt;/title&gt;
* 
* &lt;script type="text/javascript"&gt;
* 
* function initElement()
* {
* var p = document.getElementById("foo");
* // NOTE: showAlert(); or showAlert(param); will NOT work here.
* // Must be a reference to a function name, not a function call.
* p.ondblclick = showAlert;
* };
* 
* function showAlert()
* {
* alert("ondblclick Event detected!")
* }
* &lt;/script&gt;
* 
* &lt;style type="text/css"&gt;
* &lt;!--
* #foo {
* border: solid blue 2px;
* }
* --&gt;
* &lt;/style&gt;
* &lt;/head&gt;
* 
* &lt;body onload="initElement()";&gt;
* &lt;span id="foo"&gt;My Event Element&lt;/span&gt;
* &lt;p&gt;double-click on the above element.&lt;/p&gt;
* &lt;/body&gt;
* &lt;/html&gt;
* </pre>
* <h2> <span>Notes </span></h2>
* <p>The dblclick event is raised when the user double clicks an element.
* </p>
* <h2> <span>Specification </span></h2>
* <p>DOM Level 0. Not part of specification.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
ondblclick: undefined,
/**
* <h2> <span>Summary</span></h2>
* <p>The <b>onfocus</b> property returns the onFocus event handler code on the current element.
* </p>
* <h2> <span>Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">event handling code = element.onfocus
* </pre>
* <h2> <span>Notes </span></h2>
* <p>The focus event is raised when the user sets focus on the given element.
* </p><p>Oppsite to MSIE, which almost all kinds of elements receive focus event, almost all kinds of elements on gecko browsers do NOT work with this event.
* </p>
* <h2> <span>Specification </span></h2>
* <p>Not part of specification.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
onfocus: undefined,
/**
* <h2> <span>Summary</span></h2>
* <p>The <b>onkeydown</b> property returns the onKeyDown event handler code on the current element.
* </p>
* <h2> <span>Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">event handling code = element.onkeydown
* </pre>
* <h2> <span>Notes </span></h2>
* <p>The keydown event is raised when the user presses a keyboard key.
* </p>
* <h2> <span>Specification </span></h2>
* <p>Not part of specification.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
onkeydown: undefined,
/**
* <h2> <span>Summary</span></h2>
* <p>The <b>onkeypress</b> property sets and returns the onKeyPress event handler code for the current element.
* </p>
* <h2> <span>Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">element.onkeypress = event handling code
* </pre>
* <h2> <span>Notes </span></h2>
* <p>The keypress event is raised when the user presses a key on the keyboard.
* </p>
* <h2> <span>Specification </span></h2>
* <p>Not part of specification.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
onkeypress: undefined,
/**
* <h2> <span>Summary</span></h2>
* <p>The <b>onkeyup</b> property returns the onKeyUp event handler code for the current element.
* </p>
* <h2> <span>Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">event handling code = element.onkeyup
* </pre>
* <h2> <span>Example</span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve"> &lt;input type="text" onKeyUp="keyWasPressed(event)"&gt;
* &lt;script&gt;function keyWasPressed(evt){ alert(evt.keyCode) }&lt;/script&gt;
* </pre>
* <h2> <span>Notes </span></h2>
* <p>The keyup event is raised when the user releases a key that's been pressed.
* </p>
* <h2> <span>Specification </span></h2>
* <p>Not part of specification.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
onkeyup: undefined,
/**
* <h2> <span>Summary</span></h2>
* <p>The <b>onmousedown</b> property returns the onMouseDown event handler code on the current element.
* </p>
* <h2> <span>Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">event handling code = element.onMouseDown
* </pre>
* <h2> <span>Notes </span></h2>
* <p>The <code>mousedown</code> event is raised when the user presses the left mouse button.
* </p>
* <h2> <span>Specification </span></h2>
* <p>DOM Level 0. Not part of specification.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
onmousedown: undefined,
/**
* <h2> <span> Summary </span></h2>
* <p>The <b>onmousemove</b> property returns the <code>mousemove</code> event handler code on the current element.
* </p>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">event handling code = element.onMouseMove
* </pre>
* <h2> <span> Notes </span></h2>
* <p>The <code>mousemove</code> event is raised when the user moves the mouse.
* </p>
* <h2> <span> Specification </span></h2>
* <p>DOM Level 0. Not part of specification.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
onmousemove: undefined,
/**
* <h2> <span>Summary</span></h2>
* <p>The <b>onmouseout</b> property returns the onMouseOut event handler code on the current element.
* </p>
* <h2> <span>Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">event handling code = element.onMouseOut
* </pre>
* <h2> <span>Notes </span></h2>
* <p>The mouseout event is raised when the mouse leaves an element (e.g, when the mouse moves off of an image in the web page, the mouseout event is raised for that image element).
* </p>
* <h2> <span>Specification </span></h2>
* <p>DOM Level 0. Not part of specification.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
onmouseout: undefined,
/**
* <h2> <span>Summary</span></h2>
* <p>The <b>onmouseover</b> property returns the onMouseOver event handler code on the current element.
* </p>
* <h2> <span>Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">event handling code = element.onmouseover
* </pre>
* <h2> <span>Notes </span></h2>
* <p>The mouseover event is raised when the user moves the mouse over a particular element.
* </p>
* <h2> <span>Specification </span></h2>
* <p>Not part of specification.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
onmouseover: undefined,
/**
* <h2> <span>Summary</span></h2>
* <p>The <b>onmouseup</b> property returns the onMouseUp event handler code on the current element.
* </p>
* <h2> <span>Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">event handling code = element.onMouseUp
* </pre>
* <h2> <span>Notes </span></h2>
* <p>The mouseup event is raised when the user releases the left mouse button.
* </p>
* <h2> <span>Specification </span></h2>
* <p>DOM Level 0. Not part of specification.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
onmouseup: undefined,
/**
* <h2> <span>Summary</span></h2>
* <p><b>onresize</b> returns the element's onresize event handler code.  It can also be used to set the code to be executed when the resize event occurs.
* </p><p>Only the <var>window</var> object has an onresize event.
* </p>
* <h2> <span>Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">// Set onresize to an anonymous function
* window.onresize = function(){alert('Window resized')}
* 
* // Set onresize to a function reference
* function sayHi(){alert('Hi')}
* 
* window.onresize = sayHi;
* 
* // Show the value of window.onresize
* alert(window.onresize);
* </pre>
* <p>When used with frames, an onresize event will occur whenever the frame is resized either directly or as a result of the window being resized.
* </p>
* <h2> <span>Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* &lt;!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN"
* "http://www.w3.org/TR/html4/strict.dtd"&gt;
* &lt;html&gt;
* &lt;head&gt;
* &lt;title&gt;onresize example&lt;/title&gt;
* &lt;script type="text/javascript"&gt;
* 
* function sayHi(){
* alert('Hi');
* }
* 
* window.onresize = sayHi;
* 
* &lt;/script&gt;
* &lt;/head&gt;
* &lt;body&gt;
* &lt;input type="button"
* value="Click to show window.onresize"
* onclick="alert(window.onresize);"
* &gt;
* &lt;/body&gt;
* &lt;/html&gt;</pre>
* <h2> <span>Notes </span></h2>
* <p>Any element can be given an onresize attribute, however only the window object has a resize event.  Resizing other elements (say by modifying the width or height of an img element using script) will not raise a resize event.
* </p>
* <h2> <span>Specification </span></h2>
* <p>DOM Level 0. Not part of specification.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
onresize: undefined,
/**
* <h2> <span>Summary</span></h2>
* <p>The <b>onscroll</b> property returns the onScroll event handler code on the current element.
* </p>
* <h2> <span>Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">event handling code = element.onScroll
* </pre>
* <h2> <span>Notes </span></h2>
* <p>The <code>scroll</code> event is raised when the user scrolls the contents of a element.
* </p>
* <h2> <span>Specification </span></h2>
* <p>DOM Level 0. Not part of specification.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
onscroll: undefined,
/**
* <h2> <span>Summary</span></h2>
* <p>The <b>ownerDocument</b> property returns the top-level document object for this node.
* </p>
* <h2> <span>Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve"><i>document</i> = element.ownerDocument
* </pre>
* <h2> <span>Parameters </span></h2>
* <ul><li> <code>document</code> is the <code>document</code> object parent of the current element.
* </li></ul>
* <h2> <span>Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* // given a node "p", get the top-level HTML child
* // of the document object
* d = p.ownerDocument;
* html = d.documentElement;
* </pre>
* <h2> <span>Notes </span></h2>
* <p>The <code>document</code> object returned by this property is the main object with which all the child nodes in the actual HTML document are created.
* If this property is used on a node that is itself a document, the result is <code>NULL</code>.
* </p>
* <h2> <span>Specification </span></h2>
* <p><a href="http://www.w3.org/TR/2000/REC-DOM-Level-2-Core-20001113/core.html#node-ownerDoc" rel="nofollow" shape="rect" title="http://www.w3.org/TR/2000/REC-DOM-Level-2-Core-20001113/core.html#node-ownerDoc">ownerDocument </a>
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
ownerDocument: undefined,
/**
* <h2> <span> Summary </span></h2>
* <p>Returns the parent of the specified node in the DOM tree.
* </p>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve"><i>parentNode</i> = <i>node</i>.parentNode
* </pre>
* <p><code>parentNode</code> is the parent of the current node.  The parent of an element is an <code>Element</code> node, a <code>Document</code> node, or a <code>DocumentFragment</code> node.
* </p>
* <h2> <span> Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">if (node.parentNode) {
* // remove a node from the tree, unless
* // it's not in the tree already
* node.parentNode.removeChild(node);
* }
* </pre>
* <h2> <span> Notes </span></h2>
* <p><code>parentNode</code> returns <code>null</code> for the following <a href="DOM:element.nodeType" shape="rect" title="DOM:element.nodeType">node types</a>: <code>Attr</code>, <code>Document</code>, <code>DocumentFragment</code>, <code>Entity</code>, and <code>Notation</code>.
* </p><p>It also returns <code>null</code> if the node has just been created and is not yet attached to the tree.
* </p>
* <h2> <span> See also </span></h2>
* <p><code><a href="DOM:element.firstChild" shape="rect" title="DOM:element.firstChild">element.firstChild</a></code>, <code><a href="element.lastChild" shape="rect" title="DOM:element.lastChild">element.lastChild</a></code>, <code><a href="element.childNodes" shape="rect" title="DOM:element.childNodes">element.childNodes</a></code>, <code><a href="element.nextSibling" shape="rect" title="DOM:element.nextSibling">element.nextSibling</a></code>, <code><a href="element.previousSibling" shape="rect" title="DOM:element.previousSibling">element.previousSibling</a></code>.
* </p>
* <h2> <span> Specification </span></h2>
* <p><a href="http://www.w3.org/TR/DOM-Level-2-Core/core.html#ID-1060184317" rel="nofollow" shape="rect" title="http://www.w3.org/TR/DOM-Level-2-Core/core.html#ID-1060184317">DOM Level 2 Core: Node.parentNode</a>
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
parentNode: undefined,
/**
* <h2> <span> Summary </span></h2>
* <p><code>prefix</code> returns the namespace prefix of the specified node, or <code>null</code> if no prefix is specified.
* </p>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve"><i>string</i> = element.prefix
* element.prefix = <i>string</i>
* </pre>
* <h2> <span> Examples </span></h2>
* <p>The following alerts "x".
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">&lt;x:div onclick="alert(this.prefix)"/&gt;
* </pre>
* <p>
* </p>
* <h2> <span> Notes </span></h2>
* <p>This will only work when a namespace-aware parser is used, i.e. when a document is served with an XML mime-type. This will not work for HTML documents.
* </p>
* <h2> <span> Specification </span></h2>
* <p><a href="http://www.w3.org/TR/2000/REC-DOM-Level-2-Core-20001113/core.html#ID-NodeNSPrefix" rel="nofollow" shape="rect" title="http://www.w3.org/TR/2000/REC-DOM-Level-2-Core-20001113/core.html#ID-NodeNSPrefix">Node.prefix</a> (introduced in DOM2)
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
prefix: undefined,
/**
* <h2> <span> Summary </span></h2>
* <p>Returns the node immediately preceding the specified one in its parent's <a href="element.childNodes" shape="rect" title="DOM:element.childNodes">childNodes</a> list, <code>null</code> if the specified node is the first in that list.
* </p>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve"><i>previousNode</i> = <i>node</i>.previousSibling
* </pre>
* <h2> <span> Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">// &lt;a&gt;&lt;b1 id="b1"/&gt;&lt;b2 id="b2"/&gt;&lt;/a&gt;
* alert(document.getElementById("b1").previousSibling); // null
* alert(document.getElementById("b2").previousSibling.id); // "b1"
* </pre>
* <h2> <span> Notes </span></h2>
* <p>Gecko-based browsers insert text nodes into a document to represent whitespace in the source markup.  Therefore a node obtained for example via <a href="DOM:element.firstChild" shape="rect" title="DOM:element.firstChild">firstChild</a> or <strong>previousSibling</strong> may refer to a whitespace text node, rather than the actual element the author intended to get.
* </p><p>See <a href="http://developer.mozilla.org/en/docs/Whitespace_in_the_DOM" shape="rect" title="Whitespace in the DOM">Whitespace in the DOM</a> and <a href="http://www.w3.org/DOM/faq.html#emptytext" rel="nofollow" shape="rect" title="http://www.w3.org/DOM/faq.html#emptytext">W3C DOM 3 FAQ: Why are some Text nodes empty?</a> for more information.
* </p><p>
* To navigate the opposite way through the child nodes list use <a href="element.nextSibling" shape="rect" title="DOM:element.nextSibling">element.nextSibling</a>.
* </p>
* <h2> <span> Specification </span></h2>
* <p><a href="http://www.w3.org/TR/REC-DOM-Level-1/level-one-core.html#attribute-previousSibling" rel="nofollow" shape="rect" title="http://www.w3.org/TR/REC-DOM-Level-1/level-one-core.html#attribute-previousSibling">DOM Level 1 Core: previousSibling</a>
* </p><p><a href="http://www.w3.org/TR/DOM-Level-2-Core/core.html#ID-640FB3C8" rel="nofollow" shape="rect" title="http://www.w3.org/TR/DOM-Level-2-Core/core.html#ID-640FB3C8">DOM Level 2 Core: previousSibling</a>
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
previousSibling: undefined,
/**
* <h2> <span> Summary </span></h2>
* <p><code>removeAttribute</code> removes an attribute from the specified element.
* </p>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve"><i>element</i>.removeAttribute(<i>attrName</i>)
* </pre>
* <ul><li> <code>attrName</code> is a string that names the attribute to be removed from <i>element</i>.
* </li></ul>
* <h2> <span> Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* // &lt;div id="div1" align="left" width="200px"&gt;
* document.getElementById("div1").removeAttribute("align");
* // now: &lt;div id="div1" width="200px"&gt;
* </pre>
* <h2> <span> Notes </span></h2>
* <p>You should use <code>removeAttribute</code> instead of setting the attribute value to <code>null</code> using <a href="DOM:element.setAttribute" shape="rect" title="DOM:element.setAttribute">setAttribute</a>.
* </p><p>Attempting to remove an attribute that is not on the element doesn't raise an exception.
* </p><p>DOM methods dealing with element's attributes:
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <th colspan="1" rowspan="1">Not namespace-aware, most commonly used methods</th>
* <th colspan="1" rowspan="1">Namespace-aware variants (DOM Level 2)</th>
* <th colspan="1" rowspan="1">DOM Level 1 methods for dealing with <code>Attr</code> nodes directly (seldom used)</th>
* <th colspan="1" rowspan="1">DOM Level 2 namespace-aware methods for dealing with <code>Attr</code> nodes directly (seldom used)</th>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><a href="element.setAttribute" shape="rect" title="DOM:element.setAttribute">setAttribute</a> (DOM 1)</td>
* <td colspan="1" rowspan="1"><a href="element.setAttributeNS" shape="rect" title="DOM:element.setAttributeNS">setAttributeNS</a></td>
* <td colspan="1" rowspan="1"><a href="element.setAttributeNode" shape="rect" title="DOM:element.setAttributeNode">setAttributeNode</a></td>
* <td colspan="1" rowspan="1"><a href="element.setAttributeNodeNS" shape="rect" title="DOM:element.setAttributeNodeNS">setAttributeNodeNS</a></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><a href="element.getAttribute" shape="rect" title="DOM:element.getAttribute">getAttribute</a> (DOM 1)</td>
* <td colspan="1" rowspan="1"><a href="element.getAttributeNS" shape="rect" title="DOM:element.getAttributeNS">getAttributeNS</a></td>
* <td colspan="1" rowspan="1"><a href="element.getAttributeNode" shape="rect" title="DOM:element.getAttributeNode">getAttributeNode</a></td>
* <td colspan="1" rowspan="1"><a href="element.getAttributeNodeNS" shape="rect" title="DOM:element.getAttributeNodeNS">getAttributeNodeNS</a></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><a href="element.hasAttribute" shape="rect" title="DOM:element.hasAttribute">hasAttribute</a> (DOM 2)</td>
* <td colspan="1" rowspan="1"><a href="element.hasAttributeNS" shape="rect" title="DOM:element.hasAttributeNS">hasAttributeNS</a></td>
* <td colspan="1" rowspan="1">-</td>
* <td colspan="1" rowspan="1">-</td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><strong>removeAttribute</strong> (DOM 1)</td>
* <td colspan="1" rowspan="1"><a href="element.removeAttributeNS" shape="rect" title="DOM:element.removeAttributeNS">removeAttributeNS</a></td>
* <td colspan="1" rowspan="1"><a href="element.removeAttributeNode" shape="rect" title="DOM:element.removeAttributeNode">removeAttributeNode</a></td>
* <td colspan="1" rowspan="1">-</td>
* </tr>
* </table>
* <h2> <span> Specification </span></h2>
* <p><a href="http://www.w3.org/TR/DOM-Level-2-Core/core.html#ID-6D6AC0F9" rel="nofollow" shape="rect" title="http://www.w3.org/TR/DOM-Level-2-Core/core.html#ID-6D6AC0F9">DOM Level 2 Core: removeAttribute</a> (introduced in <a href="http://www.w3.org/TR/REC-DOM-Level-1/level-one-core.html#method-removeAttribute" rel="nofollow" shape="rect" title="http://www.w3.org/TR/REC-DOM-Level-1/level-one-core.html#method-removeAttribute">DOM Level 1 Core</a>)
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
removeAttribute: function(attrName) {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span> Summary </span></h2>
* <p><code>removeAttributeNS</code> removes the specified attribute from an element.
* </p><p><span style="border: 1px solid #818151; background-color: #FFFFE1; font-size: 9px; vertical-align: text-top;">New in <a href="http://developer.mozilla.org/en/docs/Firefox_3_for_developers" shape="rect" title="Firefox 3 for developers">Firefox 3</a></span> In Firefox 3 and later, this method resets DOM values to their defaults.
* </p>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve"><i>element</i>.removeAttributeNS(<i>namespace</i>, <i>attrName</i>);
* </pre>
* <ul><li> <code>namespace</code> is a string that contains the namespace of the attribute.
* </li><li> <code>attrName</code> is a string that names the attribute to be removed from the current node.
* </li></ul>
* <h2> <span> Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* // &lt;div id="div1" xmlns:special="http://www.mozilla.org/ns/specialspace"
* //      special:specialAlign="utterleft" width="200px" /&gt;
* d = document.getElementById("div1");
* d.removeAttributeNS("http://www.mozilla.org/ns/specialspace", "specialAlign");
* // now: &lt;div id="div1" width="200px" /&gt;
* </pre>
* <h2> <span> Notes </span></h2>
* <p>DOM methods dealing with element's attributes:
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <th colspan="1" rowspan="1">Not namespace-aware, most commonly used methods</th>
* <th colspan="1" rowspan="1">Namespace-aware variants (DOM Level 2)</th>
* <th colspan="1" rowspan="1">DOM Level 1 methods for dealing with <code>Attr</code> nodes directly (seldom used)</th>
* <th colspan="1" rowspan="1">DOM Level 2 namespace-aware methods for dealing with <code>Attr</code> nodes directly (seldom used)</th>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><a href="DOM:element.setAttribute" shape="rect" title="DOM:element.setAttribute">setAttribute</a> (DOM 1)</td>
* <td colspan="1" rowspan="1"><a href="element.setAttributeNS" shape="rect" title="DOM:element.setAttributeNS">setAttributeNS</a></td>
* <td colspan="1" rowspan="1"><a href="element.setAttributeNode" shape="rect" title="DOM:element.setAttributeNode">setAttributeNode</a></td>
* <td colspan="1" rowspan="1"><a href="element.setAttributeNodeNS" shape="rect" title="DOM:element.setAttributeNodeNS">setAttributeNodeNS</a></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><a href="element.getAttribute" shape="rect" title="DOM:element.getAttribute">getAttribute</a> (DOM 1)</td>
* <td colspan="1" rowspan="1"><a href="element.getAttributeNS" shape="rect" title="DOM:element.getAttributeNS">getAttributeNS</a></td>
* <td colspan="1" rowspan="1"><a href="element.getAttributeNode" shape="rect" title="DOM:element.getAttributeNode">getAttributeNode</a></td>
* <td colspan="1" rowspan="1"><a href="element.getAttributeNodeNS" shape="rect" title="DOM:element.getAttributeNodeNS">getAttributeNodeNS</a></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><a href="element.hasAttribute" shape="rect" title="DOM:element.hasAttribute">hasAttribute</a> (DOM 2)</td>
* <td colspan="1" rowspan="1"><a href="element.hasAttributeNS" shape="rect" title="DOM:element.hasAttributeNS">hasAttributeNS</a></td>
* <td colspan="1" rowspan="1">-</td>
* <td colspan="1" rowspan="1">-</td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><a href="element.removeAttribute" shape="rect" title="DOM:element.removeAttribute">removeAttribute</a> (DOM 1)</td>
* <td colspan="1" rowspan="1"><strong>removeAttributeNS</strong></td>
* <td colspan="1" rowspan="1"><a href="element.removeAttributeNode" shape="rect" title="DOM:element.removeAttributeNode">removeAttributeNode</a></td>
* <td colspan="1" rowspan="1">-</td>
* </tr>
* </table>
* <h2> <span> Specification </span></h2>
* <p><a href="http://www.w3.org/TR/DOM-Level-2-Core/core.html#ID-ElRemAtNS" rel="nofollow" shape="rect" title="http://www.w3.org/TR/DOM-Level-2-Core/core.html#ID-ElRemAtNS">DOM Level 2 Core: removeAttributeNS</a>
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
removeAttributeNS: function(namespace, attrName) {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span> Summary </span></h2>
* <p><code>removeAttributeNode</code> removes the specified attribute from the current element.
* </p>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve"><i>removedAttr</i> = <i>element</i>.removeAttributeNode(<i>attributeNode</i>)
* </pre>
* <ul><li> <code>attributeNode</code> is the <code>Attr</code> node that needs to be removed.
* </li><li> <code>removedAttr</code> is the removed <code>Attr</code> node.
* </li></ul>
* <h2> <span> Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* // &lt;div id="top" align="center" /&gt;
* var d = document.getElementById("top");
* var d_align = d.getAttributeNode("align");
* d.removeAttributeNode(d_align);
* // align has a default value, center,
* // so the removed attribute is immediately
* // replaced: &lt;div id="top" align="center" /&gt;
* </pre>
* <h2> <span> Notes </span></h2>
* <p>If the removed Attribute has a default value it is immediately replaced. The replacing attribute has the same namespace URI and local name, as well as the original prefix, when applicable.
* </p><p>DOM methods dealing with element's attributes:
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <th colspan="1" rowspan="1">Not namespace-aware, most commonly used methods</th>
* <th colspan="1" rowspan="1">Namespace-aware variants (DOM Level 2)</th>
* <th colspan="1" rowspan="1">DOM Level 1 methods for dealing with <code>Attr</code> nodes directly (seldom used)</th>
* <th colspan="1" rowspan="1">DOM Level 2 namespace-aware methods for dealing with <code>Attr</code> nodes directly (seldom used)</th>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><a href="DOM:element.setAttribute" shape="rect" title="DOM:element.setAttribute">setAttribute</a> (DOM 1)</td>
* <td colspan="1" rowspan="1"><a href="element.setAttributeNS" shape="rect" title="DOM:element.setAttributeNS">setAttributeNS</a></td>
* <td colspan="1" rowspan="1"><a href="element.setAttributeNode" shape="rect" title="DOM:element.setAttributeNode">setAttributeNode</a></td>
* <td colspan="1" rowspan="1"><a href="element.setAttributeNodeNS" shape="rect" title="DOM:element.setAttributeNodeNS">setAttributeNodeNS</a></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><a href="element.getAttribute" shape="rect" title="DOM:element.getAttribute">getAttribute</a> (DOM 1)</td>
* <td colspan="1" rowspan="1"><a href="element.getAttributeNS" shape="rect" title="DOM:element.getAttributeNS">getAttributeNS</a></td>
* <td colspan="1" rowspan="1"><a href="element.getAttributeNode" shape="rect" title="DOM:element.getAttributeNode">getAttributeNode</a></td>
* <td colspan="1" rowspan="1"><a href="element.getAttributeNodeNS" shape="rect" title="DOM:element.getAttributeNodeNS">getAttributeNodeNS</a></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><a href="element.hasAttribute" shape="rect" title="DOM:element.hasAttribute">hasAttribute</a> (DOM 2)</td>
* <td colspan="1" rowspan="1"><a href="element.hasAttributeNS" shape="rect" title="DOM:element.hasAttributeNS">hasAttributeNS</a></td>
* <td colspan="1" rowspan="1">-</td>
* <td colspan="1" rowspan="1">-</td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><a href="element.removeAttribute" shape="rect" title="DOM:element.removeAttribute">removeAttribute</a> (DOM 1)</td>
* <td colspan="1" rowspan="1"><a href="element.removeAttributeNS" shape="rect" title="DOM:element.removeAttributeNS">removeAttributeNS</a></td>
* <td colspan="1" rowspan="1"><strong>removeAttributeNode</strong></td>
* <td colspan="1" rowspan="1">-</td>
* </tr>
* </table>
* <h2> <span> Specification </span></h2>
* <p><a href="http://www.w3.org/TR/DOM-Level-2-Core/core.html#ID-D589198" rel="nofollow" shape="rect" title="http://www.w3.org/TR/DOM-Level-2-Core/core.html#ID-D589198">DOM Level 2 Core: removeAttributeNode</a> (introduced in <a href="http://www.w3.org/TR/REC-DOM-Level-1/level-one-core.html#method-removeAttributeNode" rel="nofollow" shape="rect" title="http://www.w3.org/TR/REC-DOM-Level-1/level-one-core.html#method-removeAttributeNode">DOM Level 1 Core</a>)
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
removeAttributeNode: function(attributeNode) { // COMPAT=IE7|FF1|FF2|FF3|OPERA|SAFARI2|SAFARI3|KONQ
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span> Summary </span></h2>
* <p>Removes a child node from the DOM.
* </p>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve"><i>oldChild</i> = <i>element</i>.removeChild(<i>child</i>)
* </pre>
* <ul><li><code>child</code> is the child node to be removed from the DOM.
* </li><li><code>element</code> is the parent node of <code>child</code>.
* </li><li><code>oldChild</code> holds a reference to the removed child node. <code>oldChild</code> == <code>child</code>.
* </li></ul>
* <p>The removed child node still exists in memory, but is no longer part of the DOM. You may reuse the removed node later in your code, via the <code>oldChild</code> object reference.
* </p><p>If <code>child</code> is actually not a child of the <code>element</code> node, the method throws an exception.
* </p>
* <h2> <span> Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* // &lt;div id="top" align="center"&gt;
* //   &lt;div id="nested"&gt;&lt;/div&gt;
* // &lt;/div&gt;
* 
* var d = document.getElementById("top");
* var d_nested = document.getElementById("nested");
* var throwawayNode = d.removeChild(d_nested);
* </pre>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* // remove all children from element
* var element = document.getElementById("top");
* while (element.firstChild) {
* element.removeChild(element.firstChild);
* }
* </pre>
* <h2> <span> Specification </span></h2>
* <ul><li> <a href="http://www.w3.org/TR/REC-DOM-Level-1/level-one-core.html#method-removeChild" rel="nofollow" shape="rect" title="http://www.w3.org/TR/REC-DOM-Level-1/level-one-core.html#method-removeChild">DOM Level 1 Core: removeChild</a>
* </li><li> <a href="http://www.w3.org/TR/DOM-Level-2-Core/core.html#ID-1734834066" rel="nofollow" shape="rect" title="http://www.w3.org/TR/DOM-Level-2-Core/core.html#ID-1734834066">DOM Level 2 Core: removeChild</a>
* </li></ul>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
removeChild: function(child) {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span>Summary</span></h2>
* <p><b>removeEventListener</b> allows the removal of event listeners from the event target.
* </p>
* <h2> <span>Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">element.removeEventListener(<i>type</i>, <i>listener</i>, <i>useCapture</i>)
* </pre>
* <h2> <span>Parameters </span></h2>
* <dl><dt style="font-weight:bold"><code>type</code> </dt><dd> A string representing the event type being registered.
* </dd><dt style="font-weight:bold"><code>listener</code> </dt><dd> The listener parameter takes an interface implemented by the user which contains the methods to be called when the event occurs.
* </dd><dt style="font-weight:bold"><code>useCapture</code> </dt><dd> If true, useCapture indicates that the user wishes to initiate capture. After initiating capture, all events of the specified type will be dispatched to the registered EventListener before being dispatched to any EventTargets beneath them in the tree. Events which are bubbling upward through the tree will not trigger an EventListener designated to use capture.
* </dd></dl>
* <h2> <span>Notes </span></h2>
* <p>If an EventListener is removed from an EventTarget while it is processing an event, it will not be triggered by the current actions. EventListeners can never be invoked after being removed.
* Calling removeEventListener with arguments which do not identify any currently registered EventListener on the EventTarget has no effect.
* See also <a href="http://developer.mozilla.org/en/docs/addEventListener" shape="rect" title="addEventListener">addEventListener</a>.
* </p>
* <h2> <span>Specification </span></h2>
* <p><a href="http://www.w3.org/TR/2000/REC-DOM-Level-2-Events-20001113/events.html#Events-EventTarget-removeEventListener" rel="nofollow" shape="rect" title="http://www.w3.org/TR/2000/REC-DOM-Level-2-Events-20001113/events.html#Events-EventTarget-removeEventListener">removeEventListener </a>
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
removeEventListener: function(type, listener, useCapture) {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span>Summary</span></h2>
* <p>Replaces one child node of the specified element with another.
* </p>
* <h2> <span>Syntax</span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve"><i>replacedNode</i> = <i>parentNode</i>.replaceChild(<i>newChild</i>, <i>oldChild</i>);
* </pre>
* <ul><li> <code>newChild</code> is the new node to replace <code>oldChild</code>. If it already exists in the DOM, it is first removed.
* </li><li> <code>oldChild</code> is the existing child to be replaced.
* </li><li> <code>replacedNode</code> is the replaced node. This is the same node as <code>oldChild</code>.
* </li></ul>
* <h2> <span>Example</span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* 
* // &lt;div&gt;
* //  &lt;span id="childSpan"&gt;foo bar&lt;/span&gt;
* // &lt;/div&gt;
* 
* // create an empty element node
* // without an ID, any attributes, or any content
* var sp1 = document.createElement("span");
* 
* // give it an id attribute called 'newSpan'
* sp1.setAttribute("id", "newSpan");
* 
* // create some content for the new element.
* var sp1_content = document.createTextNode("new replacement span element.");
* 
* // apply that content to the new element
* sp1.appendChild(sp1_content);
* 
* // build a reference to the existing node to be replaced
* var sp2 = document.getElementById("childSpan");
* var parentDiv = sp2.parentNode;
* 
* // replace existing node sp2 with the new span element sp1
* parentDiv.replaceChild(sp1, sp2);
* 
* </pre>
* <h2> <span>Specification</span></h2>
* <p><a href="http://www.w3.org/TR/REC-DOM-Level-1/level-one-core.html#method-replaceChild" rel="nofollow" shape="rect" title="http://www.w3.org/TR/REC-DOM-Level-1/level-one-core.html#method-replaceChild">DOM Level 1 Core: replaceChild</a>
* </p><p><a href="http://www.w3.org/TR/DOM-Level-2-Core/core.html#ID-785887307" rel="nofollow" shape="rect" title="http://www.w3.org/TR/DOM-Level-2-Core/core.html#ID-785887307">DOM Level 2 Core: replaceChild</a>
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
replaceChild: function(newChild, oldChild) {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span>scrollHeight</span></h2>
* <p><abbr title="Dynamic HyperText Markup Language">DHTML</abbr> property that gets the height of the scroll view of an element; it includes the element padding but <b>not</b> its margin.
* </p>
* <h2> <span>Syntax and values</span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var <i>intElemScrollHeight</i> = document.getElementById(<i>id_attribute_value</i>).scrollHeight;
* </pre>
* <p><i>intElemScrollHeight</i> is a variable storing an integer corresponding to the scrollHeight pixel value of the element.
* scrollHeight is a read-only property.
* </p>
* <h2> <span>Description</span></h2>
* <p>An element's <b>scrollHeight</b> is a measurement of the height of an element's content including content not visible on the screen due to overflow.
* </p><p>If the element's content generated a vertical scrollbar, the <code>scrollHeight</code> value is equal to the minimum <code>clientHeight</code> the element would require in order to fit all the content in the viewpoint without using a vertical scrollbar.  When an element's content does not generate a vertical scrollbar, then its <code>scrollHeight</code> property is equal to its <code>clientHeight</code> property.
* </p>
* <h2> <span>Example</span></h2>
* <div id="offsetContainer" style="margin: 26px 0px; background-color: rgb(255, 255, 204); border: 4px dashed black; color: black; position: absolute; left: 260px;"><div id="idDiv" style="margin: 24px 29px; border: 24px black solid; padding: 0px 28px; width: 199px; height: 102px; overflow: auto; background-color: white; font-size: 13px!important; font-family: Arial, sans-serif;"><p id="PaddingTopLabel" style="text-align: center; font-style: italic; font-weight: bold; font-size: 13px!important; font-family: Arial, sans-serif; margin: 0px;">padding-top</p>
* <p>Gentle, individualistic and very loyal, Birman cats fall between Siamese and Persian in character. If you admire cats that are non aggressive, that enjoy being with humans and tend to be on the quiet side, you may well find that Birman cats are just the felines for you.</p>
* <p><span style="float: right;"><a href="http://developer.mozilla.org/en/docs/Image:BirmanCat.jpg" shape="rect" title="Image:BirmanCat.jpg"/></span>All Birmans have colorpointed features, dark coloration of the face, ears, legs and tail.</p>
* <p>Cat image and text coming from <a href="http://www.best-cat-art.com/" rel="nofollow" shape="rect" title="http://www.best-cat-art.com/">www.best-cat-art.com</a></p>
* <p id="PaddingBottomLabel" style="text-align: center; font-style: italic; font-weight: bold; font-size: 13px!important; font-family: Arial, sans-serif; margin: 0px;">padding-bottom</p></div><span style="position: absolute; left: -32px; top: 85px; color: blue; font-weight: bold; font-size: 13px!important; font-family: Arial, sans-serif;">Left</span><span style="position: absolute; left: 170px; top: -24px; color: blue; font-weight: bold; font-size: 13px!important; font-family: Arial, sans-serif;">Top</span><span style="position: absolute; left: 370px; top: 85px; color: blue; font-weight: bold; font-size: 13px!important; font-family: Arial, sans-serif;">Right</span><span style="position: absolute; left: 164px; top: 203px; color: blue; font-weight: bold; font-size: 13px!important; font-family: Arial, sans-serif;">Bottom</span><span style="position: absolute; left: 143px; top: 5px; font-style: italic; font-weight: bold; font-size: 13px!important; font-family: Arial, sans-serif;">margin-top</span><span style="position: absolute; left: 138px; top: 175px; font-style: italic; font-weight: bold; font-size: 13px!important; font-family: Arial, sans-serif;">margin-bottom</span><span style="position: absolute; left: 143px; top: 27px; color: white; font-style: italic; font-weight: bold; font-size: 13px!important; font-family: Arial, sans-serif;">border-top</span><span style="position: absolute; left: 138px; top: 153px; color: white; font-style: italic; font-weight: bold; font-size: 13px!important; font-family: Arial, sans-serif;">border-bottom</span></div>
* <p style="margin-top: 270px;"><a href="http://developer.mozilla.org/en/docs/Image:scrollHeight.png" shape="rect" title="Image:scrollHeight.png"/></p>
* <h2> <span>Specification</span></h2>
* <p>scrollHeight is part of the MSIE's <abbr title="Dynamic HyperText Markup Language">DHTML</abbr> object model.
* scrollHeight is not part of any W3C specification or technical recommendation.
* </p>
* <h2> <span>Notes</span></h2>
* <p>scrollHeight is a property of the <abbr title="Dynamic HyperText Markup Language">DHTML</abbr> object model which was first introduced by MSIE. It is referred to as the height of an element's physical scrolling view.
* </p>
* <h2> <span>References</span></h2>
* <ul><li> <a href="http://msdn.microsoft.com/workshop/author/dhtml/reference/properties/scrollheight.asp?frame=true" rel="nofollow" shape="rect" title="http://msdn.microsoft.com/workshop/author/dhtml/reference/properties/scrollheight.asp?frame=true">MSDN's scrollHeight definition</a>
* </li><li> <a href="http://msdn.microsoft.com/workshop/author/om/measuring.asp" rel="nofollow" shape="rect" title="http://msdn.microsoft.com/workshop/author/om/measuring.asp">MSDN's Measuring Element Dimension and Location</a>
* </li><li> <a href="http://www.mozilla.org/docs/dom/domref/scrollHeight.html" rel="nofollow" shape="rect" title="http://www.mozilla.org/docs/dom/domref/scrollHeight.html">Gecko DOM Reference on scrollHeight</a>
* </li></ul>
* <h2> <span>See Also</span></h2>
* <ul><li> <a href="DOM:element.clientHeight" shape="rect" title="DOM:element.clientHeight">DOM:element.clientHeight</a>
* </li><li> <a href="element.offsetHeight" shape="rect" title="DOM:element.offsetHeight">DOM:element.offsetHeight</a>
* </li></ul>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
scrollHeight: undefined,
/**
* <h1> <span>Summary</span></h1>
* <p>The <b>scrollIntoView</b> method scrolls the element into view.
* </p>
* <h1> <span>Syntax</span></h1>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">element.scrollIntoView(alignWithTop);
* </pre>
* <ul><li>alignWithTop is an optional boolean that if set to true, aligns the scrolled element with the top of the scroll area.  If false, it is aligned with the bottom.
* </li><li>if no alignWithTop parameter is provided, the element is aligned with the top.
* </li></ul>
* <h1> <span>Example</span></h1>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* &lt;html&gt;
* &lt;head&gt;
* &lt;title&gt;ScrollIntoView() example&lt;/title&gt;
* 
* &lt;script type="text/javascript"&gt;
* function showIt(elID)
* {
* var el = document.getElementById(elID);
* el.scrollIntoView(true);
* }
* &lt;/script&gt;
* 
* &lt;/head&gt;
* &lt;body&gt;
* &lt;div style="height: 5em; width: 30em; overflow: scroll;
* border: 1px solid blue;"&gt;
* &lt;div style="height: 100px"&gt;&lt;/div&gt;
* &lt;p id="pToShow"&gt;The para to show&lt;/p&gt;
* &lt;div style="height: 100px"&gt;&lt;/div&gt;
* &lt;/div&gt;
* &lt;input type="button" value="Show para"
* onclick="showIt('pToShow');"&gt;
* 
* &lt;/body&gt;
* &lt;/html&gt;
* </pre>
* <h1> <span>Notes</span></h1>
* <p>The element may not be scrolled completely to the top or bottom depending on the layout of other elements.
* </p>
* <h1> <span>Specification</span></h1>
* <p>Not part of the specification.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
scrollIntoView: function(alignWithTop) {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span>Summary</span></h2>
* <p><b>scrollLeft </b> gets or sets the number of pixels that an element's content is scrolled to the left.
* </p>
* <h2> <span>Syntax and values</span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">// Get the number of pixels scrolled
* var <var>sLeft</var> = <var>element</var>.scrollLeft;
* </pre>
* <p><var>sLeft</var> is an integer representing the number of pixels that <var>element</var> has been scrolled to the left.
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">// Set the number of pixels scrolled
* <var>element</var>.scrollLeft = 10;
* </pre>
* <p><b>scrollLeft</b> can be set to any integer value, however:
* </p>
* <ul><li>If the element can't be scrolled (e.g. it has no overflow), scrollLeft is set to 0.
* </li><li>If set to a value less than 0, scrollLeft is set to 0.
* </li><li>If set to a value greater than the maximum that the content can be scrolled, scrollLeft is set to the maximum.
* </li></ul>
* <h2> <span>Example</span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* &lt;script type="text/javascript"&gt;
* 
* function doScrollLeft(el, p)
* {
* el.scrollLeft = p;
* }
* 
* &lt;/script&gt;
* 
* &lt;div id="aDiv"
* style="width: 100px; height: 200px; overflow: auto;"
* &gt;
* &lt;script type="text/javascript"&gt;
* for (var i=0; i&lt;100; ++i){
* document.write(i + '-FooBar-FooBar-FooBar&lt;br&gt;');
* }
* &lt;/script&gt;
* &lt;/div&gt;
* &lt;br&gt;
* &lt;input type="button" value="Scroll left 50"
* onclick="doScrollLeft(document.getElementById('aDiv'), 50);"
* &gt;
* </pre>
* <h2> <span>Specification</span></h2>
* <p>Not part of any W3C specification.
* </p>
* <h2> <span>References</span></h2>
* <p><a href="http://msdn.microsoft.com/workshop/author/dhtml/reference/properties/scrollleft.asp" rel="nofollow" shape="rect" title="http://msdn.microsoft.com/workshop/author/dhtml/reference/properties/scrollleft.asp">MSDN scrollLeft</a>
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
scrollLeft: undefined,
/**
* <h2> <span>Summary</span></h2>
* <p><b>scrollTop</b> gets or sets the number of pixels that the content of an element is scrolled upward.
* </p>
* <h2> <span>Syntax and values</span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">// Get the number of pixels scrolled
* var <var> intElemScrollTop</var> = <var>element</var>.scrollTop;
* </pre>
* <p><var>intElemScrollTop</var> is an integer corresponding to number of pixels that <var>element</var>'s content has been scrolled upward.
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">// Set the number of pixels scrolled
* <var>element</var>.scrollTop = <var>intValue</var>;
* </pre>
* <p><b>scrollTop</b> can be set to any integer value, however:
* </p>
* <ul><li>If the element can't be scrolled (e.g. it has no overflow), scrollTop is set to 0.
* </li><li>If set to a value less than 0, scrollTop is set to 0.
* </li><li>If set to a value greater than the maximum that the content can be scrolled, scrollTop is set to the maximum.
* </li></ul>
* <h2> <span>Description</span></h2>
* <p>An element's <b>scrollTop</b> is a measurement of the distance of an element's top to its topmost visible content.
* </p><p>When an element content does not generate a vertical scrollbar, then its scrollTop value defaults to 0.
* </p>
* <h2> <span>Example</span></h2>
* <div id="offsetContainer" style="margin: 26px 0px; background-color: rgb(255, 255, 204); border: 4px dashed black; color: black; position: absolute; left: 260px;"><div id="idDiv" style="margin: 24px 29px; border: 24px black solid; padding: 0px 28px; width: 199px; height: 102px; overflow: auto; background-color: white; font-size: 13px!important; font-family: Arial, sans-serif;"><p id="PaddingTopLabel" style="text-align: center; font-style: italic; font-weight: bold; font-size: 13px!important; font-family: Arial, sans-serif; margin: 0px;">padding-top</p>
* <p>Gentle, individualistic and very loyal, Birman cats fall between Siamese and Persian in character. If you admire cats that are non aggressive, that enjoy being with humans and tend to be on the quiet side, you may well find that Birman cats are just the felines for you.</p>
* <p><span style="float: right;"><a href="http://developer.mozilla.org/en/docs/Image:BirmanCat.jpg" shape="rect" title="Image:BirmanCat.jpg"/></span>All Birmans have colorpointed features, dark coloration of the face, ears, legs and tail.</p>
* <p>Cat image and text coming from <a href="http://www.best-cat-art.com/" rel="nofollow" shape="rect" title="http://www.best-cat-art.com/">www.best-cat-art.com</a></p>
* <p id="PaddingBottomLabel" style="text-align: center; font-style: italic; font-weight: bold; font-size: 13px!important; font-family: Arial, sans-serif; margin: 0px;">padding-bottom</p></div><span style="position: absolute; left: -32px; top: 85px; color: blue; font-weight: bold; font-size: 13px!important; font-family: Arial, sans-serif;">Left</span><span style="position: absolute; left: 170px; top: -24px; color: blue; font-weight: bold; font-size: 13px!important; font-family: Arial, sans-serif;">Top</span><span style="position: absolute; left: 370px; top: 85px; color: blue; font-weight: bold; font-size: 13px!important; font-family: Arial, sans-serif;">Right</span><span style="position: absolute; left: 164px; top: 203px; color: blue; font-weight: bold; font-size: 13px!important; font-family: Arial, sans-serif;">Bottom</span><span style="position: absolute; left: 143px; top: 5px; font-style: italic; font-weight: bold; font-size: 13px!important; font-family: Arial, sans-serif;">margin-top</span><span style="position: absolute; left: 138px; top: 175px; font-style: italic; font-weight: bold; font-size: 13px!important; font-family: Arial, sans-serif;">margin-bottom</span><span style="position: absolute; left: 143px; top: 27px; color: white; font-style: italic; font-weight: bold; font-size: 13px!important; font-family: Arial, sans-serif;">border-top</span><span style="position: absolute; left: 138px; top: 153px; color: white; font-style: italic; font-weight: bold; font-size: 13px!important; font-family: Arial, sans-serif;">border-bottom</span></div>
* <p style="margin-top: 270px;"><a href="http://developer.mozilla.org/en/docs/Image:scrollTop.png" shape="rect" title="Image:scrollTop.png"/></p>
* <p>We do not have an interactive demo example like we do at Gecko DOM reference.</p>
* <h2> <span>Specification</span></h2>
* <p>scrollTop is part of the MSIE's <abbr title="Dynamic HyperText Markup Language">DHTML</abbr> object model.
* scrollTop is not part of any W3C specification or technical recommendation.
* </p>
* <h2> <span>Notes</span></h2>
* <p>scrollTop is a property of the <abbr title="Dynamic HyperText Markup Language">DHTML</abbr> object model which was first introduced by MSIE. It is referred as the distance to the top of an element physical scrolling view.
* </p>
* <h2> <span>References</span></h2>
* <ul><li> <a href="http://msdn.microsoft.com/workshop/author/dhtml/reference/properties/scrolltop.asp?frame=true" rel="nofollow" shape="rect" title="http://msdn.microsoft.com/workshop/author/dhtml/reference/properties/scrolltop.asp?frame=true">MSDN's scrollTop definition</a>
* </li><li> <a href="http://msdn.microsoft.com/workshop/author/om/measuring.asp" rel="nofollow" shape="rect" title="http://msdn.microsoft.com/workshop/author/om/measuring.asp">MSDN's Measuring Element Dimension and Location</a>
* </li><li> <a href="http://www.mozilla.org/docs/dom/domref/scrollTop.html" rel="nofollow" shape="rect" title="http://www.mozilla.org/docs/dom/domref/scrollTop.html">Gecko DOM Reference on scrollTop</a>
* </li></ul>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
scrollTop: undefined,
/**
* <h2> <span>Summary</span></h2>
* <p><b>scrollWidth</b> is a read–only property that returns either the width in pixels of the content of an element or the width of the element itself, whichever is greater.
* </p>
* <h2> <span>Syntax and values</span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var <var>xScrollWidth</var> = <var>element</var>.scrollWidth;
* </pre>
* <p><var>xScrollWidth</var> is the width of the content of <var>element</var> in pixels.
* </p>
* <h2> <span>Example</span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* &lt;div id="aDiv"
* style="width: 100px; height: 200px; overflow: auto;"
* &gt;-FooBar-FooBar-FooBar&lt;/div&gt;
* &lt;br&gt;
* &lt;input type="button" value="Show scrollWidth"
* onclick="alert(document.getElementById('aDiv').scrollWidth);"&gt;
* </pre>
* <h2> <span>Specification</span></h2>
* <p>There is no W3C specification for <b>scrollWidth</b>.
* </p><p>There is an editor's draft: <a href="http://dev.w3.org/cvsweb/~checkout~/csswg/cssom/Overview.src.html" rel="nofollow" shape="rect" title="http://dev.w3.org/cvsweb/~checkout~/csswg/cssom/Overview.src.html">[1]</a>
* </p>
* <h2> <span>References</span></h2>
* <p><a href="http://msdn.microsoft.com/workshop/author/dhtml/reference/properties/scrollwidth.asp" rel="nofollow" shape="rect" title="http://msdn.microsoft.com/workshop/author/dhtml/reference/properties/scrollwidth.asp">MSDN scrollWidth reference</a>
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
scrollWidth: undefined,
/**
* <h2> <span> Summary </span></h2>
* <p>Adds a new attribute or changes the value of an existing attribute on the specified element.
* </p>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve"><i>element</i>.setAttribute(<i>name</i>, <i>value</i>);
* </pre>
* <ul><li> <code>name</code> is the name of the attribute as a string.
* </li><li> <code>value</code> is the desired new value of the attribute.
* </li></ul>
* <h2> <span> Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var d = document.getElementById("d1");
* d.setAttribute("align", "center");
* </pre>
* <h2> <span> Notes </span></h2>
* <p>If the specified attribute already exists, then the value of that attribute is changed to the value passed to this function. If it does not exist, then the attribute is created.
* </p><p>Even though <code><a href="DOM:element.getAttribute" shape="rect" title="DOM:element.getAttribute">getAttribute()</a></code> returns <code>null</code> for missing attributes, you should use <code><a href="element.removeAttribute" shape="rect" title="DOM:element.removeAttribute">removeAttribute()</a></code> instead of <code><i>elt</i>.setAttribute(<i>attr</i>, null)</code> to remove the attribute.
* </p><p>Using <code>setAttribute()</code> to modify certain attributes, most notably <code>value</code> in XUL and HTML and <code>selected</code> in HTML, works inconsistently, as the attribute specifies the default value. To access or modify the current values, you should use the properties.  For example, use <code><i>elt</i>.value</code> instead of <code><i>elt</i>.setAttribute('value', <i>val</i>)</code>.
* </p><p>DOM methods dealing with element's attributes:
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <th colspan="1" rowspan="1">Not namespace-aware, most commonly used methods</th>
* <th colspan="1" rowspan="1">Namespace-aware variants (DOM Level 2)</th>
* <th colspan="1" rowspan="1">DOM Level 1 methods for dealing with <code>Attr</code> nodes directly (seldom used)</th>
* <th colspan="1" rowspan="1">DOM Level 2 namespace-aware methods for dealing with <code>Attr</code> nodes directly (seldom used)</th>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><strong>setAttribute</strong> (DOM 1)</td>
* <td colspan="1" rowspan="1"><a href="element.setAttributeNS" shape="rect" title="DOM:element.setAttributeNS">setAttributeNS</a></td>
* <td colspan="1" rowspan="1"><a href="element.setAttributeNode" shape="rect" title="DOM:element.setAttributeNode">setAttributeNode</a></td>
* <td colspan="1" rowspan="1"><a href="element.setAttributeNodeNS" shape="rect" title="DOM:element.setAttributeNodeNS">setAttributeNodeNS</a></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><a href="element.getAttribute" shape="rect" title="DOM:element.getAttribute">getAttribute</a> (DOM 1)</td>
* <td colspan="1" rowspan="1"><a href="element.getAttributeNS" shape="rect" title="DOM:element.getAttributeNS">getAttributeNS</a></td>
* <td colspan="1" rowspan="1"><a href="element.getAttributeNode" shape="rect" title="DOM:element.getAttributeNode">getAttributeNode</a></td>
* <td colspan="1" rowspan="1"><a href="element.getAttributeNodeNS" shape="rect" title="DOM:element.getAttributeNodeNS">getAttributeNodeNS</a></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><a href="element.hasAttribute" shape="rect" title="DOM:element.hasAttribute">hasAttribute</a> (DOM 2)</td>
* <td colspan="1" rowspan="1"><a href="element.hasAttributeNS" shape="rect" title="DOM:element.hasAttributeNS">hasAttributeNS</a></td>
* <td colspan="1" rowspan="1">-</td>
* <td colspan="1" rowspan="1">-</td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><a href="element.removeAttribute" shape="rect" title="DOM:element.removeAttribute">removeAttribute</a> (DOM 1)</td>
* <td colspan="1" rowspan="1"><a href="element.removeAttributeNS" shape="rect" title="DOM:element.removeAttributeNS">removeAttributeNS</a></td>
* <td colspan="1" rowspan="1"><a href="element.removeAttributeNode" shape="rect" title="DOM:element.removeAttributeNode">removeAttributeNode</a></td>
* <td colspan="1" rowspan="1">-</td>
* </tr>
* </table>
* <h2> <span> Specification </span></h2>
* <p><a href="http://www.w3.org/TR/DOM-Level-2-Core/core.html#ID-F68F082" rel="nofollow" shape="rect" title="http://www.w3.org/TR/DOM-Level-2-Core/core.html#ID-F68F082">DOM Level 2 Core: setAttribute</a> (introduced in <a href="http://www.w3.org/TR/REC-DOM-Level-1/level-one-core.html#method-setAttribute" rel="nofollow" shape="rect" title="http://www.w3.org/TR/REC-DOM-Level-1/level-one-core.html#method-setAttribute">DOM Level 1 Core</a>)
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
setAttribute: function(name, value) {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span> Summary </span></h2>
* <p><code>setAttributeNS</code> adds a new attribute or changes the value of an attribute with the given namespace and name.
* </p>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve"><i>element</i>.setAttributeNS(<i>namespace</i>, <i>name</i>, <i>value</i>)
* </pre>
* <ul><li> <code>namespace</code> is a string specifying the namespace of the attribute.
* </li><li> <code>name</code> is a string identifying the attribute to be set.
* </li><li> <code>value</code> is the desired string value of the new attribute.
* </li></ul>
* <h2> <span> Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var d = document.getElementById("d1");
* d.setAttributeNS("http://www.mozilla.org/ns/specialspace", "align", "center");
* </pre>
* <h2> <span> Notes </span></h2>
* <p>DOM methods dealing with element's attributes:
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <th colspan="1" rowspan="1">Not namespace-aware, most commonly used methods</th>
* <th colspan="1" rowspan="1">Namespace-aware variants (DOM Level 2)</th>
* <th colspan="1" rowspan="1">DOM Level 1 methods for dealing with <code>Attr</code> nodes directly (seldom used)</th>
* <th colspan="1" rowspan="1">DOM Level 2 namespace-aware methods for dealing with <code>Attr</code> nodes directly (seldom used)</th>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><a href="DOM:element.setAttribute" shape="rect" title="DOM:element.setAttribute">setAttribute</a> (DOM 1)</td>
* <td colspan="1" rowspan="1"><strong>setAttributeNS</strong></td>
* <td colspan="1" rowspan="1"><a href="element.setAttributeNode" shape="rect" title="DOM:element.setAttributeNode">setAttributeNode</a></td>
* <td colspan="1" rowspan="1"><a href="element.setAttributeNodeNS" shape="rect" title="DOM:element.setAttributeNodeNS">setAttributeNodeNS</a></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><a href="element.getAttribute" shape="rect" title="DOM:element.getAttribute">getAttribute</a> (DOM 1)</td>
* <td colspan="1" rowspan="1"><a href="element.getAttributeNS" shape="rect" title="DOM:element.getAttributeNS">getAttributeNS</a></td>
* <td colspan="1" rowspan="1"><a href="element.getAttributeNode" shape="rect" title="DOM:element.getAttributeNode">getAttributeNode</a></td>
* <td colspan="1" rowspan="1"><a href="element.getAttributeNodeNS" shape="rect" title="DOM:element.getAttributeNodeNS">getAttributeNodeNS</a></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><a href="element.hasAttribute" shape="rect" title="DOM:element.hasAttribute">hasAttribute</a> (DOM 2)</td>
* <td colspan="1" rowspan="1"><a href="element.hasAttributeNS" shape="rect" title="DOM:element.hasAttributeNS">hasAttributeNS</a></td>
* <td colspan="1" rowspan="1">-</td>
* <td colspan="1" rowspan="1">-</td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><a href="element.removeAttribute" shape="rect" title="DOM:element.removeAttribute">removeAttribute</a> (DOM 1)</td>
* <td colspan="1" rowspan="1"><a href="element.removeAttributeNS" shape="rect" title="DOM:element.removeAttributeNS">removeAttributeNS</a></td>
* <td colspan="1" rowspan="1"><a href="element.removeAttributeNode" shape="rect" title="DOM:element.removeAttributeNode">removeAttributeNode</a></td>
* <td colspan="1" rowspan="1">-</td>
* </tr>
* </table>
* <h2> <span> Specification </span></h2>
* <p><a href="http://www.w3.org/TR/DOM-Level-2-Core/core.html#ID-ElSetAttrNS" rel="nofollow" shape="rect" title="http://www.w3.org/TR/DOM-Level-2-Core/core.html#ID-ElSetAttrNS">DOM Level 2 Core: setAttributeNS</a>
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
setAttributeNS: function(namespace, name, value) {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span> Summary </span></h2>
* <p><code>setAttributeNode()</code> adds a new <code>Attr</code> node to the specified element.
* </p>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve"><i>replacedAttr</i> = <i>element</i>.setAttributeNode(<i>attribute</i>)
* </pre>
* <ul><li> <code>attribute</code> is the <code>Attr</code> node to set on the element.
* </li><li> <code>replacedAttr</code> is the replaced attribute node, if any, returned by this function.
* </li></ul>
* <h2> <span> Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* // &lt;div id="one" align="left"&gt;one&lt;/div&gt;
* // &lt;div id="two"&gt;two&lt;/div&gt;
* var d1 = document.getElementById("one");
* var d2 = document.getElementById("two");
* var a = d1.getAttributeNode("align");
* d2.setAttributeNode(a);
* alert(d2.attributes[1].value)
* // returns: `left'
* </pre>
* <h2> <span> Notes </span></h2>
* <p>If the attribute named already exists on the element, that attribute is replaced with the new one and the replaced one is returned.
* </p><p>This method is seldom used, with <code><a href="DOM:element.setAttribute" shape="rect" title="DOM:element.setAttribute">setAttribute()</a></code> usually being used to change element's attributes.
* </p><p>DOM methods dealing with element's attributes:
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <th colspan="1" rowspan="1">Not namespace-aware, most commonly used methods</th>
* <th colspan="1" rowspan="1">Namespace-aware variants (DOM Level 2)</th>
* <th colspan="1" rowspan="1">DOM Level 1 methods for dealing with <code>Attr</code> nodes directly (seldom used)</th>
* <th colspan="1" rowspan="1">DOM Level 2 namespace-aware methods for dealing with <code>Attr</code> nodes directly (seldom used)</th>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><a href="element.setAttribute" shape="rect" title="DOM:element.setAttribute">setAttribute</a> (DOM 1)</td>
* <td colspan="1" rowspan="1"><a href="element.setAttributeNS" shape="rect" title="DOM:element.setAttributeNS">setAttributeNS</a></td>
* <td colspan="1" rowspan="1"><strong>setAttributeNode</strong></td>
* <td colspan="1" rowspan="1"><a href="element.setAttributeNodeNS" shape="rect" title="DOM:element.setAttributeNodeNS">setAttributeNodeNS</a></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><a href="element.getAttribute" shape="rect" title="DOM:element.getAttribute">getAttribute</a> (DOM 1)</td>
* <td colspan="1" rowspan="1"><a href="element.getAttributeNS" shape="rect" title="DOM:element.getAttributeNS">getAttributeNS</a></td>
* <td colspan="1" rowspan="1"><a href="element.getAttributeNode" shape="rect" title="DOM:element.getAttributeNode">getAttributeNode</a></td>
* <td colspan="1" rowspan="1"><a href="element.getAttributeNodeNS" shape="rect" title="DOM:element.getAttributeNodeNS">getAttributeNodeNS</a></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><a href="element.hasAttribute" shape="rect" title="DOM:element.hasAttribute">hasAttribute</a> (DOM 2)</td>
* <td colspan="1" rowspan="1"><a href="element.hasAttributeNS" shape="rect" title="DOM:element.hasAttributeNS">hasAttributeNS</a></td>
* <td colspan="1" rowspan="1">-</td>
* <td colspan="1" rowspan="1">-</td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><a href="element.removeAttribute" shape="rect" title="DOM:element.removeAttribute">removeAttribute</a> (DOM 1)</td>
* <td colspan="1" rowspan="1"><a href="element.removeAttributeNS" shape="rect" title="DOM:element.removeAttributeNS">removeAttributeNS</a></td>
* <td colspan="1" rowspan="1"><a href="element.removeAttributeNode" shape="rect" title="DOM:element.removeAttributeNode">removeAttributeNode</a></td>
* <td colspan="1" rowspan="1">-</td>
* </tr>
* </table>
* <h2> <span> Specification </span></h2>
* <p><a href="http://www.w3.org/TR/DOM-Level-2-Core/core.html#ID-887236154" rel="nofollow" shape="rect" title="http://www.w3.org/TR/DOM-Level-2-Core/core.html#ID-887236154">DOM Level 2 Core: setAttributeNode</a> (introduced in <a href="http://www.w3.org/TR/REC-DOM-Level-1/level-one-core.html#method-setAttributeNode" rel="nofollow" shape="rect" title="http://www.w3.org/TR/REC-DOM-Level-1/level-one-core.html#method-setAttributeNode">DOM Level 1 Core</a>)
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
setAttributeNode: function(attribute) { // COMPAT=IE6|IE7|FF1|FF2|FF3|OPERA|SAFARI2|SAFARI3|KONQ
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span> Summary </span></h2>
* <p><code>setAttributeNodeNS</code> adds a new attribute node with the specified namespace and name.
* </p>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve"><i>replacedAttr</i> = element.setAttributeNodeNS(<i>attributeNode</i>)
* </pre>
* <ul><li> <code>replacedAttr</code> is the replaced attribute node, if any, returned by this function.
* </li><li> <code>attributeNode</code> is an <code>Attr</code> node.
* </li></ul>
* <h2> <span> Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* // &lt;div id="one" special-align="utterleft"&gt;one&lt;/div&gt;
* // &lt;div id="two"&gt;two&lt;/div&gt;
* 
* var myns = "http://www.mozilla.org/ns/specialspace";
* var d1 = document.getElementById("one");
* var d2 = document.getElementById("two");
* var a = d1.getAttributeNodeNS(myns, "special-align");
* d2.setAttributeNodeNS(a);
* 
* alert(d2.attributes[1].value) // returns: `utterleft'
* </pre>
* <h2> <span> Notes </span></h2>
* <p>If the specified attribute already exists on the element, then that attribute is replaced with the new one and the replaced one is returned.
* </p><p>DOM methods dealing with element's attributes:
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <th colspan="1" rowspan="1">Not namespace-aware, most commonly used methods</th>
* <th colspan="1" rowspan="1">Namespace-aware variants (DOM Level 2)</th>
* <th colspan="1" rowspan="1">DOM Level 1 methods for dealing with <code>Attr</code> nodes directly (seldom used)</th>
* <th colspan="1" rowspan="1">DOM Level 2 namespace-aware methods for dealing with <code>Attr</code> nodes directly (seldom used)</th>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><a href="DOM:element.setAttribute" shape="rect" title="DOM:element.setAttribute">setAttribute</a> (DOM 1)</td>
* <td colspan="1" rowspan="1"><a href="element.setAttributeNS" shape="rect" title="DOM:element.setAttributeNS">setAttributeNS</a></td>
* <td colspan="1" rowspan="1"><a href="element.setAttributeNode" shape="rect" title="DOM:element.setAttributeNode">setAttributeNode</a></td>
* <td colspan="1" rowspan="1"><strong>setAttributeNodeNS</strong></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><a href="element.getAttribute" shape="rect" title="DOM:element.getAttribute">getAttribute</a> (DOM 1)</td>
* <td colspan="1" rowspan="1"><a href="element.getAttributeNS" shape="rect" title="DOM:element.getAttributeNS">getAttributeNS</a></td>
* <td colspan="1" rowspan="1"><a href="element.getAttributeNode" shape="rect" title="DOM:element.getAttributeNode">getAttributeNode</a></td>
* <td colspan="1" rowspan="1"><a href="element.getAttributeNodeNS" shape="rect" title="DOM:element.getAttributeNodeNS">getAttributeNodeNS</a></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><a href="element.hasAttribute" shape="rect" title="DOM:element.hasAttribute">hasAttribute</a> (DOM 2)</td>
* <td colspan="1" rowspan="1"><a href="element.hasAttributeNS" shape="rect" title="DOM:element.hasAttributeNS">hasAttributeNS</a></td>
* <td colspan="1" rowspan="1">-</td>
* <td colspan="1" rowspan="1">-</td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><a href="element.removeAttribute" shape="rect" title="DOM:element.removeAttribute">removeAttribute</a> (DOM 1)</td>
* <td colspan="1" rowspan="1"><a href="element.removeAttributeNS" shape="rect" title="DOM:element.removeAttributeNS">removeAttributeNS</a></td>
* <td colspan="1" rowspan="1"><a href="element.removeAttributeNode" shape="rect" title="DOM:element.removeAttributeNode">removeAttributeNode</a></td>
* <td colspan="1" rowspan="1">-</td>
* </tr>
* </table>
* <h2> <span> Specification </span></h2>
* <p><a href="http://www.w3.org/TR/DOM-Level-2-Core/core.html#ID-ElSetAtNodeNS" rel="nofollow" shape="rect" title="http://www.w3.org/TR/DOM-Level-2-Core/core.html#ID-ElSetAtNodeNS">DOM Level 2 Core: setAttributeNodeNS</a>
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
setAttributeNodeNS: function(attributeNode) {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span> Summary </span></h2>
* <p>Returns an object that represents the element's <code>style</code> attribute.
* </p>
* <h2> <span> Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var div = document.getElementById("div1");
* div.style.marginTop = ".25in";
* </pre>
* <h2> <span> Notes </span></h2>
* <p>Since the <code>style</code> property has the same (and highest) priority in the CSS cascade as an inline style declaration via the <code>style</code> attribute, it is useful for setting style on one specific element.
* </p><p>However, it is not useful for learning about the element's style in general, since it represents only the CSS declarations set in the element's inline <code>style</code> attribute, not those that come from style rules elsewhere, such as style rules in the &lt;head&gt; section, or external style sheets.
* </p><p>To get the values of all CSS properties for an element you should use <a href="window.getComputedStyle" shape="rect" title="DOM:window.getComputedStyle">window.getComputedStyle</a> instead.
* </p><p>See the <a href="CSS" shape="rect" title="DOM:CSS">DOM CSS Properties List</a> for a list of the CSS properties that are accessible from the Gecko DOM. There are some additional notes there about the use of the <code>style</code> property to style elements in the DOM.
* </p><p>It is generally better to use the <code>style</code> property than to use <code>elt.setAttribute('style', '...')</code>, since use of the <code>style</code> property will not overwrite other CSS properties that may be specified in the <code>style</code> attribute.
* </p><p>Styles can <i>not</i> be set by assigning a string to the (read only) <code>style</code> property, as in <code>elt.style = "color: blue;"</code>.  This is because the style attribute returns a <code>CSSStyleDeclaration</code> object.  Instead, you can set style properties like this:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">elt.style.color = "blue";  // Directly
* 
* var st = elt.style;
* st.color = "blue";  // Indirectly
* </pre>
* <p>The following code displays the names of all the style properties, the values set explicitly for element <code>elt</code> and the inherited 'computed' values:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var elt = document.getElementById("elementIdHere");
* var out = "";
* var st = elt.style;
* var cs = window.getComputedStyle(elt, null);
* for (x in st)
* out += "  " + x + " = '" + st[x] + "' &gt; '" + cs[x] + "'\n";
* </pre>
* <h2> <span> Specification </span></h2>
* <p><a href="http://www.w3.org/TR/DOM-Level-2-Style/css.html#CSS-ElementCSSInlineStyle" rel="nofollow" shape="rect" title="http://www.w3.org/TR/DOM-Level-2-Style/css.html#CSS-ElementCSSInlineStyle">DOM Level 2 Style: ElementCSSInlineStyle.style</a>
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
style: undefined,
/**
* <h2> <span>Summary</span></h2>
* <p>Gets/sets the tab order of the current element.
* </p>
* <h2> <span>Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">element.tabIndex = <i>iIndex</i>
* </pre>
* <h2> <span>Parameters </span></h2>
* <ul><li> <code>iIndex</code> is a number
* </li></ul>
* <h2> <span>Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* b1 = document.getElementById("button1");
* b1.tabIndex = 1;
* </pre>
* <h2> <span>Specification </span></h2>
* <p><a href="http://www.w3.org/TR/2000/WD-DOM-Level-2-HTML-20001113/html.html#ID-40676705" rel="nofollow" shape="rect" title="http://www.w3.org/TR/2000/WD-DOM-Level-2-HTML-20001113/html.html#ID-40676705">tabIndex </a>
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
tabIndex: undefined,
/**
* <h2> <span> Summary</span></h2>
* <p>Returns the name of the element.
* </p>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve"><i>elementName</i> = element.tagName
* </pre>
* <ul><li> <code>elementName</code> is a string containing the name of the current element.
* </li></ul>
* <h2> <span> Notes </span></h2>
* <p>In XML (and XML-based languages such as XHTML), <code>tagName</code> preserves case. In HTML, <code>tagName</code> returns the element name in the canonical uppercase form. The value of <code>tagName</code> is the same as that of <a href="element.nodeName" shape="rect" title="DOM:element.nodeName">nodeName</a>.
* </p>
* <h2> <span> Example </span></h2>
* <p>Given the following markup:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">&lt;span id="born"&gt;When I was born...&lt;/span&gt;
* </pre>
* <p>and the following script
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var span = document.getElementById("born");
* alert(span.tagName);
* </pre>
* <p>In XHTML (or any other XML format), "span" would be alerted. In HTML, "SPAN" would be alerted instead.
* </p>
* <h2> <span> Specification </span></h2>
* <p><a href="http://www.w3.org/TR/2000/REC-DOM-Level-2-Core-20001113/core.html#ID-104682815" rel="nofollow" shape="rect" title="http://www.w3.org/TR/2000/REC-DOM-Level-2-Core-20001113/core.html#ID-104682815">DOM Level 2 Core: tagName</a>
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
tagName: undefined,
/**
* <h2> <span> Summary </span></h2>
* <p>Gets or sets the text content of a node and its descendants.
* </p>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve"><i>text</i> = element.textContent
* element.textContent = "this is some sample text"
* </pre>
* <h2> <span> Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">// Given the following HTML fragment:
* //   &lt;div id="divA"&gt;This is &lt;span&gt;some&lt;/span&gt; text&lt;/div&gt;
* 
* // Get the text content:
* var text = document.getElementById("divA").textContent;
* // |text| is set to "This is some text".
* 
* // Set the text content:
* document.getElementById("divA").textContent = "This is some text";
* // The HTML for divA is now &lt;div id="divA"&gt;This is some text&lt;/div&gt;
* </pre>
* <h2> <span> Notes </span></h2>
* <ul><li> <code>textContent</code> returns <code>null</code> if the element is a document, a document type, or a notation.
* </li><li> If the node is a CDATA section, a comment, a processing instruction, or a text node, <code>textContent</code> returns the text inside this node (the <a href="DOM:element.nodeValue" shape="rect" title="DOM:element.nodeValue">nodeValue</a>).
* </li><li> For other node types, <code>textContent</code> returns the concatenation of the <code>textContent</code> attribute value of every child node, excluding comments and processing instruction nodes. This is an empty string if the node has no children.
* </li><li> Setting this property on a node removes all of its children and replaces them with a single text node with the given value.
* </li></ul>
* <h2> <span> Specification </span></h2>
* <p><a href="http://www.w3.org/TR/2004/REC-DOM-Level-3-Core-20040407/core.html#Node3-textContent" rel="nofollow" shape="rect" title="http://www.w3.org/TR/2004/REC-DOM-Level-3-Core-20040407/core.html#Node3-textContent">textContent</a>
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
textContent: undefined,
};

/**
* <p>This chapter provides a brief reference for the general methods, properties, and events available to most HTML and XML elements in the Gecko DOM.
* </p><p>Various W3C specifications apply to elements:
* </p>
* <ul><li> <a href="http://www.w3.org/TR/DOM-Level-2-Core/" rel="nofollow" shape="rect" title="http://www.w3.org/TR/DOM-Level-2-Core/">DOM Core Specification</a>—describes the core interfaces shared by most DOM objects in HTML and XML documents
* </li><li> <a href="http://www.w3.org/TR/DOM-Level-2-HTML/" rel="nofollow" shape="rect" title="http://www.w3.org/TR/DOM-Level-2-HTML/">DOM HTML Specification</a>—describes interfaces for objects in HTML and XHTML documents that build on the core specification
* </li><li> <a href="http://www.w3.org/TR/DOM-Level-2-Events/" rel="nofollow" shape="rect" title="http://www.w3.org/TR/DOM-Level-2-Events/">DOM Events Specification</a>—describes events shared by most DOM objects, building on the DOM Core and <a href="http://www.w3.org/TR/DOM-Level-2-Views/" rel="nofollow" shape="rect" title="http://www.w3.org/TR/DOM-Level-2-Views/">Views</a> specifications
* </li></ul>
* <p>The articles listed here span the above and include links to the appropriate W3C DOM specification.
* </p><p>While these interfaces are generally shared by most HTML and XML elements, there are more specialized interfaces for particular objects listed in the DOM HTML Specification—for example the <a href="table" shape="rect" title="DOM:table">HTML Table Element</a> and <a href="form" shape="rect" title="DOM:form">HTML Form Element</a> interfaces.
* </p>
* 
* <h2> <span> Properties </span></h2>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <th colspan="1" rowspan="1">Name</th>
* <th colspan="1" rowspan="1">Description</th>
* <th colspan="1" rowspan="1">Type</th>
* <th colspan="1" rowspan="1">Availability</th>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="DOM:element.attributes" shape="rect" title="DOM:element.attributes">attributes</a></code></td>
* <td colspan="1" rowspan="1">All attributes associated with an element.</td>
* <td colspan="1" rowspan="1"><code><a href="http://developer.mozilla.org/en/docs/index.php?title=NamedNodeMap&amp;action=edit" shape="rect" title="DOM:NamedNodeMap">NamedNodeMap</a><a href="http://www.xulplanet.com/references/objref/NamedNodeMap.html" rel="nofollow" shape="rect" title="http://www.xulplanet.com/references/objref/NamedNodeMap.html">[1]</a></code></td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="DOM" shape="rect" title="DOM">All</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.childNodes" shape="rect" title="DOM:element.childNodes">childNodes</a></code></td>
* <td colspan="1" rowspan="1">All child nodes of an element.</td>
* <td colspan="1" rowspan="1"><code><a href="http://developer.mozilla.org/en/docs/index.php?title=NodeList&amp;action=edit" shape="rect" title="DOM:NodeList">NodeList</a><a href="http://www.xulplanet.com/references/objref/NodeList.html" rel="nofollow" shape="rect" title="http://www.xulplanet.com/references/objref/NodeList.html">[2]</a></code></td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="DOM" shape="rect" title="DOM">All</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.className" shape="rect" title="DOM:element.className">className</a></code></td>
* <td colspan="1" rowspan="1">Gets/sets the class of the element.</td>
* <td colspan="1" rowspan="1"><code><a href="http://developer.mozilla.org/en/docs/Global_Objects:String" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String">String</a></code></td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="http://developer.mozilla.org/en/docs/HTML" shape="rect" title="HTML">HTML</a>, <a href="http://developer.mozilla.org/en/docs/XUL" shape="rect" title="XUL">XUL</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.clientHeight" shape="rect" title="DOM:element.clientHeight">clientHeight</a></code></td>
* <td colspan="1" rowspan="1">The inner height of an element.</td>
* <td colspan="1" rowspan="1"><code><a href="http://developer.mozilla.org/en/docs/Global_Objects:Number" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Number">Number</a></code></td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="http://developer.mozilla.org/en/docs/HTML" shape="rect" title="HTML">HTML</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.clientLeft" shape="rect" title="DOM:element.clientLeft">clientLeft</a></code></td>
* <td colspan="1" rowspan="1">The width of the left border of an element.</td>
* <td colspan="1" rowspan="1"><code><a href="http://developer.mozilla.org/en/docs/Global_Objects:Number" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Number">Number</a></code></td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="http://developer.mozilla.org/en/docs/HTML" shape="rect" title="HTML">HTML</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.clientTop" shape="rect" title="DOM:element.clientTop">clientTop</a></code></td>
* <td colspan="1" rowspan="1">The width of the top border of an element.</td>
* <td colspan="1" rowspan="1"><code><a href="http://developer.mozilla.org/en/docs/Global_Objects:Number" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Number">Number</a></code></td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="http://developer.mozilla.org/en/docs/HTML" shape="rect" title="HTML">HTML</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.clientWidth" shape="rect" title="DOM:element.clientWidth">clientWidth</a></code></td>
* <td colspan="1" rowspan="1">The inner width of an element.</td>
* <td colspan="1" rowspan="1"><code><a href="http://developer.mozilla.org/en/docs/Global_Objects:Number" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Number">Number</a></code></td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="http://developer.mozilla.org/en/docs/HTML" shape="rect" title="HTML">HTML</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.dir" shape="rect" title="DOM:element.dir">dir</a></code></td>
* <td colspan="1" rowspan="1">Gets/sets the directionality of the element.</td>
* <td colspan="1" rowspan="1"><code><a href="http://developer.mozilla.org/en/docs/Global_Objects:String" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String">String</a></code></td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="http://developer.mozilla.org/en/docs/HTML" shape="rect" title="HTML">HTML</a>, <a href="http://developer.mozilla.org/en/docs/XUL" shape="rect" title="XUL">XUL</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.firstChild" shape="rect" title="DOM:element.firstChild">firstChild</a></code></td>
* <td colspan="1" rowspan="1">The first direct child node of an element, or <code>null</code> if this element has no child nodes.</td>
* <td colspan="1" rowspan="1"><code><a href="Node" shape="rect" title="DOM:Node">Node</a></code></td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="DOM" shape="rect" title="DOM">All</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.id" shape="rect" title="DOM:element.id">id</a></code></td>
* <td colspan="1" rowspan="1">Gets/sets the id of the element.</td>
* <td colspan="1" rowspan="1"><code><a href="http://developer.mozilla.org/en/docs/Global_Objects:String" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String">String</a></code></td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="http://developer.mozilla.org/en/docs/HTML" shape="rect" title="HTML">HTML</a>, <a href="http://developer.mozilla.org/en/docs/XUL" shape="rect" title="XUL">XUL</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.innerHTML" shape="rect" title="DOM:element.innerHTML">innerHTML</a></code></td>
* <td colspan="1" rowspan="1">Gets/sets the markup and content of the element.</td>
* <td colspan="1" rowspan="1"><code><a href="http://developer.mozilla.org/en/docs/Global_Objects:String" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String">String</a></code></td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="http://developer.mozilla.org/en/docs/HTML" shape="rect" title="HTML">HTML</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.lang" shape="rect" title="DOM:element.lang">lang</a></code></td>
* <td colspan="1" rowspan="1">Gets/sets the language of an element's attributes, text, and element contents.</td>
* <td colspan="1" rowspan="1"><code><a href="http://developer.mozilla.org/en/docs/Global_Objects:String" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String">String</a></code></td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="http://developer.mozilla.org/en/docs/HTML" shape="rect" title="HTML">HTML</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.lastChild" shape="rect" title="DOM:element.lastChild">lastChild</a></code></td>
* <td colspan="1" rowspan="1">The last direct child node of an element, or <code>null</code> if this element has no child nodes.</td>
* <td colspan="1" rowspan="1"><code><a href="Node" shape="rect" title="DOM:Node">Node</a></code></td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="DOM" shape="rect" title="DOM">All</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.localName" shape="rect" title="DOM:element.localName">localName</a></code></td>
* <td colspan="1" rowspan="1">The local part of the qualified name of an element.</td>
* <td colspan="1" rowspan="1"><code><a href="http://developer.mozilla.org/en/docs/Global_Objects:String" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String">String</a></code></td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="DOM" shape="rect" title="DOM">All</a></small></td>
* </tr>
* <tr>
* <th colspan="1" rowspan="1">Name</th>
* <th colspan="1" rowspan="1">Description</th>
* <th colspan="1" rowspan="1">Type</th>
* <th colspan="1" rowspan="1">Availability</th>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.name" shape="rect" title="DOM:element.name">name</a></code></td>
* <td colspan="1" rowspan="1">Gets/sets the name attribute of an element.</td>
* <td colspan="1" rowspan="1"><code><a href="http://developer.mozilla.org/en/docs/Global_Objects:String" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String">String</a></code></td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="http://developer.mozilla.org/en/docs/HTML" shape="rect" title="HTML">HTML</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.namespaceURI" shape="rect" title="DOM:element.namespaceURI">namespaceURI</a></code></td>
* <td colspan="1" rowspan="1">The namespace URI of this node, or <code>null</code> if it is unspecified.</td>
* <td colspan="1" rowspan="1"><code><a href="http://developer.mozilla.org/en/docs/Global_Objects:String" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String">String</a></code></td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="DOM" shape="rect" title="DOM">All</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.nextSibling" shape="rect" title="DOM:element.nextSibling">nextSibling</a></code></td>
* <td colspan="1" rowspan="1">The node immediately following the given one in the tree, or <code>null</code> if there is no sibling node.</td>
* <td colspan="1" rowspan="1"><code><a href="Node" shape="rect" title="DOM:Node">Node</a></code></td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="DOM" shape="rect" title="DOM">All</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.nodeName" shape="rect" title="DOM:element.nodeName">nodeName</a></code></td>
* <td colspan="1" rowspan="1">The name of the node.</td>
* <td colspan="1" rowspan="1"><code><a href="http://developer.mozilla.org/en/docs/Global_Objects:String" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String">String</a></code></td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="DOM" shape="rect" title="DOM">All</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.nodeType" shape="rect" title="DOM:element.nodeType">nodeType</a></code></td>
* <td colspan="1" rowspan="1">A number representing the type of the node. Is always equal to <code>1</code> for DOM elements.</td>
* <td colspan="1" rowspan="1"><code><a href="http://developer.mozilla.org/en/docs/Global_Objects:Number" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Number">Number</a></code></td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="DOM" shape="rect" title="DOM">All</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.nodeValue" shape="rect" title="DOM:element.nodeValue">nodeValue</a></code></td>
* <td colspan="1" rowspan="1">The value of the node. Is always equal to <code>null</code> for DOM elements.</td>
* <td colspan="1" rowspan="1"><code><a href="http://developer.mozilla.org/en/docs/Global_Objects:String" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String">String</a></code></td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="DOM" shape="rect" title="DOM">All</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.offsetHeight" shape="rect" title="DOM:element.offsetHeight">offsetHeight</a></code></td>
* <td colspan="1" rowspan="1">The height of an element, relative to the layout.</td>
* <td colspan="1" rowspan="1"><code><a href="http://developer.mozilla.org/en/docs/Global_Objects:Number" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Number">Number</a></code></td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="http://developer.mozilla.org/en/docs/HTML" shape="rect" title="HTML">HTML</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.offsetLeft" shape="rect" title="DOM:element.offsetLeft">offsetLeft</a></code></td>
* <td colspan="1" rowspan="1">The distance from this element's left border to its <code>offsetParent</code>'s left border.</td>
* <td colspan="1" rowspan="1"><code><a href="http://developer.mozilla.org/en/docs/Global_Objects:Number" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Number">Number</a></code></td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="http://developer.mozilla.org/en/docs/HTML" shape="rect" title="HTML">HTML</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.offsetParent" shape="rect" title="DOM:element.offsetParent">offsetParent</a></code></td>
* <td colspan="1" rowspan="1">The element from which all offset calculations are currently computed.</td>
* <td colspan="1" rowspan="1"><code><strong>Element</strong></code></td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="http://developer.mozilla.org/en/docs/HTML" shape="rect" title="HTML">HTML</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.offsetTop" shape="rect" title="DOM:element.offsetTop">offsetTop</a></code></td>
* <td colspan="1" rowspan="1">The distance from this element's top border to its <code>offsetParent</code>'s top border.</td>
* <td colspan="1" rowspan="1"><code><a href="http://developer.mozilla.org/en/docs/Global_Objects:Number" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Number">Number</a></code></td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="http://developer.mozilla.org/en/docs/HTML" shape="rect" title="HTML">HTML</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.offsetWidth" shape="rect" title="DOM:element.offsetWidth">offsetWidth</a></code></td>
* <td colspan="1" rowspan="1">The width of an element, relative to the layout.</td>
* <td colspan="1" rowspan="1"><code><a href="http://developer.mozilla.org/en/docs/Global_Objects:Number" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Number">Number</a></code></td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="http://developer.mozilla.org/en/docs/HTML" shape="rect" title="HTML">HTML</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.ownerDocument" shape="rect" title="DOM:element.ownerDocument">ownerDocument</a></code></td>
* <td colspan="1" rowspan="1">The document that this node is in, or <code>null</code> if the node is not inside of one.</td>
* <td colspan="1" rowspan="1"><code><a href="document" shape="rect" title="DOM:document">Document</a></code></td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="DOM" shape="rect" title="DOM">All</a></small></td>
* </tr>
* <tr>
* <th colspan="1" rowspan="1">Name</th>
* <th colspan="1" rowspan="1">Description</th>
* <th colspan="1" rowspan="1">Type</th>
* <th colspan="1" rowspan="1">Availability</th>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.parentNode" shape="rect" title="DOM:element.parentNode">parentNode</a></code></td>
* <td colspan="1" rowspan="1">The parent element of this node, or <code>null</code> if the node is not inside of a <a href="document" shape="rect" title="DOM:document">DOM Document</a>.</td>
* <td colspan="1" rowspan="1"><code><a href="Node" shape="rect" title="DOM:Node">Node</a></code></td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="DOM" shape="rect" title="DOM">All</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.prefix" shape="rect" title="DOM:element.prefix">prefix</a></code></td>
* <td colspan="1" rowspan="1">The namespace prefix of the node, or <code>null</code> if no prefix is specified.</td>
* <td colspan="1" rowspan="1"><code><a href="http://developer.mozilla.org/en/docs/Global_Objects:String" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String">String</a></code></td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="DOM" shape="rect" title="DOM">All</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.previousSibling" shape="rect" title="DOM:element.previousSibling">previousSibling</a></code></td>
* <td colspan="1" rowspan="1">The node immediately preceding the given one in the tree, or <code>null</code> if there is no sibling node.</td>
* <td colspan="1" rowspan="1"><code><a href="Node" shape="rect" title="DOM:Node">Node</a></code></td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="DOM" shape="rect" title="DOM">All</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.scrollHeight" shape="rect" title="DOM:element.scrollHeight">scrollHeight</a></code></td>
* <td colspan="1" rowspan="1">The scroll view height of an element.</td>
* <td colspan="1" rowspan="1"><code><a href="http://developer.mozilla.org/en/docs/Global_Objects:Number" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Number">Number</a></code></td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="http://developer.mozilla.org/en/docs/HTML" shape="rect" title="HTML">HTML</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.scrollLeft" shape="rect" title="DOM:element.scrollLeft">scrollLeft</a></code></td>
* <td colspan="1" rowspan="1">Gets/sets the left scroll offset of an element.</td>
* <td colspan="1" rowspan="1"><code><a href="http://developer.mozilla.org/en/docs/Global_Objects:Number" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Number">Number</a></code></td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="http://developer.mozilla.org/en/docs/HTML" shape="rect" title="HTML">HTML</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.scrollTop" shape="rect" title="DOM:element.scrollTop">scrollTop</a></code></td>
* <td colspan="1" rowspan="1">Gets/sets the top scroll offset of an element.</td>
* <td colspan="1" rowspan="1"><code><a href="http://developer.mozilla.org/en/docs/Global_Objects:Number" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Number">Number</a></code></td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="http://developer.mozilla.org/en/docs/HTML" shape="rect" title="HTML">HTML</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.scrollWidth" shape="rect" title="DOM:element.scrollWidth">scrollWidth</a></code></td>
* <td colspan="1" rowspan="1">The scroll view width of an element.</td>
* <td colspan="1" rowspan="1"><code><a href="http://developer.mozilla.org/en/docs/Global_Objects:Number" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Number">Number</a></code></td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="http://developer.mozilla.org/en/docs/HTML" shape="rect" title="HTML">HTML</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.style" shape="rect" title="DOM:element.style">style</a></code></td>
* <td colspan="1" rowspan="1">An object representing the declarations of an element's style attributes.</td>
* <td colspan="1" rowspan="1"><code><a href="http://developer.mozilla.org/en/docs/index.php?title=CSSStyleDeclaration&amp;action=edit" shape="rect" title="DOM:CSSStyleDeclaration">CSSStyleDeclaration</a><a href="http://www.xulplanet.com/references/objref/CSSStyleDeclaration.html" rel="nofollow" shape="rect" title="http://www.xulplanet.com/references/objref/CSSStyleDeclaration.html">[3]</a></code></td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="http://developer.mozilla.org/en/docs/HTML" shape="rect" title="HTML">HTML</a>, <a href="http://developer.mozilla.org/en/docs/XUL" shape="rect" title="XUL">XUL</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.tabIndex" shape="rect" title="DOM:element.tabIndex">tabIndex</a></code></td>
* <td colspan="1" rowspan="1">Gets/sets the position of the element in the tabbing order.</td>
* <td colspan="1" rowspan="1"><code><a href="http://developer.mozilla.org/en/docs/Global_Objects:Number" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Number">Number</a></code></td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="http://developer.mozilla.org/en/docs/HTML" shape="rect" title="HTML">HTML</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.tagName" shape="rect" title="DOM:element.tagName">tagName</a></code></td>
* <td colspan="1" rowspan="1">The name of the tag for the given element.</td>
* <td colspan="1" rowspan="1"><code><a href="http://developer.mozilla.org/en/docs/Global_Objects:String" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String">String</a></code></td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="DOM" shape="rect" title="DOM">All</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.textContent" shape="rect" title="DOM:element.textContent">textContent</a></code></td>
* <td colspan="1" rowspan="1">Gets/sets the textual contents of an element and all its descendants.</td>
* <td colspan="1" rowspan="1"><code><a href="http://developer.mozilla.org/en/docs/Global_Objects:String" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String">String</a></code></td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="DOM" shape="rect" title="DOM">All</a></small></td>
* </tr>
* </table>
* <h2> <span> Methods </span></h2>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <th colspan="1" rowspan="1">Name &amp; Description</th>
* <th colspan="1" rowspan="1">Return</th>
* <th colspan="1" rowspan="1">Availability</th>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="DOM:element.addEventListener" shape="rect" title="DOM:element.addEventListener">addEventListener</a>( <a href="http://developer.mozilla.org/en/docs/Global_Objects:String" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String">type</a>, <a href="http://developer.mozilla.org/en/docs/Global_Objects:Function" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Function">listener</a>, <a href="http://developer.mozilla.org/en/docs/Global_Objects:Boolean" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Boolean">useCapture</a> )</code>
* Register an event handler to a specific event type on the element.</td>
* <td colspan="1" rowspan="1">-</td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="event" shape="rect" title="DOM:event">All</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.appendChild" shape="rect" title="DOM:element.appendChild">appendChild</a>( <a href="Node" shape="rect" title="DOM:Node">appendedNode</a> )</code>
* Insert a node as the last child node of this element.</td>
* <td colspan="1" rowspan="1"><a href="Node" shape="rect" title="DOM:Node">Node</a></td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="DOM" shape="rect" title="DOM">All</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.blur" shape="rect" title="DOM:element.blur">blur</a>()</code>
* Removes keyboard focus from the current element.</td>
* <td colspan="1" rowspan="1">-</td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="http://developer.mozilla.org/en/docs/HTML" shape="rect" title="HTML">HTML</a>, <a href="http://developer.mozilla.org/en/docs/XUL" shape="rect" title="XUL">XUL</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.click" shape="rect" title="DOM:element.click">click</a>()</code>
* Simulates a click on the current element.</td>
* <td colspan="1" rowspan="1">-</td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="http://developer.mozilla.org/en/docs/HTML" shape="rect" title="HTML">HTML</a>, <a href="http://developer.mozilla.org/en/docs/XUL" shape="rect" title="XUL">XUL</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.cloneNode" shape="rect" title="DOM:element.cloneNode">cloneNode</a>( <a href="http://developer.mozilla.org/en/docs/Global_Objects:Boolean" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Boolean">deep</a> )</code>
* Clone a node, and optionally, all of its contents.</td>
* <td colspan="1" rowspan="1"><a href="Node" shape="rect" title="DOM:Node">Node</a></td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="DOM" shape="rect" title="DOM">All</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.dispatchEvent" shape="rect" title="DOM:element.dispatchEvent">dispatchEvent</a>( <a href="event" shape="rect" title="DOM:event">event</a> )</code>
* Dispatch an event to this node in the DOM.</td>
* <td colspan="1" rowspan="1"><a href="http://developer.mozilla.org/en/docs/Global_Objects:Boolean" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Boolean">Boolean</a></td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="DOM" shape="rect" title="DOM">All</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.focus" shape="rect" title="DOM:element.focus">focus</a>()</code>
* Gives keyboard focus to the current element.</td>
* <td colspan="1" rowspan="1">-</td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="http://developer.mozilla.org/en/docs/HTML" shape="rect" title="HTML">HTML</a>, <a href="http://developer.mozilla.org/en/docs/XUL" shape="rect" title="XUL">XUL</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.getAttribute" shape="rect" title="DOM:element.getAttribute">getAttribute</a>( <a href="http://developer.mozilla.org/en/docs/Global_Objects:String" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String">name</a> )</code>
* Retrieve the value of the named attribute from the current node.</td>
* <td colspan="1" rowspan="1"><a href="http://developer.mozilla.org/en/docs/Global_Objects:Object" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object">Object</a></td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="DOM" shape="rect" title="DOM">All</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.getAttributeNS" shape="rect" title="DOM:element.getAttributeNS">getAttributeNS</a>( <a href="http://developer.mozilla.org/en/docs/Global_Objects:String" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String">namespace</a>, <a href="http://developer.mozilla.org/en/docs/Global_Objects:String" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String">name</a> )</code>
* Retrieve the value of the attribute with the specified name and namespace, from the current node.</td>
* <td colspan="1" rowspan="1"><a href="http://developer.mozilla.org/en/docs/Global_Objects:Object" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object">Object</a></td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="DOM" shape="rect" title="DOM">All</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.getAttributeNode" shape="rect" title="DOM:element.getAttributeNode">getAttributeNode</a>( <a href="http://developer.mozilla.org/en/docs/Global_Objects:String" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String">name</a> )</code>
* Retrieve the node representation of the named attribute from the current node.</td>
* <td colspan="1" rowspan="1"><a href="http://developer.mozilla.org/en/docs/index.php?title=Attr&amp;action=edit" shape="rect" title="DOM:Attr">Attr</a></td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="DOM" shape="rect" title="DOM">All</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.getAttributeNodeNS" shape="rect" title="DOM:element.getAttributeNodeNS">getAttributeNodeNS</a>( <a href="http://developer.mozilla.org/en/docs/Global_Objects:String" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String">namespace</a>, <a href="http://developer.mozilla.org/en/docs/Global_Objects:String" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String">name</a> )</code>
* Retrieve the node representation of the attribute with the specified name and namespace, from the current node.</td>
* <td colspan="1" rowspan="1"><a href="http://developer.mozilla.org/en/docs/index.php?title=Attr&amp;action=edit" shape="rect" title="DOM:Attr">Attr</a></td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="DOM" shape="rect" title="DOM">All</a></small></td>
* </tr>
* <tr>
* <th colspan="1" rowspan="1">Name &amp; Description</th>
* <th colspan="1" rowspan="1">Return</th>
* <th colspan="1" rowspan="1">Availability</th>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.getElementsByTagName" shape="rect" title="DOM:element.getElementsByTagName">getElementsByTagName</a>( <a href="http://developer.mozilla.org/en/docs/Global_Objects:String" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String">name</a> )</code>
* Retrieve a set of all descendant elements, of a particular tag name, from the current element.</td>
* <td colspan="1" rowspan="1"><a href="http://developer.mozilla.org/en/docs/index.php?title=NodeSet&amp;action=edit" shape="rect" title="DOM:NodeSet">NodeSet</a></td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="DOM" shape="rect" title="DOM">All</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.getElementsByTagNameNS" shape="rect" title="DOM:element.getElementsByTagNameNS">getElementsByTagNameNS</a>( <a href="http://developer.mozilla.org/en/docs/Global_Objects:String" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String">namespace</a>, <a href="http://developer.mozilla.org/en/docs/Global_Objects:String" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String">name</a> )</code>
* Retrieve a set of all descendant elements, of a particular tag name and namespace, from the current element.</td>
* <td colspan="1" rowspan="1"><a href="http://developer.mozilla.org/en/docs/index.php?title=NodeSet&amp;action=edit" shape="rect" title="DOM:NodeSet">NodeSet</a></td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="DOM" shape="rect" title="DOM">All</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.hasAttribute" shape="rect" title="DOM:element.hasAttribute">hasAttribute</a>( <a href="http://developer.mozilla.org/en/docs/Global_Objects:String" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String">name</a> )</code>
* Check if the element has the specified attribute, or not.</td>
* <td colspan="1" rowspan="1"><a href="http://developer.mozilla.org/en/docs/Global_Objects:Boolean" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Boolean">Boolean</a></td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="DOM" shape="rect" title="DOM">All</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.hasAttributeNS" shape="rect" title="DOM:element.hasAttributeNS">hasAttributeNS</a>( <a href="http://developer.mozilla.org/en/docs/Global_Objects:String" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String">namespace</a>, <a href="http://developer.mozilla.org/en/docs/Global_Objects:String" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String">name</a> )</code>
* Check if the element has the specified attribute, in the specified namespace, or not.</td>
* <td colspan="1" rowspan="1"><a href="http://developer.mozilla.org/en/docs/Global_Objects:Boolean" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Boolean">Boolean</a></td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="DOM" shape="rect" title="DOM">All</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.hasAttributes" shape="rect" title="DOM:element.hasAttributes">hasAttributes</a>()</code>
* Check if the element has any attributes, or not.</td>
* <td colspan="1" rowspan="1"><a href="http://developer.mozilla.org/en/docs/Global_Objects:Boolean" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Boolean">Boolean</a></td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="DOM" shape="rect" title="DOM">All</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.hasChildNodes" shape="rect" title="DOM:element.hasChildNodes">hasChildNodes</a>()</code>
* Check if the element has any child nodes, or not.</td>
* <td colspan="1" rowspan="1"><a href="http://developer.mozilla.org/en/docs/Global_Objects:Boolean" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Boolean">Boolean</a></td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="DOM" shape="rect" title="DOM">All</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.insertBefore" shape="rect" title="DOM:element.insertBefore">insertBefore</a>( <a href="Node" shape="rect" title="DOM:Node">insertedNode</a>, <a href="Node" shape="rect" title="DOM:Node">adjacentNode</a> )</code>
* Inserts the first node before the second, child, Node in the DOM.</td>
* <td colspan="1" rowspan="1"><a href="Node" shape="rect" title="DOM:Node">Node</a></td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="DOM" shape="rect" title="DOM">All</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.normalize" shape="rect" title="DOM:element.normalize">normalize</a>()</code>
* Clean up all the text nodes under this element (merge adjacent, remove empty).</td>
* <td colspan="1" rowspan="1">-</td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="DOM" shape="rect" title="DOM">All</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.removeAttribute" shape="rect" title="DOM:element.removeAttribute">removeAttribute</a>( <a href="http://developer.mozilla.org/en/docs/Global_Objects:String" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String">name</a> )</code>
* Remove the named attribute from the current node.</td>
* <td colspan="1" rowspan="1">-</td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="DOM" shape="rect" title="DOM">All</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.removeAttributeNS" shape="rect" title="DOM:element.removeAttributeNS">removeAttributeNS</a>( <a href="http://developer.mozilla.org/en/docs/Global_Objects:String" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String">namespace</a>, <a href="http://developer.mozilla.org/en/docs/Global_Objects:String" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String">name</a> )</code>
* Remove the attribute with the specified name and namespace, from the current node.</td>
* <td colspan="1" rowspan="1">-</td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="DOM" shape="rect" title="DOM">All</a></small></td>
* </tr>
* <tr>
* <th colspan="1" rowspan="1">Name &amp; Description</th>
* <th colspan="1" rowspan="1">Return</th>
* <th colspan="1" rowspan="1">Availability</th>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.removeAttributeNode" shape="rect" title="DOM:element.removeAttributeNode">removeAttributeNode</a>( <a href="http://developer.mozilla.org/en/docs/Global_Objects:String" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String">name</a> )</code>
* Remove the node representation of the named attribute from the current node.</td>
* <td colspan="1" rowspan="1">-</td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="DOM" shape="rect" title="DOM">All</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.removeChild" shape="rect" title="DOM:element.removeChild">removeChild</a>( <a href="Node" shape="rect" title="DOM:Node">removedNode</a> )</code>
* Removes a child node from the current element.</td>
* <td colspan="1" rowspan="1"><a href="Node" shape="rect" title="DOM:Node">Node</a></td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="DOM" shape="rect" title="DOM">All</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.removeEventListener" shape="rect" title="DOM:element.removeEventListener">removeEventListener</a>( <a href="http://developer.mozilla.org/en/docs/Global_Objects:String" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String">type</a>, <a href="http://developer.mozilla.org/en/docs/Global_Objects:Function" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Function">handler</a>, <a href="http://developer.mozilla.org/en/docs/Global_Objects:Boolean" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Boolean">useCapture</a> )</code>
* Removes an event listener from the element.</td>
* <td colspan="1" rowspan="1">-</td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="event" shape="rect" title="DOM:event">All</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.replaceChild" shape="rect" title="DOM:element.replaceChild">replaceChild</a>( <a href="Node" shape="rect" title="DOM:Node">insertedNode</a>, <a href="Node" shape="rect" title="DOM:Node">replacedNode</a> )</code>
* Replaces one child node in the current element with another.</td>
* <td colspan="1" rowspan="1"><a href="Node" shape="rect" title="DOM:Node">Node</a></td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="DOM" shape="rect" title="DOM">All</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.scrollIntoView" shape="rect" title="DOM:element.scrollIntoView">scrollIntoView</a>( <a href="http://developer.mozilla.org/en/docs/Global_Objects:Boolean" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Boolean">alignWithTop</a> )</code>
* Scrolls the page until the element gets into the view.</td>
* <td colspan="1" rowspan="1">-</td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="http://developer.mozilla.org/en/docs/HTML" shape="rect" title="HTML">HTML</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.setAttribute" shape="rect" title="DOM:element.setAttribute">setAttribute</a>( <a href="http://developer.mozilla.org/en/docs/Global_Objects:String" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String">name</a>, <a href="http://developer.mozilla.org/en/docs/Global_Objects:Object" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object">value</a> )</code>
* Set the value of the named attribute from the current node.</td>
* <td colspan="1" rowspan="1">-</td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="DOM" shape="rect" title="DOM">All</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.setAttributeNS" shape="rect" title="DOM:element.setAttributeNS">setAttributeNS</a>( <a href="http://developer.mozilla.org/en/docs/Global_Objects:String" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String">namespace</a>, <a href="http://developer.mozilla.org/en/docs/Global_Objects:String" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String">name</a>, <a href="http://developer.mozilla.org/en/docs/Global_Objects:Object" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object">value</a> )</code>
* Set the value of the attribute with the specified name and namespace, from the current node.</td>
* <td colspan="1" rowspan="1">-</td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="DOM" shape="rect" title="DOM">All</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.setAttributeNode" shape="rect" title="DOM:element.setAttributeNode">setAttributeNode</a>( <a href="http://developer.mozilla.org/en/docs/Global_Objects:String" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String">name</a>, <a href="http://developer.mozilla.org/en/docs/index.php?title=Attr&amp;action=edit" shape="rect" title="DOM:Attr">attrNode</a> )</code>
* Set the node representation of the named attribute from the current node.</td>
* <td colspan="1" rowspan="1">-</td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="DOM" shape="rect" title="DOM">All</a></small></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code><a href="element.setAttributeNodeNS" shape="rect" title="DOM:element.setAttributeNodeNS">setAttributeNodeNS</a>( <a href="http://developer.mozilla.org/en/docs/Global_Objects:String" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String">namespace</a>, <a href="http://developer.mozilla.org/en/docs/Global_Objects:String" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String">name</a>, <a href="http://developer.mozilla.org/en/docs/index.php?title=Attr&amp;action=edit" shape="rect" title="DOM:Attr">attrNode</a> )</code>
* Set the node representation of the attribute with the specified name and namespace, from the current node.</td>
* <td colspan="1" rowspan="1">-</td>
* <td colspan="1" nowrap="nowrap" rowspan="1"><small><a href="DOM" shape="rect" title="DOM">All</a></small></td>
* </tr>
* </table>
* <h2> <span> Event Handlers </span></h2>
* <p>These are properties that correspond to the HTML 'on' event attributes.
* </p><p>Unlike the corresponding attributes, the values of these properties are functions (or any other object implementing the <a href="http://www.w3.org/TR/DOM-Level-2-Events/events.html#Events-EventListener" rel="nofollow" shape="rect" title="http://www.w3.org/TR/DOM-Level-2-Events/events.html#Events-EventListener">EventListener</a> interface) rather than a string. In fact, assigning an event attribute in HTML creates a wrapper function around the specified code. For example, given the following HTML:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* &lt;div onclick="foo();"&gt;click me!&lt;/div&gt;
* </pre>
* <p>If <code>element</code> is a reference to this <code>div</code>, the value of <code>element.onclick</code> is effectively:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* function onclick(event) {
* foo();
* }
* </pre>
* <p>Note how the <a href="event" shape="rect" title="DOM:event">event</a> object is passed as parameter <code>event</code> to this wrapper function.
* </p>
* <dl><dt style="font-weight:bold"> <a href="element.onblur" shape="rect" title="DOM:element.onblur">onblur</a>
* </dt><dd> Returns the event handling code for the blur event.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="element.onchange" shape="rect" title="DOM:element.onchange">onchange</a>
* </dt><dd> Returns the event handling code for the change event.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="element.onclick" shape="rect" title="DOM:element.onclick">onclick</a>
* </dt><dd> Returns the event handling code for the click event.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="element.ondblclick" shape="rect" title="DOM:element.ondblclick">ondblclick</a>
* </dt><dd> Returns the event handling code for the dblclick event.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="element.onfocus" shape="rect" title="DOM:element.onfocus">onfocus</a>
* </dt><dd> Returns the event handling code for the focus event.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="element.onkeydown" shape="rect" title="DOM:element.onkeydown">onkeydown</a>
* </dt><dd> Returns the event handling code for the keydown event.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="element.onkeypress" shape="rect" title="DOM:element.onkeypress">onkeypress</a>
* </dt><dd> Returns the event handling code for the keypress event.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="element.onkeyup" shape="rect" title="DOM:element.onkeyup">onkeyup</a>
* </dt><dd> Returns the event handling code for the keyup event.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="element.onmousedown" shape="rect" title="DOM:element.onmousedown">onmousedown</a>
* </dt><dd> Returns the event handling code for the mousedown event.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="element.onmousemove" shape="rect" title="DOM:element.onmousemove">onmousemove</a>
* </dt><dd> Returns the event handling code for the mousemove event.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="element.onmouseout" shape="rect" title="DOM:element.onmouseout">onmouseout</a>
* </dt><dd> Returns the event handling code for the mouseout event.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="element.onmouseover" shape="rect" title="DOM:element.onmouseover">onmouseover</a>
* </dt><dd> Returns the event handling code for the mouseover event.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="element.onmouseup" shape="rect" title="DOM:element.onmouseup">onmouseup</a>
* </dt><dd> Returns the event handling code for the mouseup event.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="element.onresize" shape="rect" title="DOM:element.onresize">onresize</a>
* </dt><dd> Returns the event handling code for the resize event.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <a href="element.onscroll" shape="rect" title="DOM:element.onscroll">onscroll</a>
* </dt><dd> Returns the event handling code for the scroll event.
* </dd></dl>
* <p>
* </p>
* <h2> <span> Other Events </span></h2>
* <p>There are also other <a href="DOM_Events" shape="rect" title="DOM Events">DOM Events</a> like
* <code>DOMSubtreeModified</code>, <code>DOMAttrModified</code> etc. as well as
* <a href="http://developer.mozilla.org/en/docs/Gecko-Specific_DOM_Events" shape="rect" title="Gecko-Specific DOM Events">Gecko-Specific DOM Events</a> like
* <code>DOMContentLoaded</code>, <code>DOMTitleChanged</code> etc.
* </p>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:Gecko_DOM_Reference" shape="rect" title="Category:Gecko DOM Reference">Gecko DOM Reference</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
var element = new Element();
