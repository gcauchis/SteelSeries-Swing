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
package eu.hansolo.steelseries.gauges;

import eu.hansolo.steelseries.tools.BackgroundColor;
import eu.hansolo.steelseries.tools.ConicalGradientPaint;
import eu.hansolo.steelseries.tools.CustomGaugeType;
import eu.hansolo.steelseries.tools.FrameDesign;
import eu.hansolo.steelseries.tools.GaugeType;
import eu.hansolo.steelseries.tools.GaugeTypeInfo;
import eu.hansolo.steelseries.tools.GaugeTypeUtil;
import eu.hansolo.steelseries.tools.GradientWrapper;
import eu.hansolo.steelseries.tools.KnobType;
import eu.hansolo.steelseries.tools.LcdColor;
import eu.hansolo.steelseries.tools.MeasuredValueImageFactory;
import eu.hansolo.steelseries.tools.Model;
import eu.hansolo.steelseries.tools.NumberSystem;
import eu.hansolo.steelseries.tools.Orientation;
import eu.hansolo.steelseries.tools.PostPosition;
import eu.hansolo.steelseries.tools.Scaler;
import eu.hansolo.steelseries.tools.Section;
import eu.hansolo.steelseries.tools.Shadow;
import eu.hansolo.steelseries.tools.Util;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.LinearGradientPaint;
import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.TexturePaint;
import java.awt.Transparency;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;
import java.util.Random;


/**
 * A {@link Radial} who is adjusted in size to fit the custom gauge type.
 * 
 * @author Gerrit Grunwald <han.solo at muenster.de> and Gabriel Cauchis <gabriel.cauchis at gmail.com>
 */
public class RadialCustom extends AbstractRadial {
    // <editor-fold defaultstate="collapsed" desc="Variable declarations">
    protected static final int BASE = 10;
    private BufferedImage bImage;
    private BufferedImage fImage;
    private BufferedImage glowImageOff;
    private BufferedImage glowImageOn;
    private BufferedImage pointerImage;
    private BufferedImage pointerShadowImage;
    private BufferedImage thresholdImage;
    private BufferedImage minMeasuredImage;
    private BufferedImage maxMeasuredImage;
    private BufferedImage lcdThresholdImage;
    private BufferedImage disabledImage;
    private double angle;
    protected final Point2D CENTER = new Point2D.Double();
    private final Rectangle2D LCD = new Rectangle2D.Double();
    private boolean section3DEffectVisible;
    private RadialGradientPaint section3DEffect;
    private boolean area3DEffectVisible;
    private RadialGradientPaint area3DEffect;
    private final Point2D TRACK_OFFSET = new Point2D.Double();
    private final FontRenderContext RENDER_CONTEXT = new FontRenderContext(null, true, true);
    private TextLayout unitLayout;
    private final Rectangle2D UNIT_BOUNDARY = new Rectangle2D.Double();
    private double unitStringWidth;
    private TextLayout valueLayout;
    private final Rectangle2D VALUE_BOUNDARY = new Rectangle2D.Double();
    private TextLayout infoLayout;
    private final Rectangle2D INFO_BOUNDARY = new Rectangle2D.Double();
    private Area areaOfMeasuredValues;
    private Area lcdArea;
    private final Point2D pointerImageOrigin = new Point2D.Double();
    private final Point2D measuredValueOffset = new Point2D.Double();
    
    private final float outerFrameScale = 1f;
    private float frameThikness = 0.08f;
    private float mainFrameScale = outerFrameScale - 0.02f;
    private float backgroundScale = outerFrameScale - frameThikness;
    private float innerFrameScale = backgroundScale + 0.02f;
    private float innerFrameGlossy1 = innerFrameScale * 1.01f;
    private float innerFrameGlossy2 = innerFrameScale * 1.005f;
    private float tickMarkScale = backgroundScale * 0.93f;
    private float tickMarkLabelDistanceFactor = 0.09f;
    
    private boolean tickLabelIn = true;
    
    private static final Color DARK_NOISE = new Color(0.2f, 0.2f, 0.2f);
    private static final Color BRIGHT_NOISE = new Color(0.8f, 0.8f, 0.8f);
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Constructor">
    public RadialCustom() {
        super();
        angle = 0;
        section3DEffectVisible = false;
        area3DEffectVisible = false;
//        init(getInnerBounds().width, getInnerBounds().height);
    }

