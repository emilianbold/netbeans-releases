/**
 * Construct a new Shape object.
 * @constructor
 * @class Shape creates new Shape class
 * @return {Shape/Coordinate} A new shape.
 */
function Shape(){

   /**
    * This is an example of a function that is not given as a property
    * of a prototype, but instead it is assigned within a constructor.
    * For inner functions like this to be picked up by the parser, the
    * function that acts as a constructor <b>must</b> be denoted with
    * the <b>&#64;constructor</b> tag in its comment.
    * @type {String}
    */
   this.getClassName = function(){
      return "Shape";
   }

   /**
    * This is an inner method, just used here as an example
    */
   function addReference(){
       // Do nothing...
   }

}

/**
 * Create a new Hexagon instance.
 * @cfg {int} sideLength The length of one side for the new Hexagon
 */
function Hexagon(sideLength) {
}


/**
 * This is an unattached (static) function that adds two integers together.
 * @param {int} One The first number to add
 * @param {int} Two The second number to add
 */
function Add(One, Two){
    return One + Two;
}


/**
 * The color of this shape
 * @type {Color}
 */
Shape.prototype.color = null;

/**
 * The border of this shape.
 * @property
 */
Shape.prototype.border = function(){return border;};

/*
 * These are all the instance method implementations for Shape
 */

/**
 * Get the coordinates of this shape. It is assumed that we're always talking
 * about shapes in a 2D location here.
 * @return A Coordinate object representing the location of this Shape
 * @type {Coordinate[]}
 */
Shape.prototype.getCoords = function(){
   return this.coords;
}

/**
 * Get the color of this shape.
 * @link #setColor sets the color
 * @type {Color}
 */
Shape.prototype.getColor = function(){
   return this.color;
}

/**
 * Set the coordinates for this Shape
 * @param {Coordinate} coordinates The coordinates to set for this Shape
 */
Shape.prototype.setCoords = function(coordinates){
   this.coords = coordinates;
}

/**
 * Set the color for this Shape
 * @param {Color} color The color to set for this Shape
 * @param other There is no other param, but it can still be documented if
 *              optional parameters are used
 */
Shape.prototype.setColor = function(color){
   this.color = color;
}

/**
 * Clone this shape
 * @return {Shape} A copy of this shape
 */
Shape.prototype.clone = function(){
   return new Shape();
}

/**
 * Clone this shape
 * @return {Shape} A copy of this shape
 */
Shape.prototype.clone2 = function(){
   return new Shape();
}

/**
 * Here should be returned null since no type or return tag is available.
 */
Shape.prototype.clone3 = function(){
   return new Shape();
}

/**
 * Create a new Rectangle instance.
 * @constructor
 * @param {int} width The optional width for this Rectangle
 * @param {int} height Thie optional height for this Rectangle
 */
function Rectangle(width, // This is the width
                  height // This is the height
                  ){
   if (width){
      this.width = width;
      if (height){
	 this.height = height;
      }
   }
}


/* Inherit from Shape */
Rectangle.prototype = new Shape();

/**
 * Value to represent the width of the Rectangle.
 * <br>Text in <b>bold</b> and <i>italic</i> and a
 * link to <a href="http://sf.net">SourceForge</a>
 * @private
 * @type {int}
 */
Rectangle.prototype.width = 0;

/**
 * Value to represent the height of the Rectangle
 * @private
 * @type {int}
 */
Rectangle.prototype.height = 0;

/**
 * Get the type of this object.
 * @type {String}
 */
Rectangle.prototype.getClassName= function(){
    return "Rectangle";
}

/**
 * Get the value of the width for the Rectangle
 * @type {int}
 */
Rectangle.prototype.getWidth = function(){
   return this.width;
}

/**
 * Get the value of the height for the Rectangle.
 * Another getter is the Shape#getColor method in the
 * Shape base class.
 * @return The height of this Rectangle
 * @type {int}
 */
Rectangle.prototype.getHeight = function(){
    return this.height;
}

/**
 * Set the width value for this Rectangle.
 * @param {int} width The width value to be set
 */
Rectangle.prototype.setWidth = function(width){
   this.width = width;
}

/**
 * Set the height value for this Rectangle.
 * @param {int} height The height value to be set
 */
Rectangle.prototype.setHeight = function(height){
   this.height = height;
}

/**
 * Get the value for the total area of this Rectangle
 * @return {int} mam, je tamtotal area of this Rectangle
 * @type {int}
 */
Rectangle.prototype.getArea = function(){
   return width * height;
}


/**
 * Create a new Square instance.
 * @param {int} width The optional width for this Rectangle
 * @param {int} height The optional height for this Rectangle
 */
