package kz.eospatial.GeoForestry.controllers;

import kz.eospatial.GeoForestry.facades.ForestryFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/forestry")
public class ForestryController {

    private static final Logger log = LoggerFactory.getLogger(ForestryController.class);

    private final ForestryFacade forestryFacade;

    public ForestryController(ForestryFacade forestryFacade) {
        this.forestryFacade = forestryFacade;
    }

    @GetMapping("/{token}")
    public ResponseEntity<?> getForestryByToken(@PathVariable String token) {
        return forestryFacade.getForestryByToken(token);
    }
}
