package organization.config;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import jakarta.persistence.Cache;
import organization.entity.Organization;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

@Interceptor
@CacheLog
@Priority(Interceptor.Priority.APPLICATION)
public class CacheLoggingInterceptor implements Serializable {

    // Глобальные счетчики
    public static final AtomicLong hits = new AtomicLong();
    public static final AtomicLong misses = new AtomicLong();
    public static boolean enabled = true;

    @Inject
    private EntityManagerProducer emProducer;

    @AroundInvoke
    public Object logCache(InvocationContext ctx) throws Exception {
        if (!enabled) return ctx.proceed();

        Object id = ctx.getParameters()[0]; // Предполагаем findById(Long id)
        Cache cache = emProducer.getEmf().getCache();

        if (cache.contains(Organization.class, id)) {
            hits.incrementAndGet();
            System.out.println("[L2 Cache] HIT for ID: " + id);
        } else {
            misses.incrementAndGet();
            System.out.println("[L2 Cache] MISS for ID: " + id);
        }

        return ctx.proceed();
    }
}