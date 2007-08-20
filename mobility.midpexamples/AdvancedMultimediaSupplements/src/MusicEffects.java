/*
 * Copyright 2007 Sun Microsystems, Inc. All rights reserved.
 * Use is subject to license terms.
 */

import java.io.*;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import javax.microedition.media.*;
import javax.microedition.media.control.*;
import javax.microedition.amms.*;
import javax.microedition.amms.control.*;
import javax.microedition.amms.control.audioeffect.*;

public class MusicEffects extends MIDlet {

    // UI stuff {

    private final static int MAX_GAUGE_VALUE = 25;

    // Commands and listeners

    Command exitCommand = new Command("Exit", Command.EXIT, 1);
    Command setupItemCommand = new Command("Setup", Command.ITEM, 2);
    Command finishSetupCommand = new Command("Done", Command.BACK, 1);

    public class ExitCommandListener implements CommandListener {
        public void commandAction(Command c, Displayable d) {
            if (c == null)
                return;
            if (c == exitCommand) {
                destroyApp(true);
                notifyDestroyed();
            }
        }
    }

    public class SetupItemCommandListener implements ItemCommandListener {

        private Form setupForm;    

        public SetupItemCommandListener(Form form) {
            setupForm = form;
        }

        public void commandAction(Command c, Item item) {
            if (c == null)
                return;
            if (c == setupItemCommand)
                Display.getDisplay(MusicEffects.this).setCurrent(setupForm);
        }
    }

    public class FinishSetupCommandListener implements CommandListener {
        public void commandAction(Command c, Displayable d) {
            if (c == null)
                return;
            if (c == finishSetupCommand) {
                Display.getDisplay(MusicEffects.this).setCurrent(mainForm);
                mainForm.updateControls(equalizerForm.getMode());
            }
        }
    }

    FinishSetupCommandListener finishSetupCommandListener = new FinishSetupCommandListener();

    
    // equalizerForm stuff

    public class EqualizerForm extends Form implements ItemStateListener {

        public final static int MODE_BASS_TREBLE = 0;
        public final static int MODE_PRESET = 1;
        public final static int MODE_BANDS = 2;

        int mode = MODE_BASS_TREBLE;
        
        ChoiceGroup modeChoice = new ChoiceGroup("Equalizer mode", Choice.EXCLUSIVE, new String[] {"Simple (bass/treble)", "Preset", "Custom"}, null);
        ChoiceGroup presetChoice = new ChoiceGroup("Preset", Choice.EXCLUSIVE);
        Gauge [] bandGauges;

        int minLevel = -1;
        int maxLevel = 1;

        public EqualizerForm() {
            super("Advanced Equalizer Setup");

            if (equalizerControl != null) {
                String [] presets = equalizerControl.getPresetNames();
                if (presets != null && presets.length != 0)
                    for (int i = 0; i < presets.length; ++i)
                        presetChoice.append(presets[i], null);
                else
                    presetChoice.setLabel("No Presets...");

                minLevel = equalizerControl.getMinBandLevel();
                maxLevel = equalizerControl.getMaxBandLevel();
                if (maxLevel <= minLevel)
                    maxLevel = minLevel + 1;
                
                int num = equalizerControl.getNumberOfBands();

                bandGauges = new Gauge [num];

                for (int i = 0; i < num; ++i) {
                    int level = (equalizerControl.getBandLevel(i) - minLevel) * MAX_GAUGE_VALUE / (maxLevel - minLevel);
                    bandGauges[i] = new Gauge(String.valueOf(equalizerControl.getCenterFreq(i) / 1000) + " hz", true, MAX_GAUGE_VALUE, level);
                    bandGauges[i].setLayout(Item.LAYOUT_EXPAND);
                }
            }

            append(modeChoice);
            
            setItemStateListener(this);
            addCommand(finishSetupCommand);
            setCommandListener(finishSetupCommandListener);
        }

        public int getMode() { return mode; }

