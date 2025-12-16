package organization.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.NoArgsConstructor;
import organization.config.CacheLog; // Наша новая аннотация
import organization.entity.Address;
import organization.entity.Organization;
import organization.entity.OrganizationType;

import java.util.List;

@ApplicationScoped
@NoArgsConstructor
public class OrganizationRepository {

    // ВАЖНО: Используем @Inject, чтобы получить EM из нашего EntityManagerProducer (DBCP2)
    @Inject
    private EntityManager em;

    public void create(Organization organization) {
        em.persist(organization);
        em.flush();
    }

    public void delete(Organization organization) {
        if (organization != null) {
            if (!em.contains(organization)) {
                organization = em.merge(organization);
            }
            em.remove(organization);
        }
    }

    public void deleteById(Long id) {
        Organization organization = em.find(Organization.class, id);
        if (organization != null) {
            em.remove(organization);
        }
    }

    public void update(Organization organization) {
        em.merge(organization);
    }

    // ВАЖНО: Вешаем интерцептор для статистики кэша сюда
    @CacheLog
    public Organization findById(Long id) {
        return em.find(Organization.class, id);
    }

    public Organization getOrganizationWithMaxFullName() {
        List<Organization> result = em.createQuery(
                        "SELECT o FROM Organization o WHERE o.fullName = (SELECT MAX(o2.fullName) FROM Organization o2)", Organization.class)
                .getResultList();
        return result.isEmpty() ? null : result.get(0);
    }

    public long countByPostalAddress(Address postalAddress) {
        String street = postalAddress.getStreet();
        String zip = postalAddress.getZipCode();

        if (zip == null || zip.isBlank()) {
            return em.createQuery(
                            "SELECT COUNT(o) FROM Organization o WHERE o.postalAddress.street = :street", Long.class)
                    .setParameter("street", street)
                    .getSingleResult();
        } else {
            return em.createQuery(
                            "SELECT COUNT(o) FROM Organization o WHERE o.postalAddress.street = :street AND o.postalAddress.zipCode = :zip", Long.class)
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
        return em.createQuery("SELECT o FROM Organization o", Organization.class).getResultList();
    }
}