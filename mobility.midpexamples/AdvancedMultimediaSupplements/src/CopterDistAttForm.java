/*
 * Copyright 2007 Sun Microsystems, Inc. All rights reserved.
 * Use is subject to license terms.
 */

import javax.microedition.lcdui.*;
        
public class CopterDistAttForm extends CopterForm {

    CopterMeterGauge cmggMinDist;
    CopterMeterGauge cmggRollofFactor;
    ChoiceGroup cgMaxDistIsInfinite;
    CopterMeterGauge cmggMaxDist;
    ChoiceGroup cgMuteAfterMax;

    public CopterDistAttForm( String title, 
            Command cmdExit, 
            CommandListener cl,
            ItemStateListener isl,
            Canvas canv,
            CopterAudioScene cas )
    {
        super( title, cmdExit, cl, isl, canv, cas );
        cmggMinDist = new CopterMeterGauge( getForm(), 1, 26, 
                getAudioScene().getMinDist() / 1000, 1, 
            "Minimum Attenuation Distance (up to which the source is loudest)", 
                "m" );
        cmggRollofFactor = new CopterMeterGauge( getForm(), 
                0, 
                2500,
                getAudioScene().getRollofFactor(),
                100,
       "Factor determining how fast the sound is attenuated with distance",
                "" );
        cmggMaxDist = new CopterMeterGauge( getForm(), 0, 100, 
                getAudioScene().getMaxDist() / 1000, 4, 
                "Maximum Attenuation Distance", "m" );
        cgMuteAfterMax = new ChoiceGroup( null, Choice.MULTIPLE );
        cgMuteAfterMax.append( "Mute beyond Maximum Distance", null );
        cgMuteAfterMax.setSelectedIndex( 0, getAudioScene().getMuteAfterMax() );
        getForm().append( cgMuteAfterMax );
    }
    
    public void handleItem( Item i )
    {
        if( cmggRollofFactor.isThisItem(i) )
        {
            getAudioScene().setRollofFactor( cmggRollofFactor.getValue() );
        }
        else if( cmggMinDist.isThisItem(i) )
        {
            getAudioScene().setMinDist( cmggMinDist.getValue() * 1000 );
        }
        else if( cmggMaxDist.isThisItem(i) )
        {
            getAudioScene().setMaxDist( cmggMaxDist.getValue() * 1000 );
        }
        else if( i == cgMuteAfterMax )
        {
            getAudioScene().setMuteAfterMax( cgMuteAfterMax.isSelected(0) );
        }
    }
}
