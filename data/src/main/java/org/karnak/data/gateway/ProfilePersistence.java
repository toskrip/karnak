package org.karnak.data.gateway;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfilePersistence extends JpaRepository<ProfileTable, Long> {
    Boolean existsByName(String name);

    ProfileTable findByName(String name);
}
