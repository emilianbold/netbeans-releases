/*
 * Copyright 2007 Sun Microsystems, Inc. All rights reserved.
 * Use is subject to license terms.
 */

import java.io.*;

import javax.microedition.amms.*;
import javax.microedition.amms.control.*;
import javax.microedition.amms.control.audio3d.*;
import javax.microedition.amms.control.audioeffect.*;
import javax.microedition.media.*;
import javax.microedition.media.control.VolumeControl;

public class CopterAudioScene {

    LocationControl ctlLocationH; // the helicopter location control
    LocationControl ctlLocation0; // the observer location control
    MacroscopicControl ctlMacroH; // the helicopter size control
    DopplerControl ctlDopplerH; // Doppler control for the 'copter
    CommitControl ctlCommit;
    PanControl ctlPan;
    OrientationControl ctlOri; // for the spectator
    DirectivityControl ctlDirect; // for the 'copter
    DistanceAttenuationControl ctlDistAtt;
    ObstructionControl ctlObstr;

    VolumeControl ctlVolume;

    Player rPlayer, rColPlayer;

    SoundSource3D ss3dCopter;

    InputStream istrCopterBuzz;
    InputStream istrBubble;

    int _velocityFactor; // in mm per second

    int _altitude; // in mm
    int _scale; // in mm
    
    boolean _isPlayerStarted;

    public CopterAudioScene( int scale, int altitude, int velFac )
    {
        setupAudioScene();

        ctlMacroH.setSize(1000, 2000, 5000);

        _velocityFactor = velFac;
        _altitude = altitude;
        _scale = scale;
        
        _isPlayerStarted = false;

    }

    void setupAudioScene() {
        try {
            istrCopterBuzz = getClass().getResourceAsStream(
                    "helicopter.wav");
            istrBubble = getClass().getResourceAsStream("bubbles.wav");

            rPlayer = Manager.createPlayer(istrCopterBuzz, "audio/x-wav");
            rColPlayer = Manager.createPlayer(istrBubble, "audio/x-wav");

            rPlayer.setLoopCount(-1); rPlayer.realize();
            rColPlayer.realize();

            ss3dCopter = GlobalManager.createSoundSource3D();
            ss3dCopter.addPlayer(rPlayer);
            ss3dCopter.addPlayer(rColPlayer);

            ctlLocationH = (LocationControl)ss3dCopter.getControl(
                    "javax.microedition.amms.control.audio3d.LocationControl");
            ctlMacroH = (MacroscopicControl)ss3dCopter.getControl(
                 "javax.microedition.amms.control.audio3d.MacroscopicControl");
            ctlDopplerH = (DopplerControl)ss3dCopter.getControl(
                    "javax.microedition.amms.control.audio3d.DopplerControl");
            ctlDirect = (DirectivityControl)ss3dCopter.getControl(
               "javax.microedition.amms.control.audio3d.DirectivityControl");
            ctlDistAtt = (DistanceAttenuationControl)ss3dCopter.getControl(
        "javax.microedition.amms.control.audio3d.DistanceAttenuationControl");
            if( ctlDistAtt == null )
                System.out.println( "000000000000000000000" );

            ctlObstr = (ObstructionControl)ss3dCopter.getControl(
              "javax.microedition.amms.control.audio3d.ObstructionControl");
            ctlVolume = (VolumeControl)GlobalManager.getControl(
                    "javax.microedition.media.control.VolumeControl");

            ctlPan = (PanControl)GlobalManager.getControl(
                    "javax.microedition.amms.control.PanControl");
            

            Spectator spectator = GlobalManager.getSpectator();
            ctlLocation0 = (LocationControl)spectator.getControl(
                    "javax.microedition.amms.control.audio3d.LocationControl");
            ctlOri = (OrientationControl)spectator.getControl(
                 "javax.microedition.amms.control.audio3d.OrientationControl");

            ctlCommit = (CommitControl)GlobalManager.getControl(
                    "javax.microedition.amms.control.audio3d.CommitControl");

            rPlayer.prefetch();
            rColPlayer.prefetch();
        } catch (Exception e)
        {
            e.printStackTrace();
            System.out.println(e);
        }
    }         

