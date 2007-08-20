/*
 * Copyright 2007 Sun Microsystems, Inc. All rights reserved.
 * Use is subject to license terms.
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
