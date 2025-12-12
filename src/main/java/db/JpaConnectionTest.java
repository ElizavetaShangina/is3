package db;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class JpaConnectionTest {
    public static void main(String[] args) {
        // имя persistence unit из persistence.xml
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("organizationPU");
        EntityManager em = emf.createEntityManager();

        try {
            Long count = em.createQuery("SELECT COUNT(o) FROM Organization o", Long.class)
                    .getSingleResult();

            System.out.println("Количество организаций в базе: " + count);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            em.close();
            emf.close();
        }
    }
}
