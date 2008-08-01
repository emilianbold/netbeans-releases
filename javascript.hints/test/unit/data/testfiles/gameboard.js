    var XorO;
    var imagesPrefix = '/ttt-java/images/';
    var winner = '<h1>We have a winner!!!</h1>';
    
    function getXHR() {
        if (window.XMLHttpRequest) {
            return new XMLHttpRequest();
        } else if (window.ActiveXObject) {
            return new ActiveXObject("Microsoft.XMLHTTP");
        }
        return null;
    };
        
    this.submitForm = function(args) {
        setXorO();
        updateBoard({ position: args.name, imageLoc: XorO });
        request = this.getXHR();
        url="http://localhost:8080/ttt-java/tictactoe?position=" + args.name + "&imageLoc=" + XorO;
        request.open("POST", url, false);
        request.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
        request.onreadystatechange = function() {
            if (request.readyState == 4) {
                if (request.status == 200) {
                    if (request.responseText) {
                        // TODO: Winner in both contexts should be notified using Comet.
                        // TODO: This is only a workaround.
                        if (request.responseText == "winner") {
                            reportWinner(winner);
                        }
                        updatePage();
                    }
                } else {
                    // handle error
                }
            }
        }

        request.send(args);
    };
        
    function updatePage() {
        parent.frames[0].location.href="/ttt-java/tictactoe?action=start";
    }
        
    function reportWinner(response) {
        //alert("reportWinner invoked");
        document.getElementById("result").innerHTML = response;
    }
        
    function updateBoard(args) {
        //alert(args.imageLoc);
        document.images[args.position].src = args.imageLoc;
        if (args.result == winner) {
            document.getElementById("result").innerHTML = args.result;
        }
    }
        
    function updateMessage(args) {
        document.getElementById("result").innerHTML = args;
    }
        
    function initBoard() {
        document.images["R1C1"].src = imagesPrefix + "/blank.png";
        document.images["R1C2"].src = imagesPrefix + "/blank.png";
        document.images["R1C3"].src = imagesPrefix + "/blank.png";
        document.images["R2C1"].src = imagesPrefix + "/blank.png";
        document.images["R2C2"].src = imagesPrefix + "/blank.png";
        document.images["R2C3"].src = imagesPrefix + "/blank.png";
        document.images["R3C1"].src = imagesPrefix + "/blank.png";
        document.images["R3C2"].src = imagesPrefix + "/blank.png";
        document.images["R3C3"].src = imagesPrefix + "/blank.png";
    }
        
    function setXorO() {
        request = this.getXHR();
        url = "http://localhost:8080/ttt-java/tictactoe?action=currentSymbol";
        request.open("POST", url, false);
        request.onreadystatechange = function() {
            if (request.readyState == 4) {
                if (request.status == 200) {
                    if (request.responseText) {
                        XorO = imagesPrefix + request.responseText + '.png';
                    }
                } else {
                    // handle error
                }
            }
        }

        request.send();
    }
        
    function submitOnClick() {
        var s1 = "winner";
        if (s1 == "winner")
            alert("string matched");
        else
            alert("string did not match");
        document.getElementById('result').innerHTML = 'winner';
    }