    void orientCopter( Copter2DSpaceScene s )
    {
        int f[] = { s.getCopterVx(), 0, s.getCopterVy() };
        int a[] = { 0, 1, 0 };

        ctlDirect.setOrientation( f, a );
    }
    
    public void nextFrame( Copter2DSpaceScene s )
    {
        ctlLocationH.setCartesian( s.getCopterX() * _scale, _altitude, 
                                   s.getCopterY() * _scale );

        ctlLocation0.setCartesian( s.getSpecX() * _scale, 0, 
                                   s.getSpecY() * _scale);

        ctlDopplerH.setVelocityCartesian( s.getCopterVx() * _velocityFactor, 0, 
                                          s.getCopterVy() * _velocityFactor);

        orientCopter( s );
        
        if( !_isPlayerStarted ){
            try { rPlayer.start(); } catch(Exception e) { }
            _isPlayerStarted = true;
        }
    }

    public int getVolume()
    {
        return ctlVolume.getLevel();
    }

    public void setVolume( int level )
    {
        ctlVolume.setLevel( level );
    }

    public int getPan()
    {
        return ctlPan.getPan();
    }

    public void setPan( int pan )
    {
        ctlPan.setPan( pan );
    }

    public int getVelFactor()
    {
        return _velocityFactor;
    }

    public void setVelFactor( int value )
    {
        _velocityFactor = value;
    }

    public void stop()
    {
        try { rPlayer.stop(); 
              rColPlayer.stop(); 
            } catch(Exception e) {
                e.printStackTrace();
                System.out.println(e);
            }

        rPlayer.close();
        rColPlayer.close();

        ss3dCopter.removePlayer( rPlayer );
        ss3dCopter.removePlayer( rColPlayer );

/*        try {
            istrCopterBuzz.close();
            istrBubble.close();
        } catch ( Exception e ) {
            e.printStackTrace();
            System.out.println(e);
        }*/
    }

    public void setAltitude( int a )
    {
        _altitude = a;
    }

    public int getAltitude()
    {
        return _altitude;
    }

    public void setStepScale( int s )
    {
        _scale = s;
    }

    public int getStepScale()
    {
        return _scale;
    }

    public boolean isDopplerEnabled()
    {
        return ctlDopplerH.isEnabled();
    }

    public void setDopplerEnabled( boolean enabled )
    {
        ctlDopplerH.setEnabled( enabled );
    }
    
    public int getCopterLength()
    {
        int size[] = ctlMacroH.getSize();
        return size[2] / 1000; // in meters
    }
    
    public int getCopterWidth()
    {
        int size[] = ctlMacroH.getSize();
        return size[0] / 1000; // in meters
    }
    
    public int getCopterHeight()
    {
        int size[] = ctlMacroH.getSize();
        return size[1] / 1000; // in meters
    }
    
    public void setCopterLength( int len )
    {
        int size[] = ctlMacroH.getSize();
        ctlMacroH.setSize( size[0], size[1], len*1000 );
    }

    public void setCopterWidth( int w )
    {
        int size[] = ctlMacroH.getSize();
        ctlMacroH.setSize( w*1000, size[1], size[2] );
    }

    public void setCopterHeight( int h )
    {
        int size[] = ctlMacroH.getSize();
        ctlMacroH.setSize( size[0], h*1000, size[2] );
    }
    
    public void setStandingStraight()
    {
        int front[] = {0, 0, -1};
        int above[] = {0, 1, 0};
        try {
            ctlOri.setOrientation( front, above );
        } catch (Exception e) {
        }
    }
    
    public void setFallenLeft()
    {
        int f[] = { 0, 0, -1 };
        int a[] = { -1, 0, 0 };

        try {
            ctlOri.setOrientation( f, a );
        } catch (Exception e) {
        }
    }

    public void setFallenRight()
    {
        int f[] = { 0, 0, -1 };
        int a[] = { 1, 0, 0 };

        try {
            ctlOri.setOrientation( f, a );
        } catch (Exception e) {
        }
    }

    public void setFallenFacedown()
    {
        int f[] = { 0, -1, 0 };
        int a[] = { 0, 0, -1 };

        try {
            ctlOri.setOrientation( f, a );
        } catch (Exception e) {
        }
    }
    
    public void setFallenFlatBack()
    {
        int f[] = { 0, 1, 0 };
        int a[] = { 0, 0, 1 };

        try {
            ctlOri.setOrientation( f, a );
        } catch (Exception e) {
        }
    }

