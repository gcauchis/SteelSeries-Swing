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

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Arc2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;

/**
 * A set of utilities for {@link CustomGaugeType}.
 * 
 * @author Gerrit Grunwald <han.solo at muenster.de> and Gabriel Cauchis <gabriel.cauchis at gmail.com>
 */
public final class GaugeTypeUtil
{
    public static final double FRAME_THETA_MARGING_DEG = 8;
    public static final double FRAME_THETA_MARGING_RAD = Math.toRadians(FRAME_THETA_MARGING_DEG);
    public static final double FRAME_MARGING = 0.10;

    private GaugeTypeUtil()
    {
    }
    
    /**
     * Compute the dimension and center using the given param. 
     * @param gaugeType the gauge type.
     * @param customGaugeType the custom gauge type.
     * @param dimension the current dimension who will be update.
     * @param center the center who will be update.
     */
    public static void computeDimention(final GaugeType gaugeType, final CustomGaugeType customGaugeType,final Dimension dimension, final Point2D center)
    {
        if (gaugeType != GaugeType.CUSTOM)
        {
            computeDimention(new CustomGaugeType(gaugeType.FREE_AREA_ANGLE,
                                                    gaugeType.ROTATION_OFFSET,
                                                    gaugeType.TICKMARK_OFFSET,
                                                    gaugeType.TICKLABEL_ORIENTATION_CHANGE_ANGLE,
                                                    gaugeType.ANGLE_RANGE,
                                                    gaugeType.ORIGIN_CORRECTION,
                                                    gaugeType.APEX_ANGLE,
                                                    gaugeType.BARGRAPH_OFFSET,
                                                    gaugeType.LCD_FACTORS,
                                                    gaugeType.POST_POSITIONS),
                                dimension, center);
        }
        else
        {
            computeDimention(customGaugeType, dimension, center);
        }
    }
    
    /**
     * Compute the dimension and center using the given param. 
     * @param gaugeType the gauge type.
     * @param dimension the current dimension who will be update.
     * @param center the center who will be update.
     */
    private static void computeDimention(final CustomGaugeType gaugeType, final Dimension dimension, final Point2D center)
    {
        final GaugeTypeInfo gaugeTypeInfo = GaugeTypeInfo.getGaugeTypeInfo(gaugeType);
        
        //compute new dimension
        if (dimension.height * gaugeTypeInfo.dimPropRatio < dimension.width)
        {
            dimension.width = (int) (dimension.height * gaugeTypeInfo.dimPropRatio);
        }
        else
        {
            dimension.height = (int) (dimension.width / gaugeTypeInfo.dimPropRatio);
        }
        
        center.setLocation(computeCenter(gaugeTypeInfo, dimension));
    }
    
    /**
     * Compute the center position for the given gauge type..
     * 
     * @param gaugeTypeInfo the reference.
     * @param dimension the current dimension.
     * @param center the center to update.
     */
    public static void computeCenter(final CustomGaugeType gaugeType, final Dimension dimension, final Point2D center)
    {
        center.setLocation(computeCenter(GaugeTypeInfo.getGaugeTypeInfo(gaugeType), dimension));
    }
    
    /**
     * Compute the center position for the given gauge type..
     * 
     * @param gaugeTypeInfo the reference.
     * @param dimension the current dimension.
     * @return the center position.
     */
    public static Point2D computeCenter(final GaugeTypeInfo gaugeTypeInfo, final Dimension dimension)
    {
      //Compute new center
        return new Point2D.Double((int) (dimension.width * gaugeTypeInfo.leftWidthRatio / (gaugeTypeInfo.leftWidthRatio + gaugeTypeInfo.rightWidthRatio)),
                                    (int) (dimension.height * gaugeTypeInfo.upHeightRatio / (gaugeTypeInfo.upHeightRatio + gaugeTypeInfo.downHeightRatio)));
    }
    
    /**
     * Compute the ratio in the direction.
     * 
     * @param directionsInRange if the direction is in range. If true return 1.
     * @param hasElementInDirections if the dial is present in the directions.
     * @param ratio the ratios if present in the direction.
     * @return a ratio between {@link #FRAME_MARGING FRAME_MARGING} and 1.
     */
    protected static double computeDirectionRatio(final boolean directionsInRange, final boolean hasElementInDirections, final double ratio)
    {
        double result;
        if (directionsInRange)
        {
            //half gauge is present in the direction.
            result = 1;
        }
        else if (hasElementInDirections)
        {
            //gauge a little present in the direction
            result = Math.max(ratio, FRAME_MARGING);
        }
        else
        {
            //no gauge in the directions
            result = FRAME_MARGING;
        }
        
        return result;
    }
    
