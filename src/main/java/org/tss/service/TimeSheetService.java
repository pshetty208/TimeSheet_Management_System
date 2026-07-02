package org.tss.service;

import org.springframework.stereotype.Service;
import org.tss.model.TimeSheet;
import org.tss.repository.TimeSheetRepository;

import java.util.List;
import java.util.Optional;

@Service
public class TimeSheetService {
    private final TimeSheetRepository timeSheetRepository;

    public TimeSheetService(TimeSheetRepository timeSheetRepository) {
        this.timeSheetRepository = timeSheetRepository;
    }

    public TimeSheet save(TimeSheet t) {
        return timeSheetRepository.save(t);
    }

    public Optional<TimeSheet> findById(Long id) {
        return timeSheetRepository.findById(id);
    }

    public List<TimeSheet> findAll() {
        return timeSheetRepository.findAll();
    }

    public void delete(Long id) {
        timeSheetRepository.deleteById(id);
    }
}