        public void setMode(int newMode) {
            if (mode == newMode)
                return;
            
            deleteAll();

            append(modeChoice);

            switch (newMode) {
            case MODE_BASS_TREBLE: break;
            case MODE_PRESET: append(presetChoice); break;
            case MODE_BANDS:
               for (int i = 0; i < bandGauges.length; ++i)
                   append(bandGauges[i]);
               break;
            }
            
            mode = newMode;
        }

        public void itemStateChanged(Item item) {
            if (item == null)
                return;
            else if (item == modeChoice)
                setMode(modeChoice.getSelectedIndex());
            else if (item == presetChoice)
                equalizerControl.setPreset(presetChoice.getString(presetChoice.getSelectedIndex()));
            else
                for (int i = 0; i < bandGauges.length; ++i)
                    if (item == bandGauges[i]) {
                        equalizerControl.setBandLevel(minLevel + bandGauges[i].getValue() * (maxLevel - minLevel) / MAX_GAUGE_VALUE, i);
                        break;
                    }
        }
    }


    // reverbForm stuff

    public class ReverbForm extends Form implements ItemStateListener {
        public final static int MIN_TIME = 1;
        public final static int MAX_TIME = 10000;
        public final static int MIN_LEVEL = -1200;
        public final static int MAX_LEVEL = 0;

        Gauge timeGauge = new Gauge("Time", true, MAX_GAUGE_VALUE, 0);
        Gauge levelGauge = new Gauge("Level", true, MAX_GAUGE_VALUE, 0);
        Gauge roomLevelGauge = new Gauge("Room level", true, MAX_GAUGE_VALUE, 0);
        ChoiceGroup presetChoice = new ChoiceGroup("Reverb Preset", Choice.EXCLUSIVE);

        public ReverbForm() {
            super("Reverb Setup");

            timeGauge.setLayout(Item.LAYOUT_EXPAND);
            levelGauge.setLayout(Item.LAYOUT_EXPAND);
            roomLevelGauge.setLayout(Item.LAYOUT_EXPAND);

            try {
                timeGauge .setValue((reverbControl.getReverbTime() - MIN_TIME ) * MAX_GAUGE_VALUE / (MAX_TIME - MIN_TIME ));
                levelGauge.setValue((reverbControl.getReverbLevel() - MIN_LEVEL) * MAX_GAUGE_VALUE / (MAX_LEVEL - MIN_LEVEL));
                roomLevelGauge.setValue((reverbSourceControl.getRoomLevel() - MIN_LEVEL) * MAX_GAUGE_VALUE / (MAX_LEVEL - MIN_LEVEL));
            } catch (Exception e) {}

            presetChoice.append("TURN OFF", null);

            String preset = reverbControl.getPreset();
            String [] presets = reverbControl.getPresetNames();
            for (int i = 0; i < presets.length; ++i) {
                presetChoice.append(presets[i], null);
                if (presets[i].equals(preset) && reverbControl.isEnabled())
                    presetChoice.setSelectedIndex(i + 1, true);
            }

            append(timeGauge);
            append(levelGauge);
            append(roomLevelGauge);
            append(presetChoice);

            setItemStateListener(this);
            addCommand(finishSetupCommand);
            setCommandListener(finishSetupCommandListener);
        }