    /**
     * Check if the given angle is in the range of the gauge.
     * 
     * @param gaugeType the gauge type.
     * @param angle the angle to check in radians with reference the trigonometric circle.
     * @return <pre>true</pre> if the angle is in the range of the gauge.
     */
    public static boolean isInRange(final CustomGaugeType gaugeType, final double angle)
    {
        if (gaugeType.ANGLE_RANGE >= 2 * Math.PI)
        {
            return true;
        }
        final double start = roundTrigoValue(gaugeType.ROTATION_OFFSET);
        final double angleRef = toSteelSeriesAngle(angle);
        final double range = gaugeType.ANGLE_RANGE;
        final double end = start + range;
        
        if (angleRef >= start)
        {
            return angleRef <= end;
        }
        else if (end > 2 * Math.PI)
        {
            return angleRef <= (end) % (2 * Math.PI);
        }
        return false;
    }
    
    /**
     * 
     * @param angle a trigo angle.
     * @return
     */
    protected static double toSteelSeriesAngle(final double angle)
    {
        return roundTrigoValue(-angle + Math.PI / 2);
    }
    
    /**
     * 
     * @param angle a steel series angle.
     * @return
     */
    protected static double toTrigoAngle(final double angle)
    {
        return roundTrigoValue(-angle + Math.PI / 2);
    }
    
    /**
     * Compute the angle value between 0 and 2 * PI.
     * 
     * @param angle the angle in radians.
     * @return the angle value between 0 and 2 * PI.
     */
    public static double roundTrigoValue(final double angle) {
        double result = angle % (2 * Math.PI);
        if (result < 0) {
            result = (2 * Math.PI) + result;
        }
        return result;
    }
    
    /**
     * Build the shape for frame and background.
     * 
     * @param gaugeTypeInfo the information.
     * @param dimension the current dimension of the gauge.
     * @param ratio the ratio of the shape between 0.5 and 1. 1 for outer frame or background if no frame.
     * @param frameType the frame type.
     * @return a shape for the asked dimensions.
     */
    public static Shape buildShape(final GaugeTypeInfo gaugeTypeInfo, final Dimension dimension, final double ratio, final FrameType frameType) {
        if (ratio > 1 || ratio < 0.5) {
            throw new IllegalArgumentException("Ratio must be between 0.5 and 1");
        }
        final Rectangle bound = new Rectangle((int) ((dimension.width - (dimension.width * ratio)) / 2),
                (int) ((dimension.height - (dimension.height * ratio)) / 2),
                (int) (dimension.width * ratio),
                (int) (dimension.height * ratio));
        Shape shape;
        switch (frameType) {
            case SQUARE:
                shape = bound;
                break;
            case ROUND:
            default:
                Point2D center = computeCenter(gaugeTypeInfo, dimension);
                double radius = computeRadius(gaugeTypeInfo, bound, center);
                double range = gaugeTypeInfo.gaugeTypeMarged.ANGLE_RANGE;
                
                Point2D startArc = new Point2D.Double(center.getX() + radius * gaugeTypeInfo.cosStartAngle , center.getY() - radius * gaugeTypeInfo.sinStartAngle);
                Point2D endArc = new Point2D.Double(center.getX() + radius * gaugeTypeInfo.cosEndAngle , center.getY() - radius * gaugeTypeInfo.sinEndAngle);
                
                final GeneralPath path =  new GeneralPath();
                path.setWindingRule(Path2D.WIND_EVEN_ODD);
                //Need to be done twice... Why ?
                path.moveTo(endArc.getX(), endArc.getX());
                path.moveTo(endArc.getX(), endArc.getY());
                final double startArcDeg = Math.toDegrees(gaugeTypeInfo.endAngle) % 360;
                final double rangeArcDeg = Math.toDegrees(range);
                path.append(new Arc2D.Double(center.getX() - radius, center.getY() - radius, radius * 2, radius * 2, startArcDeg, rangeArcDeg, Arc2D.OPEN), true);
                
                
                double currentAngle = (startArcDeg + rangeArcDeg) % 360;
                if (range < Math.PI / 2) {
                    Point2D nextPoint = computeNextShapePoint(startArc, null, bound, currentAngle);
                    path.lineTo(nextPoint.getX(), nextPoint.getY());
                    currentAngle += 90;
                    nextPoint = computeNextShapePoint(nextPoint, null, bound, currentAngle);
                    path.lineTo(nextPoint.getX(), nextPoint.getY());
                    currentAngle += 90;
                    nextPoint = computeNextShapePoint(nextPoint, endArc, bound, currentAngle);
                    path.lineTo(nextPoint.getX(), nextPoint.getY());
                } else if (range < Math.PI) {
                    Point2D nextPoint = computeNextShapePoint(startArc, null, bound, currentAngle);
                    path.lineTo(nextPoint.getX(), nextPoint.getY());
                    currentAngle += 90;
                    nextPoint = computeNextShapePoint(nextPoint, endArc, bound, currentAngle);
                    path.lineTo(nextPoint.getX(), nextPoint.getY());
                } else if (range < 3 * Math.PI / 2) {
                    Point2D nextPoint = computeNextShapePoint(startArc, endArc, bound, currentAngle);
                    path.lineTo(nextPoint.getX(), nextPoint.getY());
                } else {
                    if (gaugeTypeInfo.northInRange && gaugeTypeInfo.southInRange && gaugeTypeInfo.eastInRange && gaugeTypeInfo.westInRange) {
                        path.append(new Arc2D.Double(center.getX() - radius, center.getY() - radius, radius * 2, radius * 2, Math.toDegrees(gaugeTypeInfo.endAngle + range), 360 - Math.toDegrees(range), Arc2D.OPEN), true);
                    } else {
                        path.lineTo(startArc.getX(), startArc.getY());
                    }
                }
                
                path.closePath();
                shape = path;
                break;
        }
        return shape;
    }
    