    public int getMinAngle()
    {
        int p[] = ctlDirect.getParameters();
        return p[0];
    }
    
    public int getMaxAngle()
    {
        int p[] = ctlDirect.getParameters();
        return p[1];
    }
    
    public int getRearLevel()
    {
        int p[] = ctlDirect.getParameters();
        return p[2];
    }
    
    public void setMinAngle( int a )
    {
        int p[] = ctlDirect.getParameters();
        if( a > p[1] )
        {
            a = p[1];
        }
        try {
            ctlDirect.setParameters( a, p[1], p[2] );
        }
        catch( Exception e ) {}
    }
    
    public void setMaxAngle( int a )
    {
        int p[] = ctlDirect.getParameters();
        if( a < p[0] )
        {
            a = p[0];
        }
        try {
            ctlDirect.setParameters( p[0], a, p[2] );
        }
        catch( Exception e ) {}
    }
    
    public void setRearLevel( int lev )
    {
        int p[] = ctlDirect.getParameters();
        if( lev > 0 )
        {
            lev = 0;
        }
        try {
            ctlDirect.setParameters(p[0], p[1], lev );
        }
        catch( Exception e ) {}
    }
    
    public boolean isDeferred()
    {
        return ctlCommit.isDeferred();
    }
    
    public void setDeferred( boolean d )
    {
        ctlCommit.setDeferred( d );
    }
    
    public void commit()
    {
        ctlCommit.commit();
    }
    
    public int getMinDist()
    {
        return ctlDistAtt.getMinDistance();
    }
    
    public void setMinDist( int d )
    {
        ctlDistAtt.setParameters( d,
                ctlDistAtt.getMaxDistance(),
                ctlDistAtt.getMuteAfterMax(),
                ctlDistAtt.getRolloffFactor() );
    }

    public int getMaxDist()
    {
        return ctlDistAtt.getMaxDistance();
    }
    
    public void setMaxDist( int d )
    {
        ctlDistAtt.setParameters( ctlDistAtt.getMinDistance(),
                d,
                ctlDistAtt.getMuteAfterMax(),
                ctlDistAtt.getRolloffFactor() );
    }
    
    public boolean getMuteAfterMax()
    {
        return ctlDistAtt.getMuteAfterMax();
    }
    
    public void setMuteAfterMax( boolean m )
    {
        ctlDistAtt.setParameters( ctlDistAtt.getMinDistance(),
                ctlDistAtt.getMaxDistance(),
                m,
                ctlDistAtt.getRolloffFactor() );
    }
    
    public int getRollofFactor()
    {
        return ctlDistAtt.getRolloffFactor();
    }
    
    public void setRollofFactor( int r )
    {
        ctlDistAtt.setParameters( ctlDistAtt.getMinDistance(),
                ctlDistAtt.getMaxDistance(),
                ctlDistAtt.getMuteAfterMax(),
                r );
    }

    public void setNoObstr()
    {
        ctlObstr.setLevel(0);
        ctlObstr.setHFLevel(0);
    }

    public void setSingleWin()
    {
        ctlObstr.setLevel( -1098 );
        ctlObstr.setHFLevel( -812 );
    }
    
    public void setDoubleWin()
    {
        ctlObstr.setLevel( -2000 );
        ctlObstr.setHFLevel( -3000 );
    }
    
    public void setThinDoor()
    {
        ctlObstr.setLevel( -1188 );
        ctlObstr.setHFLevel( -612 );
    }
    
    public void setThickDoor()
    {
        ctlObstr.setLevel( -2816 );
        ctlObstr.setHFLevel( -1584 );
    }
    
    public void setWoodWall()
    {
        ctlObstr.setLevel( -2000 );
        ctlObstr.setHFLevel( -2000 );
    }
    
    public void setBrickWall()
    {
        ctlObstr.setLevel( -3000 );
        ctlObstr.setHFLevel( -2000 );
    }
    
    public void setStoneWall()
    {
        ctlObstr.setLevel( -4080 );
        ctlObstr.setHFLevel( -1920 );
    }
    
    public void setCurtain()
    {
        ctlObstr.setLevel( -180 );
        ctlObstr.setHFLevel( -1020 );
    }
    
}
