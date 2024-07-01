package com.example.diamondstore.services.interfaces;

import com.example.diamondstore.dto.DiamondDTO;
import com.example.diamondstore.dto.MountDTO;
import com.example.diamondstore.entities.Diamond;
import com.example.diamondstore.entities.DiamondMount;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public interface DiamondService {
    List<Diamond> DiamondList();
    Diamond getDiamondById(int id);
    Diamond createDiamond(DiamondDTO diamondDTO);
    Diamond updateDiamond(DiamondDTO diamondDTO, int id);
    List<BigDecimal> getTop20PriceByCaratWeight(float caratWeight);
}
