package eu.hansolo.steelseries.tools;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.Dimension;
import java.awt.geom.Point2D;

import org.junit.Test;

public class GaugeTypeUtilsTest
{

    @Test
    public void isInRange()
    {
        assertTrue(GaugeTypeUtil.isInRange(new CustomGaugeType(180, 0), 0));
        assertFalse(GaugeTypeUtil.isInRange(new CustomGaugeType(180, 0), Math.PI * 3 / 4));
        assertTrue(GaugeTypeUtil.isInRange(new CustomGaugeType(180, 0), Math.PI / 2));
        assertTrue(GaugeTypeUtil.isInRange(new CustomGaugeType(180, 0), -Math.PI / 2));
        assertFalse(GaugeTypeUtil.isInRange(new CustomGaugeType(180, 0), Math.PI));
        assertTrue(GaugeTypeUtil.isInRange(new CustomGaugeType(180, 0), Math.PI * 3 / 2));
        
        assertTrue(GaugeTypeUtil.isInRange(new CustomGaugeType(90, 0), 0));
        assertTrue(GaugeTypeUtil.isInRange(new CustomGaugeType(90, 0), Math.PI / 4));
        assertTrue(GaugeTypeUtil.isInRange(new CustomGaugeType(90, 0), Math.PI / 2));
        assertFalse(GaugeTypeUtil.isInRange(new CustomGaugeType(90, 0), Math.PI));
        
        assertTrue(GaugeTypeUtil.isInRange(new CustomGaugeType(180, 270), 0));
        assertTrue(GaugeTypeUtil.isInRange(new CustomGaugeType(180, 270), Math.PI / 2));
        assertFalse(GaugeTypeUtil.isInRange(new CustomGaugeType(180, 270), -Math.PI / 2));
        assertFalse(GaugeTypeUtil.isInRange(new CustomGaugeType(180, 270), Math.PI * 3 / 2));
        
        assertTrue(GaugeTypeUtil.isInRange(new CustomGaugeType(180, 90), 0));
        assertTrue(GaugeTypeUtil.isInRange(new CustomGaugeType(180, 90), -Math.PI / 2));
        assertTrue(GaugeTypeUtil.isInRange(new CustomGaugeType(180, 90), Math.PI));
        assertFalse(GaugeTypeUtil.isInRange(new CustomGaugeType(180, 90), Math.PI / 2));
    }
    
    @Test
    public void roundTrigoValue()
    {
        assertEquals(Math.PI, GaugeTypeUtil.roundTrigoValue(Math.PI * 3), 0d);
        assertEquals(Math.PI / 3, GaugeTypeUtil.roundTrigoValue(Math.PI / 3), 0d);
        assertEquals(Math.PI / 2, GaugeTypeUtil.roundTrigoValue(-Math.PI * 3 / 2), 0d);
        assertEquals(Math.PI / 2, GaugeTypeUtil.roundTrigoValue(-Math.PI * 7 / 2), 0d);
    }
    
    @Test
    public void computeDimention()
    {
        final double thetaMargingDeg = GaugeTypeUtil.FRAME_THETA_MARGING_DEG;
        Dimension dim = new Dimension(200, 200);
        Point2D center = new Point2D.Double(0, 0);
        GaugeTypeUtil.computeDimention(GaugeTypeInfo.getGaugeTypeInfo(new CustomGaugeType(360, 0), 0), dim, center);
        assertEquals(200, dim.height);
        assertEquals(200, dim.width);
        assertEquals(100, (int) center.getX());
        assertEquals(100, (int) center.getY());
        
        dim.setSize(200, 200);
        GaugeTypeUtil.computeDimention(GaugeTypeInfo.getGaugeTypeInfo(new CustomGaugeType(180, 0), 0), dim, center);
        double ratioLeft = Math.max(GaugeTypeUtil.FRAME_MARGING, Math.cos(Math.PI / 2 - GaugeTypeUtil.FRAME_THETA_MARGING_RAD));
        assertEquals((int) (100 + ratioLeft * 100), dim.width);
        assertEquals(200, dim.height);
        assertEquals((int) (ratioLeft * 100), (int) center.getX());
        assertEquals(100, (int) center.getY());
        
        dim.setSize(200, 200);
        GaugeTypeUtil.computeDimention(GaugeTypeInfo.getGaugeTypeInfo(new CustomGaugeType(90, 0), 0), dim, center);
        ratioLeft = Math.max(GaugeTypeUtil.FRAME_MARGING, Math.cos(Math.PI / 2 - GaugeTypeUtil.FRAME_THETA_MARGING_RAD));
        double ratioDown = Math.max(GaugeTypeUtil.FRAME_MARGING, Math.sin(GaugeTypeUtil.FRAME_THETA_MARGING_RAD));
        assertEquals(199, dim.width);
        assertEquals(200, dim.height);
        assertEquals((int) (200 * ratioLeft / (1 + ratioLeft)), (int) center.getX());
        assertEquals((int) (200 * 1 / (1 + ratioDown)), (int) center.getY());
        
        dim.setSize(200, 200);
        GaugeTypeUtil.computeDimention(GaugeTypeInfo.getGaugeTypeInfo(new CustomGaugeType(180 - 2 * thetaMargingDeg, 0 + thetaMargingDeg), 0), dim, center);
        assertEquals((int) (100 + GaugeTypeUtil.FRAME_MARGING * 100), dim.width);
        assertEquals(200, dim.height);
        assertEquals((int) (GaugeTypeUtil.FRAME_MARGING * 100), (int) center.getX());
        assertEquals(100, (int) center.getY());
        
        dim.setSize(200, 200);
        GaugeTypeUtil.computeDimention(GaugeTypeInfo.getGaugeTypeInfo(new CustomGaugeType(90 - 2 * thetaMargingDeg, 0 + thetaMargingDeg), 0), dim, center);
        assertEquals(200, dim.width);
        assertEquals(200, dim.height);
        assertEquals((int) (200 * GaugeTypeUtil.FRAME_MARGING / (1 + GaugeTypeUtil.FRAME_MARGING)), (int) center.getX());
        assertEquals((int) (200 * 1 / (1 + GaugeTypeUtil.FRAME_MARGING)), (int) center.getY());
        
    }
}
