/*
 * Copyright 2007 Sun Microsystems, Inc. All rights reserved.
 * Use is subject to license terms.
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
