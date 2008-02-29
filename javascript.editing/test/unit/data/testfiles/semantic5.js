/* Make sure call-parameters are considered reads as well - but not globals! */
function dosomething(iterator) {
    var result;
    this.each(function(value, index) {
            if (iterator(value, index)) {
                result = value;
                throw $break;
                global = result;
            }
    });
    return result;
}  

