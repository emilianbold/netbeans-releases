/**
* <div id="contentSub">(Redirected from <a href="http://developer.mozilla.org/en/docs/index.php?title=Core_JavaScript_1.5_Reference:Objects:String&amp;redirect=no" shape="rect" title="Core JavaScript 1.5 Reference:Objects:String">Core JavaScript 1.5 Reference:Objects:String</a>)</div>
* 
* <h2> <span> Summary </span></h2>
* <p><b>Core Object</b>
* </p><p>An object representing a series of characters in a string.
* </p>
* <h2> <span> Created by </span></h2>
* <p>The String constructor:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">new String(<i>string</i>)
* </pre>
* <h2> <span> Parameters </span></h2>
* <dl><dt style="font-weight:bold"> <code>string</code>Ê</dt><dd> Any string.
* </dd></dl>
* <h2> <span> Description </span></h2>
* <p>String objects are created by calling the constructor <code>new String()</code>:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">s_obj = new String("foo"); // creates a String object
* </pre>
* <p>The <code>String</code> object wraps Javascript's string primitive data type with the methods described below. The global function <code>String()</code> can also be called without <code>new</code> in front to create a primitive string:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">s_prim = String("bar"); //creates a primitive string
* </pre>
* <p>Literal strings in Javascript source code create primitive strings:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">s_also_prim = "foo"; // creates a primitive string
* </pre>
* <p>Because Javascript automatically converts between string primitives and String objects, you can call any of the methods of the <code>String</code> object on a string primitive. JavaScript automatically converts the string primitive to a temporary <code>String</code> object, calls the method, then discards the temporary <code>String</code> object. For example, you can use the <code>String.length</code> property on a string primitive created from a string literal:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">s_obj.length;       // 3
* s_prim.length;      // 3
* s_also_prim.length; // 3
* 'foo'.length;       // 3
* "foo".length;       // 3
* </pre>
* <p>(A string literal can use single or double quotation marks.)
* </p><p>String objects can be converted to primitive strings with <code>String.valueOf()</code>.
* </p><p>String primitives and String objects give different results when evaluated as Javascript. Primitives are treated as source code; String objects are treated as a character sequence object. For example:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">s1 = "2 + 2" // creates a string primitive
* s2 = new String("2 + 2") // creates a String object
* eval(s1)     // returns the number 4
* eval(s2)     // returns the string "2 + 2"
* eval(s2.valueOf()); // returns the number 4
* </pre>
* <p>You can convert the value of any object into a string using the global <a href="String" shape="rect" title="Core JavaScript 1.5 Reference:Functions:String">String</a> function.
* </p>
* <h3> <span> Accessing individual characters in a string </span></h3>
* <p>There are two ways to access an individual character in a string. The first is the <a href="Core_JavaScript_1.5_Reference:Global_Objects:String:charAt" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:charAt">charAt</a> method:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">'cat'.charAt(1) // returns "a"
* </pre>
* <p>The other way is to treat the string as an array, where each index corresponds to an individual character:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">'cat'[1] // returns "a"
* </pre>
* <p>Note: The second way (treating the string as an array) is not part of the ECMAScript; it's a JavaScript feature.
* </p><p>In both cases, attempting to set an individual character won't work. Trying to set a character through <code>charAt</code> results in an error:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var x = 'cat';
* x.charAt(0) = 'b'; // error
* </pre>
* <p>Setting a character via indexing does not throw an error, but the string itself is unchanged:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var x = 'cat';
* x[2] = 'p';
* alert(x);    // still outputs 'cat'
* alert(x[2]); // still outputs 't'
* </pre>
* <h3> <span> Comparing strings </span></h3>
* <p>C developers have the <code>strcmp()</code> function for comparing strings. In JavaScript, you just use the less-than and greater-than operators:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var a = "a";
* var b = "b";
* if (a &lt; b) // true
* document.write(a + " is less than " + b);
* else if (a &gt; b)
* document.write(a + " is greater than " + b);
* else
* document.write(a + " and " + b + " are equal.");
* </pre>
* <h2> <span> Properties </span></h2>
* <ul><li> <a href="String:constructor" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:constructor">constructor</a>: Specifies the function that creates an object's prototype.
* </li><li> <a href="String:length" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:length">length</a>: Reflects the length of the string.
* </li><li> <a href="String:prototype" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:prototype">prototype</a>: Allows the addition of properties to a String object.
* </li></ul>
* <h2> <span> Static methods </span></h2>
* <ul><li> <a href="Core_JavaScript_1.5_Reference:Global_Objects:String:fromCharCode" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:fromCharCode">fromCharCode</a>: Returns a string created by using the specified sequence of Unicode values.
* </li></ul>
* <h2> <span> Methods </span></h2>
* <h3> <span> Methods unrelated to HTML </span></h3>
* <ul><li> <a href="String:charAt" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:charAt">charAt</a>: Returns the character at the specified index.
* </li><li> <a href="String:charCodeAt" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:charCodeAt">charCodeAt</a>: Returns a number indicating the Unicode value of the character at the given index.
* </li><li> <a href="String:concat" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:concat">concat</a>: Combines the text of two strings and returns a new string.
* </li><li> <a href="String:indexOf" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:indexOf">indexOf</a>: Returns the index within the calling String object of the first occurrence of the specified value, or -1 if not found.
* </li><li> <a href="String:lastIndexOf" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:lastIndexOf">lastIndexOf</a>: Returns the index within the calling String object of the last occurrence of the specified value, or -1 if not found.
* </li><li> <a href="String:match" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:match">match</a>: Used to match a regular expression against a string.
* </li><li> <a href="String:replace" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:replace">replace</a>: Used to find a match between a regular expression and a string, and to replace the matched substring with a new substring.
* </li><li> <a href="String:search" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:search">search</a>: Executes the search for a match between a regular expression and a specified string.
* </li><li> <a href="String:slice" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:slice">slice</a>: Extracts a section of a string and returns a new string.
* </li><li> <a href="String:split" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:split">split</a>: Splits a String object into an array of strings by separating the string into substrings.
* </li><li> <a href="String:substr" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:substr">substr</a>: Returns the characters in a string beginning at the specified location through the specified number of characters.
* </li><li> <a href="String:substring" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:substring">substring</a>: Returns the characters in a string between two indexes into the string.
* </li><li> <a href="String:toLowerCase" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:toLowerCase">toLowerCase</a>: Returns the calling string value converted to lowercase.
* </li><li> <a href="String:toSource" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:toSource">toSource</a>: Returns an object literal representing the specified object; you can use this value to create a new object. Overrides the <a href="Object:toSource" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object:toSource">Object.toSource</a> method.
* </li><li> <a href="String:toString" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:toString">toString</a>: Returns a string representing the specified object. Overrides the <a href="Object:toString" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object:toString">Object.toString</a> method.
* </li><li> <a href="String:toUpperCase" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:toUpperCase">toUpperCase</a>: Returns the calling string value converted to uppercase.
* </li><li> <a href="String:valueOf" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:valueOf">valueOf</a>: Returns the primitive value of the specified object. Overrides the <a href="Object:valueOf" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object:valueOf">Object.valueOf</a> method.
* </li></ul>
* <h3> <span> HTML wrapper methods </span></h3>
* <div style="border: 1px solid #FFB752; background-color: #FEE3BC; font-weight: bold; text-align: center; padding: 0px 10px 0px 10px; margin: 10px 0px 10px 0px;"><p style="margin: 4px 0px 4px 0px;">Non-standard</p></div>
* <p>Each of the following methods returns a copy of the string wrapped inside an HTML tag. For example, "test".bold() returns "&lt;b&gt;test&lt;/b&gt;".
* </p>
* <ul><li> <a href="Core_JavaScript_1.5_Reference:Global_Objects:String:anchor" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:anchor">anchor</a>: <a href="http://www.w3.org/TR/html401/struct/links.html#adef-name-A" rel="nofollow" shape="rect" title="http://www.w3.org/TR/html401/struct/links.html#adef-name-A"><code>&lt;a name="<i>name</i>"&gt;</code></a> (hypertext target)
* </li><li> <a href="String:big" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:big">big</a>: <a href="http://www.w3.org/TR/html401/present/graphics.html#edef-BIG" rel="nofollow" shape="rect" title="http://www.w3.org/TR/html401/present/graphics.html#edef-BIG"><code>&lt;big&gt;</code></a>
* </li><li> <a href="String:blink" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:blink">blink</a>: <code>&lt;blink&gt;</code>
* </li><li> <a href="String:bold" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:bold">bold</a>: <a href="http://www.w3.org/TR/html401/present/graphics.html#edef-B" rel="nofollow" shape="rect" title="http://www.w3.org/TR/html401/present/graphics.html#edef-B"><code>&lt;b&gt;</code></a>
* </li><li> <a href="String:fixed" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:fixed">fixed</a>: <a href="http://www.w3.org/TR/html401/present/graphics.html#edef-TT" rel="nofollow" shape="rect" title="http://www.w3.org/TR/html401/present/graphics.html#edef-TT"><code>&lt;tt&gt;</code></a>
* </li><li> <a href="String:fontcolor" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:fontcolor">fontcolor</a>: <code>&lt;font color="<i>color</i>"&gt;</code>
* </li><li> <a href="String:fontsize" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:fontsize">fontsize</a>: <code>&lt;font size="<i>size</i>"&gt;</code>
* </li><li> <a href="String:italics" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:italics">italics</a>: <a href="http://www.w3.org/TR/html401/present/graphics.html#edef-I" rel="nofollow" shape="rect" title="http://www.w3.org/TR/html401/present/graphics.html#edef-I"><code>&lt;i&gt;</code></a>
* </li><li> <a href="String:link" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:link">link</a>: <a href="http://www.w3.org/TR/html401/struct/links.html#adef-href" rel="nofollow" shape="rect" title="http://www.w3.org/TR/html401/struct/links.html#adef-href"><code>&lt;a href="<i>url</i>"&gt;</code></a> (link to URL)
* </li><li> <a href="String:small" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:small">small</a>: <a href="http://www.w3.org/TR/html401/present/graphics.html#edef-SMALL" rel="nofollow" shape="rect" title="http://www.w3.org/TR/html401/present/graphics.html#edef-SMALL"><code>&lt;small&gt;</code></a>.
* </li><li> <a href="String:strike" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:strike">strike</a>: <a href="http://www.w3.org/TR/html401/present/graphics.html#edef-STRIKE" rel="nofollow" shape="rect" title="http://www.w3.org/TR/html401/present/graphics.html#edef-STRIKE"><code>&lt;strike&gt;</code></a>
* </li><li> <a href="String:sub" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:sub">sub</a>: <a href="http://www.w3.org/TR/html401/struct/text.html#edef-SUB" rel="nofollow" shape="rect" title="http://www.w3.org/TR/html401/struct/text.html#edef-SUB"><code>&lt;sub&gt;</code></a>
* </li><li> <a href="String:sup" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:sup">sup</a>: <a href="http://www.w3.org/TR/html401/struct/text.html#edef-SUP" rel="nofollow" shape="rect" title="http://www.w3.org/TR/html401/struct/text.html#edef-SUP"><code>&lt;sup&gt;</code></a>
* </li></ul>
* <p>These methods are of limited use, as they provide only a subset of the available HTML tags and attributes.
* </p>
* <h2> <span> Examples </span></h2>
* <h3> <span> Example: String primitive </span></h3>
* <p>The following statement creates a string primitive from the string literal "Schaefer", and assigns it to the variable <code>last_name</code>:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var last_name = "Schaefer";
* </pre>
* <h3> <span> Example: String primitive properties </span></h3>
* <p>The following expressions evaluate to <code>8</code>, "<code>SCHAEFER</code>", and "<code>schaefer</code>", respectively:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">last_name.length
* last_name.toUpperCase()
* last_name.toLowerCase()
* </pre>
* <h3> <span> Example: Setting an individual character in a string </span></h3>
* <p>An individual character cannot be directly set in a string. Instead, a new string can be created using the <a href="String:substr" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:substr">substr</a> or <a href="String:substring" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:substring">substring</a> methods:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">function setCharAt(str, index, ch) {
* return str.substr(0, index) + ch + str.substr(index + 1);
* }
* 
* alert(setCharAt('scam', 1, 'p')); // outputs "spam"
* </pre>
* <h3> <span> Example: Pass a string among scripts in different windows or frames </span></h3>
* <p>The following code creates two string variables and opens a second window:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var lastName = "Schaefer";
* var firstName = "Jesse";
* empWindow = window.open('string2.html', 'window1', 'width=300,height=300');
* </pre>
* <p>If the HTML source for the second window (<code>string2.html</code>) creates two string variables, <code>empLastName</code> and <code>empFirstName</code>, the following code in the first window assigns values to the second window's variables:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">empWindow.empFirstName = firstName;
* empWindow.empLastName = lastName;
* </pre>
* <p>The following code in the first window displays the values of the second window's variables:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">alert('empFirstName in empWindow is ' + empWindow.empFirstName);
* alert('empLastName in empWindow is ' + empWindow.empLastName);
* </pre>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
var String = {
  // This is just a stub for a builtin native JavaScript object.
/**
* <div style="border: 1px solid #FFB752; background-color: #FEE3BC; font-weight: bold; text-align: center; padding: 0px 10px 0px 10px; margin: 10px 0px 10px 0px;"><p style="margin: 4px 0px 4px 0px;">Non-standard</p></div>
* 
* <h2> <span> Summary </span></h2>
* <p>Creates an <a href="http://www.w3.org/TR/html401/struct/links.html#adef-name-A" rel="nofollow" shape="rect" title="http://www.w3.org/TR/html401/struct/links.html#adef-name-A">HTML anchor</a> that is used as a hypertext target.
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <td colspan="2" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Method of <a href="String" shape="rect" title="Core JavaScript 1.5 Reference:Objects:String">String</a></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">Implemented in:</td>
* <td colspan="1" rowspan="1">JavaScript 1.0, NES2.0</td>
* </tr>
* </table>
* <h2> <span> Syntax </span></h2>
* <p><code>
* anchor(<i>nameAttribute</i>)
* </code>
* </p>
* <h2> <span> Parameters </span></h2>
* <dl><dt style="font-weight:bold"> <code>nameAttribute</code>Ê</dt><dd> A string.
* </dd></dl>
* <h2> <span> Description </span></h2>
* <p>Use the <code>anchor</code> method with the <code>document.write</code> or <code>document.writeln</code> methods to programmatically create and display an anchor in a document. Create the anchor with the <code>anchor</code> method, and then call <code>write</code> or <code>writeln</code> to display the anchor in a document. In server-side JavaScript, use the <code>write</code> function to display the anchor.
* </p><p>In the syntax, the text string represents the literal text that you want the user to see. The <code>nameAttribute</code> string represents the <code>NAME</code> attribute of the A tag.
* </p><p>Anchors created with the <code>anchor</code> method become elements in the <code>document.anchors</code> array.
* </p>
* <h2> <span> Examples </span></h2>
* <h3> <span> Example: Using <code>anchor</code> </span></h3>
* <p>The following example code within an HTML <code>script</code> element:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* var myString = "Table of Contents";
* document.writeln(myString.anchor("contents_anchor"));
* </pre>
* <p>will output the following HTML:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* &lt;A NAME="contents_anchor"&gt;Table of Contents&lt;/A&gt;
* </pre>
* <h2> <span> See Also </span></h2>
* <p><a href="String:link" shape="rect" title="Core JavaScript 1.5 Reference:Objects:String:link">link</a>
* </p>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>

 @type String 
*/
anchor: function(nameAttribute) {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <div style="border: 1px solid #FFB752; background-color: #FEE3BC; font-weight: bold; text-align: center; padding: 0px 10px 0px 10px; margin: 10px 0px 10px 0px;"><p style="margin: 4px 0px 4px 0px;">Non-standard</p></div>
* 
* <h2> <span> Summary </span></h2>
* <p>Causes a string to be displayed in a big font as if it were in a <a href="http://www.w3.org/TR/html401/present/graphics.html#edef-BIG" rel="nofollow" shape="rect" title="http://www.w3.org/TR/html401/present/graphics.html#edef-BIG"><code>BIG</code> tag</a>.
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <td colspan="2" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Method of <a href="String" shape="rect" title="Core JavaScript 1.5 Reference:Objects:String">String</a></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">Implemented in:</td>
* <td colspan="1" rowspan="1">JavaScript 1.0, NES2.0</td>
* </tr>
* </table>
* <h2> <span> Syntax </span></h2>
* <p><code>
* big()
* </code>
* </p>
* <h2> <span> Parameters </span></h2>
* <p>None.
* </p>
* <h2> <span> Description </span></h2>
* <p>Use the <code>big</code> method with the <code>write</code> or <code>writeln</code> methods to format and display a string in a document. In server-side JavaScript, use the <code>write</code> function to display the string.
* </p>
* <h2> <span> Examples </span></h2>
* <h3> <span> Example: Using <code>big</code> </span></h3>
* <p>The following example uses <code>string</code> methods to change the size of a string:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* var worldString="Hello, world"
* 
* document.write(worldString.small())
* document.write("&lt;P&gt;" + worldString.big())
* document.write("&lt;P&gt;" + worldString.fontsize(7))
* </pre>
* <p>This example produces the same output as the following HTML:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* &lt;SMALL&gt;Hello, world&lt;/SMALL&gt;
* &lt;P&gt;&lt;BIG&gt;Hello, world&lt;/BIG&gt;
* &lt;P&gt;&lt;FONTSIZE=7&gt;Hello, world&lt;/FONTSIZE&gt;
* </pre>
* <h2> <span> See Also </span></h2>
* <p><a href="String:fontsize" shape="rect" title="Core JavaScript 1.5 Reference:Objects:String:fontsize">fontsize</a>,
* <a href="String:small" shape="rect" title="Core JavaScript 1.5 Reference:Objects:String:small">small</a>
* </p>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>

 @type String 
*/
big: function() {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <div style="border: 1px solid #FFB752; background-color: #FEE3BC; font-weight: bold; text-align: center; padding: 0px 10px 0px 10px; margin: 10px 0px 10px 0px;"><p style="margin: 4px 0px 4px 0px;">Non-standard</p></div>
* 
* <h2> <span> Summary </span></h2>
* <p>Causes a string to blink as if it were in a <code>BLINK</code> tag.
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <td colspan="2" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Method of <a href="String" shape="rect" title="Core JavaScript 1.5 Reference:Objects:String">String</a></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">Implemented in:</td>
* <td colspan="1" rowspan="1">JavaScript 1.0, NES2.0</td>
* </tr>
* </table>
* <h2> <span> Syntax </span></h2>
* <p><code>
* blink()
* </code>
* </p>
* <h2> <span> Parameters </span></h2>
* <p>None.
* </p>
* <h2> <span> Description </span></h2>
* <p>Use the <code>blink</code> method with the <code>write</code> or <code>writeln</code> methods to format and display a string in a document. In server-side JavaScript, use the <code>write</code> function to display the string.
* </p>
* <h2> <span> Examples </span></h2>
* <h3> <span> Example: Using <code>string</code> methods to change the formatting of a string </span></h3>
* <p>The following example uses <code>string</code> methods to change the formatting of a string:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* var worldString="Hello, world"
* 
* document.write(worldString.blink())
* document.write("&lt;P&gt;" + worldString.bold())
* document.write("&lt;P&gt;" + worldString.italics())
* document.write("&lt;P&gt;" + worldString.strike())
* </pre>
* <p>This example produces the same output as the following HTML:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* &lt;BLINK&gt;Hello, world&lt;/BLINK&gt;
* &lt;P&gt;&lt;B&gt;Hello, world&lt;/B&gt;
* &lt;P&gt;&lt;I&gt;Hello, world&lt;/I&gt;
* &lt;P&gt;&lt;STRIKE&gt;Hello, world&lt;/STRIKE&gt;
* </pre>
* <h2> <span> See Also </span></h2>
* <p><a href="String:bold" shape="rect" title="Core JavaScript 1.5 Reference:Objects:String:bold">bold</a>,
* <a href="String:italics" shape="rect" title="Core JavaScript 1.5 Reference:Objects:String:italics">italics</a>,
* <a href="String:strike" shape="rect" title="Core JavaScript 1.5 Reference:Objects:String:strike">strike</a>
* </p>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>

 @type String 
*/
blink: function() {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <div style="border: 1px solid #FFB752; background-color: #FEE3BC; font-weight: bold; text-align: center; padding: 0px 10px 0px 10px; margin: 10px 0px 10px 0px;"><p style="margin: 4px 0px 4px 0px;">Non-standard</p></div>
* 
* <h2> <span> Summary </span></h2>
* <p>Causes a string to be displayed as bold as if it were in a <a href="http://www.w3.org/TR/html401/present/graphics.html#edef-B" rel="nofollow" shape="rect" title="http://www.w3.org/TR/html401/present/graphics.html#edef-B"><code>B</code> tag</a>.
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <td colspan="2" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Method of <a href="String" shape="rect" title="Core JavaScript 1.5 Reference:Objects:String">String</a></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">Implemented in:</td>
* <td colspan="1" rowspan="1">JavaScript 1.0, NES2.0</td>
* </tr>
* </table>
* <h2> <span> Syntax </span></h2>
* <p><code>
* bold()
* </code>
* </p>
* <h2> <span> Parameters </span></h2>
* <p>None.
* </p>
* <h2> <span> Description </span></h2>
* <p>Use the <code>bold</code> method with the <code>write</code> or <code>writeln</code> methods to format and display a string in a document. In server-side JavaScript, use the <code>write</code> function to display the string.
* </p>
* <h2> <span> Examples </span></h2>
* <h3> <span> Example: Using <code>string</code> methods to change the formatting of a string </span></h3>
* <p>The following example uses <code>string</code> methods to change the formatting of a string:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* var worldString="Hello, world"
* 
* document.write(worldString.blink())
* document.write("&lt;P&gt;" + worldString.bold())
* document.write("&lt;P&gt;" + worldString.italics())
* document.write("&lt;P&gt;" + worldString.strike())
* </pre>
* <p>This example produces the same output as the following HTML:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* &lt;BLINK&gt;Hello, world&lt;/BLINK&gt;
* &lt;P&gt;&lt;B&gt;Hello, world&lt;/B&gt;
* &lt;P&gt;&lt;I&gt;Hello, world&lt;/I&gt;
* &lt;P&gt;&lt;STRIKE&gt;Hello, world&lt;/STRIKE&gt;
* </pre>
* <h2> <span> See Also </span></h2>
* <p><a href="String:blink" shape="rect" title="Core JavaScript 1.5 Reference:Objects:String:blink">blink</a>,
* <a href="String:italics" shape="rect" title="Core JavaScript 1.5 Reference:Objects:String:italics">italics</a>,
* <a href="String:strike" shape="rect" title="Core JavaScript 1.5 Reference:Objects:String:strike">strike</a>
* </p>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>

 @type String 
*/
bold: function() {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span> Summary </span></h2>
* <p>Returns the specified character from a string.
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <td colspan="2" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Method of <a href="String" shape="rect" title="Core JavaScript 1.5 Reference:Objects:String">String</a></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">Implemented in:</td>
* <td colspan="1" rowspan="1">JavaScript 1.0, NES2.0</td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">ECMA Version:</td>
* <td colspan="1" rowspan="1">ECMA-262</td>
* </tr>
* </table>
* <h2> <span> Syntax </span></h2>
* <p><code>
* charAt(<i>index</i>)
* </code>
* </p>
* <h2> <span> Parameters </span></h2>
* <dl><dt style="font-weight:bold"> <code>index</code>Ê</dt><dd> An integer between 0 and 1 less than the length of the string.
* </dd></dl>
* <h2> <span> Description </span></h2>
* <p>Characters in a string are indexed from left to right. The index of the first character is 0, and the index of the last character in a string called <code>stringName</code> is <code>stringName.length - 1</code>. If the <code>index</code> you supply is out of range, JavaScript returns an empty string.
* </p>
* <h2> <span> Examples </span></h2>
* <h3> <span> Example: Displaying characters at different locations in a string </span></h3>
* <p>The following example displays characters at different locations in the string "<code>Brave new world</code>":
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* var anyString="Brave new world"
* 
* document.writeln("The character at index 0 is '" + anyString.charAt(0) + "'")
* document.writeln("The character at index 1 is '" + anyString.charAt(1) + "'")
* document.writeln("The character at index 2 is '" + anyString.charAt(2) + "'")
* document.writeln("The character at index 3 is '" + anyString.charAt(3) + "'")
* document.writeln("The character at index 4 is '" + anyString.charAt(4) + "'")
* document.writeln("The character at index 999 is '" + anyString.charAt(999) + "'")
* </pre>
* <p>These lines display the following:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* The character at index 0 is 'B'
* The character at index 1 is 'r'
* The character at index 2 is 'a'
* The character at index 3 is 'v'
* The character at index 4 is 'e'
* The character at index 999 is ''
* </pre>
* <h2> <span> See Also </span></h2>
* <p><a href="String:indexOf" shape="rect" title="Core JavaScript 1.5 Reference:Objects:String:indexOf">indexOf</a>,
* <a href="String:lastIndexOf" shape="rect" title="Core JavaScript 1.5 Reference:Objects:String:lastIndexOf">lastIndexOf</a>,
* <a href="String:split" shape="rect" title="Core JavaScript 1.5 Reference:Objects:String:split">split</a>,
* <a href="String:charCodeAt" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:charCodeAt">charCodeAt</a>
* </p>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
 * @type String
*/
charAt: function(index) {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span> Summary </span></h2>
* <p>Returns a number indicating the Unicode value of the character at the given index.
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <td colspan="2" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Method of <a href="String" shape="rect" title="Core JavaScript 1.5 Reference:Objects:String">String</a></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">Implemented in:</td>
* <td colspan="1" rowspan="1">JavaScript 1.2, NES3.0
* <p>JavaScript 1.3: returns a Unicode value rather than an ISO-Latin-1 value.
* </p>
* </td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">ECMA Version:</td>
* <td colspan="1" rowspan="1">ECMA-262</td>
* </tr>
* </table>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var codepoint = <i>string</i>.charCodeAt(<i>index</i>);
* </pre>
* <h2> <span> Parameters </span></h2>
* <dl><dt style="font-weight:bold"> <code>index</code>Ê</dt><dd> An integer between 0 and 1 less than the length of the string; if unspecified, defaults to 0.
* </dd></dl>
* <h2> <span> Description </span></h2>
* <p>Unicode code points range from 0 to 1,114,111. The first 128 Unicode code points are a direct match of the ASCII character encoding. For information on Unicode, see the <a href="Core_JavaScript_1.5_Guide:Unicode" shape="rect" title="Core JavaScript 1.5 Guide:Unicode">Core JavaScript 1.5 Guide</a>. Note that <code>charCodeAt</code> will always return a value that is less than 65,536.
* </p><p><code>charCodeAt</code> returns <code><a href="NaN" shape="rect" title="Core JavaScript 1.5 Reference:Properties:NaN">NaN</a></code> if the given index is not between 0 and 1 less than the length of the string.
* </p>
* <h2> <span> Backward Compatibility </span></h2>
* <h3> <span> JavaScript 1.2 </span></h3>
* <p>The <code>charCodeAt</code> method returns a number indicating the ISO-Latin-1 codeset value of the character at the given index. The ISO-Latin-1 codeset ranges from 0 to 255. The first 0 to 127 are a direct match of the ASCII character set.
* </p>
* <h2> <span> Examples </span></h2>
* <h3> <span> Example: Using <code>charCodeAt</code> </span></h3>
* <p>The following example returns 65, the Unicode value for A.
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">"ABC".charCodeAt(0) // returns 65
* </pre>
* <h2> <span>See Also</span></h2>
* <p><a href="Core_JavaScript_1.5_Reference:Global_Objects:String:fromCharCode" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:fromCharCode">fromCharCode</a>,
* <a href="String:charAt" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:charAt">charAt</a>
* </p>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
 * @type Number
*/
charCodeAt: function(index) {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span> Summary </span></h2>
* <p>Combines the text of two or more strings and returns a new string.
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <td colspan="2" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Method of <a href="String" shape="rect" title="Core JavaScript 1.5 Reference:Objects:String">String</a></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">Implemented in:</td>
* <td colspan="1" rowspan="1">JavaScript 1.2, NES3.0</td>
* </tr>
* </table>
* <h2> <span> Syntax </span></h2>
* <p><code>
* concat(<i>string2</i>, <i>string3</i>[, ..., <i>stringN</i>])
* </code>
* </p>
* <h2> <span> Parameters </span></h2>
* <dl><dt style="font-weight:bold"> <code>string2...string<i>N</i></code>Ê</dt><dd> Strings to concatenate to this string.
* </dd></dl>
* <h2> <span> Description </span></h2>
* <p><code>concat</code> combines the text from one or more strings and returns a new string. Changes to the text in one string do not affect the other string.
* </p>
* <h2> <span> Examples </span></h2>
* <h3> <span> Example: Using <code>concat</code> </span></h3>
* <p>The following example combines strings into a new string.
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* s1="Oh "
* s2="what a beautiful "
* s3="mornin'."
* s4=s1.concat(s2,s3) // returns "Oh what a beautiful mornin'."
* </pre>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
 * @type String
*/
concat: function(string2, string3,stringN) {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span> Summary </span></h2>
* <p>Returns a reference to the <a href="String" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String">String</a> function that created the instance's prototype. Note that the value of this property is a reference to the function itself, not a string containing the function's name.
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <td colspan="2" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Property of <a href="String" shape="rect" title="Core JavaScript 1.5 Reference:Objects:String">String</a></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">Implemented in:</td>
* <td colspan="1" rowspan="1">JavaScript 1.1, NES 2.0</td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">ECMA Version:</td>
* <td colspan="1" rowspan="1">ECMA-262</td>
* </tr>
* </table>
* <h2> <span> Description </span></h2>
* <p>See <a href="Core_JavaScript_1.5_Reference:Objects:Object:constructor" shape="rect" title="Core JavaScript 1.5 Reference:Objects:Object:constructor">constructor</a>
* </p>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
constructor: undefined,
/**
* <div style="border: 1px solid #FFB752; background-color: #FEE3BC; font-weight: bold; text-align: center; padding: 0px 10px 0px 10px; margin: 10px 0px 10px 0px;"><p style="margin: 4px 0px 4px 0px;">Non-standard</p></div>
* 
* <h2> <span> Summary </span></h2>
* <p>Causes a string to be displayed in fixed-pitch font as if it were in a <a href="http://www.w3.org/TR/html401/present/graphics.html#edef-TT" rel="nofollow" shape="rect" title="http://www.w3.org/TR/html401/present/graphics.html#edef-TT"><code>TT</code> tag</a>.
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <td colspan="2" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Method of <a href="String" shape="rect" title="Core JavaScript 1.5 Reference:Objects:String">String</a></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">Implemented in:</td>
* <td colspan="1" rowspan="1">JavaScript 1.0, NES2.0</td>
* </tr>
* </table>
* <h2> <span> Syntax </span></h2>
* <p><code>
* fixed()
* </code>
* </p>
* <h2> <span> Parameters </span></h2>
* <p>None.
* </p>
* <h2> <span> Description </span></h2>
* <p>Use the <code>fixed</code> method with the <code>write</code> or <code>writeln</code> methods to format and display a string in a document. In server-side JavaScript, use the <code>write</code> function to display the string.
* </p>
* <h2> <span> Examples </span></h2>
* <h3> <span> Example: Using <code>fixed</code> to change the formatting of a string </span></h3>
* <p>The following example uses the <code>fixed</code> method to change the formatting of a string:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* var worldString="Hello, world"
* document.write(worldString.fixed())
* </pre>
* <p>This example produces the same output as the following HTML:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* &lt;TT&gt;Hello, world&lt;/TT&gt;
* </pre>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>

 @type String 
*/
fixed: function() {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <div style="border: 1px solid #FFB752; background-color: #FEE3BC; font-weight: bold; text-align: center; padding: 0px 10px 0px 10px; margin: 10px 0px 10px 0px;"><p style="margin: 4px 0px 4px 0px;">Non-standard</p></div>
* 
* <h2> <span> Summary </span></h2>
* <p>Causes a string to be displayed in the specified color as if it were in a <code>&lt;FONT COLOR="<i>color</i>"&gt;</code> tag.
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <td colspan="2" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Method of <a href="String" shape="rect" title="Core JavaScript 1.5 Reference:Objects:String">String</a></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">Implemented in:</td>
* <td colspan="1" rowspan="1">JavaScript 1.0, NES2.0</td>
* </tr>
* </table>
* <h2> <span> Syntax </span></h2>
* <p><code>
* fontcolor(<i>color</i>)
* </code>
* </p>
* <h2> <span> Parameters </span></h2>
* <dl><dt style="font-weight:bold"> <code>color</code>Ê</dt><dd> A string expressing the color as a hexadecimal RGB triplet or as a string literal. String literals for color names are listed in the <a href="Core_JavaScript_1.5_Guide" shape="rect" title="Core JavaScript 1.5 Guide">Core JavaScript 1.5 Guide</a>.
* </dd></dl>
* <h2> <span> Description </span></h2>
* <p>Use the <code>fontcolor</code> method with the <code>write</code> or <code>writeln</code> methods to format and display a string in a document. In server-side JavaScript, use the <code>write</code> function to display the string.
* </p><p>If you express color as a hexadecimal RGB triplet, you must use the format <code>rrggbb</code>. For example, the hexadecimal RGB values for salmon are red=FA, green=80, and blue=72, so the RGB triplet for salmon is "<code>FA8072</code>".
* </p><p>The <code>fontcolor</code> method overrides a value set in the <code>fgColor</code> property.
* </p>
* <h2> <span> Examples </span></h2>
* <h3> <span> Example: Using <code>fontcolor</code> </span></h3>
* <p>The following example uses the <code>fontcolor</code> method to change the color of a string:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* var worldString="Hello, world"
* 
* document.write(worldString.fontcolor("maroon") +
* " is maroon in this line")
* document.write("&lt;P&gt;" + worldString.fontcolor("salmon") +
* " is salmon in this line")
* document.write("&lt;P&gt;" + worldString.fontcolor("red") +
* " is red in this line")
* 
* document.write("&lt;P&gt;" + worldString.fontcolor("8000") +
* " is maroon in hexadecimal in this line")
* document.write("&lt;P&gt;" + worldString.fontcolor("FA8072") +
* " is salmon in hexadecimal in this line")
* document.write("&lt;P&gt;" + worldString.fontcolor("FF00") +
* " is red in hexadecimal in this line")
* </pre>
* <p>The previous example produces the same output as the following HTML:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* &lt;FONT COLOR="maroon"&gt;Hello, world&lt;/FONT&gt; is maroon in this line
* &lt;P&gt;&lt;FONT COLOR="salmon"&gt;Hello, world&lt;/FONT&gt; is salmon in this line
* &lt;P&gt;&lt;FONT COLOR="red"&gt;Hello, world&lt;/FONT&gt; is red in this line
* 
* &lt;P&gt;&lt;FONT COLOR="8000"&gt;Hello, world&lt;/FONT&gt;
* is maroon in hexadecimal in this line
* &lt;P&gt;&lt;FONT COLOR="FA8072"&gt;Hello, world&lt;/FONT&gt;
* is salmon in hexadecimal in this line
* &lt;P&gt;&lt;FONT COLOR="FF00"&gt;Hello, world&lt;/FONT&gt;
* is red in hexadecimal in this line
* </pre>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>

 @type String 
*/
fontcolor: function(color) {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <div style="border: 1px solid #FFB752; background-color: #FEE3BC; font-weight: bold; text-align: center; padding: 0px 10px 0px 10px; margin: 10px 0px 10px 0px;"><p style="margin: 4px 0px 4px 0px;">Non-standard</p></div>
* 
* <h2> <span> Summary </span></h2>
* <p>Causes a string to be displayed in the specified font size as if it were in a <code>&lt;FONT SIZE="<i>size</i>"&gt;</code> tag.
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <td colspan="2" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Method of <a href="String" shape="rect" title="Core JavaScript 1.5 Reference:Objects:String">String</a></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">Implemented in:</td>
* <td colspan="1" rowspan="1">JavaScript 1.0, NES2.0</td>
* </tr>
* </table>
* <h2> <span> Syntax </span></h2>
* <p><code>
* fontsize(<i>size</i>)
* </code>
* </p>
* <h2> <span> Parameters </span></h2>
* <dl><dt style="font-weight:bold"> <code>size</code>Ê</dt><dd> An integer between 1 and 7, a string representing a signed integer between 1 and 7.
* </dd></dl>
* <h2> <span> Description </span></h2>
* <p>Use the <code>fontsize</code> method with the <code>write</code> or <code>writeln</code> methods to format and display a string in a document. In server-side JavaScript, use the <code>write</code> function to display the string.
* </p><p>When you specify size as an integer, you set the size of <code>stringName</code> to one of the 7 defined sizes. When you specify <code>size</code> as a string such as "-2", you adjust the font size of <code>stringName</code> relative to the size set in the <code>BASEFONT</code> tag.
* </p>
* <h2> <span> Examples </span></h2>
* <h3> <span> Example: Using <code>string</code> methods to change the size of a string </span></h3>
* <p>The following example uses <code>string</code> methods to change the size of a string:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* var worldString="Hello, world"
* 
* document.write(worldString.small())
* document.write("&lt;P&gt;" + worldString.big())
* document.write("&lt;P&gt;" + worldString.fontsize(7))
* </pre>
* <p>This example produces the same output as the following HTML:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* &lt;SMALL&gt;Hello, world&lt;/SMALL&gt;
* &lt;P&gt;&lt;BIG&gt;Hello, world&lt;/BIG&gt;
* &lt;P&gt;&lt;FONT SIZE="7"&gt;Hello, world&lt;/FONT&gt;
* </pre>
* <h2> <span> See Also </span></h2>
* <p><a href="String:big" shape="rect" title="Core JavaScript 1.5 Reference:Objects:String:big">big</a>,
* <a href="String:small" shape="rect" title="Core JavaScript 1.5 Reference:Objects:String:small">small</a>
* </p>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>

 @type String 
*/
fontsize: function(size) {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span> Summary </span></h2>
* <p>Returns a string created by using the specified sequence of Unicode values.
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <td colspan="2" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Method of <a href="String" shape="rect" title="Core JavaScript 1.5 Reference:Objects:String">String</a></td>
* </tr>
* <tr>
* <td colspan="2" rowspan="1">
* <p><b>Static</b>
* </p>
* </td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">Implemented in:</td>
* <td colspan="1" rowspan="1">JavaScript 1.2, NES3.0
* <p>JavaScript 1.3: Uses a Unicode value rather than an ISO-Latin-1 value.
* </p>
* </td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">ECMA Version:</td>
* <td colspan="1" rowspan="1">ECMA-262</td>
* </tr>
* </table>
* <h2> <span> Syntax </span></h2>
* <p><code>
* String.fromCharCode(<i>num1</i>, <i>...</i>, <i>numN</i>)
* </code>
* </p>
* <h2> <span> Parameters </span></h2>
* <dl><dt style="font-weight:bold"> <code>num1, ..., num<i>N</i></code>Ê</dt><dd> A sequence of numbers that are Unicode values.
* </dd></dl>
* <h2> <span> Description </span></h2>
* <p>This method returns a string and not a <code>String</code> object.
* </p><p>Because <code>fromCharCode</code> is a static method of <code>String</code>, you always use it as <code>String.fromCharCode()</code>, rather than as a method of a <code>String</code> object you created.
* </p>
* <h2> <span> Backward Compatibility </span></h2>
* <h3> <span> JavaScript 1.2 </span></h3>
* <p>The <code>fromCharCode</code> method returns a string created by using the specified sequence of ISO-Latin-1 codeset values.
* </p>
* <h2> <span> Examples </span></h2>
* <h3> <span> Example: Using <code>fromCharCode</code> </span></h3>
* <p>The following example returns the string "ABC".
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* String.fromCharCode(65,66,67)
* </pre>
* <h2> <span>See Also</span></h2>
* <p><a href="String:charCodeAt" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:charCodeAt">charCodeAt</a>
* </p>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
 * @type String
*/
fromCharCode: function(num1,numN) {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span> Summary </span></h2>
* <p>Returns the index within the calling <code>String</code> object of the first occurrence of the specified value, starting the search at <code>fromIndex</code>, or -1 if the value is not found.
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <td colspan="2" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Method of <a href="String" shape="rect" title="Core JavaScript 1.5 Reference:Objects:String">String</a></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">Implemented in:</td>
* <td colspan="1" rowspan="1">JavaScript 1.0, NES2.0</td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">ECMA Version:</td>
* <td colspan="1" rowspan="1">ECMA-262</td>
* </tr>
* </table>
* <h2> <span> Syntax </span></h2>
* <p><code>
* indexOf(<i>searchValue</i>[, <i>fromIndex</i>])
* </code>
* </p>
* <h2> <span> Parameters </span></h2>
* <dl><dt style="font-weight:bold"> <code>searchValue</code>Ê</dt><dd> A string representing the value to search for.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <code>fromIndex</code>Ê</dt><dd> The location within the calling string to start the search from. It can be any integer between 0 and the length of the string. The default value is 0.
* </dd></dl>
* <h2> <span> Description </span></h2>
* <p>Characters in a string are indexed from left to right. The index of the first character is 0, and the index of the last character of a string called <code>stringName</code> is <code>stringName.length - 1</code>.
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* "Blue Whale".indexOf("Blue")    // returns 0
* "Blue Whale".indexOf("Blute")   // returns -1
* "Blue Whale".indexOf("Whale",0) // returns 5
* "Blue Whale".indexOf("Whale",5) // returns 5
* "Blue Whale".indexOf("",9)      // returns 9
* "Blue Whale".indexOf("",10)     // returns 10
* "Blue Whale".indexOf("",11)     // returns 10
* </pre>
* <p>The <code>indexOf</code> method is case sensitive. For example, the following expression returns -1:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* "Blue Whale".indexOf("blue")
* </pre>
* <h2> <span> Examples </span></h2>
* <h3> <span> Example: Using <code>indexOf</code> and <code>lastIndexOf</code> </span></h3>
* <p>The following example uses <code>indexOf</code> and <code>lastIndexOf</code> to locate values in the string "<code>Brave new world</code>".
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* var anyString="Brave new world"
* 
* // Displays 8
* document.write("&lt;P&gt;The index of the first w from the beginning is " +
* anyString.indexOf("w"))
* // Displays 10
* document.write("&lt;P&gt;The index of the first w from the end is " +
* anyString.lastIndexOf("w"))
* // Displays 6
* document.write("&lt;P&gt;The index of 'new' from the beginning is " +
* anyString.indexOf("new"))
* // Displays 6
* document.write("&lt;P&gt;The index of 'new' from the end is " +
* anyString.lastIndexOf("new"))
* </pre>
* <h3> <span> Example: <code>indexOf</code> and case-sensitivity </span></h3>
* <p>The following example defines two string variables. The variables contain the same string except that the second string contains uppercase letters. The first <code>writeln</code> method displays 19. But because the <code>indexOf</code> method is case sensitive, the string "<code>cheddar</code>" is not found in <code>myCapString</code>, so the second <code>writeln</code> method displays -1.
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* myString="brie, pepper jack, cheddar"
* myCapString="Brie, Pepper Jack, Cheddar"
* document.writeln('myString.indexOf("cheddar") is ' +
* myString.indexOf("cheddar"))
* document.writeln('&lt;P&gt;myCapString.indexOf("cheddar") is ' +
* myCapString.indexOf("cheddar"))
* </pre>
* <h3> <span> Example: Using <code>indexOf</code> to count occurrences of a letter in a string </span></h3>
* <p>The following example sets <code>count</code> to the number of occurrences of the letter <code>x</code> in the string <code>str</code>:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* count = 0;
* pos = str.indexOf("x");
* while ( posÊ!= -1 ) {
* count++;
* pos = str.indexOf("x",pos+1);
* }
* </pre>
* <h2> <span> See Also </span></h2>
* <p><a href="String:charAt" shape="rect" title="Core JavaScript 1.5 Reference:Objects:String:charAt">charAt</a>,
* <a href="String:lastIndexOf" shape="rect" title="Core JavaScript 1.5 Reference:Objects:String:lastIndexOf">lastIndexOf</a>,
* <a href="String:split" shape="rect" title="Core JavaScript 1.5 Reference:Objects:String:split">split</a>
* </p>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
 * @type Number
*/
indexOf: function(searchValue, fromIndex) {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <div style="border: 1px solid #FFB752; background-color: #FEE3BC; font-weight: bold; text-align: center; padding: 0px 10px 0px 10px; margin: 10px 0px 10px 0px;"><p style="margin: 4px 0px 4px 0px;">Non-standard</p></div>
* 
* <h2> <span> Summary </span></h2>
* <p>Causes a string to be italic, as if it were in an <a href="http://www.w3.org/TR/html401/present/graphics.html#edef-I" rel="nofollow" shape="rect" title="http://www.w3.org/TR/html401/present/graphics.html#edef-I"><code>I</code> tag</a>.
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <td colspan="2" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Method of <a href="String" shape="rect" title="Core JavaScript 1.5 Reference:Objects:String">String</a></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">Implemented in:</td>
* <td colspan="1" rowspan="1">JavaScript 1.0, NES2.0</td>
* </tr>
* </table>
* <h2> <span> Syntax </span></h2>
* <p><code>
* italics()
* </code>
* </p>
* <h2> <span> Parameters </span></h2>
* <p>None.
* </p>
* <h2> <span> Description </span></h2>
* <p>Use the <code>italics</code> method with the <code>write</code> or <code>writeln</code> methods to format and display a string in a document. In server-side JavaScript, use the <code>write</code> function to display the string.
* </p>
* <h2> <span> Examples </span></h2>
* <h3> <span> Example: Using <code>string</code> methods to change the formatting of a string </span></h3>
* <p>The following example uses <code>string</code> methods to change the formatting of a string:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* var worldString="Hello, world"
* 
* document.write(worldString.blink())
* document.write("&lt;P&gt;" + worldString.bold())
* document.write("&lt;P&gt;" + worldString.italics())
* document.write("&lt;P&gt;" + worldString.strike())
* </pre>
* <p>This example produces the same output as the following HTML:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* &lt;BLINK&gt;Hello, world&lt;/BLINK&gt;
* &lt;P&gt;&lt;B&gt;Hello, world&lt;/B&gt;
* &lt;P&gt;&lt;I&gt;Hello, world&lt;/I&gt;
* &lt;P&gt;&lt;STRIKE&gt;Hello, world&lt;/STRIKE&gt;
* </pre>
* <h2> <span> See Also </span></h2>
* <p><a href="String:blink" shape="rect" title="Core JavaScript 1.5 Reference:Objects:String:blink">blink</a>,
* <a href="String:bold" shape="rect" title="Core JavaScript 1.5 Reference:Objects:String:bold">bold</a>,
* <a href="String:strike" shape="rect" title="Core JavaScript 1.5 Reference:Objects:String:strike">strike</a>
* </p>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>

 @type String 
*/
italics: function() {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span> Summary </span></h2>
* <p>Returns the index within the calling <code>String</code> object of the last occurrence of the specified value, or -1 if not found. The calling string is searched backward, starting at <code>fromIndex</code>.
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <td colspan="2" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Method of <a href="String" shape="rect" title="Core JavaScript 1.5 Reference:Objects:String">String</a></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">Implemented in:</td>
* <td colspan="1" rowspan="1">JavaScript 1.0, NES2.0</td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">ECMA Version:</td>
* <td colspan="1" rowspan="1">ECMA-262</td>
* </tr>
* </table>
* <h2> <span> Syntax </span></h2>
* <p><code>
* lastIndexOf(<i>searchValue</i>[, <i>fromIndex</i>])
* </code>
* </p>
* <h2> <span> Parameters </span></h2>
* <dl><dt style="font-weight:bold"> <code>searchValue</code>Ê</dt><dd> A string representing the value to search for.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <code>fromIndex</code>Ê</dt><dd> The location within the calling string to start the search from. It can be any integer between 0 and the length of the string. The default value is the length of the string.
* </dd></dl>
* <h2> <span> Description </span></h2>
* <p>Characters in a string are indexed from left to right. The index of the first character is 0, and the index of the last character is <code>stringName.length - 1</code>.
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* "canal".lastIndexOf("a")   // returns 3
* "canal".lastIndexOf("a",2) // returns 1
* "canal".lastIndexOf("a",0) // returns -1
* "canal".lastIndexOf("x")   // returns -1
* </pre>
* <p>The <code>lastIndexOf</code> method is case sensitive. For example, the following expression returns -1:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* "Blue Whale, Killer Whale".lastIndexOf("blue")
* </pre>
* <h2> <span> Examples </span></h2>
* <h3> <span> Example: Using <code>indexOf</code> and <code>lastIndexOf</code> </span></h3>
* <p>The following example uses <code>indexOf</code> and <code>lastIndexOf</code> to locate values in the string "<code>Brave new world</code>".
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* var anyString="Brave new world"
* 
* // Displays 8
* document.write("&lt;P&gt;The index of the first w from the beginning is " +
* anyString.indexOf("w"))
* // Displays 10
* document.write("&lt;P&gt;The index of the first w from the end is " +
* anyString.lastIndexOf("w"))
* // Displays 6
* document.write("&lt;P&gt;The index of 'new' from the beginning is " +
* anyString.indexOf("new"))
* // Displays 6
* document.write("&lt;P&gt;The index of 'new' from the end is " +
* anyString.lastIndexOf("new"))
* </pre>
* <h2> <span> See Also </span></h2>
* <p><a href="String:charAt" shape="rect" title="Core JavaScript 1.5 Reference:Objects:String:charAt">charAt</a>,
* <a href="String:indexOf" shape="rect" title="Core JavaScript 1.5 Reference:Objects:String:indexOf">indexOf</a>,
* <a href="String:split" shape="rect" title="Core JavaScript 1.5 Reference:Objects:String:split">split</a>
* </p>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
 * @type Number
*/
lastIndexOf: function(searchValue, fromIndex) {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span> Summary </span></h2>
* <p>The length of a string.
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <td colspan="2" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Property of <a href="String" shape="rect" title="Core JavaScript 1.5 Reference:Objects:String">String</a></td>
* </tr>
* <tr>
* <td colspan="2" rowspan="1">
* <p><b>Read-only</b>
* </p>
* </td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">Implemented in:</td>
* <td colspan="1" rowspan="1">JavaScript 1.0, NES2.0</td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">ECMA Version:</td>
* <td colspan="1" rowspan="1">ECMA-262</td>
* </tr>
* </table>
* <h2> <span> Description </span></h2>
* <p>This property returns the number of characters in the string. For an empty string, <code>length</code> is 0.
* </p>
* <h2> <span> Examples </span></h2>
* <p>The following example displays 8 in an Alert dialog box:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* var x = "Netscape";
* alert("The string length is " + x.length);
* </pre>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
 * @type Number
*/
length: undefined,
/**
* <div style="border: 1px solid #FFB752; background-color: #FEE3BC; font-weight: bold; text-align: center; padding: 0px 10px 0px 10px; margin: 10px 0px 10px 0px;"><p style="margin: 4px 0px 4px 0px;">Non-standard</p></div>
* 
* <h2> <span> Summary </span></h2>
* <p>Creates an <a href="http://www.w3.org/TR/html401/struct/links.html#adef-href" rel="nofollow" shape="rect" title="http://www.w3.org/TR/html401/struct/links.html#adef-href">HTML hypertext link</a> that requests another URL.
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <td colspan="2" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Method of <a href="String" shape="rect" title="Core JavaScript 1.5 Reference:Objects:String">String</a></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">Implemented in:</td>
* <td colspan="1" rowspan="1">JavaScript 1.0, NES2.0</td>
* </tr>
* </table>
* <h2> <span> Syntax </span></h2>
* <p><code>
* link(<i>hrefAttribute</i>)
* </code>
* </p>
* <h2> <span> Parameters </span></h2>
* <dl><dt style="font-weight:bold"> <code>hrefAttribute</code>Ê</dt><dd> Any string that specifies the <code>HREF</code> of the <code>A</code> tag; it should be a valid URL (relative or absolute).
* </dd></dl>
* <h2> <span> Description </span></h2>
* <p>Use the <code>link</code> method to create an HTML snippet for a hypertext link. The returned string can then be added to the document via <code><a href="http://developer.mozilla.org/en/docs/DOM:document.write" shape="rect" title="DOM:document.write">document.write</a></code> or <code><a href="http://developer.mozilla.org/en/docs/element.innerHTML" shape="rect" title="DOM:element.innerHTML">element.innerHTML</a></code>.
* </p><p>Links created with the <code>link</code> method become elements in the <code>links</code> array of the <code>document</code> object. See <code><a href="http://developer.mozilla.org/en/docs/document.links" shape="rect" title="DOM:document.links">document.links</a></code>.
* </p>
* <h2> <span> Examples </span></h2>
* <h3> <span> Example: Using <code>link</code> </span></h3>
* <p>The following example displays the word "Netscape" as a hypertext link that returns the user to the Netscape home page:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* var hotText="Netscape"
* var URL="http://home.netscape.com"
* 
* document.write("Click to return to " + hotText.link(URL))
* </pre>
* <p>This example produces the same output as the following HTML:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* Click to return to &lt;A HREF="http://home.netscape.com"&gt;Netscape&lt;/A&gt;
* </pre>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>

 @type String 
*/
link: function(hrefAttribute) {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span> Summary </span></h2>
* <p>Used to retrieve the matches when matching a <i>string</i> against a <i>regular expression</i>.
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <td colspan="2" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Method of <a href="String" shape="rect" title="Core JavaScript 1.5 Reference:Objects:String">String</a></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">Implemented in:</td>
* <td colspan="1" rowspan="1">JavaScript 1.2</td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">ECMA Version:</td>
* <td colspan="1" rowspan="1">ECMA-262, Edition 3</td>
* </tr>
* </table>
* <h2> <span> Syntax </span></h2>
* <p><code>
* match(<i>regexp</i>)
* </code>
* </p>
* <h2> <span> Parameters </span></h2>
* <dl><dt style="font-weight:bold"> <code>regexp</code>Ê</dt><dd> A <a href="RegExp" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:RegExp"> regular expression</a> object. If a non-RegExp object <code>obj</code> is passed, it is implicitly converted to a RegExp by using <code>new RegExp(obj)</code>.
* </dd></dl>
* <h2> <span> Description </span></h2>
* <p>If the regular expression does not include the <code>g</code> flag, returns the same result as <code><a href="Core_JavaScript_1.5_Reference:Objects:RegExp:exec" shape="rect" title="Core JavaScript 1.5 Reference:Objects:RegExp:exec"><i>regexp</i>.exec(<i>string</i>)</a></code>.
* </p><p>If the regular expression includes the <code>g</code> flag, the method returns an <code><a href="Array" shape="rect" title="Core JavaScript 1.5 Reference:Objects:Array">Array</a></code> containing all matches.
* </p>
* <h3> <span> Notes </span></h3>
* <ul><li> If you need to know if a string matches a regular expression <code>regexp</code>, use <code><a href="Core_JavaScript_1.5_Reference:Objects:RegExp:test" shape="rect" title="Core JavaScript 1.5 Reference:Objects:RegExp:test"><i>regexp</i>.test(<i>string</i>)</a></code>.
* </li><li> If you only want the first match found, you might want to use <code><a href="RegExp:exec" shape="rect" title="Core JavaScript 1.5 Reference:Objects:RegExp:exec"><i>regexp</i>.exec(<i>string</i>)</a></code> instead.
* </li></ul>
* <h2> <span> Additional Reading </span></h2>
* <ul><li> See Â§15.5.4.10 of the ECMA-262 specification.
* </li></ul>
* <h2> <span> Examples </span></h2>
* <h3> <span> Example: Using <code>match</code> </span></h3>
* <p>In the following example, <code>match</code> is used to find "<code>Chapter</code>" followed by 1 or more numeric characters followed by a decimal point and numeric character 0 or more times. The regular expression includes the <code>i</code> flag so that case will be ignored.
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* &lt;SCRIPT&gt;
* str = "For more information, see Chapter 3.4.5.1";
* re = /(chapter \d+(\.\d)*)/i;
* found = str.match(re);
* document.write(found);
* &lt;/SCRIPT&gt;
* </pre>
* <p>This returns the array containing Chapter 3.4.5.1,Chapter 3.4.5.1,.1
* </p><p>"<code>Chapter 3.4.5.1</code>" is the first match and the first value remembered from <code>(Chapter \d+(\.\d)*)</code>.
* </p><p>"<code>.1</code>" is the second value remembered from <code>(\.\d)</code>.
* </p>
* <h3> <span> Example: Using global and ignore case flags with <code>match</code> </span></h3>
* <p>The following example demonstrates the use of the global and ignore case flags with <code>match</code>.
* All letters A through E and a through e are returned, each its own element in the array
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* var str = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
* var regexp = /[A-E]/gi;
* var matches_array = str.match(regexp);
* document.write(matches_array);
* </pre>
* <p><code>matches_array</code> now equals <code>['A', 'B', 'C', 'D', 'E', 'a', 'b', 'c', 'd', 'e']</code>
* </p>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
 * @type String[]
*/
match: function(regexp) {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span> Summary </span></h2>
* <p>Represents the prototype for this class. You can use the prototype to add properties or methods to all instances of a class. For information on prototypes, see <a href="Function:prototype" shape="rect" title="Core JavaScript 1.5 Reference:Objects:Function:prototype">Function.prototype</a>.
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <td colspan="2" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Property of <a href="String" shape="rect" title="Core JavaScript 1.5 Reference:Objects:String">String</a></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">Implemented in:</td>
* <td colspan="1" rowspan="1">JavaScript 1.1, NES3.0</td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">ECMA Version:</td>
* <td colspan="1" rowspan="1">ECMA-262</td>
* </tr>
* </table>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
prototype: undefined,
/**
* <h2> <span> Summary </span></h2>
* <p>Finds a match between a regular expression and a string, and replaces the matched substring with a new substring.
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <td colspan="2" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Method of <a href="String" shape="rect" title="Core JavaScript 1.5 Reference:Objects:String">String</a></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">Implemented in:</td>
* <td colspan="1" rowspan="1">JavaScript 1.2
* <p>JavaScript 1.3 added the ability to specify a function as the second parameter.
* </p>
* </td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">ECMAScript Edition:</td>
* <td colspan="1" rowspan="1">ECMA-262 Edition 3</td>
* </tr>
* </table>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var newString = <i>str</i>.replace(<i>regexp</i>/<i>substr</i>, <i>newSubStr</i>/<i>function</i>[, <i>flags</i>]);
* </pre>
* <h2> <span> Parameters </span></h2>
* <dl><dt style="font-weight:bold"> <code>regexp</code>Ê</dt><dd> A <a href="RegExp" shape="rect" title="Core JavaScript 1.5 Reference:Objects:RegExp"> RegExp</a> object. The match is replaced by the return value of parameter #2.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <code>substr</code>Ê</dt><dd> A <a href="String" shape="rect" title="Core JavaScript 1.5 Reference:Objects:String"> String</a> that is to be replaced by <code>newSubStr</code>.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <code>newSubStr</code>Ê</dt><dd> The <a href="String" shape="rect" title="Core JavaScript 1.5 Reference:Objects:String"> String</a> that replaces the substring received from parameter #1.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <code>function</code>Ê</dt><dd> A function to be invoked to create the new substring (to put in place of the substring received from parameter #1).
* </dd></dl>
* <dl><dt style="font-weight:bold"> <code>flags</code>Ê</dt><dd> <i>(<a href="http://developer.mozilla.org/en/docs/SpiderMonkey" shape="rect" title="SpiderMonkey">SpiderMonkey</a> extension)</i> A <a href="String" shape="rect" title="Core JavaScript 1.5 Reference:Objects:String"> String</a> containing any combination of the RegExp flags: <code>g</code> - global match, <code>i</code> - ignore case, <code>m</code> - match over multiple lines. This parameter is only used if the first parameter is a string.
* </dd></dl>
* <h2> <span> Description </span></h2>
* <p>This method does not change the <code>String</code> object it is called on. It simply returns a new string.
* </p><p>To perform a global search and replace, either include the <code>g</code> flag in the regular expression or if the first parameter is a string, include <code>g</code> in the <code>flags</code> parameter.
* </p>
* <h3> <span> Specifying a string as a parameter </span></h3>
* <p>The replacement string can include the following special replacement patterns:
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <td colspan="1" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Pattern</td>
* <td colspan="1" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Inserts</td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code>$$</code></td>
* <td colspan="1" rowspan="1">Inserts a "$".</td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code>$&amp;</code></td>
* <td colspan="1" rowspan="1">Inserts the matched substring.</td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code>$`</code></td>
* <td colspan="1" rowspan="1">Inserts the portion of the string that precedes the matched substring.</td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1"><code>$'</code></td>
* <td colspan="1" rowspan="1">Inserts the portion of the string that follows the matched substring.</td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1" style="white-space:nowrap"><code>$<i>n</i></code> or <code>$<i>nn</i></code></td>
* <td colspan="1" rowspan="1">Where <code><i>n</i></code> or <code><i>nn</i></code> are decimal digits, inserts the <i>n</i>th parenthesized submatch string, provided the first argument was a <code>RegExp</code> object.</td>
* </tr>
* </table>
* <h3> <span> Specifying a function as a parameter </span></h3>
* <p>When you specify a function as the second parameter, the function is invoked after the match has been performed. (The use of a function in this manner is often called a lambda expression.)
* </p><p>In your function, you can dynamically generate the string that replaces the matched substring. The result of the function call is used as the replacement value.
* </p><p>The nested function can use the matched substrings to determine the new string (<code>newSubStr</code>) that replaces the found substring. You get the matched substrings through the parameters of your function. The first parameter of your function holds the complete matched substring. If the first argument was a <code>RegExp</code> object, then the following <i>n</i> parameters can be used for parenthetical matches, remembered submatch strings, where <i>n</i> is the number of submatch strings in the regular expression. Finally, the last two parameters are the offset within the string where the match occurred and the string itself. For example, the following <code>replace</code> method returns XXzzzz - XX , zzzz.
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">function replacer(str, p1, p2, offset, s)
* {
* return str + " - " + p1 + " , " + p2;
* }
* var newString = "XXzzzz".replace(/(X*)(z*)/, replacer);
* </pre>
* <h2> <span> Examples </span></h2>
* <h3> <span> Example: Using <code>global</code> and <code>ignore</code> with <code>replace</code> </span></h3>
* <p>In the following example, the regular expression includes the global and ignore case flags which permits <code>replace</code> to replace each occurrence of 'apples' in the string with 'oranges'.
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var re = /apples/gi;
* var str = "Apples are round, and apples are juicy.";
* var newstr = str.replace(re, "oranges");
* print(newstr);
* </pre>
* <p>In this version, a string is used as the first parameter and the global and ignore case flags are specified in the <code>flags</code> parameter.
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var str = "Apples are round, and apples are juicy.";
* var newstr = str.replace("apples", "oranges", "gi");
* print(newstr);
* </pre>
* <p>Both of these examples print "oranges are round, and oranges are juicy."
* </p>
* <h3> <span> Example: Defining the regular expression in <code>replace</code> </span></h3>
* <p>In the following example, the regular expression is defined in <code>replace</code> and includes the ignore case flag.
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var str = "Twas the night before Xmas...";
* var newstr = str.replace(/xmas/i, "Christmas");
* print(newstr);
* </pre>
* <p>This prints "Twas the night before Christmas..."
* </p>
* <h3> <span> Example: Switching words in a string </span></h3>
* <p>The following script switches the words in the string. For the replacement text, the script uses the <code>$1</code> and <code>$2</code> replacement patterns.
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var re = /(\w+)\s(\w+)/;
* var str = "John Smith";
* var newstr = str.replace(re, "$2, $1");
* print(newstr);
* </pre>
* <p>This prints "Smith, John".
* </p>
* <h3> <span> Example: Using an inline function that modifies the matched characters </span></h3>
* <p>In this example, all occurrences of capital letters in the string are converted to lower case, and a hyphen is inserted just before the match location.  The important thing here is that additional operations are needed on the matched item before it is given back as a replacement.
* </p><p>The replacement function accepts the matched snippet as its parameter, and uses it to transform the case and concatenate the hyphen before returning.
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">function styleHyphenFormat(propertyName)
* {
* function upperToHyphenLower(match)
* {
* return '-' + match.toLowerCase();
* }
* return propertyName.replace(/[A-Z]/, upperToHyphenLower);
* }
* </pre>
* <p>Given <code>styleHyphenFormat('borderTop')</code>, this returns 'border-top'.
* </p><p>Because we want to further transform the <i>result</i> of the match before the final substitution is made, we must use a function.  This forces the evaluation of the match prior to the <code>toLowerCase()</code> method.  If we had tried to do this using the match without a function, the toLowerCase() would have no effect.
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var newString = propertyName.replace(/[A-Z]/, '-' + '$&amp;'.toLowerCase());  // won't work
* </pre>
* <p>This is because <code>'$&amp;'.toLowerCase()</code> would be evaluated first as a string literal (resulting in the same <code>'$&amp;'</code>) before using the characters as a pattern.
* </p>
* <h3> <span> Example: Replacing a Fahrenheit degree with its Celsius equivalent </span></h3>
* <p>The following example replaces a Fahrenheit degree with its equivalent Celsius degree. The Fahrenheit degree should be a number ending with F. The function returns the Celsius number ending with C. For example, if the input number is 212F, the function returns 100C. If the number is 0F, the function returns -17.77777777777778C.
* </p><p>The regular expression <code>test</code> checks for any number that ends with F. The number of Fahrenheit degree is accessible to the function through its second parameter, <code>p1</code>. The function sets the Celsius number based on the Fahrenheit degree passed in a string to the <code>f2c</code> function. <code>f2c</code> then returns the Celsius number. This function approximates Perl's s///e flag.
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">function f2c(x)
* {
* function convert(str, p1, offset, s)
* {
* return ((p1-32) * 5/9) + "C";
* }
* var s = String(x);
* var test = /(\d+(?:\.\d*)?)F\b/g;
* return s.replace(test, convert);
* }
* </pre>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
 * @type String
*/
replace: function(regexp_or_substr, newSubStr_or_function, flags) {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span> Summary </span></h2>
* <p>Executes the search for a match between a regular expression and this <code>String</code> object.
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <td colspan="2" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Method of <a href="String" shape="rect" title="Core JavaScript 1.5 Reference:Objects:String">String</a></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">Implemented in:</td>
* <td colspan="1" rowspan="1">JavaScript 1.2</td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">ECMA Version:</td>
* <td colspan="1" rowspan="1">ECMA-262, Edition 3</td>
* </tr>
* </table>
* <h2> <span> Syntax </span></h2>
* <p><code>
* search(<i>regexp</i>)
* </code>
* </p>
* <h2> <span> Parameters </span></h2>
* <dl><dt style="font-weight:bold"> <code>regexp</code>Ê</dt><dd> A <a href="RegExp" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:RegExp"> regular expression</a> object. If a non-RegExp object <code>obj</code> is passed, it is implicitly converted to a RegExp by using <code>new RegExp(obj)</code>.
* </dd></dl>
* <h2> <span> Description </span></h2>
* <p>If successful, search returns the index of the regular expression inside the string. Otherwise, it returns -1.
* </p><p>When you want to know whether a pattern is found in a string use <code>search</code> (similar to the regular expression <code><a href="Core_JavaScript_1.5_Reference:Objects:RegExp:test" shape="rect" title="Core JavaScript 1.5 Reference:Objects:RegExp:test">test</a></code> method); for more information (but slower execution) use <code><a href="String:match" shape="rect" title="Core JavaScript 1.5 Reference:Objects:String:match">match</a></code> (similar to the regular expression <code><a href="RegExp:exec" shape="rect" title="Core JavaScript 1.5 Reference:Objects:RegExp:exec">exec</a></code> method).
* </p>
* <h2> <span> Examples </span></h2>
* <h3> <span> Example: Using <code>search</code> </span></h3>
* <p>The following example prints a message which depends on the success of the test.
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* function testinput(re, str){
* if (str.search(re)Ê!= -1)
* midstring = " contains ";
* else
* midstring = " does not contain ";
* document.write (str + midstring + re.source);
* }
* </pre>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
 * @type Number
*/
search: function(regexp) {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span> Summary </span></h2>
* <p>Extracts a section of a string and returns a new string.
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <td colspan="2" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Method of <a href="String" shape="rect" title="Core JavaScript 1.5 Reference:Objects:String">String</a></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">Implemented in:</td>
* <td colspan="1" rowspan="1">JavaScript 1.0, NES2.0</td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">ECMA Version:</td>
* <td colspan="1" rowspan="1">ECMA-262, Edition 3</td>
* </tr>
* </table>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var sub = <i>string</i>.slice(<i>beginslice</i>[, <i>endSlice</i>]);
* </pre>
* <h2> <span> Parameters </span></h2>
* <dl><dt style="font-weight:bold"> <code>beginSlice</code>Ê</dt><dd> The zero-based index at which to begin extraction.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <code>endSlice</code>Ê</dt><dd> The zero-based index at which to end extraction. If omitted, <code>slice</code> extracts to the end of the string.
* </dd></dl>
* <h2> <span> Description </span></h2>
* <p><code>slice</code> extracts the text from one string and returns a new string. Changes to the text in one string do not affect the other string.
* </p><p><code>slice</code> extracts up to but not including <code>endSlice</code>. <code>string.slice(1,4)</code> extracts the second character through the fourth character (characters indexed 1, 2, and 3).
* </p><p>As a negative index, endSlice indicates an offset from the end of the string. string.slice(2,-1) extracts the third character through the second to last character in the string.
* </p>
* <h2> <span> Examples </span></h2>
* <h3> <span> Example: Using <code>slice</code> to create a new string </span></h3>
* <p>The following example uses <code>slice</code> to create a new string.
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">// assumes a print function is defined
* var str1 = "The morning is upon us.";
* var str2 = str1.slice(3, -2);
* print(str2);
* </pre>
* <p>This writes:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">morning is upon u
* </pre>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
 * @type String
*/
slice: function(beginslice, endSlice) {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <div style="border: 1px solid #FFB752; background-color: #FEE3BC; font-weight: bold; text-align: center; padding: 0px 10px 0px 10px; margin: 10px 0px 10px 0px;"><p style="margin: 4px 0px 4px 0px;">Non-standard</p></div>
* 
* <h2> <span> Summary </span></h2>
* <p>Causes a string to be displayed in a small font, as if it were in a <a href="http://www.w3.org/TR/html401/present/graphics.html#edef-SMALL" rel="nofollow" shape="rect" title="http://www.w3.org/TR/html401/present/graphics.html#edef-SMALL"><code>SMALL</code> tag</a>.
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <td colspan="2" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Method of <a href="String" shape="rect" title="Core JavaScript 1.5 Reference:Objects:String">String</a></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">Implemented in:</td>
* <td colspan="1" rowspan="1">JavaScript 1.0, NES2.0</td>
* </tr>
* </table>
* <h2> <span> Syntax </span></h2>
* <p><code>
* small()
* </code>
* </p>
* <h2> <span> Parameters </span></h2>
* <p>None.
* </p>
* <h2> <span> Description </span></h2>
* <p>Use the <code>small</code> method with the <code>write</code> or <code>writeln</code> methods to format and display a string in a document. In server-side JavaScript, use the <code>write</code> function to display the string.
* </p>
* <h2> <span> Examples </span></h2>
* <h3> <span> Example: Using <code>string</code> methods to change the size of a string </span></h3>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* var worldString="Hello, world"
* 
* document.write(worldString.small())
* document.write("&lt;P&gt;" + worldString.big())
* document.write("&lt;P&gt;" + worldString.fontsize(7))
* </pre>
* <p>This example produces the same output as the following HTML:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* &lt;SMALL&gt;Hello, world&lt;/SMALL&gt;
* &lt;P&gt;&lt;BIG&gt;Hello, world&lt;/BIG&gt;
* &lt;P&gt;&lt;FONTSIZE=7&gt;Hello, world&lt;/FONTSIZE&gt;
* </pre>
* <h2> <span> See Also </span></h2>
* <p><a href="String:big" shape="rect" title="Core JavaScript 1.5 Reference:Objects:String:big">big</a>,
* <a href="String:fontsize" shape="rect" title="Core JavaScript 1.5 Reference:Objects:String:fontsize">fontsize</a>
* </p>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>

 @type String 
*/
small: function() {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span> Summary </span></h2>
* <p>Splits a <code>String</code> object into an array of strings by separating the string into substrings.
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <td colspan="2" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Method of <a href="String" shape="rect" title="Core JavaScript 1.5 Reference:Objects:String">String</a></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">Implemented in:</td>
* <td colspan="1" rowspan="1">JavaScript 1.1, NES2.0</td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">ECMAScript Version:</td>
* <td colspan="1" rowspan="1">ECMA-262 (if separator is a string)
* ECMA-262, Edition 3 (if separator is a regular expression)</td>
* </tr>
* </table>
* <h2> <span> Syntax </span></h2>
* <p><code>
* split([<i>separator</i>][, <i>limit</i>])
* </code>
* </p>
* <h2> <span> Parameters </span></h2>
* <dl><dt style="font-weight:bold"> <code>separator</code>Ê</dt><dd> Specifies the character to use for separating the string. The <code>separator</code> is treated as a string or a <a href="RegExp" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:RegExp"> regular expression</a>. If <code>separator</code> is omitted, the array returned contains one element consisting of the entire string.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <code>limit</code>Ê</dt><dd> Integer specifying a limit on the number of splits to be found.
* </dd></dl>
* <h2> <span> Description </span></h2>
* <p>The <code>split</code> method returns the new array.
* </p><p>When found, <code>separator</code> is removed from the string and the substrings are returned in an array. If <code>separator</code> is omitted, the array contains one element consisting of the entire string.
* </p><p>Note: When the string is empty, <code>split</code> returns an array containing one empty string, rather than an empty array.
* </p>
* <h2> <span> Examples </span></h2>
* <h3> <span> Example: Using <code>split</code> </span></h3>
* <p>The following example defines a function that splits a string into an array of strings using the specified separator. After splitting the string, the function displays messages indicating the original string (before the split), the separator used, the number of elements in the array, and the individual array elements.
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* function splitString(stringToSplit,separator) {
* var arrayOfStrings = stringToSplit.split(separator);
* document.write('&lt;p&gt;The original string is: "' + stringToSplit + '"');
* document.write('&lt;br&gt;The separator is: "' + separator + '"');
* document.write("&lt;br&gt;The array has " + arrayOfStrings.length + " elements: ");
* 
* for (var i=0; i &lt; arrayOfStrings.length; i++) {
* document.write(arrayOfStrings[i] + " / ");
* }
* }
* 
* var tempestString = "Oh brave new world that has such people in it.";
* var monthString = "Jan,Feb,Mar,Apr,May,Jun,Jul,Aug,Sep,Oct,Nov,Dec";
* 
* var space = " ";
* var comma = ",";
* 
* splitString(tempestString, space);
* splitString(tempestString);
* splitString(monthString, comma);
* </pre>
* <p>This example produces the following output:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* The original string is: "Oh brave new world that has such people in it."
* The separator is: " "
* The array has 10 elements: Oh / brave / new / world / that / has / such / people / in / it. /
* 
* The original string is: "Oh brave new world that has such people in it."
* The separator is: "undefined"
* The array has 1 elements: Oh brave new world that has such people in it. /
* 
* The original string is: "Jan,Feb,Mar,Apr,May,Jun,Jul,Aug,Sep,Oct,Nov,Dec"
* The separator is: ","
* The array has 12 elements: Jan / Feb / Mar / Apr / May / Jun / Jul / Aug / Sep / Oct / Nov / Dec /
* </pre>
* <h3> <span> Example: Removing spaces from a string </span></h3>
* <p>In the following example, <code>split</code> looks for 0 or more spaces followed by a semicolon followed by 0 or more spaces and, when found, removes the spaces from the string. <code>nameList</code> is the array returned as a result of <code>split</code>.
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* &lt;script type="text/javascript"&gt;
* var names = "Harry TrumpÊ;Fred Barney; Helen RigbyÊ; Bill AbelÊ;Chris Hand ";
* document.write(names + "&lt;br&gt;" + "&lt;br&gt;");
* var re = /\s*;\s* /;
* var nameList = names.split(re);
* document.write(nameList);
* &lt;/script&gt;
* </pre>
* <p>This prints two lines; the first line prints the original string, and the second line prints the resulting array.
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* Harry TrumpÊ;Fred Barney; Helen RigbyÊ; Bill AbelÊ;Chris Hand
* Harry Trump,Fred Barney,Helen Rigby,Bill Abel,Chris Hand
* </pre>
* <h3> <span> Example: Returning a limited number of splits </span></h3>
* <p>In the following example, <code>split</code> looks for 0 or more spaces in a string and returns the first 3 splits that it finds.
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* var myString = "Hello World. How are you doing?";
* var splits = myString.split(" ", 3);
* document.write(splits);
* </pre>
* <p>This script displays the following:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* ["Hello", "World.", "How"]
* </pre>
* <h2> <span> See Also </span></h2>
* <p><a href="String:charAt" shape="rect" title="Core JavaScript 1.5 Reference:Objects:String:charAt">charAt</a>,
* <a href="String:indexOf" shape="rect" title="Core JavaScript 1.5 Reference:Objects:String:indexOf">indexOf</a>,
* <a href="String:lastIndexOf" shape="rect" title="Core JavaScript 1.5 Reference:Objects:String:lastIndexOf">lastIndexOf</a>,
* <a href="Array:join" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:join">Array:join</a>
* </p>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
 * @type String[]
*/
split: function(separator, limit) {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <div style="border: 1px solid #FFB752; background-color: #FEE3BC; font-weight: bold; text-align: center; padding: 0px 10px 0px 10px; margin: 10px 0px 10px 0px;"><p style="margin: 4px 0px 4px 0px;">Non-standard</p></div>
* 
* <h2> <span> Summary </span></h2>
* <p>Causes a string to be displayed as struck-out text, as if it were in a <a href="http://www.w3.org/TR/html401/present/graphics.html#edef-STRIKE" rel="nofollow" shape="rect" title="http://www.w3.org/TR/html401/present/graphics.html#edef-STRIKE"><code>STRIKE</code> tag</a>.
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <td colspan="2" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Method of <a href="String" shape="rect" title="Core JavaScript 1.5 Reference:Objects:String">String</a></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">Implemented in:</td>
* <td colspan="1" rowspan="1">JavaScript 1.0, NES2.0</td>
* </tr>
* </table>
* <h2> <span> Syntax </span></h2>
* <p><code>
* strike()
* </code>
* </p>
* <h2> <span> Parameters </span></h2>
* <p>None.
* </p>
* <h2> <span> Description </span></h2>
* <p>Use the <code>strike</code> method with the <code>write</code> or <code>writeln</code> methods to format and display a string in a document. In server-side JavaScript, use the <code>write</code> function to display the string.
* </p>
* <h2> <span> Examples </span></h2>
* <h3> <span> Example: Using <code>string</code> methods to change the formatting of a string </span></h3>
* <p>The following example uses <code>string</code> methods to change the formatting of a string:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* var worldString="Hello, world"
* 
* document.write(worldString.blink())
* document.write("&lt;P&gt;" + worldString.bold())
* document.write("&lt;P&gt;" + worldString.italics())
* document.write("&lt;P&gt;" + worldString.strike())
* </pre>
* <p>This example produces the same output as the following HTML:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* &lt;BLINK&gt;Hello, world&lt;/BLINK&gt;
* &lt;P&gt;&lt;B&gt;Hello, world&lt;/B&gt;
* &lt;P&gt;&lt;I&gt;Hello, world&lt;/I&gt;
* &lt;P&gt;&lt;STRIKE&gt;Hello, world&lt;/STRIKE&gt;
* </pre>
* <h2> <span> See Also </span></h2>
* <p><a href="String:blink" shape="rect" title="Core JavaScript 1.5 Reference:Objects:String:blink">blink</a>,
* <a href="String:bold" shape="rect" title="Core JavaScript 1.5 Reference:Objects:String:bold">bold</a>,
* <a href="String:italics" shape="rect" title="Core JavaScript 1.5 Reference:Objects:String:italics">italics</a>
* </p>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>

 @type String 
*/
strike: function() {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <div style="border: 1px solid #FFB752; background-color: #FEE3BC; font-weight: bold; text-align: center; padding: 0px 10px 0px 10px; margin: 10px 0px 10px 0px;"><p style="margin: 4px 0px 4px 0px;">Non-standard</p></div>
* 
* <h2> <span> Summary </span></h2>
* <p>Causes a string to be displayed as a subscript, as if it were in a <a href="http://www.w3.org/TR/html401/struct/text.html#edef-SUB" rel="nofollow" shape="rect" title="http://www.w3.org/TR/html401/struct/text.html#edef-SUB"><code>SUB</code> tag</a>.
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <td colspan="2" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Method of <a href="String" shape="rect" title="Core JavaScript 1.5 Reference:Objects:String">String</a></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">Implemented in:</td>
* <td colspan="1" rowspan="1">JavaScript 1.0, NES2.0</td>
* </tr>
* </table>
* <h2> <span> Syntax </span></h2>
* <p><code>
* sub()
* </code>
* </p>
* <h2> <span> Parameters </span></h2>
* <p>None.
* </p>
* <h2> <span> Description </span></h2>
* <p>Use the <code>sub</code> method with the <code>write</code> or <code>writeln</code> methods to format and display a string in a document. In server-side JavaScript, use the <code>write</code> function to generate the HTML.
* </p>
* <h2> <span> Examples </span></h2>
* <h3> <span> Example: Using <code>sub</code> and <code>sup</code> methods to format a string </span></h3>
* <p>The following example uses the <code>sub</code> and <code>sup</code> methods to format a string:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* var superText="superscript"
* var subText="subscript"
* 
* document.write("This is what a " + superText.sup() + " looks like.")
* document.write("&lt;P&gt;This is what a " + subText.sub() + " looks like.")
* </pre>
* <p>This example produces the same output as the following HTML:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* This is what a &lt;SUP&gt;superscript&lt;/SUP&gt; looks like.
* &lt;P&gt;This is what a &lt;SUB&gt;subscript&lt;/SUB&gt; looks like.
* </pre>
* <h2> <span> See Also </span></h2>
* <p><a href="String:sup" shape="rect" title="Core JavaScript 1.5 Reference:Objects:String:sup">sup</a>
* </p>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>

 @type String 
*/
sub: function() {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span> Summary </span></h2>
* <p>Returns the characters in a string beginning at the specified location through the specified number of characters.
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <td colspan="2" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Method of <a href="String" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String">String</a></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">Implemented in:</td>
* <td colspan="1" rowspan="1">JavaScript 1.0, JScript 3</td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">ECMA Version:</td>
* <td colspan="1" rowspan="1">None, although ECMA-262 ed. 3 has a non-normative section suggesting uniform semantics for <code>substr</code></td>
* </tr>
* </table>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var sub = <i>string</i>.substr(<i>start</i>[, <i>length</i>]);
* </pre>
* <h2> <span> Parameters </span></h2>
* <dl><dt style="font-weight:bold"> <code>start</code>Ê</dt><dd> Location at which to begin extracting characters (an integer between 0 and one less than the length of the string).
* </dd></dl>
* <dl><dt style="font-weight:bold"> <code>length</code>Ê</dt><dd> The number of characters to extract.
* </dd></dl>
* <h2> <span> Description </span></h2>
* <p><code>start</code> is a character index. The index of the first character is 0, and the index of the last character is 1 less than the length of the string. <code>substr</code> begins extracting characters at <code>start</code> and collects <code>length</code> characters (unless it reaches the end of the string first, in which case it will return fewer).
* </p><p>If <code>start</code> is positive and is greater than or equal to the length of the string, <code>substr</code> returns an empty string.
* </p><p>If <code>start</code> is negative, <code>substr</code> uses it as a character index from the end of the string. If <code>start</code> is negative and <code>abs(start)</code> is larger than the length of the string, <code>substr</code> uses 0 as the start index. Note: the described handling of negative values of the <code>start</code> argument is not supported by Microsoft JScript <a href="http://msdn2.microsoft.com/en-us/library/0esxc5wy.aspx" rel="nofollow" shape="rect" title="http://msdn2.microsoft.com/en-us/library/0esxc5wy.aspx">[1]</a>.
* </p><p>If <code>length</code> is 0 or negative, <code>substr</code> returns an empty string. If <code>length</code> is omitted, <code>start</code> extracts characters to the end of the string.
* </p>
* <h2> <span> Examples </span></h2>
* <h3> <span> Example: Using <code>substr</code> </span></h3>
* <p>Consider the following script:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">// assumes a print function is defined
* var str = "abcdefghij";
* print("(1,2): "    + str.substr(1,2));
* print("(-2,2): "   + str.substr(-2,2));
* print("(1): "      + str.substr(1));
* print("(-20, 2): " + str.substr(-20,2));
* print("(20, 2): "  + str.substr(20,2));
* </pre>
* <p>This script displays:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">(1,2): bc
* (-2,2): ij
* (1): bcdefghij
* (-20, 2): ab
* (20, 2):
* </pre>
* <h2> <span> See also </span></h2>
* <p><a href="Core_JavaScript_1.5_Reference:Global_Objects:String:substring" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:substring">substring</a>
* </p>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
 * @type String
*/
substr: function(start, length) {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span> Summary </span></h2>
* <p>Returns a subset of a <code>String</code> object.
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <td colspan="2" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Method of <a href="String" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String">String</a></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">Implemented in:</td>
* <td colspan="1" rowspan="1">JavaScript 1.0, NES2.0</td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">ECMA Version:</td>
* <td colspan="1" rowspan="1">ECMA-262</td>
* </tr>
* </table>
* <h2> <span> Syntax </span></h2>
* <p><code>
* substring(<i>indexA</i>, [<i>indexB</i>])
* </code>
* </p>
* <h2> <span> Parameters </span></h2>
* <dl><dt style="font-weight:bold"> <code>indexA</code>Ê</dt><dd> An integer between 0 and one less than the length of the string.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <code>indexB</code>Ê</dt><dd> (optional) An integer between 0 and the length of the string.
* </dd></dl>
* <h2> <span> Description </span></h2>
* <p><code>substring</code> extracts characters from <code>indexA</code> up to but not including <code>indexB</code>. In particular:
* </p>
* <ul><li> If <code>indexA</code> equals <code>indexB</code>, <code>substring</code> returns an empty string.
* </li><li> If <code>indexB</code> is omitted, <code>substring</code> extracts characters to the end of the string.
* </li><li> If either argument is less than 0 or is <code>NaN</code>, it is treated as if it were 0.
* </li><li> If either argument is greater than <code>stringName.length</code>, it is treated as if it were <code>stringName.length</code>.
* </li></ul>
* <p>If <code>indexA</code> is larger than <code>indexB</code>, then the effect of <code>substring</code> is as if the two arguments were swapped; for example, <code><i>str</i>.substring(1, 0) == <i>str</i>.substring(0, 1)</code>.
* </p>
* <h2> <span> Examples </span></h2>
* <h3> <span> Example: Using <code>substring</code> </span></h3>
* <p>The following example uses <code>substring</code> to display characters from the string "<code>Mozilla</code>":
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">// assumes a print function is defined
* var anyString = "Mozilla";
* 
* // Displays "Moz"
* print(anyString.substring(0,3));
* print(anyString.substring(3,0));
* 
* // Displays "lla"
* print(anyString.substring(4,7));
* print(anyString.substring(7,4));
* 
* // Displays "Mozill"
* print(anyString.substring(0,6));
* 
* // Displays "Mozilla"
* print(anyString.substring(0,7));
* print(anyString.substring(0,10));
* </pre>
* <h3> <span> Example: Replacing a substring within a string </span></h3>
* <p>The following example replaces a substring within a string. It will replace both individual characters and substrings. The function call at the end of the example changes the string "<code>Brave New World</code>" into "<code>Brave New Web</code>".
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">function replaceString(oldS, newS, fullS) {
* // Replaces oldS with newS in the string fullS
* for (var i = 0; i &lt; fullS.length; i++) {
* if (fullS.substring(i, i + oldS.length) == oldS) {
* fullS = fullS.substring(0, i) + newS + fullS.substring(i + oldS.length, fullS.length);
* }
* }
* return fullS;
* }
* 
* replaceString("World", "Web", "Brave New World");
* </pre>
* <h2> <span> See also </span></h2>
* <p><a href="Core_JavaScript_1.5_Reference:Global_Objects:String:substr" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:substr">substr</a>
* </p>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
 * @type String
*/
substring: function(indexA, indexB) {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <div style="border: 1px solid #FFB752; background-color: #FEE3BC; font-weight: bold; text-align: center; padding: 0px 10px 0px 10px; margin: 10px 0px 10px 0px;"><p style="margin: 4px 0px 4px 0px;">Non-standard</p></div>
* 
* <h2> <span> Summary </span></h2>
* <p>Causes a string to be displayed as a superscript, as if it were in a <a href="http://www.w3.org/TR/html401/struct/text.html#edef-SUP" rel="nofollow" shape="rect" title="http://www.w3.org/TR/html401/struct/text.html#edef-SUP"><code>SUP</code> tag</a>.
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <td colspan="2" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Method of <a href="String" shape="rect" title="Core JavaScript 1.5 Reference:Objects:String">String</a></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">Implemented in:</td>
* <td colspan="1" rowspan="1">JavaScript 1.0, NES2.0</td>
* </tr>
* </table>
* <h2> <span> Syntax </span></h2>
* <p><code>
* sup()
* </code>
* </p>
* <h2> <span> Parameters </span></h2>
* <p>None.
* </p>
* <h2> <span> Description </span></h2>
* <p>Use the <code>sup</code> method with the <code>write</code> or <code>writeln</code> methods to format and display a string in a document. In server-side JavaScript, use the <code>write</code> function to generate the HTML.
* </p>
* <h2> <span> Examples </span></h2>
* <h3> <span> Example: Using <code>sub</code> and <code>sup</code> methods to format a string </span></h3>
* <p>The following example uses the <code>sub</code> and <code>sup</code> methods to format a string:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* var superText="superscript"
* var subText="subscript"
* 
* document.write("This is what a " + superText.sup() + " looks like.")
* document.write("&lt;P&gt;This is what a " + subText.sub() + " looks like.")
* </pre>
* <p>This example produces the same output as the following HTML:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* This is what a &lt;SUP&gt;superscript&lt;/SUP&gt; looks like.
* &lt;P&gt;This is what a &lt;SUB&gt;subscript&lt;/SUB&gt; looks like.
* </pre>
* <h2> <span> See Also </span></h2>
* <p><a href="String:sub" shape="rect" title="Core JavaScript 1.5 Reference:Objects:String:sub">sub</a>
* </p>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>

 @type String 
*/
sup: function() {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span> Summary </span></h2>
* <p>Returns the calling string value converted to lowercase.
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <td colspan="2" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Method of <a href="String" shape="rect" title="Core JavaScript 1.5 Reference:Objects:String">String</a></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">Implemented in:</td>
* <td colspan="1" rowspan="1">JavaScript 1.0, NES2.0</td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">ECMA Version:</td>
* <td colspan="1" rowspan="1">ECMA-262</td>
* </tr>
* </table>
* <h2> <span> Syntax </span></h2>
* <p><code>
* toLowerCase()
* </code>
* </p>
* <h2> <span> Parameters </span></h2>
* <p>None.
* </p>
* <h2> <span> Description </span></h2>
* <p>The <code>toLowerCase</code> method returns the value of the string converted to lowercase. <code>toLowerCase</code> does not affect the value of the string itself.
* </p>
* <h2> <span> Examples </span></h2>
* <h3> <span> Example: Using <code>toLowerCase</code> </span></h3>
* <p>The following example displays the lowercase string "<code>alphabet</code>":
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* var upperText="ALPHABET"
* document.write(upperText.toLowerCase())
* </pre>
* <h2> <span> See Also </span></h2>
* <p><a href="String:toUpperCase" shape="rect" title="Core JavaScript 1.5 Reference:Objects:String:toUpperCase">toUpperCase</a>
* </p>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
 * @type String
*/
toLowerCase: function() {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <div style="border: 1px solid #FFB752; background-color: #FEE3BC; font-weight: bold; text-align: center; padding: 0px 10px 0px 10px; margin: 10px 0px 10px 0px;"><p style="margin: 4px 0px 4px 0px;">Non-standard</p></div>
* 
* <h2> <span> Summary </span></h2>
* <p>Returns a string representing the source code of the object.
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <td colspan="2" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Method of <a href="String" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String">String</a></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">Implemented in:</td>
* <td colspan="1" rowspan="1">JavaScript 1.3</td>
* </tr>
* </table>
* <h2> <span> Syntax </span></h2>
* <p><code><i>string</i>.toSource()</code>
* </p>
* <h2> <span> Parameters </span></h2>
* <p>None.
* </p>
* <h2> <span> Description </span></h2>
* <p>The <code>toSource</code> method returns the following values:
* </p>
* <ul><li> For the built-in <code>String</code> object, <code>toSource</code> returns the following string indicating that the source code is not available:
* </li></ul>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">function String() {[native code]}
* </pre>
* <ul><li> For instances of <code>String</code> or string literals, <code>toSource</code> returns a string representing the source code.
* </li></ul>
* <p>This method is usually called internally by JavaScript and not explicitly in code.
* </p>
* <h2> <span> See also </span></h2>
* <p><a href="Object:toSource" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object:toSource">Object.prototype.toSource</a>
* </p>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
toSource: function() {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span> Summary </span></h2>
* <p>Returns a string representing the specified object.
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <td colspan="2" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Method of <a href="String" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String">String</a></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">Implemented in:</td>
* <td colspan="1" rowspan="1">JavaScript 1.1, NES2.0</td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">ECMA Version:</td>
* <td colspan="1" rowspan="1">ECMA-262</td>
* </tr>
* </table>
* <h2> <span> Syntax </span></h2>
* <p><code>
* toString()
* </code>
* </p>
* <h2> <span> Parameters </span></h2>
* <p>None.
* </p>
* <h2> <span> Description </span></h2>
* <p>The <code>String</code> object overrides the <code>toString</code> method of the <code><a href="Core_JavaScript_1.5_Reference:Global_Objects:Object" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object">Object</a></code> object; it does not inherit <code><a href="Object:toString" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object:toString">Object.toString</a></code>. For <code>String</code> objects, the <code>toString</code> method returns a string representation of the object.
* </p>
* <h2> <span> Examples </span></h2>
* <h3> <span> Example: Using <code>toString</code> </span></h3>
* <p>The following example displays the string value of a String object:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* x = new String("Hello world");
* alert(x.toString())      // Displays "Hello world"
* </pre>
* <h2> <span> See also </span></h2>
* <p><a href="Core_JavaScript_1.5_Reference:Global_Objects:Object:toString" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object:toString">Object.toString</a>
* </p>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
 * @type String
*/
toString: function() {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span> Summary </span></h2>
* <p>Returns the calling string value converted to uppercase.
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <td colspan="2" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Method of <a href="String" shape="rect" title="Core JavaScript 1.5 Reference:Objects:String">String</a></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">Implemented in:</td>
* <td colspan="1" rowspan="1">JavaScript 1.0, NES2.0</td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">ECMA Version:</td>
* <td colspan="1" rowspan="1">ECMA-262</td>
* </tr>
* </table>
* <h2> <span> Syntax </span></h2>
* <p><code>
* toUpperCase()
* </code>
* </p>
* <h2> <span> Parameters </span></h2>
* <p>None.
* </p>
* <h2> <span> Description </span></h2>
* <p>The <code>toUpperCase</code> method returns the value of the string converted to uppercase.  <code>toUpperCase</code> does not affect the value of the string itself.
* </p>
* <h2> <span> Examples </span></h2>
* <h3> <span> Example: Using <code>toUpperCase</code> </span></h3>
* <p>The following example displays the string "<code>ALPHABET</code>":
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* var lowerText="alphabet"
* document.write(lowerText.toUpperCase())
* </pre>
* <h2> <span> See Also </span></h2>
* <p><a href="String:toLowerCase" shape="rect" title="Core JavaScript 1.5 Reference:Objects:String:toLowerCase">toLowerCase</a>
* </p>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
 * @type String
*/
toUpperCase: function() {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span> Summary </span></h2>
* <p>Returns the primitive value of a String object.
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <td colspan="2" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Method of <a href="String" shape="rect" title="Core JavaScript 1.5 Reference:Objects:String">String</a></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">Implemented in:</td>
* <td colspan="1" rowspan="1">JavaScript 1.1</td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">ECMA Version:</td>
* <td colspan="1" rowspan="1">ECMA-262</td>
* </tr>
* </table>
* <h2> <span> Syntax </span></h2>
* <p><code>
* valueOf()
* </code>
* </p>
* <h2> <span> Parameters </span></h2>
* <p>None.
* </p>
* <h2> <span> Description </span></h2>
* <p>The <code>valueOf</code> method of <code>String</code> returns the primitive value of a String object as a string data type. This value is equivalent to String.toString.
* </p><p>This method is usually called internally by JavaScript and not explicitly in code.
* </p>
* <h2> <span> Examples </span></h2>
* <h3> <span> Example: Using <code>valueOf</code> </span></h3>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* x = new String("Hello world");
* alert(x.valueOf())          // Displays "Hello world"
* </pre>
* <h2> <span> See Also </span></h2>
* <p><a href="String:toString" shape="rect" title="Core JavaScript 1.5 Reference:Objects:String:toString">toString</a>,
* <a href="Object:valueOf" shape="rect" title="Core JavaScript 1.5 Reference:Objects:Object:valueOf">Object.valueOf</a>
* </p>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
 * @type String
*/
valueOf: function() {
  // This is just a stub for a builtin native JavaScript object.
},
};
/**
* <div id="contentSub">(Redirected from <a href="http://developer.mozilla.org/en/docs/index.php?title=Core_JavaScript_1.5_Reference:Objects:String&amp;redirect=no" shape="rect" title="Core JavaScript 1.5 Reference:Objects:String">Core JavaScript 1.5 Reference:Objects:String</a>)</div>
* 
* <h2> <span> Summary </span></h2>
* <p><b>Core Object</b>
* </p><p>An object representing a series of characters in a string.
* </p>
* <h2> <span> Created by </span></h2>
* <p>The String constructor:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">new String(<i>string</i>)
* </pre>
* <h2> <span> Parameters </span></h2>
* <dl><dt style="font-weight:bold"> <code>string</code>Ê</dt><dd> Any string.
* </dd></dl>
* <h2> <span> Description </span></h2>
* <p>String objects are created by calling the constructor <code>new String()</code>:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">s_obj = new String("foo"); // creates a String object
* </pre>
* <p>The <code>String</code> object wraps Javascript's string primitive data type with the methods described below. The global function <code>String()</code> can also be called without <code>new</code> in front to create a primitive string:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">s_prim = String("bar"); //creates a primitive string
* </pre>
* <p>Literal strings in Javascript source code create primitive strings:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">s_also_prim = "foo"; // creates a primitive string
* </pre>
* <p>Because Javascript automatically converts between string primitives and String objects, you can call any of the methods of the <code>String</code> object on a string primitive. JavaScript automatically converts the string primitive to a temporary <code>String</code> object, calls the method, then discards the temporary <code>String</code> object. For example, you can use the <code>String.length</code> property on a string primitive created from a string literal:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">s_obj.length;       // 3
* s_prim.length;      // 3
* s_also_prim.length; // 3
* 'foo'.length;       // 3
* "foo".length;       // 3
* </pre>
* <p>(A string literal can use single or double quotation marks.)
* </p><p>String objects can be converted to primitive strings with <code>String.valueOf()</code>.
* </p><p>String primitives and String objects give different results when evaluated as Javascript. Primitives are treated as source code; String objects are treated as a character sequence object. For example:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">s1 = "2 + 2" // creates a string primitive
* s2 = new String("2 + 2") // creates a String object
* eval(s1)     // returns the number 4
* eval(s2)     // returns the string "2 + 2"
* eval(s2.valueOf()); // returns the number 4
* </pre>
* <p>You can convert the value of any object into a string using the global <a href="String" shape="rect" title="Core JavaScript 1.5 Reference:Functions:String">String</a> function.
* </p>
* <h3> <span> Accessing individual characters in a string </span></h3>
* <p>There are two ways to access an individual character in a string. The first is the <a href="Core_JavaScript_1.5_Reference:Global_Objects:String:charAt" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:charAt">charAt</a> method:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">'cat'.charAt(1) // returns "a"
* </pre>
* <p>The other way is to treat the string as an array, where each index corresponds to an individual character:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">'cat'[1] // returns "a"
* </pre>
* <p>Note: The second way (treating the string as an array) is not part of the ECMAScript; it's a JavaScript feature.
* </p><p>In both cases, attempting to set an individual character won't work. Trying to set a character through <code>charAt</code> results in an error:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var x = 'cat';
* x.charAt(0) = 'b'; // error
* </pre>
* <p>Setting a character via indexing does not throw an error, but the string itself is unchanged:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var x = 'cat';
* x[2] = 'p';
* alert(x);    // still outputs 'cat'
* alert(x[2]); // still outputs 't'
* </pre>
* <h3> <span> Comparing strings </span></h3>
* <p>C developers have the <code>strcmp()</code> function for comparing strings. In JavaScript, you just use the less-than and greater-than operators:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var a = "a";
* var b = "b";
* if (a &lt; b) // true
* document.write(a + " is less than " + b);
* else if (a &gt; b)
* document.write(a + " is greater than " + b);
* else
* document.write(a + " and " + b + " are equal.");
* </pre>
* <h2> <span> Properties </span></h2>
* <ul><li> <a href="String:constructor" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:constructor">constructor</a>: Specifies the function that creates an object's prototype.
* </li><li> <a href="String:length" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:length">length</a>: Reflects the length of the string.
* </li><li> <a href="String:prototype" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:prototype">prototype</a>: Allows the addition of properties to a String object.
* </li></ul>
* <h2> <span> Static methods </span></h2>
* <ul><li> <a href="Core_JavaScript_1.5_Reference:Global_Objects:String:fromCharCode" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:fromCharCode">fromCharCode</a>: Returns a string created by using the specified sequence of Unicode values.
* </li></ul>
* <h2> <span> Methods </span></h2>
* <h3> <span> Methods unrelated to HTML </span></h3>
* <ul><li> <a href="String:charAt" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:charAt">charAt</a>: Returns the character at the specified index.
* </li><li> <a href="String:charCodeAt" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:charCodeAt">charCodeAt</a>: Returns a number indicating the Unicode value of the character at the given index.
* </li><li> <a href="String:concat" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:concat">concat</a>: Combines the text of two strings and returns a new string.
* </li><li> <a href="String:indexOf" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:indexOf">indexOf</a>: Returns the index within the calling String object of the first occurrence of the specified value, or -1 if not found.
* </li><li> <a href="String:lastIndexOf" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:lastIndexOf">lastIndexOf</a>: Returns the index within the calling String object of the last occurrence of the specified value, or -1 if not found.
* </li><li> <a href="String:match" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:match">match</a>: Used to match a regular expression against a string.
* </li><li> <a href="String:replace" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:replace">replace</a>: Used to find a match between a regular expression and a string, and to replace the matched substring with a new substring.
* </li><li> <a href="String:search" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:search">search</a>: Executes the search for a match between a regular expression and a specified string.
* </li><li> <a href="String:slice" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:slice">slice</a>: Extracts a section of a string and returns a new string.
* </li><li> <a href="String:split" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:split">split</a>: Splits a String object into an array of strings by separating the string into substrings.
* </li><li> <a href="String:substr" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:substr">substr</a>: Returns the characters in a string beginning at the specified location through the specified number of characters.
* </li><li> <a href="String:substring" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:substring">substring</a>: Returns the characters in a string between two indexes into the string.
* </li><li> <a href="String:toLowerCase" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:toLowerCase">toLowerCase</a>: Returns the calling string value converted to lowercase.
* </li><li> <a href="String:toSource" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:toSource">toSource</a>: Returns an object literal representing the specified object; you can use this value to create a new object. Overrides the <a href="Object:toSource" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object:toSource">Object.toSource</a> method.
* </li><li> <a href="String:toString" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:toString">toString</a>: Returns a string representing the specified object. Overrides the <a href="Object:toString" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object:toString">Object.toString</a> method.
* </li><li> <a href="String:toUpperCase" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:toUpperCase">toUpperCase</a>: Returns the calling string value converted to uppercase.
* </li><li> <a href="String:valueOf" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:valueOf">valueOf</a>: Returns the primitive value of the specified object. Overrides the <a href="Object:valueOf" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object:valueOf">Object.valueOf</a> method.
* </li></ul>
* <h3> <span> HTML wrapper methods </span></h3>
* <div style="border: 1px solid #FFB752; background-color: #FEE3BC; font-weight: bold; text-align: center; padding: 0px 10px 0px 10px; margin: 10px 0px 10px 0px;"><p style="margin: 4px 0px 4px 0px;">Non-standard</p></div>
* <p>Each of the following methods returns a copy of the string wrapped inside an HTML tag. For example, "test".bold() returns "&lt;b&gt;test&lt;/b&gt;".
* </p>
* <ul><li> <a href="Core_JavaScript_1.5_Reference:Global_Objects:String:anchor" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:anchor">anchor</a>: <a href="http://www.w3.org/TR/html401/struct/links.html#adef-name-A" rel="nofollow" shape="rect" title="http://www.w3.org/TR/html401/struct/links.html#adef-name-A"><code>&lt;a name="<i>name</i>"&gt;</code></a> (hypertext target)
* </li><li> <a href="String:big" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:big">big</a>: <a href="http://www.w3.org/TR/html401/present/graphics.html#edef-BIG" rel="nofollow" shape="rect" title="http://www.w3.org/TR/html401/present/graphics.html#edef-BIG"><code>&lt;big&gt;</code></a>
* </li><li> <a href="String:blink" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:blink">blink</a>: <code>&lt;blink&gt;</code>
* </li><li> <a href="String:bold" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:bold">bold</a>: <a href="http://www.w3.org/TR/html401/present/graphics.html#edef-B" rel="nofollow" shape="rect" title="http://www.w3.org/TR/html401/present/graphics.html#edef-B"><code>&lt;b&gt;</code></a>
* </li><li> <a href="String:fixed" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:fixed">fixed</a>: <a href="http://www.w3.org/TR/html401/present/graphics.html#edef-TT" rel="nofollow" shape="rect" title="http://www.w3.org/TR/html401/present/graphics.html#edef-TT"><code>&lt;tt&gt;</code></a>
* </li><li> <a href="String:fontcolor" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:fontcolor">fontcolor</a>: <code>&lt;font color="<i>color</i>"&gt;</code>
* </li><li> <a href="String:fontsize" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:fontsize">fontsize</a>: <code>&lt;font size="<i>size</i>"&gt;</code>
* </li><li> <a href="String:italics" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:italics">italics</a>: <a href="http://www.w3.org/TR/html401/present/graphics.html#edef-I" rel="nofollow" shape="rect" title="http://www.w3.org/TR/html401/present/graphics.html#edef-I"><code>&lt;i&gt;</code></a>
* </li><li> <a href="String:link" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:link">link</a>: <a href="http://www.w3.org/TR/html401/struct/links.html#adef-href" rel="nofollow" shape="rect" title="http://www.w3.org/TR/html401/struct/links.html#adef-href"><code>&lt;a href="<i>url</i>"&gt;</code></a> (link to URL)
* </li><li> <a href="String:small" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:small">small</a>: <a href="http://www.w3.org/TR/html401/present/graphics.html#edef-SMALL" rel="nofollow" shape="rect" title="http://www.w3.org/TR/html401/present/graphics.html#edef-SMALL"><code>&lt;small&gt;</code></a>.
* </li><li> <a href="String:strike" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:strike">strike</a>: <a href="http://www.w3.org/TR/html401/present/graphics.html#edef-STRIKE" rel="nofollow" shape="rect" title="http://www.w3.org/TR/html401/present/graphics.html#edef-STRIKE"><code>&lt;strike&gt;</code></a>
* </li><li> <a href="String:sub" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:sub">sub</a>: <a href="http://www.w3.org/TR/html401/struct/text.html#edef-SUB" rel="nofollow" shape="rect" title="http://www.w3.org/TR/html401/struct/text.html#edef-SUB"><code>&lt;sub&gt;</code></a>
* </li><li> <a href="String:sup" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:sup">sup</a>: <a href="http://www.w3.org/TR/html401/struct/text.html#edef-SUP" rel="nofollow" shape="rect" title="http://www.w3.org/TR/html401/struct/text.html#edef-SUP"><code>&lt;sup&gt;</code></a>
* </li></ul>
* <p>These methods are of limited use, as they provide only a subset of the available HTML tags and attributes.
* </p>
* <h2> <span> Examples </span></h2>
* <h3> <span> Example: String primitive </span></h3>
* <p>The following statement creates a string primitive from the string literal "Schaefer", and assigns it to the variable <code>last_name</code>:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var last_name = "Schaefer";
* </pre>
* <h3> <span> Example: String primitive properties </span></h3>
* <p>The following expressions evaluate to <code>8</code>, "<code>SCHAEFER</code>", and "<code>schaefer</code>", respectively:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">last_name.length
* last_name.toUpperCase()
* last_name.toLowerCase()
* </pre>
* <h3> <span> Example: Setting an individual character in a string </span></h3>
* <p>An individual character cannot be directly set in a string. Instead, a new string can be created using the <a href="String:substr" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:substr">substr</a> or <a href="String:substring" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:substring">substring</a> methods:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">function setCharAt(str, index, ch) {
* return str.substr(0, index) + ch + str.substr(index + 1);
* }
* 
* alert(setCharAt('scam', 1, 'p')); // outputs "spam"
* </pre>
* <h3> <span> Example: Pass a string among scripts in different windows or frames </span></h3>
* <p>The following code creates two string variables and opens a second window:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var lastName = "Schaefer";
* var firstName = "Jesse";
* empWindow = window.open('string2.html', 'window1', 'width=300,height=300');
* </pre>
* <p>If the HTML source for the second window (<code>string2.html</code>) creates two string variables, <code>empLastName</code> and <code>empFirstName</code>, the following code in the first window assigns values to the second window's variables:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">empWindow.empFirstName = firstName;
* empWindow.empLastName = lastName;
* </pre>
* <p>The following code in the first window displays the values of the second window's variables:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">alert('empFirstName in empWindow is ' + empWindow.empFirstName);
* alert('empLastName in empWindow is ' + empWindow.empLastName);
* </pre>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
function String(val) {};
/**
* <div id="contentSub">(Redirected from <a href="http://developer.mozilla.org/en/docs/index.php?title=Core_JavaScript_1.5_Reference:Objects:String&amp;redirect=no" shape="rect" title="Core JavaScript 1.5 Reference:Objects:String">Core JavaScript 1.5 Reference:Objects:String</a>)</div>
* 
* <h2> <span> Summary </span></h2>
* <p><b>Core Object</b>
* </p><p>An object representing a series of characters in a string.
* </p>
* <h2> <span> Created by </span></h2>
* <p>The String constructor:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">new String(<i>string</i>)
* </pre>
* <h2> <span> Parameters </span></h2>
* <dl><dt style="font-weight:bold"> <code>string</code>Ê</dt><dd> Any string.
* </dd></dl>
* <h2> <span> Description </span></h2>
* <p>String objects are created by calling the constructor <code>new String()</code>:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">s_obj = new String("foo"); // creates a String object
* </pre>
* <p>The <code>String</code> object wraps Javascript's string primitive data type with the methods described below. The global function <code>String()</code> can also be called without <code>new</code> in front to create a primitive string:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">s_prim = String("bar"); //creates a primitive string
* </pre>
* <p>Literal strings in Javascript source code create primitive strings:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">s_also_prim = "foo"; // creates a primitive string
* </pre>
* <p>Because Javascript automatically converts between string primitives and String objects, you can call any of the methods of the <code>String</code> object on a string primitive. JavaScript automatically converts the string primitive to a temporary <code>String</code> object, calls the method, then discards the temporary <code>String</code> object. For example, you can use the <code>String.length</code> property on a string primitive created from a string literal:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">s_obj.length;       // 3
* s_prim.length;      // 3
* s_also_prim.length; // 3
* 'foo'.length;       // 3
* "foo".length;       // 3
* </pre>
* <p>(A string literal can use single or double quotation marks.)
* </p><p>String objects can be converted to primitive strings with <code>String.valueOf()</code>.
* </p><p>String primitives and String objects give different results when evaluated as Javascript. Primitives are treated as source code; String objects are treated as a character sequence object. For example:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">s1 = "2 + 2" // creates a string primitive
* s2 = new String("2 + 2") // creates a String object
* eval(s1)     // returns the number 4
* eval(s2)     // returns the string "2 + 2"
* eval(s2.valueOf()); // returns the number 4
* </pre>
* <p>You can convert the value of any object into a string using the global <a href="String" shape="rect" title="Core JavaScript 1.5 Reference:Functions:String">String</a> function.
* </p>
* <h3> <span> Accessing individual characters in a string </span></h3>
* <p>There are two ways to access an individual character in a string. The first is the <a href="Core_JavaScript_1.5_Reference:Global_Objects:String:charAt" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:charAt">charAt</a> method:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">'cat'.charAt(1) // returns "a"
* </pre>
* <p>The other way is to treat the string as an array, where each index corresponds to an individual character:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">'cat'[1] // returns "a"
* </pre>
* <p>Note: The second way (treating the string as an array) is not part of the ECMAScript; it's a JavaScript feature.
* </p><p>In both cases, attempting to set an individual character won't work. Trying to set a character through <code>charAt</code> results in an error:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var x = 'cat';
* x.charAt(0) = 'b'; // error
* </pre>
* <p>Setting a character via indexing does not throw an error, but the string itself is unchanged:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var x = 'cat';
* x[2] = 'p';
* alert(x);    // still outputs 'cat'
* alert(x[2]); // still outputs 't'
* </pre>
* <h3> <span> Comparing strings </span></h3>
* <p>C developers have the <code>strcmp()</code> function for comparing strings. In JavaScript, you just use the less-than and greater-than operators:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var a = "a";
* var b = "b";
* if (a &lt; b) // true
* document.write(a + " is less than " + b);
* else if (a &gt; b)
* document.write(a + " is greater than " + b);
* else
* document.write(a + " and " + b + " are equal.");
* </pre>
* <h2> <span> Properties </span></h2>
* <ul><li> <a href="String:constructor" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:constructor">constructor</a>: Specifies the function that creates an object's prototype.
* </li><li> <a href="String:length" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:length">length</a>: Reflects the length of the string.
* </li><li> <a href="String:prototype" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:prototype">prototype</a>: Allows the addition of properties to a String object.
* </li></ul>
* <h2> <span> Static methods </span></h2>
* <ul><li> <a href="Core_JavaScript_1.5_Reference:Global_Objects:String:fromCharCode" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:fromCharCode">fromCharCode</a>: Returns a string created by using the specified sequence of Unicode values.
* </li></ul>
* <h2> <span> Methods </span></h2>
* <h3> <span> Methods unrelated to HTML </span></h3>
* <ul><li> <a href="String:charAt" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:charAt">charAt</a>: Returns the character at the specified index.
* </li><li> <a href="String:charCodeAt" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:charCodeAt">charCodeAt</a>: Returns a number indicating the Unicode value of the character at the given index.
* </li><li> <a href="String:concat" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:concat">concat</a>: Combines the text of two strings and returns a new string.
* </li><li> <a href="String:indexOf" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:indexOf">indexOf</a>: Returns the index within the calling String object of the first occurrence of the specified value, or -1 if not found.
* </li><li> <a href="String:lastIndexOf" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:lastIndexOf">lastIndexOf</a>: Returns the index within the calling String object of the last occurrence of the specified value, or -1 if not found.
* </li><li> <a href="String:match" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:match">match</a>: Used to match a regular expression against a string.
* </li><li> <a href="String:replace" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:replace">replace</a>: Used to find a match between a regular expression and a string, and to replace the matched substring with a new substring.
* </li><li> <a href="String:search" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:search">search</a>: Executes the search for a match between a regular expression and a specified string.
* </li><li> <a href="String:slice" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:slice">slice</a>: Extracts a section of a string and returns a new string.
* </li><li> <a href="String:split" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:split">split</a>: Splits a String object into an array of strings by separating the string into substrings.
* </li><li> <a href="String:substr" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:substr">substr</a>: Returns the characters in a string beginning at the specified location through the specified number of characters.
* </li><li> <a href="String:substring" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:substring">substring</a>: Returns the characters in a string between two indexes into the string.
* </li><li> <a href="String:toLowerCase" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:toLowerCase">toLowerCase</a>: Returns the calling string value converted to lowercase.
* </li><li> <a href="String:toSource" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:toSource">toSource</a>: Returns an object literal representing the specified object; you can use this value to create a new object. Overrides the <a href="Object:toSource" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object:toSource">Object.toSource</a> method.
* </li><li> <a href="String:toString" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:toString">toString</a>: Returns a string representing the specified object. Overrides the <a href="Object:toString" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object:toString">Object.toString</a> method.
* </li><li> <a href="String:toUpperCase" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:toUpperCase">toUpperCase</a>: Returns the calling string value converted to uppercase.
* </li><li> <a href="String:valueOf" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:valueOf">valueOf</a>: Returns the primitive value of the specified object. Overrides the <a href="Object:valueOf" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object:valueOf">Object.valueOf</a> method.
* </li></ul>
* <h3> <span> HTML wrapper methods </span></h3>
* <div style="border: 1px solid #FFB752; background-color: #FEE3BC; font-weight: bold; text-align: center; padding: 0px 10px 0px 10px; margin: 10px 0px 10px 0px;"><p style="margin: 4px 0px 4px 0px;">Non-standard</p></div>
* <p>Each of the following methods returns a copy of the string wrapped inside an HTML tag. For example, "test".bold() returns "&lt;b&gt;test&lt;/b&gt;".
* </p>
* <ul><li> <a href="Core_JavaScript_1.5_Reference:Global_Objects:String:anchor" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:anchor">anchor</a>: <a href="http://www.w3.org/TR/html401/struct/links.html#adef-name-A" rel="nofollow" shape="rect" title="http://www.w3.org/TR/html401/struct/links.html#adef-name-A"><code>&lt;a name="<i>name</i>"&gt;</code></a> (hypertext target)
* </li><li> <a href="String:big" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:big">big</a>: <a href="http://www.w3.org/TR/html401/present/graphics.html#edef-BIG" rel="nofollow" shape="rect" title="http://www.w3.org/TR/html401/present/graphics.html#edef-BIG"><code>&lt;big&gt;</code></a>
* </li><li> <a href="String:blink" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:blink">blink</a>: <code>&lt;blink&gt;</code>
* </li><li> <a href="String:bold" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:bold">bold</a>: <a href="http://www.w3.org/TR/html401/present/graphics.html#edef-B" rel="nofollow" shape="rect" title="http://www.w3.org/TR/html401/present/graphics.html#edef-B"><code>&lt;b&gt;</code></a>
* </li><li> <a href="String:fixed" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:fixed">fixed</a>: <a href="http://www.w3.org/TR/html401/present/graphics.html#edef-TT" rel="nofollow" shape="rect" title="http://www.w3.org/TR/html401/present/graphics.html#edef-TT"><code>&lt;tt&gt;</code></a>
* </li><li> <a href="String:fontcolor" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:fontcolor">fontcolor</a>: <code>&lt;font color="<i>color</i>"&gt;</code>
* </li><li> <a href="String:fontsize" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:fontsize">fontsize</a>: <code>&lt;font size="<i>size</i>"&gt;</code>
* </li><li> <a href="String:italics" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:italics">italics</a>: <a href="http://www.w3.org/TR/html401/present/graphics.html#edef-I" rel="nofollow" shape="rect" title="http://www.w3.org/TR/html401/present/graphics.html#edef-I"><code>&lt;i&gt;</code></a>
* </li><li> <a href="String:link" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:link">link</a>: <a href="http://www.w3.org/TR/html401/struct/links.html#adef-href" rel="nofollow" shape="rect" title="http://www.w3.org/TR/html401/struct/links.html#adef-href"><code>&lt;a href="<i>url</i>"&gt;</code></a> (link to URL)
* </li><li> <a href="String:small" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:small">small</a>: <a href="http://www.w3.org/TR/html401/present/graphics.html#edef-SMALL" rel="nofollow" shape="rect" title="http://www.w3.org/TR/html401/present/graphics.html#edef-SMALL"><code>&lt;small&gt;</code></a>.
* </li><li> <a href="String:strike" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:strike">strike</a>: <a href="http://www.w3.org/TR/html401/present/graphics.html#edef-STRIKE" rel="nofollow" shape="rect" title="http://www.w3.org/TR/html401/present/graphics.html#edef-STRIKE"><code>&lt;strike&gt;</code></a>
* </li><li> <a href="String:sub" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:sub">sub</a>: <a href="http://www.w3.org/TR/html401/struct/text.html#edef-SUB" rel="nofollow" shape="rect" title="http://www.w3.org/TR/html401/struct/text.html#edef-SUB"><code>&lt;sub&gt;</code></a>
* </li><li> <a href="String:sup" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:sup">sup</a>: <a href="http://www.w3.org/TR/html401/struct/text.html#edef-SUP" rel="nofollow" shape="rect" title="http://www.w3.org/TR/html401/struct/text.html#edef-SUP"><code>&lt;sup&gt;</code></a>
* </li></ul>
* <p>These methods are of limited use, as they provide only a subset of the available HTML tags and attributes.
* </p>
* <h2> <span> Examples </span></h2>
* <h3> <span> Example: String primitive </span></h3>
* <p>The following statement creates a string primitive from the string literal "Schaefer", and assigns it to the variable <code>last_name</code>:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var last_name = "Schaefer";
* </pre>
* <h3> <span> Example: String primitive properties </span></h3>
* <p>The following expressions evaluate to <code>8</code>, "<code>SCHAEFER</code>", and "<code>schaefer</code>", respectively:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">last_name.length
* last_name.toUpperCase()
* last_name.toLowerCase()
* </pre>
* <h3> <span> Example: Setting an individual character in a string </span></h3>
* <p>An individual character cannot be directly set in a string. Instead, a new string can be created using the <a href="String:substr" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:substr">substr</a> or <a href="String:substring" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:substring">substring</a> methods:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">function setCharAt(str, index, ch) {
* return str.substr(0, index) + ch + str.substr(index + 1);
* }
* 
* alert(setCharAt('scam', 1, 'p')); // outputs "spam"
* </pre>
* <h3> <span> Example: Pass a string among scripts in different windows or frames </span></h3>
* <p>The following code creates two string variables and opens a second window:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var lastName = "Schaefer";
* var firstName = "Jesse";
* empWindow = window.open('string2.html', 'window1', 'width=300,height=300');
* </pre>
* <p>If the HTML source for the second window (<code>string2.html</code>) creates two string variables, <code>empLastName</code> and <code>empFirstName</code>, the following code in the first window assigns values to the second window's variables:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">empWindow.empFirstName = firstName;
* empWindow.empLastName = lastName;
* </pre>
* <p>The following code in the first window displays the values of the second window's variables:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">alert('empFirstName in empWindow is ' + empWindow.empFirstName);
* alert('empLastName in empWindow is ' + empWindow.empLastName);
* </pre>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
function String(string) {};

