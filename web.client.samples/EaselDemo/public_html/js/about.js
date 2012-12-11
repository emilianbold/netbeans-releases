/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

function loadRest() {
    url = "rest/resources/manufacturer.json";
    $.getJSON(url, function(data) {
        content = "<div id='restDataList'><table border=0><tr>";
        for (i = 0; i < 4; i++) {
            content += "<td><a id='listItem' onclick='showDetails(" + data[i].manufacturerId + ")' class='btn btn-primary'>" + data[i].name + " &raquo;</a></td></tr>";
        }
        content += "</table></div>";
        $("#rest").html(content);
    });
}

function showDetails(id) {
    url = "rest/resources/id" + id+".json";
    $.getJSON(url, function(data) {
       $("#restName").text(data.name);
       $("#restRep").text(data.rep);
       $("#restEmail").text(data.email);
       $("#restPhone").text(data.phone);
    });
    dataSwitch();
}

function dataSwitch() {
    $('#restDataList').toggle(700, 'easeOutExpo');
    $('#restDetails').toggle(700, 'easeOutExpo');
}

// This is the jQuery animation code
$(document).ready(function() {
	$("#start").click(function() {
		$("#animation-div").animate({
			height:200,
			opacity: 0.0
		}, 500);
		$("#animation-div").animate({
			width:200,
			opacity: 1.0
		}, 500);
		$("#animation-div").animate({height:100}, 500);
		$("#animation-div").animate({width:100}, 500);
	});
	
	$('#switch').click(function() {
		$('#blue_div').toggle('slow', 'easeOutExpo');
		$('#red_div').toggle('slow', 'easeOutExpo');
	});
	
	/* Add extra jQuery examples here */

});

function loadTweetData() {
    $.getJSON('http://search.twitter.com/search.json?q=JavaOne&callback=?', function(data) {
       $("#tweetImg").attr("src",data.results[0].profile_image_url);
       $("#tweetUsername").text(data.results[0].from_user_name);
       $("#tweetUser").text("( @"+ data.results[0].from_user + " )");
       $("#tweetText").text(data.results[0].text);
    });
}

// This is the twitter Popup code section
$(document).ready(function() {
	$('a.tweet-window').click(function() {
		
		// Loading the data from a REST call
                loadTweetData();

		//Getting the variable's value from a link 
		var tweetBox = $(this).attr('href');

		//Fade in the Popup
		$(tweetBox).fadeIn(300);

		//Set the center alignment padding + border see css style
		var popMargTop = ($(tweetBox).height() + 24) / 2;
		var popMargLeft = ($(tweetBox).width() + 24) / 2;

		$(tweetBox).css({
			'margin-top' : -popMargTop,
			'margin-left' : -popMargLeft
		});

		// Add the mask to the body
		$('body').append('<div id="mask"></div>');
		$('#mask').fadeIn(300);

		return false;
	});

	// When clicking on the close button or the mask layer the popup is closed
	$('a.tweet-close, #mask').live('click', function() {
		$('#mask , .tweet-popup').fadeOut(300, function() {
			$('#mask').remove();
		});
		return false;
	});
});