        public void itemStateChanged(Item item) {
            if (item == null)
                return;
            else if (item == timeGauge)
                try {
                    reverbControl.setReverbTime(MIN_TIME + timeGauge.getValue() * (MAX_TIME - MIN_TIME ) / MAX_GAUGE_VALUE);
                } catch (Exception e) {
                    System.out.println(e);
                }
            else if (item == levelGauge)
                try {
                    reverbControl.setReverbLevel(MIN_LEVEL + levelGauge.getValue() * (MAX_LEVEL - MIN_LEVEL) / MAX_GAUGE_VALUE);
                } catch (Exception e) {
                    System.out.println(e);
                }
            else if (item == roomLevelGauge)
                try {
                    reverbSourceControl.setRoomLevel(MIN_LEVEL + roomLevelGauge.getValue() * (MAX_LEVEL - MIN_LEVEL) / MAX_GAUGE_VALUE);
                } catch (Exception e) {
                    System.out.println(e);
                }
            else if (item == presetChoice) {
                if (0 == presetChoice.getSelectedIndex())
                    reverbControl.setEnabled(false);
                else {
                    if (!reverbControl.isEnabled())
                        reverbControl.setEnabled(true);
                    reverbControl.setPreset(presetChoice.getString(presetChoice.getSelectedIndex()));
                }
            }
        }
    }


    // chorusForm stuff

    public class ChorusForm extends Form implements ItemStateListener {

        ChoiceGroup presetChoice = new ChoiceGroup("Chorus mode", Choice.EXCLUSIVE);
        Gauge wetLevelGauge = new Gauge("Wet level", true, MAX_GAUGE_VALUE, 0);
        Gauge modulationRateGauge = new Gauge("Modulation rate", true, MAX_GAUGE_VALUE, 0);
        Gauge modulationDepthGauge = new Gauge("Modulation depth", true, MAX_GAUGE_VALUE, 0);
        Gauge averageDelayGauge = new Gauge("Average delay", true, MAX_GAUGE_VALUE, 0);

        int minModulationRate = 0;
        int maxModulationRate = 1000;
        int maxModulationDepth = 100;
        int maxAverageDelay = 1000;

        public ChorusForm() {
            super("Chorus Setup");

            minModulationRate  = chorusControl.getMinModulationRate();
            maxModulationRate  = chorusControl.getMaxModulationRate();
            maxModulationDepth = chorusControl.getMaxModulationDepth();
            maxAverageDelay    = chorusControl.getMaxAverageDelay();

            presetChoice.append("OFF", null);
            String [] presets = chorusControl.getPresetNames();
            if (presets != null && presets.length != 0) {
                String preset = chorusControl.getPreset();
                for (int i = 0; i < presets.length; ++i) {
                    presetChoice.append(presets[i], null);
                    if (presets[i].equals(preset))
                        presetChoice.setSelectedIndex(i + 1, true);
                }
            }
            append(presetChoice);

            updateControls();

            wetLevelGauge       .setLayout(Item.LAYOUT_EXPAND);
            modulationRateGauge .setLayout(Item.LAYOUT_EXPAND);
            modulationDepthGauge.setLayout(Item.LAYOUT_EXPAND);
            averageDelayGauge   .setLayout(Item.LAYOUT_EXPAND);

            append(wetLevelGauge);
            append(modulationRateGauge);
            append(modulationDepthGauge);
            append(averageDelayGauge);

            setItemStateListener(this);
            addCommand(finishSetupCommand);
            setCommandListener(finishSetupCommandListener);

        }

        public void itemStateChanged(Item item) {
            if (item == null)
                return;
            else if (item == wetLevelGauge)
                try {
                    chorusControl.setWetLevel(wetLevelGauge.getValue() * 100 / MAX_GAUGE_VALUE);
                } catch (Exception e) {
                    System.out.println(e);
                }
            else if (item == modulationRateGauge)
                try {
                    //System.out.println("mr=" + (minModulationRate + modulationRateGauge.getValue() * (maxModulationRate - minModulationRate) / MAX_GAUGE_VALUE));
                    chorusControl.setModulationRate(minModulationRate + modulationRateGauge.getValue() * (maxModulationRate - minModulationRate) / MAX_GAUGE_VALUE);
                } catch (Exception e) {
                    System.out.println(e);
                }
            else if (item == modulationDepthGauge)
                try {
                    chorusControl.setModulationDepth(modulationDepthGauge.getValue() * maxModulationDepth / MAX_GAUGE_VALUE);
                } catch (Exception e) {
                    System.out.println(e);
                }
            else if (item == averageDelayGauge)
                try {
                    chorusControl.setAverageDelay(averageDelayGauge.getValue() * maxAverageDelay / MAX_GAUGE_VALUE);
                } catch (Exception e) {
                    System.out.println(e);
                }
            else if (item == presetChoice) {
                if (0 == presetChoice.getSelectedIndex())
                    chorusControl.setEnabled(false);
                else {
                    if (!chorusControl.isEnabled())
                        chorusControl.setEnabled(true);
                    chorusControl.setPreset(presetChoice.getString(presetChoice.getSelectedIndex()));
                }
                updateControls();
            }
        }

