package com.hospital.opd.repository;

import com.hospital.opd.domain.Token;
import com.hospital.opd.domain.enums.TokenStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {
    long countByAssignedSlotIdAndStatus(Long slotId, TokenStatus status);

    List<Token> findByAssignedSlotIdAndStatus(Long slotId, TokenStatus status);
    
    // Find active tokens for a slot ordered by priority (descending priority value means lower priority)
    // Actually we want to find the lowest priority active token to bump. 
    // Higher priority value = Lower Logic Priority (5 vs 1)
    // So to find lowest priority, we sort by priority DESC (5, 4, 3...)
    List<Token> findByAssignedSlotIdAndStatusOrderByPriorityDesc(Long slotId, TokenStatus status);

    // Find waitlisted tokens for a slot ordered by priority (ascending value 1, 2, 3...) and then created time
    List<Token> findByAssignedSlotIdAndStatusOrderByPriorityAscCreatedAtAsc(Long slotId, TokenStatus status);
}
