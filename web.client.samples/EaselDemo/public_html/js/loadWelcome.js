/* 
 * Simple javascript file to get the current language from the browser
 * and display the proper welcome in that language if posssible.
 */
document.write('<scr'+'ipt type="text/javascript" src="js/jquery.js" ></scr'+'ipt>');
var lang;

function getWelcome() {
    lang = getLanguage();
    if (!lang || !phrases[lang]) {
        lang = 'nl';
    }
    document.getElementById('welcome').innerHTML = phrases[lang];
    
}

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

var phrases = { /* translation table for page */
    en: ["<h1>Welcome!</h1><p>NetBeans Project Easel is about combining state of the art HTML5/CSS3/JavaScript client development with Java/REST web services. Enabling the development and customization of flexabilble and performant industry standard client-side interfaces for multiple devices.</p><p><a href='about.html' class='btn btn-primary btn-large'>Learn more &raquo;</a></p>"],
    nl: ["<h1>Welkom!</h1><p>NetBeans Project Schildersezel gaat over een combinatie van state of the art HTML5/CSS3/JavaScript client ontwikkeling met Java / REST webservices. Het inschakelen van de ontwikkeling en aanpassing van flexabilble en performante industrie standaard client-side interfaces voor meerdere apparaten.</p><p><a href='about.html' class='btn btn-primary btn-large'> meer informatie &raquo;</a></p>"]
};