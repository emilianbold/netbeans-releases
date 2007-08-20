/*
 * Copyright 2007 Sun Microsystems, Inc. All rights reserved.
 * Use is subject to license terms.
 */
package example.mmademo;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import javax.microedition.media.*;
import javax.microedition.media.control.*;

/**
 * An example MIDlet to demonstrate simple tones: Manager.playTone(), MIDIControl
 *
 * @version 1.3
 */
public class SimpleTones extends BaseListMidlet implements Utils.BreadCrumbTrail {

    private static final boolean USE_LONG_MIDI = false;

    // cache MIDIPlayer so that we don't open/close all the time
    private Player mp;

    public SimpleTones() {
	super("MMAPI Simple Tones");
    }

    protected void fillList(List list) {
	list.append("Short Single Tone", null);
	list.append("Long Single Tone", null);
	list.append("Short MIDI event", null);
	if (USE_LONG_MIDI) {
	    list.append("Long MIDI event", null);
	}
	list.append("MMAPI Drummer", null);
	list.addCommand(exitCommand);
	list.addCommand(playCommand);
    }

    protected void selectCommand(int index) {
	switch (index) {
	case 0:
	    simpleTone(ToneControl.C4, 100);
	    break;
	case 1:
	    simpleTone(ToneControl.C4 + 4, 1000);
	    break;
	case 2:
	    midiShort();
	    break;
	case 3:
	    if (USE_LONG_MIDI) {
	    	midiLong();
		break;
	    }
	    /* fall through */
	case 4:
	    drummer();
	    break;
	}
    }

    public void destroyApp(boolean unconditional) {
	if (mp != null) {
	    mp.close();
	    mp = null;
	}
    }

    private void simpleTone(int note, int duration) {
	try {
	    Manager.playTone(note, duration, 80 /*vol*/);
	} catch (Exception ex){
	    Utils.error(ex, this);
	}
    }

    MIDIControl getMIDIControl() throws Exception {
	if (mp == null) {
	    mp = Manager.createPlayer(Manager.MIDI_DEVICE_LOCATOR);
	    mp.prefetch();
	}
	return (MIDIControl) mp.getControl("javax.microedition.media.control.MIDIControl");
    }

    byte[] niceChord=new byte[] {
	0x2B, 0x40, 0x44, 0x4C, 0x53, 0x58, 0x23, 0x3B, 0x32, 0x1F
    };


    private void midiShort() {
	try {
	    MIDIControl mc = getMIDIControl();
	    // some notes on channel 0
	    // 0x90: Note On
	    for (int i=0; i<niceChord.length; i++) {
		// Note On, note number, velocity
		mc.shortMidiEvent(0x90, niceChord[i], 127);
	    }
	    // some drums on channel 9
	    mc.shortMidiEvent(0x99, 35, 127); // bass drum
	    mc.shortMidiEvent(0x99, 35, 0);
	    mc.shortMidiEvent(0x99, 58, 127); // vibraslap
	    mc.shortMidiEvent(0x99, 58, 0);
	    mc.shortMidiEvent(0x99, 57, 127); // crash cymbal
	    mc.shortMidiEvent(0x99, 57, 0);
	    Thread.sleep(200);
	    // turn off all notes: Note On event with 0 velocity
	    for (int i=0; i<niceChord.length; i++) {
		mc.shortMidiEvent(0x90, niceChord[i], 0);
	    }
	} catch (Exception ex){
	    Utils.error(ex, this);
	}
    }

    private void midiLong() {
	try {
	    MIDIControl mc=getMIDIControl();
	    // send the chord as sys ex event
	    int len=niceChord.length*3; // 3 bytes per event
	    byte[] data=new byte[len];
	    int c=0;
	    for (int i=0; i<len/3; i++) {
		data[c++]=(byte) 0x90;
		data[c++]=niceChord[i % niceChord.length];
		data[c++]=127;
	    }
	    int count = mc.longMidiEvent(data, 0, len);
	    //System.out.println("1. longEvent returned "+count);
	    Thread.sleep(200);
	    // replace the velocity by 0
	    for (int i=2; i<len; i+=3) {
		data[i]=0;
	    }
	    count = mc.longMidiEvent(data, 0, len);
	    //System.out.println("2. longEvent returned "+count);
	} catch (Exception ex){
	    Utils.error(ex, this);
	}
    }

    private synchronized void drummer() {
    	DrummerCanvas dc = new DrummerCanvas(this, this);
    	go(dc);
    	dc.show();
    }

    public void handle(String name, String url) {
	throw new RuntimeException("SimpleTones.handle() must not be called");
    }

}
