package com.capstone.jfc.service;

import com.capstone.jfc.model.ConcurrencyConfigEntity;
import com.capstone.jfc.repository.ConcurrencyConfigRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ConcurrencyConfigService {

    private final ConcurrencyConfigRepository configRepository;

    public ConcurrencyConfigService(ConcurrencyConfigRepository configRepository) {
        this.configRepository = configRepository;
    }

    /**
     * Loads all concurrency config entries from the database
     * and returns two separate maps:
     *
     * 1) typeToolMap: Map<(eventType, tool), concurrencyLimit>
     * 2) tenantMap: Map<tenantId, concurrencyLimit>
     */
    public ConcurrencyConfigData loadAllConcurrencyConfigs() {
        List<ConcurrencyConfigEntity> allConfigs = configRepository.findAll();

        Map<String, Integer> typeToolMap = new HashMap<>();
        Map<Long, Integer> tenantMap = new HashMap<>();

        for (ConcurrencyConfigEntity cfg : allConfigs) {
            if ("TYPE_TOOL".equalsIgnoreCase(cfg.getConcurrencyType())) {
                // Build a key like: "SCAN_REQUEST_JOB:code-scan"
                String key = cfg.getEventType() + ":" + cfg.getTool();
                typeToolMap.put(key, cfg.getConcurrencyLimit());
            } else if ("TENANT".equalsIgnoreCase(cfg.getConcurrencyType()) && cfg.getTenantId() != null) {
                tenantMap.put(cfg.getTenantId(), cfg.getConcurrencyLimit());
            }
        }

        return new ConcurrencyConfigData(typeToolMap, tenantMap);
    }

    /**
     * Wrapper class to hold the two maps.
     */
    public static class ConcurrencyConfigData {
        private final Map<String, Integer> typeToolMap; // key = "eventType:tool"
        private final Map<Long, Integer> tenantMap;     // key = tenantId

        public ConcurrencyConfigData(Map<String, Integer> typeToolMap, Map<Long, Integer> tenantMap) {
            this.typeToolMap = typeToolMap;
            this.tenantMap = tenantMap;
        }

        public Map<String, Integer> getTypeToolMap() {
            return typeToolMap;
        }

        public Map<Long, Integer> getTenantMap() {
            return tenantMap;
        }
    }
}
