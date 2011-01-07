        
function addToFavorites(id) {
    jQuery.ajax({ 
        type: "GET",
        url: "http://localhost/favorites/add", 
        data: "id="+id,
        success: function(message){
            
            document.getElementById('p'+id).innerHTML = message;
            
    
        }

    });
}

function removeFromFavorites(id) {
    jQuery.ajax({ 
        type: "GET",
        url: "http://localhost/favorites/remove", 
        data: "id="+id,
        success: function(message){
            document.getElementById('p'+id).innerHTML = message;
    
        }

    });
}

function addToFavoritesDetail(id) {
    jQuery.ajax({ 
        type: "GET",
        url: "http://localhost/favorites/add-detail", 
        data: "id="+id,
        success: function(message){
            document.getElementById('p'+id).innerHTML = message;
    
        }

    });
}

function removeFromFavoritesDetail(id) {
    jQuery.ajax({ 
        type: "GET",
        url: "http://localhost/favorites/remove-detail", 
        data: "id="+id,
        success: function(message){
            document.getElementById('p'+id).innerHTML = message;
    
        }

    });
}

function removeFromFavoritesInFavorites(id) {
    jQuery.ajax({ 
        type: "GET",
        url: "http://localhost/favorites/remove-detail", 
        data: "id="+id,
        success: function(message){
            //                alert(id);
            var elid = document.getElementById('p'+id);
            //                alert(elid);
            elid.setAttribute("style", "display:none");
            
        }

    });
}