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
