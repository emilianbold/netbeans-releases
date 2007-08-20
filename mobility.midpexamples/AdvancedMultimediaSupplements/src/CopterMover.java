/*
 * Copyright 2007 Sun Microsystems, Inc. All rights reserved.
 * Use is subject to license terms.
 */

/*
 * This class defines location and velocity of the helicopter
 * at the moment
 */
public class CopterMover {
    int _x;
    int _y;

    int _dx;
    int _dy;

    int _maxX;
    int _maxY;
    

    public CopterMover( int maxX, int maxY)
    {
        _maxX = maxX;
        _maxY = maxY;
        _x = _maxX / 3;
        _y = _maxY / 3;

        _dx = 1;
        _dy = 1;
    }

    public int getVelocityX()
    {
        return _dx;
    }

    public int getVelocityY()
    {
        return _dy;
    }

    public int getX()
    {
        return _x;
    }

    public int getY()
    {
        return _y;
    }

    public void setMaxY( int maxY )
    {
        _maxY = maxY;
    }
    
    public void nextFrame()
    {
        _x += _dx;
        _y += _dy;

        // bouncing from the screen bounds
        if( _x <= 0 )
        {
            _x = 0;
            _dx *= -1;
        }
        else if( _x >= _maxX )
        {
            _x = _maxX;
            _dx *= -1;
        }
        
        if( _y <= 0 )
        {
            _y = 0;
            _dy *= -1;
        }
        else if( _y >=_maxY ) {
            _y = _maxY;
            _dy *= -1;
        }
    }
}
