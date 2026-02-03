package com.hospital.opd.service;

import com.hospital.opd.domain.Doctor;
import com.hospital.opd.domain.TimeSlot;
import com.hospital.opd.domain.Token;
import com.hospital.opd.domain.enums.TokenSource;
import com.hospital.opd.domain.enums.TokenStatus;
import com.hospital.opd.repository.DoctorRepository;
import com.hospital.opd.repository.TimeSlotRepository;
import com.hospital.opd.repository.TokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduleService {
    private final DoctorRepository doctorRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final TokenRepository tokenRepository;

    public Doctor createDoctor(String name, String specialization) {
        return doctorRepository.save(new Doctor(name, specialization));
    }

    public TimeSlot createSlot(Long doctorId, LocalTime start, LocalTime end, int capacity) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        return timeSlotRepository.save(new TimeSlot(doctor, start, end, capacity));
    }

    public List<TimeSlot> getDoctorSlots(Long doctorId) {
        return timeSlotRepository.findByDoctorId(doctorId);
    }
    
    @Transactional
    public Token bookToken(String patientName, String contactNumber, String userIdNumber, TokenSource source, Long slotId) {
        TimeSlot slot = timeSlotRepository.findById(slotId)
                .orElseThrow(() -> new RuntimeException("Slot not found"));
        
        Token token = new Token(patientName, contactNumber, userIdNumber, source, slot);
        
        long activeCount = tokenRepository.countByAssignedSlotIdAndStatus(slotId, TokenStatus.ACTIVE);
        
        // Allocation Logic
        if (activeCount < slot.getMaxCapacity()) {
            token.setStatus(TokenStatus.ACTIVE);
        } else {
            // Slot Full
            if (source == TokenSource.EMERGENCY) {
                // Emergency overrides capacity
                token.setStatus(TokenStatus.ACTIVE);
            } else {
                // Others go to waitlist
                token.setStatus(TokenStatus.WAITLIST);
            }
        }
        
        return tokenRepository.save(token);
    }
    
    @Transactional
    public void cancelToken(Long tokenId) {
        Token token = tokenRepository.findById(tokenId)
                .orElseThrow(() -> new RuntimeException("Token not found"));
                
        // Only trigger reallocation if cancelling an active token
        boolean wasActive = token.getStatus() == TokenStatus.ACTIVE;
        
        token.setStatus(TokenStatus.CANCELLED);
        tokenRepository.save(token);
        
        if (wasActive) {
            reallocateSlot(token.getAssignedSlot().getId());
        }
    }
    
    private void reallocateSlot(Long slotId) {
        TimeSlot slot = timeSlotRepository.findById(slotId)
                .orElseThrow(() -> new RuntimeException("Slot not found"));
                
        long activeCount = tokenRepository.countByAssignedSlotIdAndStatus(slotId, TokenStatus.ACTIVE);
        
        if (activeCount < slot.getMaxCapacity()) {
            // Find highest priority waitlisted token
            // Sort by Priority ASC (1=Emergency... 5=WalkIn) then CreatedAt ASC (FIFO)
            List<Token> waitlist = tokenRepository.findByAssignedSlotIdAndStatusOrderByPriorityAscCreatedAtAsc(slotId, TokenStatus.WAITLIST);
            
            if (!waitlist.isEmpty()) {
                Token nextToken = waitlist.get(0);
                nextToken.setStatus(TokenStatus.ACTIVE);
                tokenRepository.save(nextToken);
            }
        }
    }
}
