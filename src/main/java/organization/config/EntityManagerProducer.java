package organization.config;

import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.apache.commons.dbcp2.BasicDataSource;

import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class EntityManagerProducer {

    private EntityManagerFactory emf;
    private BasicDataSource dataSource;

    public EntityManagerProducer() {
        // Настройка DBCP2
        dataSource = new BasicDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl("jdbc:postgresql://localhost:5432/studs"); // Твои настройки
        dataSource.setUsername("s408391"); // Твой логин
        dataSource.setPassword("your_password"); // Твой пароль (лучше вынести в ENV)

        // Параметры пула (для отчета)
        dataSource.setInitialSize(5);
        dataSource.setMaxTotal(20);
        dataSource.setMaxIdle(10);
        dataSource.setMinIdle(5);
        dataSource.setMaxWaitMillis(5000);

        // Передаем DataSource в Persistence Unit
        Map<String, Object> properties = new HashMap<>();
        properties.put("jakarta.persistence.nonJtaDataSource", dataSource);

        this.emf = Persistence.createEntityManagerFactory("organizationPU", properties);
    }

    @Produces
    @RequestScoped
    public EntityManager createEntityManager() {
        return emf.createEntityManager();
    }

    public void closeEntityManager(@jakarta.enterprise.inject.Disposes EntityManager em) {
        if (em.isOpen()) {
            em.close();
        }
    }

    @PreDestroy
    public void close() {
        if (emf != null && emf.isOpen()) emf.close();
        try { if (dataSource != null) dataSource.close(); } catch (Exception e) { e.printStackTrace(); }
    }

    public EntityManagerFactory getEmf() {
        return emf;
    }
}