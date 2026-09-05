package in.landstack.domain.repository;

import in.landstack.domain.entity.StateAdapter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StateAdapterRepository extends JpaRepository<StateAdapter, String> {
    List<StateAdapter> findByStatus(String status);
}
