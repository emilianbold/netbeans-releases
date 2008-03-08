// Make sure window.onload isn't considered a method/variable definition
      window.onload = function() {
        $('search-text').value = '';
        $('search').onsubmit = function() {
          $('search-text').value = 'site:rubyonrails.org ' + $F('search-text');
        }
      }

// Make sure processCall is static
function Jmaki() {
     this.processCall = function() {
     }      
}

// Make sure foo and bar only show up once
function Foo() {
    this.bar = function() {
    }    
    this.foo = 5;
} 

// Make sure we get a superclass of Spry.Effec.Animator for Spry.Effect.Move here
Spry.Effect.Move = function(element, fromPos, toPos, options)
{
        Spry.Effect.Animator.call(this, options);
}
// Ditto
MySubClass = function(element, fromPos, toPos, options)
{
        MySuperClass.call(this, options);
}


Window.onwhatever = function() { }

