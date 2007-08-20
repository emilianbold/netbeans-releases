/*
 * Copyright 2007 Sun Microsystems, Inc. All rights reserved.
 * Use is subject to license terms.
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
