/*
 * Copyright 2007 Sun Microsystems, Inc. All rights reserved.
 * Use is subject to license terms.
 */

import javax.microedition.lcdui.*;

public abstract class CopterForm {
    Form _f;
    Command _c;
    CopterAudioScene _cas;
    
    public CopterForm( String title, 
            Command cmdExit, 
            CommandListener cl,
            ItemStateListener isl,
            Canvas canv,
            CopterAudioScene cas )
    {
            _f = new Form( title );
            _f.addCommand( cmdExit );
            _f.setItemStateListener( isl );
            _f.setCommandListener( cl );
     
            _c = new Command( title, Command.ITEM, 2 );
            canv.addCommand( _c );
            _cas = cas;
    }
    
    protected Form getForm()
    {
        return _f;
    }
    
    protected Command getCommand()
    {
        return _c;
    }
    
    protected CopterAudioScene getAudioScene()
    {
        return _cas;
    }
    
    public boolean isCurrent( Display d )
    {
        return ( d.getCurrent() == _f );
    }
    
    public abstract void handleItem( Item i );
    
    public boolean isThisCommand( Command c )
    {
        return ( c == _c );
    }
    
    public void setCurrent( Display d )
    {
        d.setCurrent( _f );
    }
}
