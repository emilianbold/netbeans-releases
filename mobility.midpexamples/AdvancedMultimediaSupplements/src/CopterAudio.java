/*
 * Copyright 2007 Sun Microsystems, Inc. All rights reserved.
 * Use is subject to license terms.
 */

import java.util.*;
import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;

public class CopterAudio extends MIDlet implements CommandListener
{
	Display disp;
        CopterCanvas canvas;
        CopterAudioScene audio_sc;

        Timer timer;
        Mover mover;

//        ComLsnr cmLsnr;

        final int framesPerSecond = 5;
        final int timeToCrossScreen = 9; // in seconds

        final int halfSqrt2x1000 = 707; // sqrt(2)/2*1000

        Command cmdExit;
        Command cmdVolume;
        Command cmdPan;
        Command cmdDoppler;
        Command cmdLocation;
        Command cmdCopterSize;
        Command cmdOrientation;
        Command cmdDirect;
        Command cmdObstr;

        Form formVolume;
        CopterMeterGauge cmggVolume;

        Form formPan;
        CopterGauge cggPan;

        Form formDoppler;
        ChoiceGroup cgDoppler;
        CopterMeterGauge cmggExplicitVel;
        boolean _isDopplerVImplicit;
        
        Form formCopterSize;
        CopterMeterGauge cmggLength;
        CopterMeterGauge cmggWidth;
        CopterMeterGauge cmggHeight;

        Form formLocation;
        CopterMeterGauge cmggScale;
        CopterMeterGauge cmggAltitude;
        
        Form formOrientation;
        ChoiceGroup cgOrienPresets;
        
        Form formDirectivity;
        CopterMeterGauge cmggMinAngle;
        CopterMeterGauge cmggMaxAngle;
        CopterMeterGauge cmggRearLevel;
        
        CopterDistAttForm formDistAtt;
        
        Form formObstr;
        ChoiceGroup cgWall;
        
        public CopterStateListener stateLsnr;

        public CopterAudio() {
                disp = Display.getDisplay(this);
                mover = new Mover();
                timer = new Timer();

                initCommands();

                initCanvas();

                audio_sc = new CopterAudioScene( 
                    getStepScale( 30 ), // consider screen width to be 30 m
                    1000, // 1 metre altitude 
                    calcVelFactor( getStepScale( 30 ) ) 
                    // calculate the velocity factor (for Doppler Effect)
                    // from the scale set through the 1st parameter
                    );

                stateLsnr = new CopterStateListener();

                initVolumeForm();

                initPanForm();

                initLocationForm();

                initDopplerForm();
                
                initCopterSizeForm();
                
                initOrientForm();
                
                initDirectForm();
                
                formDistAtt = new CopterDistAttForm( 
                        "Distance Attenuation settings", cmdExit, this, 
                        stateLsnr, canvas, audio_sc );
                
                initObstrForm();
                
        }

	public void startApp() throws MIDletStateChangeException
        {
            disp.setCurrent( canvas );
            if (timer == null) {
                mover = new Mover();
                timer = new Timer();
            }
            timer.schedule( mover, getFramePeriod(), getFramePeriod() );
    	}
    
    	public void pauseApp()
        {
            timer.cancel();
            mover.cancel();
            timer = null;
            mover = null;
   	}

    	public void destroyApp(boolean unconditional) throws MIDletStateChangeException {

            cleanup();
    	}

        void initCommands()
        {
            cmdExit = new Command( "Exit", Command.EXIT, 1);
            cmdVolume = new Command( "Volume", Command.ITEM, 2);
            cmdPan = new Command( "Pan", Command.ITEM, 2);
            cmdDoppler = new Command ( "Doppler Effect", Command.ITEM, 2);
            cmdLocation = new Command ( "Location settings", Command.ITEM, 2);
            cmdCopterSize = new Command ( "Helicopter size", Command.ITEM, 2 );
            cmdOrientation = new Command ( "Spectator orientation", 
                    Command.ITEM, 2 );
            cmdDirect = new Command ( 
                    "Directivity of the sound",
                    Command.ITEM, 2);
            cmdObstr = new Command( 
                    "Sound obstruction settings", 
                    Command.ITEM, 2);
        }

        void initCanvas()
        {
            canvas = new CopterCanvas( getNStepsHorizontal() );
            canvas.addCommand( cmdExit );
            canvas.addCommand( cmdVolume );
            canvas.addCommand( cmdPan );
            canvas.addCommand( cmdLocation );
            canvas.addCommand( cmdDoppler );
            canvas.addCommand( cmdCopterSize );
            canvas.addCommand( cmdOrientation );
            canvas.addCommand( cmdDirect );
            canvas.addCommand( cmdObstr );
            canvas.setCommandListener( this );
        }

