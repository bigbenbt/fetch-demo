package com.fetch.rewards.service;

import com.fetch.rewards.dao.PointsDAO;
import com.fetch.rewards.dto.BalanceDTO;
import com.fetch.rewards.dto.PointsDTO;
import com.fetch.rewards.exception.InsufficientPointsException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PointsService {

    private final PointsDAO pointsDAO;

    public PointsService(PointsDAO pointsDAO) {
        this.pointsDAO = pointsDAO;
    }

    public void addPoints(List<PointsDTO> pointsList) {
        pointsDAO.addAll(pointsList);
    }

    public List<BalanceDTO> getBalances() {
        List<PointsDTO> current = pointsDAO.getAll();
        List<BalanceDTO> balances = new ArrayList<>();
        //aggregate the point totals
        Map<String, Integer> groupedBalances = groupEntries(current);
        //now turn them into meaningful objects
        for (String payer : pointsDAO.getPayers()) {
            balances.add(BalanceDTO.builder().payer(payer).points(groupedBalances.getOrDefault(payer, 0)).build());
        }
        return balances;
    }

    public List<BalanceDTO> attemptPayment(Integer target) {

        Integer sum = 0;
        List<PointsDTO> spent = new ArrayList<>();
        List<PointsDTO> sortedPoints = pointsDAO.getAll();
        PointsDTO remainder = null;
        //iterate through the points, starting with the first one recorded
        for (PointsDTO pointsDTO : sortedPoints) {
            if (target <= sum + pointsDTO.getPoints()) {
                //the DTO we're working with will be sufficient to cover the balance, so log that information and create
                //the new updated balance object for the remainder
                Integer newPoints = pointsDTO.getPoints() - (target - sum);
                remainder = pointsDTO.toBuilder().points(newPoints).build();
                spent.add(pointsDTO.toBuilder().points(target - sum).build());
                sum=target;
            } else {
                //otherwise, we need to get more points.  Record this entry as spent and move onto the next
                sum += pointsDTO.getPoints();
                spent.add(pointsDTO);
            }
            //if we hit the target, we can just stop.  Break the loop, we're done with this bit
            if (sum >= target) {
                break;
            }
        }

        //entirely possible that the person won't have enough points to cover; throw an exception and don't update any records
        if (sum < target) {
            log.warn("Insufficient points to cover request.");
            throw new InsufficientPointsException("Insufficient points available");
        } else {
            log.info("Payment accepted, updating balances");
            //remove everything we spent, and then insert the remainder back into the datastore
            pointsDAO.removeAll(spent);
            if (remainder!=null) {
                pointsDAO.add(remainder);
            }
            return groupEntries(spent)
                    .entrySet()
                    .stream()
                    .map(e -> BalanceDTO.builder().points(-e.getValue()).payer(e.getKey()).build())
                    .collect(Collectors.toList());
        }
    }

    //convenience method, because I ended up needing it in two places
    private Map<String, Integer> groupEntries(List<PointsDTO> pointsDTOList) {
        return pointsDTOList.stream()
                .collect(Collectors.groupingBy(PointsDTO::getPayer, Collectors.summingInt(PointsDTO::getPoints)));
    }

}
