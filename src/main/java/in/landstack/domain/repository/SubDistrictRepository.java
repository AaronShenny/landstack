package in.landstack.domain.repository;

import in.landstack.domain.entity.SubDistrict;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubDistrictRepository extends JpaRepository<SubDistrict, String> {}