    public RadialCustom(final Model MODEL) {
        super();
        setModel(MODEL);
        angle = 0;
        section3DEffectVisible = false;
        area3DEffectVisible = false;
        areaOfMeasuredValues = new Area();
        lcdArea = new Area();
        init(getInnerBounds().width, getInnerBounds().height);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Initialization">
    @Override
    public final AbstractGauge init(final int WIDTH, final int HEIGHT) {
        computeScales();
        final int GAUGE_WIDTH = isFrameVisible() ? WIDTH : getGaugeBounds().width;
        final int GAUGE_HEIGHT = isFrameVisible() ? HEIGHT : getGaugeBounds().height;
        final Dimension GAUGE_DIM = new Dimension(GAUGE_WIDTH, GAUGE_HEIGHT);
        setFramelessOffset(getGaugeBounds().x, getGaugeBounds().y);

        CENTER.setLocation(getGaugeBounds().getCenterX() - getInsets().left, getGaugeBounds().getCenterX() - getInsets().top);
        if (getGaugeType() == GaugeType.CUSTOM) {
            GaugeTypeUtil.computeCenter(getGaugeTypeInfo(), GAUGE_DIM, CENTER);
        }
        final float widthRadiusFactor = getWidthRadiusFactor();
//        final float tickMarkRadiusFactor = (float) (widthRadiusFactor * tickMarkScale);
//        System.out.println(String.format("tickMarkRadiusFactor: %f, widthRadiusFactor: %f, backgroundScale: %f, tickMarkScale: %f, frameThikness: %f, GAUGE_WIDTH: %d, GAUGE_WIDTH * widthRadiusFactor: %f, GAUGE_WIDTH * tickMarkRadiusFactor: %f",
//                tickMarkRadiusFactor, widthRadiusFactor, backgroundScale, tickMarkScale, frameThikness, GAUGE_WIDTH, GAUGE_WIDTH * widthRadiusFactor, GAUGE_WIDTH * tickMarkRadiusFactor));
        final double radius = GAUGE_WIDTH * getWidthRadiusFactor();
        final int pointerWidth = (int) (radius * 2);
        final float tickMarkRadiusFactor = (float) (widthRadiusFactor * tickMarkScale);
        final GaugeTypeInfo gaugeTypeInfo = getGaugeTypeInfo();
        
        if (GAUGE_WIDTH <= 1 || GAUGE_HEIGHT <= 1) {
            return this;
        }

        if (isLcdVisible()) {
            if (isDigitalFont()) {
                setLcdValueFont(getModel().getDigitalBaseFont().deriveFont(0.7f * GAUGE_WIDTH * 0.15f));
            } else {
                setLcdValueFont(getModel().getStandardBaseFont().deriveFont(0.625f * GAUGE_WIDTH * 0.15f));
            }

            if (isCustomLcdUnitFontEnabled()) {
                setLcdUnitFont(getCustomLcdUnitFont().deriveFont(0.25f * GAUGE_WIDTH * 0.15f));
            } else {
                setLcdUnitFont(getModel().getStandardBaseFont().deriveFont(0.25f * GAUGE_WIDTH * 0.15f));
            }

            setLcdInfoFont(getModel().getStandardInfoFont().deriveFont(0.15f * GAUGE_WIDTH * 0.15f));
        }
        // Create Background Image
        if (bImage != null) {
            bImage.flush();
        }
        bImage = UTIL.createImage(GAUGE_WIDTH, GAUGE_HEIGHT, Transparency.TRANSLUCENT);

        // Create Foreground Image
        if (fImage != null) {
            fImage.flush();
        }
        fImage = UTIL.createImage(GAUGE_WIDTH, GAUGE_HEIGHT, Transparency.TRANSLUCENT);

        if (isFrameVisible()) {
            create_FRAME_Image(GAUGE_WIDTH, GAUGE_HEIGHT, bImage);
        }

        if (isBackgroundVisible()) {
            create_BACKGROUND_Image(GAUGE_WIDTH, GAUGE_HEIGHT, "", "", bImage);
        }

        if (isGlowVisible()) {
            if (glowImageOff != null) {
                glowImageOff.flush();
            }
            glowImageOff = create_GLOW_Image(GAUGE_WIDTH, getGlowColor(), false, getGaugeType(), true, getOrientation());
            if (glowImageOn != null) {
                glowImageOn.flush();
            }
            glowImageOn = create_GLOW_Image(GAUGE_WIDTH, getGlowColor(), true, getGaugeType(), true, getOrientation());
        } else {
            setGlowPulsating(false);
        }

        if (getPostsVisible()) {
            createPostsImage(GAUGE_WIDTH, fImage, getModel().getPostPosition());
        } else {
            createPostsImage(GAUGE_WIDTH, fImage, new PostPosition[]{PostPosition.CENTER});
        }

        TRACK_OFFSET.setLocation(0, 0);

        if (isTrackVisible()) {
            create_TRACK_Image(GAUGE_WIDTH, getFreeAreaAngle(),
                    getTickmarkOffset(),
                    getMinValue(),
                    getMaxValue(),
                    getAngleStep(),
                    getTrackStart(),
                    getTrackSection(),
                    getTrackStop(),
                    getTrackStartColor(),
                    getTrackSectionColor(),
                    getTrackStopColor(),
                    tickMarkRadiusFactor,
                    CENTER,
                    getTickmarkDirection(),
                    TRACK_OFFSET,
                    bImage);
        }

        // Create areas if not empty
        if (!getAreas().isEmpty()) {
            // Create the sections 3d effect gradient overlay
            if (area3DEffectVisible) {
                area3DEffect = createArea3DEffectGradient(GAUGE_WIDTH, widthRadiusFactor);
            }
            createAreas(bImage);
        }

        // Create sections if not empty
        if (!getSections().isEmpty()) {
            // Create the sections 3d effect gradient overlay
            if (section3DEffectVisible) {
                section3DEffect = createSection3DEffectGradient(GAUGE_WIDTH, widthRadiusFactor);
            }
            createSections(bImage);
        }

        TICKMARK_FACTORY.create_RADIAL_TICKMARKS_Image(GAUGE_WIDTH,
                                                       GAUGE_HEIGHT,
                                                       getModel().getNiceMinValue(),
                                                       getModel().getNiceMaxValue(),
                                                       getModel().getMaxNoOfMinorTicks(),
                                                       getModel().getMaxNoOfMajorTicks(),
                                                       getModel().getMinorTickSpacing(),
                                                       getModel().getMajorTickSpacing(),
                                                       getGaugeType(),
                                                       getCustomGaugeType(),
                                                       getMinorTickmarkType(),
                                                       getMajorTickmarkType(),
                                                       isTickmarksVisible(),
                                                       isTicklabelsVisible(),
                                                       getModel().isMinorTickmarksVisible(),
                                                       getModel().isMajorTickmarksVisible(),
                                                       getLabelNumberFormat(),
                                                       isTickmarkSectionsVisible(),
                                                       getBackgroundColor(),
                                                       getTickmarkColor(),
                                                       isTickmarkColorFromThemeEnabled(),
                                                       getTickmarkSections(),
                                                       isSectionTickmarksOnly(),
                                                       getSections(),
                                                       tickMarkRadiusFactor,
                                                       tickMarkLabelDistanceFactor,
                                                       CENTER,
                                                       new Point2D.Double(0, 0),
                                                       Orientation.NORTH,
                                                       getModel().getTicklabelOrientation(),
                                                       getModel().isNiceScale(),
                                                       getModel().isLogScale(),
                                                       bImage);

        create_TITLE_Image(GAUGE_WIDTH, getTitle(), getUnitString(), bImage);

        if (isLcdVisible()) {
            if (isLcdBackgroundVisible()) {
            createLcdImage(new Rectangle2D.Double(((getGaugeBounds().width - GAUGE_WIDTH * getModel().getLcdFactors().getX()) / 2.0),
                             (getGaugeBounds().height * getModel().getLcdFactors().getY()),
                             (GAUGE_WIDTH * getModel().getLcdFactors().getWidth()),
                             (GAUGE_WIDTH * getModel().getLcdFactors().getHeight())),
                             getLcdColor(),
                             getCustomLcdBackground(),
                             bImage);
            }
            LCD.setRect(((getGaugeBounds().width - GAUGE_WIDTH * getModel().getLcdFactors().getX()) / 2.0), (getGaugeBounds().height * getModel().getLcdFactors().getY()), GAUGE_WIDTH * getModel().getLcdFactors().getWidth(), GAUGE_WIDTH * getModel().getLcdFactors().getHeight());
            lcdArea = new Area(LCD);

            // Create the lcd threshold indicator image
            if (lcdThresholdImage != null) {
                lcdThresholdImage.flush();
            }
            lcdThresholdImage = create_LCD_THRESHOLD_Image((int) (LCD.getHeight() * 0.2045454545), (int) (LCD.getHeight() * 0.2045454545), getLcdColor().TEXT_COLOR);
        }

        pointerImageOrigin.setLocation(0 - (1 - gaugeTypeInfo.leftWidthRatio) * pointerWidth / 2,
                                        0 - (1 - gaugeTypeInfo.upHeightRatio) * pointerWidth / 2);
        initPointer(pointerWidth);

        if (thresholdImage != null) {
            thresholdImage.flush();
        }
        thresholdImage = create_THRESHOLD_Image(GAUGE_WIDTH);

        measuredValueOffset.setLocation(radius * gaugeTypeInfo.leftWidthRatio - pointerWidth * MeasuredValueImageFactory.RATIO_WIDTH / 2,
                                        GAUGE_HEIGHT * (1 - tickMarkScale * 1.07));
        initMaxMeasured(pointerWidth);

        // Calc area of measured values
        if ((getGaugeType() == GaugeType.TYPE3 || getGaugeType() == GaugeType.TYPE4) && isLcdVisible()) {
            areaOfMeasuredValues = new Area(getModel().getRadialShapeOfMeasuredValues());
            areaOfMeasuredValues.subtract(lcdArea);
        } else {
            areaOfMeasuredValues = new Area(getModel().getRadialShapeOfMeasuredValues());
        }

        if (isForegroundVisible()) {
            switch (getFrameType()) {
                case SQUARE:
                    FOREGROUND_FACTORY.createLinearForeground(GAUGE_WIDTH, GAUGE_WIDTH, false, fImage);
                    break;

                case ROUND:

                default:
                    FOREGROUND_FACTORY.createRadialForeground(GAUGE_WIDTH, false, getForegroundType(), fImage);
                    break;
            }
        }

        if (disabledImage != null) {
            disabledImage.flush();
        }
        disabledImage = create_DISABLED_Image(GAUGE_WIDTH);

        setCurrentLedImage(getLedImageOff());

        return this;
    }

    /**
     * Compute scales.<b>
     */
    private void computeScales() {
//        final GaugeTypeInfo gaugeTypeInfo = getGaugeTypeInfo();
        float frameThikness = this.frameThikness;
        if (isFrameVisible()) {
//            if (gaugeTypeInfo.northInRange) {
                backgroundScale = outerFrameScale - frameThikness;
//            } else {
//                backgroundScale = outerFrameScale - frameThikness / 2;
//            }
            mainFrameScale = outerFrameScale - 0.02f;
            innerFrameScale = backgroundScale + 0.02f;
            innerFrameGlossy1 = innerFrameScale * 1.01f;
            innerFrameGlossy2 = innerFrameScale * 1.005f;
        } else {
            frameThikness = 0;
            backgroundScale = outerFrameScale;
            mainFrameScale = outerFrameScale;
            innerFrameScale = outerFrameScale;
            innerFrameGlossy1 = outerFrameScale;
            innerFrameGlossy2 = outerFrameScale;
        }

        if (tickLabelIn) {
            tickMarkScale = backgroundScale * 0.93f;
            tickMarkLabelDistanceFactor = 0.09f;
        } else {
            tickMarkScale = backgroundScale * 0.84f;
            tickMarkLabelDistanceFactor = -0.04f;
        }
    }

    /**
     * Returns the image of the posts for the pointer
     * 
     * @param WIDTH
     * @param POSITIONS
     * @param image
     * @return the post image that is used
     */
    protected BufferedImage createPostsImage(final int WIDTH, BufferedImage image, final PostPosition... POSITIONS) {
        if (WIDTH <= 0) {
            return UTIL.createImage(1, 1, Transparency.TRANSLUCENT);
        }

        if (image == null) {
            image = UTIL.createImage(WIDTH, WIDTH, Transparency.TRANSLUCENT);
        }
        final Graphics2D G2 = image.createGraphics();
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        final int IMAGE_WIDTH = image.getWidth();
        final int IMAGE_HEIGHT = image.getHeight();

        final BufferedImage SINGLE_POST = create_KNOB_Image((int) Math.ceil(WIDTH * 0.03738316893577576), KnobType.SMALL_STD_KNOB, getModel().getKnobStyle());

        List<PostPosition> postPositionList = Arrays.asList(POSITIONS);

        // Draw center knob
        if (postPositionList.contains(PostPosition.CENTER) || postPositionList.contains(PostPosition.LOWER_CENTER)) {
            AffineTransform beforeCenterTranform = G2.getTransform();
            AffineTransform centerTranform = new AffineTransform();
            centerTranform.scale(WIDTH / (double) IMAGE_WIDTH, WIDTH / (double) IMAGE_HEIGHT);
            centerTranform.translate(CENTER.getX() - WIDTH * 0.5, CENTER.getY() - WIDTH * 0.5);
            G2.setTransform(centerTranform);
            switch (getKnobType()) {
                case SMALL_STD_KNOB:

                    final Ellipse2D CENTER_KNOB_FRAME = new Ellipse2D.Double(IMAGE_WIDTH * 0.4579439163208008, IMAGE_HEIGHT * 0.4579439163208008, IMAGE_WIDTH * 0.08411216735839844, IMAGE_HEIGHT * 0.08411216735839844);
                    final Point2D CENTER_KNOB_FRAME_START = new Point2D.Double(0, CENTER_KNOB_FRAME.getBounds2D().getMinY());
                    final Point2D CENTER_KNOB_FRAME_STOP = new Point2D.Double(0, CENTER_KNOB_FRAME.getBounds2D().getMaxY());
                    final float[] CENTER_KNOB_FRAME_FRACTIONS = {
                        0.0f,
                        0.46f,
                        1.0f
                    };
                    final Color[] CENTER_KNOB_FRAME_COLORS = {
                        new Color(180, 180, 180, 255),
                        new Color(63, 63, 63, 255),
                        new Color(40, 40, 40, 255)
                    };
                    Util.INSTANCE.validateGradientPoints(CENTER_KNOB_FRAME_START, CENTER_KNOB_FRAME_STOP);
                    final LinearGradientPaint CENTER_KNOB_FRAME_GRADIENT = new LinearGradientPaint(CENTER_KNOB_FRAME_START, CENTER_KNOB_FRAME_STOP, CENTER_KNOB_FRAME_FRACTIONS, CENTER_KNOB_FRAME_COLORS);
                    G2.setPaint(CENTER_KNOB_FRAME_GRADIENT);
                    G2.fill(CENTER_KNOB_FRAME);

                    final Ellipse2D CENTER_KNOB_MAIN = new Ellipse2D.Double(IMAGE_WIDTH * 0.4672897160053253, IMAGE_HEIGHT * 0.4672897160053253, IMAGE_WIDTH * 0.06542053818702698, IMAGE_HEIGHT * 0.06542053818702698);
                    final Point2D CENTER_KNOB_MAIN_START = new Point2D.Double(0, CENTER_KNOB_MAIN.getBounds2D().getMinY());
                    final Point2D CENTER_KNOB_MAIN_STOP = new Point2D.Double(0, CENTER_KNOB_MAIN.getBounds2D().getMaxY());
                    final float[] CENTER_KNOB_MAIN_FRACTIONS = {
                        0.0f,
                        0.5f,
                        1.0f
                    };

                    final Color[] CENTER_KNOB_MAIN_COLORS;
                    switch (getModel().getKnobStyle()) {
                        case BLACK:
                            CENTER_KNOB_MAIN_COLORS = new Color[]{
                                new Color(0xBFBFBF),
                                new Color(0x2B2A2F),
                                new Color(0x7D7E80)
                            };
                            break;

                        case BRASS:
                            CENTER_KNOB_MAIN_COLORS = new Color[]{
                                new Color(0xDFD0AE),
                                new Color(0x7A5E3E),
                                new Color(0xCFBE9D)
                            };
                            break;

                        case SILVER:

                        default:
                            CENTER_KNOB_MAIN_COLORS = new Color[]{
                                new Color(0xD7D7D7),
                                new Color(0x747474),
                                new Color(0xD7D7D7)
                            };
                            break;
                    }
                    Util.INSTANCE.validateGradientPoints(CENTER_KNOB_MAIN_START, CENTER_KNOB_MAIN_STOP);
                    final LinearGradientPaint CENTER_KNOB_MAIN_GRADIENT = new LinearGradientPaint(CENTER_KNOB_MAIN_START, CENTER_KNOB_MAIN_STOP, CENTER_KNOB_MAIN_FRACTIONS, CENTER_KNOB_MAIN_COLORS);
                    G2.setPaint(CENTER_KNOB_MAIN_GRADIENT);
                    G2.fill(CENTER_KNOB_MAIN);

                    final Ellipse2D CENTER_KNOB_INNERSHADOW = new Ellipse2D.Double(IMAGE_WIDTH * 0.4672897160053253, IMAGE_HEIGHT * 0.4672897160053253, IMAGE_WIDTH * 0.06542053818702698, IMAGE_HEIGHT * 0.06542053818702698);
                    final Point2D CENTER_KNOB_INNERSHADOW_CENTER = new Point2D.Double((0.4953271028037383 * IMAGE_WIDTH), (0.49065420560747663 * IMAGE_HEIGHT));
                    final float[] CENTER_KNOB_INNERSHADOW_FRACTIONS = {
                        0.0f,
                        0.75f,
                        0.76f,
                        1.0f
                    };
                    final Color[] CENTER_KNOB_INNERSHADOW_COLORS = {
                        new Color(0, 0, 0, 0),
                        new Color(0, 0, 0, 0),
                        new Color(0, 0, 0, 1),
                        new Color(0, 0, 0, 51)
                    };
                    final RadialGradientPaint CENTER_KNOB_INNERSHADOW_GRADIENT = new RadialGradientPaint(CENTER_KNOB_INNERSHADOW_CENTER, (float) (0.03271028037383177 * IMAGE_WIDTH), CENTER_KNOB_INNERSHADOW_FRACTIONS, CENTER_KNOB_INNERSHADOW_COLORS);
                    G2.setPaint(CENTER_KNOB_INNERSHADOW_GRADIENT);
                    G2.fill(CENTER_KNOB_INNERSHADOW);
                    break;

                case BIG_STD_KNOB:
                    final Ellipse2D BIGCENTER_BACKGROUNDFRAME = new Ellipse2D.Double(IMAGE_WIDTH * 0.4392523467540741, IMAGE_HEIGHT * 0.4392523467540741, IMAGE_WIDTH * 0.1214953362941742, IMAGE_HEIGHT * 0.1214953362941742);
                    final Point2D BIGCENTER_BACKGROUNDFRAME_START = new Point2D.Double(0, BIGCENTER_BACKGROUNDFRAME.getBounds2D().getMinY());
                    final Point2D BIGCENTER_BACKGROUNDFRAME_STOP = new Point2D.Double(0, BIGCENTER_BACKGROUNDFRAME.getBounds2D().getMaxY());
                    final float[] BIGCENTER_BACKGROUNDFRAME_FRACTIONS = {
                        0.0f,
                        1.0f
                    };

                    final Color[] BIGCENTER_BACKGROUNDFRAME_COLORS;
                    switch (getModel().getKnobStyle()) {
                        case BLACK:
                            BIGCENTER_BACKGROUNDFRAME_COLORS = new Color[]{
                                new Color(129, 133, 136, 255),
                                new Color(61, 61, 73, 255)
                            };
                            break;

                        case BRASS:
                            BIGCENTER_BACKGROUNDFRAME_COLORS = new Color[]{
                                new Color(143, 117, 80, 255),
                                new Color(100, 76, 49, 255)
                            };
                            break;

                        case SILVER:

                        default:
                            BIGCENTER_BACKGROUNDFRAME_COLORS = new Color[]{
                                new Color(152, 152, 152, 255),
                                new Color(118, 121, 126, 255)
                            };
                            break;
                    }
                    Util.INSTANCE.validateGradientPoints(BIGCENTER_BACKGROUNDFRAME_START, BIGCENTER_BACKGROUNDFRAME_STOP);
                    final LinearGradientPaint BIGCENTER_BACKGROUNDFRAME_GRADIENT = new LinearGradientPaint(BIGCENTER_BACKGROUNDFRAME_START, BIGCENTER_BACKGROUNDFRAME_STOP, BIGCENTER_BACKGROUNDFRAME_FRACTIONS, BIGCENTER_BACKGROUNDFRAME_COLORS);
                    G2.setPaint(BIGCENTER_BACKGROUNDFRAME_GRADIENT);
                    G2.fill(BIGCENTER_BACKGROUNDFRAME);

                    final Ellipse2D BIGCENTER_BACKGROUND = new Ellipse2D.Double(IMAGE_WIDTH * 0.44392523169517517, IMAGE_HEIGHT * 0.44392523169517517, IMAGE_WIDTH * 0.11214950680732727, IMAGE_HEIGHT * 0.11214950680732727);
                    final Point2D BIGCENTER_BACKGROUND_START = new Point2D.Double(0, BIGCENTER_BACKGROUND.getBounds2D().getMinY());
                    final Point2D BIGCENTER_BACKGROUND_STOP = new Point2D.Double(0, BIGCENTER_BACKGROUND.getBounds2D().getMaxY());
                    final float[] BIGCENTER_BACKGROUND_FRACTIONS = {
                        0.0f,
                        1.0f
                    };

                    final Color[] BIGCENTER_BACKGROUND_COLORS;
                    switch (getModel().getKnobStyle()) {
                        case BLACK:
                            BIGCENTER_BACKGROUND_COLORS = new Color[]{
                                new Color(26, 27, 32, 255),
                                new Color(96, 97, 102, 255)
                            };
                            break;

                        case BRASS:
                            BIGCENTER_BACKGROUND_COLORS = new Color[]{
                                new Color(98, 75, 49, 255),
                                new Color(149, 109, 54, 255)
                            };
                            break;

                        case SILVER:

                        default:
                            BIGCENTER_BACKGROUND_COLORS = new Color[]{
                                new Color(118, 121, 126, 255),
                                new Color(191, 191, 191, 255)
                            };
                            break;
                    }
                    Util.INSTANCE.validateGradientPoints(BIGCENTER_BACKGROUND_START, BIGCENTER_BACKGROUND_STOP);
                    final LinearGradientPaint BIGCENTER_BACKGROUND_GRADIENT = new LinearGradientPaint(BIGCENTER_BACKGROUND_START, BIGCENTER_BACKGROUND_STOP, BIGCENTER_BACKGROUND_FRACTIONS, BIGCENTER_BACKGROUND_COLORS);
                    G2.setPaint(BIGCENTER_BACKGROUND_GRADIENT);
                    G2.fill(BIGCENTER_BACKGROUND);

                    final Ellipse2D BIGCENTER_FOREGROUNDFRAME = new Ellipse2D.Double(IMAGE_WIDTH * 0.4532710313796997, IMAGE_HEIGHT * 0.4532710313796997, IMAGE_WIDTH * 0.09345793724060059, IMAGE_HEIGHT * 0.09345793724060059);
                    final Point2D BIGCENTER_FOREGROUNDFRAME_START = new Point2D.Double(0, BIGCENTER_FOREGROUNDFRAME.getBounds2D().getMinY());
                    final Point2D BIGCENTER_FOREGROUNDFRAME_STOP = new Point2D.Double(0, BIGCENTER_FOREGROUNDFRAME.getBounds2D().getMaxY());
                    final float[] BIGCENTER_FOREGROUNDFRAME_FRACTIONS = {
                        0.0f,
                        0.47f,
                        1.0f
                    };

                    final Color[] BIGCENTER_FOREGROUNDFRAME_COLORS;
                    switch (getModel().getKnobStyle()) {
                        case BLACK:
                            BIGCENTER_FOREGROUNDFRAME_COLORS = new Color[]{
                                new Color(191, 191, 191, 255),
                                new Color(56, 57, 61, 255),
                                new Color(143, 144, 146, 255)
                            };
                            break;

                        case BRASS:
                            BIGCENTER_FOREGROUNDFRAME_COLORS = new Color[]{
                                new Color(147, 108, 54, 255),
                                new Color(82, 66, 50, 255),
                                new Color(147, 108, 54, 255)
                            };
                            break;

                        case SILVER:

                        default:
                            BIGCENTER_FOREGROUNDFRAME_COLORS = new Color[]{
                                new Color(191, 191, 191, 255),
                                new Color(116, 116, 116, 255),
                                new Color(143, 144, 146, 255)
                            };
                            break;
                    }
                    Util.INSTANCE.validateGradientPoints(BIGCENTER_FOREGROUNDFRAME_START, BIGCENTER_FOREGROUNDFRAME_STOP);
                    final LinearGradientPaint BIGCENTER_FOREGROUNDFRAME_GRADIENT = new LinearGradientPaint(BIGCENTER_FOREGROUNDFRAME_START, BIGCENTER_FOREGROUNDFRAME_STOP, BIGCENTER_FOREGROUNDFRAME_FRACTIONS, BIGCENTER_FOREGROUNDFRAME_COLORS);
                    G2.setPaint(BIGCENTER_FOREGROUNDFRAME_GRADIENT);
                    G2.fill(BIGCENTER_FOREGROUNDFRAME);

                    final Ellipse2D BIGCENTER_FOREGROUND = new Ellipse2D.Double(IMAGE_WIDTH * 0.4579439163208008, IMAGE_HEIGHT * 0.4579439163208008, IMAGE_WIDTH * 0.08411216735839844, IMAGE_HEIGHT * 0.08411216735839844);
                    final Point2D BIGCENTER_FOREGROUND_START = new Point2D.Double(0, BIGCENTER_FOREGROUND.getBounds2D().getMinY());
                    final Point2D BIGCENTER_FOREGROUND_STOP = new Point2D.Double(0, BIGCENTER_FOREGROUND.getBounds2D().getMaxY());
                    final float[] BIGCENTER_FOREGROUND_FRACTIONS = {
                        0.0f,
                        0.21f,
                        0.5f,
                        0.78f,
                        1.0f
                    };

                    final Color[] BIGCENTER_FOREGROUND_COLORS;
                    switch (getModel().getKnobStyle()) {
                        case BLACK:
                            BIGCENTER_FOREGROUND_COLORS = new Color[]{
                                new Color(191, 191, 191, 255),
                                new Color(94, 93, 99, 255),
                                new Color(43, 42, 47, 255),
                                new Color(78, 79, 81, 255),
                                new Color(143, 144, 146, 255)
                            };
                            break;

                        case BRASS:
                            BIGCENTER_FOREGROUND_COLORS = new Color[]{
                                new Color(223, 208, 174, 255),
                                new Color(159, 136, 104, 255),
                                new Color(122, 94, 62, 255),
                                new Color(159, 136, 104, 255),
                                new Color(223, 208, 174, 255)
                            };
                            break;

                        case SILVER:

                        default:
                            BIGCENTER_FOREGROUND_COLORS = new Color[]{
                                new Color(215, 215, 215, 255),
                                new Color(139, 142, 145, 255),
                                new Color(100, 100, 100, 255),
                                new Color(139, 142, 145, 255),
                                new Color(215, 215, 215, 255)
                            };
                            break;
                    }
                    Util.INSTANCE.validateGradientPoints(BIGCENTER_FOREGROUND_START, BIGCENTER_FOREGROUND_STOP);
                    final LinearGradientPaint BIGCENTER_FOREGROUND_GRADIENT = new LinearGradientPaint(BIGCENTER_FOREGROUND_START, BIGCENTER_FOREGROUND_STOP, BIGCENTER_FOREGROUND_FRACTIONS, BIGCENTER_FOREGROUND_COLORS);
                    G2.setPaint(BIGCENTER_FOREGROUND_GRADIENT);
                    G2.fill(BIGCENTER_FOREGROUND);
                    break;

                case BIG_CHROME_KNOB:
                    final Ellipse2D CHROMEKNOB_BACKFRAME = new Ellipse2D.Double(IMAGE_WIDTH * 0.42990654706954956, IMAGE_HEIGHT * 0.42990654706954956, IMAGE_WIDTH * 0.14018690586090088, IMAGE_HEIGHT * 0.14018690586090088);
                    final Point2D CHROMEKNOB_BACKFRAME_START = new Point2D.Double((0.46261682242990654 * IMAGE_WIDTH), (0.4392523364485981 * IMAGE_HEIGHT));
                    final Point2D CHROMEKNOB_BACKFRAME_STOP = new Point2D.Double(((0.46261682242990654 + 0.0718114890783315) * IMAGE_WIDTH), ((0.4392523364485981 + 0.1149224055539082) * IMAGE_HEIGHT));
                    final float[] CHROMEKNOB_BACKFRAME_FRACTIONS = {
                        0.0f,
                        1.0f
                    };
                    final Color[] CHROMEKNOB_BACKFRAME_COLORS = {
                        new Color(129, 139, 140, 255),
                        new Color(166, 171, 175, 255)
                    };
                    Util.INSTANCE.validateGradientPoints(CHROMEKNOB_BACKFRAME_START, CHROMEKNOB_BACKFRAME_STOP);
                    final LinearGradientPaint CHROMEKNOB_BACKFRAME_GRADIENT = new LinearGradientPaint(CHROMEKNOB_BACKFRAME_START, CHROMEKNOB_BACKFRAME_STOP, CHROMEKNOB_BACKFRAME_FRACTIONS, CHROMEKNOB_BACKFRAME_COLORS);
                    G2.setPaint(CHROMEKNOB_BACKFRAME_GRADIENT);
                    G2.fill(CHROMEKNOB_BACKFRAME);

                    final Ellipse2D CHROMEKNOB_BACK = new Ellipse2D.Double(IMAGE_WIDTH * 0.43457943201065063, IMAGE_HEIGHT * 0.43457943201065063, IMAGE_WIDTH * 0.13084113597869873, IMAGE_HEIGHT * 0.13084113597869873);
                    final Point2D CHROMEKNOB_BACK_CENTER = new Point2D.Double(CHROMEKNOB_BACK.getCenterX(), CHROMEKNOB_BACK.getCenterY());
                    final float[] CHROMEKNOB_BACK_FRACTIONS = {
                        0.0f,
                        0.09f,
                        0.12f,
                        0.16f,
                        0.25f,
                        0.29f,
                        0.33f,
                        0.38f,
                        0.48f,
                        0.52f,
                        0.65f,
                        0.69f,
                        0.8f,
                        0.83f,
                        0.87f,
                        0.97f,
                        1.0f
                    };
                    final Color[] CHROMEKNOB_BACK_COLORS = {
                        new Color(255, 255, 255, 255),
                        new Color(255, 255, 255, 255),
                        new Color(136, 136, 138, 255),
                        new Color(164, 185, 190, 255),
                        new Color(158, 179, 182, 255),
                        new Color(112, 112, 112, 255),
                        new Color(221, 227, 227, 255),
                        new Color(155, 176, 179, 255),
                        new Color(156, 176, 177, 255),
                        new Color(254, 255, 255, 255),
                        new Color(255, 255, 255, 255),
                        new Color(156, 180, 180, 255),
                        new Color(198, 209, 211, 255),
                        new Color(246, 248, 247, 255),
                        new Color(204, 216, 216, 255),
                        new Color(164, 188, 190, 255),
                        new Color(255, 255, 255, 255)
                    };
                    final ConicalGradientPaint CHROMEKNOB_BACK_GRADIENT = new ConicalGradientPaint(false, CHROMEKNOB_BACK_CENTER, 0, CHROMEKNOB_BACK_FRACTIONS, CHROMEKNOB_BACK_COLORS);
                    G2.setPaint(CHROMEKNOB_BACK_GRADIENT);
                    G2.fill(CHROMEKNOB_BACK);

                    final Ellipse2D CHROMEKNOB_FOREFRAME = new Ellipse2D.Double(IMAGE_WIDTH * 0.4672897160053253, IMAGE_HEIGHT * 0.4672897160053253, IMAGE_WIDTH * 0.06542053818702698, IMAGE_HEIGHT * 0.06542053818702698);
                    final Point2D CHROMEKNOB_FOREFRAME_START = new Point2D.Double((0.48130841121495327 * IMAGE_WIDTH), (0.4719626168224299 * IMAGE_HEIGHT));
                    final Point2D CHROMEKNOB_FOREFRAME_STOP = new Point2D.Double(((0.48130841121495327 + 0.033969662360372466) * IMAGE_WIDTH), ((0.4719626168224299 + 0.05036209552904459) * IMAGE_HEIGHT));
                    final float[] CHROMEKNOB_FOREFRAME_FRACTIONS = {
                        0.0f,
                        1.0f
                    };
                    final Color[] CHROMEKNOB_FOREFRAME_COLORS = {
                        new Color(225, 235, 232, 255),
                        new Color(196, 207, 207, 255)
                    };
                    Util.INSTANCE.validateGradientPoints(CHROMEKNOB_FOREFRAME_START, CHROMEKNOB_FOREFRAME_STOP);
                    final LinearGradientPaint CHROMEKNOB_FOREFRAME_GRADIENT = new LinearGradientPaint(CHROMEKNOB_FOREFRAME_START, CHROMEKNOB_FOREFRAME_STOP, CHROMEKNOB_FOREFRAME_FRACTIONS, CHROMEKNOB_FOREFRAME_COLORS);
                    G2.setPaint(CHROMEKNOB_FOREFRAME_GRADIENT);
                    G2.fill(CHROMEKNOB_FOREFRAME);

                    final Ellipse2D CHROMEKNOB_FORE = new Ellipse2D.Double(IMAGE_WIDTH * 0.4719626307487488, IMAGE_HEIGHT * 0.4719626307487488, IMAGE_WIDTH * 0.05607473850250244, IMAGE_HEIGHT * 0.05607473850250244);
                    final Point2D CHROMEKNOB_FORE_START = new Point2D.Double((0.48130841121495327 * IMAGE_WIDTH), (0.4766355140186916 * IMAGE_HEIGHT));
                    final Point2D CHROMEKNOB_FORE_STOP = new Point2D.Double(((0.48130841121495327 + 0.03135661140957459) * IMAGE_WIDTH), ((0.4766355140186916 + 0.04648808818065655) * IMAGE_HEIGHT));
                    final float[] CHROMEKNOB_FORE_FRACTIONS = {
                        0.0f,
                        1.0f
                    };
                    final Color[] CHROMEKNOB_FORE_COLORS = {
                        new Color(237, 239, 237, 255),
                        new Color(148, 161, 161, 255)
                    };
                    Util.INSTANCE.validateGradientPoints(CHROMEKNOB_FORE_START, CHROMEKNOB_FORE_STOP);
                    final LinearGradientPaint CHROMEKNOB_FORE_GRADIENT = new LinearGradientPaint(CHROMEKNOB_FORE_START, CHROMEKNOB_FORE_STOP, CHROMEKNOB_FORE_FRACTIONS, CHROMEKNOB_FORE_COLORS);
                    G2.setPaint(CHROMEKNOB_FORE_GRADIENT);
                    G2.fill(CHROMEKNOB_FORE);
                    break;

                case METAL_KNOB:
                    final Ellipse2D METALKNOB_FRAME = new Ellipse2D.Double(IMAGE_WIDTH * 0.4579439163208008, IMAGE_HEIGHT * 0.4579439163208008, IMAGE_WIDTH * 0.08411216735839844, IMAGE_HEIGHT * 0.08411216735839844);
                    final Point2D METALKNOB_FRAME_START = new Point2D.Double(0, METALKNOB_FRAME.getBounds2D().getMinY());
                    final Point2D METALKNOB_FRAME_STOP = new Point2D.Double(0, METALKNOB_FRAME.getBounds2D().getMaxY());
                    final float[] METALKNOB_FRAME_FRACTIONS = {
                        0.0f,
                        0.47f,
                        1.0f
                    };
                    final Color[] METALKNOB_FRAME_COLORS = {
                        new Color(92, 95, 101, 255),
                        new Color(46, 49, 53, 255),
                        new Color(22, 23, 26, 255)
                    };
                    Util.INSTANCE.validateGradientPoints(METALKNOB_FRAME_START, METALKNOB_FRAME_STOP);
                    final LinearGradientPaint METALKNOB_FRAME_GRADIENT = new LinearGradientPaint(METALKNOB_FRAME_START, METALKNOB_FRAME_STOP, METALKNOB_FRAME_FRACTIONS, METALKNOB_FRAME_COLORS);
                    G2.setPaint(METALKNOB_FRAME_GRADIENT);
                    G2.fill(METALKNOB_FRAME);

                    final Ellipse2D METALKNOB_MAIN = new Ellipse2D.Double(IMAGE_WIDTH * 0.46261683106422424, IMAGE_HEIGHT * 0.46261683106422424, IMAGE_WIDTH * 0.0747663676738739, IMAGE_HEIGHT * 0.0747663676738739);
                    final Point2D METALKNOB_MAIN_START = new Point2D.Double(0, METALKNOB_MAIN.getBounds2D().getMinY());
                    final Point2D METALKNOB_MAIN_STOP = new Point2D.Double(0, METALKNOB_MAIN.getBounds2D().getMaxY());
                    final float[] METALKNOB_MAIN_FRACTIONS = {
                        0.0f,
                        1.0f
                    };
                    final Color[] METALKNOB_MAIN_COLORS;
                    switch (getModel().getKnobStyle()) {
                        case BLACK:
                            METALKNOB_MAIN_COLORS = new Color[]{
                                new Color(0x2B2A2F),
                                new Color(0x1A1B20)
                            };
                            break;

                        case BRASS:
                            METALKNOB_MAIN_COLORS = new Color[]{
                                new Color(0x966E36),
                                new Color(0x7C5F3D)
                            };
                            break;

                        case SILVER:

                        default:
                            METALKNOB_MAIN_COLORS = new Color[]{
                                new Color(204, 204, 204, 255),
                                new Color(87, 92, 98, 255)
                            };
                            break;
                    }
                    Util.INSTANCE.validateGradientPoints(METALKNOB_MAIN_START, METALKNOB_MAIN_STOP);
                    final LinearGradientPaint METALKNOB_MAIN_GRADIENT = new LinearGradientPaint(METALKNOB_MAIN_START, METALKNOB_MAIN_STOP, METALKNOB_MAIN_FRACTIONS, METALKNOB_MAIN_COLORS);
                    G2.setPaint(METALKNOB_MAIN_GRADIENT);
                    G2.fill(METALKNOB_MAIN);

                    final GeneralPath METALKNOB_LOWERHL = new GeneralPath();
                    METALKNOB_LOWERHL.setWindingRule(Path2D.WIND_EVEN_ODD);
                    METALKNOB_LOWERHL.moveTo(IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.5280373831775701);
                    METALKNOB_LOWERHL.curveTo(IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.5186915887850467, IMAGE_WIDTH * 0.5093457943925234, IMAGE_HEIGHT * 0.514018691588785, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.514018691588785);
                    METALKNOB_LOWERHL.curveTo(IMAGE_WIDTH * 0.48598130841121495, IMAGE_HEIGHT * 0.514018691588785, IMAGE_WIDTH * 0.4766355140186916, IMAGE_HEIGHT * 0.5186915887850467, IMAGE_WIDTH * 0.4766355140186916, IMAGE_HEIGHT * 0.5280373831775701);
                    METALKNOB_LOWERHL.curveTo(IMAGE_WIDTH * 0.48130841121495327, IMAGE_HEIGHT * 0.5327102803738317, IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.5373831775700935, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.5373831775700935);
                    METALKNOB_LOWERHL.curveTo(IMAGE_WIDTH * 0.5046728971962616, IMAGE_HEIGHT * 0.5373831775700935, IMAGE_WIDTH * 0.514018691588785, IMAGE_HEIGHT * 0.5327102803738317, IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.5280373831775701);
                    METALKNOB_LOWERHL.closePath();
                    final Point2D METALKNOB_LOWERHL_CENTER = new Point2D.Double((0.5 * IMAGE_WIDTH), (0.5373831775700935 * IMAGE_HEIGHT));
                    final float[] METALKNOB_LOWERHL_FRACTIONS = {
                        0.0f,
                        1.0f
                    };
                    final Color[] METALKNOB_LOWERHL_COLORS = {
                        new Color(255, 255, 255, 153),
                        new Color(255, 255, 255, 0)
                    };
                    final RadialGradientPaint METALKNOB_LOWERHL_GRADIENT = new RadialGradientPaint(METALKNOB_LOWERHL_CENTER, (float) (0.03271028037383177 * IMAGE_WIDTH), METALKNOB_LOWERHL_FRACTIONS, METALKNOB_LOWERHL_COLORS);
                    G2.setPaint(METALKNOB_LOWERHL_GRADIENT);
                    G2.fill(METALKNOB_LOWERHL);

                    final GeneralPath METALKNOB_UPPERHL = new GeneralPath();
                    METALKNOB_UPPERHL.setWindingRule(Path2D.WIND_EVEN_ODD);
                    METALKNOB_UPPERHL.moveTo(IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.48130841121495327);
                    METALKNOB_UPPERHL.curveTo(IMAGE_WIDTH * 0.5280373831775701, IMAGE_HEIGHT * 0.4672897196261682, IMAGE_WIDTH * 0.514018691588785, IMAGE_HEIGHT * 0.45794392523364486, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.45794392523364486);
                    METALKNOB_UPPERHL.curveTo(IMAGE_WIDTH * 0.48130841121495327, IMAGE_HEIGHT * 0.45794392523364486, IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.4672897196261682, IMAGE_WIDTH * 0.46261682242990654, IMAGE_HEIGHT * 0.48130841121495327);
                    METALKNOB_UPPERHL.curveTo(IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.48598130841121495, IMAGE_WIDTH * 0.48130841121495327, IMAGE_HEIGHT * 0.49065420560747663, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.49065420560747663);
                    METALKNOB_UPPERHL.curveTo(IMAGE_WIDTH * 0.514018691588785, IMAGE_HEIGHT * 0.49065420560747663, IMAGE_WIDTH * 0.5280373831775701, IMAGE_HEIGHT * 0.48598130841121495, IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.48130841121495327);
                    METALKNOB_UPPERHL.closePath();
                    final Point2D METALKNOB_UPPERHL_CENTER = new Point2D.Double((0.4953271028037383 * IMAGE_WIDTH), (0.45794392523364486 * IMAGE_HEIGHT));
                    final float[] METALKNOB_UPPERHL_FRACTIONS = {
                        0.0f,
                        1.0f
                    };
                    final Color[] METALKNOB_UPPERHL_COLORS = {
                        new Color(255, 255, 255, 191),
                        new Color(255, 255, 255, 0)
                    };
                    final RadialGradientPaint METALKNOB_UPPERHL_GRADIENT = new RadialGradientPaint(METALKNOB_UPPERHL_CENTER, (float) (0.04906542056074766 * IMAGE_WIDTH), METALKNOB_UPPERHL_FRACTIONS, METALKNOB_UPPERHL_COLORS);
                    G2.setPaint(METALKNOB_UPPERHL_GRADIENT);
                    G2.fill(METALKNOB_UPPERHL);

                    final Ellipse2D METALKNOB_INNERFRAME = new Ellipse2D.Double(IMAGE_WIDTH * 0.47663551568984985, IMAGE_HEIGHT * 0.4813084006309509, IMAGE_WIDTH * 0.04205608367919922, IMAGE_HEIGHT * 0.04205608367919922);
                    final Point2D METALKNOB_INNERFRAME_START = new Point2D.Double(0, METALKNOB_INNERFRAME.getBounds2D().getMinY());
                    final Point2D METALKNOB_INNERFRAME_STOP = new Point2D.Double(0, METALKNOB_INNERFRAME.getBounds2D().getMaxY());
                    final float[] METALKNOB_INNERFRAME_FRACTIONS = {
                        0.0f,
                        1.0f
                    };
                    final Color[] METALKNOB_INNERFRAME_COLORS = {
                        new Color(0, 0, 0, 255),
                        new Color(204, 204, 204, 255)
                    };
                    Util.INSTANCE.validateGradientPoints(METALKNOB_INNERFRAME_START, METALKNOB_INNERFRAME_STOP);
                    final LinearGradientPaint METALKNOB_INNERFRAME_GRADIENT = new LinearGradientPaint(METALKNOB_INNERFRAME_START, METALKNOB_INNERFRAME_STOP, METALKNOB_INNERFRAME_FRACTIONS, METALKNOB_INNERFRAME_COLORS);
                    G2.setPaint(METALKNOB_INNERFRAME_GRADIENT);
                    G2.fill(METALKNOB_INNERFRAME);

                    final Ellipse2D METALKNOB_INNERBACKGROUND = new Ellipse2D.Double(IMAGE_WIDTH * 0.4813084006309509, IMAGE_HEIGHT * 0.4859813153743744, IMAGE_WIDTH * 0.03271031379699707, IMAGE_HEIGHT * 0.03271028399467468);
                    final Point2D METALKNOB_INNERBACKGROUND_START = new Point2D.Double(0, METALKNOB_INNERBACKGROUND.getBounds2D().getMinY());
                    final Point2D METALKNOB_INNERBACKGROUND_STOP = new Point2D.Double(0, METALKNOB_INNERBACKGROUND.getBounds2D().getMaxY());
                    final float[] METALKNOB_INNERBACKGROUND_FRACTIONS = {
                        0.0f,
                        1.0f
                    };
                    final Color[] METALKNOB_INNERBACKGROUND_COLORS = {
                        new Color(1, 6, 11, 255),
                        new Color(50, 52, 56, 255)
                    };
                    Util.INSTANCE.validateGradientPoints(METALKNOB_INNERBACKGROUND_START, METALKNOB_INNERBACKGROUND_STOP);
                    final LinearGradientPaint METALKNOB_INNERBACKGROUND_GRADIENT = new LinearGradientPaint(METALKNOB_INNERBACKGROUND_START, METALKNOB_INNERBACKGROUND_STOP, METALKNOB_INNERBACKGROUND_FRACTIONS, METALKNOB_INNERBACKGROUND_COLORS);
                    G2.setPaint(METALKNOB_INNERBACKGROUND_GRADIENT);
                    G2.fill(METALKNOB_INNERBACKGROUND);
                    break;
            }
            G2.setTransform(beforeCenterTranform);
        }

//        // Draw min bottom
//        if (postPositionList.contains(PostPosition.MIN_BOTTOM)) {
//            G2.drawImage(SINGLE_POST, (int) (IMAGE_WIDTH * 0.336448609828949), (int) (IMAGE_HEIGHT * 0.8037382960319519), null);
//        }
//
//        // Draw max bottom post
//        if (postPositionList.contains(PostPosition.MAX_BOTTOM)) {
//            G2.drawImage(SINGLE_POST, (int) (IMAGE_WIDTH * 0.6261682510375977), (int) (IMAGE_HEIGHT * 0.8037382960319519), null);
//        }
//
//        // Draw min center bottom post
//        if (postPositionList.contains(PostPosition.MAX_CENTER_BOTTOM)) {
//            G2.drawImage(SINGLE_POST, (int) (IMAGE_WIDTH * 0.5233644843101501), (int) (IMAGE_HEIGHT * 0.8317757248878479), null);
//        }
//
//        // Draw max center top post
//        if (postPositionList.contains(PostPosition.MAX_CENTER_TOP)) {
//            G2.drawImage(SINGLE_POST, (int) (IMAGE_WIDTH * 0.5233644843101501), (int) (IMAGE_HEIGHT * 0.13084112107753754), null);
//        }
//
//        // Draw max right post
//        if (postPositionList.contains(PostPosition.MAX_RIGHT)) {
//            G2.drawImage(SINGLE_POST, (int) (IMAGE_WIDTH * 0.8317757248878479), (int) (IMAGE_HEIGHT * 0.514018714427948), null);
//        }
//
//        // Draw min left post
//        if (postPositionList.contains(PostPosition.MIN_LEFT)) {
//            G2.drawImage(SINGLE_POST, (int) (IMAGE_WIDTH * 0.13084112107753754), (int) (IMAGE_HEIGHT * 0.514018714427948), null);
//        }

//        // Draw lower center post
//        final AffineTransform OLD_TRANSFORM = G2.getTransform();
//        final Point2D KNOB_CENTER = new Point2D.Double();
//
//        // Reset orientation
//        G2.setTransform(OLD_TRANSFORM);
//
//        // Draw radialvertical gauge right post
//        if (postPositionList.contains(PostPosition.SMALL_GAUGE_MAX_RIGHT)) {
//            switch (getOrientation()) {
//                case WEST:
//                    KNOB_CENTER.setLocation(IMAGE_WIDTH * 0.7803738117218018 + SINGLE_POST.getWidth() / 2.0, IMAGE_HEIGHT * 0.44859811663627625 + SINGLE_POST.getHeight() / 2.0);
//                    G2.rotate(Math.PI / 2, KNOB_CENTER.getX(), KNOB_CENTER.getY());
//                    break;
//            }
//            G2.drawImage(SINGLE_POST, (int) (IMAGE_WIDTH * 0.7803738117218018), (int) (IMAGE_HEIGHT * 0.44859811663627625), null);
//            G2.setTransform(OLD_TRANSFORM);
//        }
//
//        // Draw radialvertical gauge left post
//        if (postPositionList.contains(PostPosition.SMALL_GAUGE_MIN_LEFT)) {
//            switch (getOrientation()) {
//                case WEST:
//                    KNOB_CENTER.setLocation(IMAGE_WIDTH * 0.1822429895401001 + SINGLE_POST.getWidth() / 2.0, IMAGE_HEIGHT * 0.44859811663627625 + SINGLE_POST.getHeight() / 2.0);
//                    G2.rotate(Math.PI / 2, KNOB_CENTER.getX(), KNOB_CENTER.getY());
//                    break;
//            }
//            G2.drawImage(SINGLE_POST, (int) (IMAGE_WIDTH * 0.1822429895401001), (int) (IMAGE_HEIGHT * 0.44859811663627625), null);
//            G2.setTransform(OLD_TRANSFORM);
//        }

        G2.dispose();

        return image;
    }

    protected float getWidthRadiusFactor()
    {
        final Dimension GAUGE_DIM = new Dimension(isFrameVisible() ? getWidth() : getGaugeBounds().width, isFrameVisible() ? getHeight() : getGaugeBounds().height);
        return GaugeTypeUtil.computeWidthRadiusFactor(getGaugeTypeInfo(), new Rectangle(GAUGE_DIM), CENTER);
    }

    protected GaugeTypeInfo getGaugeTypeInfo()
    {
        return GaugeTypeInfo.getGaugeTypeInfo(getGaugeType(), getCustomGaugeType(), isFrameVisible() ? getFrameThikness() : 0);
    }
    
    protected void initPointer(final int GAUGE_WIDTH) {
        if (pointerImage != null) {
            pointerImage.flush();
        }
        pointerImage = create_POINTER_Image(GAUGE_WIDTH, getPointerType());

        if (pointerShadowImage != null) {
            pointerShadowImage.flush();
        }
        if (getModel().isPointerShadowVisible()) {
            pointerShadowImage = create_POINTER_SHADOW_Image(GAUGE_WIDTH, getPointerType());
        } else {
            pointerShadowImage = null;
        }
    }

    protected void initMaxMeasured(final int GAUGE_WIDTH) {
        if (minMeasuredImage != null) {
            minMeasuredImage.flush();
        }
        minMeasuredImage = create_MEASURED_VALUE_Image(GAUGE_WIDTH, new Color(0, 23, 252, 255));

        if (maxMeasuredImage != null) {
            maxMeasuredImage.flush();
        }
        maxMeasuredImage = create_MEASURED_VALUE_Image(GAUGE_WIDTH, new Color(252, 29, 0, 255));
    }
    
    protected BufferedImage create_BACKGROUND_Image(final int WIDTH, final int HEIGHT, final String TITLE, final String UNIT_STRING, BufferedImage image) {
        if (getGaugeType() != GaugeType.CUSTOM) {
            return super.create_BACKGROUND_Image(WIDTH, TITLE, UNIT_STRING, image);
        }
        if (WIDTH <= 0) {
            return null;
        }

        if (image == null) {
            image = UTIL.createImage(WIDTH, HEIGHT, Transparency.TRANSLUCENT);
        }
        final Graphics2D G2 = image.createGraphics();
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
        G2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        final int IMAGE_WIDTH = image.getWidth();
        final int IMAGE_HEIGHT = image.getHeight();

        boolean fadeInOut = false;

        final Shape GAUGE_BACKGROUND = GaugeTypeUtil.buildShape(getGaugeTypeInfo(), new Dimension(IMAGE_WIDTH, IMAGE_HEIGHT), backgroundScale, getFrameType());

        final Point2D GAUGE_BACKGROUND_START = new Point2D.Double(0, GAUGE_BACKGROUND.getBounds2D().getMinY());
        final Point2D GAUGE_BACKGROUND_STOP = new Point2D.Double(0, GAUGE_BACKGROUND.getBounds2D().getMaxY());
        final float[] GAUGE_BACKGROUND_FRACTIONS = {
            0.0f,
            0.4f,
            1.0f
        };

        Paint backgroundPaint = null;

        // Set custom background paint if selected
        if (getCustomBackground() != null && getBackgroundColor() == BackgroundColor.CUSTOM) {
            G2.setPaint(getCustomBackground());
        } else {
            final Color[] GAUGE_BACKGROUND_COLORS = {
                getBackgroundColor().GRADIENT_START_COLOR,
                getBackgroundColor().GRADIENT_FRACTION_COLOR,
                getBackgroundColor().GRADIENT_STOP_COLOR
            };

            if (getBackgroundColor() == BackgroundColor.BRUSHED_METAL) {
                backgroundPaint = new TexturePaint(UTIL.createBrushMetalTexture(getModel().getTextureColor(), GAUGE_BACKGROUND.getBounds().width, GAUGE_BACKGROUND.getBounds().height), GAUGE_BACKGROUND.getBounds());
            } else if (getBackgroundColor() == BackgroundColor.STAINLESS) {
                final float[] STAINLESS_FRACTIONS = {
                    0f,
                    0.03f,
                    0.10f,
                    0.14f,
                    0.24f,
                    0.33f,
                    0.38f,
                    0.5f,
                    0.62f,
                    0.67f,
                    0.76f,
                    0.81f,
                    0.85f,
                    0.97f,
                    1.0f
                };

                // Define the colors of the conical gradient paint
                final Color[] STAINLESS_COLORS = {
                    new Color(0xFDFDFD),
                    new Color(0xFDFDFD),
                    new Color(0xB2B2B4),
                    new Color(0xACACAE),
                    new Color(0xFDFDFD),
                    new Color(0x6E6E70),
                    new Color(0x6E6E70),
                    new Color(0xFDFDFD),
                    new Color(0x6E6E70),
                    new Color(0x6E6E70),
                    new Color(0xFDFDFD),
                    new Color(0xACACAE),
                    new Color(0xB2B2B4),
                    new Color(0xFDFDFD),
                    new Color(0xFDFDFD)
                };

                // Define the conical gradient paint
                backgroundPaint = new ConicalGradientPaint(false, getCenter(), -0.45f, STAINLESS_FRACTIONS, STAINLESS_COLORS);
            } else if (getBackgroundColor() == BackgroundColor.STAINLESS_GRINDED) {
                backgroundPaint = new TexturePaint(BACKGROUND_FACTORY.STAINLESS_GRINDED_TEXTURE, new java.awt.Rectangle(0, 0, 100, 100));
            } else if (getBackgroundColor() == BackgroundColor.CARBON) {
                backgroundPaint = new TexturePaint(BACKGROUND_FACTORY.CARBON_FIBRE_TEXTURE, new java.awt.Rectangle(0, 0, 12, 12));
                fadeInOut = true;
            } else if (getBackgroundColor() == BackgroundColor.PUNCHED_SHEET) {
                backgroundPaint = new TexturePaint(BACKGROUND_FACTORY.getPunchedSheetTexture(), new java.awt.Rectangle(0, 0, 12, 12));
                fadeInOut = true;
            } else if (getBackgroundColor() == BackgroundColor.LINEN) {
                backgroundPaint = new TexturePaint(UTIL.createLinenTexture(getModel().getTextureColor(), GAUGE_BACKGROUND.getBounds().width, GAUGE_BACKGROUND.getBounds().height), GAUGE_BACKGROUND.getBounds());
            } else if (getBackgroundColor() == BackgroundColor.NOISY_PLASTIC) {
                GAUGE_BACKGROUND_START.setLocation(0.0, GAUGE_BACKGROUND.getBounds2D().getMinY());
                GAUGE_BACKGROUND_STOP.setLocation(0.0, GAUGE_BACKGROUND.getBounds2D().getMaxY());
                if (GAUGE_BACKGROUND_START.equals(GAUGE_BACKGROUND_STOP)) {
                    GAUGE_BACKGROUND_STOP.setLocation(0.0, GAUGE_BACKGROUND_START.getY() + 1);
                }
                final float[] FRACTIONS = {
                    0.0f,
                    1.0f
                };
                final Color[] COLORS = {
                    UTIL.lighter(getTextureColor(), 0.15f),
                    UTIL.darker(getTextureColor(), 0.15f)
                };
                Util.INSTANCE.validateGradientPoints(GAUGE_BACKGROUND_START, GAUGE_BACKGROUND_STOP);
                backgroundPaint = new LinearGradientPaint(GAUGE_BACKGROUND_START, GAUGE_BACKGROUND_STOP, FRACTIONS, COLORS);
            } else {
                Util.INSTANCE.validateGradientPoints(GAUGE_BACKGROUND_START, GAUGE_BACKGROUND_STOP);
                backgroundPaint = new LinearGradientPaint(GAUGE_BACKGROUND_START, GAUGE_BACKGROUND_STOP, GAUGE_BACKGROUND_FRACTIONS, GAUGE_BACKGROUND_COLORS);
            }
            G2.setPaint(backgroundPaint);
        }
        G2.fill(GAUGE_BACKGROUND);

        // Create inner shadow on background shape
        final BufferedImage CLP;
        if (getCustomBackground() != null && getBackgroundColor() == BackgroundColor.CUSTOM) {
            CLP = Shadow.INSTANCE.createInnerShadow((Shape) GAUGE_BACKGROUND, getCustomBackground(), 0, 0.65f, Color.BLACK, 20, 315);
        } else {
            CLP = Shadow.INSTANCE.createInnerShadow((Shape) GAUGE_BACKGROUND, backgroundPaint, 0, 0.65f, Color.BLACK, 20, 315);
        }
        G2.drawImage(CLP, GAUGE_BACKGROUND.getBounds().x, GAUGE_BACKGROUND.getBounds().y, null);

        // add noise if NOISY_PLASTIC
        if (getBackgroundColor() == BackgroundColor.NOISY_PLASTIC) {
            final Random BW_RND = new Random();
            final Random ALPHA_RND = new Random();
            final Shape OLD_CLIP = G2.getClip();
            G2.setClip(GAUGE_BACKGROUND);
            Color noiseColor;
            int noiseAlpha;
            for (int y = 0 ; y < GAUGE_BACKGROUND.getBounds().getHeight() ; y ++) {
                for (int x = 0 ; x < GAUGE_BACKGROUND.getBounds().getWidth() ; x ++) {
                    if (BW_RND.nextBoolean()) {
                        noiseColor = BRIGHT_NOISE;
                    } else {
                        noiseColor = DARK_NOISE;
                    }
                    noiseAlpha = 10 + ALPHA_RND.nextInt(10) - 5;
                    G2.setColor(new Color(noiseColor.getRed(), noiseColor.getGreen(), noiseColor.getBlue(), noiseAlpha));
                    G2.drawLine((int) (x + GAUGE_BACKGROUND.getBounds2D().getMinX()), (int) (y + GAUGE_BACKGROUND.getBounds2D().getMinY()), (int) (x + GAUGE_BACKGROUND.getBounds2D().getMinX()), (int) (y + GAUGE_BACKGROUND.getBounds2D().getMinY()));
                }
            }
            G2.setClip(OLD_CLIP);
        }

        // Draw an overlay gradient that gives the carbon fibre a more realistic look
        if (fadeInOut) {
            final float[] SHADOW_OVERLAY_FRACTIONS = {
                0.0f,
                0.4f,
                0.6f,
                1.0f
            };
            final Color[] SHADOW_OVERLAY_COLORS = {
                new Color(0f, 0f, 0f, 0.6f),
                new Color(0f, 0f, 0f, 0.0f),
                new Color(0f, 0f, 0f, 0.0f),
                new Color(0f, 0f, 0f, 0.6f)
            };
            final LinearGradientPaint SHADOW_OVERLAY_GRADIENT;
            if (Util.INSTANCE.pointsEquals(GAUGE_BACKGROUND.getBounds2D().getMinX(), 0, GAUGE_BACKGROUND.getBounds2D().getMaxX(), 0)) {
                SHADOW_OVERLAY_GRADIENT = new LinearGradientPaint(new Point2D.Double(GAUGE_BACKGROUND.getBounds().getMinX(), 0), new Point2D.Double(GAUGE_BACKGROUND.getBounds().getMaxX() + 1, 0), SHADOW_OVERLAY_FRACTIONS, SHADOW_OVERLAY_COLORS);
            } else {
                SHADOW_OVERLAY_GRADIENT = new LinearGradientPaint(new Point2D.Double(GAUGE_BACKGROUND.getBounds().getMinX(), 0), new Point2D.Double(GAUGE_BACKGROUND.getBounds().getMaxX(), 0), SHADOW_OVERLAY_FRACTIONS, SHADOW_OVERLAY_COLORS);
            }
            G2.setPaint(SHADOW_OVERLAY_GRADIENT);
            G2.fill(GAUGE_BACKGROUND);
        }

        // Draw the custom layer if selected
        if (isCustomLayerVisible()) {
            G2.drawImage(UTIL.getScaledInstance(getCustomLayer(), IMAGE_WIDTH, IMAGE_HEIGHT, RenderingHints.VALUE_INTERPOLATION_BICUBIC), 0, 0, null);
        }

        final FontRenderContext RENDER_CONTEXT = new FontRenderContext(null, true, true);

        if (!TITLE.isEmpty()) {
            if (isLabelColorFromThemeEnabled()) {
                G2.setColor(getBackgroundColor().LABEL_COLOR);
            } else {
                G2.setColor(getLabelColor());
            }
            G2.setFont(new Font("Verdana", 0, (int) (0.04672897196261682 * IMAGE_WIDTH)));
            final TextLayout TITLE_LAYOUT = new TextLayout(TITLE, G2.getFont(), RENDER_CONTEXT);
            final Rectangle2D TITLE_BOUNDARY = TITLE_LAYOUT.getBounds();
            G2.drawString(TITLE, (float) ((IMAGE_WIDTH - TITLE_BOUNDARY.getWidth()) / 2.0), (float) (0.44f * IMAGE_HEIGHT) + TITLE_LAYOUT.getAscent() - TITLE_LAYOUT.getDescent());
        }

        if (!UNIT_STRING.isEmpty()) {
            if (isLabelColorFromThemeEnabled()) {
                G2.setColor(getBackgroundColor().LABEL_COLOR);
            } else {
                G2.setColor(getLabelColor());
            }
            G2.setFont(new Font("Verdana", 0, (int) (0.04672897196261682 * IMAGE_WIDTH)));
            final TextLayout UNIT_LAYOUT = new TextLayout(UNIT_STRING, G2.getFont(), RENDER_CONTEXT);
            final Rectangle2D UNIT_BOUNDARY = UNIT_LAYOUT.getBounds();
            G2.drawString(UNIT_STRING, (float) ((IMAGE_WIDTH - UNIT_BOUNDARY.getWidth()) / 2.0), 0.52f * IMAGE_HEIGHT + UNIT_LAYOUT.getAscent() - UNIT_LAYOUT.getDescent());
        }

        G2.dispose();

        return image;
    }
    
    private BufferedImage create_FRAME_Image(final int WIDTH, final int HEIGHT, BufferedImage image) {
        if (WIDTH <= 0) {
            return null;
        }
        final double VERTICAL_SCALE;

        if (image == null) {
            image = UTIL.createImage(WIDTH, HEIGHT, Transparency.TRANSLUCENT);
            VERTICAL_SCALE = 1.0;
        } else {
            VERTICAL_SCALE = 0.641860465116279;
        }
        final Graphics2D G2 = image.createGraphics();
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);

        final int IMAGE_WIDTH = image.getWidth();
        final int IMAGE_HEIGHT = image.getHeight();
        final Dimension imageDimension = new Dimension(IMAGE_WIDTH, IMAGE_HEIGHT);

        // Define shape that will be subtracted from frame shapes
        final Shape SUBTRACT_PATH = GaugeTypeUtil.buildShape(getGaugeTypeInfo(), imageDimension, backgroundScale, getFrameType());
        final Area SUBTRACT = new Area(SUBTRACT_PATH);

        final Shape FRAME_OUTERFRAME = GaugeTypeUtil.buildShape(getGaugeTypeInfo(), imageDimension, outerFrameScale, getFrameType());
        G2.setPaint(getOuterFrameColor());
        final Area FRAME_OUTERFRAME_AREA = new Area(FRAME_OUTERFRAME);
        FRAME_OUTERFRAME_AREA.subtract(SUBTRACT);
        G2.fill(FRAME_OUTERFRAME_AREA);

        final Shape FRAME_MAIN = GaugeTypeUtil.buildShape(getGaugeTypeInfo(), imageDimension, mainFrameScale, getFrameType());
        final Point2D FRAME_MAIN_START = new Point2D.Double(0, FRAME_MAIN.getBounds2D().getMinY());
        final Point2D FRAME_MAIN_STOP = new Point2D.Double(0, FRAME_MAIN.getBounds2D().getMaxY());
        final Point2D FRAME_MAIN_CENTER = new Point2D.Double(FRAME_MAIN.getBounds2D().getCenterX(), FRAME_MAIN.getBounds2D().getHeight() * 0.7753623188 * VERTICAL_SCALE);

        final Area FRAME_MAIN_AREA = new Area(FRAME_MAIN);

        if (getFrameDesign() == FrameDesign.CUSTOM) {
            G2.setPaint(getCustomFrameDesign());
            FRAME_MAIN_AREA.subtract(SUBTRACT);
            G2.fill(FRAME_MAIN_AREA);
        } else {
            switch (getFrameDesign()) {
                case BLACK_METAL:
                    float[] frameMainFractions1 = {
                        0.0f,
                        45.0f,
                        85.0f,
                        180.0f,
                        275.0f,
                        315.0f,
                        360.0f
                    };

                    Color[] frameMainColors1 = {
                        new Color(254, 254, 254, 255),
                        new Color(0, 0, 0, 255),
                        new Color(0, 0, 0, 255),
                        new Color(0, 0, 0, 255),
                        new Color(0, 0, 0, 255),
                        new Color(0, 0, 0, 255),
                        new Color(254, 254, 254, 255)
                    };

                    Paint frameMainGradient1 = new ConicalGradientPaint(true, FRAME_MAIN_CENTER, 0, frameMainFractions1, frameMainColors1);
                    G2.setPaint(frameMainGradient1);
                    FRAME_MAIN_AREA.subtract(SUBTRACT);
                    G2.fill(FRAME_MAIN_AREA);
                    break;

                case METAL:
                    float[] frameMainFractions2 = {
                        0.0f,
                        0.07f,
                        0.12f,
                        1.0f
                    };

                    Color[] frameMainColors2 = {
                        new Color(254, 254, 254, 255),
                        new Color(210, 210, 210, 255),
                        new Color(179, 179, 179, 255),
                        new Color(213, 213, 213, 255)
                    };
                    Util.INSTANCE.validateGradientPoints(FRAME_MAIN_START, FRAME_MAIN_STOP);
                    Paint frameMainGradient2 = new LinearGradientPaint(FRAME_MAIN_START, FRAME_MAIN_STOP, frameMainFractions2, frameMainColors2);
                    G2.setPaint(frameMainGradient2);
                    FRAME_MAIN_AREA.subtract(SUBTRACT);
                    G2.fill(FRAME_MAIN_AREA);
                    break;

                case SHINY_METAL:
                    float[] frameMainFractions3;
                    Color[] frameMainColors3;
                    if (isFrameBaseColorEnabled()) {
                        frameMainFractions3 = new float[]{
                            0.0f,
                            45.0f,
                            90.0f,
                            135.0f,
                            180.0f,
                            225.0f,
                            270.0f,
                            315.0f,
                            360.0f
                        };

                        frameMainColors3 = new Color[]{
                            new Color(254, 254, 254, 255),
                            new Color(getFrameBaseColor().getRed(), getFrameBaseColor().getGreen(), getFrameBaseColor().getBlue(), 255),
                            new Color(getFrameBaseColor().getRed(), getFrameBaseColor().getGreen(), getFrameBaseColor().getBlue(), 255),
                            new Color(getFrameBaseColor().brighter().brighter().getRed(), getFrameBaseColor().brighter().brighter().getGreen(), getFrameBaseColor().brighter().brighter().getBlue(), 255),
                            new Color(getFrameBaseColor().getRed(), getFrameBaseColor().getGreen(), getFrameBaseColor().getBlue(), 255),
                            new Color(getFrameBaseColor().brighter().brighter().getRed(), getFrameBaseColor().brighter().brighter().getGreen(), getFrameBaseColor().brighter().brighter().getBlue(), 255),
                            new Color(getFrameBaseColor().getRed(), getFrameBaseColor().getGreen(), getFrameBaseColor().getBlue(), 255),
                            new Color(getFrameBaseColor().getRed(), getFrameBaseColor().getGreen(), getFrameBaseColor().getBlue(), 255),
                            new Color(254, 254, 254, 255)
                        };
                    } else {
                        frameMainFractions3 = new float[]{
                            0.0f,
                            45.0f,
                            90.0f,
                            95.0f,
                            180.0f,
                            265.0f,
                            270.0f,
                            315.0f,
                            360.0f
                        };

                        frameMainColors3 = new Color[]{
                            new Color(254, 254, 254, 255),
                            new Color(210, 210, 210, 255),
                            new Color(179, 179, 179, 255),
                            new Color(160, 160, 160, 255),
                            new Color(160, 160, 160, 255),
                            new Color(160, 160, 160, 255),
                            new Color(179, 179, 179, 255),
                            new Color(210, 210, 210, 255),
                            new Color(254, 254, 254, 255)
                        };
                    }

                    Paint frameMainGradient3 = new ConicalGradientPaint(true, FRAME_MAIN_CENTER, 0, frameMainFractions3, frameMainColors3);
                    G2.setPaint(frameMainGradient3);
                    FRAME_MAIN_AREA.subtract(SUBTRACT);
                    G2.fill(FRAME_MAIN_AREA);
                    break;

                case GLOSSY_METAL:
                    final Shape FRAME_MAIN_GLOSSY1 = GaugeTypeUtil.buildShape(getGaugeTypeInfo(), imageDimension, outerFrameScale, getFrameType());
                    final Area FRAME_MAIN_GLOSSY_1 = new Area(FRAME_MAIN_GLOSSY1);
                    FRAME_MAIN_GLOSSY_1.subtract(SUBTRACT);
                    G2.setPaint(new RadialGradientPaint(new Point2D.Double(0.5 * IMAGE_WIDTH, 0.9927007299270073 * IMAGE_HEIGHT * VERTICAL_SCALE), (float)(0.4953271028037383 * IMAGE_WIDTH), new float[]{0.0f, 0.01f, 0.95f, 1.0f}, new Color[]{new Color(0.8235294118f, 0.8235294118f, 0.8235294118f, 1f), new Color(0.8235294118f, 0.8235294118f, 0.8235294118f, 1f), new Color(0.8235294118f, 0.8235294118f, 0.8235294118f, 1f), new Color(0.9960784314f, 0.9960784314f, 0.9960784314f, 1f)}));
                    G2.fill(FRAME_MAIN_GLOSSY_1);

                    final Shape FRAME_MAIN_GLOSSY2 = GaugeTypeUtil.buildShape(getGaugeTypeInfo(), imageDimension, mainFrameScale, getFrameType());
                    final Area FRAME_MAIN_GLOSSY_2 = new Area(FRAME_MAIN_GLOSSY2);
                    FRAME_MAIN_GLOSSY_2.subtract(SUBTRACT);
                    G2.setPaint(new LinearGradientPaint(new Point2D.Double(0.5 * IMAGE_WIDTH, 0.0072992700729927005 * IMAGE_HEIGHT * VERTICAL_SCALE), new Point2D.Double(0.5000000000000001 * IMAGE_WIDTH, 1.0 * IMAGE_HEIGHT * VERTICAL_SCALE), new float[]{0.0f, 0.24f, 0.34f, 0.65f, 0.85f, 1.0f}, new Color[]{new Color(0.9764705882f, 0.9764705882f, 0.9764705882f, 1f), new Color(0.7843137255f, 0.7647058824f, 0.7490196078f, 1f), new Color(0.9882352941f, 0.9882352941f, 0.9882352941f, 1f), new Color(0.1215686275f, 0.1215686275f, 0.1215686275f, 1f), new Color(0.7843137255f, 0.7607843137f, 0.7529411765f, 1f), new Color(0.8156862745f, 0.8156862745f, 0.8156862745f, 1f)}));
                    G2.fill(FRAME_MAIN_GLOSSY_2);

                    final Shape FRAME_MAIN_GLOSSY3 = GaugeTypeUtil.buildShape(getGaugeTypeInfo(), imageDimension, innerFrameGlossy1, getFrameType());
                    final Area FRAME_MAIN_GLOSSY_3 = new Area(FRAME_MAIN_GLOSSY3);
                    FRAME_MAIN_GLOSSY_3.subtract(SUBTRACT);
                    G2.setPaint(new Color(0.9647058824f, 0.9647058824f, 0.9647058824f, 1f));
                    G2.fill(FRAME_MAIN_GLOSSY_3);

                    final Shape FRAME_MAIN_GLOSSY4 = GaugeTypeUtil.buildShape(getGaugeTypeInfo(), imageDimension, innerFrameGlossy2, getFrameType());
                    final Area FRAME_MAIN_GLOSSY_4 = new Area(FRAME_MAIN_GLOSSY4);
                    FRAME_MAIN_GLOSSY_4.subtract(SUBTRACT);
                    G2.setPaint(new Color(0.2f, 0.2f, 0.2f, 1f));
                    G2.fill(FRAME_MAIN_GLOSSY_4);
                    break;

                case BRASS:
                    float[] frameMainFractions5 = {
                        0.0f,
                        0.05f,
                        0.10f,
                        0.50f,
                        0.90f,
                        0.95f,
                        1.0f
                    };

                    Color[] frameMainColors5 = {
                        new Color(249, 243, 155, 255),
                        new Color(246, 226, 101, 255),
                        new Color(240, 225, 132, 255),
                        new Color(90, 57, 22, 255),
                        new Color(249, 237, 139, 255),
                        new Color(243, 226, 108, 255),
                        new Color(202, 182, 113, 255)
                    };
                    Util.INSTANCE.validateGradientPoints(FRAME_MAIN_START, FRAME_MAIN_STOP);
                    Paint frameMainGradient5 = new LinearGradientPaint(FRAME_MAIN_START, FRAME_MAIN_STOP, frameMainFractions5, frameMainColors5);
                    G2.setPaint(frameMainGradient5);
                    FRAME_MAIN_AREA.subtract(SUBTRACT);
                    G2.fill(FRAME_MAIN_AREA);
                    break;

                case STEEL:
                    float[] frameMainFractions6 = {
                        0.0f,
                        0.05f,
                        0.10f,
                        0.50f,
                        0.90f,
                        0.95f,
                        1.0f
                    };

                    Color[] frameMainColors6 = {
                        new Color(231, 237, 237, 255),
                        new Color(189, 199, 198, 255),
                        new Color(192, 201, 200, 255),
                        new Color(23, 31, 33, 255),
                        new Color(196, 205, 204, 255),
                        new Color(194, 204, 203, 255),
                        new Color(189, 201, 199, 255)
                    };
                    Util.INSTANCE.validateGradientPoints(FRAME_MAIN_START, FRAME_MAIN_STOP);
                    Paint frameMainGradient6 = new LinearGradientPaint(FRAME_MAIN_START, FRAME_MAIN_STOP, frameMainFractions6, frameMainColors6);
                    G2.setPaint(frameMainGradient6);
                    FRAME_MAIN_AREA.subtract(SUBTRACT);
                    G2.fill(FRAME_MAIN_AREA);
                    break;

                case CHROME:
                    float[] frameMainFractions7 = {
                        0.0f,
                        0.09f,
                        0.12f,
                        0.16f,
                        0.25f,
                        0.29f,
                        0.33f,
                        0.38f,
                        0.48f,
                        0.52f,
                        0.63f,
                        0.68f,
                        0.8f,
                        0.83f,
                        0.87f,
                        0.97f,
                        1.0f
                    };

                    Color[] frameMainColors7 = {
                        new Color(255, 255, 255, 255),
                        new Color(255, 255, 255, 255),
                        new Color(136, 136, 138, 255),
                        new Color(164, 185, 190, 255),
                        new Color(158, 179, 182, 255),
                        new Color(112, 112, 112, 255),
                        new Color(221, 227, 227, 255),
                        new Color(155, 176, 179, 255),
                        new Color(156, 176, 177, 255),
                        new Color(254, 255, 255, 255),
                        new Color(255, 255, 255, 255),
                        new Color(156, 180, 180, 255),
                        new Color(198, 209, 211, 255),
                        new Color(246, 248, 247, 255),
                        new Color(204, 216, 216, 255),
                        new Color(164, 188, 190, 255),
                        new Color(255, 255, 255, 255)
                    };

                    Paint frameMainGradient7 = new ConicalGradientPaint(false, FRAME_MAIN_CENTER, 0, frameMainFractions7, frameMainColors7);
                    G2.setPaint(frameMainGradient7);
                    FRAME_MAIN_AREA.subtract(SUBTRACT);
                    G2.fill(FRAME_MAIN_AREA);
                    break;

                case GOLD:
                    float[] frameMainFractions8 = {
                        0.0f,
                        0.15f,
                        0.22f,
                        0.3f,
                        0.38f,
                        0.44f,
                        0.51f,
                        0.6f,
                        0.68f,
                        0.75f,
                        1.0f
                    };

                    Color[] frameMainColors8 = {
                        new Color(255, 255, 207, 255),
                        new Color(255, 237, 96, 255),
                        new Color(254, 199, 57, 255),
                        new Color(255, 249, 203, 255),
                        new Color(255, 199, 64, 255),
                        new Color(252, 194, 60, 255),
                        new Color(255, 204, 59, 255),
                        new Color(213, 134, 29, 255),
                        new Color(255, 201, 56, 255),
                        new Color(212, 135, 29, 255),
                        new Color(247, 238, 101, 255)
                    };
                    Util.INSTANCE.validateGradientPoints(FRAME_MAIN_START, FRAME_MAIN_STOP);
                    Paint frameMainGradient8 = new LinearGradientPaint(FRAME_MAIN_START, FRAME_MAIN_STOP, frameMainFractions8, frameMainColors8);
                    G2.setPaint(frameMainGradient8);
                    FRAME_MAIN_AREA.subtract(SUBTRACT);
                    G2.fill(FRAME_MAIN_AREA);
                    break;

                case ANTHRACITE:
                    float[] frameMainFractions9 = {
                        0.0f,
                        0.06f,
                        0.12f,
                        1.0f
                    };
                    Color[] frameMainColors9 = {
                        new Color(118, 117, 135, 255),
                        new Color(74, 74, 82, 255),
                        new Color(50, 50, 54, 255),
                        new Color(97, 97, 108, 255)
                    };
                    Util.INSTANCE.validateGradientPoints(FRAME_MAIN_START, FRAME_MAIN_STOP);
                    Paint frameMainGradient9 = new LinearGradientPaint(FRAME_MAIN_START, FRAME_MAIN_STOP, frameMainFractions9, frameMainColors9);
                    G2.setPaint(frameMainGradient9);
                    FRAME_MAIN_AREA.subtract(SUBTRACT);
                    G2.fill(FRAME_MAIN_AREA);
                    break;

                case TILTED_GRAY:
                    FRAME_MAIN_START.setLocation((0.2336448598130841 * IMAGE_WIDTH), (0.08411214953271028 * IMAGE_HEIGHT));
                    FRAME_MAIN_STOP.setLocation(((0.2336448598130841 + 0.5789369637935792) * IMAGE_WIDTH), ((0.08411214953271028 + 0.8268076708711319) * IMAGE_HEIGHT));
                    float[] frameMainFractions10 = {
                        0.0f,
                        0.07f,
                        0.16f,
                        0.33f,
                        0.55f,
                        0.79f,
                        1.0f
                    };
                    Color[] frameMainColors10 = {
                        new Color(255, 255, 255, 255),
                        new Color(210, 210, 210, 255),
                        new Color(179, 179, 179, 255),
                        new Color(255, 255, 255, 255),
                        new Color(197, 197, 197, 255),
                        new Color(255, 255, 255, 255),
                        new Color(102, 102, 102, 255)
                    };
                    Util.INSTANCE.validateGradientPoints(FRAME_MAIN_START, FRAME_MAIN_STOP);
                    Paint frameMainGradient10 = new LinearGradientPaint(FRAME_MAIN_START, FRAME_MAIN_STOP, frameMainFractions10, frameMainColors10);
                    G2.setPaint(frameMainGradient10);
                    FRAME_MAIN_AREA.subtract(SUBTRACT);
                    G2.fill(FRAME_MAIN_AREA);
                    break;

                case TILTED_BLACK:
                    FRAME_MAIN_START.setLocation((0.22897196261682243 * IMAGE_WIDTH), (0.0794392523364486 * IMAGE_HEIGHT));
                    FRAME_MAIN_STOP.setLocation(((0.22897196261682243 + 0.573576436351046) * IMAGE_WIDTH), ((0.0794392523364486 + 0.8191520442889918) * IMAGE_HEIGHT));
                    float[] frameMainFractions11 = {
                        0.0f,
                        0.21f,
                        0.47f,
                        0.99f,
                        1.0f
                    };
                    Color[] frameMainColors11 = {
                        new Color(102, 102, 102, 255),
                        new Color(0, 0, 0, 255),
                        new Color(102, 102, 102, 255),
                        new Color(0, 0, 0, 255),
                        new Color(0, 0, 0, 255)
                    };
                    Util.INSTANCE.validateGradientPoints(FRAME_MAIN_START, FRAME_MAIN_STOP);
                    Paint frameMainGradient11 = new LinearGradientPaint(FRAME_MAIN_START, FRAME_MAIN_STOP, frameMainFractions11, frameMainColors11);
                    G2.setPaint(frameMainGradient11);
                    FRAME_MAIN_AREA.subtract(SUBTRACT);
                    G2.fill(FRAME_MAIN_AREA);
                    break;

                default:
                    float[] frameMainFractions = {
                        0.0f,
                        0.07f,
                        0.12f,
                        1.0f
                    };

                    Color[] frameMainColors = {
                        new Color(254, 254, 254, 255),
                        new Color(210, 210, 210, 255),
                        new Color(179, 179, 179, 255),
                        new Color(213, 213, 213, 255)
                    };
                    Util.INSTANCE.validateGradientPoints(FRAME_MAIN_START, FRAME_MAIN_STOP);
                    Paint frameMainGradient = new LinearGradientPaint(FRAME_MAIN_START, FRAME_MAIN_STOP, frameMainFractions, frameMainColors);
                    G2.setPaint(frameMainGradient);
                    FRAME_MAIN_AREA.subtract(SUBTRACT);
                    G2.fill(FRAME_MAIN_AREA);
                    break;
            }
        }

        final Shape FRAME_INNERFRAME = GaugeTypeUtil.buildShape(getGaugeTypeInfo(), imageDimension, innerFrameScale, getFrameType());
        G2.setPaint(getInnerFrameColor());
        final Area FRAME_INNERFRAME_AREA = new Area(FRAME_INNERFRAME);
        FRAME_INNERFRAME_AREA.subtract(SUBTRACT);
        G2.fill(FRAME_INNERFRAME_AREA);

        // Apply frame effects
        final float[] EFFECT_FRACTIONS;
        final Color[] EFFECT_COLORS;
        final GradientWrapper EFFECT_GRADIENT;
        float scale = 1.0f;
        final java.awt.Shape[] EFFECT = new java.awt.Shape[100];
        switch (getFrameEffect()) {
            case EFFECT_BULGE:
                EFFECT_FRACTIONS = new float[]{
                    0.0f,
                    0.13f,
                    0.14f,
                    0.17f,
                    0.18f,
                    1.0f
                };
                EFFECT_COLORS = new Color[]{
                    new Color(0, 0, 0, 102),            // Outside
                    new Color(255, 255, 255, 151),
                    new Color(219, 219, 219, 153),
                    new Color(0, 0, 0, 95),
                    new Color(0, 0, 0, 76),       // Inside
                    new Color(0, 0, 0, 0)
                };
                EFFECT_GRADIENT = new GradientWrapper(new Point2D.Double(100, 0), new Point2D.Double(0, 0), EFFECT_FRACTIONS, EFFECT_COLORS);
                for (int i = 0; i < 100; i++) {
                    EFFECT[i] = Scaler.INSTANCE.scale(FRAME_MAIN_AREA, scale);
                    scale -= 0.01f;
                }
                G2.setStroke(new BasicStroke(1.5f));
                for (int i = 0; i < EFFECT.length; i++) {
                    G2.setPaint(EFFECT_GRADIENT.getColorAt(i / 100f));
                    G2.draw(EFFECT[i]);
                }
                break;

            case EFFECT_CONE:
                EFFECT_FRACTIONS = new float[]{
                    0.0f,
                    0.0399f,
                    0.04f,
                    0.1799f,
                    0.18f,
                    1.0f
                };
                EFFECT_COLORS = new Color[]{
                    new Color(0, 0, 0, 76),
                    new Color(223, 223, 223, 127),
                    new Color(255, 255, 255, 124),
                    new Color(9, 9, 9, 51),
                    new Color(0, 0, 0, 50),
                    new Color(0, 0, 0, 0)
                };
                EFFECT_GRADIENT = new GradientWrapper(new Point2D.Double(100, 0), new Point2D.Double(0, 0), EFFECT_FRACTIONS, EFFECT_COLORS);
                for (int i = 0; i < 100; i++) {
                    EFFECT[i] = Scaler.INSTANCE.scale(FRAME_MAIN_AREA, scale);
                    scale -= 0.01f;
                }
                G2.setStroke(new BasicStroke(1.5f));
                for (int i = 0; i < EFFECT.length; i++) {
                    G2.setPaint(EFFECT_GRADIENT.getColorAt(i / 100f));
                    G2.draw(EFFECT[i]);
                }
                break;

            case EFFECT_TORUS:
                EFFECT_FRACTIONS = new float[]{
                    0.0f,
                    0.08f,
                    0.1799f,
                    0.18f,
                    1.0f
                };
                EFFECT_COLORS = new Color[]{
                    new Color(0, 0, 0, 76),
                    new Color(255, 255, 255, 64),
                    new Color(13, 13, 13, 51),
                    new Color(0, 0, 0, 50),
                    new Color(0, 0, 0, 0)
                };
                EFFECT_GRADIENT = new GradientWrapper(new Point2D.Double(100, 0), new Point2D.Double(0, 0), EFFECT_FRACTIONS, EFFECT_COLORS);
                for (int i = 0; i < 100; i++) {
                    EFFECT[i] = Scaler.INSTANCE.scale(FRAME_MAIN_AREA, scale);
                    scale -= 0.01f;
                }
                G2.setStroke(new BasicStroke(1.5f));
                for (int i = 0; i < EFFECT.length; i++) {
                    G2.setPaint(EFFECT_GRADIENT.getColorAt(i / 100f));
                    G2.draw(EFFECT[i]);
                }
                break;

            case EFFECT_INNER_FRAME:
                final Shape EFFECT_BIGINNERFRAME = Scaler.INSTANCE.scale(FRAME_MAIN_AREA, 0.8785046339035034);
                final Point2D EFFECT_BIGINNERFRAME_START = new Point2D.Double(0, EFFECT_BIGINNERFRAME.getBounds2D().getMinY());
                final Point2D EFFECT_BIGINNERFRAME_STOP = new Point2D.Double(0, EFFECT_BIGINNERFRAME.getBounds2D().getMaxY());
                EFFECT_FRACTIONS = new float[]{
                    0.0f,
                    0.3f,
                    0.5f,
                    0.71f,
                    1.0f
                };
                EFFECT_COLORS = new Color[]{
                    new Color(0, 0, 0, 183),
                    new Color(148, 148, 148, 25),
                    new Color(0, 0, 0, 159),
                    new Color(0, 0, 0, 81),
                    new Color(255, 255, 255, 158)
                };
                Util.INSTANCE.validateGradientPoints(EFFECT_BIGINNERFRAME_START, EFFECT_BIGINNERFRAME_STOP);
                final LinearGradientPaint EFFECT_BIGINNERFRAME_GRADIENT = new LinearGradientPaint(EFFECT_BIGINNERFRAME_START, EFFECT_BIGINNERFRAME_STOP, EFFECT_FRACTIONS, EFFECT_COLORS);
                G2.setPaint(EFFECT_BIGINNERFRAME_GRADIENT);
                G2.fill(EFFECT_BIGINNERFRAME);
                break;
        }

        G2.dispose();

        return image;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Visualization">
    @Override
    protected void paintComponent(Graphics g) {
        if (!isInitialized()) {
            return;
        }

        final Graphics2D G2 = (Graphics2D) g.create();

        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        G2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
        G2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        G2.translate(getFramelessOffset().getX(), getFramelessOffset().getY());

        // Draw combined background image
        G2.drawImage(bImage, 0, 0, null);

        // Draw an Arc2d object that will visualize the range of measured values
        if (isRangeOfMeasuredValuesVisible()) {
            G2.setPaint(getModel().getRangeOfMeasuredValuesPaint());
            if ((getGaugeType() == GaugeType.TYPE3 || getGaugeType() == GaugeType.TYPE4) && isLcdVisible()) {
                final Area area = getModel().getRadialAreaOfMeasuredValues();
                area.subtract(lcdArea);
                G2.fill(area);
            } else {
                G2.fill(getModel().getRadialShapeOfMeasuredValues());
            }
        }

        // Highlight active area
        if (isHighlightArea()) {
            for(Section area : getAreas()) {
                if (area.contains(getValue())) {
                    G2.setColor(area.getHighlightColor());
                    if ((getGaugeType() == GaugeType.TYPE3 || getGaugeType() == GaugeType.TYPE4) && isLcdVisible()) {
                        final Area currentArea = new Area(area.getFilledArea());
                        currentArea.subtract(lcdArea);
                        G2.fill(currentArea);
                    } else {
                        G2.fill(area.getFilledArea());
                    }
                    break;
                }
            }
        }

        // Highlight active section
        if (isHighlightSection()) {
            for(Section section : getSections()) {
                if (section.contains(getValue())) {
                    G2.setColor(section.getHighlightColor());
                    G2.fill(section.getSectionArea());
                    break;
                }
            }
        }

        // Draw threshold indicator
        if (isThresholdVisible()) {
            double angleRotation;
            if (!isLogScale()) {
                angleRotation = getRotationOffset() + (getThreshold() - getMinValue()) * getAngleStep();
            } else {
                angleRotation = getRotationOffset() + UTIL.logOfBase(BASE, getThreshold() - getMinValue()) * getLogAngleStep();
            }
            G2.rotate(angleRotation, CENTER.getX(), CENTER.getY());
            G2.drawImage(thresholdImage, (int) (getGaugeBounds().width * 0.4813084112), (int) (getGaugeBounds().height * 0.0841121495), null);
            G2.rotate(-angleRotation, CENTER.getX(), CENTER.getY());
        }

        drawMinMaxMeasured(G2);

        // Draw LED if enabled
        if (isLedVisible()) {
            G2.drawImage(getCurrentLedImage(), (int) (getGaugeBounds().width * getLedPosition().getX()), (int) (getGaugeBounds().height * getLedPosition().getY()), null);
        }

        // Draw user LED if enabled
        if (isUserLedVisible()) {
            G2.drawImage(getCurrentUserLedImage(), (int) (getGaugeBounds().width * getUserLedPosition().getX()), (int) (getGaugeBounds().height * getUserLedPosition().getY()), null);
        }

        // Draw LCD display
        if (isLcdVisible()) {
            if (getLcdColor() == LcdColor.CUSTOM) {
                G2.setColor(getCustomLcdForeground());
            } else {
                G2.setColor(getLcdColor().TEXT_COLOR);
            }
            // Draw LCD text only if isVisible() in AbstractGauge (this is needed for lcd blinking)
            if (isLcdTextVisible()) {
            G2.setFont(getLcdUnitFont());
            if (isLcdUnitStringVisible()) {
                unitLayout = new TextLayout(getLcdUnitString(), G2.getFont(), RENDER_CONTEXT);
                UNIT_BOUNDARY.setFrame(unitLayout.getBounds());
                G2.drawString(getLcdUnitString(), (int) (LCD.getX() + (LCD.getWidth() - UNIT_BOUNDARY.getWidth()) - LCD.getWidth() * 0.03), (int) (LCD.getY() + LCD.getHeight() * 0.76));
                unitStringWidth = UNIT_BOUNDARY.getWidth();
            } else {
                unitStringWidth = 0;
            }
            G2.setFont(getLcdValueFont());
            switch (getModel().getNumberSystem()) {
                case HEX:
                    valueLayout = new TextLayout(Integer.toHexString((int) getLcdValue()).toUpperCase(), G2.getFont(), RENDER_CONTEXT);
                    VALUE_BOUNDARY.setFrame(valueLayout.getBounds());
                    G2.drawString(Integer.toHexString((int) getLcdValue()).toUpperCase(), (float) (LCD.getX() + (LCD.getWidth() - unitStringWidth - VALUE_BOUNDARY.getWidth()) - LCD.getHeight() * 0.333333333), (float) (LCD.getY() + LCD.getHeight() * 0.76));
                    break;

                case OCT:
                    valueLayout = new TextLayout(Integer.toOctalString((int) getLcdValue()), G2.getFont(), RENDER_CONTEXT);
                    VALUE_BOUNDARY.setFrame(valueLayout.getBounds());
                    G2.drawString(Integer.toOctalString((int) getLcdValue()), (float) (LCD.getX() + (LCD.getWidth() - unitStringWidth - VALUE_BOUNDARY.getWidth()) - LCD.getHeight() * 0.333333333), (float) (LCD.getY() + LCD.getHeight() * 0.76));
                    break;

                case DEC:

                default:
                    int digitalFontNo_1Offset = 0;
                    if (isDigitalFont() && Double.toString(getLcdValue()).startsWith("1")) {
                        digitalFontNo_1Offset = (int) (LCD.getHeight() * 0.2166666667);
                    }
                    valueLayout = new TextLayout(formatLcdValue(getLcdValue()), G2.getFont(), RENDER_CONTEXT);
                    VALUE_BOUNDARY.setFrame(valueLayout.getBounds());
                    G2.drawString(formatLcdValue(getLcdValue()), (float) (LCD.getX() + (LCD.getWidth() - unitStringWidth - VALUE_BOUNDARY.getWidth()) - LCD.getHeight() * 0.333333333) - digitalFontNo_1Offset, (float) (LCD.getY() + LCD.getHeight() * 0.76));
                    break;
            }
            }
            // Draw lcd info string
            if (!getLcdInfoString().isEmpty()) {
                G2.setFont(getLcdInfoFont());
                infoLayout = new TextLayout(getLcdInfoString(), G2.getFont(), RENDER_CONTEXT);
                INFO_BOUNDARY.setFrame(infoLayout.getBounds());
                G2.drawString(getLcdInfoString(), (float) LCD.getBounds().x + 5f, LCD.getBounds().y + (float) INFO_BOUNDARY.getHeight() + 5f);
            }
            // Draw lcd threshold indicator
            if (getLcdNumberSystem() == NumberSystem.DEC && isLcdThresholdVisible() && getLcdValue() >= getLcdThreshold()) {
                G2.drawImage(lcdThresholdImage, (int) (LCD.getX() + LCD.getHeight() * 0.0568181818), (int) (LCD.getY() + LCD.getHeight() - lcdThresholdImage.getHeight() - LCD.getHeight() * 0.0568181818), null);
            }
        }

        drawPointer(G2);

        // Draw combined foreground image
        G2.drawImage(fImage, 0, 0, null);

        // Draw glow indicator
        if (isGlowVisible()) {
            if (isGlowing()) {
                G2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, getGlowAlpha()));
                G2.drawImage(glowImageOn, 0, 0, null);
                G2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            } else {
                G2.drawImage(glowImageOff, 0, 0, null);
            }
        }

