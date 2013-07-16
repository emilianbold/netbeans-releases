   
function getInfo() {
    console.log("getInfo from global");
}

function getGlobal() {
    console.log("global");
}

var A = {
    getInfo: function () {
        console.log("getInfo from A");
    },
    getName: function() {
        console.log("A");
    },
    B: {},
    monitor : 24
};
  
with(A) {
    getInfo();
    getGlobal();
    getName();
    B.getName = function () {
        console.log("B");
    };     
    B.createBuf = function () {
        console.log("create buf");
    };
    
}   

A.getName();
A.B.getName(); 
