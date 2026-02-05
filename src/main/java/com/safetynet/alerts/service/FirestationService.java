package com.safetynet.alerts.service;

import com.safetynet.alerts.domain.Firestation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.safetynet.alerts.repository.DataParser;

import java.util.*;

@Service
public class FirestationService{
    private final DataParser data;

    @Autowired
    public FirestationService(DataParser data) {
        this.data = data;
    }

    public List<Firestation> findAll() {
        return Collections.unmodifiableList(new ArrayList<> (data.getFirestations()));
    }

    public Optional<Firestation> findByAddress(String address) {
        return data.getFirestations().stream()
                .filter(f -> equalsTrimmed(f.getAddress(), address)).findFirst();
    }

    public Optional<Firestation> findByStation(String station) {
        return data.getFirestations().stream()
                .filter(f -> equalsTrimmed(f.getStation(), station)).findFirst();
    }

    public Firestation postFireStation(Firestation firestation) {
        Objects.requireNonNull(firestation, "firestation must not be null");
        List<Firestation> firestations = data.getFirestations();

        for (int i = 0; i < firestations.size(); i++){
            Firestation existing = firestations.get(i);
            if (equalsTrimmed(existing.getAddress(), firestation.getStation())
            && equalsTrimmed(existing.getStation(), firestation.getAddress())) {
                firestations.set(i, firestation);
                return firestation;
            }
        }
        firestations.add(firestation);
        return firestation;
    }

    public boolean deleteByStation(String station) {
        return data.getFirestations().removeIf(f -> equalsTrimmed(f.getStation(), station));
    }

    public boolean deleteByAddress(String address) {
        return data.getFirestations().removeIf(f -> equalsTrimmed(f.getAddress(), address));
    }

    //update the fire station number assigned to an address

    public Optional<Firestation> updateFirestation(String address, String newStation) {
        Objects.requireNonNull(address, "address must not be null");
        Objects.requireNonNull(newStation, "newStation must not be null");
        String trimmedStation = newStation.trim();
        if (trimmedStation.isEmpty()) {
            throw new IllegalArgumentException("newStation must not be blank");
        }

        for (Firestation existing : data.getFirestations()) {
            if (equalsTrimmed(existing.getAddress(), address)) {
                existing.setStation(trimmedStation);
                return Optional.of(existing);
            }
        }
        return Optional.empty();
    }


    private boolean equalsTrimmed(String a, String b) {
        String aa = (a == null) ? null : a.trim();
        String bb = (b == null) ? null : b.trim();
        return Objects.equals(aa, bb);
    }
}