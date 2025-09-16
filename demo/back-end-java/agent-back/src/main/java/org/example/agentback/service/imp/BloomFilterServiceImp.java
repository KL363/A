package org.example.agentback.service.imp;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.example.agentback.service.BloomFilterService;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BloomFilterServiceImp implements BloomFilterService {

    private final RedissonClient redissonClient;

    private static final String USER_BLOOM_FILTER = "user_bloom_filter";

    // 初始化布隆过滤器 (预期1000万用户，1%误判率)
    @PostConstruct
    @Override
    public void init() {
        RBloomFilter<String> bloomFilter = redissonClient.getBloomFilter(USER_BLOOM_FILTER);
        bloomFilter.tryInit(10_000_000L, 0.01);
    }

    @Override
    public boolean mightContain(String userId) {
        RBloomFilter<String> bloomFilter = redissonClient.getBloomFilter(USER_BLOOM_FILTER);
        return bloomFilter.contains(userId);
    }

    @Override
    public void addUserId(String userId) {
        RBloomFilter<String> bloomFilter = redissonClient.getBloomFilter(USER_BLOOM_FILTER);
        bloomFilter.add(userId);
    }
}
