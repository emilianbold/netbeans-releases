/**
* <h2> <span> Summary </span></h2>
* <p><b>Core Object</b>
* </p><p><code>Object</code> is the primitive JavaScript object type. All JavaScript objects are descended from <code>Object</code>. That is, all JavaScript objects have the methods defined for <code>Object</code>.
* </p>
* <h2> <span> Created by </span></h2>
* <p>The <code>Object</code> constructor:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">new Object()
* </pre>
* <h2> <span> Parameters </span></h2>
* <p>None.
* </p>
* <h2> <span> Properties </span></h2>
* <p><a href="Core_JavaScript_1.5_Reference:Global_Objects:Object:constructor" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object:constructor">constructor</a>: Specifies the function that creates an object's prototype.
* </p><p><a href="Object:prototype" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object:prototype">prototype</a>: Allows the addition of properties to all objects.
* </p>
* <h2> <span> Methods </span></h2>
* <p><a href="http://developer.mozilla.org/en/docs/index.php?title=Core_JavaScript_1.5_Reference:Global_Objects:Object:defineGetter&amp;action=edit" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object:defineGetter">__defineGetter__</a>
* </p><p><a href="http://developer.mozilla.org/en/docs/index.php?title=Object:defineSetter&amp;action=edit" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object:defineSetter">__defineSetter__</a>
* </p><p><a href="Object:eval" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object:eval">eval</a>: <span style="border: 1px solid #9898F0; background-color: #DDDDFF; font-size: 9px; vertical-align: text-top;">Deprecated</span> Evaluates a string of JavaScript code in the context of the specified object.
* </p><p><a href="Object:hasOwnProperty" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object:hasOwnProperty">hasOwnProperty</a>
* </p><p><a href="Object:isPrototypeOf" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object:isPrototypeOf">isPrototypeOf</a>
* </p><p><a href="http://developer.mozilla.org/en/docs/index.php?title=Object:lookupGetter&amp;action=edit" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object:lookupGetter">__lookupGetter__</a>
* </p><p><a href="http://developer.mozilla.org/en/docs/index.php?title=Object:lookupSetter&amp;action=edit" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object:lookupSetter">__lookupSetter__</a>
* </p><p><a href="Object:propertyIsEnumerable" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object:propertyIsEnumerable">propertyIsEnumerable</a>
* </p><p><a href="Object:toSource" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object:toSource">toSource</a>: Returns an object literal representing the specified object; you can use this value to create a new object.
* </p><p><a href="Object:toString" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object:toString">toString</a>: Returns a string representing the specified object.
* </p><p><a href="Object:unwatch" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object:unwatch">unwatch</a>: Removes a watchpoint from a property of the object.
* </p><p><a href="Object:valueOf" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object:valueOf">valueOf</a>: Returns the primitive value of the specified object.
* </p><p><a href="Object:watch" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object:watch">watch</a>: Adds a watchpoint to a property of the object.
* </p>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
var Object = {
  // This is just a stub for a builtin native JavaScript object.
/**
* <h2> <span> Summary </span></h2>
* <p>Returns a reference to the <a href="Object" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object">Object</a> function that created the instance's prototype. Note that the value of this property is a reference to the function itself, not a string containing the function's name, but it isn't read only (except for primitive Boolean, Number or String values: 1, true, "read-only").
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <td colspan="2" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Property of <a href="Object" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object">Object</a></td>
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
* <h2> <span> Description </span></h2>
* <p>All objects inherit a <code>constructor</code> property from their <code>prototype</code>:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* o = new Object // or o = {} in JavaScript 1.2
* o.constructor == Object
* a = new Array // or a = [] in JavaScript 1.2
* a.constructor == Array
* n = new Number(3)
* n.constructor == Number
* </pre>
* <p>Even though you cannot construct most HTML objects, you can do comparisons. For example,
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* document.constructor == Document
* document.form3.constructor == Form
* </pre>
* <h2> <span> Examples </span></h2>
* <h3> <span> Example: Displaying the constructor of an object </span></h3>
* <p>The following example creates a prototype, <code>Tree</code>, and an object of that type, <code>theTree</code>. The example then displays the <code>constructor</code> property for the object <code>theTree</code>.
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* function Tree(name) {
* this.name = name;
* }
* theTree = new Tree("Redwood");
* print("theTree.constructor is " + theTree.constructor);
* </pre>
* <p>This example displays the following output:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">theTree.constructor is function Tree(name) {
* this.name = name;
* }
* </pre>
* <h3> <span> Example: Changing the constructor of an object </span></h3>
* <p>The following example shows how to modify constructor value of generic objects. Only true, 1 and "test" variable constructors will not be changed.
* This example explains that is not always so safe to believe in constructor function.
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* function Type(){};
* var	types = [
* new Array,	[],
* new Boolean,	true,
* new Date,
* new Error,
* new Function,	function(){},
* Math,
* new Number,	1,
* new Object,	{},
* new RegExp,	/(?:)/,
* new String,	"test"
* ];
* for(var i = 0; i &lt; types.length; i++){
* types[i].constructor = Type;
* types[i] = [types[i].constructor, types[i] instanceof Type, types[i].toString()];
* };
* alert(types.join("\n"));
* </pre>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
constructor: undefined,
/**
* <div style="border: 1px solid #5151FF; background-color: #B9B9FF; font-weight: bold; text-align: center; padding: 0px 10px 0px 10px; margin: 10px 0px 10px 0px;"><p style="margin: 4px 0px 4px 0px;">Deprecated</p></div>
* 
* <h2> <span> Summary </span></h2>
* <p>Deprecated. Evaluates a string of JavaScript code in the context of an object.
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <td colspan="2" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Method of <a href="Object" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object">Object</a></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">Implemented in:</td>
* <td colspan="1" rowspan="1">JavaScript 1.1, NES2.0
* <p>JavaScript 1.2, NES 3.0: Deprecated as a method of objects; retained as a top-level function.
* </p>
* </td>
* </tr>
* </table>
* <h2> <span> Syntax </span></h2>
* <p><code><i>object</i>.eval(<i>string</i>)</code>
* </p>
* <h2> <span> Parameters </span></h2>
* <dl><dt style="font-weight:bold"> <code>string</code>Ê</dt><dd> Any string representing a JavaScript expression, statement, or sequence of statements. The expression can include variables and properties of existing objects.
* </dd></dl>
* <h2> <span> Description </span></h2>
* <p>The <code>eval</code> method should no longer be used as a method of <code>Object</code>.  Use the top-level <code><a href="Core_JavaScript_1.5_Reference:Global_Functions:eval" shape="rect" title="Core JavaScript 1.5 Reference:Global Functions:eval">eval</a></code> function instead.
* </p>
* <h2> <span> See Also </span></h2>
* <p><a href="Core_JavaScript_1.5_Reference:Global_Functions:eval" shape="rect" title="Core JavaScript 1.5 Reference:Global Functions:eval">eval</a>
* </p>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
 * <p>Original W3C documentation:</p>
 * @param {String} code
*/
eval: function(string) {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span> Summary </span></h2>
* <p>Returns a boolean indicating whether the object has the specified property.
* </p>
* <h2> <span> Syntax </span></h2>
* <p><code>
* hasOwnProperty(<i>prop</i>)
* </code>
* </p>
* <h2> <span> Parameters </span></h2>
* <dl><dt style="font-weight:bold"> <code>prop</code>Ê</dt><dd> The name of the property to test.
* </dd></dl>
* <h2> <span> Description </span></h2>
* <p>Every object descended from <code>Object</code> inherits the <code>hasOwnProperty</code> method. This method can be used to determine whether an object has the specified property as a direct property of that object; unlike the <a href="Core_JavaScript_1.5_Reference:Operators:Special_Operators:in_Operator" shape="rect" title="Core JavaScript 1.5 Reference:Operators:Special Operators:in Operator"><code>in</code></a> operator, this method does not check down the object's prototype chain.
* </p>
* <h2> <span> Examples </span></h2>
* <h3> <span> Example: Using <code>hasOwnProperty</code> to test for a property's existence </span></h3>
* <p>The following example determines whether the <code>o</code> object contains a property named <code>prop</code>:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* o = new Object();
* o.prop = 'exists';
* 
* function changeO() {
* o.newprop = o.prop;
* delete o.prop;
* }
* 
* o.hasOwnProperty('prop');   //returns true
* changeO();
* o.hasOwnProperty('prop');   //returns false
* </pre>
* <h3> <span> Example: Direct versus inherited properties </span></h3>
* <p>The following example differentiates between direct properties and properties inherited through the prototype chain:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* o = new Object();
* o.prop = 'exists';
* o.hasOwnProperty('prop');             // returns true
* o.hasOwnProperty('toString');         // returns false
* o.hasOwnProperty('hasOwnProperty');   // returns false
* </pre>
* <h2> <span> See also </span></h2>
* <p><a href="Operators:Special_Operators:in_Operator" shape="rect" title="Core JavaScript 1.5 Reference:Operators:Special Operators:in Operator">in</a>
* </p>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
 * @type Boolean
*/
hasOwnProperty: function(prop) {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <p><code>isPrototypeOf</code> allows you to check whether or not an object instance has a class in question as one of its parents.
* </p><p>For example, the following code creates a class <code>Question</code> and another class <code>SecondaryQuestion</code> that extends <code>Question</code>:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">function Question () {
* this.answer = 42;
* }
* 
* function SecondaryQuestion () {
* 
* }
* SecondaryQuestion.prototype = new Question();
* </pre>
* <p>Later on down the road, if you instantiate <code>SecondaryQuestion</code> and need to check if it has <code>Question</code> as a parent class, you could do this:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var myquestion = new SecondaryQuestion();
* 
* ...
* 
* if (Question.prototype.isPrototypeOf(myquestion)) {
* // do something with the question
* }
* </pre>
* <p>This particularly comes in handy if you have a function or class method that can only accept an instance of a certain class as its parameter:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">function guessAnswer(q) {
* if (Question.prototype.isPrototypeOf(q)) {
* // Try to answer the question.
* // This doesn't care if you've passed a Question or a
* // SecondaryQuestion, since they're both Questions.
* } else {
* // Get upset
* }
* }
* </pre>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
 * @type Boolean
