Autocompleter.Base.prototype = {
  onHover: function(event) {
    var element = Event.findElement(event, 'LI');
    foo = 5
    //puts(foo)
    if(this.index != element.autocompleteIndex) 
    {
        this.index = element.autocompleteIndex;
        this.render();
    }
    Event.stop(event);
  }
}  
  
