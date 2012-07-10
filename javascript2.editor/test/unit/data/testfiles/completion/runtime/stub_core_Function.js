/**
* <div id="contentSub">(Redirected from <a href="http://developer.mozilla.org/en/docs/index.php?title=Core_JavaScript_1.5_Reference:Objects:Function&amp;redirect=no" shape="rect" title="Core JavaScript 1.5 Reference:Objects:Function">Core JavaScript 1.5 Reference:Objects:Function</a>)</div>
* 
* <h2> <span> Summary </span></h2>
* <p><b>Core Object</b>
* </p><p>Every function in JavaScript is actually a <code>Function</code> object.
* </p>
* <h2> <span> Created by </span></h2>
* <p>As all other objects, <code>Function</code> objects can be created using the <code>new</code> statement:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">new Function ([<i>arg1</i>[, <i>arg2</i>[, ... <i>argN</i>]],] <i>functionBody</i>)
* </pre>
* <dl><dt style="font-weight:bold"> <code>arg1, arg2, ... arg<i>N</i></code>Ê</dt><dd>  Names to be used by the function as formal argument names. Each must be a string that corresponds to a valid JavaScript identifier or a list of such strings separated with a comma; for example "<code>x</code>", "<code>theValue</code>", or "<code>a,b</code>".
* </dd></dl>
* <dl><dt style="font-weight:bold"> <code>functionBody</code>Ê</dt><dd> A string containing the JavaScript statements comprising the function definition.
* </dd></dl>
* <p>Invoking the <code>Function</code> constructor as a function (without using the <code>new</code> operator) has the same effect as invoking it as a constructor.
* </p>
* <h2> <span> Description </span></h2>
* <h3> <span> General </span></h3>
* <p><code>Function</code> objects created with the <code>Function</code> constructor are evaluated each time they are used. This is less efficient than declaring a function and calling it within your code, because declared functions are parsed only once.
* </p>
* <h3> <span> Specifying arguments with the <code>Function</code> constructor </span></h3>
* <p>The following code creates a <code>Function</code> object that takes two arguments.
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var multiply = new Function("x", "y", "return x * y");
* </pre>
* <p>The arguments "<code>x</code>" and "<code>y</code>" are formal argument names that are used in the function body, "<code>return x * y</code>".
* </p><p>The preceding code assigns a function to the variable <code>multiply</code>. To call the <code>Function</code> object, you can specify the variable name as if it were a function, as shown in the following examples.
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var theAnswer = multiply(7, 6);
* 
* var myAge = 50;
* if (myAge &gt;= 39)
* myAge = multiply(myAge, .5);
* </pre>
* <h2> <span> Properties </span></h2>
* <p><a href="Function:arguments" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Function:arguments">arguments</a> <span style="border: 1px solid #9898F0; background-color: #DDDDFF; font-size: 9px; vertical-align: text-top;">Deprecated</span>Ê: An array corresponding to the arguments passed to a function. This is deprecated as property of <code>Function</code>, use the <a href="arguments" shape="rect" title="Core JavaScript 1.5 Reference:Functions:arguments">arguments</a> object available within the function instead.
* </p><p><a href="Function:arity" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Function:arity">arity</a> <span style="border: 1px solid #9898F0; background-color: #DDDDFF; font-size: 9px; vertical-align: text-top;">Deprecated</span>Ê: Specifies the number of arguments expected by the function. Use the <a href="Function:length" shape="rect" title="Core JavaScript 1.5 Reference:Objects:Function:length">length</a> property instead.
* </p><p><a href="Function:caller" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Function:caller">caller</a> <span style="border: 1px solid #FFD599; background-color: #FFEFD9; font-size: 9px; vertical-align: text-top;">Non-standard</span>Ê: Specifies the function that invoked the currently executing function.
* </p><p><a href="Function:constructor" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Function:constructor">constructor</a>: Specifies the function that creates an object's prototype.
* </p><p><a href="Function:length" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Function:length">length</a>: Specifies the number of arguments expected by the function.
* </p><p><a href="Function:name" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Function:name">name</a> <span style="border: 1px solid #FFD599; background-color: #FFEFD9; font-size: 9px; vertical-align: text-top;">Non-standard</span>Ê: The name of the function.
* </p><p><a href="Function:prototype" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Function:prototype">prototype</a>: Allows the addition of properties to function objects (both those constructed using <code>Function</code> and those that were declared using a function declaration or a function expression).
* </p>
* <h2> <span> Methods </span></h2>
* <p><a href="Core_JavaScript_1.5_Reference:Global_Objects:Function:apply" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Function:apply">apply</a>: Applies the method of another object in the context of a different object (the calling object); arguments can be passed as an Array object.
* </p><p><a href="Function:call" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Function:call">call</a>: Calls (executes) a method of another object in the context of a different object (the calling object); arguments can be passed as they are.
* </p><p><a href="Function:toSource" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Function:toSource">toSource</a> <span style="border: 1px solid #FFD599; background-color: #FFEFD9; font-size: 9px; vertical-align: text-top;">Non-standard</span>: Returns a string representing the source code of the function.  Overrides the <a href="Object:toSource" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object:toSource">Object.toSource</a> method.
* </p><p><a href="Function:toString" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Function:toString">toString</a>: Returns a string representing the source code of the function.  Overrides the <a href="Object:toString" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object:toString">Object.toString</a> method.
* </p><p><a href="Function:valueOf" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Function:valueOf">valueOf</a>: Returns a string representing the source code of the function.  Overrides the <a href="Object:valueOf" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object:valueOf">Object.valueOf</a> method.
* </p>
* <h2> <span> Examples </span></h2>
* <h3> <span> Example: Creating "focus" and "blur" event handlers for a frame </span></h3>
* <p>The following example creates <code>onFocus</code> and <code>onBlur</code> event handlers for a frame. This code exists in the same file that contains the <code>frameset</code> tag. Note that scripting is the only way to create "focus" and "blur" event handlers for a frame, because you cannot specify the event handlers in the <code>frame</code> tag.
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* var frame = frames[0];
* frame.onfocus = new Function("document.body.style.backgroundColor = 'white';");
* frame.onblur = new Function("document.body.style.backgroundColor = '#bbbbbb';");
* </pre>
* <h2> <span> See also </span></h2>
* <p><a href="Core_JavaScript_1.5_Reference:Functions" shape="rect" title="Core JavaScript 1.5 Reference:Functions">Functions</a>
* </p>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
var Function = {
  // This is just a stub for a builtin native JavaScript object.
/**
* <h2> <span> Summary </span></h2>
* <p>Allows you to apply a method of another object in the context of a different object (the calling object).
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <td colspan="2" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Method of <a href="Function" shape="rect" title="Core JavaScript 1.5 Reference:Objects:Function">Function</a></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">Implemented in:</td>
* <td colspan="1" rowspan="1">JavaScript 1.3</td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">ECMA Version:</td>
* <td colspan="1" rowspan="1">ECMA-262 Edition 3</td>
* </tr>
* </table>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var result = <i>fun</i>.apply(<i>thisArg</i>[, <i>argArray</i>]);
* </pre>
* <h2> <span> Parameters </span></h2>
* <dl><dt style="font-weight:bold"> <code>thisArg</code>Ê</dt><dd> Determines the value of <code>this</code> inside <i><code>fun</code></i>.  If <code>thisArg</code> is <code>null</code> or <a href="undefined" shape="rect" title="Core JavaScript 1.5 Reference:Global Properties:undefined">undefined</a>, <code>this</code> will be the global object.  Otherwise, <code>this</code> will be equal to <code>Object(thisArg)</code> (which is <code>thisArg</code> if <code>thisArg</code> is already an object, or a <code>String</code>, <code>Boolean</code>, or <code>Number</code> if <code>thisArg</code> is a primitive value of the corresponding type).  Therefore, it is always true that <code>typeof this == "object"</code> when the function executes.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <code>argArray</code>Ê</dt><dd> An argument array for the object, specifying the arguments with which <i><code>fun</code></i> should be called, or <code>null</code> or <a href="undefined" shape="rect" title="Core JavaScript 1.5 Reference:Global Properties:undefined">undefined</a> if no arguments should be provided to the function.
* </dd></dl>
* <h2> <span> Description </span></h2>
* <p>You can assign a different <code>this</code> object when calling an existing function. <code>this</code> refers to the current object, the calling object. With <code>apply</code>, you can write a method once and then inherit it in another object, without having to rewrite the method for the new object.
* </p><p><code>apply</code> is very similar to <code><a href="Core_JavaScript_1.5_Reference:Objects:Function:call" shape="rect" title="Core JavaScript 1.5 Reference:Objects:Function:call">call</a></code>, except for the type of arguments it supports. You can use an arguments array instead of a named set of parameters. With <code>apply</code>, you can use an array literal, for example, <code><i>fun</i>.apply(this, [name, value])</code>, or an <code>Array</code> object, for example, <code><i>fun</i>.apply(this, new Array(name, value))</code>.
* </p><p>You can also use <code><a href="Function:arguments" shape="rect" title="Core JavaScript 1.5 Reference:Objects:Function:arguments">arguments</a></code> for the <code>argArray</code> parameter. <code>arguments</code> is a local variable of a function. It can be used for all unspecified arguments of the called object. Thus, you do not have to know the arguments of the called object when you use the <code>apply</code> method. You can use <code>arguments</code> to pass all the arguments to the called object. The called object is then responsible for handling the arguments.
* </p>
* <h2> <span> Examples </span></h2>
* <h3> <span> Example: Using <code>apply</code> to chain constructors </span></h3>
* <p>You can use <code>apply</code> to chain constructors for an object, similar to Java. In the following example, the constructor for the <code>product</code> object is defined with two parameters, <code>name</code> and <code>value</code>. Another object, <code>prod_dept</code>, initializes its unique variable (<code>dept</code>) and calls the constructor for <code>product</code> in its constructor to initialize the other variables. In this example, the parameter <code>arguments</code> is used for all arguments of the product object's constructor.
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">function product(name, value)
* {
* this.name = name;
* if (value &gt; 1000)
* this.value = 999;
* else
* this.value = value;
* }
* 
* function prod_dept(name, value, dept)
* {
* this.dept = dept;
* product.apply(this, arguments);
* }
* prod_dept.prototype = new product();
* 
* // since 5 is less than 1000 value is set
* var cheese = new prod_dept("feta", 5, "food");
* 
* // since 5000 is above 1000, value will be 999
* var car = new prod_dept("honda", 5000, "auto");
* </pre>
* <h2> <span> See Also </span></h2>
* <p><a href="Core_JavaScript_1.5_Reference:Objects:Function:call" shape="rect" title="Core JavaScript 1.5 Reference:Objects:Function:call">call</a>
* </p>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
apply: function(thisArg, argArray) {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <div style="border: 1px solid #5151FF; background-color: #B9B9FF; font-weight: bold; text-align: center; padding: 0px 10px 0px 10px; margin: 10px 0px 10px 0px;"><p style="margin: 4px 0px 4px 0px;">Deprecated</p></div>
* 
* <h2> <span> Summary </span></h2>
* <p>An array-like object corresponding to the arguments passed to a function.
* </p>
* <h2> <span> Description </span></h2>
* <p>Use the <code><a href="Core_JavaScript_1.5_Reference:Functions:arguments" shape="rect" title="Core JavaScript 1.5 Reference:Functions:arguments">arguments</a></code> object available within functions instead of <code>Function.arguments</code>.
* </p>
* <h2> <span> Notes </span></h2>
* <p>In the case of recursion, i.e. if function <code>f</code> appears several times on the call stack, the value of <code>f.arguments</code> represents the arguments corresponding to the most recent invocation of the function.
* </p>
* <h2> <span> Example </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">function f(n) { g(n-1) }
* function g(n) {
* print("before: " + g.arguments[0]);
* if(n&gt;0)
* f(n);
* print("after: " + g.arguments[0]);
* }
* f(2)
* </pre>
* <p>outputs:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">before: 1
* before: 0
* after: 0
* after: 1
* </pre>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
arguments: undefined,
/**
* <div style="border: 1px solid #5151FF; background-color: #B9B9FF; font-weight: bold; text-align: center; padding: 0px 10px 0px 10px; margin: 10px 0px 10px 0px;"><p style="margin: 4px 0px 4px 0px;">Deprecated</p></div>
* 
* <h2> <span> Summary </span></h2>
* <p>Specifies the number of arguments expected by the function.
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <td colspan="2" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Property of <a href="Function" shape="rect" title="Core JavaScript 1.5 Reference:Objects:Function">Function</a></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">Implemented in:</td>
* <td colspan="1" rowspan="1">JavaScript 1.2, NES 3.0
* <p>JavaScript 1.4: Deprecated.
* </p>
* </td>
* </tr>
* </table>
* <h2> <span> Description </span></h2>
* <p><code>arity</code> is no longer used and has been replaced by the <code><a href="Core_JavaScript_1.5_Reference:Objects:Function:length" shape="rect" title="Core JavaScript 1.5 Reference:Objects:Function:length">length</a></code> property.
* </p><p><code>arity</code> is external to the function, and indicates how many arguments a function expects. By contrast, <code><a href="Function:arguments:length" shape="rect" title="Core JavaScript 1.5 Reference:Objects:Function:arguments:length">arguments.length</a></code> provides the number of arguments actually passed to a function.
* </p>
* <h2> <span> Examples </span></h2>
* <h3> <span> Example: Using <code>arity</code> </span></h3>
* <p>See example in <a href="Function:length#Example:_Using_Function.length_and_arguments.length" shape="rect" title="Core JavaScript 1.5 Reference:Objects:Function:length">Function.length</a>
* </p>
* <h2> <span> See Also </span></h2>
* <p><a href="Core_JavaScript_1.5_Reference:Objects:Function:arguments:length" shape="rect" title="Core JavaScript 1.5 Reference:Objects:Function:arguments:length">arguments.length</a>,
* <a href="Function:length" shape="rect" title="Core JavaScript 1.5 Reference:Objects:Function:length">Function.length</a>
* </p>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
arity: undefined,
/**
* <h2> <span> Summary </span></h2>
* <p>Allows you to call (execute) a method of another object in the context of a different object (the calling object).
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <td colspan="2" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Method of <a href="Function" shape="rect" title="Core JavaScript 1.5 Reference:Objects:Function">Function</a></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">Implemented in:</td>
* <td colspan="1" rowspan="1">JavaScript 1.3</td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">ECMA Version:</td>
* <td colspan="1" rowspan="1">ECMA-262 Edition 3</td>
* </tr>
* </table>
* <h2> <span> Syntax </span></h2>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var result = <i>fun</i>.call(<i>thisArg</i>[, <i>arg1</i>[, <i>arg2</i>[, ...]]]);
* </pre>
* <h2> <span> Parameters </span></h2>
* <dl><dt style="font-weight:bold"> <code>thisArg</code>Ê</dt><dd> Determines the value of <code>this</code> inside <i><code>fun</code></i>.  If <code>thisArg</code> is <code>null</code> or <a href="undefined" shape="rect" title="Core JavaScript 1.5 Reference:Global Properties:undefined">undefined</a>, <code>this</code> will be the global object.  Otherwise, <code>this</code> will be equal to <code>Object(thisArg)</code> (which is <code>thisArg</code> if <code>thisArg</code> is already an object, or a <code>String</code>, <code>Boolean</code>, or <code>Number</code> if <code>thisArg</code> is a primitive value of the corresponding type).  Therefore, it is always true that <code>typeof this == "object"</code> when the function executes.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <code>arg1, arg2, ...</code>Ê</dt><dd> Arguments for the object.
* </dd></dl>
* <h2> <span> Description </span></h2>
* <p>You can assign a different <code>this</code> object when calling an existing function. <code>this</code> refers to the current object, the calling object.
* </p><p>With <code>call</code>, you can write a method once and then inherit it in another object, without having to rewrite the method for the new object.
* </p>
* <h2> <span> Examples </span></h2>
* <h3> <span> Example: Using <code>call</code> to chain constructors for an object </span></h3>
* <p>You can use <code>call</code> to chain constructors for an object, similar to Java. In the following example, the constructor for the product object is defined with two parameters, <code>name</code> and <code>value</code>. Another object, <code>prod_dept</code>, initializes its unique variable (<code>dept</code>) and calls the constructor for <code>product</code> in its constructor to initialize the other variables.
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* function product(name, value){
* this.name = name;
* if(value &gt;= 1000)
* this.value = 999;
* else
* this.value = value;
* }
* 
* function prod_dept(name, value, dept){
* this.dept = dept;
* product.call(this, name, value);
* }
* 
* prod_dept.prototype = new product();
* 
* // since 5 is less than 1000, value is set
* cheese = new prod_dept("feta", 5, "food");
* 
* // since 5000 is above 1000, value will be 999
* car = new prod_dept("honda", 5000, "auto");
* </pre>
* <h2> <span> See Also </span></h2>
* <p><a href="Function:apply" shape="rect" title="Core JavaScript 1.5 Reference:Objects:Function:apply">apply</a>
* </p>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
call: function(thisArg, arg1, arg2) {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <div style="border: 1px solid #FFB752; background-color: #FEE3BC; font-weight: bold; text-align: center; padding: 0px 10px 0px 10px; margin: 10px 0px 10px 0px;"><p style="margin: 4px 0px 4px 0px;">Non-standard</p></div>
* 
* <h2> <span> Summary </span></h2>
* <p>Returns the function that invoked the specified function.
* </p><p>This property is not part of ECMA-262 Edition 3 standard. It is implemented at least in <a href="http://developer.mozilla.org/en/docs/SpiderMonkey" shape="rect" title="SpiderMonkey">SpiderMonkey</a> (the JavaScript engine used in Mozilla) (see <a href="https://bugzilla.mozilla.org/show_bug.cgi?id=65683" rel="nofollow" shape="rect" title="https://bugzilla.mozilla.org/show_bug.cgi?id=65683">bug 65683</a>) and JScript.
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <td colspan="2" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Property of <a href="Function" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Function">Function</a></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">Implemented in:</td>
* <td colspan="1" rowspan="1">JavaScript 1.5</td>
* </tr>
* </table>
* <h2> <span> Description </span></h2>
* <p>If the function <code>f</code> was invoked by the top level code, the value of <code>f.caller</code> is <code>null</code>, otherwise it's the function that called <code>f</code>
* </p><p>This property replaces deprecated <a href="Core_JavaScript_1.5_Reference:Functions:arguments:caller" shape="rect" title="Core JavaScript 1.5 Reference:Functions:arguments:caller">arguments.caller</a>.
* </p>
* <h2> <span> Notes </span></h2>
* <p>Note that in case of recursion, you can't reconstruct the call stack using this property. Consider:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">function f(n) { g(n-1) }
* function g(n) { if(n&gt;0) f(n); else stop() }
* f(2)
* </pre>
* <p>At the moment <code>stop()</code> is called the call stack will be:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">f(2) -&gt; g(1) -&gt; f(1) -&gt; g(0) -&gt; stop()
* </pre>
* <p>The following is true:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">stop.caller === g &amp;&amp; f.caller === g &amp;&amp; g.caller === f
* </pre>
* <p>so if you tried to get the stack trace in the <code>stop()</code> function like this:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var f = stop;
* var stack = "Stack trace:";
* while (f) {
* stack += "\n" + f.name;
* f = f.caller;
* }
* </pre>
* <p>the loop would never stop.
* </p><p>The special property <code>__caller__</code>, which returned the activation object of the caller thus allowing to reconstruct the stack, was removed for security reasons.
* </p>
* <h2> <span> Examples </span></h2>
* <h3> <span> Example: Checking the value of a function's <code>caller</code> property </span></h3>
* <p>The following code checks the value a function's <code>caller</code> property.
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">function myFunc() {
* if (myFunc.caller == null) {
* return ("The function was called from the top!");
* } else
* return ("This function's caller was " + myFunc.caller);
* }
* </pre>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
caller: undefined,
/**
* <h2> <span> Summary </span></h2>
* <p>Returns a reference to the <a href="Function" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Function">Function</a> function that created the instance's prototype. Note that the value of this property is a reference to the function itself, not a string containing the function's name.
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <td colspan="2" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Property of <a href="Function" shape="rect" title="Core JavaScript 1.5 Reference:Objects:Function">Function</a></td>
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
* <p>See <a href="Core_JavaScript_1.5_Reference:Objects:Object:constructor" shape="rect" title="Core JavaScript 1.5 Reference:Objects:Object:constructor">Object.constructor</a>
* </p>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
constructor: undefined,
/**
* <h2> <span> Summary </span></h2>
* <p>Specifies the number of arguments expected by the function.
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <td colspan="2" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Property of <a href="Function" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Function">Function</a></td>
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
* <h2> <span> Description </span></h2>
* <p><code>length</code> is external to a function, and indicates how many arguments the function expects, i.e. the number of formal parameters. By contrast, <code><a href="Core_JavaScript_1.5_Reference:Functions:arguments:length" shape="rect" title="Core JavaScript 1.5 Reference:Functions:arguments:length">arguments.length</a></code> is local to a function and provides the number of arguments actually passed to the function.
* </p>
* <h2> <span> Examples </span></h2>
* <h3> <span> Example: Using <code>Function.length</code> and <code>arguments.length</code> </span></h3>
* <p>The following example demonstrates the use of <code>Function.length</code> and <code>arguments.length</code>.
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">function addNumbers(x, y){
* if (arguments.length == addNumbers.length) {
* return (x + y);
* }
* else
* return 0;
* }
* </pre>
* <p>If you pass more than two arguments to this function, the function returns 0:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">addNumbers(3,4,5)   // returns 0
* addNumbers(3,4)     // returns 7
* addNumbers(103,104) // returns 207
* </pre>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
 * @type Number
*/
length: undefined,
/**
* <div style="border: 1px solid #FFB752; background-color: #FEE3BC; font-weight: bold; text-align: center; padding: 0px 10px 0px 10px; margin: 10px 0px 10px 0px;"><p style="margin: 4px 0px 4px 0px;">Non-standard</p></div>
* <h2> <span> Summary </span></h2>
* <p>The name of the function.
* </p>
* <h2> <span> Description </span></h2>
* <p>The name property returns the name of a function, or an empty string for anonymous functions:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">function doSomething() {}
* alert(doSomething.name); // alerts "doSomething"
* </pre>
* <p>Note that in these examples anonymous functions are created, so <code>name</code> returns an empty string:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var f = function() { };
* var object = {
* someMethod: function() {}
* };
* 
* alert(f.name == ""); // true
* alert(object.someMethod.name == ""); // also true
* </pre>
* <p>You can define a function with a name in a <a href="Core_JavaScript_1.5_Reference:Functions#Function_constructor_vs._function_declaration_vs._function_expression" shape="rect" title="Core JavaScript 1.5 Reference:Functions">function expression</a>:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var object = {
* someMethod: function object_someMethod() {}
* };
* alert(object.someMethod.name); // alerts "object_someMethod"
* 
* try { object_someMethod } catch(e) { alert(e); }
* // ReferenceError: object_someMethod is not defined
* </pre>
* <p>You cannot change the <code>name</code> of a function, this property is read-only:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var object = {
* // anonymous
* someMethod: function(){}
* };
* object.someMethod.name = "someMethod";
* alert(object.someMethod.name); // empty string, someMethod is anonymous
* </pre>
* <h2> <span> Examples </span></h2>
* <p>You can use <code>obj.constructor.name</code> to check the "class" of an object:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">function a()
* {
* }
* 
* var b = new a();
* alert(b.constructor.name); //Alerts "a"
* </pre>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:NeedsContent" shape="rect" title="Category:NeedsContent">NeedsContent</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
name: undefined,
/**
* <h2> <span> Summary </span></h2>
* <p>A value from which instances of a particular class are created. Every object that can be created by calling a constructor function has an associated prototype property.
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <td colspan="2" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Property of <a href="Function" shape="rect" title="Core JavaScript 1.5 Reference:Objects:Function">Function</a></td>
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
* <p>You can add new properties or methods to an existing class by adding them to the prototype associated with the constructor function for that class. The syntax for adding a new property or method is:
* </p><p><code>
* <i>fun</i>.prototype.<i>name</i> = <i>value</i>
* </code>
* </p><p>where
* </p>
* <dl><dt style="font-weight:bold"> <code>fun</code>Ê</dt><dd> The name of the constructor function object you want to change.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <code>name</code>Ê</dt><dd> The name of the property or method to be created.
* </dd></dl>
* <dl><dt style="font-weight:bold"> <code>value</code>Ê</dt><dd> The value initially assigned to the new property or method.
* </dd></dl>
* <p>If you add a property to the prototype for an object, then all objects created with that object's constructor function will have that new property, even if the objects existed before you created the new property. For example, assume you have the following statements:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* var array1 = new Array();
* var array2 = new Array(3);
* Array.prototype.description=null;
* array1.description="Contains some stuff"
* array2.description="Contains other stuff"
* </pre>
* <p>After you set a property for the prototype, all subsequent objects created with <code>Array</code> will have the property:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* anotherArray=new Array()
* anotherArray.description="Currently empty"
* </pre>
* <p>Note that <code>prototype</code> is itself an object, and can be assigned properties and methods via the object literal syntax:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">function MyFunction() {
* alert("Created.");
* }
* 
* MyFunction.prototype = {
* alert1: function(str) {
* alert(str);
* },
* 
* five: 5,
* 
* alert2: function() {
* alert("Hi.");
* }
* };
* var myObject = new MyFunction();
* myObject.alert1("There.");
* myObject.five;
* myObject.alert2();
* </pre>
* <h2> <span> Example </span></h2>
* <p>The following example creates a method, <code>str_rep</code>, and uses the statement <code>String.prototype.rep = str_rep</code> to add the method to all <code>String</code> objects. All objects created with <code>new String()</code> then have that method, even objects already created. The example then creates an alternate method and adds that to one of the <code><a href="String" shape="rect" title="Core JavaScript 1.5 Reference:Objects:String">String</a></code> objects using the statement <code>s1.rep = fake_rep</code>. The <code>str_rep</code> method of the remaining <code>String</code> objects is not altered.
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* var s1 = new String("a")
* var s2 = new String("b")
* var s3 = new String("c")
* 
* // Create a repeat-string-N-times method for all String objects
* function str_rep(n) {
* var s = "", t = this.toString()
* while (--n &gt;= 0) s += t
* return s
* }
* 
* String.prototype.rep = str_rep
* 
* s1a=s1.rep(3) // returns "aaa"
* s2a=s2.rep(5) // returns "bbbbb"
* s3a=s3.rep(2) // returns "cc"
* 
* // Create an alternate method and assign it to only one String variable
* function fake_rep(n) {
* return "repeat " + this + " " + n + " times."
* }
* 
* s1.rep = fake_rep
* s1b=s1.rep(1) // returns "repeat a 1 times."
* s2b=s2.rep(4) // returns "bbbb"
* s3b=s3.rep(6) // returns "cccccc"
* </pre>
* <p>The function in this example also works on <code>String</code> objects not created with the <code>String</code> constructor. The following code returns "zzz".
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* "z".rep(3)
* </pre>
* 
* <div id="catlinks"><p><a href="http://developer.mozilla.org/en/docs/Special:Categories" shape="rect" title="Special:Categories">Category</a>: <span dir="ltr"><a href="http://developer.mozilla.org/en/docs/Category:NeedsContent" shape="rect" title="Category:NeedsContent">NeedsContent</a></span></p></div>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
prototype: undefined,
/**
* <div style="border: 1px solid #FFB752; background-color: #FEE3BC; font-weight: bold; text-align: center; padding: 0px 10px 0px 10px; margin: 10px 0px 10px 0px;"><p style="margin: 4px 0px 4px 0px;">Non-standard</p></div>
* 
* <h2> <span> Summary </span></h2>
* <p>Returns a string representing the source code for the function.
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <td colspan="2" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Method of <a href="Function" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Function">Function</a></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">Implemented in:</td>
* <td colspan="1" rowspan="1">JavaScript 1.3</td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">ECMA Version:</td>
* <td colspan="1" rowspan="1">ECMA-262 Edition 3</td>
* </tr>
* </table>
* <h2> <span> Syntax </span></h2>
* <p><code><i>function</i>.toSource()</code>
* </p>
* <h2> <span> Parameters </span></h2>
* <p>None.
* </p>
* <h2> <span> Description </span></h2>
* <p>The <code>toSource</code> method returns the following values:
* </p>
* <ul><li> For the built-in <code>Function</code> object, <code>toSource</code> returns the following string indicating that the source code is not available:
* </li></ul>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">function Function() {
* [native code]
* }
* </pre>
* <ul><li> For custom functions, <code>toSource</code> returns the JavaScript source that defines the object as a string.
* </li></ul>
* <p>This method is usually called internally by JavaScript and not explicitly in code. You can call <code>toSource</code> while debugging to examine the contents of an object.
* </p>
* <h2> <span> See Also </span></h2>
* <ul><li><a href="Function:toString" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Function:toString">toString</a>
* </li><li><a href="Object:valueOf" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object:valueOf">Object.prototype.valueOf</a>
* </li></ul>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
toSource: function() {
  // This is just a stub for a builtin native JavaScript object.
},
/**
* <h2> <span> Summary </span></h2>
* <p>Returns a string representing the source code of the function.
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <td colspan="2" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Method of <a href="Function" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Function">Function</a></td>
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
* <p><code><i>object</i>.toString()</code>
* </p>
* <h2> <span> Parameters </span></h2>
* <p>None.
* </p>
* <h2> <span> Description </span></h2>
* <p>The <a href="Core_JavaScript_1.5_Reference:Global_Objects:Function" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Function">Function</a> object overrides the <a href="Object:toString" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object:toString">toString</a> method of the <a href="Object" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object">Object</a> object; it does not inherit <code>Object.toString</code>. For <code>Function</code> objects, the <code>toString</code> method returns a string representation of the object.
* </p><p>JavaScript calls the <code>toString</code> method automatically when a <code>Function</code> is to be represented as a text value or when a <code>Function</code> is referred to in a string concatenation.
* </p><p>For <code>Function</code> objects, the built-in <code>toString</code> method decompiles the function back into the JavaScript source that defines the function. This string includes the <code>function</code> keyword, the argument list, curly braces, and function body.
* </p><p>For example, assume you have the following code that defines the <code>Dog</code> object type and creates <code>theDog</code>, an object of type <code>Dog</code>:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">function Dog(name,breed,color,sex) {
* this.name=name;
* this.breed=breed;
* this.color=color;
* this.sex=sex;
* }
* 
* theDog = new Dog("Gabby","Lab","chocolate","girl");
* </pre>
* <p>Any time <code>Dog</code> is used in a string context, JavaScript automatically calls the <code>toString</code> function, which returns the following string:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">function Dog(name, breed, color, sex) {
* this.name = name;
* this.breed = breed;
* this.color = color;
* this.sex = sex;
* }
* </pre>
* <h2> <span> See Also </span></h2>
* <p><a href="Core_JavaScript_1.5_Reference:Objects:Object:toString" shape="rect" title="Core JavaScript 1.5 Reference:Objects:Object:toString">Object.prototype.toString</a>
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
* <p>Retrns a string representing the source code of the function.
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <td colspan="2" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Method of <a href="Function" shape="rect" title="Core JavaScript 1.5 Reference:Objects:Function">Function</a></td>
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
* <p>The <code>valueOf</code> method returns the following values:
* </p>
* <ul><li> For the built-in <code>Function</code> object, <code>valueOf</code> returns the following string indicating that the source code is not available:
* </li></ul>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* function Function() {
* [native code]
* }
* </pre>
* <ul><li> For custom functions, <code>toSource</code> returns the JavaScript source that defines the object as a string. The method is equivalent to the <code>toString</code> method of the function.
* </li></ul>
* <p>This method is usually called internally by JavaScript and not explicitly in code.
* </p>
* <h2> <span> See Also </span></h2>
* <p><a href="Function:toString" shape="rect" title="Core JavaScript 1.5 Reference:Objects:Function:toString">toString</a>,
* <a href="Object:valueOf" shape="rect" title="Core JavaScript 1.5 Reference:Objects:Object:valueOf">Object.valueOf</a>
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
* <div id="contentSub">(Redirected from <a href="http://developer.mozilla.org/en/docs/index.php?title=Core_JavaScript_1.5_Reference:Objects:Function&amp;redirect=no" shape="rect" title="Core JavaScript 1.5 Reference:Objects:Function">Core JavaScript 1.5 Reference:Objects:Function</a>)</div>
* 
* <h2> <span> Summary </span></h2>
* <p><b>Core Object</b>
* </p><p>Every function in JavaScript is actually a <code>Function</code> object.
* </p>
* <h2> <span> Created by </span></h2>
* <p>As all other objects, <code>Function</code> objects can be created using the <code>new</code> statement:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">new Function ([<i>arg1</i>[, <i>arg2</i>[, ... <i>argN</i>]],] <i>functionBody</i>)
* </pre>
* <dl><dt style="font-weight:bold"> <code>arg1, arg2, ... arg<i>N</i></code>Ê</dt><dd>  Names to be used by the function as formal argument names. Each must be a string that corresponds to a valid JavaScript identifier or a list of such strings separated with a comma; for example "<code>x</code>", "<code>theValue</code>", or "<code>a,b</code>".
* </dd></dl>
* <dl><dt style="font-weight:bold"> <code>functionBody</code>Ê</dt><dd> A string containing the JavaScript statements comprising the function definition.
* </dd></dl>
* <p>Invoking the <code>Function</code> constructor as a function (without using the <code>new</code> operator) has the same effect as invoking it as a constructor.
* </p>
* <h2> <span> Description </span></h2>
* <h3> <span> General </span></h3>
* <p><code>Function</code> objects created with the <code>Function</code> constructor are evaluated each time they are used. This is less efficient than declaring a function and calling it within your code, because declared functions are parsed only once.
* </p>
* <h3> <span> Specifying arguments with the <code>Function</code> constructor </span></h3>
* <p>The following code creates a <code>Function</code> object that takes two arguments.
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var multiply = new Function("x", "y", "return x * y");
* </pre>
* <p>The arguments "<code>x</code>" and "<code>y</code>" are formal argument names that are used in the function body, "<code>return x * y</code>".
* </p><p>The preceding code assigns a function to the variable <code>multiply</code>. To call the <code>Function</code> object, you can specify the variable name as if it were a function, as shown in the following examples.
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">var theAnswer = multiply(7, 6);
* 
* var myAge = 50;
* if (myAge &gt;= 39)
* myAge = multiply(myAge, .5);
* </pre>
* <h2> <span> Properties </span></h2>
* <p><a href="Function:arguments" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Function:arguments">arguments</a> <span style="border: 1px solid #9898F0; background-color: #DDDDFF; font-size: 9px; vertical-align: text-top;">Deprecated</span>Ê: An array corresponding to the arguments passed to a function. This is deprecated as property of <code>Function</code>, use the <a href="arguments" shape="rect" title="Core JavaScript 1.5 Reference:Functions:arguments">arguments</a> object available within the function instead.
* </p><p><a href="Function:arity" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Function:arity">arity</a> <span style="border: 1px solid #9898F0; background-color: #DDDDFF; font-size: 9px; vertical-align: text-top;">Deprecated</span>Ê: Specifies the number of arguments expected by the function. Use the <a href="Function:length" shape="rect" title="Core JavaScript 1.5 Reference:Objects:Function:length">length</a> property instead.
* </p><p><a href="Function:caller" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Function:caller">caller</a> <span style="border: 1px solid #FFD599; background-color: #FFEFD9; font-size: 9px; vertical-align: text-top;">Non-standard</span>Ê: Specifies the function that invoked the currently executing function.
* </p><p><a href="Function:constructor" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Function:constructor">constructor</a>: Specifies the function that creates an object's prototype.
* </p><p><a href="Function:length" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Function:length">length</a>: Specifies the number of arguments expected by the function.
* </p><p><a href="Function:name" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Function:name">name</a> <span style="border: 1px solid #FFD599; background-color: #FFEFD9; font-size: 9px; vertical-align: text-top;">Non-standard</span>Ê: The name of the function.
* </p><p><a href="Function:prototype" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Function:prototype">prototype</a>: Allows the addition of properties to function objects (both those constructed using <code>Function</code> and those that were declared using a function declaration or a function expression).
* </p>
* <h2> <span> Methods </span></h2>
* <p><a href="Core_JavaScript_1.5_Reference:Global_Objects:Function:apply" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Function:apply">apply</a>: Applies the method of another object in the context of a different object (the calling object); arguments can be passed as an Array object.
* </p><p><a href="Function:call" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Function:call">call</a>: Calls (executes) a method of another object in the context of a different object (the calling object); arguments can be passed as they are.
* </p><p><a href="Function:toSource" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Function:toSource">toSource</a> <span style="border: 1px solid #FFD599; background-color: #FFEFD9; font-size: 9px; vertical-align: text-top;">Non-standard</span>: Returns a string representing the source code of the function.  Overrides the <a href="Object:toSource" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object:toSource">Object.toSource</a> method.
* </p><p><a href="Function:toString" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Function:toString">toString</a>: Returns a string representing the source code of the function.  Overrides the <a href="Object:toString" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object:toString">Object.toString</a> method.
* </p><p><a href="Function:valueOf" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Function:valueOf">valueOf</a>: Returns a string representing the source code of the function.  Overrides the <a href="Object:valueOf" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Object:valueOf">Object.valueOf</a> method.
* </p>
* <h2> <span> Examples </span></h2>
* <h3> <span> Example: Creating "focus" and "blur" event handlers for a frame </span></h3>
* <p>The following example creates <code>onFocus</code> and <code>onBlur</code> event handlers for a frame. This code exists in the same file that contains the <code>frameset</code> tag. Note that scripting is the only way to create "focus" and "blur" event handlers for a frame, because you cannot specify the event handlers in the <code>frame</code> tag.
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* var frame = frames[0];
* frame.onfocus = new Function("document.body.style.backgroundColor = 'white';");
* frame.onblur = new Function("document.body.style.backgroundColor = '#bbbbbb';");
* </pre>
* <h2> <span> See also </span></h2>
* <p><a href="Core_JavaScript_1.5_Reference:Functions" shape="rect" title="Core JavaScript 1.5 Reference:Functions">Functions</a>
* </p>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
function Function(arg1, arg2, argN, functionBody) {};

