/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
function allowDrop(ev)
{
    ev.preventDefault();
}

function drag(ev)
{
    ev.dataTransfer.setData("pets", ev.target.id);
}

function drop(ev)
{
    ev.preventDefault();
    var data = ev.dataTransfer.getData("pets");
    ev.target.appendChild(document.getElementById(data));    
    document.getElementById('rabbit1').setAttribute("src","img/cute_rabbit1.jpg");
    document.getElementById('rabbit1').setAttribute("width","400");
    document.getElementById('rabbit1').setAttribute("height","300");
    
    document.getElementById("div1").setAttribute('width',"400");
    document.getElementById("div1").setAttribute('height',"300");

    document.getElementById('intro').innerHTML="Well, hello there!";
    document.getElementById('drag1').style.display='none';
}

