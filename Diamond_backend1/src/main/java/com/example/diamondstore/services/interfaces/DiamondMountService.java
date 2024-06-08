package com.example.diamondstore.services.interfaces;

import com.example.diamondstore.dto.MountDTO;
import com.example.diamondstore.entities.DiamondMount;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface DiamondMountService {
    List<DiamondMount> MountList();
    DiamondMount getMountById(int id);
    DiamondMount createDiamondMount(String mountName, float size, String type, String material, float basePrice);
    DiamondMount updateDiamondMount(MountDTO mountDTO, int id);
}
