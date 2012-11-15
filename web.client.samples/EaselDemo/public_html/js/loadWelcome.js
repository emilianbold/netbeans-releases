/* 
 * Simple javascript file to get the current language from the browser
 * and display the proper welcome in that language if posssible.
 */
document.write('<scr'+'ipt type="text/javascript" src="js/jquery.js" ></scr'+'ipt>');
var lang;

function getLanguage() {
    try {
        var str1 = navigator.userAgent.match(/\((.*)\)/)[1];
        var ar1 = str1.split(/\s*;\s*/), lang;
        for (var i = 0; i < ar1.length; i++) {
            if (ar1[i].match(/^(.{2})$/)) {
                lang = ar1[i];
            }
        }
    } catch (e) {
    }
    return lang;
}


function getWelcome() {
    lang = getLanguage();
    if (!lang || !phrases[lang]) {
        lang = 'nl';
    }
    document.getElementById('welcome').innerHTML = phrases[lang];
    
}


var phrases = { /* translation table for page */
    en: ["<h1>Welcome!</h1><p>This is a template for a simple marketing or informational website. It includes a large callout called the hero unit and three supporting pieces of content. Use it as a starting point to create something more unique.</p><p><a href='about.html' class='btn btn-primary btn-large'>Learn more &raquo;</a></p>"],
    nl: ["<h1>Welkom!</h1><p>Dit is een sjabloon voor een eenvoudige marketing of informatieve website. Het omvat een groot bijschrift genaamd de held unit en drie ondersteunende stukken content. Gebruik het als een startpunt om iets te creëren meer uniek.</p><p><a href='about.html' class='btn btn-primary btn-large'> meer informatie &raquo;</a></p>"]
};


