/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
var orig_draganddrop="";
var orig_div1 = "";
var orig_rabbit1 = "";

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
    orig_draganddrop = document.getElementById('DandD_div').innerHTML;
    
    ev.preventDefault();
    var data = ev.dataTransfer.getData("pets");
    ev.target.appendChild(document.getElementById(data));    
    document.getElementById('drop1').setAttribute("src","img/cute_rabbit1.jpg");
    document.getElementById('drop1').setAttribute("width","300");
    document.getElementById('drop1').setAttribute("height","200");
    
    document.getElementById("div1").setAttribute('width',"300");
    document.getElementById("div1").setAttribute('height',"200");

    document.getElementById('draganddrop').innerHTML="Well, Hello There! <br/><a id='resetBtn' class='btn' onclick='reset()' href='#'>Reset</a>";
    document.getElementById('drag1').style.display='none';
}

function reset(){
    document.getElementById("DandD_div").innerHTML = orig_draganddrop;
}

