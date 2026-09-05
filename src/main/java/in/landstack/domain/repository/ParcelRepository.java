package in.landstack.domain.repository;

import in.landstack.domain.entity.Parcel;
import org.locationtech.jts.geom.Geometry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParcelRepository extends JpaRepository<Parcel, String> {
    
    // Hibernate Spatial query checking if parcel geometry intersects with the provided Bounding Box
    @Query("SELECT p FROM Parcel p WHERE within(p.geom, :bbox) = true")
    List<Parcel> findWithinBoundingBox(@Param("bbox") Geometry bbox);
}