        public void updateControls() {
            //System.out.println("Wet level = " + chorusControl.getWetLevel());
            //System.out.println("Modulation rate (" + minModulationRate + " to " + maxModulationRate + ") = " + chorusControl.getModulationRate());
            //System.out.println("Modulation depth = " + chorusControl.getModulationDepth());
            //System.out.println("Average delay = " + chorusControl.getAverageDelay());
            wetLevelGauge       .setValue(chorusControl.getWetLevel() * MAX_GAUGE_VALUE / 100);
            modulationRateGauge .setValue((chorusControl.getModulationRate() - minModulationRate) * MAX_GAUGE_VALUE / (maxModulationRate - minModulationRate));
            modulationDepthGauge.setValue(chorusControl.getModulationDepth() * MAX_GAUGE_VALUE / maxModulationDepth);
            averageDelayGauge   .setValue(chorusControl.getAverageDelay() * MAX_GAUGE_VALUE / maxAverageDelay);
        }

    }

    
    // mainForm stuff
    
    public class MainForm extends Form implements ItemStateListener {

        //ChoiceGroup playersChoice = new ChoiceGroup("Music selector", Choice.MULTIPLE);
        final static int BASS_CONTROL_NUM = 2;

        Gauge volumeGauge = new Gauge("Volume", true, MAX_GAUGE_VALUE, MAX_GAUGE_VALUE);
        Gauge panGauge    = new Gauge("Pan",    true, MAX_GAUGE_VALUE, MAX_GAUGE_VALUE / 2);
        Gauge bassGauge   = new Gauge("Bass",   true, MAX_GAUGE_VALUE, 0);
        Gauge trebleGauge = new Gauge("Treble", true, MAX_GAUGE_VALUE, 0);
        StringItem equalizerButton = new StringItem("Advanced equalizer setup", null, Item.BUTTON);
        StringItem reverbButton    = new StringItem("Setup reverberation", null, Item.BUTTON);
        StringItem chorusButton    = new StringItem("Setup chorus", null, Item.BUTTON);
        ChoiceGroup audioVirtualizerChoice = new ChoiceGroup("Audio virtualizer preset", Choice.EXCLUSIVE);

        public MainForm() {
            super("Advanced Music Player");

            volumeGauge.setLayout(Item.LAYOUT_EXPAND);
            panGauge   .setLayout(Item.LAYOUT_EXPAND);
            bassGauge  .setLayout(Item.LAYOUT_EXPAND);
            trebleGauge.setLayout(Item.LAYOUT_EXPAND);
            equalizerButton.setLayout(Item.LAYOUT_CENTER);
            reverbButton   .setLayout(Item.LAYOUT_CENTER);
            chorusButton   .setLayout(Item.LAYOUT_CENTER);

            equalizerButton.setDefaultCommand(setupItemCommand);
            reverbButton   .setDefaultCommand(setupItemCommand);
            chorusButton   .setDefaultCommand(setupItemCommand);
            equalizerButton.setItemCommandListener(new SetupItemCommandListener(equalizerForm));
            reverbButton   .setItemCommandListener(new SetupItemCommandListener(reverbForm));
            chorusButton   .setItemCommandListener(new SetupItemCommandListener(chorusForm));

            //playersChoice.append("mono loop (Barretto)", null);
            //playersChoice.append("stereo sample (Dixieland)", null);
            //playersChoice.setSelectedIndex(0, play);
            //playersChoice.setSelectedIndex(1, playStereo);
            
            audioVirtualizerChoice.append("OFF", null);
            String [] avPresets = audioVirtualizerControl.getPresetNames();
            if (avPresets != null && avPresets.length != 0)
                for (int i = 0 ; i < avPresets.length; ++i)
                    audioVirtualizerChoice.append(avPresets[i], null);
            if (avPresets.length == 0)
                audioVirtualizerChoice.append("ON", null);

            //append(playersChoice);
            append(volumeGauge);
            append(panGauge);
            append(bassGauge);
            append(trebleGauge);
            append(equalizerButton);
            append(reverbButton);
            append(chorusButton);
            append(audioVirtualizerChoice);

            setItemStateListener(this);
            addCommand(exitCommand);
            setCommandListener(new ExitCommandListener());
        }

