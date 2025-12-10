package lms.doantotnghiep.dto.response;

import java.util.*;

public class ScanResult {
    private final Set<String> reasons = new LinkedHashSet<>();
    private final Map<String, String> details = new LinkedHashMap<>();

    public void addReason(String r) {
        reasons.add(r);
    }

    public void addDetail(String k, String v) {
        details.put(k, v);
    }

    public boolean isDangerous() {
        return !reasons.isEmpty();
    }

    public List<String> getReasons() {
        return new ArrayList<>(reasons);
    }

    public Map<String, String> getDetails() {
        return details;
    }
}