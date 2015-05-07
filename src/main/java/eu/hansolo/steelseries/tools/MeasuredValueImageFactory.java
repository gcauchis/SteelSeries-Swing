/*
 * Copyright (c) 2012, Gerrit Grunwald
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * The names of its contributors may not be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
package eu.hansolo.steelseries.tools;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;

/**
 * A Factory used to create Measured value image.
 * 
 * @author Gerrit Grunwald <han.solo at muenster.de> and Gabriel Cauchis <gabriel.cauchis at gmail.com>
 */
public enum MeasuredValueImageFactory
{
    INSTANCE;
    
    public static final double RATIO_WIDTH = 0.0280373832;
    protected static final Util UTIL = Util.INSTANCE;
    
    /**
     * Returns the image of the MinMeasuredValue and MaxMeasuredValue dependend
     * @param WIDTH
     * @param COLOR
     * @return the image of the min or max measured value
     */
    public BufferedImage createMeasuredValueImage(final int WIDTH, final Color COLOR)
    {
        return createMeasuredValueImage(WIDTH, COLOR, 0);
    }
    
    /**
     * Returns the image of the MinMeasuredValue and MaxMeasuredValue dependend
     * @param WIDTH
     * @param COLOR
     * @param ROTATION_OFFSET
     * @return the image of the min or max measured value
     */
    public BufferedImage createMeasuredValueImage(final int WIDTH, final Color COLOR, final double ROTATION_OFFSET)
    {
        if (WIDTH <= 36) // 36 is needed otherwise the image size could be smaller than 1
        {
            return UTIL.createImage(1, 1, Transparency.TRANSLUCENT);
        }

        final int IMAGE_HEIGHT = (int) (WIDTH * RATIO_WIDTH);
        final int IMAGE_WIDTH = IMAGE_HEIGHT;

        final BufferedImage IMAGE = UTIL.createImage(IMAGE_WIDTH, IMAGE_HEIGHT, Transparency.TRANSLUCENT);
        final Graphics2D G2 = IMAGE.createGraphics();
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        G2.rotate(ROTATION_OFFSET, IMAGE_WIDTH / 2.0, IMAGE_HEIGHT / 2.0);

        final GeneralPath INDICATOR = new GeneralPath();
        INDICATOR.setWindingRule(Path2D.WIND_EVEN_ODD);
        INDICATOR.moveTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT);
        INDICATOR.lineTo(0.0, 0.0);
        INDICATOR.lineTo(IMAGE_WIDTH, 0.0);
        INDICATOR.closePath();

        G2.setColor(COLOR);
        G2.fill(INDICATOR);

        G2.dispose();

        return IMAGE;
    }
    
    @Override
    public String toString() {
        return "MeasurecValueImageFactory";
    }

}