        public void itemStateChanged(Item item) {
            if (item == null)
                return;
           /* else if (item == playersChoice) {
                if (play != playersChoice.isSelected(0)) {
                    try {
                        if (play)
                            player.stop();
                        else
                            player.start();
                    } catch (MediaException e) {}
                    play = !play;
                }

                if (playStereo != playersChoice.isSelected(1)) {
                    try {
                        if (playStereo)
                            stereoPlayer.stop();
                        else
                            stereoPlayer.start();
                    } catch (MediaException e) {}
                    playStereo = !playStereo;
                }
            }*/
            else if (item == volumeGauge)
                volumeControl.setLevel(volumeGauge.getValue() * 100 / volumeGauge.getMaxValue());
            else if (item == panGauge)
                panControl.setPan(panGauge.getValue() * 200 / panGauge.getMaxValue() - 100);
            else if (item == bassGauge)
                equalizerControl.setBass(bassGauge.getValue() * 100 / bassGauge.getMaxValue());
            else if (item == trebleGauge)
                equalizerControl.setTreble(trebleGauge.getValue() * 100 / bassGauge.getMaxValue());
            else if (item == audioVirtualizerChoice)
                if (0 == audioVirtualizerChoice.getSelectedIndex())
                    audioVirtualizerControl.setEnabled(false);
                else {
                    if (!audioVirtualizerControl.isEnabled())
                        audioVirtualizerControl.setEnabled(true);
                    String preset = audioVirtualizerChoice.getString(audioVirtualizerChoice.getSelectedIndex());
                    if (!preset.equals("ON"))
                        audioVirtualizerControl.setPreset(preset);
                }
        }

        public void updateControls(int equalizerMode) {
            volumeGauge.setValue(volumeControl.getLevel() * volumeGauge.getMaxValue() / 100);
            panGauge.setValue((100 + panControl.getPan()) * panGauge.getMaxValue() / 200);

            if (equalizerMode == EqualizerForm.MODE_BASS_TREBLE) {
                if (get(BASS_CONTROL_NUM) != bassGauge) {
                    insert(BASS_CONTROL_NUM, bassGauge);
                    insert(BASS_CONTROL_NUM + 1, trebleGauge);
                }
                bassGauge  .setValue(equalizerControl.getBass  () *   bassGauge.getMaxValue() / 100);
                trebleGauge.setValue(equalizerControl.getTreble() * trebleGauge.getMaxValue() / 100);
            } else {
                if (get(BASS_CONTROL_NUM) == bassGauge) {
                    delete(BASS_CONTROL_NUM + 1);
                    delete(BASS_CONTROL_NUM);
                }
            }
        }
    }

    
    EqualizerForm equalizerForm;
    ReverbForm reverbForm;
    ChorusForm chorusForm;
    MainForm mainForm;

    // } UI stuff

    
    // Media stuff

    boolean play = true;
    //boolean playStereo = false;

