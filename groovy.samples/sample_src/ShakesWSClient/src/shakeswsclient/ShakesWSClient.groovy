package shakeswsclient

import groovyx.net.ws.WSClient

class ShakesWSClient {

    def proxy = new WSClient("http://www.xmlme.com/WSShakespeare.asmx?WSDL", ShakesWSClient.class.classLoader)

    //<SPEECH>
    //    <PLAY>MACBETH</PLAY>
    //        <SPEAKER>ALL</SPEAKER>
    //          Fair is foul, and foul is
    //          fair: Hover through the fog
    //          and filthy air.
    //</SPEECH>

    String setSearchString(searchString) {
        def xml = proxy.GetSpeech(searchString)
        def XmlParser parser = new XmlParser()
        def speech = parser.parseText (xml)
        ["PLAY: ${speech.PLAY.text()}\n",
        "SPEAKER: ${speech.SPEAKER.text()}\n",
        "TEXT: ${speech.text()}"].sum("")
    }

}





