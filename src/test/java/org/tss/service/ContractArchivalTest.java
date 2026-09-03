package org.tss.service;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.tss.model.Contract;
import org.tss.model.TimeSheet;
import org.tss.repository.ContractRepository;
import org.tss.repository.TimeSheetRepository;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ContractArchivalTest {
    @Test void cn12ContractArchivesWhenFinalTimesheetArchives() {
        TimeSheetRepository sheets=mock(TimeSheetRepository.class); ContractRepository contracts=mock(ContractRepository.class);
        Contract c=new Contract(); c.setStatus("STARTED"); TimeSheet first=new TimeSheet(); first.setContract(c); first.setStatus("ARCHIVED"); TimeSheet last=new TimeSheet(); last.setId(2L); last.setContract(c); last.setStatus("SIGNED_BY_SUPERVISOR");
        when(sheets.findById(2L)).thenReturn(Optional.of(last)); when(sheets.save(any())).thenAnswer(i->i.getArgument(0)); when(sheets.findByContractIdOrderByPeriodStart(c.getId())).thenReturn(List.of(first,last));
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("admin","",List.of(new SimpleGrantedAuthority("ROLE_ADMINISTRATOR"))));
        new TimeSheetService(sheets,contracts).archive(2L);
        assertEquals("ARCHIVED",c.getStatus()); verify(contracts).save(c); SecurityContextHolder.clearContext();
    }
}
