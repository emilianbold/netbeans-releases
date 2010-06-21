/*
 * Copyright (c) 2010, Oracle.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in
 *       the documentation and/or other materials provided with the
 *       distribution.
 *     * Neither the name of Oracle nor the names of its
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