        Form getNewForm( String title )
        {
            Form f = new Form( title );
            f.addCommand( cmdExit );
            f.setItemStateListener( stateLsnr );
            f.setCommandListener( this );
            return f;
        }
        
        void initObstrForm()
        {
            formObstr = getNewForm( "Obstruction for the sound" );
            cgWall = new ChoiceGroup( null, Choice.EXCLUSIVE );
            cgWall.append(
                    "There is no obstruction", 
                    null );
            cgWall.append( "Obstruction is a single window", null );
            cgWall.append( "Obstruction is a double window", null );
            cgWall.append( "Obstruction is a thin door", null );
            cgWall.append( "Obstruction is a thick door", null );
            cgWall.append( "Obstruction is a wood wall", null );
            cgWall.append( "Obstruction is a brick wall", null );
            cgWall.append( "Obstruction is a stone wall", null );
            cgWall.append( "Obstruction is a curtain", null );
            cgWall.setSelectedIndex(0, true );
            formObstr.append( cgWall );

        }

        void initVolumeForm()
        {
            formVolume = getNewForm( null );

            cmggVolume = new CopterMeterGauge( formVolume, 0, 100, 
                                     audio_sc.getVolume(), 4, "Volume", "" );
        }

        void initPanForm()
        {
            formPan = getNewForm( null );
            cggPan = new CopterGauge( formPan, -100, 100, audio_sc.getPan(), 
                    8, "Pan" );

            StringItem siL = new StringItem( null, "Left" );
            StringItem siR = new StringItem( null, "Right" );

            formPan.append( siL );
            formPan.append( siR );

            formPan.insert( 2, new Spacer( formPan.getWidth() - 
                                      siL.getPreferredWidth() - 
                                      siR.getPreferredWidth(),
                                      siL.getMinimumHeight() ) );
            formPan.insert( 0, new Spacer( formPan.getWidth(),
                                           (formPan.getHeight() - 
                                           cggPan.getHeight() - 
                                           siR.getPreferredHeight()) / 2 ) );
        }

        void initLocationForm()
        {
            formLocation = getNewForm( "Location settings" );
            cmggScale = new CopterMeterGauge( formLocation, 
                                              30, 
                                              280, 
                                              getScale(), 
                                              10, 
                                              "Screen Width is", 
                                              "meters" );
            cmggAltitude = new CopterMeterGauge( formLocation,
                                                 0,
                                                 50,
                                                 getAltitude(),
                                                 2,
                                                 "Flight Altitude is",
                                                 "meters" );
        }

        void initDopplerForm()
        {
            _isDopplerVImplicit = true;

            formDoppler = getNewForm( "Doppler Effect settings" );
            cgDoppler = new ChoiceGroup( null, Choice.MULTIPLE );
            cgDoppler.append( "Doppler Effect is enabled", null );
            cgDoppler.setSelectedIndex( 0, audio_sc.isDopplerEnabled() );
            if( audio_sc.isDopplerEnabled() )
            {
                cgDoppler.append( "Velocity is calculated from the location", null );
                cgDoppler.setSelectedIndex( 1, _isDopplerVImplicit );
            }

            formDoppler.append( cgDoppler );

            cmggExplicitVel = new CopterMeterGauge( formDoppler,
                                                 1,
                                                 51,
                                                 getDiagVelocity(),
                                                 2,
                                                 "Linear speed of the Helicopter is", "mps" );
            if ( _isDopplerVImplicit ) {
                cmggExplicitVel.deleteFrom( formDoppler );
            }
        }
        
        void initCopterSizeForm()
        {
            formCopterSize = getNewForm( "Helicopter size settings"  );
            cmggLength = new CopterMeterGauge( formCopterSize, 
                    0, 25, audio_sc.getCopterLength(), 1, "Length", "m" );
            cmggWidth = new CopterMeterGauge( formCopterSize, 
                    0, 25, audio_sc.getCopterWidth(), 1, "Width", "m" );
            cmggHeight = new CopterMeterGauge( formCopterSize, 
                    0, 25, audio_sc.getCopterHeight(), 1, "Height", "m" );
        }
        
        void initOrientForm()
        {
            formOrientation = getNewForm( "Spectator 3D orientation" );
            cgOrienPresets = new ChoiceGroup( null, Choice.EXCLUSIVE );
            cgOrienPresets.append( "You're standing straight", null );
            cgOrienPresets.append( "You've fallen on your right side", null );
            cgOrienPresets.append( "You've fallen on your left side", null );
            cgOrienPresets.append( "You've fallen facedown", null );
            cgOrienPresets.append( "You've fallen flat on your back", null );
            formOrientation.append( cgOrienPresets );
        }
        
