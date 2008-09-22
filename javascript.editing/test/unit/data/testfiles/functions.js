var multiply = new Function("x", "y", "return x * y;");
function multiply2(x, y) {
   return x * y;
}
var multiply3 = function(x, y) {
   return x * y;
}
var multiply4 = function func_name(x, y) {
   return x * y;
}
function foo() {}
alert(foo); // alerted string contains function name "foo"
var bar = foo;
alert(bar); // alerted string still contains function name "foo"

foo2(); // alerts FOO!
function foo2() {
   alert('FOO!');
}

// function declaration
function foo3() {}

// function expression
(function bar2() {})

// function expression
x = function hello() {}

if (x) {
   // function expression
   function world() {}
}

// function statement
function a() {
   // function statement
   function b() {}
   if (0) {
      // function expression
      function c() {}
   }
}


