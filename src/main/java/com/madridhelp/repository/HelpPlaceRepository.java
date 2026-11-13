package com.madridhelp.repository;

import com.madridhelp.model.HelpPlace;
import com.madridhelp.model.PlaceType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Spring Data JPA builds the implementation at startup.
 * findByType is parsed from its name: "find" + "By" + the field "type".
 *
 * @EntityGraph loads the schedules in the same query (a JOIN). Without it, Hibernate would
 * run one extra query per place to load its schedules (the "N+1 problem"), and with
 * open-in-view=false the lazy collection could not be read outside the transaction.
 */
public interface HelpPlaceRepository extends JpaRepository<HelpPlace, Long> {

    @Override
    @EntityGraph(attributePaths = "schedules")
    List<HelpPlace> findAll();

    @EntityGraph(attributePaths = "schedules")
    List<HelpPlace> findByType(PlaceType type);
}