        void initDirectForm()
        {
            formDirectivity = getNewForm(
                    "Directivity of the sound settings" );
            cmggMinAngle = new CopterMeterGauge( formDirectivity,
                    0, 360, audio_sc.getMinAngle() , 15, 
                    "Fore spatial angle where the sound level is maximum",
                    "degrees" );
            cmggMaxAngle = new CopterMeterGauge( formDirectivity,
                    0, 360, audio_sc.getMaxAngle(), 15, 
           "Fore spatial angle where the level is greater than the rear level", 
                    "degrees");
            cmggRearLevel = new CopterMeterGauge( formDirectivity,
                    -10000, 0, audio_sc.getRearLevel(), 400, 
                    "Rear level ( assuming the fore level 0 dB )",
                    "dB");
        }
        

        int calcVelFactor( int stepScale )
        {
            return stepScale * framesPerSecond;
        }

        int velFac2diagVelocity( int vf )
        {
            return vf / halfSqrt2x1000; // vf * sqrt(2) / 1000;

        }

        int diagVelocity2VelFac( int dv )
        {
            return dv * halfSqrt2x1000; // dv * 1000 / sqrt(2);
        }

        int calcDiagVelocity( int stepScale )
        {
            return velFac2diagVelocity( calcVelFactor( stepScale ) );
        }

        int getDiagVelocity()
        {
            return velFac2diagVelocity( audio_sc.getVelFactor() );
        }
        
        void setVelFacFromLocation()
        {
            audio_sc.setVelFactor( calcVelFactor( audio_sc.getStepScale() ) );
        }

        int getFramePeriod() // in milliseconds
        {
            return 1000 / framesPerSecond;
        }

        int getNStepsHorizontal()
        {
            return timeToCrossScreen * framesPerSecond;
        }

        int getStepScale( int screenWidth /* in meters */ )
        {
            return screenWidth * 1000 / getNStepsHorizontal();
        }

        void setScale( int screenWidth /* in meters */ )
        {
            audio_sc.setStepScale( getStepScale( screenWidth ) );
        }

        // returns the Screen width in meters
        int getScale()
        {
            return audio_sc.getStepScale() * getNStepsHorizontal() / 1000;
        }

        void setAltitude( int a /* in meters */ )
        {
            audio_sc.setAltitude( a * 1000 );
        }

        int getAltitude()
        {
            return audio_sc.getAltitude() / 1000;
        }

        class Mover extends TimerTask
        {
            public void run()
            {
                canvas.nextFrame( disp  );
                audio_sc.nextFrame( canvas );
            }
        }

        void cleanup()
        {
            timer.cancel();
            audio_sc.stop();
        }

        public void commandAction(Command c, Displayable d) {
            if( c == cmdVolume )
                disp.setCurrent( formVolume );
            else if ( c == cmdPan ) {
                disp.setCurrent( formPan );
            }
            else if ( c == cmdLocation ) {
                disp.setCurrent( formLocation );
            }
            else if ( c == cmdDoppler ) {
                disp.setCurrent( formDoppler );
            }
            else if ( c == cmdCopterSize ) {
                disp.setCurrent( formCopterSize );
            }
            else if( c == cmdOrientation ) {
                disp.setCurrent( formOrientation );
            }
            else if( c == cmdDirect ) {
                disp.setCurrent( formDirectivity );
            }
            else if( formDistAtt.isThisCommand(c) ) {
                formDistAtt.setCurrent( disp );
            }
            else if( c == cmdObstr ) {
                disp.setCurrent( formObstr );
            }
            else if ( c == cmdExit ) {
                if( d == canvas )
                {
                    cleanup();
                    notifyDestroyed();
                }
                else
                {
                    disp.setCurrent( canvas );
                }
            }
        }

