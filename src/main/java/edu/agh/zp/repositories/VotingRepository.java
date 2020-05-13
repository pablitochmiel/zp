package edu.agh.zp.repositories;

import edu.agh.zp.objects.CitizenEntity;
import edu.agh.zp.objects.VotingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

public interface VotingRepository extends JpaRepository<VotingEntity, Long> {
    VotingEntity findByVotingID(Long VotingID);

    List<VotingEntity> findByVotingDate(Date votingDate);
    List<VotingEntity> findByVotingDateBetweenOrderByVotingDateAscOpenVotingAsc(Date votingDate, Date votingDate2);
}
