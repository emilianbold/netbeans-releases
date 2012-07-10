/**
* <div id="contentSub">(Redirected from <a href="http://developer.mozilla.org/en/docs/index.php?title=Core_JavaScript_1.5_Reference:Objects:Number&amp;redirect=no" shape="rect" title="Core JavaScript 1.5 Reference:Objects:Number">Core JavaScript 1.5 Reference:Objects:Number</a>)</div>
* 
* <h2> <span> Summary </span></h2>
* <p><b>Core Object</b>
* </p><p>Lets you work with numeric values.  The <code>Number</code> object is an object wrapper for primitive numeric values.
* </p>
* <h2> <span> Created by </span></h2>
* <p>The <code>Number</code> constructor:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">new Number(<i>value</i>)
* </pre>
* <h2> <span> Parameters </span></h2>
* <dl><dt style="font-weight:bold"> <code>value</code>Ê</dt><dd> The numeric value of the object being created.
* </dd></dl>
* <h2> <span> Description </span></h2>
* <p>The primary uses for the <code>Number</code> object are:
* </p>
* <ul><li> To access its constant properties, which represent the largest and smallest representable numbers, positive and negative infinity, and the Not-a-Number value.
* </li></ul>
* <ul><li> To create numeric objects that you can add properties to. Most likely, you will rarely need to create a <code>Number</code> object.
* </li></ul>
* <p>The properties of <code>Number</code> are properties of the class itself, not of individual <code>Number</code> objects.
* </p><p><b>JavaScript 1.2</b>: <code>Number(x)</code> now produces <code>NaN</code> rather than an error if <code>x</code> is a string that does not contain a well-formed numeric literal. For example, the following prints <code>NaN</code>:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* x=Number("three");
* document.write(x + "&lt;BR&gt;");
* </pre>
* <p>You can convert any object to a number using the top-level <a href="Number" shape="rect" title="Core JavaScript 1.5 Reference:Functions:Number">Number</a> function.
* </p>
* <h2> <span> Properties </span></h2>
* <p><a href="Core_JavaScript_1.5_Reference:Global_Objects:Number:constructor" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Number:constructor">constructor</a>: Specifies the function that creates an object's prototype.
* </p><p><a href="Number:MAX_VALUE" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Number:MAX VALUE">MAX_VALUE</a>: The largest representable number.
* </p><p><a href="Number:MIN_VALUE" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Number:MIN VALUE">MIN_VALUE</a>: The smallest representable number.
* </p><p><a href="Number:NaN" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Number:NaN">NaN</a>: Special "not a number" value.
* </p><p><a href="Number:NEGATIVE_INFINITY" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Number:NEGATIVE INFINITY">NEGATIVE_INFINITY</a>: Special value representing negative infinity; returned on overflow.
* </p><p><a href="Number:POSITIVE_INFINITY" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Number:POSITIVE INFINITY">POSITIVE_INFINITY</a>: Special value representing infinity; returned on overflow.
* </p><p><a href="Number:prototype" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Number:prototype">prototype</a>: Allows the addition of properties to a Number object.
* </p>
* <h2> <span> Methods </span></h2>
* <p><a href="Core_JavaScript_1.5_Reference:Global_Objects:Number:toExponential" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Number:toExponential">toExponential</a>: Returns a string representing the number in exponential notation.
* </p><p><a href="Number:toFixed" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Number:toFixed">toFixed</a>: Returns a string representing the number in fixed-point notation.
* </p><p><a href="http://developer.mozilla.org/en/docs/index.php?title=Number:toLocaleString&amp;action=edit" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Number:toLocaleString">toLocaleString</a>:
* Returns a human readable string representing the number using the locale of the environment. Overrides the <a href="Object:toLocaleString" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object:toLocaleString">Object.toLocaleString</a> method.
* </p><p><a href="Number:toPrecision" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Number:toPrecision">toPrecision</a>: Returns a string representing the number to a specified precision in fixed-point or exponential notation.
* </p><p><a href="Number:toSource" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Number:toSource">toSource</a>: Returns an object literal representing the specified Number object; you can use this value to create a new object. Overrides the <a href="Object:toSource" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object:toSource">Object.toSource</a> method.
* </p><p><a href="Number:toString" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Number:toString">toString</a>: Returns a string representing the specified object. Overrides the <a href="Object:toString" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object:toString">Object.toString</a> method.
* </p><p><a href="Number:valueOf" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Number:valueOf">valueOf</a>: Returns the primitive value of the specified object. Overrides the <a href="Object:valueOf" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object:valueOf">Object.valueOf</a> method.
* </p><p>In addition, this object inherits the <a href="Object:watch" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object:watch">watch</a> and <a href="Object:unwatch" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object:unwatch">unwatch</a> methods from <a href="Object" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object">Object</a>.
* </p>
* <h2> <span> Examples </span></h2>
* <h3> <span> Example: Using the <code>Number</code> object to assign values to numeric variables </span></h3>
* <p>The following example uses the <code>Number</code> object's properties to assign values to several numeric variables:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">biggestNum = Number.MAX_VALUE;
* smallestNum = Number.MIN_VALUE;
* infiniteNum = Number.POSITIVE_INFINITY;
* negInfiniteNum = Number.NEGATIVE_INFINITY;
* notANum = Number.NaN;
* </pre>
* <h3> <span> Example: Using <code>Number</code> object to modify all <code>Number</code> objects </span></h3>
* <p>The following example creates a Number object, myNum, then adds a description property to all Number objects. Then a value is assigned to the myNum object's description property.
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">myNum = new Number(65);
* Number.prototype.description = null;
* myNum.description = "wind speed";
* </pre>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
var Number = {
  // This is just a stub for a builtin native JavaScript object.
/**
* <h2> <span> Summary </span></h2>
* <p>The maximum numeric value representable in JavaScript.
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <td colspan="2" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Property of <a href="Number" shape="rect" title="Core JavaScript 1.5 Reference:Objects:Number">Number</a></td>
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
* <p>The <code>MAX_VALUE</code> property has a value of approximately 1.79E+308. Values larger than <code>MAX_VALUE</code> are represented as "<code>Infinity</code>".
* </p><p>Because <code>MAX_VALUE</code> is a static property of <code>Number</code>, you always use it as <code>Number.MAX_VALUE</code>, rather than as a property of a <code>Number</code> object you created.
* </p>
* <h2> <span> Examples </span></h2>
* <h3> <span> Example: Using <code>MAX_VALUE</code> </span></h3>
* <p>The following code multiplies two numeric values. If the result is less than or equal to <code>MAX_VALUE</code>, the <code>func1</code> function is called; otherwise, the <code>func2</code> function is called.
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">if (num1 * num2 &lt;= Number.MAX_VALUE)
* func1();
* else
* func2();
* </pre>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
 * @type Number
*/
MAX_VALUE: undefined,
/**
* <h2> <span> Summary </span></h2>
* <p>The smallest positive numeric value representable in JavaScript.
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <td colspan="2" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Property of <a href="Number" shape="rect" title="Core JavaScript 1.5 Reference:Objects:Number">Number</a></td>
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
* <p>The <code>MIN_VALUE</code> property is the number closest to 0, not the most negative number, that JavaScript can represent.
* </p><p><code>MIN_VALUE</code> has a value of approximately 5e-324. Values smaller than <code>MIN_VALUE</code> ("underflow values") are converted to 0.
* </p><p>Because <code>MIN_VALUE</code> is a static property of <code>Number</code>, you always use it as <code>Number.MIN_VALUE</code>, rather than as a property of a <code>Number</code> object you created.
* </p>
* <h2> <span> Examples </span></h2>
* <h3> <span> Example: Using <code>MIN_VALUE</code> </span></h3>
* <p>The following code divides two numeric values. If the result is greater than or equal to <code>MIN_VALUE</code>, the <code>func1</code> function is called; otherwise, the <code>func2</code> function is called.
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* if (num1 / num2 &gt;= Number.MIN_VALUE)
* func1()
* else
* func2()
* </pre>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
 * @type Number
*/
MIN_VALUE: undefined,
/**
* <h2> <span> Summary </span></h2>
* <p>A value representing the negative Infinity value.
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <td colspan="2" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Property of <a href="Number" shape="rect" title="Core JavaScript 1.5 Reference:Objects:Number">Number</a></td>
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
* <p>The value of <code>Number.NEGATIVE_INFINITY</code> is the same as the negative value of the global object's <a href="Core_JavaScript_1.5_Reference:Global_Properties:Infinity" shape="rect" title="Core JavaScript 1.5 Reference:Global Properties:Infinity">Infinity</a> property.
* </p><p>This value behaves slightly differently than mathematical infinity:
* </p>
* <ul><li> Any positive value, including POSITIVE_INFINITY, multiplied by NEGATIVE_INFINITY is NEGATIVE_INFINITY.
* </li><li> Any negative value, including NEGATIVE_INFINITY, multiplied by NEGATIVE_INFINITY is POSITIVE_INFINITY.
* </li><li> Zero multiplied by NEGATIVE_INFINITY is NaN.
* </li><li> NaN multiplied by NEGATIVE_INFINITY is NaN.
* </li><li> NEGATIVE_INFINITY, divided by any negative value except NEGATIVE_INFINITY, is POSITIVE_INFINITY.
* </li><li> NEGATIVE_INFINITY, divided by any positive value except POSITIVE_INFINITY, is NEGATIVE_INFINITY.
* </li><li> NEGATIVE_INFINITY, divided by either NEGATIVE_INFINITY or POSITIVE_INFINITY, is NaN.
* </li><li> Any number divided by NEGATIVE_INFINITY is Zero.
* </li></ul>
* <p>Several JavaScript methods (such as the <code>Number</code> constructor, <code>parseFloat</code>, and <code>parseInt</code>) return <code>NaN</code> if the value specified in the parameter is significantly lower than <code>Number.MIN_VALUE</code>.
* </p><p>You might use the <code>Number.NEGATIVE_INFINITY</code> property to indicate an error condition that returns a finite number in case of success. Note, however, that <a href="isFinite" shape="rect" title="Core JavaScript 1.5 Reference:Global Functions:isFinite">isFinite</a> would be more appropriate in such a case.
* </p>
* <h2> <span> Example </span></h2>
* <p>In the following example, the variable smallNumber is assigned a value that is smaller than the minimum value. When the <code>if</code> statement executes, smallNumber has the value "<code>-Infinity</code>", so smallNumber is set to a more manageable value before continuing.
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* var smallNumber = (-Number.MAX_VALUE) * 2
* if (smallNumber == Number.NEGATIVE_INFINITY) {
* smallNumber = returnFinite();
* }
* </pre>
* <h2> <span> See Also </span></h2>
* <p><a href="Infinity" shape="rect" title="Core JavaScript 1.5 Reference:Global Properties:Infinity">Infinity</a>,
* <a href="Number:POSITIVE_INFINITY" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Number:POSITIVE INFINITY">POSITIVE_INFINITY</a>,
* <a href="isFinite" shape="rect" title="Core JavaScript 1.5 Reference:Global Functions:isFinite">isFinite</a>
* </p>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
 * @type Number
*/
NEGATIVE_INFINITY: undefined,
/**
* <h2> <span> Summary </span></h2>
* <p>A value representing Not-A-Number.
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <td colspan="2" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Property of <a href="Number" shape="rect" title="Core JavaScript 1.5 Reference:Objects:Number">Number</a></td>
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
* <p>The value of <code>Number.NaN</code> is Not-A-Number, same as the value of global object's <a href="Core_JavaScript_1.5_Reference:Properties:NaN" shape="rect" title="Core JavaScript 1.5 Reference:Properties:NaN">NaN</a> property.
* </p><p><code>NaN</code> is always unequal to any other number, including <code>NaN</code> itself; you cannot check for the not-a-number value by comparing to <code>Number.NaN</code>. Use the <code><a href="isNaN" shape="rect" title="Core JavaScript 1.5 Reference:Functions:isNaN">isNaN</a></code> function instead.
* </p><p>Several JavaScript methods (such as the <code>Number</code> constructor, <code>parseFloat</code>, and <code>parseInt</code>) return <code>NaN</code> if the value specified in the parameter can not be parsed as a number.
* </p><p>You might use the <code>NaN</code> property to indicate an error condition for your function that returns a number in case of success.
* </p><p>JavaScript prints the value <code>Number.NaN</code> as <code>NaN</code>.
* </p>
* <h2> <span> Examples </span></h2>
* <h3> <span> Example: Using <code>NaN</code> </span></h3>
* <p>In the following example, if month has a value greater than 12, it is assigned <code>NaN</code>, and a message is displayed indicating valid values.
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* var month = 13
* if (month &lt; 1 || month &gt; 12) {
* month = Number.NaN
* alert("Month must be between 1 and 12.")
* }
* </pre>
* <h2> <span> See Also </span></h2>
* <p><a href="Core_JavaScript_1.5_Reference:Properties:NaN" shape="rect" title="Core JavaScript 1.5 Reference:Properties:NaN">NaN</a>,
* <a href="isNaN" shape="rect" title="Core JavaScript 1.5 Reference:Functions:isNaN">isNaN</a>,
* <a href="parseFloat" shape="rect" title="Core JavaScript 1.5 Reference:Functions:parseFloat">parseFloat</a>,
* <a href="parseInt" shape="rect" title="Core JavaScript 1.5 Reference:Functions:parseInt">parseInt</a>
* </p>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
 * @type Number
*/
NaN: undefined,
/**
* <h2> <span> Summary </span></h2>
* <p>A value representing the positive Infinity value.
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <td colspan="2" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Property of <a href="Number" shape="rect" title="Core JavaScript 1.5 Reference:Objects:Number">Number</a></td>
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
* <p>The value of <code>Number.POSITIVE_INFINITY</code> is the same as the value of the global object's <a href="Core_JavaScript_1.5_Reference:Global_Properties:Infinity" shape="rect" title="Core JavaScript 1.5 Reference:Global Properties:Infinity">Infinity</a> property.
* </p><p>This value behaves slightly differently than mathematical infinity:
* </p>
* <ul><li> Any positive value, including POSITIVE_INFINITY, multiplied by POSITIVE_INFINITY is POSITIVE_INFINITY.
* </li><li> Any negative value, including NEGATIVE_INFINITY, multiplied by POSITIVE_INFINITY is NEGATIVE_INFINITY.
* </li><li> Zero multiplied by POSITIVE_INFINITY is NaN.
* </li><li> NaN multiplied by POSITIVE_INFINITY is NaN.
* </li><li> POSITIVE_INFINITY, divided by any negative value except NEGATIVE_INFINITY, is NEGATIVE_INFINITY.
* </li><li> POSITIVE_INFINITY, divided by any positive value except POSITIVE_INFINITY, is POSITIVE_INFINITY.
* </li><li> POSITIVE_INFINITY, divided by either NEGATIVE_INFINITY or POSITIVE_INFINITY, is NaN.
* </li><li> Any number divided by POSITIVE_INFINITY is Zero.
* </li></ul>
* <p>Several JavaScript methods (such as the <code>Number</code> constructor, <code>parseFloat</code>, and <code>parseInt</code>) return <code>NaN</code> if the value specified in the parameter is significantly higher than <code>Number.MAX_VALUE</code>.
* </p><p>You might use the <code>Number.POSITIVE_INFINITY</code> property to indicate an error condition that returns a finite number in case of success. Note, however, that <a href="isFinite" shape="rect" title="Core JavaScript 1.5 Reference:Global Functions:isFinite">isFinite</a> would be more appropriate in such a case.
* </p>
* <h2> <span> Example </span></h2>
* <p>In the following example, the variable bigNumber is assigned a value that is larger than the maximum value. When the <code>if</code> statement executes, bigNumber has the value "<code>Infinity</code>", so bigNumber is set to a more manageable value before continuing.
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* var bigNumber = Number.MAX_VALUE * 2
* if (bigNumber == Number.POSITIVE_INFINITY) {
* bigNumber = returnFinite();
* }
* </pre>
* <h2> <span> See Also </span></h2>
* <p><a href="Infinity" shape="rect" title="Core JavaScript 1.5 Reference:Global Properties:Infinity">Infinity</a>,
* <a href="Number:NEGATIVE_INFINITY" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Number:NEGATIVE INFINITY">NEGATIVE_INFINITY</a>,
* <a href="isFinite" shape="rect" title="Core JavaScript 1.5 Reference:Global Functions:isFinite">isFinite</a>
* </p>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
 * @type Number
*/
POSITIVE_INFINITY: undefined,
/**
* <h2> <span> Summary </span></h2>
* <p>Returns a reference to the <a href="Number" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Number">Number</a> function that created the instance's prototype. Note that the value of this property is a reference to the function itself, not a string containing the function's name.
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <td colspan="2" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Property of <a href="Number" shape="rect" title="Core JavaScript 1.5 Reference:Objects:Number">Number</a></td>
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
* <p>See <a href="Core_JavaScript_1.5_Reference:Objects:Object:constructor" shape="rect" title="Core JavaScript 1.5 Reference:Objects:Object:constructor">Object.constructor</a>.
* </p>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
constructor: undefined,
/**
* <h2> <span> Summary </span></h2>
* <p>Represents the prototype for this class. You can use the prototype to add properties or methods to all instances of a class. For information on prototypes, see <a href="Function:prototype" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Function:prototype">Function.prototype</a>.
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <td colspan="2" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Property of <a href="Number" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Number">Number</a></td>
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
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
prototype: undefined,
/**
* <h2> <span> Summary </span></h2>
* <p>Returns a string representing the Number object in exponential notation
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <td colspan="2" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Method of <a href="Number" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Number">Number</a></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">Implemented in:</td>
* <td colspan="1" rowspan="1">JavaScript 1.5</td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">ECMA Version:</td>
* <td colspan="1" rowspan="1">ECMA-262</td>
* </tr>
* </table>
* <h2> <span> Syntax </span></h2>
* <p><code><i>number</i>.toExponential( [<i>fractionDigits</i>] )</code>
* </p>
* <h2> <span> Parameter </span></h2>
* <dl><dt style="font-weight:bold"> fractionDigits
* </dt><dd> An integer specifying the number of digits after the decimal point. Defaults to as many digits as necessary to specify the number.
* </dd></dl>
* <h2> <span> Returns </span></h2>
* <p>A string representing a Number object in exponential notation with one digit before the decimal point, rounded to <code>fractionDigits</code> digits after the decimal point. If the <code>fractionDigits</code> argument is omitted, the number of digits after the decimal point defaults to the number of digits necessary to represent the value uniquely.
* </p><p>If you use the <code>toExponential</code> method for a numeric literal and the numeric literal has no exponent and no decimal point, leave a space before the dot that precedes the method call to prevent the dot from being interpreted as a decimal point.
* </p><p>If a number has more digits that requested by the <code>fractionDigits</code> parameter, the number is rounded to the nearest number represented by <code>fractionDigits</code> digits. See the discussion of rounding in the description of the <a href="Core_JavaScript_1.5_Reference:Global_Objects:Number:toFixed" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Number:toFixed">toFixed</a> method, which also applies to <code>toExponential</code>.
* </p>
* <h2> <span> Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* var num=77.1234;
* 
* alert("num.toExponential() is " + num.toExponential()); //displays 7.71234e+1
* 
* alert("num.toExponential(4) is " + num.toExponential(4)); //displays 7.7123e+1
* 
* alert("num.toExponential(2) is " + num.toExponential(2)); //displays 7.71e+1
* 
* alert("77.1234.toExponential() is " + 77.1234.toExponential()); //displays 7.71234e+1
* 
* alert("77 .toExponential() is " + 77 .toExponential()); //displays 7.7e+1
* </pre>
* <h2> <span> See Also </span></h2>
* <p><a href="Number:toFixed" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Number:toFixed">Number.toFixed()</a>,
* <a href="Number:toPrecision" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Number:toPrecision">Number.toPrecision()</a>,
* <a href="Number:toString" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Number:toString">Number.toString()</a>
* </p>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
 * @type String
*/
toExponential: function( fractionDigits ) {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span> Summary </span></h2>
* <p>Formats a number using fixed-point notation
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <td colspan="2" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Method of <a href="Number" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Number">Number</a></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">Implemented in:</td>
* <td colspan="1" rowspan="1">JavaScript 1.5</td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">ECMA Version:</td>
* <td colspan="1" rowspan="1">ECMA-262</td>
* </tr>
* </table>
* <h2> <span> Syntax </span></h2>
* <p><code><i>number</i>.toFixed( [<i>digits</i>] )</code>
* </p>
* <h2> <span> Parameter </span></h2>
* <dl><dt style="font-weight:bold"> digits
* </dt><dd> The number of digits to appear after the decimal point; this may be a value between 0 and 20, inclusive, and implementations may optionally support a larger range of values. If this argument is omitted, it is treated as 0.
* </dd></dl>
* <h2> <span> Returns </span></h2>
* <p>A string representation of <code>number</code> that does not use exponential notation and has exactly <code>digits</code> digits after the decimal place. The number is rounded if necessary, and the fractional part is padded with zeros if necessary so that it has the specified length. If <code>number</code> is greater than 1e+21, this method simply calls <code>Number.toString()</code> and returns a string in exponential notation.
* </p>
* <h2> <span> Throws </span></h2>
* <dl><dt style="font-weight:bold"> RangeError
* </dt><dd> If digits is too small or too large. Values between 0 and 20, inclusive, will not cause a <code>RangeError</code>. Implementations are allowed to support larger and smaller values as well.
* </dd></dl>
* <dl><dt style="font-weight:bold"> TypeError
* </dt><dd> If this method is invoked on an object that is not a <code>Number</code>.
* </dd></dl>
* <h2> <span> Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* var n = 12345.6789;
* 
* n.toFixed();              // Returns 12346: note rounding, no fractional part
* 
* n.toFixed(1);             // Returns 12345.7: note rounding
* 
* n.toFixed(6);             // Returns 12345.678900: note added zeros
* 
* (1.23e+20).toFixed(2);    // Returns 123000000000000000000.00
* 
* (1.23e-10).toFixed(2)     // Returns 0.00
* </pre>
* <h2> <span> See Also </span></h2>
* <p><a href="Number:toExponential" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Number:toExponential">Number.toExponential()</a>,
* <a href="http://developer.mozilla.org/en/docs/index.php?title=Number:toLocaleString&amp;action=edit" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Number:toLocaleString">Number.toLocaleString()</a>,
* <a href="Number:toPrecision" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Number:toPrecision">Number.toPrecision()</a>,
* <a href="Number:toString" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Number:toString">Number.toString()</a>,
* <a href="Number:toSource" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Number:toSource">Number.toSource()</a>,
* <a href="Number:valueOf" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Number:valueOf">Number.valueOf()</a>
* </p>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
 * @type String
*/
toFixed: function( digits ) {
  // This is just a stub for a builtin native JavaScript object.
},
toLocaleString: function() {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span> Summary </span></h2>
* <p>Returns a string representing the Number object to the specified precision
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <td colspan="2" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Method of <a href="Number" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Number">Number</a></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">Implemented in:</td>
* <td colspan="1" rowspan="1">JavaScript 1.5</td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">ECMA Version:</td>
* <td colspan="1" rowspan="1">ECMA-262</td>
* </tr>
* </table>
* <h2> <span> Syntax </span></h2>
* <p><code><i>number</i>.toPrecision( [<i>precision</i>] )</code>
* </p>
* <h2> <span> Parameter </span></h2>
* <dl><dt style="font-weight:bold"> precision
* </dt><dd> An integer specifying the number of digits after the decimal point.
* </dd></dl>
* <h2> <span> Returns </span></h2>
* <p>A string representing a Number object in fixed-point or exponential notation rounded to <code>precision</code> significant digits.
* </p><p>If you use the <code>toPrecision</code> method for a numeric literal and the numeric literal has no exponent and no decimal point, leave a space before the dot that precedes the method call to prevent the dot from being interpreted as a decimal point.
* </p><p>If the <code>precision</code> argument is omitted, behaves as <a href="Core_JavaScript_1.5_Reference:Global_Objects:Number:toString" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Number:toString">Number.toString()</a>.
* </p><p>If a number has more digits that requested by the <code>precision</code> parameter, the number is rounded to the nearest number represented by <code>precision</code> digits. See the discussion of rounding in the description of the <a href="Number:toFixed" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Number:toFixed">toFixed</a> method, which also applies to <code>toPrecision</code>.
* </p>
* <h2> <span> Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* var num=5.123456;
* 
* alert("num.toPrecision() is " + num.toPrecision()); //displays 5.123456
* 
* alert("num.toPrecision(4) is " + num.toPrecision(4)); //displays 5.123
* 
* alert("num.toPrecision(2) is " + num.toPrecision(2)); //displays 5.1
* 
* alert("num.toPrecision(1) is " + num.toPrecision(1)); //displays 5
* 
* alert("1250 .toPrecision(2) is " + 1250 .toPrecision(2)); //displays 1.3e+3
* 
* alert("1250 .toPrecision(5) is " + 1250 .toPrecision(5)); //displays 1250.0
* </pre>
* <h2> <span> See Also </span></h2>
* <p><a href="Number:toExponential" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Number:toExponential">Number.toExponential()</a>,
* <a href="Number:toFixed" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Number:toFixed">Number.toFixed()</a>,
* <a href="Number:toString" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Number:toString">Number.toString()</a>
* </p>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
 * @type String
*/
toPrecision: function( precision ) {
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
* <td colspan="2" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Method of <a href="Number" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Number">Number</a></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">Implemented in:</td>
* <td colspan="1" rowspan="1">JavaScript 1.3</td>
* </tr>
* </table>
* <h2> <span> Syntax </span></h2>
* <p><code><i>number</i>.toSource()</code>
* </p>
* <h2> <span> Parameters </span></h2>
* <p>None.
* </p>
* <h2> <span> Description </span></h2>
* <p>The <code>toSource</code> method returns the following values:
* </p>
* <ul><li> For the built-in <code>Number</code> object, <code>toSource</code> returns the following string indicating that the source code is not available:
* </li></ul>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">function Number() {[native code]}
* </pre>
* <ul><li> For instances of <code>Number</code>, <code>toSource</code> returns a string representing the source code.
* </li></ul>
* <p>This method is usually called internally by JavaScript and not explicitly in code.
* </p>
* <h2> <span> See Also </span></h2>
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
* <p>Returns a string representing the specified Number object
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <td colspan="2" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Method of <a href="Number" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Number">Number</a></td>
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
* <p><code><i>number</i>.toString( [<i>radix</i>] )</code>
* </p>
* <h2> <span> Parameter </span></h2>
* <dl><dt style="font-weight:bold"> radix
* </dt><dd> An integer between 2 and 36 specifying the base to use for representing numeric values.
* </dd></dl>
* <h2> <span> Description </span></h2>
* <p>The <code>Number</code> object overrides the <code>toString</code> method of the <code><a href="Core_JavaScript_1.5_Reference:Global_Objects:Object" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object">Object</a></code> object; it does not inherit <code><a href="Object:toString" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object:toString">Object.toString</a></code>. For <code>Number</code> objects, the <code>toString</code> method returns a string representation of the object in the specified radix.
* </p><p>The toString method parses its first argument, and attempts to return a string representation in the specified radix (base). For radixes above 10, the letters of the alphabet indicate numerals greater than 9. For example, for hexadecimal numbers (base 16), A through F are used.
* </p><p>If toString is given a radix not between 2 and 36, an exception is thrown.
* </p><p>If the radix is not specified, JavaScript assumes the preferred radix is 10.
* </p>
* <h2> <span> Examples </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var count = 10;
* print(count.toString());   // displays "10"
* print((17).toString());    // displays "17"
* 
* var x = 7;
* print(x.toString(2));      // displays "111"
* </pre>
* <h2> <span> See Also </span></h2>
* <ul><li><a href="Number:toExponential" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Number:toExponential">Number.prototype.toExponential</a>
* </li><li><a href="http://developer.mozilla.org/en/docs/index.php?title=Number:toLocaleString&amp;action=edit" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Number:toLocaleString">Number.prototype.toLocaleString</a>
* </li><li><a href="Number:toPrecision" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Number:toPrecision">Number.prototype.toPrecision</a>
* </li><li><a href="Number:toFixed" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Number:toFixed">Number.prototype.toFixed</a>
* </li><li><a href="Number:toSource" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Number:toSource">Number.prototype.toSource</a>
* </li><li><a href="Number:valueOf" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Number:valueOf">Number.prototype.valueOf</a>
* </li></ul>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
 * @type String
*/
toString: function( radix ) {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span> Summary </span></h2>
* <p>Returns the primitive value of a Number object.
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <td colspan="2" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Method of <a href="Number" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Number">Number</a></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">Implemented in:</td>
* <td colspan="1" rowspan="1">JavaScript 1.1</td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">ECMA version:</td>
* <td colspan="1" rowspan="1">ECMA-262</td>
* </tr>
* </table>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var primitiveNumber = <i>number</i>.valueOf();
* </pre>
* <h2> <span> Parameters </span></h2>
* <p>None.
* </p>
* <h2> <span> Description </span></h2>
* <p>The <code>valueOf</code> method of <code>Number</code> returns the primitive value of a <code>Number</code> object as a number data type.
* </p><p>This method is usually called internally by JavaScript and not explicitly in code.
* </p>
* <h2> <span> Examples </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var x = new Number();
* print(x.valueOf());     // prints "0"
* </pre>
* <h2> <span> See Also </span></h2>
* <p><a href="Core_JavaScript_1.5_Reference:Global_Objects:Object:toSource" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object:toSource">Object.toSource</a>
* </p>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
 * @type Number
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
* <div id="contentSub">(Redirected from <a href="http://developer.mozilla.org/en/docs/index.php?title=Core_JavaScript_1.5_Reference:Objects:Number&amp;redirect=no" shape="rect" title="Core JavaScript 1.5 Reference:Objects:Number">Core JavaScript 1.5 Reference:Objects:Number</a>)</div>
* 
* <h2> <span> Summary </span></h2>
* <p><b>Core Object</b>
* </p><p>Lets you work with numeric values.  The <code>Number</code> object is an object wrapper for primitive numeric values.
* </p>
* <h2> <span> Created by </span></h2>
* <p>The <code>Number</code> constructor:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">new Number(<i>value</i>)
* </pre>
* <h2> <span> Parameters </span></h2>
* <dl><dt style="font-weight:bold"> <code>value</code>Ê</dt><dd> The numeric value of the object being created.
* </dd></dl>
* <h2> <span> Description </span></h2>
* <p>The primary uses for the <code>Number</code> object are:
* </p>
* <ul><li> To access its constant properties, which represent the largest and smallest representable numbers, positive and negative infinity, and the Not-a-Number value.
* </li></ul>
* <ul><li> To create numeric objects that you can add properties to. Most likely, you will rarely need to create a <code>Number</code> object.
* </li></ul>
* <p>The properties of <code>Number</code> are properties of the class itself, not of individual <code>Number</code> objects.
* </p><p><b>JavaScript 1.2</b>: <code>Number(x)</code> now produces <code>NaN</code> rather than an error if <code>x</code> is a string that does not contain a well-formed numeric literal. For example, the following prints <code>NaN</code>:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* x=Number("three");
* document.write(x + "&lt;BR&gt;");
* </pre>
* <p>You can convert any object to a number using the top-level <a href="Number" shape="rect" title="Core JavaScript 1.5 Reference:Functions:Number">Number</a> function.
* </p>
* <h2> <span> Properties </span></h2>
* <p><a href="Core_JavaScript_1.5_Reference:Global_Objects:Number:constructor" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Number:constructor">constructor</a>: Specifies the function that creates an object's prototype.
* </p><p><a href="Number:MAX_VALUE" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Number:MAX VALUE">MAX_VALUE</a>: The largest representable number.
* </p><p><a href="Number:MIN_VALUE" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Number:MIN VALUE">MIN_VALUE</a>: The smallest representable number.
* </p><p><a href="Number:NaN" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Number:NaN">NaN</a>: Special "not a number" value.
* </p><p><a href="Number:NEGATIVE_INFINITY" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Number:NEGATIVE INFINITY">NEGATIVE_INFINITY</a>: Special value representing negative infinity; returned on overflow.
* </p><p><a href="Number:POSITIVE_INFINITY" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Number:POSITIVE INFINITY">POSITIVE_INFINITY</a>: Special value representing infinity; returned on overflow.
* </p><p><a href="Number:prototype" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Number:prototype">prototype</a>: Allows the addition of properties to a Number object.
* </p>
* <h2> <span> Methods </span></h2>
* <p><a href="Core_JavaScript_1.5_Reference:Global_Objects:Number:toExponential" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Number:toExponential">toExponential</a>: Returns a string representing the number in exponential notation.
* </p><p><a href="Number:toFixed" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Number:toFixed">toFixed</a>: Returns a string representing the number in fixed-point notation.
* </p><p><a href="http://developer.mozilla.org/en/docs/index.php?title=Number:toLocaleString&amp;action=edit" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Number:toLocaleString">toLocaleString</a>:
* Returns a human readable string representing the number using the locale of the environment. Overrides the <a href="Object:toLocaleString" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object:toLocaleString">Object.toLocaleString</a> method.
* </p><p><a href="Number:toPrecision" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Number:toPrecision">toPrecision</a>: Returns a string representing the number to a specified precision in fixed-point or exponential notation.
* </p><p><a href="Number:toSource" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Number:toSource">toSource</a>: Returns an object literal representing the specified Number object; you can use this value to create a new object. Overrides the <a href="Object:toSource" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object:toSource">Object.toSource</a> method.
* </p><p><a href="Number:toString" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Number:toString">toString</a>: Returns a string representing the specified object. Overrides the <a href="Object:toString" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object:toString">Object.toString</a> method.
* </p><p><a href="Number:valueOf" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Number:valueOf">valueOf</a>: Returns the primitive value of the specified object. Overrides the <a href="Object:valueOf" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object:valueOf">Object.valueOf</a> method.
* </p><p>In addition, this object inherits the <a href="Object:watch" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object:watch">watch</a> and <a href="Object:unwatch" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object:unwatch">unwatch</a> methods from <a href="Object" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object">Object</a>.
* </p>
* <h2> <span> Examples </span></h2>
* <h3> <span> Example: Using the <code>Number</code> object to assign values to numeric variables </span></h3>
* <p>The following example uses the <code>Number</code> object's properties to assign values to several numeric variables:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">biggestNum = Number.MAX_VALUE;
* smallestNum = Number.MIN_VALUE;
* infiniteNum = Number.POSITIVE_INFINITY;
* negInfiniteNum = Number.NEGATIVE_INFINITY;
* notANum = Number.NaN;
* </pre>
* <h3> <span> Example: Using <code>Number</code> object to modify all <code>Number</code> objects </span></h3>
* <p>The following example creates a Number object, myNum, then adds a description property to all Number objects. Then a value is assigned to the myNum object's description property.
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">myNum = new Number(65);
* Number.prototype.description = null;
* myNum.description = "wind speed";
* </pre>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
function Number(value) {};
/**
* <div id="contentSub">(Redirected from <a href="http://developer.mozilla.org/en/docs/index.php?title=Core_JavaScript_1.5_Reference:Objects:Number&amp;redirect=no" shape="rect" title="Core JavaScript 1.5 Reference:Objects:Number">Core JavaScript 1.5 Reference:Objects:Number</a>)</div>
* 
* <h2> <span> Summary </span></h2>
* <p><b>Core Object</b>
* </p><p>Lets you work with numeric values.  The <code>Number</code> object is an object wrapper for primitive numeric values.
* </p>
* <h2> <span> Created by </span></h2>
* <p>The <code>Number</code> constructor:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">new Number(<i>value</i>)
* </pre>
* <h2> <span> Parameters </span></h2>
* <dl><dt style="font-weight:bold"> <code>value</code>Ê</dt><dd> The numeric value of the object being created.
* </dd></dl>
* <h2> <span> Description </span></h2>
* <p>The primary uses for the <code>Number</code> object are:
* </p>
* <ul><li> To access its constant properties, which represent the largest and smallest representable numbers, positive and negative infinity, and the Not-a-Number value.
* </li></ul>
* <ul><li> To create numeric objects that you can add properties to. Most likely, you will rarely need to create a <code>Number</code> object.
* </li></ul>
* <p>The properties of <code>Number</code> are properties of the class itself, not of individual <code>Number</code> objects.
* </p><p><b>JavaScript 1.2</b>: <code>Number(x)</code> now produces <code>NaN</code> rather than an error if <code>x</code> is a string that does not contain a well-formed numeric literal. For example, the following prints <code>NaN</code>:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* x=Number("three");
* document.write(x + "&lt;BR&gt;");
* </pre>
* <p>You can convert any object to a number using the top-level <a href="Number" shape="rect" title="Core JavaScript 1.5 Reference:Functions:Number">Number</a> function.
* </p>
* <h2> <span> Properties </span></h2>
* <p><a href="Core_JavaScript_1.5_Reference:Global_Objects:Number:constructor" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Number:constructor">constructor</a>: Specifies the function that creates an object's prototype.
* </p><p><a href="Number:MAX_VALUE" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Number:MAX VALUE">MAX_VALUE</a>: The largest representable number.
* </p><p><a href="Number:MIN_VALUE" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Number:MIN VALUE">MIN_VALUE</a>: The smallest representable number.
* </p><p><a href="Number:NaN" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Number:NaN">NaN</a>: Special "not a number" value.
* </p><p><a href="Number:NEGATIVE_INFINITY" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Number:NEGATIVE INFINITY">NEGATIVE_INFINITY</a>: Special value representing negative infinity; returned on overflow.
* </p><p><a href="Number:POSITIVE_INFINITY" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Number:POSITIVE INFINITY">POSITIVE_INFINITY</a>: Special value representing infinity; returned on overflow.
* </p><p><a href="Number:prototype" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Number:prototype">prototype</a>: Allows the addition of properties to a Number object.
* </p>
* <h2> <span> Methods </span></h2>
* <p><a href="Core_JavaScript_1.5_Reference:Global_Objects:Number:toExponential" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Number:toExponential">toExponential</a>: Returns a string representing the number in exponential notation.
* </p><p><a href="Number:toFixed" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Number:toFixed">toFixed</a>: Returns a string representing the number in fixed-point notation.
* </p><p><a href="http://developer.mozilla.org/en/docs/index.php?title=Number:toLocaleString&amp;action=edit" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Number:toLocaleString">toLocaleString</a>:
* Returns a human readable string representing the number using the locale of the environment. Overrides the <a href="Object:toLocaleString" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object:toLocaleString">Object.toLocaleString</a> method.
* </p><p><a href="Number:toPrecision" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Number:toPrecision">toPrecision</a>: Returns a string representing the number to a specified precision in fixed-point or exponential notation.
* </p><p><a href="Number:toSource" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Number:toSource">toSource</a>: Returns an object literal representing the specified Number object; you can use this value to create a new object. Overrides the <a href="Object:toSource" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object:toSource">Object.toSource</a> method.
* </p><p><a href="Number:toString" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Number:toString">toString</a>: Returns a string representing the specified object. Overrides the <a href="Object:toString" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object:toString">Object.toString</a> method.
* </p><p><a href="Number:valueOf" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Number:valueOf">valueOf</a>: Returns the primitive value of the specified object. Overrides the <a href="Object:valueOf" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object:valueOf">Object.valueOf</a> method.
* </p><p>In addition, this object inherits the <a href="Object:watch" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object:watch">watch</a> and <a href="Object:unwatch" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object:unwatch">unwatch</a> methods from <a href="Object" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object">Object</a>.
* </p>
* <h2> <span> Examples </span></h2>
* <h3> <span> Example: Using the <code>Number</code> object to assign values to numeric variables </span></h3>
* <p>The following example uses the <code>Number</code> object's properties to assign values to several numeric variables:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">biggestNum = Number.MAX_VALUE;
* smallestNum = Number.MIN_VALUE;
* infiniteNum = Number.POSITIVE_INFINITY;
* negInfiniteNum = Number.NEGATIVE_INFINITY;
* notANum = Number.NaN;
* </pre>
* <h3> <span> Example: Using <code>Number</code> object to modify all <code>Number</code> objects </span></h3>
* <p>The following example creates a Number object, myNum, then adds a description property to all Number objects. Then a value is assigned to the myNum object's description property.
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">myNum = new Number(65);
* Number.prototype.description = null;
* myNum.description = "wind speed";
* </pre>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
function Number(val) {};

