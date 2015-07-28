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
public class GaugeTypeInfo {
    private static final Map<CustomGaugeType, Map<Float ,GaugeTypeInfo>> MAPPING_GAUGE_TYPE_INFO = new HashMap<CustomGaugeType, Map<Float ,GaugeTypeInfo>>();

    private final Map<Float, CustomGaugeType> mappingRatioCustomGaugeType = new HashMap<Float, CustomGaugeType>();

    /** The reference gauge type. */
    public final CustomGaugeType gaugeType;
    /**
     * The marged gauge type used for the computations. Represent the background.
     * 
     * @see GaugeTypeUtil#FRAME_MARGING
     * @see GaugeTypeUtil#FRAME_THETA_MARGING
     */
    public final CustomGaugeType gaugeTypeMarged;
    /**
     * The gauge type represent the outer frame.
     */
    public final CustomGaugeType gaugeTypeExtenal;

    /**
     * The frame thickness.
     */
    public final float frameThickness;

    /**
     * The start angle in classic trigonometric referential.
     * The angle is in radiant between 0 and 2 * PI.
     */
    public final double startAngle;
    /**
     * The end angle in classic trigonometric referential.
     * The angle is in radiant between 0 and 2 * PI.
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
    public static GaugeTypeInfo getGaugeTypeInfo(final GaugeType gaugeType, final CustomGaugeType customGaugeType, final float frameThickness) {
        if (gaugeType == GaugeType.CUSTOM) {
            return getGaugeTypeInfo(customGaugeType, frameThickness);
        } else {
            return getGaugeTypeInfo(new CustomGaugeType(gaugeType.FREE_AREA_ANGLE, gaugeType.ROTATION_OFFSET, gaugeType.TICKMARK_OFFSET,
                    gaugeType.TICKLABEL_ORIENTATION_CHANGE_ANGLE, gaugeType.ANGLE_RANGE, gaugeType.ORIGIN_CORRECTION, gaugeType.APEX_ANGLE,
                    gaugeType.BARGRAPH_OFFSET, gaugeType.LCD_FACTORS, gaugeType.POST_POSITIONS), frameThickness);
        }
    }

    /**
     * Retrieve or build the Gauge Type Info instance that is linked to the given gauge type.
     * 
     * @param gaugeType the reference gauge type.
     * @return an instance of Gauge Type Info that match to the given gauge type.
     */
    public static synchronized GaugeTypeInfo getGaugeTypeInfo(final CustomGaugeType gaugeType, final float frameThickness) {
        if (!MAPPING_GAUGE_TYPE_INFO.containsKey(gaugeType)) {
            MAPPING_GAUGE_TYPE_INFO.put(gaugeType, new HashMap<Float, GaugeTypeInfo>());
        }
        if (!MAPPING_GAUGE_TYPE_INFO.get(gaugeType).containsKey(frameThickness))
        {
            MAPPING_GAUGE_TYPE_INFO.get(gaugeType).put(frameThickness, new GaugeTypeInfo(gaugeType, frameThickness));
        }
        return MAPPING_GAUGE_TYPE_INFO.get(gaugeType).get(frameThickness);
    }

    /**
     * Build a Gauge Type info from the given gauge type.
     * 
     * @param gaugeType the reference gauge type.
     */
    private GaugeTypeInfo(final CustomGaugeType gaugeType, final float frameThickness) {
        if (gaugeType == null) {
            this.gaugeType = new CustomGaugeType(180, 270);
        } else {
            this.gaugeType = gaugeType;
        }
        this.frameThickness = frameThickness;

        gaugeTypeMarged = new CustomGaugeType(Math.toDegrees(this.gaugeType.ANGLE_RANGE + 2 * GaugeTypeUtil.FRAME_THETA_MARGING_RAD),
                Math.toDegrees(this.gaugeType.ROTATION_OFFSET - GaugeTypeUtil.FRAME_THETA_MARGING_RAD), this.gaugeType.POST_POSITIONS);
        gaugeTypeExtenal = computeGaugeTypeExtenal(1);

        startAngle = GaugeTypeUtil.toTrigoAngle(gaugeTypeExtenal.ROTATION_OFFSET);
        endAngle = GaugeTypeUtil.roundTrigoValue(startAngle - gaugeTypeExtenal.ANGLE_RANGE);

        cosStartAngle = Math.cos(startAngle);
        sinStartAngle = Math.sin(startAngle);

        cosEndAngle = Math.cos(endAngle);
        sinEndAngle = Math.sin(endAngle);

        maxCos = Math.max(cosStartAngle, cosEndAngle);
        minCos = Math.min(cosStartAngle, cosEndAngle);

        maxSin = Math.max(sinStartAngle, sinEndAngle);
        minSin = Math.min(sinStartAngle, sinEndAngle);

        northInRange = GaugeTypeUtil.isInRange(gaugeTypeExtenal, Math.PI / 2);
        southInRange = GaugeTypeUtil.isInRange(gaugeTypeExtenal, Math.PI * 3 / 2);
        westInRange = GaugeTypeUtil.isInRange(gaugeTypeExtenal, Math.PI);
        eastInRange = GaugeTypeUtil.isInRange(gaugeTypeExtenal, 0);

        // Compute ration on x
        leftWidthRatio = GaugeTypeUtil.computeDirectionRatio(westInRange, minCos <= 0, -minCos, frameThickness);
        rightWidthRatio = GaugeTypeUtil.computeDirectionRatio(eastInRange, maxCos >= 0, maxCos, frameThickness);
        widthRatio = (leftWidthRatio + rightWidthRatio) / 2;

        // Compute ration on y
        upHeightRatio = GaugeTypeUtil.computeDirectionRatio(northInRange, maxSin >= 0, maxSin, frameThickness);
        downHeightRatio = GaugeTypeUtil.computeDirectionRatio(southInRange, minSin <= 0, -minSin, frameThickness);
        heightRatio = (upHeightRatio + downHeightRatio) / 2;

        // Compute global ratio
        dimPropRatio = widthRatio / heightRatio;

        // Compute radius factor
        if (rightWidthRatio > GaugeTypeUtil.FRAME_MARGING) {
            widthRadiusFactor = (float) (leftWidthRatio / maxCos / (leftWidthRatio + rightWidthRatio));
        } else {
            widthRadiusFactor = (float) (rightWidthRatio / -minCos / (leftWidthRatio + rightWidthRatio));
        }
    }

