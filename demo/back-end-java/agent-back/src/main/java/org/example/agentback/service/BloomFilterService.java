package org.example.agentback.service;

public interface BloomFilterService {
    public void init();
    public boolean mightContain(String userId);
    public void addUserId(String userId);
}
