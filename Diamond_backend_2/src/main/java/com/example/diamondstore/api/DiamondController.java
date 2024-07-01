package com.example.diamondstore.api;

import com.example.diamondstore.dto.DiamondDTO;
import com.example.diamondstore.dto.MountDTO;
import com.example.diamondstore.entities.Diamond;
import com.example.diamondstore.entities.DiamondMount;
import com.example.diamondstore.response.ApiResponse;
import com.example.diamondstore.services.interfaces.DiamondService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/diamond")
@RequiredArgsConstructor
public class DiamondController {
    @Autowired
    private DiamondService diamondService;

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ROLE_Manager', 'ROLE_Sales Staff')")
    public ResponseEntity<ApiResponse> getAllDiamond() throws Exception {
        List<Diamond> diamondList = diamondService.DiamondList();
        if(diamondList.isEmpty()){
            return ResponseEntity.ok(ApiResponse.builder()
                    .success(false)
                    .message("List diamonds is empty!")
                    .build());
        }else{
            return ResponseEntity.ok(ApiResponse.builder()
                    .success(true)
                    .message("Get All Diamonds")
                    .data(diamondList)
                    .build());
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_Manager', 'ROLE_Sales Staff')")
    public ResponseEntity<ApiResponse> getDiamondId(@PathVariable int id) throws Exception {
        Diamond diamond = diamondService.getDiamondById(id);
        if(diamond != null){
            return ResponseEntity.ok(ApiResponse.builder()
                    .success(true)
                    .message("Get Diamond By ID Success")
                    .data(diamond)
                    .build());
        }else{
            return ResponseEntity.ok(ApiResponse.builder()
                    .success(false)
                    .message("Get Diamond By ID Fail")
                    .build());
        }
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ROLE_Manager')")
    public ResponseEntity<ApiResponse> createDiamond(@RequestBody DiamondDTO diamondDTO) throws Exception {
        try{
            Diamond diamond = diamondService.createDiamond(diamondDTO);
            return ResponseEntity.ok(ApiResponse.builder()
                    .success(true)
                    .message("Create diamond success!")
                    .data(diamond)
                    .build());

        }catch (Exception e){
            return ResponseEntity.ok(ApiResponse.builder()
                    .success(false)
                    .message("Create diamond fail! Error: " + e.getMessage())
                    .build());
        }
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasRole('ROLE_Manager')")
    public ResponseEntity<ApiResponse> updateDiamond(@RequestBody DiamondDTO diamondDTO, @PathVariable int id) {
        try{
            if(diamondService.getDiamondById(id) == null){
                return ResponseEntity.ok(ApiResponse.builder()
                        .success(false)
                        .message("Diamond Not Found!")
                        .build());
            } else {
                Diamond diamond = diamondService.updateDiamond(diamondDTO, id);
                return ResponseEntity.ok(ApiResponse.builder()
                        .success(true)
                        .message("Update diamond success!")
                        .data(diamond)
                        .build());
            }
        }catch (Exception e){
            return ResponseEntity.ok(ApiResponse.builder()
                    .success(false)
                    .message("Update diamond fail! Error: " + e.getMessage())
                    .build());
        }
    }

    @GetMapping("/list/prices")
    public ResponseEntity<ApiResponse> getPricesByCaratWeight(@RequestBody float caratWeight) throws Exception {
        List<BigDecimal> list = diamondService.getTop20PriceByCaratWeight(caratWeight);
        if(list.isEmpty()){
            return ResponseEntity.ok(ApiResponse.builder()
                    .success(false)
                    .message("List diamond prices is empty!")
                    .build());
        }else{
            return ResponseEntity.ok(ApiResponse.builder()
                    .success(true)
                    .message("Get List Diamond Prices")
                    .data(list)
                    .build());
        }
    }
}