    Player player;
    //Player stereoPlayer;
    VolumeControl volumeControl;
    PanControl panControl;
    EqualizerControl equalizerControl;
    ReverbControl reverbControl;
    ReverbSourceControl reverbSourceControl;
    ChorusControl chorusControl;
    AudioVirtualizerControl audioVirtualizerControl;

    // MIDlet implementation
    
    public MusicEffects() {
        try {
            String [] s = Manager.getSupportedContentTypes(null);
            System.out.println("Supported audio types:");
            for (int i = 0; i < s.length; i++)
                if (s[i].startsWith("audio"))
                    System.out.println("  " + s[i]);

            System.out.println("Supported sample rates: " + System.getProperty("audio.samplerates"));

            System.out.println("Creating WAV player...");
            player = Manager.createPlayer(getClass().getResourceAsStream("music.wav"), "audio/x-wav");
            player.setLoopCount(-1);

            //System.out.println("Creating WAV stereo player...");
            //stereoPlayer = Manager.createPlayer(getClass().getResourceAsStream("stereomusic.wav"), "audio/x-wav");
            //stereoPlayer.setLoopCount(-1);

            EffectModule effectModule = GlobalManager.createEffectModule();
            effectModule.addPlayer(player);
            //effectModule.addPlayer(stereoPlayer);

            player.realize();
            //stereoPlayer.realize();

            System.out.println("GlobalManager controls:");
            printArray(GlobalManager.getControls());

            System.out.println("Player controls:");
            printArray(player.getControls());

            System.out.println("EffectModule controls:");
            printArray(effectModule.getControls());

            System.out.println("Creating audio controls...");
            volumeControl = (VolumeControl)GlobalManager.getControl("javax.microedition.media.control.VolumeControl");

            panControl = (PanControl)effectModule.getControl("javax.microedition.amms.control.PanControl");

            equalizerControl = (EqualizerControl)effectModule.getControl("javax.microedition.amms.control.audioeffect.EqualizerControl");
            equalizerControl.setEnabled(true);

            //System.out.println("Creating reverb control...");
            reverbControl = (ReverbControl)GlobalManager.getControl("javax.microedition.amms.control.audioeffect.ReverbControl");
            
            //System.out.println("Creating reverb source control...");
            reverbSourceControl = (ReverbSourceControl)effectModule.getControl("javax.microedition.amms.control.audioeffect.ReverbSourceControl");

            chorusControl = (ChorusControl)effectModule.getControl("javax.microedition.amms.control.audioeffect.ChorusControl");

            audioVirtualizerControl = (AudioVirtualizerControl)GlobalManager.getControl("javax.microedition.amms.control.audioeffect.AudioVirtualizerControl");
            
            System.out.println("Creating equalizerForm...");
            equalizerForm = new EqualizerForm();
            System.out.println("Creating reverbForm...");
            reverbForm = new ReverbForm();
            System.out.println("Creating chorusForm...");
            chorusForm = new ChorusForm();
            System.out.println("Creating mainForm...");
            mainForm = new MainForm();            
            mainForm.updateControls(EqualizerForm.MODE_BASS_TREBLE);
            System.out.println("Init done!");

        } catch (Exception e)
        {
            e.printStackTrace();
            System.out.println(e);
        }
    }
    
    public void startApp() {
        try {
            Display.getDisplay(this).setCurrent(mainForm);
            if (play)
                player.start();
            //if (playStereo)
            //    stereoPlayer.start();
        } catch (MediaException e) {}
    }

    public void pauseApp() {
        System.out.println("About to pauseApp...");
        try {
            player.stop();
            //stereoPlayer.stop();
        } catch (MediaException e) {}
    }

    public void destroyApp(boolean unconditional) {
        player.close();
        //stereoPlayer.close();
    }

    protected final static void printArray(Object [] s) {
        if (s == null || s.length == 0)
            System.out.println("  (empty)");
        for (int i = 0; i < s.length; ++i)
            System.out.println("  " + s[i]);
   }
} 
