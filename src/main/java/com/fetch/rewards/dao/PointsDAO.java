package com.fetch.rewards.dao;

import com.fetch.rewards.dto.PointsDTO;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.*;

@Repository
public class PointsDAO {

    private final TreeMap<Timestamp, PointsDTO> SORTED_POINTS = new TreeMap<>();

    private final HashSet<String> payers = new HashSet<>();

    public PointsDAO() {}

    public void add(PointsDTO pointsDTO) {
        SORTED_POINTS.put(pointsDTO.getTimestamp(), pointsDTO);
        payers.add(pointsDTO.getPayer());
    }

    public void addAll(List<PointsDTO> pointsDTOS) {
        pointsDTOS.forEach(this::add);
    }

    public void remove(PointsDTO pointsDTO) {
        SORTED_POINTS.remove(pointsDTO.getTimestamp());
    }

    public void removeAll(List<PointsDTO> pointsDTOS) {
        pointsDTOS.forEach(this::remove);
    }

    public List<PointsDTO> getAll() {
        return new ArrayList<>(SORTED_POINTS.values());
    }

    public Set<String> getPayers() { return this.payers;}

}
