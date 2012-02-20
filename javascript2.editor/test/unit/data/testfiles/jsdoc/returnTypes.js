/**
 * @returns {Shape} A new shape
 */
Shape.prototype.clone = function(){
   return new Shape();
}

/* Here should be returned null as well since no type or return tag is available. */
Shape.prototype.clone4 = function(){
   return new Shape();
}

