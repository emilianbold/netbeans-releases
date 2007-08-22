--------------------------------------------------------------------------------
                Mobile Media API (JSR 135 API) Demonstration
--------------------------------------------------------------------------------

1. Introduction
    
    The MobileMediaAPI application contains four MIDlets that showcase 
    the multimedia capabilities. This readme file describes the MIDlets 
    and includes additional information about using multimedia from your applications.
  
    
2. Usage

    2.1 Simple Tones
        The Simple Tones example demonstrates how to use interactive synthetic 
        tones. Select an example, then click Play on the lower right.

        - Short Single Tone and Long Single Tone use Manager.playTone() to play 
          tones with different pitch.
        - Short MIDI event plays a chord on the interactive MIDI device 
          (locator "device://midi"). The shortMidiEvent() method of MIDIControl 
          is used to trigger the notes of the chord.
        - To run the MMAPI Drummer demo, click or type number keys (0-9). Each 
          number plays a different sound. 
          
    2.2 Simple Player
        The Simple Player application demonstrates the range of audio and video 
        capabilities of the emulator. It includes sample files in a variety of 
        formats and can play files from the emulator's persistent storage or from HTTP URLs.
        The player portion uses a generic javax.microedition.media.Player interface. 
        The player displays duration, media time, and controls for running the media file. 
        If metadata is available in a file, the player enables you to view the information, 
        such as author and title. In the case of MIDI files, if karaoke text is present in 
        the file, it displays on the screen during play. Graphical user interface controls 
        can be viewed on the display screen if applicable. You can access these controls 
        by selecting one of the media samples in Simple Player, then pressing the Menu button 
        to view and select the desired command.

        Select Simple Player then click Launch. The demo includes the following 
        media samples:
        - Bong plays a short WAV file. You can adjust certain playback features, 
          as described later in this document. The display shows the duration of 
          the sound in minutes:seconds:tenths of a second, for example 00:17:5. 
          This audio sample is a resource file in the MIDlet suite JAR file.
        - MIDI Scale plays a sample musical scale. The display shows the title 
          of the selected music file, the duration of the song, the elapsed time 
          during playback, and the current tempo in beats per minute (bpm). This 
          MIDI file is stored in the MIDlet suite JAR file.
        - Simple Ring Tone plays a short sequence of Beethoven's Fifth Symphony. 
          The display shows the title of the selected music file, the duration 
          of the song, the elapsed time in seconds and tenths of a second during 
          playback, and the current tempo in beats per minute (bpm). This ringtone file 
          (.jts format) is stored in the MIDlet suite JAR file.
        - WAV Music plays a brief audio file. The display shows the title of 
          the audio file, the duration of the audio the elapsed time during playback, 
          and the playback rate in percent. This WAV file is retrieved from an HTTP server.
        - MIDI Scale plays a MIDI file that is retrieved from an HTTP server.
        - The Animated GIF example shows an animated GIF that counts from 1 to 5. 
          The file is stored in the MIDlet suite JAR file.
        - Audio Capture from a default device lets you capture audio from a microphone 
          or connected device. The sound is captured and played back on the speaker. 
          To avoid feedback, use a headset.
        - Video Capture Simulation simulates viewing input video such as might be 
          possible on a device equipped with a camera.
        - MPEG1 Video [http]. Plays an MPEG video found at http://java.sun.com/products/java-media/mma/media/test-mpeg.mpg.
        - [enter URL] allows you to play back media files from arbitrary HTTP servers. 
          Type a valid URL (for example, http://java.sun.com/products/java-media/mma/media/test-wav.mpg) 
          at the insertion point and click OK to play a file. If you want to open 
          an HTTP directory from which to select media, be sure to add a slash to 
          the end of the URL.

        In addition, Simple Player parses ring tones in Ringing Tones text transfer 
        language (RTTTL). See http://www.convertyourtone.com/rtttl.html for information on RTTTL. 
        
    2.3 Video
        The Video application illustrates how the emulator is capable of playing 
        animated GIF files and capturing video. On a real device with a camera, 
        video capture can be used to show the user what the camera sees.

        Animated GIFs and video capture can be implemented using either a Form 
        Item or a Canvas. The Video demonstration includes all the possibilities. 
        Animated GIF - Form [jar] shows an animated GIF as a Form Item. The form also 
        includes some information about the playback, including the current time. 
        Choose the Snapshot command to take a snapshot of the running animation. 
        The snapshot will be placed in the form following the animated GIF.
        - Animated GIF - Canvas [jar] shows an animated GIF in a Canvas. A simple 
          indicator shows the progress through the animation. Choose Snapshot to 
          get a still image of the current appearance. The snapshot is shown briefly, 
          then the display goes back to the animation.
        - Video Capture - Form simulates capturing video from a camera or other 
          source and showing it as an Item in a Form. Choose the Snapshot command 
          to take a snapshot of the captured video. The snapshot will be placed in 
          the form following the video capture.
        - Video Capture - Canvas simulates capturing video from a camera or other 
          source and showing it in a Canvas. Choose Snapshot to get a still image 
          of the current appearance. The snapshot is shown briefly, then the display 
          goes back to the video capture.
        - MPEG1 Video - Form, MPEG1 Video - Canvas
          When you play the demo, expect to wait a few seconds while WTK obtains 
          the data. The MPEG1 demos have the same behavior as Video Capture - Form 
          and Video Capture - Canvas, respectively. 
          
    2.4 Pausing Audio Test
        This MIDlet exists to demonstrate how the Sun JavaTM Wireless Toolkit 
        for CLDC will warn you if a paused MIDlet has not stopped its running Players. 
        After you launch the MIDlet, choose the Play command to start playing some audio. 
        The screen displays a status, which is either "Well-behaved" or "Not Well-Behaved."
        Choose MIDlet > Pause from the emulator window's menu. As expected, the MIDlet 
        is paused and no message is displayed on the console. 
        Choose MIDlet > Resume from the emulator window's menu.
        Now choose the Misbehave command. Pause the MIDlet again. In console, 
        you see the warning: An active media (subtype Player) resource was 
        detected while the MIDlet is paused. Well-behaved MIDlets release their 
        resources in pauseApp(). 
        
    2.5 Attributes for MobileMediaAPI
        The MobileMediaAPI applications have some attributes that you can modify 
        in the project settings dialog box User Defined tab.

3. Required APIs
    
    JSR 30 - Connected Limited Device Configuration (CLDC) 1.0
    JSR 118 - Mobile Information Device Profile (MIDP) 2.0
    JSR 135 - Mobile Media API (MMAPI)
