package com.fetch.rewards.service;

import com.fetch.rewards.dao.PointsDAO;
import com.fetch.rewards.dto.BalanceDTO;
import com.fetch.rewards.dto.PointsDTO;
import com.fetch.rewards.exception.InsufficientPointsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

class PointsServiceTest {

    @Mock
    PointsDAO pointsDAO;

    @InjectMocks
    PointsService pointsService;

    @BeforeEach
    void setUp() {
        initMocks(this);
    }

    @Test
    void getBalances() {
        PointsDTO points1 = PointsDTO.builder().payer("ben").points(100).timestamp(new Timestamp(1000)).build();
        PointsDTO points2 = PointsDTO.builder().payer("chad").points(100).timestamp(new Timestamp(1001)).build();
        PointsDTO points3 = PointsDTO.builder().payer("ben").points(-50).timestamp(new Timestamp(1002)).build();
        List<PointsDTO> pointsDTOList = Arrays.asList(points1, points2, points3);
        Set<String> payers = new HashSet<>(Arrays.asList("ben", "chad"));

        when(pointsDAO.getAll()).thenReturn(pointsDTOList);
        when(pointsDAO.getPayers()).thenReturn(payers);

        List<BalanceDTO> output = pointsService.getBalances();

        assertEquals(output.get(0).getPoints(), 100);
        assertEquals(output.get(0).getPayer(), "chad");
        assertEquals(output.get(1).getPoints(), 50);
        assertEquals(output.get(1).getPayer(), "ben");

    }

    @Test
    void testSpend__firstTransaction() {
        PointsDTO points1 = PointsDTO.builder().payer("ben").points(100).timestamp(new Timestamp(1000)).build();
        PointsDTO points2 = PointsDTO.builder().payer("chad").points(100).timestamp(new Timestamp(1001)).build();
        PointsDTO points3 = PointsDTO.builder().payer("ben").points(50).timestamp(new Timestamp(1002)).build();
        List<PointsDTO> pointsDTOList = Arrays.asList(points1, points2, points3);

        when(pointsDAO.getAll()).thenReturn(pointsDTOList);

        var output = pointsService.attemptPayment(50);

        assertThat(output.size(), equalTo(1));
        assertThat(output.get(0), hasProperty("payer", equalTo("ben")));
        assertThat(output.get(0), hasProperty("points", equalTo(-50)));
        verify(pointsDAO, times(1)).removeAll(any());
        verify(pointsDAO, times(1)).add(any());

    }

    @Test
    void testSpend__secondTransaction() {
        PointsDTO points1 = PointsDTO.builder().payer("ben").points(100).timestamp(new Timestamp(1000)).build();
        PointsDTO points2 = PointsDTO.builder().payer("chad").points(100).timestamp(new Timestamp(1001)).build();
        PointsDTO points3 = PointsDTO.builder().payer("ben").points(50).timestamp(new Timestamp(1002)).build();
        List<PointsDTO> pointsDTOList = Arrays.asList(points1, points2, points3);

        when(pointsDAO.getAll()).thenReturn(pointsDTOList);

        var output = pointsService.attemptPayment(120);

        assertThat(output.size(), equalTo(2));
        assertThat(output.get(0), hasProperty("payer", equalTo("chad")));
        assertThat(output.get(0), hasProperty("points", equalTo(-20)));
        assertThat(output.get(1), hasProperty("payer", equalTo("ben")));
        assertThat(output.get(1), hasProperty("points", equalTo(-100)));
        verify(pointsDAO, times(1)).removeAll(any());
        verify(pointsDAO, times(1)).add(any());
    }

    @Test
    void testSpend__paymentTooLarge() {
        PointsDTO points1 = PointsDTO.builder().payer("ben").points(100).timestamp(new Timestamp(1000)).build();
        PointsDTO points2 = PointsDTO.builder().payer("chad").points(100).timestamp(new Timestamp(1001)).build();
        PointsDTO points3 = PointsDTO.builder().payer("ben").points(-50).timestamp(new Timestamp(1002)).build();
        List<PointsDTO> pointsDTOList = Arrays.asList(points1, points2, points3);

        when(pointsDAO.getAll()).thenReturn(pointsDTOList);

        assertThrows(InsufficientPointsException.class, () -> {
            pointsService.attemptPayment(1000);
        });

        verify(pointsDAO, never()).removeAll(any());
        verify(pointsDAO, never()).remove(any());
        verify(pointsDAO, never()).add(any());
        verify(pointsDAO, never()).addAll(any());
    }
}