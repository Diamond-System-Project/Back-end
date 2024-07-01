package com.example.diamondstore.services;

import com.example.diamondstore.dto.MountDTO;
import com.example.diamondstore.entities.DiamondMount;
import com.example.diamondstore.repositories.DiamondMountRepository;
import com.example.diamondstore.services.interfaces.DiamondMountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class DiamondMountServiceImpl implements DiamondMountService {
    @Autowired
    private DiamondMountRepository diamondMountRepository;

    @Override
    public List<DiamondMount> MountList() {
        return diamondMountRepository.findAll();
    }

    @Override
    public DiamondMount getMountById(int id) {
        return diamondMountRepository.findDiamondMountByMountId(id);
    }

    @Override
    public DiamondMount createDiamondMount(String mountName, float size, String type, String material, BigDecimal basePrice) {
        DiamondMount saveMount = diamondMountRepository.save(DiamondMount.builder()
                        .mountName(mountName)
                        .size(size)
                        .type(type)
                        .material(material)
                        .basePrice(basePrice)
                        .build());
        return saveMount;
    }

    @Override
    public DiamondMount updateDiamondMount(MountDTO mountDTO, int id) {
        DiamondMount saveMount = getMountById(id);
        saveMount.setMountName(mountDTO.getMountName());
        saveMount.setSize(mountDTO.getSize());
        saveMount.setType(mountDTO.getType());
        saveMount.setMaterial(mountDTO.getMaterial());
        saveMount.setBasePrice(mountDTO.getBasePrice());

        return diamondMountRepository.save(saveMount);
    }

    @Override
    public List<DiamondMount> getDiamondMountsByType(String type) {
        return diamondMountRepository.findDiamondMountsByType(type);
    }

    @Override
    public List<String> getListMountType() {
        return diamondMountRepository.findDistinctTypes();
    }
}
