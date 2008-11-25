// JavaScript 1.7 stuff
// From snippets in
// https://developer.mozilla.org/en/New_in_JavaScript_1.7

// Generators
function fib() {
  var i = 0, j = 1;
  while (true) {
    yield i;
    var t = i;
    i = j;
    j += t;
  }
}

var g = fib();
for (var i = 0; i < 10; i++) {
  document.write(g.next() + "<br>\n");
}



var obj = {name:"Jack Bauer", username:"JackB", id:12345, agency:"CTU",
          region:"Los Angeles"};

var it = Iterator(obj);

try {
  while (true) {
    print(it.next() + "\n");
  }
} catch (err if err instanceof StopIteration) {
  print("End of record.\n");
} catch (err) {
  print("Unknown error: " + err.description + "\n");
}


// Array Comprehensions
function range(begin, end) {
  for (let i = begin; i < end; ++i) {
    yield i;
  }
}
var ten_squares = [i * i for each (i in range(0, 10))];

var evens = [i for each (i in range(0, 21)) if (i % 2 == 0)];

// Blockscope with Let
var x = 5;
var y = 0;

let (x = x+10, y = 12) {
  print(x+y + "\n");
}

print((x + y) + "\n");


var x1 = 5;
var y1 = 0;
document.write( let(x1 = x1 + 10, y1 = 12) x1+y1  + "<br>\n");
document.write(x1+y1 + "<br>\n");

if (x > y) {
  let gamma = 12.7 + y;
  i = gamma * x;
}

// Let in for loop
var i=0;
for ( let i=i ; i < 10 ; i++ )
  document.write(i + "<br>\n");

for ( let [name,value] in obj )
  document.write("Name: " + name + ", Value: " + value + "<br>\n");

// Destructuring assignment
function destr() {
    var a = 1;
    var b = 3;

    [a, b] = [b, a];
}

function destr2() {
    var a = 'o';
    var b = "<span style='color:green;'>o</span>";
    var c = 'o';
    var d = 'o';
    var e = 'o';
    var f = "<span style='color:blue;'>o</span>";
    var g = 'o';
    var h = 'o';

    for (lp=0;lp<40;lp++)
        {[a, b, c, d, e, f, g, h] = [b, c, d, e, f, g, h, a];
         document.write(a+''+b+''+c+''+d+''+e+''+f+''+g+''+h+''+"<br />");}
}

function f() {
  return [1, 2];
}

function callf() {
    var a, b;
    [a, b] = f();
    document.write ("A is " + a + " B is " + b + "<br>\n");
}

// Looping across objects
let obj = { width: 3, length: 1.5, color: "orange" };

for (let [name, value] in Iterator(obj)) {
  document.write ("Name: " + name + ", Value: " + value + "<br>\n");
}

// This is not working yet:
/*
var people = [
  {
    name: "Mike Smith",
    family: {
      mother: "Jane Smith",
      father: "Harry Smith",
      sister: "Samantha Smith"
    },
    age: 35
  },
  {
    name: "Tom Jones",
    family: {
      mother: "Norah Jones",
      father: "Richard Jones",
      brother: "Howard Jones"
    },
    age: 25
  }
];

for each (let {name: n, family: { father: f } } in people) {
  document.write ("Name: " + n + ", Father: " + f + "<br>\n");
}
*/

// Ignoring some return values
function f() {
  return [1, 2, 3];
}

var [a, , b] = f();
document.write ("A is " + a + " B is " + b + "<br>\n");


function testexpr() {
    // Simple regular expression to match http / https / ftp-style URLs.
    var parsedURL = /^(\w+)\:\/\/([^\/]+)\/(.*)$/.exec(url);
    if (!parsedURL)
      return null;
    var [, protocol, fullhost, fullpath] = parsedURL;
}

