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

public class CopterCanvas extends javax.microedition.lcdui.Canvas implements
    Copter2DSpaceScene {

    int maxX, maxY;

    int _xSpec, _ySpec; // coordinates of the spectator
    
    boolean _isWall;

    RepaintNotifier repaintNotifier;

    CopterMover copter_mover;

    public CopterCanvas( int nStepsHorizontal ) {

            repaintNotifier = new RepaintNotifier();

            setMaxCoord( nStepsHorizontal ); 

            copter_mover = new CopterMover( maxX, maxY );

            _xSpec = maxX / 2;
            _ySpec = maxY / 2;
            
            _isWall = false;
    }

    public int getSpecX()
    {
        return _xSpec;
    }

    public int getSpecY()
    {
        return _ySpec;
    }

    public int getCopterX() // X coordinate of the Helicopter
    {
        return copter_mover.getX();
    }

    public int getCopterY() // Y coordinate of the Helicopter
    {
        return copter_mover.getY();
    }

    public int getCopterVx() // X velocity component of the Helicopter
    {
        return copter_mover.getVelocityX();
    }

    public int getCopterVy() // Y velocity component of the Helicopter
    {
        return copter_mover.getVelocityY();
    }
    
    int getMinYSpec()
    {
        if( _isWall )
            return  maxY * 2 / 3 + 1;
        else 
            return 0;
    }

    void setMaxCoord( int nStepsHorizontal )
    {
        maxX = nStepsHorizontal - 1;
        maxY = ( nStepsHorizontal * getHeight() ) / getWidth() - 1;
    }

    public void nextFrame( Display d )
    {
        copter_mover.nextFrame();
        if( !repaintNotifier.isRepainting() )
        {
            repaintNotifier.setRepainting();
            repaint();
            d.callSerially( repaintNotifier );
        }
    }

    int xStepsToPixels( int xSteps )
    {
        return ( getWidth() * xSteps ) / maxX;
    }

    int yStepsToPixels( int ySteps )
    {
        return ( getHeight() * ySteps ) / maxY;
    }

    int getStepWidth()
    {
        return getWidth() / maxX;
    }

    int getStepHeight()
    {
        return getHeight() / maxY;
    }

    void fillStepSizedRect( Graphics g, int x, int y )
    {
        g.fillRect( xStepsToPixels( x ), 
                    yStepsToPixels( y ), 
                    getStepWidth(),
                    getStepHeight() );
    }

    void paintHelicopter( Graphics g )
    { 
        g.setColor(0xFF0000);
        fillStepSizedRect( g, copter_mover.getX(), copter_mover.getY() );
    }

    void paintSpectator( Graphics g )
    {
        g.setColor(0x0000FF);
        fillStepSizedRect( g, _xSpec, _ySpec );
    }

    void fillBackgnd( Graphics g )
    {
        int x = g.getClipX();
        int y = g.getClipY();
        int w = g.getClipWidth();
        int h = g.getClipHeight();

        // Draw the frame 
        g.setColor(0x000000);
        g.fillRect(x, y, w, h);

    }
    
    public void setWall( boolean w )
    {
        _isWall = w;
        if( _ySpec  < getMinYSpec() )
            _ySpec = getMinYSpec();
        if( w )
            copter_mover.setMaxY( getMinYSpec() - 3 );
        else
            copter_mover.setMaxY( maxY );
    }

    public boolean isWall()
    {
        return _isWall;
    }
    
    void paintWall( Graphics g )
    {
        g.setColor( 0x00FFFF );
        g.drawLine( 0, yStepsToPixels( getMinYSpec() - 1 ), 
                xStepsToPixels( maxX ), 
                yStepsToPixels( getMinYSpec() - 1 ) );
    }
    
    protected void paint(Graphics g)
    {
        fillBackgnd( g );
        if( _isWall )
            paintWall( g );
        paintSpectator( g  );
        paintHelicopter( g );

    }
    public void keyPressed( int keyCode ) {

        int action = getGameAction( keyCode );

        switch ( action ) {
        case LEFT:
            _xSpec--;
            if( _xSpec < 0 )
            { 
                _xSpec = 0;
            }
            break;

        case RIGHT:
            _xSpec++;
            if( _xSpec > maxX )
            {
                _xSpec = maxX;
            }
            break;

        case UP:
            _ySpec--;
            if( _ySpec < getMinYSpec() )
            { 
                _ySpec = getMinYSpec();
            }
            break;

        case DOWN:
            _ySpec++;
            if( _ySpec > maxY )
            {
                _ySpec = maxY;
            }
            break;
        }
    }

    class RepaintNotifier implements Runnable
    {
        boolean _isRepainting;

        public RepaintNotifier()
        {
            _isRepainting = false;
        }

        public void run() {
            _isRepainting = false;
        }

        public boolean isRepainting()
        {
            return _isRepainting;
        }

        public void setRepainting()
        {
            _isRepainting = true;
        }
    }

}
