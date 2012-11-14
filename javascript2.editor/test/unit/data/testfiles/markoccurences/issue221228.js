function Greetings() {
    function hi() { // rename here
        formatter.println(msg);
    }

    var msg = ""; // rename here
}


var a = new Greetings();
a.hi(); // rename hi here
a.msg = "Hi"; // rename msg here