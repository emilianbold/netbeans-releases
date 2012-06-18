/**
* <div id="contentSub">(Redirected from <a href="http://developer.mozilla.org/en/docs/index.php?title=Core_JavaScript_1.5_Reference:Objects:Array&amp;redirect=no" shape="rect" title="Core JavaScript 1.5 Reference:Objects:Array">Core JavaScript 1.5 Reference:Objects:Array</a>)</div>
* 
* <h2> <span> Summary </span></h2>
* <p><b>Core Object</b>
* </p><p>Lets you work with arrays.
* </p><p>See also: <a href="http://developer.mozilla.org/en/docs/New_in_JavaScript_1.6#Array_extras" shape="rect" title="New in JavaScript 1.6">New in JavaScript 1.6#Array extras</a> and <a href="http://developer.mozilla.org/en/docs/New_in_JavaScript_1.7#Array_comprehensions" shape="rect" title="New in JavaScript 1.7">New_in_JavaScript_1.7#Array_comprehensions</a>
* </p>
* <h2> <span> Created by </span></h2>
* <p>The <code>Array</code> object constructor:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">new Array(<i>arrayLength</i>)
* new Array(<i>element0</i>, <i>element1</i>, ..., <i>elementN</i>)
* </pre>
* <p>An array literal:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">[<i>element0</i>, <i>element1</i>, ..., <i>elementN</i>]
* </pre>
* <h2> <span> Parameters </span></h2>
* <dl><dt style="font-weight:bold"> <code>arrayLength</code></dt><dd> The initial length of the array. You can access this value using the length property. If the value specified is not a number, an array of length 1 is created, with the first element having the specified value. The maximum length allowed for an array is 4,294,967,295.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <code>element0, element1, ... , element<i>N</i></code></dt><dd> A list of values for the array's elements. When this form is specified, the array is initialized with the specified values as its elements, and the array's length property is set to the number of arguments.
* </dd></dl>
* <h2> <span> Description </span></h2>
* <p>An array is an ordered set of values associated with a single variable name. Note that you <a href="http://www.andrewdupont.net/2006/05/18/javascript-associative-arrays-considered-harmful/" rel="nofollow" shape="rect" title="http://www.andrewdupont.net/2006/05/18/javascript-associative-arrays-considered-harmful/">shouldn't use it as an associative array</a>, use <a href="Object" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object">Object</a> instead.
* </p><p>The following example creates an Array object with an array literal; the coffees array contains three elements and has a length of three:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var coffees = ["French Roast", "Columbian", "Kona"];
* </pre>
* <p>You can construct a dense array of two or more elements starting with index 0 if you define initial values for all elements. A dense array is one in which each element has a value. The following code creates a dense array with three elements:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var myArray = new Array("Hello", myVar, 3.14159);
* </pre>
* <h3> <span> Indexing an array </span></h3>
* <p>You index an array by its ordinal number. For example, assume you define the following array:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var myArray = new Array("Wind", "Rain", "Fire");
* </pre>
* <p>You can then refer to the elements as thus:
* </p>
* <ul><li> <code>myArray[0]</code> is the first element
* </li><li> <code>myArray[1]</code> is the second element
* </li><li> <code>myArray[2]</code> is the third element
* </li></ul>
* <h3> <span> Specifying a single parameter </span></h3>
* <p>When you specify a single numeric parameter with the <code>Array</code> constructor, you specify the initial length of the array. The following code creates an array of five elements:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var billingMethod = new Array(5);
* </pre>
* <p>The behavior of the <code>Array</code> constructor depends on whether the single parameter is a number.
* </p>
* <ul><li> If the value specified is a number, the constructor converts the number to an unsigned, 32-bit integer and generates an array with the length property (size of the array) set to the integer. The array initially contains no elements, even though it might have a non-zero length.
* </li></ul>
* <ul><li> If the value specified is not a number, an array of length 1 is created, with the first element having the specified value.
* </li></ul>
* <p>The following code creates an array of length 25, then assigns values to the first three elements:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var musicTypes = new Array(25);
* musicTypes[0] = "R&amp;B";
* musicTypes[1] = "Blues";
* musicTypes[2] = "Jazz";
* </pre>
* <h3> <span> Increasing the array length indirectly </span></h3>
* <p>An array's length increases if you assign a value to an element higher than the current length of the array. The following code creates an array of length 0, then assigns a value to element 99. This changes the length of the array to 100.
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var colors = new Array();
* colors[99] = "midnightblue";
* </pre>
* <h3> <span> Creating an array using the result of a match </span></h3>
* <p>The result of a match between a regular expression and a string can create an array. This array has properties and elements that provide information about the match. An array is the return value of RegExp.exec, String.match, and String.replace. To help explain these properties and elements, look at the following example and then refer to the table below:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">// Match one d followed by one or more b's followed by one d
* // Remember matched b's and the following d
* // Ignore case
* 
* var myRe = /d(b+)(d)/i;
* var myArray = myRe.exec("cdbBdbsbz");
* </pre>
* <p>The properties and elements returned from this match are as follows:
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* 
* <tr>
* <td colspan="1" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Property/Element
* </td><td colspan="1" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Description
* </td><td colspan="1" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Example
* </td></tr>
* 
* <tr>
* <td colspan="1" rowspan="1"><code>input</code>
* </td><td colspan="1" rowspan="1">A read-only property that reflects the original string against which the regular expression was matched.
* </td><td colspan="1" rowspan="1">cdbBdbsbz
* </td></tr>
* 
* <tr>
* <td colspan="1" rowspan="1"><code>index</code>
* </td><td colspan="1" rowspan="1">A read-only property that is the zero-based index of the match in the string.
* </td><td colspan="1" rowspan="1">1
* </td></tr>
* 
* <tr>
* <td colspan="1" rowspan="1"><code>[0]</code>
* </td><td colspan="1" rowspan="1">A read-only element that specifies the last matched characters.
* </td><td colspan="1" rowspan="1">dbBd
* </td></tr>
* 
* <tr>
* <td colspan="1" rowspan="1"><code>[1], ...[n]</code>
* </td><td colspan="1" rowspan="1">Read-only elements that specify the parenthesized substring matches, if included in the regular expression. The number of possible parenthesized substrings is unlimited.
* </td><td colspan="1" rowspan="1">[1]: bB[2]: d
* </td></tr>
* </table>
* <p>
* </p>
* <h2> <span> Properties </span></h2>
* <dl><dd><dl><dt style="font-weight:bold"> <a href="Core_JavaScript_1.5_Reference:Global_Objects:Array:constructor" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:constructor">constructor</a></dt><dd> Specifies the function that creates an object's prototype.
* </dd><dt style="font-weight:bold"> <a href="Array:index" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:index">index</a></dt><dd> For an array created by a regular expression match, the zero-based index of the match in the string.
* </dd><dt style="font-weight:bold"> <a href="Array:input" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:input">input</a></dt><dd>  For an array created by a regular expression match, reflects the original string against which the regular expression was matched.
* </dd><dt style="font-weight:bold"> <a href="Array:length" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:length">length</a></dt><dd> Reflects the number of elements in an array.
* </dd><dt style="font-weight:bold"> <a href="Array:prototype" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:prototype">prototype</a></dt><dd> Allows the addition of properties to all objects.
* </dd></dl>
* </dd></dl>
* <h2> <span> Methods </span></h2>
* <h3> <span> Mutator methods </span></h3>
* <p>These methods modify the array:
* </p>
* <dl><dd><dl><dt style="font-weight:bold"> <a href="Array:pop" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:pop">pop</a></dt><dd> Removes the last element from an array and returns that element.
* </dd><dt style="font-weight:bold"> <a href="Array:push" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:push">push</a></dt><dd> Adds one or more elements to the end of an array and returns the new length of the array.
* </dd><dt style="font-weight:bold"> <a href="Array:reverse" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:reverse">reverse</a></dt><dd> Reverses the order of the elements of an array -- the first becomes the last, and the last becomes the first.
* </dd><dt style="font-weight:bold"> <a href="Array:shift" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:shift">shift</a></dt><dd> Removes the first element from an array and returns that element.
* </dd><dt style="font-weight:bold"> <a href="Array:sort" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:sort">sort</a></dt><dd> Sorts the elements of an array.
* </dd><dt style="font-weight:bold"> <a href="Array:splice" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:splice">splice</a></dt><dd> Adds and/or removes elements from an array.
* </dd><dt style="font-weight:bold"> <a href="Array:unshift" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:unshift">unshift</a></dt><dd> Adds one or more elements to the front of an array and returns the new length of the array.
* </dd></dl>
* </dd></dl>
* <h3> <span> Accessor methods </span></h3>
* <p>These methods do not modify the array and return some representation of the array.
* </p>
* <dl><dd><dl><dt style="font-weight:bold"> <a href="Core_JavaScript_1.5_Reference:Global_Objects:Array:concat" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:concat">concat</a></dt><dd> Returns a new array comprised of this array joined with other array(s) and/or value(s).
* </dd><dt style="font-weight:bold"> <a href="Array:join" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:join">join</a></dt><dd> Joins all elements of an array into a string.
* </dd><dt style="font-weight:bold"> <a href="Array:slice" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:slice">slice</a></dt><dd> Extracts a section of an array and returns a new array.
* </dd><dt style="font-weight:bold"> <a href="Array:toSource" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:toSource">toSource</a></dt><dd> Returns an array literal representing the specified array; you can use this value to create a new array. Overrides the Object.toSource method.
* </dd><dt style="font-weight:bold"> <a href="Array:toString" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:toString">toString</a></dt><dd> Returns a string representing the array and its elements. Overrides the <a href="Object:toString" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object:toString">Object.toString</a> method.
* </dd><dt style="font-weight:bold"> <a href="Array:valueOf" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:valueOf">valueOf</a></dt><dd> Returns the primitive value of the array.  Overrides the <a href="Object:valueOf" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object:valueOf">Object.valueOf</a> method.
* </dd></dl>
* </dd></dl>
* <div>The following methods have been introduced in JavaScript 1.6. See <a href="http://developer.mozilla.org/en/docs/New_in_JavaScript_1.6" shape="rect" title="New in JavaScript 1.6">New in JavaScript 1.6</a> for more information.</div>
* <dl><dd><dl><dt style="font-weight:bold"> <a href="Array:indexOf" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:indexOf">indexOf</a></dt><dd> Returns the first (least) index of an element within the array equal to the specified value, or -1 if none is found.
* </dd><dt style="font-weight:bold"> <a href="Array:lastIndexOf" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:lastIndexOf">lastIndexOf</a></dt><dd> Returns the last (greatest) index of an element within the array equal to the specified value, or -1 if none is found.
* </dd></dl>
* </dd></dl>
* <h3> <span> Iteration methods </span></h3>
* <div>These methods were introduced in JavaScript 1.6. See <a href="http://developer.mozilla.org/en/docs/New_in_JavaScript_1.6" shape="rect" title="New in JavaScript 1.6">New in JavaScript 1.6</a> for more information.</div>
* <p>Several methods take as arguments functions to be called back while processing the array. When these methods are called, the <code>length</code> of the array is sampled, and any element added beyond this length from within the callback is not visited. Other changes to the array (setting the value of or deleting an element) may affect the results of the operation if the method visits the changed element afterwards. The specific behaviour of these methods in such cases is not always well-defined, and should not be relied upon.
* </p>
* <dl><dd><dl><dt style="font-weight:bold"> <a href="Array:filter" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:filter">filter</a></dt><dd> Creates a new array with all of the elements of this array for which the provided filtering function returns true.
* </dd><dt style="font-weight:bold"> <a href="Array:forEach" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:forEach">forEach</a></dt><dd> Calls a function for each element in the array.
* </dd><dt style="font-weight:bold"> <a href="Array:every" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:every">every</a></dt><dd> Returns true if every element in this array satisfies the provided testing function.
* </dd><dt style="font-weight:bold"> <a href="Array:map" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:map">map</a></dt><dd> Creates a new array with the results of calling a provided function on every element in this array.
* </dd><dt style="font-weight:bold"> <a href="Array:some" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:some">some</a></dt><dd> Returns true if at least one element in this array satisfies the provided testing function.
* </dd></dl>
* </dd></dl>
* <div>The following methods were introduced in JavaScript 1.8. See <a href="http://developer.mozilla.org/en/docs/New_in_JavaScript_1.8" shape="rect" title="New in JavaScript 1.8">New in JavaScript 1.8</a> for more information.</div>
* <dl><dd><dl><dt style="font-weight:bold"> <a href="Array:reduce" shape="rect" title="Core JavaScript 1.5 Reference:Objects:Array:reduce">reduce</a></dt><dd> Apply a function simultaneously against two values of the array (from left-to-right) as to reduce it to a single value.
* </dd><dt style="font-weight:bold"> <a href="Array:reduceRight" shape="rect" title="Core JavaScript 1.5 Reference:Objects:Array:reduceRight">reduceRight</a></dt><dd> Apply a function simultaneously against two values of the array (from right-to-left) as to reduce it to a single value.
* </dd></dl>
* </dd></dl>
* <h3> <span> Generic methods </span></h3>
* <p>Many methods on the JavaScript Array object are designed to be generally applied to all objects which "look like" Arrays.  That is, they can be used on any object which has a <code>length</code> property, and which can usefully be accessed using numeric property names (as with <code>array[5]</code> indexing).
* </p><p>TODO: give examples with Array.prototype.forEach.call, and adding the method to an object like <a href="Core_JavaScript_1.5_Reference:Global_Objects:JavaArray" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:JavaArray">JavaArray</a> or <a href="String" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String">String</a>.
* </p><p>Some methods, such as <a href="Array:join" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:join">join</a>, only read the <code>length</code> and numeric properties of the object they are called on.  Others, like <a href="Array:reverse" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:reverse">reverse</a>, require that the object's numeric properties and <code>length</code> be mutable; these methods can therefore not be called on objects like <a href="String" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String">String</a>, which does not permit its <code>length</code> property or synthesized numeric properties to be set.
* </p><p>The methods that work on any Array-like object and do <b>not</b> need to alter <code>length</code> or numeric properties are:
* </p>
* <ul><li> <a href="Array:concat" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:concat">concat</a>
* </li><li> <a href="Array:every" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:every">every</a> (JS 1.6+)
* </li><li> <a href="Array:filter" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:filter">filter</a> (JS 1.6+)
* </li><li> <a href="Array:forEach" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:forEach">forEach</a> (JS 1.6+)
* </li><li> <a href="Array:indexOf" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:indexOf">indexOf</a> (JS 1.6+)
* </li><li> <a href="Array:join" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:join">join</a>
* </li><li> <a href="Array:lastIndexOf" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:lastIndexOf">lastIndexOf</a> (JS 1.6+)
* </li><li> <a href="Array:map" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:map">map</a> (JS 1.6+)
* </li><li> <a href="Array:slice" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:slice">slice</a>
* </li><li> <a href="Array:some" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:some">some</a> (JS 1.6+)
* </li><li> <a href="Array:toSource" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:toSource">toSource</a>
* </li><li> <a href="Array:toString" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:toString">toString</a>
* </li><li> <a href="Array:valueOf" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:valueOf">valueOf</a>
* </li></ul>
* <p>The methods that alter the <code>length</code> or numeric properties of the object they are called on are:
* </p>
* <ul><li> <a href="Array:pop" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:pop">pop</a>
* </li><li> <a href="Array:push" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:push">push</a>
* </li><li> <a href="Array:reverse" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:reverse">reverse</a>
* </li><li> <a href="Array:shift" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:shift">shift</a>
* </li><li> <a href="Array:sort" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:sort">sort</a>
* </li><li> <a href="Array:splice" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:splice">splice</a>
* </li><li> <a href="Array:unshift" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:unshift">unshift</a>
* </li></ul>
* <p>This example shows how to use <a href="Array:map" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:map">map</a> on a <a href="String" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String">string</a> object to get an array of bytes in the ASCII encoding representing the character values:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* var a = Array.prototype.map.call("Hello World",
* function(x) { return x.charCodeAt(0); })
* // a now equals [72,101,108,108,111,32,87,111,114,108,100]
* </pre>
* <h2> <span> Examples </span></h2>
* <h3> <span> Example: Creating an Array </span></h3>
* <p>The following example creates an array, <code>msgArray</code>, with a length of 0, then assigns values to <code>msgArray[0]</code> and <code>msgArray[99]</code>, changing the length of the array to 100.
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var msgArray = new Array();
* msgArray[0] = "Hello";
* msgArray[99] = "world";
* // The following statement is true,
* // because defined msgArray[99] element.
* if (msgArray.length == 100)
* myVar = "The length is 100.";
* </pre>
* <h3> <span> Example: Creating a Two-dimensional Array </span></h3>
* <p>The following creates chess board as a two dimensional array of strings.
* The first move is made by copying the 'P' in 1,4 to 3,4.
* The position 1,4 is left blank.
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var board =
* [ ['R','N','B','Q','K','B','N','R'],
* ['P','P','P','P','P','P','P','P'],
* [' ',' ',' ',' ',' ',' ',' ',' '],
* [' ',' ',' ',' ',' ',' ',' ',' '],
* [' ',' ',' ',' ',' ',' ',' ',' '],
* [' ',' ',' ',' ',' ',' ',' ',' '],
* ['p','p','p','p','p','p','p','p'],
* ['r','n','b','q','k','b','n','r']];
* print(board.join('\n'));
* print('\n\n\n');
* 
* // Move King's Pawn forward 2
* board[3][4] = board[1][4];
* board[1][4] = ' ';
* print(board.join('\n'));
* </pre>
* <p>Here is the output:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">R,N,B,Q,K,B,N,R
* P,P,P,P,P,P,P,P
* , , , , , , ,
* , , , , , , ,
* , , , , , , ,
* , , , , , , ,
* p,p,p,p,p,p,p,p
* r,n,b,q,k,b,n,r
* 
* R,N,B,Q,K,B,N,R
* P,P,P,P, ,P,P,P
* , , , , , , ,
* , , , ,P, , ,
* , , , , , , ,
* , , , , , , ,
* p,p,p,p,p,p,p,p
* r,n,b,q,k,b,n,r
* </pre>
* <h2> <span> See Also </span></h2>
* <p><a href="Core_JavaScript_1.5_Guide:Predefined_Core_Objects:Array_Object" shape="rect" title="Core JavaScript 1.5 Guide:Predefined Core Objects:Array Object">Core JavaScript 1.5 Guide:Predefined Core Objects:Array Object</a>
* </p>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
var Array = {
  // This is just a stub for a builtin native JavaScript object.
/**
* <h2> <span> Summary </span></h2>
* <p>Returns a new array comprised of this array joined with other array(s) and/or value(s).
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <td colspan="2" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Method of <a href="Array" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array">Array</a></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">Implemented in:</td>
* <td colspan="1" rowspan="1">JavaScript 1.2, NES 3.0</td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">ECMA Version:</td>
* <td colspan="1" rowspan="1">ECMA-262</td>
* </tr>
* </table>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var resultArray = <i>array</i>.concat(<i>value1</i>, <i>value2</i>, ..., <i>valueN</i>);
* </pre>
* <h2> <span> Parameters </span></h2>
* <dl><dt style="font-weight:bold"> <code>value<i>N</i></code></dt><dd> Arrays and/or values to concatenate to the resulting array.
* </dd></dl>
* <h2> <span> Description </span></h2>
* <p><code>concat</code> creates a new array consisting of the elements in the <code>this</code> object on which it is called, followed in order by, for each argument, the elements of that argument (if the argument is an array) or the argument itself (if the argument is not an array).
* </p><p><code>concat</code> does not alter <code>this</code> or any of the arrays provided as arguments but instead returns a "one level deep" copy that contains copies of the same elements combined from the original arrays.  Elements of the original arrays are copied into the new array as follows:
* </p>
* <ul><li> Object references (and not the actual object): <code>concat</code> copies object references into the new array. Both the original and new array refer to the same object. That is, if a referenced object is modified, the changes are visible to both the new and original arrays.
* </li></ul>
* <ul><li> Strings and numbers (not <a href="Core_JavaScript_1.5_Reference:Global_Objects:String" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String">String</a> and <a href="Number" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Number">Number</a> objects): <code>concat</code> copies the values of strings and numbers into the new array.
* </li></ul>
* <p>Any operation on the new array will have no effect on the original arrays, and vice versa.
* </p>
* <h2> <span> Examples </span></h2>
* <h3> <span> Example: Concatenating two arrays </span></h3>
* <p>The following code concatenates two arrays:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var alpha = ["a", "b", "c"];
* var numeric = [1, 2, 3];
* 
* // creates array ["a", "b", "c", 1, 2, 3]; alpha and numeric are unchanged
* var alphaNumeric = alpha.concat(numeric);
* </pre>
* <h3> <span> Example: Concatenating three arrays </span></h3>
* <p>The following code concatenates three arrays:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var num1 = [1, 2, 3];
* var num2 = [4, 5, 6];
* var num3 = [7, 8, 9];
* 
* // creates array [1, 2, 3, 4, 5, 6, 7, 8, 9]; num1, num2, num3 are unchanged
* var nums = num1.concat(num2, num3);
* </pre>
* <h3> <span> Example: Concatenating values to an array </span></h3>
* <p>The following code concatenates three values to an array:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var alpha = ['a', 'b', 'c'];
* 
* // creates array ["a", "b", "c", 1, 2, 3], leaving alpha unchanged
* var alphaNumeric = alpha.concat(1, [2, 3]);
* </pre>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
 * @type Array
*/
concat: function(value1, value2,valueN) {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span> Summary </span></h2>
* <p>Returns a reference to the <a href="Array" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array">Array</a> function that created the instance's prototype. Note that the value of this property is a reference to the function itself, not a string containing the function's name.
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <td colspan="2" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Property of <a href="Array" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array">Array</a></td>
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
* <p>See <a href="Core_JavaScript_1.5_Reference:Global_Objects:Object:constructor" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object:constructor">Object.constructor</a>.
* </p>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
constructor: undefined,
/**
* <h2> <span> Summary </span></h2>
* <p>Tests whether all elements in the array pass the test implemented by the provided function.
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <td colspan="2" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Method of <a href="Array" shape="rect" title="Core JavaScript 1.5 Reference:Objects:Array">Array</a></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">Implemented in:</td>
* <td colspan="1" rowspan="1">JavaScript 1.6 (Gecko 1.8b2 and later)</td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">ECMAScript Edition:</td>
* <td colspan="1" rowspan="1">none</td>
* </tr>
* </table>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var <i>allPassed</i> = <i>array</i>.every(<i>callback</i>[, <i>thisObject</i>]);
* </pre>
* <h2> <span> Parameters </span></h2>
* <dl><dt style="font-weight:bold"> <code>callback</code></dt><dd> Function to test for each element.
* </dd><dt style="font-weight:bold"> <code>thisObject</code></dt><dd> Object to use as <code>this</code> when executing <code>callback</code>.
* </dd></dl>
* <h2> <span> Description </span></h2>
* <p><code>every</code> executes the provided <code>callback</code> function once for each element present in the array until it finds one where <code>callback</code> returns a false value.  If such an element is found, the <code>every</code> method immediately returns <code>false</code>.  Otherwise, if <code>callback</code> returned a true value for all elements, <code>every</code> will return <code>true</code>.  <code>callback</code> is invoked only for indexes of the array which have assigned values; it is not invoked for indexes which have been deleted or which have never been assigned values.
* </p><p><code>callback</code> is invoked with three arguments: the value of the element, the index of the element, and the Array object being traversed.
* </p><p>If a <code>thisObject</code> parameter is provided to <code>every</code>, it will be used as the <code>this</code> for each invocation of the <code>callback</code>.  If it is not provided, or is <code>null</code>, the global object associated with <code>callback</code> is used instead.
* </p><p><code>every</code> does not mutate the array on which it is called.
* </p><p>The range of elements processed by <code>every</code> is set before the first invocation of <code>callback</code>.  Elements which are appended to the array after the call to <code>every</code> begins will not be visited by <code>callback</code>.  If existing elements of the array are changed, their value as passed to <code>callback</code> will be the value at the time <code>every</code> visits them; elements that are deleted are not visited.
* </p><p><code>every</code> acts like the "for all" quantifier in mathematics.  In particular, for an empty array, it returns true.  (It is <a href="http://en.wikipedia.org/wiki/Vacuous_truth#Vacuous_truths_in_mathematics" rel="nofollow" shape="rect" title="http://en.wikipedia.org/wiki/Vacuous_truth#Vacuous_truths_in_mathematics">vacuously true</a> that all elements of the <a href="http://en.wikipedia.org/wiki/Empty_set#Common_problems" rel="nofollow" shape="rect" title="http://en.wikipedia.org/wiki/Empty_set#Common_problems">empty set</a> satisfy any given condition.)
* </p>
* <h2> <span> Compatibility </span></h2>
* <p><code>every</code> is a JavaScript extension to the ECMA-262 standard; as such it may not be present in other implementations of the standard.  You can work around this by inserting the following code at the beginning of your scripts, allowing use of <code>every</code> in ECMA-262 implementations which do not natively support it.  This algorithm is exactly the one used in Firefox and SpiderMonkey.
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">if (!Array.prototype.every)
* {
* Array.prototype.every = function(fun / *, thisp* /)
* {
* var len = this.length;
* if (typeof fun!= "function")
* throw new TypeError();
* 
* var thisp = arguments[1];
* for (var i = 0; i &lt; len; i++)
* {
* if (i in this &amp;&amp;
* !fun.call(thisp, this[i], i, this))
* return false;
* }
* 
* return true;
* };
* }
* </pre>
* <h2> <span> Examples </span></h2>
* <h3> <span> Example: Testing size of all array elements </span></h3>
* <p>The following example tests whether all elements in the array are bigger than 10.
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">function isBigEnough(element, index, array) {
* return (element &gt;= 10);
* }
* var passed = [12, 5, 8, 130, 44].every(isBigEnough);
* // passed is false
* passed = [12, 54, 18, 130, 44].every(isBigEnough);
* // passed is true
* </pre>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
every: function(callback, thisObject) {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span> Summary </span></h2>
* <p>Creates a new array with all elements that pass the test implemented by the provided function.
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <td colspan="2" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Method of <a href="Array" shape="rect" title="Core JavaScript 1.5 Reference:Objects:Array">Array</a></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">Implemented in:</td>
* <td colspan="1" rowspan="1">JavaScript 1.6 (Gecko 1.8b2 and later)</td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">ECMAScript Edition:</td>
* <td colspan="1" rowspan="1">none</td>
* </tr>
* </table>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var <i>filteredArray</i> = <i>array</i>.filter(<i>callback</i>[, <i>thisObject</i>]);
* </pre>
* <h2> <span> Parameters </span></h2>
* <dl><dt style="font-weight:bold"> <code>callback</code></dt><dd> Function to test each element of the array.
* </dd><dt style="font-weight:bold"> <code>thisObject</code></dt><dd> Object to use as <code>this</code> when executing <code>callback</code>.
* </dd></dl>
* <h2> <span> Description </span></h2>
* <p><code>filter</code> calls a provided <code>callback</code> function once for each element in an array, and constructs a new array of all the values for which <code>callback</code> returns a true value.  <code>callback</code> is invoked only for indexes of the array which have assigned values; it is not invoked for indexes which have been deleted or which have never been assigned values.  Array elements which do not pass the <code>callback</code> test are simply skipped, and are not included in the new array.
* </p><p><code>callback</code> is invoked with three arguments: the value of the element, the index of the element, and the Array object being traversed.
* </p><p>If a <code>thisObject</code> parameter is provided to <code>filter</code>, it will be used as the <code>this</code> for each invocation of the <code>callback</code>.  If it is not provided, or is <code>null</code>, the global object associated with <code>callback</code> is used instead.
* </p><p><code>filter</code> does not mutate the array on which it is called.
* </p><p>The range of elements processed by <code>filter</code> is set before the first invocation of <code>callback</code>. Elements which are appended to the array after the call to <code>filter</code> begins will not be visited by <code>callback</code>. If existing elements of the array are changed, or deleted, their value as passed to <code>callback</code> will be the value at the time <code>filter</code> visits them; elements that are deleted are not visited.
* </p>
* <h2> <span> Compatibility </span></h2>
* <p><code>filter</code> is a JavaScript extension to the ECMA-262 standard; as such it may not be present in other implementations of the standard.  You can work around this by inserting the following code at the beginning of your scripts, allowing use of <code>filter</code> in ECMA-262 implementations which do not natively support it.  This algorithm is exactly the one used in Firefox and SpiderMonkey.
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">if (!Array.prototype.filter)
* {
* Array.prototype.filter = function(fun / *, thisp* /)
* {
* var len = this.length;
* if (typeof fun!= "function")
* throw new TypeError();
* 
* var res = new Array();
* var thisp = arguments[1];
* for (var i = 0; i &lt; len; i++)
* {
* if (i in this)
* {
* var val = this[i]; // in case fun mutates this
* if (fun.call(thisp, val, i, this))
* res.push(val);
* }
* }
* 
* return res;
* };
* }
* </pre>
* <h2> <span> Examples </span></h2>
* <h3> <span> Example: Filtering out all small values </span></h3>
* <p>The following example uses <code>filter</code> to create a filtered array that has all elements with values less than 10 removed.
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">function isBigEnough(element, index, array) {
* return (element &gt;= 10);
* }
* var filtered = [12, 5, 8, 130, 44].filter(isBigEnough);
* </pre>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
filter: undefined,
/**
* <h2> <span> Summary </span></h2>
* <p>Executes a provided function once per array element.
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <td colspan="2" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Method of <a href="Array" shape="rect" title="Core JavaScript 1.5 Reference:Objects:Array">Array</a></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">Implemented in:</td>
* <td colspan="1" rowspan="1"><a href="http://developer.mozilla.org/en/docs/New_in_JavaScript_1.6" shape="rect" title="New in JavaScript 1.6">JavaScript 1.6</a> (Gecko 1.8b2 and later)</td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">ECMAScript Edition:</td>
* <td colspan="1" rowspan="1">none</td>
* </tr>
* </table>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve"><i>array</i>.forEach(<i>callback</i>[, <i>thisObject</i>]);
* </pre>
* <h2> <span> Parameters </span></h2>
* <dl><dt style="font-weight:bold"> <code>callback</code></dt><dd> Function to execute for each element.
* </dd><dt style="font-weight:bold"> <code>thisObject</code></dt><dd> Object to use as <code>this</code> when executing <code>callback</code>.
* </dd></dl>
* <h2> <span> Description </span></h2>
* <p><code>forEach</code> executes the provided function (<code>callback</code>) once for each element present in the array.  <code>callback</code> is invoked only for indexes of the array which have assigned values; it is not invoked for indexes which have been deleted or which have never been assigned values.
* </p><p><code>callback</code> is invoked with three arguments: the value of the element, the index of the element, and the Array object being traversed.
* </p><p>If a <code>thisObject</code> parameter is provided to <code>forEach</code>, it will be used as the <code>this</code> for each invocation of the <code>callback</code>.  If it is not provided, or is <code>null</code>, the global object associated with <code>callback</code> is used instead.
* </p><p><code>forEach</code> does not mutate the array on which it is called.
* </p><p>The range of elements processed by <code>forEach</code> is set before the first invocation of <code>callback</code>.  Elements which are appended to the array after the call to <code>forEach</code> begins will not be visited by <code>callback</code>.  If existing elements of the array are changed, or deleted, their value as passed to <code>callback</code> will be the value at the time <code>forEach</code> visits them; elements that are deleted are not visited.
* </p>
* <h2> <span> Compatibility </span></h2>
* <p><code>forEach</code> is a JavaScript extension to the ECMA-262 standard; as such it may not be present in other implementations of the standard.  You can work around this by inserting the following code at the beginning of your scripts, allowing use of <code>forEach</code> in ECMA-262 implementations which do not natively support it.  This algorithm is exactly the one used in Firefox and SpiderMonkey.
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">if (!Array.prototype.forEach)
* {
* Array.prototype.forEach = function(fun / *, thisp* /)
* {
* var len = this.length;
* if (typeof fun!= "function")
* throw new TypeError();
* 
* var thisp = arguments[1];
* for (var i = 0; i &lt; len; i++)
* {
* if (i in this)
* fun.call(thisp, this[i], i, this);
* }
* };
* }
* </pre>
* <h2> <span> Examples </span></h2>
* <h3> <span> Example: Printing the contents of an array </span></h3>
* <p>The following code prints a line for each element in an array:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">function printElt(element, index, array) {
* print("[" + index + "] is " + element); // assumes print is already defined
* }
* [2, 5, 9].forEach(printElt);
* // Prints:
* // [0] is 2
* // [1] is 5
* // [2] is 9
* </pre>
* <h3> <span> Example: Printing the contents of an array with an object method </span></h3>
* <p>The following code creates a simple writer object and then uses the <code>writeln</code> method to write one line per element in the array:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var writer = {
* sb:       [],
* write:    function (s) {
* this.sb.push(s);
* },
* writeln:  function (s) {
* this.write(s + "\n");
* },
* toString: function () {
* return this.sb.join("");
* }
* };
* 
* [2, 5, 9].forEach(writer.writeln, writer);
* print(writer.toString()); // assumes print is already defined
* // Prints:
* // 2
* // 5
* // 9
* </pre>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
forEach: function(callback, thisObject) {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span> Summary </span></h2>
* <p>For an array created by a regular expression match, the zero-based index of the match in the string.
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <td colspan="2" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Property of <a href="Array" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array">Array</a></td>
* </tr>
* <tr>
* <td colspan="2" rowspan="1"><b>Static</b></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">Implemented in:</td>
* <td colspan="1" rowspan="1">JavaScript 1.2, NES 3.0</td>
* </tr>
* </table>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
index: undefined,
/**
* <h2> <span> Summary </span></h2>
* <p>Returns the first index at which a given element can be found in the array, or -1 if it is not present.
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <td colspan="2" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Method of <a href="Array" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array">Array</a></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">Implemented in:</td>
* <td colspan="1" rowspan="1"><a href="http://developer.mozilla.org/en/docs/New_in_JavaScript_1.6" shape="rect" title="New in JavaScript 1.6">JavaScript 1.6</a> (Gecko 1.8b2 and later)</td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">ECMAScript Edition:</td>
* <td colspan="1" rowspan="1">none</td>
* </tr>
* </table>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var index = <i>array</i>.indexOf(<i>searchElement</i>[, <i>fromIndex</i>]);
* </pre>
* <h2> <span> Parameters </span></h2>
* <dl><dt style="font-weight:bold"> <code>searchElement</code></dt><dd> Element to locate in the array.
* </dd><dt style="font-weight:bold"> <code>fromIndex</code></dt><dd> The index at which to begin the search. Defaults to 0, i.e. the whole array will be searched. If the index is greater than or equal to the length of the array, -1 is returned, i.e. the array will not be searched. If negative, it is taken as the offset from the end of the array. Note that even when the index is negative, the array is still searched from front to back. If the calculated index is less than 0, the whole array will be searched.
* </dd></dl>
* <h2> <span> Description </span></h2>
* <p><code>indexOf</code> compares <code>searchElement</code> to elements of the Array using <a href="Core_JavaScript_1.5_Reference:Operators:Comparison_Operators#Using_the_Equality_Operators" shape="rect" title="Core JavaScript 1.5 Reference:Operators:Comparison Operators">strict equality</a> (the same method used by the ===, or triple-equals, operator).
* </p>
* <h2> <span> Compatibility </span></h2>
* <p><code>indexOf</code> is a JavaScript extension to the ECMA-262 standard; as such it may not be present in other implementations of the standard. You can work around this by inserting the following code at the beginning of your scripts, allowing use of <code>indexOf</code> in ECMA-262 implementations which do not natively support it.  This algorithm is exactly the one used in Firefox and SpiderMonkey.
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">if (!Array.prototype.indexOf)
* {
* Array.prototype.indexOf = function(elt / *, from* /)
* {
* var len = this.length;
* 
* var from = Number(arguments[1]) || 0;
* from = (from &lt; 0)
* ? Math.ceil(from)
* : Math.floor(from);
* if (from &lt; 0)
* from += len;
* 
* for (; from &lt; len; from++)
* {
* if (from in this &amp;&amp;
* this[from] === elt)
* return from;
* }
* return -1;
* };
* }
* </pre>
* <p>Again, note that this implementation aims for absolute compatibility with <code>indexOf</code> in Firefox and the SpiderMonkey JavaScript engine, including in cases where the index passed to <code>indexOf</code> is not an integer value.  If you intend to use this in real-world applications, you may not need all of the code to calculate <code>from</code>.
* </p>
* <h2> <span> Examples </span></h2>
* <h3> <span> Example: Using indexOf </span></h3>
* <p>The following example uses <code>indexOf</code> to locate values in an array.
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var array = [2, 5, 9];
* var index = array.indexOf(2);
* // index is 0
* index = array.indexOf(7);
* // index is -1
* </pre>
* <h3> <span> Example: Finding all the occurrences of an element </span></h3>
* <p>The following example uses <code>indexOf</code> to find all the indices of an element in a given array, using <a href="Array:push" shape="rect" title="Core JavaScript 1.5 Reference:Objects:Array:push">push</a> to add them to another array as they are found.
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var indices = [];
* var idx = array.indexOf(element)
* while (idx!= -1)
* {
* indices.push(idx);
* idx = array.indexOf(element, idx + 1);
* }
* </pre>
* <h2> <span> See also </span></h2>
* <p><a href="Core_JavaScript_1.5_Reference:Global_Objects:Array:lastIndexOf" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:lastIndexOf">lastIndexOf</a>
* </p>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
indexOf: function(searchElement, fromIndex) {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span> Summary </span></h2>
* <p>For an array created by a regular expression match, reflect the original string against which the regular expression was matched.
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <td colspan="2" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Property of <a href="Array" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array">Array</a></td>
* </tr>
* <tr>
* <td colspan="2" rowspan="1"><b>Static</b></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">Implemented in:</td>
* <td colspan="1" rowspan="1">JavaScript 1.2, NES 3.0</td>
* </tr>
* </table>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
input: undefined,
/**
* <h2> <span> Summary </span></h2>
* <p>Joins all elements of an array into a string.
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <td colspan="2" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Method of <a href="Array" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array">Array</a></td>
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
* <h2> <span> Syntax </span></h2>
* <p><code>
* join(<i>separator</i>)
* </code>
* </p>
* <h2> <span> Parameters </span></h2>
* <dl><dt style="font-weight:bold"> <code>separator</code></dt><dd> Specifies a string to separate each element of the array.  The separator is converted to a string if necessary. If omitted, the array elements are sparated with a comma.
* </dd></dl>
* <h2> <span> Description </span></h2>
* <p>The string conversions of all array elements are joined into one string.
* </p>
* <h2> <span> Examples </span></h2>
* <h3> <span> Example: Joining an array three different ways </span></h3>
* <p>The following example creates an array, <code>a</code>, with three elements, then joins the array three times: using the default separator, then a comma and a space, and then a plus.
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var a = new Array("Wind","Rain","Fire");
* var myVar1 = a.join();      // assigns "Wind,Rain,Fire" to myVar1
* var myVar2 = a.join(", ");  // assigns "Wind, Rain, Fire" to myVar2
* var myVar3 = a.join(" + "); // assigns "Wind + Rain + Fire" to myVar3
* </pre>
* <h2> <span> See Also </span></h2>
* <p><a href="Array:reverse" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:reverse">reverse</a>,
* <a href="String:split" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:split">String:split</a>,
* <a href="Array:toString" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:toString">toString</a>
* </p>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
 * @type String
*/
join: function(separator) {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span> Summary </span></h2>
* <p>Returns the last index at which a given element can be found in the array, or -1 if it is not present. The array is searched backwards, starting at <code>fromIndex</code>.
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <td colspan="2" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Method of <a href="Array" shape="rect" title="Core JavaScript 1.5 Reference:Objects:Array">Array</a></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">Implemented in:</td>
* <td colspan="1" rowspan="1">JavaScript 1.6 (Gecko 1.8b2 and later)</td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">ECMAScript Edition:</td>
* <td colspan="1" rowspan="1">none</td>
* </tr>
* </table>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var index = <i>array</i>.lastIndexOf(<i>searchElement</i>[, <i>fromIndex</i>]);
* </pre>
* <h2> <span> Parameters </span></h2>
* <dl><dt style="font-weight:bold"> <code>searchElement</code></dt><dd> Element to locate in the array.
* </dd><dt style="font-weight:bold"> <code>fromIndex</code></dt><dd> The index at which to start searching backwards. Defaults to the array's length, i.e. the whole array will be searched. If the index is greater than or equal to the length of the array, the whole array will be searched. If negative, it is taken as the offset from the end of the array. Note that even when the index is negative, the array is still searched from back to front. If the calculated index is less than 0, -1 is returned, i.e. the array will not be searched.
* </dd></dl>
* <h2> <span> Description </span></h2>
* <p><code>lastIndexOf</code> compares <code>searchElement</code> to elements of the Array using strict equality (the same method used by the ===, or triple-equals, operator).
* </p>
* <h2> <span> Compatibility </span></h2>
* <p><code>lastIndexOf</code> is a JavaScript extension to the ECMA-262 standard; as such it may not be present in other implementations of the standard.  You can work around this by inserting the following code at the beginning of your scripts, allowing use of <code>lastIndexOf</code> in ECMA-262 implementations which do not natively support it.  This algorithm is exactly the one used in Firefox and SpiderMonkey.
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">if (!Array.prototype.lastIndexOf)
* {
* Array.prototype.lastIndexOf = function(elt / *, from* /)
* {
* var len = this.length;
* 
* var from = Number(arguments[1]);
* if (isNaN(from))
* {
* from = len - 1;
* }
* else
* {
* from = (from &lt; 0)
* ? Math.ceil(from)
* : Math.floor(from);
* if (from &lt; 0)
* from += len;
* else if (from &gt;= len)
* from = len - 1;
* }
* 
* for (; from &gt; -1; from--)
* {
* if (from in this &amp;&amp;
* this[from] === elt)
* return from;
* }
* return -1;
* };
* }
* </pre>
* <p>Again, note that this implementation aims for absolute compatibility with <code>lastIndexOf</code> in Firefox and the SpiderMonkey JavaScript engine, including in several cases which are arguably edge cases.  If you intend to use this in real-world applications, you may be able to calculate <code>from</code> with less complicated code if you ignore those cases.
* </p>
* <h2> <span> Examples </span></h2>
* <h3> <span> Example: Using lastIndexOf </span></h3>
* <p>The following example uses <code>lastIndexOf</code> to locate values in an array.
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var array = [2, 5, 9, 2];
* var index = array.lastIndexOf(2);
* // index is 3
* index = array.lastIndexOf(7);
* // index is -1
* index = array.lastIndexOf(2, 3);
* // index is 3
* index = array.lastIndexOf(2, 2);
* // index is 0
* index = array.lastIndexOf(2, -2);
* // index is 0
* index = array.lastIndexOf(2, -1);
* // index is 3
* </pre>
* <h3> <span> Example: Finding all the occurrences of an element </span></h3>
* <p>The following example uses <code>lastIndexOf</code> to find all the indices of an element in a given array, using <a href="Core_JavaScript_1.5_Reference:Objects:Array:push" shape="rect" title="Core JavaScript 1.5 Reference:Objects:Array:push">push</a> to add them to another array as they are found.
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var indices = [];
* var idx = array.lastIndexOf(element);
* while (idx!= -1)
* {
* indices.push(idx);
* idx = (idx &gt; 0? array.lastIndexOf(element, idx - 1): -1);
* }
* </pre>
* <p>Note that we have to handle the case <code>idx == 0</code> separately here because the element will always be found regardless of the <code>fromIndex</code> parameter if it is the first element of the array. This is different from the <a href="Array:indexOf" shape="rect" title="Core JavaScript 1.5 Reference:Objects:Array:indexOf">indexOf</a> method.
* </p>
* <h2> <span> See also </span></h2>
* <p><a href="Core_JavaScript_1.5_Reference:Global_Objects:Array:indexOf" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:indexOf">indexOf</a>
* </p>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
lastIndexOf: function(searchElement, fromIndex) {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span> Summary </span></h2>
* <p>An unsigned, 32-bit integer that specifies the number of elements in an array.
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <td colspan="2" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Property of <a href="Array" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array">Array</a></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">Implemented in:</td>
* <td colspan="1" rowspan="1">JavaScript 1.1, NES 2.0
* <p>JavaScript 1.3: <code>length</code> is an unsigned, 32-bit integer with a value less than 2<sup>32</sup>.
* </p>
* </td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">ECMA Version:</td>
* <td colspan="1" rowspan="1">ECMA-262</td>
* </tr>
* </table>
* <h2> <span> Description </span></h2>
* <p>The value of the <code>length</code> property is an integer with a positive sign and a value less than 2 to the 32 power (2<sup>32</sup>).
* </p><p>You can set the <code>length</code> property to truncate an array at any time. When you extend an array by changing its <code>length</code> property, the number of actual elements does not increase; for example, if you set <code>length</code> to 3 when it is currently 2, the array still contains only 2 elements.
* </p>
* <h2> <span> Examples </span></h2>
* <h3> <span> Example: Iterating over an array </span></h3>
* <p>In the following example the array <code>numbers</code> is iterated through by looking at the <code>length</code> property to see how many elements it has. Each value is then doubled.
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* var numbers = [1,2,3,4,5];
* for (var i = 0; i &lt; numbers.length; i++) {
* numbers[i] *= 2;
* }
* // numbers is now [2,4,6,8,10];
* </pre>
* <h3> <span> Example: Shortening an array </span></h3>
* <p>The following example shortens the array <code>statesUS</code> to a length of 50 if the current length is greater than 50.
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* if (statesUS.length &gt; 50) {
* statesUS.length=50
* }
* </pre>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
 * @type Number
*/
length: undefined,
/**
* <h2> <span> Summary </span></h2>
* <p>Creates a new array with the results of calling a provided function on every element in this array.
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <td colspan="2" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Method of <a href="Array" shape="rect" title="Core JavaScript 1.5 Reference:Objects:Array">Array</a></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">Implemented in:</td>
* <td colspan="1" rowspan="1">JavaScript 1.6 (Gecko 1.8b2 and later)</td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">ECMAScript Edition:</td>
* <td colspan="1" rowspan="1">none</td>
* </tr>
* </table>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var <i>mappedArray</i> = <i>array</i>.map(<i>callback</i>[, <i>thisObject</i>]);
* </pre>
* <h2> <span> Parameters </span></h2>
* <dl><dt style="font-weight:bold"> <code>callback</code></dt><dd> Function produce an element of the new Array from an element of the current one.
* </dd><dt style="font-weight:bold"> <code>thisObject</code></dt><dd> Object to use as <code>this</code> when executing <code>callback</code>.
* </dd></dl>
* <h2> <span> Description </span></h2>
* <p><code>map</code> calls a provided <code>callback</code> function once for each element in an array, in order, and constructs a new array from the results.  <code>callback</code> is invoked only for indexes of the array which have assigned values; it is not invoked for indexes which have been deleted or which have never been assigned values.
* </p><p><code>callback</code> is invoked with three arguments: the value of the element, the index of the element, and the Array object being traversed.
* </p><p>If a <code>thisObject</code> parameter is provided to <code>map</code>, it will be used as the <code>this</code> for each invocation of the <code>callback</code>.  If it is not provided, or is <code>null</code>, the global object associated with <code>callback</code> is used instead.
* </p><p><code>map</code> does not mutate the array on which it is called.
* </p><p>The range of elements processed by <code>map</code> is set before the first invocation of <code>callback</code>.  Elements which are appended to the array after the call to <code>map</code> begins will not be visited by <code>callback</code>.  If existing elements of the array are changed, or deleted, their value as passed to <code>callback</code> will be the value at the time <code>map</code> visits them; elements that are deleted are not visited.
* </p>
* <h2> <span> Compatibility </span></h2>
* <p><code>map</code> is a JavaScript extension to the ECMA-262 standard; as such it may not be present in other implementations of the standard.  You can work around this by inserting the following code at the beginning of your scripts, allowing use of <code>map</code> in ECMA-262 implementations which do not natively support it.  This algorithm is exactly the one used in Firefox and SpiderMonkey.
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">if (!Array.prototype.map)
* {
* Array.prototype.map = function(fun / *, thisp* /)
* {
* var len = this.length;
* if (typeof fun!= "function")
* throw new TypeError();
* 
* var res = new Array(len);
* var thisp = arguments[1];
* for (var i = 0; i &lt; len; i++)
* {
* if (i in this)
* res[i] = fun.call(thisp, this[i], i, this);
* }
* 
* return res;
* };
* }
* </pre>
* <h2> <span> Examples </span></h2>
* <h3> <span> Example: Pluralizing the words (strings) in an array </span></h3>
* <p>The following code creates an array of "plural" forms of nouns from an array of their singular forms.
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">function makePseudoPlural(single)
* {
* return single.replace(/o/g, "e");
* }
* 
* var singles = ["foot", "goose", "moose"];
* var plurals = singles.map(makePseudoPlural);
* // plurals is ["feet", "geese", "meese"]
* // singles is unchanged
* </pre>
* <h3> <span> Example: Mapping an array of numbers to an array of square roots </span></h3>
* <p>The following code takes an array of numbers and creates a new array containing the square roots of the numbers in the first array.
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var numbers = [1, 4, 9];
* var roots = numbers.map(Math.sqrt);
* // roots is now [1, 2, 3]
* // numbers is still [1, 4, 9]
* </pre>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
map: undefined,
/**
* <h2> <span> Summary </span></h2>
* <p>Removes the last element from an array and returns that element. This method changes the length of the array.
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <td colspan="2" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Method of <a href="Array" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array">Array</a></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">Implemented in:</td>
* <td colspan="1" rowspan="1">JavaScript 1.2, NES 3.0</td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">ECMA Version:</td>
* <td colspan="1" rowspan="1">ECMA-262 Edition 3</td>
* </tr>
* </table>
* <h2> <span> Syntax </span></h2>
* <p><code>
* pop()
* </code>
* </p>
* <h2> <span> Parameters </span></h2>
* <p>None.
* </p>
* <h2> <span> Example </span></h2>
* <h3> <span> Example: Removing the last element of an array </span></h3>
* <p>The following code creates the myFish array containing four elements, then removes its last element.
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* myFish = ["angel", "clown", "mandarin", "surgeon"];
* popped = myFish.pop();
* </pre>
* <h2> <span> See Also </span></h2>
* <p><a href="Core_JavaScript_1.5_Reference:Global_Objects:Array:push" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:push">push</a>,
* <a href="Array:shift" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:shift">shift</a>,
* <a href="Array:unshift" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:unshift">unshift</a>
* </p>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
pop: function() {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span> Summary </span></h2>
* <p>Represents the prototype for this class. You can use the prototype to add properties or methods to all instances of a class. For information on prototypes, see <a href="Function:prototype" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Function:prototype">Function.prototype</a>.
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <td colspan="2" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Property of <a href="Array" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array">Array</a></td>
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
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
prototype: undefined,
/**
* <h2> <span> Summary </span></h2>
* <p>Adds one or more elements to the end of an array and returns the new length of the array.  This method changes the length of the array.
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <td colspan="2" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Method of <a href="Array" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array">Array</a></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">Implemented in:</td>
* <td colspan="1" rowspan="1">JavaScript 1.2, NES 3.0
* <p>JavaScript 1.3: <code>push</code> returns the new length of the array rather than the last element added to the array.
* </p>
* </td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">ECMA Version:</td>
* <td colspan="1" rowspan="1">ECMA-262 Edition 3</td>
* </tr>
* </table>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var newlen = <i>array</i>.push(<i>element1</i>, ..., <i>elementN</i>);
* </pre>
* <h2> <span> Parameters </span></h2>
* <dl><dt style="font-weight:bold"> <code>element1, ..., element<i>N</i></code></dt><dd> The elements to add to the end of the array.
* </dd></dl>
* <h2> <span> Description </span></h2>
* <p>The behavior of the <code>push</code> method is analogous to the <code>push</code> function in Perl 4. Note that this behavior is different in Perl 5.
* </p>
* <h2> <span> Backward Compatibility </span></h2>
* <h3> <span> JavaScript 1.2 </span></h3>
* <p>The <code>push</code> method returns the last element added to an array.
* </p>
* <h2> <span> Examples </span></h2>
* <h3> <span> Example: Adding elements to an array </span></h3>
* <p>The following code creates the <code>myFish</code> array containing two elements, then adds two elements to it. After the code executes, <code>pushed</code> contains 4. (In JavaScript 1.2, <code>pushed</code> contains "lion" after the code executes.)
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var myFish = ["angel", "clown"];
* var pushed = myFish.push("drum", "lion");
* </pre>
* <h2> <span> See Also </span></h2>
* <p><a href="Array:pop" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:pop">pop</a>,
* <a href="Array:shift" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:shift">shift</a>,
* <a href="Array:unshift" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:unshift">unshift</a>
* </p>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
push: function(element1,elementN) {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span> Summary </span></h2>
* <p>Apply a function simultaneously against two values of the array (from left-to-right) as to reduce it to a single value.
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <td colspan="2" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Method of <a href="Array" shape="rect" title="Core JavaScript 1.5 Reference:Objects:Array">Array</a></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">Implemented in:</td>
* <td colspan="1" rowspan="1">JavaScript 1.8 (Gecko 1.9a5 and later)</td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">ECMAScript Edition:</td>
* <td colspan="1" rowspan="1">none</td>
* </tr>
* </table>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var <i>result</i> = <i>array</i>.reduce(<i>callback</i>[, <i>initialValue</i>]);
* </pre>
* <h2> <span> Parameters </span></h2>
* <dl><dt style="font-weight:bold"> <code>callback</code></dt><dd> Function to execute on each value in the array.
* </dd><dt style="font-weight:bold"> <code>initialValue</code></dt><dd> Object to use as the first argument to the first call of the <code>callback</code>.
* </dd></dl>
* <h2> <span> Description </span></h2>
* <p><code>reduce</code> executes the <code>callback</code> function once for each element present in the array, excluding holes in the array, receiving four arguments: the initial value (or value from the previous <code>callback</code> call), the value of the current element, the current index, and the array over which iteration is occurring.
* </p><p>The call to the reduce <code>callback</code> would look something like this:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">.reduce(function(previousValue, currentValue, index, array){
* // ...
* })
* </pre>
* <p>The first time the function is called, the <code>previousValue</code> and <code>currentValue</code> can be one of two values. If an <code>initialValue</code> was provided in the call to <code>reduce</code>, then <code>previousValue</code> will be equal to <code>initialValue</code> and <code>currentValue</code> will be equal to the first value in the array. If no <code>initialValue</code> was provided, then <code>previousValue</code> will be equal to the first value in the array and <code>currentValue</code> will be equal to the second.
* </p><p>Some example run-throughs of the function would look like this:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">[0,1,2,3,4].reduce(function(previousValue, currentValue, index, array){
* return previousValue + currentValue;
* });
* 
* // First call
* previousValue = 0, currentValue = 1, index = 1
* 
* // Second call
* previousValue = 1, currentValue = 2, index = 2
* 
* // Third call
* previousValue = 3, currentValue = 3, index = 3
* 
* // Fourth call
* previousValue = 6, currentValue = 4, index = 4
* 
* // array is always the object [0,1,2,3,4] upon which reduce was called
* 
* // Return Value: 10
* </pre>
* <p>And if you were to provide an <code>initialValue</code>, the result would look like this:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">[0,1,2,3,4].reduce(function(previousValue, currentValue, index, array){
* return previousValue + currentValue;
* }, 10);
* 
* // First call
* previousValue = 10, currentValue = 0, index = 0
* 
* // Second call
* previousValue = 10, currentValue = 1, index = 1
* 
* // Third call
* previousValue = 11, currentValue = 2, index = 2
* 
* // Fourth call
* previousValue = 13, currentValue = 3, index = 3
* 
* // Fifth call
* previousValue = 16, currentValue = 4, index = 4
* 
* // array is always the object [0,1,2,3,4] upon which reduce was called
* 
* // Return Value: 20
* </pre>
* <h2> <span> Compatibility </span></h2>
* <p><code>reduce</code> is a JavaScript extension to the ECMA-262 standard; as such it may not be present in other implementations of the standard.  You can work around this by inserting the following code at the beginning of your scripts, allowing use of <code>reduce</code> in ECMA-262 implementations which do not natively support it.  This algorithm is exactly the one used in Firefox and SpiderMonkey.
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">if (!Array.prototype.reduce)
* {
* Array.prototype.reduce = function(fun / *, initial* /)
* {
* var len = this.length;
* if (typeof fun!= "function")
* throw new TypeError();
* 
* // no value to return if no initial value and an empty array
* if (len == 0 &amp;&amp; arguments.length == 1)
* throw new TypeError();
* 
* var i = 0;
* if (arguments.length &gt;= 2)
* {
* var rv = arguments[1];
* }
* else
* {
* do
* {
* if (i in this)
* {
* rv = this[i++];
* break;
* }
* 
* // if array contains no values, no initial value to return
* if (++i &gt;= len)
* throw new TypeError();
* }
* while (true);
* }
* 
* for (; i &lt; len; i++)
* {
* if (i in this)
* rv = fun.call(null, rv, this[i], i, this);
* }
* 
* return rv;
* };
* }
* </pre>
* <h2> <span> Examples </span></h2>
* <h3> <span> Example: Sum up all values within an array </span></h3>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var total = [0, 1, 2, 3].reduce(function(a, b){ return a + b; });
* // total == 6
* </pre>
* <h3> <span> Example: Flatten an array of arrays </span></h3>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var flattened = [[0,1], [2,3], [4,5]].reduce(function(a,b) {
* return a.concat(b);
* }, []);
* // flattened is [0, 1, 2, 3, 4, 5]
* </pre>
* <h2> <span> See Also </span></h2>
* <p><a href="Array:reduceRight" shape="rect" title="Core JavaScript 1.5 Reference:Objects:Array:reduceRight">reduceRight</a>
* </p>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
reduce: function(callback, initialValue) {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span> Summary </span></h2>
* <p>Apply a function simultaneously against two values of the array (from right-to-left) as to reduce it to a single value.
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <td colspan="2" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Method of <a href="Array" shape="rect" title="Core JavaScript 1.5 Reference:Objects:Array">Array</a></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">Implemented in:</td>
* <td colspan="1" rowspan="1">JavaScript 1.8 (Gecko 1.9a5 and later)</td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">ECMAScript Edition:</td>
* <td colspan="1" rowspan="1">none</td>
* </tr>
* </table>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var <i>result</i> = <i>array</i>.reduceRight(<i>callback</i>[, <i>initialValue</i>]);
* </pre>
* <h2> <span> Parameters </span></h2>
* <dl><dt style="font-weight:bold"> <code>callback</code></dt><dd> Function to execute on each value in the array.
* </dd><dt style="font-weight:bold"> <code>initialValue</code></dt><dd> Object to use as the first argument to the first call of the <code>callback</code>.
* </dd></dl>
* <h2> <span> Description </span></h2>
* <p><code>reduceRight</code> executes the callback function once for each element present in the array, excluding holes in the array, receiving four arguments: the initial value (or value from the previous callback call), the value of the current element, the current index, and the array over which iteration is occurring.
* </p><p>The call to the reduceRight <code>callback</code> would look something like this:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">.reduceRight(function(previousValue, currentValue, index, array){
* // ...
* })
* </pre>
* <p>The first time the function is called, the <code>previousValue</code> and <code>currentValue</code> can be one of two values. If an <code>initialValue</code> was provided in the call to <code>reduceRight</code>, then <code>previousValue</code> will be equal to <code>initialValue</code> and <code>currentValue</code> will be equal to the last value in the array. If no <code>initialValue</code> was provided, then <code>previousValue</code> will be equal to the last value in the array and <code>currentValue</code> will be equal to the second-to-last value.
* </p><p>Some example run-throughs of the function would look like this:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">[0,1,2,3,4].reduceRight(function(previousValue, currentValue, index, array){
* return previousValue + currentValue;
* });
* 
* // First call
* previousValue = 4, currentValue = 3, index = 3
* 
* // Second call
* previousValue = 7, currentValue = 2, index = 2
* 
* // Third call
* previousValue = 9, currentValue = 1, index = 1
* 
* // Fourth call
* previousValue = 10, currentValue = 0, index = 0
* 
* // array is always the object [0,1,2,3,4] upon which reduceRight was called
* 
* // Return Value: 10
* </pre>
* <p>And if you were to provide an <code>initialValue</code>, the result would look like this:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">[0,1,2,3,4].reduceRight(function(previousValue, currentValue, index, array){
* return previousValue + currentValue;
* }, 10);
* 
* // First call
* previousValue = 10, currentValue = 4, index = 4
* 
* // Second call
* previousValue = 14, currentValue = 3, index = 3
* 
* // Third call
* previousValue = 17, currentValue = 2, index = 2
* 
* // Fourth call
* previousValue = 19, currentValue = 1, index = 1
* 
* // Fifth call
* previousValue = 20, currentValue = 0, index = 0
* 
* // array is always the object [0,1,2,3,4] upon which reduceRight was called
* 
* // Return Value: 20
* </pre>
* <h2> <span> Compatibility </span></h2>
* <p><code>reduceRight</code> is a JavaScript extension to the ECMA-262 standard; as such it may not be present in other implementations of the standard.  You can work around this by inserting the following code at the beginning of your scripts, allowing use of <code>reduceRight</code> in ECMA-262 implementations which do not natively support it.  This algorithm is exactly the one used in Firefox and SpiderMonkey.
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">if (!Array.prototype.reduceRight)
* {
* Array.prototype.reduceRight = function(fun / *, initial* /)
* {
* var len = this.length;
* if (typeof fun!= "function")
* throw new TypeError();
* 
* // no value to return if no initial value, empty array
* if (len == 0 &amp;&amp; arguments.length == 1)
* throw new TypeError();
* 
* var i = len - 1;
* if (arguments.length &gt;= 2)
* {
* var rv = arguments[1];
* }
* else
* {
* do
* {
* if (i in this)
* {
* rv = this[i--];
* break;
* }
* 
* // if array contains no values, no initial value to return
* if (--i &lt; 0)
* throw new TypeError();
* }
* while (true);
* }
* 
* for (; i &gt;= 0; i--)
* {
* if (i in this)
* rv = fun.call(null, rv, this[i], i, this);
* }
* 
* return rv;
* };
* }
* </pre>
* <h2> <span> Examples </span></h2>
* <h3> <span> Example: Sum up all values within an array </span></h3>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var total = [0, 1, 2, 3].reduceRight(function(a, b) { return a + b; });
* // total == 6
* </pre>
* <h3> <span> Example: Flatten an array of arrays </span></h3>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var flattened = [[0, 1], [2, 3], [4, 5]].reduceRight(function(a, b) {
* return a.concat(b);
* }, []);
* // flattened is [4, 5, 2, 3, 0, 1]
* </pre>
* <h2> <span> See Also </span></h2>
* <p><a href="Array:reduce" shape="rect" title="Core JavaScript 1.5 Reference:Objects:Array:reduce">reduce</a>
* </p>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
reduceRight: function(callback, initialValue) {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span> Summary </span></h2>
* <p>Transposes the elements of an array: the first array element becomes the last and the last becomes the first.
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <td colspan="2" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Method of <a href="Array" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array">Array</a></td>
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
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve"><i>array</i>.reverse();
* </pre>
* <h2> <span> Parameters </span></h2>
* <p>None.
* </p>
* <h2> <span> Description </span></h2>
* <p>The <code>reverse</code> method transposes the elements of the calling array object in place, mutating the array.
* </p>
* <h2> <span> Examples </span></h2>
* <h3> <span> Example: Reversing the elements in an array </span></h3>
* <p>The following example creates an array myArray, containing three elements, then reverses the array.
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var myArray = ["one", "two", "three"];
* myArray.reverse();
* </pre>
* <p>This code changes <code>myArray</code> so that:
* </p>
* <ul><li> <code>myArray[0]</code> is "three"
* </li><li> <code>myArray[1]</code> is "two"
* </li><li> <code>myArray[2]</code> is "one"
* </li></ul>
* <h2> <span> See Also </span></h2>
* <p><a href="Array:join" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:join">join</a>,
* <a href="Array:sort" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:sort">sort</a>
* </p>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
 * @type void
*/
reverse: function() {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span> Summary </span></h2>
* <p>Removes the first element from an array and returns that element. This method changes the length of the array.
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <td colspan="2" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Method of <a href="Array" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array">Array</a></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">Implemented in:</td>
* <td colspan="1" rowspan="1">JavaScript 1.2, NES 3.0</td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">ECMA Version:</td>
* <td colspan="1" rowspan="1">ECMA-262 Edition 3</td>
* </tr>
* </table>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var firstElement = <i>array</i>.shift();
* </pre>
* <h2> <span> Parameters </span></h2>
* <p>None.
* </p>
* <h2> <span> Examples </span></h2>
* <h3> <span> Example: Removing an element from an array </span></h3>
* <p>The following code displays the <code>myFish</code> array before and after removing its first element. It also displays the removed element:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">// assumes a print function is defined
* var myFish = ["angel", "clown", "mandarin", "surgeon"];
* print("myFish before: " + myFish);
* var shifted = myFish.shift();
* print("myFish after: " + myFish);
* print("Removed this element: " + shifted);
* </pre>
* <p>This example displays the following:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">myFish before: ["angel", "clown", "mandarin", "surgeon"]
* myFish after: ["clown", "mandarin", "surgeon"]
* Removed this element: angel
* </pre>
* <h2> <span> See Also </span></h2>
* <p><a href="Core_JavaScript_1.5_Reference:Global_Objects:Array:pop" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:pop">pop</a>,
* <a href="Array:push" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:push">push</a>,
* <a href="Array:unshift" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:unshift">unshift</a>
* </p>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
 * @type void
*/
shift: function() {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span> Summary </span></h2>
* <p>Extracts a section of an array and returns a new array.
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <td colspan="2" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Method of <a href="Array" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array">Array</a></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">Implemented in:</td>
* <td colspan="1" rowspan="1">JavaScript 1.2, NES 3.0</td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">ECMA Version:</td>
* <td colspan="1" rowspan="1">ECMA-262 Edition 3</td>
* </tr>
* </table>
* <h2> <span> Syntax </span></h2>
* <p><code>
* slice(<i>begin</i>[,<i>end</i>])
* </code>
* </p>
* <h2> <span> Parameters </span></h2>
* <dl><dt style="font-weight:bold"> <code>begin</code></dt><dd> Zero-based index at which to begin extraction.
* </dd><dd> As a negative index, <code>start</code> indicates an offset from the end of the sequence.  <code>slice(-2)</code> extracts the second-to-last element and the last element in the sequence.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <code>end</code></dt><dd> Zero-based index at which to end extraction.  <code>slice</code> extracts up to but not including <code>end</code>.
* </dd><dd> <code>slice(1,4)</code> extracts the second element through the fourth element (elements indexed 1, 2, and 3).
* </dd><dd> As a negative index, <code>end</code> indicates an offset from the end of the sequence.  <code>slice(2,-1)</code> extracts the third element through the second-to-last element in the sequence.
* </dd><dd> If <code>end</code> is omitted, <code>slice</code> extracts to the end of the sequence.
* </dd></dl>
* <h2> <span> Description </span></h2>
* <p><code>slice</code> does not alter the original array, but returns a new "one level deep" copy that contains copies of the elements sliced from the original array. Elements of the original array are copied into the new array as follows:
* </p>
* <ul><li> For object references (and not the actual object), <code>slice</code> copies object references into the new array. Both the original and new array refer to the same object. If a referenced object changes, the changes are visible to both the new and original arrays.
* </li></ul>
* <ul><li> For strings and numbers (not <a href="Core_JavaScript_1.5_Reference:Global_Objects:String" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String">String</a> and <a href="Number" shape="rect" title="Core JavaScript 1.5 Reference:Objects:Number">Number</a> objects), <code>slice</code> copies strings and numbers into the new array. Changes to the string or number in one array does not affect the other array.
* </li></ul>
* <p>If a new element is added to either array, the other array is not affected.
* </p>
* <h2> <span> Examples </span></h2>
* <h3> <span> Example: Using <code>slice</code> </span></h3>
* <p>In the following example, <code>slice</code> creates a new array, <code>newCar</code>, from <code>myCar</code>. Both include a reference to the object <code>myHonda</code>. When the color of <code>myHonda</code> is changed to purple, both arrays reflect the change.
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* 
* // Using slice, create newCar from myCar.
* var myHonda = { color: "red", wheels: 4, engine: { cylinders: 4, size: 2.2 } };
* var myCar = [myHonda, 2, "cherry condition", "purchased 1997"];
* var newCar = myCar.slice(0, 2);
* 
* // Print the values of myCar, newCar, and the color of myHonda
* //  referenced from both arrays.
* print("myCar = " + myCar.toSource());
* print("newCar = " + newCar.toSource());
* print("myCar[0].color = " + myCar[0].color);
* print("newCar[0].color = " + newCar[0].color);
* 
* // Change the color of myHonda.
* myHonda.color = "purple";
* print("The new color of my Honda is " + myHonda.color);
* 
* // Print the color of myHonda referenced from both arrays.
* print("myCar[0].color = " + myCar[0].color);
* print("newCar[0].color = " + newCar[0].color);
* </pre>
* <p>This script writes:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* myCar = [{color:"red", wheels:4, engine:{cylinders:4, size:2.2}}, 2, "cherry condition", "purchased 1997"]
* newCar = [{color:"red", wheels:4, engine:{cylinders:4, size:2.2}}, 2]
* myCar[0].color = red
* newCar[0].color = red
* The new color of my Honda is purple
* myCar[0].color = purple
* newCar[0].color = purple
* </pre>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
 * @type Array
*/
slice: function(begin,end) {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span> Summary </span></h2>
* <p>Tests whether some element in the array passes the test implemented by the provided function.
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <td colspan="2" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Method of <a href="Array" shape="rect" title="Core JavaScript 1.5 Reference:Objects:Array">Array</a></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">Implemented in:</td>
* <td colspan="1" rowspan="1">JavaScript 1.6 (Gecko 1.8b2 and later)</td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">ECMAScript Edition:</td>
* <td colspan="1" rowspan="1">none</td>
* </tr>
* </table>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var <i>someElementPassed</i> = <i>array</i>.some(<i>callback</i>[, <i>thisObject</i>]);
* </pre>
* <h2> <span> Parameters </span></h2>
* <dl><dt style="font-weight:bold"> <code>callback</code></dt><dd> Function to test for each element.
* </dd><dt style="font-weight:bold"> <code>thisObject</code></dt><dd> Object to use as <code>this</code> when executing <code>callback</code>.
* </dd></dl>
* <h2> <span> Description </span></h2>
* <p><code>some</code> executes the <code>callback</code> function once for each element present in the array until it finds one where <code>callback</code> returns a true value.  If such an element is found, <code>some</code> immediately returns <code>true</code>. Otherwise, <code>some</code> returns <code>false</code>.  <code>callback</code> is invoked only for indexes of the array which have assigned values; it is not invoked for indexes which have been deleted or which have never been assigned values.
* </p><p><code>callback</code> is invoked with three arguments: the value of the element, the index of the element, and the Array object being traversed.
* </p><p>If a <code>thisObject</code> parameter is provided to <code>some</code>, it will be used as the <code>this</code> for each invocation of the <code>callback</code>.  If it is not provided, or is <code>null</code>, the global object associated with <code>callback</code> is used instead.
* </p><p><code>some</code> does not mutate the array on which it is called.
* </p><p>The range of elements processed by <code>some</code> is set before the first invocation of <code>callback</code>.  Elements that are appended to the array after the call to <code>some</code> begins will not be visited by <code>callback</code>.  If an existing, unvisited element of the array is changed by <code>callback</code>, its value passed to the visiting <code>callback</code> will be the value at the time that <code>some</code> visits that element's index; elements that are deleted are not visited.
* </p>
* <h2> <span> Compatibility </span></h2>
* <p><code>some</code> is a JavaScript extension to the ECMA-262 standard; as such it may not be present in other implementations of the standard.  You can work around this by inserting the following code at the beginning of your scripts, allowing use of <code>some</code> in ECMA-262 implementations which do not natively support it.  This algorithm is exactly the one used in Firefox and SpiderMonkey.
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">if (!Array.prototype.some)
* {
* Array.prototype.some = function(fun / *, thisp* /)
* {
* var len = this.length;
* if (typeof fun!= "function")
* throw new TypeError();
* 
* var thisp = arguments[1];
* for (var i = 0; i &lt; len; i++)
* {
* if (i in this &amp;&amp;
* fun.call(thisp, this[i], i, this))
* return true;
* }
* 
* return false;
* };
* }
* </pre>
* <h2> <span> Examples </span></h2>
* <h3> <span> Example: Testing size of all array elements </span></h3>
* <p>The following example tests whether some element in the array is bigger than 10.
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">function isBigEnough(element, index, array) {
* return (element &gt;= 10);
* }
* var passed = [2, 5, 8, 1, 4].some(isBigEnough);
* // passed is false
* passed = [12, 5, 8, 1, 4].some(isBigEnough);
* // passed is true
* </pre>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
some: undefined,
/**
* <h2> <span> Summary </span></h2>
* <p>Sorts the elements of an array in place.
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <td colspan="2" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Method of <a href="Array" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array">Array</a></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">Implemented in:</td>
* <td colspan="1" rowspan="1">JavaScript 1.1, NES 2.0
* <p>JavaScript 1.2: modified behavior.
* </p>
* </td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">ECMA Version:</td>
* <td colspan="1" rowspan="1">ECMA-262</td>
* </tr>
* </table>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve"><i>array</i>.sort(<i>compareFunction</i>);
* </pre>
* <h2> <span> Parameters </span></h2>
* <dl><dt style="font-weight:bold"> <code>compareFunction</code></dt><dd> Specifies a function that defines the sort order. If omitted, the array is sorted lexicographically (in dictionary order) according to the string conversion of each element.
* </dd></dl>
* <h2> <span> Description </span></h2>
* <p>If <code>compareFunction</code> is not supplied, elements are sorted by converting them to strings and comparing strings in lexicographic ("dictionary" or "telephone book," not numerical) order. For example, "80" comes before "9" in lexicographic order, but in a numeric sort 9 comes before 80.
* </p><p>If <code>compareFunction</code> is supplied, the array elements are sorted according to the return value of the compare function. If <code>a</code> and <code>b</code> are two elements being compared, then:
* </p>
* <ul><li> If <code>compareFunction(a, b)</code> is less than 0, sort <code>a</code> to a lower index than <code>b</code>.
* </li></ul>
* <ul><li> If <code>compareFunction(a, b)</code> returns 0, leave <code>a</code> and <code>b</code> unchanged with respect to each other, but sorted with respect to all different elements. Note: the ECMAscript standard does not guarantee this behaviour, and thus not all browsers (e.g. Mozilla versions dating back to at least 2003) respect this.
* </li></ul>
* <ul><li> If <code>compareFunction(a, b)</code> is greater than 0, sort <code>b</code> to a lower index than <code>a</code>.
* </li></ul>
* <p>So, the compare function has the following form:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">function compare(a, b)
* {
* if (a is less than b by some ordering criterion)
* return -1;
* if (a is greater than b by the ordering criterion)
* return 1;
* // a must be equal to b
* return 0;
* }
* </pre>
* <p>To compare numbers instead of strings, the compare function can simply subtract <code>b</code> from <code>a</code>:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">function compareNumbers(a, b)
* {
* return a - b;
* }
* </pre>
* <p>Some implementations of JavaScript implement a stable sort: the index partial order of <code>a</code> and <code>b</code> does not change if <code>a</code> and <code>b</code> are equal. If <code>a</code>'s index was less than <code>b</code>'s before sorting, it will be after sorting, no matter how <code>a</code> and <code>b</code> move due to sorting.
* </p><p>Sort is stable in <a href="http://developer.mozilla.org/en/docs/SpiderMonkey" shape="rect" title="SpiderMonkey">SpiderMonkey</a> and all Mozilla-based browsers starting with <a href="http://developer.mozilla.org/en/docs/Gecko" shape="rect" title="Gecko">Gecko</a> 1.9 (see <a href="https://bugzilla.mozilla.org/show_bug.cgi?id=224128" rel="nofollow" shape="rect" title="https://bugzilla.mozilla.org/show_bug.cgi?id=224128">bug 224128</a>).
* </p><p>The behavior of the <code>sort</code> method changed between JavaScript 1.1 and JavaScript 1.2.
* </p><p>In JavaScript 1.1, on some platforms, the <code>sort</code> method does not work. This method works on all platforms for JavaScript 1.2.
* </p><p>In JavaScript 1.2, this method no longer converts <a href="undefined" shape="rect" title="Core JavaScript 1.5 Reference:Global Properties:undefined">undefined</a> elements to <code>null</code>; instead it sorts them to the high end of the array. For example, assume you have this script:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var a = [];
* a[0] = "Ant";
* a[5] = "Zebra";
* 
* // assumes a print function is defined
* function writeArray(x)
* {
* for (i = 0; i &lt; x.length; i++)
* {
* print(x[i]);
* if (i &lt; x.length-1)
* print(", ");
* }
* }
* 
* writeArray(a);
* a.sort();
* print("\n");
* writeArray(a);
* </pre>
* <p>In JavaScript 1.1, JavaScript prints:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">ant, null, null, null, null, zebra
* ant, null, null, null, null, zebra
* </pre>
* <p>In JavaScript 1.2, JavaScript prints:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">ant, undefined, undefined, undefined, undefined, zebra
* ant, zebra, undefined, undefined, undefined, undefined
* </pre>
* <h2> <span> Examples </span></h2>
* <h3> <span> Example: Creating, displaying, and sorting an array </span></h3>
* <p>The following example creates four arrays and displays the original array, then the sorted arrays. The numeric arrays are sorted without, then with, a compare function.
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var stringArray = ["Blue", "Humpback", "Beluga"];
* var numericStringArray = ["80", "9", "700"];
* var numberArray = [40, 1, 5, 200];
* var mixedNumericArray = ["80", "9", "700", 40, 1, 5, 200];
* 
* function compareNumbers(a, b)
* {
* return a - b;
* }
* 
* // again, assumes a print function is defined
* print("stringArray: " + stringArray.join() +"\n");
* print("Sorted: " + stringArray.sort() +"\n\n");
* 
* print("numberArray: " + numberArray.join() +"\n");
* print("Sorted without a compare function: " + numberArray.sort() +"\n");
* print("Sorted with compareNumbers: " + numberArray.sort(compareNumbers) +"\n\n");
* 
* print("numericStringArray: " + numericStringArray.join() +"\n");
* print("Sorted without a compare function: " + numericStringArray.sort() +"\n");
* print("Sorted with compareNumbers: " + numericStringArray.sort(compareNumbers) +"\n\n");
* </pre>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">print("mixedNumericArray: " + mixedNumericArray.join() +"\n");
* print("Sorted without a compare function: " + mixedNumericArray.sort() +"\n");
* print("Sorted with compareNumbers: " + mixedNumericArray.sort(compareNumbers) +"\n\n");
* </pre>
* <p>This example produces the following output. As the output shows, when a compare function is used, numbers sort correctly whether they are numbers or numeric strings.
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">stringArray: Blue,Humpback,Beluga
* Sorted: Beluga,Blue,Humpback
* 
* numberArray: 40,1,5,200
* Sorted without a compare function: 1,200,40,5
* Sorted with compareNumbers: 1,5,40,200
* 
* numericStringArray: 80,9,700
* Sorted without a compare function: 700,80,9
* Sorted with compareNumbers: 9,80,700
* 
* mixedNumericArray: 80,9,700,40,1,5,200
* Sorted without a compare function: 1,200,40,5,700,80,9
* Sorted with compareNumbers: 1,5,9,40,80,200,700
* </pre>
* <h2> <span> See Also </span></h2>
* <p><a href="Core_JavaScript_1.5_Reference:Objects:Array:join" shape="rect" title="Core JavaScript 1.5 Reference:Objects:Array:join">join</a>,
* <a href="Array:reverse" shape="rect" title="Core JavaScript 1.5 Reference:Objects:Array:reverse">reverse</a>
* </p>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
 * @type Array
*/
sort: function(compareFunction) {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span> Summary </span></h2>
* <p>Changes the content of an array, adding new elements while removing old elements.
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <td colspan="2" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Method of <a href="Array" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array">Array</a></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">Implemented in:</td>
* <td colspan="1" rowspan="1">JavaScript 1.2, NES 3.0
* <p>JavaScript 1.3: returns an array containing the removed elements.
* </p>
* </td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">ECMA Version:</td>
* <td colspan="1" rowspan="1">ECMA-262 Edition 3</td>
* </tr>
* </table>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve"><i>array</i>.splice(<i>index</i>, <i>howMany</i>, [<i>element1</i>][, ..., <i>elementN</i>]);
* <i>array</i>.splice(<i>index</i>, [<i>howMany</i>, [<i>element1</i>][, ..., <i>elementN</i>]]);  // SpiderMonkey extension
* </pre>
* <h2> <span> Parameters </span></h2>
* <dl><dt style="font-weight:bold"> <code>index</code></dt><dd> Index at which to start changing the array.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <code>howMany</code></dt><dd> An integer indicating the number of old array elements to remove. If <code>howMany</code> is 0, no elements are removed. In this case, you should specify at least one new element. If no <code>howMany</code> parameter is specified (second syntax above, which is a SpiderMonkey extension), all elements after <code>index</code> are removed.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <code>element1, ..., element<i>N</i></code></dt><dd> The elements to add to the array. If you don't specify any elements, <code>splice</code> simply removes elements from the array.
* </dd></dl>
* <h2> <span> Description </span></h2>
* <p>If you specify a different number of elements to insert than the number you're removing, the array will have a different length at the end of the call.
* </p><p>The <code>splice</code> method returns an array containing the removed elements. If only one element is removed, an array of one element is returned.
* </p>
* <h2> <span> Backward Compatibility </span></h2>
* <h3> <span> JavaScript 1.2 </span></h3>
* <p>The <code>splice</code> method returns the element removed, if only one element is removed (<code>howMany</code> parameter is 1); otherwise, the method returns an array containing the removed elements.
* </p>
* <h2> <span> Examples </span></h2>
* <h3> <span> Example: Using <code>splice</code> </span></h3>
* <p>The following script illustrate the use of splice:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">// assumes a print function is defined
* var myFish = ["angel", "clown", "mandarin", "surgeon"];
* print("myFish: " + myFish);
* 
* var removed = myFish.splice(2, 0, "drum");
* print("After adding 1: " + myFish);
* print("removed is: " + removed);
* 
* removed = myFish.splice(3, 1);
* print("After removing 1: " + myFish);
* print("removed is: " + removed);
* 
* removed = myFish.splice(2, 1, "trumpet");
* print("After replacing 1: " + myFish);
* print("removed is: " + removed);
* 
* removed = myFish.splice(0, 2, "parrot", "anemone", "blue");
* print("After replacing 2: " + myFish);
* print("removed is: " + removed);
* </pre>
* <p>This script displays:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">myFish: angel,clown,mandarin,surgeon
* After adding 1: angel,clown,drum,mandarin,surgeon
* removed is:
* After removing 1: angel,clown,drum,surgeon
* removed is: mandarin
* After replacing 1: angel,clown,trumpet,surgeon
* removed is: drum
* After replacing 2: parrot,anemone,blue,trumpet,surgeon
* removed is: angel,clown
* </pre>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
 * @type Array
*/
splice: function(index, howMany, element1,elementN) {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <div style="border: 1px solid #FFB752; background-color: #FEE3BC; font-weight: bold; text-align: center; padding: 0px 10px 0px 10px; margin: 10px 0px 10px 0px;"><p style="margin: 4px 0px 4px 0px;">Non-standard</p></div>
* 
* <h2> <span> Summary </span></h2>
* <p>Returns a string representing the source code of the array.
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <td colspan="2" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Method of <a href="Array" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array">Array</a></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">Implemented in:</td>
* <td colspan="1" rowspan="1">JavaScript 1.3</td>
* </tr>
* </table>
* <h2> <span> Syntax </span></h2>
* <p><code><i>array</i>.toSource()</code>
* </p>
* <h2> <span> Parameters </span></h2>
* <p>None.
* </p>
* <h2> <span> Description </span></h2>
* <p>The <code>toSource</code> method returns the following values:
* </p>
* <ul><li> For the built-in <code>Array</code> object, <code>toSource</code> returns the following string indicating that the source code is not available:
* </li></ul>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">function Array() {
* [native code]
* }
* </pre>
* <ul><li> For instances of <code>Array</code>, <code>toSource</code> returns a string representing the source code.
* </li></ul>
* <p>This method is usually called internally by JavaScript and not explicitly in code. You can call <code>toSource</code> while debugging to examine the contents of an array.
* </p>
* <h2> <span> Examples </span></h2>
* <h3> <span> Example: Examining the source code of an array </span></h3>
* <p>To examine the source code of an array:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">alpha = new Array("a", "b", "c");
* alpha.toSource()                    //returns ["a", "b", "c"]
* </pre>
* <h2> <span> See Also </span></h2>
* <p><a href="Array:toString" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:toString">toString</a>
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
* <p>Returns a string representing the specified array and its elements.
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <td colspan="2" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Method of <a href="Array" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array">Array</a></td>
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
* <h2> <span> Syntax </span></h2>
* <p><code>
* toString()
* </code>
* </p>
* <h2> <span> Parameters </span></h2>
* <p>None.
* </p>
* <h2> <span> Description </span></h2>
* <p>The <a href="Core_JavaScript_1.5_Reference:Global_Objects:Array" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array">Array</a> object overrides the <code>toString</code> method of <a href="Object" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object">Object</a>. For Array objects, the <code>toString</code> method joins the array and returns one string containing each array element separated by commas. For example, the following code creates an array and uses <code>toString</code> to convert the array to a string.
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* var monthNames = new Array("Jan","Feb","Mar","Apr");
* myVar = monthNames.toString(); // assigns "Jan,Feb,Mar,Apr" to myVar
* </pre>
* <p>JavaScript calls the <code>toString</code> method automatically when an array is to be represented as a text value or when an array is referred to in a string concatenation.
* </p>
* <h2> <span> See Also </span></h2>
* <p><a href="Core_JavaScript_1.5_Reference:Global_Objects:Array:toSource" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:toSource">toSource</a>
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
* <p>Adds one or more elements to the beginning of an array and returns the new length of the array.
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <td colspan="2" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Method of <a href="Array" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array">Array</a></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">Implemented in:</td>
* <td colspan="1" rowspan="1">JavaScript 1.2, NES 3.0</td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">ECMA Version:</td>
* <td colspan="1" rowspan="1">ECMA-262 Edition 3</td>
* </tr>
* </table>
* <h2> <span> Syntax </span></h2>
* <p><code>
* arrayName.unshift(<i>element1</i>, ..., <i>elementN</i>)
* </code>
* </p>
* <h2> <span> Parameters </span></h2>
* <dl><dt style="font-weight:bold"> <code>element1, ..., element<i>N</i></code></dt><dd> The elements to add to the front of the array.
* </dd></dl>
* <h2> <span> Examples </span></h2>
* <h3> <span> Example: Adding elements to an array </span></h3>
* <p>The following code displays the <code>myFish</code> array before and after adding elements to it.
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* myFish = ["angel", "clown"];
* document.writeln("myFish before: " + myFish);
* unshifted = myFish.unshift("drum", "lion");
* document.writeln("myFish after: " + myFish);
* document.writeln("New length: " + unshifted);
* </pre>
* <p>This example displays the following:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* myFish before: ["angel", "clown"]
* myFish after: ["drum", "lion", "angel", "clown"]
* New length: 4
* </pre>
* <h2> <span> See Also </span></h2>
* <p><a href="Core_JavaScript_1.5_Reference:Global_Objects:Array:pop" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:pop">pop</a>,
* <a href="Array:push" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:push">push</a>,
* <a href="Array:shift" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:shift">shift</a>
* </p>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
 * @type Number
*/
unshift: function(element1,elementN) {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span> Summary </span></h2>
* <p>Returns the primitive value of an array.
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <td colspan="2" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Method of <a href="Array" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array">Array</a></td>
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
* <p>The <a href="Core_JavaScript_1.5_Reference:Global_Objects:Array" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array">Array</a> object inherits the <code>valueOf</code> method of <a href="Object" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object">Object</a>. The <code>valueOf</code> method of Array returns the primitive value of an array or the primitive value of its elements as follows:
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <td colspan="1" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Object type of element</td>
* <td colspan="1" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Data type of returned value</td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">Boolean</td>
* <td colspan="1" rowspan="1">Boolean</td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">Number or Date</td>
* <td colspan="1" rowspan="1">number</td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">All others</td>
* <td colspan="1" rowspan="1">string</td>
* </tr>
* </table>
* <h2> <span> See Also </span></h2>
* <p><a href="Core_JavaScript_1.5_Reference:Global_Objects:Object:valueOf" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object:valueOf">Object.valueOf</a>
* </p>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
valueOf: function() {
  // This is just a stub for a builtin native JavaScript object.
},
};
/**
* <div id="contentSub">(Redirected from <a href="http://developer.mozilla.org/en/docs/index.php?title=Core_JavaScript_1.5_Reference:Objects:Array&amp;redirect=no" shape="rect" title="Core JavaScript 1.5 Reference:Objects:Array">Core JavaScript 1.5 Reference:Objects:Array</a>)</div>
* 
* <h2> <span> Summary </span></h2>
* <p><b>Core Object</b>
* </p><p>Lets you work with arrays.
* </p><p>See also: <a href="http://developer.mozilla.org/en/docs/New_in_JavaScript_1.6#Array_extras" shape="rect" title="New in JavaScript 1.6">New in JavaScript 1.6#Array extras</a> and <a href="http://developer.mozilla.org/en/docs/New_in_JavaScript_1.7#Array_comprehensions" shape="rect" title="New in JavaScript 1.7">New_in_JavaScript_1.7#Array_comprehensions</a>
* </p>
* <h2> <span> Created by </span></h2>
* <p>The <code>Array</code> object constructor:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">new Array(<i>arrayLength</i>)
* new Array(<i>element0</i>, <i>element1</i>, ..., <i>elementN</i>)
* </pre>
* <p>An array literal:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">[<i>element0</i>, <i>element1</i>, ..., <i>elementN</i>]
* </pre>
* <h2> <span> Parameters </span></h2>
* <dl><dt style="font-weight:bold"> <code>arrayLength</code></dt><dd> The initial length of the array. You can access this value using the length property. If the value specified is not a number, an array of length 1 is created, with the first element having the specified value. The maximum length allowed for an array is 4,294,967,295.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <code>element0, element1, ... , element<i>N</i></code></dt><dd> A list of values for the array's elements. When this form is specified, the array is initialized with the specified values as its elements, and the array's length property is set to the number of arguments.
* </dd></dl>
* <h2> <span> Description </span></h2>
* <p>An array is an ordered set of values associated with a single variable name. Note that you <a href="http://www.andrewdupont.net/2006/05/18/javascript-associative-arrays-considered-harmful/" rel="nofollow" shape="rect" title="http://www.andrewdupont.net/2006/05/18/javascript-associative-arrays-considered-harmful/">shouldn't use it as an associative array</a>, use <a href="Object" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object">Object</a> instead.
* </p><p>The following example creates an Array object with an array literal; the coffees array contains three elements and has a length of three:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var coffees = ["French Roast", "Columbian", "Kona"];
* </pre>
* <p>You can construct a dense array of two or more elements starting with index 0 if you define initial values for all elements. A dense array is one in which each element has a value. The following code creates a dense array with three elements:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var myArray = new Array("Hello", myVar, 3.14159);
* </pre>
* <h3> <span> Indexing an array </span></h3>
* <p>You index an array by its ordinal number. For example, assume you define the following array:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var myArray = new Array("Wind", "Rain", "Fire");
* </pre>
* <p>You can then refer to the elements as thus:
* </p>
* <ul><li> <code>myArray[0]</code> is the first element
* </li><li> <code>myArray[1]</code> is the second element
* </li><li> <code>myArray[2]</code> is the third element
* </li></ul>
* <h3> <span> Specifying a single parameter </span></h3>
* <p>When you specify a single numeric parameter with the <code>Array</code> constructor, you specify the initial length of the array. The following code creates an array of five elements:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var billingMethod = new Array(5);
* </pre>
* <p>The behavior of the <code>Array</code> constructor depends on whether the single parameter is a number.
* </p>
* <ul><li> If the value specified is a number, the constructor converts the number to an unsigned, 32-bit integer and generates an array with the length property (size of the array) set to the integer. The array initially contains no elements, even though it might have a non-zero length.
* </li></ul>
* <ul><li> If the value specified is not a number, an array of length 1 is created, with the first element having the specified value.
* </li></ul>
* <p>The following code creates an array of length 25, then assigns values to the first three elements:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var musicTypes = new Array(25);
* musicTypes[0] = "R&amp;B";
* musicTypes[1] = "Blues";
* musicTypes[2] = "Jazz";
* </pre>
* <h3> <span> Increasing the array length indirectly </span></h3>
* <p>An array's length increases if you assign a value to an element higher than the current length of the array. The following code creates an array of length 0, then assigns a value to element 99. This changes the length of the array to 100.
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var colors = new Array();
* colors[99] = "midnightblue";
* </pre>
* <h3> <span> Creating an array using the result of a match </span></h3>
* <p>The result of a match between a regular expression and a string can create an array. This array has properties and elements that provide information about the match. An array is the return value of RegExp.exec, String.match, and String.replace. To help explain these properties and elements, look at the following example and then refer to the table below:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">// Match one d followed by one or more b's followed by one d
* // Remember matched b's and the following d
* // Ignore case
* 
* var myRe = /d(b+)(d)/i;
* var myArray = myRe.exec("cdbBdbsbz");
* </pre>
* <p>The properties and elements returned from this match are as follows:
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* 
* <tr>
* <td colspan="1" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Property/Element
* </td><td colspan="1" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Description
* </td><td colspan="1" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Example
* </td></tr>
* 
* <tr>
* <td colspan="1" rowspan="1"><code>input</code>
* </td><td colspan="1" rowspan="1">A read-only property that reflects the original string against which the regular expression was matched.
* </td><td colspan="1" rowspan="1">cdbBdbsbz
* </td></tr>
* 
* <tr>
* <td colspan="1" rowspan="1"><code>index</code>
* </td><td colspan="1" rowspan="1">A read-only property that is the zero-based index of the match in the string.
* </td><td colspan="1" rowspan="1">1
* </td></tr>
* 
* <tr>
* <td colspan="1" rowspan="1"><code>[0]</code>
* </td><td colspan="1" rowspan="1">A read-only element that specifies the last matched characters.
* </td><td colspan="1" rowspan="1">dbBd
* </td></tr>
* 
* <tr>
* <td colspan="1" rowspan="1"><code>[1], ...[n]</code>
* </td><td colspan="1" rowspan="1">Read-only elements that specify the parenthesized substring matches, if included in the regular expression. The number of possible parenthesized substrings is unlimited.
* </td><td colspan="1" rowspan="1">[1]: bB[2]: d
* </td></tr>
* </table>
* <p>
* </p>
* <h2> <span> Properties </span></h2>
* <dl><dd><dl><dt style="font-weight:bold"> <a href="Core_JavaScript_1.5_Reference:Global_Objects:Array:constructor" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:constructor">constructor</a></dt><dd> Specifies the function that creates an object's prototype.
* </dd><dt style="font-weight:bold"> <a href="Array:index" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:index">index</a></dt><dd> For an array created by a regular expression match, the zero-based index of the match in the string.
* </dd><dt style="font-weight:bold"> <a href="Array:input" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:input">input</a></dt><dd>  For an array created by a regular expression match, reflects the original string against which the regular expression was matched.
* </dd><dt style="font-weight:bold"> <a href="Array:length" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:length">length</a></dt><dd> Reflects the number of elements in an array.
* </dd><dt style="font-weight:bold"> <a href="Array:prototype" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:prototype">prototype</a></dt><dd> Allows the addition of properties to all objects.
* </dd></dl>
* </dd></dl>
* <h2> <span> Methods </span></h2>
* <h3> <span> Mutator methods </span></h3>
* <p>These methods modify the array:
* </p>
* <dl><dd><dl><dt style="font-weight:bold"> <a href="Array:pop" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:pop">pop</a></dt><dd> Removes the last element from an array and returns that element.
* </dd><dt style="font-weight:bold"> <a href="Array:push" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:push">push</a></dt><dd> Adds one or more elements to the end of an array and returns the new length of the array.
* </dd><dt style="font-weight:bold"> <a href="Array:reverse" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:reverse">reverse</a></dt><dd> Reverses the order of the elements of an array -- the first becomes the last, and the last becomes the first.
* </dd><dt style="font-weight:bold"> <a href="Array:shift" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:shift">shift</a></dt><dd> Removes the first element from an array and returns that element.
* </dd><dt style="font-weight:bold"> <a href="Array:sort" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:sort">sort</a></dt><dd> Sorts the elements of an array.
* </dd><dt style="font-weight:bold"> <a href="Array:splice" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:splice">splice</a></dt><dd> Adds and/or removes elements from an array.
* </dd><dt style="font-weight:bold"> <a href="Array:unshift" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:unshift">unshift</a></dt><dd> Adds one or more elements to the front of an array and returns the new length of the array.
* </dd></dl>
* </dd></dl>
* <h3> <span> Accessor methods </span></h3>
* <p>These methods do not modify the array and return some representation of the array.
* </p>
* <dl><dd><dl><dt style="font-weight:bold"> <a href="Core_JavaScript_1.5_Reference:Global_Objects:Array:concat" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:concat">concat</a></dt><dd> Returns a new array comprised of this array joined with other array(s) and/or value(s).
* </dd><dt style="font-weight:bold"> <a href="Array:join" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:join">join</a></dt><dd> Joins all elements of an array into a string.
* </dd><dt style="font-weight:bold"> <a href="Array:slice" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:slice">slice</a></dt><dd> Extracts a section of an array and returns a new array.
* </dd><dt style="font-weight:bold"> <a href="Array:toSource" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:toSource">toSource</a></dt><dd> Returns an array literal representing the specified array; you can use this value to create a new array. Overrides the Object.toSource method.
* </dd><dt style="font-weight:bold"> <a href="Array:toString" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:toString">toString</a></dt><dd> Returns a string representing the array and its elements. Overrides the <a href="Object:toString" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object:toString">Object.toString</a> method.
* </dd><dt style="font-weight:bold"> <a href="Array:valueOf" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:valueOf">valueOf</a></dt><dd> Returns the primitive value of the array.  Overrides the <a href="Object:valueOf" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object:valueOf">Object.valueOf</a> method.
* </dd></dl>
* </dd></dl>
* <div>The following methods have been introduced in JavaScript 1.6. See <a href="http://developer.mozilla.org/en/docs/New_in_JavaScript_1.6" shape="rect" title="New in JavaScript 1.6">New in JavaScript 1.6</a> for more information.</div>
* <dl><dd><dl><dt style="font-weight:bold"> <a href="Array:indexOf" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:indexOf">indexOf</a></dt><dd> Returns the first (least) index of an element within the array equal to the specified value, or -1 if none is found.
* </dd><dt style="font-weight:bold"> <a href="Array:lastIndexOf" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:lastIndexOf">lastIndexOf</a></dt><dd> Returns the last (greatest) index of an element within the array equal to the specified value, or -1 if none is found.
* </dd></dl>
* </dd></dl>
* <h3> <span> Iteration methods </span></h3>
* <div>These methods were introduced in JavaScript 1.6. See <a href="http://developer.mozilla.org/en/docs/New_in_JavaScript_1.6" shape="rect" title="New in JavaScript 1.6">New in JavaScript 1.6</a> for more information.</div>
* <p>Several methods take as arguments functions to be called back while processing the array. When these methods are called, the <code>length</code> of the array is sampled, and any element added beyond this length from within the callback is not visited. Other changes to the array (setting the value of or deleting an element) may affect the results of the operation if the method visits the changed element afterwards. The specific behaviour of these methods in such cases is not always well-defined, and should not be relied upon.
* </p>
* <dl><dd><dl><dt style="font-weight:bold"> <a href="Array:filter" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:filter">filter</a></dt><dd> Creates a new array with all of the elements of this array for which the provided filtering function returns true.
* </dd><dt style="font-weight:bold"> <a href="Array:forEach" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:forEach">forEach</a></dt><dd> Calls a function for each element in the array.
* </dd><dt style="font-weight:bold"> <a href="Array:every" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:every">every</a></dt><dd> Returns true if every element in this array satisfies the provided testing function.
* </dd><dt style="font-weight:bold"> <a href="Array:map" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:map">map</a></dt><dd> Creates a new array with the results of calling a provided function on every element in this array.
* </dd><dt style="font-weight:bold"> <a href="Array:some" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:some">some</a></dt><dd> Returns true if at least one element in this array satisfies the provided testing function.
* </dd></dl>
* </dd></dl>
* <div>The following methods were introduced in JavaScript 1.8. See <a href="http://developer.mozilla.org/en/docs/New_in_JavaScript_1.8" shape="rect" title="New in JavaScript 1.8">New in JavaScript 1.8</a> for more information.</div>
* <dl><dd><dl><dt style="font-weight:bold"> <a href="Array:reduce" shape="rect" title="Core JavaScript 1.5 Reference:Objects:Array:reduce">reduce</a></dt><dd> Apply a function simultaneously against two values of the array (from left-to-right) as to reduce it to a single value.
* </dd><dt style="font-weight:bold"> <a href="Array:reduceRight" shape="rect" title="Core JavaScript 1.5 Reference:Objects:Array:reduceRight">reduceRight</a></dt><dd> Apply a function simultaneously against two values of the array (from right-to-left) as to reduce it to a single value.
* </dd></dl>
* </dd></dl>
* <h3> <span> Generic methods </span></h3>
* <p>Many methods on the JavaScript Array object are designed to be generally applied to all objects which "look like" Arrays.  That is, they can be used on any object which has a <code>length</code> property, and which can usefully be accessed using numeric property names (as with <code>array[5]</code> indexing).
* </p><p>TODO: give examples with Array.prototype.forEach.call, and adding the method to an object like <a href="Core_JavaScript_1.5_Reference:Global_Objects:JavaArray" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:JavaArray">JavaArray</a> or <a href="String" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String">String</a>.
* </p><p>Some methods, such as <a href="Array:join" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:join">join</a>, only read the <code>length</code> and numeric properties of the object they are called on.  Others, like <a href="Array:reverse" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:reverse">reverse</a>, require that the object's numeric properties and <code>length</code> be mutable; these methods can therefore not be called on objects like <a href="String" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String">String</a>, which does not permit its <code>length</code> property or synthesized numeric properties to be set.
* </p><p>The methods that work on any Array-like object and do <b>not</b> need to alter <code>length</code> or numeric properties are:
* </p>
* <ul><li> <a href="Array:concat" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:concat">concat</a>
* </li><li> <a href="Array:every" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:every">every</a> (JS 1.6+)
* </li><li> <a href="Array:filter" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:filter">filter</a> (JS 1.6+)
* </li><li> <a href="Array:forEach" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:forEach">forEach</a> (JS 1.6+)
* </li><li> <a href="Array:indexOf" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:indexOf">indexOf</a> (JS 1.6+)
* </li><li> <a href="Array:join" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:join">join</a>
* </li><li> <a href="Array:lastIndexOf" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:lastIndexOf">lastIndexOf</a> (JS 1.6+)
* </li><li> <a href="Array:map" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:map">map</a> (JS 1.6+)
* </li><li> <a href="Array:slice" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:slice">slice</a>
* </li><li> <a href="Array:some" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:some">some</a> (JS 1.6+)
* </li><li> <a href="Array:toSource" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:toSource">toSource</a>
* </li><li> <a href="Array:toString" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:toString">toString</a>
* </li><li> <a href="Array:valueOf" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:valueOf">valueOf</a>
* </li></ul>
* <p>The methods that alter the <code>length</code> or numeric properties of the object they are called on are:
* </p>
* <ul><li> <a href="Array:pop" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:pop">pop</a>
* </li><li> <a href="Array:push" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:push">push</a>
* </li><li> <a href="Array:reverse" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:reverse">reverse</a>
* </li><li> <a href="Array:shift" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:shift">shift</a>
* </li><li> <a href="Array:sort" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:sort">sort</a>
* </li><li> <a href="Array:splice" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:splice">splice</a>
* </li><li> <a href="Array:unshift" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:unshift">unshift</a>
* </li></ul>
* <p>This example shows how to use <a href="Array:map" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:map">map</a> on a <a href="String" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String">string</a> object to get an array of bytes in the ASCII encoding representing the character values:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* var a = Array.prototype.map.call("Hello World",
* function(x) { return x.charCodeAt(0); })
* // a now equals [72,101,108,108,111,32,87,111,114,108,100]
* </pre>
* <h2> <span> Examples </span></h2>
* <h3> <span> Example: Creating an Array </span></h3>
* <p>The following example creates an array, <code>msgArray</code>, with a length of 0, then assigns values to <code>msgArray[0]</code> and <code>msgArray[99]</code>, changing the length of the array to 100.
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var msgArray = new Array();
* msgArray[0] = "Hello";
* msgArray[99] = "world";
* // The following statement is true,
* // because defined msgArray[99] element.
* if (msgArray.length == 100)
* myVar = "The length is 100.";
* </pre>
* <h3> <span> Example: Creating a Two-dimensional Array </span></h3>
* <p>The following creates chess board as a two dimensional array of strings.
* The first move is made by copying the 'P' in 1,4 to 3,4.
* The position 1,4 is left blank.
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var board =
* [ ['R','N','B','Q','K','B','N','R'],
* ['P','P','P','P','P','P','P','P'],
* [' ',' ',' ',' ',' ',' ',' ',' '],
* [' ',' ',' ',' ',' ',' ',' ',' '],
* [' ',' ',' ',' ',' ',' ',' ',' '],
* [' ',' ',' ',' ',' ',' ',' ',' '],
* ['p','p','p','p','p','p','p','p'],
* ['r','n','b','q','k','b','n','r']];
* print(board.join('\n'));
* print('\n\n\n');
* 
* // Move King's Pawn forward 2
* board[3][4] = board[1][4];
* board[1][4] = ' ';
* print(board.join('\n'));
* </pre>
* <p>Here is the output:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">R,N,B,Q,K,B,N,R
* P,P,P,P,P,P,P,P
* , , , , , , ,
* , , , , , , ,
* , , , , , , ,
* , , , , , , ,
* p,p,p,p,p,p,p,p
* r,n,b,q,k,b,n,r
* 
* R,N,B,Q,K,B,N,R
* P,P,P,P, ,P,P,P
* , , , , , , ,
* , , , ,P, , ,
* , , , , , , ,
* , , , , , , ,
* p,p,p,p,p,p,p,p
* r,n,b,q,k,b,n,r
* </pre>
* <h2> <span> See Also </span></h2>
* <p><a href="Core_JavaScript_1.5_Guide:Predefined_Core_Objects:Array_Object" shape="rect" title="Core JavaScript 1.5 Guide:Predefined Core Objects:Array Object">Core JavaScript 1.5 Guide:Predefined Core Objects:Array Object</a>
* </p>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
function Array(element0, element1, elementN) {};
/**
* <div id="contentSub">(Redirected from <a href="http://developer.mozilla.org/en/docs/index.php?title=Core_JavaScript_1.5_Reference:Objects:Array&amp;redirect=no" shape="rect" title="Core JavaScript 1.5 Reference:Objects:Array">Core JavaScript 1.5 Reference:Objects:Array</a>)</div>
* 
* <h2> <span> Summary </span></h2>
* <p><b>Core Object</b>
* </p><p>Lets you work with arrays.
* </p><p>See also: <a href="http://developer.mozilla.org/en/docs/New_in_JavaScript_1.6#Array_extras" shape="rect" title="New in JavaScript 1.6">New in JavaScript 1.6#Array extras</a> and <a href="http://developer.mozilla.org/en/docs/New_in_JavaScript_1.7#Array_comprehensions" shape="rect" title="New in JavaScript 1.7">New_in_JavaScript_1.7#Array_comprehensions</a>
* </p>
* <h2> <span> Created by </span></h2>
* <p>The <code>Array</code> object constructor:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">new Array(<i>arrayLength</i>)
* new Array(<i>element0</i>, <i>element1</i>, ..., <i>elementN</i>)
* </pre>
* <p>An array literal:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">[<i>element0</i>, <i>element1</i>, ..., <i>elementN</i>]
* </pre>
* <h2> <span> Parameters </span></h2>
* <dl><dt style="font-weight:bold"> <code>arrayLength</code></dt><dd> The initial length of the array. You can access this value using the length property. If the value specified is not a number, an array of length 1 is created, with the first element having the specified value. The maximum length allowed for an array is 4,294,967,295.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <code>element0, element1, ... , element<i>N</i></code></dt><dd> A list of values for the array's elements. When this form is specified, the array is initialized with the specified values as its elements, and the array's length property is set to the number of arguments.
* </dd></dl>
* <h2> <span> Description </span></h2>
* <p>An array is an ordered set of values associated with a single variable name. Note that you <a href="http://www.andrewdupont.net/2006/05/18/javascript-associative-arrays-considered-harmful/" rel="nofollow" shape="rect" title="http://www.andrewdupont.net/2006/05/18/javascript-associative-arrays-considered-harmful/">shouldn't use it as an associative array</a>, use <a href="Object" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object">Object</a> instead.
* </p><p>The following example creates an Array object with an array literal; the coffees array contains three elements and has a length of three:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var coffees = ["French Roast", "Columbian", "Kona"];
* </pre>
* <p>You can construct a dense array of two or more elements starting with index 0 if you define initial values for all elements. A dense array is one in which each element has a value. The following code creates a dense array with three elements:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var myArray = new Array("Hello", myVar, 3.14159);
* </pre>
* <h3> <span> Indexing an array </span></h3>
* <p>You index an array by its ordinal number. For example, assume you define the following array:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var myArray = new Array("Wind", "Rain", "Fire");
* </pre>
* <p>You can then refer to the elements as thus:
* </p>
* <ul><li> <code>myArray[0]</code> is the first element
* </li><li> <code>myArray[1]</code> is the second element
* </li><li> <code>myArray[2]</code> is the third element
* </li></ul>
* <h3> <span> Specifying a single parameter </span></h3>
* <p>When you specify a single numeric parameter with the <code>Array</code> constructor, you specify the initial length of the array. The following code creates an array of five elements:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var billingMethod = new Array(5);
* </pre>
* <p>The behavior of the <code>Array</code> constructor depends on whether the single parameter is a number.
* </p>
* <ul><li> If the value specified is a number, the constructor converts the number to an unsigned, 32-bit integer and generates an array with the length property (size of the array) set to the integer. The array initially contains no elements, even though it might have a non-zero length.
* </li></ul>
* <ul><li> If the value specified is not a number, an array of length 1 is created, with the first element having the specified value.
* </li></ul>
* <p>The following code creates an array of length 25, then assigns values to the first three elements:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var musicTypes = new Array(25);
* musicTypes[0] = "R&amp;B";
* musicTypes[1] = "Blues";
* musicTypes[2] = "Jazz";
* </pre>
* <h3> <span> Increasing the array length indirectly </span></h3>
* <p>An array's length increases if you assign a value to an element higher than the current length of the array. The following code creates an array of length 0, then assigns a value to element 99. This changes the length of the array to 100.
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var colors = new Array();
* colors[99] = "midnightblue";
* </pre>
* <h3> <span> Creating an array using the result of a match </span></h3>
* <p>The result of a match between a regular expression and a string can create an array. This array has properties and elements that provide information about the match. An array is the return value of RegExp.exec, String.match, and String.replace. To help explain these properties and elements, look at the following example and then refer to the table below:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">// Match one d followed by one or more b's followed by one d
* // Remember matched b's and the following d
* // Ignore case
* 
* var myRe = /d(b+)(d)/i;
* var myArray = myRe.exec("cdbBdbsbz");
* </pre>
* <p>The properties and elements returned from this match are as follows:
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* 
* <tr>
* <td colspan="1" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Property/Element
* </td><td colspan="1" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Description
* </td><td colspan="1" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Example
* </td></tr>
* 
* <tr>
* <td colspan="1" rowspan="1"><code>input</code>
* </td><td colspan="1" rowspan="1">A read-only property that reflects the original string against which the regular expression was matched.
* </td><td colspan="1" rowspan="1">cdbBdbsbz
* </td></tr>
* 
* <tr>
* <td colspan="1" rowspan="1"><code>index</code>
* </td><td colspan="1" rowspan="1">A read-only property that is the zero-based index of the match in the string.
* </td><td colspan="1" rowspan="1">1
* </td></tr>
* 
* <tr>
* <td colspan="1" rowspan="1"><code>[0]</code>
* </td><td colspan="1" rowspan="1">A read-only element that specifies the last matched characters.
* </td><td colspan="1" rowspan="1">dbBd
* </td></tr>
* 
* <tr>
* <td colspan="1" rowspan="1"><code>[1], ...[n]</code>
* </td><td colspan="1" rowspan="1">Read-only elements that specify the parenthesized substring matches, if included in the regular expression. The number of possible parenthesized substrings is unlimited.
* </td><td colspan="1" rowspan="1">[1]: bB[2]: d
* </td></tr>
* </table>
* <p>
* </p>
* <h2> <span> Properties </span></h2>
* <dl><dd><dl><dt style="font-weight:bold"> <a href="Core_JavaScript_1.5_Reference:Global_Objects:Array:constructor" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:constructor">constructor</a></dt><dd> Specifies the function that creates an object's prototype.
* </dd><dt style="font-weight:bold"> <a href="Array:index" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:index">index</a></dt><dd> For an array created by a regular expression match, the zero-based index of the match in the string.
* </dd><dt style="font-weight:bold"> <a href="Array:input" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:input">input</a></dt><dd>  For an array created by a regular expression match, reflects the original string against which the regular expression was matched.
* </dd><dt style="font-weight:bold"> <a href="Array:length" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:length">length</a></dt><dd> Reflects the number of elements in an array.
* </dd><dt style="font-weight:bold"> <a href="Array:prototype" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:prototype">prototype</a></dt><dd> Allows the addition of properties to all objects.
* </dd></dl>
* </dd></dl>
* <h2> <span> Methods </span></h2>
* <h3> <span> Mutator methods </span></h3>
* <p>These methods modify the array:
* </p>
* <dl><dd><dl><dt style="font-weight:bold"> <a href="Array:pop" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:pop">pop</a></dt><dd> Removes the last element from an array and returns that element.
* </dd><dt style="font-weight:bold"> <a href="Array:push" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:push">push</a></dt><dd> Adds one or more elements to the end of an array and returns the new length of the array.
* </dd><dt style="font-weight:bold"> <a href="Array:reverse" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:reverse">reverse</a></dt><dd> Reverses the order of the elements of an array -- the first becomes the last, and the last becomes the first.
* </dd><dt style="font-weight:bold"> <a href="Array:shift" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:shift">shift</a></dt><dd> Removes the first element from an array and returns that element.
* </dd><dt style="font-weight:bold"> <a href="Array:sort" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:sort">sort</a></dt><dd> Sorts the elements of an array.
* </dd><dt style="font-weight:bold"> <a href="Array:splice" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:splice">splice</a></dt><dd> Adds and/or removes elements from an array.
* </dd><dt style="font-weight:bold"> <a href="Array:unshift" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:unshift">unshift</a></dt><dd> Adds one or more elements to the front of an array and returns the new length of the array.
* </dd></dl>
* </dd></dl>
* <h3> <span> Accessor methods </span></h3>
* <p>These methods do not modify the array and return some representation of the array.
* </p>
* <dl><dd><dl><dt style="font-weight:bold"> <a href="Core_JavaScript_1.5_Reference:Global_Objects:Array:concat" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:concat">concat</a></dt><dd> Returns a new array comprised of this array joined with other array(s) and/or value(s).
* </dd><dt style="font-weight:bold"> <a href="Array:join" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:join">join</a></dt><dd> Joins all elements of an array into a string.
* </dd><dt style="font-weight:bold"> <a href="Array:slice" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:slice">slice</a></dt><dd> Extracts a section of an array and returns a new array.
* </dd><dt style="font-weight:bold"> <a href="Array:toSource" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:toSource">toSource</a></dt><dd> Returns an array literal representing the specified array; you can use this value to create a new array. Overrides the Object.toSource method.
* </dd><dt style="font-weight:bold"> <a href="Array:toString" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:toString">toString</a></dt><dd> Returns a string representing the array and its elements. Overrides the <a href="Object:toString" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object:toString">Object.toString</a> method.
* </dd><dt style="font-weight:bold"> <a href="Array:valueOf" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:valueOf">valueOf</a></dt><dd> Returns the primitive value of the array.  Overrides the <a href="Object:valueOf" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object:valueOf">Object.valueOf</a> method.
* </dd></dl>
* </dd></dl>
* <div>The following methods have been introduced in JavaScript 1.6. See <a href="http://developer.mozilla.org/en/docs/New_in_JavaScript_1.6" shape="rect" title="New in JavaScript 1.6">New in JavaScript 1.6</a> for more information.</div>
* <dl><dd><dl><dt style="font-weight:bold"> <a href="Array:indexOf" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:indexOf">indexOf</a></dt><dd> Returns the first (least) index of an element within the array equal to the specified value, or -1 if none is found.
* </dd><dt style="font-weight:bold"> <a href="Array:lastIndexOf" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:lastIndexOf">lastIndexOf</a></dt><dd> Returns the last (greatest) index of an element within the array equal to the specified value, or -1 if none is found.
* </dd></dl>
* </dd></dl>
* <h3> <span> Iteration methods </span></h3>
* <div>These methods were introduced in JavaScript 1.6. See <a href="http://developer.mozilla.org/en/docs/New_in_JavaScript_1.6" shape="rect" title="New in JavaScript 1.6">New in JavaScript 1.6</a> for more information.</div>
* <p>Several methods take as arguments functions to be called back while processing the array. When these methods are called, the <code>length</code> of the array is sampled, and any element added beyond this length from within the callback is not visited. Other changes to the array (setting the value of or deleting an element) may affect the results of the operation if the method visits the changed element afterwards. The specific behaviour of these methods in such cases is not always well-defined, and should not be relied upon.
* </p>
* <dl><dd><dl><dt style="font-weight:bold"> <a href="Array:filter" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:filter">filter</a></dt><dd> Creates a new array with all of the elements of this array for which the provided filtering function returns true.
* </dd><dt style="font-weight:bold"> <a href="Array:forEach" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:forEach">forEach</a></dt><dd> Calls a function for each element in the array.
* </dd><dt style="font-weight:bold"> <a href="Array:every" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:every">every</a></dt><dd> Returns true if every element in this array satisfies the provided testing function.
* </dd><dt style="font-weight:bold"> <a href="Array:map" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:map">map</a></dt><dd> Creates a new array with the results of calling a provided function on every element in this array.
* </dd><dt style="font-weight:bold"> <a href="Array:some" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:some">some</a></dt><dd> Returns true if at least one element in this array satisfies the provided testing function.
* </dd></dl>
* </dd></dl>
* <div>The following methods were introduced in JavaScript 1.8. See <a href="http://developer.mozilla.org/en/docs/New_in_JavaScript_1.8" shape="rect" title="New in JavaScript 1.8">New in JavaScript 1.8</a> for more information.</div>
* <dl><dd><dl><dt style="font-weight:bold"> <a href="Array:reduce" shape="rect" title="Core JavaScript 1.5 Reference:Objects:Array:reduce">reduce</a></dt><dd> Apply a function simultaneously against two values of the array (from left-to-right) as to reduce it to a single value.
* </dd><dt style="font-weight:bold"> <a href="Array:reduceRight" shape="rect" title="Core JavaScript 1.5 Reference:Objects:Array:reduceRight">reduceRight</a></dt><dd> Apply a function simultaneously against two values of the array (from right-to-left) as to reduce it to a single value.
* </dd></dl>
* </dd></dl>
* <h3> <span> Generic methods </span></h3>
* <p>Many methods on the JavaScript Array object are designed to be generally applied to all objects which "look like" Arrays.  That is, they can be used on any object which has a <code>length</code> property, and which can usefully be accessed using numeric property names (as with <code>array[5]</code> indexing).
* </p><p>TODO: give examples with Array.prototype.forEach.call, and adding the method to an object like <a href="Core_JavaScript_1.5_Reference:Global_Objects:JavaArray" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:JavaArray">JavaArray</a> or <a href="String" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String">String</a>.
* </p><p>Some methods, such as <a href="Array:join" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:join">join</a>, only read the <code>length</code> and numeric properties of the object they are called on.  Others, like <a href="Array:reverse" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:reverse">reverse</a>, require that the object's numeric properties and <code>length</code> be mutable; these methods can therefore not be called on objects like <a href="String" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String">String</a>, which does not permit its <code>length</code> property or synthesized numeric properties to be set.
* </p><p>The methods that work on any Array-like object and do <b>not</b> need to alter <code>length</code> or numeric properties are:
* </p>
* <ul><li> <a href="Array:concat" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:concat">concat</a>
* </li><li> <a href="Array:every" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:every">every</a> (JS 1.6+)
* </li><li> <a href="Array:filter" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:filter">filter</a> (JS 1.6+)
* </li><li> <a href="Array:forEach" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:forEach">forEach</a> (JS 1.6+)
* </li><li> <a href="Array:indexOf" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:indexOf">indexOf</a> (JS 1.6+)
* </li><li> <a href="Array:join" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:join">join</a>
* </li><li> <a href="Array:lastIndexOf" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:lastIndexOf">lastIndexOf</a> (JS 1.6+)
* </li><li> <a href="Array:map" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:map">map</a> (JS 1.6+)
* </li><li> <a href="Array:slice" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:slice">slice</a>
* </li><li> <a href="Array:some" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:some">some</a> (JS 1.6+)
* </li><li> <a href="Array:toSource" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:toSource">toSource</a>
* </li><li> <a href="Array:toString" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:toString">toString</a>
* </li><li> <a href="Array:valueOf" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:valueOf">valueOf</a>
* </li></ul>
* <p>The methods that alter the <code>length</code> or numeric properties of the object they are called on are:
* </p>
* <ul><li> <a href="Array:pop" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:pop">pop</a>
* </li><li> <a href="Array:push" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:push">push</a>
* </li><li> <a href="Array:reverse" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:reverse">reverse</a>
* </li><li> <a href="Array:shift" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:shift">shift</a>
* </li><li> <a href="Array:sort" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:sort">sort</a>
* </li><li> <a href="Array:splice" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:splice">splice</a>
* </li><li> <a href="Array:unshift" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:unshift">unshift</a>
* </li></ul>
* <p>This example shows how to use <a href="Array:map" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:map">map</a> on a <a href="String" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String">string</a> object to get an array of bytes in the ASCII encoding representing the character values:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* var a = Array.prototype.map.call("Hello World",
* function(x) { return x.charCodeAt(0); })
* // a now equals [72,101,108,108,111,32,87,111,114,108,100]
* </pre>
* <h2> <span> Examples </span></h2>
* <h3> <span> Example: Creating an Array </span></h3>
* <p>The following example creates an array, <code>msgArray</code>, with a length of 0, then assigns values to <code>msgArray[0]</code> and <code>msgArray[99]</code>, changing the length of the array to 100.
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var msgArray = new Array();
* msgArray[0] = "Hello";
* msgArray[99] = "world";
* // The following statement is true,
* // because defined msgArray[99] element.
* if (msgArray.length == 100)
* myVar = "The length is 100.";
* </pre>
* <h3> <span> Example: Creating a Two-dimensional Array </span></h3>
* <p>The following creates chess board as a two dimensional array of strings.
* The first move is made by copying the 'P' in 1,4 to 3,4.
* The position 1,4 is left blank.
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var board =
* [ ['R','N','B','Q','K','B','N','R'],
* ['P','P','P','P','P','P','P','P'],
* [' ',' ',' ',' ',' ',' ',' ',' '],
* [' ',' ',' ',' ',' ',' ',' ',' '],
* [' ',' ',' ',' ',' ',' ',' ',' '],
* [' ',' ',' ',' ',' ',' ',' ',' '],
* ['p','p','p','p','p','p','p','p'],
* ['r','n','b','q','k','b','n','r']];
* print(board.join('\n'));
* print('\n\n\n');
* 
* // Move King's Pawn forward 2
* board[3][4] = board[1][4];
* board[1][4] = ' ';
* print(board.join('\n'));
* </pre>
* <p>Here is the output:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">R,N,B,Q,K,B,N,R
* P,P,P,P,P,P,P,P
* , , , , , , ,
* , , , , , , ,
* , , , , , , ,
* , , , , , , ,
* p,p,p,p,p,p,p,p
* r,n,b,q,k,b,n,r
* 
* R,N,B,Q,K,B,N,R
* P,P,P,P, ,P,P,P
* , , , , , , ,
* , , , ,P, , ,
* , , , , , , ,
* , , , , , , ,
* p,p,p,p,p,p,p,p
* r,n,b,q,k,b,n,r
* </pre>
* <h2> <span> See Also </span></h2>
* <p><a href="Core_JavaScript_1.5_Guide:Predefined_Core_Objects:Array_Object" shape="rect" title="Core JavaScript 1.5 Guide:Predefined Core Objects:Array Object">Core JavaScript 1.5 Guide:Predefined Core Objects:Array Object</a>
* </p>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
function Array(arrayLength) {};

