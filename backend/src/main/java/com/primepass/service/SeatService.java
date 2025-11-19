package com.primepass.service;

import com.primepass.dao.SeatDao;
import com.primepass.model.Seat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SeatService {
    @Autowired
    private SeatDao seatDao;

    public List<Seat> getSeatsByShowtimeId(Integer showtimeId) {
        return seatDao.getSeatsByShowtimeId(showtimeId);
    }

    public void blockSeat(Integer showtimeId, Integer seatId) {
        seatDao.blockSeat(showtimeId, seatId);
    }

    public void unblockSeat(Integer showtimeId, Integer seatId) {
        seatDao.unblockSeat(showtimeId, seatId);
    }
}

