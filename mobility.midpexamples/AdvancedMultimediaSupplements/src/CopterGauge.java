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

public class CopterGauge {
    Gauge _g;
    int _increment;
    int _minValue;

    public CopterGauge( Form f, 
                        int minValue, 
                        int maxValue, 
                        int initValue,
                        int increment,
                        String label )
    {
        _increment = increment;
        _minValue = minValue;

        _g = new Gauge( label, true, 
                        adapt( maxValue ), 
                        adapt( initValue ) );
        _g.setPreferredSize(
            f.getWidth(),
            _g.getPreferredHeight() );
        _g.setLayout( Item.LAYOUT_CENTER );
        f.append( _g );
    }

    
    
    
    int adaptToGauge( int v )
    {
        int gv = adapt( v );
        if( gv < 0 ) {
            return 0;
        }
        else if( gv > _g.getMaxValue() )
        {
            return _g.getMaxValue();
        }
        return gv;
    }

    int adapt( int v )
    {
        return (v - _minValue + ( _increment >> 1 ) ) / _increment;
    }
    
    public int getValue()
    {
        return getValue( _g.getValue() );
    }

    int getValue( int gaugeValue )
    {
        return ( gaugeValue * _increment ) + _minValue;
    }

    int getMaxValue()
    {
        return getValue( _g.getMaxValue() );
    }
    
    public boolean isThisItem( Item i ) 
    {
        return ( i == _g );
    }

    public int getHeight()
    {
        return _g.getPreferredHeight();
    }

    protected static void deleteItemFromForm( Item i, Form f )
    {
        for( int j = 0; j < f.size(); j++)
        {
            if ( f.get( j ) == i ) {
                f.delete( j );
                break;
            }
        }
    }

    public void deleteFrom( Form f )
    {
        deleteItemFromForm( _g, f );
    }

    public void appendTo( Form f )
    {
        f.append( _g );
    }
    
    public void setValue( int v )
    {
        _g.setValue( adaptToGauge( v ) );
    }
    
    public int clipValue( int v )
    {
        return getValue( adaptToGauge(v) );
    }
    
    public void incr()
    {
        setValue( getValue() + _increment );
    }
    
    public void decr()
    {
        setValue( getValue() - _increment );
    }
}
