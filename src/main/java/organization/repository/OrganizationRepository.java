package organization.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import lombok.NoArgsConstructor;
import organization.entity.Address;
import organization.entity.Organization;
import organization.entity.OrganizationType;
//import organization.entity.TestEntity;

import java.util.List;

@ApplicationScoped
@NoArgsConstructor
public class OrganizationRepository {

    @PersistenceContext(unitName = "organizationPU")
    private EntityManager em;

    public Organization create(Organization organization) {
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.persist(organization);
            transaction.commit();
            return organization;
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw e;
        }
    }
    //если получится вернуть аннотацию
//    public void create(Organization organization) {
//        em.persist(organization);
//        System.out.println(organization.getId());
//    }

    public void delete(Organization organization) {
        if (organization == null) {
            return;
        }

        EntityTransaction transaction = em.getTransaction();

        try {
            transaction.begin();

            if (!em.contains(organization)) {
                organization = em.merge(organization);
            }

            em.remove(organization);
            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw e;
        }
    }

    public void deleteById(Long id) {
        EntityTransaction transaction = em.getTransaction();
        Organization organization = em.find(Organization.class, id);
        if (organization != null) {
            try {
                transaction.begin();
                em.remove(organization);
                transaction.commit();
            } catch (Exception e) {
                if (transaction.isActive()) {
                    transaction.rollback();
                }
                throw e;
            }
        }
    }

//    public void update(Organization organization) {
//        em.merge(organization);
//    }
    public void update(Organization organization) {
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.merge(organization);
            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw e;
        }
    }

    public Organization findById(Long id) {
        return em.find(Organization.class, id);
    }

//возвращает с максимальным значением - последнее в алф порядке
    public Organization getOrganizationWithMaxFullName() {
        List<Organization> result = em.createQuery(
                        "SELECT o FROM Organization o WHERE o.fullName = (SELECT MAX(o2.fullName) FROM Organization o2)", Organization.class)
                .getResultList();
        return result.isEmpty() ? null : result.get(0);
    }

//    public long countByPostalAddress(Address postalAddress) {
//        return em.createQuery("SELECT COUNT(o) FROM Organization o WHERE o.postalAddress = :address", Long.class)
//                .setParameter("address", postalAddress)
//                .getSingleResult();
//    }
    public long countByPostalAddress(Address postalAddress) {
        String street = postalAddress.getStreet();
        String zip = postalAddress.getZipCode();

        if (zip == null || zip.isBlank()) {
            return em.createQuery(
                            "SELECT COUNT(o) FROM Organization o WHERE o.postalAddress.street = :street",
                            Long.class
                    )
                    .setParameter("street", street)
                    .getSingleResult();
        } else {
            return em.createQuery(
                            "SELECT COUNT(o) FROM Organization o WHERE o.postalAddress.street = :street AND o.postalAddress.zipCode = :zip",
                            Long.class
                    )
                    .setParameter("street", street)
                    .setParameter("zip", zip)
                    .getSingleResult();
        }
    }

    public long countByTypeLessThan(OrganizationType type) {
        return em.createQuery("SELECT COUNT(o) FROM Organization o WHERE o.type < :type", Long.class)
                .setParameter("type", type)
                .getSingleResult();
    }

    public List<Organization> findAll() {
        TypedQuery<Organization> query = em.createQuery(
                "SELECT o FROM Organization o", Organization.class);
        return query.getResultList();
    }

    //для дебага
//    public TestEntity createTestEntity(TestEntity testEntity) {
//        EntityTransaction transaction = em.getTransaction();
//        try {
//            transaction.begin();
//            em.persist(testEntity);
//            transaction.commit();
//            return testEntity;
//        } catch (Exception e) {
//            if (transaction.isActive()) {
//                transaction.rollback();
//            }
//            throw e;
//        }
//    }
//    @Transactional
//    public TestEntity createTestEntity(TestEntity testEntity) {
//        em.persist(testEntity);
//        return testEntity;
//    }
}


