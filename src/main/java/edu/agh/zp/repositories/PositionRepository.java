package edu.agh.zp.repositories;

import edu.agh.zp.objects.PositionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PositionRepository extends JpaRepository<PositionEntity, Long> {
}