//package organization.config;
//
//import jakarta.annotation.Priority;
//import jakarta.inject.Inject;
//import jakarta.interceptor.AroundInvoke;
//import jakarta.interceptor.Interceptor;
//import jakarta.interceptor.InvocationContext;
//import jakarta.persistence.Cache;
//import organization.entity.Organization;
//
//import java.io.Serializable;
//import java.util.concurrent.atomic.AtomicLong;
//
//@Interceptor
//@CacheLog
//@Priority(Interceptor.Priority.APPLICATION)
//public class CacheLoggingInterceptor implements Serializable {
//
//    // Глобальные счетчики
//    public static final AtomicLong hits = new AtomicLong();
//    public static final AtomicLong misses = new AtomicLong();
//    public static boolean enabled = true;
//
//    @Inject
//    private EntityManagerProducer emProducer;
//
//    @AroundInvoke
//    public Object logCache(InvocationContext ctx) throws Exception {
//        if (!enabled) return ctx.proceed();
//
//        Object id = ctx.getParameters()[0]; // Предполагаем findById(Long id)
//        Cache cache = emProducer.getEmf().getCache();
//
//        if (cache.contains(Organization.class, id)) {
//            hits.incrementAndGet();
//            System.out.println("[L2 Cache] HIT for ID: " + id);
//        } else {
//            misses.incrementAndGet();
//            System.out.println("[L2 Cache] MISS for ID: " + id);
//        }
//
//        return ctx.proceed();
//    }
//}

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

    // Глобальные счетчики (можно вывести в UI при желании)
    public static final AtomicLong hits = new AtomicLong();
    public static final AtomicLong misses = new AtomicLong();

    // Флаг управления логами (по умолчанию true для демонстрации)
    public static boolean enabled = true;

    @Inject
    private EntityManagerProducer emProducer;

    @AroundInvoke
    public Object logCache(InvocationContext ctx) throws Exception {
        // Если логирование выключено в UI — просто идем дальше
        if (!enabled) {
            return ctx.proceed();
        }

        // Проверяем, что это метод findById(Long id)
        Object[] parameters = ctx.getParameters();
        if (parameters.length > 0 && parameters[0] instanceof Long) {
            Long id = (Long) parameters[0];

            // Получаем доступ к кэшу через EntityManagerFactory
            Cache cache = emProducer.getEmf().getCache();
            boolean contains = cache.contains(Organization.class, id);

            if (contains) {
                long h = hits.incrementAndGet();
                // Яркий вывод для консоли
                System.out.println("\n\n=======================================================");
                System.out.println(">>> [L2 CACHE] HIT! Object found in cache.");
                System.out.println(">>> Entity: Organization, ID: " + id);
                System.out.println(">>> Total Hits: " + h);
                System.out.println("=======================================================\n");
            } else {
                long m = misses.incrementAndGet();
                System.out.println("\n\n=======================================================");
                System.out.println(">>> [L2 CACHE] MISS. Object NOT found in cache.");
                System.out.println(">>> Entity: Organization, ID: " + id);
                System.out.println(">>> Going to Database...");
                System.out.println(">>> Total Misses: " + m);
                System.out.println("=======================================================\n");
            }
        }

        return ctx.proceed();
    }
}