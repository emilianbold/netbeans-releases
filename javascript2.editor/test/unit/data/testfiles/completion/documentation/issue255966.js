/**
 * @type {Array<String>} Array
 */
var arrStr = [];
arrStr.push("test");

/** 
 * @type {Array<Object>} Array
 */
var arrObj = [];
arrObj[0] = new Object();
arrObj[1];

/**
 * @type {Object.<Sring, Number>} personObj description
 */
var personObj = {firstName:"John", age:36};
personObj["firstName"];

/**
 * @type {Array.<{firstName:String, age:Number}>} Array somedesc
 */
var personsArr = [{firstName:"John", age:36},
                  {firstName:"James",age:46}];
personsArr.push({});  

/**
 * 
 * @type {{myNum:number, myString:string}} someObj
 */
var someObj = {myNum:10, myString:"test" };
someObj["myNum"];

/**
 * 
 * @type {Array.<Object.<String,Number>>} Array
 */
var personsNew = [{firstName:"John", age:36},
                  {firstName:"James",age:46}];           

personsNew.push({});


/**
 * Display object details
 * @param {Array.<{firstName: String,age: Number}>} persons somedescription
 * @returns {Array.<String>} test
 */
function printObjs(args) {
   for (var index in args) {
       console.log(args[index].firstName + " : " + args[index].age);
   }   
   return ["t1", "t2"];
}

printObjs(persons);

 
/**
 * 
 * @param {{myNum: number, myString: string}} someObj
 * @returns {String}
 */
function someFunc(args) {
   return "test"+ args.myNum + " , " +args.myString ;
}
someFunc(someObj);

/**
 * 
 * @param {*} args
 * @returns {String}
 */
function anotherFunc(args) {
  return args.toString();  
}
anotherFunc("test");

/**
 * @return {{myNum: number, myObject}}
 * An anonymous type with the given type members.
 */ 
function getTmpObject() {
    return {
        myNum: 2,
        myObject: 0 || undefined || {}
    };
}
getTmpObject();

