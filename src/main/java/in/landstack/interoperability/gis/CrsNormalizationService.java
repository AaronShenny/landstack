package in.landstack.interoperability.gis;

import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Geometry;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.api.referencing.operation.MathTransform;
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

        if (sourceCrsEpsgCode == null || "CRS_UNKNOWN".equalsIgnoreCase(sourceCrsEpsgCode)) {
            logger.warn("Source CRS is null or unknown. Assuming geometry is already EPSG:4326.");
            sourceGeometry.setSRID(4326);
            return sourceGeometry;
        }

        if (TARGET_CRS.equalsIgnoreCase(sourceCrsEpsgCode)) {
            sourceGeometry.setSRID(4326);
            return sourceGeometry;
        }

        try {
            CoordinateReferenceSystem sourceCRS = CRS.decode(sourceCrsEpsgCode, true);
            CoordinateReferenceSystem targetCRS = CRS.decode(TARGET_CRS, true);
            MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS, true);
            
            Geometry targetGeometry = JTS.transform(sourceGeometry, transform);
            targetGeometry.setSRID(4326);
            return targetGeometry;
        } catch (Exception e) {
            logger.error("Failed to transform geometry from {} to {}: {}", sourceCrsEpsgCode, TARGET_CRS, e.getMessage());
            throw new IllegalArgumentException("CRS Transformation Failed: " + e.getMessage(), e);
        }
    }
}

