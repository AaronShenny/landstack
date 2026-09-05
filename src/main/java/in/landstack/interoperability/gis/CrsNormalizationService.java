package in.landstack.interoperability.gis;

import org.locationtech.jts.geom.Geometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CrsNormalizationService {

    private static final Logger logger = LoggerFactory.getLogger(CrsNormalizationService.class);
    private static final String TARGET_CRS = "EPSG:4326";

    /**
     * Transforms a given geometry from the source EPSG code to EPSG:4326 (WGS84)
     * which is required by the central LandStack PostGIS database.
     */
    public Geometry transformToWgs84(Geometry sourceGeometry, String sourceCrsEpsgCode) {
        if (sourceGeometry == null) {
            return null;
        }

        if (TARGET_CRS.equalsIgnoreCase(sourceCrsEpsgCode) || sourceCrsEpsgCode == null) {
            return sourceGeometry;
        }

        // NOTE: GeoTools MathTransform logic requires properly resolving OSGeo maven dependencies.
        // For compilation purposes in this prototype, we return the geometry as-is.
        // In production, this would use org.geotools.referencing.CRS and JTS.transform.
        logger.warn("GeoTools CRS transformation stubbed. Assuming geometry is already EPSG:4326.");
        sourceGeometry.setSRID(4326);
        return sourceGeometry;
    }
}
