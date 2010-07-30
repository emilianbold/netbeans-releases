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

public class CopterMeterGauge extends CopterGauge {
    StringItem _meter;
    String _unit_name;

    public CopterMeterGauge( Form f, 
                             int minValue, 
                             int maxValue, 
                             int initValue, 
                             int increment, 
                             String label,
                             String unit_name )
    {
        super( f, minValue, maxValue, initValue, increment, label );

        _unit_name = new String( " " );
        _unit_name = _unit_name.concat( unit_name ) ;
        _meter = new StringItem( null, "" );
        refresh();
        _meter.setLayout( Item.LAYOUT_CENTER | Item.LAYOUT_NEWLINE_BEFORE );
        f.append( _meter );

    }

    public void refresh()
    {
        _meter.setText( String.valueOf( super.getValue() ).concat( _unit_name )
        );
    }

    public void deleteFrom( Form f )
    {
        super.deleteFrom( f );
        deleteItemFromForm( _meter, f );
    }

    public void appendTo( Form f )
    {
        super.appendTo(f);
        f.append( _meter );
    }
    
    public void setValue( int v )
    {
        super.setValue(v);
        refresh();
    }
    
    public int getValue()
    {
        refresh();
        return super.getValue();
    }
    
}
