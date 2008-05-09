
        if (Element.empty('about-content')) {
          new Ajax.Updater('about-content', 'rails/info/properties', {
            method:     'get',
            onFailure:  function() {Element.classNames('about-content').add('failure')},
            onComplete: function() {new Effect.BlindDown('about-content', {duration: 0.25})}
          });
        } else {
          new Effect[Element.visible('about-content') ?
            'BlindUp' : 'BlindDown']('about-content', {duration: 0.25});
        }
      }

      window.onload = function() {
        $('search-text').value = '';
        $('search').onsubmit = function() {
          $('search-text').value = 'site:rubyonrails.org ' + $F('search-text');
        }
      }