*/
isPrototypeOf: function(object) {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span> Summary </span></h2>
* <p>Returns a boolean indicating whether the specified property is enumerable.
* </p>
* <h2> <span> Syntax </span></h2>
* <p><code><i>obj</i>.propertyIsEnumerable(<i>prop</i>);</code>
* </p>
* <h2> <span> Parameters </span></h2>
* <dl><dt style="font-weight:bold"> <code>prop</code>Ê</dt><dd> The name of the property to test.
* </dd></dl>
* <h2> <span> Description </span></h2>
* <p>Every object has a <code>propertyIsEnumerable</code> method. This method can determine whether the specified property in an object can be enumerated by a <a href="Core_JavaScript_1.5_Reference:Statements:for...in" shape="rect" title="Core JavaScript 1.5 Reference:Statements:for...in"><code>for...in</code></a> loop, with the exception of properties inherited through the prototype chain. If the object does not have the specified property, this method returns false.
* </p>
* <h2> <span> Examples </span></h2>
* <h3> <span> Example: A basic use of <code>propertyIsEnumerable</code> </span></h3>
* <p>The following example shows the use of <code>propertyIsEnumerable</code> on objects and arrays:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var o = {};
* var a = [];
* o.prop = 'is enumerable';
* a[0] = 'is enumerable';
* 
* o.propertyIsEnumerable('prop');   // returns true
* a.propertyIsEnumerable(0);        // returns true
* </pre>
* <h3> <span> Example: User-defined versus built-in objects </span></h3>
* <p>The following example demonstrates the enumerability of user-defined versus built-in properties:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var a = ['is enumerable'];
* 
* a.propertyIsEnumerable(0);          // returns true
* a.propertyIsEnumerable('length');   // returns false
* 
* Math.propertyIsEnumerable('random');   // returns false
* this.propertyIsEnumerable('Math');     // returns false
* </pre>
* <h3> <span> Example: Direct versus inherited properties </span></h3>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var a = [];
* a.propertyIsEnumerable('constructor');         // returns false
* 
* function firstConstructor()
* {
* this.property = 'is not enumerable';
* }
* 
* function secondConstructor()
* {
* this.method = function method() { return 'is enumerable'; };
* }
* 
* secondConstructor.prototype = new firstConstructor;
* 
* var o = new secondConstructor();
* o.arbitraryProperty = 'is enumerable';
* 
* o.propertyIsEnumerable('arbitraryProperty');   // returns true
* o.propertyIsEnumerable('method');              // returns true
* o.propertyIsEnumerable('property');            // returns false
* 
* o.property = 'is enumerable';
* 
* o.propertyIsEnumerable('property');            // returns true
* </pre>
* <h2> <span> See also </span></h2>
* <p><a href="Core_JavaScript_1.5_Reference:Statements:for...in" shape="rect" title="Core JavaScript 1.5 Reference:Statements:for...in">for...in</a>
* </p>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
 * @type Boolean
*/
propertyIsEnumerable: function(prop) {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span> Summary </span></h2>
* <p>Represents the prototype for this class. You can use the prototype to add properties or methods to all instances of a class. For more information, see <code><a href="Function:prototype" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Function:prototype">Function.prototype</a></code>.
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <td colspan="2" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Property of <a href="Object" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object">Object</a></td>
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
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
prototype: undefined,
/**
* <h2> <span> Summary </span></h2>
* <p>Returns a string representing the object. This method is meant to be overriden by derived objects for locale-specific purposes.
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <td colspan="2" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Method of <a href="Object" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object">Object</a></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">ECMA Version:</td>
* <td colspan="1" rowspan="1">ECMA-262 Edition 3</td>
* </tr>
* </table>
* <h2> <span> Syntax </span></h2>
* <p><code>
* toLocaleString()
* </code>
* </p>
* <h2> <span> Parameters </span></h2>
* <p>None.
* </p>
* <h2> <span> Description </span></h2>
* <p><code>Object</code>'s <code>toLocaleString</code> returns the result of calling <code><a href="Core_JavaScript_1.5_Reference:Global_Objects:Object:toString" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object:toString">toString</a></code>.
* </p><p>This function is provided to give objects a generic <code>toLocaleString</code> method, even though not all may use it. Currently, only <code>Array</code>, <code>Number</code>, and <code>Date</code> override <code>toLocaleString</code>.
* </p>
* <h2> <span> See also </span></h2>
* <p><a href="Core_JavaScript_1.5_Reference:Global_Objects:Object:toString" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object:toString">toString</a>
* </p>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
 * @type String
*/
toLocaleString: function() {
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
* <td colspan="2" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Method of <a href="Object" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object">Object</a></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">Implemented in:</td>
* <td colspan="1" rowspan="1">JavaScript 1.3</td>
* </tr>
* </table>
* <h2> <span> Syntax </span></h2>
* <p><code><i>obj</i>.toSource()</code>
* </p>
* <h2> <span> Parameters </span></h2>
* <p>None.
* </p>
* <h2> <span> Description </span></h2>
* <p>The <code>toSource</code> method returns the following values:
* </p>
* <ul><li> For the built-in <code>Object</code> object, <code>toSource</code> returns the following string indicating that the source code is not available:
* </li></ul>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">function Object() {
* [native code]
* }
* </pre>
* <ul><li> For instances of <code>Object</code>, <code>toSource</code> returns a string representing the source code.
* </li></ul>
* <p>This method is usually called internally by JavaScript and not explicitly in code. You can call <code>toSource</code> while debugging to examine the contents of an object.
* </p>
* <h3> <span> Built-in <code>toString</code> methods </span></h3>
* <p>Each core JavaScript object of a unique class that also provides its own prototype object has a unique <code>toString</code> method. The purpose of this method is to provide an appropriate value when JavaScript needs to convert that object into a string. These objects are:
* </p>
* <ul><li> <a href="Array:toSource" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:toSource">toSource</a> - Array Object method.
* </li><li> <a href="Boolean:toSource" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Boolean:toSource">toSource</a> - Boolean Object method.
* </li><li> <a href="Date:toSource" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Date:toSource">toSource</a> - Date Object method.
* </li><li> <a href="Function:toSource" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Function:toSource">toSource</a> - Function Object method.
* </li><li> <a href="Number:toSource" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Number:toSource">toSource</a> - Number Object method.
* </li><li> <strong>toSource</strong> - Object Object method.
* </li><li> <a href="RegExp:toSource" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:RegExp:toSource">toSource</a> - RegExp Object method.
* </li><li> <a href="String:toSource" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:toSource">toSource</a> - String Object method.
* </li></ul>
* <h2> <span> Examples </span></h2>
* <h3> <span> Example: Using <code>toSource</code> </span></h3>
* <p>The following code defines the <code>Dog</code> object type and creates <code>theDog</code>, an object of type <code>Dog</code>:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">function Dog(name, breed, color, sex) {
* this.name=name;
* this.breed=breed;
* this.color=color;
* this.sex=sex;
* }
* 
* theDog = new Dog("Gabby", "Lab", "chocolate", "girl");
* </pre>
* <p>Calling the <code>toSource</code> method of <code>theDog</code> displays the JavaScript source that defines the object:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">theDog.toSource();
* </pre>
* <p>returns
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">({name:"Gabby", breed:"Lab", color:"chocolate", sex:"girl"})
* </pre>
* <h2> <span> See Also </span></h2>
* <p><a href="Core_JavaScript_1.5_Reference:Global_Objects:Object:toString" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object:toString">toString</a>
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
* <p>Returns a string representing the object.
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <td colspan="2" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Method of <a href="Object" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object">Object</a></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">Implemented in:</td>
* <td colspan="1" rowspan="1">JavaScript 1.0</td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">ECMA Version:</td>
* <td colspan="1" rowspan="1">ECMA-262</td>
* </tr>
* </table>
* <h2> <span> Syntax </span></h2>
* <p><code><i>object</i>.toString()</code>
* </p>
* <h2> <span> Description </span></h2>
* <p>Every object has a <code>toString</code> method that is automatically called when the object is to be represented as a text value or when an object is referred to in a manner in which a string is expected. By default, the <code>toString</code> method is inherited by every object descended from <code>Object</code>. If this method is not overridden in a custom object, <code>toString</code> returns <code>[object <i>type</i>]</code>, where <code><i>type</i></code> is the object type. The following code illustrates this:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var o = new Object();
* o.toString();           // returns [object Object]
* </pre>
* <h3> <span> Built-in <code>toString</code> methods </span></h3>
* <p>Each core JavaScript object has its own unique <code>toString</code> method to return an appropriate value when JavaScript needs to convert that object into a string. These include:
* </p>
* <ul><li> <a href="Core_JavaScript_1.5_Reference:Global_Objects:Array:toString" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:toString">toString</a> - Array Object method.
* </li><li> <a href="Boolean:toString" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Boolean:toString">toString</a> - Boolean Object method.
* </li><li> <a href="Date:toString" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Date:toString">toString</a> - Date Object method.
* </li><li> <a href="Function:toString" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Function:toString">toString</a> - Function Object method.
* </li><li> <a href="JavaArray:toString" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:JavaArray:toString">toString</a> - JavaArray Object method.
* </li><li> <a href="Number:toString" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Number:toString">toString</a> - Number Object method.
* </li><li> <strong>toString</strong> - Object Object method.
* </li><li> <a href="RegExp:toString" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:RegExp:toString">toString</a> - RegExp Object method.
* </li><li> <a href="String:toString" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:String:toString">toString</a> - String Object method.
* </li></ul>
* <h2> <span> Examples </span></h2>
* <h3> <span> Overriding the default <code>toString</code> method </span></h3>
* <p>You can create a function to be called in place of the default <code>toString</code> method. The <code>toString</code> method takes no arguments and should return a string. The <code>toString</code> method you create can be any value you want, but it will be most useful if it carries information about the object.
* </p><p>The following code defines the <code>Dog</code> object type and creates <code>theDog</code>, an object of type <code>Dog</code>:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">function Dog(name,breed,color,sex) {
* this.name=name;
* this.breed=breed;
* this.color=color;
* this.sex=sex;
* }
* 
* theDog = new Dog("Gabby","Lab","chocolate","female");
* </pre>
* <p>If you call the <code>toString</code> method on this custom object, it returns the default value inherited from <code>Object</code>:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">theDog.toString(); //returns [object Object]
* </pre>
* <p>The following code creates and assigns <code>dogToString</code> to override the default <code>toString</code> method. This function generates a string containing the name, breed, color, and sex of the object, in the form "<code>property = value;</code>".
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">Dog.prototype.toString = function dogToString() {
* var ret = "Dog " + this.name + " is a " + this.sex + " " + this.color + " " + this.breed;
* return ret;
* }
* </pre>
* <p>With the preceding code in place, any time <code>theDog</code> is used in a string context, JavaScript automatically calls the <code>dogToString</code> function, which returns the following string:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">Dog Gabby is female chocolate Lab
* </pre>
* <h2> <span> See Also </span></h2>
* <ul><li><a href="Core_JavaScript_1.5_Reference:Global_Objects:Object:toSource" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object:toSource">toSource</a>
* </li><li><a href="Object:valueOf" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object:valueOf">valueOf</a>
* </li></ul>
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
* <p>Removes a watchpoint set with the <code><a href="Object:watch" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object:watch">watch</a></code> method.
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <td colspan="2" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Method of <a href="Object" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object">Object</a></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">Implemented in:</td>
* <td colspan="1" rowspan="1">JavaScript 1.2, NES3.0</td>
* </tr>
* </table>
* <h2> <span> Syntax </span></h2>
* <p><code>
* unwatch(<i>prop</i>)
* </code>
* </p>
* <h2> <span> Parameters </span></h2>
* <dl><dt style="font-weight:bold"> <code>prop</code>Ê</dt><dd> The name of a property of the object.
* </dd></dl>
* <h2> <span> Description </span></h2>
* <p>The JavaScript debugger has functionality similar to that provided by this method, as well as other debugging options. For information on the debugger, see <a href="http://developer.mozilla.org/en/docs/Venkman" shape="rect" title="Venkman">Venkman</a>.
* </p><p>By default, this method is inherited by every object descended from <code>Object</code>.
* </p>
* <h2> <span> Examples </span></h2>
* <p>See <a href="Core_JavaScript_1.5_Reference:Global_Objects:Object:watch" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object:watch">watch</a>.
* </p>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
unwatch: function(prop) {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span> Summary </span></h2>
* <p>Returns the primitive value of the specified object
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <td colspan="2" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Method of <a href="Object" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object">Object</a></td>
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
* <p>JavaScript calls the <code>valueOf</code> method to convert an object to a primitive value. You rarely need to invoke the <code>valueOf</code> method yourself; JavaScript automatically invokes it when encountering an object where a primitive value is expected.
* </p><p>By default, the <code>valueOf</code> method is inherited by every object descended from <code>Object</code>. Every built-in core object overrides this method to return an appropriate value. If an object has no primitive value, <code>valueOf</code> returns the object itself, which is displayed as:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* [object Object]
* </pre>
* <p>You can use <code>valueOf</code> within your own code to convert a built-in object into a primitive value. When you create a custom object, you can override <code>Object.valueOf</code> to call a custom method instead of the default <code>Object</code> method.
* </p>
* <h3> <span> Overriding <code>valueOf</code> for custom objects </span></h3>
* <p>You can create a function to be called in place of the default <code>valueOf</code> method. Your function must take no arguments.
* </p><p>Suppose you have an object type <code>myNumberType</code> and you want to create a <code>valueOf</code> method for it. The following code assigns a user-defined function to the object's <code>valueOf</code> method:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* myNumberType.prototype.valueOf = new Function(functionText)
* </pre>
* <p>With the preceding code in place, any time an object of type <code>myNumberType</code> is used in a context where it is to be represented as a primitive value, JavaScript automatically calls the function defined in the preceding code.
* </p><p>An object's <code>valueOf</code> method is usually invoked by JavaScript, but you can invoke it yourself as follows:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* myNumber.valueOf()
* </pre>
* <h3> <span> Note </span></h3>
* <p>Objects in string contexts convert via the <code><a href="Core_JavaScript_1.5_Reference:Global_Objects:Object:toString" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object:toString">toString</a></code> method, which is different from <code>String</code> objects converting to string primitives using <code>valueOf</code>. All string objects have a string conversion, if only "<code>[object <i>type</i>]</code>". But many objects do not convert to number, boolean, or function.
* </p>
* <h2> <span> See Also </span></h2>
* <p><a href="Core_JavaScript_1.5_Reference:Global_Functions:parseInt" shape="rect" title="Core JavaScript 1.5 Reference:Global Functions:parseInt">parseInt</a>,
* <a href="Object:toString" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object:toString">toString</a>
* </p>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
valueOf: function() {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span> Summary </span></h2>
* <p>Watches for a property to be assigned a value and runs a function when that occurs.
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <td colspan="2" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Method of <a href="Object" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object">Object</a></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">Implemented in:</td>
* <td colspan="1" rowspan="1">JavaScript 1.2, NES 3.0</td>
* </tr>
* </table>
* <h2> <span> Syntax </span></h2>
* <p><code>
* watch(<i>prop</i>, <i>handler</i>)
* </code>
* </p>
* <h2> <span> Parameters </span></h2>
* <dl><dt style="font-weight:bold"> <code>prop</code>Ê</dt><dd> The name of a property of the object.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <code>handler</code>Ê</dt><dd> A function to call.
* </dd></dl>
* <h2> <span> Description </span></h2>
* <p>Watches for assignment to a property named <code>prop</code> in this object, calling <code>handler(prop, oldval, newval)</code> whenever <code>prop</code> is set and storing the return value in that property. A watchpoint can filter (or nullify) the value assignment, by returning a modified <code>newval</code> (or by returning <code>oldval</code>).
* </p><p>If you delete a property for which a watchpoint has been set, that watchpoint does not disappear. If you later recreate the property, the watchpoint is still in effect.
* </p><p>To remove a watchpoint, use the <code><a href="Core_JavaScript_1.5_Reference:Global_Objects:Object:unwatch" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object:unwatch">unwatch</a></code> method. By default, the <code>watch</code> method is inherited by every object descended from <code>Object</code>.
* </p><p>The JavaScript debugger has functionality similar to that provided by this method, as well as other debugging options. For information on the debugger, see <a href="http://developer.mozilla.org/en/docs/Venkman" shape="rect" title="Venkman">Venkman</a>.
* </p><p>In NES 3.0 and 4.x, <code>handler</code> is called from assignments in script as well as native code.  In Firefox, <code>handler</code> is only called from assignments in script, not from native code.  For example, <code>window.watch('location', myHandler)</code> will not call <code>myHandler</code> if the user clicks a link to an anchor within the current document.  However, the following code will call <code>myHandler</code>:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve"><code>window.location += '#myAnchor';</code>
* </pre>
* <h2> <span> Examples </span></h2>
* <h3> <span> Example: Using <code>watch</code> and <code>unwatch</code> </span></h3>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* &lt;script language="JavaScript"&gt;
* 
* var o = {p:1};
* o.watch("p",
* function (id,oldval,newval) {
* document.writeln("o." + id + " changed from "
* + oldval + " to " + newval);
* return newval;
* });
* 
* o.p = 2;
* o.p = 3;
* delete o.p;
* o.p = 4;
* 
* o.unwatch('p');
* o.p = 5;
* 
* &lt;/script&gt;
* </pre>
* <p>This script displays the following:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* o.p changed from 1 to 2
* o.p changed from 2 to 3
* o.p changed from undefined to 4
* </pre>
* <h3> <span> Example: Using <code>watch</code> to validate an object's properties </span></h3>
* <p>You can use <code>watch</code> to test any assignment to an object's properties. This example ensures that every Person always has a valid name and an age between 0 and 200.
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* &lt;script language="JavaScript"&gt;
* 
* Person = function(name,age) {
* this.watch("age", Person.prototype._isValidAssignment);
* this.watch("name",Person.prototype._isValidAssignment);
* this.name=name;
* this.age=age;
* };
* 
* Person.prototype.toString = function() { return this.name+","+this.age; };
* 
* Person.prototype._isValidAssignment = function(id,oldval,newval) {
* if (id=="name" &amp;&amp; (!newval || newval.length&gt;30)) { throw new RangeError("invalid name for "+this); }
* if (id=="age"  &amp;&amp; (newval&lt;0 || newval&gt;200))      { throw new RangeError("invalid age  for "+this ); }
* return newval;
* };
* 
* will = new Person("Will",29); // --&gt; Will,29
* document.writeln(will);
* 
* try {
* will.name="";  // --&gt; Error "invalid name for Will,29"
* } catch (e) { document.writeln(e); }
* 
* try {
* will.age=-4;   // --&gt; Error "invalid age  for Will,29"
* } catch (e) { document.writeln(e); }
* 
* &lt;/script&gt;
* 
* </pre>
* <p>This script displays the following:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* Will,29
* RangeError: invalid name for Will,29
* RangeError: invalid age  for Will,29
* </pre>
* <h2> <span> See Also </span></h2>
* <p><a href="Object:unwatch" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object:unwatch">unwatch</a>
* </p>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
watch: function(prop, handler) {
  // This is just a stub for a builtin native JavaScript object.
},
};
/**
* <h2> <span> Summary </span></h2>
* <p><b>Core Object</b>
* </p><p><code>Object</code> is the primitive JavaScript object type. All JavaScript objects are descended from <code>Object</code>. That is, all JavaScript objects have the methods defined for <code>Object</code>.
* </p>
* <h2> <span> Created by </span></h2>
* <p>The <code>Object</code> constructor:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">new Object()
* </pre>
* <h2> <span> Parameters </span></h2>
* <p>None.
* </p>
* <h2> <span> Properties </span></h2>
* <p><a href="Core_JavaScript_1.5_Reference:Global_Objects:Object:constructor" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object:constructor">constructor</a>: Specifies the function that creates an object's prototype.
* </p><p><a href="Object:prototype" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object:prototype">prototype</a>: Allows the addition of properties to all objects.
* </p>
* <h2> <span> Methods </span></h2>
* <p><a href="http://developer.mozilla.org/en/docs/index.php?title=Core_JavaScript_1.5_Reference:Global_Objects:Object:defineGetter&amp;action=edit" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object:defineGetter">__defineGetter__</a>
* </p><p><a href="http://developer.mozilla.org/en/docs/index.php?title=Object:defineSetter&amp;action=edit" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object:defineSetter">__defineSetter__</a>
* </p><p><a href="Object:eval" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object:eval">eval</a>: <span style="border: 1px solid #9898F0; background-color: #DDDDFF; font-size: 9px; vertical-align: text-top;">Deprecated</span> Evaluates a string of JavaScript code in the context of the specified object.
* </p><p><a href="Object:hasOwnProperty" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object:hasOwnProperty">hasOwnProperty</a>
* </p><p><a href="Object:isPrototypeOf" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object:isPrototypeOf">isPrototypeOf</a>
* </p><p><a href="http://developer.mozilla.org/en/docs/index.php?title=Object:lookupGetter&amp;action=edit" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object:lookupGetter">__lookupGetter__</a>
* </p><p><a href="http://developer.mozilla.org/en/docs/index.php?title=Object:lookupSetter&amp;action=edit" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object:lookupSetter">__lookupSetter__</a>
* </p><p><a href="Object:propertyIsEnumerable" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object:propertyIsEnumerable">propertyIsEnumerable</a>
* </p><p><a href="Object:toSource" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object:toSource">toSource</a>: Returns an object literal representing the specified object; you can use this value to create a new object.
* </p><p><a href="Object:toString" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object:toString">toString</a>: Returns a string representing the specified object.
* </p><p><a href="Object:unwatch" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object:unwatch">unwatch</a>: Removes a watchpoint from a property of the object.
* </p><p><a href="Object:valueOf" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object:valueOf">valueOf</a>: Returns the primitive value of the specified object.
* </p><p><a href="Object:watch" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object:watch">watch</a>: Adds a watchpoint to a property of the object.
* </p>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
function Object() {};