function Square(width, height){
   if (width){
      this.width = width;
      if (height){
	 this.height = height;
      }
   }

}

/* Square is a subclass of Rectangle */
Square.prototype = new Rectangle();

/**
 * Set the width value for this Shape.
 * @param {int} width The width value to be set
 */
Square.prototype.setWidth = function(width){
   this.width = this.height = width;
}

/**
 * Set the height value for this Shape
 * Sets the Rectangle#height attribute in the Rectangle.
 * @param {int} height The height value to be set
 */
Square.prototype.setHeight = function(height){
   this.height = this.width = height;
}


/**
 * Create a new Circle instance based on a radius.
 * @extends Shape
 * @param {int} radius The optional radius of this Circle 
 */
function Circle(radius){
   if (radius) {
      /** The radius of the this Circle. */
      this.radius = radius;
   }
}

/* Circle inherits from Shape */
Circle.prototype = new Shape();

/**
 * The radius value for this Circle
 * @private
 * @type {int}
 */
Circle.prototype.radius = 0;

/**
 * A very simple class (static) field that is also a constant
 * @static
 * @type {float}
 */
Circle.PI = 3.14;

/**
 * Get the radius value for this Circle
 * @type {int}
 */
Circle.prototype.getRadius = function(){
   return this.radius;
}

/**
 * Set the radius value for this Circle
 * @param {int} radius The Circle#radius value to set
 */
Circle.prototype.setRadius = function(radius){
   this.radius = radius;
}

/**
 * An example of a  class (static) method that acts as a factory for Circle
 * objects. Given a radius value, this method creates a new Circle.
 * @param {int} radius The radius value to use for the new Circle.
 * @type {Circle}
 */
Circle.createCircle = function(radius){
    return new Circle(radius);
}


/**
 * Create a new Coordinate instance based on x and y grid data.
 * @param {int} [x=0] The optional x portion of the Coordinate
 * @param {int} [y=0] The optinal y portion of the Coordinate
 */
function Coordinate(x, y){
   if (x){
      this.x = x;
      if (y){
	 this.y = y;
      }
   }
}

/**
 * The x portion of the Coordinate
 * @type int
 * @link #getX
 * @link #setX
 */
Coordinate.prototype.x = 0;

/**
 * The y portion of the Coordinate
 * @type int
 * @link #getY
 * @link #setY
 */
Coordinate.prototype.y = 0;

/**
 * Gets the x portion of the Coordinate.
 * @type int
 * @link #setX
 */
Coordinate.prototype.getX = function(){
   return this.x;
}

/**
 * Get the y portion of the Coordinate.
 * @type int
 */
Coordinate.prototype.getY = function(){
   return this.y;
}

/**
 * Sets the x portion of the Coordinate.
 * @param {int} x The x value to set
 * @link #getX
 */
Coordinate.prototype.setX = function(x){
   this.x = x;
}

/**
 * Sets the y portion of the Coordinate.
 * @param {int} y The y value to set
 * @link #getY
 */
Coordinate.prototype.setY = function(y){
   this.y = y;
}

/**
 * This class exists to demonstrate the assignment of a class prototype
 * as an anonymous block.
 */
function ShapeFactory(){
}

ShapeFactory.prototype = {
   /**
    * Creates a new Shape instance.
    * @return A new Shape
    * @type Shape
    */
   createShape: function(){
      return new Shape();
   }
}

/**
 * An example of a singleton class
 * @param ... Arguments represent coordinates in the shape.
 * @constructor
 */
MySingletonShapeFactory = function(){

   /**
    * Get the next Shape
    * @type Shape
    * @return A new Shape
    */
   this.getShape = function(){
      return null;
   }

}


/**
 * Create a new Foo instance.
 * @constructor
 */
function Foo(){}

/**
 * Creates a new instance of Bar.
 * @constructor
 */
function Bar(){}

/**
 * Nested class
 * @constructor
 */
Foo.Bar = function(){
	/** The x. */ this.x = 2;
}

Foo.Bar.prototype = new Bar();
/** The y. */
Foo.Bar.prototype.y = '3';

/**
 * @return {Number}
 */
function martion () {
    return MyObj.getVersion;
}

var MyObj = {
    version: 10,
    factory: function () {
        return this;
    },

    create: function () {
        return new MyObj();
    },

    getInfo: function() {
       return "text";
    },

    /**
    * @return {Number}
    */
    getVersion: function() {
        return version;
    }
}

Shape.prototype.clone4 = function(){
   return new Shape();
}

/**
 * Create a new Rectangle instance.
 */
function Rectangle2(width, height) {
    if (width) {
        this.width = width;
        if (height){
            this.height = height;
        }
    }
}

/**
 * @me
 */
function methodCCTest() {
}
