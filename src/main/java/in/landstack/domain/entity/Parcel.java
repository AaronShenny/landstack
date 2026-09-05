package in.landstack.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import org.locationtech.jts.geom.Polygon;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "parcels")
@FilterDef(name = "stateFilter", parameters = @ParamDef(name = "stateCode", type = String.class))
@Filter(name = "stateFilter", condition = "state_code = :stateCode")
@Data
public class Parcel {
    @Id
    private String ulpin;
    
    @ManyToOne
    @JoinColumn(name = "state_code")
    private State state;
    
    @ManyToOne
    @JoinColumn(name = "district_code")
    private District district;
    
    @ManyToOne
    @JoinColumn(name = "village_code")
    private Village village;
    
    private String localParcelId;
    
    private Polygon geom;
    
    @Column(columnDefinition = "numeric(10,2)")
    private Double areaSqm;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
