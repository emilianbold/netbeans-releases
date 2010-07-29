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