        // Draw disabled image if component isEnabled() == false
        if (!isEnabled()) {
            G2.drawImage(disabledImage, 0, 0, null);
        }

        G2.dispose();
    }
    
    protected void drawPointer(final Graphics2D G2) {
        // Draw the pointer
        if (!isLogScale()) {
            angle = getRotationOffset() + (getValue() - getMinValue()) * getAngleStep();
        } else {
            angle = getRotationOffset() + UTIL.logOfBase(BASE, getValue() - getMinValue()) * getLogAngleStep();
        }
        final Point2D originPointer = getPointerImageOrigin();
        if (isPointerShadowVisible()) {
            double angleRotation;
            if (!isLogScale()) {
                angleRotation = angle + (Math.cos(Math.toRadians(angle - getRotationOffset() - 91.5)));
            } else {
                angleRotation = angle;
            }
            G2.rotate(angleRotation, CENTER.getX(), CENTER.getY() + 2);
            G2.drawImage(pointerShadowImage, (int) originPointer.getX(), (int) originPointer.getY(), null);
            G2.rotate(-angleRotation, CENTER.getX(), CENTER.getY() + 2);
        }
        G2.rotate(angle, CENTER.getX(), CENTER.getY());
        G2.drawImage(pointerImage, (int) originPointer.getX(), (int) originPointer.getY(), null);
        G2.rotate(-angle, CENTER.getX(), CENTER.getY());
    }

    protected void drawMinMaxMeasured(final Graphics2D G2) {
        final Point2D measuredValueOffset = getMeasuredValueOffset();
        // Draw min measured value indicator
        if (isMinMeasuredValueVisible()) {
            double angleRotation;
            if (!isLogScale()) {
                angleRotation = getRotationOffset() + (getMinMeasuredValue() - getMinValue()) * getAngleStep();
            } else {
                angleRotation = getRotationOffset() + UTIL.logOfBase(BASE, getMinMeasuredValue() - getMinValue()) * getLogAngleStep();
            }
            G2.rotate(angleRotation, CENTER.getX(), CENTER.getY());
            G2.drawImage(minMeasuredImage, (int) measuredValueOffset.getX(), (int) measuredValueOffset.getY(), null);
            G2.rotate(-angleRotation, CENTER.getX(), CENTER.getY());
        }

        // Draw max measured value indicator
        if (isMaxMeasuredValueVisible()) {
            double angleRotation;
            if (!isLogScale()) {
                angleRotation = getRotationOffset() + (getMaxMeasuredValue() - getMinValue()) * getAngleStep();
            } else {
                angleRotation = getRotationOffset() + UTIL.logOfBase(BASE, getMaxMeasuredValue() - getMinValue()) * getLogAngleStep();
            }
            G2.rotate(angleRotation, CENTER.getX(), CENTER.getY());
            G2.drawImage(maxMeasuredImage, (int) measuredValueOffset.getX(), (int) measuredValueOffset.getY(), null);
            G2.rotate(-angleRotation, CENTER.getX(), CENTER.getY());
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Getters and Setters">
    @Override
    public void setValue(double value) {
        if (isValueCoupled()) {
            setLcdValue(value);
        }
        super.setValue(value);
    }

    /**
     * Returns true if the 3d effect gradient overlay for the sections is visible
     * @return true if the 3d effect gradient overlay for the sections is visible
     */
    public boolean isSection3DEffectVisible() {
        return this.section3DEffectVisible;
    }

    /**
     * Enables / disables the visibility of the 3d effect gradient overlay for the sections
     * @param SECTION_3D_EFFECT_VISIBLE
     */
    public void setSection3DEffectVisible(final boolean SECTION_3D_EFFECT_VISIBLE) {
        this.section3DEffectVisible = SECTION_3D_EFFECT_VISIBLE;
        init(getInnerBounds().width, getInnerBounds().height);
        repaint(getInnerBounds());
    }

    /**
     * Returns true if the 3d effect gradient overlay for the areas is visible
     * @return true if the 3d effect gradient overlay for the areas is visible
     */
    public boolean isArea3DEffectVisible() {
        return area3DEffectVisible;
    }

    /**
     * Enables / disables the visibility of the 3d effect gradient overlay for the areas
     * @param AREA_3DEFFECT_VISIBLE
     */
    public void setArea3DEffectVisible(final boolean AREA_3DEFFECT_VISIBLE) {
        area3DEffectVisible = AREA_3DEFFECT_VISIBLE;
        init(getInnerBounds().width, getInnerBounds().height);
        repaint(getInnerBounds());
    }

    @Override
    public Paint createCustomLcdBackgroundPaint(final Color[] LCD_COLORS) {
        final Point2D FOREGROUND_START = new Point2D.Double(0.0, LCD.getMinY() + 1.0);
        final Point2D FOREGROUND_STOP = new Point2D.Double(0.0, LCD.getMaxY() - 1);
        if (FOREGROUND_START.equals(FOREGROUND_STOP)) {
            FOREGROUND_STOP.setLocation(0.0, FOREGROUND_START.getY() + 1);
        }

        final float[] FOREGROUND_FRACTIONS = {
            0.0f,
            0.03f,
            0.49f,
            0.5f,
            1.0f
        };

        final Color[] FOREGROUND_COLORS = {
            LCD_COLORS[0],
            LCD_COLORS[1],
            LCD_COLORS[2],
            LCD_COLORS[3],
            LCD_COLORS[4]
        };
        Util.INSTANCE.validateGradientPoints(FOREGROUND_START, FOREGROUND_STOP);
        return new LinearGradientPaint(FOREGROUND_START, FOREGROUND_STOP, FOREGROUND_FRACTIONS, FOREGROUND_COLORS);
    }

    @Override
    public Point2D getCenter() {
        return new Point2D.Double(bImage.getWidth() / 2.0 + getInnerBounds().x, bImage.getHeight() / 2.0 + getInnerBounds().y);
    }

    @Override
    public Rectangle2D getBounds2D() {
        return new Rectangle2D.Double(bImage.getMinX(), bImage.getMinY(), bImage.getWidth(), bImage.getHeight());
    }

    @Override
    public Rectangle getLcdBounds() {
        return LCD.getBounds();
    }
    
    @Override
    public void setCustomGaugeType(CustomGaugeType CUSTOM_GAUGE_TYPE)
    {
        super.setCustomGaugeType(CUSTOM_GAUGE_TYPE);
        setRatioWH(getGaugeTypeInfo().dimPropRatio);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Areas related">
    private void createAreas(final BufferedImage IMAGE) {
        if (bImage != null) {
            final double ANGLE_STEP;
            if (!isLogScale()) {
                ANGLE_STEP = Math.toDegrees(getModel().getAngleRange()) / (getMaxValue() - getMinValue());
            } else {
                ANGLE_STEP = Math.toDegrees(getModel().getAngleRange()) / UTIL.logOfBase(BASE, (getMaxValue() - getMinValue()));
            }

            if (bImage != null && !getAreas().isEmpty()) {
                final double OUTER_RADIUS = bImage.getWidth() * getWidthRadiusFactor() * tickMarkScale;
                final double RADIUS;
                if (isSectionsVisible()) {
                    RADIUS = isExpandedSectionsEnabled() ? OUTER_RADIUS - bImage.getWidth() * 0.12f : OUTER_RADIUS - bImage.getWidth() * 0.04f;
                } else {
                    RADIUS = OUTER_RADIUS;
                }
//                final Area INNER = new Area(new Ellipse2D.Double(CENTER.getX() - INNER_RADIUS, CENTER.getY() - INNER_RADIUS, 2 * INNER_RADIUS, 2 * INNER_RADIUS));
                final Rectangle2D AREA_FRAME = new Rectangle2D.Double(CENTER.getX() - RADIUS, CENTER.getY() - RADIUS, 2 * RADIUS, 2 * RADIUS);
                for (Section area : getAreas()) {
                    if (!isLogScale()) {
                        area.setFilledArea(new Arc2D.Double(AREA_FRAME, getModel().getOriginCorrection() - (area.getStart() * ANGLE_STEP) + (getMinValue() * ANGLE_STEP), -(area.getStop() - area.getStart()) * ANGLE_STEP, Arc2D.PIE));
                    } else {
                        area.setFilledArea(new Arc2D.Double(AREA_FRAME, getModel().getOriginCorrection() - (UTIL.logOfBase(BASE, area.getStart()) * ANGLE_STEP) + (UTIL.logOfBase(BASE, getMinValue()) * ANGLE_STEP), -UTIL.logOfBase(BASE, area.getStop() - area.getStart()) * ANGLE_STEP, Arc2D.PIE));
                    }
                }
            }

            // Draw the areas
            if (isAreasVisible() && IMAGE != null) {
                final Graphics2D G2 = IMAGE.createGraphics();
                G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                for (Section area : getAreas()) {
                    G2.setColor(isTransparentAreasEnabled() ? area.getTransparentColor() : area.getColor());
                    G2.fill(area.getFilledArea());
                    if (area3DEffectVisible) {
                        G2.setPaint(area3DEffect);
                        G2.fill(area.getFilledArea());
                    }
                }
                G2.dispose();
            }
        }
    }
    //</editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Sections related">
    private void createSections(final BufferedImage IMAGE) {
        if (bImage != null) {
            final double ANGLE_STEP;
            if (!isLogScale()) {
                ANGLE_STEP = getModel().getApexAngle() / (getMaxValue() - getMinValue());
            } else {
                ANGLE_STEP = getModel().getApexAngle() / UTIL.logOfBase(BASE, getMaxValue() - getMinValue());
            }

            final double OUTER_RADIUS = bImage.getWidth() * getWidthRadiusFactor() * tickMarkScale;

            for (Section section : getSections()) {
                final double INNER_RADIUS = OUTER_RADIUS - OUTER_RADIUS * getSectionWidthFactor(section);
                final Area INNER = new Area(new Ellipse2D.Double(CENTER.getX() - INNER_RADIUS, CENTER.getY() - INNER_RADIUS, 2 * INNER_RADIUS, 2 * INNER_RADIUS));
                final double ANGLE_START;
                final double ANGLE_EXTEND;

                if (!isLogScale()) {
                    ANGLE_START = getModel().getOriginCorrection() - (section.getStart() * ANGLE_STEP) + (getMinValue() * ANGLE_STEP);
                    ANGLE_EXTEND = -(section.getStop() - section.getStart()) * ANGLE_STEP;
                } else {
                    ANGLE_START = getModel().getOriginCorrection() - (UTIL.logOfBase(BASE, section.getStart())) * ANGLE_STEP + (UTIL.logOfBase(BASE, getMinValue())) * ANGLE_STEP;
                    ANGLE_EXTEND = -UTIL.logOfBase(BASE, section.getStop() - section.getStart()) * ANGLE_STEP;
                }

                final Arc2D OUTER_ARC = new Arc2D.Double(Arc2D.PIE);
                OUTER_ARC.setFrame(CENTER.getX() - OUTER_RADIUS, CENTER.getY() - OUTER_RADIUS, 2 * OUTER_RADIUS, 2 * OUTER_RADIUS);
                OUTER_ARC.setAngleStart(ANGLE_START);
                OUTER_ARC.setAngleExtent(ANGLE_EXTEND);
                final Area SECTION = new Area(OUTER_ARC);

                SECTION.subtract(INNER);

                section.setSectionArea(SECTION);
            }

            // Draw the sections
            if (isSectionsVisible() && IMAGE != null) {
                final Graphics2D G2 = IMAGE.createGraphics();
                G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                for (Section section : getSections()) {
                    G2.setColor(isTransparentSectionsEnabled() ? section.getTransparentColor() : section.getColor());
                    G2.fill(section.getSectionArea());
                    if (section3DEffectVisible) {
                        G2.setPaint(section3DEffect);
                        G2.fill(section.getSectionArea());
                    }
                }
                G2.dispose();
            }
        }
    }

    protected float getSectionWidthFactor(Section section) {
        return isExpandedSectionsEnabled() ? 0.24f : 0.08f;
    }
    // </editor-fold>

    /**
     * Retrieve the new origin of the pointer image.
     * 
     * @return new origin of the pointer image.
     */
    protected Point2D getPointerImageOrigin() {
        return pointerImageOrigin;
    }

    /**
     * Retrieve the offset if the measured value image.
     * 
     * @return the offset if the measured value image.
     */
    protected Point2D getMeasuredValueOffset() {
        return measuredValueOffset;
    }
    
    @Override
    public void calcInnerBounds(int WIDTH, int HEIGHT) {
        final Insets INSETS = getInsets();
        final Dimension SIZE = computeDimensionWithRatio(WIDTH, HEIGHT);
        getInnerBounds().setBounds(INSETS.left, INSETS.top, WIDTH - INSETS.left - INSETS.right, HEIGHT - INSETS.top - INSETS.bottom);
        getGaugeBounds().setBounds(INSETS.left, INSETS.top, SIZE.width, SIZE.height);
        getFramelessBounds().setBounds(INSETS.left, INSETS.top , (int)(SIZE.width), (int)(SIZE.height));
    }

    /**
     * Checks if is tick label in.
     *
     * @return true, if is tick label in
     */
    public boolean isTickLabelIn() {
        return tickLabelIn;
    }

    /**
     * Sets the tick label in.
     *
     * @param tickLabelIn the new tick label in
     */
    public void setTickLabelIn(boolean tickLabelIn) {
        this.tickLabelIn = tickLabelIn;
        reInitialize();
    }

    /**
     * Gets the tick mark scale.
     *
     * @return the tick mark scale
     */
    public float getTickMarkScale() {
        return tickMarkScale;
    }

    /**
     * Gets the frame thikness.
     *
     * @return the frame thikness
     */
    public float getFrameThikness() {
        return frameThikness;
    }

    /**
     * Sets the frame thikness.
     *
     * @param frameThikness the new frame thikness
     */
    public void setFrameThikness(float frameThikness) {
        this.frameThikness = frameThikness;
        computeScales();
        reInitialize();
    }

    @Override
    public String toString() {
        return "Radial " + getGaugeType();
    }
}
