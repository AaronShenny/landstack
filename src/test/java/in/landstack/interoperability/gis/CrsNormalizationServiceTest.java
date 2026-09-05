package in.landstack.interoperability.gis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import static org.junit.jupiter.api.Assertions.*;

class CrsNormalizationServiceTest {

    private CrsNormalizationService crsNormalizationService;
    private GeometryFactory geometryFactory;

    @BeforeEach
    void setUp() {
        crsNormalizationService = new CrsNormalizationService();
        geometryFactory = new GeometryFactory();
    }

    @Test
    void testEpsg32643ToEpsg4326() {
        // EPSG:32643 is WGS 84 / UTM zone 43N
        // Let's take a point in Delhi, India
        // Lat: 28.6139, Lon: 77.2090
        // Easting: 715978.89, Northing: 3167151.77 (approx)

        Coordinate[] coords = new Coordinate[] {
            new Coordinate(715978.89, 3167151.77),
            new Coordinate(716078.89, 3167151.77),
            new Coordinate(716078.89, 3167251.77),
            new Coordinate(715978.89, 3167251.77),
            new Coordinate(715978.89, 3167151.77)
        };
        Polygon utmPolygon = geometryFactory.createPolygon(coords);
        utmPolygon.setSRID(32643);

        Polygon wgs84Polygon = (Polygon) crsNormalizationService.transformToWgs84(utmPolygon, "EPSG:32643");

        assertNotNull(wgs84Polygon);
        assertEquals(4326, wgs84Polygon.getSRID());

        // Check if the coordinates are roughly in Delhi
        Point centroid = wgs84Polygon.getCentroid();
        // GeoTools with forceXY = true means X = Lon, Y = Lat
        // Let's check X (Lon) ~ 77.2 and Y (Lat) ~ 28.6
        assertTrue(Math.abs(centroid.getX() - 77.209) < 0.05, "Longitude should be ~77.2, was " + centroid.getX());
        assertTrue(Math.abs(centroid.getY() - 28.613) < 0.05, "Latitude should be ~28.6, was " + centroid.getY());
    }

    @Test
    void testEpsg32644ToEpsg4326() {
        // EPSG:32644 is WGS 84 / UTM zone 44N
        // Let's take a point in Chennai, India
        // Lat: 13.0827, Lon: 80.2707
        // Easting: 420959.0, Northing: 1446187.0 (approx)

        Coordinate[] coords = new Coordinate[] {
            new Coordinate(420959.0, 1446187.0),
            new Coordinate(421059.0, 1446187.0),
            new Coordinate(421059.0, 1446287.0),
            new Coordinate(420959.0, 1446287.0),
            new Coordinate(420959.0, 1446187.0)
        };
        Polygon utmPolygon = geometryFactory.createPolygon(coords);
        utmPolygon.setSRID(32644);

        Polygon wgs84Polygon = (Polygon) crsNormalizationService.transformToWgs84(utmPolygon, "EPSG:32644");

        assertNotNull(wgs84Polygon);
        assertEquals(4326, wgs84Polygon.getSRID());

        Point centroid = wgs84Polygon.getCentroid();
        assertTrue(Math.abs(centroid.getX() - 80.27) < 0.05, "Longitude should be ~80.27, was " + centroid.getX());
        assertTrue(Math.abs(centroid.getY() - 13.08) < 0.05, "Latitude should be ~13.08, was " + centroid.getY());
    }
    
    @Test
    void testUnknownCrsThrowsException() {
        Coordinate[] coords = new Coordinate[] {
            new Coordinate(0, 0), new Coordinate(1, 0), new Coordinate(1, 1), new Coordinate(0, 1), new Coordinate(0, 0)
        };
        Polygon p = geometryFactory.createPolygon(coords);
        
        Exception e = assertThrows(IllegalArgumentException.class, () -> {
            crsNormalizationService.transformToWgs84(p, "EPSG:999999");
        });
        assertTrue(e.getMessage().contains("CRS Transformation Failed"));
    }
}