    /**
     * Compute the next point in trigo rotation order.
     * @param current the current point.
     * @param last the last point if last link else null;
     * @param bound the bound;
     * @param currentAngle the current angle in degrees.
     * @return the next point.
     */
    private static Point2D computeNextShapePoint(final Point2D current, final Point2D last, final Rectangle bound, final double currentAngle) {
        final double angle = currentAngle % 360;
        final Point2D next = new Point2D.Double();
        if (last == null) {
            if (angle <= 90) {
                next.setLocation(bound.getX(), current.getY());
            } else if (angle <= 180) {
                next.setLocation(current.getX(), bound.getY() + bound.getHeight());
            } else if (angle <= 270) {
                next.setLocation(bound.getX() + bound.getWidth(), current.getY());
            } else {
                next.setLocation(current.getX(), bound.getY());
            }
        } else {
            if (angle <= 90) {
                if (last.getY() < current.getY()) {
                    next.setLocation(current.getX(), last.getY());
                } else {
                    next.setLocation(last.getX(), current.getY());
                }
            } else if (angle <= 180) {
                if (last.getX() < current.getX()) {
                    next.setLocation(last.getX(), current.getY());
                } else {
                    next.setLocation(current.getX(), last.getY());
                }
            } else if (angle <= 270) {
                if (last.getY() > current.getY()) {
                    next.setLocation(current.getX(), last.getY());
                } else {
                    next.setLocation(last.getX(), current.getY());
                }
            } else {
                if (last.getX() > current.getX()) {
                    next.setLocation(last.getX(), current.getY());
                } else {
                    next.setLocation(current.getX(), last.getY());
                }
            }
        }
        return next;
    }
    
    /**
     * Compute the radius of the arc.
     * 
     * @param gaugeTypeInfo the information.
     * @param bound the bound of the arc.
     * @param center the center of the arc.
     * @return the radius of the arc.
     */
    private static double computeRadius(final GaugeTypeInfo gaugeTypeInfo, final Rectangle bound, final Point2D center) {
        if (gaugeTypeInfo.northInRange) {
            return center.getY() - bound.y;
        } else if (gaugeTypeInfo.southInRange) {
            return bound.height - center.getY();
        } else if (gaugeTypeInfo.eastInRange) {
            return center.getX() - bound.x;
        } else if (gaugeTypeInfo.westInRange) {
            return bound.width - center.getX();
        } else if (gaugeTypeInfo.maxSin > 0) {
            //North
            return (center.getY() - bound.y) / gaugeTypeInfo.maxSin;
        } else if (gaugeTypeInfo.minSin < 0) {
            //South
            return (bound.height - center.getY()) / -gaugeTypeInfo.minSin;
        } else if (gaugeTypeInfo.minCos < 0) {
            //East
            return (center.getX() - bound.x) / -gaugeTypeInfo.minCos;
        } else if (gaugeTypeInfo.maxCos > 0) {
            //West
            return (bound.width - center.getX()) / gaugeTypeInfo.maxCos;
        }
        return gaugeTypeInfo.widthRadiusFactor * bound.width;
    }
    
    /**
     * Compute The radius factor of the arc with the width. (radius = widthRadiusFactor * width).
     * 
     * @param gaugeTypeInfo the information.
     * @param bound the bound of the arc.
     * @param center the center of the arc.
     * @return the radius of the arc.
     */
    public static float computeWidthRadiusFactor(final GaugeTypeInfo gaugeTypeInfo, final Rectangle bound, final Point2D center) {
        return (float) (computeRadius(gaugeTypeInfo, bound, center) / bound.width);
    }
    
}
