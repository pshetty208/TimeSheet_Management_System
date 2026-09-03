package org.tss.service;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.tss.model.Contract;
import org.tss.model.TimeSheet;

@Service("access")
public class AccessService {
    public boolean contract(Contract c, Authentication auth) {
        if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRATOR"))) return true;
        String u = auth.getName();
        return same(c.getEmployee(),u) || same(c.getSupervisor(),u)
                || c.getAssistants().stream().anyMatch(x -> same(x,u))
                || c.getSecretaries().stream().anyMatch(x -> same(x,u));
    }
    public boolean timesheet(TimeSheet t, Authentication auth) { return contract(t.getContract(), auth); }
    public boolean contractManager(Contract c, Authentication auth) {
        if (isAdmin(auth)) return true;
        String u = auth.getName();
        return same(c.getSupervisor(), u) || c.getAssistants().stream().anyMatch(x -> same(x, u));
    }
    public boolean contractSecretary(Contract c, Authentication auth) {
        if (isAdmin(auth)) return true;
        return c.getSecretaries().stream().anyMatch(x -> same(x, auth.getName()));
    }
    private boolean isAdmin(Authentication auth) {
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRATOR"));
    }
    private boolean same(org.tss.model.User user, String username) { return user != null && username.equals(user.getUsername()); }
}
