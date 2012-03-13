/**
* <h2> <span> Summary </span></h2>
* <p>An array-like object corresponding to the arguments passed to a function.
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <th colspan="2" rowspan="1">
* Local variable within all functions and deprecated property of <a href="Function" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Function">Function</a></th>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">Implemented in:</td>
* <td colspan="1" rowspan="1">JavaScript 1.1, NES 2.0
* <p>JavaScript 1.2: added <code><a href="arguments:callee" shape="rect" title="Core JavaScript 1.5 Reference:Functions:arguments:callee">arguments.callee</a></code> property.
* </p><p>JavaScript 1.3: deprecated <code><a href="arguments:caller" shape="rect" title="Core JavaScript 1.5 Reference:Functions:arguments:caller">arguments.caller</a></code> property; removed support for argument names and local variable names as properties of the <code>arguments</code> object.
* </p><p>JavaScript 1.4: deprecated <code>arguments</code>, <code><a href="arguments:callee" shape="rect" title="Core JavaScript 1.5 Reference:Functions:arguments:callee">arguments.callee</a></code>, and <code><a href="arguments:length" shape="rect" title="Core JavaScript 1.5 Reference:Functions:arguments:length">arguments.length</a></code> as properties of <code><a href="Function" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Function">Function</a></code>; retained <code>arguments</code> as a local variable of a function and <code><a href="arguments:callee" shape="rect" title="Core JavaScript 1.5 Reference:Functions:arguments:callee">arguments.callee</a></code> and <code><a href="arguments:length" shape="rect" title="Core JavaScript 1.5 Reference:Functions:arguments:length">arguments.length</a></code> as properties of this variable.
* </p>
* </td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">ECMA Version:</td>
* <td colspan="1" rowspan="1">ECMA-262</td>
* </tr>
* </table>
* <h2> <span> Description </span></h2>
* <p>The <code>arguments</code> object is a local variable available within all functions; <code>arguments</code> as a property of <code>Function</code> can no longer be used.
* </p><p>You can refer to a function's arguments within the function by using the <code>arguments</code> object. This object contains an entry for each argument passed to the function, the first entry's index starting at 0. For example, if a function is passed three arguments, you can refer to the argument as follows:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">arguments[0]
* arguments[1]
* arguments[2]
* </pre>
* <p>The arguments can also be set:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">arguments[1] = 'new value';
* </pre>
* <div><b>Note:</b> The <a href="http://developer.mozilla.org/en/docs/SpiderMonkey" shape="rect" title="SpiderMonkey">SpiderMonkey</a> JavaScript engine has a <a href="https://bugzilla.mozilla.org/show_bug.cgi?id=292215" rel="nofollow" shape="rect" title="https://bugzilla.mozilla.org/show_bug.cgi?id=292215">bug</a> in which <code>arguments[n]</code> cannot be set if <code>n</code> is greater than the number of formal or actual parameters. This has been fixed in the engine for JavaScript 1.6.</div>
* <p>The <code>arguments</code> object is not an array. It is similar to an array, but does not have any array properties except <code><a href="arguments:length" shape="rect" title="Core JavaScript 1.5 Reference:Functions:arguments:length">length</a></code>. For example, it does not have the <code><a href="Array:pop" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Array:pop">pop</a></code> method. However it can be converted to an real array:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve"> var args = Array.prototype.slice.call(arguments);
* </pre>
* <p>The <code>arguments</code> object is available only within a function body. Attempting to access the <code>arguments</code> object outside a function declaration results in an error.
* </p><p>You can use the <code>arguments</code> object if you call a function with more arguments than it is formally declared to accept. This technique is useful for functions that can be passed a variable number of arguments. You can use <code><a href="arguments:length" shape="rect" title="Core JavaScript 1.5 Reference:Functions:arguments:length">arguments.length</a></code> to determine the number of arguments passed to the function, and then process each argument by using the <code>arguments</code> object. (To determine the number of arguments declared when a function was defined, use the <code><a href="Function:length" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Function:length">Function.length</a></code> property.)
* </p>
* <h2> <span> Properties </span></h2>
* <p><a href="Core_JavaScript_1.5_Reference:Functions:arguments:callee" shape="rect" title="Core JavaScript 1.5 Reference:Functions:arguments:callee">arguments.callee</a>: Specifies the function body of the currently executing function.
* </p><p><a href="arguments:caller" shape="rect" title="Core JavaScript 1.5 Reference:Functions:arguments:caller">arguments.caller</a> <span style="border: 1px solid #9898F0; background-color: #DDDDFF; font-size: 9px; vertical-align: text-top;">Deprecated</span>Ê: Specifies the name of the function that invoked the currently executing function.
* </p><p><a href="arguments:length" shape="rect" title="Core JavaScript 1.5 Reference:Functions:arguments:length">arguments.length</a>: Specifies the number of arguments passed to the function.
* </p>
* <h2> <span> Backward compatibility </span></h2>
* <h3> <span> JavaScript 1.3 and earlier versions </span></h3>
* <p>In addition to being available as a local variable, the <code>arguments</code> object is also a property of the <code>Function</code> object and can be preceded by the function name. For example, if a function <code>myFunc</code> is passed three arguments named <code>arg1</code>, <code>arg2</code>, and <code>arg3</code>, you can refer to the arguments as follows:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">myFunc.arguments[0]
* myFunc.arguments[1]
* myFunc.arguments[2]
* </pre>
* <h3> <span> JavaScript 1.1 and 1.2 </span></h3>
* <p>The following features, which were available in JavaScript 1.1 and JavaScript 1.2, have been removed:
* </p>
* <ul><li> Each local variable of a function is a property of the <code>arguments</code> object. For example, if a function <code>myFunc</code> has a local variable named <code>myLocalVar</code>, you can refer to the variable as <code>arguments.myLocalVar</code>.
* </li></ul>
* <ul><li> Each formal argument of a function is a property of the <code>arguments</code> object. For example, if a function <code>myFunc</code> has two arguments named <code>arg1</code> and <code>arg2</code>, you can refer to the arguments as <code>arguments.arg1</code> and <code>arguments.arg2</code>. (You can also refer to them as <code>arguments[0]</code> and <code>arguments[1]</code>.)
* </li></ul>
* <h2> <span> Examples </span></h2>
* <h3> <span> Example: Defining function that concatenates several strings </span></h3>
* <p>This example defines a function that concatenates several strings. The only formal argument for the function is a string that specifies the characters that separate the items to concatenate. The function is defined as follows:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">function myConcat(separator) {
* var result = "";
* 
* // iterate through non-separator arguments
* for (var i = 1; i &lt; arguments.length; i++)
* result += arguments[i] + separator;
* 
* return result;
* }
* </pre>
* <p>You can pass any number of arguments to this function, and it creates a list using each argument as an item in the list.
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">// returns "red, orange, blue, "
* myConcat(", ", "red", "orange", "blue");
* 
* // returns "elephant; giraffe; lion; cheetah; "
* myConcat("; ", "elephant", "giraffe", "lion", "cheetah");
* 
* // returns "sage. basil. oregano. pepper. parsley. "
* myConcat(". ", "sage", "basil", "oregano", "pepper", "parsley");
* </pre>
* <h3> <span> Example: Defining a function that creates HTML lists </span></h3>
* <p>This example defines a function that creates a string containing HTML for a list. The only formal argument for the function is a string that is "<code>u</code>" if the list is to be unordered (bulleted), or "<code>o</code>" if the list is to be ordered (numbered). The function is defined as follows:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* function list(type) {
* var result = "&lt;" + type + "l&gt;";
* 
* // iterate through non-type arguments
* for (var i = 1; i &lt; arguments.length; i++)
* result += "&lt;li&gt;" + arguments[i] + "&lt;/li&gt;";
* 
* result += "&lt;/" + type + "l&gt;"; // end list
* 
* return result;
* }
* </pre>
* <p>You can pass any number of arguments to this function, and it adds each argument as an item to a list of the type indicated. For example:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* var listHTML = list("u", "One", "Two", "Three");
* // listHTML is "&lt;ul&gt;&lt;li&gt;One&lt;/li&gt;&lt;li&gt;Two&lt;/li&gt;&lt;li&gt;Three&lt;/li&gt;&lt;/ul&gt;"
* </pre>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
var Arguments = {
  // This is just a stub for a builtin native JavaScript object.
/**
* <h2> <span> Summary </span></h2>
* <p>Specifies the currently executing function.
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <td colspan="2" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Property of <a href="arguments" shape="rect" title="Core JavaScript 1.5 Reference:Functions:arguments">arguments</a>; <a href="Function:arguments" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Function:arguments">Function.arguments</a> (deprecated)</td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">Implemented in:</td>
* <td colspan="1" rowspan="1">JavaScript 1.2
* <p>JavaScript 1.4: Deprecated <code>callee</code> as a property of <code>Function.arguments</code>, retained it as a property of a function's local <code>arguments</code> variable.
* </p>
* </td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">ECMA Version:</td>
* <td colspan="1" rowspan="1">ECMA-262</td>
* </tr>
* </table>
* <h2> <span> Description </span></h2>
* <p><code>callee</code> is a property of the <code>arguments</code> local variable available within all function objects; <code>callee</code> as a property of <code><a href="Core_JavaScript_1.5_Reference:Global_Objects:Function:arguments" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Function:arguments">Function.arguments</a></code> is no longer used. (<code>Function.arguments</code> itself is also deprecated.)
* </p><p><code>arguments.callee</code> allows anonymous functions to refer to themselves, which is necessary for recursive anonymous functions.
* </p><p>The <code>this</code> keyword does not refer to the currently executing function. Use the <code>callee</code> property to refer to a function within the function body.
* </p>
* <h2> <span> Examples </span></h2>
* <h3> <span> Example: Using <code>arguments.callee</code> in an anonymous recursive function </span></h3>
* <p>A recursive function must be able to refer to itself. Typically, a function refers to itself by its name. However, an anonymous function does not have a name, and if there is no accessible variable referring to it, i.e. the function is not assigned to any variable, the function cannot refer to itself. (Anonymous functions can be created by a <a href="Operators:Special_Operators:function_Operator" shape="rect" title="Core JavaScript 1.5 Reference:Operators:Special Operators:function Operator">function expression</a> or the <a href="Function" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Function"><code>Function</code> constructor</a>.) This is where <code>arguments.callee</code> comes in.
* </p><p>The following example defines a function, which, in turn, defines and returns a factorial function.
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">function makeFactorialFunc() {
* alert('making a factorial function!');
* return function(x) {
* if (x &lt;= 1)
* return 1;
* return x * arguments.callee(x - 1);
* };
* }
* 
* var result = makeFactorialFunc()(5); // returns 120 (5 * 4 * 3 * 2 * 1)
* </pre>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
callee: undefined,
/**
* <div style="border: 1px solid #FF5151; background-color: #FEBCBC; font-weight: bold; text-align: center; padding: 0px 10px 0px 10px; margin: 10px 0px 10px 0px;"><p style="margin: 4px 0px 4px 0px;">Obsolete</p></div>
* <h2> <span> Summary </span></h2>
* <p>Specifies the function that invoked the currently executing function.
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <td colspan="2" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Property of <a href="arguments" shape="rect" title="Core JavaScript 1.5 Reference:Functions:arguments">arguments</a></td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">Implemented in:</td>
* <td colspan="1" rowspan="1">JavaScript 1.1, NES 2.0
* <p>JavaScript 1.3: Deprecated.
* </p>
* </td>
* </tr>
* </table>
* <h2> <span> Description </span></h2>
* <p><b><code>arguments.caller</code> can no longer be used.</b> You can use the non-standard <code><a href="Core_JavaScript_1.5_Reference:Global_Objects:Function:caller" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Function:caller">caller</a></code> property of a function object instead. See its description for details.
* </p><p><code>arguments.caller</code> property is only available within the body of a function.
* </p>
* <h2> <span> Examples </span></h2>
* <p>The following code checks the value of <code>arguments.caller</code> in a function.
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">function myFunc() {
* if (arguments.caller == null) {
* return ("The function was called from the top!");
* } else
* return ("This function's caller was " + arguments.caller);
* }
* </pre>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
caller: undefined,
/**
* <h2> <span> Summary </span></h2>
* <p>Specifies the number of arguments passed to the function.
* </p>
* <table border="1" style="background:#FFFFFF none repeat scroll 0%;border: 1px solid #666666;margin-bottom:10px;margin-top:10px" width="100%">
* <tr>
* <td colspan="2" rowspan="1" style="background:#DDDDDD none repeat scroll 0%; border:1px solid #BBBBBB;">Property of <a href="arguments" shape="rect" title="Core JavaScript 1.5 Reference:Functions:arguments">arguments</a>; <a href="Function:arguments" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Function:arguments">Function.arguments</a> (deprecated)</td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">Implemented in:</td>
* <td colspan="1" rowspan="1">JavaScript 1.1
* <p>JavaScript 1.4: Deprecated <code>length</code> as a property of <code>Function.arguments</code>, retained it as a property of a function's local <code>arguments</code> variable.
* </p>
* </td>
* </tr>
* <tr>
* <td colspan="1" rowspan="1">ECMA Version:</td>
* <td colspan="1" rowspan="1">ECMA-262</td>
* </tr>
* </table>
* <h2> <span> Description </span></h2>
* <p><code>length</code> is a property of the <code>arguments</code> local variable available within all function objects; <code>length</code> as a property of <code><a href="Core_JavaScript_1.5_Reference:Global_Objects:Function:arguments" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Function:arguments">Function.arguments</a></code> is no longer used. (<code>Function.arguments</code> itself is also deprecated.)
* </p><p><code>arguments.length</code> provides the number of arguments actually passed to a function. By contrast, the <code>Function.length</code> property indicates how many arguments a function expects.
* </p>
* <h2> <span> Examples </span></h2>
* <h3> <span> Example: Using <code>Function.length</code> and <code>arguments.length</code> </span></h3>
* <p>The following example demonstrates the use of <code>Function.length</code> and <code>arguments.length</code>.
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* function addNumbers(x,y){
* if (arguments.length == addNumbers.length) {
* return (x+y)
* }
* else return 0
* }
* </pre>
* <p>If you pass more than two arguments to this function, the function returns 0:
* </p>
* <pre style="background:#EEEEEE none repeat scroll 0% 50%;border:1px solid #666666;padding:5px 5px" xml:space="preserve">
* result=addNumbers(3,4,5)   // returns 0
* result=addNumbers(3,4)     // returns 7
* result=addNumbers(103,104) // returns 207
* </pre>
* <h2> <span> See also </span></h2>
* <p><a href="Core_JavaScript_1.5_Reference:Global_Objects:Function:length" shape="rect" title="Core JavaScript 1.5 Reference:Global Objects:Function:length">Function.length</a>
* </p>
* 
* <ul style="list-style-type:none;font-size:0.9em;text-align:center">
* <li id="f-copyright">Content is available under <a href="http://developer.mozilla.org/en/docs/MDC:Copyrights" shape="rect" title="MDC:Copyrights">these licenses</a>.</li>	  		<li id="f-about"><a href="http://developer.mozilla.org/en/docs/MDC:About" shape="rect" title="MDC:About">About MDC</a></li>	  				</ul>
*/
length: undefined,
};

