package com.luckyDay.video.service.audit;

public interface AuditService<T,R> {

    R audit(T task);
}
