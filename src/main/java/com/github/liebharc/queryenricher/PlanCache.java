package com.github.liebharc.queryenricher;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class PlanCache {

    private final LoadingCache<Request, Plan> plans;

    public PlanCache(int cacheSize, PlanBuilder planBuilder) {
         plans = CacheBuilder.newBuilder()
                .maximumSize(cacheSize)
                .build(
                        new CacheLoader<Request, Plan>() {
                            @Override
                            public Plan load(Request request) {
                                return planBuilder.build(request);
                            }
                        }
                );
    }

    public Plan getOrBuildPlan(Request request) {
        return this.plans.getUnchecked(request);
    }
}