    public CustomGaugeType computeGaugeTypeExtenal(final float radiusCurrent) {
        if (mappingRatioCustomGaugeType.containsKey(radiusCurrent)) {
          return mappingRatioCustomGaugeType.get(radiusCurrent);
      }
        CustomGaugeType result = gaugeTypeMarged;
        final float radiusBackground = 1 - frameThickness;
        if (frameThickness > 0 && radiusBackground > 0.5 && radiusBackground < radiusCurrent) {
            // in rad
            final double startAngle = GaugeTypeUtil.toTrigoAngle(gaugeTypeMarged.ROTATION_OFFSET);
            final double range = gaugeTypeMarged.ANGLE_RANGE;
            final double endAngle = GaugeTypeUtil.roundTrigoValue(startAngle - range);
            final boolean northInRange = GaugeTypeUtil.isInRange(gaugeTypeMarged, Math.PI / 2);
            final boolean southInRange = GaugeTypeUtil.isInRange(gaugeTypeMarged, Math.PI * 3 / 2);
            final boolean westInRange = GaugeTypeUtil.isInRange(gaugeTypeMarged, Math.PI);
            final boolean eastInRange = GaugeTypeUtil.isInRange(gaugeTypeMarged, 0);

            if (!northInRange || !southInRange || !westInRange || !eastInRange) {
                double computedStartAngle = startAngle;
                double shift = 0;
                final double SHIFT_INC = Math.PI / 180;
                final float thickness = radiusCurrent - radiusBackground;
                if (startAngle < Math.PI / 2) {
                    while (thickness > radiusCurrent * Math.abs(Math.sin(startAngle + shift)) - radiusBackground * Math.abs(Math.sin(startAngle)) && startAngle + shift < Math.PI / 2) {
                        shift += SHIFT_INC;
                    }
                } else if (startAngle < Math.PI) {
                    while (thickness > radiusCurrent * Math.abs(Math.cos(startAngle + shift)) - radiusBackground * Math.abs(Math.cos(startAngle)) && startAngle + shift < Math.PI) {
                        shift += SHIFT_INC;
                    }
                } else if (startAngle < 3 * Math.PI / 2) {
                    while (thickness > radiusCurrent * Math.abs(Math.sin(startAngle + shift)) - radiusBackground * Math.abs(Math.sin(startAngle)) && startAngle + shift < 3 * Math.PI / 2) {
                        shift += SHIFT_INC;
                    }
                } else {
                    while (thickness > radiusCurrent * Math.abs(Math.cos(startAngle + shift)) - radiusBackground * Math.abs(Math.cos(startAngle)) && startAngle + shift < 2 * Math.PI) {
                        shift += SHIFT_INC;
                    }
                }
                computedStartAngle = startAngle + shift;
                double computedRange = range + shift;
                shift = 0;
                if (endAngle < Math.PI / 2) {
                    while (thickness > radiusCurrent * Math.abs(Math.cos(endAngle - shift)) - radiusBackground * Math.abs(Math.cos(endAngle)) && endAngle - shift > 0) {
                        shift += SHIFT_INC;
                    }
                } else if (endAngle < Math.PI) {
                    while (thickness > radiusCurrent * Math.abs(Math.sin(endAngle - shift)) - radiusBackground * Math.abs(Math.sin(endAngle)) && endAngle - shift > Math.PI / 2) {
                        shift += SHIFT_INC;
                    }
                } else if (endAngle < 3 * Math.PI / 2) {
                    while (thickness > radiusCurrent * Math.abs(Math.cos(endAngle - shift)) - radiusBackground * Math.abs(Math.cos(endAngle)) && endAngle - shift > Math.PI) {
                        shift += SHIFT_INC;
                    }
                } else {
                    while (thickness > radiusCurrent * Math.abs(Math.sin(endAngle - shift)) - radiusBackground * Math.abs(Math.sin(endAngle)) && endAngle - shift > 3 * Math.PI / 2) {
                        shift += SHIFT_INC;
                    }
                }
                computedRange += shift;
                result = new CustomGaugeType(Math.toDegrees(computedRange), Math.toDegrees(GaugeTypeUtil.toSteelSeriesAngle(computedStartAngle)), gaugeTypeMarged.POST_POSITIONS);
            }

        }
        mappingRatioCustomGaugeType.put(radiusCurrent, result);
        return result;
    }

}
