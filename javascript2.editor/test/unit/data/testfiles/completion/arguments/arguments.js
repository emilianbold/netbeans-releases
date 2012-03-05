
var ArgumentsContext = {};

ArgumentsContext.testFunction = function testFunction(param1, param2) {
    formatter.println("Calling testFunction with " + arguments.length + " arguments.");
    formatter.addIndent(4);
    for (var i = 0; i < arguments.length; i++) {
        formatter.println("arguments[" + i + "]: " + arguments[i]);
    }
    formatter.removeIndent(4)
    formatter.println("End of call");
    formatter.println("");
}

ArgumentsContext.testFunction(1, 2);
ArgumentsContext.testFunction(1, 2, 4, 5, 6, 7);
formatter.print("Declaration of " + ArgumentsContext.testFunction.name + " function has " 
        + ArgumentsContext.testFunction.length + " arguments.");
    
testFunction(1, 2, 3);