        public class CopterStateListener implements ItemStateListener {
            public void itemStateChanged(Item item) {
                if ( cmggVolume.isThisItem( item ) ) {
                    audio_sc.setVolume( cmggVolume.getValue() );
                }
                else if ( cggPan.isThisItem( item ) ) {
                    audio_sc.setPan( cggPan.getValue() );
                }
                else if ( item == cgDoppler ) {
                    handleDopplerFlagsChange();
                }
                else if ( cmggExplicitVel.isThisItem( item ) ) {
                    audio_sc.setVelFactor( 
                            diagVelocity2VelFac( 
                            cmggExplicitVel.getValue() ) );
                }
                else if ( cmggScale.isThisItem( item ) ) {
                    setScale( cmggScale.getValue() );
                    if( _isDopplerVImplicit ){
                        setVelFacFromLocation();
                    }
                }
                else if ( cmggAltitude.isThisItem( item ) ) {
                    setAltitude( cmggAltitude.getValue() );
                }
                else if( cmggLength.isThisItem( item ) ) {
                    audio_sc.setCopterLength( cmggLength.getValue() );
                }
                else if( cmggWidth.isThisItem(item) ) {
                    audio_sc.setCopterWidth( cmggWidth.getValue() );
                }
                else if( cmggHeight.isThisItem(item) ) {
                    audio_sc.setCopterHeight( cmggHeight.getValue() );
                }
                else if( item == cgOrienPresets ) {
                    handleOriChoiceChange();
                }
                else if( cmggMinAngle.isThisItem(item) ) {
                    if( cmggMinAngle.getValue() > 
                            cmggMaxAngle.getValue() )
                    {
                        cmggMinAngle.decr();
                    }
                    audio_sc.setMinAngle( cmggMinAngle.getValue() );
                }
                else if( cmggMaxAngle.isThisItem(item) ) {
                    if( cmggMaxAngle.getValue() < 
                            cmggMinAngle.getValue() )
                    {
                        cmggMaxAngle.incr();
                    }
                    audio_sc.setMaxAngle( cmggMaxAngle.getValue() );
                }
                else if( cmggRearLevel.isThisItem(item) ) {
                    audio_sc.setRearLevel( cmggRearLevel.getValue() );
                }
                else if( formDistAtt.isCurrent( disp ) ) {
                    formDistAtt.handleItem( item );
                }
                else if( item == cgWall ) {
                    handleWallChanged();
                }
            }
        }
        
        void handleWallChanged()
        {
                if( cgWall.getSelectedIndex() == 0 ) {
                    canvas.setWall( false );
                    audio_sc.setNoObstr();
                }
                else if( cgWall.getSelectedIndex() == 1 ) {
                    canvas.setWall( true );
                    audio_sc.setSingleWin();
                }
                else if( cgWall.getSelectedIndex() == 2 ) {
                    canvas.setWall( true );
                    audio_sc.setDoubleWin();
                }
                else if( cgWall.getSelectedIndex() == 3 ) {
                    canvas.setWall( true );
                    audio_sc.setThinDoor();
                }
                else if( cgWall.getSelectedIndex() == 4 ) {
                    canvas.setWall( true );
                    audio_sc.setThickDoor();
                }
                else if( cgWall.getSelectedIndex() == 5 ) {
                    canvas.setWall( true );
                    audio_sc.setWoodWall();
                }
                else if( cgWall.getSelectedIndex() == 6 ) {
                    canvas.setWall( true );
                    audio_sc.setBrickWall();
                }
                else if( cgWall.getSelectedIndex() == 7 ) {
                    canvas.setWall( true );
                    audio_sc.setStoneWall();
                }
                else if( cgWall.getSelectedIndex() == 8 ) {
                    canvas.setWall( true );
                    audio_sc.setCurtain();
                }
            
        }

        void handleOriChoiceChange()
        {
            switch ( cgOrienPresets.getSelectedIndex() ) {
                case 0:
                    audio_sc.setStandingStraight();
                    break;
                case 1:
                    audio_sc.setFallenRight();
                    break;
                case 2:
                    audio_sc.setFallenLeft();
                    break;
                case 3:
                    audio_sc.setFallenFacedown();
                    break;
                case 4:
                    audio_sc.setFallenFlatBack();
                    break;
                default:
            }
            
        }
        
        void handleDopplerFlagsChange()
        {
            if( cgDoppler.size() == 1 && cgDoppler.isSelected( 0 ) )
            {
                // the 1st flag was switched on, enabling Doppler
                audio_sc.setDopplerEnabled( true );
                cgDoppler.append( "Velocity is calculated from the location", 
                        null );
                cgDoppler.setSelectedIndex( 1, _isDopplerVImplicit );
                if( !_isDopplerVImplicit )
                {
                    cmggExplicitVel.appendTo( formDoppler );
                    cmggExplicitVel.setValue( getDiagVelocity() );
                }
            }
            else if( cgDoppler.size() == 2 && !cgDoppler.isSelected( 0 ) ) {
                // the 1st flag was switched off, disabling Doppler
                audio_sc.setDopplerEnabled( false );
                cmggExplicitVel.deleteFrom( formDoppler );
                cgDoppler.delete( 1 );
            }
            else if( cgDoppler.size() == 2 && !cgDoppler.isSelected( 1 ) ) {
                // the 2nd flag was switched off
                _isDopplerVImplicit = false;
                cmggExplicitVel.appendTo( formDoppler );
                cmggExplicitVel.setValue( getDiagVelocity() );
            }
            else if( cgDoppler.size() == 2 && cgDoppler.isSelected( 1 ) ){
                // the 2nd flag was just switched on
                _isDopplerVImplicit = true;
                cmggExplicitVel.deleteFrom( formDoppler );
                setVelFacFromLocation();
            }
            else {
                System.out.println( "Doppler settings error" );
            }
           
        }
}

