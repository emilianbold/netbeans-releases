function SimpleMode(){
    this.name = "Test";
    var lastName = "Simple";
    var self = this;
    this.log = function(){
        return "log";
    };
    this.printName = ko.computed(function(){
        return "<b>"+self.name+" "+lastName+"</b>";
    });

    self.skills = { "speak":1, "listen":2, "point": function(){}};
    self.today = new Date();

}
ko.applyBindings(new SimpleMode());