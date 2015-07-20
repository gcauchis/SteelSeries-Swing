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

import java.util.HashMap;
import java.util.Map;

/**
 * A {@link CustomGaugeType} helper that contain some usefull values.
 * 
 * @author Gerrit Grunwald <han.solo at muenster.de> and Gabriel Cauchis <gabriel.cauchis at gmail.com>
 */
public class GaugeTypeInfo
{
    private static final Map<CustomGaugeType, GaugeTypeInfo> MAPPING_GAUGE_TYPE_INFO = new HashMap<CustomGaugeType, GaugeTypeInfo>();
    
    /** The reference gauge type. */
    public final CustomGaugeType gaugeType;
    /**
     * The marged gauge type used for the computations.
     * 
     * @see GaugeTypeUtil#FRAME_MARGING
     * @see GaugeTypeUtil#FRAME_THETA_MARGING
     */
    public final CustomGaugeType gaugeTypeMarged;
    
    /** 
     * The start angle in classic trigonometric referential.
     * The angle is in radians between 0 and 2 * PI.
     */
    public final double startAngle;
    /** 
     * The end angle in classic trigonometric referential.
     * The angle is in radians between 0 and 2 * PI.
     */
    public final double endAngle;
    
    /** The cosinus of the start angle. */
    public final double cosStartAngle;
    /** The sinus of the start angle. */
    public final double sinStartAngle;
    
    /** The cosinus of the end angle. */
    public final double cosEndAngle;
    /** The sinus of the end angle. */
    public final double sinEndAngle;
    
    /** The max cosinus value from start and end angle. */
    public final double maxCos;
    /** The min cosinus value from start and end angle. */
    public final double minCos;
    
    /** The max sinus value from start and end angle. */
    public final double maxSin;
    /** The min sinus value from start and end angle. */
    public final double minSin;
    
    /** True if the north axe is entirely in the view. */
    public final boolean northInRange;
    /** True if the south axe is entirely in the view. */
    public final boolean southInRange;
    /** True if the east axe is entirely in the view. */
    public final boolean westInRange;
    /** True if the west axe is entirely in the view. */
    public final boolean eastInRange;
    
    /** The ratio left width from a square. */
    public final double leftWidthRatio;
    /** The ratio right width from a square. */
    public final double rightWidthRatio;
    /** The ratio width from a square. */
    public final double widthRatio;
    
    /** The ratio height top from a square. */
    public final double upHeightRatio;
    /** The ratio height down from a square. */
    public final double downHeightRatio;
    /** The ratio height from a square. */
    public final double heightRatio;
    
    /** The ratio width / height. */
    public final double dimPropRatio;
    /** The radius factor with the width. (radius = widthRadiusFactor * width) */
    public final float widthRadiusFactor;
    
    /**
     * Retrieve or build the Gauge Type Info instance that is linked to the given gauge type.
     * 
     * @param gaugeType the reference gauge type.
     * @return an instance of Gauge Type Info that match to the given gauge type.
     */
    public static GaugeTypeInfo getGaugeTypeInfo(GaugeType gaugeType, CustomGaugeType customGaugeType) {
        if (gaugeType == GaugeType.CUSTOM) {
            return getGaugeTypeInfo(customGaugeType);
        } else {
            return getGaugeTypeInfo(new CustomGaugeType(gaugeType.FREE_AREA_ANGLE,
                    gaugeType.ROTATION_OFFSET,
                    gaugeType.TICKMARK_OFFSET,
                    gaugeType.TICKLABEL_ORIENTATION_CHANGE_ANGLE,
                    gaugeType.ANGLE_RANGE,
                    gaugeType.ORIGIN_CORRECTION,
                    gaugeType.APEX_ANGLE,
                    gaugeType.BARGRAPH_OFFSET,
                    gaugeType.LCD_FACTORS,
                    gaugeType.POST_POSITIONS));
        }
    }
    
    /**
     * Retrieve or build the Gauge Type Info instance that is linked to the given gauge type.
     * 
     * @param gaugeType the reference gauge type.
     * @return an instance of Gauge Type Info that match to the given gauge type.
     */
    public static GaugeTypeInfo getGaugeTypeInfo(CustomGaugeType gaugeType) {
        if (!MAPPING_GAUGE_TYPE_INFO.containsKey(gaugeType)) {
            MAPPING_GAUGE_TYPE_INFO.put(gaugeType, new GaugeTypeInfo(gaugeType));
        }
        return MAPPING_GAUGE_TYPE_INFO.get(gaugeType);
    }
    
    /**
     * Build a Gauge Type info from the given gauge type.
     * 
     * @param gaugeType the reference gauge type.
     */
    private GaugeTypeInfo(final CustomGaugeType gaugeType) {
        if (gaugeType == null) {
            this.gaugeType = new CustomGaugeType(180, 270);
        } else {
            this.gaugeType = gaugeType;
        }
        
        gaugeTypeMarged = new CustomGaugeType(Math.toDegrees(this.gaugeType.ANGLE_RANGE + 2 * GaugeTypeUtil.FRAME_THETA_MARGING_RAD),
                Math.toDegrees(this.gaugeType.ROTATION_OFFSET - GaugeTypeUtil.FRAME_THETA_MARGING_RAD),
                this.gaugeType.POST_POSITIONS);
        
        startAngle = GaugeTypeUtil.toTrigoAngle(gaugeTypeMarged.ROTATION_OFFSET);
        endAngle = GaugeTypeUtil.roundTrigoValue(startAngle - gaugeTypeMarged.ANGLE_RANGE);
        
        cosStartAngle = Math.cos(startAngle);
        sinStartAngle = Math.sin(startAngle);
        
        cosEndAngle = Math.cos(endAngle);
        sinEndAngle = Math.sin(endAngle);
        
        maxCos = Math.max(cosStartAngle, cosEndAngle);
        minCos = Math.min(cosStartAngle, cosEndAngle);
        
        maxSin = Math.max(sinStartAngle, sinEndAngle);
        minSin = Math.min(sinStartAngle, sinEndAngle);
        
        northInRange = GaugeTypeUtil.isInRange(gaugeTypeMarged, Math.PI / 2);
        southInRange = GaugeTypeUtil.isInRange(gaugeTypeMarged, Math.PI * 3 / 2);
        westInRange = GaugeTypeUtil.isInRange(gaugeTypeMarged, Math.PI);
        eastInRange = GaugeTypeUtil.isInRange(gaugeTypeMarged, 0);
        
        //Compute ration on x
        leftWidthRatio = GaugeTypeUtil.computeDirectionRatio(westInRange, minCos <= 0, -minCos);
        rightWidthRatio = GaugeTypeUtil.computeDirectionRatio(eastInRange, maxCos >= 0, maxCos);
        widthRatio = (leftWidthRatio + rightWidthRatio) / 2;
        
        //Compute ration on y
        upHeightRatio = GaugeTypeUtil.computeDirectionRatio(northInRange, maxSin >= 0, maxSin);
        downHeightRatio = GaugeTypeUtil.computeDirectionRatio(southInRange, minSin <= 0, -minSin);
        heightRatio = (upHeightRatio + downHeightRatio) / 2;
        
        //Compute global ratio
        dimPropRatio = widthRatio / heightRatio;
        
        //Compute radius factor
        if (rightWidthRatio > GaugeTypeUtil.FRAME_MARGING) {
            widthRadiusFactor = (float) (leftWidthRatio / maxCos / (leftWidthRatio + rightWidthRatio));
        } else {
            widthRadiusFactor = (float) (rightWidthRatio / -minCos / (leftWidthRatio + rightWidthRatio));
        }
    }

}
